package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.gson.Gson
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.model.MusicLibrary
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.model.dataItem
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo
import java.io.*
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
object OfflineStorageArtistBio {
    @SuppressLint("Range")
    fun getArtistBioFromTrackItem(item: TrackItem?): ArtistInfo? {
        if (item == null) {
            return null
        }
        var artistInfo: ArtistInfo? = null
        var cursor: Cursor? = null
        var db: SQLiteDatabase? = null
        try {
            val dbHelperArtistBio =
                DbHelperArtistBio(MyApp.getContext())
            db = dbHelperArtistBio.readableDatabase
            dbHelperArtistBio.onCreate(db)
            val where: String = (DbHelperArtistBio.ARTIST_ID.toString() + " = " + item.artist_id
                    + " OR " + DbHelperArtistBio.KEY_ARTIST + "= '" + item.getArtist()!!
                .replace("'", "''") + "'")
            cursor = db.query(DbHelperArtistBio.TABLE_NAME,
                arrayOf<String>(DbHelperArtistBio.ARTIST_BIO),
                where,
                null,
                null,
                null,
                null,
                "1")
            if (cursor != null && cursor.count != 0) {
                cursor.moveToFirst()
                //retrieve and fill lyrics object
                val gson = Gson()
                artistInfo =
                    gson.fromJson(cursor.getString(cursor.getColumnIndex(DbHelperArtistBio.ARTIST_BIO)),
                        ArtistInfo::class.java)
            }
        } catch (e: Exception) {
            return null
        } finally {
            if (cursor != null) {
                cursor.close()
            }
            if (db != null) {
                db.close()
            }
        }
        return artistInfo
    }

    fun putArtistBioToDB(artistInfo: ArtistInfo?, item: TrackItem?) {
        if (item == null || artistInfo == null) {
            return
        }
        var cursor: Cursor? = null
        var db: SQLiteDatabase? = null
        try {
            val dbHelperArtistBio: DbHelperArtistBio =
                DbHelperArtistBio(MyApp.Companion.getContext())
            db = dbHelperArtistBio.writableDatabase
            dbHelperArtistBio.onCreate(db)

            //check if already exists, if yes, return
            val where: String = (DbHelperArtistBio.ARTIST_ID.toString() + " = " + item.artist_id
                    + " OR " + DbHelperArtistBio.KEY_ARTIST + "= '" + item.getArtist()!!
                .replace("'", "''") + "'")
            cursor = db.query(DbHelperArtistBio.TABLE_NAME,
                arrayOf<String>(DbHelperArtistBio.KEY_ARTIST),
                where,
                null,
                null,
                null,
                null,
                "1")
            if (cursor != null && cursor.count > 0) {
                cursor.close()
                return
            }

            //convert lyrics to json
            val gson: Gson = Gson()
            val jsonInString: String = gson.toJson(artistInfo)
            val c: ContentValues = ContentValues()
            c.put(DbHelperArtistBio.ARTIST_BIO, jsonInString)
            c.put(DbHelperArtistBio.KEY_ARTIST, item.getArtist())
            c.put(DbHelperArtistBio.ARTIST_ID, item.artist_id)
            db.insert(DbHelperArtistBio.TABLE_NAME, null, c)
        } catch (ignored: Exception) {
        } finally {
            if (cursor != null) {
                cursor.close()
            }
            if (db != null) {
                db.close()
            }
        }
    }

    fun putArtistInfoToCache(artistInfo: ArtistInfo) {
        //don't care about exception.
        //
        Executors.newSingleThreadExecutor().execute(object : Runnable {
            override fun run() {
                try {
                    val CACHE_ART_INFO: String =
                        MyApp.getContext().cacheDir.toString() + "/artistInfo/"
                    val actual_file_path: String = CACHE_ART_INFO + artistInfo.getCorrectedArtist()
                    if (File(actual_file_path).exists()) {
                        return
                    }
                    val f: File = File(CACHE_ART_INFO)
                    if (!f.exists()) {
                        f.mkdir()
                    }
                    val out: ObjectOutput
                    out = ObjectOutputStream(FileOutputStream(actual_file_path))
                    out.writeObject(artistInfo)
                    out.close()
                    Log.v("Amit AB", "saved artist to cache")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    fun getArtistInfoFromCache(artist: String): ArtistInfo? {
        val CACHE_ART_INFO: String =
            MyApp.getContext().cacheDir.toString() + "/artistInfo/"
        val actual_file_path: String = CACHE_ART_INFO + artist
        val `in`: ObjectInputStream
        var artistInfo: ArtistInfo? = null
        try {
            val fileIn = FileInputStream(actual_file_path)
            `in` = ObjectInputStream(fileIn)
            artistInfo = `in`.readObject() as ArtistInfo?
            `in`.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (artistInfo != null) {
            Log.v("Amit AB", "got from cache" + artistInfo.getOriginalArtist())
        }
        return artistInfo
    }

    fun getArtistImageUrls(): HashMap<String, String> {
        val map: HashMap<String, String> = HashMap()
        try {
            val artistItems: ArrayList<dataItem> =
                ArrayList(MusicLibrary.instance!!.dataItemsArtist)
            for (item: dataItem in artistItems) {
                val trackItem = TrackItem()
                trackItem.setArtist(item.artist_name)
                trackItem.artist_id = item.artist_id
                val artistInfo: ArtistInfo? = getArtistBioFromTrackItem(trackItem)
                if (artistInfo != null && !artistInfo.getCorrectedArtist().equals("[unknown]")) {
                    map.put((artistInfo.getOriginalArtist())!!, (artistInfo.getImageUrl())!!)
                }
            }
        } catch (e: ConcurrentModificationException) {
            return map
        }
        return map
    }
}