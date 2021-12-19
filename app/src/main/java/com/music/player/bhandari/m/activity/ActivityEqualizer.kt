package com.music.player.bhandari.m.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import com.google.firebase.analytics.FirebaseAnalytics
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.customViews.VerticalSeekBar
import com.music.player.bhandari.m.equalizer.EqualizerSetting
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.utils.UtilityFun
import io.github.inflationx.viewpump.ViewPumpContextWrapper

/**
 * Copyright 2017 Amit Bhandari AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class ActivityEqualizer : AppCompatActivity() {
    //views
    @BindView(R.id.equalizerScrollView)
    var mScrollView: ScrollView? = null

    @BindView(R.id.equalizerLinearLayout)
    var equalizerView: View? = null

    // 50Hz equalizer controls.
    @BindView(R.id.equalizer50Hz)
    var equalizer50HzSeekBar: VerticalSeekBar? = null

    @BindView(R.id.text50HzGain)
    var text50HzGainTextView: TextView? = null

    @BindView(R.id.text50Hz)
    var text50Hz: TextView? = null

    // 130Hz equalizer controls.
    @BindView(R.id.equalizer130Hz)
    var equalizer130HzSeekBar: VerticalSeekBar? = null

    @BindView(R.id.text130HzGain)
    var text130HzGainTextView: TextView? = null

    @BindView(R.id.text130Hz)
    var text130Hz: TextView? = null

    // 320Hz equalizer controls.
    @BindView(R.id.equalizer320Hz)
    var equalizer320HzSeekBar: VerticalSeekBar? = null

    @BindView(R.id.text320HzGain)
    var text320HzGainTextView: TextView? = null

    @BindView(R.id.text320Hz)
    var text320Hz: TextView? = null

    // 800 Hz equalizer controls.
    @BindView(R.id.equalizer800Hz)
    var equalizer800HzSeekBar: VerticalSeekBar? = null

    @BindView(R.id.text800HzGain)
    var text800HzGainTextView: TextView? = null

    @BindView(R.id.text800Hz)
    var text800Hz: TextView? = null

    // 2 kHz equalizer controls.
    @BindView(R.id.equalizer2kHz)
    var equalizer2kHzSeekBar: VerticalSeekBar? = null

    @BindView(R.id.text2kHzGain)
    var text2kHzGainTextView: TextView? = null

    @BindView(R.id.text2kHz)
    var text2kHz: TextView? = null

    // 5 kHz equalizer controls.
    @BindView(R.id.equalizer5kHz)
    var equalizer5kHzSeekBar: VerticalSeekBar? = null

    @BindView(R.id.text5kHzGain)
    var text5kHzGainTextView: TextView? = null

    @BindView(R.id.text5kHz)
    var text5kHz: TextView? = null

    // 12.5 kHz equalizer controls.
    @BindView(R.id.equalizer12_5kHz)
    var equalizer12_5kHzSeekBar: VerticalSeekBar? = null

    @BindView(R.id.text12_5kHzGain)
    var text12_5kHzGainTextView: TextView? = null

    @BindView(R.id.text12_5kHz)
    var text12_5kHz: TextView? = null

    // Equalizer preset controls.
    @BindView(R.id.loadPresetButton)
    var loadPresetButton: RelativeLayout? = null

    @BindView(R.id.saveAsPresetButton)
    var saveAsPresetButton: RelativeLayout? = null

    @BindView(R.id.resetAllButton)
    var resetAllButton: RelativeLayout? = null

    @BindView(R.id.load_preset_text)
    var loadPresetText: TextView? = null

    @BindView(R.id.save_as_preset_text)
    var savePresetText: TextView? = null

    @BindView(R.id.reset_all_text)
    var resetAllText: TextView? = null

    // Temp variables that hold the equalizer's settings.
    private var fiftyHertzLevel: Int = 16
    private var oneThirtyHertzLevel: Int = 16
    private var threeTwentyHertzLevel: Int = 16
    private var eightHundredHertzLevel: Int = 16
    private var twoKilohertzLevel: Int = 16
    private var fiveKilohertzLevel: Int = 16
    private var twelvePointFiveKilohertzLevel: Int = 16

    // Temp variables that hold audio fx settings.
    private var virtualizerLevel: Int = 0
    private var bassBoostLevel: Int = 0
    private var enhancementLevel: Int = 0
    private var reverbSetting: Int = 0

    //Audio FX elements.
    @BindView(R.id.virtualizer_seekbar)
    var virtualizerSeekBar: SeekBar? = null

    @BindView(R.id.bass_boost_seekbar)
    var bassBoostSeekBar: SeekBar? = null

    @BindView(R.id.enhancer_seekbar)
    var enhanceSeekBar: SeekBar? = null

    @BindView(R.id.reverb_spinner)
    var reverbSpinner: Spinner? = null

    @BindView(R.id.virtualizer_title_text)
    var virtualizerTitle: TextView? = null

    @BindView(R.id.bass_boost_title_text)
    var bassBoostTitle: TextView? = null

    @BindView(R.id.reverb_title_text)
    var reverbTitle: TextView? = null
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ColorHelper.setStatusBarGradiant(this)
        val themeSelector: Int = MyApp.getPref()
            .getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        setContentView(R.layout.activity_equalizer)
        ButterKnife.bind(this)

        //action bar
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar_)
        toolbar.setTitle(R.string.equalizer_title)
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        if (supportActionBar != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        //mScrollView.setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.GetStatusBarColor());
        }*/

        //Init reverb presets.
        val reverbPresets: ArrayList<String> = ArrayList()
        reverbPresets.add(getString(R.string.preset_none))
        reverbPresets.add(getString(R.string.preset_large_hall))
        reverbPresets.add(getString(R.string.preset_large_room))
        reverbPresets.add(getString(R.string.preset_medium_hall))
        reverbPresets.add(getString(R.string.preset_medium_room))
        reverbPresets.add(getString(R.string.preset_small_room))
        reverbPresets.add(getString(R.string.preset_plate))
        val dataAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, R.layout.simple_spinner_item, reverbPresets)
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        reverbSpinner.setAdapter(dataAdapter)

        //Set the max values for the seekbars.
        virtualizerSeekBar!!.max = 1000
        bassBoostSeekBar!!.max = 1000
        enhanceSeekBar!!.max = 1000
        resetAllButton!!.setOnClickListener(View.OnClickListener { //Reset all sliders to 0.
            equalizer50HzSeekBar!!.setProgressAndThumb(16)
            equalizer130HzSeekBar!!.setProgressAndThumb(16)
            equalizer320HzSeekBar!!.setProgressAndThumb(16)
            equalizer800HzSeekBar!!.setProgressAndThumb(16)
            equalizer2kHzSeekBar!!.setProgressAndThumb(16)
            equalizer5kHzSeekBar!!.setProgressAndThumb(16)
            equalizer12_5kHzSeekBar!!.setProgressAndThumb(16)
            virtualizerSeekBar!!.progress = 0
            bassBoostSeekBar!!.progress = 0
            enhanceSeekBar!!.progress = 0
            reverbSpinner!!.setSelection(0, false)

            //Apply the new setings to the service.
            applyCurrentEQSettings()

            //Show a confirmation toast.
            Toast.makeText(applicationContext,
                R.string.equ_reset_toast,
                Toast.LENGTH_SHORT).show()
        })
        loadPresetButton!!.setOnClickListener { showLoadPresetDialog() }
        saveAsPresetButton!!.setOnClickListener { showSavePresetDialog() }
        equalizer50HzSeekBar!!.setOnSeekBarChangeListener(equalizer50HzListener)
        equalizer130HzSeekBar!!.setOnSeekBarChangeListener(equalizer130HzListener)
        equalizer320HzSeekBar!!.setOnSeekBarChangeListener(equalizer320HzListener)
        equalizer800HzSeekBar!!.setOnSeekBarChangeListener(equalizer800HzListener)
        equalizer2kHzSeekBar!!.setOnSeekBarChangeListener(equalizer2kHzListener)
        equalizer5kHzSeekBar!!.setOnSeekBarChangeListener(equalizer5kHzListener)
        equalizer12_5kHzSeekBar!!.setOnSeekBarChangeListener(equalizer12_5kHzListener)
        virtualizerSeekBar!!.setOnSeekBarChangeListener(virtualizerListener)
        bassBoostSeekBar!!.setOnSeekBarChangeListener(bassBoostListener)
        reverbSpinner.setOnItemSelectedListener(reverbListener)
        enhanceSeekBar!!.setOnSeekBarChangeListener(enhanceListener)
        AsyncInitSlidersTask().execute(MyApp.getService()!!.getEqualizerHelper()!!.getLastEquSetting())
        try {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "equalizer_launched")
            UtilityFun.logEvent(bundle)
        } catch (ignored: Exception) {
        }
        equalizer50HzSeekBar!!.setOnTouchListener(listener)
        equalizer130HzSeekBar!!.setOnTouchListener(listener)
        equalizer320HzSeekBar!!.setOnTouchListener(listener)
        equalizer800HzSeekBar!!.setOnTouchListener(listener)
        equalizer2kHzSeekBar!!.setOnTouchListener(listener)
        equalizer5kHzSeekBar!!.setOnTouchListener(listener)
        equalizer12_5kHzSeekBar!!.setOnTouchListener(listener)
    }

    var listener = View.OnTouchListener { v, event ->
        Log.d("ActivityEqualizer", "onTouch: $event")
        if (event.action == MotionEvent.ACTION_DOWN) {
            mScrollView.requestDisallowInterceptTouchEvent(true)
        } else if (event.action == MotionEvent.ACTION_UP) {
            mScrollView.requestDisallowInterceptTouchEvent(false)
        }
        false
    }

    override fun onResume() {
        super.onResume()
        MyApp.Companion.isAppVisible = true
    }

    override fun onPause() {
        try {
            val equalizerSetting: EqualizerSetting = getCurrentEquSetting()
            MyApp.Companion.getService().getEqualizerHelper().storeLastEquSetting(equalizerSetting)
            Log.d("ActivityEqualizer",
                "onPause: stored equ setting : $equalizerSetting")
            MyApp.Companion.isAppVisible = false
        } catch (ignore: Exception) {
        }
        super.onPause()
    }

    private fun getCurrentEquSetting(): EqualizerSetting {
        val equalizerSetting: EqualizerSetting = EqualizerSetting()
        equalizerSetting.setFiftyHertz(fiftyHertzLevel)
        equalizerSetting.setOneThirtyHertz(oneThirtyHertzLevel)
        equalizerSetting.setThreeTwentyHertz(threeTwentyHertzLevel)
        equalizerSetting.setEightHundredHertz(eightHundredHertzLevel)
        equalizerSetting.setTwoKilohertz(twoKilohertzLevel)
        equalizerSetting.setFiveKilohertz(fiveKilohertzLevel)
        equalizerSetting.setTwelvePointFiveKilohertz(twelvePointFiveKilohertzLevel)
        equalizerSetting.setVirtualizer(virtualizerLevel)
        equalizerSetting.setBassBoost(bassBoostLevel)
        equalizerSetting.setEnhancement(enhancementLevel)
        equalizerSetting.setReverb(reverbSetting)
        return equalizerSetting
    }

    /**
     * 50 Hz equalizer seekbar listener.
     */
    private val equalizer50HzListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(
            arg0: SeekBar,
            seekBarLevel: Int,
            changedByUser: Boolean
        ) {
            Log.d("ActivityEqualizer", "onProgressChanged : ")
            try {
                //Get the appropriate equalizer band.
                val sixtyHertzBand = MyApp.getService()!!.getEqualizerHelper()!!.getEqualizer().getBand(50000)

                //Set the gain level text based on the slider position.
                when {
                    seekBarLevel == 16 -> {
                        text50HzGainTextView!!.text = "0 dB"
                        MyApp.getService()!!.getEqualizerHelper()!!.getEqualizer()!!.setBandLevel(sixtyHertzBand, 0.toShort())
                    }
                    seekBarLevel < 16 -> {
                        if (seekBarLevel == 0) {
                            text50HzGainTextView!!.text = "-" + "15 dB"
                            MyApp.getService().getEqualizerHelper()!!.getEqualizer()!!.setBandLevel(sixtyHertzBand, (-1500).toShort())
                        } else {
                            text50HzGainTextView!!.text = "-" + (16 - seekBarLevel) + " dB"
                            MyApp.getService()!!.getEqualizerHelper()!!.getEqualizer()!!.setBandLevel(sixtyHertzBand, (-((16 - seekBarLevel) * 100)).toShort())
                        }
                    }
                    seekBarLevel > 16 -> {
                        text50HzGainTextView!!.text = "+" + (seekBarLevel - 16) + " dB"
                        MyApp.getService()!!.getEqualizerHelper()!!.getEqualizer()!!.setBandLevel(sixtyHertzBand, ((seekBarLevel - 16) * 100).toShort())
                    }
                }
                fiftyHertzLevel = seekBarLevel
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onStartTrackingTouch(arg0: SeekBar) {
            Log.d("ActivityEqualizer", "onStartTrackingTouch : ")
        }

        override fun onStopTrackingTouch(arg0: SeekBar) {
            Log.d("ActivityEqualizer", "onStopTrackingTouch : ")
        }
    }

    /**
     * 130 Hz equalizer seekbar listener.
     */
    private val equalizer130HzListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(arg0: SeekBar, seekBarLevel: Int, changedByUser: Boolean) {
            try {
                //Get the appropriate equalizer band.
                val twoThirtyHertzBand = MyApp.getService()!!.getEqualizerHelper()!!.getEqualizer()!!.getBand(130000)

                //Set the gain level text based on the slider position.
                when {
                    seekBarLevel == 16 -> {
                        text130HzGainTextView!!.text = "0 dB"
                        MyApp.getService()!!.getEqualizerHelper()!!.getEqualizer()!!.setBandLevel(twoThirtyHertzBand, 0.toShort())
                    }
                    seekBarLevel < 16 -> {
                        if (seekBarLevel == 0) {
                            text130HzGainTextView!!.text = "-" + "15 dB"
                            MyApp.getService()!!.getEqualizerHelper()!!.getEqualizer()!!.setBandLevel(twoThirtyHertzBand, (-1500).toShort())
                        } else {
                            text130HzGainTextView!!.text = "-" + (16 - seekBarLevel) + " dB"
                            MyApp.getService()!!.getEqualizerHelper()!!.getEqualizer()!!.setBandLevel(twoThirtyHertzBand, (-((16 - seekBarLevel) * 100)).toShort())
                        }
                    }
                    seekBarLevel > 16 -> {
                        text130HzGainTextView!!.text = "+" + (seekBarLevel - 16) + " dB"
                        MyApp.getService()!!.getEqualizerHelper()!!.getEqualizer()!!.setBandLevel(twoThirtyHertzBand, ((seekBarLevel - 16) * 100).toShort())
                    }
                }
                oneThirtyHertzLevel = seekBarLevel
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onStartTrackingTouch(arg0: SeekBar) {
            // TODO Auto-generated method stub
        }

        override fun onStopTrackingTouch(arg0: SeekBar) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * 320 Hz equalizer seekbar listener.
     */
    private val equalizer320HzListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(
            arg0: SeekBar,
            seekBarLevel: Int,
            changedByUser: Boolean
        ) {
            try {
                //Get the appropriate equalizer band.
                val nineTenHertzBand: Short =
                    MyApp.Companion.getService().getEqualizerHelper().getEqualizer().getBand(320000)

                //Set the gain level text based on the slider position.
                if (seekBarLevel == 16) {
                    text320HzGainTextView.setText("0 dB")
                    MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                        .setBandLevel(nineTenHertzBand, 0.toShort())
                } else if (seekBarLevel < 16) {
                    if (seekBarLevel == 0) {
                        text320HzGainTextView.setText("-" + "15 dB")
                        MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                            .setBandLevel(nineTenHertzBand, (-1500).toShort())
                    } else {
                        text320HzGainTextView.setText("-" + (16 - seekBarLevel) + " dB")
                        MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                            .setBandLevel(nineTenHertzBand,
                                (-((16 - seekBarLevel) * 100)).toShort())
                    }
                } else if (seekBarLevel > 16) {
                    text320HzGainTextView.setText("+" + (seekBarLevel - 16) + " dB")
                    MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                        .setBandLevel(nineTenHertzBand, ((seekBarLevel - 16) * 100).toShort())
                }
                threeTwentyHertzLevel = seekBarLevel
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onStartTrackingTouch(arg0: SeekBar) {
            // TODO Auto-generated method stub
        }

        override fun onStopTrackingTouch(arg0: SeekBar) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * 800 Hz equalizer seekbar listener.
     */
    private val equalizer800HzListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(
            arg0: SeekBar,
            seekBarLevel: Int,
            changedByUser: Boolean
        ) {
            try {
                //Get the appropriate equalizer band.
                val threeKiloHertzBand: Short =
                    MyApp.Companion.getService().getEqualizerHelper().getEqualizer().getBand(800000)

                //Set the gain level text based on the slider position.
                if (seekBarLevel == 16) {
                    text800HzGainTextView.setText("0 dB")
                    MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                        .setBandLevel(threeKiloHertzBand, 0.toShort())
                } else if (seekBarLevel < 16) {
                    if (seekBarLevel == 0) {
                        text800HzGainTextView.setText("-" + "15 dB")
                        MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                            .setBandLevel(threeKiloHertzBand, (-1500).toShort())
                    } else {
                        text800HzGainTextView.setText("-" + (16 - seekBarLevel) + " dB")
                        MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                            .setBandLevel(threeKiloHertzBand,
                                (-((16 - seekBarLevel) * 100)).toShort())
                    }
                } else if (seekBarLevel > 16) {
                    text800HzGainTextView.setText("+" + (seekBarLevel - 16) + " dB")
                    MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                        .setBandLevel(threeKiloHertzBand, ((seekBarLevel - 16) * 100).toShort())
                }
                eightHundredHertzLevel = seekBarLevel
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onStartTrackingTouch(arg0: SeekBar) {
            // TODO Auto-generated method stub
        }

        override fun onStopTrackingTouch(arg0: SeekBar) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * 2 kHz equalizer seekbar listener.
     */
    private val equalizer2kHzListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(
            arg0: SeekBar,
            seekBarLevel: Int,
            changedByUser: Boolean
        ) {
            try {
                //Get the appropriate equalizer band.
                val fourteenKiloHertzBand: Short =
                    MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                        .getBand(2000000)

                //Set the gain level text based on the slider position.
                if (seekBarLevel == 16) {
                    text2kHzGainTextView.setText("0 dB")
                    MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                        .setBandLevel(fourteenKiloHertzBand, 0.toShort())
                } else if (seekBarLevel < 16) {
                    if (seekBarLevel == 0) {
                        text2kHzGainTextView.setText("-" + "15 dB")
                        MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                            .setBandLevel(fourteenKiloHertzBand, (-1500).toShort())
                    } else {
                        text2kHzGainTextView.setText("-" + (16 - seekBarLevel) + " dB")
                        MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                            .setBandLevel(fourteenKiloHertzBand,
                                (-((16 - seekBarLevel) * 100)).toShort())
                    }
                } else if (seekBarLevel > 16) {
                    text2kHzGainTextView.setText("+" + (seekBarLevel - 16) + " dB")
                    MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                        .setBandLevel(fourteenKiloHertzBand, ((seekBarLevel - 16) * 100).toShort())
                }
                twoKilohertzLevel = seekBarLevel
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onStartTrackingTouch(arg0: SeekBar) {
            // TODO Auto-generated method stub
        }

        override fun onStopTrackingTouch(arg0: SeekBar) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * 5 kHz equalizer seekbar listener.
     */
    private val equalizer5kHzListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(
            arg0: SeekBar,
            seekBarLevel: Int,
            changedByUser: Boolean
        ) {
            try {
                //Get the appropriate equalizer band.
                val fiveKiloHertzBand: Short =
                    MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                        .getBand(5000000)

                //Set the gain level text based on the slider position.
                if (seekBarLevel == 16) {
                    text5kHzGainTextView.setText("0 dB")
                    MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                        .setBandLevel(fiveKiloHertzBand, 0.toShort())
                } else if (seekBarLevel < 16) {
                    if (seekBarLevel == 0) {
                        text5kHzGainTextView.setText("-" + "15 dB")
                        MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                            .setBandLevel(fiveKiloHertzBand, (-1500).toShort())
                    } else {
                        text5kHzGainTextView.setText("-" + (16 - seekBarLevel) + " dB")
                        MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                            .setBandLevel(fiveKiloHertzBand,
                                (-((16 - seekBarLevel) * 100)).toShort())
                    }
                } else if (seekBarLevel > 16) {
                    text5kHzGainTextView.setText("+" + (seekBarLevel - 16) + " dB")
                    MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                        .setBandLevel(fiveKiloHertzBand, ((seekBarLevel - 16) * 100).toShort())
                }
                fiveKilohertzLevel = seekBarLevel
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onStartTrackingTouch(arg0: SeekBar) {
            // TODO Auto-generated method stub
        }

        override fun onStopTrackingTouch(arg0: SeekBar) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * 12.5 kHz equalizer seekbar listener.
     */
    private val equalizer12_5kHzListener =
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                arg0: SeekBar,
                seekBarLevel: Int,
                changedByUser: Boolean
            ) {
                try {
                    //Get the appropriate equalizer band.
                    val twelvePointFiveKiloHertzBand: Short =
                        MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                            .getBand(9000000)

                    //Set the gain level text based on the slider position.
                    if (seekBarLevel == 16) {
                        text12_5kHzGainTextView.setText("0 dB")
                        MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                            .setBandLevel(twelvePointFiveKiloHertzBand, 0.toShort())
                    } else if (seekBarLevel < 16) {
                        if (seekBarLevel == 0) {
                            text12_5kHzGainTextView.setText("-" + "15 dB")
                            MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                                .setBandLevel(twelvePointFiveKiloHertzBand, (-1500).toShort())
                        } else {
                            text12_5kHzGainTextView.setText("-" + (16 - seekBarLevel) + " dB")
                            MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                                .setBandLevel(twelvePointFiveKiloHertzBand,
                                    (-((16 - seekBarLevel) * 100)).toShort())
                        }
                    } else if (seekBarLevel > 16) {
                        text12_5kHzGainTextView.setText("+" + (seekBarLevel - 16) + " dB")
                        MyApp.Companion.getService().getEqualizerHelper().getEqualizer()
                            .setBandLevel(twelvePointFiveKiloHertzBand,
                                ((seekBarLevel - 16) * 100).toShort())
                    }
                    twelvePointFiveKilohertzLevel = seekBarLevel
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onStartTrackingTouch(arg0: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStopTrackingTouch(arg0: SeekBar) {
                // TODO Auto-generated method stub
            }
        }

    /**
     * Spinner listener for reverb effects.
     */
    private val reverbListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                arg0: AdapterView<*>?,
                arg1: View,
                index: Int,
                arg3: Long
            ) {
                if (MyApp.Companion.getService() != null) if (index == 0) {
                    MyApp.Companion.getService().getEqualizerHelper().getPresetReverb()
                        .setPreset(PresetReverb.PRESET_NONE)
                    reverbSetting = 0
                } else if (index == 1) {
                    MyApp.Companion.getService().getEqualizerHelper().getPresetReverb()
                        .setPreset(PresetReverb.PRESET_LARGEHALL)
                    reverbSetting = 1
                } else if (index == 2) {
                    MyApp.Companion.getService().getEqualizerHelper().getPresetReverb()
                        .setPreset(PresetReverb.PRESET_LARGEROOM)
                    reverbSetting = 2
                } else if (index == 3) {
                    MyApp.Companion.getService().getEqualizerHelper().getPresetReverb()
                        .setPreset(PresetReverb.PRESET_MEDIUMHALL)
                    reverbSetting = 3
                } else if (index == 4) {
                    MyApp.Companion.getService().getEqualizerHelper().getPresetReverb()
                        .setPreset(PresetReverb.PRESET_MEDIUMROOM)
                    reverbSetting = 4
                } else if (index == 5) {
                    MyApp.Companion.getService().getEqualizerHelper().getPresetReverb()
                        .setPreset(PresetReverb.PRESET_SMALLROOM)
                    reverbSetting = 5
                } else if (index == 6) {
                    MyApp.Companion.getService().getEqualizerHelper().getPresetReverb()
                        .setPreset(PresetReverb.PRESET_PLATE)
                    reverbSetting = 6
                } else reverbSetting = 0
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {
                // TODO Auto-generated method stub
            }
        }

    /**
     * Bass boost listener.
     */
    private val bassBoostListener = object :
        SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(arg0: SeekBar, arg1: Int, arg2: Boolean) {
            MyApp.Companion.getService().getEqualizerHelper().getBassBoost()
                .setStrength(arg1.toShort())
            bassBoostLevel = arg1
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            // TODO Auto-generated method stub
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * Enhance listener.
     */
    private val enhanceListener = object :
        SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(arg0: SeekBar, arg1: Int, arg2: Boolean) {
            MyApp.getService()?.getEqualizerHelper().getEnhancer()
                .setTargetGain(arg1.toShort())
            enhancementLevel = arg1
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            // TODO Auto-generated method stub
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * Virtualizer listener.
     */
    private val virtualizerListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(arg0: SeekBar, arg1: Int, arg2: Boolean) {
            MyApp.getService()?.getEqualizerHelper().getVirtualizer()
                .setStrength(arg1.toShort())
            virtualizerLevel = arg1
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            // TODO Auto-generated method stub
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * Builds the "Save Preset" dialog. Does not call the show() method, so you
     * should do this manually when calling this method.
     *
     * @return A fully built AlertDialog reference.
     */
    private fun showSavePresetDialog() {
//        MyDialogBuilder(this)
//            .title(R.string.title_save_preset)
//            .inputType(InputType.TYPE_CLASS_TEXT)
//            .input(getString(R.string.hint_save_preset), "", object : InputCallback() {
//                fun onInput(dialog: MaterialDialog, input: CharSequence) {
//                    // Do something
//                    //Get the preset name from the text field.
//                    if ((input == "")) {
//                        Toast.makeText(applicationContext,
//                            R.string.error_valid_preset_name_toast,
//                            Toast.LENGTH_SHORT).show()
//                        return
//                    }
//                    MyApp.Companion.getService().getEqualizerHelper()
//                        .insertPreset(input.toString(), getCurrentEquSetting())
//                    Toast.makeText(applicationContext,
//                        R.string.preset_saved_toast,
//                        Toast.LENGTH_SHORT).show()
//                    dialog.dismiss()
//                }
//            })
//            .negativeText(R.string.cancel)
//            .onNegative(object : SingleButtonCallback() {
//                fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                    dialog.dismiss()
//                }
//            })
//            .show()
    }

    /**
     * Builds the "Load Preset" dialog. Does not call the show() method, so this
     * should be done manually after calling this method.
     *
     * @return A fully built AlertDialog reference.
     */
    private fun showLoadPresetDialog() {

        //load data from db here
        val array: Array<String> = MyApp.Companion.getService().getEqualizerHelper().getPresetList()
        for (s: String in array) {
            Log.d("ActivityEqualizer", "showLoadPresetDialog: array " + s)
        }
//        MyDialogBuilder(this)
//            .title(R.string.title_load_preset)
//            .items(array)
//            .itemsCallback(object : ListCallback() {
//                fun onSelection(
//                    dialog: MaterialDialog?,
//                    view: View?,
//                    which: Int,
//                    text: CharSequence
//                ) {
//                    AsyncInitSlidersTask().execute(MyApp.Companion.getService().getEqualizerHelper()
//                        .getPreset(text.toString()))
//                }
//            })
//            .show()
    }

    /**
     * Applies the current EQ settings to the service.
     */
    fun applyCurrentEQSettings() {
        if (MyApp.getService() != null) return
        equalizer50HzListener.onProgressChanged(equalizer50HzSeekBar!!, equalizer50HzSeekBar!!.progress, true)
        equalizer130HzListener.onProgressChanged(equalizer130HzSeekBar!!, equalizer130HzSeekBar!!.progress, true)
        equalizer320HzListener.onProgressChanged(equalizer320HzSeekBar!!, equalizer320HzSeekBar!!.progress, true)
        equalizer800HzListener.onProgressChanged(equalizer800HzSeekBar!!,
            equalizer800HzSeekBar!!.progress,
            true)
        equalizer2kHzListener.onProgressChanged(equalizer2kHzSeekBar!!,
            equalizer2kHzSeekBar!!.progress,
            true)
        equalizer5kHzListener.onProgressChanged(equalizer5kHzSeekBar!!,
            equalizer5kHzSeekBar!!.progress,
            true)
        equalizer12_5kHzListener.onProgressChanged(equalizer12_5kHzSeekBar!!,
            equalizer12_5kHzSeekBar!!.progress,
            true)
        virtualizerListener.onProgressChanged(virtualizerSeekBar!!,
            virtualizerSeekBar!!.progress,
            true)
        bassBoostListener.onProgressChanged(bassBoostSeekBar!!, bassBoostSeekBar.getProgress(), true)
        enhanceListener.onProgressChanged(enhanceSeekBar!!, enhanceSeekBar.getProgress(), true)
        reverbListener.onItemSelected(reverbSpinner,
            null,
            reverbSpinner.getSelectedItemPosition(),
            0L)
    }

    @SuppressLint("StaticFieldLeak")
    inner class AsyncInitSlidersTask :
        AsyncTask<EqualizerSetting?, String?, Boolean?>() {
        var equalizerSetting: EqualizerSetting? = null

        override fun doInBackground(vararg p0: EqualizerSetting?): Boolean? {
            equalizerSetting = p0[0]
            return null
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            if (equalizerSetting == null) return
            Log.d("ActivityEqualizer",
                "onResume: found equ setting : " + equalizerSetting.toString())
            fiftyHertzLevel = equalizerSetting!!.getFiftyHertz()
            oneThirtyHertzLevel = equalizerSetting!!.getOneThirtyHertz()
            threeTwentyHertzLevel = equalizerSetting!!.getThreeTwentyHertz()
            eightHundredHertzLevel = equalizerSetting!!.getEightHundredHertz()
            twoKilohertzLevel = equalizerSetting!!.getTwoKilohertz()
            fiveKilohertzLevel = equalizerSetting!!.getFiveKilohertz()
            twelvePointFiveKilohertzLevel = equalizerSetting!!.getTwelvePointFiveKilohertz()
            virtualizerLevel = equalizerSetting!!.getVirtualizer()
            bassBoostLevel = equalizerSetting!!.getBassBoost()
            enhancementLevel = equalizerSetting!!.getEnhancement()
            reverbSetting = equalizerSetting!!.getReverb()

            //Move the sliders to the equalizer settings.
            equalizer50HzSeekBar.setProgressAndThumb(fiftyHertzLevel)
            equalizer130HzSeekBar.setProgressAndThumb(oneThirtyHertzLevel)
            equalizer320HzSeekBar.setProgressAndThumb(threeTwentyHertzLevel)
            equalizer800HzSeekBar.setProgressAndThumb(eightHundredHertzLevel)
            equalizer2kHzSeekBar.setProgressAndThumb(twoKilohertzLevel)
            equalizer5kHzSeekBar.setProgressAndThumb(fiveKilohertzLevel)
            equalizer12_5kHzSeekBar.setProgressAndThumb(twelvePointFiveKilohertzLevel)
            virtualizerSeekBar.setProgress(virtualizerLevel)
            bassBoostSeekBar.setProgress(bassBoostLevel)
            enhanceSeekBar.setProgress(enhancementLevel)
            if (reverbSetting < reverbSpinner.getAdapter().count) reverbSpinner.setSelection(
                reverbSetting,
                false)

            //50Hz Band.
            when {
                fiftyHertzLevel == 16 -> {
                    text50HzGainTextView.setText("0 dB")
                }
                fiftyHertzLevel < 16 -> {
                    if (fiftyHertzLevel == 0) {
                        text50HzGainTextView.setText("-" + "15 dB")
                    } else {
                        text50HzGainTextView.setText("-" + (16 - fiftyHertzLevel) + " dB")
                    }
                }
                fiftyHertzLevel > 16 -> {
                    text50HzGainTextView.setText("+" + (fiftyHertzLevel - 16) + " dB")
                }
            }

            //130Hz Band.
            when {
                oneThirtyHertzLevel == 16 -> {
                    text130HzGainTextView.setText("0 dB")
                }
                oneThirtyHertzLevel < 16 -> {
                    if (oneThirtyHertzLevel == 0) {
                        text130HzGainTextView.setText("-" + "15 dB")
                    } else {
                        text130HzGainTextView.setText("-" + (16 - oneThirtyHertzLevel) + " dB")
                    }
                }
                oneThirtyHertzLevel > 16 -> {
                    text130HzGainTextView.setText("+" + (oneThirtyHertzLevel - 16) + " dB")
                }
            }

            //320Hz Band.
            when {
                threeTwentyHertzLevel == 16 -> {
                    text320HzGainTextView!!.text = "0 dB"
                }
                threeTwentyHertzLevel < 16 -> {
                    if (threeTwentyHertzLevel == 0) {
                        text320HzGainTextView!!.text = "-" + "15 dB"
                    } else {
                        text320HzGainTextView!!.text = "-" + (16 - threeTwentyHertzLevel) + " dB"
                    }
                }
                threeTwentyHertzLevel > 16 -> {
                    text320HzGainTextView!!.text = "+" + (threeTwentyHertzLevel - 16) + " dB"
                }
            }

            //800Hz Band.
            when {
                eightHundredHertzLevel == 16 -> {
                    text800HzGainTextView!!.text = "0 dB"
                }
                eightHundredHertzLevel < 16 -> {
                    if (eightHundredHertzLevel == 0) {
                        text800HzGainTextView!!.text = "-" + "15 dB"
                    } else {
                        text800HzGainTextView!!.text = "-" + (16 - eightHundredHertzLevel) + " dB"
                    }
                }
                eightHundredHertzLevel > 16 -> {
                    text800HzGainTextView!!.text = "+" + (eightHundredHertzLevel - 16) + " dB"
                }
            }

            //2kHz Band.
            when {
                twoKilohertzLevel == 16 -> {
                    text2kHzGainTextView!!.text = "0 dB"
                }
                twoKilohertzLevel < 16 -> {
                    if (twoKilohertzLevel == 0) {
                        text2kHzGainTextView!!.text = "-" + "15 dB"
                    } else {
                        text2kHzGainTextView!!.text = "-" + (16 - twoKilohertzLevel) + " dB"
                    }
                }
                twoKilohertzLevel > 16 -> {
                    text2kHzGainTextView!!.text = "+" + (twoKilohertzLevel - 16) + " dB"
                }
            }

            //5kHz Band.
            when {
                fiveKilohertzLevel == 16 -> {
                    text5kHzGainTextView!!.text = "0 dB"
                }
                fiveKilohertzLevel < 16 -> {
                    if (fiveKilohertzLevel == 0) {
                        text5kHzGainTextView!!.text = "-" + "15 dB"
                    } else {
                        text5kHzGainTextView!!.text = "-" + (16 - fiveKilohertzLevel) + " dB"
                    }
                }
                fiveKilohertzLevel > 16 -> {
                    text5kHzGainTextView!!.text = "+" + (fiveKilohertzLevel - 16) + " dB"
                }
            }

            //12.5kHz Band.
            when {
                twelvePointFiveKilohertzLevel == 16 -> {
                    text12_5kHzGainTextView!!.text = "0 dB"
                }
                twelvePointFiveKilohertzLevel < 16 -> {
                    if (twelvePointFiveKilohertzLevel == 0) {
                        text12_5kHzGainTextView!!.text = "-" + "15 dB"
                    } else {
                        text12_5kHzGainTextView!!.text = "-" + (16 - twelvePointFiveKilohertzLevel) + " dB"
                    }
                }
                twelvePointFiveKilohertzLevel > 16 -> {
                    text12_5kHzGainTextView!!.text = "+" + (twelvePointFiveKilohertzLevel - 16) + " dB"
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }
}