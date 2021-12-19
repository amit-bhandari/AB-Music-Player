/*
 * Copyright 2015 Google Inc.
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
package com.music.player.bhandari.m.transition

import androidx.annotation.ColorInt
import android.graphics.drawable.Drawable
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.ColorFilter
import androidx.annotation.RequiresApi
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.util.Property
import androidx.core.content.ContextCompat
import com.music.player.bhandari.m.R

/**
 * 形态和颜色可以发生变化的Drawable，形态变化是通过cornerRadius来实现的，颜色变化是通过paint的color来实现的
 * 该类在Drawable的基础上添加了cornerRadius和color两个属性，前者是float类型，后者是int类型
 *
 *
 * A drawable that can morph size, shape (via it's corner radius) and color.  Specifically this is
 * useful for animating between a FAB and a dialog.
 */
class MorphDrawable(@ColorInt color: Int, private var cornerRadius: Float) : Drawable() {
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun getCornerRadius(): Float {
        return cornerRadius
    }

    fun setCornerRadius(cornerRadius: Float) {
        this.cornerRadius = cornerRadius
        invalidateSelf()
    }

    var color: Int
        get() = paint.color
        set(color) {
            paint.color = color
            invalidateSelf()
        }

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.right.toFloat(),
            bounds.bottom.toFloat(),
            cornerRadius,
            cornerRadius,
            paint) //hujiawei
    }

    override fun getOutline(outline: Outline) {
        outline.setRoundRect(bounds, cornerRadius)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(cf: ColorFilter?) {
        paint.colorFilter = cf
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return paint.alpha
    }

    companion object {
        val CORNER_RADIUS: Property<MorphDrawable, Float> = object : Property<MorphDrawable, Float>(
            Float::class.java, "cornerRadius") {
            override fun set(morphDrawable: MorphDrawable, value: Float) {
                morphDrawable.setCornerRadius(value)
            }

            override fun get(morphDrawable: MorphDrawable): Float {
                return morphDrawable.getCornerRadius()
            }
        }
        val COLOR: Property<MorphDrawable, Int> = object : Property<MorphDrawable, Int>(
            Int::class.java, "color") {
            override fun set(morphDrawable: MorphDrawable, value: Int) {
                morphDrawable.color = value
            }

            override fun get(morphDrawable: MorphDrawable): Int {
                return morphDrawable.color
            }
        }
    }

    init {
        paint.color = color
    }
}