package com.music.player.bhandari.m.rewards;

import android.util.Log;

import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;

import java.util.Random;

/**
 * Created by Amit AB AB on 10/31/2017.
 */

public class RewardPoints {

    private static int MAX_REWARD_POINTS = 5000;

    private static int MAX_DECREMENT_POINT = 7;
    private static int MIN_DECREMENT_POINT = 2;

    public static int getRewardPointsCount(){
        return MyApp.getPref().getInt(MyApp.getContext().getString(R.string.pref_reward_points),100);
    }

    public static void decrementByRandomInt(){

        int pointsToBeDecremented = new Random().nextInt((MAX_DECREMENT_POINT - MIN_DECREMENT_POINT) + 1) + MIN_DECREMENT_POINT;

        if(getRewardPointsCount()>pointsToBeDecremented) {
            MyApp.getPref().edit().putInt(MyApp.getContext().getString(R.string.pref_reward_points), getRewardPointsCount() - pointsToBeDecremented).apply();
        }else {
            MyApp.getPref().edit().putInt(MyApp.getContext().getString(R.string.pref_reward_points), 0).apply();
        }
        Log.d("RewardPoints", "decrementByRandomInt: decrement by :"+pointsToBeDecremented);
    }

    public static void decrementByOne(){
        if(getRewardPointsCount()>0) {
            MyApp.getPref().edit().putInt(MyApp.getContext().getString(R.string.pref_reward_points), getRewardPointsCount() - 1).apply();
        }
        Log.d("RewardPoints", "decrementByOne: 1");
    }

    public static void incrementRewardPointsCount(int increment){
        Log.d("RewardPoints", "incrementRewardPointsCount: "+increment);
        if(getRewardPointsCount() + increment < MAX_REWARD_POINTS) {
            MyApp.getPref().edit().putInt(MyApp.getContext().getString(R.string.pref_reward_points), getRewardPointsCount() + increment).apply();
        }else {
            MyApp.getPref().edit().putInt(MyApp.getContext().getString(R.string.pref_reward_points), MAX_REWARD_POINTS).apply();
        }
    }

}
