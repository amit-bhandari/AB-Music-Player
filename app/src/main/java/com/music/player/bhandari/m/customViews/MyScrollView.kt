package com.music.player.bhandari.m.customViews

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

/**
 * scroll view for setting fading only from bottom
 * used in artist info page for fading content under playback controls
 */
class MyScrollView : ScrollView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun getTopFadingEdgeStrength(): Float {
        return 0F
    }
}