package com.music.player.bhandari.m.activity.PreviewPlayer

import android.text.TextUtils
import android.util.Log

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
internal object Logger {
    private val TAG: String = "AudioPreview"
    private fun isDebugging(): Boolean {
        return Log.isLoggable(TAG, Log.DEBUG)
    }

    /**
     * Log a debug message
     *
     * @param tag [String]
     * @param msg [String]
     *
     * @throws IllegalArgumentException [IllegalArgumentException]
     */
    @Throws(IllegalArgumentException::class)
    fun logd(tag: String, msg: String) {
        if (TextUtils.isEmpty(tag)) {
            throw IllegalArgumentException("'tag' cannot be empty!")
        }
        if (TextUtils.isEmpty(msg)) {
            throw IllegalArgumentException("'msg' cannot be empty!")
        }
        if (isDebugging()) {
            Log.d(TAG, "$tag [ $msg ]")
        }
    }

    /**
     * Log a debug message
     *
     * @param tag [String]
     * @param msg [String]
     *
     * @throws IllegalArgumentException [IllegalArgumentException]
     */
    @Throws(IllegalArgumentException::class)
    fun loge(tag: String, msg: String?) {
        if (TextUtils.isEmpty(tag)) {
            throw IllegalArgumentException("'tag' cannot be empty!")
        }
        if (TextUtils.isEmpty(msg)) {
            throw IllegalArgumentException("'msg' cannot be empty!")
        }
        Log.e(TAG, tag + " [ " + msg + " ]")
    }
}