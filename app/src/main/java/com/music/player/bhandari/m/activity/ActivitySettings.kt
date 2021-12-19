package com.music.player.bhandari.m.activity

import android.R
import android.content.Context
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.afollestad.materialdialogs.MaterialDialog
import com.music.player.bhandari.m.model.Constants
import java.util.concurrent.Executors

/**
 * Copyright 2017 Amit Bhandari AB
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class ActivitySettings constructor() : AppCompatActivity() {
    private var launchedFrom: Int = 0
    private var playerService: PlayerService? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {

        //if player service not running, kill the app
        if (MyApp.Companion.getService() == null) {
            UtilityFun.restartApp()
            finish()
            return
        }
        playerService = MyApp.Companion.getService()
        ColorHelper.setStatusBarGradiant(this)
        val themeSelector: Int = MyApp.Companion.getPref()
            .getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDarkPref)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDarkPref)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        super.onCreate(savedInstanceState)
        launchedFrom = getIntent().getIntExtra("launchedFrom", 0)
        setContentView(R.layout.acitivty_settings)

        //findViewById(R.id.root_view_settings).setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar_)
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true)
            getSupportActionBar().setDisplayShowHomeEnabled(true)
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.GetStatusBarColor());
        }*/setTitle("Settings")
        getFragmentManager().beginTransaction()
            .replace(R.id.linear_layout_fragment, MyPreferenceFragment()).commit()
    }

    protected override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    public override fun onBackPressed() {
        when (launchedFrom) {
            Constants.PREF_LAUNCHED_FROM.MAIN -> startActivity(Intent(this,
                ActivityMain::class.java))
            Constants.PREF_LAUNCHED_FROM.DRAWER -> startActivity(Intent(this,
                ActivityMain::class.java))
            Constants.PREF_LAUNCHED_FROM.NOW_PLAYING -> startActivity(Intent(this,
                ActivityNowPlaying::class.java))
            else -> startActivity(Intent(this, ActivityMain::class.java))
        }
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        super.onBackPressed()
    }

    public override fun onPause() {
        MyApp.Companion.isAppVisible = false
        super.onPause()
    }

    public override fun onResume() {
        if (MyApp.Companion.getService() == null) {
            UtilityFun.restartApp()
            finish()
        }
        MyApp.Companion.isAppVisible = true
        super.onResume()
    }

    public override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY -> playerService.play()
            KeyEvent.KEYCODE_MEDIA_NEXT -> playerService.nextTrack()
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> playerService.prevTrack()
            KeyEvent.KEYCODE_MEDIA_STOP -> playerService.stop()
            KeyEvent.KEYCODE_BACK -> onBackPressed()
        }
        return false
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri: Uri = result.getUri()
                val fromFile: File = File(resultUri.getPath())
                val savePath: String
                when (backgroundSelectionStatus) {
                    MAIN_LIB -> savePath = MyApp.Companion.getContext().getFilesDir()
                        .toString() + getString(R.string.main_lib_back_custom_image)
                    NOW_PLAYING -> savePath = MyApp.Companion.getContext().getFilesDir()
                        .toString() + getString(R.string.now_playing_back_custom_image)
                    DEFAULT_ALBUM_ART -> savePath = MyApp.Companion.getContext().getFilesDir()
                        .toString() + getString(R.string.def_album_art_custom_image)
                    NAVIGATION_DRAWER -> savePath = MyApp.Companion.getContext().getFilesDir()
                        .toString() + getString(R.string.nav_back_custom_image)
                    else -> savePath = MyApp.Companion.getContext().getFilesDir()
                        .toString() + getString(R.string.nav_back_custom_image)
                }
                val toFile: File = File(savePath)
                val b: Boolean = fromFile.renameTo(toFile)
                Log.d(Constants.TAG,
                    "onActivityResult: saved custom image size : " + toFile.length() / (1024))
                if (b) {
                    when (backgroundSelectionStatus) {
                        MAIN_LIB -> MyApp.Companion.getPref().edit()
                            .putInt(getString(R.string.pref_main_library_back), 1).apply()
                        NOW_PLAYING -> MyApp.Companion.getPref().edit()
                            .putInt(getString(R.string.pref_now_playing_back), 3).apply()
                        DEFAULT_ALBUM_ART -> MyApp.Companion.getPref().edit()
                            .putInt(getString(R.string.pref_default_album_art), 1).apply()
                        NAVIGATION_DRAWER -> MyApp.Companion.getPref().edit()
                            .putInt(getString(R.string.pref_nav_library_back), 1).apply()
                        else -> MyApp.Companion.getPref().edit()
                            .putInt(getString(R.string.pref_nav_library_back), 1).apply()
                    }
                    Toast.makeText(this, "Background successfully updated!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this,
                        "Failed to save file, try some different image!",
                        Toast.LENGTH_SHORT).show()
                }
                Log.d(Constants.TAG, "onActivityResult: " + result.toString())
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Failed to select image, try again!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    @SuppressLint("validFragment")
    class MyPreferenceFragment constructor() : PreferenceFragment(), OnStartDragListener {
        val PLAY_PAUSE: String = "Play/Pause Current Track"
        val NEXT: String = "Play Next Track"
        val PREVIOUS: String = "Play Previous Track"
        val MONOSPACE: String = "Monospace"
        val SOFIA: String = "Sofia"
        val MANROPE: String = "Manrope (Recommended)"
        val ASAP: String = "Asap"
        val SYSTEM_DEFAULT: String = "System Default"
        val ROBOTO: String = "Roboto"

        /*final String ACLONICA = "Aclonica";
        final String CHERRY_SWASH = "Cherry Swash";
        final String CORBEN = "Corben";
        final String NOVA_ROUND = "Nova (Round)";
        final String NOVA_SCRIPT = "Nova (Script)";
        final String PACIFITO = "Pacifito";
        final String PURPLE_PURSE = "Purple Purse";
        final String QUANTICO = "Quantico";
        final String ROBOTO_C = "Roboto (Condensed)";
        final String ROBOTO_M = "Roboto (Mono)";
        final String TRADE_WINDS = "Trade Winds";
        final String UBUNTU = "Ubuntu";

        final String CONCERT_ONE = "Concert One";
        final String LATO = "Lato";
        final String LATO_ITALIC = "Lato (Italic)";
        final String LORA = "Lora";
        final String MONTSERRAT = "Montserrat";
        final String OPEN_SANS_LIGHT = "Open Sans Light";
        final String OSWALD = "Oswald";
        final String PROMPT = "Prompt";
        final String PROMPY_MEDIUM = "Prompt (Medium)";
        final String PT_SANS = "PT Sans";
        final String RALEWAY = "Raleway";
        final String SLABO = "Slabo";
        final String SOURCE_SANS_PRO = "Source Sans Pro";*/
        val LIST: String = "List View"
        val GRID: String = "Grid View"
        private var instantLyricStatus: CheckBoxPreference? = null
        private var mItemTouchHelper: ItemTouchHelper? = null
        public override fun onResume() {
            super.onResume()
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                setInstantLyricStatus()
            }
        }

        private fun setInstantLyricStatus() {
            if (instantLyricStatus != null) {
                if (NotificationListenerService.isListeningAuthorized(MyApp.Companion.getContext())) {
                    MyApp.Companion.getPref().edit()
                        .putBoolean(getString(R.string.pref_instant_lyric), true).apply()
                    instantLyricStatus.setChecked(true)
                } else {
                    MyApp.Companion.getPref().edit()
                        .putBoolean(getString(R.string.pref_instant_lyric), false).apply()
                    instantLyricStatus.setChecked(false)
                }
            }
        }

        public override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)

            //Theme color
            val primaryColorPref: Preference = findPreference(getString(R.string.pref_theme_color))
            primaryColorPref.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                //open browser or intent here
                //PrimarySelectionDialog();
                themeSelectionDialog()
                true
            }))

            //now playing back
            val nowPlayingBackPref: Preference =
                findPreference(getString(R.string.pref_now_playing_back))
            nowPlayingBackPref.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                nowPlayingBackDialog()
                true
            }))

            //Main library back
            val mainLibBackPref: Preference =
                findPreference(getString(R.string.pref_main_library_back))
            mainLibBackPref.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                mainLibBackDialog()
                true
            }))

            //Main library back
            val navLibBackPref: Preference =
                findPreference(getString(R.string.pref_nav_library_back))
            navLibBackPref.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                navBackDialog()
                true
            }))

            //Main library back
            val defAlbumArtPref: Preference =
                findPreference(getString(R.string.pref_default_album_art))
            defAlbumArtPref.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                defAlbumArtDialog()
                true
            }))

            //text font
            val fontPref: Preference = findPreference(getString(R.string.pref_text_font))
            val textFontPref: Int = MyApp.Companion.getPref()
                .getInt(getString(R.string.pref_text_font), Constants.TYPEFACE.MANROPE)
            when (textFontPref) {
                Constants.TYPEFACE.MONOSPACE -> findPreference(getString(R.string.pref_text_font)).setSummary(
                    MONOSPACE)
                Constants.TYPEFACE.SOFIA -> findPreference(getString(R.string.pref_text_font)).setSummary(
                    SOFIA)
                Constants.TYPEFACE.SYSTEM_DEFAULT -> findPreference(getString(R.string.pref_text_font)).setSummary(
                    SYSTEM_DEFAULT)
                Constants.TYPEFACE.MANROPE -> findPreference(getString(R.string.pref_text_font)).setSummary(
                    MANROPE)
                Constants.TYPEFACE.ASAP -> findPreference(getString(R.string.pref_text_font)).setSummary(
                    ASAP)
                Constants.TYPEFACE.ROBOTO -> findPreference(getString(R.string.pref_text_font)).setSummary(
                    ROBOTO)
            }
            fontPref.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                //open browser or intent here
                fontPrefSelectionDialog()
                true
            }))

            //lockscreen albumName art
            val lockScreenArt: CheckBoxPreference =
                findPreference(getString(R.string.pref_lock_screen_album_Art)) as CheckBoxPreference
            lockScreenArt.setOnPreferenceChangeListener(OnPreferenceChangeListener({ preference: Preference?, newValue: Any ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    Toast.makeText(getActivity(),
                        "Feature is only available on lollipop and above!",
                        Toast.LENGTH_LONG).show()
                    return@setOnPreferenceChangeListener false
                }
                if ((newValue as Boolean)) {
                    MyApp.Companion.getPref().edit()
                        .putBoolean(getString(R.string.pref_lock_screen_album_Art), true).apply()
                    MyApp.Companion.getService().setMediaSessionMetadata(true)
                } else {
                    MyApp.Companion.getPref().edit()
                        .putBoolean(getString(R.string.pref_lock_screen_album_Art), false).apply()
                    MyApp.Companion.getService().setMediaSessionMetadata(false)
                }
                true
            }))

            //prefer system equalizer
            val albumLibView: Preference = findPreference(getString(R.string.pref_album_lib_view))
            if (MyApp.Companion.getPref()
                    .getBoolean(getString(R.string.pref_album_lib_view), true)
            ) {
                albumLibView.setSummary(GRID)
            } else {
                albumLibView.setSummary(LIST)
            }
            albumLibView.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                albumViewDialog()
                true
            }))

            //prefer system equalizer
            val prefPrefSystemEqu: CheckBoxPreference =
                findPreference(getString(R.string.pref_prefer_system_equ)) as CheckBoxPreference
            prefPrefSystemEqu.setOnPreferenceChangeListener(OnPreferenceChangeListener({ preference: Preference?, newValue: Any ->
                if ((newValue as Boolean)) {
                    MyApp.Companion.getPref().edit()
                        .putBoolean(getString(R.string.pref_prefer_system_equ), true).apply()
                } else {
                    MyApp.Companion.getPref().edit()
                        .putBoolean(getString(R.string.pref_prefer_system_equ), false).apply()
                }
                true
            }))

            //notifcations
            val notifications: CheckBoxPreference =
                findPreference(getString(R.string.pref_notifications)) as CheckBoxPreference
            notifications.setOnPreferenceChangeListener(OnPreferenceChangeListener({ preference: Preference?, newValue: Any ->
                val pos_text: String
                if ((newValue as Boolean)) {
                    pos_text = getString(R.string.turn_on)
                } else {
                    pos_text = getString(R.string.turn_off)
                }
                MyDialogBuilder(getActivity())
                    .title(R.string.notifications_title)
                    .content(R.string.notification_content)
                    .positiveText(pos_text)
                    .negativeText(getString(R.string.cancel))
                    .onPositive({ dialog, which ->
                        val country: String = MyApp.Companion.getPref()
                            .getString(MyApp.Companion.getContext()
                                .getString(R.string.pref_user_country), "")
                        if (MyApp.Companion.getPref()
                                .getBoolean(getString(R.string.pref_notifications), true)
                        ) {
                            MyApp.Companion.getPref().edit()
                                .putBoolean(getString(R.string.pref_notifications), false).apply()
                            try {
                                FirebaseMessaging.getInstance().unsubscribeFromTopic(country)
                                FirebaseMessaging.getInstance().unsubscribeFromTopic("ab_music")
                            } catch (ignored: Exception) {
                            }
                            notifications.setChecked(false)
                        } else {
                            MyApp.Companion.getPref().edit()
                                .putBoolean(getString(R.string.pref_notifications), true).apply()
                            notifications.setChecked(true)
                            try {
                                FirebaseMessaging.getInstance().subscribeToTopic(country)
                                FirebaseMessaging.getInstance().subscribeToTopic("ab_music")
                            } catch (ignored: Exception) {
                            }
                        }
                    })
                    .show()
                false
            }))

            //shake
            val shakeStatus: CheckBoxPreference =
                findPreference(getString(R.string.pref_shake)) as CheckBoxPreference
            shakeStatus.setOnPreferenceChangeListener(OnPreferenceChangeListener({ preference: Preference?, newValue: Any ->
                if ((newValue as Boolean)) {
                    MyApp.Companion.getPref().edit()
                        .putBoolean(getString(R.string.pref_shake), true).apply()
                    PlayerService.setShakeListener(true)
                } else {
                    MyApp.Companion.getPref().edit()
                        .putBoolean(getString(R.string.pref_shake), false).apply()
                    PlayerService.setShakeListener(false)
                }
                true
            }))
            val continuousPlaybackPref: CheckBoxPreference =
                findPreference(getString(R.string.pref_continuous_playback)) as CheckBoxPreference
            continuousPlaybackPref.setOnPreferenceChangeListener(OnPreferenceChangeListener({ preference: Preference?, newValue: Any ->
                val pos_text: String
                if ((newValue as Boolean)) {
                    pos_text = getString(R.string.turn_on)
                } else {
                    pos_text = getString(R.string.turn_off)
                }
                val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                    .title(R.string.title_continous_playback)
                    .content(R.string.cont_playback_content)
                    .positiveText(pos_text)
                    .negativeText(getString(R.string.cancel))
                    .onPositive({ dialog12, which ->
                        if (newValue) {
                            MyApp.Companion.getPref().edit()
                                .putBoolean(getString(R.string.pref_continuous_playback), true)
                                .apply()
                            continuousPlaybackPref.setChecked(true)
                        } else {
                            MyApp.Companion.getPref().edit()
                                .putBoolean(getString(R.string.pref_continuous_playback), false)
                                .apply()
                            continuousPlaybackPref.setChecked(false)
                        }
                    })
                    .build()
                dialog.show()
                false
            }))
            val dataSaverPref: CheckBoxPreference =
                findPreference(getString(R.string.pref_data_saver)) as CheckBoxPreference
            dataSaverPref.setOnPreferenceChangeListener(OnPreferenceChangeListener({ preference: Preference?, newValue: Any ->
                val pos_text: String
                if ((newValue as Boolean)) {
                    pos_text = getString(R.string.turn_on)
                } else {
                    pos_text = getString(R.string.turn_off)
                }
                val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                    .title(R.string.title_data_Saver)
                    .content(R.string.data_saver_content)
                    .positiveText(pos_text)
                    .negativeText(getString(R.string.cancel))
                    .onPositive({ dialog13, which ->
                        if (newValue) {
                            MyApp.Companion.getPref().edit()
                                .putBoolean(getString(R.string.pref_data_saver), true).apply()
                            dataSaverPref.setChecked(true)
                        } else {
                            MyApp.Companion.getPref().edit()
                                .putBoolean(getString(R.string.pref_data_saver), false).apply()
                            dataSaverPref.setChecked(false)
                        }
                    })
                    .build()
                dialog.show()
                false
            }))
            instantLyricStatus =
                findPreference(getString(R.string.pref_instant_lyric)) as CheckBoxPreference?
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                //instant lyric
                instantLyricStatus.setOnPreferenceChangeListener(OnPreferenceChangeListener({ preference: Preference?, newValue: Any ->
                    val pos_text: String
                    if ((newValue as Boolean)) {
                        pos_text = getString(R.string.turn_on)
                    } else {
                        pos_text = getString(R.string.turn_off)
                    }
                    val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                        .title(R.string.instant_lyrics_title)
                        .content(R.string.instant_lyrics_content)
                        .positiveText(pos_text)
                        .negativeText(getString(R.string.cancel))
                        .onPositive({ dialog1, which ->
                            if (newValue) {
                                val intent: Intent =
                                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                startActivity(intent)
                                Toast.makeText(MyApp.Companion.getContext(),
                                    "Click on AB Music to enable!",
                                    Toast.LENGTH_LONG).show()
                            } else {
                                val intent: Intent =
                                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                startActivity(intent)
                                Toast.makeText(MyApp.Companion.getContext(),
                                    "Click on AB Music to disable!",
                                    Toast.LENGTH_LONG).show()
                            }
                        })
                        .build()
                    dialog.show()
                    false
                }))
            } else {
                instantLyricStatus.setOnPreferenceChangeListener(OnPreferenceChangeListener({ preference: Preference?, newValue: Any? ->
                    Toast.makeText(getActivity(),
                        "Feature is only available on Jelly Bean MR2 and above!",
                        Toast.LENGTH_LONG).show()
                    false
                }))
            }

            //shake
            val shakeAction: Preference = findPreference(getString(R.string.pref_shake_action))
            val shakeActionRead: Int = MyApp.Companion.getPref()
                .getInt(getString(R.string.pref_shake_action), Constants.SHAKE_ACTIONS.NEXT)
            if (shakeActionRead == Constants.SHAKE_ACTIONS.NEXT) {
                shakeAction.setSummary(NEXT)
            } else if (shakeActionRead == Constants.SHAKE_ACTIONS.PLAY_PAUSE) {
                shakeAction.setSummary(PLAY_PAUSE)
            } else {
                shakeAction.setSummary(PREVIOUS)
            }
            shakeAction.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                //open browser or intent here
                ShakeActionDialog()
                true
            }))


            //hide short clips preference
            val hideShortClipsPref: Preference =
                findPreference(getString(R.string.pref_hide_short_clips))
            val summary: String =
                MyApp.Companion.getPref().getInt(getString(R.string.pref_hide_short_clips), 10)
                    .toString() + " seconds"
            hideShortClipsPref.setSummary(summary)
            hideShortClipsPref.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                //open browser or intent here
                shortClipDialog()
                true
            }))

            //excluded folders preference
            val excludedFoldersPref: Preference =
                findPreference(getString(R.string.pref_excluded_folders))
            excludedFoldersPref.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                //open browser or intent here
                displayExcludedFolders()
                true
            }))
            val hideByStartPref: Preference =
                findPreference(getString(R.string.pref_hide_tracks_starting_with))
            val text1: String = MyApp.Companion.getPref()
                .getString(getString(R.string.pref_hide_tracks_starting_with_1), "")
            val text2: String = MyApp.Companion.getPref()
                .getString(getString(R.string.pref_hide_tracks_starting_with_2), "")
            val text3: String = MyApp.Companion.getPref()
                .getString(getString(R.string.pref_hide_tracks_starting_with_3), "")
            hideByStartPref.setSummary(text1 + ", " + text2 + ", " + text3)
            hideByStartPref.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                //open browser or intent here
                hideByStartDialog()
                true
            }))


            //opening tab preference
            val openingTabPref: Preference = findPreference(getString(R.string.pref_opening_tab))
            openingTabPref.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                //open browser or intent here
                tabSeqDialog()
                true
            }))


            //about us  preference
            val aboutUs: Preference = findPreference(getString(R.string.pref_about_us))
            aboutUs.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                //open browser or intent here
                getActivity().startActivity(Intent(getActivity(), ActivityAboutUs::class.java))
                true
            }))

            //cache artist data
            /*Preference cacheArtistDataPref = findPreference(getString(R.string.pref_cache_artist_data));
            Long lastTimeDidAt = MyApp.getPref().getLong(getString(R.string.pref_artist_cache_manual),0);
            if (System.currentTimeMillis() >= lastTimeDidAt +
                    (2 * 60 * 60 * 1000)) {
                cacheArtistDataPref.setEnabled(true);
            }
            cacheArtistDataPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    preference.setEnabled(false);
                    MyApp.getPref().edit().putLong(getString(R.string.pref_artist_cache_manual), System.currentTimeMillis()).apply();
                    new BulkArtInfoGrabber().start();
                    Toast.makeText(MyApp.getContext(), "Artist info local caching started in background, will be finished shortly!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });*/

            //batch download  preference
            val batchDownload: Preference = findPreference(getString(R.string.pref_batch_download))
            batchDownload.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                if (MyApp.Companion.isBatchServiceRunning) {
                    Toast.makeText(getActivity(),
                        getString(R.string.error_batch_download_running),
                        Toast.LENGTH_LONG).show()
                    return@setOnPreferenceClickListener false
                }
                getActivity().startService(Intent(getActivity(),
                    BatchDownloaderService::class.java))
                Toast.makeText(getActivity(),
                    getString(R.string.batch_download_started),
                    Toast.LENGTH_LONG).show()
                true
            }))

            //reset  preference
            val resetPref: Preference = findPreference(getString(R.string.pref_reset_pref))
            resetPref.setOnPreferenceClickListener(OnPreferenceClickListener({ preference: Preference? ->
                resetPrefDialog()
                true
            }))
        }

        private fun albumViewDialog() {
            val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                .title(getString(R.string.title_album_lib_view))
                .items(arrayOf(LIST, GRID) as Array<CharSequence>?)
                .itemsCallback(MaterialDialog.ListCallback({ dialog1, view, which, text ->
                    when (text.toString()) {
                        LIST -> {
                            MyApp.Companion.getPref().edit()
                                .putBoolean(getString(R.string.pref_album_lib_view), false).apply()
                            findPreference(getString(R.string.pref_album_lib_view)).setSummary(LIST)
                        }
                        GRID -> {
                            MyApp.Companion.getPref().edit()
                                .putBoolean(getString(R.string.pref_album_lib_view), true).apply()
                            findPreference(getString(R.string.pref_album_lib_view)).setSummary(GRID)
                        }
                    }
                }) as MaterialDialog.ListCallback?)
                .build()
            dialog.show()
        }

        private fun navBackDialog() {
            ///get current setting
            // 0 - System default   2 - custom
            val currentSelection: Int =
                MyApp.Companion.getPref().getInt(getString(R.string.pref_nav_library_back), 0)
            val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                .title(R.string.title_nav_back)
                .items(R.array.nav_back_pref_array)
                .itemsCallbackSingleChoice(currentSelection, { dialog1, view, which, text ->
                    when (which) {
                        0 -> MyApp.Companion.getPref().edit()
                            .putInt(getString(R.string.pref_nav_library_back), which).apply()
                        1 -> {
                            backgroundSelectionStatus = NAVIGATION_DRAWER
                            CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(11, 16)
                                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                                .setOutputCompressQuality(80)
                                .start(getActivity())
                            dialog1.dismiss()
                        }
                    }
                    true
                })
                .positiveText(R.string.okay)
                .build()
            dialog.show()
        }

        private fun defAlbumArtDialog() {
            ///get current setting
            // 0 - System default   2 - custom
            val currentSelection: Int =
                MyApp.Companion.getPref().getInt(getString(R.string.pref_default_album_art), 0)
            val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                .title(R.string.nav_default_album_art)
                .items(R.array.def_album_art_pref_array)
                .itemsCallbackSingleChoice(currentSelection, { dialog1, view, which, text ->
                    when (which) {
                        0 -> MyApp.Companion.getPref().edit()
                            .putInt(getString(R.string.pref_default_album_art), which).apply()
                        1 -> {
                            backgroundSelectionStatus = DEFAULT_ALBUM_ART
                            CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1, 1)
                                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                                .setOutputCompressQuality(80)
                                .start(getActivity())
                            dialog1.dismiss()
                        }
                    }
                    true
                })
                .positiveText(R.string.okay)
                .build()
            dialog.show()
        }

        private fun mainLibBackDialog() {
            ///get current setting
            // 0 - System default   2 - custom
            val currentSelection: Int =
                MyApp.Companion.getPref().getInt(getString(R.string.pref_main_library_back), 0)
            val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                .title(R.string.title_main_library_back)
                .items(R.array.main_lib_back_pref_array)
                .itemsCallbackSingleChoice(currentSelection, { dialog1, view, which, text ->
                    when (which) {
                        0 -> MyApp.Companion.getPref().edit()
                            .putInt(getString(R.string.pref_main_library_back), which).apply()
                        1 -> {
                            backgroundSelectionStatus = MAIN_LIB
                            CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(11, 16)
                                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                                .setOutputCompressQuality(50)
                                .start(getActivity())
                            dialog1.dismiss()
                        }
                    }
                    true
                })
                .positiveText(R.string.okay)
                .build()
            dialog.show()
        }

        private fun nowPlayingBackDialog() {

            ///get current setting
            // 0 - System default   1 - artist image  2 - album art 3 - custom  4- custom (if Artist image unavailable)
            val currentSelection: Int =
                MyApp.Companion.getPref().getInt(getString(R.string.pref_now_playing_back), 1)
            val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                .title(R.string.title_now_playing_back)
                .items(R.array.now_playing_back_pref_array)
                .itemsCallbackSingleChoice(currentSelection, { dialog1, view, which, text ->
                    when (which) {
                        0, 1, 2 -> MyApp.Companion.getPref().edit()
                            .putInt(getString(R.string.pref_now_playing_back), which).apply()
                        3 -> {
                            backgroundSelectionStatus = NOW_PLAYING
                            CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(9, 16)
                                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                                .setOutputCompressQuality(50)
                                .start(getActivity())
                            dialog1.dismiss()
                        }
                    }
                    true
                })
                .positiveText(R.string.okay)
                .build()
            dialog.show()
        }

        private fun displayExcludedFolders() {
            val excludedFoldersString: String =
                MyApp.Companion.getPref().getString(getString(R.string.pref_excluded_folders), "")
            val excludedFolders: Array<String> =
                excludedFoldersString.split(",".toRegex()).toTypedArray()
            val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                .title(R.string.title_excluded_folders)
                .items(excludedFolders)
                .positiveText(getString(R.string.add))
                .onPositive({ dialog12, which ->
                    dialog12.dismiss()
                    MyDialogBuilder(getActivity())
                        .title(getString(R.string.title_how_to_add))
                        .content(getString(R.string.content_how_to_add))
                        .positiveText(getString(R.string.pos_how_to_add))
                        .show()
                })
                .negativeText(getString(R.string.reset))
                .onNegative({ dialog1, which ->
                    MyApp.Companion.getPref().edit()
                        .putString(getString(R.string.pref_excluded_folders), "").apply()
                    MusicLibrary.getInstance().RefreshLibrary()
                    Toast.makeText(getActivity(),
                        "Excluded folders reset, refreshing Music Library..",
                        Toast.LENGTH_SHORT).show()
                })
                .build()
            dialog.show()
        }

        private fun tabSeqDialog() {
            val inflater: LayoutInflater = getActivity().getLayoutInflater()
            val dialogView: View = inflater.inflate(R.layout.tab_sequence_preference, null)
            val rv: RecyclerView = dialogView.findViewById(R.id.rv_for_tab_sequence)
            val tsa: TabSequenceAdapter = TabSequenceAdapter(this)
            rv.setAdapter(tsa)
            rv.setLayoutManager(WrapContentLinearLayoutManager(MyApp.Companion.getContext()))
            val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(tsa)
            mItemTouchHelper = ItemTouchHelper(callback)
            mItemTouchHelper.attachToRecyclerView(rv)
            val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                .title(getString(R.string.setting_tab_seqe_title))
                .customView(dialogView, false)
                .dismissListener({ dialog1 ->
                    val temp: IntArray = tsa.getData()
                    val str: StringBuilder = StringBuilder()
                    for (aTemp: Int in temp) {
                        str.append(aTemp).append(",")
                    }
                    MyApp.Companion.getPref().edit()
                        .putString(getString(R.string.pref_tab_seq), str.toString()).apply()
                })
                .build()
            dialog.show()
        }

        private fun themeSelectionDialog() {
            val inflater: LayoutInflater = getActivity().getLayoutInflater()
            val dialogView: View = inflater.inflate(R.layout.theme_selector_dialog, null)
            val rv: RecyclerView = dialogView.findViewById(R.id.rv_for_theme_selector)
            val tsa: ThemeSelectorAdapter = ThemeSelectorAdapter()
            rv.setAdapter(tsa)
            val layoutManager: FlexboxLayoutManager = FlexboxLayoutManager(getActivity())
            layoutManager.setFlexDirection(FlexDirection.ROW)
            layoutManager.setJustifyContent(JustifyContent.SPACE_EVENLY)
            rv.setLayoutManager(layoutManager)
            //rv.setLayoutManager(new GridLayoutManager(getActivity(), 4));
            val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                .title("Select theme")
                .customView(dialogView, false)
                .dismissListener({ dialog12 -> })
                .positiveText("Apply")
                .onPositive({ dialog1, which -> restartSettingsActivity() })
                .build()
            dialog.show()
        }

        private fun RescanLibrary() {
            MusicLibrary.getInstance().RefreshLibrary()
            val dialog: ProgressDialog = ProgressDialog.show(getActivity(), "",
                getString(R.string.library_rescan), true)
            Executors.newSingleThreadExecutor().execute(Runnable({
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                dialog.dismiss()
                getActivity().runOnUiThread(Runnable({
                    Toast.makeText(getActivity(),
                        getString(R.string.main_act_lib_refreshed),
                        Toast.LENGTH_SHORT).show()
                }))
            }))
        }

        private fun shortClipDialog() {
            val linear: LinearLayout = LinearLayout(getActivity())
            linear.setOrientation(LinearLayout.VERTICAL)
            val text: TextView = TextView(getActivity())
            val summary: String =
                MyApp.Companion.getPref().getInt(getString(R.string.pref_hide_short_clips), 10)
                    .toString() + " seconds"
            text.setText(summary)
            text.setTypeface(TypeFaceHelper.getTypeFace(MyApp.Companion.getContext()))
            text.setPadding(0, 10, 0, 0)
            text.setGravity(Gravity.CENTER)
            val seek: SeekBar = SeekBar(getActivity())
            seek.setPadding(40, 10, 40, 10)
            seek.setMax(100)
            seek.setProgress(MyApp.Companion.getPref()
                .getInt(getString(R.string.pref_hide_short_clips), 10))
            seek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                public override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    text.setText(progress.toString() + " seconds")
                }

                public override fun onStartTrackingTouch(seekBar: SeekBar) {}
                public override fun onStopTrackingTouch(seekBar: SeekBar) {
                    val progress: Int = seekBar.getProgress()
                    MyApp.Companion.getPref().edit()
                        .putInt(getString(R.string.pref_hide_short_clips), progress).apply()
                    findPreference(getString(R.string.pref_hide_short_clips)).setSummary(progress.toString() + " seconds")
                }
            })
            linear.addView(seek)
            linear.addView(text)
            val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                .title(getString(R.string.title_hide_short_clips))
                .positiveText(getString(R.string.okay))
                .negativeText(getString(R.string.cancel))
                .onPositive({ dialog1, which -> RescanLibrary() })
                .customView(linear, false)
                .build()
            dialog.show()
        }

        private fun hideByStartDialog() {
            val text1: String = MyApp.Companion.getPref()
                .getString(getString(R.string.pref_hide_tracks_starting_with_1), "")
            val text2: String = MyApp.Companion.getPref()
                .getString(getString(R.string.pref_hide_tracks_starting_with_2), "")
            val text3: String = MyApp.Companion.getPref()
                .getString(getString(R.string.pref_hide_tracks_starting_with_3), "")
            findPreference(getString(R.string.pref_hide_tracks_starting_with)).setSummary(text1 + ", " + text2 + ", " + text3)
            val linear: LinearLayout = LinearLayout(getActivity())
            linear.setPadding(10, 10, 10, 0)
            val myEditText1: EditText = EditText(getActivity()) // Pass it an Activity or Context
            myEditText1.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)) // Pass two args; must be LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, or an integer pixel value.
            myEditText1.setText(text1)
            //myEditText1.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            myEditText1.setInputType(InputType.TYPE_CLASS_TEXT)
            myEditText1.setMaxLines(1)
            linear.addView(myEditText1)
            val myEditText2: EditText = EditText(getActivity()) // Pass it an Activity or Context
            myEditText2.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)) // Pass two args; must be LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, or an integer pixel value.
            myEditText2.setText(text2)
            // myEditText2.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            myEditText2.setMaxLines(1)
            myEditText2.setInputType(InputType.TYPE_CLASS_TEXT)
            linear.addView(myEditText2)
            val myEditText3: EditText = EditText(getActivity()) // Pass it an Activity or Context
            myEditText3.setLayoutParams(LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)) // Pass two args; must be LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, or an integer pixel value.
            myEditText3.setText(text3)
            //myEditText3.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            myEditText3.setInputType(InputType.TYPE_CLASS_TEXT)
            myEditText3.setMaxLines(1)
            linear.addView(myEditText3)
            val tv: TextView = TextView(getActivity())
            tv.setText(getString(R.string.case_sensitive_text))
            tv.setTypeface(TypeFaceHelper.getTypeFace(MyApp.Companion.getContext()))
            tv.setPadding(0, 10, 0, 0)
            linear.addView(tv)
            linear.setOrientation(LinearLayout.VERTICAL)
            val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                .title(getString(R.string.title_hide_tracks_starting_with))
                .positiveText(getString(R.string.okay))
                .negativeText(getString(R.string.cancel))
                .onPositive({ dialog1, which ->
                    val text11: String = myEditText1.getText().toString().trim({ it <= ' ' })
                    MyApp.Companion.getPref().edit()
                        .putString(getString(R.string.pref_hide_tracks_starting_with_1), text11)
                        .apply()
                    val text21: String = myEditText2.getText().toString().trim({ it <= ' ' })
                    MyApp.Companion.getPref().edit()
                        .putString(getString(R.string.pref_hide_tracks_starting_with_2), text21)
                        .apply()
                    val text31: String = myEditText3.getText().toString().trim({ it <= ' ' })
                    MyApp.Companion.getPref().edit()
                        .putString(getString(R.string.pref_hide_tracks_starting_with_3), text31)
                        .apply()
                    findPreference(getString(R.string.pref_hide_tracks_starting_with)).setSummary(
                        text11 + ", " + text21 + ", " + text31)
                    RescanLibrary()
                })
                .customView(linear, true)
                .build()
            dialog.show()
        }

        private fun ShakeActionDialog() {
            val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                .title(getString(R.string.title_shake_action))
                .items(arrayOf(NEXT, PLAY_PAUSE, PREVIOUS) as Array<CharSequence>?)
                .itemsCallback(MaterialDialog.ListCallback({ dialog1, view, which, text ->
                    when (text.toString()) {
                        NEXT -> {
                            MyApp.Companion.getPref().edit()
                                .putInt(getString(R.string.pref_shake_action),
                                    Constants.SHAKE_ACTIONS.NEXT).apply()
                            findPreference(getString(R.string.pref_shake_action)).setSummary(NEXT)
                        }
                        PLAY_PAUSE -> {
                            MyApp.Companion.getPref().edit()
                                .putInt(getString(R.string.pref_shake_action),
                                    Constants.SHAKE_ACTIONS.PLAY_PAUSE).apply()
                            findPreference(getString(R.string.pref_shake_action)).setSummary(
                                PLAY_PAUSE)
                        }
                        PREVIOUS -> {
                            MyApp.Companion.getPref().edit()
                                .putInt(getString(R.string.pref_shake_action),
                                    Constants.SHAKE_ACTIONS.PREVIOUS).apply()
                            findPreference(getString(R.string.pref_shake_action)).setSummary(
                                PREVIOUS)
                        }
                    }
                }) as MaterialDialog.ListCallback?)
                .build()
            dialog.show()
        }

        private fun resetPrefDialog() {
            val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                .title(getString(R.string.title_reset_pref) + " ?") // .content(getString(R.string.lyric_art_info_content))
                .positiveText(getString(R.string.yes))
                .negativeText(getString(R.string.cancel))
                .onPositive({ dialog1, which ->
                    val editor: SharedPreferences.Editor = MyApp.Companion.getPref().edit()
                    editor.putInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.GLOSSY)
                    editor.putInt(getString(R.string.pref_text_font), Constants.TYPEFACE.SOFIA)
                    editor.remove(getString(R.string.pref_tab_seq))
                    editor.putBoolean(getString(R.string.pref_lock_screen_album_Art), true)
                    editor.putBoolean(getString(R.string.pref_shake), false)
                    editor.putInt(getString(R.string.pref_hide_short_clips), 10)
                    editor.putString(getString(R.string.pref_hide_tracks_starting_with_1), "")
                    editor.putString(getString(R.string.pref_hide_tracks_starting_with_2), "")
                    editor.putString(getString(R.string.pref_hide_tracks_starting_with_3), "")
                    editor.putString(getString(R.string.pref_excluded_folders), "")
                    editor.putBoolean(getString(R.string.pref_prefer_system_equ), true)
                    editor.putInt(getString(R.string.pref_main_library_back), 0)
                    editor.putInt(getString(R.string.pref_now_playing_back), 0)
                    editor.putBoolean(getString(R.string.pref_hide_lock_button), false)
                    editor.putBoolean(getString(R.string.pref_notifications), true)
                    editor.putBoolean(getString(R.string.pref_continuous_playback), false)
                    editor.putBoolean(getString(R.string.pref_data_saver), false)
                    editor.apply()
                    restartSettingsActivity()
                })
                .build()
            dialog.show()
        }

        private fun fontPrefSelectionDialog() {
            val dialog: MaterialDialog = MyDialogBuilder(getActivity())
                .title(getString(R.string.title_text_font))
                .items(arrayOf(MANROPE,
                    ROBOTO,
                    ASAP,
                    SOFIA,
                    MONOSPACE,
                    SYSTEM_DEFAULT) as Array<CharSequence>?)
                .itemsCallback(MaterialDialog.ListCallback({ dialog1, view, which, text ->
                    when (text.toString()) {
                        MANROPE -> {
                            MyApp.Companion.getPref().edit()
                                .putInt(getString(R.string.pref_text_font),
                                    Constants.TYPEFACE.MANROPE).apply()
                            findPreference(getString(R.string.pref_text_font)).setSummary(MANROPE)
                        }
                        ROBOTO -> {
                            MyApp.Companion.getPref().edit()
                                .putInt(getString(R.string.pref_text_font),
                                    Constants.TYPEFACE.ROBOTO).apply()
                            findPreference(getString(R.string.pref_text_font)).setSummary(ROBOTO)
                        }
                        MONOSPACE -> {
                            MyApp.Companion.getPref().edit()
                                .putInt(getString(R.string.pref_text_font),
                                    Constants.TYPEFACE.MONOSPACE).apply()
                            findPreference(getString(R.string.pref_text_font)).setSummary(MONOSPACE)
                        }
                        ASAP -> {
                            MyApp.Companion.getPref().edit()
                                .putInt(getString(R.string.pref_text_font), Constants.TYPEFACE.ASAP)
                                .apply()
                            findPreference(getString(R.string.pref_text_font)).setSummary(ASAP)
                        }
                        SOFIA -> {
                            MyApp.Companion.getPref().edit()
                                .putInt(getString(R.string.pref_text_font),
                                    Constants.TYPEFACE.SOFIA).apply()
                            findPreference(getString(R.string.pref_text_font)).setSummary(SOFIA)
                        }
                        SYSTEM_DEFAULT -> {
                            MyApp.Companion.getPref().edit()
                                .putInt(getString(R.string.pref_text_font),
                                    Constants.TYPEFACE.SYSTEM_DEFAULT).apply()
                            findPreference(getString(R.string.pref_text_font)).setSummary(
                                SYSTEM_DEFAULT)
                        }
                    }
                    MyApp.Companion.getPref().edit()
                        .putBoolean(getString(R.string.pref_font_already_logged), false).apply()
                    val path: String? = TypeFaceHelper.getTypeFacePath()
                    if (path != null) {
                        ViewPump.init(ViewPump.builder()
                            .addInterceptor(CalligraphyInterceptor(
                                CalligraphyConfig.Builder()
                                    .setDefaultFontPath(path)
                                    .setFontAttrId(R.attr.fontPath)
                                    .build()))
                            .build())
                    }
                    restartSettingsActivity()
                }) as MaterialDialog.ListCallback?)
                .build()
            dialog.show()
        }

        private fun restartSettingsActivity() {
            val intent: Intent = getActivity().getIntent()
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra("ad", false)
            getActivity().finish()
            startActivity(intent)
        }

        fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
            mItemTouchHelper.startDrag(viewHolder)
        }
    }

    private class TabSequenceAdapter internal constructor(dragStartListener: OnStartDragListener) :
        RecyclerView.Adapter<TabSequenceAdapter.MyViewHolder?>(), ItemTouchHelperAdapter {
        var data: IntArray = IntArray(Constants.TABS.NUMBER_OF_TABS)
        private val mDragStartListener: OnStartDragListener
        public override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val inflater: LayoutInflater = LayoutInflater.from(MyApp.Companion.getContext())
            val view: View = inflater.inflate(R.layout.tab_sequence_item, parent, false)
            return MyViewHolder(view)
        }

        @SuppressLint("ClickableViewAccessibility")
        public override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            //holder.title.setText(data.get(0));
            when (data.get(position)) {
                Constants.TABS.ALBUMS -> holder.title.setText(MyApp.Companion.getContext()
                    .getString(R.string.tab_album))
                Constants.TABS.ARTIST -> holder.title.setText(MyApp.Companion.getContext()
                    .getString(R.string.tab_artist))
                Constants.TABS.FOLDER -> holder.title.setText(MyApp.Companion.getContext()
                    .getString(R.string.tab_folder))
                Constants.TABS.GENRE -> holder.title.setText(MyApp.Companion.getContext()
                    .getString(R.string.tab_genre))
                Constants.TABS.PLAYLIST -> holder.title.setText(MyApp.Companion.getContext()
                    .getString(R.string.tab_playlist))
                Constants.TABS.TRACKS -> holder.title.setText(MyApp.Companion.getContext()
                    .getString(R.string.tab_track))
            }
            holder.handle.setOnTouchListener(OnTouchListener({ view: View?, motionEvent: MotionEvent? ->
                if (MotionEventCompat.getActionMasked(motionEvent) ==
                    MotionEvent.ACTION_DOWN
                ) {
                    mDragStartListener.onStartDrag(holder)
                }
                false
            }))
        }

        public override fun getItemCount(): Int {
            return data.size
        }

        fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
            val temp: Int = data.get(fromPosition)
            data.get(fromPosition) = data.get(toPosition)
            data.get(toPosition) = temp
            notifyItemMoved(fromPosition, toPosition)
            return true
        }

        fun onItemDismiss(position: Int) {
            notifyItemChanged(position)
        }

        fun getData(): IntArray {
            return data
        }

        internal class MyViewHolder constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var title: TextView
            var handle: ImageView

            init {
                title = itemView.findViewById<TextView>(R.id.tab_name)
                title.setTypeface(TypeFaceHelper.getTypeFace(MyApp.Companion.getContext()))
                //title.setTypeface(TypeFaceHelper.getTypeFace());
                handle = itemView.findViewById<ImageView>(R.id.handle_for_drag)
            }
        }

        init {
            mDragStartListener = dragStartListener
            val savedTabSeq: String = MyApp.Companion.getPref()
                .getString(MyApp.Companion.getContext().getString(R.string.pref_tab_seq),
                    Constants.TABS.DEFAULT_SEQ)
            val st: StringTokenizer = StringTokenizer(savedTabSeq, ",")
            for (i in 0 until Constants.TABS.NUMBER_OF_TABS) {
                data.get(i) = st.nextToken().toInt()
            }
        }
    }

    private class ThemeSelectorAdapter internal constructor() :
        RecyclerView.Adapter<ThemeSelectorAdapter.MyViewHolder?>() {
        private var currentSelectedItem: Int
        public override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val inflater: LayoutInflater = LayoutInflater.from(MyApp.Companion.getContext())
            val view: View = inflater.inflate(R.layout.theme_selection_item, parent, false)
            return MyViewHolder(view)
        }

        public override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.view.setBackgroundDrawable(ColorHelper.getGradientDrawable(position))
            holder.view.setOnClickListener(View.OnClickListener({ view: View? ->
                currentSelectedItem = holder.getAdapterPosition()
                MyApp.Companion.setSelectedThemeId(holder.getAdapterPosition())
                notifyDataSetChanged()
            }))
            if (currentSelectedItem == position) {
                holder.tick.setVisibility(View.VISIBLE)
            } else {
                holder.tick.setVisibility(View.INVISIBLE)
            }
        }

        public override fun getItemCount(): Int {
            return ColorHelper.getNumberOfThemes()
        }

        internal class MyViewHolder constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var view: View
            var tick: View

            init {
                view = itemView.findViewById(R.id.themeView)
                tick = itemView.findViewById(R.id.tick)
            }
        }

        init {
            currentSelectedItem = MyApp.Companion.getSelectedThemeId()
        }
    }

    private class WrapContentLinearLayoutManager internal constructor(context: Context?) :
        LinearLayoutManager(context) {
        //... constructor
        public override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
            try {
                super.onLayoutChildren(recycler, state)
            } catch (e: IndexOutOfBoundsException) {
                Log.e("probe", "meet a IOOBE in RecyclerView")
            }
        }
    }

    companion object {
        private val MAIN_LIB: Int = 0
        private val NOW_PLAYING: Int = 1
        private val NAVIGATION_DRAWER: Int = 2
        private val DEFAULT_ALBUM_ART: Int = 3
        private var backgroundSelectionStatus: Int = -1
    }
}