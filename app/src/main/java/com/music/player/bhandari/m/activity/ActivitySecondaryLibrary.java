package com.music.player.bhandari.m.activity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.ArcMotion;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.BottomOffsetDecoration;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.adapter.SecondaryLibraryAdapter;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.model.dataItem;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.model.PlaylistManager;
import com.music.player.bhandari.m.transition.MorphMiniToNowPlaying;
import com.music.player.bhandari.m.transition.MorphNowPlayingToMini;
import com.music.player.bhandari.m.utils.AppLaunchCountManager;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

public class ActivitySecondaryLibrary extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.ad_view_wrapper) View adViewWrapper;
    @BindView(R.id.adView)  AdView mAdView;
    @BindView(R.id.ad_close)  TextView adCloseText;

    @BindView(R.id.secondaryLibraryList) RecyclerView mRecyclerView;
    private SecondaryLibraryAdapter adapter;
    private BroadcastReceiver mReceiverForMiniPLayerUpdate;
    @BindView(R.id.song_name_mini_player) TextView songNameMiniPlayer;
    @BindView(R.id.artist_mini_player) TextView artistNameMiniPlayer;
    @BindView(R.id.play_pause_mini_player)  ImageView buttonPlay;
    @BindView(R.id.album_art_mini_player)  ImageView albumArtIv;
    @BindView(R.id.mini_player) LinearLayout miniPlayer;
    @BindView(R.id.next_mini_plaayrer) ImageView buttonNext;
    @BindView(R.id.main_backdrop) ImageView mainBackdrop;
    @BindView(R.id.fab_right_side) FloatingActionButton fab;
    @BindView(R.id.border_view) View border;
    @BindView(R.id.main_collapsing) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.root_view_secondary_lib) View rootView;

    private long mLastClickTime;

    private int status;
    private int key=0;  //text view on which clicked
    private String title;

    private Drawable batmanDrawable;

    PlayerService playerService;

    private int RC_LOGIN = 100;

    public ActivitySecondaryLibrary(){}

    @Override
    protected void onNewIntent(Intent intent) {
        try {
            boolean b = intent.getBooleanExtra("refresh", false);
            if (b) {
                int position = intent.getIntExtra("position", -1);
                String title = intent.getStringExtra("title");
                String artist = intent.getStringExtra("artist");
                String album = intent.getStringExtra("album");
                String originalTitle = intent.getStringExtra("originalTitle");

                TrackItem currentItem = playerService.getCurrentTrack();
                if (currentItem.getTitle().equals(originalTitle)) {
                    //current song is playing, update  track item
                    playerService.updateTrackItem(playerService.getCurrentTrackPosition(), currentItem.getId(), title, artist, album);
                    playerService.PostNotification();
                    updateMiniplayerUI();
                }

                //data changed in edit track info activity, update item
                adapter.updateItem(position, title, artist, album);
            }
        }catch (Exception ignored){
            Log.v(Constants.TAG,ignored.toString());
        }
        super.onNewIntent(intent);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if player service not running, kill the app
        if(MyApp.getService()==null){
            UtilityFun.restartApp();
            finish();
            return;
        }

        playerService = MyApp.getService();

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
        setContentView(R.layout.activity_secondary_library);
        ButterKnife.bind(this);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setupSharedElementTransitions();
        }*/

        batmanDrawable = ContextCompat.getDrawable(this, R.drawable.ic_batman_1).mutate();
        //batmanDrawable.setColorFilter(ColorHelper.getPrimaryColor(), PorterDuff.Mode.OVERLAY);

        if(false/*AppLaunchCountManager.isEligibleForInterstialAd() && !UtilityFun.isAdsRemoved()
                &&AppLaunchCountManager.isEligibleForBannerAds()*/) {
            MobileAds.initialize(getApplicationContext(), getString(R.string.banner_secondary_activity));
            if (UtilityFun.isConnectedToInternet()) {
                AdRequest adRequest = new AdRequest.Builder()//.addTestDevice("C6CC5AB32A15AF9EFB67D507C151F23E")
                        .build();
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

        Toolbar toolbar = findViewById(R.id.toolbar_);
        try {
            toolbar.setCollapsible(false);
        }catch (Exception e){}

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if(getIntent()!=null) {
            status = getIntent().getIntExtra("status",0);
            key = getIntent().getIntExtra("key",0);
            title = getIntent().getStringExtra("title");
        }
        //remove _ from playlist name

        if(status==Constants.FRAGMENT_STATUS.PLAYLIST_FRAGMENT){
            setTitle(title.replace("_"," "));
        }else {
            setTitle(title);
        }

        if(MyApp.isLocked()){
            border.setVisibility(View.VISIBLE);
        }else {
            border.setVisibility(View.GONE);
        }

        Bitmap albumArtBitmap = null;
        switch (status) {
                case Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT:

                    adapter = new SecondaryLibraryAdapter(this, MusicLibrary.getInstance()
                            .getSongListFromArtistIdNew(key, Constants.SORT_ORDER.ASC));
                    if(adapter.getList().isEmpty()) {
                        break;
                    }
                    break;



                case Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT:
                    adapter = new SecondaryLibraryAdapter(this,
                            MusicLibrary.getInstance().getSongListFromAlbumIdNew(key, Constants.SORT_ORDER.ASC));
                    if(adapter.getList().isEmpty()) {
                        break;
                    }
                    break;

                case Constants.FRAGMENT_STATUS.GENRE_FRAGMENT:
                    adapter = new SecondaryLibraryAdapter(this,
                            MusicLibrary.getInstance().getSongListFromGenreIdNew(key, Constants.SORT_ORDER.ASC));
                    if(adapter.getList().isEmpty()) {
                        break;
                    }
                    break;

                case Constants.FRAGMENT_STATUS.PLAYLIST_FRAGMENT:
                    ArrayList<dataItem> trackList=new ArrayList<>();
                    title=title.replace(" ","_");
                    switch (title) {
                        case Constants.SYSTEM_PLAYLISTS.MOST_PLAYED:
                            trackList = PlaylistManager.getInstance(getApplicationContext())
                                    .GetPlaylist(Constants.SYSTEM_PLAYLISTS.MOST_PLAYED);
                            adapter = new SecondaryLibraryAdapter(this,trackList , status, Constants.SYSTEM_PLAYLISTS.MOST_PLAYED);
                            break;

                        case Constants.SYSTEM_PLAYLISTS.MY_FAV:
                            trackList = PlaylistManager.getInstance(getApplicationContext())
                                    .GetPlaylist(Constants.SYSTEM_PLAYLISTS.MY_FAV);
                            adapter = new SecondaryLibraryAdapter(this, trackList, status, Constants.SYSTEM_PLAYLISTS.MY_FAV);
                            break;

                        case Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED:
                            trackList = PlaylistManager.getInstance(getApplicationContext())
                                    .GetPlaylist(Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED);
                            adapter = new SecondaryLibraryAdapter(this, trackList, status, Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED);
                            break;

                        case Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED:
                            trackList = PlaylistManager.getInstance(getApplicationContext())
                                    .GetPlaylist(Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED);
                            adapter = new SecondaryLibraryAdapter(this,trackList, status, Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED);
                            break;

                        default:
                            trackList = PlaylistManager.getInstance(getApplicationContext())
                                    .GetPlaylist(title);
                            adapter = new SecondaryLibraryAdapter(this, trackList, status, title);
                            break;
                    }

                    if(trackList.isEmpty()){
                        fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add_black_24dp));
                    }
                    break;
            }

        if(adapter!=null) {
            mRecyclerView.setAdapter(adapter);
        }

        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this));

        float offsetPx = getResources().getDimension(R.dimen.bottom_offset_secondary_lib);
        BottomOffsetDecoration bottomOffsetDecoration = new BottomOffsetDecoration((int) offsetPx);
        mRecyclerView.addItemDecoration(bottomOffsetDecoration);

        mReceiverForMiniPLayerUpdate=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateMiniplayerUI();
            }
        };

        miniPlayer.setOnClickListener(this);

        buttonPlay.setOnClickListener(this);

        buttonNext.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        TrackItem item=null;
        if (adapter.getList().size() > 0) {
            item = MusicLibrary.getInstance().getTrackItemFromId(adapter.getList().get(0).id);
        }

        Log.d("SecondaryLibraryActivi", "onCreate: item " + item);
        if(item!=null){
            String url = MusicLibrary.getInstance().getArtistUrls().get(item.getArtist());
            Log.d("SecondaryLibraryActivi", "onCreate: url " + url);
            if(UtilityFun.isConnectedToInternet() && url!=null) {
                Glide
                        .with(getApplicationContext())
                        .load(url)
                        //.crossFade(500)
                        .placeholder(R.drawable.ic_batman_1)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mainBackdrop);
            }else {
                Glide.with(getApplicationContext())
                        .load(MusicLibrary.getInstance().getAlbumArtUri(item.getAlbumId()))
                        //.crossFade(500)
                        .centerCrop()
                        .placeholder(R.drawable.ic_batman_1)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mainBackdrop);
            }
        }

        final Drawable d;
        if(MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.GLOSSY) == Constants.PRIMARY_COLOR.GLOSSY){
            int color ;

            color = ColorHelper.GetDominantColor
                    (drawableToBitmap(ContextCompat.getDrawable(this, R.drawable.ic_batman_1)));

            d = new GradientDrawable(
                    GradientDrawable.Orientation.BR_TL,
                    new int[] {color,0xFF131313});
        }else {
            d = ColorHelper.getBaseThemeDrawable();
        }


        rootView.setBackgroundDrawable(d);

        miniPlayer.setBackgroundColor(ColorHelper.getPrimaryColor());
        collapsingToolbarLayout.setContentScrimColor(ColorHelper.getPrimaryColor());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status==Constants.FRAGMENT_STATUS.PLAYLIST_FRAGMENT && adapter.getItemCount()<=2){
                    startActivity(new Intent(ActivitySecondaryLibrary.this,ActivityMain.class)
                            .putExtra("move_to_tab",Constants.TABS.TRACKS));
                   }else {
                    if (adapter.getItemCount() <= 0) {
                        Toast.makeText(ActivitySecondaryLibrary.this, "Empty Track List", Toast.LENGTH_SHORT).show();
                    } else {
                        adapter.shuffleAll();
                    }
                }
            }
        });
        fab.setBackgroundTintList(ColorStateList.valueOf(ColorHelper.getColor(R.color.fab_Colors_lyric_view)));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupSharedElementTransitions() {
        //ArcMotion arcMotion = new ArcMotion();
        //arcMotion.setMinimumHorizontalAngle(50f);
        //arcMotion.setMinimumVerticalAngle(50f);

        android.view.animation.Interpolator easeInOut = AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in);

        MorphMiniToNowPlaying sharedEnter = new MorphMiniToNowPlaying();
        //sharedEnter.setPathMotion(arcMotion);
        sharedEnter.setInterpolator(easeInOut);

        MorphNowPlayingToMini sharedExit = new MorphNowPlayingToMini();
        //sharedExit.setPathMotion(arcMotion);
        sharedExit.setInterpolator(easeInOut);

        if (mainBackdrop != null) {
            sharedEnter.addTarget(mainBackdrop);
            sharedExit.addTarget(mainBackdrop);
        }

        getWindow().setSharedElementEnterTransition(sharedEnter);
        getWindow().setSharedElementExitTransition(sharedExit);
        //postponeEnterTransition();
        //getWindow().sharedElementEnterTransition = sharedEnter
        //getWindow().sharedElementReturnTransition = sharedReturn

    }

    @OnClick(R.id.ad_close)
    public void close_ad(){
        if(mAdView!=null){
            mAdView.destroy();
        }
        adViewWrapper.setVisibility(View.GONE);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void updateMiniplayerUI(){
        try {
            if (playerService != null) {
                if (playerService.getCurrentTrack() != null) {

                    //commented code for window transition flicker
                    /*Glide.with(getApplication())
                            .load(MusicLibrary.getInstance().getAlbumArtUri(playerService.getCurrentTrack().getAlbumId()))
                            //.asBitmap()
                            //.signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                            .centerCrop()
                            .placeholder(batmanDrawable)
                            //.animate(R.anim.fade_in)
                            .into(albumArtIv);*/

                    Glide.with(this)
                            .load(MusicLibrary.getInstance().getAlbumArtUri(playerService.getCurrentTrack().getAlbumId()))
                            .listener(new RequestListener<Uri, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    //Log.d("AlbumLibraryAdapter", "onException: ");
                                    if(UtilityFun.isConnectedToInternet() &&
                                            !MyApp.getPref().getBoolean(getString(R.string.pref_data_saver), false)) {
                                        final String url = MusicLibrary.getInstance().getArtistUrls().get(playerService.getCurrentTrack().getArtist());
                                        Glide
                                                .with(ActivitySecondaryLibrary.this)
                                                .load(url)
                                                .centerCrop()
                                                .crossFade(500)
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .override(100, 100)
                                                .placeholder(R.drawable.ic_batman_1)
                                                .into(albumArtIv);
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
                            //removed because of window transition flicker
                            //.signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                            //.override(100,100)
                            .placeholder(R.drawable.ic_batman_1)
                            .crossFade()
                            .into(albumArtIv);

                    //albumArtIv.setImageBitmap(playerService.getAlbumArt());
                    if (playerService.getStatus() == PlayerService.PLAYING) {
                        buttonPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_black_24dp));
                    } else {
                        buttonPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_black_24dp));
                    }
                    songNameMiniPlayer.setText(playerService.getCurrentTrack().getTitle());
                    artistNameMiniPlayer.setText(playerService.getCurrentTrack().getArtist());
                    ((AppBarLayout) findViewById(R.id.app_bar_layout)).setExpanded(true);
                    //mHandler.post(getDominantColorRunnable());
                }
            } else {
                //this should not happen
                //restart app
                System.exit(0);
            }
        }catch (Exception ignored){

        }
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
                ActivityOptions options;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    options = ActivityOptions.makeSceneTransitionAnimation(this, albumArtIv, getString(R.string.transition));
                    ActivityCompat.startActivityForResult(this, intent, RC_LOGIN, options.toBundle());
                }else {
                    startActivity(intent);
                    overridePendingTransition(R.anim.abc_slide_in_bottom, android.R.anim.fade_out);
                }
                Log.v(Constants.TAG,"Launch now playing Jarvis");
                break;

            case R.id.play_pause_mini_player:
                if(playerService.getCurrentTrack()==null) {
                    Toast.makeText(this,getString(R.string.nothing_to_play),Toast.LENGTH_LONG).show();
                    return;
                }

                if (SystemClock.elapsedRealtime() - mLastClickTime < 300){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                playerService.play();
                playerService.PostNotification();

                if (playerService.getStatus() == PlayerService.PLAYING) {
                    buttonPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_black_24dp));
                } else {
                    buttonPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_black_24dp));
                }
                /*
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent()
                        .setAction(Constants.ACTION.PLAY_PAUSE_ACTION));*/
                break;

            case R.id.next_mini_plaayrer:
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                playerService.nextTrack();
                updateMiniplayerUI();
                /*
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent()
                        .setAction(Constants.ACTION.NEXT_ACTION));*/
                Log.v(Constants.TAG,"next track please Jarvis");
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        for(int i = 0; i < menu.size(); i++){
            if(R.id.action_search==menu.getItem(i).getItemId()
                   || R.id.action_sort==menu.getItem(i).getItemId() ) {
                menu.getItem(i).setVisible(false);
            }
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this,ActivitySettings.class).putExtra("ad",true));
                break;

            case R.id.action_sleep_timer:
                setSleepTimerDialog(this);
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
        }

        return super.onOptionsItemSelected(item);
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
    public void onDestroy() {
        mRecyclerView=null;
        mAdView.destroy();
        super.onDestroy(); //get search icon back on action bar
    }

    @Override
    public void onResume() {
        super.onResume();
        MyApp.isAppVisible = true;
        if(MyApp.getService()==null){
            UtilityFun.restartApp();
            finish();
            return;
        }else {
            playerService = MyApp.getService();
        }

        if(adapter!=null) {
            adapter.bindService();
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiverForMiniPLayerUpdate
                ,new IntentFilter(Constants.ACTION.COMPLETE_UI_UPDATE));
        updateMiniplayerUI();
    }

    @Override
    protected void onPause() {
        MyApp.isAppVisible = false;
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiverForMiniPLayerUpdate);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
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
    //for catching exception generated by recycler view which was causing abend, no other way to handle this
    class WrapContentLinearLayoutManager extends LinearLayoutManager {
        WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        //... constructor
        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
    }

}
