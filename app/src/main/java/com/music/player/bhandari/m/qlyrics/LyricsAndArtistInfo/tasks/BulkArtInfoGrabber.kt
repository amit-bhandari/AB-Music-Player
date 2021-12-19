package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks

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
//this thread is started from Music library once all artists are loaded
object BulkArtInfoGrabber : Thread() {
    //make sure only one instance of this thread runs at time
    private val artInfoGrabberThreadRunning: Boolean = false

    //makes sure artist info is downloaded one after another
    private val artistInfoThreadRunning: Boolean = false

    //if thread runs more than HALF HOUR, kill it
    private val THREAD_TIMEOUT: Long = (30 * 60 * 1000).toLong()
}