package com.music.player.bhandari.m.activity

import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.InputType
import android.transition.ArcMotion
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.material.snackbar.Snackbar
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.UIElementHelper.recyclerviewHelper.OnStartDragListener
import com.music.player.bhandari.m.UIElementHelper.recyclerviewHelper.SimpleItemTouchHelperCallback
import com.music.player.bhandari.m.adapter.CurrentTracklistAdapter
import com.music.player.bhandari.m.customViews.CustomViewPager
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.MusicLibrary
import com.music.player.bhandari.m.model.PlaylistManager
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics
import com.music.player.bhandari.m.service.PlayerService
import com.music.player.bhandari.m.trackInfo.TrackInfoActivity
import com.music.player.bhandari.m.transition.MorphMiniToNowPlaying
import com.music.player.bhandari.m.transition.MorphNowPlayingToMini
import com.music.player.bhandari.m.utils.AppLaunchCountManager
import com.music.player.bhandari.m.utils.SignUp
import com.music.player.bhandari.m.utils.UtilityFun
import com.sackcentury.shinebuttonlib.ShineButton
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import jp.wasabeef.blurry.Blurry
import java.io.File
import java.io.FileNotFoundException
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
class ActivityNowPlaying : AppCompatActivity(), View.OnClickListener, OnStartDragListener {
    var screenWidth: Int = 0
    var screenHeight: Int = 0
    private var mLastClickTime: Long = 0
    private var stopProgressRunnable: Boolean = false
    private var updateTimeTaskRunning: Boolean = false

    @JvmField @BindView(R.id.root_view_now_playing)
    var rootView: View? = null

    @JvmField @BindView(R.id.pw_ivShuffle)
    var shuffle: ImageView? = null

    @JvmField @BindView(R.id.pw_ivRepeat)
    var repeat: ImageView? = null

    @JvmField @BindView(R.id.text_in_repeat)
    var textInsideRepeat: TextView? = null

    @JvmField @BindView(R.id.seekbar_now_playing)
    var seekBar: SeekBar? = null

    @JvmField @BindView(R.id.pw_playButton)
    var mPlayButton: ImageButton? = null

    @JvmField @BindView(R.id.pw_runningTime)
    var runningTime: TextView? = null

    @JvmField @BindView(R.id.pw_totalTime)
    var totalTime: TextView? = null

    @JvmField @BindView(R.id.sliding_layout)
    var slidingUpPanelLayout: SlidingUpPanelLayout? = null

    @JvmField @BindView(R.id.view_pager_now_playing)
    var viewPager: CustomViewPager? = null

    @JvmField @BindView(R.id.shineButton)
    var shineButton: ShineButton? = null

    @JvmField @BindView(R.id.toolbar_)
    var toolbar: Toolbar? = null

    @JvmField @BindView(R.id.controls_wrapper)
    var controlsWrapper: View? = null

    //@JvmField @BindView(R.id.nowPlayingBackgroundImageOverlay) View backgroundOverlay;
    private var pref: SharedPreferences? = null

    //is artist thumb loaded in blurry background
    private var isArtistLoadedInBackground: Boolean = false
    private var viewPagerAdapter: ViewPagerAdapter? = null
    private var audioManager: AudioManager? = null
    private var isInvokedFromFileExplorer: Boolean = false

    //bind player service
    private var playerService: PlayerService? = null
    private var mUIUpdateReceiver: BroadcastReceiver? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: CurrentTracklistAdapter? = null
    private val mLayoutManager: WrapContentLinearLayoutManager =
        WrapContentLinearLayoutManager(this)
    private var mItemTouchHelper: ItemTouchHelper? = null
    private val mHandler: Handler = Handler()
    private var mGoogleApiClient: GoogleApiClient? = null

    //now playing background bitmap
    var nowPlayingCustomBackBitmap: Bitmap? = null
    private var selectedPageIndex: Int = 0

    //location of controls wrapper
    var yControl: Float = 0f
    var toolbarHeight: Float = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //if player service not running, kill the app
        if (MyApp.Companion.getService() == null) {
            UtilityFun.restartApp()
            finish()
            return
        }
        playerService = MyApp.Companion.getService()
        ColorHelper.setStatusBarGradiant(this@ActivityNowPlaying)
        val themeSelector: Int = MyApp.Companion.getPref()
            .getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        setContentView(R.layout.activity_now_playing)
        ButterKnife.bind(this)

