package com.music.player.bhandari.m.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import com.music.player.bhandari.m.activity.ActivityPermissionSeek
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.service.PlayerService
import com.music.player.bhandari.m.model.MusicLibrary
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import java.lang.Exception

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
class WidgetReceiver : AppWidgetProvider() {
    var TAG = "Widget"
    var action: String? = null
    var context: Context? = null
    private val playerServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(arg0: ComponentName, service: IBinder) {
            val playerBinder = service as PlayerService.PlayerBinder
            val playerService: PlayerService = playerBinder.getService()
            MyApp.setService(playerService)
            context!!.startService(Intent(context, PlayerService::class.java)
                .setAction(action))
        }

        override fun onServiceDisconnected(arg0: ComponentName) {}
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.v(TAG, "Intent " + intent.action)
        this.context = context
        action = intent.action
        if (intent.action == null) {
            //launch player
            if (MyApp.getService() == null) {
                MusicLibrary.instance
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        context.startForegroundService(Intent(context,
                            PlayerService::class.java).setAction(Constants.ACTION.LAUNCH_PLAYER_FROM_WIDGET))
                    }
                    else -> {
                        context.startService(Intent(context, PlayerService::class.java)
                            .setAction(Constants.ACTION.LAUNCH_PLAYER_FROM_WIDGET))
                    }
                }
            } else {
                //permission seek activity is used here to show splash screen
                context.startActivity(Intent(context, ActivityPermissionSeek::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        } else {
            if (MyApp.getService() == null) {
                Log.v(TAG, "Widget " + "Service is null")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(Intent(context,
                        PlayerService::class.java).setAction(intent.action))
                } else {
                    context.startService(Intent(context, PlayerService::class.java))
                }

                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

                /*new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        IBinder service = peekService(context, new Intent(context, playerService!!.class));

                        if (service != null){
                            playerService!!.PlayerBinder playerBinder = (playerService!!.PlayerBinder) service;
                            PlayerService playerService = playerBinder.getTrackInfoService();
                            MyApp.setService(playerService);
                            context.startService(new Intent(context, playerService!!.class)
                                    .setAction(action));
                            Log.v(TAG,"Widget "+ action);
                            Log.v(TAG,"Widget "+ "Service started");
                        }else {
                            Log.v(TAG,"Widget "+ "Service null");
                        }
                    }
                }, 500);*/
            } else {
                context.startService(Intent(context, PlayerService::class.java)
                    .setAction(intent.action))
            }
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("WidgetReceiver", "onUpdate: called")

        //if player service is null, start the service
        //current song info will be updated in widget from the service itself
        if (MyApp.getService() == null) {
            Log.d("WidgetReceiver", "onUpdate: Music service is null")
            MusicLibrary.instance
            try {
                val playerServiceIntent = Intent(context, PlayerService::class.java)
                playerServiceIntent.action = Constants.ACTION.WIDGET_UPDATE
                context.startService(playerServiceIntent)
                //context.bindService(playerServiceIntent, playerServiceConnection, Context.BIND_AUTO_CREATE);
            } catch (e: Exception) {
                Log.d("WidgetReceiver", "onUpdate: Error in creating widget")
                e.printStackTrace()
            }
        }

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (appWidgetId in appWidgetIds) {
            val intent = Intent(context, WidgetReceiver::class.java)
            val activity_p = PendingIntent.getBroadcast(context, 0, intent, 0)
            val previousIntent = Intent(context, WidgetReceiver::class.java)
            previousIntent.action = Constants.ACTION.PREV_ACTION
            val prev_p = PendingIntent.getBroadcast(context, 0,
                previousIntent, 0)
            val playIntent = Intent(context, WidgetReceiver::class.java)
            playIntent.action = Constants.ACTION.PLAY_PAUSE_ACTION
            val play_pause_p = PendingIntent.getBroadcast(context, 0,
                playIntent, 0)
            val nextIntent = Intent(context, WidgetReceiver::class.java)
            nextIntent.action = Constants.ACTION.NEXT_ACTION
            val next_p = PendingIntent.getBroadcast(context, 0,
                nextIntent, 0)
            val shuffleIntent = Intent(context, WidgetReceiver::class.java)
            shuffleIntent.action = Constants.ACTION.SHUFFLE_WIDGET
            val shuffle_p = PendingIntent.getBroadcast(context, 0,
                shuffleIntent, 0)
            val repeatIntent = Intent(context, WidgetReceiver::class.java)
            repeatIntent.action = Constants.ACTION.REPEAT_WIDGET
            val repeat_p = PendingIntent.getBroadcast(context, 0,
                repeatIntent, 0)
            val favIntent = Intent(context, WidgetReceiver::class.java)
            favIntent.action = Constants.ACTION.FAV_WIDGET
            val fav_p = PendingIntent.getBroadcast(context, 0,
                favIntent, 0)
            val views = RemoteViews(context.packageName, R.layout.wigdet)
            views.setOnClickPendingIntent(R.id.root_view_widget, activity_p)
            views.setOnClickPendingIntent(R.id.widget_Play, play_pause_p)
            views.setOnClickPendingIntent(R.id.widget_Skip_back, prev_p)
            views.setOnClickPendingIntent(R.id.widget_Skip_forward, next_p)
            views.setOnClickPendingIntent(R.id.repeat_wrapper, repeat_p)
            views.setOnClickPendingIntent(R.id.widget_shuffle, shuffle_p)
            views.setOnClickPendingIntent(R.id.widget_fav, fav_p)
            if (MyApp.getService() != null) {
                MyApp.getService()?.updateWidget(true)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}