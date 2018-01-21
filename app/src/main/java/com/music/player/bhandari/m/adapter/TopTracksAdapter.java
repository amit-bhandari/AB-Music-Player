package com.music.player.bhandari.m.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.activity.ActivityExploreLyrics;
import com.music.player.bhandari.m.activity.ActivityLyricView;
import com.music.player.bhandari.m.customViews.RoundedImageView;
import com.music.player.bhandari.m.lyricsExplore.Track;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by abami on 12/6/2017.
 */

public class TopTracksAdapter extends RecyclerView.Adapter<TopTracksAdapter.MyViewHolder> implements PopupMenu.OnMenuItemClickListener {

    private Context context;
    private LayoutInflater inflater;
    private List<Track> trackList;
    private int clickedPostion;

    public TopTracksAdapter(Context context, List<Track> trackList){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.trackList = trackList;
        setHasStableIds(true);
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
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.track_item_explore_lyrics, parent, false);
        return new MyViewHolder (view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        String trackInfo = trackList.get(position).artist + " - " + trackList.get(position).title ;
        String playcount = "Playcount - " + trackList.get(position).playCount;
        holder.trackName.setText(trackInfo);
        holder.playCount.setText(playcount);

        Glide.with(context)
                .load(trackList.get(position).imageUrl)
                .asBitmap()
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleTarget<Bitmap>(300, 300) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        holder.imageView.setImageBitmap(resource);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    private void onClick(View v, int position) {
        /**/
        clickedPostion = position;
        switch (v.getId()) {
            case R.id.more:
                PopupMenu popup = new PopupMenu(context, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_explore_lyric_item, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(this);
                break;

            case R.id.root_view_item_explore_lyrics:
                Intent intent = new Intent(context, ActivityLyricView.class);
                intent.putExtra("track_title", trackList.get(position).title);
                intent.putExtra("artist", trackList.get(position).artist);
                context.startActivity(intent);
                if (context instanceof ActivityExploreLyrics) {
                    ((ActivityExploreLyrics) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search_youtube:
                UtilityFun.LaunchYoutube(context,trackList.get(clickedPostion).artist + " - " + trackList.get(clickedPostion).title);
                break;
        }
        return true;
    }


    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.trackInfo) TextView trackName;
        @BindView(R.id.playCount) TextView playCount;
        @BindView(R.id.imageView) RoundedImageView imageView;
        @BindView(R.id.more) ImageView overflow;
        MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            overflow.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            TopTracksAdapter.this.onClick(v,getLayoutPosition());
        }
    }

}
