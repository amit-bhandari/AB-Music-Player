package com.music.player.bhandari.m.activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.BottomOffsetDecoration;
import com.music.player.bhandari.m.UIElementHelper.FastScroller;
import com.music.player.bhandari.m.adapter.MainLibraryAdapter;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;

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

public class FragmentLibrary extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener {
    private int status;
    private MainLibraryAdapter cursoradapter;
    private RecyclerView mRecyclerView;
    private BroadcastReceiver mRefreshLibraryReceiver;

    public FragmentLibrary() {
    }

    public int getStatus(){return status;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(this.getArguments()!=null) {
            this.status = this.getArguments().getInt("status");
        }
        mRefreshLibraryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v("FragmentLibrary","Items found tracks = "+MusicLibrary.getInstance().getDataItemsForTracks().size());
                Log.v("FragmentLibrary","Items found art= "+MusicLibrary.getInstance().getDataItemsArtist().size());
                Log.v("FragmentLibrary","Items found alb= "+MusicLibrary.getInstance().getDataItemsForAlbums().size());
                Log.v("FragmentLibrary","Items found genr= "+MusicLibrary.getInstance().getDataItemsForGenres().size());
                switch (status){
                    case Constants.FRAGMENT_STATUS.TITLE_FRAGMENT:
                        cursoradapter=new MainLibraryAdapter(FragmentLibrary.this, getContext()
                                ,new ArrayList<>(MusicLibrary.getInstance().getDataItemsForTracks().values()));
                        cursoradapter.sort(MyApp.getPref().getInt(getString(R.string.pref_tracks_sort_by),Constants.SORT_BY.NAME));
                        break;

                    case Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT:
                        cursoradapter=new MainLibraryAdapter(FragmentLibrary.this, getContext()
                                , MusicLibrary.getInstance().getDataItemsArtist());
                        cursoradapter.sort(MyApp.getPref().getInt(getString(R.string.pref_artist_sort_by),Constants.SORT_BY.NAME));
                        break;

                    case Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT:
                        cursoradapter=new MainLibraryAdapter(FragmentLibrary.this, getContext()
                                , MusicLibrary.getInstance().getDataItemsForAlbums());
                        cursoradapter.sort(MyApp.getPref().getInt(getString(R.string.pref_album_sort_by),Constants.SORT_BY.NAME));
                        break;

                    case Constants.FRAGMENT_STATUS.GENRE_FRAGMENT:
                        cursoradapter=new MainLibraryAdapter(FragmentLibrary.this, getContext()
                                , MusicLibrary.getInstance().getDataItemsForGenres());
                        cursoradapter.sort(MyApp.getPref().getInt(getString(R.string.pref_genre_sort_by),Constants.SORT_BY.NAME));
                        break;
                }

                notifyDataSetChanges();
                mRecyclerView.setAdapter(cursoradapter);
            }
        };

    }

    @Override
    public void onDestroy() {
        if(cursoradapter!=null)
            cursoradapter.clear();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.setAdapter(null);
    }

    public void filter(String s){
        if(cursoradapter!=null) {
            cursoradapter.filter(s);
        }
    }

    public void notifyDataSetChanges(){
        if(cursoradapter!=null){
            cursoradapter.notifyDataSetChanged();
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
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_library, container, false);

        mRecyclerView = layout.findViewById(R.id.recyclerviewList);

        FastScroller fastScroller = layout.findViewById(R.id.fastscroller);
        fastScroller.setRecyclerView(mRecyclerView);

        initializeAdapter(status);

        float offsetPx = getResources().getDimension(R.dimen.bottom_offset_dp);
        BottomOffsetDecoration bottomOffsetDecoration = new BottomOffsetDecoration((int) offsetPx);
        mRecyclerView.addItemDecoration(bottomOffsetDecoration);

        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getActivity()));
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

    public void initializeAdapter(int status){
        Log.d("FragmentLibrary", "initializeAdapter: ");
        switch (status){
            case Constants.FRAGMENT_STATUS.TITLE_FRAGMENT:
                cursoradapter=new MainLibraryAdapter(FragmentLibrary.this, getContext()
                        ,new ArrayList<>(MusicLibrary.getInstance().getDataItemsForTracks().values()));
                cursoradapter.sort(MyApp.getPref().getInt(getString(R.string.pref_tracks_sort_by),Constants.SORT_BY.NAME));
                break;

            case Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT:
                cursoradapter=new MainLibraryAdapter(FragmentLibrary.this, getContext()
                        , MusicLibrary.getInstance().getDataItemsArtist());
                cursoradapter.sort(MyApp.getPref().getInt(getString(R.string.pref_artist_sort_by),Constants.SORT_BY.NAME));
                break;

            case Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT:
                cursoradapter=new MainLibraryAdapter(FragmentLibrary.this, getContext()
                        , MusicLibrary.getInstance().getDataItemsForAlbums());
                cursoradapter.sort(MyApp.getPref().getInt(getString(R.string.pref_album_sort_by),Constants.SORT_BY.NAME));
                break;

            case Constants.FRAGMENT_STATUS.GENRE_FRAGMENT:
                cursoradapter=new MainLibraryAdapter(FragmentLibrary.this, getContext()
                        , MusicLibrary.getInstance().getDataItemsForGenres());
                cursoradapter.sort(MyApp.getPref().getInt(getString(R.string.pref_genre_sort_by),Constants.SORT_BY.NAME));
                break;
        }

        Log.v("FragmentLibrary","item count "+cursoradapter.getItemCount());
        //cursoradapter.setHasStableIds(true);
        mRecyclerView.setAdapter(cursoradapter);
    }

    public void sort(int sort_id){
        if(cursoradapter !=null) {
            cursoradapter.sort(sort_id);
        }
    }

    public void updateItem(int position, String ...param){
        if(cursoradapter !=null) {
            cursoradapter.updateItem(position, param);
        }
    }

    @Override
    public void onRefresh() {
        MusicLibrary.getInstance().RefreshLibrary();
    }

    //for catching exception generated by recycler view which was causing abend, no other way to handle this
        class WrapContentLinearLayoutManager extends LinearLayoutManager {
            WrapContentLinearLayoutManager(Context context) {
                super(context);
            }

            //... constructor
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                }
            }
        }
}

