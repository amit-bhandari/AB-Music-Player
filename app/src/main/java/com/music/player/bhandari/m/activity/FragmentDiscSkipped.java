package com.music.player.bhandari.m.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.utils.AppLaunchCountManager;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Amit Bhandari on 3/10/2017.
 */

public class FragmentDiscSkipped extends Fragment{

    private PlayerService playerService;
    private BroadcastReceiver mUIUpdate;
    @BindView(R.id.album_art_now_playing) ImageView albumArt;

    public FragmentDiscSkipped(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View layout = inflater.inflate(R.layout.fragment_disc_skipped, container, false);

        ButterKnife.bind(this, layout);

        playerService = MyApp.getService();

        mUIUpdate = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(Constants.TAG, "update disc please Jarvis");
                UpdateUI();
            }
        };

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        Log.v(Constants.TAG,"Disc paused........");
        if(getContext()!=null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUIUpdate);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.v(Constants.TAG,"Disc resumed........");
        if(getContext()!=null) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUIUpdate
                    , new IntentFilter(Constants.ACTION.COMPLETE_UI_UPDATE));
        }

        UpdateUI();
        super.onResume();
    }

    private void UpdateUI(){

        if(getActivity()==null || !isAdded() || playerService.getCurrentTrack()==null){
            return;
        }

        int currentNowPlayingBackPref = MyApp.getPref().getInt(getString(R.string.pref_now_playing_back),1);
        //if album art selected, hide small album art
        if(currentNowPlayingBackPref==2){
            albumArt.setImageBitmap(null);
        }else {
            /*
            Bitmap b = null;
            try {
                b = UtilityFun.decodeUri(getContext()
                        , MusicLibrary.getInstance().getAlbumArtUri(playerService.getCurrentTrack().getAlbumId()), 500);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (b != null) {
                albumArt.setImageBitmap(b);
            } else {
                albumArt.setImageDrawable(getResources().getDrawable(R.drawable.ic_batman_1));
            }*/
            Glide
                    .with(this)
                    .load(MusicLibrary.getInstance().getAlbumArtUri(playerService.getCurrentTrack().getAlbumId()))
                    .listener(new RequestListener<Uri, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                            Log.d("AlbumLibraryAdapter", "onException: ");
                            if(UtilityFun.isConnectedToInternet() &&
                                    !MyApp.getPref().getBoolean(getString(R.string.pref_data_saver), false)) {

                                final String url = MusicLibrary.getInstance().getArtistUrls().get(playerService.getCurrentTrack().getArtist());
                                Glide
                                        .with(getActivity())
                                        .load(url)
                                        .centerCrop()
                                        .crossFade(500)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        //.override(100, 100)
                                        .placeholder(R.drawable.ic_batman_1)
                                        .into(albumArt);
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .placeholder(R.drawable.ic_batman_1)
                    .crossFade()
                    .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                    .into(albumArt);

        }
    }

}
