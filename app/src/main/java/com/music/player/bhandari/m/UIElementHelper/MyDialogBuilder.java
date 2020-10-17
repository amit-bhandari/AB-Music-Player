package com.music.player.bhandari.m.UIElementHelper;

import android.content.Context;
import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;

/**
 * Custom material dialog builder for creating global animation and setting drawable gradient
 */

public class MyDialogBuilder extends MaterialDialog.Builder{

    public MyDialogBuilder(@NonNull Context context) {
        super(context);

        //set typeface globally
        typeface(TypeFaceHelper.getTypeFace(MyApp.getContext())
                ,TypeFaceHelper.getTypeFace(MyApp.getContext()));
    }

    @Override
    public MaterialDialog build() {
        return new MyDialog(this);
    }
}

class MyDialog extends MaterialDialog {

    MyDialog(Builder builder) {
        super(builder);
    }

    @Override
    public void show() {
        //if you add something here, remember to add that in FileSaveDialog and AfterSaveActionDialog of
        //ringtone cutter too
        //but even if you forget, disaster won't happen
        if(getWindow()!=null) {
            getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
            getWindow().setBackgroundDrawable(ColorHelper.getGradientDrawableDark());
        }

        super.show();
    }
}
