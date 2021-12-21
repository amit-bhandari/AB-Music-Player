package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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
class DbHelperLyrics internal constructor(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TABLE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        private val DATABASE_VERSION: Int = 2
        private val DATABASE_NAME: String = "lyrics"
        val KEY_TITLE: String = "song_title"
        val _ID: String = "_id"
        val LYRICS: String = "lyric"
        val TABLE_NAME: String = "offline_lyrics"
        private val TABLE_CREATE: String = ("CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME + " (" + _ID + " INTEGER, " + KEY_TITLE + " TEXT, "
                + LYRICS + " TEXT);")
    }
}