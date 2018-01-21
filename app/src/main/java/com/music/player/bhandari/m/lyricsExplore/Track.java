package com.music.player.bhandari.m.lyricsExplore;

/**
 * Created by abami on 12/6/2017.
 */

public class Track {

    public Track(String title, String artist, int playCount, String imageUrl){
        this.title = title;
        this.artist = artist;
        this.playCount = playCount;
        this.imageUrl = imageUrl;
    }

    public Track(){}

    public String title = "";
    public String artist = "";
    public int playCount;
    public String imageUrl = "";

}
