package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Keys
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Net
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
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
class DownloadArtInfoThread constructor(
    callback: ArtistInfo.Callback,
    artist: String,
    item: TrackItem?
) : Thread(
    getRunnable(callback, artist.trim { it <= ' ' }, item)) {
    companion object {
        private val API_ROOT_URL: String = "http://ws.audioscrobbler.com/2.0"
        private val API_ROOT_URL_SPOTI: String = "https://api.spotify.com/v1/search"
        private val FORMAT_STRING: String =
            "/?method=artist.getinfo&artist=%s&autocorrect=1&api_key=%s&format=json"
        private val FORMAT_STRING_SPOTI: String = "/?q=%s&type=artist"
        private fun getRunnable(
            callback: ArtistInfo.Callback,
            artist: String,
            item: TrackItem?
        ): Runnable {
            return object : Runnable {
                override fun run() {
                    val artistInfo = ArtistInfo(artist)
                    val url: String =
                        java.lang.String.format(API_ROOT_URL + FORMAT_STRING, artist, Keys.LASTFM)
                    var response: JsonObject?
                    try {
                        val queryURL: URL = URL(url)
                        val connection: Connection = Jsoup.connect(queryURL.toExternalForm())
                            .header("Authorization", "Bearer " + Keys.LASTFM)
                            .timeout(10000) //10 seconds timeout
                            .ignoreContentType(true)
                        val document: Document = connection.userAgent(Net.USER_AGENT).get()
                        response = JsonParser().parse(document.text()).asJsonObject
                        if (response != null) {
                            val content: String =
                                response.getAsJsonObject("artist").getAsJsonObject("bio")
                                    .get("content").asString

                            //JsonArray imageUrlArray = response.getAsJsonObject("artist").getAsJsonArray("image");
                            //String imageUrl = imageUrlArray.get(3).getAsJsonObject().get("#text").getAsString();
                            val artistUrl: String =
                                response.getAsJsonObject("artist").get("url").asString
                            artistInfo.setImageUrl(getImageUrl())
                            artistInfo.setArtistContent(content)
                            artistInfo.setArtistUrl(artistUrl)
                            if (item != null) {
                                artistInfo.setOriginalArtist(item.getArtist())
                            }
                            artistInfo.setCorrectedArtist(response.getAsJsonObject("artist")
                                .get("name").asString)
                            if (content != "") {
                                artistInfo.setFlag(ArtistInfo.POSITIVE)
                            }
                        }
                    } catch (e: IOException) {
                        artistInfo.setArtistContent("Request timed out, check your connection and try again later!")
                    } catch (e: Exception) {
                        Log.v(Constants.TAG, e.toString())
                    }
                    threadMsg(artistInfo)
                }

                @SuppressLint("ApplySharedPref")
                private fun getImageUrl(): String {
                    var imageUrl: String? = null
                    var access_token = ""
                    try {
                        val time: Long = MyApp.getPref()!!.getLong("spoty_expiry_time", 0)
                        val diff: Long = System.currentTimeMillis() - time
                        Log.d("ArtInfoThread", "getImageUrl: difference $diff")
                        val i = if (diff / 1000 < 3600) {
                            access_token = MyApp.getPref()!!.getString("spoty_token", "")!!
                            Log.d("ArtInfoThread",
                                "getImageUrl: Access token from cache $access_token")
                        } else {
                            val queryURL: URL = URL("https://accounts.spotify.com/api/token")
                            val connection: Connection = Jsoup.connect(queryURL.toExternalForm())
                                .header("Authorization",
                                    "Basic NmQ1MGI5OGZkMWNmNGI0NThmMGZhNzhiNzM4YzU1MzA6MzI2NjRjODE5OTBkNDk1ZTgzNzY5Y2VmYmQ1YWM1ZGI=")
                                .timeout(10000) //10 seconds timeout
                                .ignoreContentType(true)
                            val document: Document = connection.userAgent(Net.USER_AGENT)
                                .data("grant_type", "client_credentials").post()
                            val response: JsonObject? =
                                JsonParser().parse(document.text()).asJsonObject
                            if (response != null && response.has("access_token")) {
                                val token: String = response.get("access_token").asString
                                MyApp.getPref()!!.edit()
                                    .putLong("spoty_expiry_time", System.currentTimeMillis())
                                    .commit()
                                MyApp.getPref()!!.edit().putString("spoty_token", token)
                                    .commit()
                                access_token = token
                            }
                            Log.d("ArtInfoThread",
                                "getImageUrl: Access token from internet $access_token")
                        }
                        val url: String =
                            String.format(API_ROOT_URL_SPOTI + FORMAT_STRING_SPOTI, artist)
                        val response: JsonObject?
                        val queryURL: URL = URL(url)
                        val connection: Connection = Jsoup.connect(queryURL.toExternalForm())
                            .header("Authorization", "Bearer " + access_token)
                            .timeout(10000) //10 seconds timeout
                            .ignoreContentType(true)
                        val document: Document = connection.userAgent(Net.USER_AGENT).get()
                        response = JsonParser().parse(document.text()).asJsonObject
                        if (response != null && response.has("artists")) {
                            val res: JsonArray =
                                response.getAsJsonObject("artists").getAsJsonArray("items")
                            if (res.size() > 0) {
                                imageUrl =
                                    res.get(0).asJsonObject.getAsJsonArray("images").get(0)
                                        .asJsonObject.get("url").asString
                            }
                        }
                    } catch (e: IOException) {
                        Log.d(Constants.TAG, "getArtistUrl: io exception")
                    } catch (e: Exception) {
                        Log.v(Constants.TAG, e.toString())
                    }
                    return if (imageUrl == null) "" else imageUrl
                }

                private fun threadMsg(artistInfo: ArtistInfo?) {
                    if (artistInfo != null) {

                        //put in db
                        if (item != null && artistInfo.getFlag() === ArtistInfo.POSITIVE) {
                            OfflineStorageArtistBio.putArtistBioToDB(artistInfo, item)
                        }
                        val msgObj: Message = handler.obtainMessage()
                        val b: Bundle = Bundle()
                        b.putSerializable("artist_info", artistInfo)
                        msgObj.data = b
                        handler.sendMessage(msgObj)
                    }
                }

                // Define the Handler that receives messages from the thread and update the progress
                private val handler: Handler = object : Handler(Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        val result: ArtistInfo? =
                            msg.data.getSerializable("artist_info") as ArtistInfo?
                        if (result != null) callback.onArtInfoDownloaded(result)
                    }
                }
            }
        }
    }
}