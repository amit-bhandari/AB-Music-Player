package com.music.player.bhandari.m.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.rewards.RewardPoints;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.util.Random;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Amit AB AB on 10/31/2017.
 */

public class ActivityRewardVideo extends AppCompatActivity {

    private RewardedVideoAd mRewardedVideoAd;
    private boolean showAdAfterLoading = false;
    private boolean rewardGranted = false;
    private MaterialDialog dialog;

    private int MIN_REWARD = 200;
    private int MAX_REWARD = 500;

    private int REWARD_AFTER_AD_IS_CLICKED = 1000;

    private int pointsToBeRewarded;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeRewardedAdVideo();

        int reward_points = MyApp.getPref().getInt(getString(R.string.pref_reward_points), 100);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);

        builder.title(R.string.title_reward_points)
                .typeface(TypeFaceHelper.getTypeFace(ActivityRewardVideo.this),TypeFaceHelper.getTypeFace(ActivityRewardVideo.this))
                .theme(Theme.DARK)
                .customView(R.layout.reward_dialog_view, true)
                .cancelable(false)
                .autoDismiss(false)
                .positiveText(R.string.pos_reward_points)
                .negativeText(R.string.neg_reward_points)
                .neutralText(R.string.remove_ads_permanently)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        startActivity(new Intent(ActivityRewardVideo.this, ActivityRemoveAds.class));
                        dialog.dismiss();
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        if(!UtilityFun.isConnectedToInternet()){
                            Toast.makeText(ActivityRewardVideo.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        dialog.dismiss();
                        showAd();
                    }
                });


        dialog = builder.build();

        View customView = dialog.getCustomView();
        if(customView!=null) {
            ((TextView) customView.findViewById(R.id.text_view_reward_points_count)).setText(String.format("%d", reward_points));
            ((TextView) customView.findViewById(R.id.reward_dialog_content)).setText(getString(R.string.reward_dialog_content));
        }

        dialog.show();
    }

    private void showAd() {



        if(mRewardedVideoAd.isLoaded()){
            //Toast.makeText(ActivityRewardVideo.this, "rewarded_video_shown", Toast.LENGTH_SHORT).show();
            Log.d("ActivityRewardVideo", "showAd: showing reward video");
            mRewardedVideoAd.show();
        } else {
            showAdAfterLoading = true;

            if(dialog!=null && dialog.isShowing()){
                dialog.dismiss();
            }

            MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(ActivityRewardVideo.this)
                    .typeface(TypeFaceHelper.getTypeFace(ActivityRewardVideo.this),TypeFaceHelper.getTypeFace(ActivityRewardVideo.this))
                    .title(R.string.title_loading_ad)
                    .content(R.string.content_loading_ad)
                    .autoDismiss(false)
                    .cancelable(false)
                    .negativeText(R.string.cancel)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            finish();
                        }
                    })
                    .theme(Theme.DARK)
                    .progress(true,0);

            dialog = dialogBuilder.build();
            dialog.show();
        }
    }

    private void initializeRewardedAdVideo() {
        //initialise rewarded ad video
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);

        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Log.d("RewardActivity", "onRewardedVideoAdLoaded: ");
                if(showAdAfterLoading){

                    try {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }catch (IllegalArgumentException ignored){

                    }

                    mRewardedVideoAd.show();
                }
            }

            @Override
            public void onRewardedVideoAdOpened() {
                Log.d("RewardActivity", "onRewardedVideoAdOpened: ");
            }

            @Override
            public void onRewardedVideoStarted() {
                Log.d("RewardActivity", "onRewardedVideoStarted: ");
            }

            @Override
            public void onRewardedVideoAdClosed() {
                Log.d("RewardActivity", "onRewardedVideoAdClosed: ");
                if(!rewardGranted){

                    if(dialog!=null && dialog.isShowing()){
                        dialog.dismiss();
                    }

                    try {
                        new MaterialDialog.Builder(ActivityRewardVideo.this)
                                .typeface(TypeFaceHelper.getTypeFace(ActivityRewardVideo.this), TypeFaceHelper.getTypeFace(ActivityRewardVideo.this))
                                .title(R.string.title_reward_failed)
                                .content(R.string.content_reward_failed)
                                .positiveText(R.string.pos_reward_failed)
                                .negativeText(R.string.neg_reward_failed)
                                .cancelable(false)
                                .autoDismiss(false)
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                })
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        if (!UtilityFun.isConnectedToInternet()) {
                                            Toast.makeText(ActivityRewardVideo.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        dialog.dismiss();
                                        loadAdd();
                                        showAd();
                                    }
                                }).show();

                        //throw new Exception();
                    }catch (Exception e){
                        Log.d("ActivityRewardVideo", "onRewardedVideoAdClosed: exception");
                        finish();
                    }
                }
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                Log.d("RewardActivity", "onRewarded: ");

                pointsToBeRewarded = new Random().nextInt((MAX_REWARD - MIN_REWARD) + 1) + MIN_REWARD;
                RewardPoints.incrementRewardPointsCount(pointsToBeRewarded);

                showAdAfterLoading = false;
                rewardGranted = true;

                if(dialog!=null && dialog.isShowing()){
                    dialog.dismiss();
                }

                showRewardedDialog();
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                Log.d("RewardActivity", "onRewardedVideoAdLeftApplication: ");

                //most probably clicked on ad
                //give more reward
                int toBeAdded = REWARD_AFTER_AD_IS_CLICKED - pointsToBeRewarded;
                pointsToBeRewarded = REWARD_AFTER_AD_IS_CLICKED;
                RewardPoints.incrementRewardPointsCount(toBeAdded);

                Log.d("ActivityRewardVideo", "onRewardedVideoAdLeftApplication: reward being increased because of ad click");

                if(dialog!=null && dialog.isShowing()){
                    dialog.dismiss();
                }

                showRewardedDialog();

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                Log.d("RewardActivity", "onRewardedVideoAdFailedToLoad: ");
            }

            private void showRewardedDialog() {
                loadAdd();
                MaterialDialog.Builder builder = new MaterialDialog.Builder(ActivityRewardVideo.this)
                        .typeface(TypeFaceHelper.getTypeFace(ActivityRewardVideo.this),TypeFaceHelper.getTypeFace(ActivityRewardVideo.this))
                        .title("Congrats!")
                        .content(String.format("You have been awarded %d reward points. To add more, click on Refill." , pointsToBeRewarded))
                        .positiveText("Refill")
                        .negativeText("Done")
                        .autoDismiss(false)
                        .cancelable(false)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if(!UtilityFun.isConnectedToInternet()){
                                    Toast.makeText(ActivityRewardVideo.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                showAd();
                            }
                        });

                dialog = builder.build();
                dialog.show();
            }
        });


        loadAdd();
    }

    private void loadAdd() {
        mRewardedVideoAd.loadAd(getString(R.string.reward_video_ad),
                new AdRequest.Builder().build());
        /*mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());*/
    }


    @Override
    protected void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        mRewardedVideoAd.resume(this);
        super.onResume();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
