package com.music.player.bhandari.m.model;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.Serializable;

/**
 * Created by amit on 29/11/16.
 */

public class TrackItem implements Serializable {
    private int id;
    private String filePath="";
    private String title="";
    private String artist="";
    private String album="";
    private String genre="";
    private String duration="";  //string in milliseconds
    private int album_id;
    private int artist_id;
    //default constructor
    public TrackItem() {

    }

    public TrackItem(String filePath, String title, String artist, String album,String genre, String duration
            , int album_id, int artist_id, int id){
        this.filePath=filePath;
        this.title=title;
        this.artist=artist;
        this.album=album;
        this.genre=genre;
        this.duration=duration;
        this.album_id=album_id;
        this.id = id;
        this.artist_id=artist_id;
    }



    //if needed in case
    public TrackItem(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        Log.e("filepath",filePath);
        mmr.setDataSource(filePath);
        String duration=mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        this.title=(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        this.artist=(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        this.album=(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
        this.duration=(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        this.filePath=filePath;
    }

    //return in ms
    public int getDurInt(){
        int durationMs = 0;
        try {
            durationMs = Integer.parseInt(duration);
        }catch (NumberFormatException e){
            //send predefined number 300 msec
            durationMs = 3000;
        }
        return durationMs;
    }

    public String getDurStr(){
        int minutes=(Integer.parseInt(duration)/1000)/60;
        int seconds= (Integer.parseInt(duration)/1000)%60;
        String durFormatted=String.format("%02d",seconds);
        return  String.valueOf(minutes)+":"+durFormatted;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId() {
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
        if (artist==null || artist.isEmpty()){
            this.artist="<unknown>";
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

    public int getAlbumId(){return album_id;}

    public int getArtist_id(){return artist_id;}

    public void setArtist_id(int artist_id){
        this.artist_id = artist_id;
    }

    public boolean haveAlbumArt(){
        return MusicLibrary.getInstance().getAlbumArtUri(album_id) != null;
    }
}
