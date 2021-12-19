package com.music.player.bhandari.m.customViews

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

/**
 * recycler view for setting fading only from bottom
 * used in lyrics fragment for fading content under playback controls
 */
class MyRecyclerView : RecyclerView {
    constructor(context: Context?) : super((context)!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super((context)!!, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        (context)!!, attrs, defStyle) {
    }

    override fun getTopFadingEdgeStrength(): Float {
        return 0F
    } /*@Override
    protected float getBottomFadingEdgeStrength() {
        return 1000;
    }*/
}