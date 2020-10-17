package com.music.player.bhandari.m.customViews;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * recycler view for setting fading only from bottom
 * used in lyrics fragment for fading content under playback controls
 */

public class MyRecyclerView extends RecyclerView {
    public MyRecyclerView(Context context) {
        super(context);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        return 0;
    }

    /*@Override
    protected float getBottomFadingEdgeStrength() {
        return 1000;
    }*/
}
