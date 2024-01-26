package com.music.player.bhandari.m.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.activity.ActivityPermissionSeek;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

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

public class WidgetReceiver extends AppWidgetProvider {

    String TAG = "Widget";
    String action;
    Context context;

    private ServiceConnection playerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            PlayerService.PlayerBinder playerBinder = (PlayerService.PlayerBinder) service;
            PlayerService playerService = playerBinder.getService();
            MyApp.setService(playerService);
            context.startService(new Intent(context, PlayerService.class)
                    .setAction(action));
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.v(TAG,"Intent "+ intent.getAction());
        this.context = context;
        action=intent.getAction();

        if(intent.getAction()==null){
            //launch player
            if(MyApp.getService()==null){
                MusicLibrary.getInstance();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, PlayerService.class).setAction(Constants.ACTION.LAUNCH_PLAYER_FROM_WIDGET));
                } else {
                    context.startService(new Intent(context,PlayerService.class)
                        .setAction(Constants.ACTION.LAUNCH_PLAYER_FROM_WIDGET));
                }

            }else {
                //permission seek activity is used here to show splash screen
                context.startActivity(new Intent(context, ActivityPermissionSeek.class).addFlags(FLAG_ACTIVITY_NEW_TASK));
            }
        }else {
            if(MyApp.getService()==null){
                Log.v(TAG,"Widget "+ "Service is null");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, PlayerService.class).setAction(intent.getAction()));
                } else {
                    context.startService(new Intent(context,PlayerService.class));
                }

                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

                /*new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        IBinder service = peekService(context, new Intent(context, PlayerService.class));

                        if (service != null){
                            PlayerService.PlayerBinder playerBinder = (PlayerService.PlayerBinder) service;
                            PlayerService playerService = playerBinder.getTrackInfoService();
                            MyApp.setService(playerService);
                            context.startService(new Intent(context, PlayerService.class)
                                    .setAction(action));
                            Log.v(TAG,"Widget "+ action);
                            Log.v(TAG,"Widget "+ "Service started");
                        }else {
                            Log.v(TAG,"Widget "+ "Service null");
                        }
                    }
                }, 500);*/
            }else {
                context.startService(new Intent(context, PlayerService.class)
                        .setAction(intent.getAction()));
            }

        }
        super.onReceive(context, intent);
    }


    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Log.d("WidgetReceiver", "onUpdate: called");
        
        //if player service is null, start the service
        //current song info will be updated in widget from the service itself
        if(MyApp.getService()==null){
            Log.d("WidgetReceiver", "onUpdate: Music service is null");
            MusicLibrary.getInstance();
            try {
                Intent playerServiceIntent = new Intent(context, PlayerService.class);
                playerServiceIntent.setAction(Constants.ACTION.WIDGET_UPDATE);
                context.startService(playerServiceIntent);
                //context.bindService(playerServiceIntent, playerServiceConnection, Context.BIND_AUTO_CREATE);
            }catch (Exception e){
                Log.d("WidgetReceiver", "onUpdate: Error in creating widget");
                e.printStackTrace();
            }
        }

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, WidgetReceiver.class);
            PendingIntent activity_p = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE);

            Intent previousIntent = new Intent(context, WidgetReceiver.class);
            previousIntent.setAction(Constants.ACTION.PREV_ACTION);
            PendingIntent prev_p = PendingIntent.getBroadcast(context, 0,
                    previousIntent, PendingIntent.FLAG_MUTABLE);

            Intent playIntent = new Intent(context, WidgetReceiver.class);
            playIntent.setAction(Constants.ACTION.PLAY_PAUSE_ACTION);
            PendingIntent play_pause_p = PendingIntent.getBroadcast(context, 0,
                    playIntent, PendingIntent.FLAG_MUTABLE);

            Intent nextIntent = new Intent(context, WidgetReceiver.class);
            nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
            PendingIntent next_p = PendingIntent.getBroadcast(context, 0,
                    nextIntent, PendingIntent.FLAG_MUTABLE);

            Intent shuffleIntent = new Intent(context, WidgetReceiver.class);
            shuffleIntent.setAction(Constants.ACTION.SHUFFLE_WIDGET);
            PendingIntent shuffle_p = PendingIntent.getBroadcast(context, 0,
                    shuffleIntent, PendingIntent.FLAG_MUTABLE);

            Intent repeatIntent = new Intent(context, WidgetReceiver.class);
            repeatIntent.setAction(Constants.ACTION.REPEAT_WIDGET);
            PendingIntent repeat_p = PendingIntent.getBroadcast(context, 0,
                    repeatIntent, PendingIntent.FLAG_MUTABLE);

            Intent favIntent = new Intent(context, WidgetReceiver.class);
            favIntent.setAction(Constants.ACTION.FAV_WIDGET);
            PendingIntent fav_p = PendingIntent.getBroadcast(context, 0,
                    favIntent, PendingIntent.FLAG_MUTABLE);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wigdet);
            views.setOnClickPendingIntent(R.id.root_view_widget, activity_p);
            views.setOnClickPendingIntent(R.id.widget_Play, play_pause_p);
            views.setOnClickPendingIntent(R.id.widget_Skip_back, prev_p);
            views.setOnClickPendingIntent(R.id.widget_Skip_forward, next_p);
            views.setOnClickPendingIntent(R.id.repeat_wrapper, repeat_p);
            views.setOnClickPendingIntent(R.id.widget_shuffle, shuffle_p);
            views.setOnClickPendingIntent(R.id.widget_fav, fav_p);

            if(MyApp.getService()!=null) {
                MyApp.getService().updateWidget(true);
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}