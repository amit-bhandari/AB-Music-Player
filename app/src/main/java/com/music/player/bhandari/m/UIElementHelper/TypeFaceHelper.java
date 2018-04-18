package com.music.player.bhandari.m.UIElementHelper;

import android.content.Context;
import android.graphics.Typeface;

import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.MyApp;

/**
 * Created by Amit AB Bhandari on 3/18/2017.
 */

public class TypeFaceHelper {

    private static Typeface typeface = null;
    private static String typefacePath = "";

    public static Typeface getTypeFace(Context context) {
        if (typeface == null) {
            switch (MyApp.getPref().getInt(MyApp.getContext().getString(R.string.pref_text_font), Constants.TYPEFACE.MONOSPACE)){
                case Constants.TYPEFACE.SOFIA:
                    typeface = Typeface.createFromAsset(context.getAssets(), "fonts/sofia.ttf");
                    break;

                case Constants.TYPEFACE.RISQUE:
                    typeface = Typeface.createFromAsset(context.getAssets(), "fonts/risque.ttf");
                    break;

                case Constants.TYPEFACE.SYSTEM_DEFAULT:
                    typeface = null;
                    break;

                case Constants.TYPEFACE.MONOSPACE:
                    typeface = Typeface.createFromAsset(context.getAssets(), "fonts/monospace.ttf");
                    break;

                case Constants.TYPEFACE.VAST_SHADOW:
                    typeface = Typeface.createFromAsset(context.getAssets(), "fonts/vastshadow.ttf");
                    break;

                case Constants.TYPEFACE.ACME:
                    typeface = Typeface.createFromAsset(context.getAssets(), "fonts/acme.ttf");
                    break;

            }

        }
        return typeface;
    }

    public static String getTypeFacePath() {
        switch (MyApp.getPref().getInt(MyApp.getContext().getString(R.string.pref_text_font), Constants.TYPEFACE.MONOSPACE)){
            case Constants.TYPEFACE.SOFIA:
                typefacePath = "fonts/sofia.ttf";
                break;

            case Constants.TYPEFACE.RISQUE:
                typefacePath = "fonts/risque.ttf";
                break;

            case Constants.TYPEFACE.SYSTEM_DEFAULT:
                typefacePath = "null";
                break;

            case Constants.TYPEFACE.MONOSPACE:
                typefacePath = "fonts/monospace.ttf";
                break;

            case Constants.TYPEFACE.VAST_SHADOW:
                typefacePath = "fonts/vastshadow.ttf";
                break;

            case Constants.TYPEFACE.ACME:
                typefacePath = "fonts/acme.ttf";
                break;
        }

        return typefacePath;
    }

}
