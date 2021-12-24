package com.music.player.bhandari.m.activity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.adapter.TopTracksAdapter
import com.music.player.bhandari.m.databinding.ActivityLyricsExploreBinding
import com.music.player.bhandari.m.databinding.ActivityTrackInfoBinding
import com.music.player.bhandari.m.lyricsExplore.OnPopularTracksReady
import com.music.player.bhandari.m.lyricsExplore.PopularTrackRepo
import com.music.player.bhandari.m.lyricsExplore.Track
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.utils.UtilityFun
import io.github.inflationx.viewpump.ViewPumpContextWrapper

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
class ActivityExploreLyrics : AppCompatActivity(), OnPopularTracksReady,
    View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    @JvmField @BindView(R.id.root_view_lyrics_explore)
    var rootView: View? = null

    @JvmField @BindView(R.id.recyclerView)
    var recyclerView: RecyclerView? = null

    @JvmField @BindView(R.id.recycler_view_wrapper)
    var rvWrapper: View? = null

    @JvmField @BindView(R.id.progressBar)
    var progressBar: ProgressBar? = null

    @JvmField @BindView(R.id.statusTextView)
    var statusText: TextView? = null

    @JvmField @BindView(R.id.swipeRefreshLayout)
    var swipeRefreshLayout: SwipeRefreshLayout? = null

    @JvmField @BindView(R.id.fab_right_side)
    var fab: FloatingActionButton? = null

    @JvmField @BindView(R.id.trending_now_text)
    var trendingNow: TextView? = null
    private var handler: Handler? = null

    private lateinit var binding: ActivityTrackInfoBinding

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, ActivityMain::class.java))
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_right)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackInfoBinding.inflate(layoutInflater)

        ColorHelper.setStatusBarGradiant(this)
        when (MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        setContentView(R.layout.activity_lyrics_explore)
        ButterKnife.bind(this)
        growShrinkAnimate()
        handler = Handler(Looper.getMainLooper())
        val toolbar: Toolbar = findViewById(R.id.toolbar_)
        toolbar.setTitle(R.string.lyrics_explore)
        setSupportActionBar(toolbar)

        // add back ar
        // row to toolbar
        if (supportActionBar != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        //rootView.setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());
        swipeRefreshLayout!!.setOnRefreshListener(this)

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.GetStatusBarColor());
        }*/
        fab!!.backgroundTintList = ColorStateList.valueOf(ColorHelper.getWidgetColor())
        fab!!.setOnClickListener(this)
        try {
            val bundle: Bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "explore_lyrics_launched")
            UtilityFun.logEvent(bundle)
        } catch (ignored: Exception) {
        }
    }

    private fun growShrinkAnimate() {
        val growAnim: ScaleAnimation = ScaleAnimation(1.0f,
            1.15f,
            1.0f,
            1.15f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f)
        val shrinkAnim: ScaleAnimation = ScaleAnimation(1.15f,
            1.0f,
            1.15f,
            1.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f)
        growAnim.duration = 500
        shrinkAnim.duration = 500
        fab!!.animation = growAnim
        growAnim.start()
        growAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                fab!!.animation = shrinkAnim
                shrinkAnim.start()
            }
        })
        shrinkAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                fab!!.animation = growAnim
                growAnim.start()
            }
        })
    }

    private fun loadPopularTracks(lookInCache: Boolean) {
        val country =
            MyApp.getPref().getString(getString(R.string.pref_user_country), "")!!
        PopularTrackRepo().fetchPopularTracks(country, this, lookInCache)
    }

    @OnClick(R.id.statusTextView)
    fun retryLoading() {
        progressBar!!.visibility = View.VISIBLE
        statusText!!.visibility = View.GONE
        handler!!.postDelayed({ loadPopularTracks(false) }, 1000)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d("ActivityExploreLyrics", "onNewIntent: ")
    }

    override fun popularTracksReady(tracks: List<Track>?, region: String) {
        handler!!.post {
            swipeRefreshLayout!!.isRefreshing = false
            progressBar!!.visibility = View.GONE
            if (tracks!!.isEmpty()) {
                statusText!!.setText(R.string.error_fetching_popular_tracks)
                statusText!!.visibility = View.VISIBLE
            } else {
                val adapter = TopTracksAdapter(this@ActivityExploreLyrics, tracks)
                recyclerView!!.adapter = adapter
                recyclerView!!.layoutManager = WrapContentLinearLayoutManager(this@ActivityExploreLyrics)
                rvWrapper!!.visibility = View.VISIBLE
                trendingNow!!.text = getString(R.string.trending_now_in, region)
            }
        }
    }

    override fun error() {
        handler!!.post {
            progressBar!!.visibility = View.GONE
            statusText!!.setText(R.string.error_fetching_popular_tracks)
            statusText!!.visibility = View.VISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onResume() {
        super.onResume()
        MyApp.Companion.isAppVisible = true
        if (intent.extras != null && intent.extras!!
                .getBoolean("fresh_load", false)
        ) {
            loadPopularTracks(false)
        } else {
            loadPopularTracks(true)
        }
        if (intent.extras != null && intent.extras!!
                .getBoolean("search_on_launch", false)
        ) {
            Log.d("ActivityExploreLyrics", "onCreate: search lyric dialog on startup")
            searchLyricDialog()
        }
        if (intent.extras != null && intent.extras!!.getBoolean("from_notif")) {
            try {
                val bundle: Bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification_clicked")
                UtilityFun.logEvent(bundle)
            } catch (ignored: Exception) {
            }
        }
    }

    override fun onPause() {
        super.onPause()
        MyApp.Companion.isAppVisible = false
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab_right_side -> searchLyricDialog()
        }
    }

    private fun searchLyricDialog() {
//        val builder: MaterialDialog.Builder = MyDialogBuilder(this)
//            .title(R.string.title_search_lyrics)
//            .customView(R.layout.lyric_search_dialog, true)
//            .positiveButton(R.string.pos_search_lyric)
//            .negativeButton(R.string.cancel)
//            .autoDismiss(false)
//        val layout: View? = builder.build().getCustomView()
//        if (layout == null) {
//            return
//        }
//        val trackTitle: EditText = layout.findViewById<EditText>(R.id.track_title_edit)
//        val artist: EditText = layout.findViewById<EditText>(R.id.artist_edit)
//        val progressBar: ProgressBar = layout.findViewById<ProgressBar>(R.id.progressBar)
        handler!!.postDelayed({
            binding.trackTitle.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                0f,
                0f,
                0))
            binding.trackTitle.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
                0f,
                0f,
                0))
        }, 200)
