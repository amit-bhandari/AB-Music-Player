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

package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.annotations.Reflection;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Net;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import static com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.ERROR;
import static com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.NO_RESULT;
import static com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.POSITIVE_RESULT;


@Reflection
public class MetalArchives {

    public static final String domain = "metal-archives.com";

    @Reflection
    public static Lyrics fromMetaData(String artist, String title) {
        String baseURL = "http://www.metal-archives.com/search/ajax-advanced/searching/songs/?bandName=%s&songTitle=%s&releaseType[]=1&exactSongMatch=1&exactBandMatch=1";
        String urlArtist = artist.replaceAll("\\s", "+");
        String urlTitle = title.replaceAll("\\s", "+");
        String url;
        String text;
        try {
            String response = Net.getUrlAsString(String.format(baseURL, urlArtist, urlTitle));
            JsonObject jsonResponse = new JsonParser().parse(response).getAsJsonObject();
            JsonArray track = jsonResponse.getAsJsonArray("aaData").get(0).getAsJsonArray();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < track.size(); i++)
                builder.append(track.get(i).getAsString());
            Document trackDocument = Jsoup.parse(builder.toString());
            url = trackDocument.getElementsByTag("a").get(1).attr("href");
            String id = trackDocument.getElementsByClass("viewLyrics").get(0).id().substring(11);
            text = Jsoup.connect("http://www.metal-archives.com/release/ajax-view-lyrics/id/" + id)
                    .get().body().html();
        } catch (JsonParseException | IndexOutOfBoundsException e) {
            return new Lyrics(NO_RESULT);
        } catch (Exception e) {
            return new Lyrics(ERROR);
        }
        Lyrics lyrics = new Lyrics(POSITIVE_RESULT);
        lyrics.setArtist(artist);
        lyrics.setTitle(title);
        lyrics.setText(text);
        lyrics.setSource(domain);
        lyrics.setURL(url);

        return lyrics;
    }

    @Reflection
    public static Lyrics fromURL(String url, String artist, String title) {
        // TODO: support metal-archives URL
        return new Lyrics(NO_RESULT);
    }

}
