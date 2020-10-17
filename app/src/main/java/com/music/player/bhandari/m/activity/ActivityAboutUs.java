package com.music.player.bhandari.m.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.utils.UtilityFun;

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

public class ActivityAboutUs extends AppCompatActivity {

    private final static String FB_URL = "http://www.facebook.com/abmusicoffline/";
    private final String SITE_URL = "http://www.thetechguru.in/ab_music";
    final static String WEBSITE = "http://www.thetechguru.in";
    final static String TRANSLATION_HELP_WEBSITE = "https://github.com/Amit AB-bhandari/AB-Music-Translations";

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        //if player service not running, kill the app
        if(MyApp.getService()==null){
            UtilityFun.restartApp();
        }

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        FloatingActionButton fab = findViewById(R.id.fb_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_url(FB_URL);
            }
        });

        TextView site_link = findViewById(R.id.website_link);
        SpannableString spanWebsite = new SpannableString(site_link.getText());
        ClickableSpan clickableSpanWebsite = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                open_url(SITE_URL);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setTypeface(Typeface.create(ds.getTypeface(), Typeface.BOLD));
            }
        };
        spanWebsite.setSpan(clickableSpanWebsite, 0, site_link.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
        site_link.setText(spanWebsite);
        site_link.setMovementMethod(LinkMovementMethod.getInstance());

        //findViewById(R.id.root_view_about_us).setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());

        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            ((TextView)findViewById(R.id.version)).setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Toolbar toolbar = findViewById(R.id.toolbar_);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.GetStatusBarColor());
        }*/
        setTitle(getString(R.string.title_about_us));

        try {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "about_us_launched");
            UtilityFun.logEvent(bundle);
        }catch (Exception ignored){
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void open_url(String url){
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }catch (Exception ignored){
            Toast.makeText(ActivityAboutUs.this,getString(R.string.error_opening_browser),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                MyApp.getService().play();
                break;

            case KeyEvent.KEYCODE_MEDIA_NEXT:
                MyApp.getService().nextTrack();
                break;

            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                MyApp.getService().prevTrack();
                break;

            case KeyEvent.KEYCODE_MEDIA_STOP:
                MyApp.getService().stop();
                break;

            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                break;
        }

        return false;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;

            case R.id.action_feedback:
                String myDeviceModel = android.os.Build.MODEL;
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto",getString(R.string.au_email_id), null));
                String[] address = new String[]{getString(R.string.au_email_id)};
                emailIntent.putExtra(Intent.EXTRA_EMAIL, address);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for "+myDeviceModel);
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello AndroidDevs, \n");
                startActivity(Intent.createChooser(emailIntent, "Send Feedback"));
                break;

            case R.id.action_support_dev:
                selectDonateDialog();
                break;

            case R.id.action_licenses:
                /*new LibsBuilder()
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withActivityTitle("Libraries")
                        .start(this);*/
                startActivity(new Intent(this, ActivityLicenses.class));
                break;

            case R.id.action_tou:
                showDisclaimerDialog();
                break;

            case R.id.nav_website:
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE));
                    startActivity(browserIntent);
                }catch (Exception e){
                    Toast.makeText(ActivityAboutUs.this,getString(R.string.error_opening_browser),Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.nav_call_for_help:
                callForHelpDialog();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void callForHelpDialog(){
        MaterialDialog dialog = new MyDialogBuilder(this)
                .title("AB Music needs your help!")
                .content("As AB Music grows bigger and reach more audience, its language support also needs to be widened. \n\n As Catelyn of House Stark once said, " +
                        "In the name of King Robert and good lords you serve, I call upon you to seize the opportunity to contribute and help me translate " +
                        "AB Music to your language!")
                .positiveText("Sure")
                .negativeText("Nah, I don't want to")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        try {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TRANSLATION_HELP_WEBSITE));
                            startActivity(browserIntent);
                        }catch (Exception e){
                            Toast.makeText(ActivityAboutUs.this,  getString(R.string.error_opening_browser), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .build();

        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void selectDonateDialog() {

        MaterialDialog dialog = new MyDialogBuilder(this)
                .title(getString(R.string.about_us_support_dev_title))
                .content(getString(R.string.about_us_support_dev_content))
                .positiveText(getString(R.string.about_us_support_dev_pos))
                .negativeText(getString(R.string.about_us_support_dev_neg))
                .neutralText(getString(R.string.about_us_support_dev_neu))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(ActivityAboutUs.this,ActivityDonateFunds.class);
                        intent.putExtra("donate_type",Constants.DONATE.COFFEE);
                        startActivity(intent);
                    }
                }).onNeutral(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                Intent intent = new Intent(ActivityAboutUs.this,ActivityDonateFunds.class);
                intent.putExtra("donate_type",Constants.DONATE.JD);
                startActivity(intent);
            }
        })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(ActivityAboutUs.this,ActivityDonateFunds.class);
                        intent.putExtra("donate_type",Constants.DONATE.BEER);
                        startActivity(intent);
                    }
                })
                .build();

        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

        dialog.show();
    }

    private void showDisclaimerDialog(){
        MaterialDialog dialog = new MyDialogBuilder(this)
                .title(getString(R.string.lyrics_disclaimer_title))
                .content(getString(R.string.lyrics_disclaimer_content))
                .positiveText(getString(R.string.lyrics_disclaimer_title_pos))
                .negativeText(getString(R.string.lyrics_disclaimer_title_neg))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_disclaimer_accepted),true).apply();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                MyApp.getPref().edit().putBoolean(getString(R.string.pref_disclaimer_accepted),false).apply();
            }
        })
                .build();

        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

        dialog.show();
    }
}
