package com.music.player.bhandari.m.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.service.NotificationListenerService
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
class ActivityRequestNotificationAccess : AppCompatActivity(), View.OnClickListener {
    @BindView(R.id.text_never_ask)
    var never_ask: TextView? = null

    @BindView(R.id.text_skip)
    var skip: TextView? = null

    @BindView(R.id.progressBar)
    var progressBar: ProgressBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        ColorHelper.setStatusBarGradiant(this)
        when (MyApp.getPref()!!.getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        setContentView(R.layout.activity_request_notification_access)
        ButterKnife.bind(this)

        //findViewById(R.id.root_view_request_notification_access).setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());
        findViewById<View>(R.id.request_button).setOnClickListener(this)
        skip!!.setOnClickListener(this)
        never_ask!!.setOnClickListener(this)
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        super.onCreate(savedInstanceState)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onResume() {
        super.onResume()
        if (NotificationListenerService.isListeningAuthorized(this)) {
            launchMainActivity()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.request_button -> {
                val intent: Intent =
                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                startActivity(intent)
                Toast.makeText(this, "Click on AB Music to enable!", Toast.LENGTH_LONG).show()
            }
            R.id.text_skip -> {
                launchMainActivity()
                skip!!.visibility = View.GONE
                never_ask!!.visibility = View.GONE
                progressBar!!.visibility = View.VISIBLE
            }
            R.id.text_never_ask -> {
                never_ask!!.visibility = View.GONE
                progressBar!!.visibility = View.VISIBLE
                MyApp.getPref()!!.edit()
                    .putBoolean(getString(R.string.pref_never_ask_notitication_permission), true)
                    .apply()
                launchMainActivity()
            }
        }
    }

    private fun launchMainActivity() {
        val mainActIntent = Intent(this, ActivityMain::class.java)
        startActivity(mainActIntent)
        finish()
    }
}