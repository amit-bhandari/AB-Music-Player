package com.music.player.bhandari.m.activity.PreviewPlayer

import android.app.Activity
import android.content.*
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.graphics.Rect
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.*
import android.view.View.OnTouchListener
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import com.music.player.bhandari.m.R
import java.io.IOException
import java.lang.ref.WeakReference

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
class AudioPreviewActivity : Activity(), MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener, OnPreparedListener, View.OnClickListener,
    OnAudioFocusChangeListener, OnSeekBarChangeListener, OnTouchListener {
    // Seeking flag
    private var mIsSeeking: Boolean = false
    private var mWasPlaying: Boolean = false
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (mPreviewPlayer != null && mIsSeeking) {
            mPreviewPlayer!!.seekTo(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        mIsSeeking = true
        if (mCurrentState == State.PLAYING) {
            mWasPlaying = true
            pausePlayback(false)
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        if (mWasPlaying) {
            startPlayback()
        }
        mWasPlaying = false
        mIsSeeking = false
    }

    private enum class State {
        INIT, PREPARED, PLAYING, PAUSED
    }

    public override fun onPause() {
        overridePendingTransition(0, 0)
        super.onPause()
    }

    inner class UiHandler(val MSG_UPDATE_PROGRESS: Int = 1000) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_UPDATE_PROGRESS -> updateProgressForPlayer()
                else -> super.handleMessage(msg)
            }
        }
    }

    // Members
    private val mAudioNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // [NOTE][MSB]: Handle any audio output changes
            if ((AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action)) {
                pausePlayback()
            }
        }
    }
    private val mHandler: UiHandler? = UiHandler()
    private var mAudioManager: AudioManager? = null
    private var mPreviewPlayer: PreviewPlayer? = null
    private val mPreviewSong = PreviewSong()
    private var mDuration: Int = 0
    private var mLastOrientationWhileBuffering: Int = 0

    // Views
    private var mTitleTextView: TextView? = null
    private var mArtistTextView: TextView? = null
    private var mSeekBar: SeekBar? = null
    private var mProgressBar: ProgressBar? = null
    private var mPlayPauseBtn: ImageButton? = null
    private var mContainerView: View? = null

    // Flags
    private var mIsReceiverRegistered: Boolean = false
    private var mCurrentState: State = State.INIT

    //listener for phone state
    private var phoneStateListener: PhoneStateListener? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        mLastOrientationWhileBuffering = requestedOrientation
        Logger.logd(TAG, "onCreate($savedInstanceState)")
        val intent: Intent? = intent
        if (intent == null) {
            Logger.loge(TAG, "No intent")
            finish()
            return
        }
        val uri: Uri? = intent.data
        if (uri == null) {
            Logger.loge(TAG, "No uri data")
            finish()
            return
        }
        Logger.logd(TAG, "URI: " + uri)
        mPreviewSong.URI = uri
        val localPlayer: PreviewPlayer? = lastNonConfigurationInstance as PreviewPlayer?
        if (localPlayer == null) {
            mPreviewPlayer = PreviewPlayer()
            mPreviewPlayer!!.setCallbackActivity(this)
            try {
                mPreviewPlayer!!.setDataSourceAndPrepare(mPreviewSong.URI)
            } catch (e: IOException) {
                Logger.loge(TAG, e.message)
                onError(mPreviewPlayer!!, MediaPlayer.MEDIA_ERROR_IO, 0)
                return
            }
        } else {
            mPreviewPlayer = localPlayer
            mPreviewPlayer!!.setCallbackActivity(this)
        }
        mAudioManager = (getSystemService(AUDIO_SERVICE) as AudioManager?)
        sAsyncQueryHandler = object : AsyncQueryHandler(contentResolver) {
            override fun onQueryComplete(token: Int, cookie: Any, cursor: Cursor) {
                this@AudioPreviewActivity.onQueryComplete(token, cookie, cursor)
            }
        }
        initializeInterface()
        registerNoisyAudioReceiver()
        if (savedInstanceState == null) {
            processUri()
        } else {
            mPreviewSong.TITLE = savedInstanceState.getString(MediaStore.Audio.Media.TITLE)
            mPreviewSong.ARTIST = savedInstanceState.getString(MediaStore.Audio.Media.ARTIST)
            setNames()
        }
        if (localPlayer != null) {
            sendStateChange(State.PREPARED)
            if (localPlayer.isPlaying) {
                startProgressUpdates()
                sendStateChange(State.PLAYING)
            }
        }
        RegisterPhoneStateListener()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        if (mIsReceiverRegistered) {
            unregisterReceiver(mAudioNoisyReceiver)
            mIsReceiverRegistered = false
        }
        outState.putString(MediaStore.Audio.Media.TITLE, mPreviewSong.TITLE)
        outState.putString(MediaStore.Audio.Media.ARTIST, mPreviewSong.ARTIST)
        super.onSaveInstanceState(outState)
    }

    override fun onRetainNonConfigurationInstance(): Any {
        mPreviewPlayer!!.clearCallbackActivity()
        val localPlayer: PreviewPlayer? = mPreviewPlayer
        mPreviewPlayer = null
        return (localPlayer)!!
    }

    public override fun onDestroy() {
        if (mIsReceiverRegistered) {
            unregisterReceiver(mAudioNoisyReceiver)
            mIsReceiverRegistered = false
        }
        stopPlaybackAndTeardown()
        UnregisterPhoneStateListener()
        super.onDestroy()
    }

    private fun sendStateChange(newState: State) {
        mCurrentState = newState
        handleStateChangeForUi()
    }

    private fun handleStateChangeForUi() {
        when (mCurrentState) {
            State.INIT -> Logger.logd(TAG, "INIT")
            State.PREPARED -> {
                Logger.logd(TAG, "PREPARED")
                if (mPreviewPlayer != null) {
                    mDuration = mPreviewPlayer!!.duration
                }
                if (mDuration > 0 && mSeekBar != null) {
                    mSeekBar!!.max = mDuration
                    mSeekBar!!.isEnabled = true
                    mSeekBar!!.visibility = View.VISIBLE
                }
                if (mProgressBar != null) {
                    mProgressBar!!.visibility = View.INVISIBLE
                    requestedOrientation = mLastOrientationWhileBuffering
                }
                if (mPlayPauseBtn != null) {
                    mPlayPauseBtn!!.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                    mPlayPauseBtn!!.isEnabled = true
                    mPlayPauseBtn!!.setOnClickListener(this)
                }
            }
            State.PLAYING -> {
                Logger.logd(TAG, "PLAYING")
                if (mPlayPauseBtn != null) {
                    mPlayPauseBtn!!.setImageResource(R.drawable.ic_pause_black_24dp)
                    mPlayPauseBtn!!.isEnabled = true
                }
            }
            State.PAUSED -> {
                Logger.logd(TAG, "PAUSED")
                if (mPlayPauseBtn != null) {
                    mPlayPauseBtn!!.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                    mPlayPauseBtn!!.isEnabled = true
                }
            }
        }
        setNames()
    }

    private fun onQueryComplete(token: Int, cookie: Any, cursor: Cursor?) {
        var title: String? = null
        var artist: String? = null
        if (cursor == null || cursor.count < 1) {
            Logger.loge(TAG, "Null or empty cursor!")
            return
        }
        val moved: Boolean = cursor.moveToFirst()
        if (!moved) {
            Logger.loge(TAG, "Failed to read cursor!")
            return
        }
        var index: Int = -1
        when (token) {
            CONTENT_QUERY_TOKEN -> {
                index = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                if (index > -1) {
                    title = cursor.getString(index)
                }
                index = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                if (index > -1) {
                    artist = cursor.getString(index)
                }
            }
            CONTENT_BAD_QUERY_TOKEN -> {
                index = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                if (index > -1) {
                    title = cursor.getString(index)
                }
            }
            else -> title = null
        }
        cursor.close()

        // Well if we didn't get the name lets fallback to something else
        if (TextUtils.isEmpty(title)) {
            title = getNameFromPath()
        }
        mPreviewSong.TITLE = title
        mPreviewSong.ARTIST = artist
        setNames()
    }

    private fun getNameFromPath(): String {
        var path: String = "Unknown" // [TODO][MSB]: Localize
        if (mPreviewSong.URI != null) {
            path = (mPreviewSong.URI!!.lastPathSegment)!!
        }
        return path
    }

    private fun setNames() {
        // Set the text
        mTitleTextView!!.text = mPreviewSong.TITLE
        mArtistTextView!!.text = mPreviewSong.ARTIST
    }

    private fun initializeInterface() {
        volumeControlStream = AudioManager.STREAM_MUSIC
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_audio_preview)
        mContainerView = findViewById(R.id.grp_container_view)
        // Make it so if the user touches the background overlay we exit
        val v: View = findViewById(R.id.grp_transparent_wrapper)
        v.setOnTouchListener(this)
        mTitleTextView = findViewById<View>(R.id.tv_title) as TextView?
        mArtistTextView = findViewById<View>(R.id.tv_artist) as TextView?
        mSeekBar = findViewById<View>(R.id.sb_progress) as SeekBar?
        mSeekBar!!.setOnSeekBarChangeListener(this)
        mProgressBar = findViewById<View>(R.id.pb_loader) as ProgressBar?
        mPlayPauseBtn = findViewById<View>(R.id.ib_playpause) as ImageButton?
    }

    private fun processUri() {
        val scheme: String = (mPreviewSong.URI!!.scheme)!!
        Logger.logd(TAG, "Uri Scheme: $scheme")
        when {
            SCHEME_CONTENT.equals(scheme, ignoreCase = true) -> {
                handleContentScheme()
            }
            SCHEME_FILE.equals(scheme, ignoreCase = true) -> {
                handleFileScheme()
            }
            SCHEME_HTTP.equals(scheme, ignoreCase = true) -> {
                handleHttpScheme()
            }
        }
    }

    private fun startProgressUpdates() {
        if (mHandler != null) {
            mHandler.removeMessages(UiHandler.MSG_UPDATE_PROGRESS)
            val msg: Message = mHandler.obtainMessage(UiHandler.MSG_UPDATE_PROGRESS)
            mHandler.sendMessage(msg)
        }
    }

    private fun updateProgressForPlayer() {
        try {
            if (mSeekBar != null && mPreviewPlayer != null) {
                if (mPreviewPlayer!!.isPrepared()) {
                    mSeekBar!!.progress = mPreviewPlayer!!.currentPosition
                }
            }
            if (mHandler != null) {
                mHandler.removeMessages(UiHandler.MSG_UPDATE_PROGRESS)
                val msg: Message = mHandler.obtainMessage(UiHandler.MSG_UPDATE_PROGRESS)
                mHandler.sendMessageDelayed(msg, PROGRESS_DELAY_INTERVAL.toLong())
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun handleContentScheme() {
        val authority: String = (mPreviewSong.URI!!.authority)!!
        if (!AUTHORITY_MEDIA.equals(authority, ignoreCase = true)) {
            Logger.logd(TAG, "Bad authority!")
            sAsyncQueryHandler!!.startQuery(CONTENT_BAD_QUERY_TOKEN, null, mPreviewSong.URI, null, null, null,
                    null)
        } else {
            sAsyncQueryHandler!!.startQuery(CONTENT_QUERY_TOKEN, null, mPreviewSong.URI, MEDIA_PROJECTION, null,
                    null, null)
        }
    }

    private fun handleFileScheme() {
        val path: String = (mPreviewSong.URI!!.path)!!
        sAsyncQueryHandler!!.startQuery(CONTENT_QUERY_TOKEN,
            null,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MEDIA_PROJECTION,
            "_data=?",
            arrayOf(path),
            null)
    }

    private fun handleHttpScheme() {
        if (mProgressBar != null) {
            mProgressBar!!.visibility = View.VISIBLE
            mLastOrientationWhileBuffering = requestedOrientation
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        }
        mPreviewSong.TITLE = getNameFromPath()
        setNames()
    }

    private fun registerNoisyAudioReceiver() {
        val localIntentFilter: IntentFilter = IntentFilter()
        localIntentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(mAudioNoisyReceiver, localIntentFilter)
        mIsReceiverRegistered = true
    }

    override fun onCompletion(mp: MediaPlayer) {
        mHandler!!.removeMessages(UiHandler.MSG_UPDATE_PROGRESS)
        if (mSeekBar != null && mPreviewPlayer != null) {
            mSeekBar!!.progress = mPreviewPlayer!!.duration
        }
        sendStateChange(State.PREPARED)
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        when (what) {
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Toast.makeText(this,
                getString(R.string.preview_player_server_died),
                Toast.LENGTH_SHORT).show()
            MediaPlayer.MEDIA_ERROR_IO -> Toast.makeText(this,
                getString(R.string.preview_player_io_error),
                Toast.LENGTH_SHORT).show()
            MediaPlayer.MEDIA_ERROR_MALFORMED -> Toast.makeText(this,
                getString(R.string.preview_player_malform_media),
                Toast.LENGTH_SHORT).show()
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> Toast.makeText(this,
                getString(R.string.preview_player_invalid),
                Toast.LENGTH_SHORT)
                .show()
            MediaPlayer.MEDIA_ERROR_TIMED_OUT -> Toast.makeText(this,
                getString(R.string.preview_player_time_out),
                Toast.LENGTH_SHORT).show()
            MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> Toast.makeText(this,
                getString(R.string.preview_player_unsupported),
                Toast.LENGTH_SHORT).show()
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> Toast.makeText(this,
                getString(R.string.preview_player_unknown) + what,
                Toast.LENGTH_LONG)
                .show()
            else -> Toast.makeText(this,
                getString(R.string.preview_player_unknown) + what,
                Toast.LENGTH_LONG)
                .show()
        }
        stopPlaybackAndTeardown()
        finish()
        return true // false causes flow to not call onCompletion
    }

    override fun onPrepared(mp: MediaPlayer) {
        sendStateChange(State.PREPARED)
        startPlayback()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val x: Int = event.x.toInt()
        val y: Int = event.y.toInt()
        val containerX1: Int = mContainerView!!.x.toInt()
        val containerY1: Int = mContainerView!!.y.toInt()
        val containerX2: Int = (mContainerView!!.x + mContainerView!!.width).toInt()
        val containerY2: Int = (mContainerView!!.y + mContainerView!!.height).toInt()
        val r: Rect = Rect()
        r.set(containerX1, containerY1, containerX2, containerY2)
        if (!r.contains(x, y)) {
            stopPlaybackAndTeardown()
            finish()
        }
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ib_playpause -> if (mCurrentState == State.PREPARED || mCurrentState == State.PAUSED) {
                startPlayback()
            } else {
                pausePlayback()
            }
            R.id.grp_transparent_wrapper -> {
                stopPlaybackAndTeardown()
                finish()
            }
            else -> {}
        }
    }

    private fun gainAudioFocus(): Boolean {
        if (mAudioManager == null) {
            return false
        }
        val r: Int = mAudioManager!!.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        return r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        if (mAudioManager != null) {
            mAudioManager!!.abandonAudioFocus(this)
        }
    }

    private fun startPlayback() {
        when {
            mPreviewPlayer != null && !mPreviewPlayer!!.isPlaying -> {
                when {
                    mPreviewPlayer!!.isPrepared() -> {
                        when {
                            gainAudioFocus() -> {
                                mPreviewPlayer!!.start()
                                sendStateChange(State.PLAYING)
                                startProgressUpdates()
                            }
                            else -> {
                                Logger.loge(TAG, "Failed to gain audio focus!")
                                onError(mPreviewPlayer!!, MediaPlayer.MEDIA_ERROR_TIMED_OUT, 0)
                            }
                        }
                    }
                    else -> {
                        Logger.loge(TAG, "Not prepared!")
                    }
                }
            }
            else -> {
                Logger.logd(TAG, "No player or is not playing!")
            }
        }
    }

    private fun stopPlaybackAndTeardown() {
        try {
            if (mPreviewPlayer != null) {
                if (mPreviewPlayer!!.isPlaying) {
                    mPreviewPlayer!!.stop()
                }
                mPreviewPlayer!!.release()
                mPreviewPlayer = null
            }
            abandonAudioFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pausePlayback(updateUi: Boolean = true) {
        if (mPreviewPlayer != null && mPreviewPlayer!!.isPlaying) {
            mPreviewPlayer!!.pause()
            if (updateUi) {
                sendStateChange(State.PAUSED)
            }
            mHandler!!.removeMessages(UiHandler.MSG_UPDATE_PROGRESS)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (mPreviewPlayer == null) {
            if (mAudioManager != null) {
                mAudioManager!!.abandonAudioFocus(this)
            }
        }
        Logger.logd(TAG, "Focus change: $focusChange")
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                stopPlaybackAndTeardown()
                finish()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pausePlayback()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mPreviewPlayer!!.setVolume(0.2f,
                0.2f)
            AudioManager.AUDIOFOCUS_GAIN -> {
                mPreviewPlayer!!.setVolume(1.0f, 1.0f)
                startPlayback()
            }
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> {}
        }
    }

    public override fun onUserLeaveHint() {
        stopPlaybackAndTeardown()
        finish()
        super.onUserLeaveHint()
    }

    override fun onKeyDown(keyCode: Int, keyEvent: KeyEvent): Boolean {
        var result = true
        when (keyCode) {
            KeyEvent.KEYCODE_HEADSETHOOK -> pausePlayback()
            KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.KEYCODE_MEDIA_PREVIOUS, KeyEvent.KEYCODE_MEDIA_REWIND, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> return true
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                startPlayback()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                pausePlayback()
                return true
            }
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_MUTE -> {
                result = super.onKeyDown(keyCode, keyEvent)
                return result
            }
            else -> result = super.onKeyDown(keyCode, keyEvent)
        }
        stopPlaybackAndTeardown()
        finish()
        return result
    }

    private fun UnregisterPhoneStateListener() {
        (getSystemService(TELEPHONY_SERVICE) as TelephonyManager?)?.listen(phoneStateListener,
            PhoneStateListener.LISTEN_NONE)
    }

    private fun RegisterPhoneStateListener() {
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        //Incoming call: Pause music
                        stopPlaybackAndTeardown()
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        //Not in call
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        //A call is dialing, active or on hold
                        stopPlaybackAndTeardown()
                    }
                }
                super.onCallStateChanged(state, incomingNumber)
            }
        }
        (getSystemService(TELEPHONY_SERVICE) as TelephonyManager?)?.listen(phoneStateListener,
            PhoneStateListener.LISTEN_CALL_STATE)
    }

    /**
     * <pre>
     * Media player specifically tweaked for use in this audio preview context
    </pre> *
     */
    private class PreviewPlayer : MediaPlayer(), OnPreparedListener {
        // Members
        private var mActivityReference // weakref from static class
                : WeakReference<AudioPreviewActivity>? = null
        private var mIsPrepared: Boolean = false

        /* package */
        fun isPrepared(): Boolean {
            return mIsPrepared
        }

        /* package */
        fun clearCallbackActivity() {
            mActivityReference!!.clear()
            mActivityReference = null
            setOnErrorListener(null)
            setOnCompletionListener(null)
        }

        /* package */
        @Throws(IllegalArgumentException::class)
        fun setCallbackActivity(activity: AudioPreviewActivity?) {
            if (activity == null) {
                throw IllegalArgumentException("'activity' cannot be null!")
            }
            mActivityReference = WeakReference(activity)
            setOnErrorListener(activity)
            setOnCompletionListener(activity)
        }

        /* package */
        @Throws(IllegalArgumentException::class, IOException::class)
        fun setDataSourceAndPrepare(uri: Uri?) {
            if (uri == null || uri.toString().length < 1) {
                throw IllegalArgumentException("'uri' cannot be null or empty!")
            }
            val activity: AudioPreviewActivity? = mActivityReference!!.get()
            if (activity != null && !activity.isFinishing) {
                setDataSource(activity, uri)
                prepareAsync()
            }
        }

        override fun onPrepared(mp: MediaPlayer) {
            mIsPrepared = true
            if (mActivityReference != null) {
                val activity: AudioPreviewActivity? = mActivityReference!!.get()
                if (activity != null && !activity.isFinishing) {
                    activity.onPrepared(mp)
                }
            }
        }

        /* package */
        init {
            setOnPreparedListener(this)
        }
    }

    companion object {
        // Constants
        private val TAG: String = AudioPreviewActivity::class.java.simpleName
        private val PROGRESS_DELAY_INTERVAL: Int = 250
        private val SCHEME_CONTENT: String = "content"
        private val SCHEME_FILE: String = "file"
        private val SCHEME_HTTP: String = "http"
        private val AUTHORITY_MEDIA: String = "media"
        private val CONTENT_QUERY_TOKEN: Int = 1000
        private val CONTENT_BAD_QUERY_TOKEN: Int = CONTENT_QUERY_TOKEN + 1
        private val MEDIA_PROJECTION: Array<String> = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST
        )
        private var sAsyncQueryHandler: AsyncQueryHandler? = null
    }
}