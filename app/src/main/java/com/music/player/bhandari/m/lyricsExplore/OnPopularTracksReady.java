package com.music.player.bhandari.m.lyricsExplore;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by abami on 12/6/2017.
 */

public interface OnPopularTracksReady{
    public void popularTracksReady(List<Track> tracks, @NonNull String region) ;

    void error();
}
