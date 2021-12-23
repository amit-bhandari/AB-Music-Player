/*
 * *
 *  * This file is part of QuickLyric
 *  * Created by geecko
 *  *
 *  * QuickLyric is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * QuickLyric is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  * You should have received a copy of the GNU General Public License
 *  * along with QuickLyric.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.io.*

class Lyrics : Serializable, Parcelable {
    private var mTitle: String? = null
    private var mArtist: String? = null
    private var mOriginalTitle: String? = null
    private var mOriginalArtist: String? = null
    private var mSourceUrl: String? = null
    private var mCoverURL: String? = null
    private var mLyrics: String? = null
    private var mSource: String? = null
    private var trackId: Int = -1
    private var mLRC: Boolean = false
    private val mFlag: Int
    fun getTrackId(): Int {
        return trackId
    }

    fun setTrackId(trackId: Int) {
        this.trackId = trackId
    }

    interface Callback {
        fun onLyricsDownloaded(lyrics: Lyrics?)
    }

    constructor(flag: Int) {
        mFlag = flag
    }

    protected constructor(`in`: Parcel) {
        mTitle = `in`.readString()
        mArtist = `in`.readString()
        mOriginalTitle = `in`.readString()
        mOriginalArtist = `in`.readString()
        mSourceUrl = `in`.readString()
        mCoverURL = `in`.readString()
        mLyrics = `in`.readString()
        mSource = `in`.readString()
        mLRC = `in`.readByte().toInt() != 0
        mFlag = `in`.readInt()
        trackId = `in`.readInt()
    }

    fun getTrack(): String? {
        return mTitle
    }

    fun setTitle(title: String?) {
        mTitle = title
    }

    fun getOriginalTrack(): String? {
        if (mOriginalTitle != null) return mOriginalTitle else return mTitle
    }

    fun setOriginalTitle(originalTitle: String?) {
        mOriginalTitle = originalTitle
    }

    fun getArtist(): String? {
        return mArtist
    }

    fun setArtist(artist: String?) {
        mArtist = artist
    }

    fun getOriginalArtist(): String? {
        return if (mOriginalArtist != null) mOriginalArtist else mArtist
    }

    fun setOriginalArtist(originalArtist: String?) {
        mOriginalArtist = originalArtist
    }

    fun getURL(): String? {
        return mSourceUrl
    }

    fun setURL(uRL: String?) {
        mSourceUrl = uRL
    }

    fun getCoverURL(): String? {
        return mCoverURL
    }

    fun setCoverURL(coverURL: String?) {
        mCoverURL = coverURL
    }

    fun getText(): String? {
        return mLyrics
    }

    fun setText(lyrics: String?) {
        mLyrics = lyrics
    }

    fun getSource(): String? {
        return mSource
    }

    fun getFlag(): Int {
        return mFlag
    }

    fun setSource(mSource: String?) {
        this.mSource = mSource
    }

    fun setLRC(LRC: Boolean) {
        mLRC = LRC
    }

    fun isLRC(): Boolean {
        return mLRC
    }

    @Throws(IOException::class)
    fun toBytes(): ByteArray {
        val bos: ByteArrayOutputStream = ByteArrayOutputStream()
        try {
            val out: ObjectOutput = ObjectOutputStream(bos)
            out.writeObject(this)
            out.close()
        } finally {
            bos.close()
        }
        return bos.toByteArray()
    }

    override fun equals(`object`: Any?): Boolean {
        val isLyrics: Boolean = `object` is Lyrics
        if (isLyrics && (getURL() != null) && ((`object` as Lyrics?)!!.getURL() != null)) return (getURL() == `object`!!.getURL()) else if (isLyrics) {
            val other: Lyrics? = `object` as Lyrics?
            var result: Boolean = (getText() == other!!.getText())
            result = result and (getFlag() == other.getFlag())
            result = result and (getSource() == other.getSource())
            result = result and (getArtist() == other.getArtist())
            result = result and (getTrack() == other.getTrack())
            return result
        } else return false
    }

    override fun hashCode(): Int {
        // Potential issue with the Birthday Paradox when we hash over 50k lyrics
        return if (getURL() != null) getURL().hashCode() else ("" + getOriginalArtist() + getOriginalTrack() + getSource()).hashCode()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(mTitle)
        dest.writeString(mArtist)
        dest.writeString(mOriginalTitle)
        dest.writeString(mOriginalArtist)
        dest.writeString(mSourceUrl)
        dest.writeString(mCoverURL)
        dest.writeString(mLyrics)
        dest.writeString(mSource)
        dest.writeByte((if (mLRC) 1 else 0).toByte())
        dest.writeInt(mFlag)
        dest.writeInt(trackId)
    }

    companion object {
        val NO_RESULT: Int = -2
        val NEGATIVE_RESULT: Int = -1
        val POSITIVE_RESULT: Int = 1
        val ERROR: Int = -3
        val SEARCH_ITEM: Int = 2
        @JvmField
        val CREATOR = object : Creator<Lyrics?> {
            override fun createFromParcel(`in`: Parcel): Lyrics {
                return Lyrics(`in`)
            }
            override fun newArray(size: Int): Array<Lyrics?> {
                return arrayOfNulls(size)
            }
        }

        @Throws(IOException::class, ClassNotFoundException::class)
        fun fromBytes(data: ByteArray?): Lyrics? {
            if (data == null) return null
            val `in`: ByteArrayInputStream = ByteArrayInputStream(data)
            val `is`: ObjectInputStream = ObjectInputStream(`in`)
            return `is`.readObject() as Lyrics?
        }
    }
}