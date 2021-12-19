/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.music.player.bhandari.m.transition

import android.animation.Animator
import androidx.annotation.ColorInt
import android.graphics.drawable.Drawable
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.ColorFilter
import androidx.annotation.RequiresApi
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Context
import android.transition.ChangeBounds
import android.transition.TransitionValues
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.music.player.bhandari.m.R

/**
 * 从Dialog到Fab的变化
 *
 * A transition that morphs a rectangle into a circle, changing it's background color.
 */
class MorphNowPlayingToMini : ChangeBounds {
    constructor() : super()
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun getTransitionProperties(): Array<String> {
        return TRANSITION_PROPERTIES
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        super.captureStartValues(transitionValues)
        val view = transitionValues.view
        if (view.width <= 0 || view.height <= 0) {
            return
        }
        transitionValues.values[PROPERTY_COLOR] =
            ContextCompat.getColor(view.context, R.color.dialog_background_color)
        transitionValues.values[PROPERTY_CORNER_RADIUS] =
            view.resources
                .getDimensionPixelSize(R.dimen.dialog_corners)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        super.captureEndValues(transitionValues)
        val view = transitionValues.view
        if (view.width <= 0 || view.height <= 0) {
            return
        }
        transitionValues.values[PROPERTY_COLOR] =
            ContextCompat.getColor(view.context, R.color.fab_background_color)
        transitionValues.values[PROPERTY_CORNER_RADIUS] =
            view.height / 2 //view.getHeight() / 2
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues,
        endValues: TransitionValues
    ): Animator {
        val changeBounds = super.createAnimator(sceneRoot, startValues, endValues)
        if (startValues == null || endValues == null || changeBounds == null) {
            return null
        }
        val startColor = startValues.values[PROPERTY_COLOR] as Int?
        val startCornerRadius = startValues.values[PROPERTY_CORNER_RADIUS] as Int?
        val endColor = endValues.values[PROPERTY_COLOR] as Int?
        val endCornerRadius = endValues.values[PROPERTY_CORNER_RADIUS] as Int?
        if (startColor == null || startCornerRadius == null || endColor == null || endCornerRadius == null) {
            return null
        }
        val background = MorphDrawable(startColor, startCornerRadius.toFloat())
        endValues.view.background = background
        val color: Animator = ObjectAnimator.ofArgb<Any>(background, background.COLOR, endColor)
        val corners: Animator = ObjectAnimator.ofFloat<Any>(background,
            background.CORNER_RADIUS,
            endCornerRadius.toFloat())

        // hide child views (offset down & fade out)
        /*if (endValues.view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) endValues.view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View v = vg.getChildAt(i);
                v.animate().alpha(0f).translationY(v.getHeight() / 3).setStartDelay(0L).setDuration(50L)
                        .setInterpolator(AnimationUtils.loadInterpolator(vg.getContext(), android.R.interpolator.fast_out_linear_in))
                        .start();
            }
        }*/
        val transition = AnimatorSet()
        transition.playTogether(changeBounds, corners, color)
        transition.interpolator =
            AnimationUtils.loadInterpolator(sceneRoot.context,
                android.R.interpolator.fast_out_slow_in)
        transition.duration = 3000
        return transition
    }

    companion object {
        private const val PROPERTY_COLOR = "rectMorph:color"
        private const val PROPERTY_CORNER_RADIUS = "rectMorph:cornerRadius"
        private val TRANSITION_PROPERTIES = arrayOf(
            PROPERTY_COLOR,
            PROPERTY_CORNER_RADIUS
        )
    }
}