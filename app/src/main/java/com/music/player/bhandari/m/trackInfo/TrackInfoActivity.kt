package com.music.player.bhandari.m.trackInfo

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.trackInfo.models.FetchTrackInfo
import com.music.player.bhandari.m.trackInfo.models.RESULT
import com.music.player.bhandari.m.trackInfo.models.TrackInfo
import com.music.player.bhandari.m.trackInfo.models.track.Tag
import com.nshmura.snappysmoothscroller.SnapType
import com.nshmura.snappysmoothscroller.SnappyLinearLayoutManager
import kotlinx.android.synthetic.main.activity_track_info.*
import java.lang.NullPointerException

class TrackInfoActivity: AppCompatActivity() , TrackInfo.Callback{

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
            //positive result means its guaranteed track is found. Not sure about album and similar tracks, need to put null check
            trackTitle.text = "Title : ${trackInfo.track?.name ?: ""}"
            trackArtist.text = "Artist : ${trackInfo.track?.artist?.name ?: ""}"
            trackPlaycount.text = "Playcount : ${trackInfo.track?.playcount ?: ""}"

            albumTitle.text = "Title : ${trackInfo.album?.name ?: ""}"
            albumPlaycount.text = "Playcount : ${trackInfo.album?.playcount ?: ""}"

            val snappyLinearLayoutManager = SnappyLinearLayoutManager(this)
            snappyLinearLayoutManager.setSnapType(SnapType.CENTER)
            snappyLinearLayoutManager.setSnapDuration(1500)

            recyclerTrackTags.adapter = TagsAdapter(trackInfo.track?.toptags?.tag?.map { it.name } ?: listOf())
            recyclerTrackTags.layoutManager = snappyLinearLayoutManager

            if(trackInfo.album!=null){
                recyclerAlbumTags.adapter = TagsAdapter(trackInfo.album?.tags?.tag?.map { it.name } ?: listOf())
                recyclerAlbumTags.layoutManager = snappyLinearLayoutManager

                recyclerAlbumTracks.adapter = TracksAdapter(this,
                        trackInfo.album
                                ?.tracks?.track
                                ?.map { TracksAdapter.TrackItem(it.name, it.duration, trackInfo.track?.album?.image?.last()?.text ?: "") }
                                ?: listOf())
                recyclerSimilarTracks.layoutManager = LinearLayoutManager(this)
            }else{
                albumSection.visibility = View.GONE
            }

            if(trackInfo.similarTracks!=null){
                recyclerSimilarTracks.adapter = TracksAdapter(this,
                        trackInfo.similarTracks
                                ?.track
                                ?.map { TracksAdapter.TrackItem(it.name, it.match.toString(), it.image.last().text) } ?: listOf())
                recyclerSimilarTracks.layoutManager = LinearLayoutManager(this)
            }else{
                similarTrackSection.visibility = View.GONE
            }

        }else{
            outOfLuck.visibility = View.VISIBLE
            trackSection.visibility = View.INVISIBLE
            albumSection.visibility = View.INVISIBLE
            similarTrackSection.visibility = View.INVISIBLE

            Snackbar.make(rootTrackInfo, "Track information could not be found on last fm", Snackbar.LENGTH_INDEFINITE).show()
        }

    }

    class TagsAdapter(): RecyclerView.Adapter<TagsAdapter.MyViewHolder>(){

        private var tags: List<String> = mutableListOf()

        constructor(tags: List<String>):this(){
            this.tags = tags
        }

        class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
            var tagTextView: TextView = view.findViewById(R.id.tagTextView)
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
            val v = LayoutInflater.from(MyApp.getContext()).inflate(R.layout.item_tag, p0, false)
            return MyViewHolder(v)
        }

        override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
            p0.tagTextView.text = tags[p1]
        }

        override fun getItemCount(): Int {
            return tags.count()
        }
    }

    class TracksAdapter(): RecyclerView.Adapter<TracksAdapter.MyViewHolder>(){

        data class TrackItem(val trackTitle: String, val secondaryText: String, val imageUrl: String)
        private var tracks:List<TrackItem> = mutableListOf()
        private lateinit var context: Context

        constructor(context: Context, tracks: List<TrackItem>):this(){
            this.context = context
            this.tracks = tracks
        }

        class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
            var trackTitle: TextView = view.findViewById(R.id.trackInfo)
            var secondaryText: TextView = view.findViewById(R.id.playCount)
            var imageView: ImageView = view.findViewById(R.id.imageView)
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
            val v = LayoutInflater.from(MyApp.getContext()).inflate(R.layout.track_item_explore_lyrics, p0, false)
            return MyViewHolder(v)
        }

        override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
            p0.trackTitle.text = tracks[p1].trackTitle
            p0.secondaryText.text = tracks[p1].secondaryText
            Glide.with(context).load(tracks[p1].imageUrl).into(p0.imageView)
        }

        override fun getItemCount(): Int {
            return tracks.count()
        }
    }
}