package com.music.player.bhandari.m.activity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.vending.billing.IInAppBillingService;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.fcm.CountryInfo;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.service.NotificationListenerService;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.utils.UtilityFun;


import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Amit Bhandari on 1/29/2017.
 */

public class ActivityPermissionSeek extends AppCompatActivity {


    final private int MY_PERMISSIONS_REQUEST = 0;
    private static String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE
            , Manifest.permission.WRITE_EXTERNAL_STORAGE
            ,Manifest.permission.RECORD_AUDIO};
    private boolean mBound=false;
    private boolean mInAppBillingBound = false;
    private ServiceConnection playerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            PlayerService.PlayerBinder playerBinder = (PlayerService.PlayerBinder) service;
            PlayerService playerService = playerBinder.getService();
            MyApp.setService(playerService);
            mBound=true;
            Log.v(Constants.TAG,"LAUNCH MAIN ACTIVITY");

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                    !MyApp.getPref().getBoolean(getString(R.string.pref_never_ask_notitication_permission), false) &&
                    !NotificationListenerService.isListeningAuthorized(ActivityPermissionSeek.this)){
                startActivity(new Intent(ActivityPermissionSeek.this, ActivityRequestNotificationAccess.class));
            }else {
                startActivity(new Intent(ActivityPermissionSeek.this, ActivityMain.class));
            }
            finish();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound=false;
        }
    };

    private IInAppBillingService mService;

    private ServiceConnection inAppBillingConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mInAppBillingBound = false;
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            mInAppBillingBound =true;
            //check if user has already removed ads
            //in case, he removed ads, and reinstalled app
            Bundle ownedItems = null;
            try {
                ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
            } catch (RemoteException e) {

                e.printStackTrace();
                return;
            }

            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> ownedSkus =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");

                if (ownedSkus != null) {
                    for (int i = 0; i < ownedSkus.size(); ++i) {
                        String sku = ownedSkus.get(i);
                        if (sku.equals(getString(R.string.remove_ads))) {
                            //Toast.makeText(getApplicationContext()
                            // , "You already have removed the ads!",Toast.LENGTH_LONG).show();
                            MyApp.getPref().edit().putBoolean(getString(R.string.pref_remove_ads_after_payment),true).apply();
                            return;
                        }
                    }
                    MyApp.getPref().edit().putBoolean(getString(R.string.pref_remove_ads_after_payment),false).apply();
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //check version and make changes if any

        if(!hasPermissions(this, PERMISSIONS)) {
            try {
                permissionDetailsDialog();
            }catch (Exception e){
                RequestPermission();
            }
        }else {
            bindService();
        }

        setNotificationChannelForOreoPlus();

        changeSettingsForVersion();

        new CountryInfo().start();

        //log selected font to know which font is used maximum
        //logFont();
    }

    private void permissionDetailsDialog(){
        new MaterialDialog.Builder(this)
                .typeface(TypeFaceHelper.getTypeFace(this),TypeFaceHelper.getTypeFace(this))
                .title(R.string.permission_details_title)
                .content(R.string.permission_details_content)
                .positiveText(R.string.permission_details_pos)
                .negativeText(getString(R.string.cancel))
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        RequestPermission();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();
                    }
                })
                .show();
    }

    private void setNotificationChannelForOreoPlus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    /* Create or update. */
                NotificationChannel channel = new NotificationChannel("channel_01",
                        "Playback Notification",
                        NotificationManager.IMPORTANCE_LOW);
                channel.setSound(null, null);
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            }
        }catch (Exception ignored){}
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void RequestPermission(){
        // Here, thisActivity is the current activity

            ActivityCompat.requestPermissions(this,
                    PERMISSIONS,
                    MY_PERMISSIONS_REQUEST);

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void changeSettingsForVersion() {

        //if first install
        if(MyApp.getPref().getBoolean(getString(R.string.pref_first_install),true)) {
            MyApp.getPref().edit().putBoolean(getString(R.string.pref_first_install),false).apply();
            MyApp.getPref().edit().putInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.GLOSSY).apply();
            MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color), Constants.PRIMARY_COLOR.ANTIQUE_RUBY).apply();
            MyApp.getPref().edit().putBoolean(getString(R.string.pref_prefer_system_equ),true).apply();
            MyApp.getPref().edit().putInt(getString(R.string.pref_text_font), Constants.TYPEFACE.MONOSPACE).apply();
            MyApp.getPref().edit().putInt(getString(R.string.pref_reward_points), 500).apply();
        }

        //disable lock screen alum art
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            MyApp.getPref().edit().putBoolean(getString(R.string.pref_lock_screen_album_Art), false).apply();
        }



        //remove ads
        //check if ads removed, if yes, then change boolean accordingly
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, inAppBillingConnection, Context.BIND_AUTO_CREATE);

        setDeprecatedPreferencesValues();
    }

    private void setDeprecatedPreferencesValues() {
        if(MyApp.getPref().getInt(getString(R.string.pref_click_on_notif)
                ,-1)!= Constants.CLICK_ON_NOTIF.OPEN_DISC_VIEW){
            MyApp.getPref().edit().putInt(getString(R.string.pref_click_on_notif)
                    ,Constants.CLICK_ON_NOTIF.OPEN_DISC_VIEW).apply();
        }

        //REMOVED PREFERENCES

        if(MyApp.getPref().getFloat(getString(R.string.pref_disc_size)
                ,-1)!=Constants.DISC_SIZE.MEDIUM){
            MyApp.getPref().edit().putFloat(getString(R.string.pref_disc_size)
                    ,Constants.DISC_SIZE.MEDIUM).apply();
        }

        if(!MyApp.getPref().getBoolean(getString(R.string.pref_album_lib_view)
                ,false)){
            MyApp.getPref().edit().putBoolean(getString(R.string.pref_album_lib_view)
                    ,true).apply();
        }
    }

    private void bindService(){
        //initialize music library instance
       // MusicLibrary.getInstance();

        startService(new Intent(this,PlayerService.class));
        try {
            Intent playerServiceIntent = new Intent(this, PlayerService.class);
            bindService(playerServiceIntent, playerServiceConnection, Context.BIND_AUTO_CREATE);
        }catch (Exception e){

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(mBound) {
                unbindService(playerServiceConnection);
                mBound=false;
            }

            if(mInAppBillingBound){
                unbindService(inAppBillingConnection);
                mInAppBillingBound = false;
            }
        }catch (Exception ignored){

        }

    }

    private void logFont(){

        if(MyApp.getPref().getBoolean(getString(R.string.pref_font_already_logged), false)){
            return;
        }

        try {
            int textFontPref = MyApp.getPref().getInt(getString(R.string.pref_text_font), Constants.TYPEFACE.MONOSPACE);
            String fontString = "";
            switch (textFontPref){
                case Constants.TYPEFACE.MONOSPACE:
                    fontString = "MONOSPACE";
                    break;

                case Constants.TYPEFACE.SOFIA:
                    fontString = "SOFIA";
                    break;

                case Constants.TYPEFACE.SYSTEM_DEFAULT:
                    fontString = "SYSTEM_DEFAULT";
                    break;

                case Constants.TYPEFACE.RISQUE:
                    fontString = "RISQUE";
                    break;

                case Constants.TYPEFACE.VAST_SHADOW:
                    fontString = "VAST_SHADOW";
                    break;
            }
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, fontString );
            UtilityFun.logEvent(bundle);
            MyApp.getPref().edit().putBoolean(getString(R.string.pref_font_already_logged), true).apply();
        }catch (Exception ignored){
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {

                if(grantResults.length==0){
                    return;
                }
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        //&& grantResults[2] == PackageManager.PERMISSION_GRANTED
                        ) {
                    bindService();
                } else {

                    if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                        //READ PHONE STATE DENIED
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Toast.makeText(this, getString(R.string.phone_stat_perm_required), Toast.LENGTH_LONG).show();
                        finish();
                    }else if(grantResults[1] == PackageManager.PERMISSION_DENIED) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Toast.makeText(this, getString(R.string.storage_perm_required), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
            break;

        }
    }
}
