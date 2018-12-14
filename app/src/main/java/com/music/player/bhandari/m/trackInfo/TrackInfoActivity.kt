package com.music.player.bhandari.m.trackInfo

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.trackInfo.models.FetchTrackInfo
import com.music.player.bhandari.m.trackInfo.models.RESULT
import com.music.player.bhandari.m.trackInfo.models.TrackInfo
import kotlinx.android.synthetic.main.activity_track_info.*
import java.lang.NullPointerException

class TrackInfoActivity: AppCompatActivity() , TrackInfo.Callback{

    @BindView(R.id.rootTrackInfo) lateinit var rootView: View

    @BindView(R.id.trackSection) lateinit var trackSection: View
    @BindView(R.id.albumSection) lateinit var albumSection: View
    @BindView(R.id.similarTrackSection) lateinit var similarSection: View

    @BindView(R.id.outOfLuck) lateinit var outOfLuck: TextView
    @BindView(R.id.trackTitle) lateinit var title: TextView
    @BindView(R.id.trackArtist) lateinit var artist: TextView
    @BindView(R.id.trackPlaycount) lateinit var trackPlayCount: TextView
    @BindView(R.id.albumTitle) lateinit var albumTitle: TextView
    @BindView(R.id.albumPlaycount) lateinit var albumPlayCount: TextView

    @BindView(R.id.recyclerTrackTags) lateinit var rvTrackTags: RecyclerView
    @BindView(R.id.recyclerAlbumTags) lateinit var rvAlbumTags: RecyclerView
    @BindView(R.id.recyclerSimilarTracks) lateinit var rvSimilarTracks: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        ColorHelper.setStatusBarGradiant(this)

        val themeSelector = MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)

            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)

            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_info)
        ButterKnife.bind(this)

        if(intent.extras?.getSerializable("trackItem")==null) throw NullPointerException("TrackItem can't be null")

        val trackItem = intent.extras?.getSerializable("trackItem") as TrackItem

        FetchTrackInfo(trackItem.artist, trackItem.title, this).start()

        val toolbar = findViewById<Toolbar>(R.id.toolbar_)
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        if (supportActionBar != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        super.onBackPressed()
    }

    @SuppressLint("SetTextI18n")
    override fun onTrackInfoReady(trackInfo: TrackInfo) {
        progressBar.visibility = View.GONE
        if(trackInfo.result == RESULT.POSITIVE){
            title.text = "Title : ${trackInfo.track?.name ?: ""}"
            artist.text = "Artist : ${trackInfo.track?.artist?.name ?: ""}"
            trackPlayCount.text = "Playcount : ${trackInfo.track?.playcount ?: ""}"

            albumTitle.text = "Title : ${trackInfo.album?.name ?: ""}"
            albumPlayCount.text = "Playcount : ${trackInfo.album?.playcount ?: ""}"

        }else{
            outOfLuck.visibility = View.VISIBLE
            trackSection.visibility = View.INVISIBLE
            albumSection.visibility = View.INVISIBLE
            similarSection.visibility = View.INVISIBLE

            Snackbar.make(rootView, "Track information could not be found on last fm", Snackbar.LENGTH_INDEFINITE).show()
        }

    }
}