package com.music.player.bhandari.m.activity

import android.content.Context
import android.view.View
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
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
class ActivityRequestNotificationAccess constructor() : AppCompatActivity(), View.OnClickListener {
    @BindView(R.id.text_never_ask)
    var never_ask: TextView? = null

    @BindView(R.id.text_skip)
    var skip: TextView? = null

    @BindView(R.id.progressBar)
    var progressBar: ProgressBar? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        ColorHelper.setStatusBarGradiant(this)
        val themeSelector: Int = MyApp.Companion.getPref()
            .getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        setContentView(R.layout.activity_request_notification_access)
        ButterKnife.bind(this)

        //findViewById(R.id.root_view_request_notification_access).setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());
        findViewById<View>(R.id.request_button).setOnClickListener(this)
        skip.setOnClickListener(this)
        never_ask.setOnClickListener(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility((
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN))
        }
        super.onCreate(savedInstanceState)
    }

    protected override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    protected override fun onResume() {
        super.onResume()
        if (NotificationListenerService.isListeningAuthorized(this)) {
            launchMainActivity()
        }
    }

    public override fun onClick(view: View) {
        when (view.getId()) {
            R.id.request_button -> {
                val intent: Intent =
                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                startActivity(intent)
                Toast.makeText(this, "Click on AB Music to enable!", Toast.LENGTH_LONG).show()
            }
            R.id.text_skip -> {
                launchMainActivity()
                skip.setVisibility(View.GONE)
                never_ask.setVisibility(View.GONE)
                progressBar.setVisibility(View.VISIBLE)
            }
            R.id.text_never_ask -> {
                never_ask.setVisibility(View.GONE)
                progressBar.setVisibility(View.VISIBLE)
                MyApp.Companion.getPref().edit()
                    .putBoolean(getString(R.string.pref_never_ask_notitication_permission), true)
                    .apply()
                launchMainActivity()
            }
        }
    }

    private fun launchMainActivity() {
        val mainActIntent: Intent = Intent(this, ActivityMain::class.java)
        startActivity(mainActIntent)
        finish()
    }
}