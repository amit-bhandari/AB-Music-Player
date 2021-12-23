package com.music.player.bhandari.m.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
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
import com.music.player.bhandari.m.adapter.FolderLibraryAdapter
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.MusicLibrary
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
class FragmentFolderLibrary : Fragment(), SwipeRefreshLayout.OnRefreshListener /*ActionMode.Callback*/ {
    private var mRecyclerView: RecyclerView? = null
    private var adapter: FolderLibraryAdapter? = null
    private val mReceiverForLibraryRefresh: BroadcastReceiver
    fun filter(s: String?) {
        if (adapter != null) {
            adapter!!.filter(s!!)
        }
    }

    override fun onDestroy() {
        if (adapter != null) adapter!!.clear()
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mRecyclerView!!.adapter = null
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(MyApp.getContext()).registerReceiver(
            mReceiverForBackPressedAction!!, IntentFilter(ActivityMain.NOTIFY_BACK_PRESSED))
        LocalBroadcastManager.getInstance(MyApp.getContext()).registerReceiver(
            mReceiverForLibraryRefresh,
            IntentFilter(Constants.ACTION.REFRESH_LIB))
        Log.d("FragmentFolderLibrary", "onResume: receivers registered")
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(MyApp.getContext()).unregisterReceiver(mReceiverForBackPressedAction!!)
        LocalBroadcastManager.getInstance(MyApp.getContext()).unregisterReceiver(mReceiverForLibraryRefresh)
        Log.d("FragmentFolderLibrary", "onPause: receivers unregistered")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout: View = inflater.inflate(R.layout.fragment_library, container, false)
        /*swipeRefreshLayout = layout.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);*/mRecyclerView =
            layout.findViewById(R.id.recyclerviewList)

        //private SwipeRefreshLayout swipeRefreshLayout;
        val fastScroller: FastScroller = layout.findViewById(R.id.fastscroller)
        fastScroller.setRecyclerView(mRecyclerView)
        /*mRecyclerView.setTrackColor(ColorHelper.getColor(R.color.colorTransparent));
        mRecyclerView.setThumbColor(ColorHelper.getAccentColor());
        mRecyclerView.setPopupBgColor(ColorHelper.getAccentColor());*/
        mRecyclerView!!.layoutManager = WrapContentLinearLayoutManager(context)
        val offsetPx: Float = resources.getDimension(R.dimen.bottom_offset_dp)
        val bottomOffsetDecoration: BottomOffsetDecoration =
            BottomOffsetDecoration(offsetPx.toInt())
        mRecyclerView!!.addItemDecoration(bottomOffsetDecoration)
        adapter = FolderLibraryAdapter(requireActivity())
        mRecyclerView!!.adapter = adapter!!
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

    override fun onRefresh() {
        Executors.newSingleThreadExecutor().execute { MusicLibrary.instance.RefreshLibrary() }
    }

    //for catching exception generated by recycler view which was causing abend, no other way to handle this
    internal inner class WrapContentLinearLayoutManager constructor(context: Context?) :
        LinearLayoutManager(context) {
        //... constructor
        override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
            try {
                super.onLayoutChildren(recycler, state)
            } catch (ignored: IndexOutOfBoundsException) {
            }
        }
    }

    companion object {
        /*overwrite back button for this fragment as we will be using same recycler view for
        walking into directory
     */
        private var mReceiverForBackPressedAction: BroadcastReceiver? = null
    }

    init {
        mReceiverForBackPressedAction = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (adapter != null) {
                    adapter!!.onStepBack()
                }
            }
        }
        mReceiverForLibraryRefresh = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                //updateUI();
                adapter = FolderLibraryAdapter(requireContext())
                val mHandler = Handler(requireContext().mainLooper)
                mHandler.post {
                    mRecyclerView!!.adapter = adapter!!
                    //swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
    }
}