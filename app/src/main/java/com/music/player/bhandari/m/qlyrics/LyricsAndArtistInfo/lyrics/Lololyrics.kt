/*
 * *
 *  * This file is part of QuickLyric
 *  * Created by maximko, edited by geecko
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
//import android.util.Log;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.annotations.Reflection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.io.IOException
import java.net.URLEncoder

@Reflection
object Lololyrics {
    private val baseUrl: String =
        "http://api.lololyrics.com/0.5/getLyric?artist=%1s&track=%1s&rawutf8=1"
    val domain: String = "www.lololyrics.com/"
    @Reflection
    fun fromMetaData(artist: String?, song: String?): Lyrics {
        if ((artist == null) || (song == null)) return Lyrics(Lyrics.ERROR)
        try {
            val encodedArtist: String = URLEncoder.encode(artist, "UTF-8")
            val encodedSong: String = URLEncoder.encode(song, "UTF-8")
            val url: String = String.format(baseUrl, encodedArtist, encodedSong)
            val body: String = Jsoup.connect(url).execute().body()
            val lololyrics: Document = Jsoup.parse(body.replace("(\\n)".toRegex(), "<br />"))
            val loloResult: Element? = lololyrics.select("result").first()
            if ((loloResult == null) || (loloResult.select("status") == null
                        ) || !("OK" == loloResult.select("status").text())
            ) return Lyrics(Lyrics.NO_RESULT)
            if (loloResult.select("response").hasText()) {
                val lyrics: Lyrics = Lyrics(Lyrics.POSITIVE_RESULT)
                lyrics.setArtist(artist)
                lyrics.setTitle(song)
                val text: String =
                    Parser.unescapeEntities(loloResult.select("response").html(), true)
                lyrics.setText(text)
                lyrics.setSource(domain)
                if (loloResult.select("cover")
                        .hasText()
                ) lyrics.setCoverURL(loloResult.select("cover").text())
                val weburl: String = loloResult.select("url").html()
                lyrics.setURL(weburl)
                return lyrics
            } else {
                return Lyrics(Lyrics.NO_RESULT)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return Lyrics(Lyrics.ERROR)
        }
    }

    // TODO handle lololyrics.com url
    fun fromURL(url: String?, artist: String?, song: String?): Lyrics {
        /** We can't transform generic lololyrics url to API url.
         * Also we can't get artist name and song title from Lololyrics API.  */
        return Lyrics(Lyrics.NO_RESULT)
    }
}