package com.music.player.bhandari.m.model

import com.music.player.bhandari.m.utils.UtilityFun.escapeDoubleQuotes
import java.lang.NumberFormatException
import android.media.MediaMetadataRetriever
import android.util.Log
import android.content.ContentResolver
import java.util.concurrent.atomic.AtomicInteger
import java.util.ArrayList
import java.util.Collections
import java.util.LinkedHashMap
import android.util.SparseArray
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import java.lang.Runnable
import java.lang.InterruptedException
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.Intent
import java.lang.Exception
import android.net.Uri
import android.provider.MediaStore
import android.database.Cursor
import com.music.player.bhandari.m.utils.UtilityFun
import android.graphics.Bitmap
import android.content.ContentUris
import android.os.ParcelFileDescriptor
import java.io.FileDescriptor
import android.graphics.BitmapFactory
import java.io.File
import androidx.annotation.RequiresApi
import android.os.Build
import java.io.IOException
import kotlin.jvm.Synchronized
import java.util.HashMap
import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues
import android.os.Looper
import android.widget.Toast
import android.annotation.SuppressLint
import android.view.Gravity
import java.util.ConcurrentModificationException
import java.util.Comparator
import android.database.DatabaseUtils
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet

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
class dataItem {
    constructor(
        id: Int,
        title: String?,
        artist_id: Int,
        artist_name: String?,
        album_id: Int,
        albumName: String?,
        year: String?,
        file_path: String?,
        duration: String,
        trackNumber: Int
    ) {
        if (title != null) {
            this.title = title
        }
        this.id = id
        this.album_id = album_id
        if (albumName != null) {
            this.albumName = albumName
        }
        this.artist_id = artist_id
        if (artist_name != null) {
            this.artist_name = artist_name
        }
        this.file_path = file_path
        if (year != null) this.year = year

        //Log.v("Year", title + " : " + year);
        this.duration = duration
        if (duration != "") {
            durStr = getDurStr()
        }
        this.trackNumber = trackNumber
    }

    constructor(id: Int, title: String?, numberOfTracks: Int, numberOfAlbums: Int) {
        artist_id = id
        if (title != null) {
            this.title = title
            artist_name = title
        }
        this.numberOfTracks = numberOfTracks
        this.numberOfAlbums = numberOfAlbums
    }

    constructor(
        id: Int,
        title: String?,
        artist_name: String,
        numberOfTracks: Int,
        year: String?,
        artist_id: Int
    ) {
        this.artist_name = artist_name
        this.artist_id = artist_id
        if (title != null) {
            this.title = title
            albumName = title
        }
        album_id = id

        // Log.v("Year", title + " : " + year);
        if (year != null) this.year = year
        this.numberOfTracks = numberOfTracks
    }

    constructor(genre_id: Int, genre_name: String?, numberOfTracks: Int) {
        id = genre_id
        if (genre_name != null) {
            title = genre_name
        }
        this.numberOfTracks = numberOfTracks
    }

    var id = 0
    var title = ""
    var artist_id = 0
    var artist_name = ""
    var album_id = 0
    var albumName = ""
    var year = "zzzz"
    var numberOfTracks = 0
    var numberOfAlbums = 0
    var file_path: String? = null
    var duration: String? = null
    var durStr: String? = null
    var trackNumber = 0
    private fun getDurStr(): String {
        var minutes = 0
        var seconds = 0
        try {
            minutes = duration!!.toInt() / 1000 / 60
            seconds = duration!!.toInt() / 1000 % 60
        } catch (ignored: NumberFormatException) {
        }
        val durFormatted = String.format("%02d", seconds)
        return "$minutes:$durFormatted"
    }
}