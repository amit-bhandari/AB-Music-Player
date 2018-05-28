package com.music.player.bhandari.m.activity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.music.player.bhandari.m.BuildConfig;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.customViews.RoundedImageView;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.rewards.RewardPoints;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.utils.AppLaunchCountManager;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.model.PlaylistManager;
import com.music.player.bhandari.m.utils.SignUp;
import com.music.player.bhandari.m.utils.UtilityFun;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import jp.wasabeef.blurry.Blurry;
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

public class ActivityMain extends AppCompatActivity
        implements ActionMode.Callback, NavigationView.OnNavigationItemSelectedListener, View.OnClickListener
        , SwipeRefreshLayout.OnRefreshListener , PopupMenu.OnMenuItemClickListener, GoogleApiClient.OnConnectionFailedListener{

    final static String FB_URL = "http://www.facebook.com/abmusicoffline/";
    final static String WEBSITE = "http://www.thetechguru.in";
    final static String GITHUB = "https://github.com/amit-bhandari/AB-Music-Player";
    final static String INSTA_WEBSITE = "https://www.instagram.com/_amit_bhandari/?hl=en";
    final static String AB_REMOTE_WALL_URL = "https://play.google.com/store/apps/details?id=in.thetechguru.walle.remote.abremotewallpaperchanger&hl=en";

    private long mLastClickTime = 0;
    private AdView mAdView;

    //to receive broadcast to update mini player
    private  BroadcastReceiver mReceiverForMiniPLayerUpdate;
    private BroadcastReceiver mReceiverForLibraryRefresh;


    public static final String NOTIFY_BACK_PRESSED="BACK_PRESSED";

    private  ViewPager viewPager;
    private  ViewPagerAdapter viewPagerAdapter;
    private  ImageView buttonPlay;
    private ImageView albumArt;
    private  TextView songNameMiniPlayer,artistNameMiniPlayer;
    private  NavigationView navigationView;
    private FloatingActionButton fab_right_side, fab_lock;
    private SeekBar seekBar;
    private View rootView;


    //bind player service
    private PlayerService playerService;
    //search box relaated things
    private  MenuItem mSearchAction;
    private  boolean isSearchOpened = false;
    private  EditText editSearch;
    private  String searchQuery="";
    private  Handler mHandler;
    private InputMethodManager imm;

    private  boolean stopProgressRunnable = false;
    private boolean updateTimeTaskRunning = false;

    private String currentPageSort = ""; //holds the value for pref id for current page sort by

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 7;

    //tab sequence
    int[] savedTabSeqInt = {0,1,2,3,4,5};

    private Bitmap mainLibBackBitmap;

    //rate dialog reward variables
    private static int RATE_REWARD_BONUS = 1000;

    @Override
    protected void onNewIntent(Intent intent) {
        //go to tracks tab when clicked on add button in playlist section
        int i = intent.getIntExtra("move_to_tab",-1);

        int currentItemToBeSet=0;
        for(int tab:savedTabSeqInt){
            if(tab==i){
                break;
            }
            currentItemToBeSet ++;
        }

        if(viewPager!=null && i!=-1){
            viewPager.setCurrentItem(currentItemToBeSet);
        }

        boolean b = intent.getBooleanExtra("refresh",false);
        if(b){
            //data changed in edit track info activity, update item
            String originalTitle = intent.getStringExtra("originalTitle");
            int position = intent.getIntExtra("position", -1);
            String title = intent.getStringExtra("title");
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");

            if(playerService.getCurrentTrack().getTitle().equals(originalTitle)){
                //current song is playing, update  track item
                playerService.updateTrackItem(playerService.getCurrentTrackPosition(),playerService.getCurrentTrack().getId(),title,artist,album);
                playerService.PostNotification();
                updateUI(false);
            }

            if(viewPagerAdapter.getItem(viewPager.getCurrentItem()) instanceof FragmentAlbumLibrary){
                //this should not happen
            }else if(viewPagerAdapter.getItem(viewPager.getCurrentItem()) instanceof FragmentLibrary){
                ((FragmentLibrary) viewPagerAdapter.getItem(viewPager.getCurrentItem()))
                        .updateItem(position, title, artist, album);
            }

        }

        super.onNewIntent(intent);
    }
    @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //bind music service
        //startService(new Intent(this,playerService.class));

        //if player service not running, kill the app
        playerService = MyApp.getService();
        if(playerService==null){
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

        //TypeFaceHelper.setDefaultFont(this, "monospace", "DancingScript-Regular.otf");

        setContentView(R.layout.activity_main);

        seekBar = findViewById(R.id.seekbar);
        seekBar.setMax(100);
        seekBar.setPadding(0,0,0,0);
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        rootView = findViewById(R.id.root_view_main_activity);


        // Obtain the Firebase Analytics instance.
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        //Sets whether analytics collection is enabled for this app on this device.
        firebaseAnalytics.setAnalyticsCollectionEnabled(true);

        //Sets the duration of inactivity that terminates the current session. The default value is 1800000 (30 minutes).
        firebaseAnalytics.setSessionTimeoutDuration(10000);


        if( /*AppLaunchCountManager.isEligibleForInterstialAd() &&*/AppLaunchCountManager.isEligibleForBannerAds() && !UtilityFun.isAdsRemoved() ) {
                MobileAds.initialize(getApplicationContext(), getString(R.string.banner_main_activity));
                mAdView = findViewById(R.id.adView);
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

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setBackgroundDrawable(ColorHelper.getColoredThemeGradientDrawable());

        findViewById(R.id.app_bar_layout).setBackgroundColor(ColorHelper.getPrimaryColor());
        findViewById(R.id.tabs).setBackgroundColor(ColorHelper.getPrimaryColor());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.getDarkPrimaryColor());
        }

        setTitle(getString(R.string.abm_title));

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mHandler = new Handler();

        Toolbar toolbar = findViewById(R.id.toolbar);
        try {
            toolbar.setCollapsible(false);
        }catch (Exception ignored){

        }
        setSupportActionBar(toolbar);

        mReceiverForMiniPLayerUpdate=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI(true);
            }
        };

        mReceiverForLibraryRefresh=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //updateUI();
                if(MusicLibrary.getInstance().getDefaultTracklistNew().isEmpty()){
                    Snackbar.make(rootView, getString(R.string.main_act_empty_lib), Snackbar.LENGTH_LONG).show();
                }
            }
        };


        LinearLayout miniPlayer = (LinearLayout) findViewById(R.id.mini_player);
        miniPlayer.setOnClickListener(this);

        buttonPlay= findViewById(R.id.play_pause_mini_player);
        buttonPlay.setOnClickListener(this);

        ImageView buttonNext = findViewById(R.id.next_mini_plaayrer);
        buttonNext.setOnClickListener(this);

        songNameMiniPlayer= findViewById(R.id.song_name_mini_player);

        artistNameMiniPlayer= findViewById(R.id.artist_mini_player);

        albumArt = findViewById(R.id.album_art_mini_player);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        //get tab sequence
        final String savedTabSeq = MyApp.getPref().getString(getString(R.string.pref_tab_seq), Constants.TABS.DEFAULT_SEQ);
        StringTokenizer st = new StringTokenizer(savedTabSeq, ",");
        savedTabSeqInt = new int[Constants.TABS.NUMBER_OF_TABS];
        for (int i = 0; i < Constants.TABS.NUMBER_OF_TABS; i++) {
            savedTabSeqInt[i] = Integer.parseInt(st.nextToken());
        }

        viewPager = findViewById(R.id.viewpager);

        // 0 - System default   1 - custom
        int currentMainLbBackground = MyApp.getPref().getInt(getString(R.string.pref_main_library_back),0);

        switch (currentMainLbBackground){
            case 0:
                setSystemDefaultBackground();
                break;

            case 1:
                setBlurryBackground(getMainLibBackBitmap());
                break;
        }


        setupViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.v(Constants.TAG,"position : " + viewPager.getCurrentItem());
                invalidateOptionsMenu();


                if(savedTabSeqInt[position]==Constants.TABS.PLAYLIST){
                    fab_right_side.setImageDrawable(ContextCompat.getDrawable(ActivityMain.this,R.drawable.ic_add_black_24dp));
                }else {
                    fab_right_side.setImageDrawable(ContextCompat.getDrawable(ActivityMain.this,R.drawable.ic_shuffle_black_24dp));
                }
                if(!searchQuery.equals("")){
                    try {
                        if (savedTabSeqInt[position] != Constants.TABS.FOLDER
                                && savedTabSeqInt[position] != Constants.TABS.PLAYLIST
                                && savedTabSeqInt[position] != Constants.TABS.ALBUMS) {
                            if (viewPagerAdapter.getItem(position) instanceof FragmentLibrary){
                                ((FragmentLibrary) viewPagerAdapter.getItem(position))
                                        .filter(String.valueOf(searchQuery));
                            }
                        }
                        if (savedTabSeqInt[position] == Constants.TABS.ALBUMS) {
                                if(viewPagerAdapter.getItem(position) instanceof FragmentAlbumLibrary) {
                                    ((FragmentAlbumLibrary) viewPagerAdapter.getItem(position))
                                            .filter(String.valueOf(searchQuery));
                                }
                        }
                        if (savedTabSeqInt[position] == Constants.TABS.FOLDER){
                            if(viewPagerAdapter.getItem(position) instanceof FragmentFolderLibrary) {
                                ((FragmentFolderLibrary) viewPagerAdapter.getItem(position))
                                        .filter(String.valueOf(searchQuery));
                            }
                        }
                    }catch (Exception ignored){

                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        TabLayout tabLayout= findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Iterate over all tabs and set the custom view
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(viewPagerAdapter.getTabView(i));
            }
        }

        fab_right_side = findViewById(R.id.fab_right_side);
        fab_right_side.setBackgroundTintList(ColorStateList.valueOf(ColorHelper.getColor(R.color.fab_Colors_lyric_view)));
        fab_right_side.setOnClickListener(this);

        fab_lock = findViewById(R.id.fab_lock);
        fab_lock.setBackgroundTintList(ColorStateList.valueOf(ColorHelper.getColor(R.color.fab_Colors_lyric_view)));
        fab_lock.setOnClickListener(this);

        if(MyApp.getPref().getBoolean(getString(R.string.pref_hide_lock_button),false)){
            fab_lock.setVisibility(View.GONE);
        }
        if(MyApp.isLocked()){
            findViewById(R.id.border_view).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.border_view).setVisibility(View.GONE);
        }

        //ask for rating
        AppLaunchCountManager.app_launched(this);

        firstTimeInfoManage();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(MyApp.getContext())
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //throw new NullPointerException();
        Log.d("ActivityMain", "onCreate: reward point count is :" + RewardPoints.getRewardPointsCount());
    }

    private Bitmap getMainLibBackBitmap(){
        if(mainLibBackBitmap !=null){
            return mainLibBackBitmap;
        }

        String  picPath = MyApp.getContext().getFilesDir() + getString(R.string.main_lib_back_custom_image);
        Log.d(Constants.TAG, "UpdateUI: setBlurryBackgroundCustomImage: " + picPath);
        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        mainLibBackBitmap = BitmapFactory.decodeFile(picPath, options);*/

        try {
            mainLibBackBitmap = UtilityFun.decodeUri(this, Uri.fromFile(new File(picPath)), 500);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return mainLibBackBitmap;
    }

    private void setSystemDefaultBackground() {
        findViewById(R.id.image_view_view_pager).setBackgroundDrawable(ColorHelper.getBaseThemeDrawable());
    }

    public void setBlurryBackground(Bitmap b){

        Animation fadeIn = AnimationUtils.loadAnimation(ActivityMain.this, R.anim.fade_in);
        fadeIn.setDuration(2000);
        findViewById(R.id.image_view_view_pager).startAnimation(fadeIn);

        Blurry.with(this).radius(1)
                .color(Color.argb(120
                , 0, 0, 0)).from(b)
                .into(((ImageView) findViewById(R.id.image_view_view_pager)));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void firstTimeInfoManage(){
        if(!MyApp.getPref().getBoolean(getString(R.string.pref_lock_button_info_shown),false)){
            showInfo(Constants.FIRST_TIME_INFO.MINI_PLAYER);
            return;
        }

        newVersionInfo();

    }

    private void newVersionInfo() {
        int verCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            verCode = pInfo.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //if updating or first install
        if(verCode!=0 && MyApp.getPref().getInt(getString(R.string.pref_version_code),-1) < verCode ) {

            MyApp.getPref().edit().putString(getString(R.string.pref_card_image_links),"").apply();
            new MaterialDialog.Builder(this)
                    .typeface(TypeFaceHelper.getTypeFace(this),TypeFaceHelper.getTypeFace(this))
                    .title(getString(R.string.main_act_whats_new_title))
                    .content(getString(R.string.whats_new))
                    .positiveText(getString(R.string.okay))
                    .negativeText(getString(R.string.main_act_whats_new_neg))
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            shareApp();
                        }
                    })
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            Toast.makeText(getApplicationContext(), "Artist Information local sync started in background.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();

            int baseThemePref = MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT);
            if(baseThemePref == Constants.PRIMARY_COLOR.LIGHT){
                MyApp.getPref().edit().putInt(getString(R.string.pref_theme)
                        , Constants.PRIMARY_COLOR.GLOSSY).apply();
            }

        }

        /*
        if(MyApp.getPref().getInt(getString(R.string.pref_version_code),-1) < 88){
            new MaterialDialog.Builder(this)
                    .typeface(TypeFaceHelper.getTypeFace(this),TypeFaceHelper.getTypeFace(this))
                    .title(R.string.title_update_ab_music)
                    .content(R.string.content_ab_music)
                    .positiveText(R.string.pos_update_ab_music)
                    .negativeText(R.string.cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                        }
                    })
                    .show();
        }*/

        MyApp.getPref().edit().putInt(getString(R.string.pref_version_code), verCode).apply();
    }

    @SuppressLint("WrongViewCast")
    private void showInfo(int first_time_info){
        if(first_time_info!=-1){
            switch (first_time_info){
                case Constants.FIRST_TIME_INFO.MINI_PLAYER:
                    TapTargetView.showFor(this,
                            TapTarget.forView(findViewById(R.id.album_art_mini_player),
                                    getString(R.string.mini_player_primary)
                                    , getString(R.string.mini_player_secondary))
                                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                                    .outerCircleAlpha(0.9f)
                                    .transparentTarget(true)
                                    .titleTextColor(R.color.colorwhite)
                                    .descriptionTextColor(R.color.colorwhite)
                                    .drawShadow(true)
                                    .tintTarget(true)  ,
                            new TapTargetView.Listener() {
                                @Override
                                public void onTargetClick(TapTargetView view) {
                                    super.onTargetClick(view);
                                    view.dismiss(true);
                                }

                                @Override
                                public void onOuterCircleClick(TapTargetView view) {
                                    super.onOuterCircleClick(view);
                                    view.dismiss(true);
                                }

                                @Override
                                public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                                    super.onTargetDismissed(view, userInitiated);
                                    showNext();
                                }

                                private void showNext(){
                                    MyApp.getPref().edit().putBoolean(getString(R.string.pref_lock_button_info_shown),true).apply();
                                    showInfo(Constants.FIRST_TIME_INFO.SORTING);
                                }
                            });
                    break;

                case Constants.FIRST_TIME_INFO.SORTING:
                    View menuItemView = findViewById(R.id.action_sort); // SAME ID AS MENU ID
                    if(menuItemView==null){
                        showInfo(Constants.FIRST_TIME_INFO.MINI_PLAYER);
                    }else {
                        TapTargetView.showFor(this,
                                TapTarget.forView(findViewById(R.id.action_sort)
                                        , getString(R.string.sorting_primary)
                                        , getString(R.string.sorting_secondary))
                                        .outerCircleColorInt(ColorHelper.getPrimaryColor())
                                        .outerCircleAlpha(0.9f)
                                        .transparentTarget(true)
                                        .titleTextColor(R.color.colorwhite)
                                        .descriptionTextColor(R.color.colorwhite)
                                        .drawShadow(true)
                                        .tintTarget(true)  ,
                                new TapTargetView.Listener() {
                                    @Override
                                    public void onTargetClick(TapTargetView view) {
                                        super.onTargetClick(view);
                                        view.dismiss(true);
                                    }

                                    @Override
                                    public void onOuterCircleClick(TapTargetView view) {
                                        super.onOuterCircleClick(view);
                                        view.dismiss(true);
                                    }

                                    @Override
                                    public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                                        super.onTargetDismissed(view, userInitiated);
                                        showNext();
                                    }

                                    private void showNext(){
                                        showInfo(Constants.FIRST_TIME_INFO.MUSIC_LOCK);
                                    }
                                });
                    }

                    break;

                case Constants.FIRST_TIME_INFO.MUSIC_LOCK:
                    TapTargetView.showFor(this,
                            TapTarget.forView(findViewById(R.id.fab_lock)
                                    , getString(R.string.music_lock_primary)
                                    , getString(R.string.music_lock_secondary))
                                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                                    .outerCircleAlpha(0.9f)
                                    .transparentTarget(true)
                                    .titleTextColor(R.color.colorwhite)
                                    .descriptionTextColor(R.color.colorwhite)
                                    .drawShadow(true)
                                    .tintTarget(true)  ,
                            new TapTargetView.Listener() {
                                @Override
                                public void onTargetClick(TapTargetView view) {
                                    super.onTargetClick(view);
                                }

                                @Override
                                public void onOuterCircleClick(TapTargetView view) {
                                    super.onOuterCircleClick(view);
                                    view.dismiss(true);
                                }

                                @Override
                                public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                                    super.onTargetDismissed(view, userInitiated);
                                    newVersionInfo();
                                }
                            });
                    break;
            }
        }
    }

    private boolean ValidatePlaylistName(String playlist_name){

        String pattern= "^[a-zA-Z0-9 ]*$";
        if (playlist_name.matches(pattern)){
            if(playlist_name.length()>2) {
                //if playlist starts with digit, not allowed
                if(Character.isDigit(playlist_name.charAt(0))){
                    Snackbar.make(rootView, getString(R.string.playlist_error_1), Snackbar.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }else {
                //Toast.makeText(this,"Enter at least 3 characters",Toast.LENGTH_SHORT).show();
                Snackbar.make(rootView, getString(R.string.playlist_error_2), Snackbar.LENGTH_LONG).show();
                return false;
            }
        }else {
            //Toast.makeText(this,"Only alphanumeric characters allowed",Toast.LENGTH_SHORT).show();
            Snackbar.make(rootView, getString(R.string.playlist_error_3), Snackbar.LENGTH_LONG).show();
            return false;
        }
    }

    //boolean to to let function know if expand is needed for mini player or not
    //in case of resuming activty, no need to expand mini player
    //even when presed back from secondary activity, no need to expand
    private void updateUI(boolean expandNeeded){
        try {
            if (playerService != null) {
                if (playerService.getCurrentTrack() != null) {
                    Uri uri = MusicLibrary.getInstance().getAlbumArtUri(playerService.getCurrentTrack().getAlbumId());

                    //albumArt.setImageDrawable(getResources().getDrawable(R.drawable.ic_batman_1));
                    Glide.with(this)
                            .load(uri)
                            .listener(new RequestListener<Uri, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    Log.d("AlbumLibraryAdapter", "onException: ");
                                    if(UtilityFun.isConnectedToInternet() &&
                                            !MyApp.getPref().getBoolean(getString(R.string.pref_data_saver), false)) {
                                        final String url = MusicLibrary.getInstance().getArtistUrls().get(playerService.getCurrentTrack().getArtist());
                                        Glide
                                                .with(ActivityMain.this)
                                                .load(url)
                                                .centerCrop()
                                                .crossFade(500)
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .override(100, 100)
                                                .placeholder(R.drawable.ic_batman_1)
                                                .into(albumArt);
                                        return true;
                                    }
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .centerCrop()
                            .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                            .override(100,100)
                            .placeholder(R.drawable.ic_batman_1)
                            .crossFade()
                            .into(albumArt);


                    if (playerService.getStatus() == PlayerService.PLAYING) {
                        buttonPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_black_24dp));
                    } else {
                        buttonPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_black_24dp));
                    }

                    songNameMiniPlayer.setText(playerService.getCurrentTrack().getTitle());
                    artistNameMiniPlayer.setText(playerService.getCurrentTrack().getArtist());
                    if(expandNeeded) {
                        ((AppBarLayout) findViewById(R.id.app_bar_layout)).setExpanded(true);
                    }

                }

                if (playerService.getStatus() == PlayerService.PLAYING) {
                    startUpdateTask();
                } else {
                    stopUpdateTask();
                }

            } else {
                //this should not happen
                //restart app
                UtilityFun.restartApp();
            finish();
            }
        }catch (Exception ignored){

        }
    }

    //intitalize view pager with fragments and tab names
    private void setupViewPager(ViewPager viewPager) {

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());



        for(int tab:savedTabSeqInt){
            switch (tab){
                case Constants.TABS.ALBUMS:
                    Bundle bundle3=new Bundle();
                    bundle3.putInt("status",Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT);
                    if(!MyApp.getPref().getBoolean(getString(R.string.pref_album_lib_view),true)) {
                        FragmentLibrary musicByAlbumFrag=new FragmentLibrary();
                        musicByAlbumFrag.setArguments(bundle3);
                        viewPagerAdapter.addFragment(musicByAlbumFrag, getString(R.string.tab_album));
                    }else {
                        FragmentAlbumLibrary musicByAlbumFrag = new FragmentAlbumLibrary();
                        musicByAlbumFrag.setArguments(bundle3);
                        viewPagerAdapter.addFragment(musicByAlbumFrag,  getString(R.string.tab_album));
                    }
                    break;

                case Constants.TABS.ARTIST:
                    Bundle bundle2=new Bundle();
                    bundle2.putInt("status",Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT);
                    FragmentLibrary musicByArtistFrag=new FragmentLibrary();
                    musicByArtistFrag.setArguments(bundle2);
                    viewPagerAdapter.addFragment(musicByArtistFrag,  getString(R.string.tab_artist));
                    break;

                case Constants.TABS.FOLDER:
                    FragmentFolderLibrary folderFragment=new FragmentFolderLibrary();
                    viewPagerAdapter.addFragment(folderFragment, getString(R.string.tab_folder));
                    break;

                case Constants.TABS.GENRE:
                    Bundle bundle4=new Bundle();
                    bundle4.putInt("status",Constants.FRAGMENT_STATUS.GENRE_FRAGMENT);
                    FragmentLibrary musicByGenreFrag=new FragmentLibrary();
                    musicByGenreFrag.setArguments(bundle4);
                    viewPagerAdapter.addFragment(musicByGenreFrag,  getString(R.string.tab_genre));
                    break;

                case Constants.TABS.PLAYLIST:
                    FragmentPlaylistLibrary playlistFrag = new FragmentPlaylistLibrary();
                    viewPagerAdapter.addFragment(playlistFrag, getString(R.string.tab_playlist));
                    break;

                case Constants.TABS.TRACKS:
                    Bundle bundle1=new Bundle();
                    bundle1.putInt("status",Constants.FRAGMENT_STATUS.TITLE_FRAGMENT);
                    FragmentLibrary musicByTitleFrag=new FragmentLibrary();
                    musicByTitleFrag.setArguments(bundle1);
                    viewPagerAdapter.addFragment(musicByTitleFrag,  getString(R.string.tab_track));
                    break;
            }

        }

        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else if(count>0) {
            findViewById(R.id.mini_player).setVisibility(View.VISIBLE);
            getSupportFragmentManager().popBackStack();
        }//see if current fragment is folder fragmnet, if yes, override onBackPressed with fragments own action
        else if(savedTabSeqInt[viewPager.getCurrentItem()]==Constants.TABS.FOLDER) {
            if (viewPagerAdapter.getItem(viewPager.getCurrentItem()) instanceof FragmentFolderLibrary) {
                Intent intent = new Intent(NOTIFY_BACK_PRESSED);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }
        else {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        if(viewPager!=null) {
            if (savedTabSeqInt[viewPager.getCurrentItem()] == Constants.TABS.FOLDER
                    || savedTabSeqInt[viewPager.getCurrentItem()] == Constants.TABS.PLAYLIST) {
                for (int i = 0; i < menu.size(); i++) {
                    if (R.id.action_sort == menu.getItem(i).getItemId()
                            ) {
                        menu.getItem(i).setVisible(false);
                    }
                }
            }
        }


        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        if(isSearchOpened) {
            mSearchAction.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_close_white_24dp));
        }else {
            mSearchAction.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_search_white_48dp));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()){
            case R.id.action_settings:
                finish();
                startActivity(new Intent(this,ActivitySettings.class)
                        .putExtra("launchedFrom",Constants.PREF_LAUNCHED_FROM.MAIN)
                        .putExtra("ad",true));
                break;

            case R.id.action_sleep_timer:
                setSleepTimerDialog(this);
                break;

            case R.id.action_search:
                handleSearch();
                break;

            case R.id.action_equ:
                launchEqu();
                break;

            case R.id.action_sort:
                sortLibrary();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void launchEqu() {
        Intent intent = new Intent(AudioEffect
                .ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);

        if(MyApp.getPref().getBoolean(getString(R.string.pref_prefer_system_equ), true)
                && (intent.resolveActivity(getPackageManager()) != null)){
            try {
                //show system equalizer
                startActivityForResult(intent, 0);
            }catch (Exception ignored){}
        }else {
            //show app equalizer
            if(playerService.getEqualizerHelper().isEqualizerSupported()) {
                startActivity(new Intent(this, ActivityEqualizer.class));
            }else {
                Snackbar.make(rootView, R.string.error_equ_not_supported, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void sortLibrary() {
        PopupMenu popupMenu;
        View menuItemView = findViewById(R.id.action_sort); // SAME ID AS MENU ID
        if(menuItemView==null){
             popupMenu = new PopupMenu(this, findViewById(R.id.action_search));
        }else {
             popupMenu = new PopupMenu(this, menuItemView);
        }
        popupMenu.inflate(R.menu.sort_menu);

        if(savedTabSeqInt[viewPager.getCurrentItem()]!= Constants.TABS.TRACKS){
            popupMenu.getMenu().removeItem(R.id.action_sort_size);
            popupMenu.getMenu().removeItem(R.id.action_sort_by_duration);
            if(savedTabSeqInt[viewPager.getCurrentItem()]!=Constants.TABS.ALBUMS){
                popupMenu.getMenu().removeItem(R.id.action_sort_year);
            }
        }

        if(savedTabSeqInt[viewPager.getCurrentItem()]!=Constants.TABS.ARTIST){
            popupMenu.getMenu().removeItem(R.id.action_sort_no_of_album);
            popupMenu.getMenu().removeItem(R.id.action_sort_no_of_tracks);
        }


        if(MyApp.getPref().getInt(getString(R.string.pref_order_by),Constants.SORT_BY.ASC)==Constants.SORT_BY.ASC){
            popupMenu.getMenu().findItem(R.id.action_sort_asc).setChecked(true);
        }else {
            popupMenu.getMenu().findItem(R.id.action_sort_asc).setChecked(false);
        }

        switch (savedTabSeqInt[viewPager.getCurrentItem()]){
            case Constants.TABS.ALBUMS:
                currentPageSort = getString(R.string.pref_album_sort_by);
                break;

            case Constants.TABS.ARTIST:
                currentPageSort = getString(R.string.pref_artist_sort_by);
                break;

            case Constants.TABS.GENRE:
                currentPageSort = getString(R.string.pref_genre_sort_by);
                break;

            case Constants.TABS.FOLDER:
            case Constants.TABS.PLAYLIST:
                break;

            case Constants.TABS.TRACKS:
                currentPageSort = getString(R.string.pref_tracks_sort_by);
                break;
        }

        switch (MyApp.getPref().getInt(currentPageSort,Constants.SORT_BY.NAME)){
            case Constants.SORT_BY.NAME:
                popupMenu.getMenu().findItem(R.id.action_sort_name).setChecked(true);
                break;

            case Constants.SORT_BY.YEAR:
                popupMenu.getMenu().findItem(R.id.action_sort_year).setChecked(true);
                break;

            case Constants.SORT_BY.SIZE:
                popupMenu.getMenu().findItem(R.id.action_sort_size).setChecked(true);
                break;

            case Constants.SORT_BY.NO_OF_ALBUMS:
                popupMenu.getMenu().findItem(R.id.action_sort_no_of_album).setChecked(true);
                break;

            case Constants.SORT_BY.NO_OF_TRACKS:
                popupMenu.getMenu().findItem(R.id.action_sort_no_of_tracks).setChecked(true);
                break;

            case Constants.SORT_BY.DURATION:
                popupMenu.getMenu().findItem(R.id.action_sort_by_duration).setChecked(true);
                break;

        }
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    public void refreshLibrary(){
        MusicLibrary.getInstance().RefreshLibrary();
    }

    protected void handleSearch(){
        if(isSearchOpened){ //test if the search is open
            if (getSupportActionBar() != null){
                getSupportActionBar().setDisplayShowCustomEnabled(false);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
            }

            //hides the keyboard
            View view = getCurrentFocus();
            if (view == null) {
                view = new View(this);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            //add the search icon in the action bar
            mSearchAction.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_search_white_48dp));
            clearSearch();
            searchQuery="";
            findViewById(R.id.mini_player).setVisibility(View.VISIBLE);

            isSearchOpened = false;
        } else { //open the search entry
            findViewById(R.id.mini_player).setVisibility(View.GONE);

            if (getSupportActionBar() != null){
                getSupportActionBar().setDisplayShowCustomEnabled(true); //enable it to display a custom view
                getSupportActionBar().setCustomView(R.layout.search_bar_layout);//add the custom view
                getSupportActionBar().setDisplayShowTitleEnabled(false); //hide the title
            }
            editSearch = getSupportActionBar().getCustomView().findViewById(R.id.edtSearch); //the text editor
            editSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery=String.valueOf(s).toLowerCase();
                    searchAdapters(searchQuery);
                }
            });
            editSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imm.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT);
                }
            });

            editSearch.requestFocus();

            //open the keyboard focused in the edtSearch
            imm.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT);

            mSearchAction.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_close_white_24dp));
            //add the close icon
            //mSearchAction.setIcon(getResources().getDrawable(R.drawable.cancel));
            isSearchOpened = true;
        }
    }

    private void clearSearch(){

        String savedTabSeq = MyApp.getPref().getString(getString(R.string.pref_tab_seq), Constants.TABS.DEFAULT_SEQ);
        StringTokenizer st = new StringTokenizer(savedTabSeq, ",");
        int[] savedTabSeqInt = new int[Constants.TABS.NUMBER_OF_TABS];
        for (int i = 0; i < Constants.TABS.NUMBER_OF_TABS; i++) {
            savedTabSeqInt[i] = Integer.parseInt(st.nextToken());
        }

        for(int tab:savedTabSeqInt){
            if(viewPagerAdapter.getItem(tab) instanceof FragmentAlbumLibrary){
                ((FragmentAlbumLibrary)viewPagerAdapter.getItem(tab))
                        .filter("");
            } else if (viewPagerAdapter.getItem(tab) instanceof FragmentLibrary){
                ((FragmentLibrary)viewPagerAdapter.getItem(tab))
                        .filter("");
            } else if (viewPagerAdapter.getItem(tab) instanceof FragmentFolderLibrary){
                ((FragmentFolderLibrary)viewPagerAdapter.getItem(tab))
                        .filter("");
            }
        }
    }

    private void searchAdapters(String searchQuery){
        if(viewPagerAdapter.getItem(viewPager.getCurrentItem()) instanceof FragmentAlbumLibrary){
            ((FragmentAlbumLibrary) viewPagerAdapter.getItem(viewPager.getCurrentItem()))
                    .filter(String.valueOf(searchQuery));
        }else if(viewPagerAdapter.getItem(viewPager.getCurrentItem()) instanceof FragmentLibrary){
            ((FragmentLibrary) viewPagerAdapter.getItem(viewPager.getCurrentItem()))
                    .filter(String.valueOf(searchQuery));
        } else if(viewPagerAdapter.getItem(viewPager.getCurrentItem()) instanceof FragmentFolderLibrary){
            ((FragmentFolderLibrary) viewPagerAdapter.getItem(viewPager.getCurrentItem()))
                    .filter(String.valueOf(searchQuery));
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id==R.id.nav_settings){
            finish();
            startActivity(new Intent(this,ActivitySettings.class)
                    .putExtra("launchedFrom",Constants.PREF_LAUNCHED_FROM.DRAWER)
                    .putExtra("ad",true));
        } else  if (id == R.id.nav_remove_ads){
            startActivity(new Intent(this, ActivityRemoveAds.class));
            //finish();
        } else if(id==R.id.nav_share){
            shareApp();
        } else if(id==R.id.nav_rate){
            setRateDialog();
        } else if(id==R.id.nav_feedback){
            feedbackEmail();
        } else if(id==R.id.nav_website){
            openUrl(Uri.parse(WEBSITE));
        } else if(id==R.id.nav_signup){
            signIn();
        } else if(id==R.id.nav_logout){
            signOut();
        } else if(id==R.id.nav_rewards){
            rewardDialog();
        } else if(id==R.id.nav_explore_lyrics){
            startActivity(new Intent(this, ActivityExploreLyrics.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if(id==R.id.nav_dev_message){
            devMessageDialog();
        } else if(id==R.id.nav_instagram){
            openUrl(Uri.parse(INSTA_WEBSITE));
        } else if(id==R.id.nav_try_new_app){
            tryApp();
        } else if(id == R.id.nav_lyric_card){
            lyricCardDialog();
        } else if(id==192){
            uploadPhotos();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * upload lyric card photos
     */
    private void uploadPhotos(){
        Log.d("ActivityMain", "uploadPhotos: ");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("cardlinksNew");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final int numberOfLinks =((int) dataSnapshot.getChildrenCount());

                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {


                        File dir =new File(Environment.getExternalStorageDirectory().toString() + "/upload/compressjpeg");

                        File[] files = dir.listFiles();
                        for(int i=numberOfLinks; i<files.length+numberOfLinks; i++){

                            if(files[i-numberOfLinks].isDirectory()) continue;

                            File thumbFile = new File(dir + "/thumb/" + files[i-numberOfLinks].getName().replace(".jpg","") + "_tn.jpg" );
                            StorageReference uploadedFileThumb = FirebaseStorage.getInstance().getReference().child("cardimages").child(thumbFile.getName());
                            final UploadTask uploadTaskThumb = uploadedFileThumb.putFile(Uri.fromFile(thumbFile));
                            Log.d("ActivityMain", "run: Uploading " + thumbFile.getName());

                            final String[] thumbUrl = new String[1];
                            uploadTaskThumb.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("ActivityMain", "onFailure: " + e.getLocalizedMessage());
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.d("ActivityMain", "onSuccess: " + taskSnapshot.getDownloadUrl());
                                    thumbUrl[0] = taskSnapshot.getDownloadUrl().toString();
                                }
                            });

                            try {
                                com.google.android.gms.tasks.Tasks.await(uploadTaskThumb);
                            }catch (UnsupportedOperationException e){
                                e.printStackTrace();
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                            StorageReference uploadedFile = FirebaseStorage.getInstance().getReference().child("cardimages").child(files[i-numberOfLinks].getName());
                            final UploadTask uploadTask = uploadedFile.putFile(Uri.fromFile(files[i-numberOfLinks]));
                            Log.d("ActivityMain", "run: Uploading " + files[i-numberOfLinks].getName());

                            final int finalI = i;
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("ActivityMain", "onFailure: " + e.getLocalizedMessage());
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.d("ActivityMain", "onSuccess: " + taskSnapshot.getDownloadUrl());
                                    if(taskSnapshot.getDownloadUrl()==null)  return;

                                    Map<String, String> image = new HashMap<>();
                                    image.put("thumb", thumbUrl[0]);
                                    image.put("image", taskSnapshot.getDownloadUrl().toString());
                                    myRef.child(Integer.toString(finalI)).setValue(image);

                                    Toast.makeText(playerService, "Uploaded : " + taskSnapshot.getDownloadUrl().toString(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            try {
                                com.google.android.gms.tasks.Tasks.await(uploadTask);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void lyricCardDialog(){
        new MaterialDialog.Builder(this)
                .typeface(TypeFaceHelper.getTypeFace(this),TypeFaceHelper.getTypeFace(this))
                .title(getString(R.string.nav_lyric_cards))
                .content(R.string.dialog_lyric_card_content)
                .positiveText(R.string.dialog_lyric_card_pos)
                .neutralText(getString(R.string.cancel))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent searchLyricIntent = new Intent(MyApp.getContext(), ActivityExploreLyrics.class);
                        searchLyricIntent.setAction(Constants.ACTION.MAIN_ACTION);
                        searchLyricIntent.putExtra("search_on_launch", true);
                        searchLyricIntent.putExtra("from_notif", false);
                        startActivity(searchLyricIntent);
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void tryApp(){
        final String appPackageName = "in.thetechguru.walle.remote.abremotewallpaperchanger"; // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void openUrl(Uri parse) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, parse);
            startActivity(browserIntent);
        } catch (Exception e) {
            Snackbar.make(rootView, getString(R.string.error_opening_browser), Snackbar.LENGTH_LONG).show();
        }
    }

    private void rewardDialog(){
        startActivity(new Intent(this, ActivityRewardVideo.class));
    }

    private void feedbackEmail() {
        String myDeviceModel = Build.MODEL;
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto",getString(R.string.au_email_id), null));
        String[] address = new String[]{getString(R.string.au_email_id)};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, address);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "¯\\_(ツ)_/¯ AB Music ¯\\_(ツ)_/¯  : Device " + myDeviceModel);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello AB, \n\n You are coolest person I know on the planet and I just want to say that ");
        startActivity(Intent.createChooser(emailIntent, "Write me"));
    }

    private void shareApp() {
        try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                String sAux = getString(R.string.main_act_share_app_text);
                sAux = sAux + getString(R.string.share_app) + " \n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, getString(R.string.main_act_share_app_choose)));
        } catch(Exception e) {
            //e.toString();
        }
    }

    private void devMessageDialog(){
        new MaterialDialog.Builder(this)
                .typeface(TypeFaceHelper.getTypeFace(this),TypeFaceHelper.getTypeFace(this))
                .title(getString(R.string.nav_developers_message))
                .content(getString(R.string.developers_message))
                .neutralText(R.string.write_me)
                .positiveText(getString(R.string.main_act_rate_dialog_pos))
                .negativeText(getString(R.string.title_contribute))
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        feedbackEmail();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        openUrl(Uri.parse(GITHUB));
                    }
                })
                .show();
    }

    private void setRateDialog(){
        LinearLayout linear = new LinearLayout(this);
        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text = new TextView(this);
        text.setText(getString(R.string.main_act_rate_us));
        text.setTypeface(TypeFaceHelper.getTypeFace(this));
        text.setPadding(20, 10,20,10);
        text.setTextSize(16);
        //text.setGravity(Gravity.CENTER);

        LinearLayout ratingWrap = new LinearLayout(this);
        ratingWrap.setOrientation(LinearLayout.VERTICAL);
        ratingWrap.setGravity(Gravity.CENTER);

        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
       //ratingBar.setNumStars(5);
        ratingBar.setRating(5);
        ratingWrap.addView(ratingBar);

        linear.addView(text);
        linear.addView(ratingWrap);

        new MaterialDialog.Builder(this)
                .typeface(TypeFaceHelper.getTypeFace(this),TypeFaceHelper.getTypeFace(this))
                .title(getString(R.string.main_act_rate_dialog_title))
               // .content(getString(R.string.lyric_art_info_content))
                .positiveText(getString(R.string.main_act_rate_dialog_pos))
                .negativeText(getString(R.string.cancel))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }

                        /*
                        leftAppForRating = true;
                        timeOfLeavingForRating = System.currentTimeMillis();
                        Toast.makeText(ActivityMain.this, "Rate 5* to claim your reward!", Toast.LENGTH_SHORT).show();*/
                    }
                })
                .customView(linear,true)
                .show();

    }

    public void setSleepTimerDialog(final Context context){

        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);

        LinearLayout linear = new LinearLayout(context);
        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text = new TextView(context);

        int timer = MyApp.getPref().getInt(context.getString(R.string.pref_sleep_timer),0);
        if(timer==0) {
            String tempString = "0 "+context.getString(R.string.main_act_sleep_timer_status_minutes);
            text.setText(tempString);
        }else {
            String stringTemp = context.getString(R.string.main_act_sleep_timer_status_part1) +
                    timer +
                    context.getString(R.string.main_act_sleep_timer_status_part2);

            text.setText(stringTemp);

            builder.neutralText(context.getString(R.string.main_act_sleep_timer_neu))
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        MyApp.getPref().edit().putInt(context.getString(R.string.pref_sleep_timer),0).apply();
                        playerService.setSleepTimer(0, false);
                        //Toast.makeText(context, "Sleep timer discarded", Toast.LENGTH_LONG).show();
                        Snackbar.make(rootView,context.getString(R.string.sleep_timer_discarded) , Snackbar.LENGTH_LONG).show();
                    }
            });
        }
        text.setPadding(0, 10,0,0);
        text.setGravity(Gravity.CENTER);
        text.setTypeface(TypeFaceHelper.getTypeFace(this));

        final SeekBar seek = new SeekBar(context);
        seek.setPadding(40,10,40,10);
        seek.setMax(100);
        seek.setProgress(0);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String tempString = progress+context.getString(R.string.main_act_sleep_timer_status_minutes);
                text.setText(tempString);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        linear.addView(seek);
        linear.addView(text);

        builder
                .typeface(TypeFaceHelper.getTypeFace(this),TypeFaceHelper.getTypeFace(this))
                .title(context.getString(R.string.main_act_sleep_timer_title))
                .positiveText(context.getString(R.string.okay))
                .negativeText(context.getString(R.string.cancel))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if(seek.getProgress()!=0) {
                            MyApp.getPref().edit().putInt(context.getString(R.string.pref_sleep_timer),seek.getProgress()).apply();
                            playerService.setSleepTimer(seek.getProgress(), true);
                            String temp = context.getString(R.string.sleep_timer_successfully_set)
                                    + seek.getProgress()
                                    + context.getString(R.string.main_act_sleep_timer_status_minutes);
                            //Toast.makeText(context, temp, Toast.LENGTH_LONG).show();
                            Snackbar.make(rootView, temp, Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .customView(linear,true)
                .show();
    }
    @Override
    protected void onStart() {
        super.onStart();
        loginSilently();
    }

    private void loginSilently() {
        //login silently to google
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(Constants.TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result, false);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            //showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    //hideProgressDialog();
                    handleSignInResult(googleSignInResult, false);
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v("TAG","Main activity getting destroyed");
        mHandler.removeCallbacksAndMessages(null);
        viewPager.clearOnPageChangeListeners();
        viewPager = null;
        viewPagerAdapter = null;
        navigationView.setNavigationItemSelectedListener(null);
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if(MyApp.getService()==null){
            UtilityFun.restartApp();
            finish();
            return;
        }
        switch (view.getId()){
            case R.id.mini_player:
                Intent intent=new Intent(getApplicationContext(),ActivityNowPlaying.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                Log.v(Constants.TAG,"Launch now playing Jarvis");

                break;

            case R.id.play_pause_mini_player:
                ColorSwitchRunnableForImageView colorSwitchRunnablePlay = new ColorSwitchRunnableForImageView((ImageView) view);
                mHandler.post(colorSwitchRunnablePlay);
                if(playerService.getCurrentTrack()==null) {
                    //Toast.makeText(this,"Nothing to play!",Toast.LENGTH_LONG).show();
                    Snackbar.make(rootView, getString(R.string.nothing_to_play), Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (SystemClock.elapsedRealtime() - mLastClickTime < 300){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                playerService.play();


                if (playerService.getStatus() == PlayerService.PLAYING) {
                    buttonPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_black_24dp));
                    startUpdateTask();
                } else {
                    buttonPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_black_24dp));
                    stopUpdateTask();
                }

                actionMode = startSupportActionMode(this);
                break;

            case R.id.next_mini_plaayrer:
                ColorSwitchRunnableForImageView colorSwitchRunnableNext = new ColorSwitchRunnableForImageView((ImageView) view);
                mHandler.post(colorSwitchRunnableNext);
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                playerService.nextTrack();
                //no need to expand mini player
                updateUI(false);
                Log.v(Constants.TAG,"next track please Jarvis");
                break;

            case R.id.fab_lock:
                if(MyApp.isLocked()){
                    MyApp.setLocked(false);
                    fab_lock.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_lock_open_black_24dp));
                    findViewById(R.id.border_view).setVisibility(View.GONE);
                }else {
                    findViewById(R.id.border_view).setVisibility(View.VISIBLE);
                    fab_lock.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_lock_outline_black_24dp));
                    MyApp.setLocked(true);
                }
                Animation shake1 = AnimationUtils.loadAnimation(this, R.anim.shake_animation);
                fab_lock.startAnimation(shake1);
                lockInfoDialog();
                break;

            case R.id.fab_right_side:
                if(savedTabSeqInt[viewPager.getCurrentItem()]==Constants.TABS.PLAYLIST){
                    CreatePlaylistDialog();
                }else {
                    if(MyApp.isLocked()){
                        Snackbar.make(rootView, getString(R.string.music_is_locked), Snackbar.LENGTH_LONG).show();
                        return ;
                    }
                    if (playerService.getTrackList().size() > 0) {
                        playerService.shuffleAll();
                    } else {
                        Snackbar.make(rootView, getString(R.string.empty_track_list), Snackbar.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        MyApp.isAppVisible = false;
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiverForMiniPLayerUpdate);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiverForLibraryRefresh);
        super.onPause();
        stopUpdateTask();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(MyApp.getService()==null){
            UtilityFun.restartApp();
            finish();
            return;
        }else {
            playerService = MyApp.getService();
        }

        MyApp.isAppVisible = true;
        updateUI(false);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiverForMiniPLayerUpdate
                ,new IntentFilter(Constants.ACTION.COMPLETE_UI_UPDATE));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiverForLibraryRefresh
                ,new IntentFilter(Constants.ACTION.REFRESH_LIB));
        seekBar.setProgress(UtilityFun.getProgressPercentage(playerService.getCurrentTrackProgress(), playerService.getCurrentTrackDuration()));
        startUpdateTask();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateUI(false);
    }

    public void hideFab(boolean hide){
        if(hide && fab_right_side.isShown()) {
            fab_right_side.hide();
            if(!MyApp.getPref().getBoolean(getString(R.string.pref_hide_lock_button),false)) {
                fab_lock.hide();
            }
        }else {
            fab_right_side.show();
            if(!MyApp.getPref().getBoolean(getString(R.string.pref_hide_lock_button),false)) {
                fab_lock.show();
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(playerService==null) {
            UtilityFun.restartApp();
            finish();
            return false;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                playerService.play();
                updateUI(false);
                break;

            case KeyEvent.KEYCODE_MEDIA_NEXT:
                playerService.nextTrack();
                updateUI(false);
                break;

            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                playerService.prevTrack();
                updateUI(false);
                break;

            case KeyEvent.KEYCODE_MEDIA_STOP:
                playerService.stop();
                updateUI(false);
                break;

            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                break;
        }

        return false;
    }

    @Override
    public void onRefresh() {
        refreshLibrary();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        int sort_id = MyApp.getPref().getInt(currentPageSort,Constants.SORT_BY.NAME);
        switch (item.getItemId()){
            case  R.id.action_sort_name:
                MyApp.getPref().edit().putInt(currentPageSort,Constants.SORT_BY.NAME).apply();
                sort_id = Constants.SORT_BY.NAME;
                break;

            case R.id.action_sort_year:
                MyApp.getPref().edit().putInt(currentPageSort,Constants.SORT_BY.YEAR).apply();
                sort_id = Constants.SORT_BY.YEAR;
                break;

            case R.id.action_sort_size:
                MyApp.getPref().edit().putInt(currentPageSort,Constants.SORT_BY.SIZE).apply();
                sort_id = Constants.SORT_BY.SIZE;
                break;

            case R.id.action_sort_no_of_album:
                MyApp.getPref().edit().putInt(currentPageSort,Constants.SORT_BY.NO_OF_ALBUMS).apply();
                sort_id = Constants.SORT_BY.NO_OF_ALBUMS;
                break;

            case R.id.action_sort_no_of_tracks:
                MyApp.getPref().edit().putInt(currentPageSort,Constants.SORT_BY.NO_OF_TRACKS).apply();
                sort_id = Constants.SORT_BY.NO_OF_TRACKS;
                break;

            case R.id.action_sort_by_duration:
                MyApp.getPref().edit().putInt(currentPageSort,Constants.SORT_BY.DURATION).apply();
                sort_id = Constants.SORT_BY.DURATION;
                break;

            case R.id.action_sort_asc:
                if(!item.isChecked()) {
                    MyApp.getPref().edit().putInt(getString(R.string.pref_order_by), Constants.SORT_BY.ASC).apply();

                }else {
                    MyApp.getPref().edit().putInt(getString(R.string.pref_order_by), Constants.SORT_BY.DESC).apply();
                }
                break;
        }

        Log.v(Constants.TAG,"view pager item"+viewPager.getCurrentItem()+"");

        if(viewPagerAdapter.getItem(viewPager.getCurrentItem()) instanceof  FragmentAlbumLibrary){
            ((FragmentAlbumLibrary) viewPagerAdapter.getItem(viewPager.getCurrentItem())).sort(sort_id);
        }else {
            ((FragmentLibrary) viewPagerAdapter.getItem(viewPager.getCurrentItem())).sort(sort_id);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result, true);
        }
    }

    private void lockInfoDialog(){
        if(!MyApp.getPref().getBoolean(getString(R.string.pref_show_lock_info_dialog),true)){
            return;
        }

        new MaterialDialog.Builder(this)
                .typeface(TypeFaceHelper.getTypeFace(this),TypeFaceHelper.getTypeFace(this))
                .title(getString(R.string.main_act_lock_info_title))
                .content(getString(R.string.main_act_lock_info_content))
                .positiveText(getString(R.string.main_act_lock_info_pos))
                .negativeText(getString(R.string.main_act_lock_info_neg))
                .neutralText(getString(R.string.main_act_lock_info_neu))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_hide_lock_button),true).apply();
                        fab_lock.hide();
                        MyApp.setLocked(false);
                        findViewById(R.id.border_view).setVisibility(View.GONE);
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_show_lock_info_dialog),false).apply();
                    }
                })
                .show();

    }

    private void CreatePlaylistDialog(){
        final EditText input = new EditText(ActivityMain.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
                input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
            }
        }, 200);

        new MaterialDialog.Builder(this)
                .typeface(TypeFaceHelper.getTypeFace(this),TypeFaceHelper.getTypeFace(this))
                .title(getString(R.string.main_act_create_play_list_title))
                .positiveText(getString(R.string.okay))
                .negativeText(getString(R.string.cancel))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String playlist_name = input.getText().toString().trim();
                        if(ValidatePlaylistName(playlist_name)) {
                            if(PlaylistManager.getInstance(ActivityMain.this).CreatePlaylist(playlist_name)) {

                                int tabCount = 0;
                                for(int tab:savedTabSeqInt){
                                    if(tab==Constants.TABS.PLAYLIST){
                                        if((viewPagerAdapter
                                                .getItem(tabCount)) instanceof FragmentPlaylistLibrary) {
                                            ((FragmentPlaylistLibrary) viewPagerAdapter
                                                    .getItem(tabCount))
                                                    .refreshPlaylistList();
                                        }
                                        break;
                                    }
                                    tabCount++;
                                }

                                //Toast.makeText(ActivityMain.this, "Playlist created", Toast.LENGTH_SHORT).show();
                                Snackbar.make(rootView, getString(R.string.play_list_created), Snackbar.LENGTH_LONG).show();
                            }else {
                                //Toast.makeText(ActivityMain.this, "Playlist already exists", Toast.LENGTH_SHORT).show();
                                Snackbar.make(rootView, getString(R.string.play_list_already_exists), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                })
                .customView(input,true)
                .show();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        //updateUI(false);
                        MyApp.hasUserSignedIn=false;
                        updateDrawerUI(null, null, false);
                        Snackbar.make(rootView, getString(R.string.signed_out), Snackbar.LENGTH_LONG).show();

                    }
                });
    }

    private void handleSignInResult(GoogleSignInResult result, boolean manualSignIn) {
        Log.d(Constants.TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.

            if(manualSignIn){
                //permanently hide  sign in button on now playing activity
                MyApp.getPref().edit().putBoolean("never_show_button_again",true).apply();
            }

            MyApp.hasUserSignedIn = true;

            GoogleSignInAccount acct = result.getSignInAccount();
            if(acct==null){
                return;
            }

            //sign up user to tech guru newsletter
            String email = acct.getEmail();
            String name = acct.getGivenName();
            //store this email id and time of first sign in
            if(manualSignIn && email!=null) {
                new SignUp().execute(email,name);
            }

            String personPhotoUrl="";
            if(acct.getPhotoUrl()!=null) {
                personPhotoUrl = acct.getPhotoUrl().toString();
            }

            updateDrawerUI(acct.getDisplayName(), personPhotoUrl, true);
        } else {
            // some Error or user logged out, either case, update the drawer and give user appropriate info
            MyApp.hasUserSignedIn=false;

            updateDrawerUI(null,null, false);
            if(manualSignIn) {
                if (result.getStatus().getStatusCode() == CommonStatusCodes.NETWORK_ERROR) {
                    //Toast.makeText(this, "Network Error, try again later!", Toast.LENGTH_SHORT).show();
                    Snackbar.make(rootView, getString(R.string.network_error), Snackbar.LENGTH_LONG).show();
                } else {
                    //Toast.makeText(this, "Unknown Error, try again later!", Toast.LENGTH_SHORT).show();
                    Snackbar.make(rootView, getString(R.string.unknown_error), Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    private void updateDrawerUI(String displayName, String personPhotoUrl, boolean signedIn) {
        final TextView textView = navigationView.getHeaderView(0).findViewById(R.id.signed_up_user_name);
        if(displayName!=null) {
            textView.setText(displayName);
        }else {
            textView.setText("");
        }

        final RoundedImageView imageView = navigationView.getHeaderView(0).findViewById(R.id.navHeaderImageView);
        if(personPhotoUrl!=null) {
            Glide.with(getApplicationContext()).load(personPhotoUrl)
                    .asBitmap()
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new SimpleTarget<Bitmap>(300, 300) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            imageView.setImageBitmap(resource);
                        }
                    });
        }else {
            imageView.setImageResource(R.drawable.ic_batman_1);
        }

        navigationView.getMenu().clear(); //clear old inflated items.
        if(signedIn) {
            navigationView.inflateMenu(R.menu.drawer_menu_logged_in);
        }else {
            navigationView.inflateMenu(R.menu.drawer_menu_logged_out);
        }

        if(UtilityFun.isAdsRemoved()) {
            navigationView.getMenu().removeItem(R.id.nav_rewards);
            navigationView.getMenu().removeItem(R.id.nav_remove_ads);
            //navigationView.getMenu().removeItem(R.id.nav_remove_ads_free);
        }

        //add upload image button
        if(BuildConfig.DEBUG){
            navigationView.getMenu().add(R.id.grp2, 192, 10,"Upload");
        }

        //updateNavigationMenuItems();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(Constants.TAG, "onConnectionFailed:" + connectionResult);
    }

    ActionMode actionMode;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if(actionMode!=null) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.menu_cab_recyclerview_lyrics, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        View getTabView(int position) {
            // Given you have a custom layout in `res/layout/custom_tab.xml` with a TextView and ImageView
            View v = LayoutInflater.from(getBaseContext()).inflate(R.layout.custom_tab, null);
            TextView tv = v.findViewById(R.id.textview_custom_tab);
            tv.setText(mFragmentTitleList.get(position));
            return v;
        }
    }

    private class ColorSwitchRunnableForImageView implements Runnable {

        ImageView v;
        boolean colorChanged = false;

        public ColorSwitchRunnableForImageView(ImageView v){
            this.v = v;
        }
        @Override
        public void run() {
            if(!colorChanged) {
                v.setColorFilter(ColorHelper.getPrimaryColor());
                colorChanged=true;
                mHandler.postDelayed(this,200);
            }else {
                v.setColorFilter(ColorHelper.getColor(R.color.colorwhite));
                colorChanged = false;
            }
        }
    }

    private void startUpdateTask(){
        if(!updateTimeTaskRunning && playerService.getStatus()==playerService.PLAYING ){
            stopProgressRunnable=false;
            updateTimeTaskRunning=true;
            Executors.newSingleThreadExecutor().execute(mUpdateTimeTask);
        }
    }

    private void stopUpdateTask(){
        stopProgressRunnable=true;
        updateTimeTaskRunning=false;
    }

    //for seekbar on mini player top
    private final Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            while (true) {
                if (stopProgressRunnable) {
                    break;
                }
                updateTimeTaskRunning=true;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int totalDur = playerService.getCurrentTrackDuration();
                        int curDur = playerService.getCurrentTrackProgress();
                        seekBar.setProgress(UtilityFun.getProgressPercentage(curDur,totalDur));
                        Log.v("update task","update");
                    }
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            updateTimeTaskRunning = false;
        }
    };
}
