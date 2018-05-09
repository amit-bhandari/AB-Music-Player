package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 Copyright 2017 Amit Bhandari AB

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

public class DbHelperLyrics extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "lyrics";

    static final String KEY_TITLE = "song_title";
    static final String _ID = "_id";
    static final String LYRICS = "lyric";

    static final String TABLE_NAME = "offline_lyrics";

    private static final String TABLE_CREATE= "CREATE TABLE IF NOT EXISTS "
            +TABLE_NAME+" ("+ _ID+" INTEGER, " + KEY_TITLE + " TEXT, "
            +LYRICS + " TEXT);";



    public DbHelperLyrics(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME );
        onCreate(db);
    }
}
