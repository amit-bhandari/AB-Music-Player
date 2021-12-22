package com.music.player.bhandari.m.equalizer

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.media.audiofx.*
import com.google.gson.Gson
import com.music.player.bhandari.m.DBHelper.DbHelperEqualizer
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R

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
class EqualizerHelper constructor(
    private val mContext: Context,
    audioSessionId: Int,
    equalizerEnabled: Boolean
) {
    private var equalizer: Equalizer? = null
    private var virtualizer: Virtualizer? = null
    private var bassBoost: BassBoost? = null
    private var presetReverb: PresetReverb? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var isEqualizerSupported: Boolean = true
    private var dbHelperEqualizer: DbHelperEqualizer? = null
    fun releaseEqObjects() {
        try {
            equalizer!!.release()
            virtualizer!!.release()
            bassBoost!!.release()
            presetReverb!!.release()
        } catch (ignored: Exception) {
        }
        equalizer = null
        virtualizer = null
        bassBoost = null
        presetReverb = null
    }

    fun getEqualizer(): Equalizer {
        return equalizer!!
    }

    fun getVirtualizer(): Virtualizer {
        return virtualizer!!
    }

    fun getBassBoost(): BassBoost {
        return bassBoost!!
    }

    fun getEnhancer(): LoudnessEnhancer {
        return loudnessEnhancer!!
    }

    fun getPresetReverb(): PresetReverb {
        return presetReverb!!
    }

    //general equ setting is stored in shared preference to reemove db complications and calls
    fun storeLastEquSetting(equSetting: EqualizerSetting?) {
        val jsonSetting: String = Gson().toJson(equSetting)
        MyApp.getPref().edit().putString(mContext.getString(R.string.pref_last_equ_setting), jsonSetting).apply()
    }

    fun getLastEquSetting(): EqualizerSetting {
        val jsonString = MyApp.getPref().getString(mContext.getString(R.string.pref_last_equ_setting), "")
        return Gson().fromJson(jsonString,
            EqualizerSetting::class.java)
    }

    @SuppressLint("Range")
    fun getPresetList(): Array<String?> {
        val cursor: Cursor = dbHelperEqualizer!!.readableDatabase
            .rawQuery("select * from " + DbHelperEqualizer.TABLE_NAME, null)
        val size: Int = cursor.count
        val array: Array<String?> = arrayOfNulls(size)
        var index = 0
        while (cursor.moveToNext()) {
            array[index] = cursor.getString(cursor.getColumnIndex(DbHelperEqualizer.EQU_PRESET_NAME))
            index++
        }
        cursor.close()
        return array
    }

    fun insertPreset(
        preset_name: String?,
        equalizerSetting: EqualizerSetting?
    ) {
        val values = ContentValues()
        values.put(DbHelperEqualizer.EQU_PRESET_NAME, preset_name)
        values.put(DbHelperEqualizer.EQU_SETTING_STRING, Gson().toJson(equalizerSetting))
        dbHelperEqualizer!!.writableDatabase.insert(DbHelperEqualizer.TABLE_NAME, null, values)
    }

    @SuppressLint("Range")
    fun getPreset(preset_name: String): EqualizerSetting {
        val condition: String =
            DbHelperEqualizer.EQU_PRESET_NAME + "=" + "'" + preset_name.replace("'",
                "''") + "'"
        val columnsToReturn: Array<String> =
            arrayOf(DbHelperEqualizer.EQU_PRESET_NAME, DbHelperEqualizer.EQU_SETTING_STRING)
        val cursor: Cursor? = dbHelperEqualizer!!.readableDatabase
            .query(DbHelperEqualizer.TABLE_NAME, columnsToReturn, condition, null, null, null, null)
        if (cursor != null && cursor.count > 0) {
            cursor.moveToFirst()
            val jsonString: String =
                cursor.getString(cursor.getColumnIndex(DbHelperEqualizer.EQU_SETTING_STRING))
            val equalizerSetting: EqualizerSetting =
                Gson().fromJson(jsonString,
                    EqualizerSetting::class.java)
            cursor.close()
            return equalizerSetting
        } else {
            cursor?.close()
            return EqualizerSetting()
        }
    }

    fun isEqualizerSupported(): Boolean {
        return isEqualizerSupported
    }

    fun setEqualizerSupported(equalizerSupported: Boolean) {
        isEqualizerSupported = equalizerSupported
    }

    init {
        try {
            dbHelperEqualizer = DbHelperEqualizer(mContext)
            equalizer = Equalizer(0, audioSessionId)
            equalizer!!.enabled = equalizerEnabled
            virtualizer = Virtualizer(0, audioSessionId)
            virtualizer!!.enabled = equalizerEnabled
            bassBoost = BassBoost(0, audioSessionId)
            bassBoost!!.enabled = equalizerEnabled
            presetReverb = PresetReverb(0, audioSessionId)
            presetReverb!!.enabled = equalizerEnabled
            loudnessEnhancer = LoudnessEnhancer(audioSessionId)
            loudnessEnhancer!!.enabled = equalizerEnabled
        } catch (e: Exception) {
            isEqualizerSupported = false
        }
    }
}