package com.music.player.bhandari.m.activity

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.service.PlayerService

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
class FragmentAlbumArt constructor() : Fragment() {
    private var playerService: PlayerService? = null
    private var mUIUpdate: BroadcastReceiver? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.album_art_now_playing)
    var albumArt: ImageView? = null
    public override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout: View = inflater.inflate(R.layout.fragment_album_art, container, false)

        /*Configuration configuration = getActivity().getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.

        Log.d("Fragment Disc", "onCreateView: " + screenWidthDp);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(screenWidthDp -50, screenWidthDp -50);*/ButterKnife.bind(
            this,
            layout)
        playerService = MyApp.Companion.getService()
        mUIUpdate = object : BroadcastReceiver() {
            public override fun onReceive(context: Context, intent: Intent) {
                Log.v(Constants.TAG, "update disc please Jarvis")
                UpdateUI()
            }
        }
        return layout
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isAdded() && getActivity() != null) {
            //exit animation
            getActivity()!!.startPostponedEnterTransition()

            //place album art view properly in center
            albumArt.getViewTreeObserver().addOnGlobalLayoutListener(
                object : OnGlobalLayoutListener {
                    public override fun onGlobalLayout() {
                        albumArt.getViewTreeObserver().removeOnGlobalLayoutListener(this)

                        //y position of control buttons
                        val yControl: Float = (getActivity() as ActivityNowPlaying?)!!.yControl

                        //height of toolbar
                        val toolbarHeight: Float =
                            (getActivity() as ActivityNowPlaying?)!!.toolbarHeight
                        if (toolbarHeight != 0f || yControl != 0f) {
                            //centre the album art
                            albumArt.setY(((yControl - toolbarHeight) / 2) - albumArt.getHeight() / 2)
                        }
                    }
                })
        }
    }

    public override fun onDestroyView() {
        super.onDestroyView()
    }

    public override fun onPause() {
        Log.v(Constants.TAG, "Disc paused........")
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUIUpdate)
        }
        super.onPause()
    }

    public override fun onResume() {
        Log.v(Constants.TAG, "Disc resumed........")
        if (getContext() != null) {
            LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mUIUpdate, IntentFilter(Constants.ACTION.COMPLETE_UI_UPDATE))
        }
        UpdateUI()
        super.onResume()
    }

    private fun UpdateUI() {
        if ((getActivity() == null) || !isAdded() || (playerService!!.getCurrentTrack() == null)) {
            return
        }
        val currentNowPlayingBackPref: Int =
            MyApp.Companion.getPref().getInt(getString(R.string.pref_now_playing_back), 1)
        //if album art selected, hide small album art
        if (currentNowPlayingBackPref == 2) {
            albumArt.setImageBitmap(null)
        } else {
            val request: RequestBuilder<Drawable> = Glide.with(this)
                .load(MusicLibrary.getInstance()
                    .getAlbumArtFromTrack(playerService!!.getCurrentTrack().getId()))
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
            val defaultAlbumArtSetting: Int =
                MyApp.Companion.getPref().getInt(getString(R.string.pref_default_album_art), 0)
            when (defaultAlbumArtSetting) {
                0 -> request.listener(object : RequestListener<Drawable?> {
                    public override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        //Log.d("AlbumLibraryAdapter", "onException: ");
                        if (UtilityFun.isConnectedToInternet &&
                            !MyApp.Companion.getPref()
                                .getBoolean(getString(R.string.pref_data_saver), false)
                        ) {
                            val url: String? = MusicLibrary.getInstance().getArtistUrls().get(
                                playerService!!.getCurrentTrack().getArtist())
                            if (url != null && !url.isEmpty()) request.load(Uri.parse(url))
                                .into(albumArt)
                            return true
                        }
                        return false
                    }

                    public override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                }).placeholder(R.drawable.ic_batman_1)
                1 -> request.listener(object : RequestListener<Drawable?> {
                    public override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (UtilityFun.isConnectedToInternet &&
                            !MyApp.Companion.getPref()
                                .getBoolean(getString(R.string.pref_data_saver), false)
                        ) {
                            val url: String? = MusicLibrary.getInstance().getArtistUrls().get(
                                playerService!!.getCurrentTrack().getArtist())
                            if (url != null && !url.isEmpty()) request.load(Uri.parse(url))
                                .into(albumArt)
                            return true
                        }
                        return false
                    }

                    public override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                }).placeholder(UtilityFun.defaultAlbumArtDrawable)
            }
            request.into(albumArt)
        }
    }
}