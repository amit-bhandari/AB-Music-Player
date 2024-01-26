package com.music.player.bhandari.m.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.view.GestureDetectorCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.BottomOffsetDecoration;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.adapter.LyricsViewAdapter;
import com.music.player.bhandari.m.lyricCard.ActivityLyricCard;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.ViewLyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadArtInfoThread;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadLyricThread;
import com.music.player.bhandari.m.utils.UtilityFun;
import com.nshmura.snappysmoothscroller.SnapType;
import com.nshmura.snappysmoothscroller.SnappyLayoutManager;
import com.nshmura.snappysmoothscroller.SnappyLinearLayoutManager;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.blurry.Blurry;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

/**
 * Copyright 2017 Amit Bhandari AB
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class ActivityLyricView extends AppCompatActivity implements View.OnClickListener, RecyclerView.OnItemTouchListener, Lyrics.Callback
        , ActionMode.Callback, ArtistInfo.Callback {
    private Handler handler;
    private String trackTitle = "";
    private String artist = "";

    @BindView(R.id.text_view_lyric_status)
    TextView lyricStatus;
    @BindView(R.id.text_view_artist_info)
    TextView artInfoTextView;
    @BindView(R.id.lyric_view_wrapper)
    View lyricWrapper;
    @BindView(R.id.view_artist_info)
    FloatingActionButton viewArtInfoFab;
    @BindView(R.id.fab_save_lyrics)
    FloatingActionButton saveLyrics;
    @BindView(R.id.fab_video)
    FloatingActionButton watchVideo;
    @BindView(R.id.loading_lyrics_animation)
    AVLoadingIndicatorView lyricLoadAnimation;
    @BindView(R.id.dynamic_lyrics_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.root_view_instant_lyrics)
    View rootView;

    private ActionMode actionMode;
    private boolean actionModeActive = false;
    private GestureDetectorCompat gestureDetector;

    private Lyrics mLyrics;
    private ArtistInfo artistInfo;

    private boolean isLyricsSaved = false;
    private boolean isLyricsShown = true;

    LyricsViewAdapter adapter;

    private DownloadLyricThread lyricThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ColorHelper.setStatusBarGradiant(this);
        int themeSelector = MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT);
        switch (themeSelector) {
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
        setContentView(R.layout.activity_instant_lyrics);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar_);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        handler = new Handler(Looper.getMainLooper());

        initializeListeners();
    }

    private void initializeListeners() {
        lyricStatus.setOnClickListener(this);
        saveLyrics.setBackgroundTintList(ColorStateList.valueOf(ColorHelper.getWidgetColor()));
        saveLyrics.setOnClickListener(this);
        watchVideo.setBackgroundTintList(ColorStateList.valueOf(ColorHelper.getWidgetColor()));
        watchVideo.setOnClickListener(this);
        watchVideo.setVisibility(View.VISIBLE);

        growShrinkAnimate();
        viewArtInfoFab.setBackgroundTintList(ColorStateList.valueOf(ColorHelper.getWidgetColor()));
        viewArtInfoFab.setOnClickListener(this);
        rootView.setBackgroundColor(ColorHelper.getColor(R.color.blackTransparent));
    }

    private void growShrinkAnimate() {
        final ScaleAnimation growAnim = new ScaleAnimation(1.0f, 1.15f, 1.0f, 1.15f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        final ScaleAnimation shrinkAnim = new ScaleAnimation(1.15f, 1.0f, 1.15f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        growAnim.setDuration(500);
        shrinkAnim.setDuration(500);

        watchVideo.setAnimation(growAnim);
        growAnim.start();

        growAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                watchVideo.setAnimation(shrinkAnim);
                shrinkAnim.start();
            }
        });
        shrinkAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                watchVideo.setAnimation(growAnim);
                growAnim.start();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_save_lyrics:
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        saveOrDeleteLyrics();
                    }
                });
                break;

            case R.id.view_artist_info:
                //clear offline stored lyrics if any and reload
                if (artistInfo == null) {
                    Snackbar.make(rootView, getString(R.string.art_info_not_available)
                            , Snackbar.LENGTH_SHORT).show();
                    return;
                }

                toggleLyricsArtInfoView();
                break;

            case R.id.lyrics_line:
                int idx = recyclerView.getChildLayoutPosition(v);
                if (actionModeActive) {
                    myToggleSelection(idx);
                    return;
                }
                break;

            case R.id.text_view_lyric_status:
                mLyrics = null;
                updateLyrics(true);
                break;

            case R.id.fab_video:
                if (artistInfo != null) {
                    UtilityFun.LaunchYoutube(this, trackTitle + " - " + artistInfo.getCorrectedArtist());
                } else {
                    UtilityFun.LaunchYoutube(this, trackTitle + " - " + artist);
                }
                break;
        }
    }


    private void saveOrDeleteLyrics() {
        if (isLyricsSaved) {
            if (OfflineStorageLyrics.clearLyricsFromDB(trackTitle)) {
                updateSaveDeleteFabDrawable();
                Snackbar.make(rootView, getString(R.string.lyrics_removed), Snackbar.LENGTH_SHORT).show();
            }
        } else {
            if (mLyrics != null && mLyrics.getOriginalTrack().equals(trackTitle) && mLyrics.getOriginalArtist().equals(artist)) {
                TrackItem item = new TrackItem();
                item.setArtist(artist);
                item.setTitle(trackTitle);
                item.setId(-1);
                if (OfflineStorageLyrics.putInstantLyricsInDB(mLyrics, item)) {
                    updateSaveDeleteFabDrawable();
                    Snackbar.make(rootView, getString(R.string.lyrics_saved), Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(rootView, getString(R.string.error_saving_instant_lyrics)
                            , Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(rootView, getString(R.string.error_saving_instant_lyrics)
                        , Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void toggleLyricsArtInfoView() {
        if (isLyricsShown) {
            lyricWrapper.setVisibility(View.GONE);
            findViewById(R.id.artist_info_wrapper).setVisibility(View.VISIBLE);
            artInfoTextView.setText(artistInfo.getArtistContent());
            isLyricsShown = false;
            viewArtInfoFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_subject_black_24dp));
        } else {
            lyricWrapper.setVisibility(View.VISIBLE);
            findViewById(R.id.artist_info_wrapper).setVisibility(View.GONE);
            isLyricsShown = true;
            viewArtInfoFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_info_black_24dp));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.isAppVisible = false;
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable("lyrics", mLyrics);
        savedInstanceState.putParcelable("artInfo", artistInfo);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mLyrics = savedInstanceState.getParcelable("lyrics");
        artistInfo = savedInstanceState.getParcelable("artInfo");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyApp.isAppVisible = true;

        if (getIntent().getExtras() == null) {
            finish();
            return;
        }

        if (getIntent().getExtras().getBoolean("from_notif")) {
            try {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification_clicked");
                UtilityFun.logEvent(bundle);
            } catch (Exception ignored) {
            }
        }

        mLyrics = (Lyrics) getIntent().getExtras().get("lyrics");

        trackTitle = getIntent().getExtras().getString("track_title");
        artist = getIntent().getExtras().getString("artist");

        updateLyrics(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lyric_view, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        //startActivity(new Intent(this, ActivityExploreLyrics.class));
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_share:
                shareLyrics();
                break;

            case R.id.action_wrong_lyrics:
                wrongLyrics();
                break;

            case R.id.action_search:
                searchLyricDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void searchLyricDialog() {
        MaterialDialog.Builder builder = new MyDialogBuilder(this)
                .title(R.string.title_search_lyrics)
                .customView(R.layout.lyric_search_dialog, true)
                .positiveText(R.string.pos_search_lyric)
                .negativeText(R.string.cancel)
                .autoDismiss(false);

        View layout = builder.build().getCustomView();
        final EditText trackTitleEditText = layout.findViewById(R.id.track_title_edit);
        final EditText artistTextView = layout.findViewById(R.id.artist_edit);

        trackTitleEditText.setText(trackTitle);
        artistTextView.setText(artist);

        final ProgressBar progressBar = layout.findViewById(R.id.progressBar);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                trackTitleEditText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                trackTitleEditText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
            }
        }, 200);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(trackTitleEditText, InputMethodManager.SHOW_IMPLICIT);
        }

        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                if (trackTitleEditText.getText().toString().equals("")) {
                    trackTitleEditText.setError(getString(R.string.error_empty_title_lyric_search));
                    return;
                }
                String artistName = artistTextView.getText().toString();
                if (artistName.equals("")) {
                    artistName = getString(R.string.unknown_artist);
                }
                progressBar.setVisibility(View.VISIBLE);
                final String finalArtistName = artistName;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        finish();
                        Intent intent = new Intent(ActivityLyricView.this, ActivityLyricView.class);
                        intent.putExtra("track_title", trackTitleEditText.getText().toString());
                        intent.putExtra("artist", finalArtistName);
                        startActivity(intent);
                    }
                }, 1000);
            }
        });

        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });

        builder.build().show();
    }

    public void wrongLyrics() {
        if (mLyrics == null || mLyrics.getFlag() != Lyrics.POSITIVE_RESULT) {
            Toast.makeText(this, getString(R.string.error_no_lyrics), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mLyrics.getSource() == null || !mLyrics.getSource().equals(ViewLyrics.clientUserAgent)) {
            Toast.makeText(this, "No lyrics from other sources available!", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (artist != null && trackTitle != null) {
                    try {
                        ViewLyrics.fromMetaData(ActivityLyricView.this, artist, trackTitle, null, ActivityLyricView.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void shareLyrics() {
        if (mLyrics == null || mLyrics.getFlag() != Lyrics.POSITIVE_RESULT) {
            Snackbar.make(rootView, getString(R.string.error_no_lyrics)
                    , Snackbar.LENGTH_SHORT).show();
            return;
        }

        String shareBody = getString(R.string.lyrics_share_text);
        shareBody += "\n\nTrack : " + mLyrics.getTrack() + "\n" + "Artist : " + mLyrics.getArtist() + "\n\n";
        if (mLyrics.isLRC()) {
            shareBody += Html.fromHtml(adapter.getStaticLyrics()).toString();
        } else {
            shareBody += Html.fromHtml(mLyrics.getText());
        }

        shareTextIntent(shareBody);
    }

    private void shareTextIntent(String shareBody) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Lyrics");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Lyrics share!"));
    }

    private void updateLyrics(boolean discardCache) {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(trackTitle);
            getSupportActionBar().setSubtitle(artist);
        }

        artInfoTextView.setText(getString(R.string.artist_info_loading));

        TrackItem item = new TrackItem();
        item.setArtist(artist);
        item.setTitle(trackTitle);
        item.setId(-1);

        if (mLyrics != null
                && mLyrics.getOriginalArtist().toLowerCase().equals(artist.toLowerCase())
                && mLyrics.getOriginalTrack().toLowerCase().equals(trackTitle.toLowerCase())) {
            onLyricsDownloaded(mLyrics);
            return;
        }

        //set loading animation
        lyricLoadAnimation.setVisibility(View.VISIBLE);
        lyricLoadAnimation.show();

        //lyricCopyRightText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        lyricStatus.setVisibility(View.VISIBLE);
        lyricStatus.setText(getString(R.string.lyrics_loading));

        //check in offline storage
        //for saved lyrics
        mLyrics = OfflineStorageLyrics.getInstantLyricsFromDB(item);
        if (mLyrics != null) {
            onLyricsDownloaded(mLyrics);
            return;
        }

        if (!discardCache) {
            mLyrics = OfflineStorageLyrics.getLyricsFromCache(item);
            if (mLyrics != null) {
                onLyricsDownloaded(mLyrics);
                return;
            }
        }

        if (UtilityFun.isConnectedToInternet()) {
            fetchLyrics(item.getArtist(), item.getTitle(), null);
        } else {
            lyricStatus.setText(getString(R.string.no_connection));
            lyricLoadAnimation.hide();
        }
    }

    private void fetchArtistImage(String artist) {
        artist = UtilityFun.filterArtistString(artist);

        artistInfo = OfflineStorageArtistBio.getArtistInfoFromCache(artist);
        if (artistInfo != null) {
            onArtInfoDownloaded(artistInfo);
        } else {
            new DownloadArtInfoThread(this, artist, null).start();
        }
    }

    private void fetchLyrics(String... params) {

        String artist = params[0];
        String title = params[1];

        ///filter title string
        //title = filterTitleString(title);

        String url = null;
        if (params.length > 2)
            url = params[2];

        if (artist != null && title != null) {
            if (url == null)
                lyricThread = new DownloadLyricThread(this, true, null, artist, title);
            else
                lyricThread = new DownloadLyricThread(this, true, null, url, artist, title);
            lyricThread.start();
        }
    }

    @Override
    public void onLyricsDownloaded(Lyrics lyrics) {
        if (!(lyrics.getOriginalArtist().toLowerCase().equals(artist.toLowerCase()) && lyrics.getOriginalTrack().toLowerCase().equals(trackTitle.toLowerCase()))) {
            return;
        }

        //put lyrics to cache
        OfflineStorageLyrics.putLyricsToCache(lyrics);

        lyricLoadAnimation.hide();
        mLyrics = lyrics;
        if (lyrics.getFlag() == Lyrics.POSITIVE_RESULT) {
            lyricStatus.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            lyricStatus.setVisibility(View.GONE);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(lyrics.getTrack());
                getSupportActionBar().setSubtitle(lyrics.getArtist());
            }

            //if(!lyrics.getArtist().equals(mLyrics.getArtist())){
            fetchArtistImage(lyrics.getArtist());
            //}

            initializeLyricsView();
        } else {
            lyricStatus.setText(getString(R.string.tap_to_refresh_lyrics));
            lyricStatus.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                updateSaveDeleteFabDrawable();
            }
        });
    }

    private void initializeLyricsView() {
        if (mLyrics == null) {
            return;
        }

        adapter = new LyricsViewAdapter(this, mLyrics);
        adapter.setNoDynamicLyrics(true);
        SnappyLayoutManager snappyLinearLayoutManager = new SnappyLinearLayoutManager(this);
        snappyLinearLayoutManager.setSnapType(SnapType.CENTER);
        snappyLinearLayoutManager.setSnapDuration(1500);
        //layoutManager.setSnapInterpolator(new DecelerateInterpolator());

        // Attach layout manager to the RecyclerView:
        recyclerView.setLayoutManager((RecyclerView.LayoutManager) snappyLinearLayoutManager);

        float offsetPx = getResources().getDimension(R.dimen.bottom_offset_secondary_lib);
        BottomOffsetDecoration bottomOffsetDecoration = new BottomOffsetDecoration((int) offsetPx);
        recyclerView.addItemDecoration(bottomOffsetDecoration);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(this);
        gestureDetector =
                new GestureDetectorCompat(this, new ActivityLyricView.RecyclerViewDemoOnGestureListener());
    }

    private void updateSaveDeleteFabDrawable() {
        final Drawable drawable;
        if (OfflineStorageLyrics.isLyricsPresentInDB(trackTitle, (mLyrics == null) ? -1 : mLyrics.getTrackId())) {
            isLyricsSaved = true;
            drawable = getResources().getDrawable(R.drawable.ic_delete_black_24dp);
        } else {
            isLyricsSaved = false;
            drawable = getResources().getDrawable(R.drawable.ic_save_black_24dp);
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                saveLyrics.setImageDrawable(drawable);
            }
        });
    }

    private void setBlurryBackground(Bitmap b) {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeIn.setDuration(2000);
        findViewById(R.id.full_screen_iv).startAnimation(fadeIn);

        Blurry.with(this).radius(1).color(Color.argb(100
                , 50, 0, 0)).from(b).into(((ImageView) findViewById(R.id.full_screen_iv)));
    }

    @Override
    public void onArtInfoDownloaded(ArtistInfo artistInfo) {
        this.artistInfo = artistInfo;
        if (artistInfo.getArtistContent().equals("")) {
            artInfoTextView.setText(R.string.artist_info_no_result);
            return;
        }
        OfflineStorageArtistBio.putArtistInfoToCache(artistInfo);
        new ActivityLyricView.SetBlurryImagetask().execute(artistInfo);
    }

    private class RecyclerViewDemoOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                onClick(view);
            }
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (actionModeActive) {
                return;
            }
            // Start the CAB using the ActionMode.Callback defined above
            actionMode = startActionMode(ActivityLyricView.this);
            actionModeActive = true;
            int idx = recyclerView.getChildPosition(view);
            myToggleSelection(idx);
            super.onLongPress(e);
        }
    }

    private void myToggleSelection(int idx) {
        adapter.toggleSelection(idx);
        //String title = adapter.getSelectedItemCount();
        //actionMode.setTitle(title);
        if (adapter.getSelectedItemCount() == 0) {
            actionMode.finish();
            actionMode = null;
            return;
        }
        int numberOfItems = adapter.getSelectedItemCount();
        String selectionString = numberOfItems == 1 ? " item selected" : " items selected";
        String title = numberOfItems + selectionString;
        actionMode.setTitle(title);
    }

    @SuppressLint("StaticFieldLeak")
    private class SetBlurryImagetask extends AsyncTask<ArtistInfo, String, Bitmap> {

        Bitmap b;

        @Override
        protected Bitmap doInBackground(ArtistInfo... params) {

            //store file in cache with artist id as name
            //create folder in cache for artist images
            String CACHE_ART_THUMBS = MyApp.getContext().getCacheDir() + "/art_thumbs/";
            String actual_file_path = CACHE_ART_THUMBS + params[0].getOriginalArtist();
            File f = new File(CACHE_ART_THUMBS);
            if (!f.exists()) {
                f.mkdir();
            }
            if (!new File(actual_file_path).exists()) {
                //create file
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(new File(actual_file_path));
                    URL url = new URL(params[0].getImageUrl());
                    InputStream inputStream = url.openConnection().getInputStream();
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0;
                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, bufferLength);
                    }
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            b = BitmapFactory.decodeFile(actual_file_path);
            return b;
        }

        protected void onPostExecute(Bitmap b) {
            if (b != null) {
                setBlurryBackground(b);
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_cab_recyclerview_lyrics, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_share:
                shareTextIntent(getSelectedLyricString().toString());
                actionMode.finish();
                actionModeActive = false;
                break;

            case R.id.menu_lyric_card:
                Intent intent = new Intent(this, ActivityLyricCard.class);
                intent.putExtra("lyric", getSelectedLyricString().toString())
                        .putExtra("artist", mLyrics.getArtist())
                        .putExtra("track", mLyrics.getTrack());
                startActivity(intent);
                break;
        }
        return false;
    }

    private StringBuilder getSelectedLyricString() {
        StringBuilder shareString = new StringBuilder();
        List<Integer> selectedItemPositions = adapter.getSelectedItems();
        int currPos;
        for (int i = 0; i <= selectedItemPositions.size() - 1; i++) {
            currPos = selectedItemPositions.get(i);
            String lyricLine = adapter.getLineAtPosition(currPos);
            if (lyricLine != null) {
                shareString.append(lyricLine).append("\n");
            }
        }
        return shareString;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        actionMode.finish();
        actionModeActive = false;
        adapter.clearSelections();
    }
}
