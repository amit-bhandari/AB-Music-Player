package com.music.player.bhandari.m.activity

/**
 * Created by Amit AB on 16/1/17.
 */
import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import com.music.player.bhandari.m.UIElementHelper.FastScroller
import com.music.player.bhandari.m.model.Constants
import java.util.concurrent.Executors

/**
 * Copyright 2017 Amit Bhandari AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class FragmentAlbumLibrary constructor() : Fragment(), OnRefreshListener {
    private var mRecyclerView: RecyclerView? = null
    private var fastScroller: FastScroller? = null
    private var albumLibraryAdapter: AlbumLibraryAdapter? = null

    //private SwipeRefreshLayout swipeRefreshLayout;
    private var mRefreshLibraryReceiver: BroadcastReceiver? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRefreshLibraryReceiver = object : BroadcastReceiver() {
            public override fun onReceive(context: Context, intent: Intent) {
                albumLibraryAdapter = AlbumLibraryAdapter(getContext(),
                    MusicLibrary.getInstance().getDataItemsForAlbums())
                mRecyclerView.setAdapter(albumLibraryAdapter)
                //swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    public override fun onDestroy() {
        mRecyclerView = null
        super.onDestroy()
    }

    fun filter(s: String?) {
        if (albumLibraryAdapter != null) {
            albumLibraryAdapter.filter(s)
        }
    }

    fun sort(sort_id: Int) {
        if (albumLibraryAdapter != null) {
            albumLibraryAdapter.sort(sort_id)
        }
    }

    public override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(getContext())
            .registerReceiver(mRefreshLibraryReceiver, IntentFilter(Constants.ACTION.REFRESH_LIB))
    }

    public override fun onStop() {
        super.onStop()
    }

    public override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mRefreshLibraryReceiver)
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout: View = inflater.inflate(R.layout.fragment_library, container, false)
        //swipeRefreshLayout = layout.findViewById(R.id.swipeRefreshLayout);
        //swipeRefreshLayout.setOnRefreshListener(this);
        mRecyclerView = layout.findViewById(R.id.recyclerviewList)
        fastScroller = layout.findViewById(R.id.fastscroller)
        fastScroller.setRecyclerView(mRecyclerView)

        /*mRecyclerView.setTrackColor(ColorHelper.getColor(R.color.colorTransparent));
        mRecyclerView.setThumbColor(ColorHelper.getAccentColor());
        mRecyclerView.setPopupBgColor(ColorHelper.getAccentColor());*/albumLibraryAdapter =
            AlbumLibraryAdapter(getContext(), MusicLibrary.getInstance().getDataItemsForAlbums())
        albumLibraryAdapter.sort(MyApp.Companion.getPref()
            .getInt(getString(R.string.pref_album_sort_by), Constants.SORT_BY.NAME))
        mRecyclerView.setAdapter(albumLibraryAdapter)
        val mLayoutManager: LayoutManager = GridLayoutManager(getContext(), 3)
        mRecyclerView.setLayoutManager(mLayoutManager)
        mRecyclerView.setItemAnimator(DefaultItemAnimator())
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            public override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    (getActivity() as ActivityMain?)!!.hideFab(true)
                } else (getActivity() as ActivityMain?)!!.hideFab(false)
            }

            public override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    (getActivity() as ActivityMain?)!!.hideFab(false)
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        return layout
    }

    public override fun onRefresh() {
        Executors.newSingleThreadExecutor().execute(object : Runnable {
            public override fun run() {
                MusicLibrary.getInstance().RefreshLibrary()
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        })
    }

    public override fun onDestroyView() {
        super.onDestroyView()
        mRecyclerView.setAdapter(null)
    }
}