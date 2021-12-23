package com.music.player.bhandari.m.lyricCard

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.UIElementHelper.ColorHelper
import com.music.player.bhandari.m.customViews.ZoomTextView
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadArtInfoThread
import com.music.player.bhandari.m.utils.UtilityFun
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

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
class ActivityLyricCard : AppCompatActivity(), View.OnTouchListener {
    private val PICK_IMAGE: Int = 0

    @JvmField @BindView(R.id.rv_colors)
    var recyclerViewColors: RecyclerView? = null

    @JvmField @BindView(R.id.rv_images)
    var recyclerViewImages: RecyclerView? = null

    @JvmField @BindView(R.id.mainImageLyricCard)
    var mainImage: ImageView? = null

    @JvmField @BindView(R.id.text_lyric)
    var lyricText: ZoomTextView? = null

    @JvmField @BindView(R.id.text_artist)
    var artistText: ZoomTextView? = null

    @JvmField @BindView(R.id.text_track)
    var trackText: ZoomTextView? = null

    @JvmField @BindView(R.id.dragView)
    var dragView: View? = null

    @JvmField @BindView(R.id.progressBar)
    var progressBar: ProgressBar? = null

    @JvmField @BindView(R.id.brightnessSeekBar)
    var brightnessSeekBar: SeekBar? = null

    @JvmField @BindView(R.id.overImageLayer)
    var overImageLayer: View? = null

    @JvmField @BindView(R.id.watermark)
    var watermark: View? = null

