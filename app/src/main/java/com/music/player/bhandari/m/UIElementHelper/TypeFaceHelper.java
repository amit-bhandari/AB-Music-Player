package com.music.player.bhandari.m.UIElementHelper;

import android.content.Context;
import android.graphics.Typeface;

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

public class TypeFaceHelper {

    private static Typeface typeface = null;
    private static String typefacePath = "";

    public static Typeface getTypeFace(Context context) {
        if (typeface == null) {
            switch (MyApp.getPref().getInt(MyApp.getContext().getString(R.string.pref_text_font), Constants.TYPEFACE.ASAP)){
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

                case Constants.TYPEFACE.ASAP:
                    typeface = Typeface.createFromAsset(context.getAssets(), "fonts/asap.ttf");
                    break;

                case Constants.TYPEFACE.ACME:
                    typeface = Typeface.createFromAsset(context.getAssets(), "fonts/acme.ttf");
                    break;

            }

        }
        return typeface;
    }

    public static String getTypeFacePath() {
        switch (MyApp.getPref().getInt(MyApp.getContext().getString(R.string.pref_text_font), Constants.TYPEFACE.ASAP)){
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

            case Constants.TYPEFACE.ASAP:
                typefacePath = "fonts/asap.ttf";
                break;

            case Constants.TYPEFACE.ACME:
                typefacePath = "fonts/acme.ttf";
                break;
        }

        return typefacePath;
    }

}
