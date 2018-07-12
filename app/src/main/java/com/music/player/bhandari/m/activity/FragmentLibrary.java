package com.music.player.bhandari.m.activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.BottomOffsetDecoration;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.FastScroller;
import com.music.player.bhandari.m.adapter.MainLibraryAdapter;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

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
        SwipeRefreshLayout.OnRefreshListener /*, RecyclerView.OnItemTouchListener
        , ActionMode.Callback,  View.OnClickListener */{
    private int status;
    private MainLibraryAdapter cursoradapter;
    private RecyclerView mRecyclerView;
    private FastScroller fastScroller;
    //private SwipeRefreshLayout swipeRefreshLayout;
    private BroadcastReceiver mRefreshLibraryReceiver;

    /*private GestureDetectorCompat gestureDetector;
    private ActionMode actionMode;
    private boolean actionModeActive = false;*/

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
                Log.v(Constants.TAG,"Items found tracks = "+MusicLibrary.getInstance().getDataItemsForTracks().size());
                Log.v(Constants.TAG,"Items found art= "+MusicLibrary.getInstance().getDataItemsArtist().size());
                Log.v(Constants.TAG,"Items found alb= "+MusicLibrary.getInstance().getDataItemsForAlbums().size());
                Log.v(Constants.TAG,"Items found genr= "+MusicLibrary.getInstance().getDataItemsForGenres().size());
                switch (status){
                    case Constants.FRAGMENT_STATUS.TITLE_FRAGMENT:
                        cursoradapter=new MainLibraryAdapter(FragmentLibrary.this, getContext()
                                ,MusicLibrary.getInstance().getDataItemsForTracks());
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
                //swipeRefreshLayout.setRefreshing(false);
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
        /*
        if(actionMode!=null){
            actionMode.finish();
            actionMode = null;
        }*/
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
        //swipeRefreshLayout = layout.findViewById(R.id.swipeRefreshLayout);
        //swipeRefreshLayout.setOnRefreshListener(this);
        mRecyclerView = layout.findViewById(R.id.recyclerviewList);
            /*mRecyclerView.setTrackColor(ColorHelper.getColor(R.color.colorTransparent));
            mRecyclerView.setThumbColor(ColorHelper.getAccentColor());
            mRecyclerView.setPopupBgColor(ColorHelper.getAccentColor());*/

        fastScroller = layout.findViewById(R.id.fastscroller);
        fastScroller.setRecyclerView(mRecyclerView);

        initializeAdapter(status);
        Log.v(Constants.TAG,"STARTED");

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

        /*mRecyclerView.addOnItemTouchListener(this);
        gestureDetector =
                new GestureDetectorCompat(getContext(), new FragmentLibrary.RecyclerViewDemoOnGestureListener());

        ProgressBar progressBar = layout.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionMode = ((ActivityMain)getActivity()).startSupportActionMode(FragmentLibrary.this);
            }
        });*/
        return layout;
    }

    public void initializeAdapter(int status){
        switch (status){
            case Constants.FRAGMENT_STATUS.TITLE_FRAGMENT:
                cursoradapter=new MainLibraryAdapter(FragmentLibrary.this, getContext()
                        ,MusicLibrary.getInstance().getDataItemsForTracks());
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

        Log.v(Constants.TAG,"item count "+cursoradapter.getItemCount());
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



/*
    //action mode things
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        /f(actionMode!=null) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.menu_cab_recyclerview_lyrics, menu);
            return true;
        }
        return false;
    }

    /*
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_share:
                StringBuilder shareString = new StringBuilder();
                List<Integer> selectedItemPositions = cursoradapter.getSelectedItems();
                int currPos;
                for (int i = 0 ; i <=  selectedItemPositions.size() - 1; i++) {
                    currPos = selectedItemPositions.get(i);
                    //String lyricLine = cursoradapter.getLineAtPosition(currPos);
                    //if(lyricLine!=null) {
                    //    shareString.append(lyricLine).append("\n\n");
                    //}
                }
                //shareTextIntent(shareString.toString());
                actionMode.finish();
                actionModeActive = false;
                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode.finish();
        actionModeActive = false;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    private void myToggleSelection(int idx) {
        cursoradapter.toggleSelection(idx);
        //String title = adapter.getSelectedItemCount();
        //actionMode.setTitle(title);
        if(cursoradapter.getSelectedItemCount()==0){
            actionMode.finish();
            actionMode = null;
            return;
        }
        int numberOfItems = cursoradapter.getSelectedItemCount();
        String selectionString = numberOfItems==1 ? " item selected" : " items selected";
        String title = numberOfItems  + selectionString;
        actionMode.setTitle(title);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lyrics_line:
                if (mRecyclerView != null) {
                    int idx = mRecyclerView.getChildLayoutPosition(view);
                    if (actionModeActive) {
                        myToggleSelection(idx);
                        return;
                    }
                }
                break;
        }
    }
*/
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

/*
    private class RecyclerViewDemoOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if(view!=null) {
                onClick(view);
            }
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (actionModeActive) {
                return;
            }
            // Start the CAB using the ActionMode.Callback defined above
            actionMode = ((ActivityMain)getActivity()).startSupportActionMode(FragmentLibrary.this);
            actionModeActive = true;
            int idx = mRecyclerView.getChildPosition(view);
            myToggleSelection(idx);
            super.onLongPress(e);
        }
    }
*/
}

