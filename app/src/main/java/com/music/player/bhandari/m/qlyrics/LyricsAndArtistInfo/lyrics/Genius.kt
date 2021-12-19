package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics

import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Keys
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Net
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.annotations.Reflection
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist
import org.jsoup.select.Elements
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.text.Normalizer
import java.util.regex.Pattern

/**
 * This file is part of QuickLyric
 * Created by geecko
 *
 *
 * QuickLyric is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * QuickLyric is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with QuickLyric.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
object Genius {
    @Reflection
    val domain: String = "genius.com"
    fun search(query: String?): ArrayList<Lyrics> {
        var query: String? = query
        val results: ArrayList<Lyrics> = ArrayList<Lyrics>()
        query = Normalizer.normalize(query, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        var response: JsonObject? = null
        try {
            val queryURL: URL = URL(String.format("http://api.genius.com/search?q=%s",
                URLEncoder.encode(query, "UTF-8")))
            val connection: Connection = Jsoup.connect(queryURL.toExternalForm())
                .header("Authorization", "Bearer " + Keys.GENIUS)
                .timeout(0)
                .ignoreContentType(true)
            val document: Document = connection.userAgent(Net.USER_AGENT).get()
            response = JsonParser().parse(document.text()).asJsonObject
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (response == null || response.getAsJsonObject("meta").get("status")
                .asInt != 200
        ) return results
        val hits = response.getAsJsonObject("response").getAsJsonArray("hits")
        var processed = 0
        while (processed < hits.size()) {
            val song: JsonObject = hits.get(processed).asJsonObject.getAsJsonObject("result")
            val artist: String = song.getAsJsonObject("primary_artist").get("name").asString
            val title: String = song.get("title").asString
            val url: String = "http://genius.com/songs/" + song.get("id").asString
            val l = Lyrics(Lyrics.SEARCH_ITEM)
            l.setArtist(artist)
            l.setTitle(title)
            l.setURL(url)
            l.setSource("Genius")
            results.add(l)
            processed++
        }
        return results
    }

    @Reflection
    fun fromMetaData(originalArtist: String?, originalTitle: String?): Lyrics {
        var urlArtist: String = Normalizer.normalize(originalArtist, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        var urlTitle: String = Normalizer.normalize(originalTitle, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        urlArtist =
            urlArtist.replace("[^a-zA-Z0-9\\s+]".toRegex(), "").replace("&".toRegex(), "and")
                .trim { it <= ' ' }.replace("[\\s+]".toRegex(), "-")
        urlTitle = urlTitle.replace("[^a-zA-Z0-9\\s+]".toRegex(), "").replace("&".toRegex(), "and")
            .trim { it <= ' ' }.replace("[\\s+]".toRegex(), "-")
        val url: String = String.format("http://genius.com/%s-%s-lyrics", urlArtist, urlTitle)
        return fromURL(url, originalArtist, originalTitle)
    }

    fun fromURL(url: String?, artist: String?, title: String?): Lyrics {
        var artist: String? = artist
        var title: String? = title
        val lyricsPage: Document
        val text: String
        try {
            lyricsPage = Jsoup.connect(url).userAgent(Net.USER_AGENT).get()
            val lyricsDiv: Elements = lyricsPage.select(".lyrics")
            if (lyricsDiv.isEmpty()) throw StringIndexOutOfBoundsException() else text =
                Jsoup.clean(lyricsDiv.html(), Whitelist.none().addTags("br")).trim { it <= ' ' }
        } catch (e: HttpStatusException) {
            return Lyrics(Lyrics.NO_RESULT)
        } catch (e: IOException) {
            e.printStackTrace()
            return Lyrics(Lyrics.ERROR)
        } catch (e: StringIndexOutOfBoundsException) {
            e.printStackTrace()
            return Lyrics(Lyrics.ERROR)
        }
        if (artist == null) {
            title = lyricsPage.getElementsByClass("text_title").get(0).text()
            artist = lyricsPage.getElementsByClass("text_artist").get(0).text()
        }
        var result = Lyrics(Lyrics.POSITIVE_RESULT)
        if (("[Instrumental]" == text)) result = Lyrics(Lyrics.NEGATIVE_RESULT)
        val pattern: Pattern = Pattern.compile("\\[.+\\]")
        val builder: StringBuilder = StringBuilder()
        for (line: String in text.split("<br> ".toRegex()).toTypedArray()) {
            val strippedLine: String = line.replace("\\s".toRegex(), "")
            if (!pattern.matcher(strippedLine)
                    .matches() && !(strippedLine.isEmpty() && builder.isEmpty())
            ) builder.append(line.replace("\\P{Print}".toRegex(), "")).append("<br/>")
        }
        if (builder.length > 5) builder.delete(builder.length - 5, builder.length)
        result.setArtist(artist)
        result.setTitle(title)
        result.setText(Normalizer.normalize(builder.toString(), Normalizer.Form.NFD))
        result.setURL(url)
        result.setSource("Genius")
        Log.v(Constants.TAG, "Lyrics downloaded from Genius " + result.getFlag())
        return result
    }
}