package com.music.player.bhandari.m.service;

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

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.audiofx.PresetReverb;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.activity.ActivityMain;
import com.music.player.bhandari.m.activity.ActivityNowPlaying;
import com.music.player.bhandari.m.activity.ActivityPermissionSeek;
import com.music.player.bhandari.m.equalizer.EqualizerHelper;
import com.music.player.bhandari.m.equalizer.EqualizerSetting;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.model.PlaylistManager;
import com.music.player.bhandari.m.utils.UtilityFun;
import com.music.player.bhandari.m.widget.WidgetReceiver;
import com.squareup.seismic.ShakeDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;

public class PlayerService extends Service implements
        AudioManager.OnAudioFocusChangeListener, ShakeDetector.Listener {

    static public final int STOPPED = -1, PAUSED = 0, PLAYING = 1;
    private AudioManager mAudioManager;
    private MediaPlayer mediaPlayer;
    private boolean playAfterPrepare = false;

    //playlist (now playing)
    private ArrayList<Integer> trackList = new ArrayList<>();
    private TrackItem currentTrack;
    private int currentVolume = 0;
    private boolean fVolumeIsBeingChanged = false;

    private IntentFilter headsetFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    private HeadSetReceiver mReceiverHeadset = new HeadSetReceiver();

    private int status, currentTrackPosition;
    private IBinder playerBinder;

    private PendingIntent pendingIntent;
    private PendingIntent pSwipeToDismiss;
    private PendingIntent ppreviousIntent;
    private PendingIntent pplayIntent;
    private PendingIntent pnextIntent;
    private PendingIntent pdismissIntent;
    private NotificationManager mNotificationManager;

    //media session and related objects
    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder stateBuilder;

    private PhoneStateListener phoneStateListener;
    private BroadcastReceiver mReceiver;
    private static ShakeDetector shakeDetector;
    private static SensorManager sensorManager;

    private boolean musicPausedBecauseOfCall, musicPuasedBecauseOfFocusLoss;

    private Handler mHandler;

    //variables for measuring time between consecutive play pause button click on earphone
    private long lastTimePlayPauseClicked;

    //equlizer helper
    EqualizerHelper mEqualizerHelper;

    //Bluetooth callback receivers
    BroadcastReceiver bluetoothReceiver = new BluetoothReceiver();

    //boolean which decides weather to stop playback when either bluetooth is turned off or
    //bluetooth device is disconnected.
    static Boolean doesMusicNeedsToBePaused = false;

    @Override
    public void onCreate() {
        // MyApp.getPref().edit().putBoolean(getString(R.string.pref_remove_ads),true).apply();
        Log.d("PlayerService", "onCreate: ");
        super.onCreate();
        mHandler = new Handler();

        //for shake to play feature
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(this);
        shakeDetector.setSensitivity(ShakeDetector.SENSITIVITY_LIGHT);

        if (MyApp.getPref().getBoolean(getString(R.string.pref_shake), false)) {
            setShakeListener(true);
        }

        InitializeIntents();
        InitializeReceiver();

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            //bluetooth button control and lock screen albumName art
            InitializeMediaSession();
        }

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //initialize stuff  ///to broadcast to UI when track changes automatically
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                Log.d("PlayerService", "onCompletion: " + arg0);
                if (currentTrackPosition == trackList.size() - 1) {
                    if (MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.REPEAT_ALL) {
                        playTrack(0);
                        currentTrackPosition = 0;
                    } else if (MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.REPEAT_ONE) {
                        playTrack(currentTrackPosition);
                    } else {
                        if (MyApp.getPref().getBoolean(getString(R.string.pref_continuous_playback), false)) {
                            if (trackList.size() < 10) {
                                List<Integer> dataItems = MusicLibrary.getInstance().getDefaultTracklistNew();
                                Collections.shuffle(dataItems);
                                trackList.addAll(dataItems);
                                playTrack(currentTrackPosition + 1);
                            } else {
                                playTrack(0);
                                currentTrackPosition = 0;
                            }
                        } else {
                            stop();
                        }
                    }
                } else if (MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.REPEAT_ONE) {
                    playTrack(currentTrackPosition);
                } else {
                    nextTrack();
                    PostNotification();
                    return;  //to avoid double call to notifyUi as it is getting called from nextTrack()  Sounds stupid but it works, so it ain't stupid
                }
                notifyUI();
                PostNotification();
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (playAfterPrepare) {
                    mediaPlayer.start();
                } else {
                    Log.d("PlayerService", "onPrepared: seeking to : " + MyApp.getPref().getInt(Constants.PREFERENCES.STORED_SONG_POSITION_DURATION, 0));
                    try {
                        seekTrack(MyApp.getPref().getInt(Constants.PREFERENCES.STORED_SONG_POSITION_DURATION, 0));
                    } catch (Exception e) {
                        Log.d("PlayerService", "onPrepared: Unable to seek track");
                    }
                }
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                Log.d("PlayerService", "onError: " + mediaPlayer + " " + i + " " + i1);
                return false;
            }
        });

        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                Log.d("PlayerService", "onInfo: " + mediaPlayer);
                return false;
            }
        });

        currentTrackPosition = -1;
        setStatus(STOPPED);
        playerBinder = new PlayerBinder();

        try {
            restoreTracklist();
        } catch (Exception ignored) {
            Log.e(Constants.TAG, ignored.toString());
        }

        this.registerReceiver(mReceiverHeadset, headsetFilter);

        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Incoming call: Pause music
                    Log.v(Constants.TAG, "Ringing");
                    if (status == PLAYING) {
                        pause();
                        notifyUI();
                        musicPausedBecauseOfCall = true;
                    }
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    //Not in call: Play music
                    Log.v(Constants.TAG, "Idle");
                    if (musicPausedBecauseOfCall) {
                        play();
                        notifyUI();
                        musicPausedBecauseOfCall = false;
                    }
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //A call is dialing, active or on hold
                    Log.v(Constants.TAG, "Dialling");
                    if (status == PLAYING) {
                        pause();
                        notifyUI();
                        musicPausedBecauseOfCall = true;
                    }
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        initAudioFX();

        try {
            applyMediaPlayerEQ();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        //bluetooth callback receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        this.registerReceiver(bluetoothReceiver, filter);

        if (UtilityFun.isBluetoothHeadsetConnected()) {
            doesMusicNeedsToBePaused = true;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (MyApp.getService() == null) {
            MyApp.setService(this);
        }
        if (intent != null && intent.getAction() != null) {
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            Log.d("PlayerService", "onStartCommand: " + intent.getAction());
        } else {
            Log.d("PlayerService", "onStartCommand: null intent or no action in intent");
        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notification_channel))
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("AB Music")
                    .setAutoCancel(true);

            Notification notification = builder.build();
            startForeground(1, notification);

        }*/
        return START_STICKY;
    }

    private void InitializeReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d("PlayerService", "onReceive: action " + action);
                if (action == null) {
                    return;
                }
                switch (action) {
                    case Constants.ACTION.PLAY_PAUSE_ACTION:
                        Log.v("Widget", "play");
                        play();
                        notifyUI();
                        break;

                    case Constants.ACTION.PREV_ACTION:
                        prevTrack();
                        //notifyUI();
                        break;

                    case Constants.ACTION.NEXT_ACTION:
                        nextTrack();
                        //notifyUI();
                        break;

                    case Constants.ACTION.DISMISS_EVENT:
                        if (getStatus() == PLAYING) {
                            mediaPlayer.pause();
                            setStatus(PAUSED);
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                                        .setActions(
                                                PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID | PlaybackStateCompat.ACTION_PAUSE |
                                                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                                        .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1)
                                        .build();
                                mMediaSession.setPlaybackState(state);
                            }
                        }
                        // even if music is stopped because of focus loss, don't allow to resume playback after clicking close button
                        musicPuasedBecauseOfFocusLoss = false;
                        mNotificationManager.cancel(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE);
                        notifyUI();
                        setShakeListener(false);
                        stopForeground(true);
                        stopSelf();
                        break;

                    case Constants.ACTION.SWIPE_TO_DISMISS:
                        musicPuasedBecauseOfFocusLoss = false;
                        setShakeListener(false);
                        break;

                    case Constants.ACTION.REFRESH_LIB:
                        if (currentTrack == null || trackList.isEmpty()) {
                            trackList.clear();
                            trackList.addAll(MusicLibrary.getInstance().getDefaultTracklistNew());
                            if (!trackList.isEmpty()) {
                                try {
                                    if (trackList.size() == 1) {
                                        //to avoid sending 0 to nextInt function
                                        currentTrack = MusicLibrary.getInstance().getTrackItemFromId(trackList.get(0));
                                        currentTrackPosition = 1;
                                    } else {
                                        int random = new Random().nextInt(trackList.size() - 1);
                                        currentTrack = MusicLibrary.getInstance().getTrackItemFromId(trackList.get(random));
                                        currentTrackPosition = random;
                                    }
                                } catch (Exception ignored) {

                                }
                            } else {
                                currentTrackPosition = -1;
                                //Toast.makeText(getApplicationContext(),"Empty library!",Toast.LENGTH_LONG).show();
                            }
                            notifyUI();
                        }
                        break;

                    case Constants.ACTION.WIDGET_UPDATE:
                        updateWidget(true);
                        break;

                    case Constants.ACTION.LAUNCH_PLAYER_FROM_WIDGET:
                        Log.v(Constants.TAG, "Luaanch now playing from service");
                        //permission seek activity is used here to show splash screen
                        startActivity(new Intent(getApplicationContext(), ActivityPermissionSeek.class).addFlags(FLAG_ACTIVITY_NEW_TASK));
                        break;

                    case Constants.ACTION.SHUFFLE_WIDGET:
                        if (MyApp.getPref().getBoolean(Constants.PREFERENCES.SHUFFLE, false)) {
                            //shuffle is on, turn it off
                            //Toast.makeText(this, "shuffle off", Toast.LENGTH_SHORT).show();
                            MyApp.getPref().edit().putBoolean(Constants.PREFERENCES.SHUFFLE, false).apply();
                            shuffle(false);
                        } else {
                            //shuffle is off, turn it on
                            MyApp.getPref().edit().putBoolean(Constants.PREFERENCES.SHUFFLE, true).apply();
                            shuffle(true);
                        }
                        updateWidget(false);
                        break;

                    case Constants.ACTION.REPEAT_WIDGET:
                        SharedPreferences pref = MyApp.getPref();
                        if (pref.getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.NO_REPEAT) {
                            pref.edit().putInt(Constants.PREFERENCES.REPEAT, Constants.PREFERENCE_VALUES.REPEAT_ALL).apply();
                        } else if (pref.getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.REPEAT_ALL) {
                            pref.edit().putInt(Constants.PREFERENCES.REPEAT, Constants.PREFERENCE_VALUES.REPEAT_ONE).apply();
                        } else if (pref.getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.REPEAT_ONE) {
                            pref.edit().putInt(Constants.PREFERENCES.REPEAT, Constants.PREFERENCE_VALUES.NO_REPEAT).apply();
                        }
                        updateWidget(false);
                        break;

                    case Constants.ACTION.FAV_WIDGET:
                        if (getCurrentTrack() == null) return;
                        if (PlaylistManager.getInstance(getApplicationContext()).isFavNew(getCurrentTrack().getId())) {
                            PlaylistManager.getInstance(getApplicationContext()).RemoveFromFavNew(getCurrentTrack().getId());
                        } else {
                            PlaylistManager.getInstance(getApplicationContext())
                                    .addSongToFav(getCurrentTrack().getId());
                        }
                        updateWidget(false);
                        break;
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(Constants.ACTION.PLAY_PAUSE_ACTION);
        intentFilter.addAction(Constants.ACTION.PREV_ACTION);
        intentFilter.addAction(Constants.ACTION.NEXT_ACTION);
        intentFilter.addAction(Constants.ACTION.DISMISS_EVENT);
        intentFilter.addAction(Constants.ACTION.SWIPE_TO_DISMISS);
        intentFilter.addAction(Constants.ACTION.REFRESH_LIB);
        intentFilter.addAction(Constants.ACTION.LAUNCH_PLAYER_FROM_WIDGET);
        intentFilter.addAction(Constants.ACTION.WIDGET_UPDATE);
        intentFilter.addAction(Constants.ACTION.SHUFFLE_WIDGET);
        intentFilter.addAction(Constants.ACTION.REPEAT_WIDGET);
        ;
        intentFilter.addAction(Constants.ACTION.FAV_WIDGET);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, intentFilter);
    }

    private void InitializeIntents() {
        //Notification intents
        Intent notificationIntent;
        if (MyApp.getPref().getInt(getString(R.string.pref_click_on_notif)
                , Constants.CLICK_ON_NOTIF.OPEN_LIBRARY_VIEW) == Constants.CLICK_ON_NOTIF.OPEN_LIBRARY_VIEW) {
            notificationIntent = new Intent(this, ActivityMain.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, PendingIntent.FLAG_MUTABLE);
        } else if (MyApp.getPref().getInt(getString(R.string.pref_click_on_notif)
                , Constants.CLICK_ON_NOTIF.OPEN_LIBRARY_VIEW) == Constants.CLICK_ON_NOTIF.OPEN_DISC_VIEW) {
            notificationIntent = new Intent(this, ActivityNowPlaying.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, PendingIntent.FLAG_MUTABLE);
        }

        Intent favIntent = new Intent(this, PlayerService.class);
        favIntent.setAction(Constants.ACTION.FAV_ACTION);
        PendingIntent pfavintent = PendingIntent.getService(this, 0,
                favIntent, PendingIntent.FLAG_MUTABLE);

        Intent previousIntent = new Intent(this, PlayerService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, PendingIntent.FLAG_MUTABLE);

        Intent playIntent = new Intent(this, PlayerService.class);
        playIntent.setAction(Constants.ACTION.PLAY_PAUSE_ACTION);
        pplayIntent = PendingIntent.getService(this, 0,
                playIntent, PendingIntent.FLAG_MUTABLE);

        Intent nextIntent = new Intent(this, PlayerService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, PendingIntent.FLAG_MUTABLE);

        Intent dismissIntent = new Intent(this, PlayerService.class);
        dismissIntent.setAction(Constants.ACTION.DISMISS_EVENT);
        pdismissIntent = PendingIntent.getService(this, 0,
                dismissIntent, PendingIntent.FLAG_MUTABLE);

        Intent swipeToDismissIntent = new Intent(this, PlayerService.class);
        swipeToDismissIntent.setAction(Constants.ACTION.SWIPE_TO_DISMISS);
        pSwipeToDismiss = PendingIntent.getService(this, 0, swipeToDismissIntent, PendingIntent.FLAG_MUTABLE);

    }

    @TargetApi(21)
    private void InitializeMediaSession() {
        mMediaSession = new MediaSessionCompat(getApplicationContext(), getPackageName() + "." + TAG);
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
                Log.d(TAG, "onMediaButtonEvent called: " + mediaButtonIntent);
                return super.onMediaButtonEvent(mediaButtonIntent);
            }

            public void onPause() {
                Log.d(TAG, "onPause called (media button pressed)");
                onPlayPauseButtonClicked();
                super.onPause();
            }

            @Override
            public void onFastForward() {
                Log.d(TAG, "onFastForward: called");
                super.onFastForward();
            }

            @Override
            public void onCommand(String command, Bundle extras, ResultReceiver cb) {
                Log.d(TAG, "onCommand: " + command);
                super.onCommand(command, extras, cb);
            }

            @Override
            public void onSeekTo(long pos) {
                Log.d(TAG, "onSeekTo: called " + pos);
                seekTrack((int) pos);
                super.onSeekTo(pos);
            }

            @Override
            public void onRewind() {
                Log.d(TAG, "onRewind: called");
                super.onRewind();
            }

            public void onSkipToPrevious() {
                Log.d(TAG, "onskiptoPrevious called (media button pressed)");
                prevTrack();
                super.onSkipToPrevious();
            }

            public void onSkipToNext() {
                Log.d(TAG, "onskiptonext called (media button pressed)");
                nextTrack();
                super.onSkipToNext();
            }

            public void onPlay() {
                Log.d(TAG, "onPlay called (media button pressed)");
                onPlayPauseButtonClicked();
                super.onPlay();
            }

            public void onStop() {
                stop();
                notifyUI();
                Log.d(TAG, "onStop called (media button pressed)");
                super.onStop();
            }

            private void onPlayPauseButtonClicked() {

                //if pressed multiple times in 500 ms, skip to next song
                long currentTime = System.currentTimeMillis();
                Log.d(TAG, "onPlay: " + lastTimePlayPauseClicked + " current " + currentTime);
                if (currentTime - lastTimePlayPauseClicked < 500) {
                    Log.d(TAG, "onPlay: nextTrack on multiple play pause click");
                    nextTrack();
                    //notifyUI();
                    return;
                }

                lastTimePlayPauseClicked = System.currentTimeMillis();
                play();
                notifyUI();
            }
        });
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SEEK_TO |
                                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID | PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        PlaybackStateCompat state = stateBuilder
                .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1)
                .build();

        mMediaSession.setPlaybackState(state);
        mMediaSession.setActive(true);
    }

    /**
     * Initializes the equalizer and audio effects for this service session.
     */
    public void initAudioFX() {

        try {
            //Instatiate the equalizer helper object.
            mEqualizerHelper = new EqualizerHelper(getApplicationContext(), mediaPlayer.getAudioSessionId(),
                    MyApp.getPref().getBoolean("pref_equ_enabled", true));

        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void applyMediaPlayerEQ() {

        if (mEqualizerHelper == null || !mEqualizerHelper.isEqualizerSupported())
            return;

        EqualizerSetting equalizerSetting = mEqualizerHelper.getLastEquSetting();
        if (equalizerSetting == null) return;

        Log.d("PlayerService", "applyMediaPlayerEQ: applying equ setting " + equalizerSetting.toString());
        int fiftyHertzBandValue = equalizerSetting.getFiftyHertz();
        int oneThirtyHertzBandValue = equalizerSetting.getFiftyHertz();
        int threeTwentyHertzBandValue = equalizerSetting.getFiftyHertz();
        int eightHundredHertzBandValue = equalizerSetting.getFiftyHertz();
        int twoKilohertzBandValue = equalizerSetting.getFiftyHertz();
        int fiveKilohertzBandValue = equalizerSetting.getFiftyHertz();
        int twelvePointFiveKilohertzBandValue = equalizerSetting.getFiftyHertz();
        short virtualizerLevelValue = (short) equalizerSetting.getVirtualizer();
        short bassboostValue = (short) equalizerSetting.getBassBoost();
        short reverbValue = (short) equalizerSetting.getReverb();

        short fiftyHertzBand = mEqualizerHelper.getEqualizer().getBand(50000);
        short oneThirtyHertzBand = mEqualizerHelper.getEqualizer().getBand(130000);
        short threeTwentyHertzBand = mEqualizerHelper.getEqualizer().getBand(320000);
        short eightHundredHertzBand = mEqualizerHelper.getEqualizer().getBand(800000);
        short twoKilohertzBand = mEqualizerHelper.getEqualizer().getBand(2000000);
        short fiveKilohertzBand = mEqualizerHelper.getEqualizer().getBand(5000000);
        short twelvePointFiveKilohertzBand = mEqualizerHelper.getEqualizer().getBand(9000000);


        //50Hz Band.
        if (fiftyHertzBandValue == 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) 0);
        } else if (fiftyHertzBandValue < 16) {

            if (fiftyHertzBandValue == 0) {
                mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) (-(16 - fiftyHertzBandValue) * 100));
            }

        } else if (fiftyHertzBandValue > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) ((fiftyHertzBandValue - 16) * 100));
        }

        //130Hz Band.
        if (oneThirtyHertzBandValue == 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) 0);
        } else if (oneThirtyHertzBandValue < 16) {

            if (oneThirtyHertzBandValue == 0) {
                mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) (-(16 - oneThirtyHertzBandValue) * 100));
            }

        } else if (oneThirtyHertzBandValue > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) ((oneThirtyHertzBandValue - 16) * 100));
        }

        //320Hz Band.
        if (threeTwentyHertzBandValue == 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) 0);
        } else if (threeTwentyHertzBandValue < 16) {

            if (threeTwentyHertzBandValue == 0) {
                mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) (-(16 - threeTwentyHertzBandValue) * 100));
            }

        } else if (threeTwentyHertzBandValue > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) ((threeTwentyHertzBandValue - 16) * 100));
        }

        //800Hz Band.
        if (eightHundredHertzBandValue == 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) 0);
        } else if (eightHundredHertzBandValue < 16) {

            if (eightHundredHertzBandValue == 0) {
                mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) (-(16 - eightHundredHertzBandValue) * 100));
            }

        } else if (eightHundredHertzBandValue > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) ((eightHundredHertzBandValue - 16) * 100));
        }

        //2kHz Band.
        if (twoKilohertzBandValue == 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) 0);
        } else if (twoKilohertzBandValue < 16) {

            if (twoKilohertzBandValue == 0) {
                mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) (-(16 - twoKilohertzBandValue) * 100));
            }

        } else if (twoKilohertzBandValue > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) ((twoKilohertzBandValue - 16) * 100));
        }

        //5kHz Band.
        if (fiveKilohertzBandValue == 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) 0);
        } else if (fiveKilohertzBandValue < 16) {

            if (fiveKilohertzBandValue == 0) {
                mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) (-(16 - fiveKilohertzBandValue) * 100));
            }

        } else if (fiveKilohertzBandValue > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) ((fiveKilohertzBandValue - 16) * 100));
        }

        //12.5kHz Band.
        if (twelvePointFiveKilohertzBandValue == 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) 0);
        } else if (twelvePointFiveKilohertzBandValue < 16) {

            if (twelvePointFiveKilohertzBandValue == 0) {
                mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) (-(16 - twelvePointFiveKilohertzBandValue) * 100));
            }

        } else if (twelvePointFiveKilohertzBandValue > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) ((twelvePointFiveKilohertzBandValue - 16) * 100));
        }

        //Set the audioFX values.
        mEqualizerHelper.getVirtualizer().setStrength(virtualizerLevelValue);
        mEqualizerHelper.getBassBoost().setStrength(bassboostValue);

        if (reverbValue == 0) {
            mEqualizerHelper.getPresetReverb().setPreset(PresetReverb.PRESET_NONE);
        } else if (reverbValue == 1) {
            mEqualizerHelper.getPresetReverb().setPreset(PresetReverb.PRESET_LARGEHALL);
        } else if (reverbValue == 2) {
            mEqualizerHelper.getPresetReverb().setPreset(PresetReverb.PRESET_LARGEROOM);
        } else if (reverbValue == 3) {
            mEqualizerHelper.getPresetReverb().setPreset(PresetReverb.PRESET_MEDIUMHALL);
        } else if (reverbValue == 4) {
            mEqualizerHelper.getPresetReverb().setPreset(PresetReverb.PRESET_MEDIUMROOM);
        } else if (reverbValue == 5) {
            mEqualizerHelper.getPresetReverb().setPreset(PresetReverb.PRESET_SMALLROOM);
        } else if (reverbValue == 6) {
            mEqualizerHelper.getPresetReverb().setPreset(PresetReverb.PRESET_PLATE);
        }

    }

    public static void setShakeListener(boolean status) {
        if (status) {
            shakeDetector.start(sensorManager);
        } else {
            try {
                shakeDetector.stop();
            } catch (Exception ignored) {

            }
        }

    }

    public void PostNotification() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {

                if (currentTrack == null) {
                    return;
                }

                Bitmap b = null;
                try {
                    b = UtilityFun.decodeUri(getApplication()
                            , MusicLibrary.getInstance().getAlbumArtUri(getCurrentTrack().getAlbumId()), 200);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (b != null) {
                    int width = b.getWidth();
                    int height = b.getHeight();
                    int maxWidth = 300;
                    int maxHeight = 300;
                    if (width > height) {
                        // landscape
                        float ratio = (float) width / maxWidth;
                        width = maxWidth;
                        height = (int) (height / ratio);
                    } else if (height > width) {
                        // portrait
                        float ratio = (float) height / maxHeight;
                        height = maxHeight;
                        width = (int) (width / ratio);
                    } else {
                        // square
                        height = maxHeight;
                        width = maxWidth;
                    }
                    b = Bitmap.createScaledBitmap(b, width, height, false);
                }

                String secondaryText = "";
                if (sleepTimerMinutes != 0) {
                    secondaryText = sleepTimerMinutes - sleepTimeAlreadyOver + getString(R.string.notif_minutes_to_sleep);
                } else {
                    //show up next song instead
                    // see if song is there to play next
                    secondaryText = getString(R.string.next_track);
                    if (currentTrackPosition == trackList.size() - 1) {
                        secondaryText += getString(R.string.empty_queue);
                    } else {
                        try {
                            secondaryText += MusicLibrary.getInstance().getTrackItemFromId(trackList.get(currentTrackPosition + 1)).getTitle();
                        } catch (IndexOutOfBoundsException e) {
                            Log.v(Constants.TAG, e.toString());
                        } catch (Exception ignored) {

                        }
                    }
                }

                String trackInfo = currentTrack.getArtist() + " - " + currentTrack.getTitle();

                final Notification notification;

                androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2);

                if (mMediaSession != null) {
                    mediaStyle.setMediaSession(mMediaSession.getSessionToken());
                }


                //For lollipop above devices, use media style notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(PlayerService.this, getString(R.string.notification_channel))
                        .setSmallIcon(R.drawable.ic_batman_kitkat)
                        .setContentTitle(trackInfo)
                        .setContentText(secondaryText)
                        .setContentIntent(pendingIntent)
                        .setDeleteIntent(pSwipeToDismiss)
                        .setAutoCancel(false);

                //posting notification fails for huawei devices in case of mediastyle notification
                boolean isHuawei = (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1
                        || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP)
                        && Build.MANUFACTURER.toLowerCase(Locale.getDefault()).contains("huawei");
                if (!isHuawei) {
                    builder.setStyle(mediaStyle);
                }

                if (b != null) {
                    builder.setLargeIcon(b);
                } else {
                    builder.setColor(ColorHelper.getWidgetColor());
                }

                builder.addAction(new NotificationCompat.Action(R.drawable.ic_skip_previous_black_24dp, "Prev", ppreviousIntent));

                if (status == PLAYING) {
                    builder.addAction(new NotificationCompat.Action(R.drawable.ic_pause_black_24dp, "Pause", pplayIntent));
                } else {
                    builder.addAction(new NotificationCompat.Action(R.drawable.ic_play_arrow_black_24dp, "Play", pplayIntent));
                }

                builder.addAction(new NotificationCompat.Action(R.drawable.ic_skip_next_black_24dp, "Next", pnextIntent));

                builder.addAction(new NotificationCompat.Action(R.drawable.ic_close_white_24dp, "Close", pdismissIntent));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                }

                builder.setPriority(Notification.PRIORITY_MAX);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setChannelId(getString(R.string.notification_channel));
                }

                notification = builder.build();

                if (getStatus() == PLAYING) {
                    //builder.setOngoing(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
                    } else {
                        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
                    }
                } else {
                    //stopForeground(false);
                    mNotificationManager.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
                }


            }
        });
    }


    //get played song position, shuffle the list and get the played song at first position
    //this will be called when song is played from music library
    private void shuffleTracklist(int position) {
        if (!trackList.isEmpty()) {
            Integer originalSongPlayed = trackList.get(position);
            Collections.shuffle(trackList);
            trackList.remove(originalSongPlayed);
            trackList.add(position, originalSongPlayed);
        }
    }

    //this will be called when clicked on shuffle button on now playing
    public void shuffle(boolean shuffleStatus) {
        if (!trackList.isEmpty()) {
            Integer currentSongPlaying = trackList.get(currentTrackPosition);
            if (shuffleStatus) {
                Collections.shuffle(trackList);
                trackList.remove(currentSongPlaying);
                trackList.add(0, currentSongPlaying);
                currentTrackPosition = 0;
            } else {
                long time = System.currentTimeMillis();
                Collections.sort(trackList, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer integer, Integer t1) {
                        try {
                            return MusicLibrary.getInstance().getTrackMap().get(integer)
                                    .compareToIgnoreCase(MusicLibrary.getInstance().getTrackMap().get(t1));
                        } catch (NullPointerException e) {
                            return 0;
                        }
                    }
                });
                Log.d(TAG, "shuffle: sorted in " + (System.currentTimeMillis() - time));
                currentTrackPosition = trackList.indexOf(currentSongPlaying);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return playerBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void swapPosition(int from, int to) {
        if (!trackList.isEmpty()) {
            int currentTrack = trackList.get(currentTrackPosition);
            Collections.swap(trackList, from, to);
            currentTrackPosition = trackList.indexOf(currentTrack);
        }
    }

    public void removeTrack(int position) {
        if (currentTrackPosition > position) {
            currentTrackPosition--;
        }
        try {
            trackList.remove(position);
        } catch (ArrayIndexOutOfBoundsException ignored) {

        }
    }

    public void removeTrack(Integer _id) {
        if (trackList.contains(_id)) {
            int position = trackList.indexOf(_id);
            if (currentTrackPosition > position) {
                currentTrackPosition--;
            }
            trackList.remove(_id);
        }
    }

    private void notifyUI() {
        Intent UIIntent = new Intent();
        UIIntent.setAction(Constants.ACTION.COMPLETE_UI_UPDATE);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(UIIntent);
        try {
            updateWidget(true);
        } catch (Exception ignored) {

        }
    }

    public void updateWidget(boolean loadBitmap) {
        Log.d("PlayerService", "updateWidget: called");
        Context context = this;
        if (getCurrentTrack() == null) {
            Log.d("PlayerService", "updateWidget: failed because of null current track");
            return;
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wigdet);
        views.setTextViewText(R.id.song_name_widget, getCurrentTrack().getTitle());
        views.setTextViewText(R.id.artist_widget, getCurrentTrack().getArtist());

        if (loadBitmap) {
            Bitmap b = null;
            try {
                b = UtilityFun.decodeUri(this
                        , MusicLibrary.getInstance().getAlbumArtUri(getCurrentTrack().getAlbumId()), 200);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            if (b != null) {
                views.setImageViewBitmap(R.id.widget_album_art, b);
            } else {
                views.setImageViewBitmap(R.id.widget_album_art, UtilityFun.drawableToBitmap(UtilityFun.getDefaultAlbumArtDrawable()));
                //views.setImageViewResource(R.id.widget_album_art, R.drawable.ic_batman_1);
            }
        }

        if (status == PLAYING) {
            views.setImageViewResource(R.id.widget_Play, R.drawable.ic_pause_black_24dp);
        } else {
            views.setImageViewResource(R.id.widget_Play, R.drawable.ic_play_arrow_black_24dp);
        }

        if (MyApp.getPref().getBoolean(Constants.PREFERENCES.SHUFFLE, false)) {
            views.setInt(R.id.widget_shuffle, "setColorFilter", ColorHelper.getColor(R.color.colorwhite));
        } else {
            views.setInt(R.id.widget_shuffle, "setColorFilter", ColorHelper.getColor(R.color.gray3));
        }

        views.setTextColor(R.id.text_in_repeat_widget, ColorHelper.getColor(R.color.colorwhite));

        if (MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.REPEAT_ALL) {
            views.setTextViewText(R.id.text_in_repeat_widget, "A");
            views.setInt(R.id.widget_repeat, "setColorFilter", ColorHelper.getColor(R.color.colorwhite));
        } else if (MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.REPEAT_ONE) {
            views.setTextViewText(R.id.text_in_repeat_widget, "1");
            views.setInt(R.id.text_in_repeat_widget, "setTextColor", ColorHelper.getColor(R.color.colorwhite));
            views.setInt(R.id.widget_repeat, "setColorFilter", ColorHelper.getColor(R.color.colorwhite));
        } else if (MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.NO_REPEAT) {
            views.setTextViewText(R.id.text_in_repeat_widget, "");
            views.setInt(R.id.widget_repeat, "setColorFilter", ColorHelper.getColor(R.color.dark_gray3));
        }

        if (getCurrentTrack() != null && PlaylistManager.getInstance(getApplicationContext()).isFavNew(getCurrentTrack().getId())) {
            //views.setInt(R.id.widget_fav, "setColorFilter", ColorHelper.GetWidgetColor());
            views.setImageViewResource(R.id.widget_fav, R.drawable.ic_favorite_black_24dp);
        } else {
            //views.setInt(R.id.widget_fav, "setColorFilter", ColorHelper.getColor(R.color.colorwhite));
            views.setImageViewResource(R.id.widget_fav, R.drawable.ic_favorite_border_black_24dp);
        }

        ComponentName thisWidget = new ComponentName(context, WidgetReceiver.class);
        appWidgetManager.updateAppWidget(thisWidget, views);
    }

    private void setStatus(int s) {
        switch (s) {
            case PLAYING:
                Log.d("PlayerService", "setStatus: Playing");
                //if(BluetoothDevice)
                break;

            case PAUSED:
                Log.d("PlayerService", "setStatus: Paused");
                break;

            case STOPPED:
                Log.d("PlayerService", "setStatus: Stopped");
                break;
        }
        status = s;
    }

    public int getStatus() {
        return status;
    }

    public TrackItem getCurrentTrack() {
        if (currentTrackPosition < 0) {
            return null;
        } else {
            return currentTrack;
        }
    }

    public int getCurrentTrackPosition() {
        return currentTrackPosition;
    }

    public ArrayList<Integer> getTrackList() {
        return trackList;
    }

    @TargetApi(21)
    public void setMediaSessionMetadata(final boolean enable) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (currentTrack == null) return;
                MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, currentTrack.getTitle());
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, currentTrack.getArtist());
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM, currentTrack.getAlbum());
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI
                        , MusicLibrary.getInstance().getAlbumArtUri(currentTrack.getAlbumId()).toString());
                metadataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, currentTrack.getDurInt());
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_GENRE, currentTrack.getGenre());
                if (MyApp.getPref().getBoolean(getString(R.string.pref_lock_screen_album_Art), true)) {
                    if (enable) {
                        Bitmap b = null;
                        try {
                            b = UtilityFun.decodeUri(getApplication()
                                    , MusicLibrary.getInstance().getAlbumArtUri(getCurrentTrack().getAlbumId()), 200);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, b);
                    } else {
                        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, null);
                    }
                }
                mMediaSession.setMetadata(metadataBuilder.build());
            }
        });
    }

    public void playTrack(int pos) {

        try {
            trackList.get(pos);
        } catch (Exception ignored) {
            return;
        }

        TrackItem temp = MusicLibrary.getInstance().getTrackItemFromId(trackList.get(pos));
        if (temp == null) {
            Handler h = new Handler(getApplicationContext().getMainLooper());

            h.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext()
                            , getString(R.string.error_playing_track)
                            , Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        currentTrack = temp;


        //here
        PlaylistManager.getInstance(getApplicationContext()).AddToRecentlyPlayedAndUpdateCount(trackList.get(pos));

        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return;
        }

        //currentTrack=trackInfoFetcher.get(trackList.get(pos)).iterator().next();

        gradualIncreaseVolume();

        if (status > STOPPED) {
            stop();
        }
        FileInputStream file;
        try {
            file = new FileInputStream(new File(currentTrack.getFilePath()));
            mediaPlayer.setDataSource(file.getFD());
            mediaPlayer.prepareAsync();
            playAfterPrepare = true;
            //mediaPlayer.prepare();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentTrackPosition = pos;
        setStatus(PLAYING);
        if (currentTrack != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            setSessionState();
            setMediaSessionMetadata(true);
        }

        //sometimes player may shut down abruptly, make sure you save the current queue every time song changes
        //storeTracklist();
    }

    private void setSessionState() {
        //set state play
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            if (status == PLAYING) {
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, getCurrentTrackProgress(), 1);
            } else if (status == PAUSED) {
                stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, getCurrentTrackProgress(), 1);
            } else {
                stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, 0, 1);
            }
            mMediaSession.setPlaybackState(stateBuilder.build());
        }
    }

    public void playAtPosition(int position) {
        if (MyApp.getPref().getBoolean(Constants.PREFERENCES.SHUFFLE, false)) {
            shuffleTracklist(position);
            playTrack(position);
        } else {
            playTrack(position);
        }
        PostNotification();
        notifyUI();
    }

    public void playAtPositionFromNowPlaying(int position) {
        playTrack(position);
        PostNotification();

        Intent intent = new Intent().setAction(Constants.ACTION.COMPLETE_UI_UPDATE);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        try {
            updateWidget(true);
        } catch (Exception ignored) {

        }
    }

    public void play() {
        Log.d("PlayerService", "play: current status " + status);
        switch (status) {
            case STOPPED:
                if (!trackList.isEmpty()) {
                    playTrack(currentTrackPosition);
                    notifyUI();
                }
                break;

            case PLAYING:
                try {
                    mediaPlayer.pause();
                    setStatus(PAUSED);
                    setSessionState();
                } catch (IllegalStateException ignored) {
                }
                break;

            case PAUSED:
                gradualIncreaseVolume();
                try {
                    mediaPlayer.start();
                    setStatus(PLAYING);
                    setSessionState();
                } catch (IllegalStateException ignored) {
                }
                break;
        }
        PostNotification();
        updateWidget(false);
    }

    public void pause() {
        try {
            mediaPlayer.pause();
        } catch (IllegalStateException ignored) {

        }
        setStatus(PAUSED);
        setSessionState();
        PostNotification();
        updateWidget(false);
    }

    public void stop() {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            mediaPlayer.stop();
            mediaPlayer.reset();
            setStatus(STOPPED);
        } catch (IllegalStateException e) {
            setStatus(STOPPED);
        }
        updateWidget(false);
    }

    public void shuffleAll() {
        ArrayList<Integer> tempList = MusicLibrary.getInstance().getDefaultTracklistNew();
        if (tempList != null) {
            Collections.shuffle(tempList);
            setTrackList(tempList);
            playTrack(0);
            notifyUI();
            PostNotification();
        }
    }


    /*
    add songs to playlist for
    arguments:  clickedON = header string to process
                status = which fragment  (title,artist,albumName,genre)
                whereToAdd = position where to add (immediately, atLast)
     */
    public void addToQ(int clickedOn, int whereToAdd) {
        int addPosition = (whereToAdd == Constants.ADD_TO_Q.IMMEDIATE_NEXT ? currentTrackPosition : trackList.size() - 1);
        try {
            trackList.add(addPosition + 1, clickedOn);
        } catch (ArrayIndexOutOfBoundsException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_adding_song_to_q), Toast.LENGTH_LONG).show();
        }
    }

    public void nextTrack() {

//        Log.v(Constants.TAG,"Next "+Log.getStackTraceString(new Exception()));

        if (MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.REPEAT_ONE) {
            playTrack(currentTrackPosition);
        } else if (currentTrackPosition < trackList.size() - 1) {
            playTrack(currentTrackPosition + 1);
        } else if (currentTrackPosition == trackList.size() - 1) {
            //if repeat all on, play first song
            if (MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.REPEAT_ALL) {
                playTrack(0);
                currentTrackPosition = 0;
            } else {
                if (MyApp.getPref().getBoolean(getString(R.string.pref_continuous_playback), false)) {
                    if (trackList.size() < 10) {
                        List<Integer> dataItems = MusicLibrary.getInstance().getDefaultTracklistNew();
                        Collections.shuffle(dataItems);
                        trackList.addAll(dataItems);
                        playTrack(currentTrackPosition + 1);
                    } else {
                        playTrack(0);
                        currentTrackPosition = 0;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.empty_queue), Toast.LENGTH_LONG).show();
                }
            }
        }
        PostNotification();
        notifyUI();
    }

    public void prevTrack() {
        if (((float) getCurrentTrackProgress() / (float) getCurrentTrackDuration())
                > Constants.PREFERENCE_VALUES.PREV_ACT_TIME_CONSTANT) {
            //start same song from start
            seekTrack(0);
        } else if (currentTrackPosition > 0) {
            playTrack(currentTrackPosition - 1);
        } else if (currentTrackPosition == 0) {
            //if repeat all on, play first song
            if (MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.REPEAT_ALL) {
                playTrack(trackList.size() - 1);
                currentTrackPosition = trackList.size() - 1;
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.empty_queue), Toast.LENGTH_LONG).show();
            }
        }
        PostNotification();
        notifyUI();
    }

    //to update trackitem when tags are changed
    public void updateTrackItem(int position, int _id, String... param) {
        if (position == getCurrentTrackPosition()) {
            try {
                currentTrack.setTitle(param[0]);
                currentTrack.setArtist(param[1]);
                currentTrack.setAlbum(param[2]);
                currentTrack.setId(_id);
            } catch (Exception ignored) {

            }
        }
    }

    public int getCurrentTrackProgress() {
        if (status > STOPPED) {
            try {
                return mediaPlayer.getCurrentPosition();
            } catch (IllegalStateException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    //returns duration of current item in ms
    public int getCurrentTrackDuration() {

        if (currentTrackPosition != -1 && currentTrack != null) {
            return currentTrack.getDurInt();
        } else {
            return 0;
        }
    }

    public EqualizerHelper getEqualizerHelper() {
        return mEqualizerHelper;
    }

    public void seekTrack(int p) {
        Log.d("Seek to", "Seek to " + p);
        if (status > STOPPED) {
            try {
                mediaPlayer.seekTo(p);
                setSessionState();
            } catch (IllegalStateException ignored) {

            }
        }
    }

    public void setTrackList(ArrayList<Integer> tracklist1) {
        this.trackList.clear();
        this.trackList.addAll(tracklist1);
    }

    /*
    sleep timer runnable and variables
     */
    private int sleepTimerMinutes = 0;   //sleep timer minutes
    private int sleepTimeAlreadyOver = -1;  //already over minutes
    private Handler sleepTimerHandler = new Handler();

    public void setSleepTimer(int minutes, boolean enable) {
        sleepTimerHandler.removeCallbacksAndMessages(null);
        sleepTimeAlreadyOver = -1;
        sleepTimerMinutes = minutes;
        if (!enable) {
            PostNotification();
            return;
        }

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (sleepTimerMinutes == ++sleepTimeAlreadyOver) {
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(new Intent().setAction(Constants.ACTION.DISMISS_EVENT));
                    Toast.makeText(getApplicationContext(), getString(R.string.timer_over), Toast.LENGTH_LONG).show();
                    MyApp.getPref().edit().putInt(getString(R.string.pref_sleep_timer), 0).apply();
                    sleepTimerMinutes = 0;
                    sleepTimeAlreadyOver = 0;
                } else {
                    if (getStatus() == PLAYING) {
                        PostNotification();
                    }
                    sleepTimerHandler.postDelayed(this, 1000 * 60);
                }
            }
        };
        sleepTimerHandler.post(runnable);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (getStatus() != PLAYING) {
            if (MyApp.getPref().getInt(getString(R.string.pref_sleep_timer), 0) != 0) {
                MyApp.getPref().edit().putInt(getString(R.string.pref_sleep_timer), 0).apply();
                sleepTimerHandler.removeCallbacksAndMessages(null);
            }
            stopForeground(true);
            stopSelf();
            Log.d(TAG, "onTaskRemoved: Stopping player service");
        }
    }

    @Override
    public void onDestroy() {
        Log.d("PlayerService", "onDestroy: ");
        updateWidget(false);
        storeTracklist();
        //mNotificationManager.cancelAll();
        mNotificationManager.cancel(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE);

        if (mEqualizerHelper != null) {
            mEqualizerHelper.releaseEqObjects();
            mEqualizerHelper = null;
        }

        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStatus(STOPPED);
            setSessionState();
            mMediaSession.setActive(false);
            mMediaSession.release();
        }
        shakeDetector.stop();
        this.unregisterReceiver(mReceiverHeadset);
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
        mAudioManager.abandonAudioFocus(this);

        unregisterReceiver(bluetoothReceiver);

        MyApp.setService(null);
        super.onDestroy();
    }

    public void storeTracklist() {

        //send copy to avoid concurrent modification
        PlaylistManager.getInstance(getApplicationContext()).StoreLastPlayingQueueNew(new ArrayList<>(trackList));
        try {
            MyApp.getPref().edit().putString(Constants.PREFERENCES.STORED_SONG_ID, currentTrack.getId() + "").apply();
            MyApp.getPref().edit().putInt(Constants.PREFERENCES.STORED_SONG_POSITION_DURATION, getCurrentTrackProgress()).apply();
            Log.d("PlayerService", "storeTracklist: " + currentTrack.getId());
        } catch (Exception ignored) {

        }
    }

    private void restoreTracklist() {
        trackList.addAll(PlaylistManager.getInstance(getApplicationContext()).RestoreLastPlayingQueueNew());

        String id_string = MyApp.getPref().getString(Constants.PREFERENCES.STORED_SONG_ID, "");
        Log.d("PlayerService", "restoreTracklist: restored song id : " + id_string);
        int id = 0;
        try {
            id = Integer.valueOf(id_string);
        } catch (Exception ignored) {
        }

        //here
        if (trackList.isEmpty() || !trackList.contains(id)) {

            //this should not happen.
            //but if does, handle it by loading default tracklist
            Log.d("PlayerService", "restoreTracklist: load default list");
            trackList.clear();
            try {
                trackList.addAll(MusicLibrary.getInstance().getDefaultTracklistNew());
            } catch (Exception e) {
                trackList.clear();
                Log.v(Constants.TAG, e.toString());
            }

            if (!trackList.isEmpty()) {
                if (trackList.size() == 1) {
                    //to avoid sending 0 to nextInt function
                    currentTrack = MusicLibrary.getInstance().getTrackItemFromId(trackList.get(0));
                    currentTrackPosition = 1;
                } else {
                    int random = new Random().nextInt(trackList.size() - 1);
                    currentTrack = MusicLibrary.getInstance().getTrackItemFromId(trackList.get(random));
                    currentTrackPosition = random;
                }
            } else {
                currentTrackPosition = -1;
                //Toast.makeText(getApplicationContext(),"Empty library!",Toast.LENGTH_LONG).show();
            }
        } else {
            currentTrackPosition = trackList.indexOf(id);
            try {
                currentTrack = MusicLibrary.getInstance().getTrackItemFromId(id);
            } catch (Exception e) {
                Log.e(Constants.TAG, Arrays.toString(e.getStackTrace()), e);
            }
            Log.d("PlayerService", "restoreTracklist: " + currentTrack.getTitle());
        }
        if (MyApp.getPref().getBoolean(Constants.PREFERENCES.SHUFFLE, false)) {
            shuffle(true);
        }
        FileInputStream file;
        try {
            file = new FileInputStream(new File(currentTrack.getFilePath()));
            mediaPlayer.setDataSource(file.getFD());
            mediaPlayer.prepareAsync();
            playAfterPrepare = false;
            setStatus(PAUSED);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ignored) {
        }
        Log.d("PlayerService", "restoreTracklist: restored track item : " + currentTrack.getId());
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.v(Constants.TAG, "focus" + focusChange);
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            if (status == PLAYING) {
                pause();
                notifyUI();
                musicPuasedBecauseOfFocusLoss = true;
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            if (musicPuasedBecauseOfFocusLoss) {
                if (status == PAUSED) {
                    play();
                    notifyUI();
                    musicPuasedBecauseOfFocusLoss = false;
                }
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            if (status == PLAYING) {
                pause();
                notifyUI();
                musicPuasedBecauseOfFocusLoss = true;
            }
        }
    }

    @Override
    public void hearShake() {
        switch (MyApp.getPref().getInt(getString(R.string.pref_shake_action), Constants.SHAKE_ACTIONS.NEXT)) {
            case Constants.SHAKE_ACTIONS.NEXT:
                if (status == PLAYING) {
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(Constants.ACTION.NEXT_ACTION));
                }
                break;

            case Constants.SHAKE_ACTIONS.PLAY_PAUSE:
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(new Intent(Constants.ACTION.PLAY_PAUSE_ACTION));
                break;

            case Constants.SHAKE_ACTIONS.PREVIOUS:
                if (status == PLAYING) {
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(Constants.ACTION.PREV_ACTION));
                }
                break;
        }
    }

    private void gradualIncreaseVolume() {
        //gradually increase volume from zero to current volume level
        //this is called when call is done.
        //android mutes music stream when call happens
        //to prevent current volume to go to mute, we set it 1/3rd of max volume
        if (musicPausedBecauseOfCall) {
            currentVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 3;
            return;
        } else if (!fVolumeIsBeingChanged) {
            currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        } else {
            mHandler.removeCallbacksAndMessages(gradualVolumeRaiseRunnable);
        }
        //Log.d("PlayerService", "gradualIncreaseVolume: current volume : " + currentVolume);
        //currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        mHandler.post(gradualVolumeRaiseRunnable);
    }

    private final Runnable gradualVolumeRaiseRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) < currentVolume) {
                    fVolumeIsBeingChanged = true;
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + 1, 0);
                    //Log.d("PlayerService", "run: Volume :" + mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                    mHandler.postDelayed(gradualVolumeRaiseRunnable, 50);
                } else {
                    fVolumeIsBeingChanged = false;
                }
            } catch (SecurityException exc) {
                Log.d("PlayerService", "run: security exception");
            }
        }
    };

    public class PlayerBinder extends Binder {

        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    private class HeadSetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) return;
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        if (!isInitialStickyBroadcast())   //this is for removing any sticky headset broadcast in system
                        {
                            if (status == PLAYING) {
                                pause();
                                notifyUI();
                            }
                            Log.d(getClass().toString(), "Headset unplugged");
                        }
                        break;

                    case 1:
                        Log.d(getClass().toString(), "Headset plugged");
                        break;
                }
            }
        }
    }

    //receiver which handles pausing music upon bluetooth disconnection
    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                Log.d("BluetoothReceiver", "onReceive: " + intent.getAction());
                switch (intent.getAction()) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)) {
                            case BluetoothAdapter.STATE_OFF:
                                if (doesMusicNeedsToBePaused && status == PLAYING) {
                                    pause();
                                    notifyUI();
                                    Log.d(TAG, "onReceive: pausing music");
                                }
                                break;
                        }
                        break;

                    case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                        switch (intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED)) {
                            case BluetoothAdapter.STATE_CONNECTED:
                                doesMusicNeedsToBePaused = true;
                                break;

                            case BluetoothAdapter.STATE_DISCONNECTED:
                                if (doesMusicNeedsToBePaused && status == PLAYING) {
                                    pause();
                                    notifyUI();
                                    Log.d(TAG, "onReceive: pausing music");
                                }
                                doesMusicNeedsToBePaused = false;
                                break;
                        }
                        break;
                }
            }
        }
    }
}

