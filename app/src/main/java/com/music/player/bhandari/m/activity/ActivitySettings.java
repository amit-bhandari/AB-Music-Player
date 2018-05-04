package com.music.player.bhandari.m.activity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.messaging.FirebaseMessaging;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.BulkArtInfoGrabber;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.rewards.RewardPoints;
import com.music.player.bhandari.m.service.BatchDownloaderService;
import com.music.player.bhandari.m.service.NotificationListenerService;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.utils.AppLaunchCountManager;
import com.music.player.bhandari.m.utils.UtilityFun;
import com.music.player.bhandari.m.UIElementHelper.recyclerviewHelper.ItemTouchHelperAdapter;
import com.music.player.bhandari.m.UIElementHelper.recyclerviewHelper.OnStartDragListener;
import com.music.player.bhandari.m.UIElementHelper.recyclerviewHelper.SimpleItemTouchHelperCallback;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Amit AB Bhandari on 1/27/2017.
 */

public class ActivitySettings extends AppCompatActivity {

    private  int launchedFrom = 0;
    private AdView mAdView;

    //flag to know for which background, crop image is invoked
    //true = main library
    //false = now playing
    private static boolean isMainLibraryBackground;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {

        //if player service not running, kill the app
        if(MyApp.getService()==null){
            Intent intent = new Intent(this, ActivityPermissionSeek.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }


        int themeSelector = MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT);
        switch (themeSelector){
            case Constants.PRIMARY_COLOR.DARK:
                setTheme(R.style.AppThemeDark);
                break;

            case Constants.PRIMARY_COLOR.GLOSSY:
                setTheme(R.style.AppThemeDark);
                break;

            case Constants.PRIMARY_COLOR.LIGHT:
                setTheme(R.style.AppThemeLight);
                break;
        }

        super.onCreate(savedInstanceState);

        launchedFrom = getIntent().getIntExtra("launchedFrom",0);
        setContentView(R.layout.acitivty_settings);

        if(/*AppLaunchCountManager.isEligibleForInterstialAd() &&*/ !UtilityFun.isAdsRemoved()
                && AppLaunchCountManager.isEligibleForBannerAds()) {

            //banner ad
            MobileAds.initialize(getApplicationContext(), getString(R.string.banner_settings_activity));
             mAdView = (AdView) findViewById(R.id.adView);
            if (UtilityFun.isConnectedToInternet()) {
                AdRequest adRequest = new AdRequest.Builder()//.addTestDevice("F40E78AED9B7FE233362079AC4C05B61")
                        .build();
                if (mAdView != null) {
                    mAdView.loadAd(adRequest);
                    mAdView.setVisibility(View.VISIBLE);
                }
            } else {
                if (mAdView != null) {
                    mAdView.setVisibility(View.GONE);
                }
            }
        }

        findViewById(R.id.root_view_settings).setBackgroundDrawable(ColorHelper.getBaseThemeDrawable());

        Toolbar toolbar = findViewById(R.id.toolbar_);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ColorHelper.getPrimaryColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.getDarkPrimaryColor());
        }

        setTitle("Settings");

        getFragmentManager().beginTransaction().replace(R.id.linear_layout_fragment, new MyPreferenceFragment()).commit();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        switch (launchedFrom){
            case Constants.PREF_LAUNCHED_FROM.MAIN:
                startActivity(new Intent(this, ActivityMain.class));
                break;

            case Constants.PREF_LAUNCHED_FROM.DRAWER:
                startActivity(new Intent(this, ActivityMain.class));
                break;

            case Constants.PREF_LAUNCHED_FROM.NOW_PLAYING:
                startActivity(new Intent(this, ActivityNowPlaying.class));
                break;

            default:
                startActivity(new Intent(this, ActivityMain.class));
                break;
        }
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        MyApp.isAppVisible = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        MyApp.isAppVisible = true;
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                MyApp.getService().play();
                break;

            case KeyEvent.KEYCODE_MEDIA_NEXT:
                MyApp.getService().nextTrack();
                break;

            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                MyApp.getService().prevTrack();
                break;

            case KeyEvent.KEYCODE_MEDIA_STOP:
                MyApp.getService().stop();
                break;

            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                break;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                File fromFile = new File(resultUri.getPath());

                String savePath;
                if(isMainLibraryBackground){
                    savePath = MyApp.getContext().getFilesDir() + getString(R.string.main_lib_back_custom_image);
                }else {
                    savePath = MyApp.getContext().getFilesDir() + getString(R.string.now_playing_back_custom_image);
                }

                File toFile = new File(savePath);
                boolean b = fromFile.renameTo(toFile);
                Log.d(Constants.TAG, "onActivityResult: saved custom image size : " + toFile.length()/(1024));

                if(b){
                    if(isMainLibraryBackground){
                        //1 - custom image
                        MyApp.getPref().edit().putInt(getString(R.string.pref_main_library_back),1).apply();
                    }else {
                        //3 - custom image
                        MyApp.getPref().edit().putInt(getString(R.string.pref_now_playing_back),3).apply();
                    }

                    Toast.makeText(this, "Background successfully updated!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Failed to save file, try some different image!", Toast.LENGTH_SHORT).show();
                }

                Log.d(Constants.TAG, "onActivityResult: "+result.toString());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Failed to select image, try again!", Toast.LENGTH_SHORT).show();
                Exception error = result.getError();
            }
        }
    }

    @Override
    protected void onDestroy() {

        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @SuppressLint("validFragment")
    public static class MyPreferenceFragment extends PreferenceFragment
      implements  OnStartDragListener{

        final String PLAY_PAUSE = "Play/Pause Current Track";
        final String NEXT = "Play Next Track";
        final String PREVIOUS = "Play Previous Track";

        final String RED = "Red Carmine";
        final String GREEN = "Green Dartmouth";
        final String BLUE = "Blue Catalina";
        final String PINK = "Pink Cersie";
        final String YELLOW = "Amber";
        final String BLACK = "All Black";
        final String AMBER = "Cyber Grape";
        final String BONDI_BLUE = "Bondi Blue";
        final String BYZANTIUM = "Byzantium";
        final String DARK_SLATE_GRAY = "Dark Slate Gray";
        final String ANTIQUE_BRONZE = "Antique Bronze";
        final String ANTIQUE_RUBY = "Antique Ruby";
        final String BLUE_MAGNETA_VIOLET = "Blue Magneta Violet";
        final String EGGPLANT = "Eggplant";
        final String FRENCH_BISTRE = "French Bistre";
        final String DEEP_CHESTNUT = "Deep Chestnut";
        final String MANUAL_INPUT = "Manual Color Input";

        final String GUNMETAL = "Gun Metal";
        final String HALAYA_UBE = "Halaya Blue";
        final String INTERNATIONAL_ORANGE = "International Orange";
        final String JACARTA = "Jacarta";
        final String JAPANESE_VIOLET = "Japanese Violet";
        final String MAGENTA = "Magenta";
        final String MAASTRICHT_BLUE = "Maasstricht Blue";
        final String MAROON = "Maroon";
        final String PINE_TREE = "Pine Tree";
        final String POLIE_BLUE = "Police Blue";

        final String DARK = "Dark";
        final String LIGHT = "Light";
        final String GLOSSY = "Glossy";

        final String MONOSPACE = "Monospace";
        final String SOFIA = "Sofia";
        final String RISQUE = "Risque";
        final String ASAP = "Asap (Recommended)";
        final String SYSTEM_DEFAULT = "System Default";

        private CheckBoxPreference instantLyricStatus;

        private ItemTouchHelper mItemTouchHelper;

        @Override
        public void onResume() {
            super.onResume();

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
                setInstantLyricStatus();
            }

        }

        private void setInstantLyricStatus() {
            if(instantLyricStatus!=null) {
                if (NotificationListenerService.isListeningAuthorized(MyApp.getContext())) {
                    MyApp.getPref().edit().putBoolean(getString(R.string.pref_instant_lyric), true).apply();
                    instantLyricStatus.setChecked(true);
                } else {
                    MyApp.getPref().edit().putBoolean(getString(R.string.pref_instant_lyric), false).apply();
                    instantLyricStatus.setChecked(false);
                }
            }
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);


            //base Theme
            Preference baseTheme = findPreference(getString(R.string.pref_theme));
            int baseThemePref = MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT);
            switch (baseThemePref){

                case Constants.PRIMARY_COLOR.LIGHT:
                    findPreference(getString(R.string.pref_theme)).setSummary(LIGHT);
                    break;

                case Constants.PRIMARY_COLOR.DARK:
                    findPreference(getString(R.string.pref_theme)).setSummary(DARK);
                    break;

                case Constants.PRIMARY_COLOR.GLOSSY:
                    findPreference(getString(R.string.pref_theme)).setSummary(GLOSSY);

            }
            baseTheme.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    BaseThemeSelectionDialog();
                    return true;
                }
            });

            //Theme color
            Preference primaryColorPref = findPreference(getString(R.string.pref_theme_color));
            int themePrefRead = MyApp.getPref().getInt(getString(R.string.pref_theme_color), Constants.PRIMARY_COLOR.BLACK);
            switch (themePrefRead){

                case Constants.PRIMARY_COLOR.BLACK:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(BLACK);
                    break;

                case Constants.PRIMARY_COLOR.BLUE_CATALINA:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(BLUE);
                    break;


                case Constants.PRIMARY_COLOR.RED_CARMINE:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(RED);
                    break;

                case Constants.PRIMARY_COLOR.AMBER:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(YELLOW);
                    break;

                case Constants.PRIMARY_COLOR.PINK_CERISE:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(PINK);
                    break;

                case Constants.PRIMARY_COLOR.GREEN_DARTMOUTH:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(GREEN);
                    break;

                case Constants.PRIMARY_COLOR.CYBER_GRAPE:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(AMBER);
                    break;

                case Constants.PRIMARY_COLOR.BONDI_BLUE:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(BONDI_BLUE);
                    break;

                case Constants.PRIMARY_COLOR.BYZANTIUM:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(BYZANTIUM);
                    break;

                case Constants.PRIMARY_COLOR.DARK_SLATE_GRAY:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(DARK_SLATE_GRAY);
                    break;

                case Constants.PRIMARY_COLOR.ANTIQUE_BRONZE:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(ANTIQUE_BRONZE);
                    break;

                case Constants.PRIMARY_COLOR.ANTIQUE_RUBY:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(ANTIQUE_RUBY);
                    break;

                case Constants.PRIMARY_COLOR.BLUE_MAGNETA_VIOLET:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(BLUE_MAGNETA_VIOLET);
                    break;

                case Constants.PRIMARY_COLOR.EGGPLANT:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(EGGPLANT);
                    break;

                case Constants.PRIMARY_COLOR.FRENCH_BISTRE:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(FRENCH_BISTRE);
                    break;

                case Constants.PRIMARY_COLOR.DEEP_CHESTNUT:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(DEEP_CHESTNUT);
                    break;

                case Constants.PRIMARY_COLOR.GUNMETAL:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(GUNMETAL);
                    break;

                case Constants.PRIMARY_COLOR.HALAYA_UBE:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(HALAYA_UBE);
                    break;

                case Constants.PRIMARY_COLOR.INTERNATIONAL_ORANGE:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(INTERNATIONAL_ORANGE);
                    break;

                case Constants.PRIMARY_COLOR.JACARTA:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(JACARTA);
                    break;

                case Constants.PRIMARY_COLOR.JAPANESE_VIOLET:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(JAPANESE_VIOLET);
                    break;

                case Constants.PRIMARY_COLOR.MAGENTA:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(MAGENTA);
                    break;

                case Constants.PRIMARY_COLOR.MAASTRICHT_BLUE:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(MAASTRICHT_BLUE);
                    break;

                case Constants.PRIMARY_COLOR.MAROON:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(MAROON);
                    break;

                case Constants.PRIMARY_COLOR.PINE_TREE:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(PINE_TREE);
                    break;

                case Constants.PRIMARY_COLOR.POLIE_BLUE:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(POLIE_BLUE);
                    break;

                //manual input
                default:
                    findPreference(getString(R.string.pref_theme_color)).setSummary(MANUAL_INPUT);

            }
            primaryColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    PrimarySelectionDialog();
                    return true;
                }
            });

            //now playing back
            final Preference nowPlayingBackPref = findPreference(getString(R.string.pref_now_playing_back));
            nowPlayingBackPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    nowPlayingBackDialog();
                    return true;
                }
            });

            //Main library back
            final Preference mainLibBackPref = findPreference(getString(R.string.pref_main_library_back));
            mainLibBackPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    mainLibBackDialog();
                    return true;
                }
            });

            //text font
            Preference fontPref = findPreference(getString(R.string.pref_text_font));
            int textFontPref = MyApp.getPref().getInt(getString(R.string.pref_text_font), Constants.TYPEFACE.ASAP);
            switch (textFontPref){

                case Constants.TYPEFACE.MONOSPACE:
                    findPreference(getString(R.string.pref_text_font)).setSummary(MONOSPACE);
                    break;

                case Constants.TYPEFACE.SOFIA:
                    findPreference(getString(R.string.pref_text_font)).setSummary(SOFIA);
                    break;

                case Constants.TYPEFACE.SYSTEM_DEFAULT:
                    findPreference(getString(R.string.pref_text_font)).setSummary(SYSTEM_DEFAULT);
                    break;

                case Constants.TYPEFACE.RISQUE:
                    findPreference(getString(R.string.pref_text_font)).setSummary(RISQUE);
                    break;

                case Constants.TYPEFACE.ASAP:
                    findPreference(getString(R.string.pref_text_font)).setSummary(ASAP);
                    break;
            }
            fontPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    fontPrefSelectionDialog();
                    return true;
                }
            });

            //lockscreen albumName art
            CheckBoxPreference lockScreenArt = (CheckBoxPreference)findPreference(getString(R.string.pref_lock_screen_album_Art));

            lockScreenArt.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                        Toast.makeText(getActivity(),"Feature is only available on lollipop and above!", Toast.LENGTH_LONG).show();
                        return false;
                    }

                    if(((boolean) newValue)){
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_lock_screen_album_Art),true).apply();
                        MyApp.getService().setMediaSessionMetadata(true);
                    }else {
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_lock_screen_album_Art),false).apply();
                        MyApp.getService().setMediaSessionMetadata(false);
                    }
                    return true;
                }
            });

            //prefer system equalizer
            CheckBoxPreference prefPrefSystemEqu = (CheckBoxPreference)findPreference(getString(R.string.pref_prefer_system_equ));

            prefPrefSystemEqu.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if(((boolean) newValue)){
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_prefer_system_equ),true).apply();
                    }else {
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_prefer_system_equ),false).apply();
                    }
                    return true;
                }
            });

            //notifcations
            final CheckBoxPreference notifications = (CheckBoxPreference)findPreference(getString(R.string.pref_notifications));

            notifications.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String pos_text = "Turn On";
                    if (((boolean) newValue)) {
                        pos_text = getString(R.string.turn_on);
                    } else {
                        pos_text = getString(R.string.turn_off);
                    }
                    new MaterialDialog.Builder(getActivity())
                            .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                            .title(R.string.notifications_title)
                            .content(R.string.notification_content)
                            .positiveText(pos_text)
                            .negativeText(getString(R.string.cancel))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    String country = MyApp.getPref().getString(MyApp.getContext().getString(R.string.pref_user_country),"");
                                    if(MyApp.getPref().getBoolean(getString(R.string.pref_notifications), true)) {
                                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_notifications), false).apply();
                                        try {
                                            FirebaseMessaging.getInstance().unsubscribeFromTopic(country);
                                            FirebaseMessaging.getInstance().unsubscribeFromTopic("ab_music");
                                        }catch (Exception ignored){}
                                        notifications.setChecked(false);
                                    }else {
                                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_notifications), true).apply();
                                        notifications.setChecked(true);
                                        try {
                                            FirebaseMessaging.getInstance().subscribeToTopic(country);
                                            FirebaseMessaging.getInstance().subscribeToTopic("ab_music");
                                        }catch (Exception ignored){}
                                    }
                                }
                            })
                            .show();
                    return false;
                }
            });

            //shake
            CheckBoxPreference shakeStatus = (CheckBoxPreference)findPreference(getString(R.string.pref_shake));
            shakeStatus.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    if(((boolean) newValue)){
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_shake),true).apply();
                        PlayerService.setShakeListener(true);
                    }else {
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_shake),false).apply();
                        PlayerService.setShakeListener(false);
                    }
                    return true;
                }
            });

            final CheckBoxPreference continuousPlaybackPref = (CheckBoxPreference) findPreference(getString(R.string.pref_continuous_playback));
            continuousPlaybackPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, final Object newValue) {
                    String pos_text = "Turn On";
                    if (((boolean) newValue)) {
                        pos_text = getString(R.string.turn_on);
                    } else {
                        pos_text = getString(R.string.turn_off);
                    }

                    new MaterialDialog.Builder(getActivity())
                            .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                            .title(R.string.title_continous_playback)
                            .content(R.string.cont_playback_content)
                            .positiveText(pos_text)
                            .negativeText(getString(R.string.cancel))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    if (((boolean) newValue)) {
                                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_continuous_playback), true).apply();
                                        continuousPlaybackPref.setChecked(true);
                                    } else {
                                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_continuous_playback), false).apply();
                                        continuousPlaybackPref.setChecked(false);
                                    }
                                }
                            })
                            .show();

                    return false;
                }
            });

            final CheckBoxPreference dataSaverPref = (CheckBoxPreference) findPreference(getString(R.string.pref_data_saver));
            dataSaverPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, final Object newValue) {
                    String pos_text = "Turn On";
                    if (((boolean) newValue)) {
                        pos_text = getString(R.string.turn_on);
                    } else {
                        pos_text = getString(R.string.turn_off);
                    }

                    new MaterialDialog.Builder(getActivity())
                            .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                            .title(R.string.title_data_Saver)
                            .content(R.string.data_saver_content)
                            .positiveText(pos_text)
                            .negativeText(getString(R.string.cancel))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    if (((boolean) newValue)) {
                                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_data_saver), true).apply();
                                        dataSaverPref.setChecked(true);
                                    } else {
                                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_data_saver), false).apply();
                                        dataSaverPref.setChecked(false);
                                    }
                                }
                            })
                            .show();

                    return false;
                }
            });

            instantLyricStatus = (CheckBoxPreference) findPreference(getString(R.string.pref_instant_lyric));
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                //instant lyric
                instantLyricStatus.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, final Object newValue) {

                        String pos_text = "Turn On";
                        if (((boolean) newValue)) {
                            pos_text = getString(R.string.turn_on);
                        } else {
                            pos_text = getString(R.string.turn_off);
                        }

                        new MaterialDialog.Builder(getActivity())
                                .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                                .title(R.string.instant_lyrics_title)
                                .content(R.string.instant_lyrics_content)
                                .positiveText(pos_text)
                                .negativeText(getString(R.string.cancel))
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        if (((boolean) newValue)) {
                                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                            startActivity(intent);
                                            Toast.makeText(MyApp.getContext(), "Click on AB Music to enable!", Toast.LENGTH_LONG).show();
                                        } else {
                                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                            startActivity(intent);
                                            Toast.makeText(MyApp.getContext(), "Click on AB Music to disable!", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                })
                                .show();

                        return false;
                    }
                });
            }else {
                instantLyricStatus.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        Toast.makeText(getActivity(),"Feature is only available on Jelly Bean MR2 and above!", Toast.LENGTH_LONG).show();
                        return false;
                    }
                });
            }

            //shake
            Preference shakeAction = findPreference(getString(R.string.pref_shake_action));
            int shakeActionRead = MyApp.getPref().getInt(getString(R.string.pref_shake_action),Constants.SHAKE_ACTIONS.NEXT);
            if(shakeActionRead==Constants.SHAKE_ACTIONS.NEXT){
                shakeAction.setSummary(NEXT);
            }else if(shakeActionRead==Constants.SHAKE_ACTIONS.PLAY_PAUSE){
                shakeAction.setSummary(PLAY_PAUSE);
            }else {
                shakeAction.setSummary(PREVIOUS);
            }
            shakeAction.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    ShakeActionDialog();
                    return true;
                }
            });


            //hide short clips preference
            Preference hideShortClipsPref = findPreference(getString(R.string.pref_hide_short_clips));
            String summary = String.valueOf(MyApp.getPref().getInt(getString(R.string.pref_hide_short_clips),10)) + " seconds";
            hideShortClipsPref.setSummary(summary);
            hideShortClipsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    shortClipDialog();
                    return true;
                }
            });

            //excluded folders preference
            Preference excludedFoldersPref = findPreference(getString(R.string.pref_excluded_folders));
            excludedFoldersPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    displayExcludedFolders();
                    return true;
                }
            });

            Preference hideByStartPref = findPreference(getString(R.string.pref_hide_tracks_starting_with));
            String text1 = MyApp.getPref().getString(getString(R.string.pref_hide_tracks_starting_with_1),"");
            String text2 = MyApp.getPref().getString(getString(R.string.pref_hide_tracks_starting_with_2),"");
            String text3 = MyApp.getPref().getString(getString(R.string.pref_hide_tracks_starting_with_3),"");
            hideByStartPref.setSummary(text1+", "+text2+", "+text3);
            hideByStartPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    hideByStartDialog();
                    return true;
                }
            });


            //opening tab preference
            Preference openingTabPref = findPreference(getString(R.string.pref_opening_tab));
            openingTabPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    tabSeqDialog();
                    return true;
                }
            });



            //about us  preference
            Preference aboutUs = findPreference(getString(R.string.pref_about_us));
            aboutUs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    getActivity().startActivity(new Intent(getActivity(), ActivityAboutUs.class));
                    return true;
                }
            });

            //cache artist data
            Preference cacheArtistDataPref = findPreference(getString(R.string.pref_cache_artist_data));
            Long lastTimeDidAt = MyApp.getPref().getLong(getString(R.string.pref_artist_cache_manual),0);
            if (System.currentTimeMillis() >= lastTimeDidAt +
                    (2 * 60 * 60 * 1000)) {
                cacheArtistDataPref.setEnabled(true);
            }
            cacheArtistDataPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    preference.setEnabled(false);
                    MyApp.getPref().edit().putLong(getString(R.string.pref_artist_cache_manual), System.currentTimeMillis()).apply();
                    new BulkArtInfoGrabber().start();
                    Toast.makeText(MyApp.getContext(), "Artist info local caching started in background, will be finished shortly!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            //batch download  preference
            Preference batchDownload = findPreference(getString(R.string.pref_batch_download));
            batchDownload.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {

                    if(MyApp.isBatchServiceRunning){
                        Toast.makeText(getActivity(),getString(R.string.error_batch_download_running), Toast.LENGTH_LONG).show();
                        return false;
                    }

                    //check if number of songs equals reward points, if not, give error
                    if(!UtilityFun.isAdsRemoved()) {

                        int numberOfTracks = MusicLibrary.getInstance().getDataItemsForTracks().size();
                        int rewardPoints = RewardPoints.getRewardPointsCount();
                        if(rewardPoints<numberOfTracks) {
                            new MaterialDialog.Builder(getActivity())
                                    .typeface(TypeFaceHelper.getTypeFace(getActivity()), TypeFaceHelper.getTypeFace(getActivity()))
                                    .title(R.string.title_not_enough_reward_points)
                                    .content(String.format(getString(R.string.not_enough_reward_points_for_batch_lyrics)
                                            ,rewardPoints, numberOfTracks))
                                    .positiveText(R.string.pos_not_enough_reward_points)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            startActivity(new Intent(getActivity(), ActivityRewardVideo.class));
                                            dialog.dismiss();
                                        }
                                    })
                                    .negativeText(R.string.neu_not_enough_reward_points)
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            getActivity().startService(new Intent(getActivity(),BatchDownloaderService.class));
                                            Toast.makeText(getActivity(),getString(R.string.batch_download_started), Toast.LENGTH_LONG).show();
                                            dialog.dismiss();
                                        }
                                    })
                                    .neutralText(R.string.cancel)
                                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .theme(Theme.DARK).show();
                            return false;
                        }
                    }
                    getActivity().startService(new Intent(getActivity(),BatchDownloaderService.class));
                    Toast.makeText(getActivity(),getString(R.string.batch_download_started), Toast.LENGTH_LONG).show();
                    return true;
                }
            });

            //reset  preference
            final Preference resetPref = findPreference(getString(R.string.pref_reset_pref));
            resetPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    resetPrefDialog();
                    return true;
                }
            });

            //remove ads
            Preference removeAdPref = findPreference(getString(R.string.pref_remove_ads_after_payment));
            removeAdPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                   // removeAdsDialog();
                    startActivity(new Intent(getActivity(), ActivityRemoveAds.class));
                    getActivity().finish();
                    return true;
                }
            });
        }

        private void mainLibBackDialog(){
            ///get current setting
            // 0 - System default   2 - custom
            int currentSelection = MyApp.getPref().getInt(getString(R.string.pref_main_library_back),0);

            new MaterialDialog.Builder(getActivity())
                    .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                    .title(R.string.title_main_library_back)
                    .items(R.array.main_lib_back_pref_array)
                    .itemsCallbackSingleChoice(currentSelection, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            /**
                             * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                             * returning false here won't allow the newly selected radio button to actually be selected.
                             **/


                            switch (which){
                                //for 0 and 1 and 2, change the pref and move on, no need to confirm anything
                                case 0:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_main_library_back),which).apply();
                                    break;

                                //for 3: custom image: ask user to pick image and change pref only upon successful picking up image
                                case 1:
                                    isMainLibraryBackground = true;
                                    CropImage.activity()
                                            .setGuidelines(CropImageView.Guidelines.ON)
                                            .setAspectRatio(9,16)
                                            .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                                            .setOutputCompressQuality(50)
                                            .start(getActivity());
                                    dialog.dismiss();
                                    break;

                            }
                            return true;
                        }
                    })
                    .positiveText(R.string.okay)
                    .show();
        }

        private void nowPlayingBackDialog(){

            ///get current setting
            // 0 - System default   1 - artist image  2 - album art 3 - custom  4- custom (if Artist image unavailable)
            int currentSelection = MyApp.getPref().getInt(getString(R.string.pref_now_playing_back),1);

            new MaterialDialog.Builder(getActivity())
                    .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                    .title(R.string.title_now_playing_back)
                    .items(R.array.now_playing_back_pref_array)
                    .itemsCallbackSingleChoice(currentSelection, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            /**
                             * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                             * returning false here won't allow the newly selected radio button to actually be selected.
                             **/


                            switch (which){
                                //for 0 and 1 and 2, change the pref and move on, no need to confirm anything
                                case 0:
                                case 1:
                                case 2:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_now_playing_back),which).apply();
                                    break;

                                //for 3: custom image: ask user to pick image and change pref only upon successful picking up image
                                case 3:
                                    isMainLibraryBackground = false;
                                    CropImage.activity()
                                            .setGuidelines(CropImageView.Guidelines.ON)
                                            .setAspectRatio(9,16)
                                            .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                                            .setOutputCompressQuality(50)
                                            .start(getActivity());
                                    dialog.dismiss();
                                    break;
                            }
                            return true;
                        }
                    })
                    .positiveText(R.string.okay)
                    .show();
        }

        private void displayExcludedFolders(){
            String excludedFoldersString = MyApp.getPref().getString(getString(R.string.pref_excluded_folders),"");
            String[] excludedFolders = excludedFoldersString.split(",");

            new MaterialDialog.Builder(getActivity())
                    .title(R.string.title_excluded_folders)
                    .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                    .items(excludedFolders)
                    .positiveText(getString(R.string.add))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            new MaterialDialog.Builder(getActivity())
                                    .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                                    .title(getString(R.string.title_how_to_add))
                                    .content(getString(R.string.content_how_to_add))
                                    .positiveText(getString(R.string.pos_how_to_add))
                                    .show();
                        }
                    })
                    .negativeText(getString(R.string.reset))
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            MyApp.getPref().edit().putString(getString(R.string.pref_excluded_folders),"").apply();
                            MusicLibrary.getInstance().RefreshLibrary();
                            Toast.makeText(getActivity(), "Excluded folders reset, refreshing Music Library..", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();

        }

        private void tabSeqDialog(){

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.tab_sequence_preference, null);
            RecyclerView rv = dialogView.findViewById(R.id.rv_for_tab_sequence);
            final TabSequenceAdapter tsa = new TabSequenceAdapter(this);
            rv.setAdapter(tsa);
            rv.setLayoutManager(new WrapContentLinearLayoutManager(MyApp.getContext()));
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(tsa);
            mItemTouchHelper = new ItemTouchHelper(callback);
            mItemTouchHelper.attachToRecyclerView(rv);

            new MaterialDialog.Builder(getActivity())
                    .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                    .title(getString(R.string.setting_tab_seqe_title))
                    .customView(dialogView,false)
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            int[] temp = tsa.getData();
                            StringBuilder str = new StringBuilder();
                            for (int aTemp : temp) {
                                str.append(aTemp).append(",");
                            }
                            MyApp.getPref().edit().putString(getString(R.string.pref_tab_seq), str.toString()).apply();
                        }
                    })
                    .show();

        }

        private void RescanLibrary(){
            MusicLibrary.getInstance().RefreshLibrary();
            final ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
                    getString(R.string.library_rescan), true);
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),getString(R.string.main_act_lib_refreshed), Toast.LENGTH_SHORT).show();
                           // Snackbar.make(rootView, "Library Refreshed", Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }

        private void shortClipDialog() {
            LinearLayout linear = new LinearLayout(getActivity());

            linear.setOrientation(LinearLayout.VERTICAL);
            final TextView text = new TextView(getActivity());
            String summary = String.valueOf(MyApp.getPref().getInt(getString(R.string.pref_hide_short_clips),10)) + " seconds";
            text.setText(summary);
            text.setTypeface(TypeFaceHelper.getTypeFace(MyApp.getContext()));
            text.setPadding(0, 10,0,0);
            text.setGravity(Gravity.CENTER);

            SeekBar seek = new SeekBar(getActivity());
            seek.setPadding(40,10,40,10);
            seek.setMax(100);
            seek.setProgress(MyApp.getPref().getInt(getString(R.string.pref_hide_short_clips),10));

            seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    text.setText(progress+" seconds");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    MyApp.getPref().edit().putInt(getString(R.string.pref_hide_short_clips),progress).apply();
                    findPreference(getString(R.string.pref_hide_short_clips)).setSummary(progress+ " seconds");
                }
            });

            linear.addView(seek);
            linear.addView(text);

            new MaterialDialog.Builder(getActivity())
                    .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                    .title(getString(R.string.title_hide_short_clips))
                    // .content(getString(R.string.lyric_art_info_content))
                    .positiveText(getString(R.string.okay))
                    .negativeText(getString(R.string.cancel))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            RescanLibrary();
                        }
                    })
                    .customView(linear,false)
                    .show();
        }

        private void hideByStartDialog(){

            String text1 = MyApp.getPref().getString(getString(R.string.pref_hide_tracks_starting_with_1),"");
            String text2 = MyApp.getPref().getString(getString(R.string.pref_hide_tracks_starting_with_2),"");
            String text3 = MyApp.getPref().getString(getString(R.string.pref_hide_tracks_starting_with_3),"");
            findPreference(getString(R.string.pref_hide_tracks_starting_with)).setSummary(text1+", "+text2+", "+text3);
            LinearLayout linear = new LinearLayout(getActivity());
            linear.setPadding(10,10,10,0);

            final EditText myEditText1 = new EditText(getActivity()); // Pass it an Activity or Context
            myEditText1.setLayoutParams(new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)); // Pass two args; must be LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, or an integer pixel value.
            myEditText1.setText(text1);
            //myEditText1.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            myEditText1.setInputType(InputType.TYPE_CLASS_TEXT);
            myEditText1.setMaxLines(1);
            linear.addView(myEditText1);

            final EditText myEditText2 = new EditText(getActivity()); // Pass it an Activity or Context
            myEditText2.setLayoutParams(new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)); // Pass two args; must be LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, or an integer pixel value.
            myEditText2.setText(text2);
           // myEditText2.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            myEditText2.setMaxLines(1);
            myEditText2.setInputType(InputType.TYPE_CLASS_TEXT);
            linear.addView(myEditText2);

            final EditText myEditText3 = new EditText(getActivity()); // Pass it an Activity or Context
            myEditText3.setLayoutParams(new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)); // Pass two args; must be LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, or an integer pixel value.
            myEditText3.setText(text3);
            //myEditText3.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            myEditText3.setInputType(InputType.TYPE_CLASS_TEXT);
            myEditText3.setMaxLines(1);
            linear.addView(myEditText3);

            TextView tv = new TextView(getActivity());
            tv.setText(getString(R.string.case_sensitive_text));
            tv.setTypeface(TypeFaceHelper.getTypeFace(MyApp.getContext()));
            tv.setPadding(0,10,0,0);
            linear.addView(tv);

            linear.setOrientation(LinearLayout.VERTICAL);


            new MaterialDialog.Builder(getActivity())
                    .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                    .title(getString(R.string.title_hide_tracks_starting_with))
                    // .content(getString(R.string.lyric_art_info_content))
                    .positiveText(getString(R.string.okay))
                    .negativeText(getString(R.string.cancel))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            String text1 = myEditText1.getText().toString().trim();
                            MyApp.getPref().edit().putString(getString(R.string.pref_hide_tracks_starting_with_1),text1).apply();

                            String text2 = myEditText2.getText().toString().trim();
                            MyApp.getPref().edit().putString(getString(R.string.pref_hide_tracks_starting_with_2),text2).apply();

                            String text3 = myEditText3.getText().toString().trim();
                            MyApp.getPref().edit().putString(getString(R.string.pref_hide_tracks_starting_with_3),text3).apply();

                            findPreference(getString(R.string.pref_hide_tracks_starting_with)).setSummary(text1+", "+text2+", "+text3);

                            RescanLibrary();
                        }
                    })
                    .customView(linear,true)
                    .show();
        }

        private void ShakeActionDialog(){

            new MaterialDialog.Builder(getActivity())
                    .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                    .title(getString(R.string.title_shake_action))
                    .items((CharSequence[]) new String[]{NEXT,PLAY_PAUSE,PREVIOUS})
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            switch (text.toString()){

                                case NEXT:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_shake_action)
                                            ,Constants.SHAKE_ACTIONS.NEXT).apply();
                                    findPreference(getString(R.string.pref_shake_action)).setSummary(NEXT);
                                    break;

                                case PLAY_PAUSE:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_shake_action)
                                            ,Constants.SHAKE_ACTIONS.PLAY_PAUSE).apply();
                                    findPreference(getString(R.string.pref_shake_action)).setSummary(PLAY_PAUSE);
                                    break;

                                case PREVIOUS:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_shake_action)
                                            ,Constants.SHAKE_ACTIONS.PREVIOUS).apply();
                                    findPreference(getString(R.string.pref_shake_action)).setSummary(PREVIOUS);
                                    break;
                            }
                        }
                    })
                    .show();
        }

        private void BaseThemeSelectionDialog(){
            new MaterialDialog.Builder(getActivity())
                    .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                    .title(getString(R.string.title_theme))
                    .items((CharSequence[]) new String[]{DARK,GLOSSY})
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            switch (text.toString()){

                                case DARK:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme)
                                            , Constants.PRIMARY_COLOR.DARK).apply();
                                    findPreference(getString(R.string.pref_theme)).setSummary(DARK);
                                    break;

                                case LIGHT:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme)
                                            , Constants.PRIMARY_COLOR.LIGHT).apply();
                                    findPreference(getString(R.string.pref_theme)).setSummary(LIGHT);
                                    break;

                                case GLOSSY:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme)
                                            , Constants.PRIMARY_COLOR.GLOSSY).apply();
                                    findPreference(getString(R.string.pref_theme)).setSummary(GLOSSY);
                                    break;
                            }

                            restartSettingsActivity();
                        }
                    })
                    .show();
        }

        private void PrimarySelectionDialog(){

            new MaterialDialog.Builder(getActivity())
                    .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                    .title(getString(R.string.title_theme_color))
                    .items((CharSequence[]) new String[]{MANUAL_INPUT,BLACK, GUNMETAL, HALAYA_UBE, INTERNATIONAL_ORANGE
                            , JACARTA, JAPANESE_VIOLET, MAGENTA, MAASTRICHT_BLUE
                            , MAROON,PINE_TREE, POLIE_BLUE,ANTIQUE_RUBY,BONDI_BLUE,PINK
                            ,AMBER,BLUE,RED,BLUE_MAGNETA_VIOLET,GREEN,YELLOW
                            ,BYZANTIUM,DARK_SLATE_GRAY,ANTIQUE_BRONZE,EGGPLANT,FRENCH_BISTRE,DEEP_CHESTNUT})
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            switch (text.toString()){

                                case BLACK:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.BLACK).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(BLACK);
                                    break;

                                case BLUE:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.BLUE_CATALINA).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(BLUE);
                                    break;

                                case RED:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.RED_CARMINE).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(RED);
                                    break;

                                case YELLOW:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.AMBER).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(YELLOW);
                                    break;

                                case PINK:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.PINK_CERISE).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(PINK);
                                    break;

                                case GREEN:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.GREEN_DARTMOUTH).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(GREEN);
                                    break;

                                case AMBER:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.CYBER_GRAPE).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(AMBER);
                                    break;

                                case BONDI_BLUE:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.BONDI_BLUE).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(BONDI_BLUE);
                                    break;

                                case BYZANTIUM:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.BYZANTIUM).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(BYZANTIUM);
                                    break;

                                case DARK_SLATE_GRAY:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.DARK_SLATE_GRAY).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(DARK_SLATE_GRAY);
                                    break;


                                case ANTIQUE_BRONZE:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.ANTIQUE_BRONZE).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(ANTIQUE_BRONZE);
                                    break;

                                case ANTIQUE_RUBY:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.ANTIQUE_RUBY).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(ANTIQUE_RUBY);
                                    break;

                                case BLUE_MAGNETA_VIOLET:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.BLUE_MAGNETA_VIOLET).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(BLUE_MAGNETA_VIOLET);
                                    break;

                                case EGGPLANT:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.EGGPLANT).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(EGGPLANT);
                                    break;

                                case FRENCH_BISTRE:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.FRENCH_BISTRE).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(FRENCH_BISTRE);
                                    break;

                                case DEEP_CHESTNUT:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.DEEP_CHESTNUT).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(DEEP_CHESTNUT);
                                    break;

                                case GUNMETAL:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.GUNMETAL).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(GUNMETAL);
                                    break;

                                case HALAYA_UBE:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.HALAYA_UBE).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(HALAYA_UBE);
                                    break;

                                case INTERNATIONAL_ORANGE:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.INTERNATIONAL_ORANGE).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(INTERNATIONAL_ORANGE);
                                    break;

                                case JACARTA:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.JACARTA).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(JACARTA);
                                    break;

                                case JAPANESE_VIOLET:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.JAPANESE_VIOLET).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(JAPANESE_VIOLET);
                                    break;

                                case MAGENTA:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.MAGENTA).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(MAGENTA);
                                    break;

                                case MAASTRICHT_BLUE:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.MAASTRICHT_BLUE).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(MAASTRICHT_BLUE);
                                    break;

                                case MAROON:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.MAROON).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(MAROON);
                                    break;

                                case PINE_TREE:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.PINE_TREE).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(PINE_TREE);
                                    break;

                                case POLIE_BLUE:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                            , Constants.PRIMARY_COLOR.POLIE_BLUE).apply();
                                    findPreference(getString(R.string.pref_theme_color)).setSummary(POLIE_BLUE);
                                    break;

                                case MANUAL_INPUT:
                                    int current_color = MyApp.getPref().getInt(getString(R.string.pref_theme_color),-1);
                                    String pre_fill_text = "#";
                                    if(current_color!=-1) {
                                        pre_fill_text = String.format("#%06X", (0xFFFFFF & current_color));
                                    }
                                    new MaterialDialog.Builder(getActivity())
                                            .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                                            .title(R.string.manual_color_title)
                                            .content(R.string.manual_color_content)
                                            .inputType(InputType.TYPE_CLASS_TEXT)
                                            .autoDismiss(false)
                                            .input(getString(R.string.manual_color_hint), pre_fill_text, new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                    // Do something
                                                    try {
                                                        int color = Color.parseColor(input.toString());
                                                        MyApp.getPref().edit().putInt(getString(R.string.pref_theme_color)
                                                                , color).apply();
                                                        findPreference(getString(R.string.pref_theme_color)).setSummary(MANUAL_INPUT);
                                                    }catch (Exception e){
                                                        dialog.getInputEditText().setError(getString(R.string.manual_color_error));
                                                        return;
                                                    }
                                                    dialog.dismiss();
                                                    restartSettingsActivity();
                                                }
                                            }).show();
                                    break;

                            }

                            if(!text.toString().equals(MANUAL_INPUT)) {
                                restartSettingsActivity();
                            }
                        }


                    })
                    .show();
        }

        private void resetPrefDialog(){

            new MaterialDialog.Builder(getActivity())
                    .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                    .title(getString(R.string.title_reset_pref) + " ?")
                    // .content(getString(R.string.lyric_art_info_content))
                    .positiveText(getString(R.string.yes))
                    .negativeText(getString(R.string.cancel))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            SharedPreferences.Editor editor= MyApp.getPref().edit();

                            editor.putInt(getString(R.string.pref_theme)
                                    , Constants.PRIMARY_COLOR.GLOSSY);

                            editor.putInt(getString(R.string.pref_theme_color)
                                    , Constants.PRIMARY_COLOR.ANTIQUE_RUBY);

                            editor.putInt(getString(R.string.pref_text_font)
                                    , Constants.TYPEFACE.SOFIA);

                            editor.remove(getString(R.string.pref_tab_seq));

                            editor.putBoolean(getString(R.string.pref_lock_screen_album_Art),true);

                            editor.putBoolean(getString(R.string.pref_shake),false);

                            editor.putInt(getString(R.string.pref_hide_short_clips),10);

                            editor.putString(getString(R.string.pref_hide_tracks_starting_with_1),"");
                            editor.putString(getString(R.string.pref_hide_tracks_starting_with_2),"");
                            editor.putString(getString(R.string.pref_hide_tracks_starting_with_3),"");

                            editor.putString(getString(R.string.pref_excluded_folders),"");

                            editor.putBoolean(getString(R.string.pref_prefer_system_equ ), true);

                            editor.putInt(getString(R.string.pref_main_library_back), 0);

                            editor.putInt(getString(R.string.pref_now_playing_back), 0);

                            editor.putBoolean(getString(R.string.pref_hide_lock_button),false);

                            editor.putBoolean(getString(R.string.pref_notifications), true);

                            editor.putBoolean(getString(R.string.pref_continuous_playback), false);

                            editor.putBoolean(getString(R.string.pref_data_saver), false);

                            editor.apply();


                            restartSettingsActivity();
                        }
                    })
                   // .customView(linear,false)
                    .show();
        }

        private void fontPrefSelectionDialog(){

            new MaterialDialog.Builder(getActivity())
                    .typeface(TypeFaceHelper.getTypeFace(MyApp.getContext()),TypeFaceHelper.getTypeFace(MyApp.getContext()))
                    .title(getString(R.string.title_text_font))
                    .items((CharSequence[]) new String[]{ ASAP, MONOSPACE, SOFIA, RISQUE, SYSTEM_DEFAULT})
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            switch (text.toString()){
                                case MONOSPACE:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_text_font), Constants.TYPEFACE.MONOSPACE).apply();
                                    findPreference(getString(R.string.pref_text_font)).setSummary(MONOSPACE);
                                    break;

                                case SOFIA:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_text_font), Constants.TYPEFACE.SOFIA).apply();
                                    findPreference(getString(R.string.pref_text_font)).setSummary(SOFIA);
                                    break;

                                case RISQUE:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_text_font), Constants.TYPEFACE.RISQUE).apply();
                                    findPreference(getString(R.string.pref_text_font)).setSummary(RISQUE);
                                    break;

                                case ASAP:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_text_font), Constants.TYPEFACE.ASAP).apply();
                                    findPreference(getString(R.string.pref_text_font)).setSummary(ASAP);
                                    break;

                                case SYSTEM_DEFAULT:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_text_font), Constants.TYPEFACE.SYSTEM_DEFAULT).apply();
                                    findPreference(getString(R.string.pref_text_font)).setSummary(SYSTEM_DEFAULT);
                                    break;
                            }

                            MyApp.getPref().edit().putBoolean(getString(R.string.pref_font_already_logged), false).apply();

                            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                                    .setDefaultFontPath(TypeFaceHelper.getTypeFacePath())
                                    .setFontAttrId(R.attr.fontPath)
                                    .build());

                            restartSettingsActivity();
                        }
                    })
                    .show();
        }

        private void restartSettingsActivity() {
            Intent intent = getActivity().getIntent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("ad", false);
            getActivity().finish();
            startActivity(intent);
        }

        @Override
        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
            mItemTouchHelper.startDrag(viewHolder);
        }
    }

    private static class TabSequenceAdapter extends RecyclerView.Adapter<TabSequenceAdapter.MyViewHolder>
            implements ItemTouchHelperAdapter{

        private LayoutInflater inflater;
        int[] data = new int[Constants.TABS.NUMBER_OF_TABS];
        private OnStartDragListener mDragStartListener;

        TabSequenceAdapter(OnStartDragListener dragStartListener){
            mDragStartListener = dragStartListener;
            String savedTabSeq = MyApp.getPref().getString(MyApp.getContext().getString(R.string.pref_tab_seq), Constants.TABS.DEFAULT_SEQ);
            StringTokenizer st = new StringTokenizer(savedTabSeq, ",");
            for (int i = 0; i < Constants.TABS.NUMBER_OF_TABS; i++) {
                data[i] = Integer.parseInt(st.nextToken());
            }
        }

        @Override
        public TabSequenceAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            inflater=LayoutInflater.from(MyApp.getContext());
            final View view = inflater.inflate(R.layout.tab_sequence_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final TabSequenceAdapter.MyViewHolder holder, int position) {
            //holder.title.setText(data.get(0));
                switch (data[position]) {
                    case Constants.TABS.ALBUMS:
                        holder.title.setText(MyApp.getContext().getString(R.string.tab_album));
                        break;

                    case Constants.TABS.ARTIST:
                        holder.title.setText(MyApp.getContext().getString(R.string.tab_artist));
                        break;

                    case Constants.TABS.FOLDER:
                        holder.title.setText(MyApp.getContext().getString(R.string.tab_folder));
                        break;

                    case Constants.TABS.GENRE:
                        holder.title.setText(MyApp.getContext().getString(R.string.tab_genre));
                        break;

                    case Constants.TABS.PLAYLIST:
                        holder.title.setText(MyApp.getContext().getString(R.string.tab_playlist));
                        break;

                    case Constants.TABS.TRACKS:
                        holder.title.setText(MyApp.getContext().getString(R.string.tab_track));
                        break;
                }

            holder.handle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (MotionEventCompat.getActionMasked(motionEvent) ==
                            MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(holder);
                    }
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.length;
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            int temp = data[fromPosition];
            data[fromPosition]=data[toPosition];
            data[toPosition]=temp;
            notifyItemMoved(fromPosition,toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(int position) {
            notifyItemChanged(position);
        }

        public int[] getData(){
            return data;
        }
        class MyViewHolder extends RecyclerView.ViewHolder{
            TextView title;
            ImageView handle;

            MyViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tab_name);
                title.setTypeface(TypeFaceHelper.getTypeFace(MyApp.getContext()));
                //title.setTypeface(TypeFaceHelper.getTypeFace());

                handle = itemView.findViewById(R.id.handle_for_drag);
            }
        }
    }

    private static class WrapContentLinearLayoutManager extends LinearLayoutManager {
        WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        //... constructor
        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("probe", "meet a IOOBE in RecyclerView");
            }
        }
    }
}
