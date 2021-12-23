package com.music.player.bhandari.m.UIElementHelper

import android.content.Context
import android.graphics.Typeface
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
object TypeFaceHelper {
    private var typeface: Typeface? = null
    private var typefacePath: String = ""

    //private static int typefaceId = R.font.manrope;
    fun getTypeFace(context: Context): Typeface? {
        if (typeface == null) {
            when (MyApp.getPref().getInt(MyApp.getContext().getString(R.string.pref_text_font),
                    Constants.TYPEFACE.MANROPE)) {
                Constants.TYPEFACE.SOFIA -> typeface =
                    Typeface.createFromAsset(context.assets, "fonts/sofia.ttf")
                Constants.TYPEFACE.MANROPE -> typeface =
                    Typeface.createFromAsset(context.assets, "fonts/manrope.ttf")
                Constants.TYPEFACE.SYSTEM_DEFAULT -> typeface = null
                Constants.TYPEFACE.MONOSPACE -> typeface =
                    Typeface.createFromAsset(context.assets, "fonts/monospace.ttf")
                Constants.TYPEFACE.ASAP -> typeface =
                    Typeface.createFromAsset(context.assets, "fonts/asap.ttf")
            }
        }
        return typeface
    }

    fun getTypeFacePath(): String {
        when (MyApp.getPref().getInt(MyApp.getContext().getString(R.string.pref_text_font),
                Constants.TYPEFACE.MANROPE)) {
            Constants.TYPEFACE.SOFIA -> typefacePath = "fonts/sofia.ttf"
            Constants.TYPEFACE.MANROPE -> typefacePath = "fonts/manrope.ttf"
            Constants.TYPEFACE.MONOSPACE -> typefacePath = "fonts/monospace.ttf"
            Constants.TYPEFACE.ASAP -> typefacePath = "fonts/asap.ttf"
            Constants.TYPEFACE.ROBOTO -> typefacePath = "fonts/robot-regular.ttf"
            Constants.TYPEFACE.SYSTEM_DEFAULT -> typefacePath = "null"
        }
        return typefacePath
    } /*public static int getTypeFacePathId() {
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

            */
    /*case Constants.TYPEFACE.ACME:
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
                break;*/
    /*
        }

        return typefaceId;
    }*/
}