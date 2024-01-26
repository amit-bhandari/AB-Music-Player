package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.model.dataItem;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo;
import com.music.player.bhandari.m.MyApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.concurrent.Executors;

/**
 * Copyright 2017 Amit Bhandari AB
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class OfflineStorageArtistBio {

    public static ArtistInfo getArtistBioFromTrackItem(TrackItem item) {
        if (item == null) {
            return null;
        }
        ArtistInfo artistInfo = null;
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            DbHelperArtistBio dbHelperArtistBio = new DbHelperArtistBio(MyApp.getContext());
            db = dbHelperArtistBio.getReadableDatabase();
            dbHelperArtistBio.onCreate(db);

            String where = DbHelperArtistBio.ARTIST_ID + " = " + item.getArtist_id()
                    + " OR " + DbHelperArtistBio.KEY_ARTIST + "= '" + item.getArtist().replace("'", "''") + "'";
            ;

            cursor = db.query(DbHelperArtistBio.TABLE_NAME, new String[]{DbHelperArtistBio.ARTIST_BIO}
                    , where, null, null, null, null, "1");

            if (cursor != null && cursor.getCount() != 0) {
                cursor.moveToFirst();
                //retrieve and fill lyrics object
                Gson gson = new Gson();
                artistInfo = gson.fromJson(cursor.getString
                        (cursor.getColumnIndex(DbHelperArtistBio.ARTIST_BIO)), ArtistInfo.class);
            }

        } catch (Exception e) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return artistInfo;
    }

    public static void putArtistBioToDB(ArtistInfo artistInfo, TrackItem item) {
        if (item == null || artistInfo == null) {
            return;
        }
        Cursor cursor = null;
        SQLiteDatabase db = null;
        try {
            DbHelperArtistBio dbHelperArtistBio = new DbHelperArtistBio(MyApp.getContext());
            db = dbHelperArtistBio.getWritableDatabase();
            dbHelperArtistBio.onCreate(db);

            //check if already exists, if yes, return
            String where = DbHelperArtistBio.ARTIST_ID + " = " + item.getArtist_id()
                    + " OR " + DbHelperArtistBio.KEY_ARTIST + "= '" + item.getArtist().replace("'", "''") + "'";
            ;

            cursor = db.query(DbHelperArtistBio.TABLE_NAME, new String[]{DbHelperArtistBio.KEY_ARTIST}
                    , where, null, null, null, null, "1");
            if (cursor != null && cursor.getCount() > 0) {
                cursor.close();
                return;
            }

            //convert lyrics to json
            Gson gson = new Gson();
            String jsonInString = gson.toJson(artistInfo);

            ContentValues c = new ContentValues();
            c.put(DbHelperArtistBio.ARTIST_BIO, jsonInString);
            c.put(DbHelperArtistBio.KEY_ARTIST, item.getArtist());
            c.put(DbHelperArtistBio.ARTIST_ID, item.getArtist_id());
            db.insert(DbHelperArtistBio.TABLE_NAME, null, c);
        } catch (Exception ignored) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    public static void putArtistInfoToCache(final ArtistInfo artistInfo) {
        //don't care about exception.
        //
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String CACHE_ART_INFO = MyApp.getContext().getCacheDir() + "/artistInfo/";
                    String actual_file_path = CACHE_ART_INFO + artistInfo.getCorrectedArtist();

                    if (new File(actual_file_path).exists()) {
                        return;
                    }

                    File f = new File(CACHE_ART_INFO);
                    if (!f.exists()) {
                        f.mkdir();
                    }
                    ObjectOutput out;
                    out = new ObjectOutputStream(new FileOutputStream(actual_file_path));
                    out.writeObject(artistInfo);
                    out.close();
                    Log.v("Amit AB", "saved artist to cache");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static ArtistInfo getArtistInfoFromCache(String artist) {
        String CACHE_ART_INFO = MyApp.getContext().getCacheDir() + "/artistInfo/";
        String actual_file_path = CACHE_ART_INFO + artist;
        ObjectInputStream in;
        ArtistInfo artistInfo = null;
        try {
            FileInputStream fileIn = new FileInputStream(actual_file_path);
            in = new ObjectInputStream(fileIn);
            artistInfo = (ArtistInfo) in.readObject();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (artistInfo != null) {
            Log.v("Amit AB", "got from cache" + artistInfo.getOriginalArtist());
        }
        return artistInfo;
    }

    public static HashMap<String, String> getArtistImageUrls() {
        HashMap<String, String> map = new HashMap<>();
        try {
            ArrayList<dataItem> artistItems = new ArrayList<>(MusicLibrary.getInstance().getDataItemsArtist());
            for (dataItem item : artistItems) {
                TrackItem trackItem = new TrackItem();
                trackItem.setArtist(item.artist_name);
                trackItem.setArtist_id(item.artist_id);

                ArtistInfo artistInfo = OfflineStorageArtistBio.getArtistBioFromTrackItem(trackItem);
                if (artistInfo != null && !artistInfo.getCorrectedArtist().equals("[unknown]")) {
                    map.put(artistInfo.getOriginalArtist(), artistInfo.getImageUrl());
                }
            }
        } catch (ConcurrentModificationException e) {
            return map;
        }
        return map;
    }
}
