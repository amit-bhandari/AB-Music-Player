package com.music.player.bhandari.m.fcm

import android.annotation.TargetApi
import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.activity.ActivityExploreLyrics
import com.music.player.bhandari.m.activity.ActivityLyricView
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.utils.AppLaunchCountManager
import com.music.player.bhandari.m.utils.UtilityFun
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*

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
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("MyFirebaseMessaging", "onMessageReceived: " + remoteMessage.toString())
        val map: Map<String, String> = remoteMessage.data
        for (keys in map.keys) {
            Log.d("MyFirebaseMessaging", "onMessageReceived: $keys")
        }

        //if opt out of notifications in settings, this part should never be executed
        //in case it does, ignore the message
        if (!MyApp.Companion.getPref().getBoolean(getString(R.string.pref_notifications), true)) {
            return
        }

        //do not show notification to users with ads removed
        if (map["type"] == "discount" && UtilityFun.isAdsRemoved) {
            return
        }
        if (map["type"] == "review" && (!AppLaunchCountManager.isEligibleForRatingAsk
                    || MyApp.Companion.getPref()
                .getBoolean(getString(R.string.pref_already_rated), false))
        ) {
            return
        }
        val handler = Handler(Looper.getMainLooper())
        handler.post { generatePictureStyleNotification(map).execute() }
    }

    class generatePictureStyleNotification internal constructor(private val map: Map<String, String>) :
        AsyncTask<String?, Void?, Bitmap?>() {
        private var builder: NotificationCompat.Builder? = null
        override fun doInBackground(vararg params: String?): Bitmap? {
            val `in`: InputStream
            try {
                val url = URL(map["image_link"])
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                `in` = connection.inputStream
                return BitmapFactory.decodeStream(`in`)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            try {
                val contentTitle = map["title"]
                val contentText = map["body"]
                val subText = map["subtitle"]
                builder = NotificationCompat.Builder(MyApp.getContext(),
                    MyApp.getContext().getString(R.string.notification_channel))
                    .setColor(ColorHelper.getColor(R.color.notification_color))
                    .setSmallIcon(R.drawable.ic_batman_kitkat)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSubText(subText)
                    .setAutoCancel(true)
                    .setStyle(NotificationCompat.BigPictureStyle().bigPicture(result)
                        .setSummaryText(contentText))
                builder!!.setVisibility(Notification.VISIBILITY_PUBLIC)
                builder!!.priority = Notification.PRIORITY_MAX
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder!!.setChannelId(MyApp.getContext()
                        .getString(R.string.notification_channel))
                }
                when (map["type"]) {
                    "discount" -> discountNotif()
                    "trending_tracks" -> trending_tracksNotif()
                    "check_out_lyric" -> Check_out_lyricNotif(map)
                    "unknown" -> UnknownNotif(map)
                    "search_lyric" -> searchLyricNotif()
                    "review" -> reviewNotif(map)
                }
                val notification = builder!!.build()
                val mNotificationManager: NotificationManager =
                    MyApp.getContext().getSystemService(
                        Context.NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.notify(Random().nextInt(), notification)
                try {
                    val bundle = Bundle()
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE,
                        "notification_displayed")
                    UtilityFun.logEvent(bundle)
                } catch (ignored: Exception) {
                }
            } catch (e: Exception) {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification_crashed_app")
                UtilityFun.logEvent(bundle)
            }
        }

        //helper methods to build specific notifications
        private fun discountNotif() {}
        private fun trending_tracksNotif() {
            val requestCode = Random().nextInt()
            val notificationIntent =
                Intent(MyApp.getContext(), ActivityExploreLyrics::class.java)
            notificationIntent.action = Constants.ACTION.MAIN_ACTION
            notificationIntent.putExtra("fresh_load", true)
            notificationIntent.putExtra("from_notif", true)
            notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val contentIntent: PendingIntent =
                PendingIntent.getActivity(MyApp.getContext(), requestCode,
                    notificationIntent, 0)
            builder!!.setContentIntent(contentIntent)
        }

        private fun Check_out_lyricNotif(map: Map<String, String>) {
            val trackTitle = map["trackname"]
            val artist = map["artist"]
            val requestCode = Random().nextInt()
            val notificationIntent =
                Intent(MyApp.getContext(), ActivityLyricView::class.java)
            notificationIntent.action = Constants.ACTION.MAIN_ACTION
            notificationIntent.putExtra("track_title", trackTitle)
            notificationIntent.putExtra("artist", artist)
            notificationIntent.putExtra("from_notif", true)
            notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val contentIntent: PendingIntent =
                PendingIntent.getActivity(MyApp.Companion.getContext(), requestCode,
                    notificationIntent, 0)
            builder!!.setContentIntent(contentIntent)
        }

        private fun UnknownNotif(map: Map<String, String>) {
            val notificationIntent = Intent(Intent.ACTION_VIEW)
            notificationIntent.data = Uri.parse(map["link"])
            val contentIntent: PendingIntent =
                PendingIntent.getActivity(MyApp.Companion.getContext(), 0,
                    notificationIntent, 0)
            builder!!.setContentIntent(contentIntent)
        }

        private fun searchLyricNotif() {
            val requestCode = Random().nextInt()
            val notificationIntent =
                Intent(MyApp.getContext(), ActivityExploreLyrics::class.java)
            notificationIntent.action = Constants.ACTION.MAIN_ACTION
            notificationIntent.putExtra("search_on_launch", true)
            notificationIntent.putExtra("from_notif", true)
            notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val contentIntent: PendingIntent =
                PendingIntent.getActivity(MyApp.Companion.getContext(), requestCode,
                    notificationIntent, 0)
            builder!!.setContentIntent(contentIntent)
        }

        private fun reviewNotif(map: Map<String, String>) {
            val action1Intent: Intent =
                Intent(MyApp.getContext(), NotificationActionService::class.java)
                    .setAction(ALREADY_RATED)
            action1Intent.putExtra("from_notif", true)
            val alreadyRatedIntent: PendingIntent =
                PendingIntent.getService(MyApp.Companion.getContext(), 20,
                    action1Intent, PendingIntent.FLAG_ONE_SHOT)
            val action2Intent: Intent =
                Intent(MyApp.Companion.getContext(), NotificationActionService::class.java)
                    .setAction(RATE_NOW)
            action2Intent.putExtra("from_notif", true)
            val rateNowIntent: PendingIntent =
                PendingIntent.getService(MyApp.Companion.getContext(), 20,
                    action2Intent, PendingIntent.FLAG_ONE_SHOT)
            builder!!.addAction(NotificationCompat.Action(R.drawable.ic_close_white_24dp,
                "Rate now!",
                rateNowIntent))
            builder!!.addAction(NotificationCompat.Action(R.drawable.ic_close_white_24dp,
                "Already rated",
                alreadyRatedIntent))
            builder!!.setContentIntent(rateNowIntent)
        }

        class NotificationActionService :
            IntentService(NotificationActionService::class.java.simpleName) {
            override fun onHandleIntent(intent: Intent?) {
                val action: String = intent!!.action ?: return
                if (intent.extras != null && intent.extras!!.getBoolean("from_notif")) {
                    try {
                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE,
                            "notification_clicked")
                        UtilityFun.logEvent(bundle)
                    } catch (ignored: Exception) {
                    }
                }
                Log.d("NotificationAction", "onHandleIntent: $action")
                when (action) {
                    ALREADY_RATED -> MyApp.getPref().edit()
                        .putBoolean(getString(R.string.pref_already_rated), true).apply()
                    RATE_NOW -> {
                        val appPackageName: String =
                            packageName // getPackageName() from Context or Activity object
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=$appPackageName")))
                        } catch (anfe: ActivityNotFoundException) {
                            startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                        }
                        try {
                            val bundle = Bundle()
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE,
                                "rate_on_notif_click")
                            UtilityFun.logEvent(bundle)
                        } catch (ignored: Exception) {
                        }
                    }
                }
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager?)?.cancel(Constants.NOTIFICATION_ID.FCM)
            }
        }

        companion object {
            private const val ALREADY_RATED = "already_rated"
            private const val RATE_NOW = "rate_now"
        }
    }
}