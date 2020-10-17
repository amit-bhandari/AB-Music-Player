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

public class MyImageView extends androidx.appcompat.widget.AppCompatImageView {

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
