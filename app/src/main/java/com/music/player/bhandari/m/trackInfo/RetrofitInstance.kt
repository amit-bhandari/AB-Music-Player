package com.music.player.bhandari.m.trackInfo.models

import com.google.gson.Gson
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private var retrofit: Retrofit? = null

    fun getTrackInfoService():TrackInfoService{
        if(retrofit==null){
            retrofit = Retrofit.Builder()
                    .baseUrl("http://ws.audioscrobbler.com/2.0/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
        return retrofit!!.create<TrackInfoService>(TrackInfoService::class.java)
    }

}