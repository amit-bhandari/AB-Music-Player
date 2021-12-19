package com.music.player.bhandari.m.lyricCard

import android.R
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.provider.FontRequest
import com.afollestad.materialdialogs.DialogAction
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.target.Target
import com.music.player.bhandari.m.model.Constants
import java.lang.reflect.Type

/**
 * / **
 * Copyright 2017 Amit Bhandari AB
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * I know, I know, there is not even bit of "Good Architecture" is followed in this class
 * Everything is mixed up, UI, data, card creation. Everything.
 * But its too late for this app to follow architectural guidelines.
 * You agree with me? Cheers.
 * If not. May god help you.
 */
class ActivityLyricCard constructor() : AppCompatActivity(), OnTouchListener {
    private val PICK_IMAGE: Int = 0

    @BindView(R.id.rv_colors)
    var recyclerViewColors: RecyclerView? = null

    @BindView(R.id.rv_images)
    var recyclerViewImages: RecyclerView? = null

    @BindView(R.id.mainImageLyricCard)
    var mainImage: ImageView? = null

    @BindView(R.id.text_lyric)
    var lyricText: ZoomTextView? = null

    @BindView(R.id.text_artist)
    var artistText: ZoomTextView? = null

    @BindView(R.id.text_track)
    var trackText: ZoomTextView? = null

    @BindView(R.id.dragView)
    var dragView: View? = null

    @BindView(R.id.progressBar)
    var progressBar: ProgressBar? = null

    @BindView(R.id.brightnessSeekBar)
    var brightnessSeekBar: SeekBar? = null

    @BindView(R.id.overImageLayer)
    var overImageLayer: View? = null

    @BindView(R.id.watermark)
    var watermark: View? = null

