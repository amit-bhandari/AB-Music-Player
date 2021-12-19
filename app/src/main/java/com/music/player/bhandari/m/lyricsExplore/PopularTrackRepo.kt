package com.music.player.bhandari.m.lyricsExplore

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Keys
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Net
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.lang.reflect.Type
import java.net.URL

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
class PopularTrackRepo {
    fun fetchPopularTracks(
        country: String,
        callback: OnPopularTracksReady?,
        lookInCache: Boolean
    ) {
        LastFM(country, callback, lookInCache).start()
    }

    internal class LastFM(
        country: String,
        callback: OnPopularTracksReady?,
        lookInCache: Boolean
    ) : Thread(
        getRunnable(country, callback, lookInCache)) {
        companion object {
            private fun getRunnable(
                country: String,
                callback: OnPopularTracksReady?,
                lookInCache: Boolean
            ): Runnable {
                return object : Runnable {
                    val DAYS_UNTIL_OFFLINE_STALE = 1
                    override fun run() {

                        //check offline content first
                        if (lookInCache) {
                            val trackList: List<Track>? =
                                trackListOffline
                            if (trackList != null && trackList.isNotEmpty()) {
                                callback?.popularTracksReady(trackList,
                                    country)
                                return
                            }
                        }
                        if (country == "") {
                            globalTopTracks
                            return
                        }
                        val url =
                            java.lang.String.format(API_ROOT_URL + GEO_TOP_TRACKS_FORMAT_STRING,
                                country,
                                Keys.LASTFM)
                        var response: JsonObject? = null
                        try {
                            val queryURL = URL(url)
                            val connection: Connection = Jsoup.connect(queryURL.toExternalForm())
                                .header("Authorization", "Bearer " + Keys.LASTFM)
                                .timeout(15000) //10 seconds timeout
                                .ignoreContentType(true)
                            val document = connection.userAgent(Net.USER_AGENT).get()
                            response = JsonParser().parse(document.text()).asJsonObject
                            val tracks: MutableList<Track> =
                                ArrayList<Track>()
                            if (response != null) {
                                val arrJson =
                                    response.getAsJsonObject("tracks").getAsJsonArray("track")
                                for (element in arrJson) {
                                    val imgString: String =
                                        element.asJsonObject.get("image").asJsonArray
                                            .get(2).asJsonObject.get("#text").asString
                                    var playCount = 0
                                    try {
                                        playCount = Integer.valueOf(element.asJsonObject
                                            .get("listeners").asString)
                                    } catch (ignored: NumberFormatException) {
                                        continue
                                    }
                                    tracks.add(Track(
                                        element.asJsonObject.get("name").asString,
                                        element.asJsonObject.get("artist").asJsonObject
                                            .get("name").asString,
                                        playCount,
                                        imgString)
                                    )
                                }
                                if (tracks.size != 0) storeTrackListOffline(tracks)
                                callback?.popularTracksReady(tracks, country)
                                Log.d("LastFM", "run: $response")
                            } else {
                                globalTopTracks
                            }
                        } catch (e: Exception) {
                            globalTopTracks
                            Log.v(Constants.TAG, e.toString())
                        }
                    }

                    //10 seconds timeout
                    private val globalTopTracks: Unit
                        private get() {
                            val url =
                                java.lang.String.format(API_ROOT_URL + GLOBAL_TOP_TRACKS_FORMAT_STRING,
                                    Keys.LASTFM)
                            var response: JsonObject? = null
                            try {
                                val queryURL = URL(url)
                                val connection: Connection =
                                    Jsoup.connect(queryURL.toExternalForm())
                                        .header("Authorization", "Bearer " + Keys.LASTFM)
                                        .timeout(15000) //10 seconds timeout
                                        .ignoreContentType(true)
                                val document = connection.userAgent(Net.USER_AGENT).get()
                                response = JsonParser().parse(document.text()).asJsonObject
                                val tracks: MutableList<Track> =
                                    ArrayList<Track>()
                                if (response != null) {
                                    val arrJson =
                                        response!!.getAsJsonObject("tracks").getAsJsonArray("track")
                                    for (element in arrJson) {
                                        val imgString: String =
                                            element.asJsonObject.get("image").asJsonArray
                                                .get(2).asJsonObject.get("#text").asString
                                        val playCount: Int =
                                            Integer.valueOf(element.asJsonObject
                                                .get("listeners").asString)
                                        tracks.add(Track(
                                            element.asJsonObject.get("name").asString,
                                            element.asJsonObject.get("artist")
                                                .asJsonObject.get("name").asString,
                                            playCount,
                                            imgString))
                                    }
                                    if (tracks.size != 0) storeTrackListOffline(tracks)
                                    callback?.popularTracksReady(tracks,
                                        "World")
                                    Log.d("LastFM", "run: " + response.toString())
                                } else {
                                    callback?.error()
                                }
                            } catch (e: Exception) {
                                callback?.error()
                                Log.v(Constants.TAG, e.toString())
                            }
                        }

                    private fun storeTrackListOffline(tracks: List<Track>) {
                        val prefs =
                            MyApp.getContext()!!.getSharedPreferences("explore_track_list", 0)
                        val gsonString: String = Gson().toJson(tracks)
                        prefs.edit().putString("track_list", gsonString).apply()
                        prefs.edit().putLong("time", System.currentTimeMillis()).apply()
                    }

                    private val trackListOffline: List<Track>?
                        get() {
                            val prefs =
                                MyApp.getContext()!!.getSharedPreferences("explore_track_list", 0)
                            val gsonString = prefs.getString("track_list", "")
                            val time: Long = prefs.getLong("time", -1)
                            if (gsonString == "" || time == -1L) return null
                            if (System.currentTimeMillis() >= time +
                                DAYS_UNTIL_OFFLINE_STALE * 24 * 60 * 60 * 1000
                            ) {
                                return null
                            }
                            val listType: Type = object :
                                TypeToken<List<Track?>?>() {}.type
                            return Gson().fromJson(gsonString, listType)
                        }
                }
            }
        }
    }

    companion object {
        private const val API_ROOT_URL = "http://ws.audioscrobbler.com/2.0"
        private const val GEO_TOP_TRACKS_FORMAT_STRING =
            "/?method=geo.gettoptracks&country=%s&api_key=%s&format=json&limit=100"
        private const val GLOBAL_TOP_TRACKS_FORMAT_STRING =
            "/?method=chart.gettoptracks&api_key=%s&format=json&limit=100"
    }
}