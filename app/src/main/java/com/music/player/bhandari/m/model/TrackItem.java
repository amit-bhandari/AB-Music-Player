package com.music.player.bhandari.m.model;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.Serializable;

/**
 * Copyright 2017 Amit Bhandari AB
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class TrackItem implements Serializable {
    private long id;
    private String filePath = "";
    private String title = "";
    private String artist = "";
    private String album = "";
    private String genre = "";
    private String duration = "";  //string in milliseconds
    private long album_id;
    private long artist_id;

    //default constructor
    public TrackItem() {

    }

    public TrackItem(String filePath, String title, String artist, String album, String genre, String duration
            , long album_id, int artist_id, int id) {
        this.filePath = filePath;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.duration = duration;
        this.album_id = album_id;
        this.id = id;
        this.artist_id = artist_id;
    }

    //return in ms
    public int getDurInt() {
        int durationMs = 0;
        try {
            durationMs = Integer.parseInt(duration);
        } catch (NumberFormatException e) {
            //send predefined number 300 msec
            durationMs = 3000;
        }
        return durationMs;
    }

    public String getDurStr() {
        int minutes = (Integer.parseInt(duration) / 1000) / 60;
        int seconds = (Integer.parseInt(duration) / 1000) % 60;
        String durFormatted = String.format("%02d", seconds);
        return String.valueOf(minutes) + ":" + durFormatted;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String title) {
        this.filePath = filePath;
    }

    public String getGenre() {
        return genre;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String title) {
        this.album = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        if (artist == null || artist.isEmpty()) {
            this.artist = "<unknown>";
            return;
        }
        this.artist = artist;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public long getAlbumId() {
        return album_id;
    }

    public long getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(long artist_id) {
        this.artist_id = artist_id;
    }
}
