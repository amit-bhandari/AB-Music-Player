package com.music.player.bhandari.m.adapter

import android.content.Context
import android.view.View
import android.widget.PopupMenu
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
class PlaylistLibraryAdapter constructor(private val context: Context) :
    RecyclerView.Adapter<PlaylistLibraryAdapter.MyViewHolder?>(),
    PopupMenu.OnMenuItemClickListener {
    private var headers: ArrayList<String>
    private val inflater: LayoutInflater
    private var position: Int = 0
    private val playerService: PlayerService
    private var viewParent: View? = null
    fun clear() {}
    fun refreshPlaylistList() {
        headers = PlaylistManager.getInstance(MyApp.Companion.getContext()).getSystemPlaylistsList()
        notifyDataSetChanged()
    }

    public override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = inflater.inflate(R.layout.fragment_playlist_item, parent, false)
        viewParent = parent
        val holder: MyViewHolder = MyViewHolder(view)
        //int color = ColorHelper.getBaseThemeTextColor() ;
        ((view.findViewById<View>(R.id.header)) as TextView).setTextColor(ColorHelper.getPrimaryTextColor())
        ((view.findViewById<View>(R.id.secondaryHeader)) as TextView).setTextColor(ColorHelper.getSecondaryTextColor())
        ((view.findViewById<View>(R.id.count)) as TextView).setTextColor(ColorHelper.getSecondaryTextColor())
        ((view.findViewById<View>(R.id.menuPopup)) as ImageView).setColorFilter(ColorHelper.getSecondaryTextColor())
        return holder
    }

    public override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.setText(headers.get(position))
        holder.title.setPadding(20, 0, 0, 0)
        val count: Long = PlaylistManager.getInstance(MyApp.Companion.getContext())
            .getTrackCountFromCache(headers.get(position))
        if (count != 0L) {
            holder.count.setText(context.getString(R.string.track_count, count.toString()))
        } else {
            holder.count.setText(context.getString(R.string.empty_playlist))
        }
        holder.count.setPadding(20, 0, 0, 0)
    }

    public override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.action_play -> {
                if (MyApp.Companion.isLocked()) {
                    //Toast.makeText(context,"Music is Locked!",Toast.LENGTH_SHORT).show();
                    Snackbar.make(viewParent,
                        context.getString(R.string.music_is_locked),
                        Snackbar.LENGTH_SHORT).show()
                    return true
                }
                Play()
            }
            R.id.action_share -> Share()
            R.id.action_delete -> Delete()
            R.id.action_play_next -> AddToQ(Constants.ADD_TO_Q.IMMEDIATE_NEXT)
            R.id.action_add_to_q -> AddToQ(Constants.ADD_TO_Q.AT_LAST)
            R.id.action_clear_playlist -> if (PlaylistManager.getInstance(MyApp.Companion.getContext())
                    .ClearPlaylist(headers.get(position))
            ) {
                Snackbar.make(viewParent,
                    context.getString(R.string.snack_cleared) + " " + headers.get(position),
                    Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(viewParent,
                    context.getString(R.string.snack_unable_to_Clear) + " " + headers.get(position),
                    Snackbar.LENGTH_SHORT).show()
            }
        }
        return true
    }

    private fun Play() {
        val temp: ArrayList<dataItem> = PlaylistManager.getInstance(MyApp.Companion.getContext())
            .GetPlaylist(headers.get(position))
        val trackList: ArrayList<Int> = ArrayList()
        for (d: dataItem in temp) {
            trackList.add(d.id)
        }
        if (!trackList.isEmpty()) {
            playerService.setTrackList(trackList)
            playerService.playAtPosition(0)
            /*
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent()
                    .setAction(Constants.ACTION.PLAY_AT_POSITION)
                    .putExtra("position",0));*/
        } else {
            //Toast.makeText(context,"empty playlist",Toast.LENGTH_SHORT).show();
            Snackbar.make(viewParent,
                context.getString(R.string.empty_play_list),
                Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun Share() {
        val files: ArrayList<Uri> = ArrayList<Uri>() //for sending multiple files
        val temp: ArrayList<dataItem> = PlaylistManager.getInstance(MyApp.Companion.getContext())
            .GetPlaylist(headers.get(position))
        val trackList: ArrayList<Int> = ArrayList()
        for (d: dataItem in temp) {
            trackList.add(d.id)
        }
        for (id: Int in trackList) {
            try {
                val file: File =
                    File(MusicLibrary.getInstance().getTrackItemFromId(id).getFilePath())
                val fileUri: Uri = FileProvider.getUriForFile(context,
                    context.getApplicationContext()
                        .getPackageName() + "com.bhandari.music.provider",
                    file)
                files.add(fileUri)
            } catch (e: Exception) {
                //Toast.makeText(context,"Something wrong!",Toast.LENGTH_LONG).show();
                Snackbar.make(viewParent,
                    context.getString(R.string.error_something_wrong),
                    Snackbar.LENGTH_SHORT).show()
                return
            }
        }
        if (!files.isEmpty()) {
            val intent: Intent = Intent()
            intent.setAction(Intent.ACTION_SEND_MULTIPLE)
            intent.setType("*/*")
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
            context.startActivity(Intent.createChooser(intent, "multiple audio files"))
        } else {
            //Toast.makeText(context,"empty playlist",Toast.LENGTH_SHORT).show();
            Snackbar.make(viewParent,
                context.getString(R.string.empty_play_list),
                Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun AddToQ(positionToAdd: Int) {
        //we are using same function for adding to q and playing next
        // toastString is to identify which string to disokay as toast
        val toastString: String =
            (if (positionToAdd == Constants.ADD_TO_Q.AT_LAST) context.getString(R.string.added_to_q) else context.getString(
                R.string.playing_next))
        //when adding to playing next, order of songs should be desc
        //and asc for adding at last
        //this is how the function in player service is writte, deal with it
        val sortOrder: Int =
            (if (positionToAdd == Constants.ADD_TO_Q.AT_LAST) Constants.SORT_ORDER.ASC else Constants.SORT_ORDER.DESC)
        val temp: ArrayList<dataItem> =
            PlaylistManager.getInstance(context).GetPlaylist(headers.get(position))
        val trackList: ArrayList<Int> = ArrayList()
        for (d: dataItem in temp) {
            trackList.add(d.id)
        }
        if (!trackList.isEmpty()) {
            for (id: Int in trackList) {
                playerService.addToQ(id, positionToAdd)
            }
            //to update the to be next field in notification
            MyApp.Companion.getService().PostNotification()

            /*Toast.makeText(context
                    , toastString + headers.get(position)
                    , Toast.LENGTH_SHORT).show();*/Snackbar.make(viewParent,
                toastString + headers.get(position),
                Snackbar.LENGTH_SHORT).show()
        } else {
            //Toast.makeText(context,"empty playlist",Toast.LENGTH_SHORT).show();
            Snackbar.make(viewParent,
                context.getString(R.string.empty_play_list),
                Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun Delete() {
        MyDialogBuilder(context)
            .title(context.getString(R.string.are_u_sure))
            .positiveText(R.string.yes)
            .negativeText(R.string.no)
            .onPositive(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    if (((headers.get(position) == Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED) || (headers.get(
                            position) == Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED) || (headers.get(
                            position) == Constants.SYSTEM_PLAYLISTS.MOST_PLAYED) || (headers.get(
                            position) == Constants.SYSTEM_PLAYLISTS.MY_FAV))
                    ) {
                        //Toast.makeText(context,"Cannot delete "+headers.get(position),Toast.LENGTH_SHORT).show();
                        Snackbar.make(viewParent,
                            context.getString(R.string.cannot_del) + headers.get(position),
                            Snackbar.LENGTH_SHORT).show()
                        return
                    }
                    if (PlaylistManager.getInstance(MyApp.Companion.getContext())
                            .DeletePlaylist(headers.get(position))
                    ) {
                        //Toast.makeText(context,"Deleted "+headers.get(position),Toast.LENGTH_SHORT).show();
                        Snackbar.make(viewParent,
                            context.getString(R.string.deleted) + headers.get(position),
                            Snackbar.LENGTH_SHORT).show()
                        headers.remove(headers.get(position))
                        notifyDataSetChanged()
                    } else {
                        //Toast.makeText(context,"Cannot delete "+headers.get(position),Toast.LENGTH_SHORT).show();
                        Snackbar.make(viewParent,
                            context.getString(R.string.cannot_del) + headers.get(position),
                            Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
            .show()
    }

    public override fun getItemCount(): Int {
        return headers.size
    }

    fun onClick(view: View, position: Int) {
        this.position = position
        when (view.getId()) {
            R.id.libraryItem -> {
                val intent: Intent = Intent(context, ActivitySecondaryLibrary::class.java)
                intent.putExtra("status", Constants.FRAGMENT_STATUS.PLAYLIST_FRAGMENT)
                intent.putExtra("title", headers.get(position).trim({ it <= ' ' }))
                context.startActivity(intent)
                (context as Activity).overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left)
            }
            R.id.menuPopup -> {
                val popup: PopupMenu = PopupMenu(context, view)
                val inflater: MenuInflater = popup.getMenuInflater()
                inflater.inflate(R.menu.system_playlist_menu, popup.getMenu())
                popup.show()
                popup.setOnMenuItemClickListener(this)
            }
        }
    }

    public override fun getItemViewType(position: Int): Int {
        return position
    }

    public override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class MyViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var title: TextView
        var count: TextView
        public override fun onClick(view: View) {
            this@PlaylistLibraryAdapter.onClick(view, getLayoutPosition())
        }

        init {
            title = itemView.findViewById<TextView>(R.id.header)
            count = itemView.findViewById<TextView>(R.id.secondaryHeader)
            itemView.findViewById<View>(R.id.album_art_wrapper).setVisibility(View.GONE)
            itemView.setOnClickListener(this)
            itemView.findViewById<View>(R.id.menuPopup).setOnClickListener(this)
        }
    }

    init {
        //create first page for folder fragment
        val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        inflater = LayoutInflater.from(context)
        headers = PlaylistManager.getInstance(MyApp.Companion.getContext()).getSystemPlaylistsList()
        headers.addAll(PlaylistManager.getInstance(MyApp.Companion.getContext())
            .getUserCreatedPlaylistList())
        playerService = MyApp.Companion.getService()
        setHasStableIds(true)
    }
}