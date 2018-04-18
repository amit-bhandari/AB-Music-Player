package com.music.player.bhandari.m.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.BottomOffsetDecoration;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.adapter.FolderLibraryAdapter;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.concurrent.Executors;

/**
 * Created by Amit AB on 7/12/16.
 */

public class FragmentFolderLibrary extends Fragment implements SwipeRefreshLayout.OnRefreshListener
        /*ActionMode.Callback*/{

    private  FastScrollRecyclerView mRecyclerView;
    private  FolderLibraryAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

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
                        swipeRefreshLayout.setRefreshing(false);
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
        swipeRefreshLayout = layout.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        mRecyclerView = layout.findViewById(R.id.recyclerviewList);
        mRecyclerView.setTrackColor(ColorHelper.getColor(R.color.colorTransparent));
        mRecyclerView.setThumbColor(ColorHelper.getAccentColor());
        mRecyclerView.setPopupBgColor(ColorHelper.getAccentColor());
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

    /*@Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        if(actionMode!=null) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.menu_cab_recyclerview_lyrics, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

    }*/

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