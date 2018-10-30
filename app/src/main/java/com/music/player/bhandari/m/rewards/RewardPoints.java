package com.music.player.bhandari.m.rewards;

import android.util.Log;

import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;

import java.util.Random;

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

public class RewardPoints {

    private static int MAX_REWARD_POINTS = 5000;

    private static int MAX_DECREMENT_POINT = 5;
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
