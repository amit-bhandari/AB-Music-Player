package com.music.player.bhandari.m.customViews

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.FrameLayout
import com.music.player.bhandari.m.R

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
class FixedAspectRatioFrameLayout : FrameLayout {
    private var mAspectRatioWidth: Int = 0
    private var mAspectRatioHeight: Int = 0

    constructor(context: Context?) : super((context)!!) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context,
        attrs,
        defStyle) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val a: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.FixedAspectRatioFrameLayout)
        mAspectRatioWidth = a.getInt(R.styleable.FixedAspectRatioFrameLayout_aspectRatioWidth, 1)
        mAspectRatioHeight = a.getInt(R.styleable.FixedAspectRatioFrameLayout_aspectRatioHeight, 1)
        a.recycle()
    }

    // **overrides**
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val originalWidth: Int = MeasureSpec.getSize(widthMeasureSpec)
        val originalHeight: Int = MeasureSpec.getSize(heightMeasureSpec)
        val calculatedHeight: Int = originalWidth * mAspectRatioHeight / mAspectRatioWidth
        val finalWidth: Int
        val finalHeight: Int
        when {
            calculatedHeight > originalHeight -> {
                finalWidth = originalHeight * mAspectRatioWidth / mAspectRatioHeight
                finalHeight = originalHeight
            }
            else -> {
                finalWidth = originalWidth
                finalHeight = calculatedHeight
            }
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY))
    }
}