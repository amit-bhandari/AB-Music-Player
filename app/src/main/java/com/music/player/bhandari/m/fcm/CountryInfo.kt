package com.music.player.bhandari.m.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Copyright 2017 Amit Bhandari AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class CountryInfo : Thread(Runnable {
    var country: String = MyApp.getPref().getString(MyApp.Companion.getContext()
        .getString(R.string.pref_user_country), "")!!
    if (country == "") {
        Log.d("CountryInfo", "run: fetching country info ")
        val client = OkHttpClient()
        client.setConnectTimeout(10, TimeUnit.SECONDS)
        client.setReadTimeout(30, TimeUnit.SECONDS)
        val request = Request.Builder()
            .url("http://ip-api.com/json")
            .build()
        try {
            val response = client.newCall(request).execute()
            Log.d("CountryInfo", "run: $response")
            val jsonData = response.body().string()
            Log.d("CountryInfo", "run: $jsonData")
            val jObject = JSONObject(jsonData)
            country = jObject.getString("country")
            Log.d("CountryInfo", "run: $country")
        } catch (e: Exception) {
            country =
                MyApp.Companion.getContext().resources.configuration.locale.country
            e.printStackTrace()
        }
        if (country == null || country == "") {
            country = "unknown"
        }
        try {
            country = country.replace(" ".toRegex(), "_")
            FirebaseMessaging.getInstance().subscribeToTopic(country)
            FirebaseMessaging.getInstance().subscribeToTopic("ab_music")

            /*FirebaseDatabase database = FirebaseDatabase.getInstance();
                     final DatabaseReference myRef = database.getReference("countries").child(country);

                     myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot dataSnapshot) {
                             try {
                                 if (dataSnapshot.getValue() == null) {
                                     myRef.setValue(1L);
                                 } else {
                                     myRef.setValue((Long) dataSnapshot.getValue() + 1L);
                                 }
                             } catch (Exception ignored) {
                             }
                         }

                         @Override
                         public void onCancelled(DatabaseError databaseError) {

                         }
                     });*/
        } catch (ignored: Exception) {
        }
        MyApp.getPref().edit().putString(MyApp.Companion.getContext().getString(R.string.pref_user_country), country).apply()
    }
})