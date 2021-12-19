package com.music.player.bhandari.m.model

import android.content.Context
import android.util.Size
import com.music.player.bhandari.m.BuildConfig
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.BulkArtInfoGrabber
import java.util.concurrent.Executors

/**
 * Copyright 2017 Amit Bhandari AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//singleton class
//maintains music library
class MusicLibrary private constructor() {
    private val context: Context
    private val cr: ContentResolver
    private val atomicInt: AtomicInteger = AtomicInteger()
    private var libraryLoadCounter = 0

    //short clip time
    private var SHORT_CLIPS_TIME_IN_MS = 0
    private var REMOVE_TRACK_CONTAINING_1: String? = null
    private var REMOVE_TRACK_CONTAINING_2: String? = null
    private var REMOVE_TRACK_CONTAINING_3: String? = null
    private var excludedFolders: Array<String>

    //all the folders in which songs are there
    val foldersList = ArrayList<String>()

    //data for all frgaments
    private val dataItemsForTracks: MutableMap<Int, dataItem>? =
        Collections.synchronizedMap(LinkedHashMap<Int, dataItem>())
    private val dataItemsForAlbums: ArrayList<dataItem> = ArrayList<dataItem>()
    private val dataItemsForGenres: ArrayList<dataItem> = ArrayList<dataItem>()
    private val dataItemsForArtists: ArrayList<dataItem> = ArrayList<dataItem>()

    //track id to track name hashmap
    //used for shuffling tracks using track name in now playing
    private val trackMap: SparseArray<String> = SparseArray<String>()
    fun RefreshLibrary() {
        val excludedFoldersString: String =
            MyApp.getPref().getString(context.getString(R.string.pref_excluded_folders), "")
        excludedFolders = excludedFoldersString.split(",".toRegex()).toTypedArray()

        //filter audio based on track duration
        SHORT_CLIPS_TIME_IN_MS =
            MyApp.getPref().getInt(context.getString(R.string.pref_hide_short_clips), 10) * 1000

        //filter audio based on name
        REMOVE_TRACK_CONTAINING_1 = MyApp.getPref()
            .getString(context.getString(R.string.pref_hide_tracks_starting_with_1), "")
        REMOVE_TRACK_CONTAINING_2 = MyApp.getPref()
            .getString(context.getString(R.string.pref_hide_tracks_starting_with_2), "")
        REMOVE_TRACK_CONTAINING_3 = MyApp.getPref()
            .getString(context.getString(R.string.pref_hide_tracks_starting_with_3), "")
        atomicInt.set(0)
        dataItemsForTracks!!.clear()
        dataItemsForGenres.clear()
        dataItemsForAlbums.clear()
        dataItemsForArtists.clear()
        Executors.newSingleThreadExecutor().execute {
            val start = System.currentTimeMillis()
            Log.v(Constants.TAG, "refresh started")
            fillDataForTracks()
            fillDataForAlbums()
            fillDataForArtist()
            fillDataForGenre()
            while (libraryLoadCounter != 4) {
                //Log.v(Constants.TAG,"waiting..");
                try {
                    Thread.sleep(200)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            atomicInt.set(0)
            libraryLoadCounter = 0
            Log.v(Constants.TAG, "refreshed")
            PlaylistManager.getInstance(MyApp.getContext())!!.PopulateUserMusicTable()
            LocalBroadcastManager.getInstance(context)
                .sendBroadcast(Intent(Constants.ACTION.REFRESH_LIB))
            Log.v("See the time", (System.currentTimeMillis() - start).toString() + "")
        }
    }

    private fun fillFoldersList() {
        Executors.newSingleThreadExecutor().execute {
            try {
                foldersList.clear()
                for (item in dataItemsForTracks!!.values) {
                    var path: String = item.file_path
                    path = path.substring(0, path.lastIndexOf("/"))
                    var isExcluded = false
                    //check if excluded folder
                    for (excludedPath in excludedFolders) {
                        if (excludedPath == path) {
                            isExcluded = true
                        }
                    }
                    if (isExcluded) continue
                    if (!foldersList.contains(path)) {
                        foldersList.add(path)
                    }
                }
            } catch (ignored: Exception) {
            }
        }
    }

    val defaultTracklistNew: ArrayList<Int>
        get() {
            val tracklist = ArrayList<Int>()
            return try {
                if (dataItemsForTracks != null) {
                    for (item in dataItemsForTracks.values) {
                        tracklist.add(item.id)
                    }
                }
                tracklist
            } catch (ignored: Exception) {
                tracklist
            }
        }

    private fun fillDataForTracks() {
        Executors.newSingleThreadExecutor().execute {
            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val selection: String = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
            val projection = arrayOf<String>(
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
            )
            val sortOrder: String = MediaStore.Audio.Media.TITLE + " ASC"
            var cursor: Cursor? = null
            try {
                cursor = cr.query(uri, projection, selection, null, sortOrder)
            } catch (ignored: Exception) {
            }
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    if (REMOVE_TRACK_CONTAINING_1 != ""
                        && cursor.getString(INDEX_FOR_TRACK_CURSOR.TITLE)
                            .startsWith(REMOVE_TRACK_CONTAINING_1)
                    ) {
                        continue
                    }
                    if (REMOVE_TRACK_CONTAINING_2 != ""
                        && cursor.getString(INDEX_FOR_TRACK_CURSOR.TITLE)
                            .startsWith(REMOVE_TRACK_CONTAINING_2)
                    ) {
                        continue
                    }
                    if (REMOVE_TRACK_CONTAINING_3 != ""
                        && cursor.getString(INDEX_FOR_TRACK_CURSOR.TITLE)
                            .startsWith(REMOVE_TRACK_CONTAINING_3)
                    ) {
                        continue
                    }
                    val filePath: String = cursor.getString(6)
                    if (filePath != null) {
                        val folderPath = filePath.substring(0, filePath.lastIndexOf("/"))
                        var isExcluded = false
                        for (excludedPath in excludedFolders) {
                            if (folderPath == excludedPath) isExcluded = true
                        }
                        if (isExcluded) continue
                    }

                    /*System.out.println("Track number "
                                    + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                                    + " " + cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)));*/if (cursor.getInt(
                            cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) > SHORT_CLIPS_TIME_IN_MS
                    ) {
                        dataItemsForTracks!![cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID))] =
                            dataItem(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)),
                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)),
                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)),
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)))
                        trackMap.put(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)))
                    }
                }
            }
            if (cursor != null) {
                cursor.close()
            }
            libraryLoadCounter = atomicInt.incrementAndGet()
            fillFoldersList()
        }
    }

    private fun fillDataForArtist() {
        Executors.newSingleThreadExecutor().execute {
            val mProjection = arrayOf<String>(
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
            )
            var cursor: Cursor? = null
            try {
                cursor = cr.query(
                    MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                    mProjection,
                    null,
                    null,
                    MediaStore.Audio.Artists.ARTIST + " ASC")
            } catch (ignored: Exception) {
            }
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    dataItemsForArtists.add(dataItem(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists._ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS))
                    ))
                }
            }
            if (cursor != null) {
                cursor.close()
            }
            updateArtistInfo()
            libraryLoadCounter = atomicInt.incrementAndGet()
            if (!BuildConfig.DEBUG) {
                //if its been more than 2 days since artist info has been cached locally, do it
                //fetch art info thread
                val lastTimeDidAt: Long =
                    MyApp.getPref().getLong(context.getString(R.string.pref_artinfo_libload), 0)
                if (System.currentTimeMillis() >= lastTimeDidAt +
                    2 * 24 * 60 * 60 * 1000
                ) {
                    BulkArtInfoGrabber().start()
                }
            }
        }
    }

    private fun fillDataForAlbums() {
        Executors.newSingleThreadExecutor().execute {
            val mProjection = arrayOf<String>(
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.FIRST_YEAR,
                MediaStore.Audio.Albums.ALBUM_ART,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ALBUM
            )
            var cursor: Cursor? = null
            try {
                cursor = cr.query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    mProjection,
                    null,
                    null,
                    MediaStore.Audio.Albums.ALBUM + " ASC")
            } catch (ignored: Exception) {
                println(ignored)
            }
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    dataItemsForAlbums.add(dataItem(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums._ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID))
                    ))

                    /*Log.d("MusicLibrary", "album : " + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM))
                             + " Folder path : " + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));*/
                }
                cursor.close()
            }
            libraryLoadCounter = atomicInt.incrementAndGet()
        }
    }

    private fun fillDataForGenre() {
        Executors.newSingleThreadExecutor().execute {
            val mProjection = arrayOf<String>(
                MediaStore.Audio.Genres._ID,
                MediaStore.Audio.Genres.NAME
            )
            var cursor: Cursor? = null
            try {
                cursor = cr.query(
                    MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                    mProjection,
                    null,
                    null,
                    MediaStore.Audio.Genres.NAME + " ASC")
            } catch (ignored: Exception) {
            }
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    val songList =
                        getSongListFromGenreIdNew(cursor.getInt(INDEX_FOR_GENRE_CURSOR._ID),
                            Constants.SORT_ORDER.ASC)
                    if (songList == null || songList.size == 0) continue
                    val genre_name: String =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Genres.NAME))
                            ?: continue
                    dataItemsForGenres.add(dataItem(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Genres._ID)),
                        genre_name,
                        0)
                    )
                }
                cursor.close()
            }
            libraryLoadCounter = atomicInt.incrementAndGet()
        }
    }

    fun updateTrackNew(id: Int, vararg param: String): dataItem? {
        val d: dataItem? = dataItemsForTracks!![id]
        if (d != null) {
            d.title = param[0]
            d.artist_name = param[1]
            d.albumName = param[2]
            return d
        }
        return null
    }

    fun getDataItemsForAlbums(): ArrayList<dataItem> {
        return dataItemsForAlbums
    }

    val dataItemsArtist: ArrayList<com.music.player.bhandari.m.model.dataItem>
        get() = dataItemsForArtists

    fun getDataItemsForTracks(): Map<Int, dataItem>? {
        return dataItemsForTracks
    }

    fun getDataItemsForGenres(): ArrayList<dataItem> {
        return dataItemsForGenres
    }

    fun getTrackMap(): SparseArray<String> {
        return trackMap
    }

    fun getSongListFromArtistIdNew(artist_id: Int, sort: Int): ArrayList<Int>? {
        val songList = ArrayList<Int>()
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection: String =
            MediaStore.Audio.Media.IS_MUSIC + "!= 0" + " AND " + MediaStore.Audio.Media.ARTIST_ID + "=" + artist_id
        val projection = arrayOf<String>(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media._ID
        )
        var sortOrder = ""
        sortOrder = if (sort == Constants.SORT_ORDER.ASC) {
            MediaStore.Audio.Media.TITLE + " ASC"
        } else {
            MediaStore.Audio.Media.TITLE + " DESC"
        }
        var cursor: Cursor? = null
        try {
            cursor = cr.query(uri, projection, selection, null, sortOrder)
        } catch (ignored: Exception) {
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(1) > SHORT_CLIPS_TIME_IN_MS) {
                    songList.add(cursor.getInt(2))
                }
            }
            cursor.close()
            return songList
        }
        return null
    }

    fun getSongListFromAlbumIdNew(album_id: Int, sort: Int): ArrayList<Int>? {
        val songList = ArrayList<Int>()
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection: String =
            MediaStore.Audio.Media.IS_MUSIC + "!= 0" + " AND " + MediaStore.Audio.Media.ALBUM_ID + "=" + album_id
        val projection = arrayOf<String>(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media._ID
        )
        var sortOrder = ""
        sortOrder = if (sort == Constants.SORT_ORDER.ASC) {
            MediaStore.Audio.Media.TITLE + " ASC"
        } else {
            MediaStore.Audio.Media.TITLE + " DESC"
        }
        var cursor: Cursor? = null
        try {
            cursor = cr.query(uri, projection, selection, null, sortOrder)
        } catch (ignored: Exception) {
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(1) > SHORT_CLIPS_TIME_IN_MS) {
                    songList.add(cursor.getInt(2))
                }
            }
            cursor.close()
            return songList
        }
        return null
    }

    fun getSongListFromGenreIdNew(genre_id: Int, sort: Int): ArrayList<Int>? {
        val songList = ArrayList<Int>()
        val uri: Uri = MediaStore.Audio.Genres.Members.getContentUri("external", genre_id.toLong())
        val projection = arrayOf<String>(MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media._ID)
        var sortOrder = ""
        sortOrder = if (sort == Constants.SORT_ORDER.ASC) {
            MediaStore.Audio.Media.TITLE + " ASC"
        } else {
            MediaStore.Audio.Media.TITLE + " DESC"
        }
        var cursor: Cursor? = null
        try {
            cursor = cr.query(uri, projection, null, null, sortOrder)
        } catch (ignored: Exception) {
        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(1) > SHORT_CLIPS_TIME_IN_MS) {
                    songList.add(cursor.getInt(2))
                }
            }
            cursor.close()
            return songList
        }
        return null
    }

    fun getTrackItemFromTitle(title: String): TrackItem? {
        var title = title
        if (title.contains("'")) {
            //title = ((char)34+title+(char)34);
            //fuck you bug
            //you bugged my mind
            title = title.replace("'".toRegex(), "''")
        }
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection: String = (MediaStore.Audio.Media.IS_MUSIC + "!= 0" + " AND "
                + MediaStore.Audio.Media.TITLE + "= '" + title + "'")
        val projection = arrayOf<String>(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID
        )
        var cursor: Cursor? = null
        try {
            cursor = cr.query(
                uri,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.TITLE + " ASC")
        } catch (ignored: Exception) {
        }
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst()
            val item = TrackItem(cursor.getString(INDEX_FOR_TRACK_CURSOR.DATA_PATH),
                cursor.getString(INDEX_FOR_TRACK_CURSOR.TITLE),
                cursor.getString(INDEX_FOR_TRACK_CURSOR.ARTIST),
                cursor.getString(INDEX_FOR_TRACK_CURSOR.ALBUM),
                "",
                cursor.getString(INDEX_FOR_TRACK_CURSOR.DURATION),
                cursor.getInt(INDEX_FOR_TRACK_CURSOR.ALBUM_ID),
                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)),
                cursor.getInt(INDEX_FOR_TRACK_CURSOR._ID))
            cursor.close()
            return item
        }
        return null
    }

    fun getTrackItemFromId(_id: Int): TrackItem? {
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection: String = (MediaStore.Audio.Media.IS_MUSIC + "!= 0" + " AND "
                + MediaStore.Audio.Media._ID + "= '" + _id + "'")
        val projection = arrayOf<String>(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID
        )
        var cursor: Cursor? = null
        try {
            cursor = cr.query(
                uri,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.TITLE + " ASC")
        } catch (ignored: Exception) {
        }
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst()
            val item = TrackItem(cursor.getString(INDEX_FOR_TRACK_CURSOR.DATA_PATH),
                cursor.getString(INDEX_FOR_TRACK_CURSOR.TITLE),
                cursor.getString(INDEX_FOR_TRACK_CURSOR.ARTIST),
                cursor.getString(INDEX_FOR_TRACK_CURSOR.ALBUM),
                "",
                cursor.getString(INDEX_FOR_TRACK_CURSOR.DURATION),
                cursor.getInt(INDEX_FOR_TRACK_CURSOR.ALBUM_ID),
                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)),
                cursor.getInt(INDEX_FOR_TRACK_CURSOR._ID))
            cursor.close()
            return item
        }
        return null
    }

    fun getIdFromFilePath(filePath: String): Int {
        var filePath = filePath
        if (filePath.contains("\"")) {
            filePath = UtilityFun.escapeDoubleQuotes(filePath)
        }
        val videosUri: Uri = MediaStore.Audio.Media.getContentUri("external")
        val projection = arrayOf<String>(MediaStore.Audio.Media._ID)
        var cursor: Cursor? = null
        try {
            cursor = cr.query(videosUri,
                projection,
                MediaStore.Audio.Media.DATA + " LIKE ?",
                arrayOf(filePath),
                null)
        } catch (ignored: Exception) {
        }
        return if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst()
            val id: Int
            id = try {
                cursor.getInt(0)
            } catch (e: Exception) {
                cursor.close()
                return -1
            }
            cursor.close()
            id
        } else {
            -1
        }
    }

    fun getAlbumArtFromId(id: Int): Bitmap? {
        var uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection: String = (MediaStore.Audio.Media.IS_MUSIC + "!= 0" + " AND "
                + MediaStore.Audio.Media._ID + "=" + "'" + id + "'")
        val projection = arrayOf<String>(
            MediaStore.Audio.Media.ALBUM_ID
        )
        var cursor: Cursor? = null
        try {
            cursor = cr.query(
                uri,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.TITLE + " ASC")
        } catch (ignored: Exception) {
        }
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst()
            val album_id: Int = cursor.getInt(0)
            var bm: Bitmap? = null
            try {
                val sArtworkUri: Uri = Uri
                    .parse("content://media/external/audio/albumart")
                uri = ContentUris.withAppendedId(sArtworkUri, album_id.toLong())
                val pfd: ParcelFileDescriptor = cr
                    .openFileDescriptor(uri, "r")
                if (pfd != null) {
                    val fd: FileDescriptor = pfd.getFileDescriptor()
                    bm = BitmapFactory.decodeFileDescriptor(fd)
                }
            } catch (e: Exception) {
                cursor.close()
            }
            cursor.close()
            if (bm != null) {
                return bm
            }
        }
        if (cursor != null) {
            cursor.close()
        }
        return null
    }

    fun getAlbumArtUri(album_id: Int): Uri? {
        return Uri.fromFile(File(MyApp.getInstance().getFilesDir()
            .getAbsolutePath() + "random.png"))
        /*Uri songCover = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(songCover, album_id);*/
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun getAlbumArtFromTrack(trackId: Int): Bitmap? {
        //This will get you the uri of the track, if you already have the track id
        val trackUri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            trackId.toLong())
        var bm: Bitmap? = null
        try {
            bm = MyApp.getContext().getContentResolver()
                .loadThumbnail(trackUri, Size(512, 512), null)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bm
    }

    internal interface INDEX_FOR_TRACK_CURSOR {
        companion object {
            const val _ID = 0
            const val TITLE = 1
            const val DATA_PATH = 2
            const val ARTIST = 3
            const val ALBUM = 4
            const val DURATION = 5
            const val ALBUM_ID = 6
        }
    }

    internal interface INDEX_FOR_GENRE_CURSOR {
        companion object {
            const val _ID = 0
            const val GENRE = 1
            const val NUMBER_OF_TRACKS = 2
        }
    }

    @Synchronized
    private fun updateArtistInfo() {
        Companion.artistUrls.clear()
        Companion.artistUrls.putAll(OfflineStorageArtistBio.getArtistImageUrls())
    }

    @Synchronized
    fun putEntryInArtistUrl(artist: String, link: String) {
        Companion.artistUrls[artist] = link
    }

    val artistUrls: HashMap<String, String>
        get() = Companion.artistUrls

    companion object {
        private var musicLibrary: MusicLibrary? = MusicLibrary()

        //artist photo urls for art library fragment only
        private val artistUrls = HashMap<String, String>()
        val instance: MusicLibrary?
            get() {
                if (musicLibrary == null) {
                    musicLibrary = MusicLibrary()
                }
                return musicLibrary
            }
    }

    init {
        context = MyApp.getContext()
        cr = context.contentResolver
        RefreshLibrary()
    }
}