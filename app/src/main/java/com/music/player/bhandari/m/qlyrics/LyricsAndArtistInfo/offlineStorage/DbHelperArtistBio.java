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
public class DbHelperArtistBio extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "artist_bio";

    static final String KEY_ARTIST = "song_artist";
    static final String ARTIST_ID = "_id";
    static final String ARTIST_BIO = "art_bio";

    static final String TABLE_NAME = "offline_artist_bio";

    private static final String TABLE_CREATE= "CREATE TABLE IF NOT EXISTS "
            +TABLE_NAME+" ("+ ARTIST_ID +" INTEGER, " + KEY_ARTIST + " TEXT, "
            +ARTIST_BIO + " TEXT);";


    public DbHelperArtistBio(Context context){
        super(context, DATABASE_NAME, null,DATABASE_VERSION);
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
