package com.music.player.bhandari.m.equalizer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;

import com.google.gson.Gson;
import com.music.player.bhandari.m.DBHelper.DbHelperEqualizer;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;

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

public class EqualizerHelper {

    private Context mContext;

    private Equalizer equalizer;
    private Virtualizer virtualizer;
    private BassBoost bassBoost;
    private PresetReverb presetReverb;

    private boolean isEqualizerSupported = true;

    private DbHelperEqualizer dbHelperEqualizer;

    public EqualizerHelper(Context context, int audioSessionId, boolean equalizerEnabled){
        mContext = context;

        try {
            dbHelperEqualizer = new DbHelperEqualizer(context);

            equalizer = new Equalizer(0, audioSessionId);
            equalizer.setEnabled(equalizerEnabled);

            virtualizer = new Virtualizer(0, audioSessionId);
            virtualizer.setEnabled(equalizerEnabled);

            bassBoost = new BassBoost(0, audioSessionId);
            bassBoost.setEnabled(equalizerEnabled);

            presetReverb = new PresetReverb(0, audioSessionId);
            presetReverb.setEnabled(equalizerEnabled);

        }catch (Exception e){
            isEqualizerSupported = false;
        }
    }

    public void releaseEqObjects(){
        try {
            equalizer.release();
            virtualizer.release();
            bassBoost.release();
            presetReverb.release();
        }catch (Exception ignored){}

        equalizer = null;
        virtualizer = null;
        bassBoost = null;
        presetReverb = null;
    }

    public Equalizer getEqualizer(){
        return equalizer;
    }

    public Virtualizer getVirtualizer(){
        return virtualizer;
    }

    public BassBoost getBassBoost(){
        return bassBoost;
    }

    public PresetReverb getPresetReverb(){
        return presetReverb;
    }

    //general equ setting is stored in shared preference to reemove db complications and calls
    public void storeLastEquSetting(EqualizerSetting equSetting){
        String jsonSetting = new Gson().toJson(equSetting);
        MyApp.getPref().edit().putString(mContext.getString(R.string.pref_last_equ_setting),jsonSetting).apply();
    }

    public EqualizerSetting getLastEquSetting(){
        String jsonString = MyApp.getPref().getString(mContext.getString(R.string.pref_last_equ_setting), "");
        return new Gson().fromJson(jsonString, EqualizerSetting.class);
    }

    public String[] getPresetList(){
        Cursor  cursor = dbHelperEqualizer.getReadableDatabase().rawQuery("select * from " + DbHelperEqualizer.TABLE_NAME,null);
        int size = cursor.getCount();
        String[] array = new String[size];

        int index =0;
        while (cursor.moveToNext()){
            array[index] = cursor.getString(cursor.getColumnIndex(DbHelperEqualizer.EQU_PRESET_NAME));
            index++;
        }
        cursor.close();
        return array;
    }

    public void insertPreset(String preset_name, EqualizerSetting equalizerSetting){
        ContentValues values = new ContentValues();
        values.put(DbHelperEqualizer.EQU_PRESET_NAME, preset_name);
        values.put(DbHelperEqualizer.EQU_SETTING_STRING, new Gson().toJson(equalizerSetting));

        dbHelperEqualizer.getWritableDatabase().insert(DbHelperEqualizer.TABLE_NAME, null, values);
    }

    public EqualizerSetting getPreset(String preset_name){
        String condition = DbHelperEqualizer.EQU_PRESET_NAME + "=" + "'" + preset_name.replace("'", "''") + "'";
        String[] columnsToReturn = { DbHelperEqualizer.EQU_PRESET_NAME ,DbHelperEqualizer.EQU_SETTING_STRING };

        Cursor  cursor = dbHelperEqualizer.getReadableDatabase().query(DbHelperEqualizer.TABLE_NAME, columnsToReturn, condition, null, null, null, null);

        if (cursor!=null && cursor.getCount()>0)  {
            cursor.moveToFirst();
            String jsonString = cursor.getString(cursor.getColumnIndex(DbHelperEqualizer.EQU_SETTING_STRING));
            EqualizerSetting equalizerSetting = new Gson().fromJson(jsonString, EqualizerSetting.class);
            cursor.close();
            return equalizerSetting;
        } else {
            if (cursor != null) {
                cursor.close();
            }
            return new EqualizerSetting();
        }
    }

    public boolean isEqualizerSupported() {
        return isEqualizerSupported;
    }

    public void setEqualizerSupported(boolean equalizerSupported) {
        isEqualizerSupported = equalizerSupported;
    }
}
