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


import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.annotations.Reflection;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Net;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Reflection
public class PLyrics {

    public static final String domain = "www.plyrics.com/";

    @Reflection
    public static Lyrics fromMetaData(String artist, String song) {
        String htmlArtist = artist.replaceAll("[\\s'\"-]", "")
                .replaceAll("&", "and").replaceAll("[^A-Za-z0-9]", "");
        String htmlSong = song.replaceAll("[\\s'\"-]", "")
                .replaceAll("&", "and").replaceAll("[^A-Za-z0-9]", "");

        if (htmlArtist.toLowerCase(Locale.getDefault()).startsWith("the"))
            htmlArtist = htmlArtist.substring(3);

        String urlString = String.format(
                "http://www.plyrics.com/lyrics/%s/%s.html",
                htmlArtist.toLowerCase(Locale.getDefault()),
                htmlSong.toLowerCase(Locale.getDefault()));
        return fromURL(urlString, artist, song);
    }

    public static Lyrics fromURL(String url, String artist, String song) {
        String html;
        try {
            Document document = Jsoup.connect(url).userAgent(Net.USER_AGENT).get();
            if (document.location().contains(domain))
                html = document.html();
            else
                throw new IOException("Redirected to wrong domain " + document.location());
        } catch (HttpStatusException e) {
            return new Lyrics(Lyrics.NO_RESULT);
        } catch (IOException e) {
            e.printStackTrace();
            return new Lyrics(Lyrics.ERROR);
        }
        Pattern p = Pattern.compile(
                "<!-- start of lyrics -->(.*)<!-- end of lyrics -->",
                Pattern.DOTALL);
        Matcher matcher = p.matcher(html);

        if (artist == null || song == null) {
            Pattern metaPattern = Pattern.compile(
                    "ArtistName = \"(.*)\";\r\nSongName = \"(.*)\";\r\n",
                    Pattern.DOTALL);
            Matcher metaMatcher = metaPattern.matcher(html);
            if (metaMatcher.find()) {
                artist = metaMatcher.group(1);
                song = metaMatcher.group(2);
                song = song.substring(0, song.indexOf('"'));
            } else
                artist = song = "";
        }

        if (matcher.find()) {
            Lyrics l = new Lyrics(Lyrics.POSITIVE_RESULT);
            l.setArtist(artist);
            String text = matcher.group(1);
            text = text.replaceAll("\\[[^\\[]*\\]", "");
            l.setText(text);
            l.setTitle(song);
            l.setURL(url);
            l.setSource("PLyrics");
            return l;
        } else
            return new Lyrics(Lyrics.NEGATIVE_RESULT);
    }

}