package com.music.player.bhandari.m;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.service.PlayerService;
import com.squareup.leakcanary.LeakCanary;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Amit AB Bhandari on 1/26/2017.
 */

public class MyApp extends Application {
    private static MyApp instance;
    private static SharedPreferences pref;
    private static PlayerService service;

    //music lock status flag
    private static boolean isLocked = false;

    //check if app is in foreground
    //this is for button actions on bluetooth headset
    public static boolean isAppVisible;

    //batch lyrics download service status flag
    public static boolean isBatchServiceRunning=false;

    //user signed in or not status flag
    public static boolean hasUserSignedIn=false;

    @Override
    public void onCreate() {
        instance = this;
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath(TypeFaceHelper.getTypeFacePath())
                        .setFontAttrId(R.attr.fontPath)
                        .build());

        //this stops crash reports, that's why removed
        /*Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                handleUncaughtException (thread, e);
            }
        });*/

        super.onCreate();
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically
        Intent intent = new Intent ();
        intent.setAction ("com.bhandari.music.SEND_LOG"); // see step 5.
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        startActivity (intent);
        System.exit(1); // kill off the crashed app
    }

    public static MyApp getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance;
    }

    public static SharedPreferences getPref(){
        return pref;
    }

    public  static void setService(PlayerService s){
        service = s;
    }

    public static  PlayerService getService(){
        return  service;
    }

    public static boolean isLocked(){return isLocked;}

    public static void setLocked(boolean lock){isLocked = lock;}

}