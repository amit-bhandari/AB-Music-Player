package com.music.player.bhandari.m.lyricsExplore;

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
