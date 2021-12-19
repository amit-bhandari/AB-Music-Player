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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import java.net.URLEncoder

@Reflection
object JLyric {
    val domain: String = "j-lyric.net"
    private val baseUrl: String =
        "http://search.j-lyric.net/index.php?ct=0&ca=0&kl=&cl=0&ka=%1s&kt=%1s"

    fun search(query: String?): ArrayList<Lyrics> {
        val results: ArrayList<Lyrics> = ArrayList<Lyrics>()
        var artistBlocks: Elements? = null
        try {
            val searchPage: Document =
                Jsoup.connect(String.format("http://search.j-lyric.net/index.php?ct=0&ka=&ca=0&kl=&cl=0&kt=%s",
                    URLEncoder.encode(query, "UTF-8")))
                    .userAgent(Net.USER_AGENT).get()
            artistBlocks = searchPage.body().select("div#lyricList .body")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (artistBlocks?.first() == null) return results
        for (result: Element in artistBlocks) {
            val l = Lyrics(Lyrics.SEARCH_ITEM)
            val title: String = result.select("div.title > a").text()
            val artist: String = result.select("div.status > a").text()
            val url: String = result.select("div.title > a").attr("href")
            l.setTitle(title)
            l.setArtist(artist)
            l.setURL(url)
            l.setSource("J-Lyric")
            results.add(l)
        }
        return results
    }

    @Reflection
    fun fromMetaData(artist: String?, song: String?): Lyrics {
        if ((artist == null) || (song == null)) return Lyrics(Lyrics.ERROR)
        val encodedArtist: String
        val encodedSong: String
        val url: String
        try {
            encodedArtist = URLEncoder.encode(artist, "UTF-8")
            encodedSong = URLEncoder.encode(song, "UTF-8")
            val searchPage: Document =
                Jsoup.connect(String.format(baseUrl, encodedArtist, encodedSong))
                    .userAgent(Net.USER_AGENT).get()
            if (!searchPage.location()
                    .startsWith("http://search.j-lyric.net/")
            ) throw IOException("Redirected to wrong domain " + searchPage.location())
            val artistBlocks: Elements = searchPage.body().select("div#lyricList")

            //@todo give all results
            if (artistBlocks.first() == null) {
                val lyrics: Lyrics = Lyrics(Lyrics.NO_RESULT)
                lyrics.setArtist(artist)
                lyrics.setTitle(song)
                return lyrics
            }
            url = artistBlocks.first()!!.select("div.title a").attr("href")
        } catch (e: IOException) {
            e.printStackTrace()
            return Lyrics(Lyrics.ERROR)
        }
        return fromURL(url, artist, song)
    }

    fun fromURL(url: String?, artist: String?, song: String?): Lyrics {
        var artist: String? = artist
        var song: String? = song
        val lyrics: Lyrics
        var text: String? = null
        try {
            val lyricsPage: Document = Jsoup.connect(url).userAgent(Net.USER_AGENT).get()
            if (!lyricsPage.location()
                    .contains(domain)
            ) throw IOException("Redirected to wrong domain " + lyricsPage.location())
            text = lyricsPage.select("p#lyricBody").html()
            if (artist == null) artist = lyricsPage.select("div.body")
                .get(0).child(0).child(0).child(0).child(0).child(0).text()
            if (song == null) song = lyricsPage.select("div.caption").get(0).child(0).text()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        lyrics = if (text == null) Lyrics(Lyrics.ERROR) else Lyrics(Lyrics.POSITIVE_RESULT)
        lyrics.setArtist(artist)
        lyrics.setTitle(song)
        lyrics.setText(text)
        lyrics.setSource("J-Lyric")
        lyrics.setURL(url)
        return lyrics
    }
}