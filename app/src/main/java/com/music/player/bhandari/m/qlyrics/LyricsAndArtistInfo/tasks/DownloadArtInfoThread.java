package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Keys;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Net;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;

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

public class DownloadArtInfoThread extends Thread {

    private static String API_ROOT_URL = "http://ws.audioscrobbler.com/2.0";
    private static String API_ROOT_URL_SPOTI = "https://api.spotify.com/v1/search";

    private static String FORMAT_STRING = "/?method=artist.getinfo&artist=%s&autocorrect=1&api_key=%s&format=json";
    private static String FORMAT_STRING_SPOTI = "/?q=%s&type=artist";


    public DownloadArtInfoThread(ArtistInfo.Callback callback, final String artist, TrackItem item) {
        super(DownloadArtInfoThread.getRunnable(callback, artist.trim(), item));
    }

    private static Runnable getRunnable(final ArtistInfo.Callback callback, final String artist, final TrackItem item) {
        return new Runnable() {
            @Override
            public void run() {
                final ArtistInfo artistInfo = new ArtistInfo(artist);

                String url = String.format(API_ROOT_URL + FORMAT_STRING, artist, Keys.LASTFM);
                JsonObject response = null;
                try {

                    URL queryURL = new URL(url);
                    Connection connection = Jsoup.connect(queryURL.toExternalForm())
                            .header("Authorization", "Bearer " + Keys.LASTFM)
                            .timeout(10000) //10 seconds timeout
                            .ignoreContentType(true);
                    Document document = connection.userAgent(Net.USER_AGENT).get();
                    response = new JsonParser().parse(document.text()).getAsJsonObject();
                    if (response != null) {

                        String content = response.getAsJsonObject("artist").getAsJsonObject("bio").get("content").getAsString();

                        //JsonArray imageUrlArray = response.getAsJsonObject("artist").getAsJsonArray("image");
                        //String imageUrl = imageUrlArray.get(3).getAsJsonObject().get("#text").getAsString();

                        String artistUrl = response.getAsJsonObject("artist").get("url").getAsString();

                        artistInfo.setImageUrl(getImageUrl());
                        artistInfo.setArtistContent(content);
                        artistInfo.setArtistUrl(artistUrl);

                        if (item != null) {
                            artistInfo.setOriginalArtist(item.getArtist());
                        }
                        artistInfo.setCorrectedArtist(response.getAsJsonObject("artist").get("name").getAsString());
                        if (!content.equals("")) {
                            artistInfo.setFlag(ArtistInfo.POSITIVE);
                        }
                    }
                } catch (IOException e) {
                    artistInfo.setArtistContent("Request timed out, check your connection and try again later!");
                } catch (Exception e) {
                    Log.v(Constants.TAG, e.toString());
                }
                threadMsg(artistInfo);
            }

            @SuppressLint("ApplySharedPref")
            private String getImageUrl() {
                String imageUrl = null;
                String access_token = "";
                try {
                    long time = MyApp.getPref().getLong("spoty_expiry_time", 0);
                    long diff = System.currentTimeMillis() - time;
                    Log.d("ArtInfoThread", "getImageUrl: difference " + diff);
                    if (diff / 1000 < 3600) {
                        access_token = MyApp.getPref().getString("spoty_token", "");
                        Log.d("ArtInfoThread", "getImageUrl: Access token from cache " + access_token);
                    } else {
                        URL queryURL = new URL("https://accounts.spotify.com/api/token");
                        Connection connection = Jsoup.connect(queryURL.toExternalForm())
                                .header("Authorization", "Basic NmQ1MGI5OGZkMWNmNGI0NThmMGZhNzhiNzM4YzU1MzA6MzI2NjRjODE5OTBkNDk1ZTgzNzY5Y2VmYmQ1YWM1ZGI=")
                                .timeout(10000) //10 seconds timeout
                                .ignoreContentType(true);
                        Document document = connection.userAgent(Net.USER_AGENT).data("grant_type", "client_credentials").post();
                        JsonObject response = new JsonParser().parse(document.text()).getAsJsonObject();
                        if (response != null && response.has("access_token")) {
                            String token = response.get("access_token").getAsString();
                            MyApp.getPref().edit().putLong("spoty_expiry_time", System.currentTimeMillis()).commit();
                            MyApp.getPref().edit().putString("spoty_token", token).commit();
                            access_token = token;
                        }

                        Log.d("ArtInfoThread", "getImageUrl: Access token from internet " + access_token);
                    }

                    String url = String.format(API_ROOT_URL_SPOTI + FORMAT_STRING_SPOTI, artist);
                    JsonObject response;

                    URL queryURL = new URL(url);
                    Connection connection = Jsoup.connect(queryURL.toExternalForm())
                            .header("Authorization", "Bearer " + access_token)
                            .timeout(10000) //10 seconds timeout
                            .ignoreContentType(true);
                    Document document = connection.userAgent(Net.USER_AGENT).get();
                    response = new JsonParser().parse(document.text()).getAsJsonObject();
                    if (response != null && response.has("artists")) {

                        JsonArray res = response.getAsJsonObject("artists").getAsJsonArray("items");
                        if (res.size() > 0) {
                            imageUrl = res.get(0).getAsJsonObject().getAsJsonArray("images").get(0).getAsJsonObject().get("url").getAsString();
                        }
                    }

                } catch (IOException e) {
                    Log.d(Constants.TAG, "getArtistUrl: io exception");
                } catch (Exception e) {
                    Log.v(Constants.TAG, e.toString());
                }

                return imageUrl == null ? "" : imageUrl;
            }

            private void threadMsg(ArtistInfo artistInfo) {
                if (artistInfo != null) {

                    //put in db
                    if (item != null && artistInfo.getFlag() == ArtistInfo.POSITIVE) {
                        OfflineStorageArtistBio.putArtistBioToDB(artistInfo, item);
                    }

                    Message msgObj = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putSerializable("artist_info", artistInfo);
                    msgObj.setData(b);
                    handler.sendMessage(msgObj);
                }
            }

            // Define the Handler that receives messages from the thread and update the progress
            private final Handler handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    ArtistInfo result = (ArtistInfo) msg.getData().getSerializable("artist_info");
                    if (result != null)
                        callback.onArtInfoDownloaded(result);
                }
            };

        };
    }

}
