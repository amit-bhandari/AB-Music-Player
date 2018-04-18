package com.music.player.bhandari.m.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
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

import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.activity.ActivityTagEditor;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.dataItem;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.model.PlaylistManager;
import com.music.player.bhandari.m.utils.UtilityFun;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Amit AB on 6/12/16.
 */

public class SecondaryLibraryAdapter extends RecyclerView.Adapter<SecondaryLibraryAdapter.MyViewHolder>
        implements PopupMenu.OnMenuItemClickListener {

    private final String REMOVE = "Remove";
    private ArrayList<dataItem> dataItems = new ArrayList<>();

    private Context context;
    private LayoutInflater inflater;
    private int position=0;

    private int clikedON;
    private int status;
    private String playlist_name;

    private PlayerService playerService;

    public SecondaryLibraryAdapter(Context context, ArrayList<Integer> data){
        this.context=context;
        inflater=LayoutInflater.from(context);

        for (dataItem d: MusicLibrary.getInstance().getDataItemsForTracks()){
            if(data.contains(d.id))
                dataItems.add(d);
        }

        /*
        Collections.sort(dataItems, new Comparator<dataItem>() {
            @Override
            public int compare(dataItem o1, dataItem o2) {
                return o1.albumName.compareToIgnoreCase(o2.albumName);
            }
        });

        //add extra empty element
        //dataItems.add(new dataItem(0,"",0,"",0,"","","",""));
        //dataItems.add(new dataItem(0,"",0,"",0,"","","",""));

        setHasStableIds(true);*/
        bindService();
    }

    //constructor for getMostPlayed and getRecentlyPlayed
    public SecondaryLibraryAdapter(Context context, ArrayList<dataItem> data, int status, String playlist_name) {
        this.playlist_name = playlist_name;
        this.context=context;
        inflater=LayoutInflater.from(context);
        dataItems.addAll(data);
        //add extra empty element
        //dataItems.add(new dataItem(0,"",0,"",0,"","","",""));
        //dataItems.add(new dataItem(0,"",0,"",0,"","","",""));
        setHasStableIds(true);
        bindService();
        this.status=status;
    }

    public void shuffleAll() {
        if (dataItems != null) {
            //remove empty element from list of headers

            ArrayList<Integer> temp = new ArrayList<>();
            for(dataItem d:dataItems){
                if(d.id != 0) {
                    temp.add(d.id);
                }
            }
            Collections.shuffle(temp);
            playerService.setTrackList(temp);
            playerService.playAtPosition(0);
        }
    }

    public void bindService(){
        playerService = MyApp.getService();
    }

    public ArrayList<dataItem> getList(){return dataItems;}

    @Override
    public SecondaryLibraryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.fragment_library_item, parent, false);
        View viewParent = parent;
        //int color = ColorHelper.getColor(R.color.colorwhite) ;
        int color = ColorHelper.getBaseThemeTextColor();
        ((TextView)(view.findViewById(R.id.header))).setTextColor(color);
        ((TextView)(view.findViewById(R.id.secondaryHeader))).setTextColor(color);
        ((TextView)(view.findViewById(R.id.count))).setTextColor(color);
        ((ImageView)(view.findViewById(R.id.menuPopup))).setColorFilter(color);

        return new SecondaryLibraryAdapter.MyViewHolder (view);
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
    public void onBindViewHolder(SecondaryLibraryAdapter.MyViewHolder holder, int position) {

        Log.d("SecondaryLibraryAdapter", "onBindViewHolder: " + dataItems.get(position).title);


        if(dataItems.get(position).title.equals("")){
            holder.itemView.setVisibility(View.GONE);
            return;
        }

        holder.title.setPadding(20,0,0,0);
        holder.secondary.setPadding(20,0,0,0);
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

    public void onClick(View view,int position) {
        this.position=position;
        if(dataItems.get(position).title.equals("")){
            return;
        }

        clikedON = dataItems.get(position).id;
        switch (view.getId()) {
            case R.id.trackItem:
                if(MyApp.isLocked()){
                    Toast.makeText(context,"Music is Locked!",Toast.LENGTH_SHORT).show();
                    //Snackbar.make(viewParent, "Music is Locked!", Snackbar.LENGTH_LONG).show();
                    return ;
                }
                Play();
            break;

            case R.id.menuPopup:
                PopupMenu popup = new PopupMenu(context, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_tracks_by_title, popup.getMenu());
                if(status==Constants.FRAGMENT_STATUS.PLAYLIST_FRAGMENT){
                    //popup.getMenu().removeItem(R.id.action_delete);
                    if( !( playlist_name.replace(" ","_").equals(Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED)
                            || playlist_name.replace(" ","_").equals(Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED)
                            || playlist_name.replace(" ","_").equals(Constants.SYSTEM_PLAYLISTS.MOST_PLAYED))) {
                        popup.getMenu().add(REMOVE);
                    }
                }
                popup.getMenu().removeItem(R.id.action_exclude_folder);

                popup.show();
                popup.setOnMenuItemClickListener(this);
                break;

        }
    }

    public void updateItem(int position, String ... param){
        dataItems.get(position).title = param[0];
        dataItems.get(position).artist_name = param[0];
        dataItems.get(position).albumName = param[0];
        notifyItemChanged(position);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_play:
                if(MyApp.isLocked()){
                    Toast.makeText(context,context.getString(R.string.music_is_locked) ,Toast.LENGTH_SHORT).show();
                    //Snackbar.make(viewParent, "Music is Locked!", Snackbar.LENGTH_LONG).show();
                    return true;
                }
                Play();
                break;

            case R.id.action_add_to_playlist:
                int[] ids;
                ids=new int[]{dataItems.get(position).id};
                UtilityFun.AddToPlaylist(context, ids);
                break;

            case R.id.action_share:
                ArrayList<Uri> files = new ArrayList<>();
                try {
                    File fileToBeShared = new File(MusicLibrary.getInstance().getTrackItemFromId(clikedON).getFilePath());
                    files.add(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + "com.bhandari.music.provider", fileToBeShared));
                    UtilityFun.Share(context, files, dataItems.get(position).title);
                }catch (Exception e) {
                    Toast.makeText(context,context.getString(R.string.error_something_wrong) ,Toast.LENGTH_LONG).show();
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

            case R.id.action_track_info:
                setTrackInfoDialog();
                break;

            case R.id.action_edit_track_info:
                TrackItem editItem = MusicLibrary.getInstance().getTrackItemFromId(dataItems.get(position).id);
                if(editItem==null){
                    Toast.makeText(context,context.getString(R.string.unknown_error) ,Toast.LENGTH_SHORT).show();
                    //Snackbar.make(viewParent, "Error occurred!", Snackbar.LENGTH_LONG).show();
                    return true;
                }
                context.startActivity(new Intent(context, ActivityTagEditor.class)
                        .putExtra("from",Constants.TAG_EDITOR_LAUNCHED_FROM.SECONDARY_LIB)
                        .putExtra("file_path",editItem.getFilePath())
                        .putExtra("track_title",editItem.getTitle())
                        .putExtra("position",position)
                        .putExtra("id",editItem.getId()));
                break;

            case R.id.action_set_as_ringtone:
                TrackItem tempItem = MusicLibrary.getInstance().getTrackItemFromId(dataItems.get(position).id);
                if(tempItem==null){
                    Toast.makeText(context,context.getString(R.string.error_something_wrong) ,Toast.LENGTH_LONG).show();
                    //Snackbar.make(viewParent, "Something went wrong!", Snackbar.LENGTH_LONG).show();
                    return false;
                }
                UtilityFun.SetRingtone(context, tempItem.getFilePath()
                        ,tempItem.getId());
                break;
        }
        if (item.getTitle().equals(REMOVE)) {
            PlaylistManager.getInstance(context).RemoveSongFromPlaylistNew(playlist_name,dataItems.get(position).id);
            dataItems.remove(position);
            notifyItemRemoved(position);
        }
        return true;
    }

    private void setTrackInfoDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(context.getString(R.string.track_info_title) );
        LinearLayout linear = new LinearLayout(context);
        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text = new TextView(context);
        text.setTypeface(TypeFaceHelper.getTypeFace(context));
        text.setText(UtilityFun.trackInfoBuild(dataItems.get(position).id).toString());

        text.setPadding(20, 20,20,10);
        text.setTextSize(15);
        //text.setGravity(Gravity.CENTER);

        linear.addView(text);
        alert.setView(linear);
        alert.setPositiveButton(context.getString(R.string.okay) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        alert.show();
    }

    private void Play(){
        ArrayList<Integer> temp = new ArrayList<>();
        for(dataItem d: dataItems){
            if(d.id != 0) {
                temp.add(d.id);
            }
        }
        playerService.setTrackList(temp);
        playerService.playAtPosition(position);
    }

    private void AddToQ(int positionToAdd){
        //we are using same function for adding to q and playing next
        // toastString is to identify which string to disokay as toast
        String toastString=(positionToAdd==Constants.ADD_TO_Q.AT_LAST ? context.getString(R.string.added_to_q)
                : context.getString(R.string.playing_next) ) ;

        playerService.addToQ(clikedON, positionToAdd);
        //to update the to be next field in notification
        MyApp.getService().PostNotification();
        Toast.makeText(context
                ,toastString+dataItems.get(position).title
                ,Toast.LENGTH_SHORT).show();
        //Snackbar.make(viewParent, toastString+clikedON, Snackbar.LENGTH_LONG).show();
    }

    private void DeleteDialog(){

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        if(playerService.getCurrentTrack().getTitle().equals(dataItems.get(position).title)){
                            Toast.makeText(context,context.getString(R.string.song_is_playing) ,Toast.LENGTH_SHORT).show();
                           // Snackbar.make(viewParent, "Cannot delete currently playing song", Snackbar.LENGTH_LONG).show();
                            return;
                        }

                        File file;
                        try {
                            file = new File(MusicLibrary.getInstance().getTrackItemFromId(dataItems.get(position).id)
                                    .getFilePath());
                        }catch (Exception e){
                            return;
                        }
                        //delete the file first
                        ArrayList<File> files = new ArrayList<>();
                        files.add(file);
                        ArrayList<Integer> ids = new ArrayList<>();
                        ids.add(dataItems.get(position).id);
                        if(UtilityFun.Delete(context, files, ids)){
                            Toast.makeText(context, context.getString(R.string.deleted)  + dataItems.get(position).title, Toast.LENGTH_SHORT).show();
                            dataItems.remove(dataItems.get(position));
                            notifyItemRemoved(position);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(context, context.getString(R.string.unable_to_del)
                                    , Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.are_u_sure))
                .setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                .setNegativeButton(context.getString(R.string.no), dialogClickListener).show();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title,secondary,count;
        ImageButton popUp;

        MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.header);
            //title.setTypeface(TypeFaceHelper.getTypeFace());

            secondary = itemView.findViewById(R.id.secondaryHeader);
            //secondary.setTypeface(TypeFaceHelper.getTypeFace());

            count = itemView.findViewById(R.id.count);
            //count.setTypeface(TypeFaceHelper.getTypeFace());

            itemView.findViewById(R.id.album_art_wrapper).setVisibility(View.GONE);
            popUp = itemView.findViewById(R.id.menuPopup);
            itemView.setOnClickListener(this);
            itemView.findViewById(R.id.menuPopup).setOnClickListener(this);
            itemView.findViewById(R.id.imageVIewForStubAlbumArt).setVisibility(View.GONE);
        }

        @Override
        public void onClick(View view) {
            SecondaryLibraryAdapter.this.onClick(view,getLayoutPosition());
        }
    }
}
