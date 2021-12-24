package com.music.player.bhandari.m.activity

import android.annotation.SuppressLint
import android.content.*
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.*
import android.text.Html
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GestureDetectorCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.uiElementHelper.ColorHelper
import com.music.player.bhandari.m.uiElementHelper.ColorHelper.getBaseThemeDrawable
import com.music.player.bhandari.m.uiElementHelper.ColorHelper.setStatusBarGradiant
import com.music.player.bhandari.m.adapter.LyricsViewAdapter
import com.music.player.bhandari.m.lyricCard.ActivityLyricCard
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.ViewLyrics
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.ViewLyrics.fromMetaData
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio.getArtistInfoFromCache
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio.putArtistInfoToCache
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics.clearLyricsFromDB
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics.getInstantLyricsFromDB
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics.getLyricsFromCache
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics.isLyricsPresentInDB
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics.putInstantLyricsInDB
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics.putLyricsToCache
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadArtInfoThread
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadLyricThread
import com.music.player.bhandari.m.utils.AppLaunchCountManager.instantLyricsLaunched
import com.music.player.bhandari.m.utils.UtilityFun.LaunchYoutube
import com.music.player.bhandari.m.utils.UtilityFun.filterArtistString
import com.music.player.bhandari.m.utils.UtilityFun.isConnectedToInternet
import com.music.player.bhandari.m.utils.UtilityFun.logEvent
import com.nshmura.snappysmoothscroller.SnapType
import com.nshmura.snappysmoothscroller.SnappyLayoutManager
import com.nshmura.snappysmoothscroller.SnappyLinearLayoutManager
import com.wang.avi.AVLoadingIndicatorView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import jp.wasabeef.blurry.Blurry
import java.io.*
import java.net.URL
import java.util.*
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
class ActivityInstantLyric : AppCompatActivity(), OnItemTouchListener, Lyrics.Callback,
    ActionMode.Callback, View.OnClickListener, ArtistInfo.Callback {
    private var mLyrics: Lyrics? = null
    private var artistInfo: ArtistInfo? = null

    @JvmField @BindView(R.id.text_view_lyric_status)
    var lyricStatus: TextView? = null

    @JvmField @BindView(R.id.text_view_artist_info)
    var artInfoTextView: TextView? = null

    @JvmField @BindView(R.id.lyric_view_wrapper)
    var lyricWrapper: View? = null

    @JvmField @BindView(R.id.view_artist_info)
    var viewArtInfoFab: FloatingActionButton? = null

    @JvmField @BindView(R.id.fab_save_lyrics)
    var saveLyrics: FloatingActionButton? = null

    @JvmField @BindView(R.id.loading_lyrics_animation)
    var lyricLoadAnimation: AVLoadingIndicatorView? = null

    @JvmField @BindView(R.id.dynamic_lyrics_recycler_view)
    var recyclerView: RecyclerView? = null

    @JvmField @BindView(R.id.root_view_instant_lyrics)
    var rootView: View? = null

    @JvmField @BindView(R.id.fab_video)
    var watchVideo: FloatingActionButton? = null
    private var isLyricsShown = true
    private var isLyricsSaved = false
    private var fThreadCancelled = false
    private var fIsThreadRunning = false
    private var adapter: LyricsViewAdapter? = null
    private var fIsStaticLyrics = true
    private var layoutManager: LinearLayoutManager? = null
    private var currentMusicInfo: SharedPreferences? = null
    private var mReceiver: BroadcastReceiver? = null
    private var track: String? = ""
    private var artist: String? = ""
    private var lastClicked: Long = 0
    private var actionMode: ActionMode? = null
    private var actionModeActive = false
    private var gestureDetector: GestureDetectorCompat? = null
    private var handler: Handler? = null
    private var lyricThread: DownloadLyricThread? = null

    private lateinit var progressBar: ProgressBar
    private lateinit var trackTitleEditText: EditText
    private lateinit var artistEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("Amit AB", "created")
        currentMusicInfo = getSharedPreferences("current_music", MODE_PRIVATE)
        setStatusBarGradiant(this)
        val themeSelector = MyApp.getPref()
            .getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        setContentView(R.layout.activity_instant_lyrics)
        ButterKnife.bind(this)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_)
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        handler = Handler(Looper.getMainLooper())
        initializeListeners()
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                //make sure lyrics view is shown if artist info was being shown
                isLyricsShown = false
                toggleLyricsArtInfoView()
                updateLyrics(false)
            }
        }
        if (!MyApp.getPref().getBoolean(getString(R.string.pref_disclaimer_accepted), false)) {
            showDisclaimerDialog()
        }

        /*final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        }*/
        try {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "instant_lyric_launched")
            logEvent(bundle)
        }
        catch (ignored: Exception) { }
    }

    private fun growShrinkAnimate() {
        val growAnim = ScaleAnimation(1.0f,
            1.15f,
            1.0f,
            1.15f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f)
        val shrinkAnim = ScaleAnimation(1.15f,
            1.0f,
            1.15f,
            1.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f)
        growAnim.duration = 500
        shrinkAnim.duration = 500
        watchVideo!!.animation = growAnim
        growAnim.start()
        growAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                watchVideo!!.animation = shrinkAnim
                shrinkAnim.start()
            }
        })
        shrinkAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                watchVideo!!.animation = growAnim
                growAnim.start()
            }
        })
    }

    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putParcelable("lyrics", mLyrics)
        savedInstanceState.putParcelable("artInfo", artistInfo)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mLyrics = savedInstanceState.getParcelable("lyrics")
        artistInfo = savedInstanceState.getParcelable("artInfo")
    }

    private fun initializeListeners() {
        lyricStatus!!.setOnClickListener(this)
        saveLyrics!!.backgroundTintList =
            ColorStateList.valueOf(ColorHelper.getColor(R.color.fab_Colors_lyric_view))
        saveLyrics!!.setOnClickListener(this)
        viewArtInfoFab!!.backgroundTintList =
            ColorStateList.valueOf(ColorHelper.getColor(R.color.fab_Colors_lyric_view))
        viewArtInfoFab!!.setOnClickListener(this)
        watchVideo!!.backgroundTintList =
            ColorStateList.valueOf(ColorHelper.getColor(R.color.fab_Colors_lyric_view))
        watchVideo!!.setOnClickListener(this)
        growShrinkAnimate()
        findViewById<View>(R.id.root_view_instant_lyrics).setBackgroundDrawable(getBaseThemeDrawable())
    }

    override fun onPause() {
        super.onPause()
        MyApp.isAppVisible = false
        if (actionMode != null) {
            actionMode!!.finish()
            actionMode = null
        }
        if (fIsThreadRunning) {
            fThreadCancelled = true
        }
        acquireWindowPowerLock(false)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver!!)
    }

    override fun onResume() {
        super.onResume()
        MyApp.isAppVisible = true
        if (!fIsStaticLyrics && !fIsThreadRunning && currentMusicInfo!!.getBoolean("playing",
                false)
        ) {
            fThreadCancelled = false
            Executors.newSingleThreadExecutor().execute(lyricUpdater)
        }
        if (!fIsStaticLyrics) {
            Log.d("ActivityInstantLyric", "onResume: scrolling lyrics to current location")
            acquireWindowPowerLock(true)
            scrollLyricsToCurrentLocation()
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mReceiver!!, IntentFilter(Constants.ACTION.UPDATE_INSTANT_LYRIC))
        track = currentMusicInfo!!.getString("track", "")
        artist = currentMusicInfo!!.getString("artist", "")
        if (mLyrics != null && mLyrics!!.getOriginalArtist()!!
                .lowercase(Locale.getDefault()) == artist!!.lowercase(Locale.getDefault()) && mLyrics!!.getOriginalTrack()!!
                .lowercase(Locale.getDefault()) == track!!.lowercase(Locale.getDefault())
        ) {
            onLyricsDownloaded(mLyrics)
        } else {
            updateLyrics(false)
        }
        instantLyricsLaunched()
    }

    private fun acquireWindowPowerLock(acquire: Boolean) {
        /*if(acquire) {
            if (mWakeLock != null && !mWakeLock.isHeld()) {
                this.mWakeLock.acquire(10*60*1000L); // / *10 minutes
            }
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }else {
            if(mWakeLock!=null && mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }*/
        if (acquire) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun updateLyrics(discardCache: Boolean, vararg param: String) {
        if (param.size == 2) {
            track = param[0]
            artist = param[1]
        } else {
            track = currentMusicInfo!!.getString("track", "")
            artist = currentMusicInfo!!.getString("artist", "")
        }
        if (supportActionBar != null) {
            supportActionBar!!.title = track
            supportActionBar!!.subtitle = artist
        }
        artInfoTextView!!.text = getString(R.string.artist_info_loading)
        val item = TrackItem()
        item.setArtist(artist)
        item.title = track
        item.id = -1

        //set artist photo in background
        var artist = item.getArtist()
        artist = loadArtistInfo(artist)
        if (!MyApp.getPref().getBoolean(getString(R.string.pref_disclaimer_accepted), false)) {
            lyricStatus!!.visibility = View.VISIBLE
            recyclerView!!.visibility = View.GONE
            lyricStatus!!.text = getString(R.string.disclaimer_rejected)
            try {
                //some exceptions reported in play console, thats why
                lyricLoadAnimation!!.hide()
            } catch (ignored: Exception) {
            }
            // }
            return
        }
        if (mLyrics != null && mLyrics!!.getOriginalArtist()!!
                .lowercase(Locale.getDefault()) == artist.toLowerCase() && mLyrics!!.getOriginalTrack()!!
                .lowercase(Locale.getDefault()) == track!!.lowercase(Locale.getDefault())
        ) {
            onLyricsDownloaded(mLyrics)
            return
        }
        if (mLyrics == null) {
            Log.d("Lyrics", "updateLyrics: null lyrics")
        } else {
            Log.d("Lyrics",
                "updateLyrics: " + mLyrics!!.getOriginalArtist()!!.lowercase(Locale.getDefault()) + " : " + artist)
            Log.d("Lyrics",
                "updateLyrics: " + mLyrics!!.getOriginalTrack()!!.lowercase(Locale.getDefault()) + " : " + track)
        }

        //set loading animation
        lyricLoadAnimation!!.visibility = View.VISIBLE
        lyricLoadAnimation!!.show()

        //lyricCopyRightText.setVisibility(View.GONE);
        recyclerView!!.visibility = View.GONE
        fThreadCancelled = true
        lyricStatus!!.visibility = View.VISIBLE
        lyricStatus!!.text = getString(R.string.lyrics_loading)


        //check in offline storage
        //for saved lyrics
        mLyrics = getInstantLyricsFromDB(item)
        if (mLyrics != null) {
            onLyricsDownloaded(mLyrics)
            return
        }
        if (!discardCache) {
            mLyrics = getLyricsFromCache(item)
            if (mLyrics != null) {
                onLyricsDownloaded(mLyrics)
                return
            }
        }
        if (isConnectedToInternet) {
            fetchLyrics(item.getArtist()!!, item.title!!, null)
        } else {
            lyricStatus!!.text = getString(R.string.no_connection)
            lyricLoadAnimation!!.hide()
        }
    }

    private fun loadArtistInfo(artist: String?): String {
        var artist = artist
        artist = filterArtistString(artist!!)
        Log.d("ActivityInstantLyric", "updateLyrics: artist : $artist")
        artistInfo = getArtistInfoFromCache(artist)
        if (artistInfo != null) {
            onArtInfoDownloaded(artistInfo)
        } else {
            DownloadArtInfoThread(this, artist, null).start()
        }
        return artist
    }

    private fun fetchLyrics(vararg params: String?) {
        val artist = params[0]
        val title = params[1]

        ///filter title string
        //title = filterTitleString(title);
        var url: String? = null
        if (params.size > 2) url = params[2]
        lyricThread =
            if (url == null) DownloadLyricThread(this,
                true,
                null,
                artist!!,
                title!!) else DownloadLyricThread(this, true, null, url, artist!!, title!!)
        lyricThread!!.start()
    }

    override fun onLyricsDownloaded(lyrics: Lyrics?) {
        Log.d("ActivityInstantLyric",
            "onLyricsDownloaded: lyrics downloaded " + (lyrics!!.getFlag() == Lyrics.POSITIVE_RESULT))
        Log.d("ActivityInstantLyric",
            "onLyricsDownloaded: " + lyrics.getOriginalArtist() + " : " + lyrics.getOriginalTrack())
        Log.d("ActivityInstantLyric", "onLyricsDownloaded: $artist : $track")
        Log.d("ActivityInstantLyric",
            "onLyricsDownloaded: " + lyrics.getArtist() + " : " + lyrics.getTrack())
        if (lyrics.getOriginalTrack() != track) {
            return
        }
        Log.d("ActivityInstantLyric", "onLyricsDownloaded: current musix matches downloaded lyrics")

        //put lyrics to cache
        putLyricsToCache(lyrics)
        lyricLoadAnimation!!.hide()
        mLyrics = lyrics
        when (Lyrics.POSITIVE_RESULT) {
            lyrics.getFlag() -> {
                Log.d("ActivityInstantLyric",
                    "onLyricsDownloaded: " + lyrics.getArtist() + " : " + lyrics.getTrack())
                lyricStatus!!.visibility = View.GONE
                fIsStaticLyrics = !mLyrics!!.isLRC()
                if (!fIsStaticLyrics) {
                    acquireWindowPowerLock(true)
                }
                fThreadCancelled = false
                recyclerView!!.visibility = View.VISIBLE
                lyricStatus!!.visibility = View.GONE
                initializeLyricsView()
                if (supportActionBar != null) {
                    supportActionBar!!.title = lyrics.getTrack()
                    supportActionBar!!.subtitle = lyrics.getArtist()
                }
                if (lyrics.getArtist() != lyrics.getOriginalArtist()) {
                    loadArtistInfo(lyrics.getArtist())
                }
            }
            else -> {
                lyricStatus!!.text = getString(R.string.tap_to_refresh_lyrics)
                lyricStatus!!.visibility = View.VISIBLE
                recyclerView!!.visibility = View.GONE
            }
        }
        Executors.newSingleThreadExecutor().execute { updateSaveDeleteFabDrawable() }
    }

    private fun updateSaveDeleteFabDrawable() {
        val drawable: Drawable
        if (isLyricsPresentInDB(track!!, if (mLyrics == null) -1 else mLyrics!!.getTrackId())) {
            isLyricsSaved = true
            drawable = resources.getDrawable(R.drawable.ic_delete_black_24dp)
        } else {
            isLyricsSaved = false
            drawable = resources.getDrawable(R.drawable.ic_save_black_24dp)
        }
        handler!!.post { saveLyrics!!.setImageDrawable(drawable) }
    }

    override fun onArtInfoDownloaded(artistInfo: ArtistInfo?) {
        this.artistInfo = artistInfo
        if (artistInfo!!.getArtistContent() == "") {
            artInfoTextView!!.setText(R.string.artist_info_no_result)
            return
        }
        putArtistInfoToCache(artistInfo)
        SetBlurryImagetask().execute(artistInfo)
    }

    private fun initializeLyricsView() {
        if (mLyrics == null) {
            return
        }
        adapter = LyricsViewAdapter(this, mLyrics)
        val snappyLinearLayoutManager: SnappyLayoutManager = SnappyLinearLayoutManager(this)
        snappyLinearLayoutManager.setSnapType(SnapType.CENTER)
        snappyLinearLayoutManager.setSnapDuration(1500)
        //layoutManager.setSnapInterpolator(new DecelerateInterpolator());

        // Attach layout manager to the RecyclerView:
        recyclerView!!.layoutManager = snappyLinearLayoutManager as LayoutManager
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.adapter = adapter!!
        recyclerView!!.addOnItemTouchListener(this)
        gestureDetector = GestureDetectorCompat(this, RecyclerViewDemoOnGestureListener())
        layoutManager = recyclerView!!.layoutManager as LinearLayoutManager?
        fThreadCancelled = false
        if (!fIsStaticLyrics && !fIsThreadRunning) {
            Executors.newSingleThreadExecutor().execute(lyricUpdater)
            scrollLyricsToCurrentLocation()
        }

        //adapter.notifyDataSetChanged();
    }

    private fun scrollLyricsToCurrentLocation() {
        val startTime = currentMusicInfo!!.getLong("startTime", System.currentTimeMillis())
        val distance = System.currentTimeMillis() - startTime
        adapter!!.changeCurrent(distance)
        val index = adapter!!.getCurrentTimeIndex()
        Log.d("ActivityInstantLyric", "scrollLyricsToCurrentLocation: index $index")
        if (index != -1) {
            // without delay lyrics wont scroll to latest position when called from onResume for some reason
            Handler().postDelayed({ recyclerView!!.smoothScrollToPosition(index) }, 100)
        }
        adapter!!.notifyDataSetChanged()
    }

    private fun syncProblemDialog() {
        MaterialDialog(this)
            .title(R.string.lyric_sync_error_title)
            .message(R.string.lyric_sync_error_content)
            .positiveButton(R.string.okay)
            .show()
    }

    private fun shareLyrics() {
        if (mLyrics == null || mLyrics!!.getFlag() != Lyrics.POSITIVE_RESULT) {
            Snackbar.make(rootView!!, getString(R.string.error_no_lyrics), Snackbar.LENGTH_SHORT)
                .show()
            return
        }
        var shareBody = getString(R.string.lyrics_share_text)
        shareBody += """
            
            
            Track : ${mLyrics!!.getTrack()}
            Artist : ${mLyrics!!.getArtist()}
            
            
            """.trimIndent()
        when {
            mLyrics!!.isLRC() -> {
                shareBody += Html.fromHtml(adapter!!.getStaticLyrics()).toString()
            }
            else -> {
                shareBody += Html.fromHtml(mLyrics!!.getText())
            }
        }
        shareTextIntent(shareBody)
    }

    private fun shareTextIntent(shareBody: String) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Lyrics")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Lyrics share!"))
    }

    private fun myToggleSelection(idx: Int) {
        adapter!!.toggleSelection(idx)
        //String title = adapter.getSelectedItemCount();
        //actionMode.setTitle(title);
        if (adapter!!.getSelectedItemCount() == 0) {
            actionMode!!.finish()
            actionMode = null
            return
        }
        val numberOfItems = adapter!!.getSelectedItemCount()
        val selectionString = if (numberOfItems == 1) " item selected" else " items selected"
        val title = numberOfItems.toString() + selectionString
        actionMode!!.title = title
    }

    override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        val inflater = actionMode.menuInflater
        inflater.inflate(R.menu.menu_cab_recyclerview_lyrics, menu)
        return true
    }

    override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_share -> {
                shareTextIntent(selectedLyricString.toString())
                actionMode.finish()
                actionModeActive = false
            }
            R.id.menu_lyric_card -> {
                val intent = Intent(this, ActivityLyricCard::class.java)
                intent.putExtra("lyric", selectedLyricString.toString())
                    .putExtra("artist", mLyrics!!.getArtist())
                    .putExtra("track", mLyrics!!.getTrack())
                startActivity(intent)
            }
        }
        return false
    }

    private val selectedLyricString: StringBuilder
        get() {
            val shareString = StringBuilder()
            val selectedItemPositions = adapter!!.getSelectedItems()
            var currPos: Int
            for (element in selectedItemPositions) {
                currPos = element
                val lyricLine = adapter!!.getLineAtPosition(currPos)
                shareString.append(lyricLine).append("\n")
            }
            return shareString
        }

    override fun onDestroyActionMode(actionMode: ActionMode) {
        actionMode.finish()
        actionModeActive = false
        if (lyricThread != null) lyricThread!!.setCallback(null)
        adapter!!.clearSelections()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab_save_lyrics -> Executors.newSingleThreadExecutor()
                .execute { saveOrDeleteLyrics() }
            R.id.view_artist_info -> {
                //clear offline stored lyrics if any and reload
                if (artistInfo == null) {
                    Snackbar.make(rootView!!,
                        getString(R.string.art_info_not_available),
                        Snackbar.LENGTH_SHORT).show()
                    return
                }
                toggleLyricsArtInfoView()
            }
            R.id.lyrics_line -> if (recyclerView != null) {
                val idx = recyclerView!!.getChildLayoutPosition(view)
                if (actionModeActive) {
                    myToggleSelection(idx)
                    return
                }
            }
            R.id.text_view_lyric_status -> {
                mLyrics = null
                updateLyrics(true)
            }
            R.id.fab_video -> LaunchYoutube(this, "$track - $artist")
        }
    }

    private fun toggleLyricsArtInfoView() {
        if (isLyricsShown) {
            lyricWrapper!!.visibility = View.GONE
            findViewById<View>(R.id.artist_info_wrapper).visibility = View.VISIBLE
            artInfoTextView!!.text = artistInfo!!.getArtistContent()
            isLyricsShown = false
            viewArtInfoFab!!.setImageDrawable(resources.getDrawable(R.drawable.ic_subject_black_24dp))
        } else {
            lyricWrapper!!.visibility = View.VISIBLE
            findViewById<View>(R.id.artist_info_wrapper).visibility = View.GONE
            isLyricsShown = true
            viewArtInfoFab!!.setImageDrawable(resources.getDrawable(R.drawable.ic_info_black_24dp))
        }
    }

    private fun saveOrDeleteLyrics() {
        when {
            isLyricsSaved -> {
                if (clearLyricsFromDB(track!!)) {
                    updateSaveDeleteFabDrawable()
                    Snackbar.make(rootView!!, getString(R.string.lyrics_removed), Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
            else -> {
                when {
                    mLyrics != null && mLyrics!!.getOriginalTrack() == track && mLyrics!!.getOriginalArtist() == artist -> {
                        val item = TrackItem()
                        item.setArtist(artist)
                        item.title = track
                        item.id = -1
                        when {
                            putInstantLyricsInDB(mLyrics, item) -> {
                                updateSaveDeleteFabDrawable()
                                Snackbar.make(rootView!!,
                                    getString(R.string.lyrics_saved),
                                    Snackbar.LENGTH_SHORT).show()
                            }
                            else -> {
                                Snackbar.make(rootView!!,
                                    getString(R.string.error_saving_instant_lyrics),
                                    Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else -> {
                        Snackbar.make(rootView!!,
                            getString(R.string.error_saving_instant_lyrics),
                            Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        gestureDetector!!.onTouchEvent(e)
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    private fun setBlurryBackground(b: Bitmap) {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        fadeIn.duration = 2000
        findViewById<View>(R.id.full_screen_iv).startAnimation(fadeIn)
        Blurry.with(this).radius(1).color(Color.argb(100, 50, 0, 0)).from(b)
            .into(findViewById<View>(R.id.full_screen_iv) as ImageView)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_instant_lyrics, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, ActivityMain::class.java))
                overridePendingTransition(android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right)
            }
            R.id.action_share -> shareLyrics()
            R.id.action_sync_problem -> syncProblemDialog()
            R.id.action_search -> searchLyricDialog()
            R.id.action_wrong_lyrics -> wrongLyrics()
            R.id.action_reload -> {
                if (System.currentTimeMillis() - lastClicked < 2000) {
                    return super.onOptionsItemSelected(item)
                }
                val trackItem = TrackItem()
                trackItem.setArtist(artist)
                trackItem.title = track
                trackItem.id = -1
                clearLyricsFromDB(trackItem)
                mLyrics = null
                updateLyrics(true)
                lastClicked = System.currentTimeMillis()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun searchLyricDialog() {
        val builder = MaterialDialog(this)
            .title(R.string.title_search_lyrics)
            .customView(R.layout.lyric_search_dialog, scrollable = true)
            .positiveButton(R.string.pos_search_lyric){
                if (trackTitleEditText.text.toString() == track && artistEditText.text.toString() == artist) {
                    return@positiveButton
                }
                if (trackTitleEditText.text.toString() == "") {
                    trackTitleEditText.error = getString(R.string.error_empty_title_lyric_search)
                    return@positiveButton
                }
                var artistName = artistEditText.text.toString()
                if (artistName == "") {
                    artistName = getString(R.string.unknown_artist)
                }
                progressBar.visibility = View.VISIBLE
                val finalArtistName = artistName
                handler!!.postDelayed({
                    updateLyrics(false, trackTitleEditText.text.toString(), finalArtistName)
                }, 1000)
            }
            .negativeButton(R.string.cancel)

        val layout: View = builder.getCustomView()
        trackTitleEditText = layout.findViewById<EditText>(R.id.track_title_edit)
        artistEditText = layout.findViewById<EditText>(R.id.artist_edit)
        trackTitleEditText.setText(track)
        artistEditText.setText(artist)
        progressBar = layout.findViewById<ProgressBar>(R.id.progressBar)

        handler!!.postDelayed({
            trackTitleEditText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                0f,
                0f,
                0))
            trackTitleEditText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
                0f,
                0f,
                0))
        }, 200)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(trackTitleEditText, InputMethodManager.SHOW_IMPLICIT)
        builder.show()
    }

    private fun wrongLyrics() {
        if (mLyrics == null || mLyrics!!.getFlag() != Lyrics.POSITIVE_RESULT) {
            Toast.makeText(this, getString(R.string.error_no_lyrics), Toast.LENGTH_SHORT).show()
            return
        }
        if (mLyrics!!.getSource() == null || mLyrics!!.getSource() != ViewLyrics.clientUserAgent) {
            Toast.makeText(this, "No lyrics from other sources available!", Toast.LENGTH_SHORT)
                .show()
            return
        }
        Log.d("ActivityInstantLyric", "wrongLyrics: starting search of lyrics")
        Executors.newSingleThreadExecutor().execute {
            if (artist != null && track != null) {
                try {
                    fromMetaData(this@ActivityInstantLyric,
                        artist,
                        track,
                        null,
                        this@ActivityInstantLyric)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun filterTitleString(title: String): String {
        var title = title
        title = title.replace("\\(.*\\)".toRegex(), "")
        return title
    }

    private fun showDisclaimerDialog() {
        MaterialDialog(this)
            .title(R.string.lyrics_disclaimer_title)
            .message(R.string.lyrics_disclaimer_content)
            .positiveButton(R.string.lyrics_disclaimer_title_pos){
                MyApp.getPref()
                    .edit().putBoolean(getString(R.string.pref_disclaimer_accepted), true)
                    .apply()
                updateLyrics(false)
            }
            .negativeButton(R.string.lyrics_disclaimer_title_neg)
            .show()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    private inner class RecyclerViewDemoOnGestureListener :
        SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val view = recyclerView!!.findChildViewUnder(e.x, e.y)
            view?.let { onClick(it) }
            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent) {
            val view = recyclerView!!.findChildViewUnder(e.x, e.y)
            if (actionModeActive) {
                return
            }
            // Start the CAB using the ActionMode.Callback defined above
            actionMode = startActionMode(this@ActivityInstantLyric)
            actionModeActive = true
            val idx = recyclerView!!.getChildPosition(view!!)
            myToggleSelection(idx)
            super.onLongPress(e)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SetBlurryImagetask :
        AsyncTask<ArtistInfo?, String?, Bitmap?>() {
        var b: Bitmap? = null

        override fun doInBackground(vararg p0: ArtistInfo?): Bitmap? {

            //store file in cache with artist id as name
            //create folder in cache for artist images
            val CACHE_ART_THUMBS = MyApp.getContext().cacheDir.toString() + "/art_thumbs/"
            val actual_file_path = CACHE_ART_THUMBS + p0[0]!!.getOriginalArtist()
            val f = File(CACHE_ART_THUMBS)
            if (!f.exists()) {
                f.mkdir()
            }
            if (!File(actual_file_path).exists()) {
                //create file
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(File(actual_file_path))
                    val url = URL(p0[0]!!.getImageUrl())
                    val inputStream = url.openConnection().getInputStream()
                    val buffer = ByteArray(1024)
                    var bufferLength = 0
                    while (inputStream.read(buffer).also { bufferLength = it } > 0) {
                        fos.write(buffer, 0, bufferLength)
                    }
                    fos.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            b = BitmapFactory.decodeFile(actual_file_path)
            return b
        }

        override fun onPostExecute(b: Bitmap?) {
            b?.let { setBlurryBackground(it) }
        }
    }

    private val lyricUpdater = Runnable {
        while (true) {
            if (fThreadCancelled) {
                break
            }
            fIsThreadRunning = true
            //Log.v(Constants.L_TAG,"Lyric thread running");
            handler!!.post(Runnable {
                if (!currentMusicInfo!!.getBoolean("playing", false)) {
                    return@Runnable
                }
                val startTime = currentMusicInfo!!.getLong("startTime", System.currentTimeMillis())
                val distance = System.currentTimeMillis() - startTime
                val index = adapter!!.changeCurrent(distance)
                val firstVisibleItem = layoutManager!!.findFirstVisibleItemPosition()
                val lastVisibleItem = layoutManager!!.findLastVisibleItemPosition()
                if (index != -1 && index > firstVisibleItem && index < lastVisibleItem) {
                    recyclerView!!.smoothScrollToPosition(index)
                }
            })
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        fIsThreadRunning = false
        Log.v(Constants.L_TAG, "Lyric thread stopped")
    }
}