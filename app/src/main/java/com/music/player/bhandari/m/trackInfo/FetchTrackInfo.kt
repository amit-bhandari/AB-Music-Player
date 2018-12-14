package com.music.player.bhandari.m.trackInfo.models

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.lang.Exception

/*
 Thread to fetch track info
 */
class FetchTrackInfo(var artist: String, var track: String, val callback: TrackInfo.Callback) : Thread() {

    var handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var trackInfoService: TrackInfoService
    private var trackInfo = TrackInfo()

    override fun run() {
        try{
            //perform all 3 network calls (if track info comes null, send out negative response otherwise positive no matter if we got similar tracks or not)
            trackInfoService = RetrofitInstance.getTrackInfoService()
            val track = trackInfoService.getTrackInfo(artist, track).execute()
            if(track.body()?.track==null) {
                trackInfo.result = RESULT.NEGATIVE
                callback.onTrackInfoReady(trackInfo)
                return
            }
            trackInfo.track = track.body()?.track!!

            Thread.sleep(500)  ///to do not hit api in rapid pace
            val similarTracks = trackInfoService.getSimilarTracks(track.body()?.track?.mbid ?: "").execute()
            if(similarTracks.body()?.similartracks!=null) {
                trackInfo.similarTracks = similarTracks.body()?.similartracks!!
            }

            Thread.sleep(500)
            val album = trackInfoService.getAlbumInfo(track.body()?.track?.album?.mbid ?: "").execute()
            if(album.body()?.album!=null) {
                trackInfo.album = album.body()?.album!!
            }

            //extract actual data that will be useful for UI
            handler.post {
                //guaranteed to be called on UI thread
                callback.onTrackInfoReady(trackInfo)
            }

            Log.d("FetchTrackInfo", "Track Info : ${track.body()}")
            Log.d("FetchTrackInfo", "Similar Tracks : ${similarTracks.body()}")
            Log.d("FetchTrackInfo", "Track Album : ${album.body()}")
        }catch (e: Exception) {
            handler.post {
                //guaranteed to be called on UI thread
                trackInfo.result = RESULT.NEGATIVE
                callback.onTrackInfoReady(trackInfo)
            }
        }
    }
}