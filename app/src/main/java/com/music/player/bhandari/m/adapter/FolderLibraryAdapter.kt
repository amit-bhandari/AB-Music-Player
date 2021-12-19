package com.music.player.bhandari.m.adapter

import android.content.Context
import android.os.Handler
import android.text.format.Formatter
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
class FolderLibraryAdapter constructor(private val context: Context) :
    RecyclerView.Adapter<FolderLibraryAdapter.MyViewHolder?>(), PopupMenu.OnMenuItemClickListener,
    SectionedAdapter, BubbleTextGetter {
    private val files: LinkedHashMap<String?, File> =
        LinkedHashMap<String?, File>() //for getting file from inflated list string value
    private val headers: ArrayList<String?> = ArrayList() //for inflating list
    private val filteredHeaders: ArrayList<String?> = ArrayList()
    private var inflater: LayoutInflater?
    private var clickedItemPosition: Int = 0
    private var clickedFile: File? = null
    private var viewParent: View? = null
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
        for (path: String in MusicLibrary.getInstance().getFoldersList()) {
            if ((path == Environment.getExternalStorageDirectory().getAbsolutePath())) {
                continue
            }
            val file: File = File(path)
            if (file.canRead()) {
                files.put(file.getName(), file)
            }
        }
        headers.addAll(files.keys)

        // add songs which are in sdcard
        val sd: File = File(Environment.getExternalStorageDirectory().getAbsolutePath())
        try {
            for (f: File in sd.listFiles()) {
                if (!f.isDirectory() && isFileExtensionValid(f)) {
                    files.put(f.getName(), f)
                    headers.add(f.getName())
                }
            }
        } catch (ignored: Exception) {
        }
        filteredHeaders.addAll(headers)
        Collections.sort<String>(filteredHeaders)
        notifyDataSetChanged()
        isHomeFolder = true
        if (rv != null && rv.getLayoutManager() != null) rv.getLayoutManager()
            .onRestoreInstanceState(recyclerViewState)
    }

    private fun isFileExtensionValid(f: File): Boolean {
        return (f.getName().endsWith("mp3") || f.getName().endsWith("wav") || f.getName()
            .endsWith("aac") ||
                f.getName().endsWith("flac") || f.getName().endsWith("wma") || f.getName()
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
                if (s!!.toLowerCase().contains(searchQuery.toLowerCase())) {
                    filteredHeaders.add(s)
                }
            }
        } else {
            filteredHeaders.clear()
            filteredHeaders.addAll(headers)
        }
        notifyDataSetChanged()
    }

    public override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = inflater.inflate(R.layout.fragment_library_item, parent, false)
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
        if (fNavigate.canRead()) {
            for (f: File in fNavigate.listFiles()) {
                if (f.isFile() && (isFileExtensionValid(f))) {
                    files.put(f.getName(), f)
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
            Handler().postDelayed(object : Runnable {
                public override fun run() {
                    backPressedOnce = false
                }
            }, 2000)
            return
        }
        initializeFirstPage()
    }

    @SuppressLint("SetTextI18n")
    public override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val fileName: String? = filteredHeaders.get(position)
        val positionalFile: File? = files.get(fileName)
        if (fileName == null || positionalFile == null) {
            return
        }
        holder.title.setText(fileName)
        if (positionalFile.isDirectory()) {
            holder.image.setBackgroundDrawable(context.getResources()
                .getDrawable(R.drawable.ic_folder_special_black_24dp))
            try {
                holder.secondary.setText(positionalFile.listFiles(object : FilenameFilter {
                    public override fun accept(dir: File, name: String): Boolean {
                        Log.d("FolderLibraryAdapter", "accept: " + dir)
                        return isFileExtensionValid(name)
                    }
                }).size.toString() + context.getString(R.string.tracks))
            } catch (e: NullPointerException) {
                Log.d("FolderLibraryAdapter", "onBindViewHolder: ")
            }
        } else {
            holder.image.setBackgroundDrawable(context.getResources()
                .getDrawable(R.drawable.ic_audiotrack_black_24dp))
            holder.secondary.setText(Formatter.formatFileSize(MyApp.Companion.getContext(),
                positionalFile.length()))
        }
    }

    public override fun getItemCount(): Int {
        return filteredHeaders.size
    }

    public override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        rv = recyclerView
    }

    public override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        rv = null
    }

    fun onClick(view: View, position: Int) {
        Log.d("FolderLibraryAdapter", "onClick: " + position)
        clickedItemPosition = position
        clickedFile = files.get(filteredHeaders.get(clickedItemPosition))
        if (clickedFile == null) return
        when (view.getId()) {
            R.id.libraryItem -> if (clickedFile.isDirectory()) {
                //update list here
                if (rv.getLayoutManager() != null) recyclerViewState =
                    rv.getLayoutManager().onSaveInstanceState()
                refreshList(clickedFile)
                isHomeFolder = false
            } else {
                if (MyApp.Companion.isLocked()) {
                    //Toast.makeText(context,"Music is Locked!",Toast.LENGTH_SHORT).show();
                    Snackbar.make(viewParent,
                        context.getString(R.string.music_is_locked),
                        Snackbar.LENGTH_SHORT).show()
                    return
                }
                Play()
            }
            R.id.menuPopup -> {
                val popup: PopupMenu = PopupMenu(context, view)
                val inflater: MenuInflater = popup.getMenuInflater()
                inflater.inflate(R.menu.menu_tracks_by_title, popup.getMenu())
                //popup.getMenu().removeItem(R.id.action_delete);
                popup.getMenu().removeItem(R.id.action_edit_track_info)
                if (clickedFile.isDirectory()) {
                    popup.getMenu().removeItem(R.id.action_set_as_ringtone)
                } else {
                    popup.getMenu().removeItem(R.id.action_exclude_folder)
                }
                popup.show()
                popup.setOnMenuItemClickListener(this)
            }
        }
    }

    public override fun onMenuItemClick(item: MenuItem): Boolean {
        if (clickedFile == null) {
            return false
        }
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
            R.id.action_add_to_playlist -> AddToPlaylist()
            R.id.action_share -> Share()
            R.id.action_play_next -> AddToQ(Constants.ADD_TO_Q.IMMEDIATE_NEXT)
            R.id.action_add_to_q -> AddToQ(Constants.ADD_TO_Q.AT_LAST)
            R.id.action_set_as_ringtone -> {
                val abPath: String =
                    files.get(filteredHeaders.get(clickedItemPosition)).getAbsolutePath()
                UtilityFun.SetRingtone(context,
                    abPath,
                    MusicLibrary.getInstance().getIdFromFilePath(abPath))
            }
            R.id.action_track_info -> setTrackInfoDialog()
            R.id.action_delete -> Delete()
            R.id.action_exclude_folder -> excludeFolder()
            R.id.action_search_youtube -> UtilityFun.LaunchYoutube(context,
                filteredHeaders.get(clickedItemPosition))
        }
        return true
    }

    private fun excludeFolder() {
        MyApp.Companion.getPref().edit()
            .putString(MyApp.Companion.getContext().getString(R.string.pref_excluded_folders),
                MyApp.Companion.getPref().getString(MyApp.Companion.getContext()
                    .getString(R.string.pref_excluded_folders),
                    "") + clickedFile.getAbsolutePath() + ",").apply()
        try {
            files.remove(clickedFile.getName())
            filteredHeaders.remove(clickedFile.getName())
            headers.remove(clickedFile.getName())
            notifyItemRemoved(clickedItemPosition)
        } catch (ignored: ArrayIndexOutOfBoundsException) {
        }
        MusicLibrary.getInstance().RefreshLibrary()
    }

    private fun setTrackInfoDialog() {


        //final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        //alert.setTitle(context.getString(R.string.track_info_title) );
        val linear: LinearLayout = LinearLayout(context)
        linear.setOrientation(LinearLayout.VERTICAL)
        val text: TextView = TextView(context)
        text.setTypeface(TypeFaceHelper.getTypeFace(context))
        if (clickedFile.isFile()) {
            val id: Int =
                MusicLibrary.getInstance().getIdFromFilePath(clickedFile.getAbsolutePath())
            if (id != -1) {
                text.setText(UtilityFun.trackInfoBuild(id).toString())
            } else {
                text.setText(context.getString(R.string.no_info_available))
            }
        } else {
            val info: String = "File path : " + clickedFile.getAbsolutePath()
            text.setText(info)
        }
        text.setPadding(20, 20, 20, 10)
        text.setTextSize(15f)
        //text.setGravity(Gravity.CENTER);
        linear.addView(text)
        //alert.setView(linear);
        //alert.setPositiveButton(context.getString(R.string.okay) , null);
        //alert.show();
        MyDialogBuilder(context)
            .title(context.getString(R.string.track_info_title))
            .customView(linear, true)
            .positiveText(R.string.okay)
            .show()
    }

    private fun Play() {
        if (clickedFile.isFile()) {
            val fileList: Array<File> = clickedFile.getParentFile().listFiles()
            val songTitles: ArrayList<Int> = ArrayList()
            var i: Int = 0
            var original_file_index: Int = 0
            for (f: File in fileList) {
                if (isFileExtensionValid(f)) {
                    val id: Int = MusicLibrary.getInstance().getIdFromFilePath(f.getAbsolutePath())
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
        } else {
            val fileList: Array<File>? = clickedFile.listFiles()
            val songTitles: ArrayList<Int> = ArrayList()
            if (fileList != null) {
                for (f: File in fileList) {
                    if (isFileExtensionValid(f)) {
                        val id: Int =
                            MusicLibrary.getInstance().getIdFromFilePath(f.getAbsolutePath())
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

    private fun AddToPlaylist() {
        val temp: ArrayList<Int> = ArrayList()
        val ids: IntArray
        if (clickedFile.isFile()) {
            val id: Int =
                MusicLibrary.getInstance().getIdFromFilePath(clickedFile.getAbsolutePath())
            ids = IntArray(temp.size)
            for (i in ids.indices) {
                ids.get(i) = temp.get(i)
            }
            UtilityFun.AddToPlaylist(context, ids)
        } else {
            val fileList: Array<File> = clickedFile.listFiles()
            for (f: File in fileList) {
                if (isFileExtensionValid(f)) {
                    val id: Int = MusicLibrary.getInstance().getIdFromFilePath(f.getAbsolutePath())
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
                ids.get(i) = temp.get(i)
            }
            UtilityFun.AddToPlaylist(context, ids)
        }
    }

    private fun Share() {
        try {
            val files: ArrayList<Uri> = ArrayList<Uri>() //for sending multiple files
            if (clickedFile.isFile()) {
                files.add(
                    FileProvider.getUriForFile(context,
                        context.getApplicationContext()
                            .getPackageName() + "com.bhandari.music.provider",
                        clickedFile))
            } else {
                val fileList: Array<File> = clickedFile.listFiles()
                for (f: File in fileList) {
                    if (isFileExtensionValid(f)) {
                        files.add(
                            FileProvider.getUriForFile(context,
                                context.getApplicationContext()
                                    .getPackageName() + "com.bhandari.music.provider",
                                f))
                    }
                }
            }
            UtilityFun.Share(context, files, "music")
        } catch (e: IllegalArgumentException) {
            try {
                if (clickedFile.isFile()) {
                    UtilityFun.ShareFromPath(context, clickedFile.getAbsolutePath())
                } else {
                    throw Exception()
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
        val toastString: String =
            (if (positionToAdd == Constants.ADD_TO_Q.AT_LAST) context.getString(R.string.added_to_q) else context.getString(
                R.string.playing_next))
        if (clickedFile.isFile()) {
            val id: Int =
                MusicLibrary.getInstance().getIdFromFilePath(clickedFile.getAbsolutePath())
            playerService.addToQ(id, positionToAdd)
            /*Toast.makeText(context
                    ,toastString+title
                    ,Toast.LENGTH_SHORT).show();*/Snackbar.make(viewParent,
                toastString + clickedFile.getName(),
                Snackbar.LENGTH_SHORT).show()
        } else {
            val fileList: Array<File> = clickedFile.listFiles()
            for (f: File in fileList) {
                if (isFileExtensionValid(f)) {
                    val id: Int = MusicLibrary.getInstance().getIdFromFilePath(f.getAbsolutePath())
                    playerService.addToQ(id, positionToAdd)
                }
            }
            /*Toast.makeText(context
                    ,toastString+clickedFile.getName()
                    ,Toast.LENGTH_SHORT).show();*/Snackbar.make(viewParent,
                toastString + clickedFile.getName(),
                Snackbar.LENGTH_SHORT).show()
        }

        //to update the to be next field in notification
        MyApp.Companion.getService().PostNotification()
    }

    private fun Delete() {

        /*AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.are_u_sure))
                .setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                .setNegativeButton(context.getString(R.string.no), dialogClickListener).show();*/
        MyDialogBuilder(context)
            .title(context.getString(R.string.are_u_sure))
            .positiveText(R.string.yes)
            .negativeText(R.string.no)
            .onPositive(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    val files: ArrayList<File?> = ArrayList<File?>()
                    if (clickedFile.isFile()) {
                        files.add(clickedFile)
                    } else {
                        files.addAll(Arrays.asList<File>(*clickedFile.listFiles()))
                        files.add(clickedFile)
                    }
                    if (UtilityFun.Delete(context, files, null)) {
                        deleteSuccess()
                        Toast.makeText(context,
                            context.getString(R.string.deleted),
                            Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context,
                            context.getString(R.string.unable_to_del),
                            Toast.LENGTH_SHORT).show()
                    }
                }
            })
            .show()
    }

    private fun deleteSuccess() {
        try {
            files.remove(clickedFile.getName())
            filteredHeaders.remove(clickedFile.getName())
            headers.remove(clickedFile.getName())
            notifyItemRemoved(clickedItemPosition)
        } catch (ignored: ArrayIndexOutOfBoundsException) {
        }
    }

    public override fun getSectionName(position: Int): String {
        return filteredHeaders.get(position)!!.substring(0, 1).toUpperCase()
    }

    fun getTextToShowInBubble(pos: Int): String {
        return filteredHeaders.get(pos)!!.substring(0, 1).toUpperCase()
    }

    inner class MyViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var title: TextView
        var secondary: TextView
        var image: ImageView
        public override fun onClick(v: View) {
            this@FolderLibraryAdapter.onClick(v, getLayoutPosition())
        }

        init {
            title = itemView.findViewById<TextView>(R.id.header)
            secondary = itemView.findViewById<TextView>(R.id.secondaryHeader)
            itemView.findViewById<View>(R.id.album_art_wrapper).setVisibility(View.INVISIBLE)
            image = itemView.findViewById<ImageView>(R.id.imageVIewForFolderLib)
            image.setVisibility(View.VISIBLE)
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
        }*/playerService = MyApp.Companion.getService()
    }
}