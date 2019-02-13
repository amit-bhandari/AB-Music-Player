package com.music.player.bhandari.m.model;

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

public class Constants {
    public static String TAG="beta.developer";
    public static String L_TAG = "Lyrics --";

    public static int DEFAULT_THEME_ID = 12;

    public interface ACTION {
        String MAIN_ACTION = "com.bhandari.musicplayer.action.main";
        String FAV_ACTION = "com.bhandari.fav";
        String PREV_ACTION = "ccom.bhandari.musicplayer.action.prev";
        String PLAY_PAUSE_ACTION = "com.bhandari.musicplayer.action.play";
        String NEXT_ACTION = "com.bhandari.musicplayer.action.next";
        String DISMISS_EVENT =  "com.bhandari.musicplayer.action.dismiss" ;
        String SWIPE_TO_DISMISS = "com.bhandari.musicplayer.action.swipe.to.dismiss";
        String COMPLETE_UI_UPDATE ="UPDATE_NOW_PLAYING";
        String DELETE_RESULT = "delete";
        String OPEN_FROM_FILE_EXPLORER = "com.bhandari.musicplayer.action.explorer";
        String DISC_UPDATE = "com.disc.update";
        String REFRESH_LIB = "com.refresh.lib";
        String WIDGET_UPDATE = "com.update.widget";
        String LAUNCH_PLAYER_FROM_WIDGET = "comm.launch.nowplaying";
        String SHUFFLE_WIDGET = "com.shuffle.widget";
        String REPEAT_WIDGET  = "com.repeat.widget";
        String FAV_WIDGET  = "com.fav.widget";
        String UPDATE_LYRIC_AND_INFO = "com.update.lyric.info";
        String PLAY_PAUSE_UI_UPDATE ="PLAY_PAUSE_UI_UPDATE";
        String CLICK_TO_CANCEL = "com.click.to.cancel";
        String UPDATE_INSTANT_LYRIC = "com.update.constant.lyric";
        String SECONDARY_ADAPTER_DATA_READY = "com.secondary.data.ready";
    }

    public interface PREFERENCES{
        String STORED_SONG_POSITION_DURATION = "duration";
        String STORED_SONG_ID = "title";
        String SHUFFLE="shuffle";
        String REPEAT="repeat";
        String PREV_ACTION="prev_action";
    }

    public interface PREFERENCE_VALUES{
        int NO_REPEAT=0;
        int REPEAT_ALL=1;
        int REPEAT_ONE=2;
        float PREV_ACT_TIME_CONSTANT = (float) 0.1;
    }

    public interface NOTIFICATION_ID {
        int INSTANT_LYRICS = 102;
        int FOREGROUND_SERVICE = 101;
        int BATCH_DOWNLOADER = 99;
        int FCM = 98;
    }

    public interface FRAGMENT_STATUS{
        //to know which fragment is being instantiated
       int TITLE_FRAGMENT=0, ARTIST_FRAGMENT=1, ALBUM_FRAGMENT=2
                , GENRE_FRAGMENT=3, PLAYLIST_FRAGMENT=4
                ,ALBUM_FRAGMENT_GRID=5, SECONDARY_LIB_FRAG=6;
    }

    public interface ADD_TO_Q{
        //to know which fragment is being instantiated
        int IMMEDIATE_NEXT=0,AT_LAST=1;
    }

    public interface SORT_ORDER{
        int ASC=0;
        int DESC=1;
    }

    public interface SYSTEM_PLAYLISTS{
        String MOST_PLAYED = "Most_Played";
        String RECENTLY_PLAYED = "Recently_Played";
        String RECENTLY_ADDED = "Recently_Added";
        String MY_FAV = "My_Fav";
        String PLAYLIST_LIST = "playlist_list";

        String [] listOfSystemPlaylist=new String[]{MOST_PLAYED,RECENTLY_ADDED,RECENTLY_PLAYED,MY_FAV};
        int RECENTLY_PLAYED_MAX=50;
        int MOST_PLAYED_MAX=50;
    }


    public interface CLICK_ON_NOTIF{
        int OPEN_LIBRARY_VIEW=0;
        int OPEN_DISC_VIEW=1;
        int DO_NOTHING=2;
    }

    public interface DISC_SIZE{
        float SMALL = 4.5f;
        float MEDIUM = 4f;
        float BIG = 3.5f;
    }

    public interface PRIMARY_COLOR {
        int DARK = 1;
        int LIGHT = 2;
        int GLOSSY = 3;
        int BLACK = -16119286;
    }

    public interface TYPEFACE {
        int MONOSPACE = 0;
        int SOFIA = 1;
        int MANROPE = 2;
        int ASAP = 3;
        int SYSTEM_DEFAULT = 4;
        int ACME = 5;
    }
    public interface PREF_LAUNCHED_FROM{
        int MAIN = 0;
        int NOW_PLAYING = 1;
        int DRAWER = 2;
    }

    public interface SHAKE_ACTIONS{
        int PLAY_PAUSE = 0;
        int NEXT = 1;
        int PREVIOUS = 2;
    }

    public interface DONATE{
         int COFFEE = 0 ;
         int BEER = 1;
         int JD = 2;
    }

    public interface TABS {
        int ALBUMS = 0 ;
        int TRACKS = 1;
        int ARTIST = 2;
        int GENRE = 3;
        int PLAYLIST = 4;
        int FOLDER = 5;

        int NUMBER_OF_TABS = 6;
        String DEFAULT_SEQ="0,1,2,3,4,5";
    }

    public interface  SORT_BY{
        int NAME = 0;
        int YEAR = 1;
        int NO_OF_ALBUMS = 2;
        int NO_OF_TRACKS = 3;
        int ASC = 4;
        int DESC = 5;
        int SIZE = 6;
        int DURATION = 7;

    }

    public interface TAG_EDITOR_LAUNCHED_FROM{
        int MAIN_LIB = 0;
        int SECONDARY_LIB=1;
        int NOW_PLAYING=2;
    }

    public interface EXIT_NOW_PLAYING_AT{
        int DISC_FRAG = 1;
        int LYRICS_FRAG = 2;
        int ARTIST_FRAG = 0;
    }

    public interface FIRST_TIME_INFO{
        int MUSIC_LOCK=0;
        int SORTING = 1;
        int MINI_PLAYER=2;
        int FAV=3;
        int CURRENT_QUEUE = 4;
        int SWIPE_RIGHT=5;
    }
}