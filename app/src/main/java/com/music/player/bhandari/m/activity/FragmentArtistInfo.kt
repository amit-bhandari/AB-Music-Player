package com.music.player.bhandari.m.activity

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import butterknife.BindView
import butterknife.ButterKnife
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.interfaces.DoubleClickListener
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadArtInfoThread
import com.music.player.bhandari.m.service.PlayerService
import com.music.player.bhandari.m.utils.UtilityFun
import com.wang.avi.AVLoadingIndicatorView
import java.io.*
import java.net.URL

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
class FragmentArtistInfo : Fragment(), ArtistInfo.Callback {
    private var layout: View? = null
    private var mArtistUpdateReceiver: BroadcastReceiver? = null
    private var mArtistInfo: ArtistInfo? = null

    @BindView(R.id.text_view_art_bio_frag)
    var artBioText: TextView? = null

    @BindView(R.id.retry_text_view)
    var retryText: TextView? = null

    @BindView(R.id.update_track_metadata)
    var updateTagsText: TextView? = null

    @BindView(R.id.loading_lyrics_animation)
    var lyricLoadAnimation: AVLoadingIndicatorView? = null

    @BindView(R.id.track_artist_artsi_bio_frag)
    var artistEdit: EditText? = null

    @BindView(R.id.button_update_metadata)
    var buttonUpdateMetadata: Button? = null

