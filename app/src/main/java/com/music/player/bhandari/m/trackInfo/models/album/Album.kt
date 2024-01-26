package com.music.player.bhandari.m.trackInfo.models.album

import com.google.gson.annotations.SerializedName

data class AlbumWrap(
    val album: Album
)

data class Album(
    val artist: String,
    val image: List<Image>,
    val listeners: String,
    val mbid: String,
    val name: String,
    val playcount: String,
    val tags: Tags,
    val tracks: Tracks,
    val url: String,
    val wiki: Wiki
)

data class Wiki(
    val content: String,
    val published: String,
    val summary: String
)

data class Tags(
    val tag: List<Tag>
)

data class Tag(
    val name: String,
    val url: String
)

data class Image(
    @SerializedName("#text") val text: String,
    val size: String
)

data class Tracks(
    val track: List<Track>
)

data class Track(
    @SerializedName("@attr") val attr: Attr,
    val artist: Artist,
    val duration: String,
    val name: String,
    val streamable: Streamable,
    val url: String
)

data class Artist(
    val mbid: String,
    val name: String,
    val url: String
)

data class Attr(
    val rank: String
)

data class Streamable(
    @SerializedName("#text") val text: String,
    val fulltrack: String
)