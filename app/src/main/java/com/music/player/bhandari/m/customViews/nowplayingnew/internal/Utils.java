package com.music.player.bhandari.m.customViews.nowplayingnew.internal;

public class Utils {
    private Utils() {
        //no instance
    }
    public static float betweenZeroOne(float value) {
        return Math.min(1.0f, Math.max(0.0f, value));
    }
}
