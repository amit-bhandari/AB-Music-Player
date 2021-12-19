package com.music.player.bhandari.m.activity

import android.Manifest
import android.content.Context
import android.os.Build
import com.afollestad.materialdialogs.DialogAction
import com.google.android.gms.tasks.Task
import com.music.player.bhandari.m.model.Constants

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
class ActivityPermissionSeek constructor() : AppCompatActivity() {
    private val MY_PERMISSIONS_REQUEST: Int = 0
    private var mBound: Boolean = false
    private var mInAppBillingBound: Boolean = false
    private val playerServiceConnection: ServiceConnection = object : ServiceConnection {
        public override fun onServiceConnected(arg0: ComponentName, service: IBinder) {
            val playerBinder: PlayerService.PlayerBinder = service as PlayerService.PlayerBinder
            val playerService: PlayerService = playerBinder.getService()
            MyApp.Companion.setService(playerService)
            mBound = true
            Log.v(Constants.TAG, "LAUNCH MAIN ACTIVITY")
            if (((Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) &&
                        !MyApp.Companion.getPref()
                            .getBoolean(getString(R.string.pref_never_ask_notitication_permission),
                                false) &&
                        !NotificationListenerService.isListeningAuthorized(this@ActivityPermissionSeek))
            ) {
                startActivity(Intent(this@ActivityPermissionSeek,
                    ActivityRequestNotificationAccess::class.java))
            } else {
                //startActivity(new Intent(ActivityPermissionSeek.this, ActivityLyricCard.class).putExtra("lyric", "Wow"));
                startActivity(Intent(this@ActivityPermissionSeek, ActivityMain::class.java))
            }
            finish()
        }

        public override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }
    private var mService: IInAppBillingService? = null
    private val inAppBillingConnection: ServiceConnection = object : ServiceConnection {
        public override fun onServiceDisconnected(name: ComponentName) {
            mInAppBillingBound = false
            mService = null
        }

        public override fun onServiceConnected(
            name: ComponentName,
            service: IBinder
        ) {
            mService = IInAppBillingService.Stub.asInterface(service)
            mInAppBillingBound = true
            //check if user has already removed ads
            //in case, he removed ads, and reinstalled app
            var ownedItems: Bundle? = null
            try {
                ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null)
            } catch (e: RemoteException) {
                e.printStackTrace()
                return
            }
            val response: Int = ownedItems.getInt("RESPONSE_CODE")
            if (response == 0) {
                val ownedSkus: ArrayList<String>? =
                    ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST")
                if (ownedSkus != null) {
                    for (i in ownedSkus.indices) {
                        val sku: String = ownedSkus.get(i)
                        if ((sku == getString(R.string.remove_ads))) {
                            //Toast.makeText(getApplicationContext()
                            // , "You already have removed the ads!",Toast.LENGTH_LONG).show();
                            MyApp.Companion.getPref().edit()
                                .putBoolean(getString(R.string.pref_remove_ads_after_payment), true)
                                .apply()
                            return
                        }
                    }
                    MyApp.Companion.getPref().edit()
                        .putBoolean(getString(R.string.pref_remove_ads_after_payment), false)
                        .apply()
                }
            }
        }
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //check version and make changes if any

        /*
          This will finish your Launcher Activity before it is displayed by detecting that there is already a task running,
          and app should instead resume to the last visible Activity.
          https://stackoverflow.com/a/21022876/5430666
         */if ((getIntent().getFlags() and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish()
            return
        }

        //setContentView(R.layout.splash_activity);
        if (!hasPermissions(this, *PERMISSIONS)) {
            try {
                permissionDetailsDialog()
            } catch (e: Exception) {
                RequestPermission()
            }
        } else {
            bindService()
        }
        setNotificationChannelForOreoPlus()

        //if(MyApp.getPref().getBoolean(getString(R.string.pref_first_install),true)) {
        //checkForDeepLink();
        //}
        changeSettingsForVersion()
        CountryInfo().start()
        try {
            initializeRemoteConfig()
        } catch (e: Exception) {
            //unknown crash in firebase library
            Log.d("ActivityPermissionSeek", "onCreate: " + e.getLocalizedMessage())
        }
        //log selected font to know which font is used maximum
        //logFont();
    }

