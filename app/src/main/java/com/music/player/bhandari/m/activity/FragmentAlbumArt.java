package com.music.player.bhandari.m.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.service.PlayerService;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.utils.UtilityFun;

import butterknife.BindView;
import butterknife.ButterKnife;

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

public class FragmentAlbumArt extends Fragment{

    private PlayerService playerService;
    private BroadcastReceiver mUIUpdate;
    @BindView(R.id.album_art_now_playing) ImageView albumArt;

    public FragmentAlbumArt(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View layout = inflater.inflate(R.layout.fragment_album_art, container, false);

        /*Configuration configuration = getActivity().getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.

        Log.d("Fragment Disc", "onCreateView: " + screenWidthDp);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(screenWidthDp -50, screenWidthDp -50);*/

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(isAdded() && getActivity()!=null){
            //exit animation
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActivity().startPostponedEnterTransition();
            }

            //place album art view properly in center
            albumArt.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                albumArt.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                albumArt.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }

                            //y position of control buttons
                            float yControl = ((ActivityNowPlaying)getActivity()).yControl;

                            //height of toolbar
                            float toolbarHeight =  ((ActivityNowPlaying)getActivity()).toolbarHeight;

                            if(toolbarHeight!=0 || yControl!=0) {
                                //centre the album art
                                albumArt.setY(((yControl - toolbarHeight) / 2) - albumArt.getHeight() / 2);
                            }
                        }
                    });

        }
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

            final DrawableRequestBuilder<Uri> request = Glide.with(this)
                    .load(MusicLibrary.getInstance().getAlbumArtUri(playerService.getCurrentTrack().getAlbumId()))
                    .centerCrop()
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            int defaultAlbumArtSetting = MyApp.getPref().getInt(getString(R.string.pref_default_album_art), 0);

            final int[] retryCount = {0};

            switch (defaultAlbumArtSetting){
                case 0:
                    request.listener(new RequestListener<Uri, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    //Log.d("AlbumLibraryAdapter", "onException: ");
                                    if(UtilityFun.isConnectedToInternet() &&
                                            !MyApp.getPref().getBoolean(getString(R.string.pref_data_saver), false)) {
                                        final String url = MusicLibrary.getInstance().getArtistUrls().get(playerService.getCurrentTrack().getArtist());
                                        if(url!=null && !url.isEmpty())
                                            request.load(Uri.parse(url))
                                                    .into(albumArt);
                                        return true;
                                    }
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    //if(retryCount[0]>0) return true;
                                    //retryCount[0]++;
                                    return false;
                                }
                            })
                            .placeholder(R.drawable.ic_batman_1);
                    break;

                case 1:
                   request.listener(new RequestListener<Uri, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    //Log.d("AlbumLibraryAdapter", "onException: ");
                                    if(UtilityFun.isConnectedToInternet() &&
                                            !MyApp.getPref().getBoolean(getString(R.string.pref_data_saver), false)) {
                                        final String url = MusicLibrary.getInstance().getArtistUrls().get(playerService.getCurrentTrack().getArtist());
                                        if(url!=null && !url.isEmpty())
                                            request.load(Uri.parse(url))
                                                    .into(albumArt);
                                        return true;
                                    }
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    //if(retryCount[0]>0) return true;
                                    //retryCount[0]++;
                                    return false;
                                }
                            })
                            .placeholder(UtilityFun.getDefaultAlbumArtDrawable());

                    break;
            }

            request.into(albumArt);

        }
    }

}
