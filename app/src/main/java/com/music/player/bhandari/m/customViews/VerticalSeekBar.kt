/*
 * Copyright (C) Hackskrieg
 *
 * http://hackskrieg.wordpress.com/2012/04/20/working-vertical-seekbar-for-android/
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
package com.music.player.bhandari.m.customViews

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar

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
//Creates a Vertical SeekBar using Android's basic UI elements.
class VerticalSeekBar : AppCompatSeekBar {
    constructor(context: Context?) : super((context)!!)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        (context)!!, attrs, defStyle)

    constructor(context: Context?, attrs: AttributeSet?) : super((context)!!, attrs)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(c: Canvas) {
        c.rotate(-90f)
        c.translate(-height.toFloat(), 0f)
        super.onDraw(c)
    }

    private var onChangeListener: OnSeekBarChangeListener? = null
    override fun setOnSeekBarChangeListener(onChangeListener: OnSeekBarChangeListener) {
        this.onChangeListener = onChangeListener
    }

    private var lastProgress: Int = 0
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onChangeListener!!.onStartTrackingTouch(this)
                isPressed = true
                isSelected = true
            }
            MotionEvent.ACTION_MOVE -> {
                super.onTouchEvent(event)
                var progress: Int = max - (max * event.y / height).toInt()

                // Ensure progress stays within boundaries
                if (progress < 0) {
                    progress = 0
                }
                if (progress > max) {
                    progress = max
                }
                setProgress(progress) // Draw progress
                if (progress != lastProgress) {
                    // Only enact listener if the progress has actually changed
                    lastProgress = progress
                    onChangeListener!!.onProgressChanged(this, progress, true)
                }
                onSizeChanged(width, height, 0, 0)
                isPressed = true
                isSelected = true
            }
            MotionEvent.ACTION_UP -> {
                onChangeListener!!.onStopTrackingTouch(this)
                isPressed = false
                isSelected = false
            }
            MotionEvent.ACTION_CANCEL -> {
                super.onTouchEvent(event)
                isPressed = false
                isSelected = false
            }
        }
        return true
    }

    @Synchronized
    fun setProgressAndThumb(progress: Int) {
        setProgress(progress)
        onSizeChanged(width, height, 0, 0)
        if (progress != lastProgress) {
            // Only enact listener if the progress has actually changed
            lastProgress = progress
            onChangeListener!!.onProgressChanged(this, progress, true)
        }
    }

    @Synchronized
    fun setMaximum(maximum: Int) {
        max = maximum
    }

    @Synchronized
    fun getMaximum(): Int {
        return max
    }
}