package com.music.player.bhandari.m.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.BubbleTextGetter;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.activity.ActivityMain;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.utils.UtilityFun;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 Copyright 2017 Amit Bhandari AB

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

public class FolderLibraryAdapter extends RecyclerView.Adapter<FolderLibraryAdapter.MyViewHolder>
        implements PopupMenu.OnMenuItemClickListener, FastScrollRecyclerView.SectionedAdapter, BubbleTextGetter{

    private LinkedHashMap<String,File> files =new LinkedHashMap<>();   //for getting file from inflated list string value
    private ArrayList<String> headers=new ArrayList<>();   //for inflating list
    private ArrayList<String> filteredHeaders = new ArrayList<>();

    private static boolean isHomeFolder =true;
    private Context context;
    private LayoutInflater inflater;
    private int clickedItemPosition=0;
    private File clickedFile;

    private View viewParent;

    private PlayerService playerService;
    private boolean backPressedOnce = false;

    //for restoring state on coming back to folder list
    private Parcelable recyclerViewState;
    private RecyclerView rv;

    public FolderLibraryAdapter(Context context){
        //create first page for folder fragment
        this.context=context;
        inflater=LayoutInflater.from(context);
        initializeFirstPage();
        /*if(context instanceof ActivityMain){
            recyclerView=((ActivityMain) context).findViewById(R.id.recyclerviewList);
        }*/
        playerService = MyApp.getService();
    }

    public void clear(){
        headers.clear();
        filteredHeaders.clear();
        inflater=null;
        files.clear();
    }

    private void initializeFirstPage(){
        headers.clear();
        filteredHeaders.clear();
        files.clear();
        //list all the folders having songs
        for(String path:MusicLibrary.getInstance().getFoldersList()){
            if(path.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                continue;
            }
            File file = new File(path);
            if(file.canRead()){
                files.put(file.getName(), file);
            }
        }
        headers.addAll(files.keySet());

        // add songs which are in sdcard
        File sd = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        try {
            for (File f : sd.listFiles()) {
                if (!f.isDirectory() && isFileExtensionValid(f)) {
                    files.put(f.getName(), f);
                    headers.add(f.getName());
                }
            }
        }catch (Exception ignored){

        }
        filteredHeaders.addAll(headers);
        Collections.sort(filteredHeaders);
        notifyDataSetChanged();
        isHomeFolder =true;

        if(rv!=null && rv.getLayoutManager()!=null) rv.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }

    private boolean isFileExtensionValid(File f) {
        return f.getName().endsWith("mp3") || f.getName().endsWith("wav") || f.getName().endsWith("aac") ||
        f.getName().endsWith("flac") || f.getName().endsWith("wma") || f.getName().endsWith("m4a");
    }

    private boolean isFileExtensionValid(String name) {
        return name.endsWith("mp3") || name.endsWith("wav") || name.endsWith("aac") ||
                name.endsWith("flac") || name.endsWith("wma") || name.endsWith("m4a");
    }

    public void filter (String searchQuery){
        if(!searchQuery.equals("")){
            filteredHeaders.clear();
            for(String s : headers){
                if(s.toLowerCase().contains(searchQuery.toLowerCase())){
                    filteredHeaders.add(s);
                }
            }
        }else {
            filteredHeaders.clear();
            filteredHeaders.addAll(headers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FolderLibraryAdapter.MyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.fragment_library_item, parent, false);
        viewParent = parent;
        //stub=((ViewStub)view.findViewById(R.id.stub_in_fragment_library_item)).inflate();
        final FolderLibraryAdapter.MyViewHolder holder=new FolderLibraryAdapter.MyViewHolder (view);
        //int color = ColorHelper.getPrimaryTextColor() ;
        ((TextView)(view.findViewById(R.id.header))).setTextColor(ColorHelper.getPrimaryTextColor());
        ((TextView)(view.findViewById(R.id.secondaryHeader))).setTextColor(ColorHelper.getSecondaryTextColor());
        ((TextView)(view.findViewById(R.id.count))).setTextColor(ColorHelper.getSecondaryTextColor());
        ((ImageView)(view.findViewById(R.id.menuPopup))).setColorFilter(ColorHelper.getSecondaryTextColor());

        return holder;
    }

    private void refreshList(File fNavigate){
        files.clear();
        headers.clear();
        filteredHeaders.clear();
        //previousPath=fNavigate;
        if(fNavigate.canRead()) {
            for (File f : fNavigate.listFiles()) {
                if(f.isFile() && (isFileExtensionValid(f))) {
                    files.put(f.getName(), f);
                }
            }

            headers.addAll(files.keySet());
        }
        filteredHeaders.addAll(headers);
        Collections.sort(filteredHeaders);
        notifyDataSetChanged();
    }

    public void onStepBack(){

        if(isHomeFolder){
            if(backPressedOnce){
                ((ActivityMain) context).finish();
                return;
            }

            backPressedOnce = true;
            Toast.makeText(context, R.string.press_twice_exit, Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backPressedOnce = false;
                }
            }, 2000);
            return;
        }

        initializeFirstPage();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FolderLibraryAdapter.MyViewHolder holder, int position) {

        String fileName = filteredHeaders.get(position);
        File positionalFile = files.get(fileName);

        if(fileName==null || positionalFile==null){
            return;
        }

        holder.title.setText(fileName);
        if(positionalFile.isDirectory()){
            holder.image.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ic_folder_special_black_24dp));
            try {
                holder.secondary.setText(positionalFile.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        Log.d("FolderLibraryAdapter", "accept: " + dir);
                        return isFileExtensionValid(name);
                    }
                }).length + context.getString(R.string.tracks));
            }catch (NullPointerException e) {
                Log.d("FolderLibraryAdapter", "onBindViewHolder: ");
            }
        }else{
            holder.image.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ic_audiotrack_black_24dp));
            holder.secondary.setText(android.text.format.Formatter.formatFileSize(MyApp.getContext(), positionalFile.length()));
        }

    }

    @Override
    public int getItemCount() {
        return filteredHeaders.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        rv = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        rv = null;
    }

    public void onClick(View view, int position){
        Log.d("FolderLibraryAdapter", "onClick: " + position);
        clickedItemPosition = position;
        clickedFile = files.get(filteredHeaders.get(clickedItemPosition));
        if(clickedFile==null) return;
        switch (view.getId()){
            case R.id.libraryItem:
                if(clickedFile.isDirectory()) {
                    //update list here
                    if(rv.getLayoutManager() != null) recyclerViewState = rv.getLayoutManager().onSaveInstanceState();
                    refreshList(clickedFile);
                    isHomeFolder =false;
                } else{
                    if(MyApp.isLocked()){
                        //Toast.makeText(context,"Music is Locked!",Toast.LENGTH_SHORT).show();
                        Snackbar.make(viewParent, context.getString(R.string.music_is_locked) , Snackbar.LENGTH_SHORT).show();
                        return ;
                    }
                    Play();
                }
                break;

            case R.id.menuPopup:
                PopupMenu popup = new PopupMenu(context, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_tracks_by_title, popup.getMenu());
                //popup.getMenu().removeItem(R.id.action_delete);
                popup.getMenu().removeItem(R.id.action_edit_track_info);
                if(clickedFile.isDirectory()) {
                    popup.getMenu().removeItem(R.id.action_set_as_ringtone);
                }else {
                    popup.getMenu().removeItem(R.id.action_exclude_folder);
                }
                popup.show();
                popup.setOnMenuItemClickListener(this);
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(clickedFile==null){
            return false;
        }
        switch (item.getItemId()){
            case R.id.action_play:
                if(MyApp.isLocked()){
                    //Toast.makeText(context,"Music is Locked!",Toast.LENGTH_SHORT).show();
                    Snackbar.make(viewParent, context.getString(R.string.music_is_locked) , Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                Play();
                break;

            case R.id.action_add_to_playlist:
                AddToPlaylist();
                break;

            case R.id.action_share:
                Share();
                break;

            case R.id.action_play_next:
                AddToQ(Constants.ADD_TO_Q.IMMEDIATE_NEXT);
                break;

            case R.id.action_add_to_q:
                AddToQ(Constants.ADD_TO_Q.AT_LAST);
                break;


            case R.id.action_set_as_ringtone:
                String abPath = files.get(filteredHeaders.get(clickedItemPosition)).getAbsolutePath();
                UtilityFun.SetRingtone(context, abPath
                        ,MusicLibrary.getInstance().getIdFromFilePath(abPath));
                break;

            case R.id.action_track_info:
                setTrackInfoDialog();
                break;

            case R.id.action_delete:
                Delete();
                break;

            case R.id.action_exclude_folder:
                excludeFolder();
                break;

            case R.id.action_search_youtube:
                UtilityFun.LaunchYoutube(context,filteredHeaders.get(clickedItemPosition));
        }
        return true;
    }

    private void excludeFolder(){
        MyApp.getPref().edit().putString(MyApp.getContext().getString(R.string.pref_excluded_folders)
                , MyApp.getPref().getString(MyApp.getContext().getString(R.string.pref_excluded_folders), "") + clickedFile.getAbsolutePath() + ",").apply();
        try {
            files.remove(clickedFile.getName());
            filteredHeaders.remove(clickedFile.getName());
            headers.remove(clickedFile.getName());
            notifyItemRemoved(clickedItemPosition);
        }catch (ArrayIndexOutOfBoundsException ignored){}

        MusicLibrary.getInstance().RefreshLibrary();

    }

    private void setTrackInfoDialog(){



        //final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        //alert.setTitle(context.getString(R.string.track_info_title) );
        LinearLayout linear = new LinearLayout(context);
        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text = new TextView(context);
        text.setTypeface(TypeFaceHelper.getTypeFace(context));

        if(clickedFile.isFile()){
            int id=MusicLibrary.getInstance().getIdFromFilePath(clickedFile.getAbsolutePath());
            if(id!=-1) {
                text.setText(UtilityFun.trackInfoBuild(id).toString());
            }else {
                text.setText(context.getString(R.string.no_info_available));
            }
        }else {
            String info = "File path : " + clickedFile.getAbsolutePath();
            text.setText(info);
        }

        text.setPadding(20, 20,20,10);
        text.setTextSize(15);
        //text.setGravity(Gravity.CENTER);

        linear.addView(text);
        //alert.setView(linear);
        //alert.setPositiveButton(context.getString(R.string.okay) , null);
        //alert.show();

        new MyDialogBuilder(context)
                .title(context.getString(R.string.track_info_title))
                .customView(linear, true)
                .positiveText(R.string.okay)
                .show();
    }

    private void Play(){
        if(clickedFile.isFile()) {
            File[] fileList = clickedFile.getParentFile().listFiles();
            ArrayList<Integer> songTitles = new ArrayList<>();
            int i = 0, original_file_index=0;
            for(File f:fileList){
                if(isFileExtensionValid(f)) {
                    int id = MusicLibrary.getInstance().getIdFromFilePath(f.getAbsolutePath());
                    songTitles.add(id);
                    if(f.equals(clickedFile)){
                        original_file_index = i;
                    }
                    i++;
                }
            }
            if(songTitles.isEmpty()){
                Snackbar.make(viewParent, context.getString(R.string.nothing_to_play) , Snackbar.LENGTH_SHORT).show();
                return;
            }
            playerService.setTrackList(songTitles);
            playerService.playAtPosition(original_file_index);
        }
        else {
            File[] fileList = clickedFile.listFiles();
            ArrayList<Integer> songTitles = new ArrayList<>();
            if(fileList!=null) {
                for (File f : fileList) {
                    if (isFileExtensionValid(f)) {
                        int id = MusicLibrary.getInstance().getIdFromFilePath(f.getAbsolutePath());
                        songTitles.add(id);
                    }
                }
            }
            if(songTitles.isEmpty()){
                Snackbar.make(viewParent, context.getString(R.string.nothing_to_play) , Snackbar.LENGTH_SHORT).show();
                return;
            }
            playerService.setTrackList(songTitles);
            playerService.playAtPosition(0);
        }
    }

    private void AddToPlaylist(){
        ArrayList<Integer> temp = new ArrayList<>();
        int[] ids;
        if(clickedFile.isFile()) {
            int id=MusicLibrary.getInstance().getIdFromFilePath(clickedFile.getAbsolutePath());
            ids = new int[temp.size()];
            for (int i=0; i < ids.length; i++)
            {
                ids[i] = temp.get(i);
            }
            UtilityFun.AddToPlaylist(context, ids);
        }else {
            File[] fileList = clickedFile.listFiles();
            for(File f:fileList){
                if(isFileExtensionValid(f)) {
                    int id = MusicLibrary.getInstance().getIdFromFilePath(f.getAbsolutePath());
                    temp.add(id);
                }
            }
            if(temp.isEmpty()){
                //Toast.makeText(context,"Nothing to add!",Toast.LENGTH_LONG).show();
                Snackbar.make(viewParent, "Nothing to add!", Snackbar.LENGTH_SHORT).show();
                return;
            }
            ids = new int[temp.size()];
            for (int i=0; i < ids.length; i++)
            {
                ids[i] = temp.get(i);
            }
            UtilityFun.AddToPlaylist(context, ids);
        }
    }

    private void Share(){
        try {
            ArrayList<Uri> files = new ArrayList<>();  //for sending multiple files
            if (clickedFile.isFile()) {
                files.add(
                        FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + "com.bhandari.music.provider", clickedFile));
            } else {
                File[] fileList = clickedFile.listFiles();
                for (File f : fileList) {
                    if (isFileExtensionValid(f)) {
                        files.add(
                                FileProvider.getUriForFile(context
                                        , context.getApplicationContext().getPackageName() + "com.bhandari.music.provider", f));
                    }
                }
            }
            UtilityFun.Share(context, files, "music");
        }catch (IllegalArgumentException e){
            try{
                if (clickedFile.isFile()) {
                    UtilityFun.ShareFromPath(context, clickedFile.getAbsolutePath());
                } else {
                    throw new Exception();
                }
            }catch (Exception ex) {
                Snackbar.make(viewParent, R.string.error_unable_to_share, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void AddToQ(int positionToAdd){
        //we are using same function for adding to q and playing next
        // toastString is to identify which string to disokay as toast
        String toastString=(positionToAdd==Constants.ADD_TO_Q.AT_LAST ? context.getString(R.string.added_to_q)
                : context.getString(R.string.playing_next) ) ;
        if(clickedFile.isFile()) {
            int id=MusicLibrary.getInstance().getIdFromFilePath(clickedFile.getAbsolutePath());
            playerService.addToQ(id, positionToAdd);
            /*Toast.makeText(context
                    ,toastString+title
                    ,Toast.LENGTH_SHORT).show();*/
            Snackbar.make(viewParent, toastString+clickedFile.getName(), Snackbar.LENGTH_SHORT).show();
        }else {
            File[] fileList = clickedFile.listFiles();
            for(File f:fileList){
                if(isFileExtensionValid(f)) {
                    int id = MusicLibrary.getInstance().getIdFromFilePath(f.getAbsolutePath());
                    playerService.addToQ(id, positionToAdd);
                }
            }
            /*Toast.makeText(context
                    ,toastString+clickedFile.getName()
                    ,Toast.LENGTH_SHORT).show();*/
            Snackbar.make(viewParent, toastString+clickedFile.getName(), Snackbar.LENGTH_SHORT).show();
        }

        //to update the to be next field in notification
        MyApp.getService().PostNotification();

    }

    private void Delete(){

        /*AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.are_u_sure))
                .setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                .setNegativeButton(context.getString(R.string.no), dialogClickListener).show();*/

        new MyDialogBuilder(context)
                .title(context.getString(R.string.are_u_sure))
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ArrayList<File> files = new ArrayList<>();
                        if(clickedFile.isFile()) {
                            files.add(clickedFile);
                        }else {
                            files.addAll(Arrays.asList(clickedFile.listFiles()));
                            files.add(clickedFile);
                        }

                        if(UtilityFun.Delete(context, files, null)){
                            deleteSuccess();
                            Toast.makeText(context,context.getString(R.string.deleted),Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(context,context.getString(R.string.unable_to_del),Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();

    }

    private void deleteSuccess() {
        try {
            files.remove(clickedFile.getName());
            filteredHeaders.remove(clickedFile.getName());
            headers.remove(clickedFile.getName());
            notifyItemRemoved(clickedItemPosition);
        }catch (ArrayIndexOutOfBoundsException ignored){}
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return filteredHeaders.get(position).substring(0,1).toUpperCase();
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        return filteredHeaders.get(pos).substring(0,1).toUpperCase();
    }


    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView title,secondary;
        ImageView image;

        MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.header);
            secondary = itemView.findViewById(R.id.secondaryHeader);
            itemView.findViewById(R.id.album_art_wrapper).setVisibility(View.INVISIBLE);
            image= itemView.findViewById(R.id.imageVIewForFolderLib);
            image.setVisibility(View.VISIBLE);
            itemView.setOnClickListener(this);
            itemView.findViewById(R.id.menuPopup).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            FolderLibraryAdapter.this.onClick(v,getLayoutPosition());
        }
    }
}
