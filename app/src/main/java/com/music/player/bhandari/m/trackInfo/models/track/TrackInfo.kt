package com.music.player.bhandari.m.trackInfo.models.track

import com.google.gson.annotations.SerializedName
import com.music.player.bhandari.m.trackInfo.models.Artist
import com.music.player.bhandari.m.trackInfo.models.Attr
import com.music.player.bhandari.m.trackInfo.models.Image
import com.music.player.bhandari.m.trackInfo.models.Streamable
import com.music.player.bhandari.m.trackInfo.models.Track

data class Track(
        val album: Album,
        val artist: Artist,
        val duration: String,
        val listeners: String,
        val mbid: String,
        val name: String,
        val playcount: String,
        val streamable: Streamable,
        val toptags: Toptags,
        val url: String,
        val wiki: Wiki
)

data class Artist(
    val mbid: String,
    val name: String,
    val url: String
)

data class Wiki(
    val content: String,
    val published: String,
    val summary: String
)

data class Toptags(
    val tag: List<Tag>
)

data class Tag(
    val name: String,
    val url: String
)

data class Album(
        @SerializedName("@attr") val attr : Attr,
        val artist: String,
        val image: List<Image>,
        val mbid: String,
        val title: String,
        val url: String
)

data class Attr(
    val position: String
)

data class Image(
    @SerializedName("#name") val text: String,
    val size: String
)

data class Streamable(
    val text: String,
    val fulltrack: String
)