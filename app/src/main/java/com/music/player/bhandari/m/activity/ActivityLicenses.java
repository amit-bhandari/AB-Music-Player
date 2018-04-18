package com.music.player.bhandari.m.activity;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.MyApp;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Amit AB Bhandari on 1/27/2017.
 */

public class ActivityLicenses extends AppCompatActivity {

    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
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
        setContentView(R.layout.activity_licenses);


        findViewById(R.id.root_view_licenses).setBackgroundDrawable(ColorHelper.getBaseThemeDrawable());

        Toolbar toolbar = findViewById(R.id.toolbar_);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ColorHelper.getPrimaryColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.getDarkPrimaryColor());
        }
        setTitle(getString(R.string.licenses));


    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
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


    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
               // .addTestDevice("F40E78AED9B7FE233362079AC4C05B61")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }
}
