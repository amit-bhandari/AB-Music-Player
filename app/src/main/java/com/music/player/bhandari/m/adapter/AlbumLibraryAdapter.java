package com.music.player.bhandari.m.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.BubbleTextGetter;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.activity.ActivitySecondaryLibrary;
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

public class AlbumLibraryAdapter extends RecyclerView.Adapter<AlbumLibraryAdapter.MyViewHolder>
        implements PopupMenu.OnMenuItemClickListener, FastScrollRecyclerView.SectionedAdapter, BubbleTextGetter {

    private Context context;
    private LayoutInflater inflater;
    private PlayerService playerService;
    private int position;
    private ArrayList<dataItem> dataItems=new ArrayList<>();   //actual data
    private ArrayList<dataItem> filteredDataItems=new ArrayList<>();
    private View viewParent;

    private Drawable batmanDrawable;

    public void sort(int sort_id){
        int sort_order = MyApp.getPref().getInt(context.getResources().getString(R.string.pref_order_by),Constants.SORT_BY.ASC);
        switch (sort_id){
            case Constants.SORT_BY.NAME:
                if(sort_order==Constants.SORT_BY.ASC) {
                    Collections.sort(filteredDataItems, new Comparator<dataItem>() {
                        @Override
                        public int compare(dataItem o1, dataItem o2) {
                            return o1.albumName.compareToIgnoreCase(o2.albumName);
                        }
                    });
                }else {
                    Collections.sort(filteredDataItems, new Comparator<dataItem>() {
                        @Override
                        public int compare(dataItem o1, dataItem o2) {
                            return o2.albumName.compareToIgnoreCase(o1.albumName);
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
        }


        notifyDataSetChanged();
    }

    public AlbumLibraryAdapter(Context context, ArrayList<dataItem> data){
        this.context=context;
        inflater= LayoutInflater.from(context);
        playerService = MyApp.getService();
        this.dataItems = data;
        filteredDataItems.addAll(dataItems);
        setHasStableIds(true);

        int defaultAlbumArtSetting = MyApp.getPref().getInt(context.getString(R.string.pref_default_album_art), 0);
        switch (defaultAlbumArtSetting) {
            case 0:
                batmanDrawable = ContextCompat.getDrawable(context, R.drawable.ic_batman_1);
                break;

            case 1:
                batmanDrawable = UtilityFun.getDefaultAlbumArtDrawable();
                break;
        }
    }

    public void filter(String searchQuery){
        if(!searchQuery.equals("")){
            filteredDataItems.clear();
            for(dataItem d:dataItems){
                if(d.title.toLowerCase().contains(searchQuery)
                        || d.artist_name.toLowerCase().contains(searchQuery)){
                    filteredDataItems.add(d);
                }
            }
        }else {
            filteredDataItems.clear();
            filteredDataItems.addAll(dataItems);
        }
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.album_card, parent, false);
        ((TextView)view.findViewById(R.id.title)).setTextColor(ColorHelper.getPrimaryTextColor());
        ((TextView)view.findViewById(R.id.count)).setTextColor(ColorHelper.getSecondaryTextColor());
        ((ImageView)view.findViewById(R.id.overflow)).setColorFilter(ColorHelper.getSecondaryTextColor());
        ((CardView)view.findViewById(R.id.album_library_card)).setCardBackgroundColor(ColorHelper.getColor(R.color.colorTransparent));
        view.getLayoutParams ().width = UtilityFun.getScreenWidth()/3;
        viewParent = parent;
        return new AlbumLibraryAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        holder.title.setText(filteredDataItems.get(position).albumName);

        final String url = MusicLibrary.getInstance().getArtistUrls().get(filteredDataItems.get(position).artist_name);
        final Uri uri = MusicLibrary.getInstance().getAlbumArtUri(filteredDataItems.get(position).album_id);

        Glide
                .with(context)
                .load(uri)
                //.crossFade(500)
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
                                    .override(200, 200)
                                    .placeholder(batmanDrawable)
                                    .into(holder.thumbnail);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                //.signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .centerCrop()
                .placeholder(batmanDrawable)
                .into(holder.thumbnail);

        holder.count.setText(filteredDataItems.get(position).artist_name);
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

    public void onClick(View view, int position) {
        this.position=position;
        String title;
        int key;
        switch (view.getId()){
            case R.id.card_view_album:
                title=filteredDataItems.get(position).albumName;
                key=filteredDataItems.get(position).album_id;
                Intent intent = new Intent(context,ActivitySecondaryLibrary.class);
                intent.putExtra("status",Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT);
                intent.putExtra("key",key);
                intent.putExtra("title",title.trim());
                /*ActivityOptions options;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    options = ActivityOptions.makeSceneTransitionAnimation((ActivityMain)context
                            , view.findViewById(R.id.thumbnail), context.getString(R.string.secondary_transition));
                    ActivityCompat.startActivityForResult((ActivityMain)context, intent, 100, options.toBundle());
                }else {*/
                //((Activity) context).finish();
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                //}
                break;

            case R.id.overflow:
                PopupMenu popup = new PopupMenu(context, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_tracks_by_title, popup.getMenu());
                popup.getMenu().removeItem(R.id.action_set_as_ringtone);
                popup.getMenu().removeItem(R.id.action_track_info);
                popup.getMenu().removeItem(R.id.action_edit_track_info);
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
                ArrayList<Integer> temp;
                int album_id=filteredDataItems.get(position).album_id;
                temp = MusicLibrary.getInstance().getSongListFromAlbumIdNew(album_id,Constants.SORT_ORDER.ASC);
                int[] ids = new int[temp.size()];
                for (int i=0; i < ids.length; i++)
                {
                    ids[i] = temp.get(i);
                }
                UtilityFun.AddToPlaylist(context,ids);
                break;

            case R.id.action_share:
                ArrayList<Uri> files = new ArrayList<>();  //for sending multiple files
                for( int id : MusicLibrary.getInstance().getSongListFromAlbumIdNew(
                        filteredDataItems.get(position).album_id,Constants.SORT_ORDER.ASC)){
                    try {
                        File file = new File(MusicLibrary.getInstance().getTrackItemFromId(id).getFilePath());
                        Uri fileUri =
                                FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + "com.bhandari.music.provider", file);;
                        files.add(fileUri);
                    } catch (Exception e){
                       // Toast.makeText(context,"Something wrong!",Toast.LENGTH_LONG).show();
                        Snackbar.make(viewParent, context.getString(R.string.error_something_wrong), Snackbar.LENGTH_SHORT).show();
                        return true;
                    }
                }
                UtilityFun.Share(context, files, "Multiple audio files");
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

            case R.id.action_search_youtube:
                UtilityFun.LaunchYoutube(context,filteredDataItems.get(position).albumName + " - "
                        +filteredDataItems.get(position).artist_name);
        }
        return true;
    }

    private void Play(){
        int album_id=filteredDataItems.get(position).album_id;
        playerService.setTrackList(MusicLibrary.getInstance().getSongListFromAlbumIdNew(album_id,Constants.SORT_ORDER.ASC));
        playerService.playAtPosition(0);
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
        int album_id=filteredDataItems.get(position).album_id;
        for(int id:MusicLibrary.getInstance().getSongListFromAlbumIdNew(album_id,sortOrder)){
            playerService.addToQ(id, positionToAdd);
        }

        //to update the to be next field in notification
        MyApp.getService().PostNotification();
        //Toast.makeText(context
                //,toastString+filteredDataItems.get(position).title
              //  ,Toast.LENGTH_SHORT).show();
        Snackbar.make(viewParent, toastString+filteredDataItems.get(position).title, Snackbar.LENGTH_SHORT).show();

    }

    private void Delete(){

        new MyDialogBuilder(context)
                .title(context.getString(R.string.are_u_sure))
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ArrayList<Integer> ids = new ArrayList<>();
                        ArrayList<File> files = new ArrayList<>();
                        ArrayList<Integer> tracklist = MusicLibrary.getInstance().getSongListFromAlbumIdNew( filteredDataItems.get(position).album_id, Constants.SORT_ORDER.ASC);
                        for(int id:tracklist){
                            if(playerService.getCurrentTrack().getId()==id){
                                //Toast.makeText(context,"One of the song is playing currently",Toast.LENGTH_SHORT).show();
                                Snackbar.make(viewParent, context.getString(R.string.song_is_playing), Snackbar.LENGTH_SHORT).show();
                                return;
                            }
                            TrackItem item = MusicLibrary.getInstance().getTrackItemFromId(id);
                            if(item==null){
                                // Toast.makeText(context,"Something wrong!",Toast.LENGTH_LONG).show();
                                Snackbar.make(viewParent, context.getString(R.string.unable_to_del), Snackbar.LENGTH_SHORT).show();
                                return;
                            }
                            files.add(new File(item.getFilePath()));
                            ids.add(item.getId());
                        }

                        if(UtilityFun.Delete(context, files, ids)){
                            // Toast.makeText(context, "Deleted " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
                            Snackbar.make(viewParent, context.getString(R.string.deleted) + filteredDataItems.get(position).title, Snackbar.LENGTH_SHORT).show();
                            dataItems.remove(dataItems.get(position));
                            filteredDataItems.remove(filteredDataItems.get(position));
                            notifyItemRemoved(position);
                            // notifyDataSetChanged();
                        }  else {
                            //Toast.makeText(context, "Cannot delete " + filteredDataItems.get(position).title, Toast.LENGTH_SHORT).show();
                            Snackbar.make(viewParent, context.getString(R.string.unable_to_del), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return filteredDataItems.get(position).albumName.substring(0,1).toUpperCase();
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        return filteredDataItems.get(pos).albumName.substring(0,1).toUpperCase();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, count;
        ImageView thumbnail, overflow;

        MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);

            count = itemView.findViewById(R.id.count);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            overflow = itemView.findViewById(R.id.overflow);
            itemView.setOnClickListener(this);
            overflow.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            AlbumLibraryAdapter.this.onClick(v,this.getLayoutPosition());
        }
    }

}
