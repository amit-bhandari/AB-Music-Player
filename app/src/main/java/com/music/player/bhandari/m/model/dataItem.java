package com.music.player.bhandari.m.model;

import androidx.annotation.NonNull;

/**
 Copyright 2017 Amit Bhandari AB

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

public class dataItem{


    public dataItem(int id,String title,int artist_id, String artist_name, int album_id, String albumName
            , String year, String file_path, String duration, int trackNumber){
        if(title!=null) {
            this.title = title;
        }
        this.id=id;


        this.album_id=album_id;

        if(albumName!=null) {
            this.albumName=albumName;
        }

        this.artist_id=artist_id;

        if(artist_name!=null) {
            this.artist_name = artist_name;
        }

        this.file_path=file_path;
        if(year!=null)
        this.year=year;

        //Log.v("Year", title + " : " + year);

        this.duration=duration;
        if(!duration.equals("")) {
            this.durStr = getDurStr();
        }

        this.trackNumber = trackNumber;
    }

    public dataItem(int id,String title,int numberOfTracks, int numberOfAlbums){

        this.artist_id=id;
        if(title!=null) {
            this.title = title;
            this.artist_name=title;
        }

        this.numberOfTracks = numberOfTracks;
        this.numberOfAlbums=numberOfAlbums;
    }


    public dataItem(int id,String title,String artist_name, int numberOfTracks, String year, int artist_id){
        this.artist_name=artist_name;
        this.artist_id = artist_id;
        if(title!=null) {
            this.title = title;
            this.albumName = title;
        }

        this.album_id=id;

       // Log.v("Year", title + " : " + year);

        if(year!=null)
        this.year=year;
        this.numberOfTracks = numberOfTracks;
    }

    public dataItem(int genre_id,String genre_name, int numberOfTracks){
        this.id=genre_id;
        if(genre_name!=null) {
            this.title = genre_name;
        }
        this.numberOfTracks = numberOfTracks;
    }

    public int id;

    public String title="";

    public int artist_id;
    public String artist_name="";

    public int album_id;
    public String albumName="";

    public String year="zzzz";

    public int numberOfTracks;
    public int numberOfAlbums;
    public String file_path;


    public String duration;
    public String durStr;

    public int trackNumber=0;

    private String getDurStr(){
        int minutes = 0;
        int seconds = 0;
        try {
             minutes = (Integer.parseInt(duration) / 1000) / 60;
             seconds = (Integer.parseInt(duration) / 1000) % 60;
        } catch (NumberFormatException ignored){

        }
        String durFormatted=String.format("%02d",seconds);
        return  minutes+":"+durFormatted;
    }
}