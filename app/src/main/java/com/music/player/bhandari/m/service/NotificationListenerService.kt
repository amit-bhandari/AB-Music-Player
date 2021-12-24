package com.music.player.bhandari.m.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.media.*
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.activity.ActivityInstantLyric
import com.music.player.bhandari.m.model.Constants
import java.util.concurrent.Executors

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
class NotificationListenerService : NotificationListenerService(), RemoteController.OnClientUpdateListener {
    private var mController: RemoteController? = null
    private var isRemoteControllerPlaying: Boolean = false
    private var mHasBug: Boolean = true
    private var listener: MediaSessionManager.OnActiveSessionsChangedListener? = null
    private var controllerCallback: MediaController.Callback? = null
    private val TAG: String = "NotificationListener"
    private var currentMusicInfo: SharedPreferences? = null
    private var contentIntent: PendingIntent? = null
    private var mNotificationManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Creating notification listener service")
        //Log.v(TAG,"Created service");
        currentMusicInfo = getSharedPreferences("current_music", Context.MODE_PRIVATE)
        mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        val notificationIntent: Intent = Intent(this, ActivityInstantLyric::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        contentIntent = PendingIntent.getActivity(this, 0,
            notificationIntent, 0)
        listener = object : MediaSessionManager.OnActiveSessionsChangedListener {
            override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
                val controller = controllers?.get(0)
                controller?.packageName?.let { Log.v(TAG, it) }
                if (controller != null) {
                    if ((("com.google.android.youtube" == controller.packageName) || ("com.bhandari.music" == controller.packageName) || ("com.android.chrome" == controller.packageName) || ("org.videolan.vlc" == controller.packageName))) return
                }
                if (controllerCallback != null) controller?.unregisterCallback(
                    controllerCallback!!)
                controllerCallback = object : MediaController.Callback() {

                    override fun onPlaybackStateChanged(state: PlaybackState?) {
                        super.onPlaybackStateChanged(state)
                        if (mNotificationManager == null) return
                        val isPlaying: Boolean =
                            state!!.state == PlaybackState.STATE_PLAYING
                        if (!isPlaying) {
                            mNotificationManager!!.cancel(Constants.NOTIFICATION_ID.INSTANT_LYRICS)
                        }
                        controllers?.get(0)?.let { broadcastControllerState(it, isPlaying) }
                    }
                }
                controller?.registerCallback(controllerCallback!!)
                if (controller != null) {
                    broadcastControllerState(controller, null)
                }
            }
        }
        (getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager)
            .addOnActiveSessionsChangedListener(listener!!, ComponentName(this, javaClass))
    }

    override fun onClientChange(b: Boolean) {}
    override fun onClientPlaybackStateUpdate(state: Int) {
        isRemoteControllerPlaying = state == RemoteControlClient.PLAYSTATE_PLAYING
    }

    override fun onClientPlaybackStateUpdate(
        state: Int,
        stateChangeTimeMs: Long,
        currentPosMs: Long,
        speed: Float
    ) {
        isRemoteControllerPlaying = state == RemoteControlClient.PLAYSTATE_PLAYING
        mHasBug = false
        val editor: SharedPreferences.Editor = currentMusicInfo!!.edit()
        when {
            isRemoteControllerPlaying -> {
                val currentTime: Long = System.currentTimeMillis()
                editor.putLong("startTime", currentTime)
            }
            else -> {
                mNotificationManager!!.cancel(Constants.NOTIFICATION_ID.INSTANT_LYRICS)
            }
        }
        editor.putBoolean("playing", isRemoteControllerPlaying)
        editor.apply()
    }

    override fun onClientTransportControlUpdate(i: Int) {
        if (mHasBug) {
            if (isRemoteControllerPlaying) {
                val currentTime: Long = System.currentTimeMillis()
                currentMusicInfo!!.edit().putLong("startTime", currentTime).apply()
            }
        }
    }

    override fun onClientMetadataUpdate(metadataEditor: RemoteController.MetadataEditor) {
        var position: Long = mController!!.estimatedMediaPosition
        if (position > 3600000) position = -1L
        val durationObject: Any =
            metadataEditor.getObject(MediaMetadataRetriever.METADATA_KEY_DURATION, 60000)
        val artist: String? = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST,
            metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, ""))
        val track: String = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE, "")

        //postNotification(artist, track);
        if (durationObject is Double) {
            if (artist != null && !artist.isEmpty()) broadcast(artist,
                track,
                isRemoteControllerPlaying,
                0,
                position)
        } else if (durationObject is Int) {
            if (artist != null && !artist.isEmpty()) broadcast(artist,
                track,
                isRemoteControllerPlaying,
                durationObject.toLong(),
                position)
        } else if (durationObject is Long) if (artist != null && !artist.isEmpty()) broadcast(artist,
            track,
            isRemoteControllerPlaying,
            durationObject,
            position)
    }

    private fun broadcastControllerState(controller: MediaController, isPlaying: Boolean?) {
        var isPlaying: Boolean? = isPlaying
        val metadata = controller.metadata
        val playbackState = controller.playbackState
        if (metadata == null) {
            Log.d("NotificationListener", "broadcastControllerState: metadata null ")
            return
        }
        val artist: String = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
        val track: String = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
        Log.d("NotificationListener",
            "broadcastControllerState: track info $artist : $track")
        if (isPlaying == null) isPlaying =
            playbackState != null && playbackState.state == PlaybackState.STATE_PLAYING
        val duration: Long = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        val position: Long =
            if (duration == 0L || playbackState == null) -1 else playbackState.position
        broadcast(artist, track, isPlaying, duration, position)
    }

    private fun broadcast(
        artist: String,
        track: String,
        playing: Boolean,
        duration: Long,
        position: Long
    ) {
        if (playing) {
            postNotification(artist, track)
        } else {
            mNotificationManager!!.cancel(Constants.NOTIFICATION_ID.INSTANT_LYRICS)
        }
        val editor: SharedPreferences.Editor = currentMusicInfo!!.edit()
        val currentArtist = currentMusicInfo!!.getString("artist", "")
        val currentTrack = currentMusicInfo!!.getString("track", "")
        Log.v(TAG, "$track : $artist")
        try {
            editor.putString("artist", artist)
            editor.putString("track", track)
            if (!((artist == currentArtist) && (track == currentTrack))) {
                //editor.putLong("position", position);
                LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(Constants.ACTION.UPDATE_INSTANT_LYRIC))
            }
        } catch (ignored: NullPointerException) {
        }
        editor.putBoolean("playing", playing)
        if (playing) {
            val currentTime: Long = System.currentTimeMillis()
            editor.putLong("startTime", currentTime)
        }
        editor.apply()
    }

    private fun postNotification(artist: String, track: String) {
        Executors.newSingleThreadExecutor().execute {
            val builder: NotificationCompat.Builder =
                NotificationCompat.Builder(this@NotificationListenerService, "channel_01")
                    .setAutoCancel(false)
                    .setContentTitle("Get Lyrics - AB Music")
                    .setSmallIcon(R.drawable.ic_subject_black_24dp)
                    .setContentText(String.format("%s - %s", artist, track))
                    .setContentIntent(contentIntent)
            builder.setVisibility(VISIBILITY_PUBLIC)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                /* Create or update. */
                /*NotificationChannel channel = new NotificationChannel("channel_02",
                                "Instant Lyrics",
                                NotificationManager.IMPORTANCE_LOW);
                        channel.setSound(null, null);
                        mNotificationManager.createNotificationChannel(channel);*/
                builder.setChannelId("channel_02")
            }
            val notification: Notification = builder.build()
            mNotificationManager!!.notify(Constants.NOTIFICATION_ID.INSTANT_LYRICS, notification)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        Log.v(TAG, "Notification posted$sbn")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        //Log.v(TAG,"Notification removed"+sbn.toString());
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Destroying notification listener service")
        (getSystemService(
        Context.MEDIA_SESSION_SERVICE) as MediaSessionManager)
        .removeOnActiveSessionsChangedListener(listener!!)
    }

    companion object {
        fun isListeningAuthorized(context: Context): Boolean {
            val contentResolver: ContentResolver = context.contentResolver
            val enabledNotificationListeners: String? =
                Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            val packageName: String = context.packageName
            return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(
                packageName))
        }
    }
}