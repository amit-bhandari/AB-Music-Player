package com.music.player.bhandari.m.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.KeyEvent;
import android.view.MenuItem;

import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.model.Constants;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

/**
 * Copyright 2017 Amit Bhandari AB
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">...</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class ActivityLicenses extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        ColorHelper.setStatusBarGradiant(this);

        int themeSelector = MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT);
        switch (themeSelector) {
            case Constants.PRIMARY_COLOR.DARK, Constants.PRIMARY_COLOR.GLOSSY ->
                    setTheme(R.style.AppThemeDark);
            case Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);


        //findViewById(R.id.root_view_licenses).setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());

        Toolbar toolbar = findViewById(R.id.toolbar_);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setTitle(getString(R.string.licenses));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        MyApp.isAppVisible = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        MyApp.isAppVisible = false;
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY ->
                    MyApp.getService().play();
            case KeyEvent.KEYCODE_MEDIA_NEXT -> MyApp.getService().nextTrack();
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS -> MyApp.getService().prevTrack();
            case KeyEvent.KEYCODE_MEDIA_STOP -> MyApp.getService().stop();
            case KeyEvent.KEYCODE_BACK -> onBackPressed();
        }

        return false;
    }
}
