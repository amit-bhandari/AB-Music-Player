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

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.ringtoneCutter.soundfile.SoundFile

/**
 * WaveformView is an Android view that displays a visual representation
 * of an audio waveform.  It retrieves the frame gains from a CheapSoundFile
 * object and recomputes the shape contour at several zoom levels.
 *
 * This class doesn't handle selection or any of the touch interactions
 * directly, so it exposes a listener interface.  The class that embeds
 * this view should add itself as a listener and make the view scroll
 * and respond to other events appropriately.
 *
 * WaveformView doesn't actually handle selection, but it will just display
 * the selected part of the waveform in a different color.
 */
class WaveformView(
    context: Context?,
    attrs: AttributeSet?,
    private var mZoomFactorByZoomLevel: DoubleArray
) : View(context, attrs) {
    interface WaveformListener {
        fun waveformTouchStart(x: Float)
        fun waveformTouchMove(x: Float)
        fun waveformTouchEnd()
        fun waveformFling(x: Float)
        fun waveformDraw()
        fun waveformZoomIn()
        fun waveformZoomOut()
    }

    // Colors
    private val mGridPaint: Paint
    private val mSelectedLinePaint: Paint
    private val mUnselectedLinePaint: Paint
    private val mUnselectedBkgndLinePaint: Paint
    private val mBorderLinePaint: Paint
    private val mPlaybackLinePaint: Paint
    private val mTimecodePaint: Paint
    private var mSoundFile: SoundFile?
    private var mLenByZoomLevel: IntArray?
    private var mValuesByZoomLevel: Array<DoubleArray?>?
    private var mHeightsAtThisZoomLevel: IntArray?
    private var mZoomLevel: Int = 0
    private var mNumZoomLevels: Int = 0
    private var mSampleRate: Int = 0
    private var mSamplesPerFrame: Int = 0
    private var mOffset: Int
    private var mSelectionStart: Int
    private var mSelectionEnd: Int
    private var mPlaybackPos: Int
    private var mDensity: Float
    private var mInitialScaleSpan: Float = 0f
    private var mListener: WaveformListener? = null
    private val mGestureDetector: GestureDetector
    private val mScaleGestureDetector: ScaleGestureDetector
    private var mInitialized: Boolean

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleGestureDetector.onTouchEvent(event)
        if (mGestureDetector.onTouchEvent(event)) {
            return true
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> mListener!!.waveformTouchStart(event.x)
            MotionEvent.ACTION_MOVE -> mListener!!.waveformTouchMove(event.x)
            MotionEvent.ACTION_UP -> mListener!!.waveformTouchEnd()
        }
        return true
    }

    fun hasSoundFile(): Boolean {
        return mSoundFile != null
    }

    fun setSoundFile(soundFile: SoundFile?) {
        mSoundFile = soundFile
        mSampleRate = mSoundFile!!.getSampleRate()
        mSamplesPerFrame = mSoundFile!!.getSamplesPerFrame()
        computeDoublesForAllZoomLevels()
        mHeightsAtThisZoomLevel = null
    }

    fun isInitialized(): Boolean {
        return mInitialized
    }

    fun getZoomLevel(): Int {
        return mZoomLevel
    }

    fun setZoomLevel(zoomLevel: Int) {
        while (mZoomLevel > zoomLevel) {
            zoomIn()
        }
        while (mZoomLevel < zoomLevel) {
            zoomOut()
        }
    }

    fun canZoomIn(): Boolean {
        return (mZoomLevel > 0)
    }

    fun zoomIn() {
        if (canZoomIn()) {
            mZoomLevel--
            mSelectionStart *= 2
            mSelectionEnd *= 2
            mHeightsAtThisZoomLevel = null
            var offsetCenter: Int = mOffset + measuredWidth / 2
            offsetCenter *= 2
            mOffset = offsetCenter - measuredWidth / 2
            if (mOffset < 0) mOffset = 0
            invalidate()
        }
    }

    fun canZoomOut(): Boolean {
        return (mZoomLevel < mNumZoomLevels - 1)
    }

    fun zoomOut() {
        if (canZoomOut()) {
            mZoomLevel++
            mSelectionStart /= 2
            mSelectionEnd /= 2
            var offsetCenter: Int = mOffset + measuredWidth / 2
            offsetCenter /= 2
            mOffset = offsetCenter - measuredWidth / 2
            if (mOffset < 0) mOffset = 0
            mHeightsAtThisZoomLevel = null
            invalidate()
        }
    }

    fun maxPos(): Int {
        return mLenByZoomLevel!!.get(mZoomLevel)
    }

    fun secondsToFrames(seconds: Double): Int {
        return (1.0 * seconds * mSampleRate / mSamplesPerFrame + 0.5).toInt()
    }

    fun secondsToPixels(seconds: Double): Int {
        val z: Double = mZoomFactorByZoomLevel.get(mZoomLevel)
        return (z * seconds * mSampleRate / mSamplesPerFrame + 0.5).toInt()
    }

    fun pixelsToSeconds(pixels: Int): Double {
        val z: Double = mZoomFactorByZoomLevel.get(mZoomLevel)
        return (pixels * mSamplesPerFrame.toDouble() / (mSampleRate * z))
    }

    fun millisecsToPixels(msecs: Int): Int {
        val z: Double = mZoomFactorByZoomLevel.get(mZoomLevel)
        return ((msecs * 1.0 * mSampleRate * z) /
                (1000.0 * mSamplesPerFrame) + 0.5).toInt()
    }

    fun pixelsToMillisecs(pixels: Int): Int {
        val z: Double = mZoomFactorByZoomLevel.get(mZoomLevel)
        return (pixels * (1000.0 * mSamplesPerFrame) /
                (mSampleRate * z) + 0.5).toInt()
    }

    fun setParameters(start: Int, end: Int, offset: Int) {
        mSelectionStart = start
        mSelectionEnd = end
        mOffset = offset
    }

    fun getStart(): Int {
        return mSelectionStart
    }

    fun getEnd(): Int {
        return mSelectionEnd
    }

    fun getOffset(): Int {
        return mOffset
    }

    fun setPlayback(pos: Int) {
        mPlaybackPos = pos
    }

    fun setListener(listener: WaveformListener?) {
        mListener = listener
    }

    fun recomputeHeights(density: Float) {
        mHeightsAtThisZoomLevel = null
        mDensity = density
        mTimecodePaint.textSize = (12 * density)
        invalidate()
    }

    fun drawWaveformLine(
        canvas: Canvas,
        x: Int, y0: Int, y1: Int,
        paint: Paint?
    ) {
        canvas.drawLine(x.toFloat(), y0.toFloat(), x.toFloat(), y1.toFloat(), paint!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mSoundFile == null) return
        if (mHeightsAtThisZoomLevel == null) computeIntsForThisZoomLevel()

        // Draw waveform
        val measuredWidth: Int = measuredWidth
        val measuredHeight: Int = measuredHeight
        val start: Int = mOffset
        var width: Int = mHeightsAtThisZoomLevel!!.size - start
        val ctr: Int = measuredHeight / 2
        if (width > measuredWidth) width = measuredWidth

        // Draw grid
        val onePixelInSecs: Double = pixelsToSeconds(1)
        val onlyEveryFiveSecs: Boolean = (onePixelInSecs > 1.0 / 50.0)
        var fractionalSecs: Double = mOffset * onePixelInSecs
        var integerSecs: Int = fractionalSecs.toInt()
        var i = 0
        while (i < width) {
            i++
            fractionalSecs += onePixelInSecs
            val integerSecsNew: Int = fractionalSecs.toInt()
            if (integerSecsNew != integerSecs) {
                integerSecs = integerSecsNew
                if (!onlyEveryFiveSecs || 0 == (integerSecs % 5)) {
                    canvas.drawLine(i.toFloat(),
                        0f,
                        i.toFloat(),
                        measuredHeight.toFloat(),
                        mGridPaint)
                }
            }
        }

        // Draw waveform
        i = 0
        while (i < width) {
            val paint = when {
                i + start in mSelectionStart until mSelectionEnd -> {
                    mSelectedLinePaint
                }
                else -> {
                    drawWaveformLine(canvas, i, 0, measuredHeight,
                        mUnselectedBkgndLinePaint)
                    mUnselectedLinePaint
                }
            }
            drawWaveformLine(
                canvas, i,
                ctr - mHeightsAtThisZoomLevel!!.get(start + i),
                ctr + 1 + mHeightsAtThisZoomLevel!!.get(start + i),
                paint)
            if (i + start == mPlaybackPos) {
                canvas.drawLine(i.toFloat(),
                    0f,
                    i.toFloat(),
                    measuredHeight.toFloat(),
                    mPlaybackLinePaint)
            }
            i++
        }

        // If we can see the right edge of the waveform, draw the
        // non-waveform area to the right as unselected
        i = width
        while (i < measuredWidth) {
            drawWaveformLine(canvas, i, 0, measuredHeight,
                mUnselectedBkgndLinePaint)
            i++
        }

        // Draw borders
        canvas.drawLine(
            mSelectionStart - mOffset + 0.5f, 30f,
            mSelectionStart - mOffset + 0.5f, measuredHeight.toFloat(),
            mBorderLinePaint)
        canvas.drawLine(
            mSelectionEnd - mOffset + 0.5f, 0f,
            mSelectionEnd - mOffset + 0.5f, (measuredHeight - 30).toFloat(),
            mBorderLinePaint)

        // Draw timecode
        var timecodeIntervalSecs: Double = 1.0
        if (timecodeIntervalSecs / onePixelInSecs < 50) {
            timecodeIntervalSecs = 5.0
        }
        if (timecodeIntervalSecs / onePixelInSecs < 50) {
            timecodeIntervalSecs = 15.0
        }

        // Draw grid
        fractionalSecs = mOffset * onePixelInSecs
        var integerTimecode: Int = (fractionalSecs / timecodeIntervalSecs).toInt()
        i = 0
        while (i < width) {
            i++
            fractionalSecs += onePixelInSecs
            integerSecs = fractionalSecs.toInt()
            val integerTimecodeNew: Int = (fractionalSecs /
                    timecodeIntervalSecs).toInt()
            if (integerTimecodeNew != integerTimecode) {
                integerTimecode = integerTimecodeNew

                // Turn, e.g. 67 seconds into "1:07"
                val timecodeMinutes: String = "" + (integerSecs / 60)
                var timecodeSeconds: String = "" + (integerSecs % 60)
                if ((integerSecs % 60) < 10) {
                    timecodeSeconds = "0$timecodeSeconds"
                }
                val timecodeStr = "$timecodeMinutes:$timecodeSeconds"
                val offset: Float = (0.5 * mTimecodePaint.measureText(timecodeStr)) as Float
                canvas.drawText(timecodeStr,
                    i - offset,
                    (12 * mDensity),
                    mTimecodePaint)
            }
        }
        if (mListener != null) {
            mListener!!.waveformDraw()
        }
    }

    /**
     * Called once when a new sound file is added
     */
    private fun computeDoublesForAllZoomLevels() {
        val numFrames: Int = mSoundFile!!.getNumFrames()
        val frameGains: IntArray = mSoundFile!!.getFrameGains()
        val smoothedGains = DoubleArray(numFrames)
        when {
            numFrames == 1 -> {
                smoothedGains[0] = frameGains[0].toDouble()
            }
            numFrames == 2 -> {
                smoothedGains[0] = frameGains[0].toDouble()
                smoothedGains[1] = frameGains[1].toDouble()
            }
            numFrames > 2 -> {
                smoothedGains[0] = ((frameGains[0] / 2.0) +
                        (frameGains[1] / 2.0))
                for (i in 1 until numFrames - 1) {
                    smoothedGains[i] = (((frameGains[i - 1] / 3.0) +
                            (frameGains[i] / 3.0) +
                            (frameGains[i + 1] / 3.0)))
                }
                smoothedGains[numFrames - 1] = ((frameGains[numFrames - 2] / 2.0) +
                        (frameGains[numFrames - 1] / 2.0))
            }
        }

        // Make sure the range is no more than 0 - 255
        var maxGain = 1.0
        for (i in 0 until numFrames) {
            if (smoothedGains[i] > maxGain) {
                maxGain = smoothedGains[i]
            }
        }
        var scaleFactor: Double = 1.0
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain
        }

        // Build histogram of 256 bins and figure out the new scaled max
        maxGain = 0.0
        val gainHist: IntArray = IntArray(256)
        for (i in 0 until numFrames) {
            var smoothedGain: Int = (smoothedGains[i] * scaleFactor).toInt()
            if (smoothedGain < 0) smoothedGain = 0
            if (smoothedGain > 255) smoothedGain = 255
            if (smoothedGain > maxGain) maxGain = smoothedGain.toDouble()
            gainHist[smoothedGain]++
        }

        // Re-calibrate the min to be 5%
        var minGain: Double = 0.0
        var sum: Int = 0
        while (minGain < 255 && sum < numFrames / 20) {
            sum += gainHist.get(minGain.toInt())
            minGain++
        }

        // Re-calibrate the max to be 99%
        sum = 0
        while (maxGain > 2 && sum < numFrames / 100) {
            sum += gainHist.get(maxGain.toInt())
            maxGain--
        }

        // Compute the heights
        val heights: DoubleArray = DoubleArray(numFrames)
        val range: Double = maxGain - minGain
        for (i in 0 until numFrames) {
            var value: Double = (smoothedGains[i] * scaleFactor - minGain) / range
            if (value < 0.0) value = 0.0
            if (value > 1.0) value = 1.0
            heights[i] = value * value
        }
        mNumZoomLevels = 5
        mLenByZoomLevel = IntArray(5)
        mZoomFactorByZoomLevel = DoubleArray(5)
        mValuesByZoomLevel = arrayOfNulls(5)

        // Level 0 is doubled, with interpolated values
        mLenByZoomLevel!![0] = numFrames * 2
        mZoomFactorByZoomLevel[0] = 2.0
        mValuesByZoomLevel!![0] = DoubleArray(mLenByZoomLevel!!.get(0))
        if (numFrames > 0) {
            mValuesByZoomLevel!![0]!![0] = 0.5 * heights[0]
            mValuesByZoomLevel!![0]!![1] = heights[0]
        }
        for (i in 1 until numFrames) {
            mValuesByZoomLevel!![0]!![2 * i] = 0.5 * (heights[i - 1] + heights[i])
            mValuesByZoomLevel!![0]!![2 * i + 1] = heights[i]
        }

        // Level 1 is normal
        mLenByZoomLevel!![1] = numFrames
        mValuesByZoomLevel!![1] = DoubleArray(mLenByZoomLevel!![1])
        mZoomFactorByZoomLevel[1] = 1.0
        for (i in 0 until mLenByZoomLevel!![1]) {
            mValuesByZoomLevel!![1]!![i] = heights[i]
        }

        // 3 more levels are each halved
        for (j in 2..4) {
            mLenByZoomLevel!![j] = mLenByZoomLevel!![j - 1] / 2
            mValuesByZoomLevel!![j] = DoubleArray(mLenByZoomLevel!![j])
            mZoomFactorByZoomLevel[j] = mZoomFactorByZoomLevel[j - 1] / 2.0
            for (i in 0 until mLenByZoomLevel!![j]) {
                mValuesByZoomLevel!![j]!![i] =
                    0.5 * (mValuesByZoomLevel!![j - 1]!![2 * i] +
                            mValuesByZoomLevel!![j - 1]!![2 * i + 1])
            }
        }
        mZoomLevel = when {
            numFrames > 5000 -> {
                3
            }
            numFrames > 1000 -> {
                2
            }
            numFrames > 300 -> {
                1
            }
            else -> {
                0
            }
        }
        mInitialized = true
    }

    /**
     * Called the first time we need to draw when the zoom level has changed
     * or the screen is resized
     */
    private fun computeIntsForThisZoomLevel() {
        val halfHeight: Int = (measuredHeight / 2) - 1
        mHeightsAtThisZoomLevel = IntArray(mLenByZoomLevel!!.get(mZoomLevel))
        for (i in 0 until mLenByZoomLevel!!.get(mZoomLevel)) {
            mHeightsAtThisZoomLevel!![i] = (mValuesByZoomLevel!!.get(mZoomLevel)!![i] * halfHeight).toInt()
        }
    }

    init {
        // We don't want keys, the markers get these
        isFocusable = false
        val res: Resources = resources
        mGridPaint = Paint()
        mGridPaint.isAntiAlias = false
        mGridPaint.color = res.getColor(R.color.grid_line)
        mSelectedLinePaint = Paint()
        mSelectedLinePaint.isAntiAlias = false
        mSelectedLinePaint.color = res.getColor(R.color.waveform_selected)
        mUnselectedLinePaint = Paint()
        mUnselectedLinePaint.isAntiAlias = false
        mUnselectedLinePaint.color = res.getColor(R.color.waveform_unselected)
        mUnselectedBkgndLinePaint = Paint()
        mUnselectedBkgndLinePaint.isAntiAlias = false
        mUnselectedBkgndLinePaint.color = res.getColor(R.color.waveform_unselected_bkgnd_overlay)
        mBorderLinePaint = Paint()
        mBorderLinePaint.isAntiAlias = true
        mBorderLinePaint.strokeWidth = 1.5f
        mBorderLinePaint.pathEffect = DashPathEffect(floatArrayOf(3.0f, 2.0f), 0.0f)
        mBorderLinePaint.color = res.getColor(R.color.selection_border)
        mPlaybackLinePaint = Paint()
        mPlaybackLinePaint.isAntiAlias = false
        mPlaybackLinePaint.color = res.getColor(R.color.playback_indicator)
        mTimecodePaint = Paint()
        mTimecodePaint.textSize = 12f
        mTimecodePaint.isAntiAlias = true
        mTimecodePaint.color = res.getColor(R.color.timecode)
        mTimecodePaint.setShadowLayer(2f, 1f, 1f, res.getColor(R.color.timecode_shadow))
        mGestureDetector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent,
                    e2: MotionEvent,
                    vx: Float,
                    vy: Float
                ): Boolean {
                    mListener!!.waveformFling(vx)
                    return true
                }
            }
        )
        mScaleGestureDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScaleBegin(d: ScaleGestureDetector): Boolean {
                    Log.v("Ringdroid", "ScaleBegin " + d.currentSpanX)
                    mInitialScaleSpan = Math.abs(d.currentSpanX)
                    return true
                }

                override fun onScale(d: ScaleGestureDetector): Boolean {
                    val scale: Float = Math.abs(d.currentSpanX)
                    Log.v("Ringdroid", "Scale " + (scale - mInitialScaleSpan))
                    if (scale - mInitialScaleSpan > 40) {
                        mListener!!.waveformZoomIn()
                        mInitialScaleSpan = scale
                    }
                    if (scale - mInitialScaleSpan < -40) {
                        mListener!!.waveformZoomOut()
                        mInitialScaleSpan = scale
                    }
                    return true
                }

                override fun onScaleEnd(d: ScaleGestureDetector) {
                    Log.v("Ringdroid", "ScaleEnd " + d.currentSpanX)
                }
            }
        )
        mSoundFile = null
        mLenByZoomLevel = null
        mValuesByZoomLevel = null
        mHeightsAtThisZoomLevel = null
        mOffset = 0
        mPlaybackPos = -1
        mSelectionStart = 0
        mSelectionEnd = 0
        mDensity = 1.0f
        mInitialized = false
    }
}