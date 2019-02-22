package com.music.player.bhandari.m.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.signature.StringSignature
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics
import com.music.player.bhandari.m.utils.AppLaunchCountManager
import com.music.player.bhandari.m.utils.UtilityFun
import kotlinx.android.synthetic.main.activity_saved_lyrics.*
import kotlinx.android.synthetic.main.item_saved_lyric.view.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.io.Serializable
import java.util.HashMap
import java.util.concurrent.Executors

class ActivitySavedLyrics: AppCompatActivity() {

    private lateinit var mSearchAction: MenuItem
    private var isSearchOpened = false
    private var imm: InputMethodManager? = null
    private lateinit var editSearch: EditText

    val adapter = SavedLyricsAdapter()
    var artistImageUrls: HashMap<String, String> = hashMapOf()
    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        ColorHelper.setStatusBarGradiant(this)

        val themeSelector = MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)

            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)

            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_lyrics)
        showAdIfApplicable()

        val toolbar = findViewById<Toolbar>(R.id.toolbar_)
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        if (supportActionBar != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        recyclerViewSavedLyrics.adapter = adapter
        recyclerViewSavedLyrics.layoutManager = LinearLayoutManager(this)

        title = getString(R.string.nav_saved_lyrics)

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        Executors.newSingleThreadExecutor().execute {
            //load urls and lyrics in thread and update UI later
            artistImageUrls = OfflineStorageArtistBio.getArtistImageUrls()
            adapter.setLyrics(OfflineStorageLyrics.getAllSavedLyrics())
            handler.post {
                progressBarSavedLyrics.visibility =View.GONE
                if(adapter.isEmpty()){
                    emptyLyrics.visibility = View.VISIBLE
                }else{
                    recyclerViewSavedLyrics.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_saved_lyrics, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        mSearchAction = menu?.findItem(R.id.action_search)!!
        if (isSearchOpened) {
            mSearchAction.icon = ContextCompat.getDrawable(this, R.drawable.ic_close_white_24dp)
        } else {
            mSearchAction.icon = ContextCompat.getDrawable(this, R.drawable.ic_search_white_48dp)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.action_search -> {handleSearch()}
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleSearch() {
        if (isSearchOpened) { //test if the search is open
            if (supportActionBar != null) {
                supportActionBar!!.setDisplayShowCustomEnabled(false)
                supportActionBar!!.setDisplayShowTitleEnabled(true)
            }

            //hides the keyboard
            var view = currentFocus
            if (view == null) {
                view = View(this)
            }
            imm?.hideSoftInputFromWindow(view.windowToken, 0)

            //add the search icon in the action bar
            mSearchAction.icon = ContextCompat.getDrawable(this, R.drawable.ic_search_white_48dp)
            adapter.filter("")

            isSearchOpened = false
        } else { //open the search entry

            if (supportActionBar != null) {
                supportActionBar!!.setDisplayShowCustomEnabled(true) //enable it to display a custom view
                supportActionBar!!.setCustomView(R.layout.search_bar_layout)//add the custom view
                supportActionBar!!.setDisplayShowTitleEnabled(false) //hide the title
            }
            editSearch = supportActionBar!!.customView.findViewById(R.id.edtSearch) //the text editor
            editSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    // TODO Auto-generated method stub
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    // TODO Auto-generated method stub
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    Log.d("Search", s.toString())
                    adapter.filter(s.toString())
                }
            })
            editSearch.setOnClickListener { imm?.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT) }

            editSearch.requestFocus()

            //open the keyboard focused in the edtSearch
            imm?.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT)

            mSearchAction.icon = ContextCompat.getDrawable(this, R.drawable.ic_close_white_24dp)
            //add the close icon
            //mSearchAction.setIcon(getResources().getDrawable(R.drawable.cancel));
            isSearchOpened = true
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    private fun showAdIfApplicable() {
        if (/*AppLaunchCountManager.isEligibleForInterstialAd() && */ !UtilityFun.isAdsRemoved() && AppLaunchCountManager.isEligibleForBannerAds()) {
            MobileAds.initialize(this, getString(R.string.banner_lyric_view))
            if (UtilityFun.isConnectedToInternet()) {
                val adRequest = AdRequest.Builder()//.addTestDevice("C6CC5AB32A15AF9EFB67D507C151F23E")
                        .build()
                if (adView != null) {
                    adView.loadAd(adRequest)
                    adView.visibility = View.VISIBLE
                    ad_view_wrapper.visibility = View.VISIBLE
                    ad_close.visibility = View.VISIBLE
                    ad_close.setOnClickListener {
                        if (adView != null) {
                            adView.destroy()
                        }
                        ad_view_wrapper.visibility = View.GONE
                    }
                }
            } else {
                if (adView != null) {
                    adView.visibility = View.GONE
                    ad_view_wrapper.visibility = View.GONE
                }
            }
        }
    }

    inner class SavedLyricsAdapter: RecyclerView.Adapter<SavedLyricsAdapter.MyViewHolder>() {

        init{
            //setHasStableIds(true)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(this@ActivitySavedLyrics).inflate(R.layout.item_saved_lyric, parent, false))
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.itemView.trackInfo?.text = lyrics[position].track
            holder.itemView.playCount?.text = lyrics[position].artist
            holder.itemView.delete?.isEnabled = true
            Glide.with(this@ActivitySavedLyrics)
                    .load(artistImageUrls[lyrics[position].originalArtist])
                    .asBitmap()
                    .thumbnail(0.5f)
                    //.signature(StringSignature(System.currentTimeMillis().toString()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(object: SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                            holder.itemView.imageView.setImageBitmap(resource)
                        }
                    })
        }

        private var lyrics: MutableList<Lyrics> = mutableListOf()
        private var copyLyrics: MutableList<Lyrics> = mutableListOf()

        fun setLyrics(lyrics: MutableList<Lyrics>){
            this.lyrics = lyrics
            copyLyrics.addAll(lyrics)
        }

        fun isEmpty(): Boolean{
            return lyrics.isEmpty()
        }

        override fun getItemCount(): Int {
            return lyrics.size
        }

        fun filter(keyword: String){
            lyrics.clear()
            if(keyword.isEmpty()){
                lyrics.addAll(copyLyrics)
            }else{
                copyLyrics.forEach { lyric ->
                    if(lyric.track.contains(keyword, true) || lyric.artist.contains(keyword, true))
                        lyrics.add(lyric)
                }
            }
            notifyDataSetChanged()
        }

        /*override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }*/

        inner class MyViewHolder(v: View): RecyclerView.ViewHolder(v), View.OnClickListener {

            init {
                v.setOnClickListener(this)
                v.delete.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                val position = adapterPosition  //adapter position changes sometimes in between, don't know why
                when(v?.id){
                    R.id.root_view_item_saved_lyrics -> {
                        val intent = Intent(this@ActivitySavedLyrics, ActivityLyricView::class.java)
                        intent.putExtra("track_title", lyrics[position].originalTrack)
                        intent.putExtra("artist", lyrics[position].originalArtist)
                        intent.putExtra("lyrics", lyrics[position] as Serializable)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                    R.id.delete ->{
                        v.isEnabled = false  //to prevent double clicks
                        if (OfflineStorageLyrics.clearLyricsFromDB(lyrics[position].originalTrack, lyrics[position].trackId)) {
                            Snackbar.make(v, getString(R.string.lyrics_removed), Snackbar.LENGTH_SHORT).show()
                            lyrics.removeAt(position)
                            notifyItemRemoved(position)
                        }else{
                            v.isEnabled = true //enable click again
                            Snackbar.make(v, getString(R.string.error_removing), Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    }

}