/*
 * Copyright (C) 2008 Google Inc.
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

import android.R
import android.content.Context
import android.os.Message
import android.view.View
import com.music.player.bhandari.m.UIElementHelper.ColorHelper

class FileSaveDialog constructor(
    context: Context?,
    resources: Resources,
    originalName: String?,
    response: Message
) : Dialog(context) {
    private val mTypeSpinner: Spinner
    private val mFilename: EditText
    private val mResponse: Message
    private val mOriginalName: String?
    private val mTypeArray: ArrayList<String>
    private var mPreviousSelection: Int
    private fun setFilenameEditBoxFromName(onlyIfNotEdited: Boolean) {
        if (onlyIfNotEdited) {
            val currentText: CharSequence = mFilename.getText()
            val expectedText: String = (mOriginalName + " " +
                    mTypeArray.get(mPreviousSelection))
            if (!expectedText.contentEquals(currentText)) {
                return
            }
        }
        val newSelection: Int = mTypeSpinner.getSelectedItemPosition()
        val newSuffix: String = mTypeArray.get(newSelection)
        mFilename.setText(mOriginalName + " " + newSuffix)
        mPreviousSelection = mTypeSpinner.getSelectedItemPosition()
    }

    private val saveListener: View.OnClickListener = object : View.OnClickListener {
        public override fun onClick(view: View) {
            mResponse.obj = mFilename.getText()
            mResponse.arg1 = mTypeSpinner.getSelectedItemPosition()
            mResponse.sendToTarget()
            dismiss()
        }
    }
    private val cancelListener: View.OnClickListener = object : View.OnClickListener {
        public override fun onClick(view: View) {
            dismiss()
        }
    }

    companion object {
        // File kinds - these should correspond to the order in which
        // they're presented in the spinner control
        val FILE_KIND_MUSIC: Int = 0
        val FILE_KIND_ALARM: Int = 1
        val FILE_KIND_NOTIFICATION: Int = 2
        val FILE_KIND_RINGTONE: Int = 3

        /**
         * Return a human-readable name for a kind (music, alarm, ringtone, ...).
         * These won't be displayed on-screen (just in logs) so they shouldn't
         * be translated.
         */
        fun KindToName(kind: Int): String {
            when (kind) {
                FILE_KIND_MUSIC -> return "Music"
                FILE_KIND_ALARM -> return "Alarm"
                FILE_KIND_NOTIFICATION -> return "Notification"
                FILE_KIND_RINGTONE -> return "Ringtone"
                else -> return "Unknown"
            }
        }
    }

    init {

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.file_save)
        setTitle(resources.getString(R.string.file_save_title))
        mTypeArray = ArrayList()
        mTypeArray.add(resources.getString(R.string.type_music))
        mTypeArray.add(resources.getString(R.string.type_alarm))
        mTypeArray.add(resources.getString(R.string.type_notification))
        mTypeArray.add(resources.getString(R.string.type_ringtone))
        mFilename = findViewById<View>(R.id.filename) as EditText
        mOriginalName = originalName
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            context, R.layout.simple_spinner_item, mTypeArray)
        adapter.setDropDownViewResource(
            R.layout.simple_spinner_dropdown_item)
        mTypeSpinner = findViewById<View>(R.id.ringtone_type) as Spinner
        mTypeSpinner.setAdapter(adapter)
        mTypeSpinner.setSelection(FILE_KIND_RINGTONE)
        mPreviousSelection = FILE_KIND_RINGTONE
        setFilenameEditBoxFromName(false)
        mTypeSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            public override fun onItemSelected(
                parent: AdapterView<*>?,
                v: View,
                position: Int,
                id: Long
            ) {
                setFilenameEditBoxFromName(true)
            }

            public override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
        val save: Button = findViewById<View>(R.id.save) as Button
        save.setOnClickListener(saveListener)
        val cancel: Button = findViewById<View>(R.id.cancel) as Button
        cancel.setOnClickListener(cancelListener)
        mResponse = response
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(ColorHelper.getGradientDrawableDark())
            getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window
        }
    }
}