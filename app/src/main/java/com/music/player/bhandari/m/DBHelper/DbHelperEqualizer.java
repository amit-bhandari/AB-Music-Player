package com.music.player.bhandari.m.DBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.music.player.bhandari.m.equalizer.EqualizerSetting;

/**
 * Created by Amit AB AB on 11/7/2017.
 */

public class DbHelperEqualizer extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "equalizer_setting";

    //EQUALIZER SETTING OBJECT WILL BE STORED AS STRING
    public static final String EQU_ID = "_id";
    public static final String EQU_PRESET_NAME = "equ_preset_name";
    public static final String EQU_SETTING_STRING = "equ_setting_string";

    public final static String TABLE_NAME = "equalizer_table";
    private static String TABLE_CREATE;


    public DbHelperEqualizer(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ("+ EQU_ID +" INTEGER PRIMARY KEY, " + EQU_PRESET_NAME +" TEXT, " + EQU_SETTING_STRING +" TEXT);";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        //insert default presets if not already added
        if(cursor.getCount()==0){
            insertPreset(db,"Flat", new EqualizerSetting(16, 16, 16, 16, 16, 16,
                    16, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"Bass Only", new EqualizerSetting(31, 31, 31, 0, 0, 0,
                    31, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"Treble Only",new EqualizerSetting( 0, 0, 0, 31, 31, 31,
                    0, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"Rock", new EqualizerSetting(16, 18, 16, 17, 19, 20,
                    22, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"Grunge", new EqualizerSetting(13, 16, 18, 19, 20, 17,
                    13, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"Metal",new EqualizerSetting( 12, 16, 16, 16, 20, 24,
                    16, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"Dance", new EqualizerSetting(14, 18, 20, 17, 16, 20,
                    23, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"Country",new EqualizerSetting( 16, 16, 18, 20, 17, 19,
                    20, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"Jazz",new EqualizerSetting( 16, 16, 18, 18, 18, 16,
                    20, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"Speech",new EqualizerSetting( 14, 16, 17, 14, 13, 15,
                    16, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"Classical", new EqualizerSetting(16, 18, 18, 16, 16, 17,
                    18, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"Blues", new EqualizerSetting(16, 18, 19, 20, 17, 18,
                    16, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"Opera",new EqualizerSetting( 16, 17, 19, 20, 16, 24,
                    18, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"Swing",new EqualizerSetting( 15, 16, 18, 20, 18, 17,
                    16, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"Acoustic", new EqualizerSetting(17, 18, 16, 19, 17, 17,
                    14, (short) 0, (short) 0, (short) 0));
            insertPreset(db,"New Age",new EqualizerSetting( 16, 19, 15, 18, 16, 16,
                    18, (short) 0, (short) 0, (short) 0));
        }

        cursor.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertPreset(SQLiteDatabase db, String preset_name, EqualizerSetting equalizerSetting){
        ContentValues values = new ContentValues();
        values.put(DbHelperEqualizer.EQU_PRESET_NAME, preset_name);
        values.put(DbHelperEqualizer.EQU_SETTING_STRING, new Gson().toJson(equalizerSetting));

        db.insert(DbHelperEqualizer.TABLE_NAME, null, values);
    }

}
