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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Net
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.annotations.Reflection
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.Companion.ERROR
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.Companion.NO_RESULT
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.Companion.POSITIVE_RESULT
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@Reflection
object MetalArchives {
    val domain: String = "metal-archives.com"
    @Reflection
    fun fromMetaData(artist: String, title: String): Lyrics {
        val baseURL: String =
            "http://www.metal-archives.com/search/ajax-advanced/searching/songs/?bandName=%s&songTitle=%s&releaseType[]=1&exactSongMatch=1&exactBandMatch=1"
        val urlArtist: String = artist.replace("\\s".toRegex(), "+")
        val urlTitle: String = title.replace("\\s".toRegex(), "+")
        val url: String
        val text: String
        try {
            val response: String = Net.getUrlAsString(String.format(baseURL, urlArtist, urlTitle))
            val jsonResponse: JsonObject = JsonParser().parse(response).asJsonObject
            val track: JsonArray = jsonResponse.getAsJsonArray("aaData").get(0).asJsonArray
            val builder: StringBuilder = StringBuilder()
            for (i in 0 until track.size()) builder.append(track.get(i).asString)
            val trackDocument: Document = Jsoup.parse(builder.toString())
            url = trackDocument.getElementsByTag("a").get(1).attr("href")
            val id: String =
                trackDocument.getElementsByClass("viewLyrics").get(0).id().substring(11)
            text = Jsoup.connect("http://www.metal-archives.com/release/ajax-view-lyrics/id/$id")
                .get().body().html()
        } catch (e: JsonParseException) {
            return Lyrics(NO_RESULT)
        } catch (e: IndexOutOfBoundsException) {
            return Lyrics(NO_RESULT)
        } catch (e: Exception) {
            return Lyrics(ERROR)
        }
        val lyrics: Lyrics = Lyrics(POSITIVE_RESULT)
        lyrics.setArtist(artist)
        lyrics.setTitle(title)
        lyrics.setText(text)
        lyrics.setSource(domain)
        lyrics.setURL(url)
        return lyrics
    }

    @Reflection
    fun fromURL(url: String?, artist: String?, title: String?): Lyrics {
        // TODO: support metal-archives URL
        return Lyrics(NO_RESULT)
    }
}