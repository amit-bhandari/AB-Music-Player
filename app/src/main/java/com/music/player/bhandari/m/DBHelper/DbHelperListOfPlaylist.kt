package com.music.player.bhandari.m.DBHelper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.music.player.bhandari.m.model.Constants

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
class DbHelperListOfPlaylist constructor(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    public override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TABLE_CREATE)
        //populate playlist list with system playlist
        if ((TABLE_NAME == Constants.SYSTEM_PLAYLISTS.PLAYLIST_LIST)) {
            val cursor: Cursor = db.query(Constants.SYSTEM_PLAYLISTS.PLAYLIST_LIST,
                null,
                null,
                null,
                null,
                null,
                null)
            // if playlists list is empty
            if (cursor.getCount() == 0) {
                for (playlistName: String? in Constants.SYSTEM_PLAYLISTS.listOfSystemPlaylist) {
                    val c: ContentValues = ContentValues()
                    c.put(KEY_TITLE, playlistName)
                    db.insert(Constants.SYSTEM_PLAYLISTS.PLAYLIST_LIST, null, c)
                }
            }
            cursor.close()
        }
    }

    public override fun onUpgrade(db: SQLiteDatabase, arg1: Int, arg2: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    companion object {
        private val DATABASE_VERSION: Int = 2
        private val DATABASE_NAME: String = "player"
        val KEY_TITLE: String = "file"
        private val TABLE_NAME: String = Constants.SYSTEM_PLAYLISTS.PLAYLIST_LIST
        private var TABLE_CREATE: String = ""
    }

    init {
        TABLE_CREATE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME ($KEY_TITLE TEXT);"
    }
}