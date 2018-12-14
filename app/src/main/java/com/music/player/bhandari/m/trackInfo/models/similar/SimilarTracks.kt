package com.music.player.bhandari.m.trackInfo.models

import com.google.gson.annotations.SerializedName
import com.music.player.bhandari.m.trackInfo.models.track.Artist
import com.music.player.bhandari.m.trackInfo.models.track.Attr
import com.music.player.bhandari.m.trackInfo.models.track.Image
import com.music.player.bhandari.m.trackInfo.models.track.Streamable
import com.music.player.bhandari.m.trackInfo.models.track.Track

data class SimilarTracks(
    val similartracks: Similartracks
)

data class Similartracks(
        @SerializedName("@attr") val attr : Attr,
        val track: List<Track>
)

data class Attr(
    val artist: String
)

data class Track(
        val artist: Artist,
        val duration: Int,
        val image: List<Image>,
        val match: Double,
        val mbid: String,
        val name: String,
        val playcount: Int,
        val streamable: Streamable,
        val url: String
)

data class Artist(
    val mbid: String,
    val name: String,
    val url: String
)

data class Image(
    @SerializedName("#name") val text: String,
    val size: String
)

data class Streamable(
    @SerializedName("#name") val text: String,
    val fulltrack: String
)