    private fun initializeRemoteConfig() {
        val mRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val remoteConfigSettings: FirebaseRemoteConfigSettings =
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60)
                .build()
        mRemoteConfig.setConfigSettingsAsync(remoteConfigSettings)
        mRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        // cache expiration in seconds
        val cacheExpiration: Long = 3600L //1 hour

        //expire the cache immediately for development mode .
        /*if (mRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }*/

        // fetch
        mRemoteConfig.fetch(cacheExpiration)
            .addOnCompleteListener(this, OnCompleteListener<Void>({ task: Task<Void?> ->
                Log.d("ActivityPermissionSeek", "onComplete: ")
                if (task.isSuccessful()) {
                    mRemoteConfig.fetchAndActivate()
                    val message: String =
                        FirebaseRemoteConfig.getInstance().getString("developer_message")

                    //new developer message, update UI
                    if (!(MyApp.Companion.getPref()
                            .getString("developer_message", "") == message)
                    ) {
                        MyApp.Companion.getPref().edit().putString("developer_message", message)
                            .apply()
                        MyApp.Companion.getPref().edit().putBoolean("new_dev_message", true).apply()
                    }
                }
            }))
    }

    private fun permissionDetailsDialog() {
        MyDialogBuilder(this)
            .title(R.string.permission_details_title)
            .content(R.string.permission_details_content)
            .positiveText(R.string.permission_details_pos)
            .negativeText(getString(R.string.cancel))
            .cancelable(false)
            .onPositive(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    RequestPermission()
                }
            })
            .onNegative(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    finish()
                }
            })
            .show()
    }

    private fun setNotificationChannelForOreoPlus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                /* Create or update. */
                val channel: NotificationChannel = NotificationChannel("channel_01",
                    "Playback Notification",
                    NotificationManager.IMPORTANCE_LOW)
                channel.setSound(null, null)
                val channel2: NotificationChannel = NotificationChannel("channel_02",
                    "Instant Lyrics",
                    NotificationManager.IMPORTANCE_LOW)
                channel.setSound(null, null)
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                    channel)
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                    channel2)
            }
        } catch (ignored: Exception) {
        }
    }

    protected override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    private fun RequestPermission() {
        // Here, thisActivity is the current activity
        ActivityCompat.requestPermissions(this,
            PERMISSIONS,
            MY_PERMISSIONS_REQUEST)
    }

    private fun changeSettingsForVersion() {

        //if first install
        if (MyApp.Companion.getPref().getBoolean(getString(R.string.pref_first_install), true)) {
            MyApp.Companion.getPref().edit()
                .putBoolean(getString(R.string.pref_first_install), false).apply()
            MyApp.Companion.getPref().edit()
                .putInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.GLOSSY).apply()
            MyApp.Companion.getPref().edit()
                .putInt(getString(R.string.pref_theme_color), Constants.PRIMARY_COLOR.BLACK).apply()
            MyApp.Companion.getPref().edit()
                .putBoolean(getString(R.string.pref_prefer_system_equ), false).apply()
            MyApp.Companion.getPref().edit()
                .putInt(getString(R.string.pref_text_font), Constants.TYPEFACE.MANROPE).apply()
            MyApp.Companion.getPref().edit()
                .putInt(getString(R.string.pref_theme_id), Constants.DEFAULT_THEME_ID).apply()
            MyApp.Companion.getPref().edit()
                .putInt(getString(R.string.pref_text_font), Constants.TYPEFACE.MANROPE).apply()
        }

        //disable lock screen album art
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            MyApp.Companion.getPref().edit()
                .putBoolean(getString(R.string.pref_lock_screen_album_Art), false).apply()
        }
        setDeprecatedPreferencesValues()
    }

    private fun setDeprecatedPreferencesValues() {
        if (MyApp.Companion.getPref().getInt(getString(R.string.pref_click_on_notif),
                -1) != Constants.CLICK_ON_NOTIF.OPEN_DISC_VIEW
        ) {
            MyApp.Companion.getPref().edit().putInt(getString(R.string.pref_click_on_notif),
                Constants.CLICK_ON_NOTIF.OPEN_DISC_VIEW).apply()
        }

        //REMOVED PREFERENCES
        if (MyApp.Companion.getPref()
                .getFloat(getString(R.string.pref_disc_size), -1f) != Constants.DISC_SIZE.MEDIUM
        ) {
            MyApp.Companion.getPref().edit()
                .putFloat(getString(R.string.pref_disc_size), Constants.DISC_SIZE.MEDIUM).apply()
        }

        /*if(!MyApp.getPref().getBoolean(getString(R.string.pref_album_lib_view)
                ,false)){
            MyApp.getPref().edit().putBoolean(getString(R.string.pref_album_lib_view)
                    ,true).apply();
        }*/
    }

    private fun bindService() {
        //initialize music library instance
        // MusicLibrary.getInstance();
        startService(Intent(this, PlayerService::class.java))
        try {
            val playerServiceIntent: Intent = Intent(this, PlayerService::class.java)
            bindService(playerServiceIntent, playerServiceConnection, Context.BIND_AUTO_CREATE)
        } catch (ignored: Exception) {
        }
    }

    protected override fun onDestroy() {
        super.onDestroy()
        try {
            if (mBound) {
                unbindService(playerServiceConnection)
                mBound = false
            }
            if (mInAppBillingBound) {
                unbindService(inAppBillingConnection)
                mInAppBillingBound = false
            }
        } catch (ignored: Exception) {
        }
    }

    private fun logFont() {
        if (MyApp.Companion.getPref()
                .getBoolean(getString(R.string.pref_font_already_logged), false)
        ) {
            return
        }
        try {
            val textFontPref: Int = MyApp.Companion.getPref()
                .getInt(getString(R.string.pref_text_font), Constants.TYPEFACE.MONOSPACE)
            var fontString: String? = ""
            when (textFontPref) {
                Constants.TYPEFACE.MONOSPACE -> fontString = "MONOSPACE"
                Constants.TYPEFACE.SOFIA -> fontString = "SOFIA"
                Constants.TYPEFACE.SYSTEM_DEFAULT -> fontString = "SYSTEM_DEFAULT"
                Constants.TYPEFACE.MANROPE -> fontString = "MANROPE"
                Constants.TYPEFACE.ASAP -> fontString = "ASAP"
            }
            val bundle: Bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, fontString)
            UtilityFun.logEvent(bundle)
            MyApp.Companion.getPref().edit()
                .putBoolean(getString(R.string.pref_font_already_logged), true).apply()
        } catch (ignored: Exception) {
        }
    }

    public override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST -> {
                if (grantResults.size == 0) {
                    return
                }
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.size > 0
                            ) && (grantResults.get(0) == PackageManager.PERMISSION_GRANTED
                            ) && (grantResults.get(1) == PackageManager.PERMISSION_GRANTED) //&& grantResults[2] == PackageManager.PERMISSION_GRANTED
                ) {
                    bindService()
                } else {
                    if (grantResults.get(0) == PackageManager.PERMISSION_DENIED) {
                        //READ PHONE STATE DENIED
                        val intent: Intent = Intent(Intent.ACTION_MAIN)
                        intent.addCategory(Intent.CATEGORY_HOME)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        Toast.makeText(this,
                            getString(R.string.phone_stat_perm_required),
                            Toast.LENGTH_LONG).show()
                        finish()
                    } else if (grantResults.get(1) == PackageManager.PERMISSION_DENIED) {
                        val intent: Intent = Intent(Intent.ACTION_MAIN)
                        intent.addCategory(Intent.CATEGORY_HOME)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        Toast.makeText(this,
                            getString(R.string.storage_perm_required),
                            Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
        }
    }

    companion object {
        private val PERMISSIONS: Array<String> = arrayOf(Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

        fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && (context != null) && (permissions != null)) {
                for (permission: String? in permissions) {
                    if (ActivityCompat.checkSelfPermission(context,
                            permission) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return false
                    }
                }
            }
            return true
        }
    }
}