    @BindView(R.id.root_view_lyric_card)
    var rootView: View? = null
    var dx //for dragging text views
            : Float = 0f
    var dy: Float = 0f
    private val imagesAdapter: ImagesAdapter = ImagesAdapter()
    private var mHandler: Handler? = null
    private var currentTextAlignment: Int = 0
    private val typefaces: MutableList<Typeface> = ArrayList<Typeface>()
    var currentFontPosition: Int = 0
    var typefaceSet: Boolean = false
    protected override fun onCreate(savedInstanceState: Bundle?) {
        ColorHelper.setStatusBarGradiant(this)
        setTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lyric_card)
        ButterKnife.bind(this)
        if (getIntent().getExtras() == null) {
            Toast.makeText(this, "Missing lyric text", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        if (MyApp.Companion.getPref().getBoolean("pref_first_time_lyric_card_launch", true)) {
            MyApp.Companion.getPref().edit().putBoolean("pref_first_time_lyric_card_launch", false)
                .apply()
            firstTimeLaunch()
        }
        val text: String
        val author: String
        val track: String
        if (getIntent().getExtras().getString("lyric") != null) {
            text = getIntent().getExtras().getString("lyric")
        } else {
            text = ""
        }
        if (getIntent().getExtras().getString("artist") != null) {
            author = getIntent().getExtras().getString("artist")
        } else {
            author = ""
        }
        if (getIntent().getExtras().getString("track") != null) {
            track = getIntent().getExtras().getString("track")
        } else {
            track = ""
        }
        lyricText.setText(text)
        artistText.setText(author)
        trackText.setText(track)
        initiateToolbar()
        fillFonts()
        initiateUI()
    }

    protected override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    private fun setTheme() {
        //if player service not running, kill the app
        if (MyApp.Companion.getService() == null) {
            UtilityFun.restartApp()
        }
        val themeSelector: Int = MyApp.Companion.getPref()
            .getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
    }

    private fun initiateToolbar() {
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar_)
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true)
            getSupportActionBar().setDisplayShowHomeEnabled(true)
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.GetStatusBarColor());
        }*/setTitle("Lyric Card")
    }

    private fun initiateUI() {
        //rootView.setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());
        recyclerViewColors.setLayoutManager(LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,
            false))
        recyclerViewColors.setAdapter(ColorAdapter())
        recyclerViewImages.setLayoutManager(LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,
            false))
        recyclerViewImages.setAdapter(imagesAdapter)

        //get images links
        val type: Type = object : TypeToken<Map<String?, String?>?>() {}.getType()
        val urls: Map<String?, String?>?
        urls = Gson().fromJson<Map<String, String>>(MyApp.Companion.getPref()
            .getString(getString(R.string.pref_card_image_links), ""), type)
        if ((System.currentTimeMillis() >= MyApp.Companion.getPref()
                .getLong(getString(R.string.pref_card_image_saved_at), 0) + DAYS_UNTIL_CACHE
                    && urls != null)
        ) {
            imagesAdapter.setUrls(urls)
        } else {
            FirebaseDatabase.getInstance().getReference().child("cardlinksNew")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    public override fun onDataChange(dataSnapshot: DataSnapshot) {
                        Log.d("ActivityLyricCard", "onDataChange: ")
                        val urls: MutableMap<String?, String?> = LinkedHashMap()
                        for (snap: DataSnapshot in dataSnapshot.getChildren()) {
                            try {
                                val map: Map<String, String> = snap.getValue()
                                urls.put(map.get("thumb"), map.get("image"))
                            } catch (ignored: Exception) {
                            }
                        }
                        imagesAdapter.setUrls(urls)

                        //cache links in shared pref
                        MyApp.Companion.getPref().edit()
                            .putString(getString(R.string.pref_card_image_links),
                                Gson().toJson(urls)).apply()
                        MyApp.Companion.getPref().edit()
                            .putLong(getString(R.string.pref_card_image_saved_at),
                                System.currentTimeMillis()).apply()
                    }

                    public override fun onCancelled(databaseError: DatabaseError) {
                        Log.d("ActivityLyricCard", "onCancelled: " + databaseError.getMessage())
                        Toast.makeText(this@ActivityLyricCard,
                            "Error retrieving images from server",
                            Toast.LENGTH_SHORT).show()
                    }
                })
        }
        initiateDragView()
        brightnessSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            public override fun onProgressChanged(seekBar: SeekBar, `in`: Int, b: Boolean) {
                val i: Double = seekBar.getProgress() / 100.0
                val alpha: Int = Math.round(i * 255).toInt()
                var hex: String = Integer.toHexString(alpha).toUpperCase()
                if (hex.length == 1) hex = "0" + hex
                try {
                    overImageLayer!!.setBackgroundColor(Color.parseColor("#" + hex + "000000"))
                } catch (e: NumberFormatException) {
                    Log.d("ActivityLyricCard", "onProgressChanged: Color parse exception")
                }
            }

            public override fun onStartTrackingTouch(seekBar: SeekBar) {}
            public override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        brightnessSeekBar.setProgress(30)
    }

    private fun initiateDragView() {
        artistText.setOnTouchListener(this)
        lyricText.setOnTouchListener(this)
        trackText.setOnTouchListener(this)
    }

    private fun firstTimeLaunch() {
        if (UtilityFun.isConnectedToInternet) {
            Toast.makeText(this, R.string.first_launch_lyric_card, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.first_launch_lyric_card_no_internet, Toast.LENGTH_LONG)
                .show()
        }
    }

    public override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.getAction()) {
            MotionEvent.ACTION_DOWN -> {
                dx = v.getX() - event.getRawX()
                dy = v.getY() - event.getRawY()
            }
            MotionEvent.ACTION_MOVE -> when (v.getId()) {
                R.id.text_artist -> artistText.animate()
                    .x(event.getRawX() + dx)
                    .y(event.getRawY() + dy)
                    .setDuration(0)
                    .start()
                R.id.text_lyric -> lyricText.animate()
                    .x(event.getRawX() + dx)
                    .y(event.getRawY() + dy)
                    .setDuration(0)
                    .start()
                R.id.text_track -> trackText.animate()
                    .x(event.getRawX() + dx)
                    .y(event.getRawY() + dy)
                    .setDuration(0)
                    .start()
            }
            else -> return false
        }
        return false
    }

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.menu_lyric_card, menu)
        Handler().postDelayed(object : Runnable {
            public override fun run() {
                try {
                    if (!MyApp.Companion.getPref()
                            .getBoolean(getString(R.string.pref_info_lyric_card_shown), false)
                    ) {
                        showFirstTimeInfo()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 1000)
        return true
    }

    private fun showFirstTimeInfo() {
        TapTargetSequence(this@ActivityLyricCard)
            .targets(
                TapTarget.forView(findViewById<View>(R.id.action_font),
                    "Change text font by clicking here")
                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                    .outerCircleAlpha(0.9f)
                    .transparentTarget(true)
                    .titleTextColor(R.color.colorwhite)
                    .descriptionTextColor(R.color.colorwhite)
                    .drawShadow(true)
                    .tintTarget(true),
                TapTarget.forView(findViewById<View>(R.id.action_alignment),
                    "Change text alignment by clicking here")
                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                    .outerCircleAlpha(0.9f)
                    .transparentTarget(true)
                    .titleTextColor(R.color.colorwhite)
                    .descriptionTextColor(R.color.colorwhite)
                    .drawShadow(true)
                    .tintTarget(true),
                TapTarget.forView(findViewById<View>(R.id.action_edit),
                    "Edit lyric text by clicking here")
                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                    .outerCircleAlpha(0.9f)
                    .transparentTarget(true)
                    .titleTextColor(R.color.colorwhite)
                    .descriptionTextColor(R.color.colorwhite)
                    .drawShadow(true)
                    .tintTarget(true),
                TapTarget.forView(findViewById<View>(R.id.black_overlay_wrap),
                    "Blacken background image by using this slider")
                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                    .outerCircleAlpha(0.9f)
                    .transparentTarget(true)
                    .titleTextColor(R.color.colorwhite)
                    .descriptionTextColor(R.color.colorwhite)
                    .drawShadow(true)
                    .tintTarget(true),
                TapTarget.forView(findViewById<View>(R.id.rv_colors),
                    "Choose text color among given options")
                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                    .outerCircleAlpha(0.9f)
                    .transparentTarget(true)
                    .titleTextColor(R.color.colorwhite)
                    .descriptionTextColor(R.color.colorwhite)
                    .drawShadow(true)
                    .tintTarget(true),
                TapTarget.forView(findViewById<View>(R.id.text_artist),
                    "You can remove artist or album text by dragging it towards bottom of image.")
                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                    .outerCircleAlpha(0.9f)
                    .transparentTarget(true)
                    .titleTextColor(R.color.colorwhite)
                    .descriptionTextColor(R.color.colorwhite)
                    .drawShadow(true)
                    .tintTarget(true),
                TapTarget.forView(recyclerViewImages.getLayoutManager().findViewByPosition(0),
                    "You can select custom or artist image from here for background.")
                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                    .outerCircleAlpha(0.9f)
                    .transparentTarget(true)
                    .titleTextColor(R.color.colorwhite)
                    .descriptionTextColor(R.color.colorwhite)
                    .drawShadow(true)
                    .tintTarget(true)
            )
            .continueOnCancel(true)
            .considerOuterCircleCanceled(true)
            .listener(object : TapTargetSequence.Listener {
                // This listener will tell us when interesting(tm) events happen in regards
                // to the sequence
                public override fun onSequenceFinish() {
                    // Yay
                    MyApp.Companion.getPref().edit()
                        .putBoolean(getString(R.string.pref_info_lyric_card_shown), true).apply()
                }

                public override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}
                public override fun onSequenceCanceled(lastTarget: TapTarget) {
                    // Boo
                }
            }).start()
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> finish()
            R.id.action_share -> shareCard()
            R.id.action_font -> changeFont()
            R.id.action_alignment -> changeAlignment()
            R.id.action_edit -> showTextEditDialog()
            R.id.action_save -> {
                val f: File? = createImageFile(false)
                if (f != null) {
                    Toast.makeText(this,
                        "Lyric card is saved at " + f.getAbsolutePath(),
                        Toast.LENGTH_SHORT).show()
                }
            }
            R.id.action_remove_watermark -> if (item.isChecked()) {
                item.setChecked(false)
                watermark!!.setVisibility(View.VISIBLE)
            } else {
                item.setChecked(true)
                watermark!!.setVisibility(View.INVISIBLE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createImageFile(temp: Boolean): File {
        dragView!!.destroyDrawingCache() //if not done, image is going to be overridden every time
        dragView!!.setDrawingCacheEnabled(true)
        val bitmap: Bitmap = dragView!!.getDrawingCache()
        val dir: File = File(Environment.getExternalStorageDirectory().toString() + "/abmusic")
        dir.mkdirs()
        var fileName: String? = "temp.jpeg"
        if (!temp) {
            fileName = UUID.randomUUID().toString() + ".jpeg"
        }
        val lyricCardFile: File = File(dir, fileName)
        if (lyricCardFile.exists()) lyricCardFile.delete()
        try {
            val stream: FileOutputStream = FileOutputStream(lyricCardFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: Exception) {
            Log.d("ActivityLyricCard", "shareCard: " + e.getLocalizedMessage())
            Toast.makeText(this, "Error saving card on storage ", Toast.LENGTH_SHORT).show()
        }
        return lyricCardFile
    }

    private fun shareCard() {
        val lyricCardFile: File? = createImageFile(true)
        if (lyricCardFile == null) return
        try {
            val share: Intent = Intent(Intent.ACTION_SEND)
            share.setType("image/*")
            share.putExtra(Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + "com.bhandari.music.provider",
                    lyricCardFile))
            share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_lyric_card_extra_text))
            startActivity(Intent.createChooser(share, "Share Lyric Card"))
        } catch (e: Exception) {
            Toast.makeText(this,
                "Error while sharing, lyric card is saved at " + lyricCardFile.getAbsolutePath(),
                Toast.LENGTH_LONG).show()
        }
    }

    private fun fillFonts() {
        requestDownload(QueryBuilder("Trade Winds").build()) //unique
        requestDownload(QueryBuilder("Indie Flower").build()) //unique
        requestDownload(QueryBuilder("Satisfy").build()) //unique
        requestDownload(QueryBuilder("Ubuntu").build()) //unique
        requestDownload(QueryBuilder("Roboto Slab").build()) //unique
        requestDownload(QueryBuilder("Cabin Sketch").build()) //good
        requestDownload(QueryBuilder("Condiment").build()) //good cursue
        requestDownload(QueryBuilder("Caveat Brush").build())
        requestDownload(QueryBuilder("Cherry Swash").build()) //unique
        requestDownload(QueryBuilder("Concert One").build()) //unique
        requestDownload(QueryBuilder("Nova Round").build()) //unique
        requestDownload(QueryBuilder("Nova Script").build()) //unique
        requestDownload(QueryBuilder("Pacifico").build()) //unique
        requestDownload(QueryBuilder("Prompt").build()) //unique
        requestDownload(QueryBuilder("Purple Purse").build()) //unique
        requestDownload(QueryBuilder("Quantico").build()) //unique
        requestDownload(QueryBuilder("name=Raleway&amp;weight=700").build()) //unique
        requestDownload(QueryBuilder("Roboto").build()) //unique
        requestDownload(QueryBuilder("Slabo 13px").build()) //unique
        requestDownload(QueryBuilder("Source Sans Pro").build()) //unique
        requestDownload(QueryBuilder("Montserrat").build()) //unique1
        requestDownload(QueryBuilder("Lora").build())
    }

    private fun requestDownload(query: String) {
        Log.d("ActivityLyricCard", "requestDownload: " + query)
        val request: FontRequest = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            query,
            R.array.com_google_android_gms_fonts_certs)
        val callback: FontsContractCompat.FontRequestCallback =
            object : FontsContractCompat.FontRequestCallback() {
                public override fun onTypefaceRetrieved(typeface: Typeface) {
                    Log.d("ActivityLyricCard", "onTypefaceRetrieved: " + typeface.toString())
                    if (!typefaceSet) {
                        lyricText.setTypeface(typeface)
                        artistText.setTypeface(typeface)
                        trackText.setTypeface(typeface)
                        typefaceSet = true
                    }
                    typefaces.add(typeface)
                }

                public override fun onTypefaceRequestFailed(reason: Int) {
                    Log.d("ActivityLyricCard", "onTypefaceRequestFailed: " + reason)
                }
            }
        FontsContractCompat
            .requestFont(this, request, callback,
                getHandlerThreadHandler())
    }

    private fun getHandlerThreadHandler(): Handler? {
        if (mHandler == null) {
            val handlerThread: HandlerThread = HandlerThread("fonts")
            handlerThread.start()
            mHandler = Handler(handlerThread.getLooper())
        }
        return mHandler
    }

    private fun changeFont() {
        if (currentFontPosition >= typefaces.size - 1) {
            currentFontPosition = 0
            lyricText.setTypeface(typefaces.get(currentFontPosition))
            artistText.setTypeface(typefaces.get(currentFontPosition))
            trackText.setTypeface(typefaces.get(currentFontPosition))
        } else {
            val index: Int = ++currentFontPosition
            lyricText.setTypeface(typefaces.get(index))
            artistText.setTypeface(typefaces.get(index))
            trackText.setTypeface(typefaces.get(index))
        }
    }

    private fun changeAlignment() {
        when (currentTextAlignment) {
            1 -> {
                lyricText.setGravity(Gravity.END)
                artistText.setGravity(Gravity.END)
                trackText.setGravity(Gravity.END)
                currentTextAlignment = 2
            }
            2 -> {
                lyricText.setGravity(Gravity.START)
                artistText.setGravity(Gravity.START)
                trackText.setGravity(Gravity.START)
                currentTextAlignment = 0
            }
            else -> {
                lyricText.setGravity(Gravity.CENTER)
                artistText.setGravity(Gravity.CENTER)
                trackText.setGravity(Gravity.CENTER)
                currentTextAlignment = 1
            }
        }
    }

    private fun setMainImage(url: String) {
        progressBar.setVisibility(View.VISIBLE)
        Glide.with(getApplicationContext())
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable?> {
                public override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.setVisibility(View.GONE)
                    Toast.makeText(this@ActivityLyricCard,
                        R.string.error_loading_image_lyric_card,
                        Toast.LENGTH_SHORT).show()
                    return false
                }

                public override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.setVisibility(View.GONE)
                    return false
                }
            })
            .into(mainImage)
    }

    private fun setMainImage(uri: Uri) {
        progressBar.setVisibility(View.VISIBLE)
        Glide.with(this@ActivityLyricCard)
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable?> {
                public override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.setVisibility(View.GONE)
                    Toast.makeText(this@ActivityLyricCard,
                        R.string.error_loading_image_lyric_card,
                        Toast.LENGTH_SHORT).show()
                    return false
                }

                public override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.setVisibility(View.GONE)
                    return false
                }
            }).into(mainImage)
    }

    fun addCustomImage() {
        val intent: Intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
    }

    fun addArtistImage() {
        mainImage.setImageBitmap(null)
        progressBar.setVisibility(View.VISIBLE)
        val info: ArtistInfo? =
            OfflineStorageArtistBio.getArtistInfoFromCache(artistText.getText().toString())
        Log.d("ActivityLyricCard", "addArtistImage: " + info)
        if (info != null) {
            setMainImage(info.getImageUrl())
        } else if (UtilityFun.isConnectedToInternet) {
            val artist: String = UtilityFun.filterArtistString(artistText.getText().toString())
            DownloadArtInfoThread(object : Callback() {
                fun onArtInfoDownloaded(artistInfo: ArtistInfo?) {
                    if (artistInfo != null && !artistInfo.getImageUrl().isEmpty()) {
                        setMainImage(artistInfo.getImageUrl())
                    } else {
                        Toast.makeText(this@ActivityLyricCard,
                            "Artist image not found",
                            Toast.LENGTH_SHORT).show()
                        if (imagesAdapter.urls!!.size > 0) {
                            setMainImage(imagesAdapter.urls!!.get(0))
                        }
                    }
                }
            }, artist, null).start()
        } else {
            progressBar.setVisibility(View.INVISIBLE)
            Toast.makeText(this, "Not connected to internet", Toast.LENGTH_SHORT).show()
        }
    }

    fun showTextEditDialog() {
        val builder: MaterialDialog.Builder = MyDialogBuilder(this)
            .title("Edit text")
            .positiveText(getString(R.string.okay))
            .onPositive(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    val view: View? = dialog.getCustomView()
                    if (view == null) return
                    val lyric: AppCompatEditText = view.findViewById(R.id.text_lyric)
                    val artist: AppCompatEditText = view.findViewById(R.id.text_artist)
                    val track: AppCompatEditText = view.findViewById(R.id.text_track)
                    lyricText.setText(lyric.getText())
                    artistText.setText(artist.getText())
                    trackText.setText(track.getText())
                }
            })
            .customView(R.layout.dialog_edit_lyric_card_texts, true)
        val dialog: MaterialDialog = builder.build()
        val view: View? = dialog.getCustomView()
        if (view == null) return
        val lyric: AppCompatEditText = view.findViewById(R.id.text_lyric)
        val artist: AppCompatEditText = view.findViewById(R.id.text_artist)
        val track: AppCompatEditText = view.findViewById(R.id.text_track)
        lyric.setText(lyricText.getText())
        artist.setText(artistText.getText())
        track.setText(trackText.getText())
        dialog.show()
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE) {
            Log.d("ActivityLyricCard", "onActivityResult: " + data)
            if (data != null && data.getData() != null) setMainImage(data.getData()) else Toast.makeText(
                this,
                "Error loading image",
                Toast.LENGTH_SHORT).show()
        }
    }

    internal inner class ColorAdapter constructor() :
        RecyclerView.Adapter<ColorAdapter.MyViewHolder?>() {
        var colors: Array<String> = getResources().getStringArray(R.array.colors)
        public override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val v: View = LayoutInflater.from(this@ActivityLyricCard)
                .inflate(R.layout.item_color, parent, false)
            return MyViewHolder(v)
        }

        public override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.color.setBackgroundColor(Color.parseColor(colors.get(position)))
        }

        public override fun getItemCount(): Int {
            return colors.size
        }

        internal inner class MyViewHolder constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var color: ImageView

            init {
                itemView.setOnClickListener(object : View.OnClickListener {
                    public override fun onClick(view: View) {
                        lyricText.setTextColor(Color.parseColor(colors.get(getLayoutPosition())))
                        artistText.setTextColor(Color.parseColor(colors.get(getLayoutPosition())))
                        trackText.setTextColor(Color.parseColor(colors.get(getLayoutPosition())))
                    }
                })
                color = itemView.findViewById<ImageView>(R.id.colorView)
            }
        }
    }

    internal inner class ImagesAdapter constructor() :
        RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
        var urls: Map<String?, String?>? = LinkedHashMap()
        public override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerView.ViewHolder {
            val v: View
            when (viewType) {
                0 -> {
                    v = LayoutInflater.from(this@ActivityLyricCard)
                        .inflate(R.layout.item_custom_image, parent, false)
                    return CustomHolder(v)
                }
                1 -> {
                    v = LayoutInflater.from(this@ActivityLyricCard)
                        .inflate(R.layout.item_artist_image, parent, false)
                    return ArtistHolder(v)
                }
                2 -> {
                    v = LayoutInflater.from(this@ActivityLyricCard)
                        .inflate(R.layout.item_image_lyric_card, parent, false)
                    return ImageHolder(v)
                }
            }
            return null
        }

        public override fun getItemViewType(position: Int): Int {
            when (position) {
                0 -> return 0
                1 -> return 1
                else -> return 2
            }
        }

        public override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.getItemViewType()) {
                1 -> {
                    //load artist image in thumbnail view
                    val info: ArtistInfo? =
                        OfflineStorageArtistBio.getArtistInfoFromCache(artistText.getText()
                            .toString())
                    Log.d("ImagesAdapter", "onBindViewHolder: " + info)
                    if (info != null) {
                        (holder as ArtistHolder).progressBar.setVisibility(View.VISIBLE)
                        loadImageUsingGlide(info.getImageUrl(),
                            (holder as ArtistHolder).imageView,
                            (holder as ArtistHolder).progressBar)
                    } else if (UtilityFun.isConnectedToInternet) {
                        val artist: String =
                            UtilityFun.filterArtistString(artistText.getText().toString())
                        DownloadArtInfoThread(object : Callback() {
                            fun onArtInfoDownloaded(artistInfo: ArtistInfo?) {
                                if (artistInfo != null && !artistInfo.getImageUrl().isEmpty()) {
                                    loadImageUsingGlide(artistInfo.getImageUrl(),
                                        (holder as ArtistHolder).imageView,
                                        (holder as ArtistHolder).progressBar)
                                }
                            }
                        }, artist, null).start()
                    }
                }
                2 -> if (holder is ImageHolder) {
                    (holder as ImageHolder).progressBar.setVisibility(View.VISIBLE)
                    loadImageUsingGlide(getThumbElementByIndex(position - 2),
                        (holder as ImageHolder).imageView,
                        (holder as ImageHolder).progressBar)
                }
            }
        }

        private fun loadImageUsingGlide(url: String, view: ImageView, progressBar: ProgressBar) {
            Glide.with(this@ActivityLyricCard)
                .load(url) //offset for 2 extra elements
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .listener(object : RequestListener<Drawable?> {
                    public override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.setVisibility(View.GONE)
                        return false
                    }

                    public override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.setVisibility(View.GONE)
                        return false
                    }
                }).into(view)
        }

        public override fun getItemCount(): Int {
            return urls!!.size + 2 //offset for 2 extra elements
        }

        fun setUrls(urls: Map<String?, String?>?) {
            this.urls = urls
            if (urls!!.size != 0) {
                val mainUrls: List<String?> = ArrayList(urls.values)
                setMainImage(mainUrls.get(UtilityFun.getRandom(0, urls.size)))
            }
            notifyDataSetChanged()
        }

        private fun getThumbElementByIndex(index: Int): String {
            return (urls!!.keys.toTypedArray()).get(index).toString()
        }

        private fun getMainElementByIndex(index: Int): String? {
            return urls!!.get((urls!!.keys.toTypedArray()).get(index))
        }

        internal inner class ImageHolder constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var imageView: ImageView
            var progressBar: ProgressBar

            init {
                itemView.setOnClickListener(object : View.OnClickListener {
                    public override fun onClick(view: View) {
                        setMainImage(getMainElementByIndex(getLayoutPosition() - 2))
                    }
                })
                imageView = itemView.findViewById<ImageView>(R.id.image_lyric_card)
                progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar)
            }
        }

        internal inner class ArtistHolder constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var imageView: ImageView
            var progressBar: ProgressBar

            init {
                imageView = itemView.findViewById<ImageView>(R.id.addArtistImage)
                progressBar = itemView.findViewById<ProgressBar>(R.id.progressBar)
                itemView.setOnClickListener(object : View.OnClickListener {
                    public override fun onClick(v: View) {
                        addArtistImage()
                    }
                })
            }
        }

        internal inner class CustomHolder constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener(object : View.OnClickListener {
                    public override fun onClick(v: View) {
                        addCustomImage()
                    }
                })
            }
        }
    }

    companion object {
        private val DAYS_UNTIL_CACHE: Int = 5 //Min number of days
    }
}