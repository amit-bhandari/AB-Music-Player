package com.music.player.bhandari.m;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.service.PlayerService;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

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

public class MyApp extends Application {
    private static MyApp instance;
    private static SharedPreferences pref;
    private static PlayerService service;

    //music lock status flag
    private static boolean isLocked = false;

    //check if app is in foreground
    //this is for button actions on bluetooth headset
    public static boolean isAppVisible;

    //batch lyrics download service status flag
    public static boolean isBatchServiceRunning = false;

    //user signed in or not status flag
    public static boolean hasUserSignedIn = false;

    //current selected theme id
    private static int selectedThemeId = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        selectedThemeId = pref.getInt(getString(R.string.pref_theme_id), Constants.DEFAULT_THEME_ID);

        String path = TypeFaceHelper.getTypeFacePath();
        if (path != null) {
            ViewPump.init(ViewPump.builder()
                    .addInterceptor(new CalligraphyInterceptor(
                            new CalligraphyConfig.Builder()
                                    .setDefaultFontPath(path)
                                    .setFontAttrId(R.attr.fontPath)
                                    .build()))
                    .build());
        }
    }

    public static MyApp getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance;
    }

    public static SharedPreferences getPref() {
        return pref;
    }

    public static void setService(PlayerService s) {
        service = s;
    }

    public static PlayerService getService() {
        return service;
    }

    public static boolean isLocked() {
        return isLocked;
    }

    public static void setLocked(boolean lock) {
        isLocked = lock;
    }

    public static int getSelectedThemeId() {
        return selectedThemeId;
    }

    public static void setSelectedThemeId(int selectedThemeId) {
        pref.edit()
                .putInt(MyApp.getContext().getString(R.string.pref_theme_id), selectedThemeId).apply();

        MyApp.selectedThemeId = selectedThemeId;
    }

}