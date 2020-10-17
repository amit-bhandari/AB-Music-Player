package com.music.player.bhandari.m.trackInfo.models

import androidx.annotation.Keep
import com.music.player.bhandari.m.trackInfo.models.album.AlbumWrap
import com.music.player.bhandari.m.trackInfo.models.similar.SimilartracksWrap
import com.music.player.bhandari.m.trackInfo.models.track.TrackWrap
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

@Keep
interface TrackInfoService {

    @GET("?method=track.getInfo&api_key=4e464c9ca4e6763aca6d5a7a04728c77&autocorrect=1&format=json")
    fun getTrackInfo(@Query("artist") artist: String, @Query("track") track : String): Call<TrackWrap>

    @GET("?method=track.getSimilar&api_key=4e464c9ca4e6763aca6d5a7a04728c77&autocorrect=1&format=json&limit=10")
    fun getSimilarTracks(@Query("mbid") artistMbid: String): Call<SimilartracksWrap>

    @GET("?method=album.getInfo&api_key=4e464c9ca4e6763aca6d5a7a04728c77&autocorrect=1&format=json")
    fun getAlbumInfo(@Query("mbid") albumMbid: String): Call<AlbumWrap>

}