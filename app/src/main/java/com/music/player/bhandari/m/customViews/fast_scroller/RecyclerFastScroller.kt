package com.music.player.bhandari.m.customViews.fast_scroller

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.adapter.MainLibraryAdapter
import com.music.player.bhandari.m.model.Constants

/**
 * Copyright 2017 Amit Bhandari AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class RecyclerFastScroller @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    protected val mBar: View
    protected val mHandle: View?
    val mHiddenTranslationX: Int
    private val mHide: Runnable
    private val mMinScrollHandleHeight: Int
    protected var mOnTouchListener: OnTouchListener? = null
    var mAppBarLayoutOffset: Int = 0
    var mRecyclerView: RecyclerView? = null
    var mCoordinatorLayout: CoordinatorLayout? = null
    var mAppBarLayout: AppBarLayout? = null
    var mAnimator: AnimatorSet? = null
    var mAnimatingIn: Boolean = false
    private var mHideDelay: Int
    private var mHidingEnabled: Boolean
    private var mHandleNormalColor: Int
    private var mHandlePressedColor: Int
    private var mBarColor: Int
    private var mTouchTargetWidth: Int
    private var mBarInset: Int = 0
    private var mHideOverride: Boolean = false
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private val mAdapterObserver: RecyclerView.AdapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            requestLayout()
        }
    }

    @ColorInt
    fun getHandlePressedColor(): Int {
        return mHandlePressedColor
    }

    fun setHandlePressedColor(@ColorInt colorPressed: Int) {
        mHandlePressedColor = colorPressed
        updateHandleColorsAndInset()
    }

    @ColorInt
    fun getHandleNormalColor(): Int {
        return mHandleNormalColor
    }

    fun setHandleNormalColor(@ColorInt colorNormal: Int) {
        mHandleNormalColor = colorNormal
        updateHandleColorsAndInset()
    }

    @ColorInt
    fun getBarColor(): Int {
        return mBarColor
    }

    /**
     * @param scrollBarColor Scroll bar color. Alpha will be set to ~22% to match stock scrollbar.
     */
    fun setBarColor(@ColorInt scrollBarColor: Int) {
        mBarColor = scrollBarColor
        updateBarColorAndInset()
    }

    fun getHideDelay(): Int {
        return mHideDelay
    }

    /**
     * @param hideDelay the delay in millis to hide the scrollbar
     */
    fun setHideDelay(hideDelay: Int) {
        mHideDelay = hideDelay
    }

    fun getTouchTargetWidth(): Int {
        return mTouchTargetWidth
    }

    /**
     * @param touchTargetWidth In pixels, less than or equal to 48dp
     */
    fun setTouchTargetWidth(touchTargetWidth: Int) {
        mTouchTargetWidth = touchTargetWidth
        val eightDp: Int = RecyclerFastScrollerUtils.convertDpToPx(context, 8F)
        mBarInset = mTouchTargetWidth - eightDp
        val fortyEightDp: Int = RecyclerFastScrollerUtils.convertDpToPx(context, 48F)
        if (mTouchTargetWidth > fortyEightDp) {
            throw RuntimeException("Touch target width cannot be larger than 48dp!")
        }
        mBar.layoutParams = LayoutParams(touchTargetWidth,
            ViewGroup.LayoutParams.MATCH_PARENT,
            GravityCompat.END)
        mHandle!!.layoutParams = LayoutParams(touchTargetWidth,
            ViewGroup.LayoutParams.MATCH_PARENT,
            GravityCompat.END)
        updateHandleColorsAndInset()
        updateBarColorAndInset()
    }

    fun isHidingEnabled(): Boolean {
        return mHidingEnabled
    }

    /**
     * @param hidingEnabled whether hiding is enabled
     */
    fun setHidingEnabled(hidingEnabled: Boolean) {
        mHidingEnabled = hidingEnabled
        if (hidingEnabled) {
            postAutoHide()
        }
    }

    private fun updateHandleColorsAndInset() {
        val drawable: StateListDrawable = StateListDrawable()
        if (!RecyclerFastScrollerUtils.isRTL(context)) {
            drawable.addState(View.PRESSED_ENABLED_STATE_SET,
                InsetDrawable(ColorDrawable(mHandlePressedColor), mBarInset, 0, 0, 0))
            drawable.addState(View.EMPTY_STATE_SET,
                InsetDrawable(ColorDrawable(mHandleNormalColor), mBarInset, 0, 0, 0))
        } else {
            drawable.addState(View.PRESSED_ENABLED_STATE_SET,
                InsetDrawable(ColorDrawable(mHandlePressedColor), 0, 0, mBarInset, 0))
            drawable.addState(View.EMPTY_STATE_SET,
                InsetDrawable(ColorDrawable(mHandleNormalColor), 0, 0, mBarInset, 0))
        }
        RecyclerFastScrollerUtils.setViewBackground(mHandle, drawable)
    }

    private fun updateBarColorAndInset() {
        val drawable: Drawable = when {
            !RecyclerFastScrollerUtils.isRTL(context) -> {
                InsetDrawable(ColorDrawable(mBarColor), mBarInset, 0, 0, 0)
            }
            else -> {
                InsetDrawable(ColorDrawable(mBarColor), 0, 0, mBarInset, 0)
            }
        }
        drawable.alpha = 57
        RecyclerFastScrollerUtils.setViewBackground(mBar, drawable)
    }

    fun attachRecyclerView(recyclerView: RecyclerView) {
        mRecyclerView = recyclerView
        mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                show(true)
            }
        })
        if (recyclerView.adapter != null) attachAdapter(recyclerView.adapter)
    }

    fun attachAdapter(adapter: RecyclerView.Adapter<*>?) {
        if (mAdapter === adapter) return
        if (mAdapter != null) {
            mAdapter!!.unregisterAdapterDataObserver(mAdapterObserver)
        }
        adapter?.registerAdapterDataObserver(mAdapterObserver)
        mAdapter = adapter
    }

    fun attachAppBarLayout(coordinatorLayout: CoordinatorLayout?, appBarLayout: AppBarLayout?) {
        mCoordinatorLayout = coordinatorLayout
        mAppBarLayout = appBarLayout
        mAppBarLayout!!.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            show(true)
            val layoutParams: MarginLayoutParams = layoutParams as MarginLayoutParams
            layoutParams.topMargin =
                mAppBarLayout!!.height + verticalOffset //AppBarLayout actual height
            mAppBarLayoutOffset = -verticalOffset
            setLayoutParams(layoutParams)
        })
    }

    fun setOnHandleTouchListener(listener: OnTouchListener?) {
        mOnTouchListener = listener
    }

    /**
     * Show the fast scroller and hide after delay
     *
     * @param animate whether to animate showing the scroller
     */
    fun show(animate: Boolean) {
        requestLayout()
        post(object : Runnable {
            override fun run() {
                if (mHideOverride) {
                    return
                }
                mHandle!!.isEnabled = true
                if (animate) {
                    if (!mAnimatingIn && translationX != 0f) {
                        if (mAnimator != null && mAnimator!!.isStarted) {
                            mAnimator!!.cancel()
                        }
                        mAnimator = AnimatorSet()
                        val animator: ObjectAnimator =
                            ObjectAnimator.ofFloat<View>(this@RecyclerFastScroller,
                                View.TRANSLATION_X,
                                0f)
                        animator.interpolator = LinearOutSlowInInterpolator()
                        animator.duration = 100
                        animator.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                mAnimatingIn = false
                            }
                        })
                        mAnimatingIn = true
                        mAnimator!!.play(animator)
                        mAnimator!!.start()
                    }
                } else {
                    translationX = 0f
                }
                postAutoHide()
            }
        })
    }

    fun postAutoHide() {
        if (mRecyclerView != null && mHidingEnabled) {
            mRecyclerView!!.removeCallbacks(mHide)
            mRecyclerView!!.postDelayed(mHide, mHideDelay.toLong())
        }
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        if (mRecyclerView == null) return
        val scrollOffset: Int = mRecyclerView!!.computeVerticalScrollOffset() + mAppBarLayoutOffset
        val verticalScrollRange: Int =
            (mRecyclerView!!.computeVerticalScrollRange() + (if (mAppBarLayout == null) 0
            else{
                mAppBarLayout!!.totalScrollRange
            })
                    + mRecyclerView!!.paddingBottom)
        val barHeight: Int = mBar.height
        val ratio: Float = scrollOffset.toFloat() / (verticalScrollRange - barHeight)
        var calculatedHandleHeight: Int =
            (barHeight.toFloat() / verticalScrollRange * barHeight) as Int
        if (calculatedHandleHeight < mMinScrollHandleHeight) {
            calculatedHandleHeight = mMinScrollHandleHeight
        }
        if (calculatedHandleHeight >= barHeight) {
            translationX = mHiddenTranslationX.toFloat()
            mHideOverride = true
            return
        }
        mHideOverride = false
        val y: Float = ratio * (barHeight - calculatedHandleHeight)
        mHandle!!.layout(mHandle.left,
            y.toInt(),
            mHandle.right,
            y.toInt() + calculatedHandleHeight)
    }

    fun updateRvScroll(dY: Int) {
        var dY: Int = dY
        Log.v(Constants.TAG, "scroll by" + dY)
        if (mAdapter is MainLibraryAdapter) {
            try {
                val itemHeight: Int = (mAdapter as MainLibraryAdapter?)!!.getHeight()
                if (dY - (dY % itemHeight) != 0) dY -= (dY % itemHeight)
            } catch (ignored: Exception) {
            }
        }
        //dY = dY - ( dY % 112 ) ;
        if (mRecyclerView != null && mHandle != null) {
            try {
                mRecyclerView!!.scrollBy(0, dY)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    companion object {
        private val DEFAULT_AUTO_HIDE_DELAY: Int = 1500
    }

    init {
        val a: TypedArray = context.obtainStyledAttributes(attrs,
            R.styleable.RecyclerFastScroller,
            defStyleAttr,
            defStyleRes)
        mBarColor = a.getColor(
            R.styleable.RecyclerFastScroller_rfs_barColor,
            RecyclerFastScrollerUtils.resolveColor(context, R.attr.colorControlNormal))
        mHandleNormalColor = a.getColor(
            R.styleable.RecyclerFastScroller_rfs_handleNormalColor,
            RecyclerFastScrollerUtils.resolveColor(context, R.attr.colorControlNormal))
        mHandlePressedColor = a.getColor(
            R.styleable.RecyclerFastScroller_rfs_handlePressedColor,
            RecyclerFastScrollerUtils.resolveColor(context, R.attr.colorAccent))
        mTouchTargetWidth = a.getDimensionPixelSize(
            R.styleable.RecyclerFastScroller_rfs_touchTargetWidth,
            RecyclerFastScrollerUtils.convertDpToPx(context, 24F))
        mHideDelay = a.getInt(R.styleable.RecyclerFastScroller_rfs_hideDelay,
            DEFAULT_AUTO_HIDE_DELAY)
        mHidingEnabled = a.getBoolean(R.styleable.RecyclerFastScroller_rfs_hidingEnabled, true)
        a.recycle()
        val fortyEightDp: Int = RecyclerFastScrollerUtils.convertDpToPx(context, 48F)
        layoutParams = ViewGroup.LayoutParams(fortyEightDp, ViewGroup.LayoutParams.MATCH_PARENT)
        mBar = View(context)
        mHandle = View(context)
        addView(mBar)
        addView(mHandle)
        setTouchTargetWidth(mTouchTargetWidth)
        mMinScrollHandleHeight = fortyEightDp
        val eightDp: Int = RecyclerFastScrollerUtils.convertDpToPx(getContext(), 8F)
        mHiddenTranslationX =
            (if (RecyclerFastScrollerUtils.isRTL(getContext())) -1 else 1) * eightDp
        mHide = Runnable {
            if (!mHandle.isPressed()) {
                if (mAnimator != null && mAnimator!!.isStarted) {
                    mAnimator!!.cancel()
                }
                mAnimator = AnimatorSet()
                val animator2: ObjectAnimator =
                    ObjectAnimator.ofFloat(this@RecyclerFastScroller, View.TRANSLATION_X,
                        mHiddenTranslationX.toFloat())
                animator2.interpolator = FastOutLinearInInterpolator()
                animator2.duration = 150
                mHandle.isEnabled = false
                mAnimator!!.play(animator2)
                mAnimator!!.start()
            }
        }
        mHandle.setOnTouchListener(object : OnTouchListener {
            private var mInitialBarHeight: Float = 0f
            private var mLastPressedYAdjustedToInitial: Float = 0f
            private var mLastAppBarLayoutOffset: Int = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (mOnTouchListener != null) {
                    mOnTouchListener!!.onTouch(v, event)
                }
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        mHandle.isPressed = true
                        mRecyclerView!!.stopScroll()
                        var nestedScrollAxis: Int = ViewCompat.SCROLL_AXIS_NONE
                        nestedScrollAxis = nestedScrollAxis or ViewCompat.SCROLL_AXIS_VERTICAL
                        mRecyclerView!!.startNestedScroll(nestedScrollAxis)
                        mInitialBarHeight = mBar.height.toFloat()
                        mLastPressedYAdjustedToInitial = event.y + mHandle.y + mBar.y
                        mLastAppBarLayoutOffset = mAppBarLayoutOffset
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val newHandlePressedY: Float = event.y + mHandle.y + mBar.y
                        val barHeight: Int = mBar.height
                        val newHandlePressedYAdjustedToInitial: Float =
                            newHandlePressedY + (mInitialBarHeight - barHeight)
                        val deltaPressedYFromLastAdjustedToInitial: Float =
                            newHandlePressedYAdjustedToInitial - mLastPressedYAdjustedToInitial
                        val dY: Int = ((deltaPressedYFromLastAdjustedToInitial / mInitialBarHeight) *
                                (mRecyclerView!!.computeVerticalScrollRange() + (when (mAppBarLayout) {
                                    null -> 0
                                    else -> mAppBarLayout!!.totalScrollRange
                                }))) as Int
                        if (mCoordinatorLayout != null && mAppBarLayout != null) {
                            val params: CoordinatorLayout.LayoutParams =
                                mAppBarLayout!!.layoutParams as CoordinatorLayout.LayoutParams
                            (params.behavior as AppBarLayout.Behavior?)?.onNestedPreScroll(
                                mCoordinatorLayout!!,
                                mAppBarLayout!!,
                                this@RecyclerFastScroller,
                                0,
                                dY,
                                IntArray(2))
                        }
                        updateRvScroll(dY + mLastAppBarLayoutOffset - mAppBarLayoutOffset)
                        mLastPressedYAdjustedToInitial = newHandlePressedYAdjustedToInitial
                        mLastAppBarLayoutOffset = mAppBarLayoutOffset
                    }
                    MotionEvent.ACTION_UP -> {
                        mLastPressedYAdjustedToInitial = -1f
                        mRecyclerView!!.stopNestedScroll()
                        mHandle.isPressed = false
                        postAutoHide()
                    }
                }
                return true
            }
        })
        translationX = mHiddenTranslationX.toFloat()
    }
}