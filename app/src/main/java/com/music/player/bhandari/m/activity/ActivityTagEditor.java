package com.music.player.bhandari.m.activity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.PlaylistManager;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.model.dataItem;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 Copyright 2017 Amit Bhandari AB

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

public class ActivityTagEditor extends AppCompatActivity implements  View.OnClickListener {

    @BindView(R.id.title_te)  EditText title;
    @BindView(R.id.artist_te)  EditText artist;
    @BindView(R.id.album_te)  EditText album;
    @BindView(R.id.album_art_te)  ImageView album_art;
    @BindView(R.id.adView) AdView mAdView;

    private int song_id;
    private String original_title, original_artist, original_album;
    private String edited_title = "";
    private String edited_artist="";
    private String edited_album="";
    private String track_title;
    private final int SAVE=10;
    boolean fChanged=false;
    private TrackItem item;
    private String ALBUM_ART_PATH="";
    //file path where changed image file is stored
    private String new_artwork_path = "";


    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        //if player service not running, kill the app
        if(MyApp.getService()==null){
            Intent intent = new Intent(this, ActivityPermissionSeek.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }

        super.onCreate(savedInstanceState);
        int themeSelector = MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT);
        switch (themeSelector){
            case Constants.PRIMARY_COLOR.DARK:
                setTheme(R.style.AppThemeDark);
                break;

            case Constants.PRIMARY_COLOR.GLOSSY:
                setTheme(R.style.AppThemeDark);
                break;

            case Constants.PRIMARY_COLOR.LIGHT:
                setTheme(R.style.AppThemeLight);
                break;
        }
        setContentView(R.layout.activity_tag_editor);
        ButterKnife.bind(this);

        showAdIfApplicable();
        //show info dialog
        showInfoDialog();

        ALBUM_ART_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+getString(R.string.album_art_dir_name);

        findViewById(R.id.root_view_tag_editor).setBackgroundDrawable(ColorHelper.getColoredThemeGradientDrawable());

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //get file path
        String file_path = getIntent().getStringExtra("file_path");
        if(file_path ==null){
            finish();
        }

        track_title = getIntent().getStringExtra("track_title");
        song_id = getIntent().getIntExtra("id",0);

        item = MusicLibrary.getInstance().getTrackItemFromId(song_id);

