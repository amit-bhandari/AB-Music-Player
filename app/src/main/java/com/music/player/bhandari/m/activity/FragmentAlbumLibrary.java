package com.music.player.bhandari.m.activity;

/**
 * Created by Amit AB on 16/1/17.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.FastScroller;
import com.music.player.bhandari.m.adapter.AlbumLibraryAdapter;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;

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

public class FragmentAlbumLibrary extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private RecyclerView mRecyclerView;
    private FastScroller fastScroller;
    private AlbumLibraryAdapter albumLibraryAdapter;
    //private SwipeRefreshLayout swipeRefreshLayout;
    private BroadcastReceiver mRefreshLibraryReceiver;

    public FragmentAlbumLibrary() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRefreshLibraryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                albumLibraryAdapter = new AlbumLibraryAdapter(getContext(), MusicLibrary.getInstance().getDataItemsForAlbums());
                mRecyclerView.setAdapter(albumLibraryAdapter);
                //swipeRefreshLayout.setRefreshing(false);
            }
        };
    }

    @Override
    public void onDestroy() {
        mRecyclerView=null;
        super.onDestroy();
    }

    public void filter(String s){
        if(albumLibraryAdapter !=null) {
            albumLibraryAdapter.filter(s);
        }
    }

    public void sort(int sort_id){
        if(albumLibraryAdapter !=null) {
            albumLibraryAdapter.sort(sort_id);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mRefreshLibraryReceiver
                ,new IntentFilter(Constants.ACTION.REFRESH_LIB));
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mRefreshLibraryReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_library, container, false);
        //swipeRefreshLayout = layout.findViewById(R.id.swipeRefreshLayout);
        //swipeRefreshLayout.setOnRefreshListener(this);


        mRecyclerView = layout.findViewById(R.id.recyclerviewList);
        fastScroller = layout.findViewById(R.id.fastscroller);
        fastScroller.setRecyclerView(mRecyclerView);

        /*mRecyclerView.setTrackColor(ColorHelper.getColor(R.color.colorTransparent));
        mRecyclerView.setThumbColor(ColorHelper.getAccentColor());
        mRecyclerView.setPopupBgColor(ColorHelper.getAccentColor());*/

        albumLibraryAdapter = new AlbumLibraryAdapter(getContext(), MusicLibrary.getInstance().getDataItemsForAlbums());
        albumLibraryAdapter.sort(MyApp.getPref().getInt(getString(R.string.pref_album_sort_by),Constants.SORT_BY.NAME));

        mRecyclerView.setAdapter(albumLibraryAdapter);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (dy > 0  )
                {
                    ((ActivityMain)getActivity()).hideFab(true);
                }else ((ActivityMain)getActivity()).hideFab(false);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState)
            {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    ((ActivityMain)getActivity()).hideFab(false);
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        return layout;
    }


    @Override
    public void onRefresh() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                MusicLibrary.getInstance().RefreshLibrary();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.setAdapter(null);
    }

}

