package com.music.player.bhandari.m.DBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.model.TrackItem;

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

public class DbHelperUserMusicData extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "beta_player";

    //columns
    static public final String KEY_TITLE = "song_title";
    static public final String KEY_ID = "song_id";
    static public final String KEY_TIME_STAMP = "last_time_played";
    static public final String KEY_COUNT = "number_of_times_played";
    public static final String KEY_FAV = "My_Fav";
    public static final String KEY_LAST_PLAYING_QUEUE = "last_playing_queue";


    public static final String TABLE_NAME = "user_music_data";

    private static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + " (" + KEY_TITLE + " TEXT, " + KEY_COUNT + " INTEGER, " + KEY_COUNT + " INTEGER, " + KEY_TIME_STAMP + " INTEGER, "
            + KEY_LAST_PLAYING_QUEUE + " INTEGER, " + KEY_FAV + " INTEGER);";

    //TABLE CREATE with song id
    private static final String TABLE_CREATE_NEW = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + " (" + KEY_TITLE + " TEXT, " + KEY_ID + " INTEGER, " + KEY_COUNT + " INTEGER, " + KEY_TIME_STAMP + " INTEGER, "
            + KEY_LAST_PLAYING_QUEUE + " INTEGER, " + KEY_FAV + " INTEGER);";

    public DbHelperUserMusicData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_NEW);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {

        if (arg2 == 3) {
            String insertQuery = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN "
                    + KEY_ID + " INTEGER DEFAULT 0";
            db.execSQL(insertQuery);
            updateIdField(db);
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL(TABLE_CREATE_NEW);
        }

        Log.d("DbHelperUserMusicData", "onUpgrade: from to :" + arg1 + " : " + arg2);
    }

    private void updateIdField(final SQLiteDatabase db) {

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Cursor cursor = db.query(TABLE_NAME, new String[]{KEY_TITLE}, null, null, null, null, null);
                    if (cursor != null && cursor.getCount() != 0) {
                        while (cursor.moveToNext()) {
                            String title = cursor.getString(0);
                            TrackItem item = MusicLibrary.getInstance().getTrackItemFromTitle(title);
                            if (item == null) continue;
                            ContentValues c = new ContentValues();
                            c.put(DbHelperUserMusicData.KEY_ID, item.getId());
                            db.update(TABLE_NAME, c, DbHelperUserMusicData.KEY_TITLE + "= ?", new String[]{title});
                            Log.d("DbHelperUserMusicData", "run: " + title);
                        }
                    }

                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception ignored) {
                }
            }
        });

    }
}
