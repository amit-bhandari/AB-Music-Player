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
    private static int typefaceId = R.font.manrope;

    public static Typeface getTypeFace(Context context) {
        if (typeface == null) {
            switch (MyApp.getPref().getInt(MyApp.getContext().getString(R.string.pref_text_font), Constants.TYPEFACE.MANROPE)){
                case Constants.TYPEFACE.SOFIA:
                    typeface = Typeface.createFromAsset(context.getAssets(), "fonts/sofia.ttf");
                    break;

                case Constants.TYPEFACE.MANROPE:
                    typeface = Typeface.createFromAsset(context.getAssets(), "fonts/manrope.ttf");
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
        switch (MyApp.getPref().getInt(MyApp.getContext().getString(R.string.pref_text_font), Constants.TYPEFACE.MANROPE)){
            case Constants.TYPEFACE.SOFIA:
                typefacePath = "fonts/sofia.ttf";
                break;

            case Constants.TYPEFACE.MANROPE:
                typefacePath = "fonts/manrope.ttf";
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

    public static int getTypeFacePathId() {
        switch (MyApp.getPref().getInt(MyApp.getContext().getString(R.string.pref_text_font), Constants.TYPEFACE.MANROPE)){
            case Constants.TYPEFACE.SOFIA:
                typefaceId = R.font.sofia;
                break;

            case Constants.TYPEFACE.MANROPE:
                typefaceId = R.font.manrope;
                break;

            case Constants.TYPEFACE.SYSTEM_DEFAULT:
                typefaceId = -1;
                break;

            case Constants.TYPEFACE.MONOSPACE:
                typefaceId = R.font.monospace;
                break;

            case Constants.TYPEFACE.ASAP:
                typefaceId = R.font.asap;
                break;

            case Constants.TYPEFACE.ACME:
                typefaceId = R.font.acme;
                break;

            case Constants.TYPEFACE.ACLONICA:
                typefaceId = R.font.aclonica;
                break;

            case Constants.TYPEFACE.CHEERYSWASH:
                typefaceId = R.font.cherry_swash;
                break;

            case Constants.TYPEFACE.CORBEN:
                typefaceId = R.font.corben;
                break;

            case Constants.TYPEFACE.NOVA_R:
                typefaceId = R.font.nova_round;
                break;

            case Constants.TYPEFACE.NOVA_S:
                typefaceId = R.font.nova_script;
                break;

            case Constants.TYPEFACE.PACFITO:
                typefaceId = R.font.pacifico;
                break;

            case Constants.TYPEFACE.PURPLEPURSE:
                typefaceId = R.font.purple_purse;
                break;

            case Constants.TYPEFACE.QUATICO:
                typefaceId = R.font.quantico;
                break;

            case Constants.TYPEFACE.ROBOTO:
                typefaceId = R.font.roboto;
                break;

            case Constants.TYPEFACE.ROBOTO_C:
                typefaceId = R.font.roboto_condensed_regular;
                break;

            case Constants.TYPEFACE.ROBOTO_M:
                typefaceId = R.font.roboto_mono;
                break;

            case Constants.TYPEFACE.TRADE_WINDS:
                typefaceId = R.font.trade_winds;

            case Constants.TYPEFACE.UBUNTU:
                typefaceId = R.font.ubuntu;
                break;

            case Constants.TYPEFACE.CONCERT_ONCE:
                typefaceId = R.font.concert_one;
                break;

            case Constants.TYPEFACE.LATO:
                typefaceId = R.font.lato;
                break;

            case Constants.TYPEFACE.LATO_ITALIC:
                typefaceId = R.font.lato_italic;
                break;

            case Constants.TYPEFACE.LORA:
                typefaceId = R.font.lora;
                break;

            case Constants.TYPEFACE.MONTESERRAT:
                typefaceId = R.font.montserrat;
                break;

            case Constants.TYPEFACE.OPEN_SANS_LIGHT:
                typefaceId = R.font.open_sans_light;
                break;

            case Constants.TYPEFACE.OSWALD:
                typefaceId = R.font.oswald;
                break;

            case Constants.TYPEFACE.PROMPT:
                typefaceId = R.font.prompt;
                break;

            case Constants.TYPEFACE.PROMPT_MEDIUM:
                typefaceId = R.font.prompt_medium;
                break;

            case Constants.TYPEFACE.PT_SANS_CAPTION:
                typefaceId = R.font.pt_sans_caption_bold;

            case Constants.TYPEFACE.RALEWAY:
                typefaceId = R.font.raleway_medium;
                break;

            case Constants.TYPEFACE.SLABO:
                typefaceId = R.font.slabo_13px;

            case Constants.TYPEFACE.SOURCE_SANS_PRO:
                typefaceId = R.font.source_sans_pro;
                break;
        }

        return typefaceId;
    }
}
