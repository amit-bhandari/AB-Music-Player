package com.music.player.bhandari.m.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.BubbleTextGetter
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.activity.ActivitySecondaryLibrary
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.MusicLibrary
import com.music.player.bhandari.m.model.dataItem
import com.music.player.bhandari.m.service.PlayerService
import com.music.player.bhandari.m.utils.UtilityFun
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.io.File
import java.util.*
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
class AlbumLibraryAdapter constructor(private val context: Context, data: ArrayList<dataItem>) :
    RecyclerView.Adapter<AlbumLibraryAdapter.MyViewHolder?>(), PopupMenu.OnMenuItemClickListener,
    FastScrollRecyclerView.SectionedAdapter, BubbleTextGetter {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val playerService: PlayerService = MyApp.getService()!!
    private var position: Int = 0
    private var dataItems: ArrayList<dataItem> = ArrayList() //actual data
    private val filteredDataItems: ArrayList<dataItem> = ArrayList()
    private lateinit var viewParent: View
    private var batmanDrawable: Drawable? = null

    fun sort(sort_id: Int) {
        val sortOrder = MyApp.getPref().getInt(context.resources.getString(R.string.pref_order_by), Constants.SORT_BY.ASC)
        when (sort_id) {
            Constants.SORT_BY.NAME -> if (sortOrder == Constants.SORT_BY.ASC) {
                filteredDataItems.sortWith { o1, o2 ->
                    o1.albumName.compareTo(o2.albumName,
                        ignoreCase = true)
                }
            } else {
                filteredDataItems.sortWith { o1, o2 ->
                    o2.albumName.compareTo(o1.albumName,
                        ignoreCase = true)
                }
            }
            Constants.SORT_BY.YEAR -> if (sortOrder == Constants.SORT_BY.ASC) {
                filteredDataItems.sortWith { o1, o2 ->
                    o1.year.compareTo(o2.year,
                        ignoreCase = true)
                }
            } else {
                filteredDataItems.sortWith { o1, o2 ->
                    o2.year.compareTo(o1.year,
                        ignoreCase = true)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun filter(searchQuery: String) {
        if (searchQuery != "") {
            filteredDataItems.clear()
            for (d: dataItem in dataItems) {
                if ((d.title.lowercase(Locale.getDefault()).contains(searchQuery)
                            || d.artist_name.lowercase(Locale.getDefault()).contains(searchQuery))
                ) {
                    filteredDataItems.add(d)
                }
            }
        } else {
            filteredDataItems.clear()
            filteredDataItems.addAll(dataItems)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = inflater.inflate(R.layout.album_card, parent, false)
        (view.findViewById<View>(R.id.title) as TextView).setTextColor(ColorHelper.getPrimaryTextColor())
        (view.findViewById<View>(R.id.count) as TextView).setTextColor(ColorHelper.getSecondaryTextColor())
        (view.findViewById<View>(R.id.overflow) as ImageView).setColorFilter(ColorHelper.getSecondaryTextColor())
        (view.findViewById<View>(R.id.album_library_card) as CardView).setCardBackgroundColor(
            ColorHelper.getColor(R.color.colorTransparent))
        view.layoutParams.width = UtilityFun.screenWidth / 3
        viewParent = parent
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = filteredDataItems.get(position).albumName
        var builder: RequestBuilder<Bitmap?>? = null
        if (!MyApp.getPref().getBoolean(context.getString(R.string.pref_data_saver), false)) {
            val url = MusicLibrary.instance.artistUrls[filteredDataItems[position].artist_name]
            builder = Glide
                .with(context)
                .asBitmap()
                .load(url)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(200, 200)
                .placeholder(batmanDrawable)
        }
        val bm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MusicLibrary.instance.getAlbumArtFromTrack(filteredDataItems.get(position).album_id)
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        Glide
            .with(context)
            .asBitmap()
            .load(bm)
            .error(builder)
            .centerCrop()
            .placeholder(batmanDrawable)
            .into(holder.thumbnail)
        holder.count.text = filteredDataItems.get(position).artist_name
    }

    override fun getItemCount(): Int {
        return filteredDataItems.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun onClick(view: View, position: Int) {
        this.position = position
        val title: String
        val key: Int
        when (view.id) {
            R.id.card_view_album -> {
                title = filteredDataItems[position].albumName
                key = filteredDataItems[position].album_id
                val intent = Intent(context, ActivitySecondaryLibrary::class.java)
                intent.putExtra("status", Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT)
                intent.putExtra("key", key)
                intent.putExtra("title", title.trim { it <= ' ' })
                /*ActivityOptions options;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    options = ActivityOptions.makeSceneTransitionAnimation((ActivityMain)context
                            , view.findViewById(R.id.thumbnail), context.getString(R.string.secondary_transition));
                    ActivityCompat.startActivityForResult((ActivityMain)context, intent, 100, options.toBundle());
                }else {*/
                //((Activity) context).finish();
                context.startActivity(intent)
                (context as Activity).overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left)
            }
            R.id.overflow -> {
                val popup = PopupMenu(context, view)
                val inflater: MenuInflater = popup.menuInflater
                inflater.inflate(R.menu.menu_tracks_by_title, popup.menu)
                popup.menu.removeItem(R.id.action_set_as_ringtone)
                popup.menu.removeItem(R.id.action_track_info)
                popup.menu.removeItem(R.id.action_edit_track_info)
                popup.menu.removeItem(R.id.action_exclude_folder)
                popup.show()
                popup.setOnMenuItemClickListener(this)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
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
            R.id.action_add_to_playlist -> {
                val temp: ArrayList<Int>
                val album_id: Int = filteredDataItems[position].album_id
                temp = MusicLibrary.instance.getSongListFromAlbumIdNew(album_id, Constants.SORT_ORDER.ASC)!!
                val ids = IntArray(temp.size)
                var i = 0
                while (i < ids.size) {
                    ids[i] = temp[i]
                    i++
                }
                UtilityFun.addToPlaylist(context, ids)
            }
            R.id.action_share -> {
                val files: ArrayList<Uri> = ArrayList() //for sending multiple files
                for (id: Int in MusicLibrary.instance.getSongListFromAlbumIdNew(filteredDataItems[position].album_id, Constants.SORT_ORDER.ASC)!!) {
                    try {
                        val file = File(MusicLibrary.instance.getTrackItemFromId(id)!!.getFilePath())
                        val fileUri = FileProvider.getUriForFile(context, context.applicationContext.packageName + "com.bhandari.music.provider", file)
                        files.add(fileUri)
                    } catch (e: Exception) {
                        // Toast.makeText(context,"Something wrong!",Toast.LENGTH_LONG).show();
                        Snackbar.make(viewParent,
                            context.getString(R.string.error_something_wrong),
                            Snackbar.LENGTH_SHORT).show()
                        return true
                    }
                }
                UtilityFun.Share(context, files, "Multiple audio files")
            }
            R.id.action_delete -> delete()
            R.id.action_play_next -> AddToQ(Constants.ADD_TO_Q.IMMEDIATE_NEXT)
            R.id.action_add_to_q -> AddToQ(Constants.ADD_TO_Q.AT_LAST)
            R.id.action_search_youtube -> UtilityFun.LaunchYoutube(context,
                (filteredDataItems[position].albumName + " - "
                        + filteredDataItems[position].artist_name))
        }
        return true
    }

    private fun Play() {
        val album_id: Int = filteredDataItems[position].album_id
        playerService.setTrackList(MusicLibrary.instance.getSongListFromAlbumIdNew(album_id, Constants.SORT_ORDER.ASC))
        playerService.playAtPosition(0)
    }

    private fun AddToQ(positionToAdd: Int) {
        //we are using same function for adding to q and playing next
        // toastString is to identify which string to disokay as toast
        val toastString = when (positionToAdd) {
            Constants.ADD_TO_Q.AT_LAST -> context.getString(R.string.added_to_q)
            else -> context.getString(
                R.string.playing_next)
        }
        //when adding to playing next, order of songs should be desc
        //and asc for adding at last
        //this is how the function in player service is writte, deal with it
        val sortOrder: Int = when (positionToAdd) {
            Constants.ADD_TO_Q.AT_LAST -> Constants.SORT_ORDER.ASC
            else -> Constants.SORT_ORDER.DESC
        }
        val albumId = filteredDataItems[position].album_id
        for (id: Int in MusicLibrary.instance.getSongListFromAlbumIdNew(albumId, sortOrder)!!) {
            playerService.addToQ(id, positionToAdd)
        }

        //to update the to be next field in notification
        MyApp.getService()!!.PostNotification()
        //Toast.makeText(context
        //,toastString+filteredDataItems.get(position).title
        //  ,Toast.LENGTH_SHORT).show();
        Snackbar.make(viewParent,
            toastString + filteredDataItems[position].title,
            Snackbar.LENGTH_SHORT).show()
    }

    private fun delete() {
        MaterialDialog(context)
            .title(text = context.getString(R.string.are_u_sure))
            .positiveButton(res = R.string.yes){
                val ids: ArrayList<Int> = ArrayList()
                val files: ArrayList<File> = ArrayList()
                val tracklist = MusicLibrary.instance.getSongListFromAlbumIdNew(filteredDataItems.get(position).album_id,
                        Constants.SORT_ORDER.ASC)
                if (tracklist != null) {
                    for (id: Int in tracklist) {
                        if (playerService.getCurrentTrack()!!.id == id) {
                            //Toast.makeText(context,"One of the song is playing currently",Toast.LENGTH_SHORT).show();
                            Snackbar.make(viewParent,
                                context.getString(R.string.song_is_playing),
                                Snackbar.LENGTH_SHORT).show()
                            return@positiveButton
                        }
                        val item = MusicLibrary.instance.getTrackItemFromId(id)
                        if (item == null) {
                            // Toast.makeText(context,"Something wrong!",Toast.LENGTH_LONG).show();
                            Snackbar.make(viewParent,
                                context.getString(R.string.unable_to_del),
                                Snackbar.LENGTH_SHORT).show()
                            return@positiveButton
                        }
                        files.add(File(item.getFilePath()))
                        ids.add(item.id)
                    }
                }
                if (UtilityFun.Delete(context, files, ids)) {
                    // Toast.makeText(context, "Deleted " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
                    Snackbar.make(viewParent, context.getString(R.string.deleted) + filteredDataItems[position].title, Snackbar.LENGTH_SHORT).show()
                    dataItems.remove(dataItems[position])
                    filteredDataItems.remove(filteredDataItems[position])
                    notifyItemRemoved(position)
                    // notifyDataSetChanged();
                } else {
                    //Toast.makeText(context, "Cannot delete " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
                    Snackbar.make(viewParent, context.getString(R.string.unable_to_del), Snackbar.LENGTH_SHORT).show()
                }
            }
            .negativeButton(R.string.no)
            .show()
    }

    override fun getSectionName(position: Int): String {
        return filteredDataItems[position].albumName.substring(0, 1)
            .uppercase(Locale.getDefault())
    }

    override fun getTextToShowInBubble(pos: Int): String {
        return filteredDataItems[pos].albumName.substring(0, 1).uppercase(Locale.getDefault())
    }

    inner class MyViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var title: TextView = itemView.findViewById(R.id.title)
        var count: TextView = itemView.findViewById(R.id.count)
        var thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
        var overflow: ImageView = itemView.findViewById(R.id.overflow)

        override fun onClick(v: View) {
            this@AlbumLibraryAdapter.onClick(v, this.layoutPosition)
        }

        init {
            itemView.setOnClickListener(this)
            overflow.setOnClickListener(this)
        }
    }

    init {
        dataItems = data
        filteredDataItems.addAll(dataItems)
        setHasStableIds(true)
        when (MyApp.getPref().getInt(context.getString(R.string.pref_default_album_art), 0)) {
            0 -> batmanDrawable = ContextCompat.getDrawable(context, R.drawable.ic_batman_1)
            1 -> batmanDrawable = UtilityFun.defaultAlbumArtDrawable
        }
    }
}