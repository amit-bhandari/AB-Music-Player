package com.music.player.bhandari.m.activity;

import android.annotation.SuppressLint;
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
import android.support.v7.widget.GridLayoutManager;
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
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.messaging.FirebaseMessaging;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.BulkArtInfoGrabber;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.rewards.RewardPoints;
import com.music.player.bhandari.m.service.BatchDownloaderService;
import com.music.player.bhandari.m.service.NotificationListenerService;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
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

public class ActivitySettings extends AppCompatActivity {

    private  int launchedFrom = 0;
    private AdView mAdView;
    private PlayerService playerService;

    //flag to know for which background, crop image is invoked
    //true = main library
    //false = now playing
    private static boolean isMainLibraryBackground;

    private static final int MAIN_LIB = 0;
    private static final int NOW_PLAYING = 1;
    private static final int NAVIGATION_DRAWER = 2;
    private static final int DEFAULT_ALBUM_ART = 3;

    private static int backgroundSelectionStatus = -1;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {

        //if player service not running, kill the app
        if(MyApp.getService()==null){
            UtilityFun.restartApp();
            finish();
            return;
        }

        playerService = MyApp.getService();


        ColorHelper.setStatusBarGradiant(this);

        int themeSelector = MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT);
        switch (themeSelector){
            case Constants.PRIMARY_COLOR.DARK:
                setTheme(R.style.AppThemeDarkPref);
                break;

            case Constants.PRIMARY_COLOR.GLOSSY:
                setTheme(R.style.AppThemeDarkPref);
                break;

            case Constants.PRIMARY_COLOR.LIGHT:
                setTheme(R.style.AppThemeLight);
                break;
        }

        super.onCreate(savedInstanceState);

        launchedFrom = getIntent().getIntExtra("launchedFrom",0);
        setContentView(R.layout.acitivty_settings);

