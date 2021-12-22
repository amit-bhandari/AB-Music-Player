package com.music.player.bhandari.m.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.util.SparseBooleanArray
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.snackbar.Snackbar
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.BubbleTextGetter
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper
import com.music.player.bhandari.m.activity.ActivitySecondaryLibrary
import com.music.player.bhandari.m.activity.ActivityTagEditor
import com.music.player.bhandari.m.activity.FragmentLibrary
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.MusicLibrary
import com.music.player.bhandari.m.model.dataItem
import com.music.player.bhandari.m.service.PlayerService
import com.music.player.bhandari.m.utils.UtilityFun
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
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
/**
 * Congrats you are reading worst piece of code I have ever written
 */
class MainLibraryAdapter(
    fl: FragmentLibrary?,
    private val context: Context,
    data: ArrayList<dataItem>
) : RecyclerView.Adapter<MainLibraryAdapter.MyViewHolder?>(), PopupMenu.OnMenuItemClickListener,
    FastScrollRecyclerView.SectionedAdapter, BubbleTextGetter {
    private var inflater: LayoutInflater?
    private var fl: FragmentLibrary?
    private var popup: PopupMenu? = null
    private val id_list = ArrayList<Int>()
    private val playerService: PlayerService?
    private var position = 0
    private var dataItems: ArrayList<dataItem> = ArrayList()
    private val filteredDataItems: ArrayList<dataItem> = ArrayList()
    private val mItemHeight = 0
    private var mLastClickTime: Long = 0
    private var batmanDrawable: Drawable? = null
    private var viewParent: View? = null
    private val selectedItems: SparseBooleanArray = SparseBooleanArray()

    fun filter(searchQuery: String) {
        if (searchQuery != "") {
            filteredDataItems.clear()
            when (fl!!.getStatus()) {
                Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT, Constants.FRAGMENT_STATUS.GENRE_FRAGMENT, Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT -> for (d: dataItem in dataItems) {
                    if (d.title.lowercase(Locale.getDefault()).contains(searchQuery)) {
                        filteredDataItems.add(d)
                    }
                }
                Constants.FRAGMENT_STATUS.TITLE_FRAGMENT -> for (d: dataItem in dataItems) {
                    if ((d.title.lowercase(Locale.getDefault()).contains(searchQuery)
                                || d.artist_name.lowercase(Locale.getDefault()).contains(searchQuery)
                                || d.albumName.lowercase(Locale.getDefault()).contains(searchQuery))
                    ) {
                        filteredDataItems.add(d)
                    }
                }
            }
        } else {
            filteredDataItems.clear()
            filteredDataItems.addAll(dataItems)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val parentView: View = inflater!!.inflate(R.layout.fragment_library_item, parent, false)
        viewParent = parent
        val color: Int = ColorHelper.getPrimaryTextColor()
        val subColor: Int = ColorHelper.getSecondaryTextColor()
        ((parentView.findViewById<View>(R.id.header)) as TextView).setTextColor(color)
        ((parentView.findViewById<View>(R.id.secondaryHeader)) as TextView).setTextColor(subColor)
        ((parentView.findViewById<View>(R.id.count)) as TextView).setTextColor(subColor)
        ((parentView.findViewById<View>(R.id.menuPopup)) as ImageView).setColorFilter(color)
        return MyViewHolder(parentView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val url = MusicLibrary.instance!!.artistUrls[filteredDataItems[position].artist_name]
        when (fl!!.getStatus()) {
            Constants.FRAGMENT_STATUS.TITLE_FRAGMENT -> {
                var builder: RequestBuilder<Drawable?>? = null
                if (!MyApp.getPref()!!.getBoolean(context.getString(R.string.pref_data_saver), false)) builder = Glide
                    .with(context)
                    .load(url)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(100, 100)
                    .placeholder(batmanDrawable)
                Glide
                    .with(context)
                    .load(MusicLibrary.instance!!.getAlbumArtUri(filteredDataItems[position].album_id))
                    .error(builder)
                    .placeholder(batmanDrawable)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .signature(ObjectKey(System.currentTimeMillis().toString()))
                    .override(100, 100)
                    .into(holder.image)
                holder.title.text = filteredDataItems[position].title
                holder.count.text = UtilityFun.msToString(filteredDataItems[position].duration!!.toInt()
                    .toLong())
                val secText: String =
                    filteredDataItems[position].artist_name + " | " + filteredDataItems[position].albumName
                holder.secondary.text = secText
            }
            Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT -> {
                Glide.with(context)
                    .load(MusicLibrary.instance!!.getAlbumArtUri(filteredDataItems[position].album_id))
                    .placeholder(batmanDrawable)
                    .into(holder.image)
                holder.title.text = filteredDataItems[position].albumName
                val trackCount: Int = filteredDataItems[position].numberOfTracks
                val trackCoun: String
                if (trackCount > 1) trackCoun =
                    trackCount.toString() + context.getString(R.string.tracks) else trackCoun =
                    trackCount.toString() + context.getString(R.string.track)
                val albumSecondaryString: StringBuilder = StringBuilder()
                    .append(filteredDataItems[position].artist_name)
                    .append(" | ")
                    .append(trackCoun)
                holder.secondary.text = albumSecondaryString
            }
            Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT -> {
                //holder.wrapper.setVisibility(View.GONE);
                holder.title.text = filteredDataItems[position].artist_name
                val tracksCount: Int = filteredDataItems[position].numberOfTracks
                val albumCount: Int = filteredDataItems[position].numberOfAlbums
                val stringBuilder = StringBuilder()
                    .append(tracksCount).append(context.getString(R.string.tracks))
                    .append(" | ").append(albumCount)
                    .append(context.getString(R.string.albums))
                holder.secondary.text = stringBuilder
                holder.title.setPadding(20, 0, 0, 0)
                holder.secondary.setPadding(20, 0, 0, 0)
                if (!MyApp.getPref()!!.getBoolean(context.getString(R.string.pref_data_saver), false)
                ) {
                    Log.d("MainLibraryAdapter",
                        "onBindViewHolder: " + filteredDataItems[position].artist_name + ":" + url)
                    Glide
                        .with(context)
                        .load(url)
                        .placeholder(R.drawable.person_blue)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .override(100, 100)
                        .into(holder.image)
                } else {
                    holder.image.setBackgroundResource(R.drawable.person_blue)
                }
            }
            Constants.FRAGMENT_STATUS.GENRE_FRAGMENT -> {
                holder.wrapper.visibility = View.GONE
                holder.title.text = filteredDataItems[position].title
                holder.title.setPadding(20, 0, 0, 0)
            }
        }
        holder.itemView.isActivated = selectedItems.get(position, false)
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

    fun clear() {
        id_list.clear()
        inflater = null
        fl = null
        popup = null
    }

    fun onClick(view: View, position: Int) {
        this.position = position
        when (view.id) {
            R.id.libraryItem -> {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                Executors.newSingleThreadExecutor().execute(object : Runnable {
                    override fun run() {
                        var title = ""
                        var key = 0
                        when (fl!!.getStatus()) {
                            Constants.FRAGMENT_STATUS.TITLE_FRAGMENT -> {
                                if (MyApp.isLocked()) {
                                    Handler(context.mainLooper).post { //Toast.makeText(context,"Music is Locked!",Toast.LENGTH_SHORT).show();
                                        Snackbar.make(viewParent!!,
                                            context.getString(R.string.music_is_locked),
                                            Snackbar.LENGTH_SHORT).show()
                                    }
                                    return
                                }
                                Play()
                            }
                            Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT -> {
                                title = filteredDataItems[position].albumName
                                key = filteredDataItems[position].album_id
                            }
                            Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT -> {
                                title = filteredDataItems[position].artist_name
                                key = filteredDataItems[position].artist_id
                            }
                            Constants.FRAGMENT_STATUS.GENRE_FRAGMENT -> {
                                title = filteredDataItems[position].title
                                key = filteredDataItems[position].id
                            }
                        }
                        if (fl!!.getStatus() !== Constants.FRAGMENT_STATUS.TITLE_FRAGMENT) {
                            val intent = Intent(context, ActivitySecondaryLibrary::class.java)
                            intent.putExtra("status", fl!!.getStatus())
                            intent.putExtra("key", key)
                            intent.putExtra("title", title.trim { it <= ' ' })
                            (context as Activity).overridePendingTransition(R.anim.slide_in_right,
                                R.anim.slide_out_left)
                            context.startActivity(intent)
                        }
                    }
                })
            }
            R.id.menuPopup -> {
                popup = PopupMenu(context, view)
                val inflater: MenuInflater = popup!!.menuInflater
                inflater.inflate(R.menu.menu_tracks_by_title, popup!!.menu)
                if (fl!!.getStatus() !== Constants.FRAGMENT_STATUS.TITLE_FRAGMENT) {
                    popup!!.menu.removeItem(R.id.action_set_as_ringtone)
                    popup!!.menu.removeItem(R.id.action_track_info)
                    popup!!.menu.removeItem(R.id.action_edit_track_info)
                }
                popup!!.menu.removeItem(R.id.action_exclude_folder)
                popup!!.show()
                popup!!.setOnMenuItemClickListener(this)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_play -> {
                if (MyApp.isLocked()) {
                    //Toast.makeText(context,"Music is Locked!",Toast.LENGTH_SHORT).show();
                    Snackbar.make(viewParent!!,
                        context.getString(R.string.music_is_locked),
                        Snackbar.LENGTH_SHORT).show()
                    return true
                }
                Play()
            }
            R.id.action_add_to_playlist -> AddToPlaylist()
            R.id.action_share -> try {
                Share()
            } catch (e: Exception) {
                //Toast.makeText(context,"Something wrong!",Toast.LENGTH_LONG).show();
                Snackbar.make(viewParent!!,
                    context.getString(R.string.error_unable_to_share),
                    Snackbar.LENGTH_SHORT).show()
            }
            R.id.action_delete -> DeleteDialog()
            R.id.action_play_next -> AddToQ(Constants.ADD_TO_Q.IMMEDIATE_NEXT)
            R.id.action_add_to_q -> AddToQ(Constants.ADD_TO_Q.AT_LAST)
            R.id.action_set_as_ringtone -> UtilityFun.SetRingtone(context,
                filteredDataItems[position].file_path,
                filteredDataItems[position].id)
            R.id.action_track_info -> setTrackInfoDialog()
            R.id.action_edit_track_info -> {
                context.startActivity(Intent(context, ActivityTagEditor::class.java)
                    .putExtra("from", Constants.TAG_EDITOR_LAUNCHED_FROM.MAIN_LIB)
                    .putExtra("file_path", filteredDataItems[position].file_path)
                    .putExtra("track_title", filteredDataItems[position].title)
                    .putExtra("position", position)
                    .putExtra("id", filteredDataItems[position].id))
                (context as Activity).overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left)
            }
            R.id.action_search_youtube -> UtilityFun.LaunchYoutube(context,
                (filteredDataItems[position].artist_name + " - "
                        + filteredDataItems[position].title))
        }
        return true
    }

    fun sort(sort_id: Int) {
        val sort_order: Int = MyApp.getPref()!!.getInt(context.resources.getString(R.string.pref_order_by), Constants.SORT_BY.ASC)
        when (sort_id) {
            Constants.SORT_BY.NAME -> if (sort_order == Constants.SORT_BY.ASC) {
                filteredDataItems.sortWith { o1, o2 ->
                    o1.title.compareTo(o2.title,
                        ignoreCase = true)
                }
            } else {
                filteredDataItems.sortWith { o1, o2 ->
                    o2.title.compareTo(o1.title,
                        ignoreCase = true)
                }
            }
            Constants.SORT_BY.SIZE -> when (sort_order) {
                Constants.SORT_BY.ASC -> {
                    filteredDataItems.sortWith { o1, o2 -> (File(o1.file_path).length() - File(o2.file_path).length()).toInt() }
                }
                else -> {
                    filteredDataItems.sortWith { o1, o2 ->
                        (File(o2.file_path).length() - File(o1.file_path).length()).toInt()
                    }
                }
            }
            Constants.SORT_BY.YEAR -> when (sort_order) {
                Constants.SORT_BY.ASC -> {
                    filteredDataItems.sortWith { o1, o2 ->
                        o1.year.compareTo(o2.year,
                            ignoreCase = true)
                    }
                }
                else -> {
                    filteredDataItems.sortWith { o1, o2 ->
                        o2.year.compareTo(o1.year,
                            ignoreCase = true)
                    }
                }
            }
            Constants.SORT_BY.NO_OF_ALBUMS -> when (sort_order) {
                Constants.SORT_BY.ASC -> {
                    filteredDataItems.sortWith { o1, o2 -> o1.numberOfAlbums - o2.numberOfAlbums }
                }
                else -> {
                    filteredDataItems.sortWith { o1, o2 -> o2.numberOfAlbums - o1.numberOfAlbums }
                }
            }
            Constants.SORT_BY.NO_OF_TRACKS -> when (sort_order) {
                Constants.SORT_BY.ASC -> {
                    filteredDataItems.sortWith { o1, o2 -> o1.numberOfTracks - o2.numberOfTracks }
                }
                else -> {
                    filteredDataItems.sortWith { o1, o2 -> o2.numberOfTracks - o1.numberOfTracks }
                }
            }
            Constants.SORT_BY.DURATION -> when (sort_order) {
                Constants.SORT_BY.ASC -> {
                    filteredDataItems.sortWith { o1, o2 ->
                        Integer.valueOf(o1.duration) - Integer.valueOf(o2.duration)
                    }
                }
                else -> {
                    filteredDataItems.sortWith { o1, o2 ->
                        Integer.valueOf(o2.duration) - Integer.valueOf(o1.duration)
                    }
                }
            }
        }
        notifyDataSetChanged()
    }

    private fun setTrackInfoDialog() {
        val linear = LinearLayout(context)
        linear.orientation = LinearLayout.VERTICAL
        val text = TextView(context)
        text.setTypeface(TypeFaceHelper.getTypeFace(context))
        text.text = UtilityFun.trackInfoBuild(filteredDataItems[position].id).toString()
        text.setPadding(20, 20, 20, 10)
        text.textSize = 15f
        text.setTypeface(TypeFaceHelper.getTypeFace(context))
        //text.setGravity(Gravity.CENTER);
        linear.addView(text)
//        MyDialogBuilder(context)
//            .title(context.getString(R.string.track_info_title))
//            .customView(linear, true)
//            .positiveText(R.string.okay)
//            .show()
    }

    private fun Play() {
        if (playerService == null) return
        when (fl!!.getStatus()) {
            Constants.FRAGMENT_STATUS.TITLE_FRAGMENT -> {
                if (playerService!!.getStatus() === playerService.PLAYING) playerService!!.pause()
                id_list.clear()
                for (d: dataItem in filteredDataItems) {
                    id_list.add(d.id)
                }
                playerService!!.setTrackList(id_list)
                playerService!!.playAtPosition(position)
            }
            Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT -> {
                val album_id: Int = filteredDataItems[position].album_id
                playerService!!.setTrackList(MusicLibrary.instance!!
                    .getSongListFromAlbumIdNew(album_id, Constants.SORT_ORDER.ASC))
            }
            Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT -> {
                val artist_id: Int = filteredDataItems[position].artist_id
                playerService!!.setTrackList(MusicLibrary.instance!!
                    .getSongListFromArtistIdNew(artist_id, Constants.SORT_ORDER.ASC))
            }
            Constants.FRAGMENT_STATUS.GENRE_FRAGMENT -> {
                val genre_id: Int = filteredDataItems[position].id
                playerService!!.setTrackList(MusicLibrary.instance!!
                    .getSongListFromGenreIdNew(genre_id, Constants.SORT_ORDER.ASC))
            }
        }
        if (fl!!.getStatus() !== Constants.FRAGMENT_STATUS.TITLE_FRAGMENT) {
            playerService!!.playAtPosition(0)
        }
    }

    private fun AddToPlaylist() {
        val ids: IntArray
        val temp: ArrayList<Int>
        when (fl!!.getStatus()) {
            Constants.FRAGMENT_STATUS.TITLE_FRAGMENT -> {
                ids = intArrayOf(filteredDataItems[position].id)
                UtilityFun.AddToPlaylist(context, ids)
            }
            Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT -> {
                val album_id: Int = filteredDataItems[position].album_id
                temp = MusicLibrary.instance!!.getSongListFromAlbumIdNew(album_id, Constants.SORT_ORDER.ASC)!!
                ids = IntArray(temp.size)
                var i = 0
                while (i < ids.size) {
                    ids[i] = temp[i]
                    i++
                }
                UtilityFun.AddToPlaylist(context, ids)
            }
            Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT -> {
                val artist_id: Int = filteredDataItems[position].artist_id
                temp = MusicLibrary.instance!!.getSongListFromArtistIdNew(artist_id, Constants.SORT_ORDER.ASC)!!
                ids = IntArray(temp.size)
                var i = 0
                while (i < ids.size) {
                    ids[i] = temp[i]
                    i++
                }
                UtilityFun.AddToPlaylist(context, ids)
            }
            Constants.FRAGMENT_STATUS.GENRE_FRAGMENT -> {
                val genre_id: Int = filteredDataItems[position].id
                temp = MusicLibrary.instance!!.getSongListFromGenreIdNew(genre_id, Constants.SORT_ORDER.ASC)!!
                ids = IntArray(temp.size)
                var i = 0
                while (i < ids.size) {
                    ids[i] = temp[i]
                    i++
                }
                UtilityFun.AddToPlaylist(context, ids)
            }
        }
        //
    }

    private fun Share() {
        val files: ArrayList<Uri> = ArrayList() //for sending multiple files
        when (fl!!.getStatus()) {
            Constants.FRAGMENT_STATUS.TITLE_FRAGMENT -> try {
                val fileToBeShared = File(filteredDataItems[position].file_path)
                files.add(FileProvider.getUriForFile(context,
                    context.applicationContext.packageName + "com.bhandari.music.provider",
                    fileToBeShared))
                UtilityFun.Share(context, files, filteredDataItems[position].title)
            } catch (e: IllegalArgumentException) {
                try {
                    UtilityFun.ShareFromPath(context, filteredDataItems[position].file_path!!)
                } catch (ex: Exception) {
                    Snackbar.make(viewParent!!, R.string.error_unable_to_share, Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
            Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT -> {
                val album_id: Int = filteredDataItems[position].album_id
                for (id: Int in MusicLibrary.instance!!.getSongListFromAlbumIdNew(album_id, Constants.SORT_ORDER.ASC)!!) {
                    val file = File(MusicLibrary.instance!!.getTrackItemFromId(id)!!.getFilePath())
                    val fileUri: Uri = FileProvider.getUriForFile(context,
                        context.applicationContext.packageName + "com.bhandari.music.provider",
                        file)
                    files.add(fileUri)
                }
            }
            Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT -> {
                val artist_id: Int = filteredDataItems[position].artist_id
                for (id: Int in MusicLibrary.instance!!.getSongListFromArtistIdNew(artist_id, Constants.SORT_ORDER.ASC)!!) {
                    val file = File(MusicLibrary.instance!!.getTrackItemFromId(id)!!.getFilePath())
                    val fileUri: Uri = FileProvider.getUriForFile(context,
                        context.applicationContext.packageName + "com.bhandari.music.provider",
                        file)
                    files.add(fileUri)
                }
            }
            Constants.FRAGMENT_STATUS.GENRE_FRAGMENT -> {
                val genre_id: Int = filteredDataItems[position].id
                for (id: Int in MusicLibrary.instance!!.getSongListFromGenreIdNew(genre_id, Constants.SORT_ORDER.ASC)!!) {
                    val file = File(MusicLibrary.instance!!.getTrackItemFromId(id)!!.getFilePath())
                    val fileUri: Uri = FileProvider.getUriForFile(context,
                        context.applicationContext.packageName + "com.bhandari.music.provider",
                        file)
                    files.add(fileUri)
                }
            }
        }
        if (fl!!.getStatus() !== Constants.FRAGMENT_STATUS.TITLE_FRAGMENT) {
            UtilityFun.Share(context, files, "Multiple audio files")
        }
    }

    private fun AddToQ(positionToAdd: Int) {
        //we are using same function for adding to q and playing next
        // toastString is to identify which string to disokay as toast
        val toastString =
            (if (positionToAdd == Constants.ADD_TO_Q.AT_LAST) context.getString(R.string.added_to_q) else context.getString(
                R.string.playing_next))
        //when adding to playing next, order of songs should be desc
        //and asc for adding at last
        //this is how the function in player service is writte, deal with it
        val sortOrder =
            (if (positionToAdd == Constants.ADD_TO_Q.AT_LAST) Constants.SORT_ORDER.ASC else Constants.SORT_ORDER.DESC)
        when (fl!!.getStatus()) {
            Constants.FRAGMENT_STATUS.TITLE_FRAGMENT -> {
                playerService!!.addToQ(filteredDataItems[position].id, positionToAdd)
                //Toast.makeText(context
                //      ,toastString+filteredDataItems.get(position).title
                ///    ,Toast.LENGTH_SHORT).show();
                Snackbar.make(viewParent!!,
                    toastString + filteredDataItems[position].title,
                    Snackbar.LENGTH_SHORT).show()
            }
            Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT -> {
                val album_id: Int = filteredDataItems[position].album_id
                for (id: Int in MusicLibrary.instance!!.getSongListFromAlbumIdNew(album_id, sortOrder)!!) {
                    playerService!!.addToQ(id, positionToAdd)
                }
                //Toast.makeText(context
                //      ,toastString+filteredDataItems.get(position).title
                //    ,Toast.LENGTH_SHORT).show();
                Snackbar.make(viewParent!!,
                    toastString + filteredDataItems[position].title,
                    Snackbar.LENGTH_SHORT).show()
            }
            Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT -> {
                val artist_id: Int = filteredDataItems[position].artist_id
                for (id: Int in MusicLibrary.instance!!.getSongListFromArtistIdNew(artist_id, sortOrder)!!) {
                    playerService!!.addToQ(id, positionToAdd)
                }
                /*Toast.makeText(context
                        ,toastString+filteredDataItems.get(position).title
                        ,Toast.LENGTH_SHORT).show();*/Snackbar.make(viewParent!!,
                    toastString + filteredDataItems[position].title,
                    Snackbar.LENGTH_SHORT).show()
            }
            Constants.FRAGMENT_STATUS.GENRE_FRAGMENT -> {
                val genre_id: Int = filteredDataItems[position].id
                for (id: Int in MusicLibrary.instance!!.getSongListFromGenreIdNew(genre_id, sortOrder)!!) {
                    playerService!!.addToQ(id, positionToAdd)
                }
                /*Toast.makeText(context
                        ,toastString+filteredDataItems.get(position).title
                        ,Toast.LENGTH_SHORT).show();*/Snackbar.make(viewParent!!,
                    toastString + filteredDataItems[position].title,
                    Snackbar.LENGTH_SHORT).show()
            }
        }

        //to update the to be next field in notification
        MyApp.getService()!!.PostNotification()
    }

    private fun DeleteDialog() {
//        MyDialogBuilder(context)
//            .title(context.getString(R.string.are_u_sure))
//            .positiveText(R.string.yes)
//            .negativeText(R.string.no)
//            .onPositive(object : SingleButtonCallback() {
//                fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                    val files: ArrayList<File> = ArrayList<File>()
//                    val ids = ArrayList<Int>()
//                    val tracklist: ArrayList<Int>
//                    when (fl.getStatus()) {
//                        Constants.FRAGMENT_STATUS.TITLE_FRAGMENT -> {
//
//
//                            //delete the file first
//                            files.add(File(filteredDataItems[position].file_path))
//                            ids.add(filteredDataItems[position].id)
//                            if (UtilityFun.Delete(context, files, ids)) {
//                                deleteSuccess()
//                            } else {
//                                Snackbar.make(viewParent,
//                                    context.getString(R.string.unable_to_del),
//                                    Snackbar.LENGTH_SHORT).show()
//                            }
//                        }
//                        Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT -> {
//                            tracklist = MusicLibrary.instance!!.getSongListFromAlbumIdNew(
//                                filteredDataItems[position].album_id, Constants.SORT_ORDER.ASC)
//                            for (id: Int in tracklist) {
//                                val item: TrackItem =
//                                    MusicLibrary.instance!!.getTrackItemFromId(id)
//                                if (item != null) {
//                                    files.add(File(item.getFilePath()))
//                                    ids.add(item.id)
//                                }
//                            }
//                            if (UtilityFun.Delete(context, files, ids)) {
//                                //Toast.makeText(context, "Deleted " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
//                                deleteSuccess()
//                            } else {
//                                //Toast.makeText(context, "Cannot delete " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
//                                Snackbar.make(viewParent,
//                                    context.getString(R.string.unable_to_del),
//                                    Snackbar.LENGTH_SHORT).show()
//                            }
//                        }
//                        Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT -> {
//                            tracklist = MusicLibrary.instance!!.getSongListFromArtistIdNew(
//                                filteredDataItems[position].artist_id, Constants.SORT_ORDER.ASC)
//                            for (id: Int in tracklist) {
//                                /*if(playerService!!.getCurrentTrack().getTitle().equals(track)){
//                                        ///Toast.makeText(context,"One of the song is playing currently",Toast.LENGTH_SHORT).show();
//                                        Snackbar.make(viewParent, "One of the song is playing currently", Snackbar.LENGTH_SHORT).show();
//                                        return;
//                                    }*/
//                                val item: TrackItem =
//                                    MusicLibrary.instance!!.getTrackItemFromId(id)
//                                if (item != null) {
//                                    files.add(File(item.getFilePath()))
//                                    ids.add(item.id)
//                                }
//                            }
//                            if (UtilityFun.Delete(context, files, ids)) {
//                                //Toast.makeText(context, "Deleted " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
//                                deleteSuccess()
//                            } else {
//                                //Toast.makeText(context, "Cannot delete " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
//                                Snackbar.make(viewParent,
//                                    context.getString(R.string.unable_to_del),
//                                    Snackbar.LENGTH_SHORT).show()
//                            }
//                        }
//                        Constants.FRAGMENT_STATUS.GENRE_FRAGMENT -> {
//                            tracklist = MusicLibrary.instance!!.getSongListFromGenreIdNew(
//                                filteredDataItems[position].id, Constants.SORT_ORDER.ASC)
//                            for (id: Int in tracklist) {
//                                /*if(playerService!!.getCurrentTrack().getTitle().equals(track)){
//                                        //Toast.makeText(context,"One of the song is playing currently",Toast.LENGTH_SHORT).show();
//                                        Snackbar.make(viewParent, "One of the song is playing currently", Snackbar.LENGTH_SHORT).show();
//                                        return;
//                                    }*/
//                                val item: TrackItem =
//                                    MusicLibrary.instance!!.getTrackItemFromId(id)
//                                if (item != null) {
//                                    files.add(File(item.getFilePath()))
//                                    ids.add(item.id)
//                                }
//                            }
//                            if (UtilityFun.Delete(context, files, ids)) {
//                                //Toast.makeText(context, "Deleted " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
//                                deleteSuccess()
//                            } else {
//                                //Toast.makeText(context, "Cannot delete " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
//                                Snackbar.make(viewParent,
//                                    context.getString(R.string.unable_to_del),
//                                    Snackbar.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                }
//
//                private fun deleteSuccess() {
//                    Snackbar.make(viewParent,
//                        context.getString(R.string.deleted) + filteredDataItems[position].title,
//                        Snackbar.LENGTH_SHORT).show()
//                    playerService!!.removeTrack(filteredDataItems[position].id)
//                    dataItems.remove(dataItems[position])
//                    filteredDataItems.remove(filteredDataItems[position])
//                    notifyItemRemoved(position)
//                    //notifyDataSetChanged();
//                }
//            })
//            .show()
    }

    fun updateItem(position: Int, vararg param: String) {
        if (param.size == 1) {
            filteredDataItems[position].title = param[0]
            notifyItemChanged(position)
        } else {
            filteredDataItems[position].title = param[0]
            filteredDataItems[position].artist_name = param[1]
            filteredDataItems[position].albumName = param[2]
            notifyItemChanged(position)
        }
    }

    fun getHeight(): Int {
        return mItemHeight
    }

    override fun getSectionName(position: Int): String {
        return filteredDataItems[position].title.substring(0, 1).uppercase(Locale.getDefault())
    }

    //action mode related methods
    //methods for item selection
    fun toggleSelection(pos: Int) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos)
        } else {
            selectedItems.put(pos, true)
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
        val items: MutableList<Int> = ArrayList<Int>(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            items.add(selectedItems.keyAt(i))
        }
        return items
    }

    override fun getTextToShowInBubble(pos: Int): String {
        return filteredDataItems[pos].title.substring(0, 1).uppercase(Locale.getDefault())
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var title: TextView = itemView.findViewById<TextView>(R.id.header)
        var secondary: TextView = itemView.findViewById<TextView>(R.id.secondaryHeader)
        var count: TextView = itemView.findViewById<TextView>(R.id.count)
        var image: ImageView = itemView.findViewById<ImageView>(R.id.imageVIewForStubAlbumArt)
        var wrapper: View = itemView.findViewById(R.id.album_art_wrapper)
        override fun onClick(v: View) {
            this@MainLibraryAdapter.onClick(v, this.layoutPosition)
        }

        init {
            itemView.setOnClickListener(this)
            itemView.findViewById<View>(R.id.menuPopup).setOnClickListener(this)
        }
    }

    init {
        inflater = LayoutInflater.from(context)
        this.fl = fl
        dataItems = data
        filteredDataItems.addAll(dataItems)
        playerService = MyApp.getService()
        when (MyApp.getPref()!!.getInt(context.getString(R.string.pref_default_album_art), 0)) {
            0 -> batmanDrawable = ContextCompat.getDrawable(context, R.drawable.ic_batman_1)
            1 -> batmanDrawable = UtilityFun.defaultAlbumArtDrawable
        }
        setHasStableIds(true)
    }
}