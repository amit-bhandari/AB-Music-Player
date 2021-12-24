package com.music.player.bhandari.m.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import com.afollestad.materialdialogs.MaterialDialog
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
object AppLaunchCountManager {
    private const val DAYS_UNTIL_PROMPT = 1 //Min number of days
    private const val HOURS_UNTIL_INTER_AD = 24 //hours
    private const val HOURS_UNTIL_BANNER_ADS = 0
    private const val DAYS_UNTIL_RATE_ASK = 1
    fun app_launched(mContext: Context) {
        val prefs = mContext.getSharedPreferences("apprater", 0)
        val editor = prefs.edit()

        // Increment launch counter
        val launchCount = prefs.getLong("launch_count", 0) + 1
        editor.putLong("launch_count", launchCount)

        // Get date of first launch
        var dateFirstlaunch = prefs.getLong("date_firstlaunch", 0)
        if (dateFirstlaunch == 0L) {
            dateFirstlaunch = System.currentTimeMillis()
            editor.putLong("date_firstlaunch", dateFirstlaunch)
        }

        // Wait at least n days before opening
        if (!prefs.getBoolean("dontshowagain", false)) {
            if (launchCount % 5 == 0L) {
                if (System.currentTimeMillis() >= dateFirstlaunch +
                    DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000
                ) {
                    showRateDialog(mContext, editor)
                }
            }
        }
        editor.apply()
    }

    private fun showRateDialog(mContext: Context, editor: SharedPreferences.Editor?) {
        val dialog = MaterialDialog(mContext)
            .title(text = "Hello there!")
            .message(text = "This is AB (developer of AB Music) and I hope you" +
                    " are enjoying AB Music as much as I enjoyed developing it. Please consider rating and leaving review for "
                    + mContext.getString(R.string.app_name)
                    + " on store, you will bring smile on my face. Thank you in advance!")
            .positiveButton(text = "Rate now!"){
                val appPackageName =
                    mContext.packageName // getPackageName() from Context or Activity object
                try {
                    mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(
                        "market://details?id=$appPackageName")))
                } catch (anfe: ActivityNotFoundException) {
                    mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://play.google.com/store/apps/details?id=$appPackageName")))
                }
            }
            .neutralButton(text = "Never"){
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true)
                    editor.commit()
                }
            }
            .negativeButton(text = "Later maybe")

        dialog.window?.attributes?.windowAnimations = R.style.MyAnimation_Window
        dialog.show()
    }

    fun nowPlayingLaunched() {
        val prefs = MyApp.getContext().getSharedPreferences("apprater", 0)
        val editor = prefs!!.edit()

        // Increment launch counter
        val launchCount = prefs.getLong("launch_count_now_playing", 0) + 1
        editor!!.putLong("launch_count_now_playing", launchCount)
        editor.apply()
    }

    val nowPlayingLaunchCount: Long
        get() {
            val prefs: SharedPreferences = MyApp.getContext().getSharedPreferences("apprater", 0)
                ?: return -1
            return prefs.getLong("launch_count_now_playing", -1)
        }

    fun instantLyricsLaunched() {
        val prefs: SharedPreferences = MyApp.getContext().getSharedPreferences("apprater", 0)
        val editor = prefs.edit()

        // Increment launch counter
        val launchCount = prefs.getLong("launch_count_instantLyrics", 0) + 1
        editor.putLong("launch_count_instantLyrics", launchCount)
        editor.apply()
    }

    val instantLyricsCount: Long
        get() {
            val prefs: SharedPreferences = MyApp.getContext().getSharedPreferences("apprater", 0)
                ?: return -1
            return prefs.getLong("launch_count_instantLyrics", -1)
        }
    val isEligibleForInterstialAd: Boolean
        get() {
            val prefs: SharedPreferences = MyApp.getContext().getSharedPreferences("apprater", 0)
            val dateFirstlaunch = prefs.getLong("date_firstlaunch", 0)
            return dateFirstlaunch != 0L && System.currentTimeMillis() >= dateFirstlaunch + HOURS_UNTIL_INTER_AD * 60 * 60 * 1000
        }
    val isEligibleForRatingAsk: Boolean
        get() {
            val prefs: SharedPreferences = MyApp.getContext().getSharedPreferences("apprater", 0)
            val dateFirstlaunch = prefs.getLong("date_firstlaunch", 0)
            return dateFirstlaunch != 0L && System.currentTimeMillis() >= dateFirstlaunch + DAYS_UNTIL_RATE_ASK * 24 * 60 * 60 * 1000
        }
    val isEligibleForBannerAds: Boolean
        get() {
            val prefs: SharedPreferences = MyApp.getContext().getSharedPreferences("apprater", 0)
            val dateFirstlaunch = prefs.getLong("date_firstlaunch", 0)
            return dateFirstlaunch != 0L && System.currentTimeMillis() >= dateFirstlaunch + HOURS_UNTIL_BANNER_ADS * 60 * 60 * 1000
        }
}