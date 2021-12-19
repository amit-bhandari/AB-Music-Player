package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.gson.Gson
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics
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
/**
 * Some things in this class are messed up because of instant lyrics and explore lyrics feature
 * Those were unplanned changes
 * But code works good without any issue
 * If it looks stupid but it works, it ain't stupid
 */
object OfflineStorageLyrics {
    //look into db for lyrics, if not found, return null
    fun getLyricsFromDB(item: TrackItem?): Lyrics? {
        if (item == null) {
            return null
        }
        var lyrics: Lyrics? = null
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            val dbHelperLyrics: DbHelperLyrics = DbHelperLyrics(MyApp.Companion.getContext())
            db = dbHelperLyrics.readableDatabase
            dbHelperLyrics.onCreate(db)
            val where: String = DbHelperLyrics._ID + " = " + item.id
            cursor = db.query(DbHelperLyrics.TABLE_NAME,
                arrayOf(DbHelperLyrics.LYRICS),
                where,
                null,
                null,
                null,
                null,
                "1")
            if (cursor != null && cursor.count != 0) {
                cursor.moveToFirst()
                //retrieve and fill lyrics object
                val gson: Gson = Gson()
                lyrics =
                    gson.fromJson(cursor.getString(cursor.getColumnIndex(DbHelperLyrics.LYRICS)),
                        Lyrics::class.java)
                lyrics.setTrackId(item.id)
                cursor.close()
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
        return lyrics
    }

    fun putLyricsInDB(lyrics: Lyrics?, item: TrackItem?) {
        if (item == null || lyrics == null) {
            return
        }
        Log.d("OfflineStorageLyrics", "putLyricsInDB: " + item.title)
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            val dbHelperLyrics: DbHelperLyrics = DbHelperLyrics(MyApp.Companion.getContext())
            db = dbHelperLyrics.writableDatabase
            dbHelperLyrics.onCreate(db)

            //check if data already exists, if it does, return
            val where: String = (DbHelperLyrics._ID.toString() + " = " + item.id
                    + " OR " + DbHelperLyrics.KEY_TITLE + "= '" + item.title!!.replace("'",
                "''") + "'")
            cursor = db.query(DbHelperLyrics.TABLE_NAME,
                arrayOf<String>(DbHelperLyrics.KEY_TITLE),
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
            val jsonInString: String = gson.toJson(lyrics)
            val c: ContentValues = ContentValues()
            c.put(DbHelperLyrics.LYRICS, jsonInString)
            c.put(DbHelperLyrics.KEY_TITLE, item.title)
            c.put(DbHelperLyrics._ID, item.id)
            db.insert(DbHelperLyrics.TABLE_NAME, null, c)
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

    //clear lyrics based on id (used for internal lyrics of AB Music offline tracks)
    fun clearLyricsFromDB(item: TrackItem?): Boolean {
        if (item == null) {
            return false
        }
        var db: SQLiteDatabase? = null
        try {
            val dbHelperLyrics: DbHelperLyrics = DbHelperLyrics(MyApp.Companion.getContext())
            db = dbHelperLyrics.readableDatabase
            dbHelperLyrics.onCreate(db)
            val where: String = DbHelperLyrics._ID.toString() + " = " + item.id
            val i: Int = db.delete(DbHelperLyrics.TABLE_NAME, where, null)
            return i >= 1
        } catch (e: Exception) {
            return false
        } finally {
            if (db != null) {
                db.close()
            }
        }
    }

    fun isLyricsPresentInDB(id: Int): Boolean {
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            val dbHelperLyrics: DbHelperLyrics = DbHelperLyrics(MyApp.Companion.getContext())
            db = dbHelperLyrics.readableDatabase
            dbHelperLyrics.onCreate(db)
            val where: String = DbHelperLyrics._ID.toString() + " = " + id
            cursor = db.query(DbHelperLyrics.TABLE_NAME,
                arrayOf<String>(DbHelperLyrics.LYRICS),
                where,
                null,
                null,
                null,
                null,
                "1")
            if (cursor != null && cursor.count != 0) {
                cursor.close()
                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            return false
        } finally {
            if (cursor != null) {
                cursor.close()
            }
            if (db != null) {
                db.close()
            }
        }
    }

    //methods for storing and retrieving instant lyrics
    //unlike lyrics from AB Music, track item will not have id
    fun getInstantLyricsFromDB(item: TrackItem?): Lyrics? {
        if (item == null) {
            return null
        }
        var lyrics: Lyrics? = null
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            val dbHelperLyrics: DbHelperLyrics = DbHelperLyrics(MyApp.Companion.getContext())
            db = dbHelperLyrics.readableDatabase
            dbHelperLyrics.onCreate(db)
            val where: String =
                (DbHelperLyrics._ID.toString() + " = " + item.id + " AND " + DbHelperLyrics.KEY_TITLE
                        + " = '" + item.title!!.replace("'", "''") + "'")
            cursor = db.query(DbHelperLyrics.TABLE_NAME,
                arrayOf<String>(DbHelperLyrics.LYRICS),
                where,
                null,
                null,
                null,
                null,
                "1")
            if (cursor.count != 0) {
                cursor.moveToFirst()
                //retrieve and fill lyrics object
                val gson: Gson = Gson()
                lyrics =
                    gson.fromJson(cursor.getString(cursor.getColumnIndex(DbHelperLyrics.LYRICS)),
                        Lyrics::class.java)
                lyrics.setTrackId(item.id)
                cursor.close()
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
        return lyrics
    }

    fun putInstantLyricsInDB(lyrics: Lyrics?, item: TrackItem?): Boolean {
        if (item == null || lyrics == null) {
            return false
        }
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            val dbHelperLyrics: DbHelperLyrics = DbHelperLyrics(MyApp.Companion.getContext())
            db = dbHelperLyrics.writableDatabase
            dbHelperLyrics.onCreate(db)

            //check if data already exists, if it does, return
            val where: String = (DbHelperLyrics._ID.toString() + " = " + item.id
                    + " AND " + DbHelperLyrics.KEY_TITLE + "= '" + item.title!!.replace("'",
                "''") + "'")
            cursor = db.query(DbHelperLyrics.TABLE_NAME,
                arrayOf<String>(DbHelperLyrics.KEY_TITLE),
                where,
                null,
                null,
                null,
                null,
                "1")
            if (cursor != null && cursor.count > 0) {
                cursor.close()
                return true
            }

            //convert lyrics to json
            val gson: Gson = Gson()
            val jsonInString: String = gson.toJson(lyrics)
            val c: ContentValues = ContentValues()
            c.put(DbHelperLyrics.LYRICS, jsonInString)
            c.put(DbHelperLyrics.KEY_TITLE, item.title)
            c.put(DbHelperLyrics._ID, item.id)
            db.insert(DbHelperLyrics.TABLE_NAME, null, c)
            return true
        } catch (ignored: Exception) {
            return false
        } finally {
            if (cursor != null) {
                cursor.close()
            }
            if (db != null) {
                db.close()
            }
        }
    }

    /**
     * check if lyrics present in db for given track title and id
     * this method is used in lyric view and instant lyric screen to determine save or delete action for fab
     * @param track
     * @param id
     * @return
     */
    fun isLyricsPresentInDB(track: String, id: Int): Boolean {
        Log.d("OfflineStorage", "isLyricsPresentInDB: " + track + " " + id)
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            val dbHelperLyrics: DbHelperLyrics = DbHelperLyrics(MyApp.Companion.getContext())
            db = dbHelperLyrics.readableDatabase
            dbHelperLyrics.onCreate(db)
            val where: String =
                (DbHelperLyrics.KEY_TITLE.toString() + " = '" + track.replace("'", "''") + "'  AND "
                        + DbHelperLyrics._ID + " = " + id)
            cursor = db.query(DbHelperLyrics.TABLE_NAME,
                arrayOf<String>(DbHelperLyrics.LYRICS),
                where,
                null,
                null,
                null,
                null,
                "1")
            if (cursor != null && cursor.count != 0) {
                cursor.close()
                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            return false
        } finally {
            if (cursor != null) {
                cursor.close()
            }
            if (db != null) {
                db.close()
            }
        }
    }

    //clear lyrics based on track title and id =- 1,used in instant lyrics screen
    fun clearLyricsFromDB(track: String): Boolean {
        var db: SQLiteDatabase? = null
        try {
            val dbHelperLyrics: DbHelperLyrics = DbHelperLyrics(MyApp.Companion.getContext())
            db = dbHelperLyrics.readableDatabase
            dbHelperLyrics.onCreate(db)
            val where: String =
                (DbHelperLyrics.KEY_TITLE.toString() + " = '" + track.replace("'", "''") + "'  AND "
                        + DbHelperLyrics._ID + " = -1")
            val i: Int = db.delete(DbHelperLyrics.TABLE_NAME, where, null)
            return i >= 1
        } catch (e: Exception) {
            return false
        } finally {
            if (db != null) {
                db.close()
            }
        }
    }

    //clear lyrics given track title and track id (used from saved lyrics screen)
    //id == -1 in case lyrics is saved from tracks other than AB Music offline tracks
    fun clearLyricsFromDB(track: String, id: Int): Boolean {
        var db: SQLiteDatabase? = null
        try {
            val dbHelperLyrics: DbHelperLyrics = DbHelperLyrics(MyApp.Companion.getContext())
            db = dbHelperLyrics.readableDatabase
            dbHelperLyrics.onCreate(db)
            val where: String =
                (DbHelperLyrics.KEY_TITLE.toString() + " = '" + track.replace("'", "''") + "'  AND "
                        + DbHelperLyrics._ID + " = " + id)
            val i: Int = db.delete(DbHelperLyrics.TABLE_NAME, where, null)
            return i >= 1
        } catch (e: Exception) {
            return false
        } finally {
            db?.close()
        }
    }

    //temporary cache for instant lyrics and explore lyrics screens for avoiding repetitive lyric network calls
    //
    fun putLyricsToCache(lyrics: Lyrics) {

        //don't care about exception.
        //
        Executors.newSingleThreadExecutor().execute(object : Runnable {
            override fun run() {
                try {
                    val CACHE_ART_LYRICS: String =
                        MyApp.Companion.getContext().getCacheDir().toString() + "/lyrics/"
                    val actual_file_path: String =
                        CACHE_ART_LYRICS + lyrics.getOriginalTrack() + lyrics.getOriginalArtist()
                    if (File(actual_file_path).exists()) {
                        return
                    }
                    val f: File = File(CACHE_ART_LYRICS)
                    if (!f.exists()) {
                        f.mkdir()
                    }
                    val out: ObjectOutput
                    out = ObjectOutputStream(FileOutputStream(actual_file_path))
                    out.writeObject(lyrics)
                    out.close()
                    Log.v("Amit AB", "saved lyrics to cache")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    fun clearLyricsFromCache(lyrics: Lyrics) {
        try {
            val CACHE_ART_LYRICS: String =
                MyApp.Companion.getContext().getCacheDir().toString() + "/lyrics/"
            val actual_file_path: String =
                CACHE_ART_LYRICS + lyrics.getOriginalTrack() + lyrics.getOriginalArtist()
            val lyricFile: File = File(actual_file_path)
            if (lyricFile.exists()) {
                lyricFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLyricsFromCache(item: TrackItem): Lyrics? {
        val CACHE_ART_LYRICS: String =
            MyApp.Companion.getContext()!!.cacheDir.toString() + "/lyrics/"
        val actual_file_path: String = CACHE_ART_LYRICS + item.title + item.getArtist()
        val `in`: ObjectInputStream
        var lyrics: Lyrics? = null
        try {
            val fileIn: FileInputStream = FileInputStream(actual_file_path)
            `in` = ObjectInputStream(fileIn)
            lyrics = `in`.readObject()
            `in`.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (lyrics != null) {
            Log.v("Amit AB", "got from cache" + lyrics.getOriginalTrack())
        }
        return lyrics
    }

    //get all saved lyrics from db
    fun getAllSavedLyrics(): MutableList<Lyrics> {
        val lyrics: MutableList<Lyrics> = ArrayList<Lyrics>()
        var db: SQLiteDatabase? = null
        var cursor: Cursor? = null
        try {
            val dbHelperLyrics: DbHelperLyrics = DbHelperLyrics(MyApp.Companion.getContext())
            db = dbHelperLyrics.readableDatabase
            dbHelperLyrics.onCreate(db)

            //String where = DbHelperLyrics._ID + " = " + item.getId();
            cursor = db.query(DbHelperLyrics.TABLE_NAME, null, null, null, null, null, null, null)
            if (cursor != null && cursor.count != 0) {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val gson: Gson = Gson()
                    val id: Int = cursor.getInt(cursor.getColumnIndex(DbHelperLyrics._ID))
                    val lyric: Lyrics? =
                        gson.fromJson(cursor.getString(cursor.getColumnIndex(DbHelperLyrics.LYRICS)),
                            Lyrics::class.java)
                    if (lyric != null) {
                        lyric.setTrackId(id)
                        lyrics.add(lyric)
                        Log.d("OfflineStorage",
                            "getAllSavedLyrics: " + lyric.getTrack()
                                .toString() + " : " + lyric.getTrackId())
                    }
                    cursor.moveToNext()
                }
                cursor.close()
            }
        } catch (e: Exception) {
            return lyrics
        } finally {
            cursor?.close()
            db?.close()
        }
        return lyrics
    }
}