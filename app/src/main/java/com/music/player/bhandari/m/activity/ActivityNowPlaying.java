package com.music.player.bhandari.m.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.transition.ArcMotion;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.adapter.CurrentTracklistAdapter;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics;
import com.music.player.bhandari.m.transition.MorphMiniToNowPlaying;
import com.music.player.bhandari.m.transition.MorphNowPlayingToMini;
import com.music.player.bhandari.m.utils.AppLaunchCountManager;
import com.music.player.bhandari.m.customViews.CustomViewPager;
import com.music.player.bhandari.m.UIElementHelper.recyclerviewHelper.OnStartDragListener;
import com.music.player.bhandari.m.UIElementHelper.recyclerviewHelper.SimpleItemTouchHelperCallback;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.model.PlaylistManager;
import com.music.player.bhandari.m.utils.SignUp;
import com.music.player.bhandari.m.utils.UtilityFun;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
public class ActivityNowPlaying extends AppCompatActivity implements
        View.OnClickListener, OnStartDragListener {

    int screenWidth, screenHeight;
    private InterstitialAd mInterstitialAd;
    private static final int LAUNCH_COUNT_BEFORE_POPUP=15;
    private static final int RC_SIGN_IN = 7;
    private long mLastClickTime;
    private  boolean stopProgressRunnable = false;
    private boolean updateTimeTaskRunning = false;

    @BindView(R.id.root_view_now_playing) View rootView;
    @BindView(R.id.pw_ivShuffle)  ImageView shuffle;
    @BindView(R.id.pw_ivRepeat)  ImageView repeat;
    @BindView(R.id.text_in_repeat)  TextView textInsideRepeat;
    @BindView(R.id.seekbar_now_playing) SeekBar seekBar;
    @BindView(R.id.pw_playButton) ImageButton mPlayButton;
    @BindView(R.id.pw_runningTime) TextView runningTime;
    @BindView(R.id.pw_totalTime) TextView totalTime;
    @BindView(R.id.sliding_layout)   SlidingUpPanelLayout slidingUpPanelLayout;
    @BindView(R.id.view_pager_now_playing)  CustomViewPager viewPager;
    @BindView(R.id.shineButton)  ShineButton shineButton;
    @BindView(R.id.toolbar_)  Toolbar toolbar;
    @BindView(R.id.controls_wrapper) View controlsWrapper;
    //@BindView(R.id.nowPlayingBackgroundImageOverlay) View backgroundOverlay;

    private SharedPreferences pref;

    //is artist thumb loaded in blurry background
    private boolean isArtistLoadedInBackground = false;
    private ActivityNowPlaying.ViewPagerAdapter viewPagerAdapter;
    private AudioManager audioManager ;
    private boolean isInvokedFromFileExplorer=false;

    //bind player service
    private  PlayerService playerService;
    private BroadcastReceiver mUIUpdateReceiver;
    private RecyclerView mRecyclerView;
    private  CurrentTracklistAdapter mAdapter;
    private ActivityNowPlaying.WrapContentLinearLayoutManager mLayoutManager=
            new ActivityNowPlaying.WrapContentLinearLayoutManager(this);
    private ItemTouchHelper mItemTouchHelper;
    private  Handler mHandler = new Handler();
    private GoogleApiClient mGoogleApiClient;
    //now playing background bitmap
    Bitmap nowPlayingCustomBackBitmap;

    private int selectedPageIndex;

    //location of controls wrapper
    float yControl;
    float toolbarHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if player service not running, kill the app
        if(MyApp.getService()==null){
            UtilityFun.restartApp();
            finish();
            return;
        }

        playerService = MyApp.getService();

        ColorHelper.setStatusBarGradiant(ActivityNowPlaying.this);

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
        setContentView(R.layout.activity_now_playing);
        ButterKnife.bind(this);

        //backgroundOverlay.setBackgroundDrawable(ColorHelper.GetGradientDrawable());

        slidingUpPanelLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            slidingUpPanelLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            slidingUpPanelLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }

                        yControl = controlsWrapper.getY();
                        toolbarHeight = toolbar.getHeight();
                        Log.d("ActivityNowPlaying", "onGlobalLayout: yControl " + yControl);
                        Log.d("ActivityNowPlaying", "onGlobalLayout: toolbarHeight " + toolbarHeight);

                        Log.d("ActivityNowPlaying", controlsWrapper.getMeasuredHeight()+"");


                    }
        });

        /*toolbar.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            toolbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            toolbar.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }

                        toolbar.getLocationOnScreen(locationToolbar);
                        Log.d("ActivityNowPlaying", "onGlobalLayout: locationToolbar " + locationToolbar[0] + " : " + locationToolbar[1]);
                    }
                });*/

        if(!MyApp.getPref().getBoolean("never_show_button_again", false)){
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Log.d(Constants.TAG, "onConnectionFailed:" + connectionResult);
                        }
                    })
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }


        //noinspection PointlessBooleanExpression
        if( /*AppLaunchCountManager.isEligibleForInterstialAd() && */ !UtilityFun.isAdsRemoved()) {

            MobileAds.initialize(getApplicationContext(), getString(R.string.banner_play_queue));

            if((AppLaunchCountManager.isEligibleForInterstialAd() &&
                    AppLaunchCountManager.getNowPlayingLaunchCount()%LAUNCH_COUNT_BEFORE_POPUP==0)) {
                mInterstitialAd = new InterstitialAd(this);
                mInterstitialAd.setAdUnitId(getString(R.string.inter_settings_activity));

                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        mInterstitialAd.show();
                    }
                });

                requestNewInterstitial();
            }
        }

        if(getIntent().getAction()!=null) {
            if (getIntent().getAction().equals(Constants.ACTION.OPEN_FROM_FILE_EXPLORER)) {
                isInvokedFromFileExplorer = true;
            }
        }else {
            isInvokedFromFileExplorer = false;
        }

        pref = MyApp.getPref();
        if(playerService!=null && playerService.getCurrentTrack()!=null) {
            toolbar.setTitle(playerService.getCurrentTrack().getTitle());
            toolbar.setSubtitle(playerService.getCurrentTrack().getArtist());
        }
        setSupportActionBar(toolbar);

        InitializeCurrentTracklistAdapter();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        audioManager =
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        final View playQueueHandle = findViewById(R.id.handle_current_queue);
        playQueueHandle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    playQueueHandle.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    playQueueHandle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                ; //height is ready

                slidingUpPanelLayout.setPanelHeight(playQueueHandle.getHeight());
                slidingUpPanelLayout.setScrollableView(mRecyclerView);
            }
        });

        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if(slideOffset>0.99){
                    playQueueHandle.setVisibility(View.INVISIBLE);
                }else {
                    playQueueHandle.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

                if(previousState==SlidingUpPanelLayout.PanelState.COLLAPSED && newState==SlidingUpPanelLayout.PanelState.DRAGGING){
                    try {
                        int position = playerService.getCurrentTrackPosition();
                        mRecyclerView.scrollToPosition(position);
                    }catch (Exception ignored){}
                    //Log.v(Constants.TAG,"DRAGGING");
                }
            }

        });
        //set gradient as background

        /*GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.BR_TL,
                new int[] {ColorHelper.getColor(R.color.colorBlackThemeBack),0xFF131313});
        gd.setCornerRadius(0f);
        slidingUpPanelLayout.setBackgroundColor(ColorHelper.getColor(R.color.blackTransparent));*/
        slidingUpPanelLayout.setDragView(R.id.play_queue_title);

        shineButton.init(this);

        View saveQueueButton = findViewById(R.id.save_queue_button);
        saveQueueButton.setOnClickListener(this);

        Log.v(Constants.TAG,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)+"VOLUME");

        mUIUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(Constants.TAG, "update UI__ please Jarvis");
                UpdateUI();
            }
        };


        /*final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        }*/


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.v(Constants.L_TAG+"wow","selected "+position );

                selectedPageIndex = position;
                //display disclaimer if not accepted already
                if(position==2 && !MyApp.getPref().getBoolean(getString(R.string.pref_disclaimer_accepted),false)){
                    showDisclaimerDialog();
                }

                //2 lyrics fragment
                if(position==2 && playerService.getStatus() == PlayerService.PLAYING){
                    acquireWindowPowerLock(true);
                }else {
                    acquireWindowPowerLock(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setOffscreenPageLimit(2);
        setupViewPager(viewPager);
        //set current item to disc
        viewPager.setCurrentItem(Constants.EXIT_NOW_PLAYING_AT.DISC_FRAG, true);

        //display current play queue header
        if(playerService!=null && playerService.getTrackList()!=null) {
            if (!playerService.getTrackList().isEmpty()) {
                String title = "Save Playlist";
                ((TextView) findViewById(R.id.save_queue_button)).setText(title);
            }
        }

        if(!MyApp.getPref().getBoolean(getString(R.string.pref_swipe_right_shown),false)) {
            showInfoDialog();
        }

        InitializeControlsUI();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setupSharedElementTransitions();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupSharedElementTransitions() {
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumHorizontalAngle(50f);
        arcMotion.setMinimumVerticalAngle(50f);

        android.view.animation.Interpolator easeInOut = AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in);

        MorphMiniToNowPlaying sharedEnter = new MorphMiniToNowPlaying();
        sharedEnter.setPathMotion(arcMotion);
        sharedEnter.setInterpolator(easeInOut);

        MorphNowPlayingToMini sharedExit = new MorphNowPlayingToMini();
        sharedExit.setPathMotion(arcMotion);
        sharedExit.setInterpolator(easeInOut);

        /*if (second_card != null) {
            sharedEnter.addTarget(second_card)
            sharedReturn.addTarget(second_card)
        }*/

        getWindow().setSharedElementEnterTransition(sharedEnter);
        getWindow().setSharedElementExitTransition(sharedExit);
        postponeEnterTransition();
        //getWindow().sharedElementEnterTransition = sharedEnter
        //getWindow().sharedElementReturnTransition = sharedReturn

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void acquireWindowPowerLock(boolean acquire){
        /*if(acquire) {
            if (mWakeLock != null && !mWakeLock.isHeld()) {
                this.mWakeLock.acquire(10*60*1000L); //10 minutes
            }
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }else {
            if(mWakeLock!=null && mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }*/

        if (acquire) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    private void showDisclaimerDialog(){
        MaterialDialog dialog = new MyDialogBuilder(this)
                .title(getString(R.string.lyrics_disclaimer_title))
                .content(getString(R.string.lyrics_disclaimer_content))
                .positiveText(getString(R.string.lyrics_disclaimer_title_pos))
                .negativeText(getString(R.string.lyrics_disclaimer_title_neg))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_disclaimer_accepted),true).apply();
                        ((FragmentLyrics)viewPagerAdapter.getItem(2)).disclaimerAccepted();
                    }
                })
                .build();

        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

        dialog.show();
    }

    private void showInfoDialog(){
        MaterialDialog dialog = new  MyDialogBuilder(this)
                .title(getString(R.string.lyric_art_info_title))
                .content(getString(R.string.lyric_art_info_content))
                .positiveText(getString(R.string.lyric_art_info_title_button_neg))
                .negativeText(getString(R.string.lyric_art_info_button_p))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_swipe_right_shown),true).apply();
                    }
                })
                .build();

        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

        dialog.show();
    }

    private void setupViewPager(ViewPager viewPager) {

        viewPagerAdapter = new ActivityNowPlaying.ViewPagerAdapter(getSupportFragmentManager());

        FragmentArtistInfo artistInfo = new FragmentArtistInfo();
        viewPagerAdapter.addFragment(artistInfo,"Artist Bio");

        FragmentAlbumArt fragmentAlbumArt =new FragmentAlbumArt();
        viewPagerAdapter.addFragment(fragmentAlbumArt, "Disc");

        FragmentLyrics fragmentLyric=new FragmentLyrics();
        viewPagerAdapter.addFragment(fragmentLyric, "Lyrics");

        viewPager.setAdapter(viewPagerAdapter);
    }

    public void InitializeCurrentTracklistAdapter(){
        mRecyclerView= findViewById(R.id.recyclerViewForCurrentTracklist);
        mAdapter= new CurrentTracklistAdapter(this,this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    public void UpdateCurrentTracklistAdapter(){
        if(mRecyclerView==null || mAdapter == null){
            return;
        }
        mAdapter.fillData();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.v(Constants.TAG, "DESTORY NOW PLAYING");
        //this removes any memory leak caused by handler
        mHandler.removeCallbacksAndMessages(null);

        //save exit status so than we can open corresponding frag next time
        /*switch (viewPager.getCurrentItem()){
            case 2:
                MyApp.getPref().edit()
                        .putInt(getString(R.string.pref_exit_now_playing_at),Constants.EXIT_NOW_PLAYING_AT.LYRICS_FRAG).apply();
                break;

            case 0:
                MyApp.getPref().edit()
                        .putInt(getString(R.string.pref_exit_now_playing_at),Constants.EXIT_NOW_PLAYING_AT.ARTIST_FRAG).apply();
                break;

            case 1:
            default:
                MyApp.getPref().edit()
                        .putInt(getString(R.string.pref_exit_now_playing_at),Constants.EXIT_NOW_PLAYING_AT.DISC_FRAG).apply();
                break;
        }*/

        /*if(mWakeLock!=null && mWakeLock.isHeld()){
            mWakeLock.release();
        }*/

        super.onDestroy();
    }

    private void UpdateUI() {
        if(playerService!=null) {
            TrackItem item = playerService.getCurrentTrack();
            if(mAdapter!=null) mAdapter.notifyDataSetChanged();
            invalidateOptionsMenu();

            if (item != null) {

                Intent intent = new Intent().setAction(Constants.ACTION.UPDATE_LYRIC_AND_INFO);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                Log.v(Constants.TAG,"Intent sent! "+intent.getAction());

                if(playerService.getStatus()==PlayerService.PLAYING) {
                    mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.pw_pause));
                }else {
                    mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.pw_play));
                }

                totalTime.setText(UtilityFun.msToString(playerService.getCurrentTrackDuration()));

                //update disc
                updateDisc();

                //check current now playing background setting
                ///get current setting
                // 0 - System default   1 - artist image 2 - album art 3 - custom
                int currentNowPlayingBackPref = MyApp.getPref().getInt(getString(R.string.pref_now_playing_back),1);

                Bitmap b=null ;// = playerService.getAlbumArt();
                try {
                    switch (currentNowPlayingBackPref){
                        case 0:
                            //by default, default image will be used
                            break;

                        case 1:
                            //look in cache for artist image
                            String CACHE_ART_THUMBS = this.getCacheDir()+"/art_thumbs/";
                            String actual_file_path = CACHE_ART_THUMBS+playerService.getCurrentTrack().getArtist();
                            b= BitmapFactory.decodeFile(actual_file_path);
                            isArtistLoadedInBackground = b != null;
                            Log.d(Constants.TAG, "UpdateUI: settingArtistImageBackground");
                            break;

                        case 2:
                            b = MusicLibrary.getInstance().getAlbumArtFromId(item.getId());
                            break;

                        case 3:
                            b = getNowPlayingBackBitmap();
                            break;
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(b!=null) {
                    int width = b.getWidth();
                    int height = b.getHeight();
                    int maxWidth = screenWidth;
                    int maxHeight = screenHeight;
                    if (width > height) {
                        // landscape
                        float ratio = (float) width / maxWidth;
                        width = maxWidth;
                        height = (int)(height / ratio);
                    } else if (height > width) {
                        // portrait
                        float ratio = (float) height / maxHeight;
                        height = maxHeight;
                        width = (int)(width / ratio);
                    } else {
                        // square
                        if(maxHeight<height) {
                            height = maxHeight;
                            width = maxWidth;
                        }
                    }

                    b = Bitmap.createScaledBitmap(b, width, height, false);
                    setBlurryBackground(b);
                }/*else {
                    b = BitmapFactory.decodeResource(getResources(),R.drawable.now_playing_back);
                    setBlurryBackground(b);
                }*/

                toolbar.setTitle(playerService.getCurrentTrack().getTitle());
                toolbar.setSubtitle(playerService.getCurrentTrack().getArtist());
            }
        }
        else {
            UtilityFun.restartApp();
            finish();
        }
    }

    private Bitmap getNowPlayingBackBitmap(){
        if(nowPlayingCustomBackBitmap!=null){
            return nowPlayingCustomBackBitmap;
        }

        String  picPath = MyApp.getContext().getFilesDir() + getString(R.string.now_playing_back_custom_image);
        Log.d(Constants.TAG, "UpdateUI: setBlurryBackgroundCustomImage: " + picPath);
        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        nowPlayingCustomBackBitmap = BitmapFactory.decodeFile(picPath, options);
*/
        try {
            nowPlayingCustomBackBitmap = UtilityFun.decodeUri(this, Uri.fromFile(new File(picPath)), 500);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return nowPlayingCustomBackBitmap;
    }

    public void setBlurryBackground(Bitmap b){
        Animation fadeIn = AnimationUtils.loadAnimation(ActivityNowPlaying.this, R.anim.fade_in);
        fadeIn.setDuration(2000);
        findViewById(R.id.full_screen_iv).startAnimation(fadeIn);

        try {
            Blurry.with(this).radius(1).color(Color.argb(100
                    , 50, 0, 0)).from(b)
                    .into(((ImageView) findViewById(R.id.full_screen_iv)));
        }catch (OutOfMemoryError e){
            Toast.makeText(playerService, "Error setting blurry background due to insufficient memory", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        MyApp.isAppVisible = false;
        Log.v(Constants.TAG,"PAUSE NOW PLAYING");
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mUIUpdateReceiver);
        stopUpdateTask();
        stopProgressRunnable=true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.isAppVisible = true;

        if(MyApp.getService()==null){
            UtilityFun.restartApp();
            return;
        }else {
            playerService = MyApp.getService();
        }

        UpdateUI();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mUIUpdateReceiver
                ,new IntentFilter(Constants.ACTION.COMPLETE_UI_UPDATE));
        AppLaunchCountManager.nowPlayingLaunched();
        //UpdateCurrentTracklistAdapter();

        setSeekbarAndTime();
        startUpdateTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_now_plying, menu);
        for(int i = 0; i < menu.size(); i++){
            if(menu.getItem(i).getItemId()==R.id.action_fav) {
                //Drawable drawable = menu.getItem(i).getIcon();
                //if (drawable != null) {
                    TrackItem item=playerService.getCurrentTrack();

                    if(item!=null && PlaylistManager.getInstance(getApplicationContext()).isFavNew(item.getId())) {
                        //rawable.mutate();
                        //drawable.setColorFilter(ColorHelper.GetWidgetColor(), PorterDuff.Mode.SRC_ATOP);
                        menu.getItem(i).setIcon(getResources().getDrawable(R.drawable.ic_favorite_black_24dp));
                    }else {
                        //drawable.mutate();
                        //drawable.setColorFilter(ColorHelper.getColor(R.color.colorwhite), PorterDuff.Mode.SRC_ATOP);
                        menu.getItem(i).setIcon(getResources().getDrawable(R.drawable.ic_favorite_border_black_24dp));
                    }
                //}
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //
        if(slidingUpPanelLayout.getPanelState()== SlidingUpPanelLayout.PanelState.EXPANDED){
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        if(isInvokedFromFileExplorer){
            finish();
            return;
        }

        if(isTaskRoot()){
            startActivity(new Intent(this,ActivityMain.class));
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            //finish();
            //return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean b = intent.getBooleanExtra("refresh",false);
        if(b){
            int position = intent.getIntExtra("position", -1);
            String title = intent.getStringExtra("title");
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");

            if(playerService!=null) {
                playerService.updateTrackItem(position, playerService.getCurrentTrack().getId(), title, artist, album);
                playerService.PostNotification();

                //update currenttracklistadapteritem
                mAdapter.updateItem(position, title, artist, album);
            }
        }



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        TrackItem trackItem = playerService.getCurrentTrack();
        switch (item.getItemId()){
            case R.id.action_fav:
                if(playerService.getCurrentTrack()==null) {
                    Snackbar.make(rootView, getString(R.string.error_nothing_to_fav), Snackbar.LENGTH_LONG).show();
                    return true;
                }
                if(PlaylistManager.getInstance(getApplicationContext()).isFavNew(playerService.getCurrentTrack().getId())){
                    PlaylistManager.getInstance(getApplicationContext()).RemoveFromFavNew(playerService.getCurrentTrack().getId());
                }else {
                    PlaylistManager.getInstance(getApplicationContext())
                            .addSongToFav(playerService.getCurrentTrack().getId());
                    shineButton.setVisibility(View.VISIBLE);
                    shineButton.showAnim();
                    shineButton.clearAnimation();
                }
                invalidateOptionsMenu();
                break;

            case R.id.action_equ:
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
                break;

            case android.R.id.home:
                if(isTaskRoot()){
                    startActivity(new Intent(this,ActivityMain.class));
                    overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                    //finish();
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                }else {
                    finish();
                }
                break;

            case R.id.action_settings:
                //finish();
                startActivity(new Intent(this,ActivitySettings.class)
                        .putExtra("launchedFrom",Constants.PREF_LAUNCHED_FROM.NOW_PLAYING)
                        .putExtra("ad",true));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

            case R.id.action_go_to_artist:
                if(trackItem!=null) {
                    Intent art_intent = new Intent(this, ActivitySecondaryLibrary.class);
                    art_intent.putExtra("status", Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT);
                    art_intent.putExtra("key",trackItem.getArtist_id());
                    art_intent.putExtra("title", trackItem.getArtist().trim());
                    startActivity(art_intent);
                } else {
                    Snackbar.make(rootView, getString(R.string.no_music_found), Snackbar.LENGTH_LONG).show();
                }
                break;

            case R.id.action_go_to_album:
                if(trackItem!=null) {
                    Intent alb_intent = new Intent(this, ActivitySecondaryLibrary.class);
                    alb_intent.putExtra("status", Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT);
                    alb_intent.putExtra("key", trackItem.getAlbumId());
                    alb_intent.putExtra("title", trackItem.getAlbum().trim());
                    startActivity(alb_intent);
                }else {
                    Snackbar.make(rootView, getString(R.string.no_music_found), Snackbar.LENGTH_LONG).show();
                }
                break;

            case R.id.action_share:
                try {
                    if (trackItem != null) {
                        File fileToBeShared = new File(trackItem.getFilePath());
                        ArrayList<Uri> fileUris = new ArrayList<>();
                        fileUris.add(FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + "com.bhandari.music.provider", fileToBeShared));
                        UtilityFun.Share(this, fileUris, trackItem.getTitle());
                    } else {
                        Snackbar.make(rootView, R.string.error_nothing_to_share, Snackbar.LENGTH_LONG).show();
                    }
                }catch (IllegalArgumentException e){
                    try{
                        UtilityFun.ShareFromPath(this, trackItem.getFilePath());
                    }catch (Exception ex) {
                        Snackbar.make(rootView, R.string.error_unable_to_share, Snackbar.LENGTH_LONG).show();
                    }
                }
                break;

            case R.id.action_add_to_playlist:
                //Toast.makeText(context,"Playlists coming soon" ,Toast.LENGTH_SHORT).show();
                if(trackItem!=null) {
                    AddToPlaylist();
                }else {
                    Snackbar.make(rootView, getString(R.string.no_music_found), Snackbar.LENGTH_LONG).show();
                }
                break;

            case R.id.action_sleep_timer:
                    setSleepTimerDialog(this);
                break;

            case R.id.action_edit_track_info:
                if(trackItem!=null) {
                    startActivity(new Intent(this, ActivityTagEditor.class)
                            .putExtra("from", Constants.TAG_EDITOR_LAUNCHED_FROM.NOW_PLAYING)
                            .putExtra("file_path", trackItem.getFilePath())
                            .putExtra("track_title", trackItem.getTitle())
                            .putExtra("position", playerService.getCurrentTrackPosition())
                            .putExtra("id",trackItem.getId()));
                }else {
                    Snackbar.make(rootView, getString(R.string.no_music_found), Snackbar.LENGTH_LONG).show();
                }
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;


            case R.id.action_clear_lyrics_offline:
                if(trackItem!=null){
                    if(OfflineStorageLyrics.clearLyricsFromDB(trackItem)){
                        ((FragmentLyrics)viewPagerAdapter.getItem(2)).clearLyrics();
                    }else {
                        //Toast.makeText(this, "Unable to delete lyrics!", Toast.LENGTH_SHORT).show();
                        Snackbar.make(rootView, getString(R.string.error_no_lyrics), Snackbar.LENGTH_LONG).show();
                    }
                }else {
                    Snackbar.make(rootView, getString(R.string.error_no_lyrics), Snackbar.LENGTH_LONG).show();
                }
                break;

            case R.id.action_share_lyrics_offline:
                if(trackItem!=null){
                   ((FragmentLyrics)viewPagerAdapter.getItem(2)).shareLyrics();
                }else {
                    Snackbar.make(rootView, getString(R.string.error_no_lyrics), Snackbar.LENGTH_LONG).show();
                }
                break;

                //when clicked on this, lyrics are searched again from viewlyrics
                //but this time option is given to select lyrics
            case R.id.action_wrong_lyrics:
                if(trackItem!=null){
                    ((FragmentLyrics)viewPagerAdapter.getItem(2)).wrongLyrics();
                }else {
                    Snackbar.make(rootView, getString(R.string.error_no_lyrics), Snackbar.LENGTH_LONG).show();
                }
                break;

            case R.id.action_search_youtube:
                if(playerService.getCurrentTrack()!=null) {
                    UtilityFun.LaunchYoutube(this, trackItem.getArtist() + " - " + trackItem.getTitle());
                }
                break;


        }

        return super.onOptionsItemSelected(item);
    }

    private void AddToPlaylist(){
        int[] ids;
        TrackItem trackItem = playerService.getCurrentTrack();
        ids=new int[]{trackItem.getId()};
        UtilityFun.AddToPlaylist(this, ids);
        invalidateOptionsMenu();
    }

    @Override
    public void onClick(View view) {

        if(MyApp.getService()==null){
            UtilityFun.restartApp();
            return;
        }
        switch (view.getId()){
            case R.id.save_queue_button:

                if(mAdapter.getItemCount()==0){
                    return;
                }

                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);

                MaterialDialog dialog = new MyDialogBuilder(this)
                        .title(getString(R.string.main_act_create_play_list_title))
                        .positiveText(getString(R.string.okay))
                        .negativeText(getString(R.string.cancel))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                String playlist_name = input.getText().toString().trim();
                                if(ValidatePlaylistName(playlist_name)) {
                                    if(PlaylistManager.getInstance(ActivityNowPlaying.this).CreatePlaylist(playlist_name)) {
                                        int[] ids = new int[mAdapter.getSongList().size()];
                                        for (int i=0; i < ids.length; i++)
                                        {
                                            ids[i] = mAdapter.getSongList().get(i);
                                        }

                                        PlaylistManager.getInstance(ActivityNowPlaying.this)
                                                .AddSongToPlaylist(playlist_name,ids);
                                       // Toast.makeText(ActivityNowPlaying.this, "Playlist saved!", Toast.LENGTH_SHORT).show();
                                        Snackbar.make(rootView, getString(R.string.playlist_saved), Snackbar.LENGTH_LONG).show();
                                    }else {
                                        //Toast.makeText(ActivityNowPlaying.this, "Playlist already exists", Toast.LENGTH_SHORT).show();
                                        Snackbar.make(rootView, getString(R.string.play_list_already_exists), Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            }
                        })
                        .customView(input,true)
                        .build();

                //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

                dialog.show();
                break;

            /*case R.id.login_to_remove_ads:
                //signInDialog();
                signIn();
                break;*/
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        Log.d("ActivityNowPlaying", "onStartDrag: ");
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //super.onKeyDown(keyCode,event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                playerService.play();
                updateDisc();
                //togglePlayPauseButton();
                break;

            case KeyEvent.KEYCODE_MEDIA_NEXT:
                playerService.nextTrack();
                UpdateUI();
                break;

            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                playerService.prevTrack();
                UpdateUI();
                break;

            case KeyEvent.KEYCODE_MEDIA_STOP:
                playerService.stop();
                UpdateUI();
                break;

            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                break;

            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                super.onKeyDown(keyCode,event);
                Log.v(Constants.TAG,keyCode + " v " + audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)  );
                break;
        }

        return false;
    }

    private void updateDisc(){
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(new Intent(Constants.ACTION.DISC_UPDATE));
        ((FragmentLyrics) viewPagerAdapter.getItem(2)).runLyricThread();
        if(viewPager.getCurrentItem()==2 &&  playerService.getStatus()==PlayerService.PLAYING) {
            acquireWindowPowerLock(true);
        }else {
            acquireWindowPowerLock(false);
        }
    }

    public boolean isArtistLoadedInBack(){
        return isArtistLoadedInBackground;
    }

    public void setSleepTimerDialog(final Context context){

        MyDialogBuilder builder = new MyDialogBuilder(context);

        LinearLayout linear = new LinearLayout(context);
        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text = new TextView(context);
        int timer = MyApp.getPref().getInt(context.getString(R.string.pref_sleep_timer),0);
        if(timer==0) {
            text.setText("0"+ getString(R.string.main_act_sleep_timer_status_minutes));
        }else {
            String stringTemp = context.getString(R.string.main_act_sleep_timer_status_part1) +
                    timer +
                    context.getString(R.string.main_act_sleep_timer_status_part2);

            text.setText(stringTemp);
            builder.neutralText(getString(R.string.main_act_sleep_timer_neu))
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            MyApp.getPref().edit().putInt(context.getString(R.string.pref_sleep_timer),0).apply();
                            playerService.setSleepTimer(0, false);
                           // Toast.makeText(context, "Sleep timer discarded", Toast.LENGTH_LONG).show();
                            Snackbar.make(rootView, getString(R.string.sleep_timer_discarded), Snackbar.LENGTH_LONG).show();
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
                int progress = seekBar.getProgress();
            }
        });

        linear.addView(seek);
        linear.addView(text);

        MaterialDialog dialog = builder
                .title(context.getString(R.string.main_act_sleep_timer_title))
                .positiveText(getString(R.string.okay))
                .negativeText(getString(R.string.cancel))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if(seek.getProgress()!=0) {
                            MyApp.getPref().edit().putInt(context.getString(R.string.pref_sleep_timer),seek.getProgress()).apply();
                            playerService.setSleepTimer(seek.getProgress(), true);
                            playerService.setSleepTimer(seek.getProgress(), true);
                            String temp = getString(R.string.sleep_timer_successfully_set)
                                    + seek.getProgress()
                                    + getString(R.string.main_act_sleep_timer_status_minutes);
                            Snackbar.make(rootView, temp, Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .customView(linear,true)
                .build();

        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

        dialog.show();
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

    public void signIn() {
        if(mGoogleApiClient==null){
            return;
        }
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //for catching exception generated by recycler view which was causing abend, no other way to handle this
    private class WrapContentLinearLayoutManager extends LinearLayoutManager {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(Constants.TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.

                //permanently hide  sign in button on now playing activity
            MyApp.getPref().edit().putBoolean("never_show_button_again",true).apply();


            MyApp.hasUserSignedIn = true;

            GoogleSignInAccount acct = result.getSignInAccount();
            if(acct==null){
                return;
            }

            //sign up user to tech guru newsletter
            String email = acct.getEmail();
            String name = acct.getGivenName();

            //store this email id and time of first sign in
            if(email!=null) {
                new SignUp().execute(email,name);
            }

        } else {
            // some Error or user logged out, either case, update the drawer and give user appropriate info
            MyApp.hasUserSignedIn=false;

            if (result.getStatus().getStatusCode() == CommonStatusCodes.NETWORK_ERROR) {
                Snackbar.make(rootView, getString(R.string.network_error), Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(rootView, getString(R.string.unknown_error), Snackbar.LENGTH_LONG).show();
            }

        }
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

    private void feedbackEmail() {
        String myDeviceModel = Build.MODEL;
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto",getString(R.string.au_email_id), null));
        String[] address = new String[]{getString(R.string.au_email_id)};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, address);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for "+myDeviceModel);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello AndroidDevs, \n\n");
        startActivity(Intent.createChooser(emailIntent, "Send Feedback"));
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("F40E78AED9B7FE233362079AC4C05B61")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    private void InitializeControlsUI(){

        if(!pref.getBoolean(Constants.PREFERENCES.SHUFFLE,false)){
            shuffle.setColorFilter(ColorHelper.getColor(R.color.dark_gray3));
        }else {
            shuffle.setColorFilter(ColorHelper.getColor(R.color.colorwhite));
        }

        if(pref.getInt(Constants.PREFERENCES.REPEAT,0)==Constants.PREFERENCE_VALUES.REPEAT_ALL){
            textInsideRepeat.setTextColor(ColorHelper.getColor(R.color.colorwhite));
            repeat.setColorFilter(ColorHelper.getColor(R.color.colorwhite));
            textInsideRepeat.setText("A");
        }else if(pref.getInt(Constants.PREFERENCES.REPEAT,0)==Constants.PREFERENCE_VALUES.REPEAT_ONE){
            textInsideRepeat.setTextColor(ColorHelper.getColor(R.color.colorwhite));
            repeat.setColorFilter(ColorHelper.getColor(R.color.colorwhite));
            textInsideRepeat.setText("1");
        }else if(pref.getInt(Constants.PREFERENCES.REPEAT,0)==Constants.PREFERENCE_VALUES.NO_REPEAT){
            textInsideRepeat.setTextColor(ColorHelper.getColor(R.color.dark_gray3));
            repeat.setColorFilter(ColorHelper.getColor(R.color.dark_gray3));
            textInsideRepeat.setText("");
        }
        if(playerService.getStatus()==PlayerService.PLAYING){
            mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.pw_pause));
        }else {
            mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.pw_play));
        }

        //mPlayButton.setBackgroundTintList(ColorStateList.valueOf(ColorHelper.GetWidgetColor()));

        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if(b) {
                    runningTime.setText(UtilityFun.msToString(
                            UtilityFun.progressToTimer(seekBar.getProgress(), playerService.getCurrentTrackDuration())));
                    if(selectedPageIndex==2) {
                        ((FragmentLyrics) viewPagerAdapter.getItem(2)).smoothScrollAfterSeekbarTouched(seekBar.getProgress());
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopUpdateTask();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                playerService.seekTrack(UtilityFun.progressToTimer(seekBar.getProgress(), playerService.getCurrentTrackDuration()));
                startUpdateTask();
            }
        });

    }

    @OnClick(R.id.pw_ivShuffle)
    void shuffle(){
        if(playerService.getCurrentTrack()==null) {
            Toast.makeText(this,getString(R.string.nothing_to_play),Toast.LENGTH_LONG).show();
            return;
        }
        // mLastClickTime = SystemClock.elapsedRealtime();
        if(pref.getBoolean(Constants.PREFERENCES.SHUFFLE,false)){
            //shuffle is on, turn it off
            pref.edit().putBoolean(Constants.PREFERENCES.SHUFFLE,false).apply();
            playerService.shuffle(false);
            shuffle.setColorFilter(ColorHelper.getColor(R.color.dark_gray3));
        }else {
            //shuffle is off, turn it on
            pref.edit().putBoolean(Constants.PREFERENCES.SHUFFLE,true).apply();
            playerService.shuffle(true);
            shuffle.setColorFilter(ColorHelper.getColor(R.color.colorwhite));
        }
        UpdateCurrentTracklistAdapter();
    }

    @OnClick(R.id.pw_ivRepeat)
    void repeat(){
        if(pref.getInt(Constants.PREFERENCES.REPEAT,0)==Constants.PREFERENCE_VALUES.NO_REPEAT){
            pref.edit().putInt(Constants.PREFERENCES.REPEAT,Constants.PREFERENCE_VALUES.REPEAT_ALL).apply();
            //repeat.setColorFilter(UtilityFun.GetDominatColor(playerService.getAlbumArt()));
            textInsideRepeat.setTextColor(ColorHelper.getColor(R.color.colorwhite));
            repeat.setColorFilter(ColorHelper.getColor(R.color.colorwhite));
            textInsideRepeat.setText("A");
        }else if(pref.getInt(Constants.PREFERENCES.REPEAT,0)==Constants.PREFERENCE_VALUES.REPEAT_ALL){
            pref.edit().putInt(Constants.PREFERENCES.REPEAT,Constants.PREFERENCE_VALUES.REPEAT_ONE).apply();
            textInsideRepeat.setTextColor(ColorHelper.getColor(R.color.colorwhite));
            repeat.setColorFilter(ColorHelper.getColor(R.color.colorwhite));
            textInsideRepeat.setText("1");
        }else if(pref.getInt(Constants.PREFERENCES.REPEAT,0)==Constants.PREFERENCE_VALUES.REPEAT_ONE){
            pref.edit().putInt(Constants.PREFERENCES.REPEAT,Constants.PREFERENCE_VALUES.NO_REPEAT).apply();
            repeat.setColorFilter(ColorHelper.getColor(R.color.dark_gray3));
            textInsideRepeat.setTextColor(ColorHelper.getColor(R.color.dark_gray3));
            textInsideRepeat.setText("");
        }
    }

    @OnClick(R.id.pw_ivSkipNext)
    void skipNext(){
        if(playerService.getCurrentTrack()==null) {
            Toast.makeText(this,getString(R.string.nothing_to_play),Toast.LENGTH_LONG).show();
            return;
        }
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        playerService.nextTrack();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION.COMPLETE_UI_UPDATE));
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION.PLAY_PAUSE_UI_UPDATE));

    }

    @OnClick(R.id.pw_ivSkipPrevious)
    void skippPrev(){
        if(playerService.getCurrentTrack()==null) {
            Toast.makeText(this,getString(R.string.nothing_to_play),Toast.LENGTH_LONG).show();
            return;
        }
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        playerService.prevTrack();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION.COMPLETE_UI_UPDATE));
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION.PLAY_PAUSE_UI_UPDATE));

    }

    @OnClick(R.id.pw_playButton)
    void play(){
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        playClicked();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION.PLAY_PAUSE_UI_UPDATE));
    }

    private void playClicked(){
        if(playerService.getCurrentTrack()==null){
            Toast.makeText(this,getString(R.string.nothing_to_play), Toast.LENGTH_SHORT).show();
            return;
        }

        playerService.play();

        if(playerService.getStatus()==PlayerService.PLAYING){
            mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.pw_pause));
            startUpdateTask();
        }else {
            mPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.pw_play));
            stopUpdateTask();
        }
    }

    private void setSeekbarAndTime() {
        seekBar.setProgress(UtilityFun.getProgressPercentage(playerService.getCurrentTrackProgress()
                , playerService.getCurrentTrackDuration()));
        runningTime.setText(UtilityFun.msToString(playerService.getCurrentTrackProgress()));
    }

    private void startUpdateTask(){
        if(!updateTimeTaskRunning && playerService.getStatus()==PlayerService.PLAYING ){
            stopProgressRunnable=false;
            Executors.newSingleThreadExecutor().execute(mUpdateTimeTask);
        }
    }

    private void stopUpdateTask(){
        stopProgressRunnable=true;
        updateTimeTaskRunning=false;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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
                        int curDur = playerService.getCurrentTrackProgress();
                        int per = UtilityFun.getProgressPercentage(playerService.getCurrentTrackProgress() / 1000,
                                playerService.getCurrentTrackDuration() / 1000);
                        runningTime.setText(UtilityFun.msToString(curDur));
                        seekBar.setProgress(per);
                    }
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Log.d("FragmentAlbumArt", "run: running");
            }
            updateTimeTaskRunning = false;
        }
    };


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
    }
}
