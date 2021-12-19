package com.music.player.bhandari.m.activity

import android.R
import android.content.Context
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.afollestad.materialdialogs.DialogAction
import com.music.player.bhandari.m.model.Constants
import com.music.player.bhandari.m.model.TrackItem

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
class ActivityTagEditor constructor() : AppCompatActivity(), View.OnClickListener {
    @BindView(R.id.title_te)
    var title: EditText? = null

    @BindView(R.id.artist_te)
    var artist: EditText? = null

    @BindView(R.id.album_te)
    var album: EditText? = null

    @BindView(R.id.album_art_te)
    var album_art: ImageView? = null
    private var song_id: Int = 0
    private var original_title: String? = null
    private var original_artist: String? = null
    private var original_album: String? = null
    private var edited_title: String = ""
    private var edited_artist: String = ""
    private var edited_album: String = ""
    private var track_title: String? = null
    private val SAVE: Int = 10
    var fChanged: Boolean = false
    private var item: TrackItem? = null
    private var ALBUM_ART_PATH: String = ""

    //file path where changed image file is stored
    private var new_artwork_path: String = ""
    protected override fun onCreate(savedInstanceState: Bundle?) {
        //if player service not running, kill the app
        if (MyApp.Companion.getService() == null) {
            val intent: Intent = Intent(this, ActivityPermissionSeek::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }
        ColorHelper.setStatusBarGradiant(this)
        super.onCreate(savedInstanceState)
        val themeSelector: Int = MyApp.Companion.getPref()
            .getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)
        when (themeSelector) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        setContentView(R.layout.activity_tag_editor)
        ButterKnife.bind(this)

        //show info dialog
        showInfoDialog()
        ALBUM_ART_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/" + getString(R.string.album_art_dir_name)

        //findViewById(R.id.root_view_tag_editor).setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        //get file path
        val file_path: String? = getIntent().getStringExtra("file_path")
        if (file_path == null) {
            finish()
        }
        track_title = getIntent().getStringExtra("track_title")
        song_id = getIntent().getIntExtra("id", 0)
        item = MusicLibrary.getInstance().getTrackItemFromId(song_id)
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
        }*/setTitle(getString(R.string.title_tag_editor))
        album_art.setOnClickListener(this)

