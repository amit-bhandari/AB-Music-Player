package com.music.player.bhandari.m.UIElementHelper;

import com.afollestad.materialdialogs.MaterialDialog;
import com.music.player.bhandari.m.R;

public class MyDialog extends MaterialDialog {

    public MyDialog(Builder builder) {
        super(builder);
    }

    @Override
    public void show() {
        if(getWindow()!=null) {
            getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
        }
        super.show();
    }
}
