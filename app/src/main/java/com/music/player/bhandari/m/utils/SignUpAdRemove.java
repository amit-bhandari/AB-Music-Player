package com.music.player.bhandari.m.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.rewards.RewardPoints;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

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

public class SignUpAdRemove {
    private final static int DAYS_UNTIL_AD_REMOVED = 7;//Min number of days
    private final static int SIGN_UP_REWARD_BONUS = 1000;

    public static void StoreEmailWithTimestamp(String email){
        MyApp.getPref().edit().putString("email-id-reg", email).apply();
        MyApp.getPref().edit().putLong("sign-up-time-stamp", System.currentTimeMillis()).apply();
        RewardPoints.incrementRewardPointsCount(SIGN_UP_REWARD_BONUS);
        //MyApp.getPref().edit().putBoolean(MyApp.getContext().getString(R.string.pref_remove_ads_temp), true).apply();
    }

    public static void CheckAndUpdateAdRemovalExpiration(Context context){
        long dateOfRegister = MyApp.getPref().getLong("sign-up-time-stamp", 0);
        if (System.currentTimeMillis() >= dateOfRegister +
                        (TimeUnit.DAYS.toMillis(DAYS_UNTIL_AD_REMOVED))) {
            MyApp.getPref().edit().putBoolean(MyApp.getContext().getString(R.string.pref_remove_ads_temp), false).apply();
        }
    }

}
