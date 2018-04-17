package com.music.player.bhandari.m.lyricCard;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.activity.ActivityPermissionSeek;
import com.music.player.bhandari.m.databinding.ActivityLyricCardBinding;
import com.music.player.bhandari.m.model.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by abami on 17-Apr-18.
 */

public class ActivityLyricCard extends AppCompatActivity {

    ModelLyricCard model;

    @BindView(R.id.rv_colors) RecyclerView recyclerViewColors;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //if player service not running, kill the app
        if(MyApp.getService()==null){
            Intent intent = new Intent(this, ActivityPermissionSeek.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
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

        super.onCreate(savedInstanceState);

        ActivityLyricCardBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_lyric_card);

        ButterKnife.bind(this);

        model = new ModelLyricCard();
        binding.setModel(model);

        findViewById(R.id.root_view_lyric_card).setBackgroundDrawable(ColorHelper.getColoredThemeGradientDrawable());

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

        setTitle("Lyric Card");

        initiateUI();
    }

    private void initiateUI(){
        recyclerViewColors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewColors.setAdapter(new ColorAdapter());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.MyViewHolder>{

        String[] colors = getResources().getStringArray(R.array.colors);

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(ActivityLyricCard.this).inflate(R.layout.item_color, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
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
                color = itemView.findViewById(R.id.colorView);
            }
        }
    }

    class imagesAdapter extends RecyclerView.Adapter<imagesAdapter.MyViewHolder>{

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        class MyViewHolder extends RecyclerView.ViewHolder{
            public MyViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
