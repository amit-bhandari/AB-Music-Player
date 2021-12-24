/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.music.player.bhandari.m.ringtoneCutter

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.afollestad.materialdialogs.MaterialDialog
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.ringtoneCutter.soundfile.SoundFile
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.io.File
import java.io.PrintWriter
import java.io.RandomAccessFile
import java.io.StringWriter
import java.util.*

/**
 * The activity for the Ringdroid main editor window.  Keeps track of
 * the waveform display, current horizontal offset, marker handles,
 * start / end text boxes, and handles all of the buttons and controls.
 */
class RingdroidEditActivity : AppCompatActivity(), MarkerView.MarkerListener,
    WaveformView.WaveformListener {
    private var mLoadingLastUpdateTime: Long = 0
    private var mLoadingKeepGoing: Boolean = false
    private var mRecordingLastUpdateTime: Long = 0
    private var mRecordingKeepGoing: Boolean = false
    private var mRecordingTime: Double = 0.0
    private var mFinishActivity: Boolean = false
    private var mTimerTextView: TextView? = null
    private var mAlertDialog: AlertDialog? = null
    private var mSoundFile: SoundFile? = null
    private var mFile: File? = null
    private var mFilename: String? = null
    private var mArtist: String? = null
    private var mTitle: String? = null
    private var mNewFileKind: Int = 0
    private var mWasGetContentIntent: Boolean = false
    private var mWaveformView: WaveformView? = null
    private var mStartMarker: MarkerView? = null
    private var mEndMarker: MarkerView? = null
    private var mStartText: TextView? = null
    private var mEndText: TextView? = null

    //private TextView mInfo;
    private var mInfoContent: String? = null
    private var mPlayButton: ImageButton? = null
    private var mRewindButton: ImageButton? = null
    private var mFfwdButton: ImageButton? = null
    private var rootView: View? = null
    private var mKeyDown: Boolean = false
    private var mCaption: String = ""
    private var mWidth: Int = 0
    private var mMaxPos: Int = 0
    private var mStartPos: Int = 0
    private var mEndPos: Int = 0
    private var mStartVisible: Boolean = false
    private var mEndVisible: Boolean = false
    private var mLastDisplayedStartPos: Int = 0
    private var mLastDisplayedEndPos: Int = 0
    private var mOffset: Int = 0
    private var mOffsetGoal: Int = 0
    private var mFlingVelocity: Int = 0
    private var mPlayStartMsec: Int = 0
    private var mPlayEndMsec: Int = 0
    private var mHandler: Handler? = null
    private var mIsPlaying: Boolean = false
    private var mPlayer: SamplePlayer? = null
    private var mTouchDragging: Boolean = false
    private var mTouchStart: Float = 0f
    private var mTouchInitialOffset: Int = 0
    private var mTouchInitialStartPos: Int = 0
    private var mTouchInitialEndPos: Int = 0
    private var mWaveformTouchStartMsec: Long = 0
    private var mDensity: Float = 0f
    private var mMarkerLeftInset: Int = 0
    private var mMarkerRightInset: Int = 0
    private var mMarkerTopOffset: Int = 0
    private var mMarkerBottomOffset: Int = 0
    private var mLoadSoundFileThread: Thread? = null
    private var mRecordAudioThread: Thread? = null
    private var mSaveSoundFileThread: Thread? = null
    //
    // Public methods and protected overrides
    //
    /** Called when the activity is first created.  */
    public override fun onCreate(icicle: Bundle?) {
        Log.v("Ringdroid", "EditActivity OnCreate")
        val themeSelector: Int = MyApp.Companion.getPref()
            .getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        super.onCreate(icicle)
        mPlayer = null
        mIsPlaying = false
        mAlertDialog = null
        mLoadSoundFileThread = null
        mRecordAudioThread = null
        mSaveSoundFileThread = null
        val intent: Intent = intent

        // If the Ringdroid media select activity was launched via a
        // GET_CONTENT intent, then we shouldn't display a "saved"
        // message when the user saves, we should just return whatever
        // they create.
        mWasGetContentIntent = intent.extras!!.getBoolean("was_get_content_intent", false)
        mFilename = intent.extras!!.getString("file_path")!!.replaceFirst("file://".toRegex(), "")
            .replace("%20".toRegex(), " ")
        mSoundFile = null
        mKeyDown = false
        mHandler = Handler()
        loadGui()
        mHandler!!.postDelayed(mTimerRunnable, 100)
        if (!(mFilename == "record")) {
            loadFromFile()
        } else {
            recordAudio()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    private fun closeThread(thread: Thread?) {
        if (thread != null && thread.isAlive) {
            try {
                thread.join()
            } catch (e: InterruptedException) {
            }
        }
    }

    /** Called when the activity is finally destroyed.  */
    override fun onDestroy() {
        Log.v("Ringdroid", "EditActivity OnDestroy")
        mLoadingKeepGoing = false
        mRecordingKeepGoing = false
        closeThread(mLoadSoundFileThread)
        closeThread(mRecordAudioThread)
        closeThread(mSaveSoundFileThread)
        mLoadSoundFileThread = null
        mRecordAudioThread = null
        mSaveSoundFileThread = null
//        if (mProgressDialog != null) {
//            mProgressDialog.dismiss()
//            mProgressDialog = null
//        }
        if (mAlertDialog != null) {
            mAlertDialog!!.dismiss()
            mAlertDialog = null
        }
        if (mPlayer != null) {
            if (mPlayer!!.isPlaying() || mPlayer!!.isPaused()) {
                mPlayer!!.stop()
            }
            mPlayer!!.release()
            mPlayer = null
        }
        super.onDestroy()
    }

    /** Called with an Activity we started with an Intent returns.  */
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        dataIntent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, dataIntent)
        Log.v("Ringdroid", "EditActivity onActivityResult")
        if (requestCode == REQUEST_CODE_CHOOSE_CONTACT) {
            // The user finished saving their ringtone and they're
            // just applying it to a contact.  When they return here,
            // they're done.
            finish()
            return
        }
    }

    /**
     * Called when the orientation changes and/or the keyboard is shown
     * or hidden.  We don't need to recreate the whole activity in this
     * case, but we do need to redo our layout somewhat.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.v("Ringdroid", "EditActivity onConfigurationChanged")
        val saveZoomLevel: Int = mWaveformView!!.getZoomLevel()
        super.onConfigurationChanged(newConfig)
        loadGui()
        mHandler!!.postDelayed(object : Runnable {
            override fun run() {
                mStartMarker!!.requestFocus()
                markerFocus(mStartMarker)
                mWaveformView!!.setZoomLevel(saveZoomLevel)
                mWaveformView!!.recomputeHeights(mDensity)
                updateDisplay()
            }
        }, 500)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.ringtone_cutter_edit_options, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_save).isVisible = true
        menu.findItem(R.id.action_reset).isVisible = true
        menu.findItem(R.id.action_about).isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                onSave()
                return true
            }
            R.id.action_reset -> {
                resetPositions()
                mOffsetGoal = 0
                updateDisplay()
                return true
            }
            R.id.action_about -> {
                onAbout(this)
                return true
            }
            R.id.home -> {
                finish()
                return true
            }
            else -> return false
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            onPlay(mStartPos)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    //
    // WaveformListener
    //
    /**
     * Every time we get a message that our waveform drew, see if we need to
     * animate and trigger another redraw.
     */
    override fun waveformDraw() {
        mWidth = mWaveformView!!.measuredWidth
        when {
            mOffsetGoal != mOffset && !mKeyDown -> updateDisplay()
            mIsPlaying -> {
                updateDisplay()
            }
            mFlingVelocity != 0 -> {
                updateDisplay()
            }
        }
    }

    override fun waveformTouchStart(x: Float) {
        mTouchDragging = true
        mTouchStart = x
        mTouchInitialOffset = mOffset
        mFlingVelocity = 0
        mWaveformTouchStartMsec = getCurrentTime()
    }

    override fun waveformTouchMove(x: Float) {
        mOffset = trap((mTouchInitialOffset + (mTouchStart - x)).toInt())
        updateDisplay()
    }

    override fun waveformTouchEnd() {
        mTouchDragging = false
        mOffsetGoal = mOffset
        val elapsedMsec: Long = getCurrentTime() - mWaveformTouchStartMsec
        if (elapsedMsec < 300) {
            if (mIsPlaying) {
                val seekMsec: Int = mWaveformView!!.pixelsToMillisecs(
                    (mTouchStart + mOffset).toInt())
                if (seekMsec in mPlayStartMsec until mPlayEndMsec
                ) {
                    mPlayer!!.seekTo(seekMsec)
                } else {
                    handlePause()
                }
            } else {
                onPlay((mTouchStart + mOffset).toInt())
            }
        }
    }

    override fun waveformFling(vx: Float) {
        mTouchDragging = false
        mOffsetGoal = mOffset
        mFlingVelocity = (-vx).toInt()
        updateDisplay()
    }

    override fun waveformZoomIn() {
        mWaveformView!!.zoomIn()
        mStartPos = mWaveformView!!.getStart()
        mEndPos = mWaveformView!!.getEnd()
        mMaxPos = mWaveformView!!.maxPos()
        mOffset = mWaveformView!!.getOffset()
        mOffsetGoal = mOffset
        updateDisplay()
    }

    override fun waveformZoomOut() {
        mWaveformView!!.zoomOut()
        mStartPos = mWaveformView!!.getStart()
        mEndPos = mWaveformView!!.getEnd()
        mMaxPos = mWaveformView!!.maxPos()
        mOffset = mWaveformView!!.getOffset()
        mOffsetGoal = mOffset
        updateDisplay()
    }

    //
    // MarkerListener
    //
    override fun markerDraw() {}
    override fun markerTouchStart(marker: MarkerView?, x: Float) {
        mTouchDragging = true
        mTouchStart = x
        mTouchInitialStartPos = mStartPos
        mTouchInitialEndPos = mEndPos
    }

    override fun markerTouchMove(marker: MarkerView?, x: Float) {
        val delta: Float = x - mTouchStart
        if (marker === mStartMarker) {
            mStartPos = trap((mTouchInitialStartPos + delta).toInt())
            mEndPos = trap((mTouchInitialEndPos + delta).toInt())
        } else {
            mEndPos = trap((mTouchInitialEndPos + delta).toInt())
            if (mEndPos < mStartPos) mEndPos = mStartPos
        }
        updateDisplay()
    }

    override fun markerTouchEnd(marker: MarkerView?) {
        mTouchDragging = false
        if (marker === mStartMarker) {
            setOffsetGoalStart()
        } else {
            setOffsetGoalEnd()
        }
    }

    override fun markerLeft(marker: MarkerView?, velocity: Int) {
        mKeyDown = true
        if (marker === mStartMarker) {
            val saveStart: Int = mStartPos
            mStartPos = trap(mStartPos - velocity)
            mEndPos = trap(mEndPos - (saveStart - mStartPos))
            setOffsetGoalStart()
        }
        if (marker === mEndMarker) {
            if (mEndPos == mStartPos) {
                mStartPos = trap(mStartPos - velocity)
                mEndPos = mStartPos
            } else {
                mEndPos = trap(mEndPos - velocity)
            }
            setOffsetGoalEnd()
        }
        updateDisplay()
    }

    override fun markerRight(marker: MarkerView?, velocity: Int) {
        mKeyDown = true
        if (marker === mStartMarker) {
            val saveStart: Int = mStartPos
            mStartPos += velocity
            if (mStartPos > mMaxPos) mStartPos = mMaxPos
            mEndPos += (mStartPos - saveStart)
            if (mEndPos > mMaxPos) mEndPos = mMaxPos
            setOffsetGoalStart()
        }
        if (marker === mEndMarker) {
            mEndPos += velocity
            if (mEndPos > mMaxPos) mEndPos = mMaxPos
            setOffsetGoalEnd()
        }
        updateDisplay()
    }

    override fun markerEnter(marker: MarkerView?) {}
    override fun markerKeyUp() {
        mKeyDown = false
        updateDisplay()
    }

    override fun markerFocus(marker: MarkerView?) {
        mKeyDown = false
        if (marker === mStartMarker) {
            setOffsetGoalStartNoUpdate()
        } else {
            setOffsetGoalEndNoUpdate()
        }

        // Delay updaing the display because if this focus was in
        // response to a touch event, we want to receive the touch
        // event too before updating the display.
        mHandler!!.postDelayed({ updateDisplay() }, 100)
    }
    //
    // Internal methods
    //
    /**
     * Called from both onCreate and onConfigurationChanged
     * (if the user switched layouts)
     */
    private fun loadGui() {
        // Inflate our UI from its XML layout description.
        ColorHelper.setStatusBarGradiant(this)
        setContentView(R.layout.ringtone_cutter_editor)


        //findViewById(R.id.playback_buttons).setBackgroundColor(ColorHelper.getBrightPrimaryColor());
        //findViewById(R.id.times).setBackgroundColor(ColorHelper.getBrightPrimaryColor());
        val toolbar: Toolbar = findViewById(R.id.toolbar_)
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        if (supportActionBar != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.GetStatusBarColor());
        }*/title = getString(R.string.title_about_us)
        val metrics: DisplayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        mDensity = metrics.density
        mMarkerLeftInset = (46 * mDensity).toInt()
        mMarkerRightInset = (48 * mDensity).toInt()
        mMarkerTopOffset = (10 * mDensity).toInt()
        mMarkerBottomOffset = (10 * mDensity).toInt()
        mStartText = findViewById<View>(R.id.starttext) as TextView?
        mStartText!!.addTextChangedListener(mTextWatcher)
        mEndText = findViewById<View>(R.id.endtext) as TextView?
        mEndText!!.addTextChangedListener(mTextWatcher)
        mPlayButton = findViewById<View>(R.id.play) as ImageButton?
        mPlayButton!!.setOnClickListener(mPlayListener)
        mRewindButton = findViewById<View>(R.id.rew) as ImageButton?
        mRewindButton!!.setOnClickListener(mRewindListener)
        mFfwdButton = findViewById<View>(R.id.ffwd) as ImageButton?
        mFfwdButton!!.setOnClickListener(mFfwdListener)
        rootView = findViewById(R.id.root_view_ringtone_cutter)
        //rootView.setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());
        val markStartButton: TextView = findViewById<View>(R.id.mark_start) as TextView
        markStartButton.setOnClickListener(mMarkStartListener)
        val markEndButton: TextView = findViewById<View>(R.id.mark_end) as TextView
        markEndButton.setOnClickListener(mMarkEndListener)
        enableDisableButtons()
        mWaveformView = findViewById<View>(R.id.waveform) as WaveformView?
        mWaveformView!!.setListener(this)

        //mInfo = (TextView)findViewById(R.id.info);
        //mInfo.setText(mCaption);
        mMaxPos = 0
        mLastDisplayedStartPos = -1
        mLastDisplayedEndPos = -1
        if (mSoundFile != null && !mWaveformView!!.hasSoundFile()) {
            mWaveformView!!.setSoundFile(mSoundFile)
            mWaveformView!!.recomputeHeights(mDensity)
            mMaxPos = mWaveformView!!.maxPos()
        }
        mStartMarker = findViewById<View>(R.id.startmarker) as MarkerView?
        mStartMarker!!.setListener(this)
        mStartMarker!!.alpha = 1f
        mStartMarker!!.isFocusable = true
        mStartMarker!!.isFocusableInTouchMode = true
        mStartVisible = true
        mEndMarker = findViewById<View>(R.id.endmarker) as MarkerView?
        mEndMarker!!.setListener(this)
        mEndMarker!!.alpha = 1f
        mEndMarker!!.isFocusable = true
        mEndMarker!!.isFocusableInTouchMode = true
        mEndVisible = true
        updateDisplay()
    }

    private fun loadFromFile() {
        mFile = File(mFilename)
        val metadataReader = SongMetadataReader(
            this, mFilename)
        mTitle = metadataReader.mTitle
        mArtist = metadataReader.mArtist
        var titleLabel: String? = mTitle
        if (mArtist != null && mArtist!!.isNotEmpty()) {
            titleLabel += " - $mArtist"
        }
        title = titleLabel
        mLoadingLastUpdateTime = getCurrentTime()
        mLoadingKeepGoing = true
        mFinishActivity = false
        /*mProgressDialog = new ProgressDialog(RingdroidEditActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(R.string.progress_dialog_loading);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(
            new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    mLoadingKeepGoing = false;
                    mFinishActivity = true;
                }
            });
        mProgressDialog.show();*/
//        mProgressDialog = MyDialogBuilder(this)
//            .title(R.string.progress_dialog_loading)
//            .progress(false, 100, true)
//            .dismissListener(object : DialogInterface.OnDismissListener {
//                override fun onDismiss(dialogInterface: DialogInterface) {
//                    mLoadingKeepGoing = false
//                    mFinishActivity = true
//                }
//            })
//            .build()
       // mProgressDialog.show()
        val listener: SoundFile.ProgressListener = object : SoundFile.ProgressListener {
            override fun reportProgress(fractionComplete: Double): Boolean {
                val now: Long = getCurrentTime()
                if (now - mLoadingLastUpdateTime > 100) {
//                    mProgressDialog.setProgress(
//                        (mProgressDialog.getMaxProgress() * fractionComplete) as Int)
                    mLoadingLastUpdateTime = now
                }
                return mLoadingKeepGoing
            }
        }

        // Load the sound file in a background thread
        mLoadSoundFileThread = object : Thread() {
            override fun run() {
                try {
                    mSoundFile = SoundFile.create(mFile!!.absolutePath, listener)
                    if (mSoundFile == null) {
                       // mProgressDialog.dismiss()
                        val name: String = mFile!!.name.lowercase(Locale.getDefault())
                        val components: Array<String> = name.split("\\.".toRegex()).toTypedArray()
                        val err: String = if (components.size < 2) {
                            resources.getString(
                                R.string.no_extension_error)
                        } else {
                            (resources.getString(
                                R.string.bad_extension_error) + " " +
                                    components[components.size - 1])
                        }
                        val finalErr: String = err
                        val runnable: Runnable =
                            Runnable { showFinalAlert(Exception(), finalErr) }
                        mHandler!!.post(runnable)
                        return
                    }
                    mPlayer = SamplePlayer(mSoundFile)
                } catch (e: Exception) {
                   // mProgressDialog.dismiss()
                    e.printStackTrace()
                    mInfoContent = e.toString()
                    /*runOnUiThread(new Runnable() {
                        public void run() {
                            mInfo.setText(mInfoContent);
                        }
                    });*/
                    val runnable: Runnable = Runnable { showFinalAlert(e, resources.getText(R.string.read_error)) }
                    mHandler!!.post(runnable)
                    return
                }
                //mProgressDialog.dismiss()
                if (mLoadingKeepGoing) {
                    val runnable: Runnable = Runnable { finishOpeningSoundFile() }
                    mHandler!!.post(runnable)
                } else if (mFinishActivity) {
                    this@RingdroidEditActivity.finish()
                }
            }
        }
        mLoadSoundFileThread!!.start()
    }

    private fun recordAudio() {
        mFile = null
        mTitle = null
        mArtist = null
        mRecordingLastUpdateTime = getCurrentTime()
        mRecordingKeepGoing = true
        mFinishActivity = false
        val adBuilder: AlertDialog.Builder = AlertDialog.Builder(this@RingdroidEditActivity)
        adBuilder.setTitle(resources.getText(R.string.progress_dialog_recording))
        adBuilder.setCancelable(true)
        adBuilder.setNegativeButton(
            resources.getText(R.string.progress_dialog_cancel)
        ) { dialog, id ->
            mRecordingKeepGoing = false
            mFinishActivity = true
        }
        adBuilder.setPositiveButton(
            resources.getText(R.string.progress_dialog_stop)
        ) { dialog, id -> mRecordingKeepGoing = false }
        // TODO(nfaralli): try to use a FrameLayout and pass it to the following inflate call.
        // Using null, android:layout_width etc. may not work (hence text is at the top of view).
        // On the other hand, if the text is big enough, this is good enough.
        adBuilder.setView(layoutInflater.inflate(R.layout.record_audio, null))
        mAlertDialog = adBuilder.show()
        mTimerTextView = mAlertDialog!!.findViewById<View>(R.id.record_audio_timer) as TextView?
        val listener: SoundFile.ProgressListener = object : SoundFile.ProgressListener {
            override fun reportProgress(elapsedTime: Double): Boolean {
                val now: Long = getCurrentTime()
                if (now - mRecordingLastUpdateTime > 5) {
                    mRecordingTime = elapsedTime
                    // Only UI thread can update Views such as TextViews.
                    runOnUiThread {
                        val min: Int = (mRecordingTime / 60).toInt()
                        val sec: Float = (mRecordingTime - 60 * min).toFloat()
                        mTimerTextView!!.text = String.format("%d:%05.2f", min, sec)
                    }
                    mRecordingLastUpdateTime = now
                }
                return mRecordingKeepGoing
            }
        }

        // Record the audio stream in a background thread
        mRecordAudioThread = object : Thread() {
            override fun run() {
                try {
                    mSoundFile = SoundFile.record(listener)
                    if (mSoundFile == null) {
                        mAlertDialog!!.dismiss()
                        val runnable = Runnable {
                            showFinalAlert(
                                Exception(),
                                resources.getText(R.string.record_error)
                            )
                        }
                        mHandler!!.post(runnable)
                        return
                    }
                    mPlayer = SamplePlayer(mSoundFile)
                } catch (e: Exception) {
                    mAlertDialog!!.dismiss()
                    e.printStackTrace()
                    mInfoContent = e.toString()
                    /*runOnUiThread(new Runnable() {
                        public void run() {
                            mInfo.setText(mInfoContent);
                        }
                    });*/
                    val runnable = Runnable { showFinalAlert(e, resources.getText(R.string.record_error)) }
                    mHandler!!.post(runnable)
                    return
                }
                mAlertDialog!!.dismiss()
                if (mFinishActivity) {
                    this@RingdroidEditActivity.finish()
                } else {
                    val runnable = Runnable { finishOpeningSoundFile() }
                    mHandler!!.post(runnable)
                }
            }
        }
        mRecordAudioThread!!.start()
    }

    private fun finishOpeningSoundFile() {
        mWaveformView!!.setSoundFile(mSoundFile)
        mWaveformView!!.recomputeHeights(mDensity)
        mMaxPos = mWaveformView!!.maxPos()
        mLastDisplayedStartPos = -1
        mLastDisplayedEndPos = -1
        mTouchDragging = false
        mOffset = 0
        mOffsetGoal = 0
        mFlingVelocity = 0
        resetPositions()
        if (mEndPos > mMaxPos) mEndPos = mMaxPos
        mCaption = (mSoundFile!!.getFiletype().toString() + ", " +
                mSoundFile!!.getSampleRate() + " Hz, " +
                mSoundFile!!.getAvgBitrateKbps() + " kbps, " +
                formatTime(mMaxPos) + " " +
                resources.getString(R.string.time_seconds))
        //mInfo.setText(mCaption);
        updateDisplay()
    }

    @Synchronized
    private fun updateDisplay() {
        if (mIsPlaying) {
            val now: Int = mPlayer!!.getCurrentPosition()
            val frames: Int = mWaveformView!!.millisecsToPixels(now)
            mWaveformView!!.setPlayback(frames)
            setOffsetGoalNoUpdate(frames - mWidth / 2)
            if (now >= mPlayEndMsec) {
                handlePause()
            }
        }
        if (!mTouchDragging) {
            var offsetDelta: Int
            if (mFlingVelocity != 0) {
                offsetDelta = mFlingVelocity / 30
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80
                } else {
                    mFlingVelocity = 0
                }
                mOffset += offsetDelta
                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2
                    mFlingVelocity = 0
                }
                if (mOffset < 0) {
                    mOffset = 0
                    mFlingVelocity = 0
                }
                mOffsetGoal = mOffset
            } else {
                offsetDelta = mOffsetGoal - mOffset
                when {
                    offsetDelta > 10 -> offsetDelta /= 10
                    offsetDelta > 0 -> offsetDelta =
                        1
                    offsetDelta < -10 -> offsetDelta /= 10
                    offsetDelta < 0 -> offsetDelta = -1
                    else -> offsetDelta = 0
                }
                mOffset += offsetDelta
            }
        }
        mWaveformView!!.setParameters(mStartPos, mEndPos, mOffset)
        mWaveformView!!.invalidate()
        mStartMarker!!.contentDescription = (resources.getText(R.string.start_marker).toString() + " " +
                formatTime(mStartPos))
        mEndMarker!!.contentDescription = (resources.getText(R.string.end_marker).toString() + " " +
                formatTime(mEndPos))
        var startX: Int = mStartPos - mOffset - mMarkerLeftInset
        when {
            startX + mStartMarker!!.width >= 0 -> {
                if (!mStartVisible) {
                    // Delay this to avoid flicker
                    mHandler!!.postDelayed({
                        mStartVisible = true
                        mStartMarker!!.alpha = 1f
                    }, 0)
                }
            }
            else -> {
                if (mStartVisible) {
                    mStartMarker!!.alpha = 0f
                    mStartVisible = false
                }
                startX = 0
            }
        }
        var endX: Int = mEndPos - mOffset - mEndMarker!!.width + mMarkerRightInset
        if (endX + mEndMarker!!.width >= 0) {
            if (!mEndVisible) {
                // Delay this to avoid flicker
                mHandler!!.postDelayed({
                    mEndVisible = true
                    mEndMarker!!.alpha = 1f
                }, 0)
            }
        } else {
            if (mEndVisible) {
                mEndMarker!!.alpha = 0f
                mEndVisible = false
            }
            endX = 0
        }
        var params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(
            startX,
            mMarkerTopOffset,
            -mStartMarker!!.width,
            -mStartMarker!!.height)
        mStartMarker!!.layoutParams = params
        params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(
            endX,
            mWaveformView!!.measuredHeight - mEndMarker!!.height - mMarkerBottomOffset,
            -mStartMarker!!.width,
            -mStartMarker!!.height)
        mEndMarker!!.layoutParams = params
    }

    private val mTimerRunnable = object: Runnable { // Updating an EditText is slow on Android.  Make sure
        // we only do the update if the text has actually changed.
        override fun run() {
            if (mStartPos != mLastDisplayedStartPos &&
                !mStartText!!.hasFocus()
            ) {
                mStartText!!.text = formatTime(mStartPos)
                mLastDisplayedStartPos = mStartPos
            }
            if (mEndPos != mLastDisplayedEndPos &&
                !mEndText!!.hasFocus()
            ) {
                mEndText!!.text = formatTime(mEndPos)
                mLastDisplayedEndPos = mEndPos
            }
            mHandler!!.postDelayed(this, 100)
        }
    }

    private fun enableDisableButtons() {
        if (mIsPlaying) {
            mPlayButton!!.setImageResource(R.drawable.pw_pause)
            mPlayButton!!.contentDescription = resources.getText(R.string.stop)
        } else {
            mPlayButton!!.setImageResource(R.drawable.pw_play)
            mPlayButton!!.contentDescription = resources.getText(R.string.play)
        }
    }

    private fun resetPositions() {
        mStartPos = mWaveformView!!.secondsToPixels(0.0)
        mEndPos = mWaveformView!!.secondsToPixels(15.0)
    }

    private fun trap(pos: Int): Int {
        if (pos < 0) return 0
        if (pos > mMaxPos) return mMaxPos
        return pos
    }

    private fun setOffsetGoalStart() {
        setOffsetGoal(mStartPos - mWidth / 2)
    }

    private fun setOffsetGoalStartNoUpdate() {
        setOffsetGoalNoUpdate(mStartPos - mWidth / 2)
    }

    private fun setOffsetGoalEnd() {
        setOffsetGoal(mEndPos - mWidth / 2)
    }

    private fun setOffsetGoalEndNoUpdate() {
        setOffsetGoalNoUpdate(mEndPos - mWidth / 2)
    }

    private fun setOffsetGoal(offset: Int) {
        setOffsetGoalNoUpdate(offset)
        updateDisplay()
    }

    private fun setOffsetGoalNoUpdate(offset: Int) {
        if (mTouchDragging) {
            return
        }
        mOffsetGoal = offset
        if (mOffsetGoal + mWidth / 2 > mMaxPos) mOffsetGoal = mMaxPos - mWidth / 2
        if (mOffsetGoal < 0) mOffsetGoal = 0
    }

    private fun formatTime(pixels: Int): String {
        return if (mWaveformView != null && mWaveformView!!.isInitialized()) {
            formatDecimal(mWaveformView!!.pixelsToSeconds(pixels))
        } else {
            ""
        }
    }

    private fun formatDecimal(x: Double): String {
        var xWhole: Int = x.toInt()
        var xFrac: Int = (100 * (x - xWhole) + 0.5) as Int
        if (xFrac >= 100) {
            xWhole++ //Round up
            xFrac -= 100 //Now we need the remainder after the round up
            if (xFrac < 10) {
                xFrac *= 10 //we need a fraction that is 2 digits long
            }
        }
        return if (xFrac < 10) "$xWhole.0$xFrac" else "$xWhole.$xFrac"
    }

    @Synchronized
    private fun handlePause() {
        if (mPlayer != null && mPlayer!!.isPlaying()) {
            mPlayer!!.pause()
        }
        mWaveformView!!.setPlayback(-1)
        mIsPlaying = false
        enableDisableButtons()
    }

    @Synchronized
    private fun onPlay(startPosition: Int) {
        if (mIsPlaying) {
            handlePause()
            return
        }
        if (mPlayer == null) {
            // Not initialized yet
            return
        }
        try {
            mPlayStartMsec = mWaveformView!!.pixelsToMillisecs(startPosition)
            mPlayEndMsec = when {
                startPosition < mStartPos -> {
                    mWaveformView!!.pixelsToMillisecs(mStartPos)
                }
                startPosition > mEndPos -> {
                    mWaveformView!!.pixelsToMillisecs(mMaxPos)
                }
                else -> {
                    mWaveformView!!.pixelsToMillisecs(mEndPos)
                }
            }
            mPlayer!!.setOnCompletionListener(object : SamplePlayer.OnCompletionListener {
                override fun onCompletion() {
                    handlePause()
                }
            })
            mIsPlaying = true
            mPlayer!!.seekTo(mPlayStartMsec)
            mPlayer!!.start()
            updateDisplay()
            enableDisableButtons()
        } catch (e: Exception) {
            showFinalAlert(e, R.string.play_error)
            return
        }
    }

    /**
     * Show a "final" alert dialog that will exit the activity
     * after the user clicks on the OK button.  If an exception
     * is passed, it's assumed to be an error condition, and the
     * dialog is presented as an error, and the stack trace is
     * logged.  If there's no exception, it's a success message.
     */
    private fun showFinalAlert(e: Exception?, message: CharSequence) {
        val title: CharSequence
        if (e != null) {
            Log.e("Ringdroid", "Error: $message")
            Log.e("Ringdroid", getStackTrace(e))
            title = resources.getText(R.string.alert_title_failure)
            setResult(Activity.RESULT_CANCELED, Intent())
        } else {
            Log.v("Ringdroid", "Success: $message")
            title = resources.getText(R.string.alert_title_success)
        }

        /*new AlertDialog.Builder(RingdroidEditActivity.this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(
                R.string.alert_ok_button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        finish();
                    }
                })
            .setCancelable(false)
            .show();*/
//        MyDialogBuilder(this)
//            .title(title)
//            .content(message)
//            .positiveButton(R.string.okay)
//            .cancelable(false)
//            .show()
    }

    private fun showFinalAlert(e: Exception, messageResourceId: Int) {
        showFinalAlert(e, resources.getText(messageResourceId))
    }

    private fun makeRingtoneFilename(title: CharSequence, extension: String): String? {
        val subdir: String
        var externalRootDir: String = Environment.getExternalStorageDirectory().path
        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/"
        }
        subdir = when (mNewFileKind) {
            FileSaveDialog.FILE_KIND_MUSIC ->             // TODO(nfaralli): can directly use Environment.getExternalStoragePublicDirectory(
                // Environment.DIRECTORY_MUSIC).getPath() instead
                "media/audio/music/"
            FileSaveDialog.FILE_KIND_ALARM -> "media/audio/alarms/"
            FileSaveDialog.FILE_KIND_NOTIFICATION -> "media/audio/notifications/"
            FileSaveDialog.FILE_KIND_RINGTONE -> "media/audio/ringtones/"
            else -> "media/audio/music/"
        }
        var parentdir: String = externalRootDir + subdir

        // Create the parent directory
        val parentDirFile = File(parentdir)
        parentDirFile.mkdirs()

        // If we can't write to that special path, try just writing
        // directly to the sdcard
        if (!parentDirFile.isDirectory) {
            parentdir = externalRootDir
        }

        // Turn the title into a filename
        var filename = ""
        for (i in title.indices) {
            if (Character.isLetterOrDigit(title[i])) {
                filename += title[i]
            }
        }

        // Try to make the filename unique
        var path: String? = null
        for (i in 0..99) {
            var testPath: String?
            if (i > 0) testPath = parentdir + filename + i + extension else testPath =
                parentdir + filename + extension
            try {
                val f = RandomAccessFile(File(testPath), "r")
                f.close()
            } catch (e: Exception) {
                // Good, the file didn't exist
                path = testPath
                break
            }
        }
        return path
    }

    private fun saveRingtone(title: CharSequence) {
        val startTime: Double = mWaveformView!!.pixelsToSeconds(mStartPos)
        val endTime: Double = mWaveformView!!.pixelsToSeconds(mEndPos)
        val startFrame: Int = mWaveformView!!.secondsToFrames(startTime)
        val endFrame: Int = mWaveformView!!.secondsToFrames(endTime)
        val duration: Int = (endTime - startTime + 0.5).toInt()

        // Create an indeterminate progress dialog
        /*mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle(R.string.progress_dialog_saving);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();*/
//        mProgressDialog = MyDialogBuilder(this)
//            .title(R.string.progress_dialog_saving)
//            .progress(true, 0)
//            .cancelable(false)
//            .build()
//        mProgressDialog.show()

        // Save the sound file in a background thread
        mSaveSoundFileThread = object : Thread() {
            override fun run() {
                // Try AAC first.
                var outPath: String? = makeRingtoneFilename(title, ".m4a")
                if (outPath == null) {
                    val runnable: Runnable = Runnable { showFinalAlert(Exception(), R.string.no_unique_filename) }
                    mHandler!!.post(runnable)
                    return
                }
                var outFile = File(outPath)
                var fallbackToWAV = false
                try {
                    // Write the new file
                    mSoundFile!!.WriteFile(outFile, startFrame, endFrame - startFrame)
                } catch (e: Exception) {
                    // log the error and try to create a .wav file instead
                    if (outFile.exists()) {
                        outFile.delete()
                    }
                    val writer = StringWriter()
                    e.printStackTrace(PrintWriter(writer))
                    Log.e("Ringdroid", "Error: Failed to create $outPath")
                    Log.e("Ringdroid", writer.toString())
                    fallbackToWAV = true
                }

                // Try to create a .wav file if creating a .m4a file failed.
                if (fallbackToWAV) {
                    outPath = makeRingtoneFilename(title, ".wav")
                    if (outPath == null) {
                        val runnable = Runnable { showFinalAlert(Exception(), R.string.no_unique_filename) }
                        mHandler!!.post(runnable)
                        return
                    }
                    outFile = File(outPath)
                    try {
                        // create the .wav file
                        mSoundFile!!.WriteWAVFile(outFile, startFrame, endFrame - startFrame)
                    } catch (e: Exception) {
                        // Creating the .wav file also failed. Stop the progress dialog, show an
                        // error message and exit.
                       // mProgressDialog.dismiss()
                        if (outFile.exists()) {
                            outFile.delete()
                        }
                        mInfoContent = e.toString()
                        /*runOnUiThread(new Runnable() {
                            public void run() {
                                mInfo.setText(mInfoContent);
                            }
                        });*/
                        val errorMessage: CharSequence = when {
                            e.message != null
                                    && (e.message == "No space left on device") -> {
                                resources.getText(R.string.no_space_error)
                            }
                            else -> {
                                resources.getText(R.string.write_error)
                            }
                        }
                        val finalErrorMessage: CharSequence = errorMessage
                        val finalException: Exception = e
                        val runnable: Runnable =
                            Runnable { showFinalAlert(finalException, finalErrorMessage) }
                        mHandler!!.post(runnable)
                        return
                    }
                }

                // Try to load the new file to make sure it worked
                try {
                    val listener: SoundFile.ProgressListener = object : SoundFile.ProgressListener {
                        override fun reportProgress(frac: Double): Boolean {
                            // Do nothing - we're not going to try to
                            // estimate when reloading a saved sound
                            // since it's usually fast, but hard to
                            // estimate anyway.
                            return true // Keep going
                        }
                    }
                    SoundFile.create(outPath, listener)
                } catch (e: Exception) {
                   // mProgressDialog.dismiss()
                    e.printStackTrace()
                    mInfoContent = e.toString()
                    /*runOnUiThread(new Runnable() {
                        public void run() {
                            mInfo.setText(mInfoContent);
                        }
                    });*/
                    val runnable = Runnable { showFinalAlert(e, resources.getText(R.string.write_error)) }
                    mHandler!!.post(runnable)
                    return
                }
               // mProgressDialog.dismiss()
                val finalOutPath: String = outPath
                val runnable = Runnable {
                    afterSavingRingtone(title,
                        finalOutPath,
                        duration)
                }
                mHandler!!.post(runnable)
            }
        }
        mSaveSoundFileThread!!.start()
    }

    private fun afterSavingRingtone(title: CharSequence, outPath: String, duration: Int) {
        val outFile = File(outPath)
        val fileSize: Long = outFile.length()
        if (fileSize <= 512) {
            outFile.delete()
            /*new AlertDialog.Builder(this)
                .setTitle(R.string.alert_title_failure)
                .setMessage(R.string.too_small_error)
                .setPositiveButton(R.string.alert_ok_button, null)
                .setCancelable(false)
                .show();*/
            MaterialDialog(this)
                .title(R.string.alert_title_failure)
                .message(R.string.too_small_error)
                .positiveButton(R.string.okay)
                .cancelable(false)
                .show()
            return
        }

        // Create the database record, pointing to the existing file path
        val mimeType: String = when {
            outPath.endsWith(".m4a") -> {
                "audio/mp4a-latm"
            }
            outPath.endsWith(".wav") -> {
                "audio/wav"
            }
            else -> {
                // This should never happen.
                "audio/mpeg"
            }
        }
        val artist: String = "" + resources.getText(R.string.artist_name)
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DATA, outPath)
        values.put(MediaStore.MediaColumns.TITLE, title.toString())
        values.put(MediaStore.MediaColumns.SIZE, fileSize)
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        values.put(MediaStore.Audio.Media.ARTIST, artist)
        values.put(MediaStore.Audio.Media.DURATION, duration)
        values.put(MediaStore.Audio.Media.IS_RINGTONE, mNewFileKind == FileSaveDialog.FILE_KIND_RINGTONE)
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, mNewFileKind == FileSaveDialog.FILE_KIND_NOTIFICATION)
        values.put(MediaStore.Audio.Media.IS_ALARM, mNewFileKind == FileSaveDialog.FILE_KIND_ALARM)
        values.put(MediaStore.Audio.Media.IS_MUSIC, mNewFileKind == FileSaveDialog.FILE_KIND_MUSIC)

        // Insert it into the database
        val uri = MediaStore.Audio.Media.getContentUriForPath(outPath)
        val newUri = contentResolver.insert(uri!!, values)
        setResult(Activity.RESULT_OK, Intent().setData(newUri))

        // If Ringdroid was launched to get content, just return
        if (mWasGetContentIntent) {
            finish()
            return
        }

        // There's nothing more to do with music or an alarm.  Show a
        // success message and then quit.
        if (mNewFileKind == FileSaveDialog.FILE_KIND_MUSIC ||
            mNewFileKind == FileSaveDialog.FILE_KIND_ALARM
        ) {
            Toast.makeText(this,
                R.string.save_success_message,
                Toast.LENGTH_SHORT)
                .show()
            finish()
            return
        }

        // If it's a notification, give the user the option of making
        // this their default notification.  If they say no, we're finished.
        if (mNewFileKind == FileSaveDialog.FILE_KIND_NOTIFICATION) {
            /*new AlertDialog.Builder(RingdroidEditActivity.this)
                .setTitle(R.string.alert_title_success)
                .setMessage(R.string.set_default_notification)
                .setPositiveButton(R.string.alert_yes_button,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            RingtoneManager.setActualDefaultRingtoneUri(
                                RingdroidEditActivity.this,
                                RingtoneManager.TYPE_NOTIFICATION,
                                newUri);
                            finish();
                        }
                    })
                .setNegativeButton(
                    R.string.alert_no_button,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    })
                .setCancelable(false)
                .show();*/
            MaterialDialog(this)
                .title(R.string.alert_title_success)
                .message(R.string.set_default_notification)
                .positiveButton(R.string.alert_yes_button){
                    RingtoneManager.setActualDefaultRingtoneUri(
                        this@RingdroidEditActivity,
                        RingtoneManager.TYPE_NOTIFICATION,
                        newUri)
                    finish()
                }
                .negativeButton(R.string.alert_no_button)
                .cancelable(false)
                .show()
            return
        }

        // If we get here, that means the type is a ringtone.  There are
        // three choices: make this your default ringtone, assign it to a
        // contact, or do nothing.
        val handler: Handler = object : Handler() {
            override fun handleMessage(response: Message) {
                when (response.arg1) {
                    R.id.button_make_default -> {
                        RingtoneManager.setActualDefaultRingtoneUri(
                            this@RingdroidEditActivity,
                            RingtoneManager.TYPE_RINGTONE,
                            newUri)
                        Toast.makeText(
                            this@RingdroidEditActivity,
                            R.string.default_ringtone_success_message,
                            Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                    R.id.button_do_nothing -> finish()
                    else -> finish()
                }
            }
        }
        val message: Message = Message.obtain(handler)
        val dlog = AfterSaveActionDialog(
            this, message)
        dlog.show()
    }

    private fun chooseContactForRingtone(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_EDIT, uri)
            intent.setClassName(
                "com.ringdroid",
                "com.ringdroid.ChooseContactActivity")
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_CONTACT)
        } catch (e: Exception) {
            Log.e("Ringdroid", "Couldn't open Choose Contact window")
        }
    }

    private fun onSave() {
        if (mIsPlaying) {
            handlePause()
        }
        val handler: Handler = object : Handler() {
            override fun handleMessage(response: Message) {
                val newTitle: CharSequence = response.obj as CharSequence
                mNewFileKind = response.arg1
                saveRingtone(newTitle)
            }
        }
        val message: Message = Message.obtain(handler)
        val dlog = FileSaveDialog(
            this, resources, mTitle, message)
        dlog.show()
    }

    private val mPlayListener: View.OnClickListener = View.OnClickListener { onPlay(mStartPos) }
    private val mRewindListener: View.OnClickListener = View.OnClickListener {
        if (mIsPlaying) {
            var newPos: Int = mPlayer!!.getCurrentPosition() - 5000
            if (newPos < mPlayStartMsec) newPos = mPlayStartMsec
            mPlayer!!.seekTo(newPos)
        } else {
            mStartMarker!!.requestFocus()
            markerFocus(mStartMarker)
        }
    }
    private val mFfwdListener: View.OnClickListener = View.OnClickListener {
        if (mIsPlaying) {
            var newPos: Int = 5000 + mPlayer!!.getCurrentPosition()
            if (newPos > mPlayEndMsec) newPos = mPlayEndMsec
            mPlayer!!.seekTo(newPos)
        } else {
            mEndMarker!!.requestFocus()
            markerFocus(mEndMarker)
        }
    }
    private val mMarkStartListener: View.OnClickListener = View.OnClickListener {
        if (mIsPlaying) {
            mStartPos = mWaveformView!!.millisecsToPixels(
                mPlayer!!.getCurrentPosition())
            updateDisplay()
        }
    }
    private val mMarkEndListener: View.OnClickListener = View.OnClickListener {
        if (mIsPlaying) {
            mEndPos = mWaveformView!!.millisecsToPixels(
                mPlayer!!.getCurrentPosition())
            updateDisplay()
            handlePause()
        }
    }
    private val mTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence, start: Int,
            count: Int, after: Int
        ) {
        }

        override fun onTextChanged(
            s: CharSequence,
            start: Int, before: Int, count: Int
        ) {
        }

        override fun afterTextChanged(s: Editable) {
            if (mStartText!!.hasFocus()) {
                try {
                    mStartPos = mWaveformView!!.secondsToPixels(
                        mStartText!!.text.toString().toDouble())
                    updateDisplay()
                } catch (e: NumberFormatException) {
                }
            }
            if (mEndText!!.hasFocus()) {
                try {
                    mEndPos = mWaveformView!!.secondsToPixels(
                        mEndText!!.text.toString().toDouble())
                    updateDisplay()
                } catch (e: NumberFormatException) {
                }
            }
        }
    }

    private fun getCurrentTime(): Long {
        return System.nanoTime() / 1000000
    }

    private fun getStackTrace(e: Exception): String {
        val writer = StringWriter()
        e.printStackTrace(PrintWriter(writer))
        return writer.toString()
    }

    companion object {
        // Result codes
        private val REQUEST_CODE_CHOOSE_CONTACT: Int = 1

        /**
         * This is a special intent action that means "edit a sound file".
         */
        val EDIT: String = "com.ringdroid.action.EDIT"

        //
        // Static About dialog method, also called from RingdroidSelectActivity
        //
        fun onAbout(activity: Activity) {
            var versionName: String? = ""
            versionName = try {
                val packageManager: PackageManager = activity.packageManager
                val packageName: String = activity.packageName
                packageManager.getPackageInfo(packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                "unknown"
            }
            /*new AlertDialog.Builder(activity)
            .setTitle(R.string.about_title)
            .setMessage(activity.getString(R.string.about_text, versionName))
            .setPositiveButton(R.string.alert_ok_button, null)
            .setCancelable(false)
            .show();*/
            MaterialDialog(activity)
                .title(R.string.about_title)
                .message(R.string.about_text, versionName)
                .positiveButton(R.string.okay)
                .cancelable(false)
                .show()
        }
    }
}