package com.music.player.bhandari.m.customViews

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

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
class RoundedImageView : AppCompatImageView {
    constructor(context: Context?) : super((context)!!)
    constructor(context: Context?, attrs: AttributeSet?) : super((context)!!, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        (context)!!, attrs, defStyle)

    override fun onDraw(canvas: Canvas) {
        val drawable: Drawable? = drawable
        if (drawable == null) {
            return
        }
        if (width == 0 || height == 0) {
            return
        }
        val b: Bitmap = (drawable.current as BitmapDrawable).bitmap
        val bitmap: Bitmap = b.copy(Bitmap.Config.ARGB_8888, true)
        val w: Int = width
        val h: Int = height
        val roundBitmap: Bitmap = getCroppedBitmap(bitmap, w)
        canvas.drawBitmap(roundBitmap, 0f, 0f, null)
    }

    companion object {
        fun getCroppedBitmap(bmp: Bitmap, radius: Int): Bitmap {
            val sbmp: Bitmap
            if (bmp.width != radius || bmp.height != radius) {
                val smallest: Float = Math.min(bmp.width, bmp.height).toFloat()
                val factor: Float = smallest / radius
                sbmp = Bitmap.createScaledBitmap(bmp,
                    (bmp.width / factor).toInt(),
                    (bmp.height / factor).toInt(), false)
            } else {
                sbmp = bmp
            }
            val output: Bitmap = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888)
            val canvas: Canvas = Canvas(output)
            val color: String = "#BAB399"
            val paint: Paint = Paint()
            val rect: Rect = Rect(0, 0, radius, radius)
            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            paint.isDither = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = Color.parseColor(color)
            canvas.drawCircle(radius / 2 + 0.7f, radius / 2 + 0.7f,
                radius / 2 + 0.1f, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(sbmp, rect, rect, paint)
            return output
        }
    }
}