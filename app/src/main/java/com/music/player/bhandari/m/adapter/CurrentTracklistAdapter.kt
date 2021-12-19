package com.music.player.bhandari.m.adapter

import android.content.Context
import android.os.Handler
import android.os.SystemClock
import android.view.View
import android.widget.PopupMenu
import com.afollestad.materialdialogs.DialogAction
import com.music.player.bhandari.m.model.Constants
import java.util.concurrent.Executors

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
    private val mDragStartListener: OnStartDragListener
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
        Executors.newSingleThreadExecutor().execute(object : Runnable {
            public override fun run() {
                val temp: ArrayList<Int> = playerService.getTrackList()
                //HashMap<dataItem> data = MusicLibrary.getInstance().getDataItemsForTracks();
                try {
                    for (id: Int in temp) {
                        val d: dataItem? =
                            MusicLibrary.getInstance().getDataItemsForTracks().get(id)
                        if (d != null) {
                            dataItems.add(d)
                        }
                    }
                    Log.d("CurrentTrack", "run: queue ready")
                    handler.post(object : Runnable {
                        public override fun run() {
                            notifyDataSetChanged()
                        }
                    })
                } catch (ignored: Exception) {
                    //ignore for now
                    Log.e("Notify", "notify")
                }
            }
        })
    }

    public override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = inflater.inflate(R.layout.track_item_for_dragging, parent, false)
        return MyViewHolder(view)
    }

    public override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (dataItems.get(position) == null) return
        holder.title.setText(dataItems.get(position).title)
        holder.secondary.setText(dataItems.get(position).artist_name)
        holder.handle.setOnTouchListener(object : OnTouchListener {
            public override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                if (MotionEventCompat.getActionMasked(motionEvent) ==
                    MotionEvent.ACTION_DOWN
                ) {
                    Log.d("CurrentTracklistAdapter", "onTouch: ")
                    mDragStartListener.onStartDrag(holder)
                }
                return false
            }
        })
        if (playerService != null && position == playerService.getCurrentTrackPosition()) {
            holder.cv.setBackgroundColor(ColorHelper.getColor(R.color.gray3))
            holder.playAnimation.setVisibility(View.VISIBLE)
            if (playerService.getStatus() === PlayerService.PLAYING) {
                //holder.iv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_black_24dp));
                holder.playAnimation.smoothToShow()
            } else {
                holder.playAnimation.smoothToHide()
                //holder.iv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
            }
            //holder.iv.setVisibility(View.VISIBLE);
        } else {
            holder.cv.setBackgroundColor(context.getResources().getColor(R.color.colorTransparent))
            //holder.iv.setVisibility(View.GONE);
            holder.playAnimation.setVisibility(View.GONE)
        }
    }

    public override fun getItemCount(): Int {
        return dataItems.size
    }

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        //no need to update list of in player service.
        //listOfHeader is reference for that list itself
        //it will automatically reflect in current tracklist in player service class
        Log.d("CurrentTracklistAdapter", "onItemMove: from to " + fromPosition + " : " + toPosition)
        playerService.swapPosition(fromPosition, toPosition)
        Collections.swap(dataItems, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    fun onItemDismiss(position: Int) {
        if (playerService.getCurrentTrackPosition() !== position) {
            //listOfHeader.remove(position);
            playerService.removeTrack(position)
            dataItems.removeAt(position)
            notifyItemRemoved(position)
        } else {
            notifyItemChanged(position)
            //notifyDataSetChanged();
        }
    }

    public override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.action_play -> {
                val oldPos: Int = position
                position = tempPosition
                playerService.playAtPositionFromNowPlaying(tempPosition)
                notifyItemChanged(oldPos)
                notifyItemChanged(position)
                val intent: Intent = Intent().setAction(Constants.ACTION.COMPLETE_UI_UPDATE)
                intent.putExtra("skip_adapter_update", true)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }
            R.id.action_add_to_playlist -> {
                val ids: IntArray = intArrayOf(dataItems.get(position).id)
                UtilityFun.AddToPlaylist(context, ids)
            }
            R.id.action_share -> try {
                val uris: ArrayList<Uri> = ArrayList<Uri>() //for sending multiple files
                val file: File = File(dataItems.get(position).file_path)
                val fileUri: Uri = FileProvider.getUriForFile(context,
                    context.getApplicationContext()
                        .getPackageName() + "com.bhandari.music.provider",
                    file)
                uris.add(fileUri)
                UtilityFun.Share(context, uris, dataItems.get(position).title)
            } catch (e: IllegalArgumentException) {
                try {
                    UtilityFun.ShareFromPath(context, dataItems.get(position).file_path)
                } catch (ex: Exception) {
                    Toast.makeText(context,
                        context.getString(R.string.error_unable_to_share),
                        Toast.LENGTH_SHORT).show()
                }
            }
            R.id.action_delete -> Delete()
            R.id.action_track_info -> setTrackInfoDialog()
            R.id.action_edit_track_info -> {
                context.startActivity(Intent(context, ActivityTagEditor::class.java)
                    .putExtra("from", Constants.TAG_EDITOR_LAUNCHED_FROM.NOW_PLAYING)
                    .putExtra("file_path", dataItems.get(position).file_path)
                    .putExtra("track_title", dataItems.get(position).title)
                    .putExtra("position", position)
                    .putExtra("id", dataItems.get(position).id))
                (context as Activity).overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left)
            }
            R.id.action_search_youtube -> UtilityFun.LaunchYoutube(context,
                (dataItems.get(position).artist_name + " - "
                        + dataItems.get(position).title))
        }
        return true
    }

    fun getSongList(): ArrayList<Int> {
        val temp: ArrayList<Int> = ArrayList()
        for (d: dataItem in dataItems) {
            if (d.id != 0) {
                temp.add(d.id)
            }
        }
        return temp
    }

    fun updateItem(position: Int, vararg param: String) {
        try {
            dataItems.get(position).title = param.get(0)
            dataItems.get(position).artist_name = param.get(1)
            dataItems.get(position).albumName = param.get(2)
            notifyItemChanged(position)
        } catch (e: Exception) {
            Log.v(Constants.TAG, e.toString())
        }
    }

    private fun setTrackInfoDialog() {
        //final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        //alert.setTitle(context.getString(R.string.track_info_title));
        val linear: LinearLayout = LinearLayout(context)
        linear.setOrientation(LinearLayout.VERTICAL)
        val text: TextView = TextView(context)
        text.setTypeface(TypeFaceHelper.getTypeFace(context))
        text.setText(UtilityFun.trackInfoBuild(dataItems.get(position).id).toString())
        text.setPadding(20, 20, 20, 10)
        text.setTextSize(15f)
        //text.setGravity(Gravity.CENTER);
        text.setTypeface(TypeFaceHelper.getTypeFace(context))
        linear.addView(text)
        //alert.setView(linear);
        //alert.show();
        MyDialogBuilder(context)
            .title(context.getString(R.string.track_info_title))
            .customView(linear, true)
            .positiveText(R.string.okay)
            .show()
    }

    private fun Delete() {
        MyDialogBuilder(context)
            .title(context.getString(R.string.are_u_sure))
            .positiveText(R.string.yes)
            .negativeText(R.string.no)
            .onPositive(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    val ids: ArrayList<Int> = ArrayList()
                    val files: ArrayList<File> = ArrayList<File>()
                    files.add(File(dataItems.get(position).file_path))
                    ids.add(dataItems.get(position).id)
                    if (UtilityFun.Delete(context, files, ids)) {  //last parameter not needed
                        Toast.makeText(context,
                            context.getString(R.string.deleted) + dataItems.get(position).title,
                            Toast.LENGTH_SHORT).show()
                        if (playerService.getCurrentTrack().getTitle()
                                .equals(dataItems.get(position).title)
                        ) {
                            playerService.nextTrack()
                            //playerService.notifyUI();
                            notifyItemChanged(position + 1)
                        }
                        playerService.removeTrack(position)
                        dataItems.removeAt(position)
                        notifyItemRemoved(position)
                        // notifyDataSetChanged();
                    } else {
                        Toast.makeText(context,
                            context.getString(R.string.unable_to_del),
                            Toast.LENGTH_SHORT).show()
                    }
                }
            })
            .show()
    }

    fun onClick(view: View, position: Int) {
        tempPosition = position
        when (view.getId()) {
            R.id.more -> {
                val popup: PopupMenu = PopupMenu(context, view)
                val inflater: MenuInflater = popup.getMenuInflater()
                inflater.inflate(R.menu.menu_tracks_by_title, popup.getMenu())
                popup.getMenu().removeItem(R.id.action_set_as_ringtone)
                popup.getMenu().removeItem(R.id.action_add_to_q)
                popup.getMenu().removeItem(R.id.action_play_next)
                popup.getMenu().removeItem(R.id.action_exclude_folder)
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
                if (position == playerService.getCurrentTrackPosition()) {
                    playerService.play()
                    val intent: Intent = Intent().setAction(Constants.ACTION.COMPLETE_UI_UPDATE)
                    intent.putExtra("skip_adapter_update", true)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    //playerService.notifyUI();
                } else {
                    playerService.playAtPositionFromNowPlaying(position)
                    Log.v(Constants.TAG, position.toString() + "  position")
                }
            }
        }
    }

    inner class MyViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var title: TextView
        var secondary: TextView
        var handle: ImageView
        var cv: View

        //ImageView iv;
        var playAnimation: AVLoadingIndicatorView
        public override fun onClick(v: View) {
            this@CurrentTracklistAdapter.onClick(v, this.getLayoutPosition())
        }

        init {
            title = itemView.findViewById<TextView>(R.id.header)
            secondary = itemView.findViewById<TextView>(R.id.secondaryHeader)
            handle = itemView.findViewById<ImageView>(R.id.handleForDrag)
            cv = itemView.findViewById(R.id.trackItemDraggable)
            //iv = itemView.findViewById(R.id.play_button_item_drag);
            playAnimation = itemView.findViewById(R.id.song_playing_animation)
            itemView.findViewById<View>(R.id.more).setOnClickListener(this)
            itemView.findViewById<View>(R.id.trackItemDraggable).setOnClickListener(this)
        }
    }

    companion object {
        private val dataItems: ArrayList<dataItem?> = ArrayList<dataItem?>()
    }

    init {
        mDragStartListener = dragStartListener
        if (MyApp.Companion.getService() == null) {
            UtilityFun.restartApp()
            return
        }
        playerService = MyApp.Companion.getService()
        handler = Handler(Looper.getMainLooper())
        fillData()
        position = playerService.getCurrentTrackPosition()
        this.context = context
        inflater = LayoutInflater.from(context)
        //setHasStableIds(true);
    }
}