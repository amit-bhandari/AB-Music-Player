package com.music.player.bhandari.m.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.utils.AppLaunchCountManager;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Amit Bhandari on 3/10/2017.
 */

public class FragmentDiscSkipped extends Fragment{

    private PlayerService playerService;
    private SharedPreferences pref;
    private BroadcastReceiver mUIUpdate;
    //private AdView mAdView;

    @BindView(R.id.pw_ivShuffle)  ImageView shuffle;
    @BindView(R.id.pw_ivRepeat)  ImageView repeat;
    @BindView(R.id.text_in_repeat)  TextView textInsideRepeat;
    @BindView(R.id.album_art_now_playing) ImageView albumArt;
    @BindView(R.id.seekbar_now_playing) SeekBar seekBar;
    @BindView(R.id.pw_playButton) FloatingActionButton mPlayButton;
    @BindView(R.id.pw_runningTime) TextView runningTime;
    @BindView(R.id.pw_totalTime) TextView totalTime;
    private long mLastClickTime;

    private Handler mHandler;
    private  boolean stopProgressRunnable = false;
    private boolean updateTimeTaskRunning = false;

    @BindView(R.id.ad_view_wrapper) View adViewWrapper;
    @BindView(R.id.adView)  AdView mAdView;
    @BindView(R.id.ad_close)  TextView adCloseText;

