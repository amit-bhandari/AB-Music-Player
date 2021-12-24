package com.music.player.bhandari.m.adapter

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.util.Log
import android.util.SparseBooleanArray
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.uiElementHelper.TypeFaceHelper
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics
import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

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
class LyricsViewAdapter(private val context: Context, lyrics: Lyrics?) : RecyclerView.Adapter<LyricsViewAdapter.MyViewHolder?>() {

    private val copyRightText: String = context.getString(R.string.lyric_copy_right_msg)
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val mLyrics: Lyrics? = lyrics
    private val dictionary: TreeMap<Long, String> = TreeMap<Long, String>()
    private var mCurrentTime = 0L
    private var mNextTime = 0L
    private var mPrevTime = 0L
    private val mTimes: MutableList<Long> = ArrayList()
    private val selectedItems: SparseBooleanArray

    //flag is used when lyric are searched through explre feature
    private var noDynamicLyrics = false
    fun setNoDynamicLyrics(noDynamicLyrics: Boolean) {
        this.noDynamicLyrics = noDynamicLyrics
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //calligraphy problem in kitkat
        //with text view having background
        val view: View = try {
            inflater.inflate(R.layout.lyrics_line_text_view, parent, false)
        } catch (e: InflateException) {
            val textView = TextView(context)
            val param = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            textView.layoutParams = param
            textView.textSize = 20f
            textView.gravity = Gravity.CENTER_HORIZONTAL
            textView.typeface = TypeFaceHelper.getTypeFace(context)
            textView.isClickable = true
            textView.setPadding(10, 10, 10, 10)
            textView.id = R.id.lyrics_line
            return MyViewHolder(textView)
        }
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val line: String = dictionary[mTimes[position]]!!
        holder.line.text = line
        Log.d("LyricsViewAdapter", "onBindViewHolder: current time $mCurrentTime")
        //when lyrics are searched through explore, no need of running lyrics
        when {
            mLyrics!!.isLRC() && !noDynamicLyrics -> {
                val color = if (mTimes[position] <= mCurrentTime) Color.YELLOW else Color.WHITE
                holder.line.setTextColor(color)
                Log.d("LyricsViewAdapter", "onBindViewHolder: setting color $color")
            }
        }

        //last item is copyright text
        when (position) {
            mTimes.size - 1 -> {
                holder.line.textSize = 10f
            }
            else -> {
                holder.line.textSize = 22f
            }
        }

        //holder.line.setTypeface(TypeFaceHelper.getTypeFace(context));
        holder.itemView.isActivated = selectedItems.get(position, false)
    }

    override fun getItemCount(): Int {
        return mTimes.size
    }

    fun getLineAtPosition(position: Int): String {
        return dictionary[mTimes[position]]!!
    }

    private fun setStaticDictionary() {
        var i: Long = 0
        try {
            val br = BufferedReader(StringReader(Html.fromHtml(mLyrics!!.getText()).toString()))
            var str: String?
            while (br.readLine().also { str = it } != null) {
                //lyricLines.add(str);
                dictionary[i] = str!!
                mTimes.add(i)
                i++
            }

            //put last element as lyrics copyright text
            if (mTimes.size != 0) {
                dictionary[i] = copyRightText
                mTimes.add(i)
            }
            br.close()
        } catch (ignored: Exception) {
        }
    }

