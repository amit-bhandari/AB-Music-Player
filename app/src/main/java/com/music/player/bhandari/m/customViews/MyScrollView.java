package com.music.player.bhandari.m.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * scroll view for setting fading only from bottom
 * used in artist info page for fading content under playback controls
 */

public class MyScrollView extends ScrollView {
    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        return 0;
    }
}
