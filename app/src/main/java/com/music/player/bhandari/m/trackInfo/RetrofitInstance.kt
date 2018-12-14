package com.music.player.bhandari.m.trackInfo.models

import android.os.Environment
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private var retrofit: Retrofit? = null

    fun getTrackInfoService():TrackInfoService{
        if(retrofit==null){
            val cacheSize: Long = 10 * 1024 * 1024 // 10 MB
            val cache = Cache(Environment.getDataDirectory(), cacheSize)

            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().addInterceptor(interceptor).cache(cache).build()

            retrofit = Retrofit.Builder()
                    .baseUrl("http://ws.audioscrobbler.com/2.0/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
        return retrofit!!.create<TrackInfoService>(TrackInfoService::class.java)
    }

}