        if(false/*AppLaunchCountManager.isEligibleForInterstialAd() && !UtilityFun.isAdsRemoved()
                && AppLaunchCountManager.isEligibleForBannerAds()*/) {

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

        //findViewById(R.id.root_view_settings).setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());

        Toolbar toolbar = findViewById(R.id.toolbar_);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.GetStatusBarColor());
        }*/

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
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        MyApp.isAppVisible = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        if(MyApp.getService()==null){
            UtilityFun.restartApp();
            finish();
        }
        MyApp.isAppVisible = true;
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                playerService.play();
                break;

            case KeyEvent.KEYCODE_MEDIA_NEXT:
                playerService.nextTrack();
                break;

            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                playerService.prevTrack();
                break;

            case KeyEvent.KEYCODE_MEDIA_STOP:
                playerService.stop();
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
                switch (backgroundSelectionStatus){
                    case MAIN_LIB:
                        savePath = MyApp.getContext().getFilesDir() + getString(R.string.main_lib_back_custom_image);
                        break;

                    case NOW_PLAYING:
                        savePath = MyApp.getContext().getFilesDir() + getString(R.string.now_playing_back_custom_image);
                        break;

                    case DEFAULT_ALBUM_ART:
                        savePath = MyApp.getContext().getFilesDir() + getString(R.string.def_album_art_custom_image);
                        break;

                    case NAVIGATION_DRAWER:
                    default:
                        savePath = MyApp.getContext().getFilesDir() + getString(R.string.nav_back_custom_image);
                        break;
                }

                File toFile = new File(savePath);
                boolean b = fromFile.renameTo(toFile);
                Log.d(Constants.TAG, "onActivityResult: saved custom image size : " + toFile.length()/(1024));


                if(b){
                    switch (backgroundSelectionStatus){
                        case MAIN_LIB:
                            MyApp.getPref().edit().putInt(getString(R.string.pref_main_library_back),1).apply();
                            break;

                        case NOW_PLAYING:
                            MyApp.getPref().edit().putInt(getString(R.string.pref_now_playing_back),3).apply();
                            break;

                        case DEFAULT_ALBUM_ART:
                            MyApp.getPref().edit().putInt(getString(R.string.pref_default_album_art),1).apply();
                            break;

                        case NAVIGATION_DRAWER:
                        default:
                            MyApp.getPref().edit().putInt(getString(R.string.pref_nav_library_back),1).apply();
                            break;
                    }

                    Toast.makeText(this, "Background successfully updated!", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Failed to save file, try some different image!", Toast.LENGTH_SHORT).show();
                }

                Log.d(Constants.TAG, "onActivityResult: "+result.toString());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Failed to select image, try again!", Toast.LENGTH_SHORT).show();
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

        final String DARK = "Dark";
        final String LIGHT = "Light";
        final String GLOSSY = "Glossy";

        final String MONOSPACE = "Monospace";
        final String SOFIA = "Sofia";
        final String MANROPE = "Manrope (Recommended)";
        final String ASAP = "Asap";
        final String SYSTEM_DEFAULT = "System Default";

        final String LIST = "List View";
        final String GRID = "Grid View";

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

            //Theme color
            Preference primaryColorPref = findPreference(getString(R.string.pref_theme_color));

            primaryColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    //PrimarySelectionDialog();
                    themeSelectionDialog();
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

            //Main library back
            final Preference navLibBackPref = findPreference(getString(R.string.pref_nav_library_back));
            navLibBackPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    navBackDialog();
                    return true;
                }
            });

            //Main library back
            final Preference defAlbumArtPref = findPreference(getString(R.string.pref_default_album_art));
            defAlbumArtPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    defAlbumArtDialog();
                    return true;
                }
            });

            //text font
            Preference fontPref = findPreference(getString(R.string.pref_text_font));
            int textFontPref = MyApp.getPref().getInt(getString(R.string.pref_text_font), Constants.TYPEFACE.MANROPE);
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

                case Constants.TYPEFACE.MANROPE:
                    findPreference(getString(R.string.pref_text_font)).setSummary(MANROPE);
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
            Preference albumLibView = findPreference(getString(R.string.pref_album_lib_view));
            if(MyApp.getPref().getBoolean(getString(R.string.pref_album_lib_view), true)){
                albumLibView.setSummary(GRID);
            }else {
                albumLibView.setSummary(LIST);
            }

            albumLibView.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    albumViewDialog();
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
                    new MyDialogBuilder(getActivity())
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

                    MaterialDialog dialog = new MyDialogBuilder(getActivity())
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
                            .build();

                    //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

                    dialog.show();

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

                    MaterialDialog dialog = new MyDialogBuilder(getActivity())
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
                            .build();

                    //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

                    dialog.show();

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

                        MaterialDialog dialog = new MyDialogBuilder(getActivity())
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
                                .build();

                        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

                        dialog.show();

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
                            new MyDialogBuilder(getActivity())
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

        private void albumViewDialog(){
            MaterialDialog dialog = new MyDialogBuilder(getActivity())
                    .title(getString(R.string.title_album_lib_view))
                    .items((CharSequence[]) new String[]{LIST, GRID})
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            switch (text.toString()){
                                case LIST:
                                    MyApp.getPref().edit().putBoolean(getString(R.string.pref_album_lib_view), false).apply();
                                    findPreference(getString(R.string.pref_album_lib_view)).setSummary(LIST);
                                    break;

                                case GRID:
                                    MyApp.getPref().edit().putBoolean(getString(R.string.pref_album_lib_view), true).apply();
                                    findPreference(getString(R.string.pref_album_lib_view)).setSummary(GRID);
                                    break;
                            }
                        }
                    })
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();
        }

        private void navBackDialog(){
            ///get current setting
            // 0 - System default   2 - custom
            int currentSelection = MyApp.getPref().getInt(getString(R.string.pref_nav_library_back),0);

            MaterialDialog dialog =new MyDialogBuilder(getActivity())
                    .title(R.string.title_nav_back)
                    .items(R.array.nav_back_pref_array)
                    .itemsCallbackSingleChoice(currentSelection, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            /**
                             * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                             * returning false here won't allow the newly selected radio button to actually be selected.
                             **/


                            switch (which){
                                //for 0, change the pref and move on, no need to confirm anything
                                case 0:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_nav_library_back),which).apply();
                                    break;

                                //for 3: custom image: ask user to pick image and change pref only upon successful picking up image
                                case 1:
                                    backgroundSelectionStatus = NAVIGATION_DRAWER;
                                    CropImage.activity()
                                            .setGuidelines(CropImageView.Guidelines.ON)
                                            .setAspectRatio(11,16)
                                            .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                                            .setOutputCompressQuality(80)
                                            .start(getActivity());
                                    dialog.dismiss();
                                    break;

                            }
                            return true;
                        }
                    })
                    .positiveText(R.string.okay)
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();
        }

        private void defAlbumArtDialog(){
            ///get current setting
            // 0 - System default   2 - custom
            int currentSelection = MyApp.getPref().getInt(getString(R.string.pref_default_album_art),0);

            MaterialDialog dialog =new MyDialogBuilder(getActivity())
                    .title(R.string.nav_default_album_art)
                    .items(R.array.def_album_art_pref_array)
                    .itemsCallbackSingleChoice(currentSelection, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            /**
                             * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                             * returning false here won't allow the newly selected radio button to actually be selected.
                             **/


                            switch (which){
                                //for 0, change the pref and move on, no need to confirm anything
                                case 0:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_default_album_art),which).apply();
                                    break;

                                //for 3: custom image: ask user to pick image and change pref only upon successful picking up image
                                case 1:
                                    backgroundSelectionStatus = DEFAULT_ALBUM_ART;
                                    CropImage.activity()
                                            .setGuidelines(CropImageView.Guidelines.ON)
                                            .setAspectRatio(1,1)
                                            .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                                            .setOutputCompressQuality(80)
                                            .start(getActivity());
                                    dialog.dismiss();
                                    break;

                            }
                            return true;
                        }
                    })
                    .positiveText(R.string.okay)
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();
        }

        private void mainLibBackDialog(){
            ///get current setting
            // 0 - System default   2 - custom
            int currentSelection = MyApp.getPref().getInt(getString(R.string.pref_main_library_back),0);

            MaterialDialog dialog = new MyDialogBuilder(getActivity())
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
                                //for 0, change the pref and move on, no need to confirm anything
                                case 0:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_main_library_back),which).apply();
                                    break;

                                //for 3: custom image: ask user to pick image and change pref only upon successful picking up image
                                case 1:
                                    backgroundSelectionStatus = MAIN_LIB;
                                    CropImage.activity()
                                            .setGuidelines(CropImageView.Guidelines.ON)
                                            .setAspectRatio(11,16)
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
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();
        }

        private void nowPlayingBackDialog(){

            ///get current setting
            // 0 - System default   1 - artist image  2 - album art 3 - custom  4- custom (if Artist image unavailable)
            int currentSelection = MyApp.getPref().getInt(getString(R.string.pref_now_playing_back),1);

            MaterialDialog dialog = new MyDialogBuilder(getActivity())
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
                                    backgroundSelectionStatus = NOW_PLAYING;
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
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();
        }

        private void displayExcludedFolders(){
            String excludedFoldersString = MyApp.getPref().getString(getString(R.string.pref_excluded_folders),"");
            String[] excludedFolders = excludedFoldersString.split(",");

            MaterialDialog dialog = new MyDialogBuilder(getActivity())
                    .title(R.string.title_excluded_folders)
                    .items(excludedFolders)
                    .positiveText(getString(R.string.add))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            new MyDialogBuilder(getActivity())
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
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();

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

            MaterialDialog dialog = new MyDialogBuilder(getActivity())
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
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();

        }

        private void themeSelectionDialog(){

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.theme_selector_dialog, null);
            RecyclerView rv = dialogView.findViewById(R.id.rv_for_theme_selector);
            final ThemeSelectorAdapter tsa = new ThemeSelectorAdapter();
            rv.setAdapter(tsa);
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getActivity());
            layoutManager.setFlexDirection(FlexDirection.ROW);
            layoutManager.setJustifyContent(JustifyContent.SPACE_EVENLY);
            rv.setLayoutManager(layoutManager);
            //rv.setLayoutManager(new GridLayoutManager(getActivity(), 4));

            MaterialDialog dialog = new MyDialogBuilder(getActivity())
                    .title("Select theme")
                    .customView(dialogView,false)
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            //@todo do something yo
                        }
                    })
                    .positiveText("Apply")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            restartSettingsActivity();
                        }
                    })
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();

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
                           // Snackbar.make(rootView, "Library Refreshed", Snackbar.LENGTH_SHORT).show();
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

            MaterialDialog dialog = new MyDialogBuilder(getActivity())
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
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();
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


            MaterialDialog dialog = new MyDialogBuilder(getActivity())
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
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();
        }

        private void ShakeActionDialog(){

            MaterialDialog dialog = new MyDialogBuilder(getActivity())
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
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();
        }

        private void resetPrefDialog(){

            MaterialDialog dialog = new MyDialogBuilder(getActivity())
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
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();
        }

        private void fontPrefSelectionDialog(){

            MaterialDialog dialog = new MyDialogBuilder(getActivity())
                    .title(getString(R.string.title_text_font))
                    .items((CharSequence[]) new String[]{ MANROPE, ASAP, MONOSPACE, SOFIA, SYSTEM_DEFAULT})
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

                                case MANROPE:
                                    MyApp.getPref().edit().putInt(getString(R.string.pref_text_font), Constants.TYPEFACE.MANROPE).apply();
                                    findPreference(getString(R.string.pref_text_font)).setSummary(MANROPE);
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
                    .build();

            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

            dialog.show();
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

    private static class ThemeSelectorAdapter extends RecyclerView.Adapter<ThemeSelectorAdapter.MyViewHolder>{

        private LayoutInflater inflater;
        private int currentSelectedItem;

        ThemeSelectorAdapter(){
            currentSelectedItem = MyApp.getSelectedThemeId();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            inflater=LayoutInflater.from(MyApp.getContext());
            final View view = inflater.inflate(R.layout.theme_selection_item, parent, false);
            return new ThemeSelectorAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            holder.view.setBackgroundDrawable(ColorHelper.GetGradientDrawable(position));
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentSelectedItem = holder.getAdapterPosition();
                    MyApp.setSelectedThemeId(holder.getAdapterPosition());
                    notifyDataSetChanged();
                }
            });
            if(currentSelectedItem==position){
                holder.tick.setVisibility(View.VISIBLE);
            }else {
                holder.tick.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return ColorHelper.GetNumberOfThemes();
        }

        class MyViewHolder extends RecyclerView.ViewHolder{
            View view;
            View tick;

            MyViewHolder(View itemView) {
                super(itemView);
                view = itemView.findViewById(R.id.themeView);
                tick = itemView.findViewById(R.id.tick);
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
