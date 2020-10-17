package com.music.player.bhandari.m.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.activity.ActivitySecondaryLibrary;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.dataItem;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.model.PlaylistManager;

import java.io.File;
import java.util.ArrayList;

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

public class PlaylistLibraryAdapter extends RecyclerView.Adapter<PlaylistLibraryAdapter.MyViewHolder>
        implements PopupMenu.OnMenuItemClickListener{

    private ArrayList<String> headers;
    private Context context;
    private LayoutInflater inflater;
    private int position=0;
    private PlayerService playerService;
    private View viewParent;


    public PlaylistLibraryAdapter(Context context){
        //create first page for folder fragment
        this.context=context;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        inflater=LayoutInflater.from(context);
        headers = PlaylistManager.getInstance(MyApp.getContext()).getSystemPlaylistsList();
        headers.addAll(PlaylistManager.getInstance(MyApp.getContext()).getUserCreatedPlaylistList());
        playerService = MyApp.getService();
        setHasStableIds(true);
    }

    public void clear(){
    }

    public void refreshPlaylistList(){
        headers = PlaylistManager.getInstance(MyApp.getContext()).getSystemPlaylistsList();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistLibraryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.fragment_playlist_item, parent, false);
        viewParent = parent;
        final PlaylistLibraryAdapter.MyViewHolder holder=new PlaylistLibraryAdapter.MyViewHolder (view);
        //int color = ColorHelper.getBaseThemeTextColor() ;

        ((TextView)(view.findViewById(R.id.header))).setTextColor(ColorHelper.getPrimaryTextColor());
        ((TextView)(view.findViewById(R.id.secondaryHeader))).setTextColor(ColorHelper.getSecondaryTextColor());
        ((TextView)(view.findViewById(R.id.count))).setTextColor(ColorHelper.getSecondaryTextColor());
        ((ImageView)(view.findViewById(R.id.menuPopup))).setColorFilter(ColorHelper.getSecondaryTextColor());


        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistLibraryAdapter.MyViewHolder holder, int position) {
        holder.title.setText(headers.get(position));
        holder.title.setPadding(20,0,0,0);

        Long count = PlaylistManager.getInstance(MyApp.getContext()).getTrackCountFromCache(headers.get(position));
        if(count!=0) {
            holder.count.setText(context.getString(R.string.track_count, count.toString()));
        }else {
            holder.count.setText(context.getString(R.string.empty_playlist));
        }
        holder.count.setPadding(20, 0, 0, 0);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_play:
                if(MyApp.isLocked()){
                    //Toast.makeText(context,"Music is Locked!",Toast.LENGTH_SHORT).show();
                    Snackbar.make(viewParent, context.getString(R.string.music_is_locked), Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                Play();
                break;

            case R.id.action_share:
                Share();
                break;

            case R.id.action_delete:
                Delete();
                break;

            case R.id.action_play_next:
                AddToQ(Constants.ADD_TO_Q.IMMEDIATE_NEXT);
                break;

            case R.id.action_add_to_q:
                AddToQ(Constants.ADD_TO_Q.AT_LAST);
                break;

            case R.id.action_clear_playlist:
                if(PlaylistManager.getInstance(MyApp.getContext()).ClearPlaylist(headers.get(position))){
                    Snackbar.make(viewParent, context.getString(R.string.snack_cleared) + " " + headers.get(position), Snackbar.LENGTH_SHORT).show();
                }else {
                    Snackbar.make(viewParent, context.getString(R.string.snack_unable_to_Clear) + " " + headers.get(position), Snackbar.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }

    private void Play(){
        ArrayList<dataItem> temp = PlaylistManager.getInstance(MyApp.getContext()).GetPlaylist(headers.get(position));
        ArrayList<Integer> trackList = new ArrayList<>();
        for(dataItem d:temp){
            trackList.add(d.id);
        }

        if(!trackList.isEmpty()) {
            playerService.setTrackList(trackList);
            playerService.playAtPosition(0);
            /*
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent()
                    .setAction(Constants.ACTION.PLAY_AT_POSITION)
                    .putExtra("position",0));*/
        }else {
            //Toast.makeText(context,"empty playlist",Toast.LENGTH_SHORT).show();
            Snackbar.make(viewParent, context.getString(R.string.empty_play_list), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void Share(){
        ArrayList<Uri> files = new ArrayList<>();  //for sending multiple files
        ArrayList<dataItem> temp = PlaylistManager.getInstance(MyApp.getContext()).GetPlaylist(headers.get(position));
        ArrayList<Integer> trackList = new ArrayList<>();
        for(dataItem d:temp){
            trackList.add(d.id);
        }
        for( int id : trackList){
            try {
                File file = new File(MusicLibrary.getInstance().getTrackItemFromId(id).getFilePath());
                Uri fileUri =
                        FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + "com.bhandari.music.provider", file);
                files.add(fileUri);
            }
            catch (Exception e ){
                //Toast.makeText(context,"Something wrong!",Toast.LENGTH_LONG).show();
                Snackbar.make(viewParent, context.getString(R.string.error_something_wrong), Snackbar.LENGTH_SHORT).show();
                return;
            }
        }
        if(!files.isEmpty()) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("*/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            context.startActivity(Intent.createChooser(intent, "multiple audio files"));
        }else {
            //Toast.makeText(context,"empty playlist",Toast.LENGTH_SHORT).show();
            Snackbar.make(viewParent, context.getString(R.string.empty_play_list), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void AddToQ(int positionToAdd){
        //we are using same function for adding to q and playing next
        // toastString is to identify which string to disokay as toast
        String toastString=(positionToAdd==Constants.ADD_TO_Q.AT_LAST ? context.getString(R.string.added_to_q)
                : context.getString(R.string.playing_next)) ;
        //when adding to playing next, order of songs should be desc
        //and asc for adding at last
        //this is how the function in player service is writte, deal with it
        int sortOrder=(positionToAdd==Constants.ADD_TO_Q.AT_LAST ? Constants.SORT_ORDER.ASC : Constants.SORT_ORDER.DESC);

        ArrayList<dataItem> temp = PlaylistManager.getInstance(context).GetPlaylist(headers.get(position));
        ArrayList<Integer> trackList = new ArrayList<>();
        for(dataItem d:temp){
            trackList.add(d.id);
        }
        if(!trackList.isEmpty()) {
            for (int id : trackList) {
                playerService.addToQ(id, positionToAdd);
            }
            //to update the to be next field in notification
            MyApp.getService().PostNotification();

            /*Toast.makeText(context
                    , toastString + headers.get(position)
                    , Toast.LENGTH_SHORT).show();*/
            Snackbar.make(viewParent, toastString + headers.get(position), Snackbar.LENGTH_SHORT).show();
        }else {
            //Toast.makeText(context,"empty playlist",Toast.LENGTH_SHORT).show();
            Snackbar.make(viewParent, context.getString(R.string.empty_play_list), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void Delete(){

        new MyDialogBuilder(context)
                .title(context.getString(R.string.are_u_sure))
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if(headers.get(position).equals(Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED)
                                || headers.get(position).equals(Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED)
                                || headers.get(position).equals(Constants.SYSTEM_PLAYLISTS.MOST_PLAYED)
                                || headers.get(position).equals(Constants.SYSTEM_PLAYLISTS.MY_FAV))
                        {
                            //Toast.makeText(context,"Cannot delete "+headers.get(position),Toast.LENGTH_SHORT).show();
                            Snackbar.make(viewParent, context.getString(R.string.cannot_del)+headers.get(position), Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        if(PlaylistManager.getInstance(MyApp.getContext()).DeletePlaylist(headers.get(position))){
                            //Toast.makeText(context,"Deleted "+headers.get(position),Toast.LENGTH_SHORT).show();
                            Snackbar.make(viewParent, context.getString(R.string.deleted)+headers.get(position), Snackbar.LENGTH_SHORT).show();
                            headers.remove(headers.get(position));
                            notifyDataSetChanged();
                        }else {
                            //Toast.makeText(context,"Cannot delete "+headers.get(position),Toast.LENGTH_SHORT).show();
                            Snackbar.make(viewParent, context.getString(R.string.cannot_del)+headers.get(position), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }
    @Override
    public int getItemCount() {
        return headers.size();
    }

    public void onClick(View view, int position) {
        this.position=position;
        switch (view.getId()){
            //launch playlist
            case R.id.libraryItem:
                Intent intent = new Intent(context,ActivitySecondaryLibrary.class);
                intent.putExtra("status",Constants.FRAGMENT_STATUS.PLAYLIST_FRAGMENT);
                intent.putExtra("title",headers.get(position).trim());
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

            case R.id.menuPopup:
                PopupMenu popup = new PopupMenu(context, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.system_playlist_menu, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(this);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView count;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.header);
            count = itemView.findViewById(R.id.secondaryHeader);
            itemView.findViewById(R.id.album_art_wrapper).setVisibility(View.GONE);
            itemView.setOnClickListener(this);
            itemView.findViewById(R.id.menuPopup).setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            PlaylistLibraryAdapter.this.onClick(view,getLayoutPosition());
        }
    }
}
