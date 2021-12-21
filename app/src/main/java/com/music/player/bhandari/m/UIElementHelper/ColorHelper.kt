package com.music.player.bhandari.m.UIElementHelper

import android.annotation.TargetApi
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Window
import android.view.WindowManager
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
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
object ColorHelper {
    fun getPrimaryColor(): Int {
        //return Color.parseColor("#E91E63")
        return MyApp.getPref()!!.getInt(MyApp.getContext()!!.resources
            .getString(R.string.pref_theme_color), Constants.PRIMARY_COLOR.BLACK)
    }

    private fun getDarkPrimaryColor(): Int {
        val color: Int = getPrimaryColor()
        //1int color = Color.parseColor("#E91E63");
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] *= 0.9f // value component
        return Color.HSVToColor(hsv)
        //  return Color.parseColor("#F44336");
    }

    fun getBaseThemeDrawable(): Drawable {
        var d: Drawable = ColorDrawable(MyApp.getContext()!!.resources
            .getColor(R.color.light_gray2))
        val pref: Int =
            MyApp.getPref()!!.getInt(MyApp.getContext()!!.resources
                .getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (pref) {
            Constants.PRIMARY_COLOR.LIGHT -> {}
            Constants.PRIMARY_COLOR.DARK -> d = ColorDrawable(
                MyApp.getContext()!!.resources.getColor(R.color.dark_gray2))
            Constants.PRIMARY_COLOR.GLOSSY -> d = GradientDrawable(
                GradientDrawable.Orientation.BR_TL, intArrayOf(getDarkPrimaryColor(), -0xececed))
        }
        return d
    }

    fun getColor(id: Int): Int {
        return MyApp.getContext()!!.resources.getColor(id)
    }

    fun getBaseThemeTextColor(): Int {
        var color: Int = MyApp.getContext()!!.resources.getColor(R.color.light_text)
        val pref: Int =
            MyApp.getPref()!!.getInt(MyApp.getContext()!!.resources
                .getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (pref) {
            Constants.PRIMARY_COLOR.LIGHT -> {}
            Constants.PRIMARY_COLOR.DARK -> color = MyApp.getContext()!!
                .resources.getColor(R.color.dark_text)
            Constants.PRIMARY_COLOR.GLOSSY -> color = MyApp.getContext()!!
                .resources.getColor(R.color.dark_text)
        }
        return color
        // return Color.parseColor("#212121");
    }

    /**
     * set gradient drawable to activity
     * @param activity
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun setStatusBarGradiant(activity: Activity) {
        val window: Window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = activity.resources.getColor(android.R.color.transparent)
        window.navigationBarColor = activity.resources
            .getColor(android.R.color.transparent)
        //window.
        activity.window.setBackgroundDrawable(getGradientDrawable())
    }

    //gradient theme getter
    fun getGradientDrawable(): Drawable {
        return getGradientDrawable(MyApp.Companion.getSelectedThemeId())
    }

    /**
     * overloaded version for getting gradient drawable for particular theme id
     * This is used in setting screens to show all gradients together for user to choose from
     * @param id
     * @return
     */
    fun getGradientDrawable(id: Int): Drawable {
        when (id) {
            1 -> return getGradient(getColor(R.color.theme1_color1),
                getColor(R.color.theme1_color2))
            2 -> return getGradient(getColor(R.color.theme2_color1),
                getColor(R.color.theme2_color2))
            3 -> return getGradient(getColor(R.color.theme3_color1),
                getColor(R.color.theme3_color2))
            4 -> return getGradient(getColor(R.color.theme4_color1),
                getColor(R.color.theme4_color2))
            5 -> return getGradient(getColor(R.color.theme5_color1),
                getColor(R.color.theme5_color2))
            6 -> return getGradient(getColor(R.color.theme6_color1),
                getColor(R.color.theme6_color2))
            7 -> return getGradient(getColor(R.color.theme7_color1),
                getColor(R.color.theme7_color2))
            8 -> return getGradient(getColor(R.color.theme8_color1),
                getColor(R.color.theme8_color2))
            9 -> return getGradient(getColor(R.color.theme9_color1),
                getColor(R.color.theme9_color2))
            10 -> return getGradient(getColor(R.color.theme10_color1),
                getColor(R.color.theme10_color2))
            11 -> return getGradient(getColor(R.color.theme11_color1),
                getColor(R.color.theme11_color2))
            12 -> return getGradient(getColor(R.color.theme12_color1),
                getColor(R.color.theme12_color2),
                getColor(R.color.theme12_color3))
            13 -> return getGradient(getColor(R.color.theme13_color1),
                getColor(R.color.theme13_color2),
                getColor(R.color.theme13_color3))
            14 -> return getGradient(getColor(R.color.theme14_color1),
                getColor(R.color.theme14_color2),
                getColor(R.color.theme14_color3))
            15 -> return getGradient(getColor(R.color.theme15_color1),
                getColor(R.color.theme15_color2),
                getColor(R.color.theme15_color3))
            16 -> return getGradient(getColor(R.color.theme16_color1),
                getColor(R.color.theme16_color2))
            17 -> return getGradient(getColor(R.color.theme17_color1),
                getColor(R.color.theme17_color2))
            18 -> return getGradient(getColor(R.color.theme18_color1),
                getColor(R.color.theme18_color2))
            19 -> return getGradient(getColor(R.color.theme19_color1),
                getColor(R.color.theme19_color2))
            20 -> return getGradient(getColor(R.color.theme20_color1),
                getColor(R.color.theme20_color2))
            21 -> return getGradient(getColor(R.color.theme21_color1),
                getColor(R.color.theme21_color2))
            else -> return getGradient(getColor(R.color.theme0_color1),
                getColor(R.color.theme0_color2))
        }
    }

    fun getGradientDrawableDark(): Drawable {
        when (MyApp.Companion.getSelectedThemeId()) {
            1 -> return getGradient(getDarkColor(getColor(R.color.theme1_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme1_color2_dark), 0.5f))
            2 -> return getGradient(getDarkColor(getColor(R.color.theme2_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme2_color2_dark), 0.5f))
            3 -> return getGradient(getDarkColor(getColor(R.color.theme3_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme3_color2_dark), 0.5f))
            4 -> return getGradient(getDarkColor(getColor(R.color.theme4_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme4_color2_dark), 0.5f))
            5 -> return getGradient(getDarkColor(getColor(R.color.theme5_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme5_color2_dark), 0.5f))
            6 -> return getGradient(getDarkColor(getColor(R.color.theme6_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme6_color2_dark), 0.5f))
            7 -> return getGradient(getDarkColor(getColor(R.color.theme7_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme7_color2_dark), 0.5f))
            8 -> return getGradient(getDarkColor(getColor(R.color.theme8_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme8_color2_dark), 0.5f))
            9 -> return getGradient(getDarkColor(getColor(R.color.theme9_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme9_color2_dark), 0.5f))
            10 -> return getGradient(getDarkColor(getColor(R.color.theme10_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme10_color2_dark), 0.5f))
            11 -> return getGradient(getDarkColor(getColor(R.color.theme11_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme11_color2_dark), 0.5f))
            12 -> return getGradient(getDarkColor(getColor(R.color.theme12_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme12_color2_dark), 0.5f),
                getDarkColor(getColor(R.color.theme12_color3_dark), 0.5f))
            13 -> return getGradient(getDarkColor(getColor(R.color.theme13_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme13_color2_dark), 0.5f),
                getDarkColor(getColor(R.color.theme13_color3_dark), 0.5f))
            14 -> return getGradient(getDarkColor(getColor(R.color.theme14_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme14_color2_dark), 0.5f),
                getDarkColor(getColor(R.color.theme14_color3_dark), 0.5f))
            15 -> return getGradient(getDarkColor(getColor(R.color.theme15_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme15_color2_dark), 0.5f),
                getDarkColor(getColor(R.color.theme15_color3_dark), 0.5f))
            16 -> return getGradient(getDarkColor(getColor(R.color.theme16_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme16_color2_dark), 0.5f))
            17 -> return getGradient(getDarkColor(getColor(R.color.theme17_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme17_color2_dark), 0.5f))
            18 -> return getGradient(getDarkColor(getColor(R.color.theme18_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme18_color2_dark), 0.5f))
            19 -> return getGradient(getDarkColor(getColor(R.color.theme19_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme19_color2_dark), 0.5f))
            20 -> return getGradient(getDarkColor(getColor(R.color.theme20_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme20_color2_dark), 0.5f))
            21 -> return getGradient(getDarkColor(getColor(R.color.theme21_color1_dark), 0.5f),
                getDarkColor(getColor(R.color.theme21_color2_dark),
                    0.5f) /*,getDarkColor(getColor(R.color.theme21_color3_dark), 0.5f)*/
            )
            else -> return getGradient(getColor(R.color.theme0_color1),
                getColor(R.color.theme0_color2))
        }
    }

    /**
     * get color for fabs and stuff
     * @return
     */
    fun getWidgetColor(): Int {
        when (MyApp.Companion.getSelectedThemeId()) {
            1 -> return getDarkColor(getColor(R.color.theme1_color2_widget), 1f)
            2 -> return getDarkColor(getColor(R.color.theme2_color2_widget), 0.7f)
            3 -> return getDarkColor(getColor(R.color.theme3_color2_widget), 1f)
            4 -> return getDarkColor(getColor(R.color.theme4_color2_widget), 1f)
            5 -> return getDarkColor(getColor(R.color.theme5_color2_widget), 0.8f)
            6 -> return getDarkColor(getColor(R.color.theme6_color2_widget), 0.9f)
            7 -> return getDarkColor(getColor(R.color.theme7_color2_widget), 1f)
            8 -> return getDarkColor(getColor(R.color.theme8_color2_widget), 0.8f)
            9 -> return getDarkColor(getColor(R.color.theme9_color2_widget), 0.8f)
            10 -> return getDarkColor(getColor(R.color.theme10_color2_widget), 0.5f)
            11 -> return getDarkColor(getColor(R.color.theme11_color2_widget), 0.5f)
            12 -> return getDarkColor(getColor(R.color.theme12_color2_widget), 0.5f)
            13 -> return getDarkColor(getColor(R.color.theme13_color2_widget), 0.5f)
            14 -> return getDarkColor(getColor(R.color.theme14_color2_widget), 0.5f)
            15 -> return getDarkColor(getColor(R.color.theme15_color2_widget), 0.5f)
            16 -> return getDarkColor(getColor(R.color.theme16_color2_widget), 0.5f)
            17 -> return getDarkColor(getColor(R.color.theme17_color2_widget), 0.5f)
            18 -> return getDarkColor(getColor(R.color.theme18_color2_widget), 0.5f)
            19 -> return getDarkColor(getColor(R.color.theme19_color2_widget), 0.5f)
            20 -> return getDarkColor(getColor(R.color.theme20_color2_widget), 0.5f)
            21 -> return getDarkColor(getColor(R.color.theme21_color2_widget), 0.5f)
            else -> return getDarkColor(Color.YELLOW, 0.6f)
        }
    }

    /**
     * hardcoded number of themes
     * @return
     */
    fun getNumberOfThemes(): Int {
        return 22
    }

    fun getPrimaryTextColor(): Int {
        return getColor(R.color.primary_text_color)
    }

    fun getSecondaryTextColor(): Int {
        return getColor(R.color.secondary_text_color)
    }

    //private
    private fun getGradient(vararg colors: Int): Drawable {
        if (colors.size < 2) throw IllegalStateException("Need at least 2 colors")
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            colors)
    }

    private fun getDarkColor(color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] *= factor // value component
        return Color.HSVToColor(hsv)
    }
}