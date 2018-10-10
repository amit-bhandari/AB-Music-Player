package com.music.player.bhandari.m.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics
import kotlinx.android.synthetic.main.activity_saved_lyrics.*
import kotlinx.android.synthetic.main.item_saved_lyric.view.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.io.Serializable

class ActivitySavedLyrics: AppCompatActivity() {

    val adapter = SavedLyricsAdapter()
    val artistImageUrls = OfflineStorageArtistBio.getArtistImageUrls()

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

    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    inner class SavedLyricsAdapter: RecyclerView.Adapter<SavedLyricsAdapter.MyViewHolder>() {

        init{
            Handler().post {
                lyrics = OfflineStorageLyrics.getAllSavedLyrics()
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(this@ActivitySavedLyrics).inflate(R.layout.item_saved_lyric, parent, false))
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.itemView?.trackInfo?.text = lyrics[position].track
            holder.itemView?.playCount?.text = lyrics[position].artist
            Glide.with(this@ActivitySavedLyrics)
                    .load(artistImageUrls[lyrics[position].artist]).centerCrop().into(holder.itemView.imageView)
            holder.itemView?.setOnClickListener {
                val intent = Intent(this@ActivitySavedLyrics, ActivityLyricView::class.java)
                intent.putExtra("track_title", lyrics[position].track)
                intent.putExtra("artist", lyrics[position].artist)
                intent.putExtra("lyrics", lyrics[position] as Serializable)
                startActivity(intent)
            }
        }

        private var lyrics: MutableList<Lyrics> = mutableListOf()

        override fun getItemCount(): Int {
            return lyrics.size
        }

        inner class MyViewHolder(v: View): RecyclerView.ViewHolder(v)

    }

}