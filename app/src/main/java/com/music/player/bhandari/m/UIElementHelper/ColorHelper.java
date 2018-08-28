package com.music.player.bhandari.m.UIElementHelper;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;


import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.MyApp;

/**
 Copyright 2017 Amit Bhandari AB

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

public class ColorHelper {

    public static int getAccentColor(){
        if(MyApp.getPref().getInt(MyApp.getContext().getResources()
                .getString(R.string.pref_theme_color), Constants.PRIMARY_COLOR.BLACK) == Constants.PRIMARY_COLOR.BLACK) {
            return getColor(R.color.colorYellow);
        }else {
            return getBrightPrimaryColor();
        }

      // return Color.parseColor("#607D8B");

    }

    public static int getPrimaryColor(){
        //return Color.parseColor("#E91E63")
        return MyApp.getPref().getInt(MyApp.getContext().getResources()
            .getString(R.string.pref_theme_color), Constants.PRIMARY_COLOR.BLACK);
    }

    public static int getDarkPrimaryColor(){
        int color = getPrimaryColor();
        //1int color = Color.parseColor("#E91E63");
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f; // value component
        return  Color.HSVToColor(hsv);
      //  return Color.parseColor("#F44336");
    }

    public static int getBrightPrimaryColor(){
        int color = getPrimaryColor();
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 1.4f; // value component
        return  Color.HSVToColor(hsv);
    }

    public static Drawable getColoredThemeGradientDrawable(){
        //int color = getLightThemebackgroundColor();
        return new GradientDrawable(
                GradientDrawable.Orientation.BR_TL,
                new int[] {getDarkPrimaryColor(),0xFF131313});
    }

    public static Drawable getBaseThemeDrawable(){
        Drawable d = new ColorDrawable(MyApp.getContext().getResources().getColor(R.color.light_gray2));
        int pref = MyApp.getPref().getInt(MyApp.getContext().getResources()
                .getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT);
        switch (pref){
            case Constants.PRIMARY_COLOR.LIGHT:
                break;
            case Constants.PRIMARY_COLOR.DARK:
                d = new ColorDrawable(MyApp.getContext().getResources().getColor(R.color.dark_gray2));
                break;

            case Constants.PRIMARY_COLOR.GLOSSY:
                d = new GradientDrawable(
                        GradientDrawable.Orientation.BR_TL,
                        new int[] {getDarkPrimaryColor(),0xFF131313});
                break;
        }
        return d;
    }

    public static int getColor(int id){
        return MyApp.getContext().getResources().getColor(id);
    }

    public static int getNowPlayingControlsColor(){
        return getColor(R.color.pw_accent);
    }

    public static int getBaseThemeTextColor(){
        int color = MyApp.getContext().getResources().getColor(R.color.light_text);
        int pref = MyApp.getPref().getInt(MyApp.getContext().getResources()
                .getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT);
        switch (pref){
            case Constants.PRIMARY_COLOR.LIGHT:
                break;

            case Constants.PRIMARY_COLOR.DARK:
                color=MyApp.getContext().getResources().getColor(R.color.dark_text);
                break;

            case Constants.PRIMARY_COLOR.GLOSSY:
                color=MyApp.getContext().getResources().getColor(R.color.dark_text);
                break;

        }
           return color;
       // return Color.parseColor("#212121");
    }

    public static int GetDominantColor(Bitmap bitmap){
        int redBucket = 0;
        int greenBucket = 0;
        int blueBucket = 0;
        int alphaBucket = 0;
        bitmap = Bitmap.createScaledBitmap(bitmap,100,100,false);
        boolean hasAlpha = bitmap.hasAlpha();
        int pixelCount = bitmap.getWidth() * bitmap.getHeight();
        int[] pixels = new int[pixelCount];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int y = 0, h = bitmap.getHeight(); y < h; y++)
        {
            for (int x = 0, w = bitmap.getWidth(); x < w; x++)
            {
                int color = pixels[x + y * w]; // x + y * width
                redBucket += (color >> 16) & 0xFF; // Color.red
                greenBucket += (color >> 8) & 0xFF; // Color.greed
                blueBucket += (color & 0xFF); // Color.blue
                if (hasAlpha) alphaBucket += (color >>> 24); // Color.alpha
            }
        }

        int color = Color.argb(
                (hasAlpha) ? (alphaBucket / pixelCount) : 255,
                redBucket/ pixelCount,
                greenBucket / pixelCount,
                blueBucket / pixelCount);
        if(isWhiteColor(color)){
            color = getDarkColor(color);
        }
        if(!isColorDark(color)){
            color = getDarkColor(color);
        }
        if(isColorDark(color)){
            color = getBrightColor(color);
        }


        return color;
    }

    //gradient theme getter
    public static Drawable GetGradientDrawable(){
        switch (MyApp.getSelectedThemeId()){
            case 1:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme1_color1),getColor(R.color.theme1_color2)});

            case 2:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme2_color1),getColor(R.color.theme2_color2)});

            case 3:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme3_color1),getColor(R.color.theme3_color2)});

            default:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme0_color1),getColor(R.color.theme0_color2)});
        }
    }

    public static Drawable GetGradientDrawable(int id){
        switch (id){
            case 1:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme1_color1),getColor(R.color.theme1_color2)});

            case 2:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme2_color1),getColor(R.color.theme2_color2)});

            case 3:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme3_color1),getColor(R.color.theme3_color2)});

            default:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme0_color1),getColor(R.color.theme0_color2)});
        }
    }

    public static Drawable GetGradientDrawableDark(){
        switch (MyApp.getSelectedThemeId()){
            case 1:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme1_color1_dark),getColor(R.color.theme1_color2_dark)});

            case 2:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme2_color1_dark),getColor(R.color.theme2_color2_dark)});

            case 3:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme3_color1_dark),getColor(R.color.theme3_color2_dark)});

            default:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme0_color1),getColor(R.color.theme0_color2)});
        }
    }

    public static Drawable GetGradientDrawableToolbar(){
        switch (MyApp.getSelectedThemeId()){
            case 1:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme1_color1),getColor(R.color.theme1_color2)});

            case 2:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme2_color1),getColor(R.color.theme2_color2)});

            case 3:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme3_color1),getColor(R.color.theme3_color2)});

            default:
                return new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {getColor(R.color.theme0_color1),getColor(R.color.theme0_color2)});
        }
    }

    public static int GetStatusBarColor(){
        switch (MyApp.getSelectedThemeId()){
            case 1:
                return getColor(R.color.theme1_color1);

            case 2:
                return getColor(R.color.theme2_color1);

            case 3:
                return getColor(R.color.theme3_color1);

            default:
                return getColor(R.color.theme0_color1);
        }
    }

    public static int GetWidgetColor(){
        switch (MyApp.getSelectedThemeId()){
            case 1:
                return getColor(R.color.theme1_color2_widget);

            case 2:
                return getColor(R.color.theme2_color2_widget);

            case 3:
                return getColor(R.color.theme3_color2_widget);

            default:
                return getColor(R.color.theme0_color2);
        }
    }

    public static int GetNumberOfThemes(){
        return 4;
    }


    //private

    private static boolean isWhiteColor(int color) {
        if (android.R.color.transparent == color)
            return true;
        boolean rtnValue = false;
        int[] rgb = { Color.red(color), Color.green(color), Color.blue(color) };
        int Y = (int)(0.2126*rgb[0] + 0.7152*rgb[1] + 0.0722*rgb[2]);
        if (Y >= 200) {
            rtnValue = true;
        }
        return rtnValue;
    }

    private static boolean isColorDark(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        if(darkness<0.5){
            return false; // It's a light color
        }else{
            return true; // It's a dark color
        }
    }

    private static int getDarkColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    private static int getBrightColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = 1.0f - 0.8f * (1.0f - hsv[2]);; // value component
        return Color.HSVToColor(hsv);
    }
}
