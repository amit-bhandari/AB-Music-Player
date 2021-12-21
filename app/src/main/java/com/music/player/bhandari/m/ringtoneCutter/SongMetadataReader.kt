/*
 * Copyright (C) 2009 Google Inc.
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
package com.music.player.bhandari.m.ringtoneCutter

import android.app.Activity
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import java.util.*

class SongMetadataReader internal constructor(activity: Activity?, filename: String?) {
    var GENRES_URI: Uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
    var mActivity: Activity? = null
    var mFilename: String? = ""
    var mTitle: String? = ""
    var mArtist: String? = ""
    var mAlbum: String? = ""
    var mGenre: String = ""
    var mYear: Int = -1
    private fun ReadMetadata() {
        // Get a map from genre ids to names
        val genreIdMap: HashMap<String, String> = HashMap()
        var c: Cursor? = mActivity!!.contentResolver.query(
            GENRES_URI, arrayOf(
                MediaStore.Audio.Genres._ID,
                MediaStore.Audio.Genres.NAME),
            null, null, null)
        c!!.moveToFirst()
        while (!c.isAfterLast) {
            genreIdMap[c.getString(0)] = c.getString(1)
            c.moveToNext()
        }
        c.close()
        mGenre = ""
        for (genreId: String in genreIdMap.keys) {
            c = mActivity!!.contentResolver.query(
                makeGenreUri(genreId), arrayOf(MediaStore.Audio.Media.DATA),
                MediaStore.Audio.Media.DATA + " LIKE \"" + mFilename + "\"",
                null, null)
            if (c!!.count != 0) {
                mGenre = (genreIdMap.get(genreId))!!
                break
            }
            c.close()
        }
        val uri: Uri? = MediaStore.Audio.Media.getContentUriForPath((mFilename)!!)
        c = mActivity!!.contentResolver.query(
            (uri)!!, arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.DATA),
            MediaStore.Audio.Media.DATA + " LIKE \"" + mFilename + "\"",
            null, null)
        if (c!!.count == 0) {
            mTitle = getBasename(mFilename)
            mArtist = ""
            mAlbum = ""
            mYear = -1
            return
        }
        c.moveToFirst()
        mTitle = getStringFromColumn(c, MediaStore.Audio.Media.TITLE)
        if (mTitle == null || mTitle!!.isEmpty()) {
            mTitle = getBasename(mFilename)
        }
        mArtist = getStringFromColumn(c, MediaStore.Audio.Media.ARTIST)
        mAlbum = getStringFromColumn(c, MediaStore.Audio.Media.ALBUM)
        mYear = getIntegerFromColumn(c, MediaStore.Audio.Media.YEAR)
        c.close()
    }

    private fun makeGenreUri(genreId: String): Uri {
        val CONTENTDIR: String = MediaStore.Audio.Genres.Members.CONTENT_DIRECTORY
        return Uri.parse(
            StringBuilder()
                .append(GENRES_URI.toString())
                .append("/")
                .append(genreId)
                .append("/")
                .append(CONTENTDIR)
                .toString())
    }

    private fun getStringFromColumn(c: Cursor?, columnName: String): String? {
        val index: Int = c!!.getColumnIndexOrThrow(columnName)
        val value: String? = c.getString(index)
        if (value != null && value.isNotEmpty()) {
            return value
        } else {
            return null
        }
    }

    private fun getIntegerFromColumn(c: Cursor?, columnName: String): Int {
        val index: Int = c!!.getColumnIndexOrThrow(columnName)
        return c.getInt(index)
    }

    private fun getBasename(filename: String?): String {
        return filename!!.substring(filename.lastIndexOf('/') + 1,
            filename.lastIndexOf('.'))
    }

    init {
        mActivity = activity
        mFilename = filename
        mTitle = getBasename(filename)
        try {
            ReadMetadata()
        } catch (e: Exception) {
        }
    }
}