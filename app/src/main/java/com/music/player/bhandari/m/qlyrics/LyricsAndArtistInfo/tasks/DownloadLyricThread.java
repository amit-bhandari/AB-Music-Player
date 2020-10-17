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

package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.AZLyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Bollywood;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Genius;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.JLyric;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lololyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.LyricWiki;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.LyricsMania;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.MetalArchives;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.PLyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.UrbanLyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.ViewLyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics;

import java.util.ArrayList;
import java.util.Arrays;

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

public class DownloadLyricThread extends Thread {

    private static final String[] mainProviders =
            {
                    "ViewLyrics",
                    "LyricWiki",
                    "Genius",
                    "LyricsMania",
                    "AZLyrics",
                    "Bollywood"
            };

    private static final ArrayList<String> providers = new ArrayList<>(Arrays.asList(mainProviders));

    private final boolean positionAvailable;
    private Lyrics.Callback callback;
    private final TrackItem item;
    private final String[] params;

    public DownloadLyricThread(final Lyrics.Callback callback, boolean positionAvailable, TrackItem item, final String... params) {
        this.positionAvailable = positionAvailable;
        this.callback = callback;
        this.item = item;
        this.params = params;
    }

    public void setCallback(Lyrics.Callback callback){
        this.callback = callback;
    }

    Lyrics download(String url, String artist, String title) {


        Lyrics lyrics = new Lyrics(Lyrics.NO_RESULT);
        for (String provider : providers) {
            switch (provider) {
                case "AZLyrics":
                    lyrics = AZLyrics.fromURL(url, artist, title);
                    break;
                case "Bollywood":
                    lyrics = Bollywood.fromURL(url, artist, title);
                    break;
                case "Genius":
                    lyrics = Genius.fromURL(url, artist, title);
                    break;
                case "JLyric":
                    lyrics = JLyric.fromURL(url, artist, title);
                    break;
                case "Lololyrics":
                    lyrics = Lololyrics.fromURL(url, artist, title);
                    break;
                case "LyricsMania":
                    lyrics = LyricsMania.fromURL(url, artist, title);
                    break;
                case "LyricWiki":
                    lyrics = LyricWiki.fromURL(url, artist, title);
                    break;
                case "MetalArchives":
                    lyrics = MetalArchives.fromURL(url, artist, title);
                    break;
                case "PLyrics":
                    lyrics = PLyrics.fromURL(url, artist, title);
                    break;
                case "UrbanLyrics":
                    lyrics = UrbanLyrics.fromURL(url, artist, title);
                    break;
                case "ViewLyrics":
                    lyrics = ViewLyrics.fromURL(url, artist, title);
                    break;
            }
            if (lyrics.isLRC() && !positionAvailable)
                continue;
            if (lyrics.getFlag() == Lyrics.POSITIVE_RESULT)
                return lyrics;
        }
        return new Lyrics(Lyrics.NO_RESULT);
    }

    Lyrics download(String artist, String title) {
        Log.v("DOWNLOAD " + artist, Log.getStackTraceString(new Exception()));

        Lyrics lyrics = new Lyrics(Lyrics.NO_RESULT);
        for (String provider : providers) {
            switch (provider) {
                case "AZLyrics":
                    lyrics = AZLyrics.fromMetaData(artist, title);
                    break;
                case "Bollywood":
                    lyrics = Bollywood.fromMetaData(artist, title);
                    break;
                case "Genius":
                    lyrics = Genius.fromMetaData(artist, title);
                    break;
                case "JLyric":
                    lyrics = JLyric.fromMetaData(artist, title);
                    break;
                case "Lololyrics":
                    lyrics = Lololyrics.fromMetaData(artist, title);
                    break;
                case "LyricsMania":
                    lyrics = LyricsMania.fromMetaData(artist, title);
                    break;
                case "LyricWiki":
                    lyrics = LyricWiki.fromMetaData(artist, title);
                    break;
                case "MetalArchives":
                    lyrics = MetalArchives.fromMetaData(artist, title);
                    break;
                case "PLyrics":
                    lyrics = PLyrics.fromMetaData(artist, title);
                    break;
                case "UrbanLyrics":
                    lyrics = UrbanLyrics.fromMetaData(artist, title);
                    break;
                case "ViewLyrics":
                    try {
                        lyrics = ViewLyrics.fromMetaData(artist, title);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
            if (lyrics.isLRC() && !positionAvailable)
                continue;
            if (lyrics.getFlag() == Lyrics.POSITIVE_RESULT)
                return lyrics;
        }
        return lyrics;
    }

    private void threadMsg(Lyrics lyrics) {
        if (lyrics != null) {

            //put lyrics in db
            if (item != null && lyrics.getFlag() == Lyrics.POSITIVE_RESULT) {
                Log.v(Constants.L_TAG, lyrics.getOriginalArtist());
                // Log.v(Constants.L_TAG,lyrics.getSource()+" ");
                Log.v(Constants.L_TAG, lyrics.isLRC() + " lrc");
                // Log.v(Constants.L_TAG,lyrics.getText() );
                OfflineStorageLyrics.putLyricsInDB(lyrics, item);
            }

            Message msgObj = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putSerializable("lyrics", lyrics);
            msgObj.setData(b);
            handler.sendMessage(msgObj);
        }
    }

    // Define the Handler that receives messages from the thread and update the progress
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Lyrics result = (Lyrics) msg.getData().getSerializable("lyrics");
            if (result != null) {
                if (callback != null) callback.onLyricsDownloaded(result);
            }
        }
    };

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        Lyrics lyrics;
        String artist = null;
        String title = null;
        String url = null;
        switch (params.length) {
            case 3: // URL + tags
                artist = params[1];
                title = params[2];
            case 1: // URL
                url = params[0];
                lyrics = download(url, artist, title);
                break;
            default: // just tags
                artist = params[0];
                title = params[1];
                lyrics = download(params[0], params[1]);
        }
        if (lyrics.getFlag() != Lyrics.POSITIVE_RESULT) {
            String[] correction = correctTags(artist, title);
            if (!(correction[0].equals(artist) && correction[1].equals(title)) || url != null) {
                lyrics = download(correction[0], correction[1]);
                lyrics.setOriginalArtist(artist);
                lyrics.setOriginalTitle(title);
            }
        }
        if (lyrics.getArtist() == null) {
            if (artist != null) {
                lyrics.setArtist(artist);
                lyrics.setTitle(title);
            } else {
                lyrics.setArtist("");
                lyrics.setTitle("");
            }
        }
        if (item != null) {
            lyrics.setTrackId(item.getId());
        }
        threadMsg(lyrics);
    }


    private static String[] correctTags(String artist, String title) {
        if (artist == null || title == null)
            return new String[]{"", ""};
        String correctedArtist = artist.replaceAll("\\(.*\\)", "")
                .replaceAll(" \\- .*", "").trim();
        String correctedTrack = title.replaceAll("\\(.*\\)", "")
                .replaceAll("\\[.*\\]", "").replaceAll(" \\- .*", "").trim();
        String[] separatedArtists = correctedArtist.split(", ");
        correctedArtist = separatedArtists[separatedArtists.length - 1];
        return new String[]{correctedArtist, correctedTrack};
    }
}