package com.music.player.bhandari.m.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.MusicLibrary
import com.music.player.bhandari.m.model.dataItem
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadLyricThread
import com.music.player.bhandari.m.utils.UtilityFun

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
class BatchDownloaderService : Service(), Lyrics.Callback {
    var mHandler: Handler? = null
    var mNotificationManager: NotificationManager? = null
    var clickOnNotif: PendingIntent? = null
    var mReceiver: BroadcastReceiver? = null
    var mBuilder: NotificationCompat.Builder? = null
    var subtitleDownloadThreadRunning: Boolean = false
    var cancelBatchService: Boolean = true
    private val FINISHED: Int = 1
    private val CANCELLED: Int = 2
    private val CONNECTION_ERROR: Int = 3
    private val UNKNOWN: Int = 4
    var finishStatus: Int = UNKNOWN

    override fun onDestroy() {
        super.onDestroy()
        MyApp.isBatchServiceRunning = false
        stopForeground(false)
        when (finishStatus) {
            FINISHED -> {
                mBuilder!!.setContentTitle(getString(R.string.batch_download_finished))
                mBuilder!!.setContentText(" ")
                mBuilder!!.setOngoing(false)
                mNotificationManager!!.notify(Constants.NOTIFICATION_ID.BATCH_DOWNLOADER,
                    mBuilder!!.build())
                try {
                    val size: Int = MusicLibrary.instance!!.getDataItemsForTracks()!!.size
                    val bundle = Bundle()
                    bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, 2)
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,
                        "batch_download_finished $size")
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "batch_download")
                    //Logs an app event.
                    FirebaseAnalytics.getInstance(this)
                        .logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                } catch (ignored: Exception) {
                }
            }
            CANCELLED -> {
                mBuilder!!.setOngoing(false)
                mNotificationManager!!.cancel(Constants.NOTIFICATION_ID.BATCH_DOWNLOADER)
            }
            UNKNOWN, CONNECTION_ERROR -> {
                mBuilder!!.setOngoing(false)
                mNotificationManager!!.notify(Constants.NOTIFICATION_ID.BATCH_DOWNLOADER,
                    mBuilder!!.build())
            }
        }
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(mReceiver!!)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action != null) {
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        MyApp.isBatchServiceRunning = true
        initializeReceiver()
        initializeIntents()
        mHandler = Handler()
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mBuilder = NotificationCompat.Builder(this, "channel_01")
        mBuilder!!.setContentTitle(getString(R.string.batch_download_not_title))
            .setContentText(getString(R.string.batch_download_starting))
            .setSmallIcon(R.drawable.ic_file_download_black_24dp)
            .setOngoing(true)
            .setContentIntent(clickOnNotif)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */
            /*NotificationChannel channel = new NotificationChannel("channel_01",
                    "Playback Notification",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(null, null);
            mNotificationManager.createNotificationChannel(channel);*/
            mBuilder!!.setChannelId("channel_01")
        }
        startForeground(Constants.NOTIFICATION_ID.BATCH_DOWNLOADER, mBuilder!!.build())
        cancelBatchService = false
        //start running thread
        runThread()
    }

    private fun runThread() {
        Thread {
            val dataItems: ArrayList<dataItem> =
                ArrayList(MusicLibrary.instance!!.getDataItemsForTracks()!!.values)
            val size: Int = dataItems.size
            for (i in 0 until size) {
                var currentItem: dataItem
                try {
                    currentItem = dataItems[i]
                } catch (e: IndexOutOfBoundsException) {
                    finishStatus = CONNECTION_ERROR
                    continue
                }
                mBuilder!!.setProgress(size, i + 1, false)
                // Displays the progress bar for the first time.
                val stringBuilder: StringBuilder = StringBuilder()
                stringBuilder.append(currentItem.title).append(" ").append("(").append(i).append("/").append(size).append(")")
                mBuilder!!.setContentText(stringBuilder)
                mNotificationManager!!.notify(Constants.NOTIFICATION_ID.BATCH_DOWNLOADER,
                    mBuilder!!.build())
                finishStatus = FINISHED

                //check if current song present in db
                if (OfflineStorageLyrics.isLyricsPresentInDB(currentItem.id)) {
                    continue
                }
                DownloadLyricThread(this@BatchDownloaderService,
                    true,
                    MusicLibrary.instance!!.getTrackItemFromId(currentItem.id),
                    currentItem.artist_name,
                    currentItem.title).start()
                subtitleDownloadThreadRunning = true
                while (subtitleDownloadThreadRunning) {
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    if (!UtilityFun.isConnectedToInternet) {
                        cancelBatchService = true
                        mHandler!!.post {
                            Toast.makeText(applicationContext,
                                getString(R.string.error_no_internet),
                                Toast.LENGTH_SHORT).show()
                            finishStatus = CONNECTION_ERROR
                            mBuilder!!.setContentText(getString(R.string.error_no_connection))
                            stopForeground(false)
                            mNotificationManager!!.notify(Constants.NOTIFICATION_ID.BATCH_DOWNLOADER,
                                mBuilder!!.build())
                        }
                        stopSelf()
                        break
                    }
                }
                if (cancelBatchService) {
                    break
                }
                Log.v(Constants.TAG, "Task done for " + currentItem.title)
            }
            stopSelf()
        }.start()
    }

    private fun initializeReceiver() {
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == null) return
                when (intent.action) {
                    Constants.ACTION.CLICK_TO_CANCEL -> {
                        cancelBatchService = true
                        finishStatus = CANCELLED
                        stopSelf()
                    }
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.ACTION.CLICK_TO_CANCEL)
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(mReceiver!!, intentFilter)
    }

    private fun initializeIntents() {
        val notificationIntent = Intent(this, BatchDownloaderService::class.java)
        notificationIntent.action = Constants.ACTION.CLICK_TO_CANCEL
        clickOnNotif = PendingIntent.getService(this, 0,
            notificationIntent, 0)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onLyricsDownloaded(lyrics: Lyrics?) {
        subtitleDownloadThreadRunning = false
    }
}