        //backgroundOverlay.setBackgroundDrawable(ColorHelper.GetGradientDrawable());
        slidingUpPanelLayout!!.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    slidingUpPanelLayout!!.viewTreeObserver
                        .removeOnGlobalLayoutListener(this)
                    yControl = controlsWrapper!!.y
                    toolbarHeight = toolbar!!.height.toFloat()
                    Log.d("ActivityNowPlaying", "onGlobalLayout: yControl $yControl")
                    Log.d("ActivityNowPlaying", "onGlobalLayout: toolbarHeight $toolbarHeight")
                    Log.d("ActivityNowPlaying",
                        controlsWrapper!!.measuredHeight.toString() + "")
                }
            })
        if (!MyApp.getPref().getBoolean("never_show_button_again", false)) {
            val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
            mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this) { connectionResult ->
                    Log.d(Constants.TAG, "onConnectionFailed:$connectionResult")
                }
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
        }
        if (intent.action != null) {
            if ((intent.action == Constants.ACTION.OPEN_FROM_FILE_EXPLORER)) {
                isInvokedFromFileExplorer = true
            }
        } else {
            isInvokedFromFileExplorer = false
        }
        pref = MyApp.getPref()
        if (playerService != null && playerService!!.getCurrentTrack() != null) {
            toolbar!!.title = playerService!!.getCurrentTrack()!!.title
            toolbar!!.subtitle = playerService!!.getCurrentTrack()!!.getArtist()
        }
        setSupportActionBar(toolbar)
        InitializeCurrentTracklistAdapter()
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?

        // add back arrow to toolbar
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
        val playQueueHandle: View = findViewById(R.id.handle_current_queue)
        playQueueHandle.viewTreeObserver
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    playQueueHandle.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    //height is ready
                    slidingUpPanelLayout!!.panelHeight = playQueueHandle.height
                    slidingUpPanelLayout!!.setScrollableView(mRecyclerView)
                }
            })
        slidingUpPanelLayout!!.addPanelSlideListener(object :
            SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                if (slideOffset > 0.99) {
                    playQueueHandle.visibility = View.INVISIBLE
                } else {
                    playQueueHandle.visibility = View.VISIBLE
                }
            }

            override fun onPanelStateChanged(
                panel: View,
                previousState: SlidingUpPanelLayout.PanelState,
                newState: SlidingUpPanelLayout.PanelState
            ) {
                if (previousState == SlidingUpPanelLayout.PanelState.COLLAPSED && newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
                    try {
                        val position: Int = playerService!!.getCurrentTrackPosition()
                        mRecyclerView!!.scrollToPosition(position)
                    } catch (ignored: Exception) {
                    }
                    //Log.v(Constants.TAG,"DRAGGING");
                }
            }
        })
        slidingUpPanelLayout!!.setDragView(R.id.play_queue_title)
        shineButton!!.init(this)
        val saveQueueButton: View = findViewById(R.id.save_queue_button)
        saveQueueButton.setOnClickListener(this)
        Log.v(Constants.TAG,
            audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toString() + "VOLUME")
        mUIUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.v(Constants.TAG, "update UI__ please Jarvis")
                UpdateUI(intent)
            }
        }
        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                Log.v(Constants.L_TAG + "wow", "selected $position")
                selectedPageIndex = position
                //display disclaimer if not accepted already
                if (position == 2 && !MyApp.getPref()
                        .getBoolean(getString(R.string.pref_disclaimer_accepted), false)) {
                    showDisclaimerDialog()
                }

                //2 lyrics fragment
                if (position == 2 && playerService!!.getStatus() === playerService!!.PLAYING) {
                    acquireWindowPowerLock(true)
                } else {
                    acquireWindowPowerLock(false)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        viewPager!!.offscreenPageLimit = 2
        setupViewPager(viewPager!!)
        //set current item to disc
        viewPager!!.setCurrentItem(Constants.EXIT_NOW_PLAYING_AT.DISC_FRAG, true)

        //display current play queue header
        if (playerService != null) {
            if (playerService!!.getTrackList().isNotEmpty()) {
                val title = "Save Playlist"
                (findViewById<View>(R.id.save_queue_button) as TextView).text = title
            }
        }
        if (!MyApp.getPref().getBoolean(getString(R.string.pref_swipe_right_shown), false)
        ) {
            showInfoDialog()
        }
        InitializeControlsUI()
        setupSharedElementTransitions()
    }

    private fun setupSharedElementTransitions() {
        val arcMotion = ArcMotion()
        arcMotion.minimumHorizontalAngle = 50f
        arcMotion.minimumVerticalAngle = 50f
        val easeInOut: Interpolator =
            AnimationUtils.loadInterpolator(this, R.interpolator.fast_out_slow_in)
        val sharedEnter = MorphMiniToNowPlaying()
        sharedEnter.pathMotion = arcMotion
        sharedEnter.interpolator = easeInOut
        val sharedExit = MorphNowPlayingToMini()
        sharedExit.pathMotion = arcMotion
        sharedExit.interpolator = easeInOut

        /*if (second_card != null) {
            sharedEnter.addTarget(second_card)
            sharedReturn.addTarget(second_card)
        }*/window.sharedElementEnterTransition = sharedEnter
        window.sharedElementExitTransition = sharedExit
        postponeEnterTransition()
        //getWindow().sharedElementEnterTransition = sharedEnter
        //getWindow().sharedElementReturnTransition = sharedReturn
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    private fun acquireWindowPowerLock(acquire: Boolean) {
        /*if(acquire) {
            if (mWakeLock != null && !mWakeLock.isHeld()) {
                this.mWakeLock.acquire(10*60*1000L); //10 minutes
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

    private fun showDisclaimerDialog() {
//        val dialog: MaterialDialog = MyDialogBuilder(this)
//            .title(getString(R.string.lyrics_disclaimer_title))
//            .content(getString(R.string.lyrics_disclaimer_content))
//            .positiveText(getString(R.string.lyrics_disclaimer_title_pos))
//            .negativeText(getString(R.string.lyrics_disclaimer_title_neg))
//            .onPositive(object : SingleButtonCallback() {
//                fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                    MyApp.Companion.getPref().edit()
//                        .putBoolean(getString(R.string.pref_disclaimer_accepted), true).apply()
//                    (viewPagerAdapter!!.getItem(2) as FragmentLyrics).disclaimerAccepted()
//                }
//            })
//            .build()
//
//        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//        dialog.show()
    }

    private fun showInfoDialog() {
//        val dialog: MaterialDialog = MyDialogBuilder(this)
//            .title(getString(R.string.lyric_art_info_title))
//            .content(getString(R.string.lyric_art_info_content))
//            .positiveText(getString(R.string.lyric_art_info_title_button_neg))
//            .negativeText(getString(R.string.lyric_art_info_button_p))
//            .onPositive(object : SingleButtonCallback() {
//                fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                    MyApp.Companion.getPref().edit()
//                        .putBoolean(getString(R.string.pref_swipe_right_shown), true).apply()
//                }
//            })
//            .build()
//
//        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//        dialog.show()
    }

    private fun setupViewPager(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        val artistInfo: FragmentArtistInfo = FragmentArtistInfo()
        viewPagerAdapter!!.addFragment(artistInfo, "Artist Bio")
        val fragmentAlbumArt: FragmentAlbumArt = FragmentAlbumArt()
        viewPagerAdapter!!.addFragment(fragmentAlbumArt, "Disc")
        val fragmentLyric: FragmentLyrics = FragmentLyrics()
        viewPagerAdapter!!.addFragment(fragmentLyric, "Lyrics")
        viewPager.adapter = viewPagerAdapter!!
    }

    fun InitializeCurrentTracklistAdapter() {
        mRecyclerView = findViewById<RecyclerView>(R.id.recyclerViewForCurrentTracklist)
        mAdapter = CurrentTracklistAdapter(this, this)
        mRecyclerView!!.adapter = mAdapter!!
        mRecyclerView!!.layoutManager = mLayoutManager
        val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(mAdapter!!)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper!!.attachToRecyclerView(mRecyclerView)
        val itemDecor = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        mRecyclerView!!.addItemDecoration(itemDecor)
    }

    fun UpdateCurrentTracklistAdapter() {
        if (mRecyclerView == null || mAdapter == null) {
            return
        }
        mAdapter!!.fillData()
    }

    override fun onDestroy() {
        Log.v(Constants.TAG, "DESTORY NOW PLAYING")
        //this removes any memory leak caused by handler
        mHandler.removeCallbacksAndMessages(null)

        //save exit status so than we can open corresponding frag next time
        /*switch (viewPager.getCurrentItem()){
            case 2:
                MyApp.getPref().edit()
                        .putInt(getString(R.string.pref_exit_now_playing_at),Constants.EXIT_NOW_PLAYING_AT.LYRICS_FRAG).apply();
                break;

            case 0:
                MyApp.getPref().edit()
                        .putInt(getString(R.string.pref_exit_now_playing_at),Constants.EXIT_NOW_PLAYING_AT.ARTIST_FRAG).apply();
                break;

            case 1:
            default:
                MyApp.getPref().edit()
                        .putInt(getString(R.string.pref_exit_now_playing_at),Constants.EXIT_NOW_PLAYING_AT.DISC_FRAG).apply();
                break;
        }*/

        /*if(mWakeLock!=null && mWakeLock.isHeld()){
            mWakeLock.release();
        }*/super.onDestroy()
    }

    private fun UpdateUI(receivedIntent: Intent?) {  //intent carries information if only particular item needs to be updated in adapter
        Log.d("ActivityNowPlaying", "UpdateUI: " + Log.getStackTraceString(Exception()))
        if (playerService != null) {
            val item: TrackItem? = playerService!!.getCurrentTrack()
            if (mAdapter != null) {
                if (receivedIntent == null || !receivedIntent.getBooleanExtra("skip_adapter_update",
                        false)
                ) {
                    mAdapter!!.notifyDataSetChanged()
                }
            }
            invalidateOptionsMenu()
            if (item != null) {
                val intent: Intent = Intent().setAction(Constants.ACTION.UPDATE_LYRIC_AND_INFO)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                Log.v(Constants.TAG, "Intent sent! " + intent.action)
                if (playerService!!.getStatus() === playerService!!.PLAYING) {
                    mPlayButton!!.setImageDrawable(resources.getDrawable(R.drawable.pw_pause))
                } else {
                    mPlayButton!!.setImageDrawable(resources.getDrawable(R.drawable.pw_play))
                }
                totalTime!!.text = UtilityFun.msToString(playerService!!.getCurrentTrackDuration().toLong())

                //update disc
                updateDisc()

                //check current now playing background setting
                ///get current setting
                // 0 - System default   1 - artist image 2 - album art 3 - custom
                val currentNowPlayingBackPref: Int =
                    MyApp.getPref().getInt(getString(R.string.pref_now_playing_back), 1)
                var b: Bitmap? = null // = playerService!!.getAlbumArt();
                try {
                    when (currentNowPlayingBackPref) {
                        0 -> {}
                        1 -> {
                            //look in cache for artist image
                            val CACHE_ART_THUMBS: String =
                                this.cacheDir.toString() + "/art_thumbs/"
                            val actual_file_path: String =
                                CACHE_ART_THUMBS + playerService!!.getCurrentTrack()!!.getArtist()
                            b = BitmapFactory.decodeFile(actual_file_path)
                            isArtistLoadedInBackground = b != null
                            Log.d(Constants.TAG, "UpdateUI: settingArtistImageBackground")
                        }
                        2 -> b = MusicLibrary.instance.getAlbumArtFromId(item.id)
                        3 -> b = getNowPlayingBackBitmap()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (b != null) {
                    var width: Int = b.width
                    var height: Int = b.height
                    val maxWidth: Int = screenWidth
                    val maxHeight: Int = screenHeight
                    when {
                        width > height -> {
                            // landscape
                            val ratio: Float = width.toFloat() / maxWidth
                            width = maxWidth
                            height = (height / ratio).toInt()
                        }
                        height > width -> {
                            // portrait
                            val ratio: Float = height.toFloat() / maxHeight
                            height = maxHeight
                            width = (width / ratio).toInt()
                        }
                        else -> {
                            // square
                            if (maxHeight < height) {
                                height = maxHeight
                                width = maxWidth
                            }
                        }
                    }
                    b = Bitmap.createScaledBitmap(b, width, height, false)
                    setBlurryBackground(b)
                } /*else {
                    b = BitmapFactory.decodeResource(getResources(),R.drawable.now_playing_back);
                    setBlurryBackground(b);
                }*/
                toolbar!!.title = playerService!!.getCurrentTrack()!!.title
                toolbar!!.subtitle = playerService!!.getCurrentTrack()!!.getArtist()
            }
        } else {
            UtilityFun.restartApp()
            finish()
        }
    }

    private fun getNowPlayingBackBitmap(): Bitmap? {
        if (nowPlayingCustomBackBitmap != null) {
            return nowPlayingCustomBackBitmap
        }
        val picPath: String = MyApp.Companion.getContext().filesDir
            .toString() + getString(R.string.now_playing_back_custom_image)
        Log.d(Constants.TAG, "UpdateUI: setBlurryBackgroundCustomImage: $picPath")
        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        nowPlayingCustomBackBitmap = BitmapFactory.decodeFile(picPath, options);
*/try {
            nowPlayingCustomBackBitmap =
                UtilityFun.decodeUri(this, Uri.fromFile(File(picPath)), 500)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return nowPlayingCustomBackBitmap
    }

    fun setBlurryBackground(b: Bitmap?) {
        val fadeIn: Animation = AnimationUtils.loadAnimation(this@ActivityNowPlaying, R.anim.fade_in)
        fadeIn.duration = 2000
        findViewById<View>(R.id.full_screen_iv).startAnimation(fadeIn)
        try {
            /*int currentNowPlayingBackPref = MyApp.getPref().getInt(getString(R.string.pref_now_playing_back),1);
            if(currentNowPlayingBackPref==2){
                //do not blur if user has selected album art as now playing option
                //very ugly fix, but we gotta do what we gotta do.
                //fix on user request
                Glide.with(this).load(b).asBitmap().into(((ImageView) findViewById(R.id.full_screen_iv)));
                */
            /*Blurry.with(this).radius(0).from(b)
                        .into(((ImageView) findViewById(R.id.full_screen_iv)));*/
            /*
            }else {*/
            Blurry.with(this).radius(1).color(Color.argb(100, 50, 0, 0)).from(b)
                .into((findViewById<View>(R.id.full_screen_iv) as ImageView?))
            /*}*/
        } catch (e: OutOfMemoryError) {
            Toast.makeText(playerService,
                "Error setting blurry background due to insufficient memory",
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        MyApp.Companion.isAppVisible = false
        Log.v(Constants.TAG, "PAUSE NOW PLAYING")
        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(mUIUpdateReceiver!!)
        stopUpdateTask()
        stopProgressRunnable = true
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        MyApp.Companion.isAppVisible = true
        if (MyApp.Companion.getService() == null) {
            UtilityFun.restartApp()
            return
        } else {
            playerService = MyApp.Companion.getService()
        }
        UpdateUI(null)
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(mUIUpdateReceiver!!, IntentFilter(Constants.ACTION.COMPLETE_UI_UPDATE))
        AppLaunchCountManager.nowPlayingLaunched()
        //UpdateCurrentTracklistAdapter();
        setSeekbarAndTime()
        startUpdateTask()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_now_plying, menu)
        for (i in 0 until menu.size()) {
            if (menu.getItem(i).itemId == R.id.action_fav) {
                //Drawable drawable = menu.getItem(i).getIcon();
                //if (drawable != null) {
                val item: TrackItem? = playerService!!.getCurrentTrack()
                when {
                    item != null && PlaylistManager.getInstance(applicationContext)!!.isFavNew(item.id) -> {
                        //rawable.mutate();
                        //drawable.setColorFilter(ColorHelper.GetWidgetColor(), PorterDuff.Mode.SRC_ATOP);
                        menu.getItem(i).icon = resources.getDrawable(R.drawable.ic_favorite_black_24dp)
                    }
                    else -> {
                        //drawable.mutate();
                        //drawable.setColorFilter(ColorHelper.getColor(R.color.colorwhite), PorterDuff.Mode.SRC_ATOP);
                        menu.getItem(i).icon = resources.getDrawable(R.drawable.ic_favorite_border_black_24dp)
                    }
                }
                //}
            }
        }
        return true
    }

    override fun onBackPressed() {
        //
        if (slidingUpPanelLayout!!.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout!!.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            return
        }
        if (isInvokedFromFileExplorer) {
            finish()
            return
        }
        if (isTaskRoot) {
            startActivity(Intent(this, ActivityMain::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_right)
            //finish();
            //return;
        }
        finishAfterTransition()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val b: Boolean = intent.getBooleanExtra("refresh", false)
        if (b) {
            val position: Int = intent.getIntExtra("position", -1)
            val title: String = intent.getStringExtra("title")!!
            val artist: String = intent.getStringExtra("artist")!!
            val album: String = intent.getStringExtra("album")!!
            if (playerService != null) {
                playerService!!.updateTrackItem(position,
                    playerService!!.getCurrentTrack()!!.id,
                    title,
                    artist,
                    album)
                playerService!!.PostNotification()

                //update currenttracklistadapteritem
                mAdapter!!.updateItem(position, title, artist, album)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val trackItem: TrackItem? = playerService!!.getCurrentTrack()
        when (item.itemId) {
            R.id.action_fav -> {
                if (playerService!!.getCurrentTrack() == null) {
                    Snackbar.make(rootView!!,
                        getString(R.string.error_nothing_to_fav),
                        Snackbar.LENGTH_SHORT).show()
                    return true
                }
                if (PlaylistManager.getInstance(applicationContext)!!.isFavNew(playerService!!.getCurrentTrack()!!.id)) {
                    PlaylistManager.getInstance(applicationContext)!!.RemoveFromFavNew(playerService!!.getCurrentTrack()!!.id)
                } else {
                    PlaylistManager.getInstance(applicationContext)!!.addSongToFav(playerService!!.getCurrentTrack()!!.id)
                    shineButton!!.visibility = View.VISIBLE
                    shineButton!!.showAnim()
                    shineButton!!.clearAnimation()
                }
                invalidateOptionsMenu()
            }
            R.id.action_equ -> {
                val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                if ((MyApp.getPref().getBoolean(getString(R.string.pref_prefer_system_equ), true)
                            && (intent.resolveActivity(packageManager) != null))
                ) {
                    try {
                        //show system equalizer
                        startActivityForResult(intent, 0)
                    } catch (ignored: Exception) {
                    }
                } else {
                    //show app equalizer
                    if (playerService!!.getEqualizerHelper().isEqualizerSupported()) {
                        startActivity(Intent(this, ActivityEqualizer::class.java))
                    } else {
                        Snackbar.make(rootView!!,
                            R.string.error_equ_not_supported,
                            Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            R.id.action_track_info -> if (trackItem != null) {
                startActivity(Intent(this, TrackInfoActivity::class.java).putExtra("trackItem",
                    trackItem))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.home -> {
                if (isTaskRoot) {
                    startActivity(Intent(this, ActivityMain::class.java))
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_right)
                    //finish();
                }
                finishAfterTransition()
            }
            R.id.action_settings -> {
                //finish();
                startActivity(Intent(this, ActivitySettings::class.java)
                    .putExtra("launchedFrom", Constants.PREF_LAUNCHED_FROM.NOW_PLAYING)
                    .putExtra("ad", true))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.action_go_to_artist -> if (trackItem != null) {
                val art_intent: Intent = Intent(this, ActivitySecondaryLibrary::class.java)
                art_intent.putExtra("status", Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT)
                art_intent.putExtra("key", trackItem.artist_id)
                art_intent.putExtra("title", trackItem.getArtist()!!.trim { it <= ' ' })
                startActivity(art_intent)
            } else {
                Snackbar.make(rootView!!, getString(R.string.no_music_found), Snackbar.LENGTH_SHORT)
                    .show()
            }
            R.id.action_go_to_album -> if (trackItem != null) {
                val alb_intent = Intent(this, ActivitySecondaryLibrary::class.java)
                alb_intent.putExtra("status", Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT)
                alb_intent.putExtra("key", trackItem.albumId)
                alb_intent.putExtra("title", trackItem.album!!.trim { it <= ' ' })
                startActivity(alb_intent)
            } else {
                Snackbar.make(rootView!!, getString(R.string.no_music_found), Snackbar.LENGTH_SHORT)
                    .show()
            }
            R.id.action_share -> try {
                if (trackItem != null) {
                    val fileToBeShared: File = File(trackItem.getFilePath())
                    val fileUris: ArrayList<Uri> = ArrayList<Uri>()
                    fileUris.add(FileProvider.getUriForFile(this,
                        applicationContext.packageName + "com.bhandari.music.provider",
                        fileToBeShared))
                    UtilityFun.Share(this, fileUris, trackItem.title)
                } else {
                    Snackbar.make(rootView!!, R.string.error_nothing_to_share, Snackbar.LENGTH_SHORT)
                        .show()
                }
            } catch (e: IllegalArgumentException) {
                try {
                    UtilityFun.ShareFromPath(this, trackItem!!.getFilePath())
                } catch (ex: Exception) {
                    Snackbar.make(rootView!!, R.string.error_unable_to_share, Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
            R.id.action_add_to_playlist ->                 //Toast.makeText(context,"Playlists coming soon" ,Toast.LENGTH_SHORT).show();
                if (trackItem != null) {
                    AddToPlaylist()
                } else {
                    Snackbar.make(rootView!!,
                        getString(R.string.no_music_found),
                        Snackbar.LENGTH_SHORT).show()
                }
            R.id.action_sleep_timer -> setSleepTimerDialog(this)
            R.id.action_edit_track_info -> {
                if (trackItem != null) {
                    startActivity(Intent(this, ActivityTagEditor::class.java)
                        .putExtra("from", Constants.TAG_EDITOR_LAUNCHED_FROM.NOW_PLAYING)
                        .putExtra("file_path", trackItem.getFilePath())
                        .putExtra("track_title", trackItem.title)
                        .putExtra("position", playerService!!.getCurrentTrackPosition())
                        .putExtra("id", trackItem.id))
                } else {
                    Snackbar.make(rootView!!,
                        getString(R.string.no_music_found),
                        Snackbar.LENGTH_SHORT).show()
                }
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.action_clear_lyrics_offline -> if (trackItem != null) {
                if (OfflineStorageLyrics.clearLyricsFromDB(trackItem)) {
                    (viewPagerAdapter!!.getItem(2) as FragmentLyrics).clearLyrics()
                } else {
                    //Toast.makeText(this, "Unable to delete lyrics!", Toast.LENGTH_SHORT).show();
                    Snackbar.make(rootView!!,
                        getString(R.string.error_no_lyrics),
                        Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(rootView!!, getString(R.string.error_no_lyrics), Snackbar.LENGTH_SHORT)
                    .show()
            }
            R.id.action_share_lyrics_offline -> if (trackItem != null) {
                (viewPagerAdapter!!.getItem(2) as FragmentLyrics).shareLyrics()
            } else {
                Snackbar.make(rootView!!, getString(R.string.error_no_lyrics), Snackbar.LENGTH_SHORT)
                    .show()
            }
            R.id.action_wrong_lyrics -> if (trackItem != null) {
                (viewPagerAdapter!!.getItem(2) as FragmentLyrics).wrongLyrics()
            } else {
                Snackbar.make(rootView!!, getString(R.string.error_no_lyrics), Snackbar.LENGTH_SHORT)
                    .show()
            }
            R.id.action_add_lyrics -> if (trackItem != null) {
                showAddLyricDialog()
            }
            R.id.action_search_youtube -> if (playerService!!.getCurrentTrack() != null) {
                UtilityFun.LaunchYoutube(this, trackItem!!.getArtist() + " - " + trackItem.title)
            }
            R.id.action_set_as_ringtone -> if (trackItem != null) {
                val abPath: String = trackItem.getFilePath()
                UtilityFun.SetRingtone(this,
                    abPath,
                    MusicLibrary.instance.getIdFromFilePath(abPath))
            } else {
                Snackbar.make(rootView!!,
                    getString(R.string.main_act_empty_lib),
                    Snackbar.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAddLyricDialog() {
        val item: TrackItem = playerService!!.getCurrentTrack()!!
        val hintText: String = getString(R.string.dialog_add_lyric_hint, item.title)
//        val dialog: MaterialDialog = MyDialogBuilder(this)
//            .title(R.string.action_add_lyrics)
//            .input(hintText, "", false, object : InputCallback() {
//                fun onInput(dialog: MaterialDialog, input: CharSequence) {
//                    Log.d("ActivityNowPlaying", "onInput: " + input)
//                }
//            })
//            .positiveText(R.string.add)
//            .negativeText(R.string.cancel)
//            .onPositive(object : SingleButtonCallback() {
//                fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                    val lyrics: String = dialog.getInputEditText().getText().toString()
//                    val result: Lyrics = Lyrics(POSITIVE_RESULT)
//                    result.setTitle(item.title)
//                    result.setArtist(item.getArtist())
//                    result.setOriginalArtist(item.getArtist())
//                    result.setOriginalTitle(item.title)
//                    result.setSource("manual")
//                    result.setText(lyrics.replace("\n", "<br />"))
//                    OfflineStorageLyrics.clearLyricsFromDB(item)
//                    OfflineStorageLyrics.putLyricsInDB(result, item)
//                    (viewPagerAdapter!!.getItem(2) as FragmentLyrics).onLyricsDownloaded(result)
//                    Snackbar.make(rootView, "Lyrics added", Snackbar.LENGTH_SHORT).show()
//                }
//            })
//            .build()
//        val texInput: EditText? = dialog.getInputEditText()
//        assert(texInput != null)
//        texInput.setSingleLine(false)
//        texInput.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION)
//        texInput.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
//        texInput.setLines(5)
//        texInput.setMaxLines(10)
//        texInput.setVerticalScrollBarEnabled(true)
//        texInput.setMovementMethod(ScrollingMovementMethod.getInstance())
//        texInput.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET)
//        texInput.setGravity(Gravity.TOP or Gravity.START)
//        dialog.show()
    }

    private fun AddToPlaylist() {
        val ids: IntArray
        val trackItem: TrackItem = playerService!!.getCurrentTrack()!!
        ids = intArrayOf(trackItem.id)
        UtilityFun.AddToPlaylist(this, ids)
        invalidateOptionsMenu()
    }

    override fun onClick(view: View) {
        if (MyApp.Companion.getService() == null) {
            UtilityFun.restartApp()
            return
        }
        when (view.id) {
            R.id.save_queue_button -> {
                if (mAdapter!!.itemCount === 0) {
                    return
                }
                val input: EditText = EditText(this)
                input.inputType = InputType.TYPE_CLASS_TEXT
//                val dialog: MaterialDialog = MyDialogBuilder(this)
//                    .title(getString(R.string.main_act_create_play_list_title))
//                    .positiveText(getString(R.string.okay))
//                    .negativeText(getString(R.string.cancel))
//                    .onPositive(object : SingleButtonCallback() {
//                        fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                            val playlist_name: String =
//                                input.getText().toString().trim({ it <= ' ' })
//                            if (ValidatePlaylistName(playlist_name)) {
//                                if (PlaylistManager.getInstance(MyApp.Companion.getContext())
//                                        .CreatePlaylist(playlist_name)
//                                ) {
//                                    val ids: IntArray = IntArray(mAdapter.getSongList().size())
//                                    var i: Int = 0
//                                    while (i < ids.size) {
//                                        ids.get(i) = mAdapter.getSongList().get(i)
//                                        i++
//                                    }
//                                    PlaylistManager.getInstance(MyApp.Companion.getContext())
//                                        .AddSongToPlaylist(playlist_name, ids)
//                                    // Toast.makeText(ActivityNowPlaying.this, "Playlist saved!", Toast.LENGTH_SHORT).show();
//                                    Snackbar.make(rootView,
//                                        getString(R.string.playlist_saved),
//                                        Snackbar.LENGTH_SHORT).show()
//                                } else {
//                                    //Toast.makeText(ActivityNowPlaying.this, "Playlist already exists", Toast.LENGTH_SHORT).show();
//                                    Snackbar.make(rootView,
//                                        getString(R.string.play_list_already_exists),
//                                        Snackbar.LENGTH_SHORT).show()
//                                }
//                            }
//                        }
//                    })
//                    .customView(input, true)
//                    .build()
//
//                //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//                dialog.show()
            }
        }
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        Log.d("ActivityNowPlaying", "onStartDrag: ")
        mItemTouchHelper!!.startDrag(viewHolder!!)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        //super.onKeyDown(keyCode,event);
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY -> {
                playerService!!.play()
                updateDisc()
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                playerService!!.nextTrack()
                UpdateUI(null)
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                playerService!!.prevTrack()
                UpdateUI(null)
            }
            KeyEvent.KEYCODE_MEDIA_STOP -> {
                playerService!!.stop()
                UpdateUI(null)
            }
            KeyEvent.KEYCODE_BACK -> onBackPressed()
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_MUTE -> {
                super.onKeyDown(keyCode, event)
                Log.v(Constants.TAG,
                    keyCode.toString() + " v " + audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC))
            }
        }
        return false
    }

    private fun updateDisc() {
        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(Intent(Constants.ACTION.DISC_UPDATE))
        (viewPagerAdapter!!.getItem(2) as FragmentLyrics).runLyricThread()
        if (viewPager!!.currentItem == 2 && playerService!!.getStatus() === playerService!!.PLAYING) {
            acquireWindowPowerLock(true)
        } else {
            acquireWindowPowerLock(false)
        }
    }

    fun isArtistLoadedInBack(): Boolean {
        return isArtistLoadedInBackground
    }

    fun setSleepTimerDialog(context: Context) {
//        val builder: MyDialogBuilder = MyDialogBuilder(context)
//        val linear: LinearLayout = LinearLayout(context)
//        linear.orientation = LinearLayout.VERTICAL
//        val text: TextView = TextView(context)
//        val timer: Int =
//            MyApp.Companion.getPref().getInt(context.getString(R.string.pref_sleep_timer), 0)
//        if (timer == 0) {
//            text.text = "0" + getString(R.string.main_act_sleep_timer_status_minutes)
//        } else {
//            val stringTemp: String =
//                (context.getString(R.string.main_act_sleep_timer_status_part1) +
//                        timer +
//                        context.getString(R.string.main_act_sleep_timer_status_part2))
//            text.text = stringTemp
//            builder.neutralText(getString(R.string.main_act_sleep_timer_neu))
//                .onNeutral(object : SingleButtonCallback() {
//                    fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                        MyApp.Companion.getPref().edit()
//                            .putInt(context.getString(R.string.pref_sleep_timer), 0).apply()
//                        playerService!!.setSleepTimer(0, false)
//                        // Toast.makeText(context, "Sleep timer discarded", Toast.LENGTH_LONG).show();
//                        Snackbar.make(rootView,
//                            getString(R.string.sleep_timer_discarded),
//                            Snackbar.LENGTH_SHORT).show()
//                    }
//                })
//        }
//        text.setPadding(0, 10, 0, 0)
//        text.gravity = Gravity.CENTER
//        text.setTypeface(TypeFaceHelper.getTypeFace(this))
//        val seek: SeekBar = SeekBar(context)
//        seek.setPadding(40, 10, 40, 10)
//        seek.max = 100
//        seek.progress = 0
//        seek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
//            override fun onProgressChanged(
//                seekBar: SeekBar,
//                progress: Int,
//                fromUser: Boolean
//            ) {
//                val tempString: String =
//                    progress.toString() + context.getString(R.string.main_act_sleep_timer_status_minutes)
//                text.text = tempString
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar) {}
//            override fun onStopTrackingTouch(seekBar: SeekBar) {
//                val progress: Int = seekBar.progress
//            }
//        })
//        linear.addView(seek)
//        linear.addView(text)
//        val dialog: MaterialDialog = builder
//            .title(context.getString(R.string.main_act_sleep_timer_title))
//            .positiveText(getString(R.string.okay))
//            .negativeText(getString(R.string.cancel))
//            .onPositive(object : SingleButtonCallback() {
//                fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                    if (seek.progress != 0) {
//                        MyApp.Companion.getPref().edit()
//                            .putInt(context.getString(R.string.pref_sleep_timer),
//                                seek.progress).apply()
//                        playerService!!.setSleepTimer(seek.progress, true)
//                        playerService!!.setSleepTimer(seek.progress, true)
//                        val temp: String = (getString(R.string.sleep_timer_successfully_set)
//                                + seek.progress
//                                + getString(R.string.main_act_sleep_timer_status_minutes))
//                        Snackbar.make(rootView, temp, Snackbar.LENGTH_SHORT).show()
//                    }
//                }
//            })
//            .customView(linear, true)
//            .build()
//
//        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
        //dialog.show()
    }

    private fun ValidatePlaylistName(playlist_name: String): Boolean {
        val pattern: String = "^[a-zA-Z0-9 ]*$"
        if (playlist_name.matches(pattern.toRegex())) {
            if (playlist_name.length > 2) {
                //if playlist starts with digit, not allowed
                if (Character.isDigit(playlist_name[0])) {
                    Snackbar.make(rootView!!,
                        getString(R.string.playlist_error_1),
                        Snackbar.LENGTH_SHORT).show()
                    return false
                }
                return true
            } else {
                //Toast.makeText(this,"Enter at least 3 characters",Toast.LENGTH_SHORT).show();
                Snackbar.make(rootView!!, getString(R.string.playlist_error_2), Snackbar.LENGTH_SHORT)
                    .show()
                return false
            }
        } else {
            //Toast.makeText(this,"Only alphanumeric characters allowed",Toast.LENGTH_SHORT).show();
            Snackbar.make(rootView!!, getString(R.string.playlist_error_3), Snackbar.LENGTH_SHORT)
                .show()
            return false
        }
    }

    fun signIn() {
        if (mGoogleApiClient == null) {
            return
        }
        val signInIntent: Intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient!!)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    //for catching exception generated by recycler view which was causing abend, no other way to handle this
    private inner class WrapContentLinearLayoutManager(context: Context?) :
        LinearLayoutManager(context) {
        //... constructor
        override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
            try {
                super.onLayoutChildren(recycler, state)
            } catch (e: IndexOutOfBoundsException) {
                Log.e("probe", "meet a IOOBE in RecyclerView")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)!!
            handleSignInResult(result)
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        Log.d(Constants.TAG, "handleSignInResult:" + result.isSuccess)
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.

            //permanently hide  sign in button on now playing activity
            MyApp.getPref().edit().putBoolean("never_show_button_again", true).apply()
            MyApp.hasUserSignedIn = true
            val acct: GoogleSignInAccount = result.signInAccount ?: return

            //sign up user to tech guru newsletter
            val email = acct.email
            val name = acct.givenName

            //store this email id and time of first sign in
            if (email != null) {
                SignUp().execute(email, name)
            }
        } else {
            // some Error or user logged out, either case, update the drawer and give user appropriate info
            MyApp.hasUserSignedIn = false
            if (result.status.statusCode == CommonStatusCodes.NETWORK_ERROR) {
                Snackbar.make(rootView!!, getString(R.string.network_error), Snackbar.LENGTH_SHORT)
                    .show()
            } else {
                Snackbar.make(rootView!!, getString(R.string.unknown_error), Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun shareApp() {
        try {
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            var sAux: String = getString(R.string.main_act_share_app_text)
            sAux = sAux + getString(R.string.share_app) + " \n\n"
            i.putExtra(Intent.EXTRA_TEXT, sAux)
            startActivity(Intent.createChooser(i, getString(R.string.main_act_share_app_choose)))
        } catch (e: Exception) {
            //e.toString();
        }
    }

    private fun feedbackEmail() {
        val myDeviceModel: String = Build.MODEL
        val emailIntent: Intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
            "mailto", getString(R.string.au_email_id), null))
        val address: Array<String> = arrayOf(getString(R.string.au_email_id))
        emailIntent.putExtra(Intent.EXTRA_EMAIL, address)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for $myDeviceModel")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello AndroidDevs, \n\n")
        startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
    }

    private fun InitializeControlsUI() {
        if (!pref!!.getBoolean(Constants.PREFERENCES.SHUFFLE, false)) {
            shuffle!!.setColorFilter(ColorHelper.getColor(R.color.dark_gray3))
        } else {
            shuffle!!.setColorFilter(ColorHelper.getColor(R.color.colorwhite))
        }
        when {
            pref!!.getInt(Constants.PREFERENCES.REPEAT,
                0) == Constants.PREFERENCE_VALUES.REPEAT_ALL -> {
                textInsideRepeat!!.setTextColor(ColorHelper.getColor(R.color.colorwhite))
                repeat!!.setColorFilter(ColorHelper.getColor(R.color.colorwhite))
                textInsideRepeat!!.text = "A"
            }
            pref!!.getInt(Constants.PREFERENCES.REPEAT,
                0) == Constants.PREFERENCE_VALUES.REPEAT_ONE -> {
                textInsideRepeat!!.setTextColor(ColorHelper.getColor(R.color.colorwhite))
                repeat!!.setColorFilter(ColorHelper.getColor(R.color.colorwhite))
                textInsideRepeat!!.text = "1"
            }
            pref!!.getInt(Constants.PREFERENCES.REPEAT,
                0) == Constants.PREFERENCE_VALUES.NO_REPEAT -> {
                textInsideRepeat!!.setTextColor(ColorHelper.getColor(R.color.dark_gray3))
                repeat!!.setColorFilter(ColorHelper.getColor(R.color.dark_gray3))
                textInsideRepeat!!.text = ""
            }
        }
        if (playerService!!.getStatus() === playerService!!.PLAYING) {
            mPlayButton!!.setImageDrawable(resources.getDrawable(R.drawable.pw_pause))
        } else {
            mPlayButton!!.setImageDrawable(resources.getDrawable(R.drawable.pw_play))
        }

        //mPlayButton.setBackgroundTintList(ColorStateList.valueOf(ColorHelper.GetWidgetColor()));
        seekBar!!.max = 100
        seekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    runningTime!!.text = UtilityFun.msToString(
                        UtilityFun.progressToTimer(seekBar.progress,
                            playerService!!.getCurrentTrackDuration()).toLong())
                    if (selectedPageIndex == 2) {
                        (viewPagerAdapter!!.getItem(2) as FragmentLyrics).smoothScrollAfterSeekbarTouched(
                            seekBar.progress)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                stopUpdateTask()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                playerService!!.seekTrack(UtilityFun.progressToTimer(seekBar.progress,
                    playerService!!.getCurrentTrackDuration()))
                startUpdateTask()
            }
        })
    }

    @OnClick(R.id.pw_ivShuffle)
    fun shuffle() {
        if (playerService!!.getCurrentTrack() == null) {
            Toast.makeText(this, getString(R.string.nothing_to_play), Toast.LENGTH_LONG).show()
            return
        }
        // mLastClickTime = SystemClock.elapsedRealtime();
        if (pref!!.getBoolean(Constants.PREFERENCES.SHUFFLE, false)) {
            //shuffle is on, turn it off
            pref!!.edit().putBoolean(Constants.PREFERENCES.SHUFFLE, false).apply()
            playerService!!.shuffle(false)
            shuffle!!.setColorFilter(ColorHelper.getColor(R.color.dark_gray3))
        } else {
            //shuffle is off, turn it on
            pref!!.edit().putBoolean(Constants.PREFERENCES.SHUFFLE, true).apply()
            playerService!!.shuffle(true)
            shuffle!!.setColorFilter(ColorHelper.getColor(R.color.colorwhite))
        }
        UpdateCurrentTracklistAdapter()
    }

    @OnClick(R.id.pw_ivRepeat)
    fun repeat() {
        when {
            pref!!.getInt(Constants.PREFERENCES.REPEAT, 0) == Constants.PREFERENCE_VALUES.NO_REPEAT -> {
                pref!!.edit().putInt(Constants.PREFERENCES.REPEAT, Constants.PREFERENCE_VALUES.REPEAT_ALL)
                    .apply()
                //repeat!!.setColorFilter(UtilityFun.GetDominatColor(playerService!!.getAlbumArt()));
                textInsideRepeat!!.setTextColor(ColorHelper.getColor(R.color.colorwhite))
                repeat!!.setColorFilter(ColorHelper.getColor(R.color.colorwhite))
                textInsideRepeat!!.text = "A"
            }
            pref!!.getInt(Constants.PREFERENCES.REPEAT,
                0) == Constants.PREFERENCE_VALUES.REPEAT_ALL -> {
                pref!!.edit().putInt(Constants.PREFERENCES.REPEAT, Constants.PREFERENCE_VALUES.REPEAT_ONE)
                    .apply()
                textInsideRepeat!!.setTextColor(ColorHelper.getColor(R.color.colorwhite))
                repeat!!.setColorFilter(ColorHelper.getColor(R.color.colorwhite))
                textInsideRepeat!!.text = "1"
            }
            pref!!.getInt(Constants.PREFERENCES.REPEAT,
                0) == Constants.PREFERENCE_VALUES.REPEAT_ONE -> {
                pref!!.edit().putInt(Constants.PREFERENCES.REPEAT, Constants.PREFERENCE_VALUES.NO_REPEAT)
                    .apply()
                repeat!!.setColorFilter(ColorHelper.getColor(R.color.dark_gray3))
                textInsideRepeat!!.setTextColor(ColorHelper.getColor(R.color.dark_gray3))
                textInsideRepeat!!.text = ""
            }
        }
    }

    @OnClick(R.id.pw_ivSkipNext)
    fun skipNext() {
        if (playerService!!.getCurrentTrack() == null) {
            Toast.makeText(this, getString(R.string.nothing_to_play), Toast.LENGTH_LONG).show()
            return
        }
        if (SystemClock.elapsedRealtime() - mLastClickTime < 100) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        playerService!!.nextTrack()
        //LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION.COMPLETE_UI_UPDATE));
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(Constants.ACTION.PLAY_PAUSE_UI_UPDATE))
    }

    @OnClick(R.id.pw_ivSkipPrevious)
    fun skippPrev() {
        if (playerService!!.getCurrentTrack() == null) {
            Toast.makeText(this, getString(R.string.nothing_to_play), Toast.LENGTH_LONG).show()
            return
        }
        if (SystemClock.elapsedRealtime() - mLastClickTime < 100) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        playerService!!.prevTrack()
        //LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION.COMPLETE_UI_UPDATE));
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(Constants.ACTION.PLAY_PAUSE_UI_UPDATE))
    }

    @OnClick(R.id.pw_playButton)
    fun play() {
        //avoid debouncing of key if multiple play clicks are given by user
        if (SystemClock.elapsedRealtime() - mLastClickTime < 100) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        playClicked()
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(Constants.ACTION.PLAY_PAUSE_UI_UPDATE))
    }

    private fun playClicked() {
        if (playerService!!.getCurrentTrack() == null) {
            Toast.makeText(this, getString(R.string.nothing_to_play), Toast.LENGTH_SHORT).show()
            return
        }
        playerService!!.play()
        if (playerService!!.getStatus() === playerService!!.PLAYING) {
            mPlayButton!!.setImageDrawable(resources.getDrawable(R.drawable.pw_pause))
            startUpdateTask()
        } else {
            mPlayButton!!.setImageDrawable(resources.getDrawable(R.drawable.pw_play))
            stopUpdateTask()
        }
    }

    private fun setSeekbarAndTime() {
        seekBar!!.progress = UtilityFun.getProgressPercentage(playerService!!.getCurrentTrackProgress(),
            playerService!!.getCurrentTrackDuration())
        runningTime!!.text = UtilityFun.msToString(playerService!!.getCurrentTrackProgress().toLong())
    }

    private fun startUpdateTask() {
        if (!updateTimeTaskRunning && playerService!!.getStatus() === playerService!!.PLAYING) {
            stopProgressRunnable = false
            Executors.newSingleThreadExecutor().execute(mUpdateTimeTask)
        }
    }

    private fun stopUpdateTask() {
        stopProgressRunnable = true
        updateTimeTaskRunning = false
        try {
            Thread.sleep(100)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private val mUpdateTimeTask: Runnable = Runnable {
        while (true) {
            if (stopProgressRunnable) {
                break
            }
            updateTimeTaskRunning = true
            mHandler.post {
                val curDur: Int = playerService!!.getCurrentTrackProgress()
                val per: Int =
                    UtilityFun.getProgressPercentage(playerService!!.getCurrentTrackProgress() / 1000,
                        playerService!!.getCurrentTrackDuration() / 1000)
                runningTime!!.text = UtilityFun.msToString(curDur.toLong())
                seekBar!!.progress = per
            }
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            //Log.d("FragmentAlbumArt", "run: running");
        }
        updateTimeTaskRunning = false
    }

    private inner class ViewPagerAdapter(manager: FragmentManager?) : FragmentPagerAdapter(manager!!) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()

        override fun getItem(position: Int): Fragment {
            return mFragmentList.get(position)
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList.get(position)
        }
    }

    companion object {
        private val LAUNCH_COUNT_BEFORE_POPUP: Int = 15
        private val RC_SIGN_IN: Int = 7
    }
}