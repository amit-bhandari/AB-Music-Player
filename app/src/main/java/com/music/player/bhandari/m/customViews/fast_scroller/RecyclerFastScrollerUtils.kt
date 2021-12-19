package com.music.player.bhandari.m.customViews.fast_scroller

import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

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
internal object RecyclerFastScrollerUtils {
    fun setViewBackground(view: View?, background: Drawable?) {
        view!!.background = background
    }

    fun isRTL(context: Context): Boolean {
        val config: Configuration = context.resources.configuration
        return config.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }

    @ColorInt
    fun resolveColor(context: Context, @AttrRes color: Int): Int {
        val a: TypedArray = context.obtainStyledAttributes(intArrayOf(color))
        val resId: Int = a.getColor(0, 0)
        a.recycle()
        return resId
    }

    fun convertDpToPx(context: Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
    }
}