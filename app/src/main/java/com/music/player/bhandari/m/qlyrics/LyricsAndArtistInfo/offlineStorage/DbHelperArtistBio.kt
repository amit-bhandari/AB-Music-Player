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
class DbHelperArtistBio constructor(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    public override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TABLE_CREATE)
    }

    public override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        private val DATABASE_VERSION: Int = 2
        private val DATABASE_NAME: String = "artist_bio"
        val KEY_ARTIST: String = "song_artist"
        val ARTIST_ID: String = "_id"
        val ARTIST_BIO: String = "art_bio"
        val TABLE_NAME: String = "offline_artist_bio"
        private val TABLE_CREATE: String = ("CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME + " (" + ARTIST_ID + " INTEGER, " + KEY_ARTIST + " TEXT, "
                + ARTIST_BIO + " TEXT);")
    }
}