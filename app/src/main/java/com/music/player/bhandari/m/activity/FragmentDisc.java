package com.music.player.bhandari.m.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.customViews.nowplayingnew.PlayLayout;
import com.music.player.bhandari.m.customViews.nowplayingnew.VisualizerShadowChanger;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.utils.AppLaunchCountManager;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.io.IOException;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Amit Bhandari on 3/10/2017.
 */

public class FragmentDisc extends Fragment{

    // private static MusicPlayerView mpv;
    @BindView(R.id.mpv)  PlayLayout mpv;
    private PlayerService playerService;
    private SharedPreferences pref;
    private BroadcastReceiver mDiscUpdate, mPlayPauseUpdate;
    //private AdView mAdView;

    @BindView(R.id.pw_ivShuffle)  ImageView shuffle;
    @BindView(R.id.pw_ivRepeat)  ImageView repeat;
    @BindView(R.id.text_in_repeat)  TextView textInsideRepeat;
    @BindView(R.id.pw_timeTextView) TextView timeText;

    private long mLastClickTime;

    private Handler mHandler;
    private  boolean stopProgressRunnable = false;
    private boolean updateTimeTaskRunning = false;

    private VisualizerShadowChanger mShadowChanger;

    final private int MY_PERMISSIONS_REQUEST = 0;
    private static String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO};

    @BindView(R.id.ad_view_wrapper) View adViewWrapper;
    @BindView(R.id.adView)  AdView mAdView;
    @BindView(R.id.ad_close)  TextView adCloseText;

    public FragmentDisc(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View layout = inflater.inflate(R.layout.fragment_disc, container, false);

        ButterKnife.bind(this, layout);

        if(!MyApp.getPref().getBoolean("never_show_button_again", false)) {
            layout.findViewById(R.id.card_view_login_to_remove_ads).setVisibility(View.VISIBLE);
            Button loginToRemoveAds = (Button) layout.findViewById(R.id.login_to_remove_ads);
            loginToRemoveAds.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getActivity()!=null && getActivity() instanceof ActivityNowPlaying){
                        ((ActivityNowPlaying)getActivity()).signIn();

                    }
                }
            });

            ImageView dismissLoginToRemoveAds = (ImageView) layout.findViewById(R.id.dismiss_Ad_removal_button);
            dismissLoginToRemoveAds.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MyApp.getPref().edit().putBoolean("never_show_button_again",true).apply();
                    layout.findViewById(R.id.card_view_login_to_remove_ads).setVisibility(View.GONE);
                }
            });
        }

        playerService = MyApp.getService();
        pref = MyApp.getPref();
        mHandler=new Handler();

        InitializeMPV();

        mDiscUpdate = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(Constants.TAG, "update disc please Jarvis");
                UpdateDisc();
            }
        };

        mPlayPauseUpdate = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(Constants.TAG, "update play pause please Jarvis");
                animateDiscView();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if(grantResults.length==0){
                    return;
                }
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startVisualiser();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void InitializeMPV(){
        mpv.setOnButtonsClickListener(new PlayLayout.OnButtonsClickListener() {
            @Override
            public void onShuffleClicked() {
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

            @Override
            public void onSkipPreviousClicked() {
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

            @Override
            public void onSkipNextClicked() {
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

            @Override
            public void onRepeatClicked() {
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

            @Override
            public void onPlayButtonClicked() {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                playClicked();
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Constants.ACTION.PLAY_PAUSE_UI_UPDATE));
            }
        });

        mpv.setOnProgressChangedListener(new PlayLayout.OnProgressChangedListener() {
            @Override
            public void onPreSetProgress() {
                //called when tuoched the progress bar
                Log.v("progress","called");
                ((ActivityNowPlaying)getActivity()).enableViewpagerScroll(false);
                //stopUpdateTask();
            }

            @Override
            public void onProgressChanged(float progress) {
                //called when left progress bar
                Log.v("progress",progress+"");
                int totalDuration = playerService.getCurrentTrackDuration();
                int currentDuration = UtilityFun.progressToTimer((int) (progress*100), totalDuration);

                ((ActivityNowPlaying)getActivity()).enableViewpagerScroll(true);

                playerService.seekTrack(currentDuration);
                //startUpdateTask();
            }
        });

        mpv.fastOpen();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            startVisualiser();
        }else {
            if(!MyApp.getPref().getBoolean(getString(R.string.pref_never_show_record_permission), false)){
                new MaterialDialog.Builder(getActivity())
                        .typeface(TypeFaceHelper.getTypeFace(getContext()),TypeFaceHelper.getTypeFace(getContext()))
                        .title(getString(R.string.disc_frag_record_perm_title))
                        .content( getString(R.string.disc_frag_record_perm_content))
                        .positiveText(getString(R.string.okay))
                        .neutralText(getString(R.string.disc_frag_record_perm_neu))
                        .negativeText(getString(R.string.disc_frag_record_perm_neg))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        PERMISSIONS,
                                        MY_PERMISSIONS_REQUEST);
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                MyApp.getPref().edit().putBoolean(getString(R.string.pref_never_show_record_permission), true).apply();
                            }
                        })
                        .show();
            }


        }

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


    }

    private void startVisualiser() {
        //thread to make sure no ANR from here
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mShadowChanger == null) {
                        mShadowChanger = VisualizerShadowChanger.newInstance(playerService.getAudioSessionId());
                        mShadowChanger.setEnabledVisualization(true);
                        mpv.setShadowProvider(mShadowChanger);
                        Log.i("startVisualiser", "startVisualiser " + playerService.getAudioSessionId());
                    }
                }catch (Exception ignored){

                }
            }
        });

    }

    private void playClicked(){
        if (mpv == null) {
            return;
        }

        if(playerService.getCurrentTrack()==null){
            Toast.makeText(getActivity(),getString(R.string.nothing_to_play), Toast.LENGTH_SHORT).show();
        }
        playerService.play();

        if(playerService.getStatus()==PlayerService.PLAYING) {
            startUpdateTask();
        }else {
            stopUpdateTask();
        }
    }

    private void animateDiscView() {
        if(playerService.getStatus()==PlayerService.PLAYING){
            if (!mpv.isOpen()) {
                mpv.startRevealAnimation();
            }
            mpv.rotate(true);
            timeText.setVisibility(View.VISIBLE);
        }else {
            if (mpv.isOpen()) {
                mpv.startDismissAnimation();
                mpv.rotate(false);

            }
            timeText.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onDestroyView() {
        stopUpdateTask();
        if (mShadowChanger != null) {
            mShadowChanger.release();
        }
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        Log.v(Constants.TAG,"Disc paused........");
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mDiscUpdate);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mPlayPauseUpdate);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mShadowChanger != null) {
                        mShadowChanger.setEnabledVisualization(false);
                        Log.d("FragmentDisc", "run: disabled");
                    }
                }catch (Exception ignored){}
            }
        });

        stopUpdateTask();
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.v(Constants.TAG,"Disc resumed........");
        if(getContext()!=null) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mDiscUpdate
                    , new IntentFilter(Constants.ACTION.DISC_UPDATE));
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mPlayPauseUpdate
                    , new IntentFilter(Constants.ACTION.PLAY_PAUSE_UI_UPDATE));
        }

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mShadowChanger != null) {
                        mShadowChanger.setEnabledVisualization(true);
                        Log.d("FragmentDisc", "run: enabled");
                    }
                }catch (Exception ignored){}
            }
        });

        UpdateDisc();
        animateDiscView();

        if(playerService.getStatus()==PlayerService.PLAYING) {
            startUpdateTask();
        }else {
            stopUpdateTask();
        }
        super.onResume();
    }

    private void UpdateDisc(){

        if(getActivity()==null || !isAdded() || playerService.getCurrentTrack()==null){
            return;
        }


        Bitmap b=null ;
        try {
            b= UtilityFun.decodeUri(getContext()
                    ,MusicLibrary.getInstance().getAlbumArtUri(playerService.getCurrentTrack().getAlbumId()),500);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        if(b!=null) {
            mpv.setImageBitmap(b);
        }else {
            mpv.setImageBitmap(null);
        }


        mpv.setProgressEnabled(true);

        int color;

        if(b!=null) {
             color = ColorHelper.GetDominantColor(b);
             //color = (color & 0x00FFFFFF) | 0x40000000;
             mpv.setProgressBallColor(ColorHelper.getBrightColor(color));
             mpv.setProgressCompleteColor(color);
             mpv.setShadowStartHardColor(ColorHelper.getBrightColor(color));
        }
        //change colors
        //mpv.

        animateDiscView();
    }

    private void startUpdateTask(){
        if(getActivity()!=null && !updateTimeTaskRunning && playerService.getStatus()==PlayerService.PLAYING ){
            stopProgressRunnable=false;
            updateTimeTaskRunning=true;
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
        Log.d("FragmentDisc", "stopUpdateTask: ");
    }

    private final Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            while (true) {
                Log.d("FragmentDisc", "stopProgressRunnable: " + stopProgressRunnable);
                if (stopProgressRunnable) {
                    break;
                }
                updateTimeTaskRunning=true;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int totalDur = playerService.getCurrentTrackDuration();
                        int curDur = playerService.getCurrentTrackProgress();
                        mpv.setProgress(((float)curDur/totalDur));
                        timeText.setText(UtilityFun.secondsToString(playerService.getCurrentTrackProgress()/1000));
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


     /*
        startStopButton = layout.findViewById(R.id.button_start_stop);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(repeatStatus == REPEAT_STATUS.NOTHING){
                    startTime = playerService.getCurrentTrackProgress();
                    startText.setText(UtilityFun.msToString(startTime));
                    repeatStatus = REPEAT_STATUS.GOT_START_TIME;
                } else if(repeatStatus == REPEAT_STATUS.GOT_START_TIME){
                    stopTime = playerService.getCurrentTrackProgress();
                    stopText.setText(UtilityFun.msToString(stopTime));
                    repeatStatus = REPEAT_STATUS.GOT_STOP_TIME;
                    new Thread(mRepeatSomePartTask).start();
                } else if(repeatStatus == REPEAT_STATUS.GOT_STOP_TIME){
                    stopText.setText("");
                    startText.setText("");
                    repeatStatus = REPEAT_STATUS.NOTHING;
                }
            }
        });

        startText = layout.findViewById(R.id.text_start);
        stopText = layout.findViewById(R.id.text_stop);
        stopText.setText("");
        startText.setText("");*/
    /*
    //parameters for repeat particular song part
    private TextView startText , stopText;
    private Button startStopButton;
    private int startTime=-1, stopTime=-1;

    private int repeatStatus=REPEAT_STATUS.NOTHING;

    private interface REPEAT_STATUS {
        int NOTHING = 0;
        int GOT_START_TIME = 1;
        int GOT_STOP_TIME = 2;
    }*/

    /*
    private final Runnable mRepeatSomePartTask = new Runnable() {
        @Override
        public void run() {
            while (repeatStatus == REPEAT_STATUS.GOT_STOP_TIME){
                int currentProgress = playerService.getCurrentTrackProgress();
                if(currentProgress>stopTime){
                    playerService.seekTrack(startTime);
                    Log.d("FragmentDisc", "run: loop repeated");
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("FragmentDisc", "run: running repeat task");
            }
        }
    };*/


}
