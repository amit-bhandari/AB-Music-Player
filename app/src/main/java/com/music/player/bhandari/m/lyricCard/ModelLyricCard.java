package com.music.player.bhandari.m.lyricCard;

import android.databinding.Bindable;
import android.databinding.Observable;

/**
 * Created by abami on 17-Apr-18.
 */

public class ModelLyricCard implements Observable {



    @Bindable
    int[] colors = new int[]{132,123,123};






    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback onPropertyChangedCallback) {

    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback onPropertyChangedCallback) {

    }
}
