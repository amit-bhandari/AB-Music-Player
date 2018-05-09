package com.music.player.bhandari.m.activity;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.BottomOffsetDecoration;
import com.music.player.bhandari.m.adapter.LyricsViewAdapter;
import com.music.player.bhandari.m.lyricCard.ActivityLyricCard;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.model.PlaylistManager;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.model.dataItem;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.ViewLyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadLyricThread;
import com.music.player.bhandari.m.rewards.RewardPoints;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.utils.AppLaunchCountManager;
import com.music.player.bhandari.m.utils.UtilityFun;
import com.nshmura.snappysmoothscroller.SnapType;
import com.nshmura.snappysmoothscroller.SnappyLayoutManager;
import com.nshmura.snappysmoothscroller.SnappyLinearLayoutManager;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

public class FragmentLyrics extends Fragment implements RecyclerView.OnItemTouchListener
        , Lyrics.Callback, ActionMode.Callback, View.OnClickListener {

    private static Lyrics mLyrics;
    private View layout;
    private TrackItem item;
    @BindView(R.id.loading_lyrics_animation) AVLoadingIndicatorView lyricLoadAnimation;

    @BindView(R.id.ad_view_wrapper) View adViewWrapper;
    @BindView(R.id.adView)  AdView mAdView;
    @BindView(R.id.ad_close)  TextView adCloseText;

    private BroadcastReceiver mLyricChange;
    @BindView(R.id.text_view_lyric_status)  TextView  lyricStatus;
    @BindView(R.id.update_track_metadata) TextView updateTagsTextView; //, lyricCopyRightText;
    @BindView(R.id.ll_dynamic_lyric_view) LinearLayout ll_lyric_view;
    private boolean fIsStaticLyrics =true;
    @BindView(R.id.track_title_lyric_frag) EditText titleEdit;
    @BindView(R.id.track_artist_lyric_frag) EditText artistEdit;
    @BindView(R.id.button_update_metadata)  Button buttonUpdateMetadata;

    private boolean isLyricsLoaded=false;

    private Boolean fLyricUpdaterThreadCancelled =false;
    private Boolean fIsLyricUpdaterThreadRunning =false;
    private Handler handler ;

    /*private boolean fSeekbarThreadCancelled = false;
    private boolean fSeekbarRunning = false;*/

    @BindView(R.id.dynamic_lyrics_recycler_view) RecyclerView recyclerView;
    private LyricsViewAdapter adapter;
    private LinearLayoutManager layoutManager;
    private GestureDetectorCompat gestureDetector;
    private ActionMode actionMode;
    private boolean actionModeActive = false;

    /*@BindView(R.id.pw_playButton) FloatingActionButton playButton;
    private long mLastClickTime;
    private Animation playButtonAnimation;*/

    //private BroadcastReceiver mPlayPauseUpdateReceiver;

    PlayerService playerService;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_lyrics, container, false);
        ButterKnife.bind(this, layout);
        playerService = MyApp.getService();
        //bindViews();
        //initializeMiniPlaybackControlDashboard();
        //UpdateUI();
        //showAdIfApplicable();
        initializeListeners();
        return layout;
    }

    private void initializeListeners() {
        /*mPlayPauseUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                UpdateUI();
            }
        };*/
        buttonUpdateMetadata.setOnClickListener(this);

        lyricStatus.setOnClickListener(this);

        mLyricChange = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(Constants.TAG, "update lyrics please Jarvis");
                updateLyricsIfNeeded();
            }
        };

    }

    private void showAdIfApplicable() {
        if(/*AppLaunchCountManager.isEligibleForInterstialAd() &&*/ !UtilityFun.isAdsRemoved()
                && AppLaunchCountManager.isEligibleForBannerAds()) {
            MobileAds.initialize(getContext(), getString(R.string.banner_lyric_frag));
            mAdView = layout.findViewById(R.id.adView);

            if (UtilityFun.isConnectedToInternet()) {
                AdRequest adRequest = new AdRequest.Builder()//.addTestDevice("C6CC5AB32A15AF9EFB67D507C151F23E")
                        .build();
                if (mAdView != null) {
                    mAdView.loadAd(adRequest);
                    mAdView.setVisibility(View.VISIBLE);
                    adViewWrapper.setVisibility(View.VISIBLE);
                    adCloseText.setVisibility(View.VISIBLE);
                    //if fragment is invisible, pause the ad
                    if(!getUserVisibleHint()){
                        mAdView.pause();
                    }
                }
            } else {
                if (mAdView != null) {
                    mAdView.setVisibility(View.GONE);
                    adViewWrapper.setVisibility(View.GONE);
                }
            }
        }
    }

    /*private void initializeMiniPlaybackControlDashboard() {
        ImageView skipNext = layout.findViewById(R.id.pw_ivSkipNext);
        ImageView skipPrev = layout.findViewById(R.id.pw_ivSkipPrevious);

        skipNext.setOnClickListener(this);

        skipPrev.setOnClickListener(this);

        playButton.setOnClickListener(this);
        playButtonAnimation = AnimationUtils.loadAnimation(MyApp.getContext(), R.anim.shake_animation);

        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(adapter!=null && !fIsStaticLyrics && b) {
                    adapter.changeCurrent(UtilityFun.progressToTimer(seekBar.getProgress(), playerService.getCurrentTrackDuration()));
                    int index = adapter.getCurrentTimeIndex();
                    if (index != -1) {
                        recyclerView.smoothScrollToPosition(index);
                    }
                    Log.d("FragmentLyrics", "scrollLyricsToCurrentLocation: index " + index);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("FragmentLyrics", "onStartTrackingTouch: ");
                fSeekbarThreadCancelled=true;
                stopLyricUpdater();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("FragmentLyrics", "onStopTrackingTouch: ");
                playerService.seekTrack(UtilityFun.progressToTimer(seekBar.getProgress(), playerService.getCurrentTrackDuration()));
                if(adapter!=null && !fIsStaticLyrics) {
                    scrollLyricsToCurrentLocation();
                }
                if(playerService.getStatus()==PlayerService.PLAYING) {
                    fSeekbarThreadCancelled = false;
                    Executors.newSingleThreadExecutor().execute(seekbarUpdater);
                }

                startLyricUpdater();
            }
        });
    }*/

    @OnClick(R.id.ad_close)
    public void close_ad(){
        if(mAdView!=null){
            mAdView.destroy();
        }
        adViewWrapper.setVisibility(View.GONE);
    }

    private void updateLyricsIfNeeded() {
        TrackItem item = playerService.getCurrentTrack();
        if(item==null){
            lyricStatus.setText(getString(R.string.no_music_found));
            lyricStatus.setVisibility(View.VISIBLE);
            lyricLoadAnimation.hide();
            return;
        }
        if(mLyrics!=null){
            //if lyrics are already displayed for current song, skip this
            if(mLyrics.getOriginalTrack().equals(item.getTitle())){
                return;
            }
        }

        if(isLyricsLoaded){
            return;
        }

        Log.v(Constants.TAG,"Intent Song playing "+playerService.getCurrentTrack().getTitle());
        updateLyrics();
    }

    private void updateLyrics(){
        //hide edit metadata things

        if(!isAdded() || getActivity()==null){
            return;
        }

        artistEdit.setVisibility(View.GONE);
        titleEdit.setVisibility(View.GONE);
        updateTagsTextView.setVisibility(View.GONE);
        buttonUpdateMetadata.setVisibility(View.GONE);
        buttonUpdateMetadata.setClickable(false);

        //set loading animation
        lyricLoadAnimation.setVisibility(View.VISIBLE);
        lyricLoadAnimation.show();

        //lyricCopyRightText.setVisibility(View.GONE);
        ll_lyric_view.setVisibility(View.GONE);
        //ll_lyric_view.removeAllViews();
        fLyricUpdaterThreadCancelled =true;

        lyricStatus.setVisibility(View.VISIBLE);
        lyricStatus.setText(getString(R.string.lyrics_loading));

        if(!MyApp.getPref().getBoolean(getString(R.string.pref_disclaimer_accepted),false)) {
            lyricStatus.setVisibility(View.VISIBLE);
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
            lyricStatus.setText(R.string.reward_points_exhausted);
            try {
                //some exceptions reported in play console, thats why
                lyricLoadAnimation.hide();
            }catch (Exception ignored){}
            // }
            return;
        }

        if(mLyrics!=null && mLyrics.getFlag()==Lyrics.POSITIVE_RESULT
                && mLyrics.getTrackId()!=-1
                && mLyrics.getTrackId() == playerService.getCurrentTrack().getId()){
            onLyricsDownloaded(mLyrics);
            return;
        }


        item = playerService.getCurrentTrack();
        if(item!=null) {


            //check in offline storage
            mLyrics = OfflineStorageLyrics.getLyricsFromDB(item);
            if(mLyrics!=null){
                if(!UtilityFun.isAdsRemoved() ) {
                    RewardPoints.decrementByRandomInt();
                }
                onLyricsDownloaded(mLyrics);
                return;
            }

            if (UtilityFun.isConnectedToInternet()) {
                if(!UtilityFun.isAdsRemoved() ) {
                    RewardPoints.decrementByRandomInt();
                }
                fetchLyrics(item.getArtist(), item.getTitle(), null);
            } else {
                lyricStatus.setText(getString(R.string.no_connection));
                lyricLoadAnimation.hide();
            }
        }
    }

    private void fetchLyrics(String... params) {
        if (getActivity() == null )
            return;

        String artist = params[0];
        String title = params[1];

        ///filter title string
        //title = filterTitleString(title);

        String url = null;
        if (params.length > 2)
            url = params[2];

        Log.d("Fragment lyrics", "fetchLyrics: download lyric thread starting!");
        if (artist != null && title != null) {
            if (url == null)
                new DownloadLyricThread(this, true, item, artist, title).start();
            else
                new DownloadLyricThread(this, true, item, url, artist, title).start();
        }
    }

    @Override
    public void onLyricsDownloaded(Lyrics lyrics){

        isLyricsLoaded = true;
        //control comes here no matter where lyrics found, in db or online
        //so update the view here
        if(lyrics==null || getActivity()==null || !isAdded()) {
            return;
        }
        //Log.v("vlah",lyrics.getTrackId() + " " + playerService.getCurrentTrack().getId());
        //hide loading animation
        //lyricLoadAnimation.setVisibility(View.INVISIBLE);

        //before lyrics getting displayed, song has been changed already, display loading lyrics and return,
        //background thread already working to fetch latest lyrics
        //track id is -1 if lyrics are downloaded from internet and have
        //id of track from content resolver if lyrics came from offline storage
        if(lyrics.getTrackId()!=-1 && lyrics.getTrackId() != playerService.getCurrentTrack().getId()){
            return;
        }

        lyricLoadAnimation.hide();

        mLyrics = lyrics;
        if(layout!=null){
            if (lyrics.getFlag() == Lyrics.POSITIVE_RESULT){
                //  lrcView.setVisibility(View.VISIBLE);
                //lrcView.setOriginalLyrics(lyrics);
                //lrcView.setSourceLrc(lyrics.getText());
                //((TextView)layout.findViewById(R.id.textView3)).setVisibility(View.GONE);
                //updateLRC();

                //see if timing information available and update view accordingly
               // if(lyrics.isLRC()){
                lyricStatus.setVisibility(View.GONE);

                fIsStaticLyrics = !mLyrics.isLRC();

                fLyricUpdaterThreadCancelled =false;
                ll_lyric_view.setVisibility(View.VISIBLE);
                lyricStatus.setVisibility(View.GONE);
                //lyricCopyRightText.setVisibility(View.VISIBLE);

                initializeLyricsView();
            }else {
                //in case no lyrics found, set staticLyric flag true as we start lyric thread based on its value
                //and we dont want our thread to run even if no lyrics found
                TrackItem item = playerService.getCurrentTrack();
                if(item!=null) {
                    artistEdit.setVisibility(View.VISIBLE);
                    titleEdit.setVisibility(View.VISIBLE);
                    updateTagsTextView.setVisibility(View.VISIBLE);
                    buttonUpdateMetadata.setVisibility(View.VISIBLE);
                    buttonUpdateMetadata.setClickable(true);
                    titleEdit.setText(item.getTitle());
                    artistEdit.setText(item.getArtist());
                }
                fIsStaticLyrics =true;
                lyricStatus.setText(getString(R.string.tap_to_refresh_lyrics));
                lyricStatus.setVisibility(View.VISIBLE);
                //lyricCopyRightText.setVisibility(View.GONE);
            }
        }
    }

    private void initializeLyricsView(){
        if(mLyrics==null){
            return;
        }

        if(handler==null) {
            handler = new Handler(Looper.getMainLooper());
        }

        adapter = new LyricsViewAdapter(getContext(), mLyrics);
        SnappyLayoutManager snappyLinearLayoutManager = new SnappyLinearLayoutManager(getContext());
        snappyLinearLayoutManager.setSnapType(SnapType.CENTER);
        snappyLinearLayoutManager.setSnapDuration(1500);
        //layoutManager.setSnapInterpolator(new DecelerateInterpolator());

        // Attach layout manager to the RecyclerView:
        recyclerView.setLayoutManager((RecyclerView.LayoutManager) snappyLinearLayoutManager);

        float offsetPx = getResources().getDimension(R.dimen.bottom_offset_secondary_lib);
        BottomOffsetDecoration bottomOffsetDecoration = new BottomOffsetDecoration((int) offsetPx);
        recyclerView.addItemDecoration(bottomOffsetDecoration);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(this);
        gestureDetector =
                new GestureDetectorCompat(getContext(), new RecyclerViewDemoOnGestureListener());

        layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
        fLyricUpdaterThreadCancelled =false;

        if(!fIsStaticLyrics && playerService.getStatus()==PlayerService.PLAYING && !fIsLyricUpdaterThreadRunning){
            Executors.newSingleThreadExecutor().execute(lyricUpdater);
            scrollLyricsToCurrentLocation();
        }


    }

    @Override
    public void onResume() {
        /* This code together with the one in onDestroy()
         * will make the screen be always on until this Activity gets destroyed. */
        startLyricUpdater();

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mLyricChange
                ,new IntentFilter(Constants.ACTION.UPDATE_LYRIC_AND_INFO));
        /*LocalBroadcastManager.getInstance(getContext()).registerReceiver(mPlayPauseUpdateReceiver
                ,new IntentFilter(Constants.ACTION.PLAY_PAUSE_UI_UPDATE));*/
        //UpdateUI();
        updateLyrics();
        super.onResume();

        /*if(!fSeekbarRunning && playerService.getStatus()==PlayerService.PLAYING) {
            fSeekbarThreadCancelled = false;
            Executors.newSingleThreadExecutor().execute(seekbarUpdater);
        }*/

    }

    private void startLyricUpdater() {
        if(!fIsStaticLyrics && !fIsLyricUpdaterThreadRunning && playerService.getStatus()== PlayerService.PLAYING){
            fLyricUpdaterThreadCancelled =false;
            Executors.newSingleThreadExecutor().execute(lyricUpdater);
            scrollLyricsToCurrentLocation();
        }
    }

    private void scrollLyricsToCurrentLocation() {
        adapter.changeCurrent(playerService.getCurrentTrackProgress());
        int index = adapter.getCurrentTimeIndex();
        if(index!=-1){
            recyclerView.smoothScrollToPosition(index);
        }
        Log.d("FragmentLyrics", "scrollLyricsToCurrentLocation: index " + index);
        adapter.notifyDataSetChanged();
    }

    public void smoothScrollAfterSeekbarTouched(int progress){
        if(adapter!=null && !fIsStaticLyrics) {
            adapter.changeCurrent(UtilityFun.progressToTimer(progress, playerService.getCurrentTrackDuration()));
            int index = adapter.getCurrentTimeIndex();
            if (index != -1) {
                recyclerView.smoothScrollToPosition(index);
                adapter.notifyDataSetChanged();
            }
            Log.d("FragmentLyrics", "scrollLyricsToCurrentLocation: index " + index);
        }
    }

    @Override
    public void onPause() {

        if(actionMode!=null){
            actionMode.finish();
            actionMode = null;
        }
        stopLyricUpdater();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mLyricChange);
        //LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mPlayPauseUpdateReceiver);

        //fSeekbarThreadCancelled = true;
        super.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(playerService==null) playerService = MyApp.getService();
        
        if (isVisibleToUser) {
            startLyricUpdater();
        }
        else {
            stopLyricUpdater();
        }

        /*if(isVisibleToUser && seekBar!=null){
            int per = UtilityFun.getProgressPercentage(playerService.getCurrentTrackProgress() / 1000,
                playerService.getCurrentTrackDuration() / 1000);
            seekBar.setProgress(per);
        }*/

        Log.v("frag",isVisibleToUser+"");
        if(isVisibleToUser && mAdView!=null){
            mAdView.resume();
        }else {
            if(mAdView!=null){
                mAdView.pause();
            }
        }

        /*if(isVisibleToUser && playerService.getStatus()==PlayerService.PLAYING){
            fSeekbarThreadCancelled = false;
            if(!fSeekbarRunning) {
                Executors.newSingleThreadExecutor().execute(seekbarUpdater);
            }
        }else {
            fSeekbarThreadCancelled=true;
        }*/

        Log.v(Constants.L_TAG,"Called...." + isVisibleToUser);
    }

    private void stopLyricUpdater() {
        if(fIsLyricUpdaterThreadRunning){
            fLyricUpdaterThreadCancelled =true;
            fIsLyricUpdaterThreadRunning = false;
        }
    }

    public void runLyricThread(){

        isLyricsLoaded = false;
        if(!fIsStaticLyrics && !fIsLyricUpdaterThreadRunning &&playerService.getStatus()==PlayerService.PLAYING){
            fLyricUpdaterThreadCancelled =false;
            Executors.newSingleThreadExecutor().execute(lyricUpdater);
        }else {
            fLyricUpdaterThreadCancelled =true;
        }
    }

    public void clearLyrics(){

        TrackItem item = playerService.getCurrentTrack();
        if(item!=null) {
            try {
                ll_lyric_view.setVisibility(View.GONE);
                fIsStaticLyrics = true;
                lyricStatus.setText(getString(R.string.tap_to_refresh_lyrics));
                lyricStatus.setVisibility(View.VISIBLE);
                buttonUpdateMetadata.setVisibility(View.VISIBLE);
                buttonUpdateMetadata.setClickable(true);
                titleEdit.setText(item.getTitle());
                artistEdit.setText(item.getArtist());
                artistEdit.setVisibility(View.VISIBLE);
                titleEdit.setVisibility(View.VISIBLE);
                updateTagsTextView.setVisibility(View.VISIBLE);

            }catch (Exception ignored){

            }
        }

    }

    //when clicked on this, lyrics are searched again from viewlyrics
    //but this time option is given to select lyrics
    public void wrongLyrics(){
        if( mLyrics==null || mLyrics.getFlag()!=Lyrics.POSITIVE_RESULT){
            if(isAdded() && getActivity()!=null)
                Toast.makeText(getActivity(),getString(R.string.error_no_lyrics), Toast.LENGTH_SHORT).show();
            return;
        }

        if(mLyrics.getSource()==null || !mLyrics.getSource().equals(ViewLyrics.clientUserAgent)){
            if(isAdded() && getActivity()!=null)
                Toast.makeText(getActivity(), "No lyrics from other sources available!", Toast.LENGTH_SHORT).show();
            return;
        }

        final TrackItem item = playerService.getCurrentTrack();

        ///filter title string
        final String title = item.getTitle();
        final String artist = item.getArtist();

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (artist != null && title != null) {
                    try {
                        ViewLyrics.fromMetaData(getActivity(), artist, title, item, FragmentLyrics.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    public void shareLyrics(){
        if( mLyrics==null || mLyrics.getFlag()!=Lyrics.POSITIVE_RESULT || adapter==null){
            if(getActivity()!=null && isAdded()) {
                Toast.makeText(getActivity(), getString(R.string.error_no_lyrics), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String shareBody = getString(R.string.lyrics_share_text);
        shareBody += "\n\nTrack : " + mLyrics.getTrack() + "\n" + "Artist : " + mLyrics.getArtist() + "\n\n";
        if(mLyrics.isLRC() ) {
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
        if(isAdded()){
            startActivity(Intent.createChooser(sharingIntent, "Lyrics share!"));
        }else {
            Toast.makeText(getActivity(),getString(R.string.error_sharing_lyrics),Toast.LENGTH_SHORT).show();
        }
    }

    public void disclaimerAccepted(){
        updateLyrics();
    }

    private void playClicked(){
        if (getActivity() == null) {
            return;
        }
        playerService.play();
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(Constants.ACTION.PLAY_PAUSE_UI_UPDATE));

        if(!fIsStaticLyrics && !fIsLyricUpdaterThreadRunning && playerService.getStatus()==PlayerService.PLAYING){
            startLyricUpdater();
        }else {
            stopLyricUpdater();
        }

        /*if(playerService.getStatus()==PlayerService.PLAYING) {
            fSeekbarThreadCancelled = false;
            Executors.newSingleThreadExecutor().execute(seekbarUpdater);
        }else {
            fSeekbarThreadCancelled = true;
        }*/
    }

    /*private void UpdateUI(){
        if(playButton !=null && playerService!=null ) {
            if(playButtonAnimation!=null){
                playButton.startAnimation(playButtonAnimation);
            }
            if(playerService.getStatus()==PlayerService.PLAYING) {
                playButton.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.pw_pause));
            }else {
                playButton.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.pw_play));
            }
        }
    }*/

    @Override
    public void onDestroy() {
        fLyricUpdaterThreadCancelled =true;
        //fSeekbarThreadCancelled = true;
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if (mAdView != null) {
            mAdView.destroy();
        }

        fLyricUpdaterThreadCancelled =true;
        //fSeekbarThreadCancelled = true;
        super.onDestroyView();
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
                try {
                    shareTextIntent(getSelectedLyricString().toString());
                    actionMode.finish();
                    actionModeActive = false;
                }catch (IndexOutOfBoundsException e){
                    actionMode.finish();
                    actionModeActive = false;
                    Toast.makeText(getActivity(), "Invalid selection, please try again", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.menu_lyric_card:
                Intent intent = new Intent(getActivity(), ActivityLyricCard.class);
                intent.putExtra("lyric", getSelectedLyricString().toString())
                        .putExtra("artist", mLyrics.getArtist())
                        .putExtra("track", mLyrics.getTrack());
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
                shareString.append(lyricLine).append("\n");
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
            case R.id.lyrics_line:
                if(recyclerView!=null) {
                    int idx = recyclerView.getChildLayoutPosition(view);
                    if (actionModeActive) {
                        myToggleSelection(idx);
                        return;
                    }
                }
                break;

                /*
            case R.id.pw_ivSkipNext:
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                playerService.nextTrack();
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Constants.ACTION.COMPLETE_UI_UPDATE));
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Constants.ACTION.PLAY_PAUSE_UI_UPDATE));
                break;

            case R.id.pw_ivSkipPrevious:
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                playerService.prevTrack();
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Constants.ACTION.COMPLETE_UI_UPDATE));
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Constants.ACTION.PLAY_PAUSE_UI_UPDATE));
                break;

            case R.id.pw_playButton:
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                playClicked();
                break;*/

            case R.id.text_view_lyric_status:
                if(lyricStatus.getText().equals(getString(R.string.reward_points_exhausted))){
                    startActivity(new Intent(getActivity(), ActivityRewardVideo.class));
                }else {
                    lyricStatus.setText(getString(R.string.lyrics_loading));
                    updateLyrics();
                }
                break;

            case R.id.button_update_metadata:
                TrackItem item = playerService.getCurrentTrack();
                if(item==null){
                    return;
                }
                String edited_title = titleEdit.getText().toString();
                String edited_artist = artistEdit.getText().toString();

                if(edited_title.isEmpty() || edited_artist.isEmpty()){
                    Toast.makeText(getContext(),getString(R.string.te_error_empty_field), Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!edited_title.equals(item.getTitle()) ||
                        !edited_artist.equals(item.getArtist()) ){

                    //changes made, save those
                    Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Audio.Media.TITLE, edited_title);
                    values.put(MediaStore.Audio.Media.ARTIST, edited_artist);
                    getContext().getContentResolver()
                            .update(uri, values, MediaStore.Audio.Media.TITLE +"=?", new String[] {item.getTitle()});

                    dataItem d = MusicLibrary.getInstance().updateTrackNew(item.getId(), edited_title
                            , edited_artist, item.getAlbum());
                    PlaylistManager.getInstance(MyApp.getContext()).addEntryToMusicTable(d);

                    Intent intent = new Intent(getContext(), ActivityNowPlaying.class);
                    intent.putExtra("refresh", true);
                    intent.putExtra("position", playerService.getCurrentTrackPosition());
                    intent.putExtra("originalTitle",item.getTitle());
                    intent.putExtra("title", edited_title);
                    intent.putExtra("artist", edited_artist);
                    intent.putExtra("album", item.getAlbum());
                    startActivity(intent);

                    artistEdit.setVisibility(View.GONE);
                    titleEdit.setVisibility(View.GONE);
                    updateTagsTextView.setVisibility(View.GONE);
                    buttonUpdateMetadata.setVisibility(View.GONE);
                    buttonUpdateMetadata.setClickable(false);

                    if(getActivity()!=null) {
                        if (getActivity().getCurrentFocus() != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity()
                                    .getSystemService(Context.INPUT_METHOD_SERVICE);
                            if(imm!=null) {
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                        }
                    }
                }else {
                    Toast.makeText(getContext(),getString(R.string.change_tags_to_update),Toast.LENGTH_SHORT).show();
                }
                break;
        }
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
            actionMode = getActivity().startActionMode(FragmentLyrics.this);
            actionModeActive = true;
            int idx = recyclerView.getChildPosition(view);
            myToggleSelection(idx);
            super.onLongPress(e);
        }
    }

    private final Runnable lyricUpdater = new Runnable() {
        @Override
        public void run() {
            while (true){
                if(fLyricUpdaterThreadCancelled){
                    break;
                }

                fIsLyricUpdaterThreadRunning =true;
                Log.v(Constants.L_TAG,"Lyric thread running");

                if(getActivity()!=null ){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            int index = adapter.changeCurrent(playerService.getCurrentTrackProgress());
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
            }

            fIsLyricUpdaterThreadRunning = false;
            Log.v(Constants.L_TAG,"Lyric thread stopped");
        }
    };

    /*private final Runnable seekbarUpdater = new Runnable() {
        @Override
        public void run() {

            while (true) {
                if(fSeekbarThreadCancelled){
                    break;
                }
                fSeekbarRunning = true;
                if(seekBar!=null) {
                    int per = UtilityFun.getProgressPercentage(playerService.getCurrentTrackProgress() / 1000,
                            playerService.getCurrentTrackDuration() / 1000);
                    seekBar.setProgress(per);
                }

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.d("FragmentLyrics", "run: ");
            }

            fSeekbarRunning = false;
        }
    };*/
}

