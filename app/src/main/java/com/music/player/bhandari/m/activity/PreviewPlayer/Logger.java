package com.music.player.bhandari.m.activity.PreviewPlayer;

import android.text.TextUtils;
import android.util.Log;

/**
 Copyright 2017 Amit Bhandari AB

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 <a href="http://www.apache.org/licenses/LICENSE-2.0">...</a>

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

class Logger {

    private static final String TAG = "AudioPreview";

    private static boolean isDebugging() {
        return Log.isLoggable(TAG, Log.DEBUG);
    }

    /**
     * Log a debug message
     *
     * @param tag {@link String}
     * @param msg {@link String }
     *
     */
    public static void logd(String tag, String msg) throws IllegalArgumentException {
        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("'tag' cannot be empty!");
        }
        if (TextUtils.isEmpty(msg)) {
            throw new IllegalArgumentException("'msg' cannot be empty!");
        }
        if (isDebugging()) {
            Log.d(TAG, tag + " [ " + msg + " ]");
        }
    }

    /**
     * Log a debug message
     *
     * @param tag {@link String}
     * @param msg {@link String }
     *
     */
    public static void loge(String tag, String msg) throws IllegalArgumentException {
        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("'tag' cannot be empty!");
        }
        if (TextUtils.isEmpty(msg)) {
            throw new IllegalArgumentException("'msg' cannot be empty!");
        }
        Log.e(TAG, tag + " [ " + msg + " ]");
    }

}