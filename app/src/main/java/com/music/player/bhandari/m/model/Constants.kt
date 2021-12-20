package com.music.player.bhandari.m.model

/**
 * Copyright 2017 Amit Bhandari AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
object Constants {
    var TAG = "beta.developer"
    var L_TAG = "Lyrics --"
    var DEFAULT_THEME_ID = 21

    interface ACTION {
        companion object {
            const val MAIN_ACTION = "com.bhandari.musicplayer.action.main"
            const val FAV_ACTION = "com.bhandari.fav"
            const val PREV_ACTION = "ccom.bhandari.musicplayer.action.prev"
            const val PLAY_PAUSE_ACTION = "com.bhandari.musicplayer.action.play"
            const val NEXT_ACTION = "com.bhandari.musicplayer.action.next"
            const val DISMISS_EVENT = "com.bhandari.musicplayer.action.dismiss"
            const val SWIPE_TO_DISMISS = "com.bhandari.musicplayer.action.swipe.to.dismiss"
            const val COMPLETE_UI_UPDATE = "UPDATE_NOW_PLAYING"
            const val DELETE_RESULT = "delete"
            const val OPEN_FROM_FILE_EXPLORER = "com.bhandari.musicplayer.action.explorer"
            const val DISC_UPDATE = "com.disc.update"
            const val REFRESH_LIB = "com.refresh.lib"
            const val WIDGET_UPDATE = "com.update.widget"
            const val LAUNCH_PLAYER_FROM_WIDGET = "comm.launch.nowplaying"
            const val SHUFFLE_WIDGET = "com.shuffle!!.widget"
            const val REPEAT_WIDGET = "com.repeat.widget"
            const val FAV_WIDGET = "com.fav.widget"
            const val UPDATE_LYRIC_AND_INFO = "com.update.lyric.info"
            const val PLAY_PAUSE_UI_UPDATE = "PLAY_PAUSE_UI_UPDATE"
            const val CLICK_TO_CANCEL = "com.click.to.cancel"
            const val UPDATE_INSTANT_LYRIC = "com.update.constant.lyric"
            const val SECONDARY_ADAPTER_DATA_READY = "com.secondary.data.ready"
        }
    }

    interface PREFERENCES {
        companion object {
            const val STORED_SONG_POSITION_DURATION = "duration"
            const val STORED_SONG_ID = "title"
            const val SHUFFLE = "shuffle"
            const val REPEAT = "repeat"
            const val PREV_ACTION = "prev_action"
        }
    }

    interface PREFERENCE_VALUES {
        companion object {
            const val NO_REPEAT = 0
            const val REPEAT_ALL = 1
            const val REPEAT_ONE = 2
            const val PREV_ACT_TIME_CONSTANT = 0.1.toFloat()
        }
    }

    interface NOTIFICATION_ID {
        companion object {
            const val INSTANT_LYRICS = 102
            const val FOREGROUND_SERVICE = 101
            const val BATCH_DOWNLOADER = 99
            const val FCM = 98
        }
    }

    interface FRAGMENT_STATUS {
        companion object {
            //to know which fragment is being instantiated
            const val TITLE_FRAGMENT = 0
            const val ARTIST_FRAGMENT = 1
            const val ALBUM_FRAGMENT = 2
            const val GENRE_FRAGMENT = 3
            const val PLAYLIST_FRAGMENT = 4
            const val ALBUM_FRAGMENT_GRID = 5
            const val SECONDARY_LIB_FRAG = 6
        }
    }

    interface ADD_TO_Q {
        companion object {
            //to know which fragment is being instantiated
            const val IMMEDIATE_NEXT = 0
            const val AT_LAST = 1
        }
    }

    interface SORT_ORDER {
        companion object {
            const val ASC = 0
            const val DESC = 1
        }
    }

    interface SYSTEM_PLAYLISTS {
        companion object {
            const val MOST_PLAYED = "Most_Played"
            const val RECENTLY_PLAYED = "Recently_Played"
            const val RECENTLY_ADDED = "Recently_Added"
            const val MY_FAV = "My_Fav"
            const val PLAYLIST_LIST = "playlist_list"
            val listOfSystemPlaylist = arrayOf(MOST_PLAYED, RECENTLY_ADDED, RECENTLY_PLAYED, MY_FAV)
            const val RECENTLY_PLAYED_MAX = 50
            const val MOST_PLAYED_MAX = 50
        }
    }

    interface CLICK_ON_NOTIF {
        companion object {
            const val OPEN_LIBRARY_VIEW = 0
            const val OPEN_DISC_VIEW = 1
            const val DO_NOTHING = 2
        }
    }

    interface DISC_SIZE {
        companion object {
            const val SMALL = 4.5f
            const val MEDIUM = 4f
            const val BIG = 3.5f
        }
    }

    interface PRIMARY_COLOR {
        companion object {
            const val DARK = 1
            const val LIGHT = 2
            const val GLOSSY = 3
            const val BLACK = -16119286
        }
    }

    interface TYPEFACE {
        companion object {
            const val MONOSPACE = 0
            const val SOFIA = 1
            const val MANROPE = 2
            const val ASAP = 3
            const val SYSTEM_DEFAULT = 4
            const val ROBOTO = 14 /*int ACME = 5;
        int ACLONICA = 6;
        int CHEERYSWASH = 7;
        int CORBEN = 8;
        int NOVA_R = 9;
        int NOVA_S = 10;
        int PACFITO = 11;
        int PURPLEPURSE = 12;
        int QUATICO = 13;
        int ROBOTO = 14;
        int ROBOTO_C = 15;
        int ROBOTO_M = 16;
        int TRADE_WINDS = 17;
        int UBUNTU = 18;

        int CONCERT_ONCE = 19;
        int LATO = 20;
        int LATO_ITALIC = 21;
        int LORA = 22;
        int MONTESERRAT = 23;
        int OPEN_SANS_LIGHT = 24;
        int OSWALD = 25;
        int PROMPT = 26;
        int PROMPT_MEDIUM = 27;
        int PT_SANS_CAPTION = 28;
        int RALEWAY = 29;
        int SLABO = 30;
        int SOURCE_SANS_PRO = 31;*/
        }
    }

    interface PREF_LAUNCHED_FROM {
        companion object {
            const val MAIN = 0
            const val NOW_PLAYING = 1
            const val DRAWER = 2
        }
    }

    interface SHAKE_ACTIONS {
        companion object {
            const val PLAY_PAUSE = 0
            const val NEXT = 1
            const val PREVIOUS = 2
        }
    }

    interface DONATE {
        companion object {
            const val COFFEE = 0
            const val BEER = 1
            const val JD = 2
        }
    }

    interface TABS {
        companion object {
            const val ALBUMS = 0
            const val TRACKS = 1
            const val ARTIST = 2
            const val GENRE = 3
            const val PLAYLIST = 4
            const val FOLDER = 5
            const val NUMBER_OF_TABS = 6
            const val DEFAULT_SEQ = "0,1,2,3,4,5"
        }
    }

    interface SORT_BY {
        companion object {
            const val NAME = 0
            const val YEAR = 1
            const val NO_OF_ALBUMS = 2
            const val NO_OF_TRACKS = 3
            const val ASC = 4
            const val DESC = 5
            const val SIZE = 6
            const val DURATION = 7
        }
    }

    interface TAG_EDITOR_LAUNCHED_FROM {
        companion object {
            const val MAIN_LIB = 0
            const val SECONDARY_LIB = 1
            const val NOW_PLAYING = 2
        }
    }

    interface EXIT_NOW_PLAYING_AT {
        companion object {
            const val DISC_FRAG = 1
            const val LYRICS_FRAG = 2
            const val ARTIST_FRAG = 0
        }
    }

    interface FIRST_TIME_INFO {
        companion object {
            const val MUSIC_LOCK = 0
            const val SORTING = 1
            const val MINI_PLAYER = 2
            const val FAV = 3
            const val CURRENT_QUEUE = 4
            const val SWIPE_RIGHT = 5
        }
    }
}