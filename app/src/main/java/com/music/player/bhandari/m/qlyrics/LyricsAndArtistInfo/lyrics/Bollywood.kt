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
import java.io.IOException
import java.net.URLEncoder

@Reflection
object Bollywood {
    @Reflection
    val domain: String = "api.quicklyric.be/bollywood/"
    fun search(query: String?): ArrayList<Lyrics> {
        val results: ArrayList<Lyrics> = ArrayList<Lyrics>()
        val searchUrl: String = "https://api.quicklyric.be/bollywood/search?q=%s"
        try {
            val jsonText: String =
                Net.getUrlAsString(String.format(searchUrl, URLEncoder.encode(query, "utf-8")))
            val jsonResponse: JsonObject = JsonParser().parse(jsonText).asJsonObject
            val lyricsResults: JsonArray? = jsonResponse.getAsJsonArray("lyrics")
            if (lyricsResults != null) for (i in 0 until lyricsResults.size()) {
                val lyricsResult: JsonObject = lyricsResults.get(i).asJsonObject
                val tags: JsonArray = lyricsResult.get("tags").asJsonArray
                val lyrics = Lyrics(Lyrics.SEARCH_ITEM)
                lyrics.setTitle(lyricsResult.get("name").asString)
                for (j in 0 until tags.size()) {
                    val tag: JsonObject = tags.get(j).asJsonObject
                    if ((tag.get("tag_type").asString == "Singer")) {
                        lyrics.setArtist(tag.get("name").asString.trim { it <= ' ' })
                        break
                    }
                }
                lyrics.setURL("https://api.quicklyric.be/bollywood/get?id=" + lyricsResult.get("id")
                    .asInt)
                results.add(lyrics)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JsonParseException) {
            e.printStackTrace()
        } catch (e: Exception) {
        }
        return results
    }

    @Reflection
    fun fromMetaData(artist: String, title: String): Lyrics {
        val searchResults: ArrayList<Lyrics> = search("$artist $title")
        for (result: Lyrics in searchResults) {
            if (((result.getArtist() != null) && artist.contains(result.getArtist()!!)
                        && (result.getTrack() != null) && title.equals(result.getTrack(),
                    ignoreCase = true))
            ) return fromAPI(result.getURL(), artist, result.getTrack())
        }
        return Lyrics(Lyrics.NO_RESULT)
    }

    // TODO handle urls
    @Reflection
    fun fromURL(url: String?, artist: String?, title: String?): Lyrics {
        return fromAPI(url, artist, title)
    }

    fun fromAPI(url: String?, artist: String?, title: String?): Lyrics {
        val lyrics: Lyrics = Lyrics(Lyrics.POSITIVE_RESULT)
        lyrics.setArtist(artist)
        lyrics.setTitle(title)
        // fixme no public url
        try {
            val jsonText: String = Net.getUrlAsString(url)
            val lyricsJSON: JsonObject = JsonParser().parse(jsonText).asJsonObject
            lyrics.setText(lyricsJSON.get("body").asString.trim({ it <= ' ' }))
        } catch (e: IOException) {
            e.printStackTrace()
            return Lyrics(Lyrics.ERROR)
        } catch (e: JsonParseException) {
            e.printStackTrace()
            return Lyrics(Lyrics.ERROR)
        }
        lyrics.setSource(domain)
        return lyrics
    }
}