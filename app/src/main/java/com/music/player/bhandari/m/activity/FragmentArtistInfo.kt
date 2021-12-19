package com.music.player.bhandari.m.activity

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo

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
class FragmentArtistInfo constructor() : Fragment(), ArtistInfo.Callback {
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
    public override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        Log.v("frag", isVisibleToUser.toString() + "")
        /*if(isVisibleToUser && mAdView!=null){
            mAdView.resume();
        }else {
            if(mAdView!=null){
                mAdView.pause();
            }
        }*/super.setUserVisibleHint(isVisibleToUser)
    }

    public override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.fragment_artist_info, container, false)
        ButterKnife.bind(this, layout)
        playerService = MyApp.Companion.getService()
        if (MyApp.Companion.getService() == null) {
            UtilityFun.restartApp()
            return layout
        }
        playerService = MyApp.Companion.getService()
        buttonUpdateMetadata.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                val item: TrackItem? = playerService.getCurrentTrack()
                if (item == null) {
                    return
                }
                val edited_artist: String = artistEdit.getText().toString().trim({ it <= ' ' })
                if (edited_artist.isEmpty()) {
                    Toast.makeText(getContext(),
                        getString(R.string.te_error_empty_field),
                        Toast.LENGTH_SHORT).show()
                    return
                }
                if (!(edited_artist == item.getArtist())) {

                    //changes made, save those
                    val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    val values: ContentValues = ContentValues()
                    values.put(MediaStore.Audio.Media.ARTIST, edited_artist)
                    getContext()!!.getContentResolver()
                        .update(uri,
                            values,
                            MediaStore.Audio.Media.TITLE + "=?",
                            arrayOf(item.title))
                    val intent: Intent = Intent(getContext(), ActivityNowPlaying::class.java)
                    intent.putExtra("refresh", true)
                    intent.putExtra("position", playerService.getCurrentTrackPosition())
                    intent.putExtra("originalTitle", item.title)
                    intent.putExtra("title", item.title)
                    intent.putExtra("artist", edited_artist)
                    intent.putExtra("album", item.album)
                    startActivity(intent)
                    artistEdit.setVisibility(View.GONE)
                    updateTagsText.setVisibility(View.GONE)
                    buttonUpdateMetadata.setVisibility(View.GONE)
                    buttonUpdateMetadata.setClickable(false)
                    if (getActivity() != null) {
                        val view: View? = getActivity()!!.getCurrentFocus()
                        if (view != null) {
                            val imm: InputMethodManager? = getActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
                            }
                        }
                    }
                    downloadArtInfo()
                } else {
                    Toast.makeText(getContext(),
                        getString(R.string.change_tags_to_update),
                        Toast.LENGTH_SHORT).show()
                }
            }
        })

        //retry click listner
        layout!!.findViewById<View>(R.id.ll_art_bio)
            .setOnClickListener(object : DoubleClickListener() {
                fun onSingleClick(v: View?) {
                    if (retryText.getVisibility() == View.VISIBLE) {
                        retryText.setVisibility(View.GONE)
                        artBioText.setVisibility(View.VISIBLE)
                        artistEdit.setVisibility(View.GONE)
                        updateTagsText.setVisibility(View.GONE)
                        buttonUpdateMetadata.setVisibility(View.GONE)
                        buttonUpdateMetadata.setClickable(false)
                        lyricLoadAnimation.setVisibility(View.GONE)
                        downloadArtInfo()
                    }
                }

                fun onDoubleClick(v: View?) {

                    //if no connection text, do not hide artist content
                    if ((retryText.getText().toString() == getString(R.string.no_connection))) {
                        return
                    }
                    if (artBioText.getVisibility() == View.VISIBLE) {
                        artBioText.setVisibility(View.GONE)
                    } else {
                        artBioText.setVisibility(View.VISIBLE)
                    }
                }
            })

        //downloadArtInfo();
        mArtistUpdateReceiver = object : BroadcastReceiver() {
            public override fun onReceive(context: Context, intent: Intent) {
                //already displayed, skip
                updateArtistInfoIfNeeded()
            }
        }
        return layout
    }

    /* @OnClick(R.id.ad_close)
    public void close_ad(){
        if(mAdView!=null){
            mAdView.destroy();
        }
        adViewWrapper.setVisibility(View.GONE);
    }*/
    public override fun onDestroyView() {
        super.onDestroyView()

        /*if (mAdView != null) {
            mAdView.destroy();
        }*/
    }

    private fun downloadArtInfo() {
        val item: TrackItem? = playerService.getCurrentTrack()
        if (item == null || item.getArtist() == null) {
            return
        }
        artBioText.setText(getString(R.string.artist_info_loading))

        //set loading animation
        lyricLoadAnimation.setVisibility(View.VISIBLE)
        lyricLoadAnimation.show()

        //see in offlinne db first
        mArtistInfo = OfflineStorageArtistBio.getArtistBioFromTrackItem(item)
        //second check is added to make sure internet call will happen
        //when user manually changes artist tag
        if (mArtistInfo != null && (item.getArtist()!!
                .trim({ it <= ' ' }) == mArtistInfo.getOriginalArtist()!!.trim())
        ) {
            onArtInfoDownloaded(mArtistInfo)
            return
        }
        if (UtilityFun.isConnectedToInternet) {
            var artist: String? = item.getArtist()
            artist = UtilityFun.filterArtistString(artist)
            DownloadArtInfoThread(this, artist, item).start()
        } else {
            artBioText.setVisibility(View.GONE)
            retryText.setText(getString(R.string.no_connection))
            retryText.setVisibility(View.VISIBLE)
            lyricLoadAnimation.hide()
            lyricLoadAnimation.setVisibility(View.GONE)
        }
    }

    public override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mArtistUpdateReceiver)
    }

    public override fun onResume() {
        super.onResume()
        if (MyApp.Companion.getService() != null) {
            updateArtistInfoIfNeeded()
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mArtistUpdateReceiver,
                IntentFilter(Constants.ACTION.UPDATE_LYRIC_AND_INFO))
        } else {
            UtilityFun.restartApp()
        }
    }

    private fun updateArtistInfoIfNeeded() {
        val item: TrackItem? = playerService.getCurrentTrack()
        if (item == null) {
            artBioText.setVisibility(View.GONE)
            retryText.setText(getString(R.string.no_music_found))
            //retryText.setVisibility(View.GONE);
            retryText.setVisibility(View.VISIBLE)
            lyricLoadAnimation.hide()
            return
        }
        if (mArtistInfo != null && mArtistInfo.getOriginalArtist()
                .equals(item.getArtist())
        ) {
            return
        }

        //set loading  text and animation
        //set loading  text and animation
        downloadArtInfo()
    }

    override fun onArtInfoDownloaded(artistInfo: ArtistInfo?) {
        mArtistInfo = artistInfo
        if ((artistInfo == null) || (getActivity() == null) || !isAdded()) {
            return
        }
        val item: TrackItem? = playerService.getCurrentTrack()
        //if song is already changed , return
        if (item != null && !(item.getArtist()!!
                .trim({ it <= ' ' }) == artistInfo.getOriginalArtist()!!.trim())
        ) {
            //artBioText.setText(getString(R.string.artist_info_loading));
            return
        }
        //hide loading animation
        lyricLoadAnimation.hide()
        lyricLoadAnimation.setVisibility(View.GONE)
        if (artistInfo.getArtistContent() == null) {
            retryText.setText(getString(R.string.artist_info_no_result))
            retryText.setVisibility(View.VISIBLE)
            artBioText.setVisibility(View.GONE)
            val tempItem: TrackItem? = playerService.getCurrentTrack()
            if (tempItem != null) {
                artistEdit.setVisibility(View.VISIBLE)
                updateTagsText.setVisibility(View.VISIBLE)
                buttonUpdateMetadata.setVisibility(View.VISIBLE)
                buttonUpdateMetadata.setClickable(true)
                artistEdit.setText(tempItem.getArtist())
            }
            return
        }
        if ((layout != null) && (getActivity() != null) && (artistInfo.getArtistContent() != null)) {
            Log.d("onArtInfoDownloaded", "onArtInfoDownloaded: " + artistInfo.getCorrectedArtist())
            val content: String = artistInfo.getArtistContent()
            val index: Int = content.indexOf("Read more")
            val ss: SpannableString = SpannableString(content)
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                public override fun onClick(textView: View) {
                    if (mArtistInfo!!.getArtistUrl() == null) {
                        Toast.makeText(getContext(),
                            getString(R.string.error_invalid_url),
                            Toast.LENGTH_SHORT).show()
                    } else {
                        try {
                            val browserIntent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                                mArtistInfo!!.getArtistUrl()))
                            startActivity(browserIntent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(getContext(),
                                "No supporting application found for opening the link.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                public override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.setUnderlineText(true)
                    ds.setTypeface(Typeface.create(ds.getTypeface(), Typeface.BOLD))
                }
            }
            if (index != -1) {
                ss.setSpan(clickableSpan, index, index + 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if (!(content == "")) {
                artBioText.setVisibility(View.VISIBLE)
                retryText.setVisibility(View.GONE)
                artBioText.setText(ss)
                artBioText.setMovementMethod(LinkMovementMethod.getInstance())
                artistEdit.setVisibility(View.GONE)
                updateTagsText.setVisibility(View.GONE)
                buttonUpdateMetadata.setVisibility(View.GONE)
                buttonUpdateMetadata.setClickable(false)
                artistEdit.setText("")
            } else {
                artBioText.setVisibility(View.GONE)
                retryText.setText(getString(R.string.artist_info_no_result))
                retryText.setVisibility(View.VISIBLE)
                val tempItem: TrackItem? = playerService.getCurrentTrack()
                if (tempItem != null) {
                    artistEdit.setVisibility(View.VISIBLE)
                    updateTagsText.setVisibility(View.VISIBLE)
                    buttonUpdateMetadata.setVisibility(View.VISIBLE)
                    buttonUpdateMetadata.setClickable(true)
                    artistEdit.setText(tempItem.getArtist())
                }
            }

            //check current now playing background setting
            ///get current setting
            // 0 - System default   1 - artist image  2 - custom
            val currentNowPlayingBackPref: Int =
                MyApp.Companion.getPref().getInt(getString(R.string.pref_now_playing_back), 1)
            if (currentNowPlayingBackPref == 1 && !artistInfo.getCorrectedArtist()
                    .equals("[unknown]")
            ) {
                if (!(getActivity() as ActivityNowPlaying?)!!.isArtistLoadedInBack()) {
                    SetBlurryImagetask().execute(artistInfo)
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SetBlurryImagetask constructor() :
        AsyncTask<ArtistInfo?, String?, Bitmap?>() {
        var b: Bitmap? = null
        protected override fun doInBackground(vararg params: ArtistInfo): Bitmap {

            //store file in cache with artist id as name
            //create folder in cache for artist images
            val CACHE_ART_THUMBS: String =
                MyApp.Companion.getContext().getCacheDir().toString() + "/art_thumbs/"
            val actual_file_path: String = CACHE_ART_THUMBS + params.get(0).getOriginalArtist()
            val f: File = File(CACHE_ART_THUMBS)
            if (!f.exists()) {
                f.mkdir()
            }
            if (!File(actual_file_path).exists()) {
                //create file
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(File(actual_file_path))
                    val url: URL = URL(params.get(0).getImageUrl())
                    val inputStream: InputStream = url.openConnection().getInputStream()
                    val buffer: ByteArray = ByteArray(1024)
                    var bufferLength: Int = 0
                    while ((inputStream.read(buffer).also({ bufferLength = it })) > 0) {
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

        protected override fun onPostExecute(b: Bitmap) {

            //set background image
            if (b != null && getActivity() != null) {
                (getActivity() as ActivityNowPlaying?)!!.setBlurryBackground(b)
            }
        }
    }
}