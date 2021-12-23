package com.music.player.bhandari.m.activity

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.BottomOffsetDecoration
import com.music.player.bhandari.m.adapter.LyricsViewAdapter
import com.music.player.bhandari.m.lyricCard.ActivityLyricCard
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.MusicLibrary
import com.music.player.bhandari.m.model.PlaylistManager
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.ViewLyrics
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadLyricThread
import com.music.player.bhandari.m.service.PlayerService
import com.music.player.bhandari.m.utils.UtilityFun
import com.nshmura.snappysmoothscroller.SnapType
import com.nshmura.snappysmoothscroller.SnappyLayoutManager
import com.nshmura.snappysmoothscroller.SnappyLinearLayoutManager
import com.wang.avi.AVLoadingIndicatorView
import java.util.concurrent.Executors

/**
 * Copyright 2017 Amit Bhandari AB
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class FragmentLyrics : Fragment(), RecyclerView.OnItemTouchListener, Lyrics.Callback,
    ActionMode.Callback, View.OnClickListener {
    private var layout: View? = null
    private var item: TrackItem? = null

    @JvmField @BindView(R.id.loading_lyrics_animation)
    var lyricLoadAnimation: AVLoadingIndicatorView? = null

    /*@JvmField @BindView(R.id.ad_view_wrapper) View adViewWrapper;
    @JvmField @BindView(R.id.adView)  AdView mAdView;
    @JvmField @BindView(R.id.ad_close)  TextView adCloseText;*/
    private var mLyricChange: BroadcastReceiver? = null

    @JvmField @BindView(R.id.text_view_lyric_status)
    var lyricStatus: TextView? = null

    @JvmField @BindView(R.id.update_track_metadata)
    var updateTagsTextView //, lyricCopyRightText;
            : TextView? = null

    @JvmField @BindView(R.id.ll_dynamic_lyric_view)
    var ll_lyric_view: LinearLayout? = null
    private var fIsStaticLyrics: Boolean = true

    @JvmField @BindView(R.id.track_title_lyric_frag)
    var titleEdit: EditText? = null

    @JvmField @BindView(R.id.track_artist_lyric_frag)
    var artistEdit: EditText? = null

    @JvmField @BindView(R.id.button_update_metadata)
    var buttonUpdateMetadata: Button? = null
    private var isLyricsLoaded: Boolean = false
    private var fLyricUpdaterThreadCancelled: Boolean = false
    private var fIsLyricUpdaterThreadRunning: Boolean = false
    private var handler: Handler? = null

    @JvmField @BindView(R.id.dynamic_lyrics_recycler_view)
    var recyclerView: RecyclerView? = null
    private var adapter: LyricsViewAdapter? = null
    private var layoutManager: LinearLayoutManager? = null
    private var gestureDetector: GestureDetectorCompat? = null
    private var actionMode: ActionMode? = null
    private var actionModeActive: Boolean = false
    var playerService: PlayerService? = null
    private var lyricThread: DownloadLyricThread? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.fragment_lyrics, container, false)
        ButterKnife.bind(this, layout!!)
        if (MyApp.getService() == null) {
            UtilityFun.restartApp()
            return layout
        }
        playerService = MyApp.getService()
        initializeListeners()
        return layout
    }

    private fun initializeListeners() {
        buttonUpdateMetadata!!.setOnClickListener(this)
        lyricStatus!!.setOnClickListener(this)
        mLyricChange = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.v(Constants.TAG, "update lyrics please Jarvis")
                updateLyricsIfNeeded()
            }
        }
    }

    private fun updateLyricsIfNeeded() {
        item = playerService!!.getCurrentTrack()
        if (item == null) {
            lyricStatus!!.text = getString(R.string.no_music_found_lyrics)
            lyricStatus!!.visibility = View.VISIBLE
            lyricLoadAnimation!!.hide()
            return
        }
        if (mLyrics != null) {
            //if lyrics are already displayed for current song, skip this
            if (mLyrics!!.getOriginalTrack().equals(
                    item!!.title)
            ) {
                return
            }
        }
        if (isLyricsLoaded) {
            return
        }
        Log.v(Constants.TAG, "Intent Song playing " + playerService!!.getCurrentTrack()!!.title)
        updateLyrics()
    }

    private fun updateLyrics() {
        //hide edit metadata things
        Log.d("FragmentLyrics", "updateLyrics: ")
        if (!isAdded || activity == null) {
            return
        }
        item = playerService!!.getCurrentTrack()
        artistEdit!!.visibility = View.GONE
        titleEdit!!.visibility = View.GONE
        updateTagsTextView!!.visibility = View.GONE
        buttonUpdateMetadata!!.visibility = View.GONE
        buttonUpdateMetadata!!.isClickable = false

        //set loading animation
        lyricLoadAnimation!!.visibility = View.VISIBLE
        lyricLoadAnimation!!.show()

        //lyricCopyRightText.setVisibility(View.GONE);
        ll_lyric_view!!.visibility = View.GONE
        //ll_lyric_view.removeAllViews();
        fLyricUpdaterThreadCancelled = true
        lyricStatus!!.visibility = View.VISIBLE
        lyricStatus!!.text = getString(R.string.lyrics_loading)
        if (!MyApp.getPref().getBoolean(getString(R.string.pref_disclaimer_accepted), false)
        ) {
            lyricStatus!!.visibility = View.VISIBLE
            lyricStatus!!.text = getString(R.string.disclaimer_rejected)
            try {
                //some exceptions reported in play console, thats why
                lyricLoadAnimation!!.hide()
            } catch (ignored: Exception) {
            }
            // }
            return
        }
        if ((mLyrics != null) && (mLyrics!!.getFlag() === Lyrics.POSITIVE_RESULT) && (mLyrics!!.getTrackId() !== -1) && (mLyrics!!.getTrackId() === item!!.id)) {
            onLyricsDownloaded(mLyrics)
            return
        }
        if (item != null) {

            //check in offline storage
            mLyrics = OfflineStorageLyrics.getLyricsFromDB(item)
            if (mLyrics != null) {
                onLyricsDownloaded(mLyrics)
                return
            }
            if (UtilityFun.isConnectedToInternet) {
                fetchLyrics((item!!.getArtist())!!, (item!!.title)!!, null)
            } else {
                lyricStatus!!.text = getString(R.string.no_connection)
                lyricLoadAnimation!!.hide()
            }
        } else {
            lyricStatus!!.text = getString(R.string.no_music_found_lyrics)
            lyricStatus!!.visibility = View.VISIBLE
            lyricLoadAnimation!!.hide()
        }
    }

    private fun fetchLyrics(vararg params: String?) {
        if (activity == null) return
        val artist = params[0]
        val title = params[1]

        ///filter title string
        //title = filterTitleString(title);
        var url: String? = null
        if (params.size > 2) url = params[2]
        val d = Log.d("Fragment lyrics", "fetchLyrics: download lyric thread starting!")
        lyricThread = when (url) {
            null -> DownloadLyricThread(this, true, item, artist!!, title!!)
            else -> DownloadLyricThread(this, true, item, url, artist!!, title!!)
        }
        lyricThread!!.start()
    }

    override fun onLyricsDownloaded(lyrics: Lyrics?) {
        isLyricsLoaded = true
        //control comes here no matter where lyrics found, in db or online
        //so update the view here
        if ((lyrics == null) || (activity == null) || !isAdded) {
            return
        }
        //Log.v("vlah",lyrics.getTrackId() + " " + playerService!!.getCurrentTrack().getId());
        //hide loading animation
        //lyricLoadAnimation.setVisibility(View.INVISIBLE);

        //before lyrics getting displayed, song has been changed already, display loading lyrics and return,
        //background thread already working to fetch latest lyrics
        //track id is -1 if lyrics are downloaded from internet and have
        //id of track from content resolver if lyrics came from offline storage
        if (lyrics.getTrackId() !== -1 && lyrics.getTrackId() !== playerService!!.getCurrentTrack()!!.id) {
            return
        }
        lyricLoadAnimation!!.hide()
        mLyrics = lyrics
        if (layout != null) {
            if (lyrics.getFlag() === Lyrics.POSITIVE_RESULT) {
                //  lrcView.setVisibility(View.VISIBLE);
                //lrcView.setOriginalLyrics(lyrics);
                //lrcView.setSourceLrc(lyrics.getText());
                //((TextView)layout.findViewById(R.id.textView3)).setVisibility(View.GONE);
                //updateLRC();

                //see if timing information available and update view accordingly
                // if(lyrics.isLRC()){
                lyricStatus!!.visibility = View.GONE
                fIsStaticLyrics = !mLyrics!!.isLRC()
                fLyricUpdaterThreadCancelled = false
                ll_lyric_view!!.visibility = View.VISIBLE
                lyricStatus!!.visibility = View.GONE
                //lyricCopyRightText.setVisibility(View.VISIBLE);
                initializeLyricsView()
            } else {
                //in case no lyrics found, set staticLyric flag true as we start lyric thread based on its value
                //and we dont want our thread to run even if no lyrics found
                if (playerService!!.getCurrentTrack() != null) {
                    artistEdit!!.visibility = View.VISIBLE
                    titleEdit!!.visibility = View.VISIBLE
                    updateTagsTextView!!.visibility = View.VISIBLE
                    buttonUpdateMetadata!!.visibility = View.VISIBLE
                    buttonUpdateMetadata!!.isClickable = true
                    titleEdit!!.setText(item!!.title)
                    artistEdit!!.setText(item!!.getArtist())
                }
                fIsStaticLyrics = true
                lyricStatus!!.text = getString(R.string.tap_to_refresh_lyrics)
                lyricStatus!!.visibility = View.VISIBLE
                //lyricCopyRightText.setVisibility(View.GONE);
            }
        }
    }

    private fun initializeLyricsView() {
        if (mLyrics == null) {
            return
        }
        if (handler == null) {
            handler = Handler(Looper.getMainLooper())
        }
        adapter = LyricsViewAdapter(requireContext(), mLyrics)
        val snappyLinearLayoutManager: SnappyLayoutManager = SnappyLinearLayoutManager(context)
        snappyLinearLayoutManager.setSnapType(SnapType.CENTER)
        snappyLinearLayoutManager.setSnapDuration(1500)
        //layoutManager.setSnapInterpolator(new DecelerateInterpolator());

        // Attach layout manager to the RecyclerView:
        recyclerView!!.layoutManager = snappyLinearLayoutManager as RecyclerView.LayoutManager?
        val offsetPx: Float = resources.getDimension(R.dimen.bottom_offset_secondary_lib)
        val bottomOffsetDecoration = BottomOffsetDecoration(offsetPx.toInt())
        recyclerView!!.addItemDecoration(bottomOffsetDecoration)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.adapter = adapter
        recyclerView!!.addOnItemTouchListener(this)
        gestureDetector = GestureDetectorCompat(context, RecyclerViewDemoOnGestureListener())
        layoutManager = recyclerView!!.layoutManager as LinearLayoutManager
        fLyricUpdaterThreadCancelled = false
        if (!fIsStaticLyrics && (playerService!!.getStatus() === playerService!!.PLAYING) && !fIsLyricUpdaterThreadRunning) {
            Executors.newSingleThreadExecutor().execute(lyricUpdater)
            scrollLyricsToCurrentLocation()
        }
    }

    override fun onResume() {
        super.onResume()
        if (MyApp.getService() == null) {
            UtilityFun.restartApp()
            return
        }

        /* This code together with the one in onDestroy()
         * will make the screen be always on until this Activity gets destroyed. */startLyricUpdater()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mLyricChange!!, IntentFilter(Constants.ACTION.UPDATE_LYRIC_AND_INFO))
        /*LocalBroadcastManager.getInstance(getContext()).registerReceiver(mPlayPauseUpdateReceiver
                ,new IntentFilter(Constants.ACTION.PLAY_PAUSE_UI_UPDATE));*/
        //UpdateUI();
        updateLyrics()

        /*if(!fSeekbarRunning && playerService!!.getStatus()==playerService!!.PLAYING) {
            fSeekbarThreadCancelled = false;
            Executors.newSingleThreadExecutor().execute(seekbarUpdater);
        }*/
    }

    private fun startLyricUpdater() {
        if (!fIsStaticLyrics && !fIsLyricUpdaterThreadRunning && (playerService!!.getStatus() === playerService!!.PLAYING)) {
            Log.d("FragmentLyrics", "startLyricUpdater: starting lyric updater")
            fLyricUpdaterThreadCancelled = false
            Executors.newSingleThreadExecutor().execute(lyricUpdater)
        }
        try {
            if (!fIsStaticLyrics) scrollLyricsToCurrentLocation()
        } catch (e: Exception) {
            Log.d("FragmentLyrics", "startLyricUpdater: unable to scroll lyrics to latest position")
        }
    }

    private fun scrollLyricsToCurrentLocation() {
        adapter!!.changeCurrent(playerService!!.getCurrentTrackProgress().toLong())
        val index: Int = adapter!!.getCurrentTimeIndex()
        if (index != -1) {
            // without delay lyrics wont scroll to latest position when called from onResume for some reason
            Handler().postDelayed({ recyclerView!!.smoothScrollToPosition(index) }, 100)
        }
        Log.d("FragmentLyrics", "scrollLyricsToCurrentLocation: index $index")
        adapter!!.notifyDataSetChanged()
    }

    fun smoothScrollAfterSeekbarTouched(progress: Int) {
        if (adapter != null && !fIsStaticLyrics) {
            adapter!!.changeCurrent(UtilityFun.progressToTimer(progress, playerService!!.getCurrentTrackDuration()).toLong())
            val index: Int = adapter!!.getCurrentTimeIndex()
            if (index != -1) {
                recyclerView!!.smoothScrollToPosition(index)
                adapter!!.notifyDataSetChanged()
            }
            Log.d("FragmentLyrics", "scrollLyricsToCurrentLocation: index $index")
        }
    }

    override fun onPause() {
        Log.d("FragmentLyrics", "onPause: stopping lyric updater threads")
        if (actionMode != null) {
            actionMode!!.finish()
            actionMode = null
        }
        stopLyricUpdater()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mLyricChange!!)
        //LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mPlayPauseUpdateReceiver);

        //fSeekbarThreadCancelled = true;
        super.onPause()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (playerService == null) playerService = MyApp.getService()
        if (isVisibleToUser) {
            startLyricUpdater()
        } else {
            stopLyricUpdater()
        }
    }

    private fun stopLyricUpdater() {
        if (fIsLyricUpdaterThreadRunning) {
            fLyricUpdaterThreadCancelled = true
            fIsLyricUpdaterThreadRunning = false
        }
    }

    fun runLyricThread() {
        isLyricsLoaded = false
        if (!fIsStaticLyrics && !fIsLyricUpdaterThreadRunning && (playerService!!.getStatus() === playerService!!.PLAYING)) {
            fLyricUpdaterThreadCancelled = false
            Executors.newSingleThreadExecutor().execute(lyricUpdater)
        } else {
            fLyricUpdaterThreadCancelled = true
        }
    }

    fun clearLyrics() {
        if (playerService == null) return
        if (playerService!!.getCurrentTrack() != null) {
            try {
                ll_lyric_view!!.visibility = View.GONE
                fIsStaticLyrics = true
                lyricStatus!!.text = getString(R.string.tap_to_refresh_lyrics)
                lyricStatus!!.visibility = View.VISIBLE
                buttonUpdateMetadata!!.visibility = View.VISIBLE
                buttonUpdateMetadata!!.isClickable = true
                titleEdit!!.setText(item!!.title)
                artistEdit!!.setText(item!!.getArtist())
                artistEdit!!.visibility = View.VISIBLE
                titleEdit!!.visibility = View.VISIBLE
                updateTagsTextView!!.visibility = View.VISIBLE
            } catch (ignored: Exception) {
            }
        }
    }

    //when clicked on this, lyrics are searched again from viewlyrics
    //but this time option is given to select lyrics
    fun wrongLyrics() {
        if (mLyrics == null || mLyrics!!.getFlag() !== Lyrics.POSITIVE_RESULT) {
            if (isAdded && activity != null) Toast.makeText(activity,
                getString(R.string.error_no_lyrics),
                Toast.LENGTH_SHORT).show()
            return
        }
        if (mLyrics!!.getSource() == null || (!mLyrics!!.getSource().equals(ViewLyrics.clientUserAgent) && !mLyrics!!.getSource().equals("manual"))) {
            if (isAdded && activity != null) Toast.makeText(activity,
                "No lyrics from other sources available!",
                Toast.LENGTH_SHORT).show()
            return
        }
        item = playerService!!.getCurrentTrack()

        ///filter title string
        val title: String? = item!!.title
        val artist: String? = item!!.getArtist()
        Executors.newSingleThreadExecutor().execute {
            if (artist != null && title != null) {
                try {
                    ViewLyrics.fromMetaData(activity,
                        artist,
                        title,
                        item,
                        this@FragmentLyrics)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun shareLyrics() {
        if ((mLyrics == null) || (mLyrics!!.getFlag() !== Lyrics.POSITIVE_RESULT) || (adapter == null)) {
            if (activity != null && isAdded) {
                Toast.makeText(activity,
                    getString(R.string.error_no_lyrics),
                    Toast.LENGTH_SHORT).show()
            }
            return
        }
        var shareBody: String = getString(R.string.lyrics_share_text)
        shareBody += "\n\nTrack : " + mLyrics!!.getTrack()
            .toString() + "\n".toString() + "Artist : " + mLyrics!!.getArtist().toString() + "\n\n"
        if (mLyrics!!.isLRC()) {
            shareBody += Html.fromHtml(adapter!!.getStaticLyrics()).toString()
        } else {
            shareBody += Html.fromHtml(mLyrics!!.getText())
        }
        shareTextIntent(shareBody)
    }

    private fun shareTextIntent(shareBody: String) {
        val sharingIntent: Intent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Lyrics")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        if (isAdded) {
            startActivity(Intent.createChooser(sharingIntent, "Lyrics share!"))
        } else {
            Toast.makeText(activity,
                getString(R.string.error_sharing_lyrics),
                Toast.LENGTH_SHORT).show()
        }
    }

    fun disclaimerAccepted() {
        updateLyrics()
    }

    override fun onDestroy() {
        fLyricUpdaterThreadCancelled = true
        if (lyricThread != null) lyricThread!!.setCallback(null)
        super.onDestroy()
    }

    override fun onDestroyView() {
        fLyricUpdaterThreadCancelled = true
        super.onDestroyView()
    }

    private fun myToggleSelection(idx: Int) {
        adapter!!.toggleSelection(idx)
        if (adapter!!.getSelectedItemCount() == 0) {
            actionMode!!.finish()
            actionMode = null
            return
        }
        val numberOfItems: Int = adapter!!.getSelectedItemCount()
        val selectionString: String = when (numberOfItems) {
            1 -> " item selected"
            else -> " items selected"
        }
        val title: String = numberOfItems.toString() + selectionString
        actionMode!!.title = title
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        gestureDetector!!.onTouchEvent(e)
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        val inflater: MenuInflater = actionMode.menuInflater
        inflater.inflate(R.menu.menu_cab_recyclerview_lyrics, menu)
        return true
    }

    override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_share -> try {
                shareTextIntent(getSelectedLyricString().toString())
                actionMode.finish()
                actionModeActive = false
            } catch (e: IndexOutOfBoundsException) {
                actionMode.finish()
                actionModeActive = false
                Toast.makeText(activity,
                    "Invalid selection, please try again",
                    Toast.LENGTH_SHORT).show()
            }
            R.id.menu_lyric_card -> {
                val intent = Intent(activity, ActivityLyricCard::class.java)
                intent.putExtra("lyric", getSelectedLyricString().toString())
                    .putExtra("artist", mLyrics!!.getArtist())
                    .putExtra("track", mLyrics!!.getTrack())
                startActivity(intent)
            }
        }
        return false
    }

    private fun getSelectedLyricString(): StringBuilder {
        val shareString: StringBuilder = StringBuilder()
        val selectedItemPositions: List<Int> = adapter!!.getSelectedItems()
        var currPos: Int
        for (i in selectedItemPositions.indices) {
            currPos = selectedItemPositions.get(i)
            val lyricLine = adapter!!.getLineAtPosition(currPos)
            shareString.append(lyricLine).append("\n")
        }
        return shareString
    }

    override fun onDestroyActionMode(actionMode: ActionMode) {
        actionMode.finish()
        actionModeActive = false
        adapter!!.clearSelections()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.lyrics_line -> if (recyclerView != null) {
                val idx: Int = recyclerView!!.getChildLayoutPosition(view)
                if (actionModeActive) {
                    myToggleSelection(idx)
                    return
                }
            }
            R.id.text_view_lyric_status -> {
                lyricStatus!!.text = getString(R.string.lyrics_loading)
                updateLyrics()
            }
            R.id.button_update_metadata -> {
                item = playerService!!.getCurrentTrack()
                if (item == null) {
                    return
                }
                val edited_title = titleEdit!!.text.toString()
                val edited_artist = artistEdit!!.text.toString()
                if (edited_title.isEmpty() || edited_artist.isEmpty()) {
                    Toast.makeText(context,
                        getString(R.string.te_error_empty_field),
                        Toast.LENGTH_SHORT).show()
                    return
                }
                if (edited_title != item!!.title ||
                    edited_artist != item!!.getArtist()
                ) {

                    //changes made, save those
                    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    val values = ContentValues()
                    values.put(MediaStore.Audio.Media.TITLE, edited_title)
                    values.put(MediaStore.Audio.Media.ARTIST, edited_artist)
                    requireContext().contentResolver
                        .update(uri,
                            values,
                            MediaStore.Audio.Media.TITLE + "=?",
                            arrayOf(item!!.title))
                    val d = MusicLibrary.instance!!.updateTrackNew(item!!.id, edited_title, edited_artist, item!!.album!!)
                    PlaylistManager.getInstance(MyApp.getContext())!!.addEntryToMusicTable(d!!)
                    val intent = Intent(context, ActivityNowPlaying::class.java)
                    intent.putExtra("refresh", true)
                    intent.putExtra("position", playerService!!.getCurrentTrackPosition())
                    intent.putExtra("originalTitle", item!!.title)
                    intent.putExtra("title", edited_title)
                    intent.putExtra("artist", edited_artist)
                    intent.putExtra("album", item!!.album)
                    startActivity(intent)
                    artistEdit!!.visibility = View.GONE
                    titleEdit!!.visibility = View.GONE
                    updateTagsTextView!!.visibility = View.GONE
                    buttonUpdateMetadata!!.visibility = View.GONE
                    buttonUpdateMetadata!!.isClickable = false
                    if (activity != null) {
                        if (requireActivity().currentFocus != null) {
                            (requireActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(
                                view.windowToken,
                                0)
                        }
                    }
                } else {
                    Toast.makeText(context,
                        getString(R.string.change_tags_to_update),
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private inner class RecyclerViewDemoOnGestureListener :
        GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val view: View? = recyclerView!!.findChildViewUnder(e.x, e.y)
            if (view != null) {
                onClick(view)
            }
            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent) {
            if (!isAdded || activity == null) return
            val view = recyclerView!!.findChildViewUnder(e.x, e.y)
            if (actionModeActive) {
                return
            }
            // Start the CAB using the ActionMode.Callback defined above
            actionMode = activity!!.startActionMode(this@FragmentLyrics)
            actionModeActive = true
            val idx: Int = recyclerView!!.getChildPosition(view!!)
            myToggleSelection(idx)
            super.onLongPress(e)
        }
    }

    private val lyricUpdater: Runnable = Runnable {
        while (true) {
            if (fLyricUpdaterThreadCancelled) {
                break
            }
            fIsLyricUpdaterThreadRunning = true
            //Log.v("FragmentLyrics","Lyric thread running");
            if (activity != null) {
                handler!!.post {
                    val index: Int = adapter!!.changeCurrent(playerService!!.getCurrentTrackProgress().toLong())
                    val firstVisibleItem: Int = layoutManager!!.findFirstVisibleItemPosition()
                    val lastVisibleItem: Int = layoutManager!!.findLastVisibleItemPosition()
                    if ((index != -1) && (index > firstVisibleItem) && (index < lastVisibleItem)) {
                        recyclerView!!.smoothScrollToPosition(index)
                    }
                }
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        fIsLyricUpdaterThreadRunning = false
        Log.v("FragmentLyrics", "Lyric thread stopped")
    }

    companion object {
        private var mLyrics: Lyrics? = null
    }
}