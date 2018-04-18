package com.music.player.bhandari.m.activity;

import android.annotation.SuppressLint;
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
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.BottomOffsetDecoration;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.adapter.SecondaryLibraryAdapter;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.model.dataItem;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.model.PlaylistManager;
import com.music.player.bhandari.m.utils.AppLaunchCountManager;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Amit AB on 6/12/16.
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

                TrackItem currentItem = MyApp.getService().getCurrentTrack();
                if (currentItem.getTitle().equals(originalTitle)) {
                    //current song is playing, update  track item
                    MyApp.getService().updateTrackItem(MyApp.getService().getCurrentTrackPosition(), currentItem.getId(), title, artist, album);
                    MyApp.getService().PostNotification();
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
        setContentView(R.layout.activity_secondary_library);
        ButterKnife.bind(this);

        batmanDrawable = ContextCompat.getDrawable(this, R.drawable.ic_batman_1).mutate();
        //batmanDrawable.setColorFilter(ColorHelper.getPrimaryColor(), PorterDuff.Mode.OVERLAY);

        if(/*AppLaunchCountManager.isEligibleForInterstialAd() &&*/ !UtilityFun.isAdsRemoved()
                &&AppLaunchCountManager.isEligibleForBannerAds()) {
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
        // first url --> then cache --> then default
        if(item!=null){
            String url = MusicLibrary.getInstance().getArtistUrls().get(item.getArtist());
            Log.d("SecondaryLibraryActivi", "onCreate: url " + url);
            if(url!=null) {
                Glide
                        .with(getApplicationContext())
                        .load(url)
                        .crossFade(500)
                        .placeholder(R.drawable.ic_batman_1)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mainBackdrop);
            }else {
                String CACHE_ART_THUMBS = this.getCacheDir()+"/art_thumbs/";
                String actual_file_path = CACHE_ART_THUMBS+title;
                albumArtBitmap = BitmapFactory.decodeFile(actual_file_path);
                if(albumArtBitmap!=null){
                    mainBackdrop.setImageBitmap(albumArtBitmap);
                }else {
                    mainBackdrop.setImageDrawable(batmanDrawable);
                }
            }
        }

        final Drawable d;
        if(MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.GLOSSY)== Constants.PRIMARY_COLOR.GLOSSY){
            int color = 0 ;
            if(albumArtBitmap!=null) {
                color = ColorHelper.GetDominantColor(albumArtBitmap);
            }else {
                color = ColorHelper.GetDominantColor
                        (drawableToBitmap(ContextCompat.getDrawable(this, R.drawable.ic_batman_1)));
            }


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
            if (MyApp.getService() != null) {
                if (MyApp.getService().getCurrentTrack() != null) {

                    Glide.with(getApplication())
                            .load(MusicLibrary.getInstance().getAlbumArtUri(MyApp.getService().getCurrentTrack().getAlbumId()))
                            .asBitmap()
                            .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                            .centerCrop()
                            .placeholder(batmanDrawable)
                            .animate(R.anim.fade_in)
                            .into(albumArtIv);

                    //albumArtIv.setImageBitmap(MyApp.getService().getAlbumArt());
                    if (MyApp.getService().getStatus() == PlayerService.PLAYING) {
                        buttonPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_black_24dp));
                    } else {
                        buttonPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_black_24dp));
                    }
                    songNameMiniPlayer.setText(MyApp.getService().getCurrentTrack().getTitle());
                    artistNameMiniPlayer.setText(MyApp.getService().getCurrentTrack().getArtist());
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
        Intent notificationIntent=new Intent(getApplicationContext(),PlayerService.class);
        switch (view.getId()){
            case R.id.mini_player:
                Intent intent=new Intent(getApplicationContext(),ActivityNowPlaying.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(R.anim.abc_slide_in_bottom, android.R.anim.fade_out);
                Log.v(Constants.TAG,"Launch now playing Jarvis");
                //finish();
                break;

            case R.id.play_pause_mini_player:
                if(MyApp.getService().getCurrentTrack()==null) {
                    Toast.makeText(this,getString(R.string.nothing_to_play),Toast.LENGTH_LONG).show();
                    return;
                }

                if (SystemClock.elapsedRealtime() - mLastClickTime < 300){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                MyApp.getService().play();
                MyApp.getService().PostNotification();

                if (MyApp.getService().getStatus() == PlayerService.PLAYING) {
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
                MyApp.getService().nextTrack();
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

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        mRecyclerView=null;
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy(); //get search icon back on action bar
    }

    @Override
    public void onResume() {
        super.onResume();
        MyApp.isAppVisible = true;
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
