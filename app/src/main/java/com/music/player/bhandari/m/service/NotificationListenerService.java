package com.music.player.bhandari.m.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.activity.ActivityInstantLyric;
import com.music.player.bhandari.m.model.Constants;

import java.util.List;
import java.util.concurrent.Executors;

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

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.KITKAT)
public class NotificationListenerService extends android.service.notification.NotificationListenerService
        implements RemoteController.OnClientUpdateListener {
    private RemoteController mController;
    private boolean isRemoteControllerPlaying;
    private boolean mHasBug = true;
    private MediaSessionManager.OnActiveSessionsChangedListener listener;
    private MediaController.Callback controllerCallback;
    private String TAG = "NotificationListenerService";
    private SharedPreferences currentMusicInfo;
    private PendingIntent contentIntent;
    private NotificationManager mNotificationManager;

    @Override
    @SuppressWarnings("NewApi")
    @TargetApi(21)
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Creating notification listener service");
        //Log.v(TAG,"Created service");
        currentMusicInfo = getSharedPreferences("current_music", Context.MODE_PRIVATE);

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, ActivityInstantLyric.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        contentIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mController = new RemoteController(this, this);
            if (!((AudioManager) getSystemService(Context.AUDIO_SERVICE)).registerRemoteController(mController)
                    && mController.clearArtworkConfiguration()) {
                throw new RuntimeException("Error while registering RemoteController!");
            }
        } else {
            listener = new MediaSessionManager.OnActiveSessionsChangedListener() {
                @Override
                public void onActiveSessionsChanged(final List<MediaController> controllers) {
                    if (controllers.size() > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        MediaController controller = controllers.get(0);

                        Log.v(TAG, controller.getPackageName());

                        if (
                                "com.google.android.youtube".equals(controller.getPackageName()) ||
                                        "com.bhandari.music".equals(controller.getPackageName()) ||
                                        "com.android.chrome".equals(controller.getPackageName()) ||
                                        "org.videolan.vlc".equals(controller.getPackageName()))
                            return;

                        if (controllerCallback != null)
                            controller.unregisterCallback(controllerCallback);
                        controllerCallback = new MediaController.Callback() {
                            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onPlaybackStateChanged(@NonNull PlaybackState state) {
                                super.onPlaybackStateChanged(state);
                                if (mNotificationManager == null) return;
                                boolean isPlaying = state.getState() == PlaybackState.STATE_PLAYING;

                                if (!isPlaying) {
                                    mNotificationManager.cancel(Constants.NOTIFICATION_ID.INSTANT_LYRICS);
                                }
                                broadcastControllerState(controllers.get(0), isPlaying);
                            }
                        };
                        controller.registerCallback(controllerCallback);
                        broadcastControllerState(controller, null);
                    }
                }
            };
            ((MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE))
                    .addOnActiveSessionsChangedListener(listener, new ComponentName(this, getClass()));
        }
    }

    @Override
    public void onClientChange(boolean b) {

    }

    @Override
    public void onClientPlaybackStateUpdate(int state) {
        this.isRemoteControllerPlaying = state == RemoteControlClient.PLAYSTATE_PLAYING;
    }

    @Override
    public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs, long currentPosMs, float speed) {
        this.isRemoteControllerPlaying = state == RemoteControlClient.PLAYSTATE_PLAYING;
        mHasBug = false;
        SharedPreferences.Editor editor = currentMusicInfo.edit();

        if (isRemoteControllerPlaying) {
            long currentTime = System.currentTimeMillis();
            editor.putLong("startTime", currentTime);
        } else {
            mNotificationManager.cancel(Constants.NOTIFICATION_ID.INSTANT_LYRICS);
        }
        editor.putBoolean("playing", isRemoteControllerPlaying);
        editor.apply();
    }

    @Override
    public void onClientTransportControlUpdate(int i) {
        if (mHasBug) {
            if (isRemoteControllerPlaying) {
                long currentTime = System.currentTimeMillis();
                currentMusicInfo.edit().putLong("startTime", currentTime).apply();
            }
        }
    }

    @Override
    public void onClientMetadataUpdate(RemoteController.MetadataEditor metadataEditor) {
        long position = mController.getEstimatedMediaPosition();
        if (position > 3600000)
            position = -1L;

        Object durationObject = metadataEditor.getObject(MediaMetadataRetriever.METADATA_KEY_DURATION, 60000);
        String artist = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST,
                metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, ""));
        String track = metadataEditor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE, "");

        //postNotification(artist, track);

        if (durationObject instanceof Double) {
            if (artist != null && !artist.isEmpty())
                broadcast(artist, track, isRemoteControllerPlaying, 0, position);
        } else if (durationObject instanceof Integer) {
            if (artist != null && !artist.isEmpty())
                broadcast(artist, track, isRemoteControllerPlaying, (Integer) durationObject, position);
        } else if (durationObject instanceof Long)
            if (artist != null && !artist.isEmpty())
                broadcast(artist, track, isRemoteControllerPlaying, (Long) durationObject, position);
    }

    @TargetApi(21)
    private void broadcastControllerState(MediaController controller, Boolean isPlaying) {
        MediaMetadata metadata = controller.getMetadata();
        PlaybackState playbackState = controller.getPlaybackState();
        if (metadata == null) {
            Log.d("NotificationListener", "broadcastControllerState: metadata null ");
            return;
        }
        String artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
        String track = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);

        Log.d("NotificationListener", "broadcastControllerState: track info " + artist + " : " + track);

        if (isPlaying == null)
            isPlaying = playbackState != null && playbackState.getState() == PlaybackState.STATE_PLAYING;

        long duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
        long position = duration == 0 || playbackState == null ? -1 : playbackState.getPosition();

        broadcast(artist, track, isPlaying, duration, position);
    }

    private void broadcast(String artist, String track, boolean playing, long duration, long position) {

        if (playing) {
            postNotification(artist, track);
        } else {
            mNotificationManager.cancel(Constants.NOTIFICATION_ID.INSTANT_LYRICS);
        }

        SharedPreferences.Editor editor = currentMusicInfo.edit();
        String currentArtist = currentMusicInfo.getString("artist", "");
        String currentTrack = currentMusicInfo.getString("track", "");

        Log.v(TAG, track + " : " + artist);


        try {
            editor.putString("artist", artist);
            editor.putString("track", track);

            if (!(artist.equals(currentArtist) && track.equals(currentTrack))) {
                //editor.putLong("position", position);
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION.UPDATE_INSTANT_LYRIC));
            }
        } catch (NullPointerException ignored) {

        }

        editor.putBoolean("playing", playing);
        if (playing) {
            long currentTime = System.currentTimeMillis();
            editor.putLong("startTime", currentTime);
        }
        editor.apply();
    }

    public static boolean isListeningAuthorized(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = context.getPackageName();

        return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName));
    }


    private void postNotification(final String artist, final String track) {

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationListenerService.this, "channel_01")
                        .setAutoCancel(false)
                        .setContentTitle("Get Lyrics - AB Music")
                        .setSmallIcon(R.drawable.ic_subject_black_24dp)
                        .setContentText(String.format("%s - %s", artist, track))
                        .setContentIntent(contentIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.setVisibility(Notification.VISIBILITY_PUBLIC);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    /* Create or update. */
                    /*NotificationChannel channel = new NotificationChannel("channel_02",
                            "Instant Lyrics",
                            NotificationManager.IMPORTANCE_LOW);
                    channel.setSound(null, null);
                    mNotificationManager.createNotificationChannel(channel);*/
                    builder.setChannelId("channel_02");
                }

                Notification notification = builder.build();

                mNotificationManager.notify(Constants.NOTIFICATION_ID.INSTANT_LYRICS, notification);
            }
        });

    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Log.v(TAG, "Notification posted" + sbn.toString());
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        //Log.v(TAG,"Notification removed"+sbn.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Destroying notification listener service");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).unregisterRemoteController(mController);
        else
            ((MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE))
                    .removeOnActiveSessionsChangedListener(listener);
    }
}
