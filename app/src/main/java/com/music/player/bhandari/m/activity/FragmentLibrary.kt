package com.music.player.bhandari.m.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.BottomOffsetDecoration
import com.music.player.bhandari.m.UIElementHelper.FastScroller
import com.music.player.bhandari.m.adapter.MainLibraryAdapter
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.MusicLibrary

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
class FragmentLibrary : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private var status: Int = 0
    private var cursoradapter: MainLibraryAdapter? = null
    private var mRecyclerView: RecyclerView? = null
    private var mRefreshLibraryReceiver: BroadcastReceiver? = null
    fun getStatus(): Int {
        return status
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            status = requireArguments().getInt("status")
        }
        mRefreshLibraryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.v("FragmentLibrary", "Items found tracks = " + MusicLibrary.instance!!.getDataItemsForTracks()!!.size)
                Log.v("FragmentLibrary", "Items found art= " + MusicLibrary.instance!!.dataItemsArtist.size)
                Log.v("FragmentLibrary", "Items found alb= " + MusicLibrary.instance!!.getDataItemsForAlbums().size)
                Log.v("FragmentLibrary", "Items found genr= " + MusicLibrary.instance!!.getDataItemsForGenres().size)
                when (status) {
                    Constants.FRAGMENT_STATUS.TITLE_FRAGMENT -> {
                        cursoradapter = MainLibraryAdapter(this@FragmentLibrary, requireContext(), ArrayList(MusicLibrary.instance!!.getDataItemsForTracks()!!.values))
                        cursoradapter!!.sort(MyApp.getPref()!!.getInt(getString(R.string.pref_tracks_sort_by), Constants.SORT_BY.NAME))
                    }
                    Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT -> {
                        cursoradapter = MainLibraryAdapter(this@FragmentLibrary, requireContext(), MusicLibrary.instance!!.dataItemsArtist)
                        cursoradapter!!.sort(MyApp.getPref()!!.getInt(getString(R.string.pref_artist_sort_by), Constants.SORT_BY.NAME))
                    }
                    Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT -> {
                        cursoradapter = MainLibraryAdapter(this@FragmentLibrary,
                            requireContext(),
                            MusicLibrary.instance!!.getDataItemsForAlbums())
                        cursoradapter!!.sort(MyApp.getPref()!!.getInt(getString(R.string.pref_album_sort_by), Constants.SORT_BY.NAME))
                    }
                    Constants.FRAGMENT_STATUS.GENRE_FRAGMENT -> {
                        cursoradapter = MainLibraryAdapter(this@FragmentLibrary,
                            requireContext(),
                            MusicLibrary.instance!!.getDataItemsForGenres())
                        cursoradapter!!.sort(MyApp.getPref()!!.getInt(getString(R.string.pref_genre_sort_by), Constants.SORT_BY.NAME))
                    }
                }
                notifyDataSetChanges()
                mRecyclerView!!.adapter = cursoradapter
            }
        }
    }

    override fun onDestroy() {
        cursoradapter?.clear()
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mRecyclerView!!.adapter = null
    }

    fun filter(s: String?) {
        if (cursoradapter != null) {
            cursoradapter!!.filter(s!!)
        }
    }

    fun notifyDataSetChanges() {
        if (cursoradapter != null) {
            cursoradapter!!.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mRefreshLibraryReceiver!!, IntentFilter(Constants.ACTION.REFRESH_LIB))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mRefreshLibraryReceiver!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layout: View = inflater.inflate(R.layout.fragment_library, container, false)
        mRecyclerView = layout.findViewById(R.id.recyclerviewList)
        val fastScroller: FastScroller = layout.findViewById(R.id.fastscroller)
        fastScroller.setRecyclerView(mRecyclerView)
        initializeAdapter(status)
        val offsetPx: Float = resources.getDimension(R.dimen.bottom_offset_dp)
        val bottomOffsetDecoration = BottomOffsetDecoration(offsetPx.toInt())
        mRecyclerView!!.addItemDecoration(bottomOffsetDecoration)
        mRecyclerView!!.layoutManager = WrapContentLinearLayoutManager(activity)
        mRecyclerView!!.setHasFixedSize(true)
        mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    (activity as ActivityMain?)!!.hideFab(true)
                } else (activity as ActivityMain?)!!.hideFab(false)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    (activity as ActivityMain?)!!.hideFab(false)
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        return layout
    }

    fun initializeAdapter(status: Int) {
        Log.d("FragmentLibrary", "initializeAdapter: ")
        when (status) {
            Constants.FRAGMENT_STATUS.TITLE_FRAGMENT -> {
                cursoradapter = MainLibraryAdapter(this@FragmentLibrary,
                    requireContext(),
                    ArrayList(MusicLibrary.instance!!.getDataItemsForTracks()!!.values))
                cursoradapter!!.sort(MyApp.getPref()!!.getInt(getString(R.string.pref_tracks_sort_by), Constants.SORT_BY.NAME))
            }
            Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT -> {
                cursoradapter = MainLibraryAdapter(this@FragmentLibrary,
                    requireContext(),
                    MusicLibrary.instance!!.dataItemsArtist)
                cursoradapter!!.sort(MyApp.getPref()!!.getInt(getString(R.string.pref_artist_sort_by), Constants.SORT_BY.NAME))
            }
            Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT -> {
                cursoradapter = MainLibraryAdapter(this@FragmentLibrary,
                    requireContext(),
                    MusicLibrary.instance!!.getDataItemsForAlbums())
                cursoradapter!!.sort(MyApp.getPref()!!.getInt(getString(R.string.pref_album_sort_by), Constants.SORT_BY.NAME))
            }
            Constants.FRAGMENT_STATUS.GENRE_FRAGMENT -> {
                cursoradapter = MainLibraryAdapter(this@FragmentLibrary,
                    requireContext(),
                    MusicLibrary.instance!!.getDataItemsForGenres())
                cursoradapter!!.sort(MyApp.getPref()!!.getInt(getString(R.string.pref_genre_sort_by), Constants.SORT_BY.NAME))
            }
        }
        Log.v("FragmentLibrary", "item count " + cursoradapter!!.getItemCount())
        //cursoradapter.setHasStableIds(true);
        mRecyclerView!!.adapter = cursoradapter
    }

    fun sort(sort_id: Int) {
        if (cursoradapter != null) {
            cursoradapter!!.sort(sort_id)
        }
    }

    fun updateItem(position: Int, vararg param: String) {
        if (cursoradapter != null) {
            cursoradapter!!.updateItem(position, *param)
        }
    }

    override fun onRefresh() {
        MusicLibrary.instance!!.RefreshLibrary()
    }

    //for catching exception generated by recycler view which was causing abend, no other way to handle this
    internal inner class WrapContentLinearLayoutManager constructor(context: Context?) :
        LinearLayoutManager(context) {
        //... constructor
        override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
            try {
                super.onLayoutChildren(recycler, state)
            } catch (e: IndexOutOfBoundsException) {
            }
        }
    }
}