package com.music.player.bhandari.m.customViews

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import kotlin.math.max
import kotlin.math.min

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
class ZoomTextView : androidx.appcompat.widget.AppCompatTextView {
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mScaleFactor: Float = 1f
    private var defaultSize: Float = 0f
    private var zoomLimit: Float = 3.0f

    constructor(context: Context?) : super(context!!) {
        initialize()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        initialize()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!,
        attrs,
        defStyleAttr) {
        initialize()
    }

    private fun initialize() {
        defaultSize = textSize
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
    }

    /***
     * @param zoomLimit
     * Default value is 3, 3 means text can zoom 3 times the default size
     */
    fun setZoomLimit(zoomLimit: Float) {
        this.zoomLimit = zoomLimit
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        super.onTouchEvent(ev)
        mScaleDetector!!.onTouchEvent(ev)
        return true
    }

    /*Scale Gesture listener class,
    mScaleFactor is getting the scaling value
    and mScaleFactor is mapped between 1.0 and and zoomLimit
    that is 3.0 by default. You can also change it. 3.0 means text
    can zoom to 3 times the default value.*/
    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor
            mScaleFactor = max(1.0f, min(mScaleFactor, zoomLimit))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultSize * mScaleFactor)
            Log.e(TAG, mScaleFactor.toString())
            return true
        }
    }

    companion object {
        private val TAG: String = "ZoomTextView"
    }
}