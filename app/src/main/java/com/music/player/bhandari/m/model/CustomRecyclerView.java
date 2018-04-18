package com.music.player.bhandari.m.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.music.player.bhandari.m.adapter.MainLibraryAdapter;

/**
 * Created by Amit AB Bhandari on 2/4/2017.
 */

public class CustomRecyclerView extends RecyclerView {
    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public int GetHeight(){
        if (getAdapter() instanceof MainLibraryAdapter){
            return  0;
        }
        return  -1;
    }
}
