package com.music.player.bhandari.m.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.service.NotificationListenerService;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
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

public class ActivityRequestNotificationAccess extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.text_never_ask)
    TextView never_ask;

    @BindView(R.id.text_skip)
    TextView skip;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        ColorHelper.setStatusBarGradiant(this);

        int themeSelector = MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT);

        switch (themeSelector){
            case Constants.PRIMARY_COLOR.DARK:
                setTheme(R.style.AppThemeDark);
                break;

            case Constants.PRIMARY_COLOR.GLOSSY:
                setTheme(R.style.AppThemeDark);
                break;

            case Constants.PRIMARY_COLOR.LIGHT:
                setTheme(R.style.AppThemeLight);
                break;
        }

        setContentView(R.layout.activity_request_notification_access);
        ButterKnife.bind(this);

        //findViewById(R.id.root_view_request_notification_access).setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());

        findViewById(R.id.request_button).setOnClickListener(this);
        skip.setOnClickListener(this);
        never_ask.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(NotificationListenerService.isListeningAuthorized(this)){
            launchMainActivity();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.request_button:
                Intent intent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
                Toast.makeText(this, "Click on AB Music to enable!", Toast.LENGTH_LONG).show();
                break;

            case R.id.text_skip:
                launchMainActivity();
                skip.setVisibility(View.GONE);
                never_ask.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                break;

            case R.id.text_never_ask:
                never_ask.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                MyApp.getPref().edit().putBoolean(getString(R.string.pref_never_ask_notitication_permission), true).apply();
                launchMainActivity();
                break;
        }
    }

    private void launchMainActivity() {
        Intent mainActIntent=new Intent(this, ActivityMain.class);
        startActivity(mainActIntent);
        finish();
    }
}
