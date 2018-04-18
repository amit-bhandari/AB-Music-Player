package com.music.player.bhandari.m.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.adapter.LyricsViewAdapter;
import com.music.player.bhandari.m.lyricCard.ActivityLyricCard;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.ViewLyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadArtInfoThread;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadLyricThread;
import com.music.player.bhandari.m.rewards.RewardPoints;
import com.music.player.bhandari.m.utils.AppLaunchCountManager;
import com.music.player.bhandari.m.utils.UtilityFun;
import com.nshmura.snappysmoothscroller.SnapType;
import com.nshmura.snappysmoothscroller.SnappyLayoutManager;
import com.nshmura.snappysmoothscroller.SnappyLinearLayoutManager;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.blurry.Blurry;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Amit AB on 9/23/2017.
 */

public class ActivityInstantLyric extends AppCompatActivity implements RecyclerView.OnItemTouchListener, Lyrics.Callback
,ActionMode.Callback, View.OnClickListener, ArtistInfo.Callback{

    private Lyrics mLyrics;
    private ArtistInfo artistInfo;


    @BindView(R.id.text_view_lyric_status) TextView lyricStatus;
    @BindView(R.id.text_view_artist_info) TextView artInfoTextView;
    @BindView(R.id.lyric_view_wrapper) View lyricWrapper;
    @BindView(R.id.view_artist_info) FloatingActionButton viewArtInfoFab;
    @BindView(R.id.fab_save_lyrics) FloatingActionButton saveLyrics;
    @BindView(R.id.loading_lyrics_animation)  AVLoadingIndicatorView lyricLoadAnimation;
    @BindView(R.id.dynamic_lyrics_recycler_view)  RecyclerView recyclerView;
    @BindView(R.id.root_view_instant_lyrics)  View rootView;
    @BindView(R.id.fab_video) FloatingActionButton watchVideo;

    @BindView(R.id.ad_view_wrapper) View adViewWrapper;
    @BindView(R.id.ad_close)  TextView adCloseText;

    private boolean isLyricsShown = true, isLyricsSaved = false;

    private Boolean fThreadCancelled =false;
    private Boolean fIsThreadRunning =false;

    private LyricsViewAdapter adapter;
    private boolean fIsStaticLyrics =true;
    private LinearLayoutManager layoutManager;

    private SharedPreferences currentMusicInfo;

    private BroadcastReceiver mReceiver;

    private String track = "";
    private String artist = "";

    private long lastClicked = 0;

    private ActionMode actionMode;
    private boolean actionModeActive = false;
    private GestureDetectorCompat gestureDetector;

    private static final int LAUNCH_COUNT_BEFORE_POPUP=5;
    private InterstitialAd mInterstitialAd;

    private Handler handler;

    @BindView(R.id.adView) AdView mAdView;

    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v("Amit AB", "created");

        currentMusicInfo = getSharedPreferences("current_music", Context.MODE_PRIVATE);

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
        setContentView(R.layout.activity_instant_lyrics);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar_);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        handler = new Handler(Looper.getMainLooper());
        initializeListeners();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //make sure lyrics view is shown if artist info was being shown
                isLyricsShown=false;
                toggleLyricsArtInfoView();
                updateLyrics(false);
            }
        };

        if(!MyApp.getPref().getBoolean(getString(R.string.pref_disclaimer_accepted),false)){
            showDisclaimerDialog();
        }

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        }

        try {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "instant_lyric_launched");
            UtilityFun.logEvent(bundle);
        }catch (Exception ignored){
        }

    }

    private void growShrinkAnimate() {
        final ScaleAnimation growAnim = new ScaleAnimation(1.0f, 1.15f, 1.0f, 1.15f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        final ScaleAnimation shrinkAnim = new ScaleAnimation(1.15f, 1.0f, 1.15f, 1.0f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        growAnim.setDuration(500);
        shrinkAnim.setDuration(500);

        watchVideo.setAnimation(growAnim);
        growAnim.start();

        growAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation){}

            @Override
            public void onAnimationRepeat(Animation animation){}

            @Override
            public void onAnimationEnd(Animation animation)
            {
                watchVideo.setAnimation(shrinkAnim);
                shrinkAnim.start();
            }
        });
        shrinkAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation){}

            @Override
            public void onAnimationRepeat(Animation animation){}

            @Override
            public void onAnimationEnd(Animation animation)
            {
                watchVideo.setAnimation(growAnim);
                growAnim.start();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("lyrics",mLyrics);
        savedInstanceState.putParcelable("artInfo",artistInfo);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mLyrics=savedInstanceState.getParcelable("lyrics");
        artistInfo=savedInstanceState.getParcelable("artInfo");
    }

    private void showAdIfApplicable() {
        if(!UtilityFun.isAdsRemoved() && AppLaunchCountManager.isEligibleForBannerAds()) {
            MobileAds.initialize(this, getString(R.string.banner_lyric_view));

            if(AppLaunchCountManager.isEligibleForInterstialAd() &&
                    (AppLaunchCountManager.getInstantLyricsCount()%LAUNCH_COUNT_BEFORE_POPUP==0)) {
                mInterstitialAd = new InterstitialAd(this);
                mInterstitialAd.setAdUnitId(getString(R.string.inter_licenses_activity));

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

            if (UtilityFun.isConnectedToInternet() ) {
                AdRequest adRequest = new AdRequest.Builder()//.addTestDevice("F40E78AED9B7FE233362079AC4C05B61")
                        .build();
                Log.d("ActivityInstantLyric", "showAdIfApplicable: adview is null " + (mAdView==null));
                if (mAdView != null) {
                    mAdView.loadAd(adRequest);
                    mAdView.setVisibility(View.VISIBLE);
                    adViewWrapper.setVisibility(View.VISIBLE);
                    adCloseText.setVisibility(View.VISIBLE);

                }
            } else {
                if (mAdView != null) {
                    mAdView.setVisibility(View.GONE);
                    adViewWrapper.setVisibility(View.GONE);
                }
            }
        }
    }

    private void initializeListeners(){
        lyricStatus.setOnClickListener(this);
        saveLyrics.setBackgroundTintList(ColorStateList.valueOf(ColorHelper.getColor(R.color.fab_Colors_lyric_view)));
        saveLyrics.setOnClickListener(this);
        viewArtInfoFab.setBackgroundTintList(ColorStateList.valueOf(ColorHelper.getColor(R.color.fab_Colors_lyric_view)));
        viewArtInfoFab.setOnClickListener(this);
        watchVideo.setBackgroundTintList(ColorStateList.valueOf(ColorHelper.getColor(R.color.fab_Colors_lyric_view)));
        watchVideo.setOnClickListener(this);
        growShrinkAnimate();
        findViewById(R.id.root_view_instant_lyrics).setBackgroundDrawable(ColorHelper.getBaseThemeDrawable());
    }

    @Override
    protected void onPause() {
        super.onPause();

        MyApp.isAppVisible = false;
        if(actionMode!=null){
            actionMode.finish();
            actionMode = null;
        }

        if(fIsThreadRunning){
            fThreadCancelled = true;
        }
        acquireWindowPowerLock(false);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.isAppVisible = true;
        showAdIfApplicable();

        if(!fIsStaticLyrics && !fIsThreadRunning && currentMusicInfo.getBoolean("playing",false)){
            fThreadCancelled=false;
            Executors.newSingleThreadExecutor().execute(lyricUpdater);
        }

        if(!fIsStaticLyrics){
            acquireWindowPowerLock(true);
            scrollLyricsToCurrentLocation();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.ACTION.UPDATE_INSTANT_LYRIC));

        track = currentMusicInfo.getString("track", "");
        artist = currentMusicInfo.getString("artist", "");

        if(mLyrics!=null
                && mLyrics.getOriginalArtist().toLowerCase().equals(artist.toLowerCase())
                && mLyrics.getOriginalTrack().toLowerCase().equals(track.toLowerCase())){
            onLyricsDownloaded(mLyrics);
        }else {
            updateLyrics(false);
        }

        AppLaunchCountManager.instantLyricsLaunched();
    }

    private void acquireWindowPowerLock(boolean acquire){
        if(acquire) {
            if (mWakeLock != null && !mWakeLock.isHeld()) {
                this.mWakeLock.acquire(10*60*1000L /*10 minutes*/);
            }
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }else {
            if(mWakeLock!=null && mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void updateLyrics(boolean discardCache, @NonNull String... param){

        if(param.length==2){
            track = param[0];
            artist = param[1];
        }else {
            track = currentMusicInfo.getString("track", "");
            artist = currentMusicInfo.getString("artist", "");
        }

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle(track);
            getSupportActionBar().setSubtitle(artist);
        }

        artInfoTextView.setText(getString(R.string.artist_info_loading));

        TrackItem item = new TrackItem();
        item.setArtist(artist);
        item.setTitle(track);
        item.setId(-1);

        //set artist photo in background
        String artist = item.getArtist();
        artist = loadArtistInfo(artist);

        if(!MyApp.getPref().getBoolean(getString(R.string.pref_disclaimer_accepted),false)) {
            lyricStatus.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            lyricStatus.setText(getString(R.string.disclaimer_rejected));
            try {
                //some exceptions reported in play console, thats why
                lyricLoadAnimation.hide();
            }catch (Exception ignored){}
            // }
            return;
        }

        //check if user have reward points, if not, display error
        if(!UtilityFun.isAdsRemoved() && RewardPoints.getRewardPointsCount()<=0){
            lyricStatus.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            lyricStatus.setText(R.string.reward_points_exhausted);
            try {
                //some exceptions reported in play console, that's why
                lyricLoadAnimation.hide();
            }catch (Exception ignored){}
            // }
            return;
        }

        if(mLyrics!=null
                && mLyrics.getOriginalArtist().toLowerCase().equals(artist.toLowerCase())
                && mLyrics.getOriginalTrack().toLowerCase().equals(track.toLowerCase())){
            onLyricsDownloaded(mLyrics);
            return;
        }



        if(mLyrics==null){
            Log.d("RewardPoints", "updateLyrics: null lyrics");
        }else {
            Log.d("RewardPoints", "updateLyrics: " + mLyrics.getOriginalArtist().toLowerCase() + " : " + artist);
            Log.d("RewardPoints", "updateLyrics: " + mLyrics.getOriginalTrack().toLowerCase() + " : " + track);
        }

        //set loading animation
        lyricLoadAnimation.setVisibility(View.VISIBLE);
        lyricLoadAnimation.show();

        //lyricCopyRightText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        fThreadCancelled =true;

        lyricStatus.setVisibility(View.VISIBLE);
        lyricStatus.setText(getString(R.string.lyrics_loading));



        //check in offline storage
        //for saved lyrics
        mLyrics = OfflineStorageLyrics.getInstantLyricsFromDB(item);
        if(mLyrics!=null){
            if(!UtilityFun.isAdsRemoved()) {
                RewardPoints.decrementByRandomInt();
            }
            onLyricsDownloaded(mLyrics);
            return;
        }

        if(!discardCache) {
            if(!UtilityFun.isAdsRemoved()) {
                RewardPoints.decrementByRandomInt();
            }
            mLyrics = OfflineStorageLyrics.getLyricsFromCache(item);
            if (mLyrics != null) {
                onLyricsDownloaded(mLyrics);
                return;
            }
        }

        if (UtilityFun.isConnectedToInternet()) {
            if(!UtilityFun.isAdsRemoved()) {
                RewardPoints.decrementByRandomInt();
            }
            fetchLyrics(item.getArtist(), item.getTitle(), null);
        } else {
            lyricStatus.setText(getString(R.string.no_connection));
            lyricLoadAnimation.hide();
        }
    }

    private String loadArtistInfo(String artist) {
        artist = UtilityFun.filterArtistString(artist);

        Log.d("ActivityInstantLyric", "updateLyrics: artist : " + artist);

        artistInfo = OfflineStorageArtistBio.getArtistInfoFromCache(artist);
        if(artistInfo!=null){
            onArtInfoDownloaded(artistInfo);
        }else {
            new DownloadArtInfoThread(this, artist, null).start();
        }
        return artist;
    }

    private void fetchLyrics(String... params) {

        String artist = params[0];
        String title = params[1];

        ///filter title string
        //title = filterTitleString(title);

        String url = null;
        if (params.length > 2)
            url = params[2];

        if (artist != null && title != null) {
            if (url == null)
                new DownloadLyricThread(this, true, null, artist, title).start();
            else
                new DownloadLyricThread(this, true, null, url, artist, title).start();
        }
    }

    @Override
    public void onLyricsDownloaded(Lyrics lyrics){

        Log.d("ActivityInstantLyric", "onLyricsDownloaded: lyrics downloaded " + (lyrics.getFlag()==Lyrics.POSITIVE_RESULT));
        Log.d("ActivityInstantLyric", "onLyricsDownloaded: " + lyrics.getOriginalArtist() + " : " + lyrics.getOriginalTrack());
        Log.d("ActivityInstantLyric", "onLyricsDownloaded: " + artist + " : " + track);
        Log.d("ActivityInstantLyric", "onLyricsDownloaded: " + lyrics.getArtist() + " : " + lyrics.getTrack());

        if(!lyrics.getOriginalTrack().equals(track)){
            return;
        }

        Log.d("ActivityInstantLyric", "onLyricsDownloaded: current musix matches downloaded lyrics");

        //put lyrics to cache
        OfflineStorageLyrics.putLyricsToCache(lyrics);

        lyricLoadAnimation.hide();
        mLyrics = lyrics;
        if (lyrics.getFlag() == Lyrics.POSITIVE_RESULT){

            Log.d("ActivityInstantLyric", "onLyricsDownloaded: "+lyrics.getArtist() + " : "+lyrics.getTrack());

            lyricStatus.setVisibility(View.GONE);
            fIsStaticLyrics = !mLyrics.isLRC();
            if(!fIsStaticLyrics){
                acquireWindowPowerLock(true);
            }
            fThreadCancelled=false;
            recyclerView.setVisibility(View.VISIBLE);
            lyricStatus.setVisibility(View.GONE);
            initializeLyricsView();

            if (getSupportActionBar() != null){
                getSupportActionBar().setTitle(lyrics.getTrack());
                getSupportActionBar().setSubtitle(lyrics.getArtist());
            }

            if(!lyrics.getArtist().equals(lyrics.getOriginalArtist())){
                loadArtistInfo(lyrics.getArtist());
            }


        }else {
            lyricStatus.setText(getString(R.string.tap_to_refresh_lyrics));
            lyricStatus.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                updateSaveDeleteFabDrawable();
            }
        });



    }

    private void updateSaveDeleteFabDrawable() {
        final Drawable drawable;
        if(OfflineStorageLyrics.isLyricsPresentInDB(track)){
            isLyricsSaved=true;
            drawable = getResources().getDrawable(R.drawable.ic_delete_black_24dp);


        }else {
            isLyricsSaved=false;
            drawable=getResources().getDrawable(R.drawable.ic_save_black_24dp);
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                saveLyrics.setImageDrawable(drawable);
            }
        });
    }

    @Override
    public void onArtInfoDownloaded(ArtistInfo artistInfo) {
        this.artistInfo =artistInfo;
        if(artistInfo.getArtistContent().equals("")){
            artInfoTextView.setText(R.string.artist_info_no_result);
            return;
        }
        OfflineStorageArtistBio.putArtistInfoToCache(artistInfo);
        new SetBlurryImagetask().execute(artistInfo);
    }

    private void initializeLyricsView(){
        if(mLyrics==null){
            return;
        }

        adapter = new LyricsViewAdapter(this, mLyrics);
        SnappyLayoutManager snappyLinearLayoutManager = new SnappyLinearLayoutManager(this);
        snappyLinearLayoutManager.setSnapType(SnapType.CENTER);
        snappyLinearLayoutManager.setSnapDuration(1500);
        //layoutManager.setSnapInterpolator(new DecelerateInterpolator());

        // Attach layout manager to the RecyclerView:
        recyclerView.setLayoutManager((RecyclerView.LayoutManager) snappyLinearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(this);
        gestureDetector =
                new GestureDetectorCompat(this, new RecyclerViewDemoOnGestureListener());

        layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
        fThreadCancelled =false;

        if(!fIsStaticLyrics && !fIsThreadRunning){
            Executors.newSingleThreadExecutor().execute(lyricUpdater);
            scrollLyricsToCurrentLocation();
        }

        //adapter.notifyDataSetChanged();


    }

    private void scrollLyricsToCurrentLocation() {
        long startTime = currentMusicInfo.getLong("startTime", System.currentTimeMillis());
        long distance = System.currentTimeMillis() - startTime;
        adapter.changeCurrent(distance);
        int index = adapter.getCurrentTimeIndex();

        if(index!=-1){
            recyclerView.smoothScrollToPosition(index);
        }
        adapter.notifyDataSetChanged();
    }

    private void syncProblemDialog(){
        new MaterialDialog.Builder(this)
                .typeface(TypeFaceHelper.getTypeFace(this),TypeFaceHelper.getTypeFace(this))
                .title(getString(R.string.lyric_sync_error_title))
                .content(getString(R.string.lyric_sync_error_content))
                .positiveText(getString(R.string.okay))
                .show();
    }

    private void shareLyrics(){
        if( mLyrics==null || mLyrics.getFlag()!=Lyrics.POSITIVE_RESULT){
            Snackbar.make(rootView, getString(R.string.error_no_lyrics)
                    , Snackbar.LENGTH_SHORT).show();
            return;
        }

        String shareBody = getString(R.string.lyrics_share_text);
        shareBody += "\n\nTrack : " + mLyrics.getTrack() + "\n" + "Artist : " + mLyrics.getArtist() + "\n\n";
        if(mLyrics.isLRC()) {
            shareBody += Html.fromHtml(adapter.getStaticLyrics()).toString();
        }else {
            shareBody += Html.fromHtml(mLyrics.getText());
        }

        shareTextIntent(shareBody);
    }

    private void shareTextIntent(String shareBody) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Lyrics");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Lyrics share!"));
    }

    private void myToggleSelection(int idx) {
        adapter.toggleSelection(idx);
        //String title = adapter.getSelectedItemCount();
        //actionMode.setTitle(title);
        if(adapter.getSelectedItemCount()==0){
            actionMode.finish();
            actionMode = null;
            return;
        }
        int numberOfItems = adapter.getSelectedItemCount();
        String selectionString = numberOfItems==1 ? " item selected" : " items selected";
        String title = numberOfItems  + selectionString;
        actionMode.setTitle(title);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_cab_recyclerview_lyrics, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_share:
                shareTextIntent(getSelectedLyricString().toString());
                actionMode.finish();
                actionModeActive = false;
                break;

            case R.id.menu_lyric_card:
                Intent intent = new Intent(this, ActivityLyricCard.class);
                intent.putExtra("lyric", getSelectedLyricString().toString()).putExtra("artist",mLyrics.getArtist());
                startActivity(intent);
                break;
        }
        return false;
    }

    private StringBuilder getSelectedLyricString() {
        StringBuilder shareString = new StringBuilder();
        List<Integer> selectedItemPositions = adapter.getSelectedItems();
        int currPos;
        for (int i = 0; i <= selectedItemPositions.size() - 1; i++) {
            currPos = selectedItemPositions.get(i);
            String lyricLine = adapter.getLineAtPosition(currPos);
            if (lyricLine != null) {
                shareString.append(lyricLine).append("\n\n");
            }
        }
        return shareString;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        actionMode.finish();
        actionModeActive = false;
        adapter.clearSelections();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab_save_lyrics:
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        saveOrDeleteLyrics();
                    }
                });
                break;

            case R.id.view_artist_info:
                //clear offline stored lyrics if any and reload
                if(artistInfo==null){
                    Snackbar.make(rootView, getString(R.string.art_info_not_available)
                            , Snackbar.LENGTH_SHORT).show();
                    return;
                }

                toggleLyricsArtInfoView();
                break;

            case R.id.lyrics_line:
                if(recyclerView!=null) {
                    int idx = recyclerView.getChildLayoutPosition(view);
                    if (actionModeActive) {
                        myToggleSelection(idx);
                        return;
                    }
                }
                break;

            case R.id.text_view_lyric_status:

                if(lyricStatus.getText().equals(getString(R.string.reward_points_exhausted))){
                    startActivity(new Intent(this, ActivityRewardVideo.class));
                }else {
                    mLyrics = null;
                    updateLyrics(true);
                    break;
                }

            case R.id.fab_video:
                UtilityFun.LaunchYoutube(this,track + " - " + artist);
                break;
        }
    }

    private void toggleLyricsArtInfoView() {
        if(isLyricsShown) {
            lyricWrapper.setVisibility(View.GONE);
            findViewById(R.id.artist_info_wrapper).setVisibility(View.VISIBLE);
            artInfoTextView.setText(artistInfo.getArtistContent());
            isLyricsShown = false;
            viewArtInfoFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_subject_black_24dp));
        }else {
            lyricWrapper.setVisibility(View.VISIBLE);
            findViewById(R.id.artist_info_wrapper).setVisibility(View.GONE);
            isLyricsShown = true;
            viewArtInfoFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_info_black_24dp));
        }
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        adViewWrapper.setVisibility(View.GONE);
        super.onDestroy();
    }

    @OnClick(R.id.ad_close)
    public void close_ad(){
        if(mAdView!=null){
            mAdView.destroy();
        }
        adViewWrapper.setVisibility(View.GONE);
    }

    private void saveOrDeleteLyrics() {
        if(isLyricsSaved){
            if(OfflineStorageLyrics.clearLyricsFromDB(track)){
                updateSaveDeleteFabDrawable();
                Snackbar.make(rootView, getString(R.string.lyrics_removed), Snackbar.LENGTH_SHORT).show();
            }
        }else {
            if (mLyrics != null && mLyrics.getOriginalTrack().equals(track) && mLyrics.getOriginalArtist().equals(artist)) {
                TrackItem item = new TrackItem();
                item.setArtist(artist);
                item.setTitle(track);
                item.setId(-1);
                if(OfflineStorageLyrics.putInstantLyricsInDB(mLyrics, item)){
                    updateSaveDeleteFabDrawable();
                    Snackbar.make(rootView, getString(R.string.lyrics_saved), Snackbar.LENGTH_SHORT).show();
                }else {
                    Snackbar.make(rootView, getString(R.string.error_saving_instant_lyrics)
                            , Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(rootView, getString(R.string.error_saving_instant_lyrics)
                        , Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    private void setBlurryBackground(Bitmap b){
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeIn.setDuration(2000);
        findViewById(R.id.full_screen_iv).startAnimation(fadeIn);

        Blurry.with(this).radius(1).color(Color.argb(100
                , 50, 0, 0)).from(b).into(((ImageView) findViewById(R.id.full_screen_iv)));
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("F40E78AED9B7FE233362079AC4C05B61")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_instant_lyrics, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(UtilityFun.isAdsRemoved()){
            menu.removeItem(R.id.action_reward_points);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                startActivity(new Intent(this,ActivityMain.class));
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                //finish();
                break;

            case R.id.action_share:
                shareLyrics();
                break;

            case R.id.action_sync_problem:
                syncProblemDialog();
                break;

            case R.id.action_remove_ads:
                startActivity(new Intent(this, ActivityRemoveAds.class));
                finish();
                break;

            case R.id.action_search:
                searchLyricDialog();
                break;

            case R.id.action_wrong_lyrics:
                wrongLyrics();
                break;

            case R.id.action_reward_points:
                startActivity(new Intent(this, ActivityRewardVideo.class));
                break;

            case R.id.action_reload:
                if((System.currentTimeMillis() - lastClicked) < 2000){
                    return super.onOptionsItemSelected(item);
                }
                TrackItem trackItem = new TrackItem();
                trackItem.setArtist(artist);
                trackItem.setTitle(track);
                trackItem.setId(-1);
                OfflineStorageLyrics.clearLyricsFromDB(trackItem);
                mLyrics=null;
                updateLyrics(true);
                lastClicked = System.currentTimeMillis();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchLyricDialog(){
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .typeface(TypeFaceHelper.getTypeFace(this),TypeFaceHelper.getTypeFace(this))
                .title(R.string.title_search_lyrics)
                .customView(R.layout.lyric_search_dialog, true)
                .positiveText(R.string.pos_search_lyric)
                .negativeText(R.string.cancel)
                .autoDismiss(false);

        View layout =  builder.build().getCustomView();
        final EditText trackTitleEditText = layout.findViewById(R.id.track_title_edit);
        final EditText artistEditText = layout.findViewById(R.id.artist_edit);

        trackTitleEditText.setText(track);
        artistEditText.setText(artist);

        final ProgressBar progressBar = layout.findViewById(R.id.progressBar);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                trackTitleEditText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
                trackTitleEditText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
            }
        }, 200);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(trackTitleEditText, InputMethodManager.SHOW_IMPLICIT);
        }

        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {

                if(trackTitleEditText.getText().toString().equals(track)
                        && artistEditText.getText().toString().equals(artist)){
                    dialog.dismiss();
                    return;
                }

                if(trackTitleEditText.getText().toString().equals("")){
                    trackTitleEditText.setError(getString(R.string.error_empty_title_lyric_search));
                    return;
                }

                String artistName = artistEditText.getText().toString();
                if(artistName.equals("")){
                    artistName = getString(R.string.unknown_artist);
                }
                progressBar.setVisibility(View.VISIBLE);
                final String finalArtistName = artistName;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        updateLyrics(false, trackTitleEditText.getText().toString(), finalArtistName);
                    }
                }, 1000);
            }
        });

        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });

        builder.build().show();
    }

    public void wrongLyrics(){
        if( mLyrics==null || mLyrics.getFlag()!=Lyrics.POSITIVE_RESULT){
            Toast.makeText(this,getString(R.string.error_no_lyrics), Toast.LENGTH_SHORT).show();
            return;
        }

        if(mLyrics.getSource()==null || !mLyrics.getSource().equals(ViewLyrics.clientUserAgent)){
            Toast.makeText(this, "No lyrics from other sources available!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ActivityInstantLyric", "wrongLyrics: starting search of lyrics");

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (artist != null && track != null) {
                    try {
                        ViewLyrics.fromMetaData(ActivityInstantLyric.this, artist, track, null, ActivityInstantLyric.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private String filterTitleString(String title){

        title = title.replaceAll("\\(.*\\)", "");
        return title;
    }

    private void showDisclaimerDialog(){
        new MaterialDialog.Builder(this)
                .typeface(TypeFaceHelper.getTypeFace(this),TypeFaceHelper.getTypeFace(this))
                .title(getString(R.string.lyrics_disclaimer_title))
                .content(getString(R.string.lyrics_disclaimer_content))
                .positiveText(getString(R.string.lyrics_disclaimer_title_pos))
                .negativeText(getString(R.string.lyrics_disclaimer_title_neg))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_disclaimer_accepted),true).apply();
                        updateLyrics(false);
                    }
                })
                .show();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }



    private class RecyclerViewDemoOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if(view!=null) {
                onClick(view);
            }
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (actionModeActive) {
                return;
            }
            // Start the CAB using the ActionMode.Callback defined above
            actionMode = startActionMode(ActivityInstantLyric.this);
            actionModeActive = true;
            int idx = recyclerView.getChildPosition(view);
            myToggleSelection(idx);
            super.onLongPress(e);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SetBlurryImagetask extends AsyncTask<ArtistInfo, String, Bitmap> {

        Bitmap b ;

        @Override
        protected Bitmap doInBackground(ArtistInfo... params) {

            //store file in cache with artist id as name
            //create folder in cache for artist images
            String CACHE_ART_THUMBS = MyApp.getContext().getCacheDir()+"/art_thumbs/";
            String actual_file_path = CACHE_ART_THUMBS+params[0].getOriginalArtist();
            File f = new File(CACHE_ART_THUMBS);
            if(!f.exists()){
                f.mkdir();
            }
            if(!new File(actual_file_path).exists()){
                //create file
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(new File(actual_file_path));
                    URL url = new URL(params[0].getImageUrl());
                    InputStream inputStream = url.openConnection().getInputStream();
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0;
                    while ( (bufferLength = inputStream.read(buffer)) > 0 )
                    {
                        fos.write(buffer, 0, bufferLength);
                    }
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            b= BitmapFactory.decodeFile(actual_file_path);
            return b;
        }

        protected void onPostExecute(Bitmap b) {
            if(b!=null) {
                setBlurryBackground(b);
            }
        }
    }

    private final Runnable lyricUpdater = new Runnable() {
        @Override
        public void run() {
            while (true){
                if(fThreadCancelled){
                    break;
                }

                fIsThreadRunning =true;
                //Log.v(Constants.L_TAG,"Lyric thread running");

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(!currentMusicInfo.getBoolean("playing", false)){
                            return;
                        }

                        long startTime = currentMusicInfo.getLong("startTime", System.currentTimeMillis());
                        long distance = System.currentTimeMillis() - startTime;
                        int index = adapter.changeCurrent(distance);

                        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                        int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                        if(index!=-1 && index>firstVisibleItem && index<lastVisibleItem){
                            recyclerView.smoothScrollToPosition(index);
                        }
                    }
                });

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            fIsThreadRunning = false;
            Log.v(Constants.L_TAG,"Lyric thread stopped");
        }
    };
}
