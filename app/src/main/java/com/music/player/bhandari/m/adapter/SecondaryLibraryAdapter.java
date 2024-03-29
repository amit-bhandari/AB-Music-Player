package com.music.player.bhandari.m.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.activity.ActivityTagEditor;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.model.PlaylistManager;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.model.dataItem;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Copyright 2017 Amit Bhandari AB
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class SecondaryLibraryAdapter extends RecyclerView.Adapter<SecondaryLibraryAdapter.MyViewHolder>
        implements PopupMenu.OnMenuItemClickListener {

    private final String REMOVE = "Remove";
    private final ArrayList<dataItem> dataItems = new ArrayList<>();

    private final Context context;
    private final LayoutInflater inflater;
    private int position = 0;

    private long clickedON;
    private int status;
    private String playlist_name;

    private PlayerService playerService;

    public SecondaryLibraryAdapter(Context context, ArrayList<Long> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);

        System.out.println("AMIT " + data.size());

        for (long id : data) {
            dataItem d = MusicLibrary.getInstance().getDataItemsForTracks().get(id);
            if (d != null) {
                dataItems.add(d);
            }
        }
        bindService();
    }

    //constructor for getMostPlayed and getRecentlyPlayed
    public SecondaryLibraryAdapter(Context context, ArrayList<dataItem> data, int status, String playlist_name) {
        this.playlist_name = playlist_name;
        this.context = context;
        inflater = LayoutInflater.from(context);
        dataItems.addAll(data);
        setHasStableIds(true);
        bindService();
        this.status = status;
    }

    public void shuffleAll() {
        //remove empty element from list of headers

        ArrayList<Long> temp = new ArrayList<>();
        for (dataItem d : dataItems) {
            if (d.id != 0) {
                temp.add(d.id);
            }
        }
        Collections.shuffle(temp);
        playerService.setTrackList(temp);
        playerService.playAtPosition(0);
    }

    public void bindService() {
        playerService = MyApp.getService();
    }

    public ArrayList<dataItem> getList() {
        return dataItems;
    }

    @NonNull
    @Override
    public SecondaryLibraryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.fragment_library_item, parent, false);

        ((TextView) (view.findViewById(R.id.header))).setTextColor(ColorHelper.getPrimaryTextColor());
        ((TextView) (view.findViewById(R.id.secondaryHeader))).setTextColor(ColorHelper.getSecondaryTextColor());
        ((TextView) (view.findViewById(R.id.count))).setTextColor(ColorHelper.getSecondaryTextColor());
        ((ImageView) (view.findViewById(R.id.menuPopup))).setColorFilter(ColorHelper.getSecondaryTextColor());

        return new SecondaryLibraryAdapter.MyViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull SecondaryLibraryAdapter.MyViewHolder holder, int position) {

        Log.d("SecondaryLibraryAdapter", "onBindViewHolder: " + dataItems.get(position).title);


        if (dataItems.get(position).title.equals("")) {
            holder.itemView.setVisibility(View.GONE);
            return;
        }

        holder.title.setPadding(20, 0, 0, 0);
        holder.secondary.setPadding(20, 0, 0, 0);
        String secondaryText = dataItems.get(position).artist_name
                + " | "
                + dataItems.get(position).albumName;

        holder.title.setText(dataItems.get(position).title);
        holder.secondary.setText(secondaryText);
        holder.count.setText(dataItems.get(position).durStr);
    }

    @Override
    public int getItemCount() {
        return dataItems.size();
    }

    public void onClick(View view, int position) {
        this.position = position;
        if (dataItems.get(position).title.equals("")) {
            return;
        }

        clickedON = dataItems.get(position).id;
        switch (view.getId()) {
            case R.id.libraryItem -> {
                if (MyApp.isLocked()) {
                    Toast.makeText(context, "Music is Locked!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Play();
            }
            case R.id.menuPopup -> {
                PopupMenu popup = new PopupMenu(context, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_tracks_by_title, popup.getMenu());
                if (status == Constants.FRAGMENT_STATUS.PLAYLIST_FRAGMENT) {
                    if (!(playlist_name.replace(" ", "_").equals(Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED)
                            || playlist_name.replace(" ", "_").equals(Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED)
                            || playlist_name.replace(" ", "_").equals(Constants.SYSTEM_PLAYLISTS.MOST_PLAYED))) {
                        popup.getMenu().add(REMOVE);
                    }
                }
                popup.getMenu().removeItem(R.id.action_exclude_folder);
                popup.show();
                popup.setOnMenuItemClickListener(this);
            }
        }
    }

    public void updateItem(int position, String... param) {
        dataItems.get(position).title = param[0];
        dataItems.get(position).artist_name = param[0];
        dataItems.get(position).albumName = param[0];
        notifyItemChanged(position);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_play -> {
                if (MyApp.isLocked()) {
                    Toast.makeText(context, context.getString(R.string.music_is_locked), Toast.LENGTH_SHORT).show();
                    return true;
                }
                Play();
            }
            case R.id.action_add_to_playlist -> {
                long[] ids;
                ids = new long[]{dataItems.get(position).id};
                UtilityFun.AddToPlaylist(context, ids);
            }
            case R.id.action_share -> {
                ArrayList<Uri> files = new ArrayList<>();
                File fileToBeShared = new File(MusicLibrary.getInstance().getTrackItemFromId(clickedON).getFilePath());
                try {
                    files.add(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + "com.bhandari.music.provider", fileToBeShared));
                    UtilityFun.Share(context, files, dataItems.get(position).title);
                } catch (IllegalArgumentException e) {
                    try {
                        UtilityFun.ShareFromPath(context, fileToBeShared.getAbsolutePath());
                    } catch (Exception ex) {
                        Toast.makeText(context, context.getString(R.string.error_unable_to_share), Toast.LENGTH_LONG).show();
                    }
                }
            }
            case R.id.action_delete -> DeleteDialog();
            case R.id.action_play_next -> AddToQ(Constants.ADD_TO_Q.IMMEDIATE_NEXT);
            case R.id.action_add_to_q -> AddToQ(Constants.ADD_TO_Q.AT_LAST);
            case R.id.action_track_info -> setTrackInfoDialog();
            case R.id.action_edit_track_info -> {
                TrackItem editItem = MusicLibrary.getInstance().getTrackItemFromId(dataItems.get(position).id);
                if (editItem == null) {
                    Toast.makeText(context, context.getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    return true;
                }
                context.startActivity(new Intent(context, ActivityTagEditor.class)
                        .putExtra("from", Constants.TAG_EDITOR_LAUNCHED_FROM.SECONDARY_LIB)
                        .putExtra("file_path", editItem.getFilePath())
                        .putExtra("track_title", editItem.getTitle())
                        .putExtra("position", position)
                        .putExtra("id", editItem.getId()));
            }
            case R.id.action_set_as_ringtone -> {
                TrackItem tempItem = MusicLibrary.getInstance().getTrackItemFromId(dataItems.get(position).id);
                if (tempItem == null) {
                    Toast.makeText(context, context.getString(R.string.error_something_wrong), Toast.LENGTH_LONG).show();
                    return false;
                }
                UtilityFun.SetRingtone(context, tempItem.getFilePath()
                        , tempItem.getId());
            }
        }
        if (item.getTitle().equals(REMOVE)) {
            PlaylistManager.getInstance(MyApp.getContext()).RemoveSongFromPlaylistNew(playlist_name, dataItems.get(position).id);
            dataItems.remove(position);
            notifyItemRemoved(position);
        }
        return true;
    }

    private void setTrackInfoDialog() {
        LinearLayout linear = new LinearLayout(context);
        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text = new TextView(context);
        text.setTypeface(TypeFaceHelper.getTypeFace(context));
        text.setText(UtilityFun.trackInfoBuild(dataItems.get(position).id).toString());

        text.setPadding(20, 20, 20, 10);
        text.setTextSize(15);

        linear.addView(text);

        new MyDialogBuilder(context)
                .title(context.getString(R.string.track_info_title))
                .customView(linear, true)
                .positiveText(R.string.okay)
                .show();
    }

    private void Play() {
        ArrayList<Long> temp = new ArrayList<>();
        for (dataItem d : dataItems) {
            if (d.id != 0) {
                temp.add(d.id);
            }
        }
        playerService.setTrackList(temp);
        playerService.playAtPosition(position);
    }

    private void AddToQ(int positionToAdd) {
        //we are using same function for adding to q and playing next
        String toastString = (positionToAdd == Constants.ADD_TO_Q.AT_LAST ? context.getString(R.string.added_to_q)
                : context.getString(R.string.playing_next));

        playerService.addToQ(clickedON, positionToAdd);
        //to update the to be next field in notification
        MyApp.getService().PostNotification();
        Toast.makeText(context
                , toastString + dataItems.get(position).title
                , Toast.LENGTH_SHORT).show();
    }

    private void DeleteDialog() {

        new MyDialogBuilder(context)
                .title(context.getString(R.string.are_u_sure))
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive((dialog, which) -> {
                    if (playerService.getCurrentTrack().getTitle().equals(dataItems.get(position).title)) {
                        Toast.makeText(context, context.getString(R.string.song_is_playing), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    File file;
                    try {
                        file = new File(MusicLibrary.getInstance().getTrackItemFromId(dataItems.get(position).id)
                                .getFilePath());
                    } catch (Exception e) {
                        return;
                    }
                    //delete the file first
                    ArrayList<File> files = new ArrayList<>();
                    files.add(file);
                    ArrayList<Long> ids = new ArrayList<>();
                    ids.add(dataItems.get(position).id);
                    if (UtilityFun.Delete(context, files, ids)) {
                        Toast.makeText(context, context.getString(R.string.deleted) + dataItems.get(position).title, Toast.LENGTH_SHORT).show();
                        dataItems.remove(dataItems.get(position));
                        notifyItemRemoved(position);
                        notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, context.getString(R.string.unable_to_del)
                                , Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, secondary, count;
        ImageButton popUp;

        MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.header);
            secondary = itemView.findViewById(R.id.secondaryHeader);
            count = itemView.findViewById(R.id.count);

            itemView.findViewById(R.id.album_art_wrapper).setVisibility(View.GONE);
            popUp = itemView.findViewById(R.id.menuPopup);
            itemView.setOnClickListener(this);
            itemView.findViewById(R.id.menuPopup).setOnClickListener(this);
            itemView.findViewById(R.id.imageVIewForStubAlbumArt).setVisibility(View.GONE);
        }

        @Override
        public void onClick(View view) {
            SecondaryLibraryAdapter.this.onClick(view, getLayoutPosition());
        }
    }
}
