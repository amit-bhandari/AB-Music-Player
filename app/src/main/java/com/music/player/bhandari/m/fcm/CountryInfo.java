package com.music.player.bhandari.m.fcm;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.utils.UtilityFun;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by abami on 12/5/2017.
 */

public class CountryInfo extends Thread {

    public CountryInfo(){
        super(new Runnable() {
            @Override
            public void run() {

                String country = MyApp.getPref().getString(MyApp.getContext().getString(R.string.pref_user_country),"");
                if(country.equals("")) {

                    Log.d("CountryInfo", "run: fetching country info ");

                    OkHttpClient client = new OkHttpClient();
                    client.setConnectTimeout(10, TimeUnit.SECONDS);
                    client.setReadTimeout(30, TimeUnit.SECONDS);
                    Request request = new Request.Builder()
                            .url("http://ip-api.com/json")
                            .build();

                    try {
                        Response response = client.newCall(request).execute();
                        Log.d("CountryInfo", "run: " + response.toString());

                        String jsonData = response.body().string();
                        Log.d("CountryInfo", "run: " + jsonData);

                        JSONObject jObject = new JSONObject(jsonData);
                        country = jObject.getString("country");
                        Log.d("CountryInfo", "run: " + country);

                    } catch (Exception e) {
                        country = MyApp.getContext().getResources().getConfiguration().locale.getCountry();
                        e.printStackTrace();
                    }

                    if(country==null || country.equals("")) {
                        country = "unknown";
                    }



                    try {
                        country = country.replaceAll(" ","_");

                        FirebaseMessaging.getInstance().subscribeToTopic(country);
                        FirebaseMessaging.getInstance().subscribeToTopic("ab_music");

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
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
                        });
                    }catch (Exception ignored){}

                    MyApp.getPref().edit().putString(MyApp.getContext().getString(R.string.pref_user_country), country).apply();

                }
            }
        });


    }


}
