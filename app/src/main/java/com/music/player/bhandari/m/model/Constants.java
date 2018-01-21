package com.music.player.bhandari.m.model;

/**
 * Created by amit on 13/12/16.
 */
public class Constants {
    public static String TAG="beta.developer";
    public static String L_TAG = "Lyrics --";

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
        String UPDATE_LYRIC_AND_INFO = "com.update.lyric.info";
        String PLAY_PAUSE_UI_UPDATE ="PLAY_PAUSE_UI_UPDATE";
        String CLICK_TO_CANCEL = "com.click.to.cancel";
        String UPDATE_INSTANT_LYRIC = "com.update.constant.lyric";
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

    public interface COLOR_ARRAY{
         String[] colorArray = new String[]{"#003300","#b30000","#99004d","#600080","#262673","#802000","#808000","#006600"};
         int COLOR_ARR_SIZE=8;
    }

    public interface ERROR_CODE{
        int SUCCESS=0;
        int FAIL=1;
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

        int MANUAL_INPUT = 99;

        int DEEP_PURPLE = -6345880;

        int ANTIQUE_BRONZE = -10068706;
        int ANTIQUE_RUBY = -8119507;
       // int ANTIQUE_RUBY = 8657709;
        int BLUE_MAGNETA_VIOLET = -11192942;

        int EGGPLANT = -10403759;
        int FRENCH_BISTRE = -8032947;
        int DEEP_CHESTNUT = -4633016;

        int RED_CARMINE = -6946792;
        int GREEN_DARTMOUTH = -16748484;
        int BLUE_CATALINA = -16373128;
        int PINK_CERISE = -2215581;
        int AMBER = -33280;
        int BLACK = -16119286;

        int CYBER_GRAPE =  -10993028;
        int BONDI_BLUE = -16738890;
        int BYZANTIUM = -9426589;
        int DARK_SLATE_GRAY = -13676721;

        int YELLOW = -1862272256;

        //new colors
        int GUNMETAL = -14011335;
        int HALAYA_UBE = -10078124;
        int INTERNATIONAL_ORANGE = -45312;
        int JACARTA = -12766627;
        int JAPANESE_VIOLET = -10800554;
        int MAGENTA = -3530885;
        int MAASTRICHT_BLUE = -16769987;
        int MAROON = -8388608;
        int PINE_TREE = -14012637;
        int POLIE_BLUE = -13152405;


        int RANDOM = -1;


        /*int RANDOM_ARR_SIZE = 10;
        int[] MIX = new int[] {RED_CARMINE,GREEN_DARTMOUTH,BLACK,BLUE_CATALINA
                ,PINK_CERISE,AMBER,CYBER_GRAPE,BONDI_BLUE,BYZANTIUM,DARK_SLATE_GRAY};*/
    }

    public interface TYPEFACE {
        int MONOSPACE = 0;
        int SOFIA = 1;
        int RISQUE = 2;
        int VAST_SHADOW = 3;
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