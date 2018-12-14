package com.music.player.bhandari.m.trackInfo.models

import android.os.Handler
import android.os.Looper
import android.util.Log


/*
 Thread to fetch track info
 */
class FetchTrackInfo: Thread {

    lateinit var handler: Handler
    lateinit var artist: String
    lateinit var track: String
    lateinit var trackInfoService: TrackInfoService

    constructor(artist: String, track: String) : super(){
        this.artist = artist
        this.track = track
        this.handler = Handler(Looper.getMainLooper())
    }

    override fun run() {
        trackInfoService = RetrofitInstance.getTrackInfoService()

        val track = trackInfoService.getTrackInfo(artist, track)
        Log.d("FetchTrackInfo", "Track Info : $track")
    }
}