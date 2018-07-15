package com.music.player.bhandari.m.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.dataItem;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadLyricThread;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.rewards.RewardPoints;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.util.ArrayList;

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

public class BatchDownloaderService extends Service implements  Lyrics.Callback {

    Handler mHandler;
    NotificationManager mNotificationManager;
    PendingIntent clickOnNotif;
    BroadcastReceiver mReceiver;
    NotificationCompat.Builder mBuilder;
    boolean subtitleDownloadThreadRunning = false;
    boolean cancelBatchService = true;

    private final int FINISHED =1 ;
    private final int CANCELLED =2;
    private final int CONNECTION_ERROR=3;
    private final int UNKNOWN =4;
    int finishStatus = UNKNOWN;

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyApp.isBatchServiceRunning=false;
        stopForeground(false);

        switch (finishStatus){
            case FINISHED:
                mBuilder.setContentTitle(getString(R.string.batch_download_finished));
                mBuilder.setContentText(" ");
                mBuilder.setOngoing(false);
                mNotificationManager.notify(Constants.NOTIFICATION_ID.BATCH_DOWNLOADER, mBuilder.build());

                try {
                    int size = MusicLibrary.getInstance().getDataItemsForTracks().size();
                    Bundle bundle = new Bundle();
                    bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, 2);
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "batch_download_finished " + size);
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "batch_download");
                    //Logs an app event.
                    FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                }catch (Exception ignored){

                }
                break;

            case CANCELLED:
                mBuilder.setOngoing(false);
                mNotificationManager.cancel(Constants.NOTIFICATION_ID.BATCH_DOWNLOADER);
                break;

            case UNKNOWN:
            case CONNECTION_ERROR:
                mBuilder.setOngoing(false);
                mNotificationManager.notify(Constants.NOTIFICATION_ID.BATCH_DOWNLOADER, mBuilder.build());
                break;
        }

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null && intent.getAction()!=null) {
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyApp.isBatchServiceRunning = true;

        initializeReceiver();
        initializeIntents();

        mHandler = new Handler();
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this, "channel_01");
        mBuilder.setContentTitle(getString(R.string.batch_download_not_title))
                .setContentText(getString(R.string.batch_download_starting))
                .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                .setOngoing(true)
                .setContentIntent(clickOnNotif);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    /* Create or update. */
            /*NotificationChannel channel = new NotificationChannel("channel_01",
                    "Playback Notification",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(null, null);
            mNotificationManager.createNotificationChannel(channel);*/
            mBuilder.setChannelId("channel_01");
        }

        startForeground(Constants.NOTIFICATION_ID.BATCH_DOWNLOADER,mBuilder.build());

        cancelBatchService = false;
        //start running thread
        runThread();
    }

    private void runThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<dataItem> dataItems = new ArrayList<>(MusicLibrary.getInstance().getDataItemsForTracks().values());
                int size = dataItems.size();
                for(int i = 0; i<size;i++){
                    dataItem currentItem;
                    try {
                        currentItem = dataItems.get(i);
                    }catch (IndexOutOfBoundsException e){
                        finishStatus = CONNECTION_ERROR;
                        continue;
                    }
                    mBuilder.setProgress(size, i+1, false);
                    // Displays the progress bar for the first time.
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(currentItem.title)
                            .append(" ").append("(").append(i).append("/").append(size).append(")");

                    mBuilder.setContentText(stringBuilder);

                    mNotificationManager.notify(Constants.NOTIFICATION_ID.BATCH_DOWNLOADER, mBuilder.build());
                    finishStatus = FINISHED;

                    //check if reward points available, if not, skip
                    if(!UtilityFun.isAdsRemoved() && RewardPoints.getRewardPointsCount()<=0){
                        continue;
                    }

                    //check if current song present in db
                    if(OfflineStorageLyrics.isLyricsPresentInDB(currentItem.id)){
                        continue;
                    }



                    if (currentItem.artist_name != null && currentItem.title != null) {
                        new DownloadLyricThread(BatchDownloaderService.this, true,
                                MusicLibrary.getInstance().getTrackItemFromId(currentItem.id)
                                , currentItem.artist_name, currentItem.title).start();
                    }

                    subtitleDownloadThreadRunning = true;
                    while (subtitleDownloadThreadRunning) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(!UtilityFun.isConnectedToInternet()){
                            cancelBatchService=true;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),getString(R.string.error_no_internet),Toast.LENGTH_SHORT).show();
                                    finishStatus = CONNECTION_ERROR;
                                    mBuilder.setContentText(getString(R.string.error_no_connection));
                                    stopForeground(false);
                                    mNotificationManager.notify(Constants.NOTIFICATION_ID.BATCH_DOWNLOADER, mBuilder.build());
                                }
                            });
                            stopSelf();
                            break;
                        }
                    }

                    if(cancelBatchService){
                        break;
                    }
                    Log.v(Constants.TAG,"Task done for "+currentItem.title);
                }

                stopSelf();
            }
        }).start();
    }

    private void initializeReceiver(){
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.getAction()==null) return;
                switch (intent.getAction()) {
                    case Constants.ACTION.CLICK_TO_CANCEL:
                        cancelBatchService = true;
                        finishStatus = CANCELLED;
                        stopSelf();
                        break;
                }
            }
        };

        IntentFilter intentFilter =  new IntentFilter();

        intentFilter.addAction(Constants.ACTION.CLICK_TO_CANCEL);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, intentFilter);
    }

    private void initializeIntents(){
        Intent notificationIntent;
        notificationIntent = new Intent(this, BatchDownloaderService.class);
        notificationIntent.setAction(Constants.ACTION.CLICK_TO_CANCEL);
        clickOnNotif = PendingIntent.getService(this, 0,
                notificationIntent, 0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLyricsDownloaded(Lyrics lyrics) {
        if(!UtilityFun.isAdsRemoved() && lyrics.getFlag()==Lyrics.POSITIVE_RESULT){
            RewardPoints.decrementByOne();
        }
        subtitleDownloadThreadRunning = false;
    }
}
