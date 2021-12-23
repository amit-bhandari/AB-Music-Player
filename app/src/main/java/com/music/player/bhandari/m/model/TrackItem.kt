package com.music.player.bhandari.m.model

import android.media.MediaMetadataRetriever
import android.util.Log
import java.io.Serializable

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
class TrackItem : Serializable {
    var id = 0
    private var filePath = ""
    var title: String? = ""
    var artist: String? = ""
    var album: String? = ""
    var genre = ""
        private set
    var duration: String? = "" //string in milliseconds
    var albumId = 0
        private set
    var artist_id = 0

    //default constructor
    constructor()
    constructor(
        filePath: String,
        title: String?,
        artist: String?,
        album: String?,
        genre: String,
        duration: String?,
        album_id: Int,
        artist_id: Int,
        id: Int
    ) {
        this.filePath = filePath
        this.title = title
        this.artist = artist
        this.album = album
        this.genre = genre
        this.duration = duration
        albumId = album_id
        this.id = id
        this.artist_id = artist_id
    }

    //if needed in case
    constructor(filePath: String) {
        val mmr = MediaMetadataRetriever()
        Log.e("filepath", filePath)
        mmr.setDataSource(filePath)
        val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
        this.duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        this.filePath = filePath
    }//send predefined number 300 msec

    //return in ms
    val durInt: Int
        get() {
            var durationMs = 0
            durationMs = try {
                duration!!.toInt()
            } catch (e: NumberFormatException) {
                //send predefined number 300 msec
                3000
            }
            return durationMs
        }
    val durStr: String
        get() {
            val minutes = duration!!.toInt() / 1000 / 60
            val seconds = duration!!.toInt() / 1000 % 60
            val durFormatted = String.format("%02d", seconds)
            return "$minutes:$durFormatted"
        }

    fun getFilePath(): String {
        return filePath
    }

    fun setFilePath(title: String?) {
        filePath = filePath
    }

    @JvmName("getArtist1")
    fun getArtist(): String? {
        return artist
    }

    @JvmName("setArtist1")
    fun setArtist(artist: String?) {
        if (artist == null || artist.isEmpty()) {
            this.artist = "<unknown>"
            return
        }
        this.artist = artist
    }

    fun haveAlbumArt(): Boolean {
        return MusicLibrary.instance.getAlbumArtUri(albumId) != null
    }
}