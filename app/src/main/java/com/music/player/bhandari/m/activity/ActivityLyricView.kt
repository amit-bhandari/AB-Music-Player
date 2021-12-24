package com.music.player.bhandari.m.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.*
import android.text.Html
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
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
import com.music.player.bhandari.m.UIElementHelper.BottomOffsetDecoration
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.adapter.LyricsViewAdapter
import com.music.player.bhandari.m.lyricCard.ActivityLyricCard
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.ViewLyrics
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadArtInfoThread
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadLyricThread
import com.music.player.bhandari.m.utils.UtilityFun
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
class ActivityLyricView : AppCompatActivity(), View.OnClickListener,
    RecyclerView.OnItemTouchListener, Lyrics.Callback, ActionMode.Callback, ArtistInfo.Callback {
    private var handler: Handler? = null
    private var trackTitle: String? = ""
    private var artist: String? = ""

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

    @JvmField @BindView(R.id.fab_video)
    var watchVideo: FloatingActionButton? = null

    @JvmField @BindView(R.id.loading_lyrics_animation)
    var lyricLoadAnimation: AVLoadingIndicatorView? = null

    @JvmField @BindView(R.id.dynamic_lyrics_recycler_view)
    var recyclerView: RecyclerView? = null

    @JvmField @BindView(R.id.root_view_instant_lyrics)
    var rootView: View? = null
    private var actionMode: ActionMode? = null
    private var actionModeActive: Boolean = false
    private var gestureDetector: GestureDetectorCompat? = null
    private var mLyrics: Lyrics? = null
    private var artistInfo: ArtistInfo? = null
    private var isLyricsSaved: Boolean = false
    private var isLyricsShown: Boolean = true
    var adapter: LyricsViewAdapter? = null
    private var lyricThread: DownloadLyricThread? = null
    
    private lateinit var progressBar: ProgressBar
    private lateinit var artistTextView: EditText
    private lateinit var trackTitleEditText: EditText
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ColorHelper.setStatusBarGradiant(this)
        when (MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        setContentView(R.layout.activity_instant_lyrics)
        ButterKnife.bind(this)
        val toolbar: Toolbar = findViewById(R.id.toolbar_)
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        getWindow().decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        handler = Handler(Looper.getMainLooper())
        initializeListeners()
    }

    private fun initializeListeners() {
        lyricStatus!!.setOnClickListener(this)
        saveLyrics!!.backgroundTintList = ColorStateList.valueOf(ColorHelper.getWidgetColor())
        saveLyrics!!.setOnClickListener(this)
        watchVideo!!.backgroundTintList = ColorStateList.valueOf(ColorHelper.getWidgetColor())
        watchVideo!!.setOnClickListener(this)
        watchVideo!!.visibility = View.VISIBLE
        growShrinkAnimate()
        viewArtInfoFab!!.backgroundTintList = ColorStateList.valueOf(ColorHelper.getWidgetColor())
        viewArtInfoFab!!.setOnClickListener(this)
        rootView!!.setBackgroundColor(ColorHelper.getColor(R.color.blackTransparent))
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab_save_lyrics -> Executors.newSingleThreadExecutor().execute { saveOrDeleteLyrics() }
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
            R.id.lyrics_line -> {
                val idx: Int = recyclerView!!.getChildLayoutPosition(v)
                if (actionModeActive) {
                    myToggleSelection(idx)
                    return
                }
            }
            R.id.text_view_lyric_status -> {
                mLyrics = null
                updateLyrics(true)
            }
            R.id.fab_video -> if (artistInfo != null) {
                UtilityFun.LaunchYoutube(this, trackTitle + " - " + artistInfo!!.getCorrectedArtist())
            } else {
                UtilityFun.LaunchYoutube(this, "$trackTitle - $artist")
            }
        }
    }

    private fun saveOrDeleteLyrics() {
        when {
            isLyricsSaved -> {
                if (OfflineStorageLyrics.clearLyricsFromDB(trackTitle!!)) {
                    updateSaveDeleteFabDrawable()
                    Snackbar.make(rootView!!, getString(R.string.lyrics_removed), Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
            else -> {
                when {
                    (mLyrics != null) && mLyrics!!.getOriginalTrack()
                        .equals(trackTitle) && mLyrics!!.getOriginalArtist().equals(artist) -> {
                        val item = TrackItem()
                        item.setArtist(artist)
                        item.title = trackTitle
                        item.id = -1
                        when {
                            OfflineStorageLyrics.putInstantLyricsInDB(mLyrics, item) -> {
                                updateSaveDeleteFabDrawable()
                                Snackbar.make(rootView!!, getString(R.string.lyrics_saved), Snackbar.LENGTH_SHORT)
                                    .show()
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

    private fun toggleLyricsArtInfoView() {
        when {
            isLyricsShown -> {
                lyricWrapper!!.visibility = View.GONE
                findViewById<View>(R.id.artist_info_wrapper).visibility = View.VISIBLE
                artInfoTextView!!.text = artistInfo!!.getArtistContent()
                isLyricsShown = false
                viewArtInfoFab!!.setImageDrawable(resources.getDrawable(R.drawable.ic_subject_black_24dp))
            }
            else -> {
                lyricWrapper!!.visibility = View.VISIBLE
                findViewById<View>(R.id.artist_info_wrapper).visibility = View.GONE
                isLyricsShown = true
                viewArtInfoFab!!.setImageDrawable(resources.getDrawable(R.drawable.ic_info_black_24dp))
            }
        }
    }

    override fun onPause() {
        super.onPause()
        MyApp.isAppVisible = false
        if (actionMode != null) {
            actionMode!!.finish()
            actionMode = null
        }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        MyApp.isAppVisible = true
        if (intent.extras == null) {
            finish()
            return
        }
        if (intent.extras!!.getBoolean("from_notif")) {
            try {
                val bundle: Bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification_clicked")
                UtilityFun.logEvent(bundle)
            } catch (ignored: Exception) {
            }
        }
        mLyrics = intent.extras!!.get("lyrics") as Lyrics
        trackTitle = intent.extras!!.getString("track_title")
        artist = intent.extras!!.getString("artist")
        updateLyrics(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_lyric_view, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        //startActivity(new Intent(this, ActivityExploreLyrics.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_right)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> onBackPressed()
            R.id.action_share -> shareLyrics()
            R.id.action_wrong_lyrics -> wrongLyrics()
            R.id.action_search -> searchLyricDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun searchLyricDialog() {
        val builder = MaterialDialog(this)
            .title(R.string.title_search_lyrics)
            .customView(R.layout.lyric_search_dialog, scrollable = true)
            .positiveButton(R.string.pos_search_lyric){
                if ((trackTitleEditText.text.toString() == "")) {
                    trackTitleEditText.error = getString(R.string.error_empty_title_lyric_search)
                    return@positiveButton
                }
                var artistName: String = artistTextView.text.toString()
                if ((artistName == "")) {
                    artistName = getString(R.string.unknown_artist)
                }
                progressBar.visibility = View.VISIBLE
                val finalArtistName: String = artistName
                handler!!.postDelayed({
                    finish()
                    val intent: Intent =
                        Intent(this@ActivityLyricView, ActivityLyricView::class.java)
                    intent.putExtra("track_title", trackTitleEditText.text.toString())
                    intent.putExtra("artist", finalArtistName)
                    startActivity(intent)
                }, 1000)
            }
            .negativeButton(R.string.cancel)

        val layout: View = builder.getCustomView()
        trackTitleEditText = layout.findViewById(R.id.track_title_edit)
        artistTextView = layout.findViewById(R.id.artist_edit)
        trackTitleEditText.setText(trackTitle)
        artistTextView.setText(artist)
        progressBar = layout.findViewById(R.id.progressBar)

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
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?)?.showSoftInput(
            trackTitleEditText,
            InputMethodManager.SHOW_IMPLICIT)
        builder.show()
    }

    private fun wrongLyrics() {
        if (mLyrics == null || mLyrics!!.getFlag() !== Lyrics.POSITIVE_RESULT) {
            Toast.makeText(this, getString(R.string.error_no_lyrics), Toast.LENGTH_SHORT).show()
            return
        }
        if (mLyrics!!.getSource() == null || !mLyrics!!.getSource()
                .equals(ViewLyrics.clientUserAgent)
        ) {
            Toast.makeText(this, "No lyrics from other sources available!", Toast.LENGTH_SHORT)
                .show()
            return
        }
        Executors.newSingleThreadExecutor().execute {
            if (artist != null && trackTitle != null) {
                try {
                    ViewLyrics.fromMetaData(this@ActivityLyricView,
                        artist,
                        trackTitle,
                        null,
                        this@ActivityLyricView)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun shareLyrics() {
        if (mLyrics == null || mLyrics!!.getFlag() !== Lyrics.POSITIVE_RESULT) {
            Snackbar.make(rootView!!, getString(R.string.error_no_lyrics), Snackbar.LENGTH_SHORT)
                .show()
            return
        }
        var shareBody: String = getString(R.string.lyrics_share_text)
        shareBody += "\n\nTrack : " + mLyrics!!.getTrack()
            .toString() + "\n" + "Artist : " + mLyrics!!.getArtist().toString() + "\n\n"
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

    private fun updateLyrics(discardCache: Boolean) {
        if (supportActionBar != null) {
            supportActionBar!!.title = trackTitle
            supportActionBar!!.subtitle = artist
        }
        artInfoTextView!!.text = getString(R.string.artist_info_loading)
        val item = TrackItem()
        item.setArtist(artist)
        item.title = trackTitle
        item.id = -1
        when {
            (mLyrics != null) && mLyrics!!.getOriginalArtist()!!.lowercase(Locale.getDefault()) == artist!!.lowercase(Locale.getDefault()) && mLyrics!!.getOriginalTrack()!!.lowercase(Locale.getDefault()) == trackTitle!!.lowercase(Locale.getDefault()) -> {
                onLyricsDownloaded(mLyrics)
                return
            }

            //set loading animation

            //lyricCopyRightText.setVisibility(View.GONE);

            //check in offline storage
            //for saved lyrics
            else -> {
                lyricLoadAnimation!!.visibility = View.VISIBLE
                lyricLoadAnimation!!.show()

                //lyricCopyRightText.setVisibility(View.GONE);
                recyclerView!!.visibility = View.GONE
                lyricStatus!!.visibility = View.VISIBLE
                lyricStatus!!.text = getString(R.string.lyrics_loading)

                //check in offline storage
                //for saved lyrics
                mLyrics = OfflineStorageLyrics.getInstantLyricsFromDB(item)
                if (mLyrics != null) {
                    onLyricsDownloaded(mLyrics)
                    return
                }
                if (!discardCache) {
                    mLyrics = OfflineStorageLyrics.getLyricsFromCache(item)
                    if (mLyrics != null) {
                        onLyricsDownloaded(mLyrics)
                        return
                    }
                }
                when {
                    UtilityFun.isConnectedToInternet -> {
                        fetchLyrics((item.getArtist())!!, (item.title)!!, null)
                    }
                    else -> {
                        lyricStatus!!.text = getString(R.string.no_connection)
                        lyricLoadAnimation!!.hide()
                    }
                }
            }
        }

    }

    private fun fetchArtistImage(artist: String) {
        var artist: String? = artist
        artist = UtilityFun.filterArtistString(artist!!)
        artistInfo = OfflineStorageArtistBio.getArtistInfoFromCache(artist)
        when {
            artistInfo != null -> {
                onArtInfoDownloaded(artistInfo)
            }
            else -> {
                DownloadArtInfoThread(this, artist, null).start()
            }
        }
    }

    private fun fetchLyrics(vararg params: String?) {
        val artist = params[0]
        val title = params[1]

        ///filter title string
        //title = filterTitleString(title);
        var url: String? = null
        if (params.size > 2) url = params[2]
        lyricThread = if (url == null) DownloadLyricThread(this, true, null, artist!!, title!!) else DownloadLyricThread(this, true, null, url, artist!!, title!!)
        lyricThread!!.start()
    }

    override fun onLyricsDownloaded(lyrics: Lyrics?) {
        if (!(lyrics!!.getOriginalArtist()!!.lowercase(Locale.getDefault()) == artist!!.lowercase(Locale.getDefault()) && lyrics.getOriginalTrack()!!
                .lowercase(Locale.getDefault())
                .equals(
                trackTitle!!.lowercase(Locale.getDefault())))
        ) {
            return
        }

        //put lyrics to cache
        OfflineStorageLyrics.putLyricsToCache(lyrics)
        lyricLoadAnimation!!.hide()
        mLyrics = lyrics
        when {
            lyrics.getFlag() === Lyrics.POSITIVE_RESULT -> {
                lyricStatus!!.visibility = View.GONE
                recyclerView!!.visibility = View.VISIBLE
                lyricStatus!!.visibility = View.GONE
                if (supportActionBar != null) {
                    supportActionBar!!.title = lyrics.getTrack()
                    supportActionBar!!.subtitle = lyrics.getArtist()
                }

                //if(!lyrics.getArtist().equals(mLyrics.getArtist())){
                fetchArtistImage(lyrics.getArtist()!!)
                //}
                initializeLyricsView()
            }
            else -> {
                lyricStatus!!.text = getString(R.string.tap_to_refresh_lyrics)
                lyricStatus!!.visibility = View.VISIBLE
                recyclerView!!.visibility = View.GONE
            }
        }
        Executors.newSingleThreadExecutor().execute { updateSaveDeleteFabDrawable() }
    }

    private fun initializeLyricsView() {
        if (mLyrics == null) {
            return
        }
        adapter = LyricsViewAdapter(this, mLyrics)
        adapter!!.setNoDynamicLyrics(true)
        val snappyLinearLayoutManager: SnappyLayoutManager = SnappyLinearLayoutManager(this)
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
        gestureDetector = GestureDetectorCompat(this, RecyclerViewDemoOnGestureListener())
    }

    private fun updateSaveDeleteFabDrawable() {
        val drawable: Drawable
        when {
            OfflineStorageLyrics.isLyricsPresentInDB(trackTitle!!,
                if ((mLyrics == null)) -1 else mLyrics!!.getTrackId()) -> {
                isLyricsSaved = true
                drawable = resources.getDrawable(R.drawable.ic_delete_black_24dp)
            }
            else -> {
                isLyricsSaved = false
                drawable = resources.getDrawable(R.drawable.ic_save_black_24dp)
            }
        }
        handler!!.post { saveLyrics!!.setImageDrawable(drawable) }
    }

    private fun setBlurryBackground(b: Bitmap) {
        val fadeIn: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        fadeIn.duration = 2000
        findViewById<View>(R.id.full_screen_iv).startAnimation(fadeIn)
        Blurry.with(this).radius(1).color(Color.argb(100, 50, 0, 0)).from(b)
            .into((findViewById<View>(R.id.full_screen_iv) as ImageView?))
    }

    override fun onArtInfoDownloaded(artistInfo: ArtistInfo?) {
        this.artistInfo = artistInfo
        if (artistInfo!!.getArtistContent().equals("")) {
            artInfoTextView!!.setText(R.string.artist_info_no_result)
            return
        }
        OfflineStorageArtistBio.putArtistInfoToCache(artistInfo)
        SetBlurryImagetask().execute(artistInfo)
    }

    private inner class RecyclerViewDemoOnGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val view: View? = recyclerView!!.findChildViewUnder(e.x, e.y)
            if (view != null) {
                onClick(view)
            }
            return super.onSingleTapConfirmed(e)
        }

        override fun onLongPress(e: MotionEvent) {
            val view: View = recyclerView!!.findChildViewUnder(e.x, e.y)!!
            if (actionModeActive) {
                return
            }
            // Start the CAB using the ActionMode.Callback defined above
            actionMode = startActionMode(this@ActivityLyricView)
            actionModeActive = true
            val idx: Int = recyclerView!!.getChildPosition(view)
            myToggleSelection(idx)
            super.onLongPress(e)
        }
    }

    private fun myToggleSelection(idx: Int) {
        adapter!!.toggleSelection(idx)
        //String title = adapter.getSelectedItemCount();
        //actionMode.setTitle(title);
        if (adapter!!.getSelectedItemCount() === 0) {
            actionMode!!.finish()
            actionMode = null
            return
        }
        val numberOfItems: Int = adapter!!.getSelectedItemCount()
        val selectionString: String =
            if (numberOfItems == 1) " item selected" else " items selected"
        val title: String = numberOfItems.toString() + selectionString
        actionMode!!.title = title
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SetBlurryImagetask :
        AsyncTask<ArtistInfo?, String?, Bitmap?>() {
        var b: Bitmap? = null
        override fun doInBackground(vararg p0: ArtistInfo?): Bitmap? {

            //store file in cache with artist id as name
            //create folder in cache for artist images
            val CACHE_ART_THUMBS: String =
                MyApp.getContext().cacheDir.toString() + "/art_thumbs/"
            val actual_file_path: String = CACHE_ART_THUMBS + p0[0]!!.getOriginalArtist()
            val f = File(CACHE_ART_THUMBS)
            if (!f.exists()) {
                f.mkdir()
            }
            if (!File(actual_file_path).exists()) {
                //create file
                val fos: FileOutputStream?
                try {
                    fos = FileOutputStream(File(actual_file_path))
                    val url = URL(p0[0]!!.getImageUrl())
                    val inputStream: InputStream = url.openConnection().getInputStream()
                    val buffer = ByteArray(1024)
                    var bufferLength: Int
                    while ((inputStream.read(buffer).also { bufferLength = it }) > 0) {
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

        override fun onPostExecute(result: Bitmap?) {
            if (result != null) {
                setBlurryBackground(result)
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
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
            R.id.menu_share -> {
                shareTextIntent(getSelectedLyricString().toString())
                actionMode.finish()
                actionModeActive = false
            }
            R.id.menu_lyric_card -> {
                val intent = Intent(this, ActivityLyricCard::class.java)
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
            currPos = selectedItemPositions[i]
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
}