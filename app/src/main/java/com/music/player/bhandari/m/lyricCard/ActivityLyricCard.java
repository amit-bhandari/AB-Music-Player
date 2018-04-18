package com.music.player.bhandari.m.lyricCard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.ColorHelper;
import com.music.player.bhandari.m.activity.ActivityPermissionSeek;
import com.music.player.bhandari.m.customViews.ZoomTextView;
import com.music.player.bhandari.m.model.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Amit AB AB on 17-Apr-18.
 *
 * I know, I know, there is not even bit of "Good Architecture" is followed in this class
 * Everything is mixed up, UI, data, card creation. Everything.
 * But its too late for this app to follow architectural guidelines.
 * You agree with me? Cheers.
 * If not. May god help you.
 *
 */

public class ActivityLyricCard extends AppCompatActivity implements View.OnTouchListener  {


    @BindView(R.id.rv_colors) RecyclerView recyclerViewColors;
    @BindView(R.id.rv_images) RecyclerView recyclerViewImages;
    @BindView(R.id.mainImageLyricCard) ImageView mainImage;
    @BindView(R.id.text_lyric) ZoomTextView lyricText;
    @BindView(R.id.text_artist) ZoomTextView artistText;
    @BindView(R.id.dragView) View dragView;

    float dx;
    float dy;

    private static final String fileName = "file.jpg";

    ImagesAdapter imagesAdapter = new ImagesAdapter();

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

        setContentView(R.layout.activity_lyric_card);
        ButterKnife.bind(this);

        if(getIntent().getExtras()==null){
            finish();
            return;
        }
        lyricText.setText(getIntent().getExtras().getString("lyric"));
        artistText.setText(getIntent().getExtras().getString("artist"));

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

        recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewImages.setAdapter(imagesAdapter);

        FirebaseDatabase.getInstance().getReference().child("cardLinks").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("ActivityLyricCard", "onDataChange: ");
                ArrayList<String> urls = new ArrayList<>();
                for(DataSnapshot snap : dataSnapshot.getChildren()){
                    urls.add(snap.getValue(String.class));
                }
                imagesAdapter.setUrls(urls);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        initiateDragView();

    }

    private void initiateDragView(){
        artistText.setOnTouchListener(this);
        lyricText.setOnTouchListener(this);
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
                }
                break;

            default: return false;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lyric_card, menu);
        return true;
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareCard(){
        mainImage.setDrawingCacheEnabled(true);
        Bitmap bitmap = mainImage.getDrawingCache();
        Canvas canvas = new Canvas(bitmap);
        TextPaint lyricPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        lyricPaint.setColor(lyricText.getCurrentTextColor());
        lyricPaint.setTextSize(lyricText.getTextSize());

        TextPaint artistPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        artistPaint.setColor(artistText.getCurrentTextColor());
        artistPaint.setTextSize(artistText.getTextSize());

        //give proper width here
        StaticLayout sl = new StaticLayout(lyricText.getText(), lyricPaint,
                300, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
        canvas.save();
        canvas.translate(lyricText.getX(), lyricText.getY());
        sl.draw(canvas);
        canvas.restore();
        //canvas.drawText(lyricText.getText().toString(), lyricText.getX(), lyricText.getY(), lyricPaint);
        //canvas.drawText(artistText.getText().toString(), artistText.getX(), artistText.getY(), artistPaint);

        File dir =new File(Environment.getExternalStorageDirectory().toString() + "/abmusic");
        dir.mkdirs();

        File lyricCardFile = new File(dir, fileName);
        if(lyricCardFile.exists()) lyricCardFile.delete();

        try{
            FileOutputStream stream = new FileOutputStream(lyricCardFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
        }catch (Exception e){
            Log.d("ActivityLyricCard", "shareCard: " + e.getLocalizedMessage());
        }

        Toast.makeText(this, "Created lyric card ", Toast.LENGTH_SHORT).show();
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
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lyricText.setTextColor(Color.parseColor(colors[getLayoutPosition()]));
                    }
                });
                color = itemView.findViewById(R.id.colorView);
            }
        }
    }

    class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.MyViewHolder>{

        private List<String> urls = new ArrayList<>();

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(ActivityLyricCard.this).inflate(R.layout.item_image_lyric_card, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Glide.with(ActivityLyricCard.this).load(urls.get(position)).into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        void setUrls(ArrayList<String> urls){
            this.urls = urls;
            setMainImage(urls.get(0));
            notifyDataSetChanged();
        }

        private void setMainImage(String url) {
            Glide.with(ActivityLyricCard.this).load(url).into(mainImage);
        }

        class MyViewHolder extends RecyclerView.ViewHolder{
            ImageView imageView;
            public MyViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setMainImage(urls.get(getLayoutPosition()));
                    }
                });
                imageView = itemView.findViewById(R.id.image_lyric_card);
            }
        }
    }
}
