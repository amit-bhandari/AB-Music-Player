package com.music.player.bhandari.m.model;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright 2017 Amit Bhandari AB
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * @noinspection ConcatenationWithEmptyString
 */


//singleton class
//maintains music library

public class MusicLibrary {

    private final Context context;
    @SuppressLint("StaticFieldLeak")
    private static MusicLibrary musicLibrary = new MusicLibrary();
    private final ContentResolver cr;
    private final AtomicInteger atomicInt = new AtomicInteger();
    private int libraryLoadCounter;

    //artist photo urls for art library fragment only
    private static final HashMap<String, String> artistUrls = new HashMap<>();

    //short clip time
    private int SHORT_CLIPS_TIME_IN_MS;
    private String REMOVE_TRACK_CONTAINING_1, REMOVE_TRACK_CONTAINING_2, REMOVE_TRACK_CONTAINING_3;
    private String[] excludedFolders;

    //all the folders in which songs are there
    private final ArrayList<String> foldersList = new ArrayList<>();

    //data for all fragments
    private final Map<Integer, dataItem> dataItemsForTracks = Collections.synchronizedMap(new LinkedHashMap<>());
    private final ArrayList<dataItem> dataItemsForAlbums = new ArrayList<>();
    private final ArrayList<dataItem> dataItemsForGenres = new ArrayList<>();
    private final ArrayList<dataItem> dataItemsForArtists = new ArrayList<>();


    //track id to track name hashmap
    //used for shuffling tracks using track name in now playing
    private final SparseArray<String> trackMap = new SparseArray<>();

    private MusicLibrary() {
        this.context = MyApp.getContext();
        this.cr = context.getContentResolver();
        RefreshLibrary();
    }

