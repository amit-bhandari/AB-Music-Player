package com.music.player.bhandari.m.service

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
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.graphics.Bitmap
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.audiofx.PresetReverb
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media.app.NotificationCompat
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.uiElementHelper.ColorHelper
import com.music.player.bhandari.m.activity.ActivityMain
import com.music.player.bhandari.m.activity.ActivityNowPlaying
import com.music.player.bhandari.m.activity.ActivityPermissionSeek
import com.music.player.bhandari.m.equalizer.EqualizerHelper
import com.music.player.bhandari.m.equalizer.EqualizerSetting
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.MusicLibrary
import com.music.player.bhandari.m.model.PlaylistManager
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.utils.UtilityFun
import com.music.player.bhandari.m.widget.WidgetReceiver
import com.squareup.seismic.ShakeDetector
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class PlayerService : Service(), AudioManager.OnAudioFocusChangeListener, ShakeDetector.Listener {
    private var mAudioManager: AudioManager? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playAfterPrepare: Boolean = false

    //playlist (now playing)
    private val trackList: ArrayList<Int> = ArrayList()
    private var currentTrack: TrackItem? = null
    private var currentVolume: Int = 0
    private var fVolumeIsBeingChanged: Boolean = false
    private val headsetFilter: IntentFilter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
    private val mReceiverHeadset: HeadSetReceiver = HeadSetReceiver()
    private var status: Int = 0
    private var currentTrackPosition: Int = 0
    private var playerBinder: IBinder? = null
    private var pendingIntent: PendingIntent? = null
    private var pSwipeToDismiss: PendingIntent? = null
    private var ppreviousIntent: PendingIntent? = null
    private var pplayIntent: PendingIntent? = null
    private var pnextIntent: PendingIntent? = null
    private var pdismissIntent: PendingIntent? = null
    private var mNotificationManager: NotificationManager? = null

    //media session and related objects
    private var mMediaSession: MediaSessionCompat? = null
    private var stateBuilder: PlaybackStateCompat.Builder? = null
    private var phoneStateListener: PhoneStateListener? = null
    private var mReceiver: BroadcastReceiver? = null
    private var musicPausedBecauseOfCall: Boolean = false
    private var musicPuasedBecauseOfFocusLoss: Boolean = false
    private var mHandler: Handler? = null

    //variables for measuring time between consecutive play pause button click on earphone
    private var lastTimePlayPauseClicked: Long = 0

    val PLAYING: Int = 1

    //equlizer helper
    var mEqualizerHelper: EqualizerHelper? = null

    //Bluetooth callback receivers
    var bluetoothReceiver: BroadcastReceiver = BluetoothReceiver()

    override fun onCreate() {
        // MyApp.getPref().edit().putBoolean(getString(R.string.pref_remove_ads),true).apply();
        Log.d("PlayerService", "onCreate: ")
        super.onCreate()
        mHandler = Handler()

        //for shake to play feature
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        shakeDetector = ShakeDetector(this)
        shakeDetector!!.setSensitivity(ShakeDetector.SENSITIVITY_LIGHT)
        if (MyApp.getPref().getBoolean(getString(R.string.pref_shake), false)) {
            setShakeListener(true)
        }
        InitializeIntents()
        InitializeReceiver()
        //bluetooth button control and lock screen albumName art
        InitializeMediaSession()
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?

        //initialize stuff  ///to broadcast to UI when track changes automatically
        mAudioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager?
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(arg0: MediaPlayer) {
                Log.d("PlayerService", "onCompletion: $arg0")
                when {
                    currentTrackPosition == trackList.size - 1 -> {
                        when {
                            MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT,
                                0) == Constants.PREFERENCE_VALUES.REPEAT_ALL -> {
                                playTrack(0)
                                currentTrackPosition = 0
                            }
                            MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT,
                                0) == Constants.PREFERENCE_VALUES.REPEAT_ONE -> {
                                playTrack(currentTrackPosition)
                            }
                            else -> {
                                if (MyApp.getPref().getBoolean(getString(R.string.pref_continuous_playback), false)
                                ) {
                                    if (trackList.size < 10) {
                                        val dataItems: List<Int> =
                                            MusicLibrary.instance.defaultTracklistNew
                                        Collections.shuffle(dataItems)
                                        trackList.addAll(dataItems)
                                        playTrack(currentTrackPosition + 1)
                                    } else {
                                        playTrack(0)
                                        currentTrackPosition = 0
                                    }
                                } else {
                                    stop()
                                }
                            }
                        }
                    }
                    MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT,
                        0) == Constants.PREFERENCE_VALUES.REPEAT_ONE -> {
                        playTrack(currentTrackPosition)
                    }
                    else -> {
                        nextTrack()
                        PostNotification()
                        return  //to avoid double call to notifyUi as it is getting called from nextTrack()  Sounds stupid but it works, so it ain't stupid
                    }
                }
                notifyUI()
                PostNotification()
            }
        })
        mediaPlayer!!.setOnPreparedListener {
            when {
                playAfterPrepare -> {
                    mediaPlayer!!.start()
                }
                else -> {
                    Log.d("PlayerService",
                        "onPrepared: seeking to : " + MyApp.getPref()
                            .getInt(Constants.PREFERENCES.STORED_SONG_POSITION_DURATION, 0))
                    try {
                        seekTrack(MyApp.getPref()
                            .getInt(Constants.PREFERENCES.STORED_SONG_POSITION_DURATION, 0))
                    } catch (e: Exception) {
                        Log.d("PlayerService", "onPrepared: Unable to seek track")
                    }
                }
            }
        }
        mediaPlayer!!.setOnErrorListener { mediaPlayer, i, i1 ->
            Log.d("PlayerService", "onError: $mediaPlayer $i $i1")
            false
        }
        mediaPlayer!!.setOnInfoListener { mediaPlayer, i, i1 ->
            Log.d("PlayerService", "onInfo: $mediaPlayer")
            false
        }
        currentTrackPosition = -1
        setStatus(STOPPED)
        playerBinder = PlayerBinder()
        try {
            restoreTracklist()
        } catch (ignored: Exception) {
            Log.e(Constants.TAG, ignored.toString())
        }
        this.registerReceiver(mReceiverHeadset, headsetFilter)
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        //Incoming call: Pause music
                        Log.v(Constants.TAG, "Ringing")
                        if (status == PLAYING) {
                            pause()
                            notifyUI()
                            musicPausedBecauseOfCall = true
                        }
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        //Not in call: Play music
                        Log.v(Constants.TAG, "Idle")
                        if (musicPausedBecauseOfCall) {
                            play()
                            notifyUI()
                            musicPausedBecauseOfCall = false
                        }
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        //A call is dialing, active or on hold
                        Log.v(Constants.TAG, "Dialling")
                        if (status == PLAYING) {
                            pause()
                            notifyUI()
                            musicPausedBecauseOfCall = true
                        }
                    }
                }
                super.onCallStateChanged(state, incomingNumber)
            }
        }
        (getSystemService(TELEPHONY_SERVICE) as TelephonyManager?)?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        initAudioFX()
        try {
            applyMediaPlayerEQ()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        //bluetooth callback receiver
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        this.registerReceiver(bluetoothReceiver, filter)
        if (UtilityFun.isBluetoothHeadsetConnected) {
            doesMusicNeedsToBePaused = true
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (MyApp.getService() == null) {
            MyApp.setService(this)
        }
        if (intent.action != null) {
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            Log.d("PlayerService", "onStartCommand: " + intent.action)
        } else {
            Log.d("PlayerService", "onStartCommand: null intent or no action in intent")
        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.notification_channel))
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("AB Music")
                    .setAutoCancel(true);

            Notification notification = builder.build();
            startForeground(1, notification);

        }*/return START_STICKY
    }

    private fun InitializeReceiver() {
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action: String? = intent.action
                Log.d("PlayerService", "onReceive: action $action")
                if (action == null) {
                    return
                }
                when (action) {
                    Constants.ACTION.PLAY_PAUSE_ACTION -> {
                        Log.v("Widget", "play")
                        play()
                        notifyUI()
                    }
                    Constants.ACTION.PREV_ACTION -> prevTrack()
                    Constants.ACTION.NEXT_ACTION -> nextTrack()
                    Constants.ACTION.DISMISS_EVENT -> {
                        if (getStatus() == PLAYING) {
                            mediaPlayer!!.pause()
                            setStatus(PAUSED)
                            val state: PlaybackStateCompat = PlaybackStateCompat.Builder()
                                .setActions(
                                    (PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_PAUSE or
                                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
                                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1f)
                                .build()
                            mMediaSession!!.setPlaybackState(state)
                        }
                        // even if music is stopped because of focus loss, don't allow to resume playback after clicking close button
                        musicPuasedBecauseOfFocusLoss = false
                        mNotificationManager!!.cancel(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE)
                        notifyUI()
                        setShakeListener(false)
                        stopForeground(true)
                        stopSelf()
                    }
                    Constants.ACTION.SWIPE_TO_DISMISS -> {
                        musicPuasedBecauseOfFocusLoss = false
                        setShakeListener(false)
                    }
                    Constants.ACTION.REFRESH_LIB -> if (currentTrack == null || trackList.isEmpty()) {
                        trackList.clear()
                        trackList.addAll(MusicLibrary.instance.defaultTracklistNew)
                        if (trackList.isNotEmpty()) {
                            try {
                                if (trackList.size == 1) {
                                    //to avoid sending 0 to nextInt function
                                    currentTrack = MusicLibrary.instance.getTrackItemFromId(trackList.get(0))
                                    currentTrackPosition = 1
                                } else {
                                    val random: Int = Random().nextInt(trackList.size - 1)
                                    currentTrack = MusicLibrary.instance.getTrackItemFromId(trackList.get(random))
                                    currentTrackPosition = random
                                }
                            } catch (ignored: Exception) {
                            }
                        } else {
                            currentTrackPosition = -1
                            //Toast.makeText(getApplicationContext(),"Empty library!",Toast.LENGTH_LONG).show();
                        }
                        notifyUI()
                    }
                    Constants.ACTION.WIDGET_UPDATE -> updateWidget(true)
                    Constants.ACTION.LAUNCH_PLAYER_FROM_WIDGET -> {
                        Log.v(Constants.TAG, "Luaanch now playing from service")
                        //permission seek activity is used here to show splash screen
                        startActivity(Intent(applicationContext,
                            ActivityPermissionSeek::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    Constants.ACTION.SHUFFLE_WIDGET -> {
                        when {
                            MyApp.getPref().getBoolean(Constants.PREFERENCES.SHUFFLE, false) -> {
                                //shuffle is on, turn it off
                                //Toast.makeText(this, "shuffle off", Toast.LENGTH_SHORT).show();
                                MyApp.getPref().edit()
                                    .putBoolean(Constants.PREFERENCES.SHUFFLE, false).apply()
                                shuffle(false)
                            }
                            else -> {
                                //shuffle is off, turn it on
                                MyApp.Companion.getPref().edit()
                                    .putBoolean(Constants.PREFERENCES.SHUFFLE, true).apply()
                                shuffle(true)
                            }
                        }
                        updateWidget(false)
                    }
                    Constants.ACTION.REPEAT_WIDGET -> {
                        val pref: SharedPreferences = MyApp.getPref()
                        when {
                            pref.getInt(Constants.PREFERENCES.REPEAT,
                                0) == Constants.PREFERENCE_VALUES.NO_REPEAT -> {
                                pref.edit().putInt(Constants.PREFERENCES.REPEAT,
                                    Constants.PREFERENCE_VALUES.REPEAT_ALL).apply()
                            }
                            pref.getInt(Constants.PREFERENCES.REPEAT,
                                0) == Constants.PREFERENCE_VALUES.REPEAT_ALL -> {
                                pref.edit().putInt(Constants.PREFERENCES.REPEAT,
                                    Constants.PREFERENCE_VALUES.REPEAT_ONE).apply()
                            }
                            pref.getInt(Constants.PREFERENCES.REPEAT,
                                0) == Constants.PREFERENCE_VALUES.REPEAT_ONE -> {
                                pref.edit().putInt(Constants.PREFERENCES.REPEAT,
                                    Constants.PREFERENCE_VALUES.NO_REPEAT).apply()
                            }
                        }
                        updateWidget(false)
                    }
                    Constants.ACTION.FAV_WIDGET -> {
                        if (getCurrentTrack() == null) return
                        when {
                            PlaylistManager.getInstance(applicationContext)!!.isFavNew(getCurrentTrack()!!.id) -> {
                                PlaylistManager.getInstance(applicationContext)!!.RemoveFromFavNew(getCurrentTrack()!!.id)
                            }
                            else -> {
                                PlaylistManager.getInstance(applicationContext)!!.addSongToFav(getCurrentTrack()!!.id)
                            }
                        }
                        updateWidget(false)
                    }
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.ACTION.PLAY_PAUSE_ACTION)
        intentFilter.addAction(Constants.ACTION.PREV_ACTION)
        intentFilter.addAction(Constants.ACTION.NEXT_ACTION)
        intentFilter.addAction(Constants.ACTION.DISMISS_EVENT)
        intentFilter.addAction(Constants.ACTION.SWIPE_TO_DISMISS)
        intentFilter.addAction(Constants.ACTION.REFRESH_LIB)
        intentFilter.addAction(Constants.ACTION.LAUNCH_PLAYER_FROM_WIDGET)
        intentFilter.addAction(Constants.ACTION.WIDGET_UPDATE)
        intentFilter.addAction(Constants.ACTION.SHUFFLE_WIDGET)
        intentFilter.addAction(Constants.ACTION.REPEAT_WIDGET)
        intentFilter.addAction(Constants.ACTION.FAV_WIDGET)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(mReceiver!!, intentFilter)
    }

    private fun InitializeIntents() {
        //Notification intents
        val notificationIntent: Intent
        when {
            MyApp.getPref().getInt(getString(R.string.pref_click_on_notif),
                Constants.CLICK_ON_NOTIF.OPEN_LIBRARY_VIEW) == Constants.CLICK_ON_NOTIF.OPEN_LIBRARY_VIEW -> {
                notificationIntent = Intent(this, ActivityMain::class.java)
                notificationIntent.action = Constants.ACTION.MAIN_ACTION
                notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0)
            }
            MyApp.getPref().getInt(getString(R.string.pref_click_on_notif),
                Constants.CLICK_ON_NOTIF.OPEN_LIBRARY_VIEW) == Constants.CLICK_ON_NOTIF.OPEN_DISC_VIEW -> {
                notificationIntent = Intent(this, ActivityNowPlaying::class.java)
                notificationIntent.action = Constants.ACTION.MAIN_ACTION
                notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0)
            }
        }
        val favIntent = Intent(this, PlayerService::class.java)
        favIntent.action = Constants.ACTION.FAV_ACTION
        val pfavintent: PendingIntent = PendingIntent.getService(this, 0, favIntent, 0)
        val previousIntent = Intent(this, PlayerService::class.java)
        previousIntent.action = Constants.ACTION.PREV_ACTION
        ppreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0)
        val playIntent = Intent(this, PlayerService::class.java)
        playIntent.action = Constants.ACTION.PLAY_PAUSE_ACTION
        pplayIntent = PendingIntent.getService(this, 0, playIntent, 0)
        val nextIntent = Intent(this, PlayerService::class.java)
        nextIntent.action = Constants.ACTION.NEXT_ACTION
        pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0)
        val dismissIntent = Intent(this, PlayerService::class.java)
        dismissIntent.action = Constants.ACTION.DISMISS_EVENT
        pdismissIntent = PendingIntent.getService(this, 0, dismissIntent, 0)
        val swipeToDismissIntent = Intent(this, PlayerService::class.java)
        swipeToDismissIntent.action = Constants.ACTION.SWIPE_TO_DISMISS
        pSwipeToDismiss = PendingIntent.getService(this, 0, swipeToDismissIntent, 0)
    }

    private fun InitializeMediaSession() {
        mMediaSession = MediaSessionCompat(applicationContext, packageName + "." + ContentValues.TAG)
        mMediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
            override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                Log.d(ContentValues.TAG, "onMediaButtonEvent called: $mediaButtonIntent")
                return super.onMediaButtonEvent(mediaButtonIntent)
            }

            override fun onPause() {
                Log.d(ContentValues.TAG, "onPause called (media button pressed)")
                onPlayPauseButtonClicked()
                super.onPause()
            }

            override fun onFastForward() {
                Log.d(ContentValues.TAG, "onFastForward: called")
                super.onFastForward()
            }

            override fun onCommand(command: String, extras: Bundle, cb: ResultReceiver) {
                Log.d(ContentValues.TAG, "onCommand: $command")
                super.onCommand(command, extras, cb)
            }

            override fun onSeekTo(pos: Long) {
                Log.d(ContentValues.TAG, "onSeekTo: called $pos")
                seekTrack(pos.toInt())
                super.onSeekTo(pos)
            }

            override fun onRewind() {
                Log.d(ContentValues.TAG, "onRewind: called")
                super.onRewind()
            }

            override fun onSkipToPrevious() {
                Log.d(ContentValues.TAG, "onskiptoPrevious called (media button pressed)")
                prevTrack()
                super.onSkipToPrevious()
            }

            override fun onSkipToNext() {
                Log.d(ContentValues.TAG, "onskiptonext called (media button pressed)")
                nextTrack()
                super.onSkipToNext()
            }

            override fun onPlay() {
                Log.d(ContentValues.TAG, "onPlay called (media button pressed)")
                onPlayPauseButtonClicked()
                super.onPlay()
            }

            override fun onStop() {
                stop()
                notifyUI()
                Log.d(ContentValues.TAG, "onStop called (media button pressed)")
                super.onStop()
            }

            private fun onPlayPauseButtonClicked() {

                //if pressed multiple times in 500 ms, skip to next song
                val currentTime: Long = System.currentTimeMillis()
                Log.d(ContentValues.TAG,
                    "onPlay: $lastTimePlayPauseClicked current $currentTime")
                if (currentTime - lastTimePlayPauseClicked < 500) {
                    Log.d(ContentValues.TAG, "onPlay: nextTrack on multiple play pause click")
                    nextTrack()
                    //notifyUI();
                    return
                }
                lastTimePlayPauseClicked = System.currentTimeMillis()
                play()
                notifyUI()
            }
        })
        mMediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS or MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
        stateBuilder = PlaybackStateCompat.Builder().setActions((PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
        val state: PlaybackStateCompat = stateBuilder!!.setState(PlaybackStateCompat.STATE_STOPPED, 0, 1f).build()
        mMediaSession!!.setPlaybackState(state)
        mMediaSession!!.isActive = true
    }

    /**
     * Initializes the equalizer and audio effects for this service session.
     */
    fun initAudioFX() {
        try {
            //Instatiate the equalizer helper object.
            mEqualizerHelper = EqualizerHelper(applicationContext, mediaPlayer!!.audioSessionId,
                    MyApp.getPref().getBoolean("pref_equ_enabled", true))
        } catch (e: UnsupportedOperationException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyMediaPlayerEQ() {
        if (mEqualizerHelper == null || !mEqualizerHelper!!.isEqualizerSupported()) return
        val equalizerSetting = mEqualizerHelper!!.getLastEquSetting()
        Log.d("PlayerService", "applyMediaPlayerEQ: applying equ setting $equalizerSetting")
        val fiftyHertzBandValue = equalizerSetting?.getFiftyHertz()
        val oneThirtyHertzBandValue = equalizerSetting?.getFiftyHertz()
        val threeTwentyHertzBandValue = equalizerSetting?.getFiftyHertz()
        val eightHundredHertzBandValue = equalizerSetting?.getFiftyHertz()
        val twoKilohertzBandValue = equalizerSetting?.getFiftyHertz()
        val fiveKilohertzBandValue = equalizerSetting?.getFiftyHertz()
        val twelvePointFiveKilohertzBandValue = equalizerSetting?.getFiftyHertz()
        val reverbValue = equalizerSetting?.getReverb()
        val fiftyHertzBand = mEqualizerHelper!!.getEqualizer().getBand(50000)
        val oneThirtyHertzBand = mEqualizerHelper!!.getEqualizer().getBand(130000)
        val threeTwentyHertzBand = mEqualizerHelper!!.getEqualizer().getBand(320000)
        val eightHundredHertzBand = mEqualizerHelper!!.getEqualizer().getBand(800000)
        val twoKilohertzBand: Short = mEqualizerHelper!!.getEqualizer().getBand(2000000)
        val fiveKilohertzBand: Short = mEqualizerHelper!!.getEqualizer().getBand(5000000)
        val twelvePointFiveKilohertzBand: Short = mEqualizerHelper!!.getEqualizer().getBand(9000000)


        //50Hz Band.
        if (fiftyHertzBandValue != null) {
            when {
                fiftyHertzBandValue == 16 -> {
                    mEqualizerHelper!!.getEqualizer().setBandLevel(fiftyHertzBand, 0.toShort())
                }
                fiftyHertzBandValue < 16 -> {
                    if (fiftyHertzBandValue == 0) {
                        mEqualizerHelper!!.getEqualizer().setBandLevel(fiftyHertzBand, (-1500).toShort())
                    } else {
                        mEqualizerHelper!!.getEqualizer().setBandLevel(fiftyHertzBand, (-(16 - fiftyHertzBandValue) * 100).toShort())
                    }
                }
                fiftyHertzBandValue > 16 -> {
                    mEqualizerHelper!!.getEqualizer().setBandLevel(fiftyHertzBand, ((fiftyHertzBandValue - 16) * 100).toShort())
                }
            }
        }

        //130Hz Band.
        if (oneThirtyHertzBandValue != null) {
            when {
                oneThirtyHertzBandValue == 16 -> {
                    mEqualizerHelper!!.getEqualizer().setBandLevel(oneThirtyHertzBand, 0.toShort())
                }
                oneThirtyHertzBandValue < 16 -> {
                    if (oneThirtyHertzBandValue == 0) {
                        mEqualizerHelper!!.getEqualizer().setBandLevel(oneThirtyHertzBand, (-1500).toShort())
                    } else {
                        mEqualizerHelper!!.getEqualizer().setBandLevel(oneThirtyHertzBand,
                            (-(16 - oneThirtyHertzBandValue) * 100).toShort())
                    }
                }
                oneThirtyHertzBandValue > 16 -> {
                    mEqualizerHelper!!.getEqualizer().setBandLevel(oneThirtyHertzBand, ((oneThirtyHertzBandValue - 16) * 100).toShort())
                }
            }
        }

        //320Hz Band.
        if (threeTwentyHertzBandValue != null) {
            when {
                threeTwentyHertzBandValue == 16 -> {
                    mEqualizerHelper!!.getEqualizer().setBandLevel(threeTwentyHertzBand, 0.toShort())
                }
                threeTwentyHertzBandValue < 16 -> {
                    if (threeTwentyHertzBandValue == 0) {
                        mEqualizerHelper!!.getEqualizer().setBandLevel(threeTwentyHertzBand, (-1500).toShort())
                    } else {
                        mEqualizerHelper!!.getEqualizer().setBandLevel(threeTwentyHertzBand,
                            (-(16 - threeTwentyHertzBandValue) * 100).toShort())
                    }
                }
                threeTwentyHertzBandValue > 16 -> {
                    mEqualizerHelper!!.getEqualizer().setBandLevel(threeTwentyHertzBand,
                        ((threeTwentyHertzBandValue - 16) * 100).toShort())
                }
            }
        }

        //800Hz Band.
        if (eightHundredHertzBandValue != null) {
            when {
                eightHundredHertzBandValue == 16 -> {
                    mEqualizerHelper!!.getEqualizer().setBandLevel(eightHundredHertzBand, 0.toShort())
                }
                eightHundredHertzBandValue < 16 -> {
                    if (eightHundredHertzBandValue == 0) {
                        mEqualizerHelper!!.getEqualizer().setBandLevel(eightHundredHertzBand, (-1500).toShort())
                    } else {
                        mEqualizerHelper!!.getEqualizer().setBandLevel(eightHundredHertzBand,
                            (-(16 - eightHundredHertzBandValue) * 100).toShort())
                    }
                }
                eightHundredHertzBandValue > 16 -> {
                    mEqualizerHelper!!.getEqualizer().setBandLevel(eightHundredHertzBand,
                        ((eightHundredHertzBandValue - 16) * 100).toShort())
                }
            }
        }

        //2kHz Band.
        if (twoKilohertzBandValue != null) {
            when {
                twoKilohertzBandValue == 16 -> {
                    mEqualizerHelper!!.getEqualizer().setBandLevel(twoKilohertzBand, 0.toShort())
                }
                twoKilohertzBandValue < 16 -> {
                    if (twoKilohertzBandValue == 0) {
                        mEqualizerHelper!!.getEqualizer().setBandLevel(twoKilohertzBand, (-1500).toShort())
                    } else {
                        mEqualizerHelper!!.getEqualizer().setBandLevel(twoKilohertzBand, (-(16 - twoKilohertzBandValue) * 100).toShort())
                    }
                }
                twoKilohertzBandValue > 16 -> {
                    mEqualizerHelper!!.getEqualizer().setBandLevel(twoKilohertzBand, ((twoKilohertzBandValue - 16) * 100).toShort())
                }
            }
        }

        //5kHz Band.
        if (fiveKilohertzBandValue != null) {
            when {
                fiveKilohertzBandValue == 16 -> {
                    mEqualizerHelper!!.getEqualizer().setBandLevel(fiveKilohertzBand, 0.toShort())
                }
                fiveKilohertzBandValue < 16 -> {
                    if (fiveKilohertzBandValue == 0) {
                        mEqualizerHelper!!.getEqualizer().setBandLevel(fiveKilohertzBand, (-1500).toShort())
                    } else {
                        mEqualizerHelper!!.getEqualizer().setBandLevel(fiveKilohertzBand,
                            (-(16 - fiveKilohertzBandValue) * 100).toShort())
                    }
                }
                fiveKilohertzBandValue > 16 -> {
                    mEqualizerHelper!!.getEqualizer().setBandLevel(fiveKilohertzBand, ((fiveKilohertzBandValue - 16) * 100).toShort())
                }
            }
        }

        //12.5kHz Band.
        if (twelvePointFiveKilohertzBandValue != null) {
            when {
                twelvePointFiveKilohertzBandValue == 16 -> {
                    mEqualizerHelper!!.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, 0.toShort())
                }
                twelvePointFiveKilohertzBandValue < 16 -> {
                    if (twelvePointFiveKilohertzBandValue == 0) {
                        mEqualizerHelper!!.getEqualizer()
                            .setBandLevel(twelvePointFiveKilohertzBand, (-1500).toShort())
                    } else {
                        mEqualizerHelper!!.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand,
                            (-(16 - twelvePointFiveKilohertzBandValue) * 100).toShort())
                    }
                }
                twelvePointFiveKilohertzBandValue > 16 -> {
                    mEqualizerHelper!!.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand,
                        ((twelvePointFiveKilohertzBandValue - 16) * 100).toShort())
                }
            }
        }

        //Set the audioFX values.
        equalizerSetting?.getVirtualizer()?.toShort()
            ?.let { mEqualizerHelper!!.getVirtualizer().setStrength(it) }
        equalizerSetting?.getBassBoost()?.toShort()
            ?.let { mEqualizerHelper!!.getBassBoost().setStrength(it) }
        when (reverbValue) {
            0 -> {
                mEqualizerHelper!!.getPresetReverb().preset = PresetReverb.PRESET_NONE
            }
            1 -> mEqualizerHelper!!.getPresetReverb().preset = PresetReverb.PRESET_LARGEHALL
            2 -> {
                mEqualizerHelper!!.getPresetReverb().preset = PresetReverb.PRESET_LARGEROOM
            }
            3 -> {
                mEqualizerHelper!!.getPresetReverb().preset = PresetReverb.PRESET_MEDIUMHALL
            }
            4 -> {
                mEqualizerHelper!!.getPresetReverb().preset = PresetReverb.PRESET_MEDIUMROOM
            }
            5 -> {
                mEqualizerHelper!!.getPresetReverb().preset = PresetReverb.PRESET_SMALLROOM
            }
            6 -> {
                mEqualizerHelper!!.getPresetReverb().preset = PresetReverb.PRESET_PLATE
            }
        }
    }

    fun PostNotification() {
        Executors.newSingleThreadExecutor().execute(object : Runnable {
            override fun run() {
                if (currentTrack == null) {
                    return
                }
                var b: Bitmap? = null
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        b = MusicLibrary.instance.getAlbumArtFromTrack(getCurrentTrack()!!.id)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (b != null) {
                    var width: Int = b.width
                    var height: Int = b.height
                    val maxWidth: Int = 300
                    val maxHeight: Int = 300
                    when {
                        width > height -> {
                            // landscape
                            val ratio: Float = width.toFloat() / maxWidth
                            width = maxWidth
                            height = (height / ratio).toInt()
                        }
                        height > width -> {
                            // portrait
                            val ratio: Float = height.toFloat() / maxHeight
                            height = maxHeight
                            width = (width / ratio).toInt()
                        }
                        else -> {
                            // square
                            height = maxHeight
                            width = maxWidth
                        }
                    }
                    b = Bitmap.createScaledBitmap(b, width, height, false)
                }
                var secondaryText: String? = ""
                when {
                    sleepTimerMinutes != 0 -> {
                        secondaryText = (sleepTimerMinutes - sleepTimeAlreadyOver).toString() + getString(R.string.notif_minutes_to_sleep)
                    }
                    else -> {
                        //show up next song instead
                        // see if song is there to play next
                        secondaryText = getString(R.string.next_track)
                        if (currentTrackPosition == trackList.size - 1) {
                            secondaryText += getString(R.string.empty_queue)
                        } else {
                            try {
                                secondaryText += MusicLibrary.instance.getTrackItemFromId(trackList.get(currentTrackPosition + 1))!!.title
                            } catch (e: IndexOutOfBoundsException) {
                                Log.v(Constants.TAG, e.toString())
                            } catch (ignored: Exception) {
                            }
                        }
                    }
                }
                val trackInfo: String = currentTrack!!.getArtist() + " - " + currentTrack!!.title
                val notification: Notification
                val mediaStyle: NotificationCompat.MediaStyle = NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                if (mMediaSession != null) {
                    mediaStyle.setMediaSession(mMediaSession!!.sessionToken)
                }


                //For lollipop above devices, use media style notification
                val builder: androidx.core.app.NotificationCompat.Builder =
                    androidx.core.app.NotificationCompat.Builder(this@PlayerService,
                        getString(R.string.notification_channel))
                        .setSmallIcon(R.drawable.ic_batman_kitkat)
                        .setContentTitle(trackInfo)
                        .setContentText(secondaryText)
                        .setContentIntent(pendingIntent)
                        .setDeleteIntent(pSwipeToDismiss)
                        .setAutoCancel(false)

                //posting notification fails for huawei devices in case of mediastyle notification
                val isHuawei: Boolean = ((Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1
                        || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP)
                        && Build.MANUFACTURER.lowercase(Locale.getDefault()).contains("huawei"))
                if (!isHuawei) {
                    builder.setStyle(mediaStyle)
                }
                if (b != null) {
                    builder.setLargeIcon(b)
                } else {
                    builder.color = ColorHelper.getWidgetColor()
                }
                builder.addAction(androidx.core.app.NotificationCompat.Action(R.drawable.ic_skip_previous_black_24dp,
                    "Prev",
                    ppreviousIntent))
                when (status) {
                    PLAYING -> {
                        builder.addAction(androidx.core.app.NotificationCompat.Action(R.drawable.ic_pause_black_24dp,
                            "Pause",
                            pplayIntent))
                    }
                    else -> {
                        builder.addAction(androidx.core.app.NotificationCompat.Action(R.drawable.ic_play_arrow_black_24dp,
                            "Play",
                            pplayIntent))
                    }
                }
                builder.addAction(androidx.core.app.NotificationCompat.Action(R.drawable.ic_skip_next_black_24dp,
                    "Next",
                    pnextIntent))
                builder.addAction(androidx.core.app.NotificationCompat.Action(R.drawable.ic_close_white_24dp,
                    "Close",
                    pdismissIntent))
                builder.setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                builder.priority = Notification.PRIORITY_MAX
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setChannelId(getString(R.string.notification_channel))
                }
                notification = builder.build()
                if (getStatus() == PLAYING) {
                    //builder.setOngoing(true);
                    startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification)
                } else {
                    //stopForeground(false);
                    mNotificationManager!!.notify(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                        notification)
                }
            }
        })
    }

    //get played song position, shuffle the list and get the played song at first position
    //this will be called when song is played from music library
    private fun shuffleTracklist(position: Int) {
        if (trackList.isNotEmpty()) {
            val originalSongPlayed: Int = trackList.get(position)
            trackList.shuffle()
            trackList.remove(originalSongPlayed)
            trackList.add(position, originalSongPlayed)
        }
    }

    //this will be called when clicked on shuffle button on now playing
    fun shuffle(shuffleStatus: Boolean) {
        if (trackList.isNotEmpty()) {
            val currentSongPlaying: Int = trackList[currentTrackPosition]
            when {
                shuffleStatus -> {
                    trackList.shuffle()
                    trackList.remove(currentSongPlaying)
                    trackList.add(0, currentSongPlaying)
                    currentTrackPosition = 0
                }
                else -> {
                    val time: Long = System.currentTimeMillis()
                    trackList.sortWith { integer, t1 ->
                        try {
                            MusicLibrary.instance.getTrackMap().get(integer)
                                .compareTo(MusicLibrary.instance.getTrackMap()
                                    .get(t1))
                        } catch (e: NullPointerException) {
                            0
                        }
                    }
                    Log.d(ContentValues.TAG, "shuffle: sorted in " + (System.currentTimeMillis() - time))
                    currentTrackPosition = trackList.indexOf(currentSongPlaying)
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return playerBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

    fun swapPosition(from: Int, to: Int) {
        if (trackList.isNotEmpty()) {
            val currentTrack: Int = trackList[currentTrackPosition]
            Collections.swap(trackList, from, to)
            currentTrackPosition = trackList.indexOf(currentTrack)
        }
    }

    fun removeTrack(position: Int) {
        if (trackList.contains(position)) {
            val position: Int = trackList.indexOf(position)
            if (currentTrackPosition > position) {
                currentTrackPosition--
            }
            trackList.remove(position)
            return
        }

        if (currentTrackPosition > position) {
            currentTrackPosition--
        }
        try {
            trackList.removeAt(position)
        } catch (ignored: ArrayIndexOutOfBoundsException) {
        }
    }

    private fun notifyUI() {
        val UIIntent = Intent()
        UIIntent.action = Constants.ACTION.COMPLETE_UI_UPDATE
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(UIIntent)
        try {
            updateWidget(true)
        } catch (ignored: Exception) {
        }
        //Intent intent = new Intent().setAction(Constants.ACTION.UPDATE_LYRIC_AND_INFO);
        //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    fun updateWidget(loadBitmap: Boolean) {
        Log.d("PlayerService", "updateWidget: called")
        val context: Context = this
        if (getCurrentTrack() == null) {
            Log.d("PlayerService", "updateWidget: failed because of null current track")
            return
        }
        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
        val views = RemoteViews(context.packageName, R.layout.wigdet)
        views.setTextViewText(R.id.song_name_widget, getCurrentTrack()!!.title)
        views.setTextViewText(R.id.artist_widget, getCurrentTrack()!!.getArtist())
        if (loadBitmap) {
            var b: Bitmap? = null
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    b = MusicLibrary.instance.getAlbumArtFromTrack(getCurrentTrack()!!.id)
                }
            } catch (e: Exception) {
                //e.printStackTrace();
            }
            if (b != null) {
                views.setImageViewBitmap(R.id.widget_album_art, b)
            } else {
                views.setImageViewBitmap(R.id.widget_album_art,
                    UtilityFun.drawableToBitmap(UtilityFun.defaultAlbumArtDrawable))
                //views.setImageViewResource(R.id.widget_album_art, R.drawable.ic_batman_1);
            }
        }
        when (status) {
            PLAYING -> {
                views.setImageViewResource(R.id.widget_Play, R.drawable.ic_pause_black_24dp)
            }
            else -> {
                views.setImageViewResource(R.id.widget_Play, R.drawable.ic_play_arrow_black_24dp)
            }
        }
        when {
            MyApp.getPref().getBoolean(Constants.PREFERENCES.SHUFFLE, false) -> {
                views.setInt(R.id.widget_shuffle,
                    "setColorFilter",
                    ColorHelper.getColor(R.color.colorwhite))
            }
            else -> {
                views.setInt(R.id.widget_shuffle, "setColorFilter", ColorHelper.getColor(R.color.gray3))
            }
        }
        views.setTextColor(R.id.text_in_repeat_widget, ColorHelper.getColor(R.color.colorwhite))
        when {
            MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.REPEAT_ALL -> {
                views.setTextViewText(R.id.text_in_repeat_widget, "A")
                views.setInt(R.id.widget_repeat,
                    "setColorFilter",
                    ColorHelper.getColor(R.color.colorwhite))
            }
            MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.REPEAT_ONE -> {
                views.setTextViewText(R.id.text_in_repeat_widget, "1")
                views.setInt(R.id.text_in_repeat_widget,
                    "setTextColor",
                    ColorHelper.getColor(R.color.colorwhite))
                views.setInt(R.id.widget_repeat,
                    "setColorFilter",
                    ColorHelper.getColor(R.color.colorwhite))
            }
            MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.NO_REPEAT -> {
                views.setTextViewText(R.id.text_in_repeat_widget, "")
                views.setInt(R.id.widget_repeat,
                    "setColorFilter",
                    ColorHelper.getColor(R.color.dark_gray3))
            }
        }
        if (getCurrentTrack() != null && PlaylistManager.getInstance(applicationContext)!!.isFavNew(getCurrentTrack()!!.id)
        ) {
            //views.setInt(R.id.widget_fav, "setColorFilter", ColorHelper.GetWidgetColor());
            views.setImageViewResource(R.id.widget_fav, R.drawable.ic_favorite_black_24dp)
        } else {
            //views.setInt(R.id.widget_fav, "setColorFilter", ColorHelper.getColor(R.color.colorwhite));
            views.setImageViewResource(R.id.widget_fav, R.drawable.ic_favorite_border_black_24dp)
        }
        val thisWidget: ComponentName = ComponentName(context, WidgetReceiver::class.java)
        appWidgetManager.updateAppWidget(thisWidget, views)
    }

    private fun setStatus(s: Int) {
        when (s) {
            PLAYING -> Log.d("PlayerService", "setStatus: Playing")
            PAUSED -> Log.d("PlayerService", "setStatus: Paused")
            STOPPED -> Log.d("PlayerService", "setStatus: Stopped")
        }
        status = s
    }

    fun getStatus(): Int {
        return status
    }

    fun getCurrentTrack(): TrackItem? {
        return when {
            currentTrackPosition < 0 -> {
                null
            }
            else -> {
                currentTrack
            }
        }
    }

    fun getCurrentTrackPosition(): Int {
        return currentTrackPosition
    }

    fun getTrackList(): ArrayList<Int> {
        return trackList
    }

    fun setMediaSessionMetadata(enable: Boolean) {
        Executors.newSingleThreadExecutor().execute(Runnable {
            if (currentTrack == null) return@Runnable
            val metadataBuilder: MediaMetadataCompat.Builder = MediaMetadataCompat.Builder()
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, currentTrack!!.title)
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, currentTrack!!.getArtist())
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM, currentTrack!!.album)
            /*metadataBuilder.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI
                    , MusicLibrary.getInstance().getAlbumArtFromTrack(currentTrack.getId()).toString());*/metadataBuilder.putLong(
            MediaMetadata.METADATA_KEY_DURATION,
            currentTrack!!.durInt.toLong())
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_GENRE, currentTrack!!.genre)
            if (MyApp.getPref().getBoolean(getString(R.string.pref_lock_screen_album_Art), true)
            ) {
                if (enable) {
                    var b: Bitmap? = null
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            b = MusicLibrary.instance.getAlbumArtFromTrack(getCurrentTrack()!!.id)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, b)
                } else {
                    metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, null)
                }
            }
            mMediaSession!!.setMetadata(metadataBuilder.build())
        })
    }

    fun playTrack(pos: Int) {
        try {
            trackList[pos]
        } catch (ignored: Exception) {
            return
        }
        val temp: TrackItem? = MusicLibrary.instance.getTrackItemFromId(trackList[pos])
        if (temp == null) {
            val h = Handler(applicationContext.mainLooper)
            h.post {
                Toast.makeText(applicationContext,
                    getString(R.string.error_playing_track),
                    Toast.LENGTH_LONG).show()
            }
            return
        }
        currentTrack = temp


        //here
        PlaylistManager.getInstance(applicationContext)!!.AddToRecentlyPlayedAndUpdateCount(trackList.get(pos))
        val result: Int = mAudioManager!!.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN)
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return
        }

        //currentTrack=trackInfoFetcher.get(trackList.get(pos)).iterator().next();
        gradualIncreaseVolume()
        if (status > STOPPED) {
            stop()
        }
        val file: FileInputStream
        try {
            file = FileInputStream(File(currentTrack!!.getFilePath()))
            mediaPlayer!!.setDataSource(file.fd)
            mediaPlayer!!.prepareAsync()
            playAfterPrepare = true
            //mediaPlayer.prepare();
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        currentTrackPosition = pos
        setStatus(PLAYING)
        setSessionState()
        setMediaSessionMetadata(true)

        //sometimes player may shut down abruptly, make sure you save the current queue every time song changes
        //storeTracklist();
    }

    private fun setSessionState() {
        //set state play
        when (status) {
            PLAYING -> {
                stateBuilder!!.setState(PlaybackStateCompat.STATE_PLAYING,
                    getCurrentTrackProgress().toLong(),
                    1f)
            }
            PAUSED -> {
                stateBuilder!!.setState(PlaybackStateCompat.STATE_PAUSED, getCurrentTrackProgress().toLong(), 1f)
            }
            else -> {
                stateBuilder!!.setState(PlaybackStateCompat.STATE_STOPPED, 0, 1f)
            }
        }
        mMediaSession!!.setPlaybackState(stateBuilder!!.build())
    }

    fun playAtPosition(position: Int) {
        if (MyApp.getPref().getBoolean(Constants.PREFERENCES.SHUFFLE, false)) {
            shuffleTracklist(position)
            playTrack(position)
        } else {
            playTrack(position)
        }
        PostNotification()
        notifyUI()
    }

    fun playAtPositionFromNowPlaying(position: Int) {
        playTrack(position)
        PostNotification()
        val intent: Intent = Intent().setAction(Constants.ACTION.COMPLETE_UI_UPDATE)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        try {
            updateWidget(true)
        } catch (ignored: Exception) {
        }
    }

    fun play() {
        Log.d("PlayerService", "play: current status $status")
        when (status) {
            STOPPED -> if (trackList.isNotEmpty()) {
                playTrack(currentTrackPosition)
                notifyUI()
            }
            PLAYING -> try {
                mediaPlayer!!.pause()
                setStatus(PAUSED)
                setSessionState()
            } catch (ignored: IllegalStateException) {
            }
            PAUSED -> {
                gradualIncreaseVolume()
                try {
                    mediaPlayer!!.start()
                    setStatus(PLAYING)
                    setSessionState()
                } catch (ignored: IllegalStateException) {
                }
            }
        }
        PostNotification()
        updateWidget(false)
    }

    fun pause() {
        try {
            mediaPlayer!!.pause()
        } catch (ignored: IllegalStateException) {
        }
        setStatus(PAUSED)
        setSessionState()
        PostNotification()
        updateWidget(false)
    }

    fun stop() {
        try {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
            }
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            setStatus(STOPPED)
        } catch (e: IllegalStateException) {
            setStatus(STOPPED)
        }
        updateWidget(false)
    }

    fun shuffleAll() {
        val tempList: ArrayList<Int>? = MusicLibrary.instance.defaultTracklistNew
        if (tempList != null) {
            tempList.shuffle()
            setTrackList(tempList)
            playTrack(0)
            notifyUI()
            PostNotification()
        }
    }

    /*
    add songs to playlist for
    arguments:  clickedON = header string to process
                status = which fragment  (title,artist,albumName,genre)
                whereToAdd = position where to add (immediately, atLast)
     */
    fun addToQ(clickedOn: Int, whereToAdd: Int) {
        val addPosition: Int =
            (if (whereToAdd == Constants.ADD_TO_Q.IMMEDIATE_NEXT) currentTrackPosition else trackList.size - 1)
        try {
            trackList.add(addPosition + 1, clickedOn)
        } catch (e: ArrayIndexOutOfBoundsException) {
            Toast.makeText(applicationContext,
                getString(R.string.error_adding_song_to_q),
                Toast.LENGTH_LONG).show()
        }
    }

    fun nextTrack() {

//        Log.v(Constants.TAG,"Next "+Log.getStackTraceString(new Exception()));
        when {
            MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.REPEAT_ONE -> {
                playTrack(currentTrackPosition)
            }
            currentTrackPosition < trackList.size - 1 -> {
                playTrack(currentTrackPosition + 1)
            }
            currentTrackPosition == trackList.size - 1 -> {
                //if repeat all on, play first song
                if (MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.REPEAT_ALL) {
                    playTrack(0)
                    currentTrackPosition = 0
                } else {
                    if (MyApp.getPref()
                            .getBoolean(getString(R.string.pref_continuous_playback), false)
                    ) {
                        if (trackList.size < 10) {
                            val dataItems: List<Int> =
                                MusicLibrary.instance.defaultTracklistNew
                            Collections.shuffle(dataItems)
                            trackList.addAll(dataItems)
                            playTrack(currentTrackPosition + 1)
                        } else {
                            playTrack(0)
                            currentTrackPosition = 0
                        }
                    } else {
                        Toast.makeText(applicationContext,
                            getString(R.string.empty_queue),
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        PostNotification()
        notifyUI()
    }

    fun prevTrack() {
        when {
            (getCurrentTrackProgress().toFloat() / getCurrentTrackDuration().toFloat())
                    > Constants.PREFERENCE_VALUES.PREV_ACT_TIME_CONSTANT -> {
                //start same song from start
                seekTrack(0)
            }
            currentTrackPosition > 0 -> {
                playTrack(currentTrackPosition - 1)
            }
            currentTrackPosition == 0 -> {
                //if repeat all on, play first song
                when (Constants.PREFERENCE_VALUES.REPEAT_ALL) {
                    MyApp.getPref().getInt(Constants.PREFERENCES.REPEAT,
                        0)
                    -> {
                        playTrack(trackList.size - 1)
                        currentTrackPosition = trackList.size - 1
                    }
                    else -> {
                        Toast.makeText(applicationContext,
                            getString(R.string.empty_queue),
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        PostNotification()
        notifyUI()
    }

    //to update trackitem when tags are changed
    fun updateTrackItem(position: Int, _id: Int, vararg param: String?) {
        if (position == getCurrentTrackPosition()) {
            try {
                currentTrack!!.title = param[0]
                currentTrack!!.setArtist(param[1])
                currentTrack!!.album = param[2]
                currentTrack!!.id = _id
            } catch (ignored: Exception) {
            }
        }
    }

    fun getCurrentTrackProgress(): Int {
        return when {
            status > STOPPED -> {
                try {
                    mediaPlayer!!.currentPosition
                } catch (e: IllegalStateException) {
                    0
                }
            }
            else -> {
                0
            }
        }
    }

    //returns duration of current item in ms
    fun getCurrentTrackDuration(): Int {
        return when {
            currentTrackPosition != -1 && currentTrack != null -> {
                currentTrack!!.durInt
            }
            else -> {
                0
            }
        }
    }

    fun getEqualizerHelper(): EqualizerHelper {
        return mEqualizerHelper!!
    }

    fun seekTrack(p: Int) {
        Log.d("Seek to", "Seek to $p")
        if (status > STOPPED) {
            try {
                mediaPlayer!!.seekTo(p)
                setSessionState()
            } catch (ignored: IllegalStateException) {
            }
        }
    }

    fun setTrackList(tracklist1: ArrayList<Int>?) {
        trackList.clear()
        trackList.addAll((tracklist1)!!)
    }

    /*
    sleep timer runnable and variables
     */
    private var sleepTimerMinutes: Int = 0 //sleep timer minutes
    private var sleepTimeAlreadyOver: Int = -1 //already over minutes
    private val sleepTimerHandler: Handler = Handler()
    fun setSleepTimer(minutes: Int, enable: Boolean) {
        sleepTimerHandler.removeCallbacksAndMessages(null)
        sleepTimeAlreadyOver = -1
        sleepTimerMinutes = minutes
        if (!enable) {
            PostNotification()
            return
        }
        val runnable: Runnable = object : Runnable {
            override fun run() {
                if (sleepTimerMinutes == ++sleepTimeAlreadyOver) {
                    LocalBroadcastManager.getInstance(applicationContext)
                        .sendBroadcast(Intent().setAction(Constants.ACTION.DISMISS_EVENT))
                    Toast.makeText(applicationContext,
                        getString(R.string.timer_over),
                        Toast.LENGTH_LONG).show()
                    MyApp.getPref().edit().putInt(getString(R.string.pref_sleep_timer), 0)
                        .apply()
                    sleepTimerMinutes = 0
                    sleepTimeAlreadyOver = 0
                } else {
                    if (getStatus() == PLAYING) {
                        PostNotification()
                    }
                    sleepTimerHandler.postDelayed(this, (1000 * 60).toLong())
                }
            }
        }
        sleepTimerHandler.post(runnable)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        if (getStatus() != PLAYING) {
            if (MyApp.getPref().getInt(getString(R.string.pref_sleep_timer), 0) != 0) {
                MyApp.getPref().edit().putInt(getString(R.string.pref_sleep_timer), 0)
                    .apply()
                sleepTimerHandler.removeCallbacksAndMessages(null)
            }
            stopForeground(true)
            stopSelf()
            Log.d(ContentValues.TAG, "onTaskRemoved: Stopping player service")
        }
    }

    override fun onDestroy() {
        Log.d("PlayerService", "onDestroy: ")
        updateWidget(false)
        storeTracklist()
        //mNotificationManager.cancelAll();
        mNotificationManager!!.cancel(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE)
        if (mEqualizerHelper != null) {
            mEqualizerHelper!!.releaseEqObjects()
            mEqualizerHelper = null
        }
        mediaPlayer!!.stop()
        mediaPlayer!!.reset()
        mediaPlayer!!.release()
        setStatus(STOPPED)
        setSessionState()
        mMediaSession!!.isActive = false
        mMediaSession!!.release()
        shakeDetector!!.stop()
        unregisterReceiver(mReceiverHeadset)
        (getSystemService(TELEPHONY_SERVICE) as TelephonyManager?)?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(mReceiver!!)
        mAudioManager!!.abandonAudioFocus(this)
        unregisterReceiver(bluetoothReceiver)
        MyApp.setService(null)
        super.onDestroy()
    }

    fun storeTracklist() {

        //send copy to avoid concurrent modification
        PlaylistManager.getInstance(applicationContext)!!.StoreLastPlayingQueueNew(ArrayList(trackList))
        try {
            MyApp.getPref().edit().putString(Constants.PREFERENCES.STORED_SONG_ID, currentTrack!!.id.toString() + "").apply()
            MyApp.getPref().edit().putInt(Constants.PREFERENCES.STORED_SONG_POSITION_DURATION,
                    getCurrentTrackProgress()).apply()
            Log.d("PlayerService", "storeTracklist: " + currentTrack!!.id)
        } catch (ignored: Exception) {
        }
    }

    private fun restoreTracklist() {
        trackList.addAll(PlaylistManager.getInstance(applicationContext)!!.RestoreLastPlayingQueueNew())
        val id_string = MyApp.getPref().getString(Constants.PREFERENCES.STORED_SONG_ID, "")
        Log.d("PlayerService", "restoreTracklist: restored song id : $id_string")
        var id: Int = 0
        try {
            id = Integer.valueOf(id_string)
        } catch (ignored: Exception) {
        }

        //here
        if (trackList.isEmpty() || !trackList.contains(id)) {

            //this should not happen.
            //but if does, handle it by loading default tracklist
            Log.d("PlayerService", "restoreTracklist: load default list")
            trackList.clear()
            try {
                trackList.addAll(MusicLibrary.instance.defaultTracklistNew)
            } catch (e: Exception) {
                trackList.clear()
                Log.v(Constants.TAG, e.toString())
            }
            if (trackList.isNotEmpty()) {
                if (trackList.size == 1) {
                    //to avoid sending 0 to nextInt function
                    currentTrack = MusicLibrary.instance.getTrackItemFromId(trackList.get(0))
                    currentTrackPosition = 1
                } else {
                    val random: Int = Random().nextInt(trackList.size - 1)
                    currentTrack =
                        MusicLibrary.instance.getTrackItemFromId(trackList.get(random))
                    currentTrackPosition = random
                }
            } else {
                currentTrackPosition = -1
                //Toast.makeText(getApplicationContext(),"Empty library!",Toast.LENGTH_LONG).show();
            }
        } else {
            currentTrackPosition = trackList.indexOf(id)
            try {
                currentTrack = MusicLibrary.instance.getTrackItemFromId(id)
            } catch (e: Exception) {
                Log.e(Constants.TAG, Arrays.toString(e.stackTrace), e)
            }
            Log.d("PlayerService", "restoreTracklist: " + currentTrack!!.title)
        }
        if (MyApp.getPref().getBoolean(Constants.PREFERENCES.SHUFFLE, false)) {
            shuffle(true)
        }
        val file: FileInputStream
        try {
            file = FileInputStream(File(currentTrack!!.getFilePath()))
            mediaPlayer!!.setDataSource(file.fd)
            mediaPlayer!!.prepareAsync()
            playAfterPrepare = false
            setStatus(PAUSED)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (ignored: Exception) {
        }
        Log.d("PlayerService", "restoreTracklist: restored track item : " + currentTrack!!.id)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        Log.v(Constants.TAG, "focus$focusChange")
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (status == PLAYING) {
                    pause()
                    notifyUI()
                    musicPuasedBecauseOfFocusLoss = true
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (musicPuasedBecauseOfFocusLoss) {
                    if (status == PAUSED) {
                        play()
                        notifyUI()
                        musicPuasedBecauseOfFocusLoss = false
                    }
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (status == PLAYING) {
                    pause()
                    notifyUI()
                    musicPuasedBecauseOfFocusLoss = true
                }
            }
        }
    }

    override fun hearShake() {
        when (MyApp.getPref().getInt(getString(R.string.pref_shake_action), Constants.SHAKE_ACTIONS.NEXT)) {
            Constants.SHAKE_ACTIONS.NEXT -> if (status == PLAYING) {
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(
                    Constants.ACTION.NEXT_ACTION))
            }
            Constants.SHAKE_ACTIONS.PLAY_PAUSE -> LocalBroadcastManager.getInstance(
                applicationContext)
                .sendBroadcast(Intent(Constants.ACTION.PLAY_PAUSE_ACTION))
            Constants.SHAKE_ACTIONS.PREVIOUS -> if (status == PLAYING) {
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(
                    Constants.ACTION.PREV_ACTION))
            }
        }
    }

    private fun gradualIncreaseVolume() {
        //gradually increase volume from zero to current volume level
        //this is called when call is done.
        //android mutes music stream when call happens
        //to prevent current volume to go to mute, we set it 1/3rd of max volume
        when {
            musicPausedBecauseOfCall -> {
                currentVolume = mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 3
                return
            }
            !fVolumeIsBeingChanged -> {
                currentVolume = mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
            }
            else -> {
                mHandler!!.removeCallbacksAndMessages(gradualVolumeRaiseRunnable)
            }
        }
        //Log.d("PlayerService", "gradualIncreaseVolume: current volume : " + currentVolume);
        //currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        mHandler!!.post(gradualVolumeRaiseRunnable)
    }

    private val gradualVolumeRaiseRunnable = object : Runnable {
        override fun run() {
            try {
                when {
                    mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC) < currentVolume -> {
                        fVolumeIsBeingChanged = true
                        mAudioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC,
                            mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC) + 1,
                            0)
                        //Log.d("PlayerService", "run: Volume :" + mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                        mHandler!!.postDelayed(this, 50)
                    }
                    else -> {
                        fVolumeIsBeingChanged = false
                    }
                }
            } catch (exc: SecurityException) {
                Log.d("PlayerService", "run: security exception")
            }
        }
    }

    inner class PlayerBinder : Binder() {
        fun getService(): PlayerService {
            return this@PlayerService
        }
    }

    private inner class HeadSetReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == null) return
            if ((intent.action == Intent.ACTION_HEADSET_PLUG)) {
                when (intent.getIntExtra("state", -1)) {
                    0 -> if (!isInitialStickyBroadcast) //this is for removing any sticky headset broadcast in system
                    {
                        if (status == PLAYING) {
                            pause()
                            notifyUI()
                        }
                        Log.d(javaClass.toString(), "Headset unplugged")
                    }
                    1 -> Log.d(javaClass.toString(), "Headset plugged")
                }
            }
        }
    }

    //receiver which handles pausing music upon bluetooth disconnection
    private inner class BluetoothReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null) {
                Log.d("BluetoothReceiver", "onReceive: " + intent.action)
                when (intent.action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> when (intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.STATE_OFF)) {
                        BluetoothAdapter.STATE_OFF -> if (doesMusicNeedsToBePaused && status == PLAYING) {
                            pause()
                            notifyUI()
                            Log.d(ContentValues.TAG, "onReceive: pausing music")
                        }
                    }
                    BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> when (intent.getIntExtra(
                        BluetoothAdapter.EXTRA_CONNECTION_STATE,
                        BluetoothAdapter.STATE_DISCONNECTED)) {
                        BluetoothAdapter.STATE_CONNECTED -> doesMusicNeedsToBePaused = true
                        BluetoothAdapter.STATE_DISCONNECTED -> {
                            if (doesMusicNeedsToBePaused && status == PLAYING) {
                                pause()
                                notifyUI()
                                Log.d(ContentValues.TAG, "onReceive: pausing music")
                            }
                            doesMusicNeedsToBePaused = false
                        }
                    }
                }
            }
        }
    }

    companion object {
        val STOPPED: Int = -1
        val PAUSED: Int = 0
        private var shakeDetector: ShakeDetector? = null
        private var sensorManager: SensorManager? = null

        //boolean which decides weather to stop playback when either bluetooth is turned off or
        //bluetooth device is disconnected.
        var doesMusicNeedsToBePaused: Boolean = false
        fun setShakeListener(status: Boolean) {
            if (status) {
                shakeDetector!!.start(sensorManager)
            } else {
                try {
                    shakeDetector!!.stop()
                } catch (ignored: Exception) {
                }
            }
        }
    }
}