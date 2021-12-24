package com.music.player.bhandari.m

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.music.player.bhandari.m.uiElementHelper.TypeFaceHelper
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.service.PlayerService
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump

/**
 * Copyright 2017 Amit Bhandari AB
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        selectedThemeId = pref!!.getInt(getString(R.string.pref_theme_id), Constants.DEFAULT_THEME_ID)
        val path = TypeFaceHelper.getTypeFacePath()
        ViewPump.init(ViewPump.builder()
            .addInterceptor(CalligraphyInterceptor(
                CalligraphyConfig.Builder()
                    .setDefaultFontPath(path)
                    .setFontAttrId(R.attr.fontPath)
                    .build()))
            .build())
    }

    companion object {
        private var instance: MyApp? = null
        private var pref: SharedPreferences? = null
        private var service: PlayerService? = null

        //music lock status flag
        private var isLocked: Boolean = false

        //check if app is in foreground
        //this is for button actions on bluetooth headset
        var isAppVisible: Boolean = false

        //batch lyrics download service status flag
        var isBatchServiceRunning: Boolean = false

        //user signed in or not status flag
        var hasUserSignedIn: Boolean = false

        //current selected theme id
        private var selectedThemeId: Int = 0

        fun getInstance(): MyApp {
            return instance!!
        }

        fun getContext(): Context {
            return instance!!
        }

        fun getPref(): SharedPreferences {
            return pref!!
        }

        fun setService(s: PlayerService?) {
            service = s
        }

        fun getService(): PlayerService? {
            return service
        }

        fun isLocked(): Boolean {
            return isLocked
        }

        fun setLocked(lock: Boolean) {
            isLocked = lock
        }

        fun getSelectedThemeId(): Int {
            return selectedThemeId
        }

        fun setSelectedThemeId(selectedThemeId: Int) {
            pref!!.edit().putInt(getContext().getString(R.string.pref_theme_id), selectedThemeId).apply()
            Companion.selectedThemeId = selectedThemeId
        }
    }
}