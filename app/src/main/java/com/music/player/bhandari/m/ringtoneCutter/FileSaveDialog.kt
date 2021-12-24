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

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.os.Message
import android.view.View
import android.widget.*
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.uiElementHelper.ColorHelper

class FileSaveDialog constructor(
    context: Context?,
    resources: Resources,
    originalName: String?,
    response: Message
) : Dialog(context!!) {
    private var mTypeSpinner: Spinner? = null
    private var mFilename: EditText? = null
    private var mResponse: Message? = null
    private val mOriginalName: String?
    private val mTypeArray: ArrayList<String>
    private var mPreviousSelection: Int
    private fun setFilenameEditBoxFromName(onlyIfNotEdited: Boolean) {
        if (onlyIfNotEdited) {
            val currentText: CharSequence = mFilename!!.text
            val expectedText = (mOriginalName + " " + mTypeArray[mPreviousSelection])
            if (!expectedText.contentEquals(currentText)) {
                return
            }
        }
        val newSelection: Int = mTypeSpinner!!.selectedItemPosition
        val newSuffix: String = mTypeArray.get(newSelection)
        mFilename!!.setText("$mOriginalName $newSuffix")
        mPreviousSelection = mTypeSpinner!!.selectedItemPosition
    }

    private val saveListener: View.OnClickListener = View.OnClickListener {
        mResponse!!.obj = mFilename!!.text
        mResponse!!.arg1 = mTypeSpinner!!.selectedItemPosition
        mResponse!!.sendToTarget()
        dismiss()
    }
    private val cancelListener: View.OnClickListener = View.OnClickListener { dismiss() }

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
            return when (kind) {
                FILE_KIND_MUSIC -> "Music"
                FILE_KIND_ALARM -> "Alarm"
                FILE_KIND_NOTIFICATION -> "Notification"
                FILE_KIND_RINGTONE -> "Ringtone"
                else -> "Unknown"
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
            context!!, R.layout.support_simple_spinner_dropdown_item, mTypeArray)
        adapter.setDropDownViewResource(
            R.layout.support_simple_spinner_dropdown_item)
        mTypeSpinner = findViewById<View>(R.id.ringtone_type) as Spinner
        mTypeSpinner!!.adapter = adapter
        mTypeSpinner!!.setSelection(FILE_KIND_RINGTONE)
        mPreviousSelection = FILE_KIND_RINGTONE
        setFilenameEditBoxFromName(false)
        mTypeSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                v: View,
                position: Int,
                id: Long
            ) {
                setFilenameEditBoxFromName(true)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        val save: Button = findViewById<View>(R.id.save) as Button
        save.setOnClickListener(saveListener)
        val cancel: Button = findViewById<View>(R.id.cancel) as Button
        cancel.setOnClickListener(cancelListener)
        mResponse = response
        if (window != null) {
            window!!.setBackgroundDrawable(ColorHelper.getGradientDrawableDark())
            window!!.attributes.windowAnimations = R.style.MyAnimation_Window
        }
    }
}