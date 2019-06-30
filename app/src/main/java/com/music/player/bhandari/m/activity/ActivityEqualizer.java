package com.music.player.bhandari.m.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.audiofx.PresetReverb;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.customViews.VerticalSeekBar;
import com.music.player.bhandari.m.equalizer.EqualizerSetting;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.utils.AppLaunchCountManager;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.util.ArrayList;

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

public class ActivityEqualizer extends AppCompatActivity {

    //views
    @BindView(R.id.equalizerScrollView)
    ScrollView mScrollView;

    @BindView(R.id.equalizerLinearLayout)
    View equalizerView;

    // 50Hz equalizer controls.
    @BindView(R.id.equalizer50Hz)
     VerticalSeekBar equalizer50HzSeekBar;
    @BindView(R.id.text50HzGain)
     TextView text50HzGainTextView;
    @BindView(R.id.text50Hz)
     TextView text50Hz;

    // 130Hz equalizer controls.
    @BindView(R.id.equalizer130Hz)
     VerticalSeekBar equalizer130HzSeekBar;
    @BindView(R.id.text130HzGain)
     TextView text130HzGainTextView;
    @BindView(R.id.text130Hz)
     TextView text130Hz;

    // 320Hz equalizer controls.
    @BindView(R.id.equalizer320Hz)
     VerticalSeekBar equalizer320HzSeekBar;
    @BindView(R.id.text320HzGain)
     TextView text320HzGainTextView;
    @BindView(R.id.text320Hz)
     TextView text320Hz;

    // 800 Hz equalizer controls.
    @BindView(R.id.equalizer800Hz)
     VerticalSeekBar equalizer800HzSeekBar;
    @BindView(R.id.text800HzGain)
     TextView text800HzGainTextView;
    @BindView(R.id.text800Hz)
     TextView text800Hz;

    // 2 kHz equalizer controls.
    @BindView(R.id.equalizer2kHz)
     VerticalSeekBar equalizer2kHzSeekBar;
    @BindView(R.id.text2kHzGain)
     TextView text2kHzGainTextView;
    @BindView(R.id.text2kHz)
     TextView text2kHz;

    // 5 kHz equalizer controls.
    @BindView(R.id.equalizer5kHz)
     VerticalSeekBar equalizer5kHzSeekBar;
    @BindView(R.id.text5kHzGain)
     TextView text5kHzGainTextView;
    @BindView(R.id.text5kHz)
     TextView text5kHz;

    // 12.5 kHz equalizer controls.
    @BindView(R.id.equalizer12_5kHz)
     VerticalSeekBar equalizer12_5kHzSeekBar;
    @BindView(R.id.text12_5kHzGain)
     TextView text12_5kHzGainTextView;
    @BindView(R.id.text12_5kHz)
     TextView text12_5kHz;

    // Equalizer preset controls.
    @BindView(R.id.loadPresetButton)
     RelativeLayout loadPresetButton;
    @BindView(R.id.saveAsPresetButton)
     RelativeLayout saveAsPresetButton;
    @BindView(R.id.resetAllButton)
     RelativeLayout resetAllButton;
    @BindView(R.id.load_preset_text)
     TextView loadPresetText;
    @BindView(R.id.save_as_preset_text)
     TextView savePresetText;
    @BindView(R.id.reset_all_text)
     TextView resetAllText;

    // Temp variables that hold the equalizer's settings.
    private int fiftyHertzLevel = 16;
    private int oneThirtyHertzLevel = 16;
    private int threeTwentyHertzLevel = 16;
    private int eightHundredHertzLevel = 16;
    private int twoKilohertzLevel = 16;
    private int fiveKilohertzLevel = 16;
    private int twelvePointFiveKilohertzLevel = 16;

    // Temp variables that hold audio fx settings.
    private int virtualizerLevel;
    private int bassBoostLevel;
    private int enhancementLevel;
    private int reverbSetting;

    //Audio FX elements.
    @BindView(R.id.virtualizer_seekbar)
     SeekBar virtualizerSeekBar;
    @BindView(R.id.bass_boost_seekbar)
     SeekBar bassBoostSeekBar;
    @BindView(R.id.enhancer_seekbar)
    SeekBar enhanceSeekBar;
    @BindView(R.id.reverb_spinner)
     Spinner reverbSpinner;
    @BindView(R.id.virtualizer_title_text)
     TextView virtualizerTitle;
    @BindView(R.id.bass_boost_title_text)
     TextView bassBoostTitle;
    @BindView(R.id.reverb_title_text)
     TextView reverbTitle;

