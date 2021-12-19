package com.music.player.bhandari.m.activity

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.bumptech.glide.RequestBuilder
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo
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
class ActivitySecondaryLibrary constructor() : AppCompatActivity(), View.OnClickListener,
    ArtistInfo.Callback {
    @BindView(R.id.secondaryLibraryList)
    var mRecyclerView: RecyclerView? = null

    @BindView(R.id.albumsInArtistFrag)
    var mAlbumsRecyclerView: RecyclerView? = null

    @BindView(R.id.artistBio)
    var artistBio: ExpandableTextView? = null
    private var adapter: SecondaryLibraryAdapter? = null
    private var mReceiverForMiniPLayerUpdate: BroadcastReceiver? = null
    private var mReceiverForDataReady: BroadcastReceiver? = null

    @BindView(R.id.song_name_mini_player)
    var songNameMiniPlayer: TextView? = null

    @BindView(R.id.artist_mini_player)
    var artistNameMiniPlayer: TextView? = null

    @BindView(R.id.play_pause_mini_player)
    var buttonPlay: ImageView? = null

    @BindView(R.id.album_art_mini_player)
    var albumArtIv: ImageView? = null

    @BindView(R.id.mini_player)
    var miniPlayer: LinearLayout? = null

    @BindView(R.id.next_mini_plaayrer)
    var buttonNext: ImageView? = null

    @BindView(R.id.main_backdrop)
    var mainBackdrop: ImageView? = null

    @BindView(R.id.fab_right_side)
    var fab: FloatingActionButton? = null

    @BindView(R.id.border_view)
    var border: View? = null

    @BindView(R.id.progressBar)
    var progressBar: View? = null

    @BindView(R.id.main_collapsing)
    var collapsingToolbarLayout: CollapsingToolbarLayout? = null

    @BindView(R.id.root_view_secondary_lib)
    var rootView: View? = null

    @BindView(R.id.app_bar_layout_secondary_library)
    var appBarLayout: AppBarLayout? = null
    private var mLastClickTime: Long = 0
    private var status: Int = 0
    private var key: Int = 0 //text view on which clicked
    private var title: String? = null
    private val handler: Handler = Handler(Looper.getMainLooper())
    var playerService: PlayerService? = null
    private val RC_LOGIN: Int = 100
    protected override fun onNewIntent(intent: Intent) {
        try {
            val b: Boolean = intent.getBooleanExtra("refresh", false)
            if (b) {
                val position: Int = intent.getIntExtra("position", -1)
                val title: String = intent.getStringExtra("title")
                val artist: String = intent.getStringExtra("artist")
                val album: String = intent.getStringExtra("album")
                val originalTitle: String = intent.getStringExtra("originalTitle")
                val currentItem: TrackItem = playerService.getCurrentTrack()
                if ((currentItem.title == originalTitle)) {
                    //current song is playing, update  track item
                    playerService.updateTrackItem(playerService.getCurrentTrackPosition(),
                        currentItem.id,
                        title,
                        artist,
                        album)
                    playerService.PostNotification()
                    updateMiniplayerUI()
                }

                //data changed in edit track info activity, update item
                adapter.updateItem(position, title, artist, album)
            }
        } catch (ignored: Exception) {
            Log.v(Constants.TAG, ignored.toString())
        }
        super.onNewIntent(intent)
    }

    @SuppressLint("RestrictedApi")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //if player service not running, kill the app
        if (MyApp.Companion.getService() == null) {
            UtilityFun.restartApp()
            finish()
            return
        }
        playerService = MyApp.Companion.getService()
        ColorHelper.setStatusBarGradiant(this)
        val themeSelector: Int = MyApp.Companion.getPref()
            .getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        setContentView(R.layout.activity_secondary_library)
        ButterKnife.bind(this)
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar_)
        try {
            toolbar.setCollapsible(false)
        } catch (ignored: Exception) {
        }
        setSupportActionBar(toolbar)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true)
            getSupportActionBar().setDisplayShowHomeEnabled(true)
        }
        if (getIntent() != null) {
            status = getIntent().getIntExtra("status", 0)
            key = getIntent().getIntExtra("key", 0)
            title = getIntent().getStringExtra("title")
        }
        //remove _ from playlist name
        if (status == Constants.FRAGMENT_STATUS.PLAYLIST_FRAGMENT) {
            setTitle(title!!.replace("_", " "))
        } else {
            setTitle(title)
        }
        if (MyApp.Companion.isLocked()) {
            //border.setVisibility(View.VISIBLE);
            border!!.setBackgroundResource(R.drawable.border_2dp)
        } else {
            border!!.setBackgroundResource(0)
        }
        Executors.newSingleThreadExecutor().execute(object : Runnable {
            public override fun run() {
                when (status) {
                    Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT -> {
                        adapter = SecondaryLibraryAdapter(this@ActivitySecondaryLibrary,
                            MusicLibrary.getInstance()
                                .getSongListFromArtistIdNew(key, Constants.SORT_ORDER.ASC))
                        if (adapter.getList().isEmpty()) {
                            break
                        }

                        //get album list for artist
                        val data: ArrayList<dataItem> = ArrayList<dataItem>()
                        for (d: dataItem in MusicLibrary.getInstance().getDataItemsForAlbums()) {
                            if (d.artist_id == key) data.add(d)
                        }
                        handler.post(object : Runnable {
                            public override fun run() {
                                mAlbumsRecyclerView.setVisibility(View.VISIBLE)
                            }
                        })
                        mAlbumsRecyclerView.setAdapter(AlbumLibraryAdapter(this@ActivitySecondaryLibrary,
                            data))
                        mAlbumsRecyclerView.setLayoutManager(LinearLayoutManager(this@ActivitySecondaryLibrary,
                            LinearLayoutManager.HORIZONTAL,
                            false))
                        mAlbumsRecyclerView.setNestedScrollingEnabled(false)
                        val item: TrackItem = TrackItem()
                        item.artist_id = key
                        item.setArtist(title)
                        val mArtistInfo: ArtistInfo? =
                            OfflineStorageArtistBio.getArtistBioFromTrackItem(item)
                        //second check is added to make sure internet call will happen
                        //when user manually changes artist tag
                        if (mArtistInfo != null && (item.getArtist()!!
                                .trim({ it <= ' ' }) == mArtistInfo.getOriginalArtist().trim())
                        ) {
                            handler.post(object : Runnable {
                                public override fun run() {
                                    onArtInfoDownloaded(mArtistInfo)
                                }
                            })
                        } else if (UtilityFun.isConnectedToInternet) {
                            var artist: String? = item.getArtist()
                            artist = UtilityFun.filterArtistString(artist)
                            DownloadArtInfoThread(this@ActivitySecondaryLibrary,
                                artist,
                                item).start()
                        }
                    }
                    Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT -> {
                        adapter = SecondaryLibraryAdapter(this@ActivitySecondaryLibrary,
                            MusicLibrary.getInstance()
                                .getSongListFromAlbumIdNew(key, Constants.SORT_ORDER.ASC))
                        Collections.sort(adapter.getList(), object : Comparator<dataItem> {
                            public override fun compare(dataItem: dataItem, t1: dataItem): Int {
                                if (dataItem.trackNumber > t1.trackNumber) return 1 else if (dataItem.trackNumber < t1.trackNumber) return -1 else return 0
                            }
                        })
                    }
                    Constants.FRAGMENT_STATUS.GENRE_FRAGMENT -> {
                        adapter = SecondaryLibraryAdapter(this@ActivitySecondaryLibrary,
                            MusicLibrary.getInstance()
                                .getSongListFromGenreIdNew(key, Constants.SORT_ORDER.ASC))
                        if (adapter.getList().isEmpty()) {
                            break
                        }
                    }
                    Constants.FRAGMENT_STATUS.PLAYLIST_FRAGMENT -> {
                        val trackList: ArrayList<dataItem>
                        title = title!!.replace(" ", "_")
                        when (title) {
                            Constants.SYSTEM_PLAYLISTS.MOST_PLAYED -> {
                                trackList = PlaylistManager.getInstance(getApplicationContext())
                                    .GetPlaylist(Constants.SYSTEM_PLAYLISTS.MOST_PLAYED)
                                adapter = SecondaryLibraryAdapter(this@ActivitySecondaryLibrary,
                                    trackList,
                                    status,
                                    Constants.SYSTEM_PLAYLISTS.MOST_PLAYED)
                            }
                            Constants.SYSTEM_PLAYLISTS.MY_FAV -> {
                                trackList = PlaylistManager.getInstance(getApplicationContext())
                                    .GetPlaylist(Constants.SYSTEM_PLAYLISTS.MY_FAV)
                                adapter = SecondaryLibraryAdapter(this@ActivitySecondaryLibrary,
                                    trackList,
                                    status,
                                    Constants.SYSTEM_PLAYLISTS.MY_FAV)
                            }
                            Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED -> {
                                trackList = PlaylistManager.getInstance(getApplicationContext())
                                    .GetPlaylist(Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED)
                                adapter = SecondaryLibraryAdapter(this@ActivitySecondaryLibrary,
                                    trackList,
                                    status,
                                    Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED)
                            }
                            Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED -> {
                                trackList = PlaylistManager.getInstance(getApplicationContext())
                                    .GetPlaylist(Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED)
                                adapter = SecondaryLibraryAdapter(this@ActivitySecondaryLibrary,
                                    trackList,
                                    status,
                                    Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED)
                            }
                            else -> {
                                trackList = PlaylistManager.getInstance(getApplicationContext())
                                    .GetPlaylist(title)
                                adapter = SecondaryLibraryAdapter(this@ActivitySecondaryLibrary,
                                    trackList,
                                    status,
                                    title)
                            }
                        }
                        if (trackList.isEmpty()) {
                            handler.post(object : Runnable {
                                public override fun run() {
                                    fab.setImageDrawable(ContextCompat.getDrawable(this@ActivitySecondaryLibrary,
                                        R.drawable.ic_add_black_24dp))
                                }
                            })
                        }
                    }
                }
                handler.post(object : Runnable {
                    public override fun run() {
                        if (adapter != null) {
                            mRecyclerView.setAdapter(adapter)
                        }
                        mRecyclerView.setLayoutManager(WrapContentLinearLayoutManager(this@ActivitySecondaryLibrary))
                        mRecyclerView.setNestedScrollingEnabled(false)
                        val offsetPx: Float =
                            getResources().getDimension(R.dimen.bottom_offset_secondary_lib)
                        val bottomOffsetDecoration: BottomOffsetDecoration = BottomOffsetDecoration(
                            offsetPx.toInt())
                        mRecyclerView.addItemDecoration(bottomOffsetDecoration)
                        border!!.setVisibility(View.VISIBLE)
                        progressBar!!.setVisibility(View.INVISIBLE)
                        var item: TrackItem? = null
                        if ((adapter != null) && (adapter.getList() != null) && (adapter.getList()
                                .size() > 0)
                        ) {
                            item = MusicLibrary.getInstance()
                                .getTrackItemFromId(adapter.getList().get(0).id)
                        }
                        Log.d("SecondaryLibraryActivi", "onCreate: item " + item)
                        if (item != null) {
                            val url: String? =
                                MusicLibrary.getInstance().getArtistUrls().get(item.getArtist())
                            Log.d("SecondaryLibraryActivi", "onCreate: url " + url)
                            if (UtilityFun.isConnectedToInternet && url != null) {
                                setArtistImage(url)
                            } else {
                                val defaultAlbumArtSetting: Int = MyApp.Companion.getPref()
                                    .getInt(getString(R.string.pref_default_album_art), 0)
                                when (defaultAlbumArtSetting) {
                                    0 -> Glide.with(this@ActivitySecondaryLibrary)
                                        .load(MusicLibrary.getInstance()
                                            .getAlbumArtUri(item.albumId))
                                        .centerCrop()
                                        .placeholder(R.drawable.ic_batman_1)
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .into(mainBackdrop)
                                    1 -> Glide.with(this@ActivitySecondaryLibrary)
                                        .load(MusicLibrary.getInstance()
                                            .getAlbumArtUri(item.albumId))
                                        .centerCrop()
                                        .placeholder(UtilityFun.defaultAlbumArtDrawable)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(mainBackdrop)
                                }
                            }
                        }
                    }
                })
            }
        })
        mReceiverForMiniPLayerUpdate = object : BroadcastReceiver() {
            public override fun onReceive(context: Context, intent: Intent) {
                updateMiniplayerUI()
            }
        }
        mReceiverForDataReady = object : BroadcastReceiver() {
            public override fun onReceive(context: Context, intent: Intent) {
                //updateMiniplayerUI();
                border!!.setVisibility(View.VISIBLE)
            }
        }
        miniPlayer.setOnClickListener(this)
        buttonPlay.setOnClickListener(this)
        buttonNext.setOnClickListener(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility((
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN))
        }
        miniPlayer.setBackgroundColor(ColorHelper.getWidgetColor())
        //collapsingToolbarLayout.setContentScrimColor(ColorHelper.Ge());
        fab.setOnClickListener(View.OnClickListener({ view: View? ->
            if (status == Constants.FRAGMENT_STATUS.PLAYLIST_FRAGMENT && adapter.getItemCount() <= 2) {
                startActivity(Intent(this@ActivitySecondaryLibrary, ActivityMain::class.java)
                    .putExtra("move_to_tab", Constants.TABS.TRACKS))
            } else {
                if (adapter.getItemCount() <= 0) {
                    Toast.makeText(this@ActivitySecondaryLibrary,
                        "Empty Track List",
                        Toast.LENGTH_SHORT).show()
                } else {
                    adapter.shuffleAll()
                }
            }
        }))
        fab.setBackgroundTintList(ColorStateList.valueOf(ColorHelper.getWidgetColor()))
        collapsingToolbarLayout.setStatusBarScrim(ColorHelper.getGradientDrawable())
        setTextAndIconColor()
    }

    private fun setTextAndIconColor() {
        songNameMiniPlayer.setTextColor(ColorHelper.getPrimaryTextColor())
        artistNameMiniPlayer.setTextColor(ColorHelper.getSecondaryTextColor())
        artistBio.setTextColor(ColorHelper.getPrimaryTextColor())
        /*buttonPlay.setColorFilter(ColorHelper.getPrimaryTextColor());
        buttonNext.setColorFilter(ColorHelper.getPrimaryTextColor());*/
    }

    private fun setArtistImage(url: String) {
        Glide
            .with(getApplicationContext())
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(R.drawable.ic_batman_1)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate()
            .into(mainBackdrop)
    }

    protected override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    private fun updateMiniplayerUI() {
        try {
            if (playerService != null) {
                if (playerService.getCurrentTrack() != null) {
                    val request: RequestBuilder<Drawable> = Glide.with(this)
                        .load(MusicLibrary.getInstance()
                            .getAlbumArtFromTrack(playerService.getCurrentTrack().getId()))
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                    var builder: RequestBuilder<Drawable>? = null
                    val url: String? = MusicLibrary.getInstance().getArtistUrls()
                        .get(playerService.getCurrentTrack().getArtist())
                    if (url != null) {
                        val defaultAlbumArtSetting: Int = MyApp.Companion.getPref()
                            .getInt(getString(R.string.pref_default_album_art), 0)
                        when (defaultAlbumArtSetting) {
                            0 -> builder = Glide.with(this)
                                .load(Uri.parse(url))
                                .placeholder(R.drawable.ic_batman_1)
                            1 -> builder = Glide.with(this)
                                .load(Uri.parse(url))
                                .placeholder(UtilityFun.defaultAlbumArtDrawable)
                        }
                    }
                    request.error(builder)
                    request.into(albumArtIv)

                    //albumArtIv.setImageBitmap(playerService.getAlbumArt());
                    if (playerService.getStatus() === PlayerService.PLAYING) {
                        buttonPlay.setImageDrawable(ContextCompat.getDrawable(this,
                            R.drawable.ic_pause_black_24dp))
                    } else {
                        buttonPlay.setImageDrawable(ContextCompat.getDrawable(this,
                            R.drawable.ic_play_arrow_black_24dp))
                    }
                    songNameMiniPlayer.setText(playerService.getCurrentTrack().getTitle())
                    artistNameMiniPlayer.setText(playerService.getCurrentTrack().getArtist())
                    (findViewById<View>(R.id.app_bar_layout) as AppBarLayout).setExpanded(true)
                    //mHandler.post(getDominantColorRunnable());
                }
            } else {
                //this should not happen
                //restart app
                System.exit(0)
            }
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }
    }

    public override fun onClick(view: View) {
        if (MyApp.Companion.getService() == null) {
            UtilityFun.restartApp()
            finish()
            return
        }
        when (view.getId()) {
            R.id.mini_player -> {
                val intent: Intent = Intent(getApplicationContext(), ActivityNowPlaying::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                val options: ActivityOptions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    options = ActivityOptions.makeSceneTransitionAnimation(this,
                        albumArtIv,
                        getString(R.string.transition))
                    ActivityCompat.startActivityForResult(this,
                        intent,
                        RC_LOGIN,
                        options.toBundle())
                } else {
                    startActivity(intent)
                    overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.fade_out)
                }
                Log.v(Constants.TAG, "Launch now playing Jarvis")
            }
            R.id.play_pause_mini_player -> {
                if (playerService.getCurrentTrack() == null) {
                    Toast.makeText(this, getString(R.string.nothing_to_play), Toast.LENGTH_LONG)
                        .show()
                    return
                }
                if (SystemClock.elapsedRealtime() - mLastClickTime < 100) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                playerService.play()
                playerService.PostNotification()
                if (playerService.getStatus() === PlayerService.PLAYING) {
                    buttonPlay.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.ic_pause_black_24dp))
                } else {
                    buttonPlay.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.ic_play_arrow_black_24dp))
                }
            }
            R.id.next_mini_plaayrer -> {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 100) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                playerService.nextTrack()
                updateMiniplayerUI()
                /*
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent()
                        .setAction(Constants.ACTION.NEXT_ACTION));*/Log.v(Constants.TAG,
                    "next track please Jarvis")
            }
        }
    }

    protected override fun onStop() {
        super.onStop()
    }

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu)
        for (i in 0 until menu.size()) {
            if ((R.id.action_search == menu.getItem(i).getItemId()
                        || R.id.action_sort == menu.getItem(i).getItemId())
            ) {
                menu.getItem(i).setVisible(false)
            }
        }
        return true
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_settings -> startActivity(Intent(this,
                ActivitySettings::class.java).putExtra("ad", true))
            R.id.action_sleep_timer -> setSleepTimerDialog(this)
            R.id.action_equ -> {
                val intent: Intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                if ((MyApp.Companion.getPref()
                        .getBoolean(getString(R.string.pref_prefer_system_equ), true)
                            && (intent.resolveActivity(getPackageManager()) != null))
                ) {
                    try {
                        //show system equalizer
                        startActivityForResult(intent, 0)
                    } catch (ignored: Exception) {
                    }
                } else {
                    //show app equalizer
                    if (playerService.getEqualizerHelper().isEqualizerSupported()) {
                        startActivity(Intent(this, ActivityEqualizer::class.java))
                    } else {
                        Snackbar.make(rootView,
                            R.string.error_equ_not_supported,
                            Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setSleepTimerDialog(context: Context) {
        val builder: MyDialogBuilder = MyDialogBuilder(context)
        val linear: LinearLayout = LinearLayout(context)
        linear.setOrientation(LinearLayout.VERTICAL)
        val text: TextView = TextView(context)
        val timer: Int =
            MyApp.Companion.getPref().getInt(context.getString(R.string.pref_sleep_timer), 0)
        if (timer == 0) {
            val tempString: String =
                "0 " + context.getString(R.string.main_act_sleep_timer_status_minutes)
            text.setText(tempString)
        } else {
            val stringTemp: String =
                (context.getString(R.string.main_act_sleep_timer_status_part1) +
                        timer +
                        context.getString(R.string.main_act_sleep_timer_status_part2))
            text.setText(stringTemp)
            builder.neutralText(context.getString(R.string.main_act_sleep_timer_neu))
                .onNeutral(object : SingleButtonCallback() {
                    fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        MyApp.Companion.getPref().edit()
                            .putInt(context.getString(R.string.pref_sleep_timer), 0).apply()
                        playerService.setSleepTimer(0, false)
                        //Toast.makeText(context, "Sleep timer discarded", Toast.LENGTH_LONG).show();
                        Snackbar.make(rootView,
                            context.getString(R.string.sleep_timer_discarded),
                            Snackbar.LENGTH_SHORT).show()
                    }
                })
        }
        text.setPadding(0, 10, 0, 0)
        text.setGravity(Gravity.CENTER)
        text.setTypeface(TypeFaceHelper.getTypeFace(this))
        val seek: SeekBar = SeekBar(context)
        seek.setPadding(40, 10, 40, 10)
        seek.setMax(100)
        seek.setProgress(0)
        seek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            public override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                val tempString: String =
                    progress.toString() + context.getString(R.string.main_act_sleep_timer_status_minutes)
                text.setText(tempString)
            }

            public override fun onStartTrackingTouch(seekBar: SeekBar) {}
            public override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        linear.addView(seek)
        linear.addView(text)
        builder
            .title(context.getString(R.string.main_act_sleep_timer_title))
            .positiveText(context.getString(R.string.okay))
            .negativeText(context.getString(R.string.cancel))
            .onPositive(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    if (seek.getProgress() != 0) {
                        MyApp.Companion.getPref().edit()
                            .putInt(context.getString(R.string.pref_sleep_timer),
                                seek.getProgress()).apply()
                        playerService.setSleepTimer(seek.getProgress(), true)
                        val temp: String = (context.getString(R.string.sleep_timer_successfully_set)
                                + seek.getProgress()
                                + context.getString(R.string.main_act_sleep_timer_status_minutes))
                        //Toast.makeText(context, temp, Toast.LENGTH_LONG).show();
                        Snackbar.make(rootView, temp, Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
            .customView(linear, true)
            .show()
    }

    public override fun onDestroy() {
        mRecyclerView = null
        super.onDestroy() //get search icon back on action bar
    }

    public override fun onResume() {
        super.onResume()
        MyApp.Companion.isAppVisible = true
        if (MyApp.Companion.getService() == null) {
            UtilityFun.restartApp()
            finish()
            return
        } else {
            playerService = MyApp.Companion.getService()
        }
        if (adapter != null) {
            adapter.bindService()
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
            mReceiverForMiniPLayerUpdate,
            IntentFilter(Constants.ACTION.COMPLETE_UI_UPDATE))
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
            mReceiverForMiniPLayerUpdate,
            IntentFilter(Constants.ACTION.SECONDARY_ADAPTER_DATA_READY))
        updateMiniplayerUI()
    }

    protected override fun onPause() {
        MyApp.Companion.isAppVisible = false
        LocalBroadcastManager.getInstance(getApplicationContext())
            .unregisterReceiver(mReceiverForMiniPLayerUpdate)
        LocalBroadcastManager.getInstance(getApplicationContext())
            .unregisterReceiver(mReceiverForDataReady)
        super.onPause()
    }

    public override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    public override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY -> playerService.play()
            KeyEvent.KEYCODE_MEDIA_NEXT -> playerService.nextTrack()
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> playerService.prevTrack()
            KeyEvent.KEYCODE_MEDIA_STOP -> playerService.stop()
            KeyEvent.KEYCODE_BACK -> onBackPressed()
        }
        return false
    }

    fun onArtInfoDownloaded(artistInfo: ArtistInfo?) {
        if (artistInfo == null) return
        artistBio.setVisibility(View.VISIBLE)
        artistBio.setText(artistInfo.getArtistContent())
        setArtistImage(artistInfo.getImageUrl())
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
}