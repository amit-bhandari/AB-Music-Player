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

import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Net
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.annotations.Reflection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

object AZLyrics {
    @Reflection
    val domain: String = "www.azlyrics.com/"
    @Reflection
    fun fromMetaData(artist: String, song: String): Lyrics {
        var htmlArtist: String = artist.replace("[\\s'\"-]".toRegex(), "")
            .replace("&".toRegex(), "and").replace("[^A-Za-z0-9]".toRegex(), "")
        val htmlSong: String = song.replace("[\\s'\"-]".toRegex(), "")
            .replace("&".toRegex(), "and").replace("[^A-Za-z0-9]".toRegex(), "")
        if (htmlArtist.toLowerCase(Locale.getDefault()).startsWith("the")) htmlArtist =
            htmlArtist.substring(3)
        val urlString: String = String.format(
            "http://www.azlyrics.com/lyrics/%s/%s.html",
            htmlArtist.toLowerCase(Locale.getDefault()),
            htmlSong.toLowerCase(Locale.getDefault()))
        return fromURL(urlString, artist, song)
    }

    fun fromURL(url: String?, artist: String?, song: String?): Lyrics {
        var artist: String? = artist
        var song: String? = song
        val html: String
        try {
            val document: Document = Jsoup.connect(url).userAgent(Net.USER_AGENT).get()
            if (document.location().contains("azlyrics")) html =
                document.html() else throw IOException("Redirected to wrong domain " + document.location())
        } catch (e: HttpStatusException) {
            return Lyrics(Lyrics.NO_RESULT)
        } catch (e: IOException) {
            e.printStackTrace()
            return Lyrics(Lyrics.ERROR)
        }
        val p: Pattern = Pattern.compile(
            "Sorry about that. -->(.*)",
            Pattern.DOTALL)
        val matcher: Matcher = p.matcher(html)
        if (artist == null || song == null) {
            val metaPattern: Pattern = Pattern.compile(
                "ArtistName = \"(.*)\";\r\nSongName = \"(.*)\";\r\n",
                Pattern.DOTALL)
            val metaMatcher: Matcher = metaPattern.matcher(html)
            if (metaMatcher.find()) {
                artist = metaMatcher.group(1)
                song = metaMatcher.group(2)
                song = song.substring(0, song.indexOf('"'))
            } else {
                song = ""
                artist = song
            }
        }
        if (matcher.find()) {
            val l: Lyrics = Lyrics(Lyrics.POSITIVE_RESULT)
            l.setArtist(artist)
            var text: String = matcher.group(1)
            text = text.substring(0, text.indexOf("</div>"))
            text = text.replace("\\[[^\\[]*\\]".toRegex(), "")
            l.setText(text)
            l.setTitle(song)
            l.setURL(url)
            l.setSource("AZLyrics")
            return l
        } else return Lyrics(Lyrics.NEGATIVE_RESULT)
    }
}