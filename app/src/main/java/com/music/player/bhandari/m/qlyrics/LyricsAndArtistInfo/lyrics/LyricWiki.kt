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

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Net.getUrlAsString
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.annotations.Reflection
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.Companion.ERROR
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.Companion.NEGATIVE_RESULT
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.Companion.NO_RESULT
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.Companion.POSITIVE_RESULT
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.Companion.SEARCH_ITEM
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.jsoup.safety.Whitelist
import org.jsoup.select.Elements
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.Normalizer

object LyricWiki {
    @Reflection
    val domain: String = "lyrics.wikia.com"
    private val baseUrl: String =
        "http://lyrics.wikia.com/api.php?action=lyrics&fmt=json&func=getSong&artist=%1s&song=%1s"
    private val baseAPIUrl: String =
        "http://lyrics.wikia.com/wikia.php?controller=LyricsApi&method=getSong&artist=%1s&song=%2s"
    private val baseSearchUrl: String =
        "http://lyrics.wikia.com/Special:Search?search=%s&fulltext=Search"

    @Reflection
    fun search(query: String): ArrayList<Lyrics> {
        var query: String = query
        val results: ArrayList<Lyrics> = ArrayList<Lyrics>()
        query = "$query song"
        query = Normalizer.normalize(query, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        try {
            val queryURL: URL = URL(String.format(baseSearchUrl, URLEncoder.encode(query, "UTF-8")))
            val searchpage: Document = Jsoup.connect(queryURL.toExternalForm()).timeout(0).get()
            var searchResults: Elements = searchpage.getElementsByClass("Results")
            if (searchResults.size >= 1) {
                searchResults = searchResults.get(0).getElementsByClass("result")
                for (searchResult: Element in searchResults) {
                    val tags: Array<String> =
                        searchResult.getElementsByTag("h1").text().split(":".toRegex())
                            .toTypedArray()
                    if (tags.size != 2) continue
                    val url: String = searchResult.getElementsByTag("a").attr("href")
                    val lyrics: Lyrics = Lyrics(SEARCH_ITEM)
                    lyrics.setArtist(tags.get(0))
                    lyrics.setTitle(tags.get(1))
                    lyrics.setURL(url)
                    lyrics.setSource(domain)
                    results.add(lyrics)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return results
    }

    @Reflection
    fun fromMetaData(artist: String?, title: String?): Lyrics {
        var artist: String? = artist
        var title: String? = title
        if ((artist == null) || (title == null)) return Lyrics(ERROR)
        val originalArtist: String = artist
        val originalTitle: String = title
        var url: String? = null
        try {
            var encodedArtist: String? = URLEncoder.encode(artist, "UTF-8")
            var encodedSong: String? = URLEncoder.encode(title, "UTF-8")
            var json: JsonObject = JsonParser().parse(getUrlAsString(URL(String.format(baseUrl,
                encodedArtist,
                encodedSong))).replace("song = ", "")).asJsonObject
            url = URLDecoder.decode(json.get("url").asString, "UTF-8")
            artist = json.get("artist").asString
            title = json.get("song").asString
            encodedArtist = URLEncoder.encode(artist, "UTF-8")
            encodedSong = URLEncoder.encode(title, "UTF-8")
            json = JsonParser().parse(getUrlAsString(URL(String.format(baseAPIUrl,
                encodedArtist,
                encodedSong)))
            ).asJsonObject.get("result").asJsonObject
            val lyrics: Lyrics = Lyrics(POSITIVE_RESULT)
            lyrics.setArtist(artist)
            lyrics.setTitle(title)
            lyrics.setText(json.get("lyrics").asString.replace("\n".toRegex(), "<br />"))
            lyrics.setURL(url)
            lyrics.setOriginalArtist(originalArtist)
            lyrics.setOriginalTitle(originalTitle)
            return lyrics
        } catch (e: JsonParseException) {
            return Lyrics(NO_RESULT)
        } catch (e: IOException) {
            return if (url == null) Lyrics(ERROR) else fromURL(url, originalArtist, originalTitle)
        } catch (e: IllegalStateException) {
            return if (url == null) Lyrics(ERROR) else fromURL(url, originalArtist, originalTitle)
        } catch (e: NullPointerException) {
            return if (url == null) Lyrics(ERROR) else fromURL(url, originalArtist, originalTitle)
        }
    }

    fun fromURL(url: String, artist: String?, song: String?): Lyrics {
        var artist: String? = artist
        var song: String? = song
        if (url.endsWith("action=edit")) {
            return Lyrics(NO_RESULT)
        }
        var text: String
        val originalArtist: String? = artist
        val originalTitle: String? = song
        try {
            //url = URLDecoder.decode(url, "utf-8");
            val lyricsPage: Document = Jsoup.connect(url).get()
            val lyricbox: Element = lyricsPage.select("div.lyricBox").get(0)
            lyricbox.getElementsByClass("references").remove()
            val lyricsHtml: String = lyricbox.html()
            val outputSettings: Document.OutputSettings = Document.OutputSettings().prettyPrint(false)
            text = Jsoup.clean(lyricsHtml, "", Whitelist().addTags("br"), outputSettings)
            if (text.contains("&#")) text = Parser.unescapeEntities(text, true)
            text = text.replace("\\[\\d\\]".toRegex(), "").trim { it <= ' ' }
            val title: String = lyricsPage.getElementsByTag("title").get(0).text()
            val colon: Int = title.indexOf(':')
            if (artist == null) artist = title.substring(0, colon).trim({ it <= ' ' })
            if (song == null) {
                val end: Int = title.lastIndexOf("Lyrics")
                song = title.substring(colon + 1, end).trim({ it <= ' ' })
            }
        } catch (e: IndexOutOfBoundsException) {
            return Lyrics(ERROR)
        } catch (e: IOException) {
            return Lyrics(ERROR)
        }
        try {
            artist = URLDecoder.decode(artist, "UTF-8")
            song = URLDecoder.decode(song, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        when {
            text.contains("Unfortunately, we are not licensed to display the full lyrics for this song at the moment.")
                    || (text == "Instrumental <br />") -> {
                val result = Lyrics(NEGATIVE_RESULT)
                result.setArtist(artist)
                result.setTitle(song)
                return result
            }
            text == "" || text.length < 3 -> return Lyrics(NO_RESULT)
            else -> {
                val lyrics = Lyrics(POSITIVE_RESULT)
                lyrics.setArtist(artist)
                lyrics.setTitle(song)
                lyrics.setOriginalArtist(originalArtist)
                lyrics.setOriginalTitle(originalTitle)
                lyrics.setText(text)
                lyrics.setSource("LyricsWiki")
                lyrics.setURL(url)
                return lyrics
            }
        }
    }
}