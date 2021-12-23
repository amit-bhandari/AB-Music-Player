package com.music.player.bhandari.m.activity

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper
import com.music.player.bhandari.m.customViews.RoundedImageView
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.MusicLibrary
import com.music.player.bhandari.m.service.PlayerService
import com.music.player.bhandari.m.utils.AppLaunchCountManager
import com.music.player.bhandari.m.utils.SignUp
import com.music.player.bhandari.m.utils.UtilityFun
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

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
class ActivityMain : AppCompatActivity(), ActionMode.Callback,
    NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
    PopupMenu.OnMenuItemClickListener, GoogleApiClient.OnConnectionFailedListener {
    private val RC_LOGIN: Int = 100
    private var mLastClickTime: Long = 0

    //to receive broadcast to update mini player
    private var mReceiverForMiniPLayerUpdate: BroadcastReceiver? = null
    private var mReceiverForLibraryRefresh: BroadcastReceiver? = null

    @JvmField @BindView(R.id.viewpager)
    var viewPager: ViewPager? = null
    private var viewPagerAdapter: ViewPagerAdapter? = null

    @JvmField @BindView(R.id.play_pause_mini_player)
    var buttonPlay: ImageView? = null

    @JvmField @BindView(R.id.next_mini_plaayrer)
    var buttonNext: ImageView? = null

    @JvmField @BindView(R.id.album_art_mini_player)
    var albumArt: ImageView? = null

    @JvmField @BindView(R.id.song_name_mini_player)
    var songNameMiniPlayer: TextView? = null

    @JvmField @BindView(R.id.artist_mini_player)
    var artistNameMiniPlayer: TextView? = null

    @JvmField @BindView(R.id.mini_player)
    var miniPlayer: View? = null

    @JvmField @BindView(R.id.nav_view)
    var navigationView: NavigationView? = null

    @JvmField @BindView(R.id.drawer_bg)
    var navViewBack: ImageView? = null

    @JvmField @BindView(R.id.fab_right_side)
    var fab_right_side: FloatingActionButton? = null

    @JvmField @BindView(R.id.fab_lock)
    var fab_lock: FloatingActionButton? = null

    //private SeekBar seekBar;
    @JvmField @BindView(R.id.root_view_main_activity)
    var rootView: View? = null

    @JvmField @BindView(R.id.album_art_mini_player_wrapper)
    var miniPlayerWrapper: View? = null

    @JvmField @BindView(R.id.overlay_for_gradient)
    var gradientOverlay: View? = null

    @JvmField @BindView(R.id.overlay_for_custom_background)
    var customBackOverlay: View? = null

    //bind player service
    private var playerService: PlayerService? = null

    //search box related things
    private var mSearchAction: MenuItem? = null
    private var isSearchOpened: Boolean = false
    private var editSearch: EditText? = null
    private var searchQuery: String = ""
    private val mHandler: Handler = Handler()
    private var imm: InputMethodManager? = null
    private var currentPageSort: String = "" //holds the value for pref id for current page sort by
    private var mGoogleApiClient: GoogleApiClient? = null

    //tab sequence
    var savedTabSeqInt: IntArray = intArrayOf(0, 1, 2, 3, 4, 5)
    private var backPressedOnce: Boolean = false
    override fun onNewIntent(intent: Intent) {
        //go to tracks tab when clicked on add button in playlist section
        val i: Int = intent.getIntExtra("move_to_tab", -1)
        var currentItemToBeSet: Int = 0
        for (tab: Int in savedTabSeqInt) {
            if (tab == i) {
                break
            }
            currentItemToBeSet++
        }
        if (viewPager != null && i != -1) {
            viewPager!!.currentItem = currentItemToBeSet
        }
        val b: Boolean = intent.getBooleanExtra("refresh", false)
        if (b) {
            //data changed in edit track info activity, update item
            val originalTitle = intent.getStringExtra("originalTitle")
            val position = intent.getIntExtra("position", -1)
            val title = intent.getStringExtra("title")
            val artist = intent.getStringExtra("artist")
            val album = intent.getStringExtra("album")
            if (playerService!!.getCurrentTrack()!!.title.equals(originalTitle)) {
                //current song is playing, update  track item
                playerService!!.updateTrackItem(playerService!!.getCurrentTrackPosition(),
                    playerService!!.getCurrentTrack()!!.id,
                    title,
                    artist,
                    album)
                playerService!!.PostNotification()
                updateUI(false)
            }
            when {
                viewPagerAdapter!!.getItem(viewPager!!.currentItem) is FragmentAlbumLibrary -> {
                    //this should not happen
                }
                viewPagerAdapter!!.getItem(viewPager!!.currentItem) is FragmentLibrary -> {
                    (viewPagerAdapter!!.getItem(viewPager!!.currentItem) as FragmentLibrary)
                        .updateItem(position, title!!, artist!!, album!!)
                }
            }
        }
        super.onNewIntent(intent)
    }

    @SuppressLint("RestrictedApi", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //bind music service
        //startService(new Intent(this,playerService!!.class));
        ColorHelper.setStatusBarGradiant(this)
        //if player service not running, kill the app
        playerService = MyApp.getService()
        if (playerService == null) {
            UtilityFun.restartApp()
            finish()
            return
        }
        when (MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }

        //TypeFaceHelper.setDefaultFont(this, "monospace", "DancingScript-Regular.otf");
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        /*seekBar = findViewById(R.id.seekbar);
        seekBar.setMax(100);
        seekBar.setPadding(0,0,0,0);
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });*/


        // Obtain the Firebase Analytics instance.
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        //Sets whether analytics collection is enabled for this app on this device.
        firebaseAnalytics.setAnalyticsCollectionEnabled(true)

        //Sets the duration of inactivity that terminates the current session. The default value is 1800000 (30 minutes).
        firebaseAnalytics.setSessionTimeoutDuration(10000)
        navigationView!!.setNavigationItemSelectedListener(this)
        disableNavigationViewScrollbars()

        //navigationView.setBackgroundDrawable(ColorHelper.getColoredThemeGradientDrawable());

        //findViewById(R.id.app_bar_layout).setBackgroundColor(ColorHelper.getPrimaryColor());
        //findViewById(R.id.tabs).setBackgroundColor(ColorHelper.getPrimaryColor());

        //findViewById(R.id.gradientBackGroundView)
        //      .setBackgroundDrawable(ColorHelper.GetGradientDrawable());

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.setStatusBarColor(ColorHelper.getDarkPrimaryColor());
            //window.setStatusBarColor(ColorHelper.GetStatusBarColor());
        }*/title = getString(R.string.abm_title)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //mHandler = new Handler();
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        try {
            toolbar.setCollapsible(false)
        } catch (ignored: Exception) {
        }
        setSupportActionBar(toolbar)
        mReceiverForMiniPLayerUpdate = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(Constants.TAG, "onReceive: Update UI")
                updateUI(true)
            }
        }
        mReceiverForLibraryRefresh = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                //updateUI();
                if (MusicLibrary.instance.defaultTracklistNew.isEmpty()) {
                    Snackbar.make(rootView!!,
                        getString(R.string.main_act_empty_lib),
                        Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        miniPlayer!!.setOnClickListener(this)
        buttonPlay!!.setOnClickListener(this)
        buttonNext!!.setOnClickListener(this)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close) {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                Log.d("onDrawerSlide", "onDrawerSlide: " + slideOffset / 2)
                rootView!!.translationX = slideOffset / 2 * drawerView.width
                drawer.bringChildToFront(drawerView)
                drawer.requestLayout()
            }
        }
        drawer.addDrawerListener(toggle)
        toggle.syncState()


        //get tab sequence
        val savedTabSeq = MyApp.getPref().getString(getString(R.string.pref_tab_seq), Constants.TABS.DEFAULT_SEQ)
        val st = StringTokenizer(savedTabSeq, ",")
        savedTabSeqInt = IntArray(Constants.TABS.NUMBER_OF_TABS)
        for (i in 0 until Constants.TABS.NUMBER_OF_TABS) {
            savedTabSeqInt[i] = st.nextToken().toInt()
        }

        // 0 - System default   1 - custom
        when (MyApp.getPref().getInt(getString(R.string.pref_main_library_back), 0)) {
            0 -> setSystemDefaultBackground()
            1 -> setBlurryBackgroundForMainLib()
        }

        // 0 - System default   1 - custom
        when (MyApp.getPref().getInt(getString(R.string.pref_nav_library_back), 0)) {
            0 -> navViewBack!!.setBackgroundDrawable(ColorHelper.getGradientDrawable())
            1 -> setBlurryBackgroundForNav()
        }
        setupViewPager(viewPager!!)
        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                Log.v(Constants.TAG, "position : " + viewPager!!.currentItem)
                invalidateOptionsMenu()
                if (savedTabSeqInt[position] == Constants.TABS.PLAYLIST) {
                    fab_right_side!!.setImageDrawable(ContextCompat.getDrawable(this@ActivityMain,
                        R.drawable.ic_add_black_24dp))
                } else {
                    fab_right_side!!.setImageDrawable(ContextCompat.getDrawable(this@ActivityMain,
                        R.drawable.ic_shuffle_black_24dp))
                }
                if (!(searchQuery == "")) {
                    try {
                        if ((savedTabSeqInt[position] != Constants.TABS.FOLDER
                                    ) && (savedTabSeqInt[position] != Constants.TABS.PLAYLIST
                                    ) && (savedTabSeqInt[position] != Constants.TABS.ALBUMS)
                        ) {
                            if (viewPagerAdapter!!.getItem(position) is FragmentLibrary) {
                                (viewPagerAdapter!!.getItem(position) as FragmentLibrary)
                                    .filter(searchQuery)
                            }
                        }
                        if (savedTabSeqInt[position] == Constants.TABS.ALBUMS) {
                            if (viewPagerAdapter!!.getItem(position) is FragmentAlbumLibrary) {
                                (viewPagerAdapter!!.getItem(position) as FragmentAlbumLibrary)
                                    .filter(searchQuery)
                            }
                        }
                        if (savedTabSeqInt.get(position) == Constants.TABS.FOLDER) {
                            if (viewPagerAdapter!!.getItem(position) is FragmentFolderLibrary) {
                                (viewPagerAdapter!!.getItem(position) as FragmentFolderLibrary)
                                    .filter(searchQuery)
                            }
                        }
                    } catch (ignored: Exception) {
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)

        // Iterate over all tabs and set the custom view
        for (i in 0 until tabLayout.tabCount) {
            val tab: TabLayout.Tab? = tabLayout.getTabAt(i)
            if (tab != null) {
                tab.customView = viewPagerAdapter!!.getTabView(i)
            }
        }
        fab_right_side!!.backgroundTintList = ColorStateList.valueOf(ColorHelper.getWidgetColor())
        fab_right_side!!.setOnClickListener(this)
        fab_lock!!.backgroundTintList = ColorStateList.valueOf(ColorHelper.getWidgetColor())
        fab_lock!!.setOnClickListener(this)
        if (MyApp.getPref().getBoolean(getString(R.string.pref_hide_lock_button), false)
        ) {
            fab_lock!!.visibility = View.GONE
        }
        when {
            MyApp.isLocked() -> {
                findViewById<View>(R.id.border_view).visibility = View.VISIBLE
            }
            else -> {
                findViewById<View>(R.id.border_view).visibility = View.GONE
            }
        }

        //ask for rating
        AppLaunchCountManager.app_launched(this)
        firstTimeInfoManage()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        mGoogleApiClient = GoogleApiClient.Builder(MyApp.getContext())
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
        setTextAndIconColor()
    }

    private fun setTextAndIconColor() {
        songNameMiniPlayer!!.setTextColor(ColorHelper.getPrimaryTextColor())
        artistNameMiniPlayer!!.setTextColor(ColorHelper.getSecondaryTextColor())
        /*buttonPlay.setColorFilter(ColorHelper.getPrimaryTextColor());
        buttonNext.setColorFilter(ColorHelper.getPrimaryTextColor());*/
    }

    private fun disableNavigationViewScrollbars() {
        if (navigationView != null) {
            navigationView!!.getChildAt(0)?.isVerticalScrollBarEnabled = false
        }
    }

    private fun setSystemDefaultBackground() {
        //findViewById(R.id.image_view_view_pager).setBackgroundDrawable(ColorHelper.getBaseThemeDrawable());
        gradientOverlay!!.visibility = View.VISIBLE
        /*findViewById(R.id.image_view_view_pager)
                .setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());*/
    }

    fun setBlurryBackgroundForMainLib() {
        customBackOverlay!!.visibility = View.VISIBLE
        Glide.with(this)
            .load(Uri.fromFile(File(MyApp.getContext().filesDir
                .toString() + getString(R.string.main_lib_back_custom_image))))
            .signature(ObjectKey(System.currentTimeMillis()
                .toString())) //.placeholder(R.drawable.back2)
            //.centerCrop()
            .into(findViewById(R.id.image_view_view_pager)!!)
    }

    fun setBlurryBackgroundForNav() {
        Glide.with(this)
            .load(Uri.fromFile(File(MyApp.getContext().filesDir.toString() + getString(R.string.nav_back_custom_image))))
            .signature(ObjectKey(System.currentTimeMillis().toString()))
            .into(navViewBack!!)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    private fun firstTimeInfoManage() {
        if (!MyApp.getPref().getBoolean(getString(R.string.pref_lock_button_info_shown), false)) {
            showInfo(Constants.FIRST_TIME_INFO.MINI_PLAYER)
            return
        }
        newVersionInfo()
    }

    private fun newVersionInfo() {
        var verCode = 0
        try {
            val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            verCode = pInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        //if updating or first install
        if (verCode != 0 && MyApp.getPref().getInt(getString(R.string.pref_version_code), -1) < verCode) {
            MyApp.getPref().edit().putString(getString(R.string.pref_card_image_links), "").apply()
//            val dialog: MaterialDialog = MyDialogBuilder(this)
//                .title(getString(R.string.main_act_whats_new_title))
//                .content(getString(R.string.whats_new))
//                .positiveText(getString(R.string.okay))
//                .negativeText(getString(R.string.main_act_whats_new_neg))
//                .onNegative(object : SingleButtonCallback() {
//                    fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                        shareApp()
//                    }
//                })
//                .dismissListener(object : DialogInterface.OnDismissListener {
//                    override fun onDismiss(dialogInterface: DialogInterface) {
//                        //Toast.makeText(getApplicationContext(), "Artist Information local sync started in background.", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .build()
//
//            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//            dialog.show()
            val baseThemePref = MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
            if (baseThemePref == Constants.PRIMARY_COLOR.LIGHT) {
                MyApp.getPref().edit().putInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.GLOSSY).apply()
            }

            //MyApp.getPref().edit().putBoolean(getString(R.string.pref_prefer_system_equ),false).apply();

            //check if unknown artist image is cached and remove it
            //@todo remove in next release
            try {
                val CACHE_ART_THUMBS = this.cacheDir.toString() + "/art_thumbs/"
                val actual_file_path = "$CACHE_ART_THUMBS<unknown>"
                val f = File(actual_file_path)
                if (f.exists()) {
                    f.delete()
                }
            } catch (ignored: Exception) {
            }

            //invalidate spotify key
            MyApp.getPref().edit().putLong("spoty_expiry_time", 0).apply()
        }
        MyApp.getPref().edit().putInt(getString(R.string.pref_version_code), verCode).apply()
    }

    @SuppressLint("WrongViewCast")
    private fun showInfo(first_time_info: Int) {
        if (first_time_info != -1) {
            when (first_time_info) {
                Constants.FIRST_TIME_INFO.MINI_PLAYER -> TapTargetView.showFor(this,
                    TapTarget.forView(findViewById(R.id.album_art_mini_player),
                        getString(R.string.mini_player_primary),
                        getString(R.string.mini_player_secondary))
                        .outerCircleColorInt(ColorHelper.getPrimaryColor())
                        .outerCircleAlpha(0.9f)
                        .transparentTarget(true)
                        .titleTextColor(R.color.colorwhite)
                        .descriptionTextColor(R.color.colorwhite)
                        .drawShadow(true)
                        .tintTarget(true),
                    object : TapTargetView.Listener() {
                        override fun onTargetClick(view: TapTargetView) {
                            super.onTargetClick(view)
                            view.dismiss(true)
                        }

                        override fun onOuterCircleClick(view: TapTargetView) {
                            super.onOuterCircleClick(view)
                            view.dismiss(true)
                        }

                        override fun onTargetDismissed(
                            view: TapTargetView,
                            userInitiated: Boolean
                        ) {
                            super.onTargetDismissed(view, userInitiated)
                            showNext()
                        }

                        private fun showNext() {
                            MyApp.getPref().edit()
                                .putBoolean(getString(R.string.pref_lock_button_info_shown), true)
                                .apply()
                            showInfo(Constants.FIRST_TIME_INFO.SORTING)
                        }
                    })
                Constants.FIRST_TIME_INFO.SORTING -> {
                    val menuItemView: View? =
                        findViewById<View>(R.id.action_sort) // SAME ID AS MENU ID
                    if (menuItemView == null) {
                        showInfo(Constants.FIRST_TIME_INFO.MINI_PLAYER)
                    } else {
                        TapTargetView.showFor(this,
                            TapTarget.forView(findViewById<View>(R.id.action_sort),
                                getString(R.string.sorting_primary),
                                getString(R.string.sorting_secondary))
                                .outerCircleColorInt(ColorHelper.getPrimaryColor())
                                .outerCircleAlpha(0.9f)
                                .transparentTarget(true)
                                .titleTextColor(R.color.colorwhite)
                                .descriptionTextColor(R.color.colorwhite)
                                .drawShadow(true)
                                .tintTarget(true),
                            object : TapTargetView.Listener() {
                                override fun onTargetClick(view: TapTargetView) {
                                    super.onTargetClick(view)
                                    view.dismiss(true)
                                }

                                override fun onOuterCircleClick(view: TapTargetView) {
                                    super.onOuterCircleClick(view)
                                    view.dismiss(true)
                                }

                                override fun onTargetDismissed(
                                    view: TapTargetView,
                                    userInitiated: Boolean
                                ) {
                                    super.onTargetDismissed(view, userInitiated)
                                    showNext()
                                }

                                private fun showNext() {
                                    showInfo(Constants.FIRST_TIME_INFO.MUSIC_LOCK)
                                }
                            })
                    }
                }
                Constants.FIRST_TIME_INFO.MUSIC_LOCK -> TapTargetView.showFor(this,
                    TapTarget.forView(findViewById(R.id.fab_lock),
                        getString(R.string.music_lock_primary),
                        getString(R.string.music_lock_secondary))
                        .outerCircleColorInt(ColorHelper.getPrimaryColor())
                        .outerCircleAlpha(0.9f)
                        .transparentTarget(true)
                        .titleTextColor(R.color.colorwhite)
                        .descriptionTextColor(R.color.colorwhite)
                        .drawShadow(true)
                        .tintTarget(true),
                    object : TapTargetView.Listener() {

                        override fun onOuterCircleClick(view: TapTargetView) {
                            super.onOuterCircleClick(view)
                            view.dismiss(true)
                        }

                        override fun onTargetDismissed(
                            view: TapTargetView,
                            userInitiated: Boolean
                        ) {
                            super.onTargetDismissed(view, userInitiated)
                            newVersionInfo()
                        }
                    })
            }
        }
    }

    private fun ValidatePlaylistName(playlist_name: String): Boolean {
        val pattern = "^[a-zA-Z0-9 ]*$"
        if (playlist_name.matches(pattern.toRegex())) {
            when {
                playlist_name.length > 2 -> {
                    //if playlist starts with digit, not allowed
                    if (Character.isDigit(playlist_name[0])) {
                        Snackbar.make(rootView!!,
                            getString(R.string.playlist_error_1),
                            Snackbar.LENGTH_SHORT).show()
                        return false
                    }
                    return true
                }
                else -> {
                    //Toast.makeText(this,"Enter at least 3 characters",Toast.LENGTH_SHORT).show();
                    Snackbar.make(rootView!!, getString(R.string.playlist_error_2), Snackbar.LENGTH_SHORT).show()
                    return false
                }
            }
        } else {
            //Toast.makeText(this,"Only alphanumeric characters allowed",Toast.LENGTH_SHORT).show();
            Snackbar.make(rootView!!, getString(R.string.playlist_error_3), Snackbar.LENGTH_SHORT)
                .show()
            return false
        }
    }

    //boolean to to let function know if expand is needed for mini player or not
    //in case of resuming activity, no need to expand mini player
    //even when pressed back from secondary activity, no need to expand
    private fun updateUI(expandNeeded: Boolean) {
        try {
            if (playerService != null) {
                if (playerService!!.getCurrentTrack() != null) {
                    var builder: RequestBuilder<Drawable?>? = null
                    val url: String? = MusicLibrary.instance.artistUrls
                        .get(playerService!!.getCurrentTrack()!!.getArtist())
                    if (url != null) {
                        when (MyApp.getPref().getInt(getString(R.string.pref_default_album_art), 0)) {
                            0 -> builder = Glide.with(this).load(Uri.parse(url))
                                .placeholder(R.drawable.ic_batman_1)
                            1 -> builder = Glide.with(this).load(Uri.parse(url))
                                .placeholder(UtilityFun.defaultAlbumArtDrawable)
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Glide.with(this)
                            .load(MusicLibrary.instance.getAlbumArtFromTrack(playerService!!.getCurrentTrack()!!.id))
                            .dontAnimate()
                            .error(builder)
                            .placeholder(R.drawable.ic_batman_1)
                            .into(albumArt!!)
                    }
                    if (playerService!!.getStatus() === playerService!!.PLAYING) buttonPlay!!.setImageDrawable(
                        ContextCompat.getDrawable(this,
                            R.drawable.ic_pause_black_24dp)) else buttonPlay!!.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_black_24dp))
                    setTextAndIconColor()
                    songNameMiniPlayer!!.text = playerService!!.getCurrentTrack()!!.title
                    artistNameMiniPlayer!!.text = playerService!!.getCurrentTrack()!!.getArtist()
                    if (expandNeeded) (findViewById<View>(R.id.app_bar_layout) as AppBarLayout).setExpanded(
                        true)
                }
            } else {
                //this should not happen
                //restart app
                UtilityFun.restartApp()
                finish()
            }
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }
    }

    //intitalize view pager with fragments and tab names
    private fun setupViewPager(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        for (tab: Int in savedTabSeqInt) {
            when (tab) {
                Constants.TABS.ALBUMS -> {
                    val bundle3 = Bundle()
                    bundle3.putInt("status", Constants.FRAGMENT_STATUS.ALBUM_FRAGMENT)
                    if (!MyApp.getPref().getBoolean(getString(R.string.pref_album_lib_view), true)
                    ) {
                        val musicByAlbumFrag = FragmentLibrary()
                        musicByAlbumFrag.arguments = bundle3
                        viewPagerAdapter!!.addFragment(musicByAlbumFrag,
                            getString(R.string.tab_album))
                    } else {
                        val musicByAlbumFrag = FragmentAlbumLibrary()
                        musicByAlbumFrag.arguments = bundle3
                        viewPagerAdapter!!.addFragment(musicByAlbumFrag,
                            getString(R.string.tab_album))
                    }
                }
                Constants.TABS.ARTIST -> {
                    val bundle2 = Bundle()
                    bundle2.putInt("status", Constants.FRAGMENT_STATUS.ARTIST_FRAGMENT)
                    val musicByArtistFrag = FragmentLibrary()
                    musicByArtistFrag.arguments = bundle2
                    viewPagerAdapter!!.addFragment(musicByArtistFrag,
                        getString(R.string.tab_artist))
                }
                Constants.TABS.FOLDER -> {
                    val folderFragment = FragmentFolderLibrary()
                    viewPagerAdapter!!.addFragment(folderFragment, getString(R.string.tab_folder))
                }
                Constants.TABS.GENRE -> {
                    val bundle4 = Bundle()
                    bundle4.putInt("status", Constants.FRAGMENT_STATUS.GENRE_FRAGMENT)
                    val musicByGenreFrag = FragmentLibrary()
                    musicByGenreFrag.arguments = bundle4
                    viewPagerAdapter!!.addFragment(musicByGenreFrag, getString(R.string.tab_genre))
                }
                Constants.TABS.PLAYLIST -> {
                    val playlistFrag = FragmentPlaylistLibrary()
                    viewPagerAdapter!!.addFragment(playlistFrag, getString(R.string.tab_playlist))
                }
                Constants.TABS.TRACKS -> {
                    val bundle1 = Bundle()
                    bundle1.putInt("status", Constants.FRAGMENT_STATUS.TITLE_FRAGMENT)
                    val musicByTitleFrag = FragmentLibrary()
                    musicByTitleFrag.arguments = bundle1
                    viewPagerAdapter!!.addFragment(musicByTitleFrag, getString(R.string.tab_track))
                }
            }
        }
        viewPager.adapter = viewPagerAdapter
    }

    override fun onBackPressed() {
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        val count: Int = supportFragmentManager.backStackEntryCount
        when {
            drawer.isDrawerOpen(GravityCompat.START) -> {
                drawer.closeDrawer(GravityCompat.START)
            }
            count > 0 -> {
                findViewById<View>(R.id.mini_player).visibility = View.VISIBLE
                supportFragmentManager.popBackStack()
            } //see if current fragment is folder fragment, if yes, override onBackPressed with fragments own action
            savedTabSeqInt.get(viewPager!!.currentItem) == Constants.TABS.FOLDER -> {
                if (viewPagerAdapter!!.getItem(viewPager!!.currentItem) is FragmentFolderLibrary) {
                    val intent = Intent(NOTIFY_BACK_PRESSED)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }
            }
            isSearchOpened -> {
                handleSearch()
            }
            else -> {
                if (backPressedOnce) {
                    super.onBackPressed()
                    return
                }
                backPressedOnce = true
                Toast.makeText(this, R.string.press_twice_exit, Toast.LENGTH_SHORT).show()
                mHandler.postDelayed({ backPressedOnce = false }, 2000)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        if (viewPager != null) {
            if ((savedTabSeqInt.get(viewPager!!.currentItem) == Constants.TABS.FOLDER
                        || savedTabSeqInt.get(viewPager!!.currentItem) == Constants.TABS.PLAYLIST)
            ) {
                for (i in 0 until menu.size()) {
                    if (R.id.action_sort == menu.getItem(i).itemId) {
                        menu.getItem(i).isVisible = false
                    }
                }
            }
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        mSearchAction = menu.findItem(R.id.action_search)
        if (isSearchOpened) {
            mSearchAction!!.icon = ContextCompat.getDrawable(this, R.drawable.ic_close_white_24dp)
        } else {
            mSearchAction!!.icon = ContextCompat.getDrawable(this, R.drawable.ic_search_white_48dp)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, ActivitySettings::class.java)
                    .putExtra("launchedFrom", Constants.PREF_LAUNCHED_FROM.MAIN)
                    .putExtra("ad", true))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
            }
            R.id.action_refresh -> {
                Toast.makeText(this, R.string.refreshing_library, Toast.LENGTH_SHORT).show()
                MusicLibrary.instance.RefreshLibrary()
            }
            R.id.action_sleep_timer -> setSleepTimerDialog()
            R.id.action_search -> handleSearch()
            R.id.action_equ -> launchEqu()
            R.id.action_sort -> sortLibrary()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchEqu() {
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
            when {
                playerService!!.getEqualizerHelper().isEqualizerSupported() -> {
                    startActivity(Intent(this, ActivityEqualizer::class.java))
                }
                else -> {
                    Snackbar.make(rootView!!, R.string.error_equ_not_supported, Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun sortLibrary() {
        val popupMenu: PopupMenu
        val menuItemView: View? = findViewById(R.id.action_sort) // SAME ID AS MENU ID
        popupMenu = when (menuItemView) {
            null -> {
                PopupMenu(this, findViewById(R.id.action_search))
            }
            else -> {
                PopupMenu(this, menuItemView)
            }
        }
        popupMenu.inflate(R.menu.sort_menu)
        if (savedTabSeqInt[viewPager!!.currentItem] != Constants.TABS.TRACKS) {
            popupMenu.menu.removeItem(R.id.action_sort_size)
            popupMenu.menu.removeItem(R.id.action_sort_by_duration)
            if (savedTabSeqInt[viewPager!!.currentItem] != Constants.TABS.ALBUMS) {
                popupMenu.menu.removeItem(R.id.action_sort_year)
            }
        }
        if (savedTabSeqInt.get(viewPager!!.currentItem) != Constants.TABS.ARTIST) {
            popupMenu.menu.removeItem(R.id.action_sort_no_of_album)
            popupMenu.menu.removeItem(R.id.action_sort_no_of_tracks)
        }
        popupMenu.menu.findItem(R.id.action_sort_asc).isChecked = MyApp.getPref().getInt(getString(R.string.pref_order_by),
            Constants.SORT_BY.ASC) == Constants.SORT_BY.ASC
        when (savedTabSeqInt[viewPager!!.currentItem]) {
            Constants.TABS.ALBUMS -> currentPageSort = getString(R.string.pref_album_sort_by)
            Constants.TABS.ARTIST -> currentPageSort = getString(R.string.pref_artist_sort_by)
            Constants.TABS.GENRE -> currentPageSort = getString(R.string.pref_genre_sort_by)
            Constants.TABS.FOLDER, Constants.TABS.PLAYLIST -> {}
            Constants.TABS.TRACKS -> currentPageSort = getString(R.string.pref_tracks_sort_by)
        }
        when (MyApp.getPref().getInt(currentPageSort, Constants.SORT_BY.NAME)) {
            Constants.SORT_BY.NAME -> popupMenu.menu.findItem(R.id.action_sort_name).isChecked = true
            Constants.SORT_BY.YEAR -> popupMenu.menu.findItem(R.id.action_sort_year).isChecked = true
            Constants.SORT_BY.SIZE -> popupMenu.menu.findItem(R.id.action_sort_size).isChecked = true
            Constants.SORT_BY.NO_OF_ALBUMS -> popupMenu.menu.findItem(R.id.action_sort_no_of_album).isChecked = true
            Constants.SORT_BY.NO_OF_TRACKS -> popupMenu.menu.findItem(R.id.action_sort_no_of_tracks).isChecked = true
            Constants.SORT_BY.DURATION -> popupMenu.menu.findItem(R.id.action_sort_by_duration).isChecked = true
        }
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }

    fun handleSearch() {
        if (isSearchOpened) { //test if the search is open
            if (supportActionBar != null) {
                supportActionBar!!.setDisplayShowCustomEnabled(false)
                supportActionBar!!.setDisplayShowTitleEnabled(true)
            }

            //hides the keyboard
            var view: View? = currentFocus
            if (view == null) {
                view = View(this)
            }
            imm!!.hideSoftInputFromWindow(view.windowToken, 0)

            //add the search icon in the action bar
            mSearchAction!!.icon = ContextCompat.getDrawable(this, R.drawable.ic_search_white_48dp)
            clearSearch()
            searchQuery = ""
            findViewById<View>(R.id.mini_player).visibility = View.VISIBLE
            isSearchOpened = false
        } else { //open the search entry
            findViewById<View>(R.id.mini_player).visibility = View.GONE
            if (supportActionBar != null) {
                supportActionBar!!.setDisplayShowCustomEnabled(true) //enable it to display a custom view
                supportActionBar!!.setCustomView(R.layout.search_bar_layout) //add the custom view
                supportActionBar!!.setDisplayShowTitleEnabled(false) //hide the title
            }
            editSearch = supportActionBar!!.customView.findViewById(R.id.edtSearch) //the text editor
            editSearch!!.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    // TODO Auto-generated method stub
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // TODO Auto-generated method stub
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    searchQuery = s.toString().lowercase(Locale.getDefault())
                    searchAdapters(searchQuery)
                }
            })
            editSearch!!.setOnClickListener {
                imm!!.showSoftInput(editSearch,
                    InputMethodManager.SHOW_IMPLICIT)
            }
            editSearch!!.requestFocus()

            //open the keyboard focused in the edtSearch
            imm!!.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT)
            mSearchAction!!.icon = ContextCompat.getDrawable(this, R.drawable.ic_close_white_24dp)
            //add the close icon
            //mSearchAction.setIcon(getResources().getDrawable(R.drawable.cancel));
            isSearchOpened = true
        }
    }

    private fun clearSearch() {
        val savedTabSeq = MyApp.getPref().getString(getString(R.string.pref_tab_seq), Constants.TABS.DEFAULT_SEQ)
        val st = StringTokenizer(savedTabSeq, ",")
        val savedTabSeqInt = IntArray(Constants.TABS.NUMBER_OF_TABS)
        for (i in 0 until Constants.TABS.NUMBER_OF_TABS) {
            savedTabSeqInt[i] = st.nextToken().toInt()
        }
        for (tab: Int in savedTabSeqInt) {
            when {
                viewPagerAdapter!!.getItem(tab) is FragmentAlbumLibrary -> {
                    (viewPagerAdapter!!.getItem(tab) as FragmentAlbumLibrary)
                        .filter("")
                }
                viewPagerAdapter!!.getItem(tab) is FragmentLibrary -> {
                    (viewPagerAdapter!!.getItem(tab) as FragmentLibrary)
                        .filter("")
                }
                viewPagerAdapter!!.getItem(tab) is FragmentFolderLibrary -> {
                    (viewPagerAdapter!!.getItem(tab) as FragmentFolderLibrary)
                        .filter("")
                }
            }
        }
    }

    private fun searchAdapters(searchQuery: String) {
        when {
            viewPagerAdapter!!.getItem(viewPager!!.currentItem) is FragmentAlbumLibrary -> {
                (viewPagerAdapter!!.getItem(viewPager!!.currentItem) as FragmentAlbumLibrary)
                    .filter(searchQuery)
            }
            viewPagerAdapter!!.getItem(viewPager!!.currentItem) is FragmentLibrary -> {
                (viewPagerAdapter!!.getItem(viewPager!!.currentItem) as FragmentLibrary)
                    .filter(searchQuery)
            }
            viewPagerAdapter!!.getItem(viewPager!!.currentItem) is FragmentFolderLibrary -> {
                (viewPagerAdapter!!.getItem(viewPager!!.currentItem) as FragmentFolderLibrary)
                    .filter(searchQuery)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_settings -> {
                startActivity(Intent(this, ActivitySettings::class.java)
                    .putExtra("launchedFrom", Constants.PREF_LAUNCHED_FROM.DRAWER)
                    .putExtra("ad", true))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
            }
            R.id.nav_share -> {
                shareApp()
            }
            R.id.nav_rate -> {
                setRateDialog()
            }
            R.id.nav_website -> {
                openUrl(Uri.parse(WEBSITE))
            }
            R.id.nav_signup -> {
                signIn()
            }
            R.id.nav_logout -> {
                signOut()
            }
            R.id.nav_explore_lyrics -> {
                startActivity(Intent(this, ActivityExploreLyrics::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.nav_dev_message -> {
                devMessageDialog()
            }
            R.id.nav_instagram -> {
                openUrl(Uri.parse(INSTA_WEBSITE))
            }
            R.id.nav_lyric_card -> {
                lyricCardDialog()
            }
            192 -> {
                //uploadPhotos();
            }
            R.id.nav_saved_lyrics -> {
                startActivity(Intent(this, ActivitySavedLyrics::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            R.id.nav_ringtone_cutter -> {
                showRingtoneCutterDialog()
            }
        }
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showRingtoneCutterDialog() {
//        MyDialogBuilder(this)
//            .title(getString(R.string.action_ringtone_cutter))
//            .content(getString(R.string.dialog_ringtone_cutter))
//            .positiveText(getString(R.string.dialog_rington_cutter_button))
//            .show()
    }

    /**
     * upload lyric card photos
     */
    /*private void uploadPhotos(){
        Log.d("ActivityMain", "uploadPhotos: ");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("cardlinksNew");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final int numberOfLinks =((int) dataSnapshot.getChildrenCount());

                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {


                        File dir =new File(Environment.getExternalStorageDirectory().toString() + "/upload/compressjpeg");

                        File[] files = dir.listFiles();
                        for(int i=numberOfLinks; i<files.length+numberOfLinks; i++){

                            if(files[i-numberOfLinks].isDirectory()) continue;

                            File thumbFile = new File(dir + "/thumb/" + files[i-numberOfLinks].getName().replace(".jpg","") + "_tn.jpg" );
                            StorageReference uploadedFileThumb = FirebaseStorage.getInstance().getReference().child("cardimages").child(thumbFile.getName());
                            final UploadTask uploadTaskThumb = uploadedFileThumb.putFile(Uri.fromFile(thumbFile));
                            Log.d("ActivityMain", "run: Uploading " + thumbFile.getName());

                            final String[] thumbUrl = new String[1];
                            uploadTaskThumb.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("ActivityMain", "onFailure: " + e.getLocalizedMessage());
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.d("ActivityMain", "onSuccess: " + taskSnapshot.getUploadSessionUri());
                                    thumbUrl[0] = taskSnapshot.getDownloadUrl().toString();
                                }
                            });

                            try {
                                com.google.android.gms.tasks.Tasks.await(uploadTaskThumb);
                            }catch (UnsupportedOperationException e){
                                e.printStackTrace();
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                            StorageReference uploadedFile = FirebaseStorage.getInstance().getReference().child("cardimages").child(files[i-numberOfLinks].getName());
                            final UploadTask uploadTask = uploadedFile.putFile(Uri.fromFile(files[i-numberOfLinks]));
                            Log.d("ActivityMain", "run: Uploading " + files[i-numberOfLinks].getName());

                            final int finalI = i;
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("ActivityMain", "onFailure: " + e.getLocalizedMessage());
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.d("ActivityMain", "onSuccess: " + taskSnapshot.getDownloadUrl());
                                    if(taskSnapshot.getDownloadUrl()==null)  return;

                                    Map<String, String> image = new HashMap<>();
                                    image.put("thumb", thumbUrl[0]);
                                    image.put("image", taskSnapshot.getDownloadUrl().toString());
                                    myRef.child(Integer.toString(finalI)).setValue(image);

                                    Toast.makeText(playerService, "Uploaded : " + taskSnapshot.getDownloadUrl().toString(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            try {
                                com.google.android.gms.tasks.Tasks.await(uploadTask);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
*/
    private fun lyricCardDialog() {
        val link = FirebaseRemoteConfig.getInstance().getString("sample_lyric_card")
//        val dialog: MaterialDialog = MyDialogBuilder(this)
//            .title(getString(R.string.nav_lyric_cards))
//            .customView(R.layout.lyric_card_dialog,
//                false) //.content(R.string.dialog_lyric_card_content)
//            .positiveText(R.string.dialog_lyric_card_pos)
//            .negativeText(getString(R.string.cancel))
//            .neutralText("Know more")
//            .onNeutral({ dialog1, which -> openUrl(Uri.parse(LYRIC_CARD_GIF)) })
//            .onPositive({ dialog12, which ->
//                val searchLyricIntent: Intent =
//                    Intent(MyApp.Companion.getContext(), ActivityExploreLyrics::class.java)
//                searchLyricIntent.action = Constants.ACTION.MAIN_ACTION
//                searchLyricIntent.putExtra("search_on_launch", true)
//                searchLyricIntent.putExtra("from_notif", false)
//                startActivity(searchLyricIntent)
//            })
//            .onNegative({ dialog13, which -> dialog13.dismiss() }).build()
//        if (dialog.getCustomView() != null) {
//            val iv: ImageView = dialog.getCustomView().findViewById(R.id.sample_album_card)
//            iv.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    Log.d("Tag", "lyricCardDialog: width " + iv.measuredWidth)
//                    val params: ViewGroup.LayoutParams = iv.layoutParams
//                    params.width = iv.measuredWidth
//                    params.height = iv.measuredWidth
//                    // existing height is ok as is, no need to edit it
//                    iv.layoutParams = params
//                    iv.viewTreeObserver.removeGlobalOnLayoutListener(this)
//                }
//            })
//            Glide.with(this)
//                .load(link)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .transition(DrawableTransitionOptions.withCrossFade())
//                .into(iv)
//        }
//
//        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//        dialog.show()
    }

    private fun openUrl(parse: Uri) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, parse)
            startActivity(browserIntent)
        } catch (e: Exception) {
            Snackbar.make(rootView!!, getString(R.string.error_opening_browser), Snackbar.LENGTH_SHORT).show()
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

    private fun devMessageDialog() {
        if (MyApp.getPref().getBoolean("new_dev_message", false)) {
            MyApp.getPref().edit().putBoolean("new_dev_message", false).apply()
            updateNewDevMessageDot(false)
        }
        var message: String = FirebaseRemoteConfig.getInstance().getString("developer_message")
        message = message.replace("$$", "\n\n")
        val link: String = FirebaseRemoteConfig.getInstance().getString("link")
//        val dialog: MaterialDialog = MyDialogBuilder(this)
//            .title(getString(R.string.nav_developers_message))
//            .content(message) //.neutralText(R.string.write_me)
//            .negativeText(getString(R.string.main_act_rate_dialog_pos))
//            .positiveText(getString(R.string.title_click_me)) /*.onNeutral(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        feedbackEmail();
//                    }
//                })*/
//            .onNegative(object : SingleButtonCallback() {
//                fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                    val appPackageName: String =
//                        packageName // getPackageName() from Context or Activity object
//                    try {
//                        startActivity(Intent(Intent.ACTION_VIEW,
//                            Uri.parse("market://details?id=" + appPackageName)))
//                    } catch (anfe: ActivityNotFoundException) {
//                        startActivity(Intent(Intent.ACTION_VIEW,
//                            Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)))
//                    }
//                }
//            })
//            .onPositive(object : SingleButtonCallback() {
//                fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                    openUrl(Uri.parse(link))
//                }
//            }).build()
//
//        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//        dialog.show()
    }

    private fun setRateDialog() {
        val linear = LinearLayout(this)
        linear.orientation = LinearLayout.VERTICAL
        val text = TextView(this)
        text.text = getString(R.string.main_act_rate_us)
        text.typeface = TypeFaceHelper.getTypeFace(this)
        text.setPadding(20, 10, 20, 10)
        text.textSize = 16f
        //text.setGravity(Gravity.CENTER);
        val ratingWrap: LinearLayout = LinearLayout(this)
        ratingWrap.orientation = LinearLayout.VERTICAL
        ratingWrap.gravity = Gravity.CENTER
        val ratingBar: RatingBar = RatingBar(this)
        ratingBar.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        //ratingBar.setNumStars(5);
        ratingBar.rating = 5f
        ratingWrap.addView(ratingBar)
        linear.addView(text)
        linear.addView(ratingWrap)
//        val dialog: MaterialDialog = MyDialogBuilder(this)
//            .title(getString(R.string.main_act_rate_dialog_title)) // .content(getString(R.string.lyric_art_info_content))
//            .positiveText(getString(R.string.main_act_rate_dialog_pos))
//            .negativeText(getString(R.string.cancel))
//            .onPositive(object : SingleButtonCallback() {
//                fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                    val appPackageName: String =
//                        packageName // getPackageName() from Context or Activity object
//                    try {
//                        startActivity(Intent(Intent.ACTION_VIEW,
//                            Uri.parse("market://details?id=" + appPackageName)))
//                    } catch (anfe: ActivityNotFoundException) {
//                        startActivity(Intent(Intent.ACTION_VIEW,
//                            Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)))
//                    }
//                }
//            })
//            .customView(linear, true)
//            .build()
//
//        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//        dialog.show()
    }

    fun setSleepTimerDialog() {
//        val builder: MyDialogBuilder = MyDialogBuilder(this)
//        val linear: LinearLayout = LinearLayout(this)
//        linear.orientation = LinearLayout.VERTICAL
//        val text: TextView = TextView(this)
//        val timer: Int =
//            MyApp.Companion.getPref().getInt(this.getString(R.string.pref_sleep_timer), 0)
//        if (timer == 0) {
//            val tempString: String =
//                "0 " + this.getString(R.string.main_act_sleep_timer_status_minutes)
//            text.text = tempString
//        } else {
//            val stringTemp: String = (this.getString(R.string.main_act_sleep_timer_status_part1) +
//                    timer +
//                    this.getString(R.string.main_act_sleep_timer_status_part2))
//            text.text = stringTemp
//            builder.neutralText(this.getString(R.string.main_act_sleep_timer_neu))
//                .onNeutral(object : SingleButtonCallback() {
//                    fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                        MyApp.Companion.getPref().edit()
//                            .putInt(getString(R.string.pref_sleep_timer), 0).apply()
//                        playerService!!.setSleepTimer(0, false)
//                        //Toast.makeText(this, "Sleep timer discarded", Toast.LENGTH_LONG).show();
//                        Snackbar.make(rootView,
//                            getString(R.string.sleep_timer_discarded),
//                            Snackbar.LENGTH_SHORT).show()
//                    }
//                })
//        }
//        text.setPadding(0, 10, 0, 0)
//        text.gravity = Gravity.CENTER
//        text.setTypeface(TypeFaceHelper.getTypeFace(this))
//        val seek: SeekBar = SeekBar(this)
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
//                    progress.toString() + getString(R.string.main_act_sleep_timer_status_minutes)
//                text.text = tempString
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar) {}
//            override fun onStopTrackingTouch(seekBar: SeekBar) {}
//        })
//        linear.addView(seek)
//        linear.addView(text)
//        val dialog: MaterialDialog = builder
//            .title(this.getString(R.string.main_act_sleep_timer_title))
//            .positiveText(this.getString(R.string.okay))
//            .negativeText(this.getString(R.string.cancel))
//            .onPositive(object : SingleButtonCallback() {
//                fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                    if (seek.progress != 0) {
//                        MyApp.Companion.getPref().edit()
//                            .putInt(getString(R.string.pref_sleep_timer), seek.progress)
//                            .apply()
//                        playerService!!.setSleepTimer(seek.progress, true)
//                        val temp: String = (getString(R.string.sleep_timer_successfully_set)
//                                + seek.progress
//                                + getString(R.string.main_act_sleep_timer_status_minutes))
//                        //Toast.makeText(this, temp, Toast.LENGTH_LONG).show();
//                        Snackbar.make(rootView, temp, Snackbar.LENGTH_SHORT).show()
//                    }
//                }
//            })
//            .customView(linear, true)
//            .build()
//
//        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        loginSilently()
    }

    private fun loginSilently() {
        //login silently to google
        val opr: OptionalPendingResult<GoogleSignInResult> =
            Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient!!)
        when {
            opr.isDone -> {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(Constants.TAG, "Got cached sign-in")
                val result: GoogleSignInResult = opr.get()
                handleSignInResult(result, false)
            }
            else -> {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                //showProgressDialog();
                opr.setResultCallback { googleSignInResult -> //hideProgressDialog();
                    handleSignInResult(googleSignInResult, false)
                }
            }
        }
    }

    override fun onDestroy() {
        Log.v("TAG", "Main activity getting destroyed")
        try {
            mHandler.removeCallbacksAndMessages(null)
            viewPager!!.clearOnPageChangeListeners()
            viewPager = null
            viewPagerAdapter = null
            navigationView!!.setNavigationItemSelectedListener(null)
        } catch (e: NullPointerException) {
            Log.d("TAG", "onDestroy: destroy called because of null player service")
        }
        super.onDestroy()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.mini_player -> {
                val intent = Intent(applicationContext, ActivityNowPlaying::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                val options: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(this,
                    miniPlayerWrapper,
                    getString(R.string.transition))
                ActivityCompat.startActivityForResult(this,
                    intent,
                    RC_LOGIN,
                    options.toBundle())
                Log.v(Constants.TAG, "Launch now playing Jarvis")
            }
            R.id.play_pause_mini_player -> {
                val colorSwitchRunnablePlay = ColorSwitchRunnableForImageView(view as ImageView)
                mHandler.post(colorSwitchRunnablePlay)
                if (playerService!!.getCurrentTrack() == null) {
                    //Toast.makeText(this,"Nothing to play!",Toast.LENGTH_LONG).show();
                    Snackbar.make(rootView!!,
                        getString(R.string.nothing_to_play),
                        Snackbar.LENGTH_SHORT).show()
                    return
                }
                if (SystemClock.elapsedRealtime() - mLastClickTime < 100) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                playerService!!.play()
                if (playerService!!.getStatus() === playerService!!.PLAYING) {
                    buttonPlay!!.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.ic_pause_black_24dp))
                } else {
                    buttonPlay!!.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.ic_play_arrow_black_24dp))
                }
                setTextAndIconColor()
                actionMode = startSupportActionMode(this)
            }
            R.id.next_mini_plaayrer -> {
                val colorSwitchRunnableNext = ColorSwitchRunnableForImageView(view as ImageView)
                mHandler.post(colorSwitchRunnableNext)
                if (SystemClock.elapsedRealtime() - mLastClickTime < 100) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime()
                playerService!!.nextTrack()
                //no need to expand mini player
                updateUI(false)
                Log.v(Constants.TAG, "next track please Jarvis")
            }
            R.id.fab_lock -> {
                when {
                    MyApp.isLocked() -> {
                        MyApp.setLocked(false)
                        fab_lock!!.setImageDrawable(ContextCompat.getDrawable(this,
                            R.drawable.ic_lock_open_black_24dp))
                        findViewById<View>(R.id.border_view).visibility = View.GONE
                    }
                    else -> {
                        findViewById<View>(R.id.border_view).visibility = View.VISIBLE
                        fab_lock!!.setImageDrawable(ContextCompat.getDrawable(this,
                            R.drawable.ic_lock_outline_black_24dp))
                        MyApp.setLocked(true)
                    }
                }
                val shake1 = AnimationUtils.loadAnimation(this, R.anim.shake_animation)
                fab_lock!!.startAnimation(shake1)
                lockInfoDialog()
            }
            R.id.fab_right_side -> when (Constants.TABS.PLAYLIST) {
                savedTabSeqInt[viewPager!!.currentItem] -> {
                    CreatePlaylistDialog()
                }
                else -> {
                    if (MyApp.isLocked()) {
                        Snackbar.make(rootView!!,
                            getString(R.string.music_is_locked),
                            Snackbar.LENGTH_SHORT).show()
                        return
                    }
                    when {
                        playerService!!.getTrackList().size > 0 -> {
                            playerService!!.shuffleAll()
                        }
                        else -> {
                            Snackbar.make(rootView!!,
                                getString(R.string.empty_track_list),
                                Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        MyApp.isAppVisible = false
        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(mReceiverForMiniPLayerUpdate!!)
        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(mReceiverForLibraryRefresh!!)
        super.onPause()
        //stopUpdateTask();
    }

    override fun onResume() {
        super.onResume()
        playerService = MyApp.getService()
        MyApp.isAppVisible = true
        updateUI(false)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            mReceiverForMiniPLayerUpdate!!,
            IntentFilter(Constants.ACTION.COMPLETE_UI_UPDATE))
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            mReceiverForLibraryRefresh!!,
            IntentFilter(Constants.ACTION.REFRESH_LIB))
        //seekBar.setProgress(UtilityFun.getProgressPercentage(playerService!!.getCurrentTrackProgress(), playerService!!.getCurrentTrackDuration()));
        //startUpdateTask();
    }

    override fun onRestart() {
        super.onRestart()
        updateUI(false)
    }

    fun hideFab(hide: Boolean) {
        if (hide && fab_right_side!!.isShown) {
            fab_right_side!!.hide()
            if (!MyApp.getPref().getBoolean(getString(R.string.pref_hide_lock_button), false)) {
                fab_lock!!.hide()
            }
        } else {
            fab_right_side!!.show()
            if (!MyApp.getPref().getBoolean(getString(R.string.pref_hide_lock_button), false)) {
                fab_lock!!.show()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (playerService == null) {
            UtilityFun.restartApp()
            finish()
            return false
        }
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY -> {
                playerService!!.play()
                updateUI(false)
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                playerService!!.nextTrack()
                updateUI(false)
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                playerService!!.prevTrack()
                updateUI(false)
            }
            KeyEvent.KEYCODE_MEDIA_STOP -> {
                playerService!!.stop()
                updateUI(false)
            }
            KeyEvent.KEYCODE_BACK -> onBackPressed()
        }
        return false
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        var sort_id: Int = MyApp.getPref().getInt(currentPageSort, Constants.SORT_BY.NAME)
        when (item.itemId) {
            R.id.action_sort_name -> {
                MyApp.getPref().edit().putInt(currentPageSort, Constants.SORT_BY.NAME).apply()
                sort_id = Constants.SORT_BY.NAME
            }
            R.id.action_sort_year -> {
                MyApp.getPref().edit().putInt(currentPageSort, Constants.SORT_BY.YEAR).apply()
                sort_id = Constants.SORT_BY.YEAR
            }
            R.id.action_sort_size -> {
                MyApp.getPref().edit().putInt(currentPageSort, Constants.SORT_BY.SIZE).apply()
                sort_id = Constants.SORT_BY.SIZE
            }
            R.id.action_sort_no_of_album -> {
                MyApp.getPref().edit().putInt(currentPageSort, Constants.SORT_BY.NO_OF_ALBUMS).apply()
                sort_id = Constants.SORT_BY.NO_OF_ALBUMS
            }
            R.id.action_sort_no_of_tracks -> {
                MyApp.getPref().edit().putInt(currentPageSort, Constants.SORT_BY.NO_OF_TRACKS).apply()
                sort_id = Constants.SORT_BY.NO_OF_TRACKS
            }
            R.id.action_sort_by_duration -> {
                MyApp.getPref().edit().putInt(currentPageSort, Constants.SORT_BY.DURATION).apply()
                sort_id = Constants.SORT_BY.DURATION
            }
            R.id.action_sort_asc -> if (!item.isChecked) {
                MyApp.getPref().edit().putInt(getString(R.string.pref_order_by), Constants.SORT_BY.ASC).apply()
            } else {
                MyApp.getPref().edit().putInt(getString(R.string.pref_order_by), Constants.SORT_BY.DESC).apply()
            }
        }
        Log.v(Constants.TAG, "view pager item" + viewPager!!.currentItem + "")
        when {
            viewPagerAdapter!!.getItem(viewPager!!.currentItem) is FragmentAlbumLibrary -> {
                (viewPagerAdapter!!.getItem(viewPager!!.currentItem) as FragmentAlbumLibrary).sort(
                    sort_id)
            }
            else -> {
                (viewPagerAdapter!!.getItem(viewPager!!.currentItem) as FragmentLibrary).sort(sort_id)
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            handleSignInResult(result!!, true)
        }
    }

    private fun lockInfoDialog() {
        if (!MyApp.getPref().getBoolean(getString(R.string.pref_show_lock_info_dialog), true)) {
            return
        }
//        val dialog: MaterialDialog = MyDialogBuilder(this)
//            .title(getString(R.string.main_act_lock_info_title))
//            .content(getString(R.string.main_act_lock_info_content))
//            .positiveText(getString(R.string.main_act_lock_info_pos))
//            .negativeText(getString(R.string.main_act_lock_info_neg))
//            .neutralText(getString(R.string.main_act_lock_info_neu))
//            .onNegative(object : SingleButtonCallback() {
//                fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                    MyApp.Companion.getPref().edit()
//                        .putBoolean(getString(R.string.pref_hide_lock_button), true).apply()
//                    fab_lock.hide()
//                    MyApp.Companion.setLocked(false)
//                    findViewById<View>(R.id.border_view).visibility = View.GONE
//                }
//            })
//            .onNeutral(object : SingleButtonCallback() {
//                fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                    MyApp.Companion.getPref().edit()
//                        .putBoolean(getString(R.string.pref_show_lock_info_dialog), false).apply()
//                }
//            })
//            .build()
//
//        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//        dialog.show()
    }

    private fun CreatePlaylistDialog() {
        val input = EditText(this@ActivityMain)
        input.inputType = InputType.TYPE_CLASS_TEXT
        mHandler.postDelayed({
            input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                0f,
                0f,
                0))
            input.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
                0f,
                0f,
                0))
        }, 200)
//        val dialog: MaterialDialog = MyDialogBuilder(this)
//            .title(getString(R.string.main_act_create_play_list_title))
//            .positiveText(getString(R.string.okay))
//            .negativeText(getString(R.string.cancel))
//            .onPositive(object : SingleButtonCallback() {
//                fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                    val playlist_name: String = input.text.toString().trim({ it <= ' ' })
//                    if (ValidatePlaylistName(playlist_name)) {
//                        if (PlaylistManager.getInstance(MyApp.Companion.getContext())
//                                .CreatePlaylist(playlist_name)
//                        ) {
//                            var tabCount: Int = 0
//                            for (tab: Int in savedTabSeqInt) {
//                                if (tab == Constants.TABS.PLAYLIST) {
//                                    if ((viewPagerAdapter
//                                            .getItem(tabCount)) is FragmentPlaylistLibrary
//                                    ) {
//                                        (viewPagerAdapter
//                                            .getItem(tabCount) as FragmentPlaylistLibrary)
//                                            .refreshPlaylistList()
//                                    }
//                                    break
//                                }
//                                tabCount++
//                            }
//
//                            //Toast.makeText(ActivityMain.this, "Playlist created", Toast.LENGTH_SHORT).show();
//                            Snackbar.make(rootView,
//                                getString(R.string.play_list_created),
//                                Snackbar.LENGTH_SHORT).show()
//                        } else {
//                            //Toast.makeText(ActivityMain.this, "Playlist already exists", Toast.LENGTH_SHORT).show();
//                            Snackbar.make(rootView,
//                                getString(R.string.play_list_already_exists),
//                                Snackbar.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//            })
//            .customView(input, true)
//            .build()
//
//        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//        dialog.show()
    }

    private fun signIn() {
        val signInIntent: Intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient!!)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient!!).setResultCallback { //updateUI(false);
            MyApp.hasUserSignedIn = false
            updateDrawerUI(null, null, false)
            Snackbar.make(rootView!!, getString(R.string.signed_out), Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult, manualSignIn: Boolean) {
        Log.d(Constants.TAG, "handleSignInResult:" + result.isSuccess)
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            if (manualSignIn) {
                //permanently hide  sign in button on now playing activity
                MyApp.getPref().edit().putBoolean("never_show_button_again", true).apply()
            }
            MyApp.hasUserSignedIn = true
            val acct: GoogleSignInAccount = result.signInAccount ?: return

            //sign up user to tech guru newsletter
            val email = acct.email
            val name = acct.givenName
            //store this email id and time of first sign in
            if (manualSignIn && email != null) {
                SignUp().execute(email, name)
            }
            var personPhotoUrl: String? = ""
            if (acct.photoUrl != null) {
                personPhotoUrl = acct.photoUrl.toString()
            }
            updateDrawerUI(acct.displayName, personPhotoUrl, true)
        } else {
            // some Error or user logged out, either case, update the drawer and give user appropriate info
            MyApp.hasUserSignedIn = false
            updateDrawerUI(null, null, false)
            if (manualSignIn) {
                when (result.status.statusCode) {
                    CommonStatusCodes.NETWORK_ERROR -> {
                        //Toast.makeText(this, "Network Error, try again later!", Toast.LENGTH_SHORT).show();
                        Snackbar.make(rootView!!,
                            getString(R.string.network_error),
                            Snackbar.LENGTH_SHORT).show()
                    }
                    else -> {
                        //Toast.makeText(this, "Unknown Error, try again later!", Toast.LENGTH_SHORT).show();
                        Snackbar.make(rootView!!,
                            getString(R.string.unknown_error),
                            Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateDrawerUI(displayName: String?, personPhotoUrl: String?, signedIn: Boolean) {
        val textView: TextView = navigationView!!.getHeaderView(0).findViewById(R.id.signed_up_user_name)
        if (displayName != null) {
            textView.text = displayName
        } else {
            textView.text = ""
        }
        val imageView: RoundedImageView = navigationView!!.getHeaderView(0).findViewById(R.id.navHeaderImageView)
        if (personPhotoUrl != null) {
            Glide.with(applicationContext).load(personPhotoUrl)
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        } else {
            when (MyApp.getPref().getInt(getString(R.string.pref_default_album_art), 0)) {
                0 -> imageView.setImageResource(R.drawable.ic_batman_1)
                1 -> imageView.setImageDrawable(UtilityFun.defaultAlbumArtDrawable)
            }
        }
        navigationView!!.menu.clear() //clear old inflated items.
        if (signedIn) {
            navigationView!!.inflateMenu(R.menu.drawer_menu_logged_in)
        } else {
            navigationView!!.inflateMenu(R.menu.drawer_menu_logged_out)
        }

        //set red dot if new developer message arrives
        if (MyApp.getPref().getBoolean("new_dev_message", false)) {
            updateNewDevMessageDot(true)
        }

        //navigationView.getMenu().findItem(R.id.nav_lyric_card).setActionView(R.layout.nav_item_lyric_card);  //showing new icon with color red

        //add upload image button
        /*if(BuildConfig.DEBUG){
            navigationView.getMenu().add(R.id.grp2, 192, 10,"Upload");
        }*/

        //updateNavigationMenuItems();
    }

    private fun updateNewDevMessageDot(set: Boolean) {
        if (set) {
            navigationView!!.menu.findItem(R.id.nav_dev_message)
                .setActionView(R.layout.nav_item_dev_message)
        } else {
            navigationView!!.menu.findItem(R.id.nav_dev_message).actionView = null
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(Constants.TAG, "onConnectionFailed:$connectionResult")
    }

    var actionMode: ActionMode? = null
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        if (actionMode != null) {
            val inflater: MenuInflater = actionMode!!.menuInflater
            inflater.inflate(R.menu.menu_cab_recyclerview_lyrics, menu)
            return true
        }
        return false
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {}
    private inner class ViewPagerAdapter(manager: FragmentManager?) : FragmentPagerAdapter(manager!!) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList.get(position)
        }

        fun getTabView(position: Int): View {
            // Given you have a custom layout in `res/layout/custom_tab.xml` with a TextView and ImageView
            val v: View = LayoutInflater.from(baseContext).inflate(R.layout.custom_tab, null)
            val tv: TextView = v.findViewById(R.id.textview_custom_tab)
            tv.text = mFragmentTitleList[position]
            return v
        }
    }

    private inner class ColorSwitchRunnableForImageView constructor(var v: ImageView) : Runnable {
        var colorChanged: Boolean = false
        override fun run() {
            when {
                !colorChanged -> {
                    v.setColorFilter(ColorHelper.getWidgetColor())
                    colorChanged = true
                    mHandler.postDelayed(this, 200)
                }
                else -> {
                    v.setColorFilter(ColorHelper.getColor(R.color.colorwhite))
                    colorChanged = false
                }
            }
        }

    }

    companion object {
        val FB_URL: String = "http://www.facebook.com/abmusicoffline/"
        val WEBSITE: String = "http://www.thetechguru.in"
        val GITHUB: String = "https://github.com/amit-bhandari/AB-Music-Player"
        val INSTA_WEBSITE: String = "https://www.instagram.com/ab_music__/?hl=en"
        val LYRIC_CARD_GIF: String = "https://media.giphy.com/media/2w6JlMibDu9ZL9xVuB/giphy.gif"
        val AB_REMOTE_WALL_URL: String =
            "https://play.google.com/store/apps/details?id=in.thetechguru.walle.remote.abremotewallpaperchanger&hl=en"
        val NOTIFY_BACK_PRESSED: String = "BACK_PRESSED"
        private val RC_SIGN_IN: Int = 7
    }
}