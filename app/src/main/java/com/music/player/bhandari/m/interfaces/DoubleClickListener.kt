package com.music.player.bhandari.m.interfaces

import android.view.View

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
abstract class DoubleClickListener constructor() : View.OnClickListener {
    private var lastClickTime: Long = 0
    public override fun onClick(v: View) {
        val clickTime: Long = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
        } else {
            onSingleClick(v)
        }
        lastClickTime = clickTime
    }

    abstract fun onSingleClick(v: View?)
    abstract fun onDoubleClick(v: View?)

    companion object {
        private val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
    }
}