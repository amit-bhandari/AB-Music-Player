package com.music.player.bhandari.m.activity;

/**
 * Created by Amit AB on 16/1/17.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.adapter.AlbumLibraryAdapter;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.concurrent.Executors;


/**
 * Created by Amit AB on 29/11/16.
 */

public class FragmentAlbumLibrary extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private FastScrollRecyclerView mRecyclerView;
    private AlbumLibraryAdapter albumLibraryAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
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
                swipeRefreshLayout.setRefreshing(false);
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
        swipeRefreshLayout = layout.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);


        mRecyclerView = layout.findViewById(R.id.recyclerviewList);

        mRecyclerView.setTrackColor(ColorHelper.getColor(R.color.colorTransparent));
        mRecyclerView.setThumbColor(ColorHelper.getAccentColor());
        mRecyclerView.setPopupBgColor(ColorHelper.getAccentColor());

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

