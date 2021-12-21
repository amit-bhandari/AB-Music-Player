package com.music.player.bhandari.m.UIElementHelper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.music.player.bhandari.m.R

class FastScroller : LinearLayout {
    private val scrollListener: ScrollListener = ScrollListener()
    private var bubble: TextView? = null
    private var handle: View? = null
    private var recyclerView: RecyclerView? = null
    private var heightHere: Int = 0
    private var currentAnimator: ObjectAnimator? = null

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        initialise(context)
    }

    constructor(context: Context) : super(context) {
        initialise(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialise(context)
    }

    private fun initialise(context: Context) {
        orientation = HORIZONTAL
        clipChildren = false
        val inflater: LayoutInflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.recyclerview_fastscroller, this, true)
        bubble = findViewById<View>(R.id.fastscroller_bubble) as TextView?
        handle = findViewById(R.id.fastscroller_handle)
        bubble!!.visibility = INVISIBLE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        heightHere = h
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.x < handle!!.x) return false
                if (currentAnimator != null) currentAnimator!!.cancel()
                if (bubble!!.visibility == INVISIBLE) showBubble()
                handle!!.isSelected = true
                val y: Float = event.y
                setBubbleAndHandlePosition(y)
                setRecyclerViewPosition(y)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val y: Float = event.y
                setBubbleAndHandlePosition(y)
                setRecyclerViewPosition(y)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handle!!.isSelected = false
                hideBubble()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView = recyclerView
        recyclerView!!.addOnScrollListener(scrollListener)
    }

    private fun setRecyclerViewPosition(y: Float) {
        try {
            if (recyclerView != null) {
                val itemCount: Int = recyclerView!!.adapter!!.itemCount
                val proportion: Float = when {
                    handle!!.y == 0f -> 0f
                    handle!!.y + handle!!.height >= heightHere - TRACK_SNAP_RANGE -> 1f
                    else -> y / heightHere.toFloat()
                }
                val targetPos: Int =
                    getValueInRange(0, itemCount - 1, (proportion * itemCount.toFloat()).toInt())
                (recyclerView!!.layoutManager as LinearLayoutManager?)!!.scrollToPositionWithOffset(
                    targetPos,
                    0)
                //      recyclerView.oPositionWithOffset(targetPos);
                val bubbleText: String =
                    (recyclerView!!.adapter as BubbleTextGetter?)!!.getTextToShowInBubble(
                        targetPos)
                bubble!!.text = bubbleText
            }
        } catch (e: Exception) {
            println("Exception At FastScroller 116")
        }
    }

    private fun getValueInRange(min: Int, max: Int, value: Int): Int {
        val minimum: Int = Math.max(min, value)
        return Math.min(minimum, max)
    }

    private fun setBubbleAndHandlePosition(y: Float) {
        val bubbleHeight: Int = bubble!!.height
        val handleHeight: Int = handle!!.height
        handle!!.y = getValueInRange(0,
            heightHere - handleHeight,
            (y - handleHeight / 2).toInt()).toFloat()
        bubble!!.y = getValueInRange(0,
            heightHere - bubbleHeight - (handleHeight / 2),
            (y - bubbleHeight).toInt()).toFloat()
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun showBubble() {
        val animatorSet = AnimatorSet()
        bubble!!.visibility = VISIBLE
        if (currentAnimator != null) currentAnimator!!.cancel()
        currentAnimator = ObjectAnimator.ofFloat(bubble, "alpha", 0f, 1f).setDuration(
            BUBBLE_ANIMATION_DURATION.toLong())
        currentAnimator!!.start()
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun hideBubble() {
        if (currentAnimator != null) currentAnimator!!.cancel()
        currentAnimator = ObjectAnimator.ofFloat(bubble, "alpha", 1f, 0f).setDuration(
            BUBBLE_ANIMATION_DURATION.toLong())
        currentAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                bubble!!.visibility = INVISIBLE
                currentAnimator = null
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                bubble!!.visibility = INVISIBLE
                currentAnimator = null
            }
        })
        currentAnimator!!.start()
    }

    private inner class ScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            if (handle!!.isSelected) {
                return
            }
            val firstVisibleView: View = recyclerView!!.getChildAt(0)
            val firstVisiblePosition: Int = recyclerView!!.getChildLayoutPosition(firstVisibleView)
            val visibleRange: Int = recyclerView!!.childCount
            val lastVisiblePosition: Int = firstVisiblePosition + visibleRange
            val itemCount: Int = recyclerView!!.adapter!!.itemCount
            val position: Int
            if (firstVisiblePosition == 0) position =
                0 else if (lastVisiblePosition == itemCount) position = itemCount else position =
                ((firstVisiblePosition.toFloat() / ((itemCount.toFloat() - visibleRange.toFloat()))) * itemCount.toFloat()).toInt()
            val proportion: Float = position.toFloat() / itemCount.toFloat()
            setBubbleAndHandlePosition(heightHere * proportion)
        }
    }

    companion object {
        private val BUBBLE_ANIMATION_DURATION: Int = 100
        private val TRACK_SNAP_RANGE: Int = 5
    }
}