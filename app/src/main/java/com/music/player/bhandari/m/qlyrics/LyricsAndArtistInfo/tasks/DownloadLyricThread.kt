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
package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks

import android.os.*
import android.util.Log
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.*
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics

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
class DownloadLyricThread constructor(
    callback: Lyrics.Callback?,
    private val positionAvailable: Boolean,
    item: TrackItem?,
    vararg params: String
) : Thread() {

    private var callback: Lyrics.Callback?
    private val item: TrackItem?
    private val params: Array<String>

    fun setCallback(callback: Lyrics.Callback?) {
        this.callback = callback
    }

    private fun download(url: String?, artist: String?, title: String?): Lyrics {
        var lyrics = Lyrics(Lyrics.NO_RESULT)
        for (provider: String? in providers) {
            when (provider) {
                "AZLyrics" -> lyrics = AZLyrics.fromURL(url, artist, title)
                "Bollywood" -> lyrics = Bollywood.fromURL(url, artist, title)
                "Genius" -> lyrics = Genius.fromURL(url, artist, title)
                "JLyric" -> lyrics = JLyric.fromURL(url, artist, title)
                "Lololyrics" -> lyrics = Lololyrics.fromURL(url, artist, title)
                "LyricsMania" -> lyrics = LyricsMania.fromURL(url, artist, title)
                "LyricWiki" -> lyrics = LyricWiki.fromURL(url!!, artist, title)
                "MetalArchives" -> lyrics = MetalArchives.fromURL(url, artist, title)
                "PLyrics" -> lyrics = PLyrics.fromURL(url, artist, title)
                "UrbanLyrics" -> lyrics = UrbanLyrics.fromURL(url, artist, title)
                "ViewLyrics" -> lyrics = ViewLyrics.fromURL(url, artist, title)
            }
            if (lyrics.isLRC() && !positionAvailable) continue
            if (lyrics.getFlag() === Lyrics.POSITIVE_RESULT) return lyrics
        }
        return Lyrics(Lyrics.NO_RESULT)
    }

    private fun download(artist: String, title: String?): Lyrics {
        Log.v("DOWNLOAD $artist", Log.getStackTraceString(Exception()))
        var lyrics = Lyrics(Lyrics.NO_RESULT)
        for (provider: String? in providers) {
            when (provider) {
                "AZLyrics" -> lyrics = AZLyrics.fromMetaData(artist, (title)!!)
                "Bollywood" -> lyrics = Bollywood.fromMetaData(artist, title!!)
                "Genius" -> lyrics = Genius.fromMetaData(artist, title)
                "JLyric" -> lyrics = JLyric.fromMetaData(artist, title)
                "Lololyrics" -> lyrics = Lololyrics.fromMetaData(artist, title)
                "LyricsMania" -> lyrics = LyricsMania.fromMetaData(artist, title!!)
                "LyricWiki" -> lyrics = LyricWiki.fromMetaData(artist, title)
                "MetalArchives" -> lyrics = MetalArchives.fromMetaData(artist, title!!)
                "PLyrics" -> lyrics = PLyrics.fromMetaData(artist, title!!)
                "UrbanLyrics" -> lyrics = UrbanLyrics.fromMetaData(artist, title!!)
                "ViewLyrics" -> try {
                    lyrics = ViewLyrics.fromMetaData(artist, title)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (lyrics.isLRC() && !positionAvailable) continue
            if (lyrics.getFlag() === Lyrics.POSITIVE_RESULT) return lyrics
        }
        return lyrics
    }

    private fun threadMsg(lyrics: Lyrics?) {
        if (lyrics != null) {

            //put lyrics in db
            if (item != null && lyrics.getFlag() === Lyrics.POSITIVE_RESULT) {
                Log.v(Constants.L_TAG, lyrics.getOriginalArtist()!!)
                // Log.v(Constants.L_TAG,lyrics.getSource()+" ");
                Log.v(Constants.L_TAG, lyrics.isLRC().toString() + " lrc")
                // Log.v(Constants.L_TAG,lyrics.getText() );
                OfflineStorageLyrics.putLyricsInDB(lyrics, item)
            }
            val msgObj: Message = handler.obtainMessage()
            val b = Bundle()
            b.putSerializable("lyrics", lyrics)
            msgObj.data = b
            handler.sendMessage(msgObj)
        }
    }

    // Define the Handler that receives messages from the thread and update the progress
    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val result: Lyrics? = msg.data.getSerializable("lyrics") as Lyrics?
            if (result != null) {
                callback?.onLyricsDownloaded(result)
            }
        }
    }

    override fun run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        var lyrics: Lyrics
        var artist: String? = null
        var title: String? = null
        var url: String? = null
        when (params.size) {
            3 -> {
                artist = params[1]
                title = params[2]
                url = params[0]
                lyrics = download(url, artist, title)
            }
            1 -> {
                url = params[0]
                lyrics = download(url, artist, title)
            }
            else -> {
                artist = params[0]
                title = params[1]
                lyrics = download(params[0], params[1])
            }
        }
        if (lyrics.getFlag() !== Lyrics.POSITIVE_RESULT) {
            val correction: Array<String> = correctTags(artist, title)
            if (!((correction[0] == artist) && (correction[1] == title)) || url != null) {
                lyrics = download(correction[0], correction[1])
                lyrics.setOriginalArtist(artist)
                lyrics.setOriginalTitle(title)
            }
        }
        if (lyrics.getArtist() == null) {
            when {
                artist != null -> {
                    lyrics.setArtist(artist)
                    lyrics.setTitle(title)
                }
                else -> {
                    lyrics.setArtist("")
                    lyrics.setTitle("")
                }
            }
        }
        if (item != null) {
            lyrics.setTrackId(item.id)
        }
        threadMsg(lyrics)
    }

    companion object {
        private val mainProviders: Array<String> = arrayOf(
            "ViewLyrics",
            "LyricWiki",
            "Genius",
            "LyricsMania",
            "AZLyrics",
            "Bollywood"
        )
        private val providers: ArrayList<String> = ArrayList(listOf(*mainProviders))

        private fun correctTags(artist: String?, title: String?): Array<String> {
            if (artist == null || title == null) return arrayOf("", "")
            var correctedArtist: String = artist.replace("\\(.*\\)".toRegex(), "").replace(" \\- .*".toRegex(), "").trim { it <= ' ' }
            val correctedTrack: String = title.replace("\\(.*\\)".toRegex(), "")
                .replace("\\[.*\\]".toRegex(), "").replace(" \\- .*".toRegex(), "")
                .trim { it <= ' ' }
            val separatedArtists: Array<String> = correctedArtist.split(", ".toRegex()).toTypedArray()
            correctedArtist = separatedArtists[separatedArtists.size - 1]
            return arrayOf(correctedArtist, correctedTrack)
        }
    }

    init {
        this.callback = callback
        this.item = item
        this.params = params as Array<String>
    }
}