    @BindView(R.id.adView)
     AdView mAdView;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setContentView(R.layout.activity_equalizer);
        ButterKnife.bind(this);

        showAdIfApplicable();

        //action bar
        Toolbar toolbar = findViewById(R.id.toolbar_);
        toolbar.setTitle(R.string.equalizer_title);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //mScrollView.setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.GetStatusBarColor());
        }*/

        //Init reverb presets.
        ArrayList<String> reverbPresets = new ArrayList<String>();
        reverbPresets.add(getString(R.string.preset_none));
        reverbPresets.add(getString(R.string.preset_large_hall));
        reverbPresets.add(getString(R.string.preset_large_room));
        reverbPresets.add(getString(R.string.preset_medium_hall));
        reverbPresets.add(getString(R.string.preset_medium_room));
        reverbPresets.add(getString(R.string.preset_small_room));
        reverbPresets.add(getString(R.string.preset_plate));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, reverbPresets);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reverbSpinner.setAdapter(dataAdapter);

        //Set the max values for the seekbars.
        virtualizerSeekBar.setMax(1000);
        bassBoostSeekBar.setMax(1000);
        enhanceSeekBar.setMax(1000);

        resetAllButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Reset all sliders to 0.
                equalizer50HzSeekBar.setProgressAndThumb(16);
                equalizer130HzSeekBar.setProgressAndThumb(16);
                equalizer320HzSeekBar.setProgressAndThumb(16);
                equalizer800HzSeekBar.setProgressAndThumb(16);
                equalizer2kHzSeekBar.setProgressAndThumb(16);
                equalizer5kHzSeekBar.setProgressAndThumb(16);
                equalizer12_5kHzSeekBar.setProgressAndThumb(16);
                virtualizerSeekBar.setProgress(0);
                bassBoostSeekBar.setProgress(0);
                enhanceSeekBar.setProgress(0);
                reverbSpinner.setSelection(0, false);

                //Apply the new setings to the service.
                applyCurrentEQSettings();

                //Show a confirmation toast.
                Toast.makeText(getApplicationContext(), R.string.equ_reset_toast, Toast.LENGTH_SHORT).show();

            }

        });

        loadPresetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadPresetDialog();
            }
        });

        saveAsPresetButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showSavePresetDialog();
            }
        });

        equalizer50HzSeekBar.setOnSeekBarChangeListener(equalizer50HzListener);
        equalizer130HzSeekBar.setOnSeekBarChangeListener(equalizer130HzListener);
        equalizer320HzSeekBar.setOnSeekBarChangeListener(equalizer320HzListener);
        equalizer800HzSeekBar.setOnSeekBarChangeListener(equalizer800HzListener);
        equalizer2kHzSeekBar.setOnSeekBarChangeListener(equalizer2kHzListener);
        equalizer5kHzSeekBar.setOnSeekBarChangeListener(equalizer5kHzListener);
        equalizer12_5kHzSeekBar.setOnSeekBarChangeListener(equalizer12_5kHzListener);

        virtualizerSeekBar.setOnSeekBarChangeListener(virtualizerListener);
        bassBoostSeekBar.setOnSeekBarChangeListener(bassBoostListener);
        reverbSpinner.setOnItemSelectedListener(reverbListener);
        enhanceSeekBar.setOnSeekBarChangeListener(enhanceListener);

        new AsyncInitSlidersTask().execute(MyApp.getService().getEqualizerHelper().getLastEquSetting());

        try {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "equalizer_launched");
            UtilityFun.logEvent(bundle);
        }catch (Exception ignored){
        }

        equalizer50HzSeekBar.setOnTouchListener(listener);
        equalizer130HzSeekBar.setOnTouchListener(listener);
        equalizer320HzSeekBar.setOnTouchListener(listener);
        equalizer800HzSeekBar.setOnTouchListener(listener);
        equalizer2kHzSeekBar.setOnTouchListener(listener);
        equalizer5kHzSeekBar.setOnTouchListener(listener);
        equalizer12_5kHzSeekBar.setOnTouchListener(listener);

    }

    View.OnTouchListener listener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d("ActivityEqualizer", "onTouch: " + event.toString());
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                mScrollView.requestDisallowInterceptTouchEvent(true);
            }else if(event.getAction() ==MotionEvent.ACTION_UP) {
                mScrollView.requestDisallowInterceptTouchEvent(false);                }
            return false;
        }
    };

    private void showAdIfApplicable() {
        if( /*AppLaunchCountManager.isEligibleForInterstialAd() &&*/ !UtilityFun.isAdsRemoved() ) {
            MobileAds.initialize(this, getString(R.string.banner_about_us_activity));

            if (UtilityFun.isConnectedToInternet()) {
                AdRequest adRequest = new AdRequest.Builder()//.addTestDevice("F40E78AED9B7FE233362079AC4C05B61")
                        .build();
                mAdView.loadAd(adRequest);
                mAdView.setVisibility(View.VISIBLE);
            } else {
                mAdView.setVisibility(View.GONE);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdView.resume();
        MyApp.isAppVisible = true;
    }

    @Override
    protected void onPause() {
        try{
            EqualizerSetting equalizerSetting = getCurrentEquSetting();
            MyApp.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting);
            Log.d("ActivityEqualizer", "onPause: stored equ setting : " + equalizerSetting.toString());
            mAdView.pause();
            MyApp.isAppVisible = false;
        }catch (Exception ignore){

        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mAdView!=null) {
            mAdView.destroy();
        }
    }

    @NonNull
    private EqualizerSetting getCurrentEquSetting() {
        EqualizerSetting equalizerSetting = new EqualizerSetting();
        equalizerSetting.setFiftyHertz(fiftyHertzLevel);
        equalizerSetting.setOneThirtyHertz(oneThirtyHertzLevel);
        equalizerSetting.setThreeTwentyHertz(threeTwentyHertzLevel);
        equalizerSetting.setEightHundredHertz(eightHundredHertzLevel);
        equalizerSetting.setTwoKilohertz(twoKilohertzLevel);
        equalizerSetting.setFiveKilohertz(fiveKilohertzLevel);
        equalizerSetting.setTwelvePointFiveKilohertz(twelvePointFiveKilohertzLevel);
        equalizerSetting.setVirtualizer(virtualizerLevel);
        equalizerSetting.setBassBoost(bassBoostLevel);
        equalizerSetting.setEnhancement(enhancementLevel);
        equalizerSetting.setReverb(reverbSetting);
        return equalizerSetting;
    }

    /**
     * 50 Hz equalizer seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer50HzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {
            Log.d("ActivityEqualizer", "onProgressChanged : ");
            try {
                //Get the appropriate equalizer band.
                short sixtyHertzBand = MyApp.getService().getEqualizerHelper().getEqualizer().getBand(50000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel==16) {
                    text50HzGainTextView.setText("0 dB");
                    MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(sixtyHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel==0) {
                        text50HzGainTextView.setText("-" + "15 dB");
                        MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(sixtyHertzBand, (short) (-1500));
                    } else {
                        text50HzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
                        MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(sixtyHertzBand, (short) -((16-seekBarLevel)*100));
                    }

                } else if (seekBarLevel > 16) {
                    text50HzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
                    MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(sixtyHertzBand, (short) ((seekBarLevel-16)*100));
                }

                fiftyHertzLevel = seekBarLevel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
            Log.d("ActivityEqualizer", "onStartTrackingTouch : ");
        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            Log.d("ActivityEqualizer", "onStopTrackingTouch : ");
        }

    };

    /**
     * 130 Hz equalizer seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer130HzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {

            try {
                //Get the appropriate equalizer band.
                short twoThirtyHertzBand = MyApp.getService().getEqualizerHelper().getEqualizer().getBand(130000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel==16) {
                    text130HzGainTextView.setText("0 dB");
                    MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twoThirtyHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel==0) {
                        text130HzGainTextView.setText("-" + "15 dB");
                        MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twoThirtyHertzBand, (short) (-1500));
                    } else {
                        text130HzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
                        MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twoThirtyHertzBand, (short) -((16-seekBarLevel)*100));
                    }

                } else if (seekBarLevel > 16) {
                    text130HzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
                    MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twoThirtyHertzBand, (short) ((seekBarLevel-16)*100));
                }

                oneThirtyHertzLevel = seekBarLevel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

    };

    /**
     * 320 Hz equalizer seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer320HzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {

            try {
                //Get the appropriate equalizer band.
                short nineTenHertzBand = MyApp.getService().getEqualizerHelper().getEqualizer().getBand(320000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel==16) {
                    text320HzGainTextView.setText("0 dB");
                    MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(nineTenHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel==0) {
                        text320HzGainTextView.setText("-" + "15 dB");
                        MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(nineTenHertzBand, (short) (-1500));
                    } else {
                        text320HzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
                        MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(nineTenHertzBand, (short) -((16-seekBarLevel)*100));
                    }

                } else if (seekBarLevel > 16) {
                    text320HzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
                    MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(nineTenHertzBand, (short) ((seekBarLevel-16)*100));
                }

                threeTwentyHertzLevel = seekBarLevel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

    };

    /**
     * 800 Hz equalizer seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer800HzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {

            try {
                //Get the appropriate equalizer band.
                short threeKiloHertzBand = MyApp.getService().getEqualizerHelper().getEqualizer().getBand(800000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel==16) {
                    text800HzGainTextView.setText("0 dB");
                    MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(threeKiloHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel==0) {
                        text800HzGainTextView.setText("-" + "15 dB");
                        MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(threeKiloHertzBand, (short) (-1500));
                    } else {
                        text800HzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
                        MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(threeKiloHertzBand, (short) -((16-seekBarLevel)*100));
                    }

                } else if (seekBarLevel > 16) {
                    text800HzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
                    MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(threeKiloHertzBand, (short) ((seekBarLevel-16)*100));
                }

                eightHundredHertzLevel = seekBarLevel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

    };

    /**
     * 2 kHz equalizer seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer2kHzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {

            try {
                //Get the appropriate equalizer band.
                short fourteenKiloHertzBand = MyApp.getService().getEqualizerHelper().getEqualizer().getBand(2000000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel==16) {
                    text2kHzGainTextView.setText("0 dB");
                    MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fourteenKiloHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel==0) {
                        text2kHzGainTextView.setText("-" + "15 dB");
                        MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fourteenKiloHertzBand, (short) (-1500));
                    } else {
                        text2kHzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
                        MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fourteenKiloHertzBand, (short) -((16-seekBarLevel)*100));
                    }

                } else if (seekBarLevel > 16) {
                    text2kHzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
                    MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fourteenKiloHertzBand, (short) ((seekBarLevel-16)*100));
                }

                twoKilohertzLevel = seekBarLevel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

    };

    /**
     * 5 kHz equalizer seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer5kHzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {

            try {
                //Get the appropriate equalizer band.
                short fiveKiloHertzBand = MyApp.getService().getEqualizerHelper().getEqualizer().getBand(5000000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel==16) {
                    text5kHzGainTextView.setText("0 dB");
                    MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fiveKiloHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel==0) {
                        text5kHzGainTextView.setText("-" + "15 dB");
                        MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fiveKiloHertzBand, (short) (-1500));
                    } else {
                        text5kHzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
                        MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fiveKiloHertzBand, (short) -((16-seekBarLevel)*100));
                    }

                } else if (seekBarLevel > 16) {
                    text5kHzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
                    MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(fiveKiloHertzBand, (short) ((seekBarLevel-16)*100));
                }

                fiveKilohertzLevel = seekBarLevel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

    };

    /**
     * 12.5 kHz equalizer seekbar listener.
     */
    private SeekBar.OnSeekBarChangeListener equalizer12_5kHzListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int seekBarLevel, boolean changedByUser) {

            try {
                //Get the appropriate equalizer band.
                short twelvePointFiveKiloHertzBand = MyApp.getService().getEqualizerHelper().getEqualizer().getBand(9000000);

                //Set the gain level text based on the slider position.
                if (seekBarLevel==16) {
                    text12_5kHzGainTextView.setText("0 dB");
                    MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) 0);
                } else if (seekBarLevel < 16) {

                    if (seekBarLevel==0) {
                        text12_5kHzGainTextView.setText("-" + "15 dB");
                        MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) (-1500));
                    } else {
                        text12_5kHzGainTextView.setText("-" + (16-seekBarLevel) + " dB");
                        MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) -((16-seekBarLevel)*100));
                    }

                } else if (seekBarLevel > 16) {
                    text12_5kHzGainTextView.setText("+" + (seekBarLevel-16) + " dB");
                    MyApp.getService().getEqualizerHelper().getEqualizer().setBandLevel(twelvePointFiveKiloHertzBand, (short) ((seekBarLevel-16)*100));
                }

                twelvePointFiveKilohertzLevel = seekBarLevel;

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            // TODO Auto-generated method stub

        }

    };

    /**
     * Spinner listener for reverb effects.
     */
    private AdapterView.OnItemSelectedListener reverbListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {

            if (MyApp.getService()!=null)
                if (index==0) {
                    MyApp.getService().getEqualizerHelper().getPresetReverb().setPreset(PresetReverb.PRESET_NONE);
                    reverbSetting = 0;
                } else if (index==1) {
                    MyApp.getService().getEqualizerHelper().getPresetReverb().setPreset(PresetReverb.PRESET_LARGEHALL);
                    reverbSetting = 1;
                } else if (index==2) {
                    MyApp.getService().getEqualizerHelper().getPresetReverb().setPreset(PresetReverb.PRESET_LARGEROOM);
                    reverbSetting = 2;
                } else if (index==3) {
                    MyApp.getService().getEqualizerHelper().getPresetReverb().setPreset(PresetReverb.PRESET_MEDIUMHALL);
                    reverbSetting = 3;
                } else if (index==4) {
                    MyApp.getService().getEqualizerHelper().getPresetReverb().setPreset(PresetReverb.PRESET_MEDIUMROOM);
                    reverbSetting = 4;
                } else if (index==5) {
                    MyApp.getService().getEqualizerHelper().getPresetReverb().setPreset(PresetReverb.PRESET_SMALLROOM);
                    reverbSetting = 5;
                } else if (index==6) {
                    MyApp.getService().getEqualizerHelper().getPresetReverb().setPreset(PresetReverb.PRESET_PLATE);
                    reverbSetting = 6;
                }

                else
                    reverbSetting = 0;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

    };

    /**
     * Bass boost listener.
     */
    private SeekBar.OnSeekBarChangeListener bassBoostListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
            MyApp.getService().getEqualizerHelper().getBassBoost().setStrength((short) arg1);
            bassBoostLevel = (short) arg1;

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

    };

    /**
     * Enhance listener.
     */
    private SeekBar.OnSeekBarChangeListener enhanceListener = new SeekBar.OnSeekBarChangeListener() {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
            MyApp.getService().getEqualizerHelper().getEnhancer().setTargetGain((short) arg1);
            enhancementLevel = (short) arg1;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

    };

    /**
     * Virtualizer listener.
     */
    private SeekBar.OnSeekBarChangeListener virtualizerListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
            MyApp.getService().getEqualizerHelper().getVirtualizer().setStrength((short) arg1);
            virtualizerLevel = (short) arg1;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

    };

    /**
     * Builds the "Save Preset" dialog. Does not call the show() method, so you 
     * should do this manually when calling this method.
     *
     * @return A fully built AlertDialog reference.
     */
    private void showSavePresetDialog() {

        new MyDialogBuilder(this)
                .title(R.string.title_save_preset)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(getString(R.string.hint_save_preset), "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                        //Get the preset name from the text field.
                        if(input.equals("")){
                            Toast.makeText(getApplicationContext(), R.string.error_valid_preset_name_toast, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        MyApp.getService().getEqualizerHelper().insertPreset(input.toString(), getCurrentEquSetting());

                        Toast.makeText(getApplicationContext(), R.string.preset_saved_toast, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .negativeText(R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Builds the "Load Preset" dialog. Does not call the show() method, so this 
     * should be done manually after calling this method.
     *
     * @return A fully built AlertDialog reference.
     */
    private void showLoadPresetDialog() {

        //load data from db here
        String[] array = MyApp.getService().getEqualizerHelper().getPresetList();

        for (String s:array) {
            Log.d("ActivityEqualizer", "showLoadPresetDialog: array " + s);
        }

        new MyDialogBuilder(this)
                .title(R.string.title_load_preset)
                .items(array)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        new AsyncInitSlidersTask().execute(MyApp.getService().getEqualizerHelper().getPreset(text.toString()));
                    }
                })
                .show();

    }

    /**
     * Applies the current EQ settings to the service.
     */
    public void applyCurrentEQSettings() {
        if (MyApp.getService()!=null)
            return;

        equalizer50HzListener.onProgressChanged(equalizer50HzSeekBar, equalizer50HzSeekBar.getProgress(), true);
        equalizer130HzListener.onProgressChanged(equalizer130HzSeekBar, equalizer130HzSeekBar.getProgress(), true);
        equalizer320HzListener.onProgressChanged(equalizer320HzSeekBar, equalizer320HzSeekBar.getProgress(), true);
        equalizer800HzListener.onProgressChanged(equalizer800HzSeekBar, equalizer800HzSeekBar.getProgress(), true);
        equalizer2kHzListener.onProgressChanged(equalizer2kHzSeekBar, equalizer2kHzSeekBar.getProgress(), true);
        equalizer5kHzListener.onProgressChanged(equalizer5kHzSeekBar, equalizer5kHzSeekBar.getProgress(), true);
        equalizer12_5kHzListener.onProgressChanged(equalizer12_5kHzSeekBar, equalizer12_5kHzSeekBar.getProgress(), true);

        virtualizerListener.onProgressChanged(virtualizerSeekBar, virtualizerSeekBar.getProgress(), true);
        bassBoostListener.onProgressChanged(bassBoostSeekBar, bassBoostSeekBar.getProgress(), true);
        enhanceListener.onProgressChanged(enhanceSeekBar, enhanceSeekBar.getProgress(), true);
        reverbListener.onItemSelected(reverbSpinner, null, reverbSpinner.getSelectedItemPosition(), 0l);

    }

    @SuppressLint("StaticFieldLeak")
    public class AsyncInitSlidersTask extends AsyncTask<EqualizerSetting, String, Boolean> {

        EqualizerSetting equalizerSetting;

        @Override
        protected Boolean doInBackground(EqualizerSetting... params) {
            equalizerSetting = params[0];
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if(equalizerSetting==null) return;

            Log.d("ActivityEqualizer", "onResume: found equ setting : " + equalizerSetting.toString());

            fiftyHertzLevel = equalizerSetting.getFiftyHertz();
            oneThirtyHertzLevel = equalizerSetting.getOneThirtyHertz();
            threeTwentyHertzLevel = equalizerSetting.getThreeTwentyHertz();
            eightHundredHertzLevel = equalizerSetting.getEightHundredHertz();
            twoKilohertzLevel = equalizerSetting.getTwoKilohertz();
            fiveKilohertzLevel = equalizerSetting.getFiveKilohertz();
            twelvePointFiveKilohertzLevel = equalizerSetting.getTwelvePointFiveKilohertz();
            virtualizerLevel = equalizerSetting.getVirtualizer();
            bassBoostLevel = equalizerSetting.getBassBoost();
            enhancementLevel = equalizerSetting.getEnhancement();
            reverbSetting = equalizerSetting.getReverb();

            //Move the sliders to the equalizer settings.
            equalizer50HzSeekBar.setProgressAndThumb(fiftyHertzLevel);
            equalizer130HzSeekBar.setProgressAndThumb(oneThirtyHertzLevel);
            equalizer320HzSeekBar.setProgressAndThumb(threeTwentyHertzLevel);
            equalizer800HzSeekBar.setProgressAndThumb(eightHundredHertzLevel);
            equalizer2kHzSeekBar.setProgressAndThumb(twoKilohertzLevel);
            equalizer5kHzSeekBar.setProgressAndThumb(fiveKilohertzLevel);
            equalizer12_5kHzSeekBar.setProgressAndThumb(twelvePointFiveKilohertzLevel);
            virtualizerSeekBar.setProgress(virtualizerLevel);
            bassBoostSeekBar.setProgress(bassBoostLevel);
            enhanceSeekBar.setProgress(enhancementLevel);

            if(reverbSetting < reverbSpinner.getAdapter().getCount())
                reverbSpinner.setSelection(reverbSetting, false);

            //50Hz Band.
            if (fiftyHertzLevel==16) {
                text50HzGainTextView.setText("0 dB");
            } else if (fiftyHertzLevel < 16) {

                if (fiftyHertzLevel==0) {
                    text50HzGainTextView.setText("-" + "15 dB");
                } else {
                    text50HzGainTextView.setText("-" + (16-fiftyHertzLevel) + " dB");
                }

            } else if (fiftyHertzLevel > 16) {
                text50HzGainTextView.setText("+" + (fiftyHertzLevel-16) + " dB");
            }

            //130Hz Band.
            if (oneThirtyHertzLevel==16) {
                text130HzGainTextView.setText("0 dB");
            } else if (oneThirtyHertzLevel < 16) {

                if (oneThirtyHertzLevel==0) {
                    text130HzGainTextView.setText("-" + "15 dB");
                } else {
                    text130HzGainTextView.setText("-" + (16-oneThirtyHertzLevel) + " dB");
                }

            } else if (oneThirtyHertzLevel > 16) {
                text130HzGainTextView.setText("+" + (oneThirtyHertzLevel-16) + " dB");
            }

            //320Hz Band.
            if (threeTwentyHertzLevel==16) {
                text320HzGainTextView.setText("0 dB");
            } else if (threeTwentyHertzLevel < 16) {

                if (threeTwentyHertzLevel==0) {
                    text320HzGainTextView.setText("-" + "15 dB");
                } else {
                    text320HzGainTextView.setText("-" + (16-threeTwentyHertzLevel) + " dB");
                }

            } else if (threeTwentyHertzLevel > 16) {
                text320HzGainTextView.setText("+" + (threeTwentyHertzLevel-16) + " dB");
            }

            //800Hz Band.
            if (eightHundredHertzLevel==16) {
                text800HzGainTextView.setText("0 dB");
            } else if (eightHundredHertzLevel < 16) {

                if (eightHundredHertzLevel==0) {
                    text800HzGainTextView.setText("-" + "15 dB");
                } else {
                    text800HzGainTextView.setText("-" + (16-eightHundredHertzLevel) + " dB");
                }

            } else if (eightHundredHertzLevel > 16) {
                text800HzGainTextView.setText("+" + (eightHundredHertzLevel-16) + " dB");
            }

            //2kHz Band.
            if (twoKilohertzLevel==16) {
                text2kHzGainTextView.setText("0 dB");
            } else if (twoKilohertzLevel < 16) {

                if (twoKilohertzLevel==0) {
                    text2kHzGainTextView.setText("-" + "15 dB");
                } else {
                    text2kHzGainTextView.setText("-" + (16-twoKilohertzLevel) + " dB");
                }

            } else if (twoKilohertzLevel > 16) {
                text2kHzGainTextView.setText("+" + (twoKilohertzLevel-16) + " dB");
            }

            //5kHz Band.
            if (fiveKilohertzLevel==16) {
                text5kHzGainTextView.setText("0 dB");
            } else if (fiveKilohertzLevel < 16) {

                if (fiveKilohertzLevel==0) {
                    text5kHzGainTextView.setText("-" + "15 dB");
                } else {
                    text5kHzGainTextView.setText("-" + (16-fiveKilohertzLevel) + " dB");
                }

            } else if (fiveKilohertzLevel > 16) {
                text5kHzGainTextView.setText("+" + (fiveKilohertzLevel-16) + " dB");
            }

            //12.5kHz Band.
            if (twelvePointFiveKilohertzLevel==16) {
                text12_5kHzGainTextView.setText("0 dB");
            } else if (twelvePointFiveKilohertzLevel < 16) {

                if (twelvePointFiveKilohertzLevel==0) {
                    text12_5kHzGainTextView.setText("-" + "15 dB");
                } else {
                    text12_5kHzGainTextView.setText("-" + (16-twelvePointFiveKilohertzLevel) + " dB");
                }

            } else if (twelvePointFiveKilohertzLevel > 16) {
                text12_5kHzGainTextView.setText("+" + (twelvePointFiveKilohertzLevel-16) + " dB");
            }

        }

    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
