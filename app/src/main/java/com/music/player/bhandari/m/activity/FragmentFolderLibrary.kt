package com.music.player.bhandari.m.activity

import android.content.Context
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import com.music.player.bhandari.m.UIElementHelper.BottomOffsetDecoration
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
class FragmentFolderLibrary constructor() : Fragment(), OnRefreshListener /*ActionMode.Callback*/ {
    private var mRecyclerView: RecyclerView? = null
    private var adapter: FolderLibraryAdapter? = null
    private val mReceiverForLibraryRefresh: BroadcastReceiver
    fun filter(s: String?) {
        if (adapter != null) {
            adapter.filter(s)
        }
    }

    public override fun onDestroy() {
        if (adapter != null) adapter.clear()
        super.onDestroy()
    }

    public override fun onDestroyView() {
        super.onDestroyView()
        mRecyclerView.setAdapter(null)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    public override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(MyApp.Companion.getContext()).registerReceiver(
            mReceiverForBackPressedAction, IntentFilter(ActivityMain.NOTIFY_BACK_PRESSED))
        LocalBroadcastManager.getInstance(MyApp.Companion.getContext()).registerReceiver(
            mReceiverForLibraryRefresh,
            IntentFilter(Constants.ACTION.REFRESH_LIB))
        Log.d("FragmentFolderLibrary", "onResume: receivers registered")
    }

    public override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(MyApp.Companion.getContext()).unregisterReceiver(
            mReceiverForBackPressedAction)
        LocalBroadcastManager.getInstance(MyApp.Companion.getContext())
            .unregisterReceiver(mReceiverForLibraryRefresh)
        Log.d("FragmentFolderLibrary", "onPause: receivers unregistered")
    }

    public override fun onCreateView(
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
        mRecyclerView.setPopupBgColor(ColorHelper.getAccentColor());*/mRecyclerView.setLayoutManager(
            WrapContentLinearLayoutManager(getContext()))
        val offsetPx: Float = getResources().getDimension(R.dimen.bottom_offset_dp)
        val bottomOffsetDecoration: BottomOffsetDecoration =
            BottomOffsetDecoration(offsetPx.toInt())
        mRecyclerView.addItemDecoration(bottomOffsetDecoration)
        adapter = FolderLibraryAdapter(getActivity())
        mRecyclerView.setAdapter(adapter)
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

    public override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
    }

    public override fun onRefresh() {
        Executors.newSingleThreadExecutor().execute(object : Runnable {
            public override fun run() {
                MusicLibrary.getInstance().RefreshLibrary()
            }
        })
    }

    //for catching exception generated by recycler view which was causing abend, no other way to handle this
    internal inner class WrapContentLinearLayoutManager constructor(context: Context?) :
        LinearLayoutManager(context) {
        //... constructor
        public override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
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
        private var mReceiverForBackPressedAction: BroadcastReceiver
    }

    init {
        mReceiverForBackPressedAction = object : BroadcastReceiver() {
            public override fun onReceive(context: Context, intent: Intent) {
                if (adapter != null) {
                    adapter.onStepBack()
                }
            }
        }
        mReceiverForLibraryRefresh = object : BroadcastReceiver() {
            public override fun onReceive(context: Context, intent: Intent) {
                //updateUI();
                adapter = FolderLibraryAdapter(getContext())
                val mHandler: Handler = Handler(getContext()!!.getMainLooper())
                mHandler.post(object : Runnable {
                    public override fun run() {
                        mRecyclerView.setAdapter(adapter)
                        //swipeRefreshLayout.setRefreshing(false);
                    }
                })
            }
        }
    }
}