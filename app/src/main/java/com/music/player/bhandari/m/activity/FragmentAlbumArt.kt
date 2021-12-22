package com.music.player.bhandari.m.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.MusicLibrary
import com.music.player.bhandari.m.service.PlayerService
import com.music.player.bhandari.m.utils.UtilityFun

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
class FragmentAlbumArt : Fragment() {
    private var playerService: PlayerService? = null
    private var mUIUpdate: BroadcastReceiver? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.album_art_now_playing)
    var albumArt: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout: View = inflater.inflate(R.layout.fragment_album_art, container, false)

        /*Configuration configuration = getActivity().getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.

        Log.d("Fragment Disc", "onCreateView: " + screenWidthDp);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(screenWidthDp -50, screenWidthDp -50);*/
        ButterKnife.bind(
            this,
            layout)
        playerService = MyApp.getService()
        mUIUpdate = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.v(Constants.TAG, "update disc please Jarvis")
                UpdateUI()
            }
        }
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isAdded && activity != null) {
            //exit animation
            requireActivity().startPostponedEnterTransition()

            //place album art view properly in center
            albumArt!!.getViewTreeObserver().addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        albumArt!!.viewTreeObserver.removeOnGlobalLayoutListener(this)

                        //y position of control buttons
                        val yControl: Float = (activity as ActivityNowPlaying?)!!.yControl

                        //height of toolbar
                        val toolbarHeight: Float =
                            (activity as ActivityNowPlaying?)!!.toolbarHeight
                        if (toolbarHeight != 0f || yControl != 0f) {
                            //centre the album art
                            albumArt!!.y = ((yControl - toolbarHeight) / 2) - albumArt!!.height / 2
                        }
                    }
                })
        }
    }

    override fun onPause() {
        Log.v(Constants.TAG, "Disc paused........")
        if (context != null) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mUIUpdate!!)
        }
        super.onPause()
    }

    override fun onResume() {
        Log.v(Constants.TAG, "Disc resumed........")
        if (context != null) {
            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(mUIUpdate!!, IntentFilter(Constants.ACTION.COMPLETE_UI_UPDATE))
        }
        UpdateUI()
        super.onResume()
    }

    @SuppressLint("CheckResult")
    private fun UpdateUI() {
        if ((activity == null) || !isAdded || (playerService!!.getCurrentTrack() == null)) {
            return
        }
        val currentNowPlayingBackPref: Int = MyApp.getPref()!!.getInt(getString(R.string.pref_now_playing_back), 1)
        //if album art selected, hide small album art
        if (currentNowPlayingBackPref == 2) {
            albumArt!!.setImageBitmap(null)
        } else {
            val request: RequestBuilder<Drawable> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Glide.with(this)
                    .load(MusicLibrary.instance!!.getAlbumArtFromTrack(playerService!!.getCurrentTrack()!!.id))
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
            } else {
                TODO("VERSION.SDK_INT < Q")
            }
            when (MyApp.getPref()!!.getInt(getString(R.string.pref_default_album_art), 0)) {
                0 -> request.listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        //Log.d("AlbumLibraryAdapter", "onException: ");
                        if (UtilityFun.isConnectedToInternet && !MyApp.getPref()!!.getBoolean(getString(R.string.pref_data_saver), false)) {
                            val url: String? = MusicLibrary.instance!!.artistUrls[playerService!!.getCurrentTrack()!!.getArtist()]
                            if (url != null && url.isNotEmpty()) request.load(Uri.parse(url))
                                .into(albumArt!!)
                            return true
                        }
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                }).placeholder(R.drawable.ic_batman_1)
                1 -> request.listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (UtilityFun.isConnectedToInternet &&
                            !MyApp.getPref()!!.getBoolean(getString(R.string.pref_data_saver), false)
                        ) {
                            val url: String? = MusicLibrary.instance!!.artistUrls[playerService!!.getCurrentTrack()!!.getArtist()]
                            if (url != null && url.isNotEmpty()) request.load(Uri.parse(url))
                                .into(albumArt!!)
                            return true
                        }
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                }).placeholder(UtilityFun.defaultAlbumArtDrawable)
            }
            request.into(albumArt!!)
        }
    }
}