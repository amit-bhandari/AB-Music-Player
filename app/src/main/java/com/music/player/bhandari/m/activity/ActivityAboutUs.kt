package com.music.player.bhandari.m.activity

import android.R
import android.content.Context
import android.os.Build
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.afollestad.materialdialogs.DialogAction
import com.music.player.bhandari.m.model.Constants

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
class ActivityAboutUs constructor() : AppCompatActivity() {
    private val SITE_URL: String = "http://www.thetechguru.in/ab_music"
    protected override fun onCreate(savedInstanceState: Bundle?) {
        //if player service not running, kill the app
        if (MyApp.Companion.getService() == null) {
            UtilityFun.restartApp()
        }
        ColorHelper.setStatusBarGradiant(this)
        val themeSelector: Int = MyApp.Companion.getPref()
            .getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        val fab: FloatingActionButton = findViewById<FloatingActionButton>(R.id.fb_fab)
        fab.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                open_url(FB_URL)
            }
        })
        val site_link: TextView = findViewById<TextView>(R.id.website_link)
        val spanWebsite: SpannableString = SpannableString(site_link.getText())
        val clickableSpanWebsite: ClickableSpan = object : ClickableSpan() {
            public override fun onClick(textView: View) {
                open_url(SITE_URL)
            }

            public override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.setUnderlineText(true)
                ds.setTypeface(Typeface.create(ds.getTypeface(), Typeface.BOLD))
            }
        }
        spanWebsite.setSpan(clickableSpanWebsite,
            0,
            site_link.getText().length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        site_link.setText(spanWebsite)
        site_link.setMovementMethod(LinkMovementMethod.getInstance())

        //findViewById(R.id.root_view_about_us).setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());
        val pInfo: PackageInfo
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0)
            val version: String = pInfo.versionName
            (findViewById<View>(R.id.version) as TextView).setText(version)
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }
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
        }*/setTitle(getString(R.string.title_about_us))
        try {
            val bundle: Bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "about_us_launched")
            UtilityFun.logEvent(bundle)
        } catch (ignored: Exception) {
        }
    }

    protected override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    private fun open_url(url: String) {
        try {
            val browserIntent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        } catch (ignored: Exception) {
            Toast.makeText(this@ActivityAboutUs,
                getString(R.string.error_opening_browser),
                Toast.LENGTH_SHORT).show()
        }
    }

    public override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY -> MyApp.Companion.getService()
                .play()
            KeyEvent.KEYCODE_MEDIA_NEXT -> MyApp.Companion.getService().nextTrack()
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> MyApp.Companion.getService().prevTrack()
            KeyEvent.KEYCODE_MEDIA_STOP -> MyApp.Companion.getService().stop()
            KeyEvent.KEYCODE_BACK -> onBackPressed()
        }
        return false
    }

    protected override fun onResume() {
        MyApp.Companion.isAppVisible = true
        super.onResume()
    }

    protected override fun onPause() {
        MyApp.Companion.isAppVisible = false
        super.onPause()
    }

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.menu_about, menu)
        return true
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> finish()
            R.id.action_feedback -> {
                val myDeviceModel: String = Build.MODEL
                val emailIntent: Intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", getString(R.string.au_email_id), null))
                val address: Array<String> = arrayOf(getString(R.string.au_email_id))
                emailIntent.putExtra(Intent.EXTRA_EMAIL, address)
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for " + myDeviceModel)
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello AndroidDevs, \n")
                startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
            }
            R.id.action_support_dev -> selectDonateDialog()
            R.id.action_licenses ->                 /*new LibsBuilder()
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withActivityTitle("Libraries")
                        .start(this);*/startActivity(Intent(this, ActivityLicenses::class.java))
            R.id.action_tou -> showDisclaimerDialog()
            R.id.nav_website -> try {
                val browserIntent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE))
                startActivity(browserIntent)
            } catch (e: Exception) {
                Toast.makeText(this@ActivityAboutUs,
                    getString(R.string.error_opening_browser),
                    Toast.LENGTH_SHORT).show()
            }
            R.id.nav_call_for_help -> callForHelpDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun callForHelpDialog() {
        val dialog: MaterialDialog = MyDialogBuilder(this)
            .title("AB Music needs your help!")
            .content(("As AB Music grows bigger and reach more audience, its language support also needs to be widened. \n\n As Catelyn of House Stark once said, " +
                    "In the name of King Robert and good lords you serve, I call upon you to seize the opportunity to contribute and help me translate " +
                    "AB Music to your language!"))
            .positiveText("Sure")
            .negativeText("Nah, I don't want to")
            .onPositive(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    try {
                        val browserIntent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                            TRANSLATION_HELP_WEBSITE))
                        startActivity(browserIntent)
                    } catch (e: Exception) {
                        Toast.makeText(this@ActivityAboutUs,
                            getString(R.string.error_opening_browser),
                            Toast.LENGTH_SHORT).show()
                    }
                }
            })
            .build()

        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
        dialog.show()
    }

    protected override fun onDestroy() {
        super.onDestroy()
    }

    private fun selectDonateDialog() {
        val dialog: MaterialDialog = MyDialogBuilder(this)
            .title(getString(R.string.about_us_support_dev_title))
            .content(getString(R.string.about_us_support_dev_content))
            .positiveText(getString(R.string.about_us_support_dev_pos))
            .negativeText(getString(R.string.about_us_support_dev_neg))
            .neutralText(getString(R.string.about_us_support_dev_neu))
            .onPositive(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    val intent: Intent =
                        Intent(this@ActivityAboutUs, ActivityDonateFunds::class.java)
                    intent.putExtra("donate_type", Constants.DONATE.COFFEE)
                    startActivity(intent)
                }
            }).onNeutral(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    val intent: Intent =
                        Intent(this@ActivityAboutUs, ActivityDonateFunds::class.java)
                    intent.putExtra("donate_type", Constants.DONATE.JD)
                    startActivity(intent)
                }
            })
            .onNegative(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    val intent: Intent =
                        Intent(this@ActivityAboutUs, ActivityDonateFunds::class.java)
                    intent.putExtra("donate_type", Constants.DONATE.BEER)
                    startActivity(intent)
                }
            })
            .build()

        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
        dialog.show()
    }

    private fun showDisclaimerDialog() {
        val dialog: MaterialDialog = MyDialogBuilder(this)
            .title(getString(R.string.lyrics_disclaimer_title))
            .content(getString(R.string.lyrics_disclaimer_content))
            .positiveText(getString(R.string.lyrics_disclaimer_title_pos))
            .negativeText(getString(R.string.lyrics_disclaimer_title_neg))
            .onPositive(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    MyApp.Companion.getPref().edit()
                        .putBoolean(getString(R.string.pref_disclaimer_accepted), true).apply()
                }
            }).onNegative(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    MyApp.Companion.getPref().edit()
                        .putBoolean(getString(R.string.pref_disclaimer_accepted), false).apply()
                }
            })
            .build()

        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
        dialog.show()
    }

    companion object {
        private val FB_URL: String = "http://www.facebook.com/abmusicoffline/"
        val WEBSITE: String = "http://www.thetechguru.in"
        val TRANSLATION_HELP_WEBSITE: String =
            "https://github.com/Amit AB-bhandari/AB-Music-Translations"
    }
}