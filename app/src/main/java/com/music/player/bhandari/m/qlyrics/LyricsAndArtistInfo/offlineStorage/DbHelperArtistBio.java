package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Amit AB Bhandari on 3/29/2017.
 */

public class DbHelperArtistBio extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "artist_bio";

    static public final String KEY_ARTIST = "song_artist";
    static public final String ARTIST_ID = "_id";
    static public final String ARTIST_BIO = "art_bio";

    public static final String TABLE_NAME = "offline_artist_bio";

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
