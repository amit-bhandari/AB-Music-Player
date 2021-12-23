package com.music.player.bhandari.m.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Parcelable
import android.text.format.Formatter
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.BubbleTextGetter
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper
import com.music.player.bhandari.m.activity.ActivityMain
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.MusicLibrary
import com.music.player.bhandari.m.service.PlayerService
import com.music.player.bhandari.m.utils.UtilityFun
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import java.io.File
import java.io.FilenameFilter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

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
class FolderLibraryAdapter constructor(private val context: Context) :
    RecyclerView.Adapter<FolderLibraryAdapter.MyViewHolder?>(), PopupMenu.OnMenuItemClickListener,
    FastScrollRecyclerView.SectionedAdapter, BubbleTextGetter {
    private val files: LinkedHashMap<String?, File> = LinkedHashMap() //for getting file from inflated list string value
    private val headers: ArrayList<String?> = ArrayList() //for inflating list
    private val filteredHeaders: ArrayList<String?> = ArrayList()
    private var inflater: LayoutInflater?
    private var clickedItemPosition: Int = 0
    private var clickedFile: File? = null
    private lateinit var viewParent: View
    private val playerService: PlayerService
    private var backPressedOnce: Boolean = false

    //for restoring state on coming back to folder list
    private var recyclerViewState: Parcelable? = null
    private var rv: RecyclerView? = null

    fun clear() {
        headers.clear()
        filteredHeaders.clear()
        inflater = null
        files.clear()
    }

    private fun initializeFirstPage() {
        headers.clear()
        filteredHeaders.clear()
        files.clear()
        //list all the folders having songs
        for (path: String in MusicLibrary.instance.foldersList) {
            if ((path == Environment.getExternalStorageDirectory().absolutePath)) {
                continue
            }
            val file = File(path)
            if (file.canRead()) {
                files[file.name] = file
            }
        }
        headers.addAll(files.keys)

        // add songs which are in sdcard
        val sd = File(Environment.getExternalStorageDirectory().absolutePath)
        try {
            for (f: File in sd.listFiles()) {
                if (!f.isDirectory && isFileExtensionValid(f)) {
                    files[f.name] = f
                    headers.add(f.name)
                }
            }
        } catch (ignored: Exception) {
        }
        filteredHeaders.addAll(headers)
        Collections.sort<String>(filteredHeaders)
        notifyDataSetChanged()
        isHomeFolder = true
        when {
            rv != null && rv!!.layoutManager != null -> rv!!.layoutManager!!.onRestoreInstanceState(recyclerViewState)
        }
    }

    private fun isFileExtensionValid(f: File): Boolean {
        return (f.name.endsWith("mp3") || f.name.endsWith("wav") || f.name
            .endsWith("aac") ||
                f.name.endsWith("flac") || f.name.endsWith("wma") || f.name
            .endsWith("m4a"))
    }

    private fun isFileExtensionValid(name: String): Boolean {
        return (name.endsWith("mp3") || name.endsWith("wav") || name.endsWith("aac") ||
                name.endsWith("flac") || name.endsWith("wma") || name.endsWith("m4a"))
    }

    fun filter(searchQuery: String) {
        if (!(searchQuery == "")) {
            filteredHeaders.clear()
            for (s: String? in headers) {
                if (s!!.lowercase(Locale.getDefault()).contains(searchQuery.lowercase(Locale.getDefault()))) {
                    filteredHeaders.add(s)
                }
            }
        } else {
            filteredHeaders.clear()
            filteredHeaders.addAll(headers)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = inflater!!.inflate(R.layout.fragment_library_item, parent, false)
        viewParent = parent
        //stub=((ViewStub)view.findViewById(R.id.stub_in_fragment_library_item)).inflate();
        val holder: MyViewHolder = MyViewHolder(view)
        //int color = ColorHelper.getPrimaryTextColor() ;
        ((view.findViewById<View>(R.id.header)) as TextView).setTextColor(ColorHelper.getPrimaryTextColor())
        ((view.findViewById<View>(R.id.secondaryHeader)) as TextView).setTextColor(ColorHelper.getSecondaryTextColor())
        ((view.findViewById<View>(R.id.count)) as TextView).setTextColor(ColorHelper.getSecondaryTextColor())
        ((view.findViewById<View>(R.id.menuPopup)) as ImageView).setColorFilter(ColorHelper.getSecondaryTextColor())
        return holder
    }

    private fun refreshList(fNavigate: File?) {
        files.clear()
        headers.clear()
        filteredHeaders.clear()
        //previousPath=fNavigate;
        if (fNavigate!!.canRead()) {
            for (f: File in fNavigate.listFiles()) {
                if (f.isFile && (isFileExtensionValid(f))) {
                    files[f.name] = f
                }
            }
            headers.addAll(files.keys)
        }
        filteredHeaders.addAll(headers)
        Collections.sort<String>(filteredHeaders)
        notifyDataSetChanged()
    }

    fun onStepBack() {
        if (isHomeFolder) {
            if (backPressedOnce) {
                (context as ActivityMain).finish()
                return
            }
            backPressedOnce = true
            Toast.makeText(context, R.string.press_twice_exit, Toast.LENGTH_SHORT).show()
            Handler().postDelayed({ backPressedOnce = false }, 2000)
            return
        }
        initializeFirstPage()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val fileName: String? = filteredHeaders.get(position)
        val positionalFile: File? = files.get(fileName)
        if (fileName == null || positionalFile == null) {
            return
        }
        holder.title.text = fileName
        if (positionalFile.isDirectory) {
            holder.image.setBackgroundDrawable(context.resources
                .getDrawable(R.drawable.ic_folder_special_black_24dp))
            try {
                holder.secondary.text = positionalFile.listFiles(FilenameFilter { dir, name ->
                    Log.d("FolderLibraryAdapter", "accept: $dir")
                    isFileExtensionValid(name)
                }).size.toString() + context.getString(R.string.tracks)
            } catch (e: NullPointerException) {
                Log.d("FolderLibraryAdapter", "onBindViewHolder: ")
            }
        } else {
            holder.image.setBackgroundDrawable(context.resources
                .getDrawable(R.drawable.ic_audiotrack_black_24dp))
            holder.secondary.text = Formatter.formatFileSize(MyApp.getContext(), positionalFile.length())
        }
    }

    override fun getItemCount(): Int {
        return filteredHeaders.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        rv = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        rv = null
    }

    fun onClick(view: View, position: Int) {
        Log.d("FolderLibraryAdapter", "onClick: " + position)
        clickedItemPosition = position
        clickedFile = files.get(filteredHeaders.get(clickedItemPosition))
        if (clickedFile == null) return
        when (view.id) {
            R.id.libraryItem -> when {
                clickedFile!!.isDirectory -> {
                    //update list here
                    if (rv!!.layoutManager != null) recyclerViewState =
                        rv!!.layoutManager!!.onSaveInstanceState()
                    refreshList(clickedFile)
                    isHomeFolder = false
                }
                else -> {
                    if (MyApp.isLocked()) {
                        //Toast.makeText(context,"Music is Locked!",Toast.LENGTH_SHORT).show();
                        Snackbar.make(viewParent,
                            context.getString(R.string.music_is_locked),
                            Snackbar.LENGTH_SHORT).show()
                        return
                    }
                    Play()
                }
            }
            R.id.menuPopup -> {
                val popup = PopupMenu(context, view)
                val inflater: MenuInflater = popup.menuInflater
                inflater.inflate(R.menu.menu_tracks_by_title, popup.menu)
                //popup.getMenu().removeItem(R.id.action_delete);
                popup.menu.removeItem(R.id.action_edit_track_info)
                when {
                    clickedFile!!.isDirectory -> {
                        popup.menu.removeItem(R.id.action_set_as_ringtone)
                    }
                    else -> {
                        popup.menu.removeItem(R.id.action_exclude_folder)
                    }
                }
                popup.show()
                popup.setOnMenuItemClickListener(this)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (clickedFile == null) {
            return false
        }
        when (item.itemId) {
            R.id.action_play -> {
                if (MyApp.isLocked()) {
                    //Toast.makeText(context,"Music is Locked!",Toast.LENGTH_SHORT).show();
                    Snackbar.make(viewParent,
                        context.getString(R.string.music_is_locked),
                        Snackbar.LENGTH_SHORT).show()
                    return true
                }
                Play()
            }
            R.id.action_add_to_playlist -> AddToPlaylist()
            R.id.action_share -> Share()
            R.id.action_play_next -> AddToQ(Constants.ADD_TO_Q.IMMEDIATE_NEXT)
            R.id.action_add_to_q -> AddToQ(Constants.ADD_TO_Q.AT_LAST)
            R.id.action_set_as_ringtone -> {
                val abPath = files[filteredHeaders[clickedItemPosition]]!!.absolutePath
                UtilityFun.SetRingtone(context,
                    abPath,
                    MusicLibrary.instance.getIdFromFilePath(abPath))
            }
            R.id.action_track_info -> setTrackInfoDialog()
            R.id.action_delete -> delete()
            R.id.action_exclude_folder -> excludeFolder()
            R.id.action_search_youtube -> UtilityFun.LaunchYoutube(context, filteredHeaders[clickedItemPosition]!!)
        }
        return true
    }

    private fun excludeFolder() {
        MyApp.getPref().edit()
            .putString(MyApp.getContext().getString(R.string.pref_excluded_folders),
                MyApp.getPref().getString(MyApp.getContext().getString(R.string.pref_excluded_folders),
                    "") + clickedFile!!.absolutePath + ",").apply()
        try {
            files.remove(clickedFile!!.name)
            filteredHeaders.remove(clickedFile!!.name)
            headers.remove(clickedFile!!.name)
            notifyItemRemoved(clickedItemPosition)
        } catch (ignored: ArrayIndexOutOfBoundsException) {
        }
        MusicLibrary.instance.RefreshLibrary()
    }

    private fun setTrackInfoDialog() {


        //final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        //alert.setTitle(context.getString(R.string.track_info_title) );
        val linear = LinearLayout(context)
        linear.orientation = LinearLayout.VERTICAL
        val text = TextView(context)
        text.typeface = TypeFaceHelper.getTypeFace(context)
        when {
            clickedFile!!.isFile -> {
                val id: Int =
                    MusicLibrary.instance.getIdFromFilePath(clickedFile!!.absolutePath)
                when {
                    id != -1 -> {
                        text.text = UtilityFun.trackInfoBuild(id).toString()
                    }
                    else -> {
                        text.text = context.getString(R.string.no_info_available)
                    }
                }
            }
            else -> {
                val info: String = "File path : " + clickedFile!!.absolutePath
                text.text = info
            }
        }
        text.setPadding(20, 20, 20, 10)
        text.textSize = 15f
        //text.setGravity(Gravity.CENTER);
        linear.addView(text)
        //alert.setView(linear);
        //alert.setPositiveButton(context.getString(R.string.okay) , null);
        //alert.show();
//        MyDialogBuilder(context)
//            .title(context.getString(R.string.track_info_title))
//            .customView(linear, true)
//            .positiveText(R.string.okay)
//            .show()
    }

    private fun Play() {
        when {
            clickedFile!!.isFile -> {
                val fileList: Array<File> = clickedFile!!.parentFile.listFiles()
                val songTitles: ArrayList<Int> = ArrayList()
                var i = 0
                var original_file_index = 0
                for (f: File in fileList) {
                    if (isFileExtensionValid(f)) {
                        val id: Int = MusicLibrary.instance.getIdFromFilePath(f.absolutePath)
                        songTitles.add(id)
                        if ((f == clickedFile)) {
                            original_file_index = i
                        }
                        i++
                    }
                }
                if (songTitles.isEmpty()) {
                    Snackbar.make(viewParent,
                        context.getString(R.string.nothing_to_play),
                        Snackbar.LENGTH_SHORT).show()
                    return
                }
                playerService.setTrackList(songTitles)
                playerService.playAtPosition(original_file_index)
            }
            else -> {
                val fileList: Array<File>? = clickedFile!!.listFiles()
                val songTitles: ArrayList<Int> = ArrayList()
                if (fileList != null) {
                    for (f: File in fileList) {
                        if (isFileExtensionValid(f)) {
                            val id: Int =
                                MusicLibrary.instance.getIdFromFilePath(f.absolutePath)
                            songTitles.add(id)
                        }
                    }
                }
                if (songTitles.isEmpty()) {
                    Snackbar.make(viewParent,
                        context.getString(R.string.nothing_to_play),
                        Snackbar.LENGTH_SHORT).show()
                    return
                }
                playerService.setTrackList(songTitles)
                playerService.playAtPosition(0)
            }
        }
    }

    private fun AddToPlaylist() {
        val temp: ArrayList<Int> = ArrayList()
        val ids: IntArray
        when {
            clickedFile!!.isFile -> {
                val id: Int =
                    MusicLibrary.instance.getIdFromFilePath(clickedFile!!.absolutePath)
                ids = IntArray(temp.size)
                for (i in ids.indices) {
                    ids[i] = temp.get(i)
                }
                UtilityFun.AddToPlaylist(context, ids)
            }
            else -> {
                val fileList: Array<File> = clickedFile!!.listFiles()
                for (f: File in fileList) {
                    if (isFileExtensionValid(f)) {
                        val id: Int = MusicLibrary.instance.getIdFromFilePath(f.absolutePath)
                        temp.add(id)
                    }
                }
                if (temp.isEmpty()) {
                    //Toast.makeText(context,"Nothing to add!",Toast.LENGTH_LONG).show();
                    Snackbar.make(viewParent, "Nothing to add!", Snackbar.LENGTH_SHORT).show()
                    return
                }
                ids = IntArray(temp.size)
                for (i in ids.indices) {
                    ids[i] = temp[i]
                }
                UtilityFun.AddToPlaylist(context, ids)
            }
        }
    }

    private fun Share() {
        try {
            val files: ArrayList<Uri> = ArrayList() //for sending multiple files
            when {
                clickedFile!!.isFile -> {
                    files.add(
                        FileProvider.getUriForFile(context,
                            context.applicationContext
                                .packageName + "com.bhandari.music.provider",
                            clickedFile!!))
                }
                else -> {
                    val fileList: Array<File> = clickedFile!!.listFiles()
                    for (f: File in fileList) {
                        if (isFileExtensionValid(f)) {
                            files.add(
                                FileProvider.getUriForFile(context,
                                    context.applicationContext
                                        .packageName + "com.bhandari.music.provider",
                                    f))
                        }
                    }
                }
            }
            UtilityFun.Share(context, files, "music")
        } catch (e: IllegalArgumentException) {
            try {
                when {
                    clickedFile!!.isFile -> {
                        UtilityFun.ShareFromPath(context, clickedFile!!.absolutePath)
                    }
                    else -> {
                        throw Exception()
                    }
                }
            } catch (ex: Exception) {
                Snackbar.make(viewParent, R.string.error_unable_to_share, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun AddToQ(positionToAdd: Int) {
        //we are using same function for adding to q and playing next
        // toastString is to identify which string to disokay as toast
        val toastString = when (positionToAdd) {
                Constants.ADD_TO_Q.AT_LAST -> context.getString(R.string.added_to_q)
                else -> context.getString(
                    R.string.playing_next)
            }
        if (!clickedFile!!.isFile) {
            val fileList: Array<File> = clickedFile!!.listFiles()
            for (f: File in fileList) {
                if (isFileExtensionValid(f)) {
                    val id: Int = MusicLibrary.instance.getIdFromFilePath(f.absolutePath)
                    playerService.addToQ(id, positionToAdd)
                }
            }
            /*Toast.makeText(context
                        ,toastString+clickedFile.getName()
                        ,Toast.LENGTH_SHORT).show();*/Snackbar.make(viewParent,
                toastString + clickedFile!!.name,
                Snackbar.LENGTH_SHORT).show()
        } else {
            val id: Int =
                MusicLibrary.instance.getIdFromFilePath(clickedFile!!.absolutePath)
            playerService.addToQ(id, positionToAdd)
            /*Toast.makeText(context
                        ,toastString+title
                        ,Toast.LENGTH_SHORT).show();*/
            Snackbar.make(viewParent,
                toastString + clickedFile!!.name,
                Snackbar.LENGTH_SHORT).show()
        }

        //to update the to be next field in notification
        MyApp.getService()!!.PostNotification()
    }

    private fun delete() {

        /*AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.are_u_sure))
                .setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                .setNegativeButton(context.getString(R.string.no), dialogClickListener).show();*/
        MaterialDialog(context)
            .title(text = context.getString(R.string.are_u_sure))
            .positiveButton(R.string.yes){
                val files = ArrayList<File>()
                when {
                    clickedFile!!.isFile -> {
                        files.add(clickedFile!!)
                    }
                    else -> {
                        files.addAll(mutableListOf(*clickedFile!!.listFiles()))
                        files.add(clickedFile!!)
                    }
                }
                when {
                    UtilityFun.Delete(context, files, null) -> {
                        deleteSuccess()
                        Toast.makeText(context,
                            context.getString(R.string.deleted),
                            Toast.LENGTH_SHORT).show()
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

    private fun deleteSuccess() {
        try {
            files.remove(clickedFile!!.name)
            filteredHeaders.remove(clickedFile!!.name)
            headers.remove(clickedFile!!.name)
            notifyItemRemoved(clickedItemPosition)
        } catch (ignored: ArrayIndexOutOfBoundsException) {
        }
    }

    override fun getSectionName(position: Int): String {
        return filteredHeaders[position]!!.substring(0, 1).uppercase(Locale.getDefault())
    }

    override fun getTextToShowInBubble(pos: Int): String {
        return filteredHeaders[pos]!!.substring(0, 1).uppercase(Locale.getDefault())
    }

    inner class MyViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var title: TextView = itemView.findViewById(R.id.header)
        var secondary: TextView = itemView.findViewById(R.id.secondaryHeader)
        var image: ImageView
        override fun onClick(v: View) {
            this@FolderLibraryAdapter.onClick(v, layoutPosition)
        }

        init {
            itemView.findViewById<View>(R.id.album_art_wrapper).visibility = View.INVISIBLE
            image = itemView.findViewById(R.id.imageVIewForFolderLib)
            image.visibility = View.VISIBLE
            itemView.setOnClickListener(this)
            itemView.findViewById<View>(R.id.menuPopup).setOnClickListener(this)
        }
    }

    companion object {
        private var isHomeFolder: Boolean = true
    }

    init {
        //create first page for folder fragment
        inflater = LayoutInflater.from(context)
        initializeFirstPage()
        /*if(context instanceof ActivityMain){
            recyclerView=((ActivityMain) context).findViewById(R.id.recyclerviewList);
        }*/
        playerService = MyApp.getService()!!
    }
}