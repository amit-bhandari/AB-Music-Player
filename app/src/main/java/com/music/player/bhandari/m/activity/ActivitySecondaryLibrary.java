package com.music.player.bhandari.m.activity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.BottomOffsetDecoration;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.adapter.AlbumLibraryAdapter;
import com.music.player.bhandari.m.adapter.SecondaryLibraryAdapter;
import com.music.player.bhandari.m.customViews.ExpandableTextView;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.model.dataItem;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadArtInfoThread;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.model.PlaylistManager;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
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

public class ActivitySecondaryLibrary extends AppCompatActivity implements View.OnClickListener, ArtistInfo.Callback {

    @BindView(R.id.secondaryLibraryList) RecyclerView mRecyclerView;
    @BindView(R.id.albumsInArtistFrag) RecyclerView mAlbumsRecyclerView;
    @BindView(R.id.artistBio) ExpandableTextView artistBio;

    private SecondaryLibraryAdapter adapter;

    private BroadcastReceiver mReceiverForMiniPLayerUpdate;
    private BroadcastReceiver mReceiverForDataReady;
    @BindView(R.id.song_name_mini_player) TextView songNameMiniPlayer;
    @BindView(R.id.artist_mini_player) TextView artistNameMiniPlayer;
    @BindView(R.id.play_pause_mini_player)  ImageView buttonPlay;
    @BindView(R.id.album_art_mini_player)  ImageView albumArtIv;
    @BindView(R.id.mini_player) LinearLayout miniPlayer;
    @BindView(R.id.next_mini_plaayrer) ImageView buttonNext;
    @BindView(R.id.main_backdrop) ImageView mainBackdrop;
    @BindView(R.id.fab_right_side) FloatingActionButton fab;
    @BindView(R.id.border_view) View border;
    @BindView(R.id.progressBar) View progressBar;
    @BindView(R.id.main_collapsing) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.root_view_secondary_lib) View rootView;

    @BindView(R.id.app_bar_layout_secondary_library) AppBarLayout appBarLayout;
    private long mLastClickTime;

    private int status;
    private int key=0;  //text view on which clicked
    private String title;

    private Handler handler = new Handler(Looper.getMainLooper());

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

        ColorHelper.setStatusBarGradiant(this);

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

        final Toolbar toolbar = findViewById(R.id.toolbar_);
        try {
            toolbar.setCollapsible(false);
        }catch (Exception ignored){}

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
            //border.setVisibility(View.VISIBLE);
            border.setBackgroundResource(R.drawable.border_2dp);
        }else {
            border.setBackgroundResource(0);
        }

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                switch (status) {
                    case Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT:

                        adapter = new SecondaryLibraryAdapter(ActivitySecondaryLibrary.this, MusicLibrary.getInstance()
                                .getSongListFromArtistIdNew(key, Constants.SORT_ORDER.ASC));
                        if(adapter.getList().isEmpty()) {
                            break;
                        }

                        //get album list for artist
                        final ArrayList<dataItem> data = new ArrayList<>();
                        for(dataItem d : MusicLibrary.getInstance().getDataItemsForAlbums()){
                            if(d.artist_id == key) data.add(d);
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mAlbumsRecyclerView.setVisibility(View.VISIBLE);
                            }
                        });
                        mAlbumsRecyclerView.setAdapter(new AlbumLibraryAdapter(ActivitySecondaryLibrary.this, data));
                        mAlbumsRecyclerView.setLayoutManager( new LinearLayoutManager(ActivitySecondaryLibrary.this, LinearLayoutManager.HORIZONTAL, false));
                        mAlbumsRecyclerView.setNestedScrollingEnabled(false);

                        TrackItem item = new TrackItem();
                        item.setArtist_id(key);
                        item.setArtist(title);
                        final ArtistInfo mArtistInfo = OfflineStorageArtistBio.getArtistBioFromTrackItem(item);
                        //second check is added to make sure internet call will happen
                        //when user manually changes artist tag
                        if(mArtistInfo!=null && item.getArtist().trim().equals(mArtistInfo.getOriginalArtist().trim())){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onArtInfoDownloaded(mArtistInfo);
                                }
                            });
                        } else if (UtilityFun.isConnectedToInternet()) {
                            String artist = item.getArtist();
                            artist = UtilityFun.filterArtistString(artist);

                            new DownloadArtInfoThread(ActivitySecondaryLibrary.this, artist , item).start();
                        }

                        break;

                    case Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT:
                        adapter = new SecondaryLibraryAdapter(ActivitySecondaryLibrary.this,
                                MusicLibrary.getInstance().getSongListFromAlbumIdNew(key, Constants.SORT_ORDER.ASC));
                        Collections.sort(adapter.getList(), new Comparator<dataItem>() {
                            @Override
                            public int compare(dataItem dataItem, dataItem t1) {
                                if(dataItem.trackNumber>t1.trackNumber) return 1;
                                else if(dataItem.trackNumber<t1.trackNumber) return -1;
                                else return 0;
                            }
                        });
                        /*if(adapter.getList().isEmpty()) {
                            break;
                        }*/
                        break;

                    case Constants.FRAGMENT_STATUS.GENRE_FRAGMENT:
                        adapter = new SecondaryLibraryAdapter(ActivitySecondaryLibrary.this,
                                MusicLibrary.getInstance().getSongListFromGenreIdNew(key, Constants.SORT_ORDER.ASC));
                        if(adapter.getList().isEmpty()) {
                            break;
                        }
                        break;

                    case Constants.FRAGMENT_STATUS.PLAYLIST_FRAGMENT:
                        ArrayList<dataItem> trackList;
                        title=title.replace(" ","_");
                        switch (title) {
                            case Constants.SYSTEM_PLAYLISTS.MOST_PLAYED:
                                trackList = PlaylistManager.getInstance(getApplicationContext())
                                        .GetPlaylist(Constants.SYSTEM_PLAYLISTS.MOST_PLAYED);
                                adapter = new SecondaryLibraryAdapter(ActivitySecondaryLibrary.this,trackList , status, Constants.SYSTEM_PLAYLISTS.MOST_PLAYED);
                                break;

                            case Constants.SYSTEM_PLAYLISTS.MY_FAV:
                                trackList = PlaylistManager.getInstance(getApplicationContext())
                                        .GetPlaylist(Constants.SYSTEM_PLAYLISTS.MY_FAV);
                                adapter = new SecondaryLibraryAdapter(ActivitySecondaryLibrary.this, trackList, status, Constants.SYSTEM_PLAYLISTS.MY_FAV);
                                break;

                            case Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED:
                                trackList = PlaylistManager.getInstance(getApplicationContext())
                                        .GetPlaylist(Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED);
                                adapter = new SecondaryLibraryAdapter(ActivitySecondaryLibrary.this, trackList, status, Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED);
                                break;

                            case Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED:
                                trackList = PlaylistManager.getInstance(getApplicationContext())
                                        .GetPlaylist(Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED);
                                adapter = new SecondaryLibraryAdapter(ActivitySecondaryLibrary.this,trackList, status, Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED);
                                break;

                            default:
                                trackList = PlaylistManager.getInstance(getApplicationContext())
                                        .GetPlaylist(title);
                                adapter = new SecondaryLibraryAdapter(ActivitySecondaryLibrary.this, trackList, status, title);
                                break;
                        }

                        if(trackList.isEmpty()){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    fab.setImageDrawable(ContextCompat.getDrawable(ActivitySecondaryLibrary.this, R.drawable.ic_add_black_24dp));
                                }
                            });
                        }
                        break;
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(adapter!=null) {
                            mRecyclerView.setAdapter(adapter);
                        }

                        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(ActivitySecondaryLibrary.this));
                        mRecyclerView.setNestedScrollingEnabled(false);

                        float offsetPx = getResources().getDimension(R.dimen.bottom_offset_secondary_lib);
                        BottomOffsetDecoration bottomOffsetDecoration = new BottomOffsetDecoration((int) offsetPx);
                        mRecyclerView.addItemDecoration(bottomOffsetDecoration);

                        border.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);

                        TrackItem item=null;

                        if (adapter!=null && adapter.getList()!=null && adapter.getList().size() > 0) {
                            item = MusicLibrary.getInstance().getTrackItemFromId(adapter.getList().get(0).id);
                        }

                        Log.d("SecondaryLibraryActivi", "onCreate: item " + item);
                        if(item!=null){
                            String url = MusicLibrary.getInstance().getArtistUrls().get(item.getArtist());
                            Log.d("SecondaryLibraryActivi", "onCreate: url " + url);
                            if(UtilityFun.isConnectedToInternet() && url!=null) {
                                setArtistImage(url);
                            }else {
                                int defaultAlbumArtSetting = MyApp.getPref().getInt(getString(R.string.pref_default_album_art), 0);
                                switch (defaultAlbumArtSetting){
                                    case 0:
                                        Glide.with(ActivitySecondaryLibrary.this)
                                                .load(MusicLibrary.getInstance().getAlbumArtUri(item.getAlbumId()))
                                                .centerCrop()
                                                .placeholder(R.drawable.ic_batman_1)
                                                .crossFade()
                                                .into(mainBackdrop);
                                        break;

                                    case 1:
                                        Glide.with(ActivitySecondaryLibrary.this)
                                                .load(MusicLibrary.getInstance().getAlbumArtUri(item.getAlbumId()))
                                                .centerCrop()
                                                .placeholder(UtilityFun.getDefaultAlbumArtDrawable())
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .crossFade().diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .into(mainBackdrop);
                                        break;
                                }

                            }
                        }
                    }
                });
            }
        });

        mReceiverForMiniPLayerUpdate=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateMiniplayerUI();
            }
        };

        mReceiverForDataReady=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //updateMiniplayerUI();
                border.setVisibility(View.VISIBLE);
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




        miniPlayer.setBackgroundColor(ColorHelper.getWidgetColor());
        //collapsingToolbarLayout.setContentScrimColor(ColorHelper.Ge());
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
        fab.setBackgroundTintList(ColorStateList.valueOf(ColorHelper.getWidgetColor()));

        collapsingToolbarLayout.setStatusBarScrim(ColorHelper.getGradientDrawable());

        setTextAndIconColor();
    }

    private void setTextAndIconColor() {
        songNameMiniPlayer.setTextColor(ColorHelper.getPrimaryTextColor());
        artistNameMiniPlayer.setTextColor(ColorHelper.getSecondaryTextColor());
        artistBio.setTextColor(ColorHelper.getPrimaryTextColor());
        /*buttonPlay.setColorFilter(ColorHelper.getPrimaryTextColor());
        buttonNext.setColorFilter(ColorHelper.getPrimaryTextColor());*/
    }

    private void setArtistImage(String url) {
        Glide
                .with(getApplicationContext())
                .load(url)
                .crossFade()
                .placeholder(R.drawable.ic_batman_1)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .into(mainBackdrop);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void updateMiniplayerUI(){
        try {
            if (playerService != null) {
                if (playerService.getCurrentTrack() != null) {

                    final DrawableRequestBuilder<Uri> request = Glide.with(this)
                            .load(MusicLibrary.getInstance().getAlbumArtUri(playerService.getCurrentTrack().getAlbumId()))
                            .centerCrop()
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL);

                    int defaultAlbumArtSetting = MyApp.getPref().getInt(getString(R.string.pref_default_album_art), 0);
                    switch (defaultAlbumArtSetting) {
                        case 0:
                           request.listener(new RequestListener<Uri, GlideDrawable>() {
                                        @Override
                                        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                            //Log.d("AlbumLibraryAdapter", "onException: ");
                                            if(UtilityFun.isConnectedToInternet() &&
                                                    !MyApp.getPref().getBoolean(getString(R.string.pref_data_saver), false)) {
                                                final String url = MusicLibrary.getInstance().getArtistUrls().get(playerService.getCurrentTrack().getArtist());
                                                if(url!=null)
                                                    request.load(Uri.parse(url))
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
                                    .placeholder(R.drawable.ic_batman_1);
                            break;

                        case 1:
                            request.listener(new RequestListener<Uri, GlideDrawable>() {
                                        @Override
                                        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                            //Log.d("AlbumLibraryAdapter", "onException: ");
                                            if(UtilityFun.isConnectedToInternet() &&
                                                    !MyApp.getPref().getBoolean(getString(R.string.pref_data_saver), false)) {
                                                final String url = MusicLibrary.getInstance().getArtistUrls().get(playerService.getCurrentTrack().getArtist());
                                                if(url!=null)
                                                    request.load(Uri.parse(url))
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
                                    .placeholder(UtilityFun.getDefaultAlbumArtDrawable());
                            break;
                    }

                    request.into(albumArtIv);

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

                if (SystemClock.elapsedRealtime() - mLastClickTime < 100){
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
                if (SystemClock.elapsedRealtime() - mLastClickTime < 100){
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
                        Snackbar.make(rootView, R.string.error_equ_not_supported, Snackbar.LENGTH_SHORT).show();
                    }
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setSleepTimerDialog(final Context context){

        MyDialogBuilder builder = new MyDialogBuilder(context);

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
                            Snackbar.make(rootView,context.getString(R.string.sleep_timer_discarded) , Snackbar.LENGTH_SHORT).show();
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
                            Snackbar.make(rootView, temp, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                })
                .customView(linear,true)
                .show();
    }

    @Override
    public void onDestroy() {
        mRecyclerView=null;
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
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiverForMiniPLayerUpdate
                ,new IntentFilter(Constants.ACTION.SECONDARY_ADAPTER_DATA_READY));
        updateMiniplayerUI();
    }

    @Override
    protected void onPause() {
        MyApp.isAppVisible = false;
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiverForMiniPLayerUpdate);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiverForDataReady);
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

    @Override
    public void onArtInfoDownloaded(ArtistInfo artistInfo) {
        if(artistInfo==null) return;

        artistBio.setVisibility(View.VISIBLE);
        artistBio.setText(artistInfo.getArtistContent());

        setArtistImage(artistInfo.getImageUrl());
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
