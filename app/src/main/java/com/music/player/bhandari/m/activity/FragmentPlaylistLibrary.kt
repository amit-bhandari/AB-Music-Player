package com.music.player.bhandari.m.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.BottomOffsetDecoration
import com.music.player.bhandari.m.adapter.PlaylistLibraryAdapter

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
class FragmentPlaylistLibrary: Fragment() {
    private var playlistAdapter: PlaylistLibraryAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layout: View = inflater.inflate(R.layout.fragment_playlist_library, container, false)
        val mRecyclerViewPlaylist = layout.findViewById<RecyclerView>(R.id.recyclerViewSystemPlaylist)
        playlistAdapter = PlaylistLibraryAdapter(requireContext())
        mRecyclerViewPlaylist.setAdapter(playlistAdapter!!)
        mRecyclerViewPlaylist.layoutManager = WrapContentLinearLayoutManager(context)
        val offsetPx: Float = resources.getDimension(R.dimen.bottom_offset_dp)
        val bottomOffsetDecoration = BottomOffsetDecoration(offsetPx.toInt())
        mRecyclerViewPlaylist.addItemDecoration(bottomOffsetDecoration)
        return layout
    }

    fun refreshPlaylistList() {
        playlistAdapter?.refreshPlaylistList()
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
}