        //get current tags from audio file and populate the fields
        setTagsFromContent()
    }

    protected override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    private fun setTagsFromContent() {
        if (item == null) {
            return
        }
        title.setText(item!!.title)
        song_id = item!!.id
        original_title = item!!.title
        album.setText(item!!.album)
        original_album = item!!.album
        artist.setText(item!!.getArtist())
        original_artist = item!!.getArtist()
        val defaultAlbumArtSetting: Int =
            MyApp.Companion.getPref().getInt(getString(R.string.pref_default_album_art), 0)
        when (defaultAlbumArtSetting) {
            0 -> Glide.with(this)
                .load(MusicLibrary.getInstance().getAlbumArtUri(item!!.albumId))
                .signature(ObjectKey(System.currentTimeMillis().toString()))
                .placeholder(R.drawable.ic_batman_1)
                .into(album_art)
            1 -> Glide.with(this)
                .load(MusicLibrary.getInstance().getAlbumArtUri(item!!.albumId))
                .signature(ObjectKey(System.currentTimeMillis().toString()))
                .placeholder(UtilityFun.defaultAlbumArtDrawable)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(album_art)
        }
    }

    protected override fun onPause() {
        super.onPause()
        MyApp.Companion.isAppVisible = false
    }

    protected override fun onResume() {
        super.onResume()
        MyApp.Companion.isAppVisible = true
    }

    public override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.add(0, SAVE, 0, getString(R.string.action_save))
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        return super.onPrepareOptionsMenu(menu)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> {
                readValues()
                if (fChanged) {
                    unsavedDataAlert()
                } else {
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    finish()
                }
            }
            SAVE -> {
                readValues()
                if (fChanged) {
                    try {
                        save()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this,
                            getString(R.string.te_error_saving_tags),
                            Toast.LENGTH_LONG).show()
                    }
                    Log.v(Constants.TAG, edited_title)
                    Log.v(Constants.TAG, edited_artist)
                    Log.v(Constants.TAG, edited_album)
                    val edited_genre: String = ""
                    Log.v(Constants.TAG, edited_genre)
                } else {
                    val intent: Intent
                    val launchedFrom: Int =
                        getIntent().getIntExtra("from", Constants.TAG_EDITOR_LAUNCHED_FROM.MAIN_LIB)
                    if (launchedFrom == Constants.TAG_EDITOR_LAUNCHED_FROM.MAIN_LIB) {
                        intent = Intent(this, ActivityMain::class.java)
                    } else if (launchedFrom == Constants.TAG_EDITOR_LAUNCHED_FROM.NOW_PLAYING) {
                        intent = Intent(this, ActivityNowPlaying::class.java)
                    } else {
                        intent = Intent(this, ActivitySecondaryLibrary::class.java)
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun readValues() {
        edited_title = title.getText().toString()
        edited_artist = artist.getText().toString()
        edited_album = album.getText().toString()
        //edited_genre = genre.getText().toString();
        if ((!(edited_title == original_title) ||
                    !(edited_artist == original_artist) ||
                    !(edited_album == original_album) ||  //!edited_genre.equals(original_genre) ||
                    !(new_artwork_path == ""))
        ) {
            fChanged = true
        }
    }

    private fun unsavedDataAlert() {
        MyDialogBuilder(this)
            .title(getString(R.string.te_unsaved_data_title))
            .content(getString(R.string.changes_discard_alert_te))
            .positiveText(getString(R.string.te_unsaved_data_pos))
            .negativeText(getString(R.string.te_unsaved_data_new))
            .onPositive(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    try {
                        save()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@ActivityTagEditor,
                            "Error while saving tags!",
                            Toast.LENGTH_LONG).show()
                    }
                    finish()
                }
            })
            .onNegative(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    finish()
                }
            })
            .show()
    }

    private fun save() {
        if (edited_title.isEmpty() || edited_album.isEmpty() || edited_artist.isEmpty()) {
            Toast.makeText(getApplicationContext(),
                getString(R.string.te_error_empty_field),
                Toast.LENGTH_SHORT).show()
            return
        }

        //change content in android
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var values: ContentValues = ContentValues()
        values.put(MediaStore.Audio.Media.TITLE, edited_title)
        values.put(MediaStore.Audio.Media.ARTIST, edited_artist)
        values.put(MediaStore.Audio.Media.ALBUM, edited_album)
        getContentResolver().update(uri,
            values,
            MediaStore.Audio.Media.TITLE + "=?",
            arrayOf(track_title))
        if (!(new_artwork_path == "")) {
            val sArtworkUri: Uri = Uri.parse("content://media/external/audio/albumart")
            val deleted: Int =
                getContentResolver().delete(ContentUris.withAppendedId(sArtworkUri, item!!.albumId),
                    null,
                    null)
            Log.v(Constants.TAG, "delete " + deleted)
            values = ContentValues()
            values.put("album_id", item!!.albumId)
            values.put("_data", new_artwork_path)
            getContentResolver().insert(sArtworkUri, values)
        }
        val d: dataItem = MusicLibrary.getInstance()
            .updateTrackNew(song_id, edited_title, edited_artist, edited_album)
        PlaylistManager.getInstance(MyApp.Companion.getContext()).addEntryToMusicTable(d)
        //   PlaylistManager.getInstance(MyApp.getContext()).PopulateUserMusicTable();
        val intent: Intent
        val launchedFrom: Int =
            getIntent().getIntExtra("from", Constants.TAG_EDITOR_LAUNCHED_FROM.MAIN_LIB)
        if (launchedFrom == Constants.TAG_EDITOR_LAUNCHED_FROM.MAIN_LIB) {
            intent = Intent(this, ActivityMain::class.java)
        } else if (launchedFrom == Constants.TAG_EDITOR_LAUNCHED_FROM.NOW_PLAYING) {
            intent = Intent(this, ActivityNowPlaying::class.java)
        } else {
            intent = Intent(this, ActivitySecondaryLibrary::class.java)
        }
        intent.putExtra("refresh", true)
        intent.putExtra("position", getIntent().getIntExtra("position", -1))
        intent.putExtra("originalTitle", original_title)
        intent.putExtra("title", edited_title)
        intent.putExtra("artist", edited_artist)
        intent.putExtra("album", edited_album)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }

    public override fun onClick(v: View) {
        if (v.getId() == R.id.album_art_te) {
            pickImage()
        }
    }

    fun pickImage() {
        val intent: Intent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        intent.setType("image/*")
        startActivityForResult(intent, 1)
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (data == null) {
            return
        }
        if (requestCode == 1) {
            //dumpIntent(data);
            checkAndCreateAlbumArtDirectory()
            val uri: Uri? = data.getData()
            if (uri != null && album_art != null) {
                Log.v(Constants.TAG, data.toString())
                val file_path_artwork: String? = getRealPathFromURI(uri)
                if (file_path_artwork == null) {
                    Toast.makeText(this,
                        getString(R.string.te_error_image_load),
                        Toast.LENGTH_SHORT).show()
                    return
                }
                Glide.with(this)
                    .load(File(file_path_artwork)) // Uri of the picture
                    .into(album_art)
                new_artwork_path = file_path_artwork
            }
        }
    }

    private fun checkAndCreateAlbumArtDirectory() {
        val f: File = File(ALBUM_ART_PATH)
        if (f.exists()) {
            return
        }
        try {
            f.mkdir()
        } catch (ignored: Exception) {
        }
    }

    fun getRealPathFromURI(uri: Uri?): String? {
        val projection: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = managedQuery(uri, projection, null, null, null)
        if (cursor == null || cursor.getCount() == 0) {
            return null
        }
        val column_index: Int = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    public override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY -> MyApp.Companion.getService()
                .play()
            KeyEvent.KEYCODE_MEDIA_NEXT -> MyApp.Companion.getService().nextTrack()
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> MyApp.Companion.getService().prevTrack()
            KeyEvent.KEYCODE_MEDIA_STOP -> MyApp.Companion.getService().stop()
            KeyEvent.KEYCODE_BACK -> onBackPressed()
        }
        return false
    }

    public override fun onBackPressed() {
        readValues()
        if (fChanged) {
            unsavedDataAlert()
        } else {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun showInfoDialog() {
        if (!MyApp.Companion.getPref()
                .getBoolean(getString(R.string.pref_show_edit_track_info_dialog), true)
        ) {
            return
        }
        MyDialogBuilder(this)
            .title(getString(R.string.te_show_info_title))
            .content(getString(R.string.te_show_info_content))
            .positiveText(getString(R.string.te_show_info_pos))
            .negativeText(getString(R.string.te_show_info_neg))
            .onPositive(object : SingleButtonCallback() {
                fun onClick(dialog: MaterialDialog, which: DialogAction) {
                    MyApp.Companion.getPref().edit()
                        .putBoolean(getString(R.string.pref_show_edit_track_info_dialog), false)
                        .apply()
                }
            })
            .show()
    }

    companion object {
        /*private void deletePhoto(){
        if(album_art!=null){
            album_art.setImageDrawable(getResources().getDrawable(R.drawable.ic_batman_1));
        }
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        getContentResolver().delete(ContentUris.withAppendedId(sArtworkUri, item.getAlbumId()), null, null);
        String customAlbumArt = Environment.getExternalStorageDirectory().getAbsolutePath()
                +"/"+getString(R.string.album_art_dir_name)+"/"
                +item.getAlbumId();
        File f = new File(customAlbumArt);
        if(f.exists()){
            try {
                f.delete();
            }catch (Exception ignored){

            }
        }
    }*/
        fun dumpIntent(i: Intent) {
            val bundle: Bundle? = i.getExtras()
            if (bundle != null) {
                val keys: Set<String> = bundle.keySet()
                val it: Iterator<String> = keys.iterator()
                Log.e(Constants.TAG, "Dumping Intent start")
                while (it.hasNext()) {
                    val key: String = it.next()
                    Log.e(Constants.TAG, "[" + key + "=" + bundle.get(key) + "]")
                }
                Log.e(Constants.TAG, "Dumping Intent end")
            }
        }
    }
}