//        builder.onPositive(object : SingleButtonCallback() {
//            fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                if ((trackTitle.getText().toString() == "")) {
//                    trackTitle.setError(getString(R.string.error_empty_title_lyric_search))
//                    return
//                }
//                var artistName: String = artist.getText().toString()
//                if ((artistName == "")) {
//                    artistName = getString(R.string.unknown_artist)
//                }
//                progressBar.visibility = View.VISIBLE
//                val finalArtistName: String = artistName
//                handler!!.postDelayed(object : Runnable {
//                    override fun run() {
//                        val intent: Intent =
//                            Intent(this@ActivityExploreLyrics, ActivityLyricView::class.java)
//                        intent.putExtra("track_title", trackTitle.getText().toString())
//                        intent.putExtra("artist", finalArtistName)
//                        startActivity(intent)
//                        dialog.dismiss()
//                        try {
//                            val bundle: Bundle = Bundle()
//                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE,
//                                "search_lyric_manually")
//                            UtilityFun.logEvent(bundle)
//                        } catch (ignored: Exception) {
//                        }
//                    }
//                }, 1000)
//            }
//        })
//        builder.onNegative(object : SingleButtonCallback() {
//            fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                dialog.dismiss()
//            }
//        })
//        val dialog: MaterialDialog = builder.build()
//
//        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//        dialog.show()
    }

    override fun onRefresh() {
        if (!UtilityFun.isConnectedToInternet) {
            Toast.makeText(this, "No Connection!", Toast.LENGTH_SHORT).show()
            swipeRefreshLayout!!.isRefreshing = false
            return
        }
        rvWrapper!!.visibility = View.GONE
        progressBar!!.visibility = View.VISIBLE
        statusText!!.visibility = View.GONE
        loadPopularTracks(false)
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