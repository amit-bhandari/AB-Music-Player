package com.music.player.bhandari.m.activity.PreviewPlayer;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.music.player.bhandari.m.R;

import java.io.IOException;
import java.lang.ref.WeakReference;


/**
 * Copyright 2017 Amit Bhandari AB
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">...</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public class AudioPreviewActivity extends Activity implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, View.OnClickListener,
        AudioManager.OnAudioFocusChangeListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener {

    // Constants
    private static final String TAG = AudioPreviewActivity.class.getSimpleName();
    private static final int PROGRESS_DELAY_INTERVAL = 250;
    private static final String SCHEME_CONTENT = "content";
    private static final String SCHEME_FILE = "file";
    private static final String SCHEME_HTTP = "http";
    private static final String AUTHORITY_MEDIA = "media";
    private static final int CONTENT_QUERY_TOKEN = 1000;
    private static final int CONTENT_BAD_QUERY_TOKEN = CONTENT_QUERY_TOKEN + 1;
    private static final String[] MEDIA_PROJECTION = new String[]{
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST
    };

    // Seeking flag
    private boolean mIsSeeking = false;
    private boolean mWasPlaying = false;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mPreviewPlayer != null && mIsSeeking) {
            mPreviewPlayer.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIsSeeking = true;
        if (mCurrentState == State.PLAYING) {
            mWasPlaying = true;
            pausePlayback(false);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mWasPlaying) {
            startPlayback();
        }
        mWasPlaying = false;
        mIsSeeking = false;
    }

    private enum State {
        INIT,
        PREPARED,
        PLAYING,
        PAUSED
    }

    @Override
    public void onPause() {
        overridePendingTransition(0, 0);
        super.onPause();
    }

    private class UiHandler extends Handler {

        static final int MSG_UPDATE_PROGRESS = 1000;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE_PROGRESS) {
                updateProgressForPlayer();
            } else {
                super.handleMessage(msg);
            }
        }

    }

    // Members
    private final BroadcastReceiver mAudioNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // [NOTE][MSB]: Handle any audio output changes
            if (intent != null) {
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                    pausePlayback();
                }
            }
        }
    };
    private final UiHandler mHandler = new UiHandler();
    private static AsyncQueryHandler sAsyncQueryHandler;
    private AudioManager mAudioManager;
    private PreviewPlayer mPreviewPlayer;
    private final PreviewSong mPreviewSong = new PreviewSong();
    private int mDuration = 0;
    private int mLastOrientationWhileBuffering;

    // Views
    private TextView mTitleTextView;
    private TextView mArtistTextView;
    private SeekBar mSeekBar;
    private ProgressBar mProgressBar;
    private ImageButton mPlayPauseBtn;
    private View mContainerView;

    // Flags
    private boolean mIsReceiverRegistered = false;
    private State mCurrentState = State.INIT;

    //listener for phone state
    private PhoneStateListener phoneStateListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        mLastOrientationWhileBuffering = getRequestedOrientation();
        Logger.logd(TAG, "onCreate(" + savedInstanceState + ")");
        Intent intent = getIntent();
        if (intent == null) {
            Logger.loge(TAG, "No intent");
            finish();
            return;
        }
        Uri uri = intent.getData();
        if (uri == null) {
            Logger.loge(TAG, "No uri data");
            finish();
            return;
        }
        Logger.logd(TAG, "URI: " + uri);
        mPreviewSong.URI = uri;
        PreviewPlayer localPlayer = (PreviewPlayer) getLastNonConfigurationInstance();
        if (localPlayer == null) {
            mPreviewPlayer = new PreviewPlayer();
            mPreviewPlayer.setCallbackActivity(this);
            try {
                mPreviewPlayer.setDataSourceAndPrepare(mPreviewSong.URI);
            } catch (IOException e) {
                Logger.loge(TAG, e.getMessage());
                onError(mPreviewPlayer, MediaPlayer.MEDIA_ERROR_IO, 0);
                return;
            }
        } else {
            mPreviewPlayer = localPlayer;
            mPreviewPlayer.setCallbackActivity(this);
        }
        mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
        sAsyncQueryHandler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                AudioPreviewActivity.this.onQueryComplete(token, cursor);
            }
        };
        initializeInterface();
        registerNoisyAudioReceiver();
        if (savedInstanceState == null) {
            processUri();
        } else {
            mPreviewSong.TITLE = savedInstanceState.getString(MediaStore.Audio.Media.TITLE);
            mPreviewSong.ARTIST = savedInstanceState.getString(MediaStore.Audio.Media.ARTIST);
            setNames();
        }
        if (localPlayer != null) {
            sendStateChange(State.PREPARED);
            if (localPlayer.isPlaying()) {
                startProgressUpdates();
                sendStateChange(State.PLAYING);
            }
        }

        RegisterPhoneStateListener();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (mIsReceiverRegistered) {
            unregisterReceiver(mAudioNoisyReceiver);
            mIsReceiverRegistered = false;
        }
        outState.putString(MediaStore.Audio.Media.TITLE, mPreviewSong.TITLE);
        outState.putString(MediaStore.Audio.Media.ARTIST, mPreviewSong.ARTIST);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        mPreviewPlayer.clearCallbackActivity();
        PreviewPlayer localPlayer = mPreviewPlayer;
        mPreviewPlayer = null;
        return localPlayer;
    }

    @Override
    public void onDestroy() {
        if (mIsReceiverRegistered) {
            unregisterReceiver(mAudioNoisyReceiver);
            mIsReceiverRegistered = false;
        }
        stopPlaybackAndTeardown();
        UnregisterPhoneStateListener();
        super.onDestroy();
    }

    private void sendStateChange(State newState) {
        mCurrentState = newState;
        handleStateChangeForUi();
    }

    private void handleStateChangeForUi() {
        switch (mCurrentState) {
            case INIT -> Logger.logd(TAG, "INIT");
            case PREPARED -> {
                Logger.logd(TAG, "PREPARED");
                if (mPreviewPlayer != null) {
                    mDuration = mPreviewPlayer.getDuration();
                }
                if (mDuration > 0 && mSeekBar != null) {
                    mSeekBar.setMax(mDuration);
                    mSeekBar.setEnabled(true);
                    mSeekBar.setVisibility(View.VISIBLE);
                }
                if (mProgressBar != null) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    setRequestedOrientation(mLastOrientationWhileBuffering);
                }
                if (mPlayPauseBtn != null) {
                    mPlayPauseBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    mPlayPauseBtn.setEnabled(true);
                    mPlayPauseBtn.setOnClickListener(this);
                }
            }
            case PLAYING -> {
                Logger.logd(TAG, "PLAYING");
                if (mPlayPauseBtn != null) {
                    mPlayPauseBtn.setImageResource(R.drawable.ic_pause_black_24dp);
                    mPlayPauseBtn.setEnabled(true);
                }
            }
            case PAUSED -> {
                Logger.logd(TAG, "PAUSED");
                if (mPlayPauseBtn != null) {
                    mPlayPauseBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    mPlayPauseBtn.setEnabled(true);
                }
            }
        }
        setNames();
    }

    private void onQueryComplete(int token, Cursor cursor) {
        String title = null;
        String artist = null;
        if (cursor == null || cursor.getCount() < 1) {
            Logger.loge(TAG, "Null or empty cursor!");
            return;
        }
        boolean moved = cursor.moveToFirst();
        if (!moved) {
            Logger.loge(TAG, "Failed to read cursor!");
            return;
        }
        int index;
        switch (token) {
            case CONTENT_QUERY_TOKEN -> {
                index = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                if (index > -1) {
                    title = cursor.getString(index);
                }
                index = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                if (index > -1) {
                    artist = cursor.getString(index);
                }
            }
            case CONTENT_BAD_QUERY_TOKEN -> {
                index = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                if (index > -1) {
                    title = cursor.getString(index);
                }
            }
            default -> title = null;
        }
        cursor.close();

        // Well if we didn't get the name lets fallback to something else
        if (TextUtils.isEmpty(title)) {
            title = getNameFromPath();
        }

        mPreviewSong.TITLE = title;
        mPreviewSong.ARTIST = artist;

        setNames();
    }

    private String getNameFromPath() {
        String path = "Unknown"; // [TODO][MSB]: Localize
        if (mPreviewSong != null) {
            if (mPreviewSong.URI != null) {
                path = mPreviewSong.URI.getLastPathSegment();
            }
        }
        return path;
    }

    private void setNames() {
        // Set the text
        mTitleTextView.setText(mPreviewSong.TITLE);
        mArtistTextView.setText(mPreviewSong.ARTIST);
    }

    private void initializeInterface() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_audio_preview);
        mContainerView = findViewById(R.id.grp_container_view);
        // Make it so if the user touches the background overlay we exit
        View v = findViewById(R.id.grp_transparent_wrapper);
        v.setOnTouchListener(this);
        mTitleTextView = findViewById(R.id.tv_title);
        mArtistTextView = findViewById(R.id.tv_artist);
        mSeekBar = findViewById(R.id.sb_progress);
        mSeekBar.setOnSeekBarChangeListener(this);
        mProgressBar = findViewById(R.id.pb_loader);
        mPlayPauseBtn = findViewById(R.id.ib_playpause);
    }

    private void processUri() {
        String scheme = mPreviewSong.URI.getScheme();
        Logger.logd(TAG, "Uri Scheme: " + scheme);
        if (SCHEME_CONTENT.equalsIgnoreCase(scheme)) {
            handleContentScheme();
        } else if (SCHEME_FILE.equalsIgnoreCase(scheme)) {
            handleFileScheme();
        } else if (SCHEME_HTTP.equalsIgnoreCase(scheme)) {
            handleHttpScheme();
        }
    }

    private void startProgressUpdates() {
        if (mHandler != null) {
            mHandler.removeMessages(UiHandler.MSG_UPDATE_PROGRESS);
            Message msg = mHandler.obtainMessage(UiHandler.MSG_UPDATE_PROGRESS);
            mHandler.sendMessage(msg);
        }
    }

    private void updateProgressForPlayer() {
        try {
            if (mSeekBar != null && mPreviewPlayer != null) {
                if (mPreviewPlayer.isPrepared()) {
                    mSeekBar.setProgress(mPreviewPlayer.getCurrentPosition());
                }
            }
            if (mHandler != null) {
                mHandler.removeMessages(UiHandler.MSG_UPDATE_PROGRESS);
                Message msg = mHandler.obtainMessage(UiHandler.MSG_UPDATE_PROGRESS);
                mHandler.sendMessageDelayed(msg, PROGRESS_DELAY_INTERVAL);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void handleContentScheme() {
        String authority = mPreviewSong.URI.getAuthority();
        if (!AUTHORITY_MEDIA.equalsIgnoreCase(authority)) {
            Logger.logd(TAG, "Bad authority!");
            sAsyncQueryHandler
                    .startQuery(CONTENT_BAD_QUERY_TOKEN, null, mPreviewSong.URI, null, null, null,
                            null);
        } else {
            sAsyncQueryHandler
                    .startQuery(CONTENT_QUERY_TOKEN, null, mPreviewSong.URI, MEDIA_PROJECTION, null,
                            null, null);
        }
    }

    private void handleFileScheme() {
        String path = mPreviewSong.URI.getPath();
        sAsyncQueryHandler.startQuery(CONTENT_QUERY_TOKEN, null, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MEDIA_PROJECTION, "_data=?", new String[]{path}, null);
    }

    private void handleHttpScheme() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mLastOrientationWhileBuffering = getRequestedOrientation();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }
        mPreviewSong.TITLE = getNameFromPath();
        setNames();
    }

    private void registerNoisyAudioReceiver() {
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(this.mAudioNoisyReceiver, localIntentFilter);
        mIsReceiverRegistered = true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mHandler.removeMessages(UiHandler.MSG_UPDATE_PROGRESS);
        if (mSeekBar != null && mPreviewPlayer != null) {
            mSeekBar.setProgress(mPreviewPlayer.getDuration());
        }
        sendStateChange(State.PREPARED);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED ->
                    Toast.makeText(this, getString(R.string.preview_player_server_died), Toast.LENGTH_SHORT).show();
            case MediaPlayer.MEDIA_ERROR_IO ->
                    Toast.makeText(this, getString(R.string.preview_player_io_error), Toast.LENGTH_SHORT).show();
            case MediaPlayer.MEDIA_ERROR_MALFORMED ->
                    Toast.makeText(this, getString(R.string.preview_player_malform_media), Toast.LENGTH_SHORT).show();
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ->
                    Toast.makeText(this, getString(R.string.preview_player_invalid), Toast.LENGTH_SHORT)
                            .show();
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT ->
                    Toast.makeText(this, getString(R.string.preview_player_time_out), Toast.LENGTH_SHORT).show();
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED ->
                    Toast.makeText(this, getString(R.string.preview_player_unsupported), Toast.LENGTH_SHORT).show();
            default ->
                    Toast.makeText(this, getString(R.string.preview_player_unknown) + what, Toast.LENGTH_LONG)
                            .show();
        }
        stopPlaybackAndTeardown();
        finish();
        return true; // false causes flow to not call onCompletion
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        sendStateChange(State.PREPARED);
        startPlayback();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int containerX1 = (int) mContainerView.getX();
        int containerY1 = (int) mContainerView.getY();
        int containerX2 = (int) (mContainerView.getX() + mContainerView.getWidth());
        int containerY2 = (int) (mContainerView.getY() + mContainerView.getHeight());

        Rect r = new Rect();
        r.set(containerX1, containerY1, containerX2, containerY2);
        if (!r.contains(x, y)) {
            stopPlaybackAndTeardown();
            finish();
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_playpause -> {
                if (mCurrentState == State.PREPARED || mCurrentState == State.PAUSED) {
                    startPlayback();
                } else {
                    pausePlayback();
                }
            }
            case R.id.grp_transparent_wrapper -> {
                stopPlaybackAndTeardown();
                finish();
            }
            default -> {
            }
        }
    }

    private boolean gainAudioFocus() {
        if (mAudioManager == null) {
            return false;
        }
        int r = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        return r == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonAudioFocus() {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(this);
        }
    }

    private void startPlayback() {
        if (mPreviewPlayer != null && !mPreviewPlayer.isPlaying()) {
            if (mPreviewPlayer.isPrepared()) {
                if (gainAudioFocus()) {
                    mPreviewPlayer.start();
                    sendStateChange(State.PLAYING);
                    startProgressUpdates();
                } else {
                    Logger.loge(TAG, "Failed to gain audio focus!");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        onError(mPreviewPlayer, MediaPlayer.MEDIA_ERROR_TIMED_OUT, 0);
                    }
                }
            } else {
                Logger.loge(TAG, "Not prepared!");
            }
        } else {
            Logger.logd(TAG, "No player or is not playing!");
        }
    }

    private void stopPlaybackAndTeardown() {
        try {
            if (mPreviewPlayer != null) {
                if (mPreviewPlayer.isPlaying()) {
                    mPreviewPlayer.stop();
                }
                mPreviewPlayer.release();
                mPreviewPlayer = null;
            }
            abandonAudioFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pausePlayback() {
        pausePlayback(true);
    }

    private void pausePlayback(boolean updateUi) {
        if (mPreviewPlayer != null && mPreviewPlayer.isPlaying()) {
            mPreviewPlayer.pause();
            if (updateUi) {
                sendStateChange(State.PAUSED);
            }
            mHandler.removeMessages(UiHandler.MSG_UPDATE_PROGRESS);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (mPreviewPlayer == null) {
            if (mAudioManager != null) {
                mAudioManager.abandonAudioFocus(this);
            }
        }
        Logger.logd(TAG, "Focus change: " + focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                stopPlaybackAndTeardown();
                finish();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pausePlayback();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mPreviewPlayer.setVolume(0.2f, 0.2f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                mPreviewPlayer.setVolume(1.0f, 1.0f);
                startPlayback();
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                break;
        }
    }

    @Override
    public void onUserLeaveHint() {
        stopPlaybackAndTeardown();
        finish();
        super.onUserLeaveHint();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        boolean result = true;
        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
                pausePlayback();
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                startPlayback();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                pausePlayback();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                result = super.onKeyDown(keyCode, keyEvent);
                return result;
            default:
                result = super.onKeyDown(keyCode, keyEvent);
                break;
        }
        stopPlaybackAndTeardown();
        finish();
        return result;
    }

    private void UnregisterPhoneStateListener() {
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    private void RegisterPhoneStateListener() {
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    //Incoming call: Pause music
                    stopPlaybackAndTeardown();
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    //Not in call

                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //A call is dialing, active or on hold
                    stopPlaybackAndTeardown();
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    /**
     * <pre>
     *     Media player specifically tweaked for use in this audio preview context
     * </pre>
     */
    private static class PreviewPlayer extends MediaPlayer
            implements MediaPlayer.OnPreparedListener {

        // Members
        private WeakReference<AudioPreviewActivity> mActivityReference; // weakref from static class
        private boolean mIsPrepared = false;

        /* package */ boolean isPrepared() {
            return mIsPrepared;
        }

        /* package */ PreviewPlayer() {
            setOnPreparedListener(this);
        }

        /* package */ void clearCallbackActivity() {
            mActivityReference.clear();
            mActivityReference = null;
            setOnErrorListener(null);
            setOnCompletionListener(null);
        }

        /* package */ void setCallbackActivity(AudioPreviewActivity activity)
                throws IllegalArgumentException {
            if (activity == null) {
                throw new IllegalArgumentException("'activity' cannot be null!");
            }
            mActivityReference = new WeakReference<>(activity);
            setOnErrorListener(activity);
            setOnCompletionListener(activity);
        }

        /* package */ void setDataSourceAndPrepare(Uri uri)
                throws IllegalArgumentException, IOException {
            if (uri == null || uri.toString().length() < 1) {
                throw new IllegalArgumentException("'uri' cannot be null or empty!");
            }
            AudioPreviewActivity activity = mActivityReference.get();
            if (activity != null && !activity.isFinishing()) {
                setDataSource(activity, uri);
                prepareAsync();
            }
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mIsPrepared = true;
            if (mActivityReference != null) {
                AudioPreviewActivity activity = mActivityReference.get();
                if (activity != null && !activity.isFinishing()) {
                    activity.onPrepared(mp);
                }
            }
        }

    }
}