package com.music.player.bhandari.m.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.UIElementHelper.MyDialogBuilder;
import com.music.player.bhandari.m.adapter.TopTracksAdapter;
import com.music.player.bhandari.m.lyricsExplore.OnPopularTracksReady;
import com.music.player.bhandari.m.lyricsExplore.PopularTrackRepo;
import com.music.player.bhandari.m.lyricsExplore.Track;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.utils.UtilityFun;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

/**
 * Copyright 2017 Amit Bhandari AB
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">...</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class ActivityExploreLyrics extends AppCompatActivity implements OnPopularTracksReady
        , View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.root_view_lyrics_explore)
    View rootView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.recycler_view_wrapper)
    View rvWrapper;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.statusTextView)
    TextView statusText;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fab_right_side)
    FloatingActionButton fab;
    @BindView(R.id.trending_now_text)
    TextView trendingNow;
    private Handler handler;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, ActivityMain.class));
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ColorHelper.setStatusBarGradiant(this);

        int themeSelector = MyApp.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT);
        switch (themeSelector) {
            case Constants.PRIMARY_COLOR.DARK, Constants.PRIMARY_COLOR.GLOSSY ->
                    setTheme(R.style.AppThemeDark);
            case Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight);
        }

        setContentView(R.layout.activity_lyrics_explore);
        ButterKnife.bind(this);
        growShrinkAnimate();

        handler = new Handler(Looper.getMainLooper());
        Toolbar toolbar = findViewById(R.id.toolbar_);
        toolbar.setTitle(R.string.lyrics_explore);
        setSupportActionBar(toolbar);

        // add back ar
        // row to toolbar
        if (getSupportActionBar() != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        swipeRefreshLayout.setOnRefreshListener(this);

        fab.setBackgroundTintList(ColorStateList.valueOf(ColorHelper.getWidgetColor()));
        fab.setOnClickListener(this);

        try {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "explore_lyrics_launched");
            UtilityFun.logEvent(bundle);
        } catch (Exception ignored) {
        }
    }

    private void growShrinkAnimate() {
        final ScaleAnimation growAnim = new ScaleAnimation(1.0f, 1.15f, 1.0f, 1.15f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        final ScaleAnimation shrinkAnim = new ScaleAnimation(1.15f, 1.0f, 1.15f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        growAnim.setDuration(500);
        shrinkAnim.setDuration(500);

        fab.setAnimation(growAnim);
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
                fab.setAnimation(shrinkAnim);
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
                fab.setAnimation(growAnim);
                growAnim.start();
            }
        });
    }

    private void loadPopularTracks(Boolean lookInCache) {
        String country = MyApp.getPref().getString(getString(R.string.pref_user_country), "");
        new PopularTrackRepo().fetchPopularTracks(country, this, lookInCache);
    }

    @OnClick(R.id.statusTextView)
    void retryLoading() {
        progressBar.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.GONE);
        handler.postDelayed(() -> loadPopularTracks(false), 1000);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d("ActivityExploreLyrics", "onNewIntent: ");
    }

    @Override
    public void popularTracksReady(final List<Track> tracks, @NonNull final String region) {

        handler.post(() -> {
            swipeRefreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
            if (tracks.size() == 0) {
                statusText.setText(R.string.error_fetching_popular_tracks);
                statusText.setVisibility(View.VISIBLE);
            } else {
                TopTracksAdapter adapter = new TopTracksAdapter(ActivityExploreLyrics.this, tracks);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(ActivityExploreLyrics.this));
                rvWrapper.setVisibility(View.VISIBLE);
                trendingNow.setText(getString(R.string.trending_now_in, region));
            }
        });


    }

    @Override
    public void error() {
        handler.post(() -> {
            progressBar.setVisibility(View.GONE);
            statusText.setText(R.string.error_fetching_popular_tracks);
            statusText.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.isAppVisible = true;
        loadPopularTracks(getIntent().getExtras() == null || !getIntent().getExtras().getBoolean("fresh_load", false));

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("search_on_launch", false)) {
            Log.d("ActivityExploreLyrics", "onCreate: search lyric dialog on startup");
            searchLyricDialog();
        }

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("from_notif")) {
            try {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "notification_clicked");
                UtilityFun.logEvent(bundle);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.isAppVisible = false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_right_side) {
            searchLyricDialog();
        }
    }

    private void searchLyricDialog() {
        MaterialDialog.Builder builder = new MyDialogBuilder(this)
                .title(R.string.title_search_lyrics)
                .customView(R.layout.lyric_search_dialog, true)
                .positiveText(R.string.pos_search_lyric)
                .negativeText(R.string.cancel)
                .autoDismiss(false);

        View layout = builder.build().getCustomView();
        if (layout == null) {
            return;
        }
        final EditText trackTitle = layout.findViewById(R.id.track_title_edit);
        final EditText artist = layout.findViewById(R.id.artist_edit);
        final ProgressBar progressBar = layout.findViewById(R.id.progressBar);

        handler.postDelayed(() -> {
            trackTitle.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
            trackTitle.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
        }, 200);

        builder.onPositive((dialog, which) -> {
            if (trackTitle.getText().toString().equals("")) {
                trackTitle.setError(getString(R.string.error_empty_title_lyric_search));
                return;
            }
            String artistName = artist.getText().toString();
            if (artistName.equals("")) {
                artistName = getString(R.string.unknown_artist);
            }
            progressBar.setVisibility(View.VISIBLE);
            final String finalArtistName = artistName;
            handler.postDelayed(() -> {
                Intent intent = new Intent(ActivityExploreLyrics.this, ActivityLyricView.class);
                intent.putExtra("track_title", trackTitle.getText().toString());
                intent.putExtra("artist", finalArtistName);
                startActivity(intent);
                dialog.dismiss();
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "search_lyric_manually");
                    UtilityFun.logEvent(bundle);
                } catch (Exception ignored) {
                }
            }, 1000);
        });

        builder.onNegative((dialog, which) -> dialog.dismiss());

        MaterialDialog dialog = builder.build();

        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;

        dialog.show();
    }

    @Override
    public void onRefresh() {
        if (!UtilityFun.isConnectedToInternet()) {
            Toast.makeText(this, "No Connection!", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        rvWrapper.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.GONE);
        loadPopularTracks(false);
    }

    //for catching exception generated by recycler view which was causing abend, no other way to handle this
    static class WrapContentLinearLayoutManager extends LinearLayoutManager {
        WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        //... constructor
        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
    }
}
