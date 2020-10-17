package com.music.player.bhandari.m.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.BubbleTextGetter;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.activity.ActivitySecondaryLibrary;
import com.music.player.bhandari.m.activity.ActivityTagEditor;
import com.music.player.bhandari.m.activity.FragmentLibrary;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.model.dataItem;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.utils.UtilityFun;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

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

/**
 * Congrats you are reading worst piece of code I have ever written
 */

public class MainLibraryAdapter extends RecyclerView.Adapter<MainLibraryAdapter.MyViewHolder>
        implements PopupMenu.OnMenuItemClickListener, FastScrollRecyclerView.SectionedAdapter, BubbleTextGetter {

    private Context context;
    private LayoutInflater inflater;
    private FragmentLibrary fl;
    private PopupMenu popup;
    private ArrayList<Integer> id_list =new ArrayList<>();
    private PlayerService playerService;
    private int position;
    private ArrayList<dataItem> dataItems=new ArrayList<>();
    private ArrayList<dataItem> filteredDataItems=new ArrayList<>();

    private int mItemHeight;
    private long mLastClickTime;

    private Drawable batmanDrawable;
    private View viewParent;

    private SparseBooleanArray selectedItems;

    public MainLibraryAdapter(final FragmentLibrary fl, final Context context, ArrayList<dataItem> data){
        this.context=context;
        selectedItems = new SparseBooleanArray();
        inflater= LayoutInflater.from(context);
        this.fl=fl;
        this.dataItems = data;
        filteredDataItems.addAll(dataItems);
        playerService = MyApp.getService();
        int defaultAlbumArtSetting = MyApp.getPref().getInt(context.getString(R.string.pref_default_album_art), 0);
        switch (defaultAlbumArtSetting) {
            case 0:
                batmanDrawable = ContextCompat.getDrawable(context, R.drawable.ic_batman_1);
                break;

            case 1:
                batmanDrawable = UtilityFun.getDefaultAlbumArtDrawable();
                break;
        }
        setHasStableIds(true);
    }

    public void filter(String searchQuery){
        if(!searchQuery.equals("")){
            filteredDataItems.clear();
            switch (fl.getStatus()){
                case Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT:
                    for(dataItem d:dataItems){
                        if(d.title.toLowerCase().contains(searchQuery)){
                            filteredDataItems.add(d);
                        }
                    }
                    break;

                case Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT:
                    for(dataItem d:dataItems){
                        if(d.title.toLowerCase().contains(searchQuery)){
                            filteredDataItems.add(d);
                        }
                    }
                    break;

                case Constants.FRAGMENT_STATUS.GENRE_FRAGMENT:
                    for(dataItem d:dataItems){
                        if(d.title.toLowerCase().contains(searchQuery)){
                            filteredDataItems.add(d);
                        }
                    }
                    break;

                case Constants.FRAGMENT_STATUS.TITLE_FRAGMENT:
                    for(dataItem d:dataItems){
                        if(d.title.toLowerCase().contains(searchQuery)
                                || d.artist_name.toLowerCase().contains(searchQuery)
                                || d.albumName.toLowerCase().contains(searchQuery)){
                            filteredDataItems.add(d);
                        }
                    }
                    break;
            }
        }else {
            filteredDataItems.clear();
            filteredDataItems.addAll(dataItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        final View parentView = inflater.inflate(R.layout.fragment_library_item, parent, false);
        viewParent = parent;
        int color = ColorHelper.getPrimaryTextColor() ;
        int subColor = ColorHelper.getSecondaryTextColor() ;
        ((TextView)(parentView.findViewById(R.id.header))).setTextColor(color);
        ((TextView)(parentView.findViewById(R.id.secondaryHeader))).setTextColor(subColor);
        ((TextView)(parentView.findViewById(R.id.count))).setTextColor(subColor);
        ((ImageView)(parentView.findViewById(R.id.menuPopup))).setColorFilter(color);


        return  new MainLibraryAdapter.MyViewHolder(parentView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final String url = MusicLibrary.getInstance().getArtistUrls().get(filteredDataItems.get(position).artist_name);
       // mItemHeight = holder.itemView.getMeasuredHeight();
        switch (fl.getStatus()) {
            case Constants.FRAGMENT_STATUS.TITLE_FRAGMENT:
                Glide
                        .with(context)
                        .load(MusicLibrary.getInstance().getAlbumArtUri(filteredDataItems.get(position).album_id))
                        .listener(new RequestListener<Uri, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                //Log.d("AlbumLibraryAdapter", "onException: ");
                                if(UtilityFun.isConnectedToInternet() &&
                                        !MyApp.getPref().getBoolean(context.getString(R.string.pref_data_saver), false)) {
                                    Glide
                                            .with(context)
                                            .load(url)
                                            .centerCrop()
                                            .crossFade(500)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .override(100, 100)
                                            .placeholder(batmanDrawable)
                                            .into(holder.image);
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .placeholder(batmanDrawable)
                        .crossFade()
                        .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                        .override(100,100)
                        .into(holder.image);

                holder.title.setText(filteredDataItems.get(position).title);
                holder.count.setText(UtilityFun.msToString(Integer.parseInt(filteredDataItems.get(position).duration)));
                String secText = filteredDataItems.get(position).artist_name + " | " + filteredDataItems.get(position).albumName;
                holder.secondary.setText(secText);
                break;

            case Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT:
                Glide.with(context)
                        .load(MusicLibrary.getInstance().getAlbumArtUri(filteredDataItems.get(position).album_id))
                        .placeholder(batmanDrawable)
                        .into(holder.image);
                holder.title.setText(filteredDataItems.get(position).albumName);
                int trackCount= filteredDataItems.get(position).numberOfTracks;
                String trackCoun ;
                if(trackCount>1)
                    trackCoun = trackCount+context.getString(R.string.tracks);
                else
                    trackCoun = trackCount+context.getString(R.string.track);
                StringBuilder albumSecondaryString = new StringBuilder()
                        .append(filteredDataItems.get(position).artist_name)
                        .append( " | ")
                        .append(trackCoun);
                holder.secondary.setText(albumSecondaryString);
                break;

            case Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT:
                //holder.wrapper.setVisibility(View.GONE);
                holder.title.setText(filteredDataItems.get(position).artist_name);
                int tracksCount=filteredDataItems.get(position).numberOfTracks;
                int albumCount=filteredDataItems.get(position).numberOfAlbums;
                StringBuilder stringBuilder = new StringBuilder()
                        .append(tracksCount).append(context.getString(R.string.tracks))
                        .append(" | ").append(albumCount)
                        .append(context.getString(R.string.albums));
                holder.secondary.setText(stringBuilder);
                holder.title.setPadding(20,0,0,0);
                holder.secondary.setPadding(20,0,0,0);
                if(!MyApp.getPref().getBoolean(context.getString(R.string.pref_data_saver), false)) {
                    Log.d("MainLibraryAdapter", "onBindViewHolder: " + filteredDataItems.get(position).artist_name + ":" + url);
                    Glide
                            .with(context)
                            .load(url)
                            .placeholder(R.drawable.person_blue)
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .override(100, 100)
                            .into(holder.image);
                }else {
                    holder.image.setBackgroundResource(R.drawable.person_blue);
                }
                break;

            case Constants.FRAGMENT_STATUS.GENRE_FRAGMENT:
                holder.wrapper.setVisibility(View.GONE);
                holder.title.setText(filteredDataItems.get(position).title);
                holder.title.setPadding(20,0,0,0);
                break;
        }

        holder.itemView.setActivated(selectedItems.get(position, false));
    }

    @Override
    public int getItemCount() {
        return filteredDataItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void clear(){
        id_list.clear();
        inflater=null;
        fl=null;
        popup=null;
    }

    public void onClick(View view, final int position) {
        this.position=position;
        switch (view.getId()){
            case R.id.libraryItem:
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        String title="";
                        int key=0;
                        switch (fl.getStatus()) {
                            case Constants.FRAGMENT_STATUS.TITLE_FRAGMENT:
                                if(MyApp.isLocked()){
                                    new Handler(context.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //Toast.makeText(context,"Music is Locked!",Toast.LENGTH_SHORT).show();
                                            Snackbar.make(viewParent, context.getString(R.string.music_is_locked), Snackbar.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }
                                Play();
                                break;


                            case Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT:
                                title=filteredDataItems.get(position).albumName;
                                key=filteredDataItems.get(position).album_id;
                                break;

                            case Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT:
                                title=filteredDataItems.get(position).artist_name;
                                key=filteredDataItems.get(position).artist_id;
                                break;

                            case Constants.FRAGMENT_STATUS.GENRE_FRAGMENT:
                                title=filteredDataItems.get(position).title;
                                key=filteredDataItems.get(position).id;
                                break;
                        }
                        if(fl.getStatus()!=Constants.FRAGMENT_STATUS.TITLE_FRAGMENT){
                            Intent intent = new Intent(context,ActivitySecondaryLibrary.class);
                            intent.putExtra("status",fl.getStatus());
                            intent.putExtra("key",key);
                            intent.putExtra("title",title.trim());
                            ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            context.startActivity(intent);
                        }
                    }
                });
                break;

            case R.id.menuPopup:
                popup=new PopupMenu(context,view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_tracks_by_title, popup.getMenu());
                if(fl.getStatus()!=Constants.FRAGMENT_STATUS.TITLE_FRAGMENT){
                    popup.getMenu().removeItem(R.id.action_set_as_ringtone);
                    popup.getMenu().removeItem(R.id.action_track_info);
                    popup.getMenu().removeItem(R.id.action_edit_track_info);
                }
                popup.getMenu().removeItem(R.id.action_exclude_folder);
                popup.show();
                popup.setOnMenuItemClickListener(this);
                break;
        }
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

            case R.id.action_add_to_playlist:
                AddToPlaylist();
                break;

            case R.id.action_share:
                try {
                    Share();
                }catch (Exception e){
                    //Toast.makeText(context,"Something wrong!",Toast.LENGTH_LONG).show();
                    Snackbar.make(viewParent, context.getString(R.string.error_unable_to_share), Snackbar.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_delete:
                DeleteDialog();
                break;

            case R.id.action_play_next:
                AddToQ(Constants.ADD_TO_Q.IMMEDIATE_NEXT);
                break;

            case R.id.action_add_to_q:
                AddToQ(Constants.ADD_TO_Q.AT_LAST);
                break;

            case R.id.action_set_as_ringtone:
                UtilityFun.SetRingtone(context, filteredDataItems.get(position).file_path
                        ,filteredDataItems.get(position).id);
                break;

            case R.id.action_track_info:
                setTrackInfoDialog();
                break;

            case R.id.action_edit_track_info:
                context.startActivity(new Intent(context, ActivityTagEditor.class)
                        .putExtra("from",Constants.TAG_EDITOR_LAUNCHED_FROM.MAIN_LIB)
                        .putExtra("file_path",filteredDataItems.get(position).file_path)
                        .putExtra("track_title",filteredDataItems.get(position).title)
                        .putExtra("position",position)
                        .putExtra("id",filteredDataItems.get(position).id));
                ((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

            case R.id.action_search_youtube:
                UtilityFun.LaunchYoutube(context,filteredDataItems.get(position).artist_name + " - "
                        +filteredDataItems.get(position).title );
                break;
        }
        return true;
    }

    public void sort(int sort_id){
        int sort_order = MyApp.getPref().getInt(context.getResources().getString(R.string.pref_order_by),Constants.SORT_BY.ASC);
        switch (sort_id){
            case Constants.SORT_BY.NAME:
                if(sort_order==Constants.SORT_BY.ASC) {
                    Collections.sort(filteredDataItems, new Comparator<dataItem>() {
                        @Override
                        public int compare(dataItem o1, dataItem o2) {
                            return o1.title.compareToIgnoreCase(o2.title);
                        }
                    });
                }else {
                    Collections.sort(filteredDataItems, new Comparator<dataItem>() {
                        @Override
                        public int compare(dataItem o1, dataItem o2) {
                            return o2.title.compareToIgnoreCase(o1.title);
                        }
                    });
                }

                break;

            case Constants.SORT_BY.SIZE:
                if(sort_order==Constants.SORT_BY.ASC) {
                    Collections.sort(filteredDataItems, new Comparator<dataItem>() {
                        @Override
                        public int compare(dataItem o1, dataItem o2) {
                            return (int) (new File(o1.file_path).length() - new File(o2.file_path).length());
                        }
                    });
                }else {
                    Collections.sort(filteredDataItems, new Comparator<dataItem>() {
                        @Override
                        public int compare(dataItem o1, dataItem o2) {
                            return (int) (new File(o2.file_path).length() - new File(o1.file_path).length());
                        }
                    });
                }
                break;

            case Constants.SORT_BY.YEAR:
                if(sort_order==Constants.SORT_BY.ASC) {
                    Collections.sort(filteredDataItems, new Comparator<dataItem>() {
                        @Override
                        public int compare(dataItem o1, dataItem o2) {
                            return o1.year.compareToIgnoreCase(o2.year);
                        }
                    });
                }else {
                    Collections.sort(filteredDataItems, new Comparator<dataItem>() {
                        @Override
                        public int compare(dataItem o1, dataItem o2) {
                            return o2.year.compareToIgnoreCase(o1.year);
                        }
                    });
                }
                break;

            case Constants.SORT_BY.NO_OF_ALBUMS:
                if(sort_order==Constants.SORT_BY.ASC) {
                    Collections.sort(filteredDataItems, new Comparator<dataItem>() {
                        @Override
                        public int compare(dataItem o1, dataItem o2) {
                            return o1.numberOfAlbums - o2.numberOfAlbums;
                        }
                    });
                }else {
                    Collections.sort(filteredDataItems, new Comparator<dataItem>() {
                        @Override
                        public int compare(dataItem o1, dataItem o2) {
                            return o2.numberOfAlbums - o1.numberOfAlbums;
                        }
                    });
                }
                break;

            case Constants.SORT_BY.NO_OF_TRACKS:
                if(sort_order==Constants.SORT_BY.ASC) {
                    Collections.sort(filteredDataItems, new Comparator<dataItem>() {
                        @Override
                        public int compare(dataItem o1, dataItem o2) {
                            return o1.numberOfTracks - o2.numberOfTracks;
                        }
                    });
                }else {
                    Collections.sort(filteredDataItems, new Comparator<dataItem>() {
                        @Override
                        public int compare(dataItem o1, dataItem o2) {
                            return o2.numberOfTracks - o1.numberOfTracks;
                        }
                    });
                }
                break;

            case Constants.SORT_BY.DURATION:
                if(sort_order==Constants.SORT_BY.ASC) {
                    Collections.sort(filteredDataItems, new Comparator<dataItem>() {
                        @Override
                        public int compare(dataItem o1, dataItem o2) {
                            return Integer.valueOf(o1.duration) -Integer.valueOf(o2.duration) ;
                        }
                    });
                }else {
                    Collections.sort(filteredDataItems, new Comparator<dataItem>() {
                        @Override
                        public int compare(dataItem o1, dataItem o2) {
                            return Integer.valueOf(o2.duration) -Integer.valueOf(o1.duration) ;
                        }
                    });
                }
                break;
        }
        notifyDataSetChanged();
    }

    private void setTrackInfoDialog(){
        LinearLayout linear = new LinearLayout(context);
        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text = new TextView(context);
        text.setTypeface(TypeFaceHelper.getTypeFace(context));
        text.setText(UtilityFun.trackInfoBuild(filteredDataItems.get(position).id).toString());

        text.setPadding(20, 20,20,10);
        text.setTextSize(15);
        text.setTypeface(TypeFaceHelper.getTypeFace(context));
        //text.setGravity(Gravity.CENTER);

        linear.addView(text);

        new MyDialogBuilder(context)
                .title(context.getString(R.string.track_info_title))
                .customView(linear, true)
                .positiveText(R.string.okay)
                .show();
    }

    private void Play(){

        if(playerService==null) return;

        switch (fl.getStatus()) {
            case Constants.FRAGMENT_STATUS.TITLE_FRAGMENT:

                if (playerService.getStatus() == PlayerService.PLAYING)
                    playerService.pause();
                id_list.clear();
                for(dataItem d:filteredDataItems){
                    id_list.add(d.id);
                }
                playerService.setTrackList(id_list);
                playerService.playAtPosition(position);


                break;

            case Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT:
                int album_id=filteredDataItems.get(position).album_id;
                playerService.setTrackList(MusicLibrary.getInstance().getSongListFromAlbumIdNew(album_id,Constants.SORT_ORDER.ASC));
                break;

            case Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT:
                int artist_id=filteredDataItems.get(position).artist_id;
                playerService.setTrackList(MusicLibrary.getInstance().getSongListFromArtistIdNew(artist_id,Constants.SORT_ORDER.ASC));
                break;

            case Constants.FRAGMENT_STATUS.GENRE_FRAGMENT:
                int genre_id=filteredDataItems.get(position).id;
                playerService.setTrackList(MusicLibrary.getInstance().getSongListFromGenreIdNew(genre_id,Constants.SORT_ORDER.ASC));
                break;
        }
        if(fl.getStatus()!=Constants.FRAGMENT_STATUS.TITLE_FRAGMENT){
            playerService.playAtPosition(0);
        }
    }

    private void AddToPlaylist(){
        int[] ids;
        ArrayList<Integer> temp;
        switch (fl.getStatus()) {
            case Constants.FRAGMENT_STATUS.TITLE_FRAGMENT:
                ids=new int[]{filteredDataItems.get(position).id};
                UtilityFun.AddToPlaylist(context, ids);
                break;

            case Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT:
                int album_id=filteredDataItems.get(position).album_id;
                temp = MusicLibrary.getInstance().getSongListFromAlbumIdNew(album_id,Constants.SORT_ORDER.ASC);
                ids = new int[temp.size()];
                for (int i=0; i < ids.length; i++)
                {
                    ids[i] = temp.get(i);
                }
                UtilityFun.AddToPlaylist(context, ids);
                break;

            case Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT:
                int artist_id=filteredDataItems.get(position).artist_id;
                temp = MusicLibrary.getInstance().getSongListFromArtistIdNew(artist_id,Constants.SORT_ORDER.ASC);
                ids = new int[temp.size()];
                for (int i=0; i < ids.length; i++)
                {
                    ids[i] = temp.get(i);
                }
                UtilityFun.AddToPlaylist(context, ids);
                break;

            case Constants.FRAGMENT_STATUS.GENRE_FRAGMENT:
                int genre_id=filteredDataItems.get(position).id;
                temp = MusicLibrary.getInstance().getSongListFromGenreIdNew(genre_id,Constants.SORT_ORDER.ASC);
                ids = new int[temp.size()];
                for (int i=0; i < ids.length; i++)
                {
                    ids[i] = temp.get(i);
                }
                UtilityFun.AddToPlaylist(context, ids);
                break;
        }
        //
    }

    private void Share(){
        ArrayList<Uri> files = new ArrayList<>();  //for sending multiple files

        switch (fl.getStatus()) {
            case Constants.FRAGMENT_STATUS.TITLE_FRAGMENT:
                try {
                    File fileToBeShared = new File(filteredDataItems.get(position).file_path);
                    files.add(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + "com.bhandari.music.provider", fileToBeShared));
                    UtilityFun.Share(context, files, filteredDataItems.get(position).title);
                }catch (IllegalArgumentException e){
                    try{
                        UtilityFun.ShareFromPath(context, filteredDataItems.get(position).file_path);
                    }catch (Exception ex) {
                        Snackbar.make(viewParent, R.string.error_unable_to_share, Snackbar.LENGTH_SHORT).show();
                    }
                }
                break;

            case Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT:
                int album_id = filteredDataItems.get(position).album_id;
                for (int id : MusicLibrary.getInstance().getSongListFromAlbumIdNew(album_id, Constants.SORT_ORDER.ASC)) {
                    File file = new File(MusicLibrary.getInstance().getTrackItemFromId(id).getFilePath());
                    Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + "com.bhandari.music.provider", file);
                    files.add(fileUri);
                }
                break;

            case Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT:
                int artist_id = filteredDataItems.get(position).artist_id;
                for (int id : MusicLibrary.getInstance().getSongListFromArtistIdNew(artist_id, Constants.SORT_ORDER.ASC)) {
                    File file = new File(MusicLibrary.getInstance().getTrackItemFromId(id).getFilePath());
                    Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + "com.bhandari.music.provider", file);
                    files.add(fileUri);
                }
                break;

            case Constants.FRAGMENT_STATUS.GENRE_FRAGMENT:
                int genre_id = filteredDataItems.get(position).id;
                for (int id : MusicLibrary.getInstance().getSongListFromGenreIdNew(genre_id, Constants.SORT_ORDER.ASC)) {
                    File file = new File(MusicLibrary.getInstance().getTrackItemFromId(id).getFilePath());
                    Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + "com.bhandari.music.provider", file);
                    files.add(fileUri);
                }
                break;
        }
        if(fl.getStatus()!=Constants.FRAGMENT_STATUS.TITLE_FRAGMENT) {
            UtilityFun.Share(context, files, "Multiple audio files");
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
        switch (fl.getStatus()) {
            case Constants.FRAGMENT_STATUS.TITLE_FRAGMENT:
                playerService.addToQ(filteredDataItems.get(position).id, positionToAdd);
                //Toast.makeText(context
                  //      ,toastString+filteredDataItems.get(position).title
                    ///    ,Toast.LENGTH_SHORT).show();
                Snackbar.make(viewParent, toastString+filteredDataItems.get(position).title, Snackbar.LENGTH_SHORT).show();
                break;

            case Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT:
                int album_id=filteredDataItems.get(position).album_id;
                for(int id:MusicLibrary.getInstance().getSongListFromAlbumIdNew(album_id,sortOrder)){
                    playerService.addToQ(id, positionToAdd);
                }
                //Toast.makeText(context
                  //      ,toastString+filteredDataItems.get(position).title
                    //    ,Toast.LENGTH_SHORT).show();
                Snackbar.make(viewParent, toastString+filteredDataItems.get(position).title, Snackbar.LENGTH_SHORT).show();

                break;

            case Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT:
                int artist_id=filteredDataItems.get(position).artist_id;
                for(int id:MusicLibrary.getInstance().getSongListFromArtistIdNew(artist_id,sortOrder)){
                    playerService.addToQ(id,  positionToAdd);
                }
                /*Toast.makeText(context
                        ,toastString+filteredDataItems.get(position).title
                        ,Toast.LENGTH_SHORT).show();*/
                Snackbar.make(viewParent, toastString+filteredDataItems.get(position).title, Snackbar.LENGTH_SHORT).show();
                break;

            case Constants.FRAGMENT_STATUS.GENRE_FRAGMENT:
                int genre_id=filteredDataItems.get(position).id;
                for(int id:MusicLibrary.getInstance().getSongListFromGenreIdNew(genre_id,sortOrder)){
                    playerService.addToQ(id, positionToAdd);
                }
                /*Toast.makeText(context
                        ,toastString+filteredDataItems.get(position).title
                        ,Toast.LENGTH_SHORT).show();*/
                Snackbar.make(viewParent, toastString+filteredDataItems.get(position).title, Snackbar.LENGTH_SHORT).show();
                break;
        }

        //to update the to be next field in notification
        MyApp.getService().PostNotification();
    }

    private void DeleteDialog(){

        new MyDialogBuilder(context)
                .title(context.getString(R.string.are_u_sure))
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ArrayList<File> files = new ArrayList<>();
                        ArrayList<Integer> ids = new ArrayList<>();
                        ArrayList<Integer> tracklist;
                        switch (fl.getStatus()) {
                            case Constants.FRAGMENT_STATUS.TITLE_FRAGMENT:


                                //delete the file first
                                files.add(new File(filteredDataItems.get(position).file_path));
                                ids.add(filteredDataItems.get(position).id);
                                if(UtilityFun.Delete(context, files, ids)){
                                    deleteSuccess();
                                } else {
                                    Snackbar.make(viewParent, context.getString(R.string.unable_to_del), Snackbar.LENGTH_SHORT).show();
                                }
                                break;

                            case Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT:
                                tracklist = MusicLibrary.getInstance().getSongListFromAlbumIdNew(
                                        filteredDataItems.get(position).album_id, Constants.SORT_ORDER.ASC);
                                for(int id:tracklist){

                                    TrackItem item = MusicLibrary.getInstance().getTrackItemFromId(id);
                                    if(item!=null) {
                                        files.add(new File(item.getFilePath()));
                                        ids.add(item.getId());
                                    }
                                }
                                if(UtilityFun.Delete(context, files, ids)){
                                    //Toast.makeText(context, "Deleted " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
                                    deleteSuccess();
                                } else {
                                    //Toast.makeText(context, "Cannot delete " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
                                    Snackbar.make(viewParent, context.getString(R.string.unable_to_del), Snackbar.LENGTH_SHORT).show();
                                }
                                break;

                            case Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT:
                                tracklist = MusicLibrary.getInstance().getSongListFromArtistIdNew(
                                        filteredDataItems.get(position).artist_id, Constants.SORT_ORDER.ASC);
                                for(int id:tracklist){
                                    /*if(playerService.getCurrentTrack().getTitle().equals(track)){
                                        ///Toast.makeText(context,"One of the song is playing currently",Toast.LENGTH_SHORT).show();
                                        Snackbar.make(viewParent, "One of the song is playing currently", Snackbar.LENGTH_SHORT).show();
                                        return;
                                    }*/
                                    TrackItem item = MusicLibrary.getInstance().getTrackItemFromId(id);
                                    if(item!=null) {
                                        files.add(new File(item.getFilePath()));
                                        ids.add(item.getId());
                                    }
                                }
                                if(UtilityFun.Delete(context, files, ids)){
                                    //Toast.makeText(context, "Deleted " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
                                    deleteSuccess();
                                } else {
                                    //Toast.makeText(context, "Cannot delete " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
                                    Snackbar.make(viewParent, context.getString(R.string.unable_to_del), Snackbar.LENGTH_SHORT).show();
                                }
                                break;

                            case Constants.FRAGMENT_STATUS.GENRE_FRAGMENT:
                                tracklist = MusicLibrary.getInstance().getSongListFromGenreIdNew(
                                        filteredDataItems.get(position).id, Constants.SORT_ORDER.ASC);
                                for(int id:tracklist){
                                    /*if(playerService.getCurrentTrack().getTitle().equals(track)){
                                        //Toast.makeText(context,"One of the song is playing currently",Toast.LENGTH_SHORT).show();
                                        Snackbar.make(viewParent, "One of the song is playing currently", Snackbar.LENGTH_SHORT).show();
                                        return;
                                    }*/
                                    TrackItem item = MusicLibrary.getInstance().getTrackItemFromId(id);
                                    if(item!=null) {
                                        files.add(new File(item.getFilePath()));
                                        ids.add(item.getId());
                                    }
                                }
                                if(UtilityFun.Delete(context, files, ids)){
                                    //Toast.makeText(context, "Deleted " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
                                    deleteSuccess();
                                } else {
                                    //Toast.makeText(context, "Cannot delete " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
                                    Snackbar.make(viewParent, context.getString(R.string.unable_to_del), Snackbar.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    }

                    private void deleteSuccess() {
                        Snackbar.make(viewParent, context.getString(R.string.deleted) + filteredDataItems.get(position).title, Snackbar.LENGTH_SHORT).show();
                        playerService.removeTrack((Integer)filteredDataItems.get(position).id);
                        dataItems.remove(dataItems.get(position));
                        filteredDataItems.remove(filteredDataItems.get(position));
                        notifyItemRemoved(position);
                        //notifyDataSetChanged();
                    }
                })
                .show();
    }

    public void updateItem(int position, String ... param){
        if(param.length==1){
            filteredDataItems.get(position).title = param[0];
            notifyItemChanged(position);
        }else {
           filteredDataItems.get(position).title = param[0];
           filteredDataItems.get(position).artist_name = param[1];
           filteredDataItems.get(position).albumName = param[2];
           notifyItemChanged(position);
        }
    }

    public int getHeight(){
        return mItemHeight;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return filteredDataItems.get(position).title.substring(0,1).toUpperCase();
    }

    //action mode related methods
    //methods for item selection
    public void toggleSelection(int pos) {

        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        }
        else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        return filteredDataItems.get(pos).title.substring(0,1).toUpperCase();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title,secondary,count;
        ImageView image;
        View wrapper;

        MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.header);

            secondary = itemView.findViewById(R.id.secondaryHeader);

            count = itemView.findViewById(R.id.count);
            image= itemView.findViewById(R.id.imageVIewForStubAlbumArt);

            wrapper = itemView.findViewById(R.id.album_art_wrapper);
            itemView.setOnClickListener(this);
            itemView.findViewById(R.id.menuPopup).setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            MainLibraryAdapter.this.onClick(v,this.getLayoutPosition());
        }


    }
}
