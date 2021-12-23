package com.music.player.bhandari.m.dbHelper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.music.player.bhandari.m.equalizer.EqualizerSetting

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
class DbHelperEqualizer constructor(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(TABLE_CREATE)
        val cursor: Cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        //insert default presets if not already added
        if (cursor.count == 0) {
            insertPreset(db, "Flat", EqualizerSetting(16, 16, 16, 16, 16, 16,
                16, 0, 0, 0))
            insertPreset(db, "Bass Only", EqualizerSetting(31, 31, 31, 0, 0, 0,
                31, 0, 0, 0))
            insertPreset(db, "Treble Only", EqualizerSetting(0, 0, 0, 31, 31, 31,
                0, 0, 0, 0))
            insertPreset(db, "Rock", EqualizerSetting(16, 18, 16, 17, 19, 20,
                22, 0, 0, 0))
            insertPreset(db, "Grunge", EqualizerSetting(13, 16, 18, 19, 20, 17,
                13, 0, 0, 0))
            insertPreset(db, "Metal", EqualizerSetting(12, 16, 16, 16, 20, 24,
                16, 0, 0, 0))
            insertPreset(db, "Dance", EqualizerSetting(14, 18, 20, 17, 16, 20,
                23, 0, 0, 0))
            insertPreset(db, "Country", EqualizerSetting(16, 16, 18, 20, 17, 19,
                20, 0, 0, 0))
            insertPreset(db, "Jazz", EqualizerSetting(16, 16, 18, 18, 18, 16,
                20, 0, 0, 0))
            insertPreset(db, "Speech", EqualizerSetting(14, 16, 17, 14, 13, 15,
                16, 0, 0, 0))
            insertPreset(db, "Classical", EqualizerSetting(16, 18, 18, 16, 16, 17,
                18, 0, 0, 0))
            insertPreset(db, "Blues", EqualizerSetting(16, 18, 19, 20, 17, 18,
                16, 0, 0, 0))
            insertPreset(db, "Opera", EqualizerSetting(16, 17, 19, 20, 16, 24,
                18, 0, 0, 0))
            insertPreset(db, "Swing", EqualizerSetting(15, 16, 18, 20, 18, 17,
                16, 0, 0, 0))
            insertPreset(db, "Acoustic", EqualizerSetting(17, 18, 16, 19, 17, 17,
                14, 0, 0, 0))
            insertPreset(db, "New Age", EqualizerSetting(16, 19, 15, 18, 16, 16,
                18, 0, 0, 0))
        }
        cursor.close()
    }

    override fun onUpgrade(db: SQLiteDatabase, i: Int, i1: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun insertPreset(db: SQLiteDatabase, preset_name: String?, equalizerSetting: EqualizerSetting?) {
        val values = ContentValues()
        values.put(EQU_PRESET_NAME, preset_name)
        values.put(EQU_SETTING_STRING, Gson().toJson(equalizerSetting))
        db.insert(TABLE_NAME, null, values)
    }

    companion object {
        private const val DATABASE_VERSION: Int = 3
        private const val DATABASE_NAME: String = "equalizer_setting"

        //EQUALIZER SETTING OBJECT WILL BE STORED AS STRING
        private val EQU_ID: String = "_id"
        const val EQU_PRESET_NAME: String = "equ_preset_name"
        const val EQU_SETTING_STRING: String = "equ_setting_string"
        const val TABLE_NAME: String = "equalizer_table"
        private var TABLE_CREATE: String = ""
    }

    init {
        TABLE_CREATE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME ($EQU_ID INTEGER PRIMARY KEY, $EQU_PRESET_NAME TEXT, $EQU_SETTING_STRING TEXT);"
    }
}