package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Amit Bhandari on 3/29/2017.
 */

public class DbHelperLyrics extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "lyrics";

    static public final String KEY_TITLE = "song_title";
    static public final String _ID = "_id";
    static public final String LYRICS = "lyric";

    public static final String TABLE_NAME = "offline_lyrics";

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