        Toolbar toolbar = findViewById(R.id.toolbar_);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ColorHelper.getPrimaryColor()));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.getDarkPrimaryColor());
        }
        setTitle(getString(R.string.title_tag_editor));

        album_art.setOnClickListener(this);

        //get current tags from audio file and populate the fields
        setTagsFromContent();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    private void showAdIfApplicable(){
        if( false /*AppLaunchCountManager.isEligibleForInterstialAd() &&AppLaunchCountManager.isEligibleForBannerAds() && !UtilityFun.isAdsRemoved()*/ ) {
            MobileAds.initialize(getApplicationContext(), getString(R.string.banner_tag_editor));
            mAdView = findViewById(R.id.adView);
            if (UtilityFun.isConnectedToInternet()) {
                AdRequest adRequest = new AdRequest.Builder()//.addTestDevice("C6CC5AB32A15AF9EFB67D507C151F23E")
                        .build();
                if (mAdView != null) {
                    mAdView.loadAd(adRequest);
                    mAdView.setVisibility(View.VISIBLE);
                }
            } else {
                if (mAdView != null) {
                    mAdView.setVisibility(View.GONE);
                }
            }
        }
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setTagsFromContent(){
        if(item==null){
            return;
        }
        title.setText(item.getTitle());
        song_id = item.getId();

        original_title = item.getTitle();

        album.setText(item.getAlbum());
        original_album = item.getAlbum();

        artist.setText(item.getArtist());
        original_artist = item.getArtist();

        int defaultAlbumArtSetting = MyApp.getPref().getInt(getString(R.string.pref_default_album_art), 0);
        switch (defaultAlbumArtSetting){
            case 0:
                Glide.with(this)
                        .load(MusicLibrary.getInstance().getAlbumArtUri(item.getAlbumId()))
                        .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                        .animate(AnimationUtils.loadAnimation(this, R.anim.fade_in))
                        .placeholder(R.drawable.ic_batman_1)
                        .into(album_art);
                break;

            case 1:
                Glide.with(this)
                        .load(MusicLibrary.getInstance().getAlbumArtUri(item.getAlbumId()))
                        .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                        .animate(AnimationUtils.loadAnimation(this, R.anim.fade_in))
                        .placeholder(UtilityFun.getDefaultAlbumArtDrawable()).diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(album_art);
                break;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.isAppVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.isAppVisible = true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.add(0, SAVE , 0, getString(R.string.action_save))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                readValues();
                if(fChanged){
                    unsavedDataAlert();
                }else {
                    overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                    finish();
                }
                break;

            case SAVE:
                readValues();
                if(fChanged){
                    try {
                        save();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this,getString(R.string.te_error_saving_tags),Toast.LENGTH_LONG).show();
                    }
                    Log.v(Constants.TAG,edited_title);
                    Log.v(Constants.TAG,edited_artist);
                    Log.v(Constants.TAG,edited_album);
                    String edited_genre = "";
                    Log.v(Constants.TAG, edited_genre);
                }else {
                    Intent intent;
                    int launchedFrom = getIntent().getIntExtra("from",Constants.TAG_EDITOR_LAUNCHED_FROM.MAIN_LIB);
                    if(launchedFrom==Constants.TAG_EDITOR_LAUNCHED_FROM.MAIN_LIB) {
                        intent = new Intent(this, ActivityMain.class);
                    }else if(launchedFrom == Constants.TAG_EDITOR_LAUNCHED_FROM.NOW_PLAYING){
                        intent = new Intent(this, ActivityNowPlaying.class);
                    }else {
                        intent = new Intent(this, ActivitySecondaryLibrary.class);
                    }
                    startActivity(intent);
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void readValues(){
        edited_title = title.getText().toString();
        edited_artist = artist.getText().toString();
        edited_album = album.getText().toString();
        //edited_genre = genre.getText().toString();
        if(!edited_title.equals(original_title) ||
                !edited_artist.equals(original_artist) ||
                !edited_album.equals(original_album) ||
                //!edited_genre.equals(original_genre) ||
                !new_artwork_path.equals("")){
            fChanged = true;
        }
    }

    private void unsavedDataAlert() {
        new MaterialDialog.Builder(this)
                .typeface(TypeFaceHelper.getTypeFace(this),TypeFaceHelper.getTypeFace(this))
                .title(getString(R.string.te_unsaved_data_title))
                .content(getString(R.string.changes_discard_alert_te))
                .positiveText(getString(R.string.te_unsaved_data_pos))
                .negativeText(getString(R.string.te_unsaved_data_new))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        try {
                            save();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ActivityTagEditor.this,"Error while saving tags!",Toast.LENGTH_LONG).show();
                        }
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();
                    }
                })
                .show();
    }

    private void save() {

        if(edited_title.isEmpty() || edited_album.isEmpty() || edited_artist.isEmpty()){
            Toast.makeText(getApplicationContext(),getString(R.string.te_error_empty_field), Toast.LENGTH_SHORT).show();
            return;
        }

        //change content in android
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.TITLE, edited_title);
        values.put(MediaStore.Audio.Media.ARTIST, edited_artist);
        values.put(MediaStore.Audio.Media.ALBUM, edited_album);
        getContentResolver().update(uri, values, MediaStore.Audio.Media.TITLE +"=?", new String[] {track_title});
        if(!new_artwork_path.equals("")){
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            int deleted = getContentResolver().delete(ContentUris.withAppendedId(sArtworkUri, item.getAlbumId()), null, null);
            Log.v(Constants.TAG,"delete "+deleted);
            values = new ContentValues();
            values.put("album_id", item.getAlbumId());
            values.put("_data", new_artwork_path);
            getContentResolver().insert(sArtworkUri, values);
        }

        dataItem d = MusicLibrary.getInstance().updateTrackNew(song_id, edited_title, edited_artist, edited_album);
        PlaylistManager.getInstance(MyApp.getContext()).addEntryToMusicTable(d);
     //   PlaylistManager.getInstance(MyApp.getContext()).PopulateUserMusicTable();

        Intent intent;
        int launchedFrom = getIntent().getIntExtra("from",Constants.TAG_EDITOR_LAUNCHED_FROM.MAIN_LIB);
        if(launchedFrom==Constants.TAG_EDITOR_LAUNCHED_FROM.MAIN_LIB) {
            intent = new Intent(this, ActivityMain.class);
        }else if(launchedFrom == Constants.TAG_EDITOR_LAUNCHED_FROM.NOW_PLAYING){
            intent = new Intent(this, ActivityNowPlaying.class);
        }else {
            intent = new Intent(this, ActivitySecondaryLibrary.class);
        }
            intent.putExtra("refresh", true);
            intent.putExtra("position", getIntent().getIntExtra("position", -1));
            intent.putExtra("originalTitle",original_title);
            intent.putExtra("title", edited_title);
            intent.putExtra("artist", edited_artist);
            intent.putExtra("album", edited_album);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        finish();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.album_art_te){
            pickImage();
        }
    }

    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

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

    public static void dumpIntent(Intent i){

        Bundle bundle = i.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            Log.e(Constants.TAG,"Dumping Intent start");
            while (it.hasNext()) {
                String key = it.next();
                Log.e(Constants.TAG,"[" + key + "=" + bundle.get(key)+"]");
            }
            Log.e(Constants.TAG,"Dumping Intent end");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        if(data==null){
            return;
        }

        if (requestCode == 1) {
            //dumpIntent(data);
            checkAndCreateAlbumArtDirectory();
            Uri uri = data.getData();
            if(uri!=null && album_art!=null) {
                Log.v(Constants.TAG, data.toString());
                String file_path_artwork = getRealPathFromURI(uri);
                if(file_path_artwork==null){
                    Toast.makeText(this, getString(R.string.te_error_image_load), Toast.LENGTH_SHORT).show();
                    return;
                }
                Glide.with(this)
                        .load(new File(file_path_artwork)) // Uri of the picture
                        .into(album_art);
                new_artwork_path = file_path_artwork;
            }
        }
    }

    private void checkAndCreateAlbumArtDirectory(){
        File f = new File(ALBUM_ART_PATH);
        if(f.exists()){
            return;
        }
        try {
            f.mkdir();
        }catch (Exception ignored){

        }
    }

    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);

        if(cursor==null || cursor.getCount()==0){
            return null;
        }

        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                MyApp.getService().play();
                break;

            case KeyEvent.KEYCODE_MEDIA_NEXT:
                MyApp.getService().nextTrack();
                break;

            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                MyApp.getService().prevTrack();
                break;

            case KeyEvent.KEYCODE_MEDIA_STOP:
                MyApp.getService().stop();
                break;

            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                break;
        }

        return false;
    }


    @Override
    public void onBackPressed() {
        readValues();
        if(fChanged){
            unsavedDataAlert();
        }else {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        }
    }

    private void showInfoDialog(){
        if(!MyApp.getPref().getBoolean(getString(R.string.pref_show_edit_track_info_dialog),true)){
            return;
        }

        new MaterialDialog.Builder(this)
                .typeface(TypeFaceHelper.getTypeFace(this),TypeFaceHelper.getTypeFace(this))
                .title(getString(R.string.te_show_info_title))
                .content(getString(R.string.te_show_info_content))
                .positiveText(getString(R.string.te_show_info_pos))
                .negativeText(getString(R.string.te_show_info_neg))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_show_edit_track_info_dialog),false).apply();
                    }
                })
                .show();
    }
}