    public void RefreshLibrary() {

        String excludedFoldersString = MyApp.getPref().getString(context.getString(R.string.pref_excluded_folders), "");
        excludedFolders = excludedFoldersString.split(",");

        //filter audio based on track duration
        SHORT_CLIPS_TIME_IN_MS = MyApp.getPref().getInt(context.getString(R.string.pref_hide_short_clips), 10) * 1000;

        //filter audio based on name
        REMOVE_TRACK_CONTAINING_1 = MyApp.getPref().getString(context.getString(R.string.pref_hide_tracks_starting_with_1), "");
        REMOVE_TRACK_CONTAINING_2 = MyApp.getPref().getString(context.getString(R.string.pref_hide_tracks_starting_with_2), "");
        REMOVE_TRACK_CONTAINING_3 = MyApp.getPref().getString(context.getString(R.string.pref_hide_tracks_starting_with_3), "");

        atomicInt.set(0);
        dataItemsForTracks.clear();
        dataItemsForGenres.clear();
        dataItemsForAlbums.clear();
        dataItemsForArtists.clear();

        Executors.newSingleThreadExecutor().execute(() -> {
            long start = System.currentTimeMillis();

            Log.v(Constants.TAG, "refresh started");
            fillDataForTracks();
            fillDataForAlbums();
            fillDataForArtist();
            fillDataForGenre();
            while (libraryLoadCounter != 4) {
                //Log.v(Constants.TAG,"waiting..");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            atomicInt.set(0);
            libraryLoadCounter = 0;
            Log.v(Constants.TAG, "refreshed");
            PlaylistManager.getInstance(MyApp.getContext()).PopulateUserMusicTable();
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Constants.ACTION.REFRESH_LIB));

            Log.v("See the time", (System.currentTimeMillis() - start) + "");
        });
    }

    private void fillFoldersList() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                foldersList.clear();
                for (dataItem item : dataItemsForTracks.values()) {
                    String path = item.file_path;
                    path = path.substring(0, path.lastIndexOf("/"));

                    boolean isExcluded = false;
                    //check if excluded folder
                    for (String excludedPath : excludedFolders) {
                        if (excludedPath.equals(path)) {
                            isExcluded = true;
                            break;
                        }
                    }
                    if (isExcluded) continue;

                    if (!foldersList.contains(path)) {
                        foldersList.add(path);
                    }
                }
            } catch (Exception ignored) {

            }
        });
    }

    public static MusicLibrary getInstance() {
        if (musicLibrary == null) {
            musicLibrary = new MusicLibrary();
        }
        return musicLibrary;
    }

    public ArrayList<Integer> getDefaultTracklistNew() {
        ArrayList<Integer> tracklist = new ArrayList<>();
        try {
            for (dataItem item : dataItemsForTracks.values()) {
                tracklist.add(item.id);
            }
            return tracklist;
        } catch (Exception ignored) {
            return tracklist;
        }
    }

    public ArrayList<String> getFoldersList() {
        return foldersList;
    }

    @SuppressLint("Range")
    private void fillDataForTracks() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST_ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.YEAR,
                    MediaStore.Audio.Media.TRACK
            };
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
            Cursor cursor = null;
            try {
                cursor = cr.query(uri, projection, selection, null, sortOrder);
            } catch (Exception ignored) {
            }
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {

                    if (!REMOVE_TRACK_CONTAINING_1.equals("")
                            && cursor.getString(INDEX_FOR_TRACK_CURSOR.TITLE).startsWith(REMOVE_TRACK_CONTAINING_1)) {
                        continue;
                    }
                    if (!REMOVE_TRACK_CONTAINING_2.equals("")
                            && cursor.getString(INDEX_FOR_TRACK_CURSOR.TITLE).startsWith(REMOVE_TRACK_CONTAINING_2)) {
                        continue;
                    }
                    if (!REMOVE_TRACK_CONTAINING_3.equals("")
                            && cursor.getString(INDEX_FOR_TRACK_CURSOR.TITLE).startsWith(REMOVE_TRACK_CONTAINING_3)) {
                        continue;
                    }

                    String filePath = cursor.getString(6);
                    if (filePath != null) {
                        String folderPath = filePath.substring(0, filePath.lastIndexOf("/"));

                        boolean isExcluded = false;
                        for (String excludedPath : excludedFolders) {
                            if (folderPath.equals(excludedPath)) {
                                isExcluded = true;
                                break;
                            }
                        }
                        if (isExcluded) continue;
                    }

                    if (cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) > SHORT_CLIPS_TIME_IN_MS) {
                        dataItemsForTracks.put(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                                new dataItem(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                                        , cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                                        , cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID))
                                        , cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                                        , cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                                        , cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                                        , cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR))
                                        , cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                                        , cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                                        , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)))
                        );

                        trackMap.put(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                                , cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }

            libraryLoadCounter = atomicInt.incrementAndGet();
            fillFoldersList();
        });
    }

    @SuppressLint("Range")
    private void fillDataForArtist() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String[] mProjection =
                    {
                            MediaStore.Audio.Artists._ID,
                            MediaStore.Audio.Artists.ARTIST,
                            MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
                    };
            Cursor cursor = null;
            try {
                cursor = cr.query(
                        MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                        mProjection,
                        null,
                        null,
                        MediaStore.Audio.Artists.ARTIST + " ASC");
            } catch (Exception ignored) {

            }
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    dataItemsForArtists.add(new dataItem(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Artists._ID))
                            , cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST))
                            , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))
                            , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS))
                    ));
                }
            }
            if (cursor != null) {
                cursor.close();
            }


            updateArtistInfo();
            libraryLoadCounter = atomicInt.incrementAndGet();
        });

    }

    @SuppressLint("Range")
    private void fillDataForAlbums() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String[] mProjection =
                    {
                            MediaStore.Audio.Albums._ID,
                            MediaStore.Audio.Albums.ALBUM,
                            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
                            MediaStore.Audio.Albums.ARTIST,
                            MediaStore.Audio.Albums.FIRST_YEAR,
                            MediaStore.Audio.Albums.ALBUM_ART,
                            MediaStore.Audio.Media.ARTIST_ID,
                            MediaStore.Audio.Media.ALBUM
                    };
            Cursor cursor = null;
            try {
                cursor = cr.query(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        mProjection,
                        null,
                        null,
                        MediaStore.Audio.Albums.ALBUM + " ASC");
            } catch (Exception ignored) {
            }
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    dataItemsForAlbums.add(new dataItem(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Albums._ID))
                            , cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM))
                            , cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST))
                            , cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
                            , cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR))
                            , cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID))
                    ));
                }
                cursor.close();
            }

            libraryLoadCounter = atomicInt.incrementAndGet();
        });

    }

    @SuppressLint("Range")
    private void fillDataForGenre() {
        Executors.newSingleThreadExecutor().execute(() -> {
            String[] mProjection =
                    {
                            MediaStore.Audio.Genres._ID,
                            MediaStore.Audio.Genres.NAME
                    };
            Cursor cursor = null;
            try {
                cursor = cr.query(
                        MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                        mProjection,
                        null,
                        null,
                        MediaStore.Audio.Genres.NAME + " ASC");
            } catch (Exception ignored) {
            }
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    ArrayList<Integer> songList = getSongListFromGenreIdNew(cursor.getInt(INDEX_FOR_GENRE_CURSOR._ID)
                            , Constants.SORT_ORDER.ASC);
                    if (songList == null || songList.size() == 0)
                        continue;

                    String genre_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Genres.NAME));
                    if (genre_name == null) continue;
                    dataItemsForGenres.add(new dataItem(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Genres._ID))
                            , genre_name
                            , 0)
                    );
                }
                cursor.close();
            }
            libraryLoadCounter = atomicInt.incrementAndGet();
        });

    }

    public dataItem updateTrackNew(int id, String... param) {
        dataItem d = dataItemsForTracks.get(id);
        if (d != null) {
            d.title = param[0];
            d.artist_name = param[1];
            d.albumName = param[2];
            return d;
        }
        return null;
    }

    public ArrayList<dataItem> getDataItemsForAlbums() {
        return dataItemsForAlbums;
    }

    public ArrayList<dataItem> getDataItemsArtist() {
        return dataItemsForArtists;
    }

    public Map<Integer, dataItem> getDataItemsForTracks() {
        return dataItemsForTracks;
    }

    public ArrayList<dataItem> getDataItemsForGenres() {
        return dataItemsForGenres;
    }

    public SparseArray<String> getTrackMap() {
        return trackMap;
    }

    public ArrayList<Integer> getSongListFromArtistIdNew(long artist_id, int sort) {
        ArrayList<Integer> songList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0" + " AND " + MediaStore.Audio.Media.ARTIST_ID + "=" + artist_id;
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID
        };
        String sortOrder;
        if (sort == Constants.SORT_ORDER.ASC) {
            sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        } else {
            sortOrder = MediaStore.Audio.Media.TITLE + " DESC";
        }
        Cursor cursor = null;
        try {
            cursor = cr.query(uri, projection, selection, null, sortOrder);
        } catch (Exception ignored) {
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(1) > SHORT_CLIPS_TIME_IN_MS) {
                    songList.add(cursor.getInt(2));
                }
            }
            cursor.close();
            return songList;
        }
        return null;
    }

    public ArrayList<Integer> getSongListFromAlbumIdNew(long album_id, int sort) {
        System.out.println("AMIT getSongListFromAlbumIdNew " + album_id + " " + sort);
        ArrayList<Integer> songList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0" + " AND " + MediaStore.Audio.Media.ALBUM_ID + "=" + album_id;
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID
        };
        String sortOrder;
        if (sort == Constants.SORT_ORDER.ASC) {
            sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        } else {
            sortOrder = MediaStore.Audio.Media.TITLE + " DESC";
        }
        Cursor cursor = null;
        try {
            cursor = cr.query(uri, projection, selection, null, sortOrder);
        } catch (Exception ignored) {
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(1) > SHORT_CLIPS_TIME_IN_MS) {
                    songList.add(cursor.getInt(2));
                }
            }
            cursor.close();
            return songList;
        }
        return null;
    }

    public ArrayList<Integer> getSongListFromGenreIdNew(long genre_id, int sort) {
        ArrayList<Integer> songList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", genre_id);
        String[] projection = new String[]{MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID};
        String sortOrder;
        if (sort == Constants.SORT_ORDER.ASC) {
            sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        } else {
            sortOrder = MediaStore.Audio.Media.TITLE + " DESC";
        }
        Cursor cursor = null;
        try {
            cursor = cr.query(uri, projection, null, null, sortOrder);
        } catch (Exception ignored) {
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(1) > SHORT_CLIPS_TIME_IN_MS) {
                    songList.add(cursor.getInt(2));
                }
            }
            cursor.close();
            return songList;
        }
        return null;
    }

    public TrackItem getTrackItemFromTitle(String title) {

        if (title.contains("'")) {
            //title = ((char)34+title+(char)34);
            //fuck you bug
            //you bugged my mind
            title = title.replaceAll("'", "''");
        }
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0" + " AND "
                + MediaStore.Audio.Media.TITLE + "= '" + title + "'";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST_ID
        };
        Cursor cursor = null;
        try {
            cursor = cr.query(
                    uri,
                    projection,
                    selection,
                    null,
                    MediaStore.Audio.Media.TITLE + " ASC");
        } catch (Exception ignored) {
        }
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            @SuppressLint("Range") TrackItem item = new TrackItem(cursor.getString(INDEX_FOR_TRACK_CURSOR.DATA_PATH),
                    cursor.getString(INDEX_FOR_TRACK_CURSOR.TITLE),
                    cursor.getString(INDEX_FOR_TRACK_CURSOR.ARTIST),
                    cursor.getString(INDEX_FOR_TRACK_CURSOR.ALBUM),
                    "",
                    cursor.getString(INDEX_FOR_TRACK_CURSOR.DURATION),
                    cursor.getLong(INDEX_FOR_TRACK_CURSOR.ALBUM_ID),
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)),
                    cursor.getInt(INDEX_FOR_TRACK_CURSOR._ID));
            cursor.close();
            return item;
        }
        return null;
    }

    public TrackItem getTrackItemFromId(int _id) {

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0" + " AND "
                + MediaStore.Audio.Media._ID + "= '" + _id + "'";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST_ID
        };
        Cursor cursor = null;
        try {
            cursor = cr.query(
                    uri,
                    projection,
                    selection,
                    null,
                    MediaStore.Audio.Media.TITLE + " ASC");
        } catch (Exception ignored) {
        }
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            @SuppressLint("Range") TrackItem item = new TrackItem(cursor.getString(INDEX_FOR_TRACK_CURSOR.DATA_PATH),
                    cursor.getString(INDEX_FOR_TRACK_CURSOR.TITLE),
                    cursor.getString(INDEX_FOR_TRACK_CURSOR.ARTIST),
                    cursor.getString(INDEX_FOR_TRACK_CURSOR.ALBUM),
                    "",
                    cursor.getString(INDEX_FOR_TRACK_CURSOR.DURATION),
                    cursor.getLong(INDEX_FOR_TRACK_CURSOR.ALBUM_ID),
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)),
                    cursor.getInt(INDEX_FOR_TRACK_CURSOR._ID));
            cursor.close();
            return item;
        }
        return null;
    }

    public int getIdFromFilePath(String filePath) {
        if (filePath.contains("\"")) {
            filePath = UtilityFun.escapeDoubleQuotes(filePath);
        }
        Uri videosUri = MediaStore.Audio.Media.getContentUri("external");
        String[] projection = {MediaStore.Audio.Media._ID};
        Cursor cursor = null;
        try {
            cursor = cr.query(videosUri, projection, MediaStore.Audio.Media.DATA + " LIKE ?", new String[]{filePath}, null);
        } catch (Exception ignored) {
        }

        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            int id;
            try {
                id = cursor.getInt(0);
            } catch (Exception e) {
                cursor.close();
                return -1;
            }
            cursor.close();
            return id;
        } else {
            return -1;
        }
    }

    public Bitmap getAlbumArtFromId(int id) {

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0" + " AND "
                + MediaStore.Audio.Media._ID + "=" + "'" + id + "'";
        String[] projection = {
                MediaStore.Audio.Media.ALBUM_ID
        };
        Cursor cursor = null;
        try {
            cursor = cr.query(
                    uri,
                    projection,
                    selection,
                    null,
                    MediaStore.Audio.Media.TITLE + " ASC");
        } catch (Exception ignored) {
        }
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            int album_id = cursor.getInt(0);
            Bitmap bm = null;
            try {
                final Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");

                uri = ContentUris.withAppendedId(sArtworkUri, album_id);

                //noinspection resource
                ParcelFileDescriptor pfd = cr.openFileDescriptor(uri, "r");

                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } catch (Exception e) {
                cursor.close();
            }

            cursor.close();
            if (bm != null) {
                return bm;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    public Uri getAlbumArtUri(long album_id) {
        Uri songCover = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(songCover, album_id);
    }

    interface INDEX_FOR_TRACK_CURSOR {
        int _ID = 0;
        int TITLE = 1;
        int DATA_PATH = 2;
        int ARTIST = 3;
        int ALBUM = 4;
        int DURATION = 5;
        int ALBUM_ID = 6;

    }

    interface INDEX_FOR_GENRE_CURSOR {
        int _ID = 0;
    }

    private synchronized void updateArtistInfo() {
        artistUrls.clear();
        artistUrls.putAll(OfflineStorageArtistBio.getArtistImageUrls());
    }

    public synchronized void putEntryInArtistUrl(String artist, String link) {
        artistUrls.put(artist, link);
    }

    public @NonNull HashMap<String, String> getArtistUrls() {
        return artistUrls;
    }

}


