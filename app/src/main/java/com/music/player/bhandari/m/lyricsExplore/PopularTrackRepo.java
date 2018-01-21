package com.music.player.bhandari.m.lyricsExplore;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Keys;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Net;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abami on 12/6/2017.
 */

public class PopularTrackRepo  {

    private static String API_ROOT_URL="http://ws.audioscrobbler.com/2.0";
    private static String GEO_TOP_TRACKS_FORMAT_STRING ="/?method=geo.gettoptracks&country=%s&api_key=%s&format=json&limit=100";
    private static String GLOBAL_TOP_TRACKS_FORMAT_STRING ="/?method=chart.gettoptracks&api_key=%s&format=json&limit=100";

    public void fetchPopularTracks(String country, OnPopularTracksReady callback, Boolean lookInCache){
        new LastFM(country, callback, lookInCache).start();
    }

    static class LastFM extends Thread {

        LastFM(String country, OnPopularTracksReady callback, Boolean lookInCache){
            super(getRunnable(country, callback,lookInCache));
        }

        private static Runnable getRunnable(final String country, final OnPopularTracksReady callback, final Boolean lookInCache) {
            return new Runnable() {

                private final static int DAYS_UNTIL_OFFLINE_STALE = 1;

                @Override
                public void run() {

                    //check offline content first
                    if(lookInCache) {
                        List<Track> trackList = getTrackListOffline();
                        if (trackList != null && trackList.size() != 0) {
                            if (callback != null) callback.popularTracksReady(trackList, country);
                            return;
                        }
                    }

                    if(country==null || country.equals("")){
                        getGlobalTopTracks();
                        return;
                    }

                    String url = String.format(API_ROOT_URL + GEO_TOP_TRACKS_FORMAT_STRING,country, Keys.LASTFM);
                    JsonObject response=null;
                    try {
                        URL queryURL = new URL(url);
                        Connection connection = Jsoup.connect(queryURL.toExternalForm())
                                .header("Authorization", "Bearer " + Keys.LASTFM)
                                .timeout(15000) //10 seconds timeout
                                .ignoreContentType(true);
                        Document document = connection.userAgent(Net.USER_AGENT).get();
                        response = new JsonParser().parse(document.text()).getAsJsonObject();

                        List<Track> tracks = new ArrayList<>();
                        if(response!=null){
                            JsonArray arrJson = response.getAsJsonObject("tracks").getAsJsonArray("track");
                            for(JsonElement element:arrJson){
                                String imgString = element.getAsJsonObject().get("image").getAsJsonArray().get(2).getAsJsonObject().get("#text").getAsString();
                                int playCount = 0;
                                try {
                                    playCount = Integer.valueOf(element.getAsJsonObject().get("listeners").getAsString());
                                }catch (NumberFormatException ignored){
                                    continue;
                                }
                                tracks.add(new Track(element.getAsJsonObject().get("name").getAsString()
                                                ,element.getAsJsonObject().get("artist").getAsJsonObject().get("name").getAsString()
                                                ,playCount
                                                ,imgString)
                                        );
                            }
                            if(tracks.size()!=0) storeTrackListOffline(tracks);
                            if(callback!=null) callback.popularTracksReady(tracks, country);
                            Log.d("LastFM", "run: " + response.toString());
                        }else {
                            getGlobalTopTracks();
                        }
                    } catch (Exception e){
                        getGlobalTopTracks();
                        Log.v(Constants.TAG,e.toString());
                    }
                }

                private void getGlobalTopTracks(){
                    String url = String.format(API_ROOT_URL + GLOBAL_TOP_TRACKS_FORMAT_STRING, Keys.LASTFM);
                    JsonObject response=null;
                    try {
                        URL queryURL = new URL(url);
                        Connection connection = Jsoup.connect(queryURL.toExternalForm())
                                .header("Authorization", "Bearer " + Keys.LASTFM)
                                .timeout(15000) //10 seconds timeout
                                .ignoreContentType(true);
                        Document document = connection.userAgent(Net.USER_AGENT).get();
                        response = new JsonParser().parse(document.text()).getAsJsonObject();
                        List<Track> tracks = new ArrayList<>();
                        if(response!=null){
                            JsonArray arrJson = response.getAsJsonObject("tracks").getAsJsonArray("track");
                            for(JsonElement element:arrJson){
                                String imgString = element.getAsJsonObject().get("image").getAsJsonArray().get(2).getAsJsonObject().get("#text").getAsString();
                                int playCount = Integer.valueOf(element.getAsJsonObject().get("listeners").getAsString());
                                tracks.add(new Track(element.getAsJsonObject().get("name").getAsString()
                                        ,element.getAsJsonObject().get("artist").getAsJsonObject().get("name").getAsString()
                                        ,playCount
                                        ,imgString));
                            }
                            if(tracks.size()!=0) storeTrackListOffline(tracks);
                            if(callback!=null) callback.popularTracksReady(tracks, "World");
                            Log.d("LastFM", "run: " + response.toString());
                        }else {
                            if(callback!=null) callback.error();
                        }
                    } catch (Exception e){
                        if(callback!=null) callback.error();
                        Log.v(Constants.TAG,e.toString());
                    }
                }

                private void storeTrackListOffline(List<Track> tracks){
                    SharedPreferences prefs = MyApp.getContext().getSharedPreferences("explore_track_list", 0);
                    String gsonString = new Gson().toJson(tracks);
                    prefs.edit().putString("track_list",gsonString).apply();
                    prefs.edit().putLong("time",System.currentTimeMillis()).apply();
                }

                private List<Track> getTrackListOffline(){
                    SharedPreferences prefs = MyApp.getContext().getSharedPreferences("explore_track_list", 0);
                    String gsonString = prefs.getString("track_list","");
                    long time = prefs.getLong("time",-1);
                    if(gsonString.equals("") || time==-1) return null;

                    if (System.currentTimeMillis() >= time +
                            (DAYS_UNTIL_OFFLINE_STALE * 24 * 60 * 60 * 1000)) {
                        return null;
                    }

                    Type listType = new TypeToken<List<Track>>() {}.getType();
                    return new Gson().fromJson(gsonString, listType);
                }
            };
        }
    }

}
