package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.model.dataItem;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amit AB AB on 1/12/2018.
 */
//this thread is started from Music library once all artists are loaded

public class BulkArtInfoGrabber extends Thread {

    //make sure only one instance of this thread runs at time
    private static boolean artInfoGrabberThreadRunning = false;

    //makes sure artist info is downloaded one after another
    private static boolean artistInfoThreadRunning = false;

    //if thread runs more than HALF HOUR, kill it
    private static long THREAD_TIMEOUT = 30 * 60 * 1000;

    public BulkArtInfoGrabber(){
        super(new Runnable() {
            @Override
            public void run() {
                if(!UtilityFun.isConnectedToInternet() || artInfoGrabberThreadRunning){
                    return;
                }

                artInfoGrabberThreadRunning = true;
                long start_time = System.currentTimeMillis();

                try {
                    List<dataItem> itemsCopy = new ArrayList<>(MusicLibrary.getInstance().getDataItemsArtist());
                    for (dataItem item : itemsCopy) {
                        Log.d("BulkArtInfoGrabber", "run: " + item.artist_name);
                        TrackItem trackItem = new TrackItem();
                        final String artist = UtilityFun.filterArtistString(item.artist_name);
                        trackItem.setArtist(item.artist_name);
                        trackItem.setArtist_id(item.artist_id);

                        ArtistInfo artistInfo = OfflineStorageArtistBio.getArtistBioFromTrackItem(trackItem);
                        if (artistInfo != null && artistInfo.getFlag() == ArtistInfo.POSITIVE) {
                            Log.d("BulkArtInfoGrabber", "run: found in db");
                            continue;
                        }

                        new DownloadArtInfoThread(new ArtistInfo.Callback() {
                            @Override
                            public void onArtInfoDownloaded(ArtistInfo artistInfo) {
                                artistInfoThreadRunning = false;
                                MusicLibrary.getInstance().putEntryInArtistUrl(artistInfo.getOriginalArtist(), artistInfo.getArtistUrl());
                                if (artistInfo.getFlag() == ArtistInfo.POSITIVE) {
                                    MusicLibrary.getInstance().putEntryInArtistUrl(artistInfo.getOriginalArtist(), artistInfo.getImageUrl());
                                    Log.d("BulkArtInfoGrabber", "onArtInfoDownloaded: found " + artistInfo.getCorrectedArtist());
                                } else {
                                    Log.d("BulkArtInfoGrabber", "onArtInfoDownloaded: not found" + artistInfo.getOriginalArtist());
                                }
                            }
                        }, artist, trackItem).start();

                        artistInfoThreadRunning = true;

                        //While one artist info is being downloaded, wait
                        //exit thread if time exceeds half hour
                        while (artistInfoThreadRunning && (System.currentTimeMillis() - start_time) < THREAD_TIMEOUT) {
                            try {
                                Thread.sleep(1200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //update the music library artist urls
                    //MusicLibrary.getInstance().updateArtistInfo();

                    Log.d("BulkArtInfoGrabber", "run: Artist info local caching complete");

                    //Toast.makeText(MyApp.getContext(), R.string.toast_artist_cache_complete, Toast.LENGTH_SHORT).show();

                    MyApp.getPref().edit().putLong(MyApp.getContext().getString(R.string.pref_artinfo_libload), System.currentTimeMillis()).apply();

                    artInfoGrabberThreadRunning = false;
                }catch (Exception e){
                    e.printStackTrace();
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Error_art_info_grab" );
                    UtilityFun.logEvent(bundle);
                }
            }
        });
    }
}
