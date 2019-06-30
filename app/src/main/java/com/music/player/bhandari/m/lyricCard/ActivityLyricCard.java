package com.music.player.bhandari.m.lyricCard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.FontRequest;
import android.support.v4.provider.FontsContractCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.activity.ActivityPermissionSeek;
import com.music.player.bhandari.m.customViews.ZoomTextView;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadArtInfoThread;
import com.music.player.bhandari.m.utils.AppLaunchCountManager;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
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
 *
 * I know, I know, there is not even bit of "Good Architecture" is followed in this class
 * Everything is mixed up, UI, data, card creation. Everything.
 * But its too late for this app to follow architectural guidelines.
 * You agree with me? Cheers.
 * If not. May god help you.
 *
 */

public class ActivityLyricCard extends AppCompatActivity implements View.OnTouchListener  {

    private final int PICK_IMAGE = 0;

    @BindView(R.id.rv_colors) RecyclerView recyclerViewColors;
    @BindView(R.id.rv_images) RecyclerView recyclerViewImages;
    @BindView(R.id.mainImageLyricCard) ImageView mainImage;
    @BindView(R.id.text_lyric) ZoomTextView lyricText;
    @BindView(R.id.text_artist) ZoomTextView artistText;
    @BindView(R.id.text_track) ZoomTextView trackText;
    @BindView(R.id.dragView) View dragView;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.brightnessSeekBar) SeekBar brightnessSeekBar;
    @BindView(R.id.overImageLayer) View overImageLayer;
    @BindView(R.id.watermark) View watermark;
    @BindView(R.id.root_view_lyric_card) View rootView;

    float dx;   //for dragging text views
    float dy;

    private ImagesAdapter imagesAdapter = new ImagesAdapter();
    private Handler mHandler;

    private int currentTextAlignment=0;
    private List<Typeface> typefaces = new ArrayList<>();
    int currentFontPosition = 0;

    private final static int DAYS_UNTIL_CACHE = 5;//Min number of days

    boolean typefaceSet = false;

    //when watermark is removed
    private InterstitialAd mInterstitialAd;
    private boolean adShown =false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ColorHelper.setStatusBarGradiant(this);
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyric_card);
        ButterKnife.bind(this);

        if(getIntent().getExtras()==null){
            Toast.makeText(this, "Missing lyric text", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if(MyApp.getPref().getBoolean("pref_first_time_lyric_card_launch", true)){
            MyApp.getPref().edit().putBoolean("pref_first_time_lyric_card_launch", false).apply();
            firstTimeLaunch();
        }

        String text;
        String author;
        String track;
        if(getIntent().getExtras().getString("lyric")!=null){
            text = getIntent().getExtras().getString("lyric");
        }else {
            text = "";
        }

        if(getIntent().getExtras().getString("artist")!=null){
            author = getIntent().getExtras().getString("artist");
        }else {
            author = "";
        }

        if(getIntent().getExtras().getString("track")!=null){
            track = getIntent().getExtras().getString("track");
        }else {
            track = "";
        }

        lyricText.setText(text);
        artistText.setText(author);
        trackText.setText(track);

        initiateToolbar();
        fillFonts();
        initiateUI();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setTheme() {
        //if player service not running, kill the app
        if(MyApp.getService()==null){
            UtilityFun.restartApp();
        }

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
    }

    private void initiateToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.GetStatusBarColor());
        }*/

        setTitle("Lyric Card");
    }

    private void initiateUI(){
        //rootView.setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());

        recyclerViewColors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewColors.setAdapter(new ColorAdapter());

        recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewImages.setAdapter(imagesAdapter);

        //get images links
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> urls;
        urls = new Gson().fromJson(MyApp.getPref().getString(getString(R.string.pref_card_image_links), ""), type);
        if(System.currentTimeMillis() >= MyApp.getPref().getLong(getString(R.string.pref_card_image_saved_at), 0) + DAYS_UNTIL_CACHE
                && urls!=null){
            imagesAdapter.setUrls(urls);
        }else {
            FirebaseDatabase.getInstance().getReference().child("cardlinksNew").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("ActivityLyricCard", "onDataChange: ");
                    Map<String, String> urls = new LinkedHashMap<>();

                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            try {
                            Map<String, String> map = (Map) snap.getValue();
                            urls.put(map.get("thumb"), map.get("image"));
                            }catch (Exception ignored){
                            }
                        }

                    imagesAdapter.setUrls(urls);

                    //cache links in shared pref
                    MyApp.getPref().edit().putString(getString(R.string.pref_card_image_links), new Gson().toJson(urls)).apply();
                    MyApp.getPref().edit().putLong(getString(R.string.pref_card_image_saved_at), System.currentTimeMillis()).apply();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("ActivityLyricCard", "onCancelled: " + databaseError.getMessage());
                    Toast.makeText(ActivityLyricCard.this, "Error retrieving images from server", Toast.LENGTH_SHORT).show();
                }
            });
        }

        initiateDragView();

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int in, boolean b) {
                double i = seekBar.getProgress()/100.0d;
                int alpha = (int) Math.round(i * 255);
                String hex = Integer.toHexString(alpha).toUpperCase();
                if (hex.length() == 1) hex = "0" + hex;
                try {
                    overImageLayer.setBackgroundColor(Color.parseColor("#" + hex + "000000"));
                }catch (NumberFormatException e){
                    Log.d("ActivityLyricCard", "onProgressChanged: Color parse exception");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        brightnessSeekBar.setProgress(30);
    }

    private void initiateDragView(){
        artistText.setOnTouchListener(this);
        lyricText.setOnTouchListener(this);
        trackText.setOnTouchListener(this);
    }

    private void firstTimeLaunch(){
        if(UtilityFun.isConnectedToInternet()){
            Toast.makeText(this, R.string.first_launch_lyric_card, Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, R.string.first_launch_lyric_card_no_internet, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                dx = v.getX() - event.getRawX();
                dy = v.getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE :
                switch (v.getId()){
                    case R.id.text_artist:
                        artistText.animate()
                                .x(event.getRawX() + dx)
                                .y(event.getRawY() + dy)
                                .setDuration(0)
                                .start();
                        break;

                    case R.id.text_lyric:
                        lyricText.animate()
                            .x(event.getRawX() + dx)
                            .y(event.getRawY() + dy)
                            .setDuration(0)
                            .start();
                        break;

                    case R.id.text_track:
                        trackText.animate()
                                .x(event.getRawX() + dx)
                                .y(event.getRawY() + dy)
                                .setDuration(0)
                                .start();
                        break;
                }
                break;

            default: return false;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lyric_card, menu);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!MyApp.getPref().getBoolean(getString(R.string.pref_info_lyric_card_shown), false)) {
                        showFirstTimeInfo();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, 1000);
        return true;
    }

    private void showFirstTimeInfo(){
        new TapTargetSequence(ActivityLyricCard.this)
                .targets(
                        TapTarget.forView(findViewById(R.id.action_font), "Change text font by clicking here")
                                .outerCircleColorInt(ColorHelper.getPrimaryColor())
                                .outerCircleAlpha(0.9f)
                                .transparentTarget(true)
                                .titleTextColor(R.color.colorwhite)
                                .descriptionTextColor(R.color.colorwhite)
                                .drawShadow(true)
                                .tintTarget(true),
                        TapTarget.forView(findViewById(R.id.action_alignment), "Change text alignment by clicking here")
                                .outerCircleColorInt(ColorHelper.getPrimaryColor())
                                .outerCircleAlpha(0.9f)
                                .transparentTarget(true)
                                .titleTextColor(R.color.colorwhite)
                                .descriptionTextColor(R.color.colorwhite)
                                .drawShadow(true)
                                .tintTarget(true),
                        TapTarget.forView(findViewById(R.id.action_edit), "Edit lyric text by clicking here")
                                .outerCircleColorInt(ColorHelper.getPrimaryColor())
                                .outerCircleAlpha(0.9f)
                                .transparentTarget(true)
                                .titleTextColor(R.color.colorwhite)
                                .descriptionTextColor(R.color.colorwhite)
                                .drawShadow(true)
                                .tintTarget(true),
                        TapTarget.forView(findViewById(R.id.black_overlay_wrap), "Blacken background image by using this slider")
                                .outerCircleColorInt(ColorHelper.getPrimaryColor())
                                .outerCircleAlpha(0.9f)
                                .transparentTarget(true)
                                .titleTextColor(R.color.colorwhite)
                                .descriptionTextColor(R.color.colorwhite)
                                .drawShadow(true)
                                .tintTarget(true),
                        TapTarget.forView(findViewById(R.id.rv_colors), "Choose text color among given options")
                                .outerCircleColorInt(ColorHelper.getPrimaryColor())
                                .outerCircleAlpha(0.9f)
                                .transparentTarget(true)
                                .titleTextColor(R.color.colorwhite)
                                .descriptionTextColor(R.color.colorwhite)
                                .drawShadow(true)
                                .tintTarget(true),
                        TapTarget.forView(findViewById(R.id.text_artist), "You can remove artist or album text by dragging it towards bottom of image.")
                                .outerCircleColorInt(ColorHelper.getPrimaryColor())
                                .outerCircleAlpha(0.9f)
                                .transparentTarget(true)
                                .titleTextColor(R.color.colorwhite)
                                .descriptionTextColor(R.color.colorwhite)
                                .drawShadow(true)
                                .tintTarget(true),
                        TapTarget.forView(recyclerViewImages.getLayoutManager().findViewByPosition(0), "You can select custom or artist image from here for background.")
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
                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        // Yay
                        MyApp.getPref().edit().putBoolean(getString(R.string.pref_info_lyric_card_shown), true).apply();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Boo
                    }
                }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;

            case R.id.action_share:
                shareCard();
                break;

            case R.id.action_font:
                changeFont();
                break;

            case R.id.action_alignment:
                changeAlignment();
                break;

            case R.id.action_edit:
                showTextEditDialog();
                break;

            case R.id.action_save:
                File f = createImageFile(false);
                if(f!=null) {
                    Toast.makeText(this, "Lyric card is saved at " +f.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_remove_watermark:
                if(item.isChecked()){
                    item.setChecked(false);
                    watermark.setVisibility(View.VISIBLE);
                }else {
                    item.setChecked(true);
                    watermark.setVisibility(View.INVISIBLE);
                    if(!UtilityFun.isAdsRemoved() && !adShown) {
                        launchAd();
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private File createImageFile(Boolean temp){
        dragView.destroyDrawingCache();  //if not done, image is going to be overridden every time
        dragView.setDrawingCacheEnabled(true);
        Bitmap bitmap = dragView.getDrawingCache();

        File dir =new File(Environment.getExternalStorageDirectory().toString() + "/abmusic");
        dir.mkdirs();

        String fileName = "temp.jpeg";
        if(!temp){
            fileName = UUID.randomUUID().toString()+".jpeg";
        }

        File lyricCardFile = new File(dir, fileName);
        if(lyricCardFile.exists()) lyricCardFile.delete();

        try{
            FileOutputStream stream = new FileOutputStream(lyricCardFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
        }catch (Exception e){
            Log.d("ActivityLyricCard", "shareCard: " + e.getLocalizedMessage());
            Toast.makeText(this, "Error saving card on storage ", Toast.LENGTH_SHORT).show();
        }
        return  lyricCardFile;
    }

    private void shareCard(){
        File lyricCardFile = createImageFile(true);
        if(lyricCardFile==null) return;
        try {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/*");
            share.putExtra(Intent.EXTRA_STREAM
                    , FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + "com.bhandari.music.provider", lyricCardFile));
            share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_lyric_card_extra_text));
            startActivity(Intent.createChooser(share, "Share Lyric Card"));
        }catch (Exception e){
            Toast.makeText(this, "Error while sharing, lyric card is saved at " + lyricCardFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }

    private void fillFonts(){
        requestDownload(new QueryBuilder("Trade Winds").build());  //unique
        requestDownload(new QueryBuilder("Indie Flower").build());  //unique
        requestDownload(new QueryBuilder("Satisfy").build());  //unique
        requestDownload(new QueryBuilder("Ubuntu").build());  //unique
        requestDownload(new QueryBuilder("Roboto Slab").build());  //unique
        requestDownload(new QueryBuilder("Cabin Sketch").build()); //good
        requestDownload(new QueryBuilder("Condiment").build()); //good cursue
        requestDownload(new QueryBuilder("Caveat Brush").build());
        requestDownload(new QueryBuilder("Cherry Swash").build());  //unique
        requestDownload(new QueryBuilder("Concert One").build());  //unique
        requestDownload(new QueryBuilder("Nova Round").build());  //unique
        requestDownload(new QueryBuilder("Nova Script").build());  //unique
        requestDownload(new QueryBuilder("Pacifico").build());  //unique
        requestDownload(new QueryBuilder("Prompt").build());  //unique
        requestDownload(new QueryBuilder("Purple Purse").build());  //unique
        requestDownload(new QueryBuilder("Quantico").build());  //unique
        requestDownload(new QueryBuilder("name=Raleway&amp;weight=700").build());  //unique
        requestDownload(new QueryBuilder("Roboto").build());  //unique
        requestDownload(new QueryBuilder("Slabo 13px").build());  //unique
        requestDownload(new QueryBuilder("Source Sans Pro").build());  //unique
        requestDownload(new QueryBuilder("Montserrat").build());  //unique1
        requestDownload(new QueryBuilder("Lora").build());
    }

    private void requestDownload(String query) {

        Log.d("ActivityLyricCard", "requestDownload: " + query);

        FontRequest request = new FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                query,
                R.array.com_google_android_gms_fonts_certs);

        FontsContractCompat.FontRequestCallback callback = new FontsContractCompat
                .FontRequestCallback() {
            @Override
            public void onTypefaceRetrieved(Typeface typeface) {
                Log.d("ActivityLyricCard", "onTypefaceRetrieved: " + typeface.toString());
                if(!typefaceSet){
                    lyricText.setTypeface(typeface);
                    artistText.setTypeface(typeface);
                    trackText.setTypeface(typeface);
                    typefaceSet = true;
                }
                typefaces.add(typeface);
            }

            @Override
            public void onTypefaceRequestFailed(int reason) {
                Log.d("ActivityLyricCard", "onTypefaceRequestFailed: " + reason);
            }
        };
        FontsContractCompat
                .requestFont(this, request, callback,
                        getHandlerThreadHandler());
    }

    private Handler getHandlerThreadHandler() {
        if (mHandler == null) {
            HandlerThread handlerThread = new HandlerThread("fonts");
            handlerThread.start();
            mHandler = new Handler(handlerThread.getLooper());
        }
        return mHandler;
    }

    private void changeFont(){
        if(currentFontPosition>=typefaces.size()-1){
            currentFontPosition=0;
            lyricText.setTypeface(typefaces.get(currentFontPosition));
            artistText.setTypeface(typefaces.get(currentFontPosition));
            trackText.setTypeface(typefaces.get(currentFontPosition));
        }else {
            int index=++currentFontPosition;
            lyricText.setTypeface(typefaces.get(index));
            artistText.setTypeface(typefaces.get(index));
            trackText.setTypeface(typefaces.get(index));
        }
    }

    private void changeAlignment(){
        switch (currentTextAlignment){
            case 1:
                lyricText.setGravity(Gravity.END);
                artistText.setGravity(Gravity.END);
                trackText.setGravity(Gravity.END);
                currentTextAlignment = 2;
                break;

            case 2:
                lyricText.setGravity(Gravity.START);
                artistText.setGravity(Gravity.START);
                trackText.setGravity(Gravity.START);
                currentTextAlignment = 0;
                break;

            default:
                lyricText.setGravity(Gravity.CENTER);
                artistText.setGravity(Gravity.CENTER);
                trackText.setGravity(Gravity.CENTER);
                currentTextAlignment = 1;
                break;
        }
    }

    private void setMainImage(String url) {
        progressBar.setVisibility(View.VISIBLE);
        Glide.with(getApplicationContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ActivityLyricCard.this, R.string.error_loading_image_lyric_card, Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mainImage);
    }

    private void setMainImage(Uri uri) {
        progressBar.setVisibility(View.VISIBLE);
        Glide.with(ActivityLyricCard.this)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ActivityLyricCard.this, R.string.error_loading_image_lyric_card, Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mainImage);
    }

    void addCustomImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    void addArtistImage(){
        mainImage.setImageBitmap(null);
        progressBar.setVisibility(View.VISIBLE);

        ArtistInfo info = OfflineStorageArtistBio.getArtistInfoFromCache(artistText.getText().toString());
        Log.d("ActivityLyricCard", "addArtistImage: " + info);
        if(info!=null){
            setMainImage(info.getImageUrl());
        } else if (UtilityFun.isConnectedToInternet()) {
            String artist = UtilityFun.filterArtistString(artistText.getText().toString());
            new DownloadArtInfoThread(new ArtistInfo.Callback() {
                @Override
                public void onArtInfoDownloaded(ArtistInfo artistInfo) {
                    if(artistInfo!=null && !artistInfo.getImageUrl().isEmpty()) {
                        setMainImage(artistInfo.getImageUrl());
                    }else {
                        Toast.makeText(ActivityLyricCard.this, "Artist image not found", Toast.LENGTH_SHORT).show();
                        if(imagesAdapter.urls.size()>0){
                            setMainImage(imagesAdapter.urls.get(0));
                        }
                    }
                }
            }, artist, null).start();
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Not connected to internet", Toast.LENGTH_SHORT).show();
        }
    }

    void showTextEditDialog(){
        MaterialDialog.Builder builder = new MyDialogBuilder(this)
                .title("Edit text")
                .positiveText(getString(R.string.okay))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        View view = dialog.getCustomView();
                        if(view==null) return;
                        AppCompatEditText lyric = view.findViewById(R.id.text_lyric);
                        AppCompatEditText artist = view.findViewById(R.id.text_artist);
                        AppCompatEditText track = view.findViewById(R.id.text_track);

                        lyricText.setText(lyric.getText());
                        artistText.setText(artist.getText());
                        trackText.setText(track.getText());
                    }
                })
                .customView(R.layout.dialog_edit_lyric_card_texts, true);

        MaterialDialog dialog = builder.build();

        View view = dialog.getCustomView();
        if(view==null) return;
        AppCompatEditText lyric = view.findViewById(R.id.text_lyric);
        AppCompatEditText artist = view.findViewById(R.id.text_artist);
        AppCompatEditText track = view.findViewById(R.id.text_track);

        lyric.setText(lyricText.getText());
        artist.setText(artistText.getText());
        track.setText(trackText.getText());

        dialog.show();
    }

    void launchAd(){
        Log.d("ActivityLyricCard", "launchAd: ");

        Toast.makeText(this, R.string.ad_serve_toast, Toast.LENGTH_LONG).show();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.inter_lyric_card_activity));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
                adShown = true;
            }
        });
        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("F40E78AED9B7FE233362079AC4C05B61")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            Log.d("ActivityLyricCard", "onActivityResult: " + data);
            if(data!=null && data.getData()!=null) setMainImage(data.getData());
            else Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.MyViewHolder>{

        String[] colors = getResources().getStringArray(R.array.colors);

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(ActivityLyricCard.this).inflate(R.layout.item_color, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.color.setBackgroundColor(Color.parseColor(colors[position]));
        }

        @Override
        public int getItemCount() {
            return colors.length;
        }

        class MyViewHolder extends RecyclerView.ViewHolder{

            ImageView color;

            public MyViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lyricText.setTextColor(Color.parseColor(colors[getLayoutPosition()]));
                        artistText.setTextColor(Color.parseColor(colors[getLayoutPosition()]));
                        trackText.setTextColor(Color.parseColor(colors[getLayoutPosition()]));
                    }
                });
                color = itemView.findViewById(R.id.colorView);
            }
        }
    }

    class ImagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private Map<String,String> urls = new LinkedHashMap<>();

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v;
            switch (viewType){
                case 0:
                    v = LayoutInflater.from(ActivityLyricCard.this).inflate(R.layout.item_custom_image, parent, false);
                    return new CustomHolder(v);

                case 1:
                    v = LayoutInflater.from(ActivityLyricCard.this).inflate(R.layout.item_artist_image, parent, false);
                    return new ArtistHolder(v);

                case 2:
                    v = LayoutInflater.from(ActivityLyricCard.this).inflate(R.layout.item_image_lyric_card, parent, false);
                    return new ImageHolder(v);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            switch (position){
                case 0:
                    return 0;

                case 1:
                    return 1;

                default:
                    return 2;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()){
                case 1:
                    //load artist image in thumbnail view
                    ArtistInfo info = OfflineStorageArtistBio.getArtistInfoFromCache(artistText.getText().toString());
                    Log.d("ImagesAdapter", "onBindViewHolder: " + info);
                    if(info!=null){
                        ((ArtistHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        loadImageUsingGlide(info.getImageUrl(), ((ArtistHolder) holder).imageView ,  ((ArtistHolder) holder).progressBar);
                    }
                    else if (UtilityFun.isConnectedToInternet()) {
                        String artist = UtilityFun.filterArtistString(artistText.getText().toString());
                        new DownloadArtInfoThread(new ArtistInfo.Callback() {
                            @Override
                            public void onArtInfoDownloaded(ArtistInfo artistInfo) {
                                if(artistInfo!=null && !artistInfo.getImageUrl().isEmpty()) {
                                    loadImageUsingGlide(artistInfo.getImageUrl(), ((ArtistHolder) holder).imageView,  ((ArtistHolder) holder).progressBar);
                                }
                            }
                        }, artist, null).start();
                    }
                    break;

                case 2:
                    if(holder instanceof ImageHolder) {
                        ((ImageHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        loadImageUsingGlide(getThumbElementByIndex(position-2),((ImageHolder) holder).imageView,  ((ImageHolder) holder).progressBar);
                    }
                    break;
            }
        }

        private void loadImageUsingGlide(String url, ImageView view, final ProgressBar progressBar){
            Glide.with(ActivityLyricCard.this)
                    .load(url)     //offset for 2 extra elements
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                           progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(view);
        }

        @Override
        public int getItemCount() {
            return urls.size()+2; //offset for 2 extra elements
        }

        void setUrls(Map<String , String> urls){
            this.urls = urls;
            if(urls.size()!=0) {
                List<String> mainUrls = new ArrayList<>(urls.values());
                setMainImage(mainUrls.get(UtilityFun.getRandom(0,urls.size())));
            }
            notifyDataSetChanged();
        }

        private String getThumbElementByIndex(int index){
            return  (urls.keySet().toArray())[ index ].toString();
        }

        private String getMainElementByIndex(int index){
            return  urls.get( (urls.keySet().toArray())[ index ] );
        }

        class ImageHolder extends RecyclerView.ViewHolder{
            ImageView imageView;
            ProgressBar progressBar;

            ImageHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setMainImage(getMainElementByIndex(getLayoutPosition()-2));
                    }
                });
                imageView = itemView.findViewById(R.id.image_lyric_card);
                progressBar = itemView.findViewById(R.id.progressBar);
            }
        }

        class ArtistHolder extends RecyclerView.ViewHolder{
            ImageView imageView;
            ProgressBar progressBar;

            ArtistHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.addArtistImage);
                progressBar = itemView.findViewById(R.id.progressBar);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addArtistImage();
                    }
                });

            }
        }

        class CustomHolder extends RecyclerView.ViewHolder{
            CustomHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addCustomImage();
                    }
                });
            }
        }
    }
}
