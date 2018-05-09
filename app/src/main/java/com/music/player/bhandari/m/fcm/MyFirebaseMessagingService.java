package com.music.player.bhandari.m.fcm;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.activity.ActivityExploreLyrics;
import com.music.player.bhandari.m.activity.ActivityLyricView;
import com.music.player.bhandari.m.activity.ActivityRemoveAds;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.utils.AppLaunchCountManager;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Random;

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

public class MyFirebaseMessagingService extends FirebaseMessagingService{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("MyFirebaseMessaging", "onMessageReceived: " + remoteMessage.toString());

        final Map<String,String> map = remoteMessage.getData();
        for(String keys: map.keySet()){
            Log.d("MyFirebaseMessaging", "onMessageReceived: " + keys);
        }

        //if opt out of notifications in settings, this part should never be executed
        //in case it does, ignore the message
        if(!MyApp.getPref().getBoolean(getString(R.string.pref_notifications), true)){
            return;
        }

        //do not show notification to users with ads removed
        if(map.get("type").equals("discount") && UtilityFun.isAdsRemoved()){
            return;
        }

        if(map.get("type").equals("review") && (!AppLaunchCountManager.isEligibleForRatingAsk()
                || MyApp.getPref().getBoolean(getString(R.string.pref_already_rated), false))){
            return;
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                new generatePictureStyleNotification(map).execute();
            }
        });
    }

    public static class generatePictureStyleNotification extends AsyncTask<String, Void, Bitmap> {

        static final private String ALREADY_RATED = "already_rated";
        static final private String RATE_NOW = "rate_now";

        private NotificationCompat.Builder builder;
        private Map<String, String> map;

        generatePictureStyleNotification(Map<String, String> map) {
            super();
            this.map = map;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            InputStream in;
            try {
                URL url = new URL(map.get("image_link"));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                return BitmapFactory.decodeStream(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            try {
                String contentTitle = map.get("title");
                String contentText = map.get("body");
                String subText = map.get("subtitle");

                builder = new NotificationCompat.Builder(MyApp.getContext(), MyApp.getContext().getString(R.string.notification_channel))
                        .setColor(ColorHelper.getColor(R.color.notification_color))
                        .setSmallIcon(R.drawable.ic_batman_kitkat)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setSubText(subText)
                        .setAutoCancel(true)
                        .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(result).setSummaryText(contentText));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.setVisibility(Notification.VISIBILITY_PUBLIC);
                }

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    builder.setPriority(Notification.PRIORITY_MAX);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setChannelId(MyApp.getContext().getString(R.string.notification_channel));
                }

                //build notification builder object
                switch (map.get("type")) {
                    case "discount":
                        discountNotif();
                        break;

                    case "trending_tracks":
                        trending_tracksNotif();
                        break;

                    case "check_out_lyric":
                        Check_out_lyricNotif(map);
                        break;

                    case "unknown":
                        UnknownNotif(map);
                        break;

                    case "search_lyric":
                        searchLyricNotif();
                        break;

                    case "review":
                        reviewNotif(map);
                        break;

                }

                Notification notification = builder.build();

                NotificationManager mNotificationManager =
                        (NotificationManager) MyApp.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

                if (mNotificationManager != null) {
                    mNotificationManager.notify(new Random().nextInt(), notification);
                    try {
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification_displayed");
                        UtilityFun.logEvent(bundle);
                    } catch (Exception ignored) {
                    }
                }
            }catch (Exception e){
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification_crashed_app");
                UtilityFun.logEvent(bundle);
            }
        }

        //helper methods to build specific notifications
        private void discountNotif(){

            Intent notificationIntent = new Intent(MyApp.getContext(), ActivityRemoveAds.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.putExtra("from_notif", true);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(MyApp.getContext(), 0,
                    notificationIntent, 0);

            builder.setContentIntent(contentIntent);

        }

        private void trending_tracksNotif(){

            int requestCode = new Random().nextInt();

            Intent notificationIntent = new Intent(MyApp.getContext(), ActivityExploreLyrics.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.putExtra("fresh_load", true);
            notificationIntent.putExtra("from_notif", true);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(MyApp.getContext(), requestCode,
                    notificationIntent, 0);

            builder.setContentIntent(contentIntent);

        }

        private void Check_out_lyricNotif(Map<String, String> map){

            String trackTitle = map.get("trackname");
            String artist = map.get("artist");

            int requestCode = new Random().nextInt();

            Intent notificationIntent = new Intent(MyApp.getContext(), ActivityLyricView.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.putExtra("track_title", trackTitle);
            notificationIntent.putExtra("artist", artist);
            notificationIntent.putExtra("from_notif", true);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(MyApp.getContext(), requestCode,
                    notificationIntent, 0);

            builder.setContentIntent(contentIntent);

        }

        private void UnknownNotif(Map<String, String> map){

            Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
            notificationIntent.setData(Uri.parse(map.get("link")));
            PendingIntent contentIntent = PendingIntent.getActivity(MyApp.getContext(), 0,
                    notificationIntent, 0);

            builder.setContentIntent(contentIntent);

        }

        private void searchLyricNotif(){

            int requestCode = new Random().nextInt();

            Intent notificationIntent = new Intent(MyApp.getContext(), ActivityExploreLyrics.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.putExtra("search_on_launch", true);
            notificationIntent.putExtra("from_notif", true);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(MyApp.getContext(), requestCode,
                    notificationIntent, 0);

            builder.setContentIntent(contentIntent);
        }

        private void reviewNotif(Map<String, String> map){
            Intent action1Intent = new Intent(MyApp.getContext(), NotificationActionService.class)
                    .setAction(ALREADY_RATED);
            action1Intent.putExtra("from_notif", true);
            PendingIntent alreadyRatedIntent = PendingIntent.getService(MyApp.getContext(), 20,
                    action1Intent, PendingIntent.FLAG_ONE_SHOT);

            Intent action2Intent = new Intent(MyApp.getContext(), NotificationActionService.class)
                    .setAction(RATE_NOW);
            action2Intent.putExtra("from_notif", true);
            PendingIntent rateNowIntent = PendingIntent.getService(MyApp.getContext(), 20,
                    action2Intent, PendingIntent.FLAG_ONE_SHOT);

            builder.addAction(new NotificationCompat.Action(R.drawable.ic_close_white_24dp,"Rate now!", rateNowIntent));
            builder.addAction(new NotificationCompat.Action(R.drawable.ic_close_white_24dp,"Already rated", alreadyRatedIntent));
            builder.setContentIntent(rateNowIntent);
        }

        public static class NotificationActionService extends IntentService {

            public NotificationActionService() {
                super(NotificationActionService.class.getSimpleName());
            }

            @Override
            protected void onHandleIntent(Intent intent) {
                String action = intent.getAction();
                if(action==null) return;

                if(intent.getExtras()!=null && intent.getExtras().getBoolean("from_notif")){
                    try {
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification_clicked");
                        UtilityFun.logEvent(bundle);
                    }catch (Exception ignored){
                    }
                }

                Log.d("NotificationAction", "onHandleIntent: " + action);
                switch (action){
                    case ALREADY_RATED:
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_already_rated),true).apply();
                        break;

                    case RATE_NOW:
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }

                        try {
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "rate_on_notif_click");
                            UtilityFun.logEvent(bundle);
                        }catch (Exception ignored){
                        }
                        break;
                }

                NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if(notificationManager!=null){
                    notificationManager.cancel(Constants.NOTIFICATION_ID.FCM);
                }

            }
        }
    }
}