    private fun setDynamicDictionary() {
        if (mLyrics == null) {
            return
        }
        mNextTime = 0
        val texts: MutableList<String> = ArrayList()
        val reader = BufferedReader(StringReader(mLyrics.getText()))
        var line: String
        var arr: Array<String>?
        try {
            while (null != reader.readLine().also { line = it }) {
                arr = parseLine(line)
                if (null == arr) {
                    continue
                }
                if (1 == arr.size && texts.size != 0) {
                    val last = texts.removeAt(texts.size - 1)
                    texts.add(last + arr[0])
                    continue
                }
                for (i in 0 until arr.size - 1) {
                    mTimes.add(arr[i].toLong())
                    texts.add(arr[arr.size - 1])
                }
            }
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        // Collections.sort(mTimes);
        for (i in mTimes.indices) {
            if (!(dictionary.isEmpty() && texts[i].replace("\\s".toRegex(), "").isEmpty())) {
                // Log.v(Constants.L_TAG+" chavan",texts.get(i));
                dictionary[mTimes[i]] = texts[i]
            }
        }
        mTimes.sort()

        //put last element as lyrics copyright text
        if (mTimes.size != 0) {
            dictionary!![mTimes[mTimes.size - 1] + 500] = copyRightText
            mTimes.add(mTimes[mTimes.size - 1] + 500)
        }
    }

    private fun parseLine(line: String): Array<String>? {
        var line = line
        val matcher = Pattern.compile("\\[.+\\].+").matcher(line)

        /*if (!matcher.matches() || line.contains("By:")) {
            if (line.contains("[by:") && line.length() > 6)
                this.uploader = line.substring(5, line.length() - 1);
            return null;
        }*/if (line.endsWith("]")) line += " "
        line = line.replace("\\[".toRegex(), "")
        val result = line.split("\\]".toRegex()).toTypedArray()
        try {
            for (i in 0 until result.size - 1) result[i] = parseTime(result[i]).toString()
        } catch (ignored: NumberFormatException) {
            return null
        } catch (ignored: ArrayIndexOutOfBoundsException) {
            return null
        }
        return result
    }

    private fun parseTime(time: String): Long {
        val min = time.split(":".toRegex()).toTypedArray()
        if (!min[1].contains(".")) min[1] += ".00"
        val sec: Array<String> = min[1].split("\\.".toRegex()).toTypedArray()
        val minInt = min[0].replace("\\D+".toRegex(), "")
            .replace("\r".toRegex(), "").replace("\n".toRegex(), "").trim { it <= ' ' }.toLong()
        val secInt = sec[0].replace("\\D+".toRegex(), "")
            .replace("\r".toRegex(), "").replace("\n".toRegex(), "").trim { it <= ' ' }.toLong()
        val milInt = sec[1].replace("\\D+".toRegex(), "")
            .replace("\r".toRegex(), "").replace("\n".toRegex(), "").trim { it <= ' ' }.toLong()
        return minInt * 60 * 1000 + secInt * 1000 + milInt * 10
    }

    @Synchronized
    fun changeCurrent(time: Long): Int {
        if (dictionary.isEmpty()) {
            return -1
        }
        mPrevTime = mCurrentTime
        mNextTime = dictionary.lastKey()
        if (time < mNextTime) mNextTime = dictionary.higherKey(time)
        mCurrentTime = dictionary.firstKey()
        if (time > mCurrentTime) mCurrentTime = dictionary.floorKey(time)
        if (mCurrentTime != mPrevTime && mPrevTime != 0L) {
            val index = mTimes.indexOf(mCurrentTime)
            notifyItemChanged(index)
            return index
        }
        return -1
    }

    fun getCurrentTimeIndex(): Int {
        return mTimes.indexOf(dictionary!!.floorKey(mCurrentTime)) ?: -1
    }

    fun getStaticLyrics(): String {
        val text = StringBuilder()
        val iterator: Iterator<String> = dictionary.values.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (text.isEmpty() && next.replace("\\s".toRegex(), "").isEmpty()) continue
            text.append(next)
            if (iterator.hasNext()) text.append("<br/>\n")
        }
        return text.toString()
    }

    //methods for item selection
    fun toggleSelection(pos: Int) {
        when {
            selectedItems.get(pos, false) -> {
                selectedItems.delete(pos)
            }
            else -> {
                selectedItems.put(pos, true)
            }
        }
        notifyItemChanged(pos)
    }

    fun clearSelections() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItemCount(): Int {
        return selectedItems.size()
    }

    fun getSelectedItems(): List<Int> {
        val items: MutableList<Int> = ArrayList(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            items.add(selectedItems.keyAt(i))
        }
        return items
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var line: TextView = itemView.findViewById(R.id.lyrics_line)
    }

    init {
        dictionary.clear()


        //ignore null pointer expcetion
        //happens when service is not started and instant lyric started
        try {
            mCurrentTime = MyApp.getService()!!.getCurrentTrackProgress().toLong()
        } catch (ignored: NullPointerException) {
        }
        mNextTime = 0L
        mPrevTime = 0L
        mTimes.clear()
        selectedItems = SparseBooleanArray()
        if (mLyrics!!.isLRC()) {
            setDynamicDictionary()
        } else {
            setStaticDictionary()
        }
        setHasStableIds(true)
    }
}