package com.music.player.bhandari.m.trackInfo.models

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface TrackInfoService {

    @GET("?method=track.getInfo&api_key=4e464c9ca4e6763aca6d5a7a04728c77&artist={artist}&track={track}&autocorrect=1&format=json")
    fun getTrackInfo(@Path("artist") artist: String, @Path("track") track : String): Call<Track>

}