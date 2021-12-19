package com.music.player.bhandari.m.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.activity.ActivityLyricView
import com.music.player.bhandari.m.lyricsExplore.Track
import com.music.player.bhandari.m.utils.UtilityFun

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
class TopTracksAdapter(private val context: Context, private val trackList: List<Track>) :
    RecyclerView.Adapter<TopTracksAdapter.MyViewHolder?>(), PopupMenu.OnMenuItemClickListener {
    private val inflater = LayoutInflater.from(context)
    private var clickedPostion = 0
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = inflater.inflate(R.layout.track_item_square_image, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val trackInfo = trackList[position].artist + " - " + trackList[position].title
        val playcount = "Playcount - " + trackList[position].playCount
        holder.trackName!!.text = trackInfo
        holder.playCount!!.text = playcount
    }

    override fun getItemCount(): Int {
        return trackList.size
    }

    private fun onClick(v: View, position: Int) {
        /**/
        clickedPostion = position
        when (v.id) {
            R.id.more -> {
                val popup = PopupMenu(context, v)
                val inflater = popup.menuInflater
                inflater.inflate(R.menu.menu_explore_lyric_item, popup.menu)
                popup.show()
                popup.setOnMenuItemClickListener(this)
            }
            R.id.root_view_item_explore_lyrics -> {
                val intent = Intent(context, ActivityLyricView::class.java)
                intent.putExtra("track_title", trackList[position].title)
                intent.putExtra("artist", trackList[position].artist)
                context.startActivity(intent)
                //if (context instanceof ActivityExploreLyrics) {
                (context as Activity).overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search_youtube -> UtilityFun.LaunchYoutube(context,
                trackList[clickedPostion].artist + " - " + trackList[clickedPostion].title)
        }
        return true
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        @BindView(R.id.trackInfo)
        var trackName: TextView? = null

        @BindView(R.id.playCount)
        var playCount: TextView? = null

        @BindView(R.id.imageView)
        var imageView: ImageView? = null

        @BindView(R.id.more)
        var overflow: ImageView? = null
        override fun onClick(v: View) {
            this@TopTracksAdapter.onClick(v, layoutPosition)
        }

        init {
            ButterKnife.bind(this, itemView)
            itemView.setOnClickListener(this)
            overflow!!.setOnClickListener(this)
        }
    }

    init {
        setHasStableIds(true)
    }
}