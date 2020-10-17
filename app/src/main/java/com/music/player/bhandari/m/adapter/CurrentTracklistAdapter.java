package com.music.player.bhandari.m.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.MotionEventCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.activity.ActivityTagEditor;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.dataItem;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.UIElementHelper.recyclerviewHelper.ItemTouchHelperAdapter;
import com.music.player.bhandari.m.UIElementHelper.recyclerviewHelper.OnStartDragListener;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.utils.UtilityFun;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

public class CurrentTracklistAdapter extends RecyclerView.Adapter<CurrentTracklistAdapter.MyViewHolder>
        implements ItemTouchHelperAdapter, PopupMenu.OnMenuItemClickListener{

    private static ArrayList<dataItem> dataItems = new ArrayList<>();
    private PlayerService playerService;
    private long mLastClickTime;
    private OnStartDragListener mDragStartListener;
    private Context context;
    private LayoutInflater inflater;
    //current playing position
    private int position=0;
    private int tempPosition = 0; //temporary variable to hold position for onMenuItemClick
    private Handler handler;

    public CurrentTracklistAdapter(Context context, OnStartDragListener dragStartListener){
        mDragStartListener = dragStartListener;

        if(MyApp.getService()==null){
            UtilityFun.restartApp();
            return;
        }

        playerService = MyApp.getService();

        handler = new Handler(Looper.getMainLooper());

        fillData();


        position = playerService.getCurrentTrackPosition();
        this.context=context;
        inflater=LayoutInflater.from(context);
                //setHasStableIds(true);
    }

    //very badly written code
    public void fillData(){
        if(playerService==null) return;
        dataItems.clear();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<Integer> temp = playerService.getTrackList();
                //HashMap<dataItem> data = MusicLibrary.getInstance().getDataItemsForTracks();
                try {
                    for (int id:temp){
                        dataItem d = MusicLibrary.getInstance().getDataItemsForTracks().get(id);
                        if(d!=null){
                            dataItems.add(d);
                        }
                    }
                    Log.d("CurrentTrack", "run: queue ready");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                }catch (Exception ignored){
                    //ignore for now
                    Log.e("Notify","notify");
                }
            }
        });
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = inflater.inflate(R.layout.track_item_for_dragging, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CurrentTracklistAdapter.MyViewHolder holder, int position) {

        if(dataItems.get(position) == null) return;
        holder.title.setText(dataItems.get(position).title);
        holder.secondary.setText(dataItems.get(position).artist_name);

        holder.handle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (MotionEventCompat.getActionMasked(motionEvent) ==
                        MotionEvent.ACTION_DOWN) {
                    Log.d("CurrentTracklistAdapter", "onTouch: ");
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
        if(playerService!=null && position==playerService.getCurrentTrackPosition()) {
            holder.cv.setBackgroundColor(ColorHelper.getColor(R.color.gray3));
            holder.playAnimation.setVisibility(View.VISIBLE);
            if (playerService.getStatus()==PlayerService.PLAYING){
                //holder.iv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_black_24dp));
                holder.playAnimation.smoothToShow();
            }else {
                holder.playAnimation.smoothToHide();
                //holder.iv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
            }
            //holder.iv.setVisibility(View.VISIBLE);
        }else {
            holder.cv.setBackgroundColor(context.getResources().getColor(R.color.colorTransparent));
            //holder.iv.setVisibility(View.GONE);
            holder.playAnimation.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return dataItems.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        //no need to update list of in player service.
        //listOfHeader is reference for that list itself
        //it will automatically reflect in current tracklist in player service class
        Log.d("CurrentTracklistAdapter", "onItemMove: from to " + fromPosition + " : " + toPosition);
        playerService.swapPosition(fromPosition,toPosition);
        Collections.swap(dataItems,fromPosition,toPosition);
        notifyItemMoved(fromPosition,toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        if(playerService.getCurrentTrackPosition()!=position) {
            //listOfHeader.remove(position);
            playerService.removeTrack(position);
            dataItems.remove(position);
            notifyItemRemoved(position);
        }else {
            notifyItemChanged(position);
            //notifyDataSetChanged();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_play:
                int oldPos = this.position;
                this.position = this.tempPosition;
                playerService.playAtPositionFromNowPlaying(this.tempPosition);
                notifyItemChanged(oldPos);
                notifyItemChanged(position);
                Intent intent = new Intent().setAction(Constants.ACTION.COMPLETE_UI_UPDATE);
                intent.putExtra("skip_adapter_update", true);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                break;

            case R.id.action_add_to_playlist:
                int[] ids = new int[]{dataItems.get(position).id};
                UtilityFun.AddToPlaylist(context,ids);
                break;

            case R.id.action_share:
                try {
                    ArrayList<Uri> uris = new ArrayList<>();  //for sending multiple files
                    File file = new File(dataItems.get(position).file_path);
                    Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + "com.bhandari.music.provider", file);
                    uris.add(fileUri);
                    UtilityFun.Share(context, uris, dataItems.get(position).title);
                }catch (IllegalArgumentException e){
                    try{
                        UtilityFun.ShareFromPath(context, dataItems.get(position).file_path);
                    }catch (Exception ex) {
                        Toast.makeText(context, context.getString(R.string.error_unable_to_share), Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case R.id.action_delete:
                Delete();
                break;

            case R.id.action_track_info:
                setTrackInfoDialog();
                break;

            case R.id.action_edit_track_info:
                context.startActivity(new Intent(context, ActivityTagEditor.class)
                        .putExtra("from",Constants.TAG_EDITOR_LAUNCHED_FROM.NOW_PLAYING)
                        .putExtra("file_path",dataItems.get(position).file_path)
                        .putExtra("track_title",dataItems.get(position).title)
                        .putExtra("position",position)
                        .putExtra("id",dataItems.get(position).id));
                ((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

            case R.id.action_search_youtube:
                UtilityFun.LaunchYoutube(context,dataItems.get(position).artist_name + " - "
                        +dataItems.get(position).title);
                break;
        }
        return true;
    }

    public ArrayList<Integer> getSongList(){
        ArrayList<Integer>temp = new ArrayList<>();
        for(dataItem d:dataItems){
            if(d.id != 0) {
                temp.add(d.id);
            }
        }
        return temp;
    }

    public void updateItem(int position, String... param){
        try {
            dataItems.get(position).title = param[0];
            dataItems.get(position).artist_name = param[1];
            dataItems.get(position).albumName = param[2];
            notifyItemChanged(position);
        }catch (Exception e){
            Log.v(Constants.TAG, e.toString());
        }
    }

    private void setTrackInfoDialog(){
        //final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        //alert.setTitle(context.getString(R.string.track_info_title));
        LinearLayout linear = new LinearLayout(context);
        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text = new TextView(context);
        text.setTypeface(TypeFaceHelper.getTypeFace(context));
        text.setText(UtilityFun.trackInfoBuild(dataItems.get(position).id).toString());

        text.setPadding(20, 20,20,10);
        text.setTextSize(15);
        //text.setGravity(Gravity.CENTER);
        text.setTypeface(TypeFaceHelper.getTypeFace(context));
        linear.addView(text);
        //alert.setView(linear);
        //alert.show();

        new MyDialogBuilder(context)
                .title(context.getString(R.string.track_info_title))
                .customView(linear, true)
                .positiveText(R.string.okay)
                .show();
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

                        files.add(new File(dataItems.get(position).file_path));
                        ids.add(dataItems.get(position).id);
                        if(UtilityFun.Delete(context, files, ids)){  //last parameter not needed
                            Toast.makeText(context, context.getString(R.string.deleted) + dataItems.get(position).title, Toast.LENGTH_SHORT).show();
                            if(playerService.getCurrentTrack().getTitle().equals(dataItems.get(position).title)){
                                playerService.nextTrack();
                                //playerService.notifyUI();
                                notifyItemChanged(position+1);
                            }
                            playerService.removeTrack(position);
                            dataItems.remove(position);
                            notifyItemRemoved(position);
                            // notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, context.getString(R.string.unable_to_del), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    public void onClick(View view, int position) {
        this.tempPosition = position;
        switch (view.getId()){
            case R.id.more:
                PopupMenu popup = new PopupMenu(context, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_tracks_by_title, popup.getMenu());
                popup.getMenu().removeItem(R.id.action_set_as_ringtone);
                popup.getMenu().removeItem(R.id.action_add_to_q);
                popup.getMenu().removeItem(R.id.action_play_next);
                popup.getMenu().removeItem(R.id.action_exclude_folder);
                popup.show();
                popup.setOnMenuItemClickListener(this);
                break;


            case R.id.trackItemDraggable:
                int oldPos = this.position;
                this.position=position;
                if (SystemClock.elapsedRealtime() - mLastClickTime < 100){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                notifyItemChanged(oldPos);
                notifyItemChanged(position);
                if(position==playerService.getCurrentTrackPosition()){
                    playerService.play();
                    Intent intent = new Intent().setAction(Constants.ACTION.COMPLETE_UI_UPDATE);
                    intent.putExtra("skip_adapter_update", true);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    //playerService.notifyUI();
                }else {
                    playerService.playAtPositionFromNowPlaying(position);
                    Log.v(Constants.TAG,position+"  position");
                }
                break;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title,secondary;
        ImageView handle;
        View cv;
        //ImageView iv;

        AVLoadingIndicatorView playAnimation;

        MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.header);

            secondary = itemView.findViewById(R.id.secondaryHeader);

            handle = itemView.findViewById(R.id.handleForDrag);
            cv = itemView.findViewById(R.id.trackItemDraggable);
            //iv = itemView.findViewById(R.id.play_button_item_drag);
            playAnimation = itemView.findViewById(R.id.song_playing_animation);
            itemView.findViewById(R.id.more).setOnClickListener(this);
            itemView.findViewById(R.id.trackItemDraggable).setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            CurrentTracklistAdapter.this.onClick(v,this.getLayoutPosition());
        }
    }
}
