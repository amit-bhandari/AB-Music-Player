package com.music.player.bhandari.m.trackInfo.models.similar

import com.google.gson.annotations.SerializedName

data class SimilartracksWrap(
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
    @SerializedName("#text") val text: String,
    val size: String
)

data class Streamable(
    @SerializedName("#text") val text: String,
    val fulltrack: String
)