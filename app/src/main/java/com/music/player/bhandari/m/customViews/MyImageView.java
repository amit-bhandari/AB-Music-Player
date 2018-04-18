package com.music.player.bhandari.m.customViews;

/**
 * Created by Amit AB AB on 17-Apr-18.
 */


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.music.player.bhandari.m.R;

/**
 *
 * Copyright (c) 2018 Billte Ltd. United Kingdom
 * All Rights Reserved
 *
 * This product is protected by copyright and distributed under
 * licenses restricting copying, distribution and decompilation.
 *
 * Created by <Amit AB AB> on 16/03/2018 (Replace with date).
 *
 * @version
 * @file
 * @copyright 2018 Billte Switzerland.
 *
 */

public class MyImageView extends android.support.v7.widget.AppCompatImageView {

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        Glide.with(this.getContext()).load(getUrl()).into(this);
    }

    private String url;

    MyImageView(Context context) {
        super(context);
    }

    MyImageView(Context context, AttributeSet attr) {
        super(context, attr);
        TypedArray array = context.obtainStyledAttributes(attr, R.styleable.MyImageView);
        url = array.getString(R.styleable.MyImageView_url);
        if (url != null) {
            Glide.with(context).load(url).into(this);
        }
        array.recycle();
    }
}
