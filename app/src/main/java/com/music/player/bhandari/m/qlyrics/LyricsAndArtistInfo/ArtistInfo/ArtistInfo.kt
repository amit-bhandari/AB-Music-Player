package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
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
class ArtistInfo : Serializable, Parcelable {
    private var originalArtist: String? = ""
    private var mArtist: String? = ""
    private var artistContent: String? = ""
    private var imageUrl: String? = ""
    private var artistUrl: String? = ""
    private var flag: Int = NEGATIVE

    constructor(artist: String?) {
        originalArtist = artist
    }

    private constructor(`in`: Parcel) {
        originalArtist = `in`.readString()
        mArtist = `in`.readString()
        artistContent = `in`.readString()
        imageUrl = `in`.readString()
        artistUrl = `in`.readString()
        flag = `in`.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(originalArtist)
        dest.writeString(mArtist)
        dest.writeString(artistContent)
        dest.writeString(imageUrl)
        dest.writeString(artistUrl)
        dest.writeInt(flag)
    }

    fun getImageUrl(): String? {
        return imageUrl
    }

    fun setImageUrl(imageUrl: String?) {
        this.imageUrl = imageUrl
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getFlag(): Int {
        return flag
    }

    fun setFlag(flag: Int) {
        this.flag = flag
    }

    fun getArtistUrl(): String? {
        return artistUrl
    }

    fun setArtistUrl(artistUrl: String?) {
        this.artistUrl = artistUrl
    }

    interface Callback {
        fun onArtInfoDownloaded(artistInfo: ArtistInfo?)
    }

    fun setOriginalArtist(artist: String?) {
        originalArtist = artist
    }

    fun getOriginalArtist(): String? {
        return originalArtist
    }

    fun setCorrectedArtist(artist: String?) {
        mArtist = artist
    }

    fun getCorrectedArtist(): String? {
        return mArtist
    }

    fun setArtistContent(artistContent: String?) {
        this.artistContent = artistContent
    }

    fun getArtistContent(): String? {
        return artistContent
    }

    companion object {
        var POSITIVE: Int = 0
        var NEGATIVE: Int = 1
        @JvmField
        val CREATOR = object: Creator<ArtistInfo> {
            override fun createFromParcel(parcel: Parcel): ArtistInfo {
                return ArtistInfo(parcel)
            }

            override fun newArray(size: Int): Array<ArtistInfo?> {
                return arrayOfNulls(size)
            }
        }
    }
}