    //@BindView(R.id.ad_view_wrapper) View adViewWrapper;
    //@BindView(R.id.adView)  AdView mAdView;
    //@BindView(R.id.ad_close)  TextView adCloseText;
    private var playerService: PlayerService? = null
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        Log.v("frag", isVisibleToUser.toString() + "")
        /*if(isVisibleToUser && mAdView!=null){
            mAdView.resume();
        }else {
            if(mAdView!=null){
                mAdView.pause();
            }
        }*/super.setUserVisibleHint(isVisibleToUser)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.fragment_artist_info, container, false)
        ButterKnife.bind(this, layout!!)
        playerService = MyApp.getService()
        if (MyApp.getService() == null) {
            UtilityFun.restartApp()
            return layout
        }
        playerService = MyApp.getService()
        buttonUpdateMetadata!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val item: TrackItem = playerService!!.getCurrentTrack() ?: return
                val edited_artist: String = artistEdit!!.text.toString().trim { it <= ' ' }
                if (edited_artist.isEmpty()) {
                    Toast.makeText(context,
                        getString(R.string.te_error_empty_field),
                        Toast.LENGTH_SHORT).show()
                    return
                }
                if (!(edited_artist == item.getArtist())) {

                    //changes made, save those
                    val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    val values: ContentValues = ContentValues()
                    values.put(MediaStore.Audio.Media.ARTIST, edited_artist)
                    context!!.contentResolver
                        .update(uri,
                            values,
                            MediaStore.Audio.Media.TITLE + "=?",
                            arrayOf(item.title))
                    val intent: Intent = Intent(context, ActivityNowPlaying::class.java)
                    intent.putExtra("refresh", true)
                    intent.putExtra("position", playerService!!.getCurrentTrackPosition())
                    intent.putExtra("originalTitle", item.title)
                    intent.putExtra("title", item.title)
                    intent.putExtra("artist", edited_artist)
                    intent.putExtra("album", item.album)
                    startActivity(intent)
                    artistEdit!!.visibility = View.GONE
                    updateTagsText!!.visibility = View.GONE
                    buttonUpdateMetadata!!.visibility = View.GONE
                    buttonUpdateMetadata!!.isClickable = false
                    if (activity != null) {
                        val view: View? = activity!!.currentFocus
                        if (view != null) {
                            (activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(
                                view.windowToken,
                                0)
                        }
                    }
                    downloadArtInfo()
                } else {
                    Toast.makeText(context,
                        getString(R.string.change_tags_to_update),
                        Toast.LENGTH_SHORT).show()
                }
            }
        })

        //retry click listner
        layout!!.findViewById<View>(R.id.ll_art_bio)
            .setOnClickListener(object : DoubleClickListener() {
                override fun onSingleClick(v: View?) {
                    if (retryText!!.visibility == View.VISIBLE) {
                        retryText!!.visibility = View.GONE
                        artBioText!!.visibility = View.VISIBLE
                        artistEdit!!.visibility = View.GONE
                        updateTagsText!!.visibility = View.GONE
                        buttonUpdateMetadata!!.visibility = View.GONE
                        buttonUpdateMetadata!!.isClickable = false
                        lyricLoadAnimation!!.visibility = View.GONE
                        downloadArtInfo()
                    }
                }

                override fun onDoubleClick(v: View?) {

                    //if no connection text, do not hide artist content
                    if ((retryText!!.text.toString() == getString(R.string.no_connection))) {
                        return
                    }
                    if (artBioText!!.visibility == View.VISIBLE) {
                        artBioText!!.visibility = View.GONE
                    } else {
                        artBioText!!.visibility = View.VISIBLE
                    }
                }
            })

        //downloadArtInfo();
        mArtistUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                //already displayed, skip
                updateArtistInfoIfNeeded()
            }
        }
        return layout
    }

    private fun downloadArtInfo() {
        val item: TrackItem? = playerService!!.getCurrentTrack()
        if (item?.getArtist() == null) {
            return
        }
        artBioText!!.setText(getString(R.string.artist_info_loading))

        //set loading animation
        lyricLoadAnimation!!.visibility = View.VISIBLE
        lyricLoadAnimation!!.show()

        //see in offlinne db first
        mArtistInfo = OfflineStorageArtistBio.getArtistBioFromTrackItem(item)
        //second check is added to make sure internet call will happen
        //when user manually changes artist tag
        if (mArtistInfo != null && (item.getArtist()!!
                .trim { it <= ' ' } == mArtistInfo!!.getOriginalArtist()!!.trim())
        ) {
            onArtInfoDownloaded(mArtistInfo)
            return
        }
        if (UtilityFun.isConnectedToInternet) {
            var artist: String? = item.getArtist()
            artist = UtilityFun.filterArtistString(artist!!)
            DownloadArtInfoThread(this, artist, item).start()
        } else {
            artBioText!!.visibility = View.GONE
            retryText!!.text = getString(R.string.no_connection)
            retryText!!.visibility = View.VISIBLE
            lyricLoadAnimation!!.hide()
            lyricLoadAnimation!!.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mArtistUpdateReceiver!!)
    }

    override fun onResume() {
        super.onResume()
        if (MyApp.getService() != null) {
            updateArtistInfoIfNeeded()
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mArtistUpdateReceiver!!,
                IntentFilter(Constants.ACTION.UPDATE_LYRIC_AND_INFO))
        } else {
            UtilityFun.restartApp()
        }
    }

    private fun updateArtistInfoIfNeeded() {
        val item: TrackItem? = playerService!!.getCurrentTrack()
        if (item == null) {
            artBioText!!.visibility = View.GONE
            retryText!!.text = getString(R.string.no_music_found)
            //retryText.setVisibility(View.GONE);
            retryText!!.visibility = View.VISIBLE
            lyricLoadAnimation!!.hide()
            return
        }
        if (mArtistInfo != null && mArtistInfo!!.getOriginalArtist().equals(item.getArtist())) {
            return
        }

        //set loading  text and animation
        //set loading  text and animation
        downloadArtInfo()
    }

    override fun onArtInfoDownloaded(artistInfo: ArtistInfo?) {
        mArtistInfo = artistInfo
        if ((artistInfo == null) || (activity == null) || !isAdded) {
            return
        }
        val item: TrackItem? = playerService!!.getCurrentTrack()
        //if song is already changed , return
        if (item != null && !(item.getArtist()!!
                .trim { it <= ' ' } == artistInfo.getOriginalArtist()!!.trim())
        ) {
            //artBioText.setText(getString(R.string.artist_info_loading));
            return
        }
        //hide loading animation
        lyricLoadAnimation!!.hide()
        lyricLoadAnimation!!.visibility = View.GONE
        if (artistInfo.getArtistContent() == null) {
            retryText!!.text = getString(R.string.artist_info_no_result)
            retryText!!.visibility = View.VISIBLE
            artBioText!!.visibility = View.GONE
            val tempItem: TrackItem? = playerService!!.getCurrentTrack()
            if (tempItem != null) {
                artistEdit!!.visibility = View.VISIBLE
                updateTagsText!!.visibility = View.VISIBLE
                buttonUpdateMetadata!!.visibility = View.VISIBLE
                buttonUpdateMetadata!!.isClickable = true
                artistEdit!!.setText(tempItem.getArtist())
            }
            return
        }
        if ((layout != null) && (activity != null) && (artistInfo.getArtistContent() != null)) {
            Log.d("onArtInfoDownloaded", "onArtInfoDownloaded: " + artistInfo.getCorrectedArtist())
            val content = artistInfo.getArtistContent()
            val index: Int = content!!.indexOf("Read more")
            val ss = SpannableString(content)
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    if (mArtistInfo!!.getArtistUrl() == null) {
                        Toast.makeText(context,
                            getString(R.string.error_invalid_url),
                            Toast.LENGTH_SHORT).show()
                    } else {
                        try {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(
                                mArtistInfo!!.getArtistUrl()))
                            startActivity(browserIntent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context,
                                "No supporting application found for opening the link.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                    ds.typeface = Typeface.create(ds.typeface, Typeface.BOLD)
                }
            }
            if (index != -1) {
                ss.setSpan(clickableSpan, index, index + 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if (!(content == "")) {
                artBioText!!.visibility = View.VISIBLE
                retryText!!.visibility = View.GONE
                artBioText!!.text = ss
                artBioText!!.movementMethod = LinkMovementMethod.getInstance()
                artistEdit!!.visibility = View.GONE
                updateTagsText!!.visibility = View.GONE
                buttonUpdateMetadata!!.visibility = View.GONE
                buttonUpdateMetadata!!.isClickable = false
                artistEdit!!.setText("")
            } else {
                artBioText!!.visibility = View.GONE
                retryText!!.text = getString(R.string.artist_info_no_result)
                retryText!!.visibility = View.VISIBLE
                val tempItem: TrackItem? = playerService!!.getCurrentTrack()
                if (tempItem != null) {
                    artistEdit!!.visibility = View.VISIBLE
                    updateTagsText!!.visibility = View.VISIBLE
                    buttonUpdateMetadata!!.visibility = View.VISIBLE
                    buttonUpdateMetadata!!.isClickable = true
                    artistEdit!!.setText(tempItem.getArtist())
                }
            }

            //check current now playing background setting
            ///get current setting
            // 0 - System default   1 - artist image  2 - custom
            val currentNowPlayingBackPref: Int =
                MyApp.getPref()!!.getInt(getString(R.string.pref_now_playing_back), 1)
            if (currentNowPlayingBackPref == 1 && !artistInfo.getCorrectedArtist()
                    .equals("[unknown]")
            ) {
                if (!(activity as ActivityNowPlaying?)!!.isArtistLoadedInBack()) {
                    SetBlurryImagetask().execute(artistInfo)
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SetBlurryImagetask :
        AsyncTask<ArtistInfo?, String?, Bitmap?>() {
        var b: Bitmap? = null
        override fun doInBackground(vararg params: ArtistInfo?): Bitmap? {

            //store file in cache with artist id as name
            //create folder in cache for artist images
            val CACHE_ART_THUMBS: String =
                MyApp.getContext()!!.cacheDir.toString() + "/art_thumbs/"
            val actual_file_path: String = CACHE_ART_THUMBS + params[0]!!.getOriginalArtist()
            val f = File(CACHE_ART_THUMBS)
            if (!f.exists()) {
                f.mkdir()
            }
            if (!File(actual_file_path).exists()) {
                //create file
                val fos: FileOutputStream?
                try {
                    fos = FileOutputStream(File(actual_file_path))
                    val url = URL(params[0]!!.getImageUrl())
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

        override fun onPostExecute(b: Bitmap?) {

            //set background image
            if (b != null && activity != null) {
                (activity as ActivityNowPlaying?)!!.setBlurryBackground(b)
            }
        }
    }
}