package com.music.player.bhandari.m.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.FileProvider
import androidx.core.view.MotionEventCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.uiElementHelper.ColorHelper
import com.music.player.bhandari.m.uiElementHelper.TypeFaceHelper
import com.music.player.bhandari.m.uiElementHelper.recyclerviewHelper.ItemTouchHelperAdapter
import com.music.player.bhandari.m.uiElementHelper.recyclerviewHelper.OnStartDragListener
import com.music.player.bhandari.m.activity.ActivityTagEditor
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.MusicLibrary
import com.music.player.bhandari.m.model.dataItem
import com.music.player.bhandari.m.service.PlayerService
import com.music.player.bhandari.m.utils.UtilityFun
import com.wang.avi.AVLoadingIndicatorView
import java.io.File
import java.util.*
import java.util.concurrent.Executors
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
class CurrentTracklistAdapter constructor(
    context: Context,
    dragStartListener: OnStartDragListener
) : RecyclerView.Adapter<CurrentTracklistAdapter.MyViewHolder?>(), ItemTouchHelperAdapter,
    PopupMenu.OnMenuItemClickListener {
    private val playerService: PlayerService?
    private var mLastClickTime: Long = 0
    private val mDragStartListener: OnStartDragListener = dragStartListener
    private val context: Context
    private val inflater: LayoutInflater

    //current playing position
    private var position: Int = 0
    private var tempPosition: Int = 0 //temporary variable to hold position for onMenuItemClick
    private val handler: Handler

    //very badly written code
    fun fillData() {
        if (playerService == null) return
        dataItems.clear()
        Executors.newSingleThreadExecutor().execute {
            val temp: ArrayList<Int> = playerService.getTrackList()
            //HashMap<dataItem> data = MusicLibrary.getInstance().getDataItemsForTracks();
            try {
                for (id: Int in temp) {
                    val d: dataItem? =
                        MusicLibrary.instance.getDataItemsForTracks()!!.get(id)
                    if (d != null) {
                        dataItems.add(d)
                    }
                }
                Log.d("CurrentTrack", "run: queue ready")
                handler.post { notifyDataSetChanged() }
            } catch (ignored: Exception) {
                //ignore for now
                Log.e("Notify", "notify")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = inflater.inflate(R.layout.track_item_for_dragging, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (dataItems[position] == null) return
        holder.title.text = dataItems[position]!!.title
        holder.secondary.text = dataItems[position]!!.artist_name
        holder.handle.setOnTouchListener { view, motionEvent ->
            if (MotionEventCompat.getActionMasked(motionEvent) ==
                MotionEvent.ACTION_DOWN
            ) {
                Log.d("CurrentTracklistAdapter", "onTouch: ")
                mDragStartListener.onStartDrag(holder)
            }
            false
        }
        when {
            playerService != null && position == playerService.getCurrentTrackPosition() -> {
                holder.cv.setBackgroundColor(ColorHelper.getColor(R.color.gray3))
                holder.playAnimation.visibility = View.VISIBLE
                when {
                    playerService.getStatus() === playerService.PLAYING -> {
                        //holder.iv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_black_24dp));
                        holder.playAnimation.smoothToShow()
                    }
                    else -> {
                        holder.playAnimation.smoothToHide()
                        //holder.iv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
                    }
                }
                //holder.iv.setVisibility(View.VISIBLE);
            }
            else -> {
                holder.cv.setBackgroundColor(context.resources.getColor(R.color.colorTransparent))
                //holder.iv.setVisibility(View.GONE);
                holder.playAnimation.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return dataItems.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        //no need to update list of in player service.
        //listOfHeader is reference for that list itself
        //it will automatically reflect in current tracklist in player service class
        Log.d("CurrentTracklistAdapter", "onItemMove: from to $fromPosition : $toPosition")
        playerService!!.swapPosition(fromPosition, toPosition)
        Collections.swap(dataItems, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemDismiss(position: Int) {
        when {
            playerService!!.getCurrentTrackPosition() !== position -> {
                //listOfHeader.remove(position);
                playerService!!.removeTrack(position)
                dataItems.removeAt(position)
                notifyItemRemoved(position)
            }
            else -> {
                notifyItemChanged(position)
                //notifyDataSetChanged();
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_play -> {
                val oldPos: Int = position
                position = tempPosition
                playerService!!.playAtPositionFromNowPlaying(tempPosition)
                notifyItemChanged(oldPos)
                notifyItemChanged(position)
                val intent: Intent = Intent().setAction(Constants.ACTION.COMPLETE_UI_UPDATE)
                intent.putExtra("skip_adapter_update", true)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
            R.id.action_add_to_playlist -> {
                val ids: IntArray = intArrayOf(dataItems[position]!!.id)
                UtilityFun.addToPlaylist(context, ids)
            }
            R.id.action_share -> try {
                val uris: ArrayList<Uri> = ArrayList() //for sending multiple files
                val file = File(dataItems[position]!!.file_path)
                val fileUri: Uri = FileProvider.getUriForFile(context,
                    context.applicationContext
                        .packageName + "com.bhandari.music.provider",
                    file)
                uris.add(fileUri)
                UtilityFun.Share(context, uris, dataItems[position]!!.title)
            } catch (e: IllegalArgumentException) {
                try {
                    UtilityFun.ShareFromPath(context, dataItems[position]!!.file_path!!)
                } catch (ex: Exception) {
                    Toast.makeText(context,
                        context.getString(R.string.error_unable_to_share),
                        Toast.LENGTH_SHORT).show()
                }
            }
            R.id.action_delete -> delete()
            R.id.action_track_info -> setTrackInfoDialog()
            R.id.action_edit_track_info -> {
                context.startActivity(Intent(context, ActivityTagEditor::class.java)
                    .putExtra("from", Constants.TAG_EDITOR_LAUNCHED_FROM.NOW_PLAYING)
                    .putExtra("file_path", dataItems[position]!!.file_path)
                    .putExtra("track_title", dataItems[position]!!.title)
                    .putExtra("position", position)
                    .putExtra("id", dataItems[position]!!.id))
                (context as Activity).overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left)
            }
            R.id.action_search_youtube -> UtilityFun.LaunchYoutube(context,
                (dataItems[position]!!.artist_name + " - " + dataItems[position]!!.title))
        }
        return true
    }

    fun getSongList(): ArrayList<Int> {
        val temp: ArrayList<Int> = ArrayList()
        for (d in dataItems) {
            if (d!!.id != 0) {
                temp.add(d.id)
            }
        }
        return temp
    }

    fun updateItem(position: Int, vararg param: String) {
        try {
            dataItems[position]!!.title = param[0]
            dataItems[position]!!.artist_name = param[1]
            dataItems[position]!!.albumName = param[2]
            notifyItemChanged(position)
        } catch (e: Exception) {
            Log.v(Constants.TAG, e.toString())
        }
    }

    private fun setTrackInfoDialog() {
        //final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        //alert.setTitle(context.getString(R.string.track_info_title));
        val linear = LinearLayout(context)
        linear.orientation = LinearLayout.VERTICAL
        val text = TextView(context)
        text.typeface = TypeFaceHelper.getTypeFace(context)
        text.text = UtilityFun.trackInfoBuild(dataItems[position]!!.id).toString()
        text.setPadding(20, 20, 20, 10)
        text.textSize = 15f
        //text.setGravity(Gravity.CENTER);
        text.typeface = TypeFaceHelper.getTypeFace(context)
        linear.addView(text)
        //alert.setView(linear);
        //alert.show();
        MaterialDialog(context)
            .title(text = context.getString(R.string.track_info_title))
            .customView(view = linear, dialogWrapContent = true)
            .positiveButton(R.string.okay)
            .show()
    }

    private fun delete() {
        MaterialDialog(context)
            .title(text = context.getString(R.string.are_u_sure))
            .positiveButton(R.string.yes){
                val ids: ArrayList<Int> = ArrayList()
                val files: ArrayList<File> = ArrayList()
                files.add(File(dataItems[position]!!.file_path))
                ids.add(dataItems[position]!!.id)
                when {
                    UtilityFun.Delete(context, files, ids) -> {  //last parameter not needed
                        Toast.makeText(context,
                            context.getString(R.string.deleted) + dataItems[position]!!.title,
                            Toast.LENGTH_SHORT).show()
                        when {
                            playerService!!.getCurrentTrack()!!.title
                                .equals(dataItems[position]!!.title) -> {
                                playerService.nextTrack()
                                //playerService!!.notifyUI();
                                notifyItemChanged(position + 1)
                            }
                        }
                        playerService?.removeTrack(position)
                        dataItems.removeAt(position)
                        notifyItemRemoved(position)
                        // notifyDataSetChanged();
                    }
                    else -> {
                        Toast.makeText(context,
                            context.getString(R.string.unable_to_del),
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .negativeButton(R.string.no)
            .show()
    }

    fun onClick(view: View, position: Int) {
        tempPosition = position
        when (view.id) {
            R.id.more -> {
                val popup = PopupMenu(context, view)
                val inflater: MenuInflater = popup.menuInflater
                inflater.inflate(R.menu.menu_tracks_by_title, popup.menu)
                popup.menu.removeItem(R.id.action_set_as_ringtone)
                popup.menu.removeItem(R.id.action_add_to_q)
                popup.menu.removeItem(R.id.action_play_next)
                popup.menu.removeItem(R.id.action_exclude_folder)
                popup.show()
                popup.setOnMenuItemClickListener(this)
            }
            R.id.trackItemDraggable -> {
                val oldPos: Int = this.position
                this.position = position
                if (SystemClock.elapsedRealtime() - mLastClickTime < 100) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                notifyItemChanged(oldPos)
                notifyItemChanged(position)
                if (position == playerService!!.getCurrentTrackPosition()) {
                    playerService.play()
                    val intent: Intent = Intent().setAction(Constants.ACTION.COMPLETE_UI_UPDATE)
                    intent.putExtra("skip_adapter_update", true)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    //playerService!!.notifyUI();
                } else {
                    playerService.playAtPositionFromNowPlaying(position)
                    Log.v(Constants.TAG, "$position  position")
                }
            }
        }
    }

    inner class MyViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var title: TextView = itemView.findViewById(R.id.header)
        var secondary: TextView = itemView.findViewById(R.id.secondaryHeader)
        var handle: ImageView = itemView.findViewById(R.id.handleForDrag)
        var cv: View = itemView.findViewById(R.id.trackItemDraggable)

        //ImageView iv;
        var playAnimation: AVLoadingIndicatorView = itemView.findViewById(R.id.song_playing_animation)

        override fun onClick(v: View) {
            this@CurrentTracklistAdapter.onClick(v, this.layoutPosition)
        }

        init {
            //iv = itemView.findViewById(R.id.play_button_item_drag);
            itemView.findViewById<View>(R.id.more).setOnClickListener(this)
            itemView.findViewById<View>(R.id.trackItemDraggable).setOnClickListener(this)
        }
    }

    companion object {
        private val dataItems: ArrayList<dataItem?> = ArrayList()
    }

    init {
        if (MyApp.getService() == null) {
            UtilityFun.restartApp()
        }
        playerService = MyApp.getService()
        handler = Handler(Looper.getMainLooper())
        fillData()
        position = playerService!!.getCurrentTrackPosition()
        this.context = context
        inflater = LayoutInflater.from(context)
        //setHasStableIds(true);
    }
}