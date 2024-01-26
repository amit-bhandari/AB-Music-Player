package com.music.player.bhandari.m.trackInfo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.activity.ActivityLyricView
import com.music.player.bhandari.m.databinding.ActivityTrackInfoBinding
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.trackInfo.models.FetchTrackInfo
import com.music.player.bhandari.m.trackInfo.models.RESULT
import com.music.player.bhandari.m.trackInfo.models.TrackInfo
import com.music.player.bhandari.m.utils.UtilityFun
import java.lang.NullPointerException
import java.util.*

class TrackInfoActivity: AppCompatActivity() , TrackInfo.Callback{

    private lateinit var  trackItem: TrackItem
    private var activityInBackground = true
    lateinit var binding: ActivityTrackInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ColorHelper.setStatusBarGradiant(this)

        val themeSelector = MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)

            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)

            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityTrackInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.indeterminateDrawable.setColorFilter(ColorHelper.getColor(R.color.colorwhite),
                android.graphics.PorterDuff.Mode.MULTIPLY)
        if(intent.extras?.getSerializable("trackItem")==null) throw NullPointerException("TrackItem can't be null")

        trackItem = intent.extras?.getSerializable("trackItem") as TrackItem

        FetchTrackInfo(trackItem.artist, trackItem.title, this).start()

        val toolbar = findViewById<Toolbar>(R.id.toolbar_)
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        if (supportActionBar != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        title = trackItem.title
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    @SuppressLint("SetTextI18n")
    override fun onTrackInfoReady(trackInfo: TrackInfo) {
        if(activityInBackground) {
            Log.d("TrackInfoActivity", "onTrackInfoReady : activity invisible, don't do shit")
            return
        }
        binding.progressBar.visibility = View.GONE
        if(trackInfo.result == RESULT.POSITIVE){
            //Glide.with(this).load("").crossFade().into(backgroundImage)
            Glide.with(this).load(trackInfo.track?.album?.image?.last()?.text ?: "").crossFade().into(binding.backgroundImage)

            binding.trackSection.visibility = View.VISIBLE
            binding.albumSection.visibility = View.VISIBLE
            binding.similarTrackSection.visibility = View.VISIBLE

            //positive result means its guaranteed track is found. Not sure about album and similar tracks, need to put null check
            binding.trackTitle.text = "Title : ${trackInfo.track?.name ?: ""}"
            binding.trackArtist.text = "Artist : ${trackInfo.track?.artist?.name ?: ""}"
            binding.trackDuration.text = "Duration : ${UtilityFun.msToString(trackInfo.track?.duration?.toLong() ?: 0)}"
            binding.trackPublishDate.text = "Published At : ${trackInfo.track?.wiki?.published ?: ""}"
            binding.trackPlaycount.text = "Play count : ${UtilityFun.coolFormat(trackInfo.track?.playcount?.toDouble() ?: 0.0, 0)}"

            if(trackInfo.track?.wiki?.content != null){
                binding.trackWiki.visibility = View.VISIBLE
                binding.trackWiki.text = trackInfo.track?.wiki?.content
            }

            binding.albumTitle.text = "Title : ${trackInfo.album?.name ?: ""}"
            binding.albumPlaycount.text = "Playcount : ${UtilityFun.coolFormat(trackInfo.album?.playcount?.toDouble() ?: 0.0, 0)}"

            val snappyLayoutManagerTrackTags = FlexboxLayoutManager(this)
            snappyLayoutManagerTrackTags.flexDirection = FlexDirection.ROW
            snappyLayoutManagerTrackTags.justifyContent = JustifyContent.FLEX_START

            binding.recyclerTrackTags.adapter = TagsAdapter(trackInfo.track?.toptags?.tag?.map { it.name } ?: listOf())
            binding.recyclerTrackTags.layoutManager = snappyLayoutManagerTrackTags

            if(trackInfo.album!=null){
                val snappyLayoutManagerAlbumTags = FlexboxLayoutManager(this)
                snappyLayoutManagerAlbumTags.flexDirection = FlexDirection.ROW
                snappyLayoutManagerAlbumTags.justifyContent = JustifyContent.FLEX_START

                binding.recyclerAlbumTags.adapter = TagsAdapter(trackInfo.album?.tags?.tag?.map { it.name } ?: listOf())
                binding.recyclerAlbumTags.layoutManager = snappyLayoutManagerAlbumTags

                if(trackInfo.album?.wiki?.content != null){
                    binding.albumWiki.visibility = View.VISIBLE
                    binding.albumWiki.text = trackInfo.album?.wiki?.content
                }

                binding.recyclerAlbumTracks.adapter = TracksAdapter(this,
                        trackInfo.album
                                ?.tracks?.track
                                ?.map { TracksAdapter.TrackItem(it.name
                                        , "Duration ${UtilityFun.msToString(it.duration.toLong()*1000)}"
                                        , trackInfo.track?.album?.image?.last()?.text ?: ""
                                        , it.url)}
                                ?: listOf())
                binding.recyclerAlbumTracks.layoutManager = LinearLayoutManager(this)
                binding.recyclerAlbumTracks.isNestedScrollingEnabled = false
            }else{
                binding.albumSection.visibility = View.GONE
            }

            if(trackInfo.similarTracks!=null){
                binding.recyclerSimilarTracks.adapter = TracksAdapter(this,
                        trackInfo.similarTracks
                                ?.track
                                ?.map { TracksAdapter.TrackItem(it.name
                                        , "${it.artist.name} | Match ${String.format("%.2f", it.match*100)}%"
                                        , it.image.last().text
                                        , it.url) } ?: listOf())
                binding.recyclerSimilarTracks.layoutManager = LinearLayoutManager(this)
                binding.recyclerSimilarTracks.isNestedScrollingEnabled = false
            }else{
                binding.similarTrackSection.visibility = View.GONE
            }

        }else{
            binding.outOfLuck.visibility = View.VISIBLE
            binding.trackSection.visibility = View.INVISIBLE
            binding.albumSection.visibility = View.INVISIBLE
            binding.similarTrackSection.visibility = View.INVISIBLE

            Snackbar.make(binding.rootTrackInfo, "Track information could not be found on last fm", Snackbar.LENGTH_INDEFINITE).show()
        }
    }

    fun launchLyricsView(trackTitle: String, trackArtist: String){
        val intent = Intent(this, ActivityLyricView::class.java)
        intent.putExtra("track_title", trackTitle)
        if(trackArtist.isEmpty()){
            //clicked on album traklist item
            intent.putExtra("artist", trackItem.artist)
        }else{
            //clicked on one of track from similar tracklist
            intent.putExtra("artist", trackArtist)
        }
        startActivity(intent)
       overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    class TagsAdapter(): RecyclerView.Adapter<TagsAdapter.MyViewHolder>(){

        private var tags: List<String> = mutableListOf()

        private val colors = arrayOf("#C62828","#AD1457","#6A1B9A", "#4527A0", "#1565C0", "#0277BD", "#2E7D32", "#64DD17", "#EF6C00", "#4E342E", "#424242", "#37474F")
        private val random = Random()

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
            (p0.itemView as CardView).setCardBackgroundColor(Color.parseColor(colors[random.nextInt(12)]))
        }

        override fun getItemCount(): Int {
            return tags.count()
        }
    }

    class TracksAdapter(): RecyclerView.Adapter<TracksAdapter.MyViewHolder>(){

        data class TrackItem(val trackTitle: String, val secondaryText: String, val imageUrl: String, val clickUrl: String)
        private var tracks:List<TrackItem> = mutableListOf()
        private lateinit var context: Context

        constructor(context: Context, tracks: List<TrackItem>):this(){
            this.context = context
            this.tracks = tracks
        }

        inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener{
            var trackTitle: TextView = view.findViewById(R.id.trackInfo)
            var secondaryText: TextView = view.findViewById(R.id.playCount)
            var imageView: ImageView = view.findViewById(R.id.imageView)

            init {
                //for some reason, on click on root view is not working
                trackTitle.setOnClickListener(this)
                secondaryText.setOnClickListener(this)
                imageView.setOnClickListener(this)
                itemView.findViewById<View>(R.id.more).setOnClickListener(this)
            }

            override fun onClick(p0: View?) {

                when(p0?.id){
                    R.id.trackInfo, R.id.playCount, R.id.imageView -> {
                        //below code is ugly, but it works. Will refactor later
                        if(secondaryText.text.contains("Duration"))
                            (context as TrackInfoActivity).launchLyricsView(tracks[adapterPosition].trackTitle, "")
                        else
                            (context as TrackInfoActivity).launchLyricsView(tracks[adapterPosition].trackTitle
                                    , tracks[adapterPosition].secondaryText.substring(0,tracks[adapterPosition].secondaryText.indexOf('|')))
                    }
                    R.id.more -> (context as TrackInfoActivity).openUrl(Uri.parse(tracks[adapterPosition].clickUrl))
                }

            }

        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
            val v = LayoutInflater.from(MyApp.getContext()).inflate(R.layout.track_item_square_image, p0, false)
            return MyViewHolder(v)
        }

        override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
            p0.trackTitle.text = tracks[p1].trackTitle
            p0.trackTitle.setTextColor(ColorHelper.getColor(R.color.colorwhite))
            p0.secondaryText.setTextColor(ColorHelper.getColor(R.color.colorwhite))
            p0.secondaryText.text = tracks[p1].secondaryText
        }

        override fun getItemCount(): Int {
            return tracks.count()
        }

    }

    private fun openUrl(parse: Uri) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, parse)
            startActivity(browserIntent)
        } catch (e: Exception) {
            //Snackbar.make(rootTrackInfo, getString(R.string.error_opening_browser), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("TrackInfoActivity", "onStart : ")
        activityInBackground = false
    }

    override fun onStop() {
        super.onStop()
        Log.d("TrackInfoActivity", "onStop : ")
        activityInBackground = true
    }
}