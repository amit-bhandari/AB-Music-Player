package com.music.player.bhandari.m.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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

import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.BottomOffsetDecoration;
import com.music.player.bhandari.m.UIElementHelper.FastScroller;
import com.music.player.bhandari.m.adapter.FolderLibraryAdapter;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.MusicLibrary;

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

public class FragmentFolderLibrary extends Fragment implements SwipeRefreshLayout.OnRefreshListener
        /*ActionMode.Callback*/{

    private  RecyclerView mRecyclerView;
    private  FolderLibraryAdapter adapter;

    /*overwrite back button for this fragment as we will be using same recycler view for
        walking into directory
     */
    private static BroadcastReceiver mReceiverForBackPressedAction;
    private BroadcastReceiver mReceiverForLibraryRefresh;

    public FragmentFolderLibrary(){
        mReceiverForBackPressedAction=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (adapter!= null){
                    adapter.onStepBack();
                }
            }
        };
        mReceiverForLibraryRefresh=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //updateUI();
                adapter=new FolderLibraryAdapter(getContext());
                Handler mHandler = new Handler(getContext().getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.setAdapter(adapter);
                        //swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        };
    }

    public void filter(String s){
        if(adapter!=null) {
            adapter.filter(s);
        }
    }

    @Override
    public void onDestroy() {
        if(adapter!=null)
        adapter.clear();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.setAdapter(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(MyApp.getContext()).registerReceiver(mReceiverForBackPressedAction,new IntentFilter(ActivityMain.NOTIFY_BACK_PRESSED));
        LocalBroadcastManager.getInstance(MyApp.getContext()).registerReceiver(mReceiverForLibraryRefresh
                ,new IntentFilter(Constants.ACTION.REFRESH_LIB));
        Log.d("FragmentFolderLibrary", "onResume: receivers registered" );
    }

    @Override
    public void onPause(){
        super.onPause();
        LocalBroadcastManager.getInstance(MyApp.getContext()).unregisterReceiver(mReceiverForBackPressedAction);
        LocalBroadcastManager.getInstance(MyApp.getContext()).unregisterReceiver(mReceiverForLibraryRefresh);
        Log.d("FragmentFolderLibrary", "onPause: receivers unregistered");
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_library, container, false);
        /*swipeRefreshLayout = layout.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);*/
        mRecyclerView = layout.findViewById(R.id.recyclerviewList);

        //private SwipeRefreshLayout swipeRefreshLayout;
        FastScroller fastScroller = layout.findViewById(R.id.fastscroller);
        fastScroller.setRecyclerView(mRecyclerView);
        /*mRecyclerView.setTrackColor(ColorHelper.getColor(R.color.colorTransparent));
        mRecyclerView.setThumbColor(ColorHelper.getAccentColor());
        mRecyclerView.setPopupBgColor(ColorHelper.getAccentColor());*/
        mRecyclerView.setLayoutManager(new FragmentFolderLibrary.WrapContentLinearLayoutManager(getContext()));
        float offsetPx = getResources().getDimension(R.dimen.bottom_offset_dp);
        BottomOffsetDecoration bottomOffsetDecoration = new BottomOffsetDecoration((int) offsetPx);
        mRecyclerView.addItemDecoration(bottomOffsetDecoration);
        adapter = new FolderLibraryAdapter(getActivity());
        mRecyclerView.setAdapter(adapter);
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onRefresh() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                MusicLibrary.getInstance().RefreshLibrary();
            }
        });
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
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
    }

}