package com.music.player.bhandari.m.DBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.music.player.bhandari.m.model.Constants;

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

public class DbHelperListOfPlaylist extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "player";
    public static final String KEY_TITLE = "file";
    private final static String TABLE_NAME = Constants.SYSTEM_PLAYLISTS.PLAYLIST_LIST;
    private static String TABLE_CREATE;

    public DbHelperListOfPlaylist(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ("+KEY_TITLE +" TEXT);";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        //populate playlist list with system playlist
        if(TABLE_NAME.equals(Constants.SYSTEM_PLAYLISTS.PLAYLIST_LIST)){
            Cursor cursor=db.query(Constants.SYSTEM_PLAYLISTS.PLAYLIST_LIST,null,null,null,null,null,null);
            // if playlists list is empty
            if (cursor.getCount()==0) {
                for (String playlistName : Constants.SYSTEM_PLAYLISTS.listOfSystemPlaylist) {
                    ContentValues c = new ContentValues();
                    c.put(DbHelperListOfPlaylist.KEY_TITLE, playlistName);
                    db.insert(Constants.SYSTEM_PLAYLISTS.PLAYLIST_LIST, null, c);
                }
            }
            cursor.close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
         onCreate(db);
    }
}