    @JvmField @BindView(R.id.root_view_lyric_card)
    var rootView: View? = null
    var dx: Float = 0f
    var dy: Float = 0f
    private val imagesAdapter: ImagesAdapter = ImagesAdapter()
    private var mHandler: Handler? = null
    private var currentTextAlignment: Int = 0
    private val typefaces: MutableList<Typeface> = ArrayList()
    var currentFontPosition: Int = 0
    var typefaceSet: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        ColorHelper.setStatusBarGradiant(this)
        setTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lyric_card)
        ButterKnife.bind(this)
        if (intent.extras == null) {
            Toast.makeText(this, "Missing lyric text", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        if (MyApp.getPref().getBoolean("pref_first_time_lyric_card_launch", true)) {
            MyApp.getPref().edit().putBoolean("pref_first_time_lyric_card_launch", false)
                .apply()
            firstTimeLaunch()
        }
        val text: String = if (intent.extras!!.getString("lyric") != null) {
            intent.extras!!.getString("lyric")!!
        } else {
            ""
        }
        val author: String = if (intent.extras!!.getString("artist") != null) {
            intent.extras!!.getString("artist")!!
        } else {
            ""
        }
        val track: String = if (intent.extras!!.getString("track") != null) {
            intent.extras!!.getString("track")!!
        } else {
            ""
        }
        lyricText!!.text = text
        artistText!!.text = author
        trackText!!.text = track
        initiateToolbar()
        fillFonts()
        initiateUI()
    }

    override fun attachBaseContext(newBase: Context) {
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
        if (supportActionBar != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.GetStatusBarColor());
        }*/
        title = "Lyric Card"
    }

    private fun initiateUI() {
        //rootView.setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());
        recyclerViewColors!!.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,
            false)
        recyclerViewColors!!.adapter = ColorAdapter()
        recyclerViewImages!!.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,
            false)
        recyclerViewImages!!.adapter = imagesAdapter

        //get images links
        val type: Type = object : TypeToken<MutableMap<String?, String?>?>() {}.type
        val urls = Gson().fromJson<MutableMap<String, String>>(MyApp.getPref()
            .getString(getString(R.string.pref_card_image_links), ""), type)
        if ((System.currentTimeMillis() >= MyApp.getPref()
                .getLong(getString(R.string.pref_card_image_saved_at), 0) + DAYS_UNTIL_CACHE
                    && urls != null)
        ) {
            imagesAdapter.setUrls(urls)
        } else {
            FirebaseDatabase.getInstance().reference.child("cardlinksNew")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        Log.d("ActivityLyricCard", "onDataChange: ")
                        val urls: MutableMap<String, String> = LinkedHashMap()
                        for (snap: DataSnapshot in dataSnapshot.children) {
                            try {
                                val map: MutableMap<String, String> = snap.value as MutableMap<String, String>
                               // urls[map["thumb"]] = map["image"]
                            } catch (ignored: Exception) {
                            }
                        }
                        imagesAdapter.setUrls(urls)

                        //cache links in shared pref
                        MyApp.getPref().edit()
                            .putString(getString(R.string.pref_card_image_links),
                                Gson().toJson(urls)).apply()
                        MyApp.getPref().edit()
                            .putLong(getString(R.string.pref_card_image_saved_at),
                                System.currentTimeMillis()).apply()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d("ActivityLyricCard", "onCancelled: " + databaseError.message)
                        Toast.makeText(this@ActivityLyricCard,
                            "Error retrieving images from server",
                            Toast.LENGTH_SHORT).show()
                    }
                })
        }
        initiateDragView()
        brightnessSeekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, `in`: Int, b: Boolean) {
                val i: Double = seekBar.progress / 100.0
                val alpha: Int = Math.round(i * 255).toInt()
                var hex: String = Integer.toHexString(alpha).uppercase(Locale.getDefault())
                if (hex.length == 1) hex = "0$hex"
                try {
                    overImageLayer!!.setBackgroundColor(Color.parseColor("#" + hex + "000000"))
                } catch (e: NumberFormatException) {
                    Log.d("ActivityLyricCard", "onProgressChanged: Color parse exception")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        brightnessSeekBar!!.progress = 30
    }

    private fun initiateDragView() {
        artistText!!.setOnTouchListener(this)
        lyricText!!.setOnTouchListener(this)
        trackText!!.setOnTouchListener(this)
    }

    private fun firstTimeLaunch() {
        if (UtilityFun.isConnectedToInternet) {
            Toast.makeText(this, R.string.first_launch_lyric_card, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.first_launch_lyric_card_no_internet, Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dx = v.x - event.rawX
                dy = v.y - event.rawY
            }
            MotionEvent.ACTION_MOVE -> when (v.id) {
                R.id.text_artist -> artistText!!.animate()
                    .x(event.rawX + dx)
                    .y(event.rawY + dy)
                    .setDuration(0)
                    .start()
                R.id.text_lyric -> lyricText!!.animate()
                    .x(event.rawX + dx)
                    .y(event.rawY + dy)
                    .setDuration(0)
                    .start()
                R.id.text_track -> trackText!!.animate()
                    .x(event.rawX + dx)
                    .y(event.rawY + dy)
                    .setDuration(0)
                    .start()
            }
            else -> return false
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_lyric_card, menu)
        Handler().postDelayed({
            try {
                if (!MyApp.getPref().getBoolean(getString(R.string.pref_info_lyric_card_shown), false)
                ) {
                    showFirstTimeInfo()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 1000)
        return true
    }

    private fun showFirstTimeInfo() {
        TapTargetSequence(this@ActivityLyricCard)
            .targets(
                TapTarget.forView(findViewById(R.id.action_font),
                    "Change text font by clicking here")
                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                    .outerCircleAlpha(0.9f)
                    .transparentTarget(true)
                    .titleTextColor(R.color.colorwhite)
                    .descriptionTextColor(R.color.colorwhite)
                    .drawShadow(true)
                    .tintTarget(true),
                TapTarget.forView(findViewById(R.id.action_alignment),
                    "Change text alignment by clicking here")
                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                    .outerCircleAlpha(0.9f)
                    .transparentTarget(true)
                    .titleTextColor(R.color.colorwhite)
                    .descriptionTextColor(R.color.colorwhite)
                    .drawShadow(true)
                    .tintTarget(true),
                TapTarget.forView(findViewById(R.id.action_edit),
                    "Edit lyric text by clicking here")
                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                    .outerCircleAlpha(0.9f)
                    .transparentTarget(true)
                    .titleTextColor(R.color.colorwhite)
                    .descriptionTextColor(R.color.colorwhite)
                    .drawShadow(true)
                    .tintTarget(true),
                TapTarget.forView(findViewById(R.id.black_overlay_wrap),
                    "Blacken background image by using this slider")
                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                    .outerCircleAlpha(0.9f)
                    .transparentTarget(true)
                    .titleTextColor(R.color.colorwhite)
                    .descriptionTextColor(R.color.colorwhite)
                    .drawShadow(true)
                    .tintTarget(true),
                TapTarget.forView(findViewById(R.id.rv_colors),
                    "Choose text color among given options")
                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                    .outerCircleAlpha(0.9f)
                    .transparentTarget(true)
                    .titleTextColor(R.color.colorwhite)
                    .descriptionTextColor(R.color.colorwhite)
                    .drawShadow(true)
                    .tintTarget(true),
                TapTarget.forView(findViewById(R.id.text_artist),
                    "You can remove artist or album text by dragging it towards bottom of image.")
                    .outerCircleColorInt(ColorHelper.getPrimaryColor())
                    .outerCircleAlpha(0.9f)
                    .transparentTarget(true)
                    .titleTextColor(R.color.colorwhite)
                    .descriptionTextColor(R.color.colorwhite)
                    .drawShadow(true)
                    .tintTarget(true),
                TapTarget.forView(recyclerViewImages!!.layoutManager!!.findViewByPosition(0),
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
                override fun onSequenceFinish() {
                    // Yay
                    MyApp.getPref().edit()
                        .putBoolean(getString(R.string.pref_info_lyric_card_shown), true).apply()
                }

                override fun onSequenceStep(lastTarget: TapTarget, targetClicked: Boolean) {}
                override fun onSequenceCanceled(lastTarget: TapTarget) {
                    // Boo
                }
            }).start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> finish()
            R.id.action_share -> shareCard()
            R.id.action_font -> changeFont()
            R.id.action_alignment -> changeAlignment()
            R.id.action_edit -> showTextEditDialog()
            R.id.action_save -> {
                val f: File? = createImageFile(false)
                if (f != null) {
                    Toast.makeText(this,
                        "Lyric card is saved at " + f.absolutePath,
                        Toast.LENGTH_SHORT).show()
                }
            }
            R.id.action_remove_watermark -> if (item.isChecked) {
                item.isChecked = false
                watermark!!.visibility = View.VISIBLE
            } else {
                item.isChecked = true
                watermark!!.visibility = View.INVISIBLE
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createImageFile(temp: Boolean): File {
        dragView!!.destroyDrawingCache() //if not done, image is going to be overridden every time
        dragView!!.isDrawingCacheEnabled = true
        val bitmap: Bitmap = dragView!!.drawingCache
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
            Log.d("ActivityLyricCard", "shareCard: " + e.localizedMessage)
            Toast.makeText(this, "Error saving card on storage ", Toast.LENGTH_SHORT).show()
        }
        return lyricCardFile
    }

    private fun shareCard() {
        val lyricCardFile: File = createImageFile(true)
        try {
            val share = Intent(Intent.ACTION_SEND)
            share.type = "image/*"
            share.putExtra(Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(this,
                    applicationContext.packageName + "com.bhandari.music.provider",
                    lyricCardFile))
            share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_lyric_card_extra_text))
            startActivity(Intent.createChooser(share, "Share Lyric Card"))
        } catch (e: Exception) {
            Toast.makeText(this,
                "Error while sharing, lyric card is saved at " + lyricCardFile.absolutePath,
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
        Log.d("ActivityLyricCard", "requestDownload: $query")
        val request: FontRequest = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            query,
            R.array.com_google_android_gms_fonts_certs)
        val callback: FontsContractCompat.FontRequestCallback =
            object : FontsContractCompat.FontRequestCallback() {
                override fun onTypefaceRetrieved(typeface: Typeface) {
                    Log.d("ActivityLyricCard", "onTypefaceRetrieved: $typeface")
                    if (!typefaceSet) {
                        lyricText!!.typeface = typeface
                        artistText!!.typeface = typeface
                        trackText!!.typeface = typeface
                        typefaceSet = true
                    }
                    typefaces.add(typeface)
                }

                override fun onTypefaceRequestFailed(reason: Int) {
                    Log.d("ActivityLyricCard", "onTypefaceRequestFailed: $reason")
                }
            }
        FontsContractCompat.requestFont(this, request, callback, getHandlerThreadHandler()!!)
    }

    private fun getHandlerThreadHandler(): Handler? {
        if (mHandler == null) {
            val handlerThread = HandlerThread("fonts")
            handlerThread.start()
            mHandler = Handler(handlerThread.looper)
        }
        return mHandler
    }

    private fun changeFont() {
        if (currentFontPosition >= typefaces.size - 1) {
            currentFontPosition = 0
            lyricText!!.typeface = typefaces[currentFontPosition]
            artistText!!.typeface = typefaces[currentFontPosition]
            trackText!!.typeface = typefaces[currentFontPosition]
        } else {
            val index: Int = ++currentFontPosition
            lyricText!!.typeface = typefaces[index]
            artistText!!.typeface = typefaces[index]
            trackText!!.typeface = typefaces[index]
        }
    }

    private fun changeAlignment() {
        when (currentTextAlignment) {
            1 -> {
                lyricText!!.gravity = Gravity.END
                artistText!!.gravity = Gravity.END
                trackText!!.gravity = Gravity.END
                currentTextAlignment = 2
            }
            2 -> {
                lyricText!!.gravity = Gravity.START
                artistText!!.gravity = Gravity.START
                trackText!!.gravity = Gravity.START
                currentTextAlignment = 0
            }
            else -> {
                lyricText!!.gravity = Gravity.CENTER
                artistText!!.gravity = Gravity.CENTER
                trackText!!.gravity = Gravity.CENTER
                currentTextAlignment = 1
            }
        }
    }

    private fun setMainImage(url: String) {
        progressBar!!.visibility = View.VISIBLE
        Glide.with(applicationContext)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar!!.visibility = View.GONE
                    Toast.makeText(this@ActivityLyricCard,
                        R.string.error_loading_image_lyric_card,
                        Toast.LENGTH_SHORT).show()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar!!.visibility = View.GONE
                    return false
                }
            })
            .into(mainImage!!)
    }

    private fun setMainImage(uri: Uri) {
        progressBar!!.visibility = View.VISIBLE
        Glide.with(this@ActivityLyricCard)
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar!!.visibility = View.GONE
                    Toast.makeText(this@ActivityLyricCard,
                        R.string.error_loading_image_lyric_card,
                        Toast.LENGTH_SHORT).show()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar!!.visibility = View.GONE
                    return false
                }
            }).into(mainImage!!)
    }

    fun addCustomImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
    }

    fun addArtistImage() {
        mainImage!!.setImageBitmap(null)
        progressBar!!.visibility = View.VISIBLE
        val info = OfflineStorageArtistBio.getArtistInfoFromCache(artistText!!.text.toString())
        Log.d("ActivityLyricCard", "addArtistImage: $info")
        when {
            info != null -> {
                setMainImage(info.getImageUrl()!!)
            }
            UtilityFun.isConnectedToInternet -> {
                val artist: String = UtilityFun.filterArtistString(artistText!!.text.toString())
                DownloadArtInfoThread(object : ArtistInfo.Callback {
                    override fun onArtInfoDownloaded(artistInfo: ArtistInfo?) {
                        when {
                            artistInfo != null && artistInfo.getImageUrl()!!.isNotEmpty() -> {
                                setMainImage(artistInfo.getImageUrl()!!)
                            }
                            else -> {
                                Toast.makeText(this@ActivityLyricCard,
                                    "Artist image not found",
                                    Toast.LENGTH_SHORT).show()
                                if (imagesAdapter.urls.isNotEmpty()) {
                                   // setMainImage(imagesAdapter.urls[0]!!)
                                }
                            }
                        }
                    }
                }, artist, null).start()
            }
            else -> {
                progressBar!!.visibility = View.INVISIBLE
                Toast.makeText(this, "Not connected to internet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showTextEditDialog() {
//        val builder: MaterialDialog.Builder = MyDialogBuilder(this)
//            .title("Edit text")
//            .positiveText(getString(R.string.okay))
//            .onPositive(object : SingleButtonCallback() {
//                fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                    val view: View? = dialog.getCustomView()
//                    if (view == null) return
//                    val lyric: AppCompatEditText = view.findViewById(R.id.text_lyric)
//                    val artist: AppCompatEditText = view.findViewById(R.id.text_artist)
//                    val track: AppCompatEditText = view.findViewById(R.id.text_track)
//                    lyricText.setText(lyric.getText())
//                    artistText.setText(artist.getText())
//                    trackText.setText(track.getText())
//                }
//            })
//            .customView(R.layout.dialog_edit_lyric_card_texts, true)
       // val dialog: MaterialDialog = builder.build()
        //val view: View? = dialog.getCustomView()
       // if (view == null) return
//        val lyric: AppCompatEditText = view.findViewById(R.id.text_lyric)
//        val artist: AppCompatEditText = view.findViewById(R.id.text_artist)
//        val track: AppCompatEditText = view.findViewById(R.id.text_track)
//        lyric.setText(lyricText.getText())
//        artist.setText(artistText.getText())
//        track.setText(trackText.getText())
      //  dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE) {
            Log.d("ActivityLyricCard", "onActivityResult: $data")
            if (data != null && data.data != null) setMainImage(data.data!!) else Toast.makeText(
                this,
                "Error loading image",
                Toast.LENGTH_SHORT).show()
        }
    }

    internal inner class ColorAdapter :
        RecyclerView.Adapter<ColorAdapter.MyViewHolder?>() {
        var colors: Array<String> = resources.getStringArray(R.array.colors)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val v: View = LayoutInflater.from(this@ActivityLyricCard)
                .inflate(R.layout.item_color, parent, false)
            return MyViewHolder(v)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.color.setBackgroundColor(Color.parseColor(colors[position]))
        }

        override fun getItemCount(): Int {
            return colors.size
        }

        internal inner class MyViewHolder constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var color: ImageView

            init {
                itemView.setOnClickListener {
                    lyricText!!.setTextColor(Color.parseColor(colors[layoutPosition]))
                    artistText!!.setTextColor(Color.parseColor(colors[layoutPosition]))
                    trackText!!.setTextColor(Color.parseColor(colors[layoutPosition]))
                }
                color = itemView.findViewById(R.id.colorView)
            }
        }
    }

    internal inner class ImagesAdapter :
        RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
        var urls: MutableMap<String, String> =  mutableMapOf<String, String>()

        override fun onCreateViewHolder(
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
            v = LayoutInflater.from(this@ActivityLyricCard)
                .inflate(R.layout.item_custom_image, parent, false)
            return CustomHolder(v)
        }

        override fun getItemViewType(position: Int): Int {
            return when (position) {
                0 -> 0
                1 -> 1
                else -> 2
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                1 -> {
                    //load artist image in thumbnail view
                    val info: ArtistInfo? =
                        OfflineStorageArtistBio.getArtistInfoFromCache(artistText!!.text
                            .toString())
                    Log.d("ImagesAdapter", "onBindViewHolder: $info")
                    when {
                        info != null -> {
                            (holder as ArtistHolder).progressBar.visibility = View.VISIBLE
                            loadImageUsingGlide(info.getImageUrl()!!,
                                holder.imageView,
                                holder.progressBar)
                        }
                        UtilityFun.isConnectedToInternet -> {
                            val artist: String =
                                UtilityFun.filterArtistString(artistText!!.text.toString())
                            DownloadArtInfoThread(object : ArtistInfo.Callback {
                                override fun onArtInfoDownloaded(artistInfo: ArtistInfo?) {
                                    if (artistInfo != null && artistInfo.getImageUrl()!!
                                            .isNotEmpty()
                                    ) {
                                        loadImageUsingGlide(artistInfo.getImageUrl()!!,
                                            (holder as ArtistHolder).imageView,
                                            holder.progressBar)
                                    }
                                }
                            }, artist, null).start()
                        }
                    }
                }
                2 -> if (holder is ImageHolder) {
                    holder.progressBar.visibility = View.VISIBLE
                    loadImageUsingGlide(getThumbElementByIndex(position - 2),
                        holder.imageView,
                        holder.progressBar)
                }
            }
        }

        private fun loadImageUsingGlide(url: String, view: ImageView, progressBar: ProgressBar) {
            Glide.with(this@ActivityLyricCard)
                .load(url) //offset for 2 extra elements
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }
                }).into(view)
        }

        override fun getItemCount(): Int {
            return urls.size + 2 //offset for 2 extra elements
        }

        @JvmName("setUrls1")
        fun setUrls(urls: MutableMap<String, String>) {
            this.urls = urls
            if (urls.isNotEmpty()) {
                val mainUrls: List<String?> = ArrayList(urls.values)
                setMainImage(mainUrls.get(UtilityFun.getRandom(0, urls.size))!!)
            }
            notifyDataSetChanged()
        }

        private fun getThumbElementByIndex(index: Int): String {
            return (urls.keys.toTypedArray())[index]
        }

        private fun getMainElementByIndex(index: Int): String? {
            return urls[(urls.keys.toTypedArray())[index]]
        }

        internal inner class ImageHolder constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var imageView: ImageView
            var progressBar: ProgressBar

            init {
                itemView.setOnClickListener { setMainImage(getMainElementByIndex(layoutPosition - 2)!!) }
                imageView = itemView.findViewById(R.id.image_lyric_card)
                progressBar = itemView.findViewById(R.id.progressBar)
            }
        }

        internal inner class ArtistHolder constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var imageView: ImageView = itemView.findViewById(R.id.addArtistImage)
            var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

            init {
                itemView.setOnClickListener { addArtistImage() }
            }
        }

        internal inner class CustomHolder constructor(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener { addCustomImage() }
            }
        }
    }

    companion object {
        private val DAYS_UNTIL_CACHE: Int = 5 //Min number of days
    }
}