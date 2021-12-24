/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.music.player.bhandari.m.ringtoneCutter

import android.app.Dialog
import android.content.Context
import android.os.Message
import android.view.View
import android.widget.Button
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.uiElementHelper.ColorHelper

class AfterSaveActionDialog constructor(context: Context?, response: Message) : Dialog(context!!) {
    private val mResponse: Message
    private fun closeAndSendResult(clickedButtonId: Int) {
        mResponse.arg1 = clickedButtonId
        mResponse.sendToTarget()
        dismiss()
    }

    init {

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.after_save_action)
        setTitle(R.string.alert_title_success)
        (findViewById<View>(R.id.button_make_default) as Button)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    closeAndSendResult(R.id.button_make_default)
                }
            })
        /*((Button)findViewById(R.id.button_choose_contact))
            .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        closeAndSendResult(R.id.button_choose_contact);
                    }
                });*/(findViewById<View>(R.id.button_do_nothing) as Button)
            .setOnClickListener { closeAndSendResult(R.id.button_do_nothing) }
        mResponse = response
        if (window != null) {
            window!!.setBackgroundDrawable(ColorHelper.getGradientDrawableDark())
            window!!.attributes.windowAnimations = R.style.MyAnimation_Window
        }
    }
}