    public FragmentDiscSkipped(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View layout = inflater.inflate(R.layout.fragment_disc_skipped, container, false);

        ButterKnife.bind(this, layout);

        playerService = MyApp.getService();
        pref = MyApp.getPref();
        mHandler=new Handler();

        InitializeUI();

        mUIUpdate = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(Constants.TAG, "update disc please Jarvis");
                UpdateUI();
            }
        };

        if(/*AppLaunchCountManager.isEligibleForInterstialAd() &&*/  !UtilityFun.isAdsRemoved()
                && AppLaunchCountManager.isEligibleForBannerAds()) {
            MobileAds.initialize(getContext(), getString(R.string.banner_disc_fragment));
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


        return layout;
    }

    @OnClick(R.id.ad_close)
    public void close_ad(){
        if(mAdView!=null){
            mAdView.destroy();
        }
        adViewWrapper.setVisibility(View.GONE);
    }

    private void InitializeUI(){

        if(!pref.getBoolean(Constants.PREFERENCES.SHUFFLE,false)){
            shuffle.setColorFilter(ColorHelper.getColor(R.color.colorwhite));
        }else {
            shuffle.setColorFilter(ColorHelper.getNowPlayingControlsColor());
        }

        if(pref.getInt(Constants.PREFERENCES.REPEAT,0)==Constants.PREFERENCE_VALUES.REPEAT_ALL){
            textInsideRepeat.setTextColor(ColorHelper.getNowPlayingControlsColor());
            repeat.setColorFilter(ColorHelper.getNowPlayingControlsColor());
            textInsideRepeat.setText("A");
        }else if(pref.getInt(Constants.PREFERENCES.REPEAT,0)==Constants.PREFERENCE_VALUES.REPEAT_ONE){
            textInsideRepeat.setTextColor(ColorHelper.getNowPlayingControlsColor());
            repeat.setColorFilter(ColorHelper.getNowPlayingControlsColor());
            textInsideRepeat.setText("1");
        }else if(pref.getInt(Constants.PREFERENCES.REPEAT,0)==Constants.PREFERENCE_VALUES.NO_REPEAT){
            repeat.setColorFilter(ColorHelper.getColor(R.color.colorwhite));
            textInsideRepeat.setTextColor(ColorHelper.getColor(R.color.colorwhite));
            textInsideRepeat.setText("");
        }
        if(playerService.getStatus()==PlayerService.PLAYING){
            mPlayButton.setImageResource(R.drawable.pw_pause);
        }else {
            mPlayButton.setImageResource(R.drawable.pw_play);
        }

        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if(b) {
                    runningTime.setText(UtilityFun.msToString(
                            UtilityFun.progressToTimer(seekBar.getProgress(), playerService.getCurrentTrackDuration())));
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
            Toast.makeText(getContext(),getString(R.string.nothing_to_play),Toast.LENGTH_LONG).show();
            return;
        }
        // mLastClickTime = SystemClock.elapsedRealtime();
        if(pref.getBoolean(Constants.PREFERENCES.SHUFFLE,false)){
            //shuffle is on, turn it off
            pref.edit().putBoolean(Constants.PREFERENCES.SHUFFLE,false).apply();
            playerService.shuffle(false);
            shuffle.setColorFilter(ColorHelper.getColor(R.color.colorwhite));
        }else {
            //shuffle is off, turn it on
            pref.edit().putBoolean(Constants.PREFERENCES.SHUFFLE,true).apply();
            playerService.shuffle(true);
            shuffle.setColorFilter(ColorHelper.getNowPlayingControlsColor());
        }
        if(getActivity()!=null){
            ((ActivityNowPlaying)getActivity()).UpdateCurrentTracklistAdapter();
        }
    }

    @OnClick(R.id.pw_ivRepeat)
    void repeat(){
        if(pref.getInt(Constants.PREFERENCES.REPEAT,0)==Constants.PREFERENCE_VALUES.NO_REPEAT){
            pref.edit().putInt(Constants.PREFERENCES.REPEAT,Constants.PREFERENCE_VALUES.REPEAT_ALL).apply();
            //repeat.setColorFilter(UtilityFun.GetDominatColor(playerService.getAlbumArt()));
            textInsideRepeat.setTextColor(ColorHelper.getNowPlayingControlsColor());
            repeat.setColorFilter(ColorHelper.getNowPlayingControlsColor());
            textInsideRepeat.setText("A");
        }else if(pref.getInt(Constants.PREFERENCES.REPEAT,0)==Constants.PREFERENCE_VALUES.REPEAT_ALL){
            pref.edit().putInt(Constants.PREFERENCES.REPEAT,Constants.PREFERENCE_VALUES.REPEAT_ONE).apply();
            textInsideRepeat.setTextColor(ColorHelper.getNowPlayingControlsColor());
            repeat.setColorFilter(ColorHelper.getNowPlayingControlsColor());
            textInsideRepeat.setText("1");
        }else if(pref.getInt(Constants.PREFERENCES.REPEAT,0)==Constants.PREFERENCE_VALUES.REPEAT_ONE){
            pref.edit().putInt(Constants.PREFERENCES.REPEAT,Constants.PREFERENCE_VALUES.NO_REPEAT).apply();
            repeat.setColorFilter(ColorHelper.getColor(R.color.colorwhite));
            textInsideRepeat.setTextColor(ColorHelper.getColor(R.color.colorwhite));
            textInsideRepeat.setText("");
        }
    }

    @OnClick(R.id.pw_ivSkipNext)
    void skipNext(){
        if(playerService.getCurrentTrack()==null) {
            Toast.makeText(getContext(),getString(R.string.nothing_to_play),Toast.LENGTH_LONG).show();
            return;
        }
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        playerService.nextTrack();
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Constants.ACTION.COMPLETE_UI_UPDATE));
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Constants.ACTION.PLAY_PAUSE_UI_UPDATE));

    }

    @OnClick(R.id.pw_ivSkipPrevious)
    void skippPrev(){
        if(playerService.getCurrentTrack()==null) {
            Toast.makeText(getContext(),getString(R.string.nothing_to_play),Toast.LENGTH_LONG).show();
            return;
        }
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        playerService.prevTrack();
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Constants.ACTION.COMPLETE_UI_UPDATE));
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Constants.ACTION.PLAY_PAUSE_UI_UPDATE));

    }

    @OnClick(R.id.pw_playButton)
    void play(){
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        playClicked();
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Constants.ACTION.PLAY_PAUSE_UI_UPDATE));
    }

    private void playClicked(){
        if(playerService.getCurrentTrack()==null){
            Toast.makeText(getActivity(),getString(R.string.nothing_to_play), Toast.LENGTH_SHORT).show();
        }

        playerService.play();

        if(playerService.getStatus()==PlayerService.PLAYING){
            mPlayButton.setImageResource(R.drawable.pw_pause);
            startUpdateTask();
        }else {
            mPlayButton.setImageResource(R.drawable.pw_play);
            stopUpdateTask();
        }
    }

    @Override
    public void onDestroyView() {
        stopUpdateTask();
        stopProgressRunnable=true;
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroyView();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if(isVisibleToUser){
            try{
                if(MyApp.getService().getStatus()==PlayerService.PLAYING){
                    mPlayButton.setImageResource(R.drawable.pw_pause);
                }else {
                    mPlayButton.setImageResource(R.drawable.pw_play);
                }
                setSeekbarAndTime();
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onPause() {
        Log.v(Constants.TAG,"Disc paused........");
        if(getContext()!=null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUIUpdate);
        }

        stopUpdateTask();
        stopProgressRunnable=true;
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.v(Constants.TAG,"Disc resumed........");
        if(getContext()!=null) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUIUpdate
                    , new IntentFilter(Constants.ACTION.COMPLETE_UI_UPDATE));
        }

        UpdateUI();

        setSeekbarAndTime();
        startUpdateTask();
        super.onResume();
    }

    private void setSeekbarAndTime() {
        seekBar.setProgress(UtilityFun.getProgressPercentage(playerService.getCurrentTrackProgress()
                , playerService.getCurrentTrackDuration()));
        runningTime.setText(UtilityFun.msToString(playerService.getCurrentTrackProgress()));
    }

    private void startUpdateTask(){
        if(getActivity()!=null && !updateTimeTaskRunning && playerService.getStatus()==PlayerService.PLAYING ){
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

    private void UpdateUI(){

        if(getActivity()==null || !isAdded() || playerService.getCurrentTrack()==null){
            return;
        }

        int currentNowPlayingBackPref = MyApp.getPref().getInt(getString(R.string.pref_now_playing_back),1);
        //if album art selected, hide small album art
        if(currentNowPlayingBackPref==2){
            albumArt.setImageBitmap(null);
        }else {
            /*
            Bitmap b = null;
            try {
                b = UtilityFun.decodeUri(getContext()
                        , MusicLibrary.getInstance().getAlbumArtUri(playerService.getCurrentTrack().getAlbumId()), 500);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (b != null) {
                albumArt.setImageBitmap(b);
            } else {
                albumArt.setImageDrawable(getResources().getDrawable(R.drawable.ic_batman_1));
            }*/
            Glide
                    .with(this)
                    .load(MusicLibrary.getInstance().getAlbumArtUri(playerService.getCurrentTrack().getAlbumId()))
                    .listener(new RequestListener<Uri, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                            Log.d("AlbumLibraryAdapter", "onException: ");
                            if(UtilityFun.isConnectedToInternet() &&
                                    !MyApp.getPref().getBoolean(getString(R.string.pref_data_saver), false)) {

                                final String url = MusicLibrary.getInstance().getArtistUrls().get(playerService.getCurrentTrack().getArtist());
                                Glide
                                        .with(getActivity())
                                        .load(url)
                                        .centerCrop()
                                        .crossFade(500)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        //.override(100, 100)
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
                    .placeholder(R.drawable.ic_batman_1)
                    .crossFade()
                    .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                    .into(albumArt);

        }
        if(playerService.getStatus()==PlayerService.PLAYING){
            mPlayButton.setImageResource(R.drawable.pw_pause);
        }else {
            mPlayButton.setImageResource(R.drawable.pw_play);
        }

        totalTime.setText(UtilityFun.msToString(playerService.getCurrentTrackDuration()));
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
                Log.d("FragmentDiscSkipped", "run: running");
            }
            updateTimeTaskRunning = false;
        }
    };

}
