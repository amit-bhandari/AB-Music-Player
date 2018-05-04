package com.music.player.bhandari.m.activity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageArtistBio;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.tasks.DownloadArtInfoThread;
import com.music.player.bhandari.m.interfaces.DoubleClickListener;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.utils.UtilityFun;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Amit AB Bhandari on 3/10/2017.
 */

public class FragmentArtistInfo extends Fragment implements ArtistInfo.Callback {
    private View layout;
    private BroadcastReceiver mArtistUpdateReceiver;
    private ArtistInfo mArtistInfo;
    @BindView(R.id.text_view_art_bio_frag) TextView artBioText;
    @BindView(R.id.retry_text_view) TextView retryText;
    @BindView(R.id.update_track_metadata) TextView updateTagsText;

    @BindView(R.id.loading_lyrics_animation) AVLoadingIndicatorView lyricLoadAnimation;
    @BindView(R.id.track_artist_artsi_bio_frag) EditText  artistEdit;
    @BindView(R.id.button_update_metadata)  Button buttonUpdateMetadata;

    @BindView(R.id.ad_view_wrapper) View adViewWrapper;
    @BindView(R.id.adView)  AdView mAdView;
    @BindView(R.id.ad_close)  TextView adCloseText;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        Log.v("frag",isVisibleToUser+"");
        if(isVisibleToUser && mAdView!=null){
            mAdView.resume();
        }else {
            if(mAdView!=null){
                mAdView.pause();
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_artist_info, container, false);

        ButterKnife.bind(this, layout);

        buttonUpdateMetadata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TrackItem item = MyApp.getService().getCurrentTrack();
                if(item==null){
                    return;
                }

                String edited_artist = artistEdit.getText().toString().trim();

                if(edited_artist.isEmpty()){
                    Toast.makeText(getContext(),getString(R.string.te_error_empty_field), Toast.LENGTH_SHORT).show();
                    return;
                }


                if(!edited_artist.equals(item.getArtist()) ){

                    //changes made, save those
                    Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Audio.Media.ARTIST, edited_artist);
                    getContext().getContentResolver()
                            .update(uri, values, MediaStore.Audio.Media.TITLE +"=?", new String[] {item.getTitle()});

                    Intent intent = new Intent(getContext(), ActivityNowPlaying.class);
                    intent.putExtra("refresh", true);
                    intent.putExtra("position", MyApp.getService().getCurrentTrackPosition());
                    intent.putExtra("originalTitle",item.getTitle());
                    intent.putExtra("title", item.getTitle());
                    intent.putExtra("artist", edited_artist);
                    intent.putExtra("album", item.getAlbum());
                    startActivity(intent);

                    artistEdit.setVisibility(View.GONE);
                    updateTagsText.setVisibility(View.GONE);
                    buttonUpdateMetadata.setVisibility(View.GONE);
                    buttonUpdateMetadata.setClickable(false);


                    if(getActivity()!=null) {
                        View view = getActivity().getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity()
                                    .getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                        }
                    }

                    downloadArtInfo();

                }else {
                    Toast.makeText(getContext(),getString(R.string.change_tags_to_update),Toast.LENGTH_SHORT).show();
                }
            }
        });

        //retry click listner
        layout.findViewById(R.id.ll_art_bio).setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(retryText.getVisibility()==View.VISIBLE) {
                    retryText.setVisibility(View.GONE);
                    artBioText.setVisibility(View.VISIBLE);
                    artistEdit.setVisibility(View.GONE);
                    updateTagsText.setVisibility(View.GONE);
                    buttonUpdateMetadata.setVisibility(View.GONE);
                    buttonUpdateMetadata.setClickable(false);
                    lyricLoadAnimation.setVisibility(View.GONE);
                    downloadArtInfo();
                }
            }

            @Override
            public void onDoubleClick(View v) {

                //if no connection text, do not hide artist content
                if(retryText.getText().toString().equals(getString(R.string.no_connection))){
                    return;
                }

                if(artBioText.getVisibility()==View.VISIBLE){
                    artBioText.setVisibility(View.GONE);
                }else {
                    artBioText.setVisibility(View.VISIBLE);
                }
            }
        });

        //downloadArtInfo();
        mArtistUpdateReceiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //already displayed, skip
                updateArtistInfoIfNeeded();
            }
        };


        //noinspection PointlessBooleanExpression
        /*if(!UtilityFun.isAdsRemoved()) {
            MobileAds.initialize(getContext(), getString(R.string.banner_art_info_frag));
            mAdView = layout.findViewById(R.id.adView);
            if (UtilityFun.isConnectedToInternet()) {
                AdRequest adRequest = new AdRequest.Builder()//.addTestDevice("C6CC5AB32A15AF9EFB67D507C151F23E")
                        .build();
                if (mAdView != null) {
                    mAdView.loadAd(adRequest);
                    mAdView.setVisibility(View.VISIBLE);
                    adViewWrapper.setVisibility(View.VISIBLE);
                    adCloseText.setVisibility(View.VISIBLE);
                    //if fragment is invisible, pause the ad
                    if(!getUserVisibleHint()){
                        mAdView.pause();
                    }
                }
            } else {
                if (mAdView != null) {
                    mAdView.setVisibility(View.GONE);
                    adViewWrapper.setVisibility(View.GONE);
                }
            }
        }*/

        return layout;
    }

    @OnClick(R.id.ad_close)
    public void close_ad(){
        if(mAdView!=null){
            mAdView.destroy();
        }
        adViewWrapper.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    private void downloadArtInfo(){
        TrackItem item = MyApp.getService().getCurrentTrack();
        if(item==null || item.getArtist()==null){
            return;
        }

        artBioText.setText(getString(R.string.artist_info_loading));

        //set loading animation
        lyricLoadAnimation.setVisibility(View.VISIBLE);
        lyricLoadAnimation.show();

        //see in offlinne db first
        mArtistInfo = OfflineStorageArtistBio.getArtistBioFromTrackItem(item);
        //second check is added to make sure internet call will happen
        //when user manually changes artist tag
        if(mArtistInfo!=null && item.getArtist().trim().equals(mArtistInfo.getOriginalArtist().trim())){
            onArtInfoDownloaded(mArtistInfo);
            return;
        }

        if (UtilityFun.isConnectedToInternet()) {

            String artist = item.getArtist();
            artist = UtilityFun.filterArtistString(artist);

            new DownloadArtInfoThread(this, artist , item).start();
        } else {
            artBioText.setVisibility(View.GONE);
            retryText.setText(getString(R.string.no_connection));
            retryText.setVisibility(View.VISIBLE);
            lyricLoadAnimation.hide();
            lyricLoadAnimation.setVisibility(View.GONE);
        }

    }

    private void playClicked(){
        if (getActivity() == null) {
            return;
        }



        MyApp.getService().play();
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Constants.ACTION.PLAY_PAUSE_UI_UPDATE));
        //animateDiscView();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mArtistUpdateReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateArtistInfoIfNeeded();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mArtistUpdateReceiver
                ,new IntentFilter(Constants.ACTION.UPDATE_LYRIC_AND_INFO));
    }

    private void updateArtistInfoIfNeeded() {
        TrackItem item = MyApp.getService().getCurrentTrack();
        if(item==null){
            artBioText.setVisibility(View.GONE);
            retryText.setText(getString(R.string.no_music_found));
            //retryText.setVisibility(View.GONE);
            retryText.setVisibility(View.VISIBLE);
            lyricLoadAnimation.hide();
            return;
        }
        if(mArtistInfo!=null && mArtistInfo.getOriginalArtist()
                .equals(item.getArtist())){
            return;
        }

        //set loading  text and animation
        //set loading  text and animation

        downloadArtInfo();
    }

    @Override
    public void onArtInfoDownloaded(ArtistInfo artistInfo) {

        mArtistInfo = artistInfo;
        if(artistInfo==null || getActivity()==null || !isAdded()){
            return;
        }
        TrackItem item = MyApp.getService().getCurrentTrack();
        //if song is already changed , return
        if(item!=null && !item.getArtist().trim().equals(artistInfo.getOriginalArtist().trim())){
            //artBioText.setText(getString(R.string.artist_info_loading));
            return;
        }
        //hide loading animation
        lyricLoadAnimation.hide();
        lyricLoadAnimation.setVisibility(View.GONE);

        if(artistInfo.getArtistContent()==null){
            retryText.setText(getString(R.string.artist_info_no_result));
            retryText.setVisibility(View.VISIBLE);
            artBioText.setVisibility(View.GONE);
            TrackItem tempItem = MyApp.getService().getCurrentTrack();
            if(tempItem!=null) {
                artistEdit.setVisibility(View.VISIBLE);
                updateTagsText.setVisibility(View.VISIBLE);
                buttonUpdateMetadata.setVisibility(View.VISIBLE);
                buttonUpdateMetadata.setClickable(true);
                artistEdit.setText(tempItem.getArtist());
            }
            return;
        }

        if(layout!=null && getActivity()!=null && artistInfo.getArtistContent()!=null){
            String content = artistInfo.getArtistContent();
            int index = content.indexOf("Read more");
            SpannableString ss = new SpannableString(content);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    if(mArtistInfo.getArtistUrl()==null){
                        Toast.makeText(getContext(),getString(R.string.error_invalid_url),Toast.LENGTH_SHORT).show();
                    }else {
                        try {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mArtistInfo.getArtistUrl()));
                            startActivity(browserIntent);
                        }catch (ActivityNotFoundException e){
                            Toast.makeText(getContext(), "No supporting application found for opening the link.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(true);
                    ds.setTypeface(Typeface.create(ds.getTypeface(), Typeface.BOLD));
                }
            };
            if(index!=-1) {
                ss.setSpan(clickableSpan, index, index+9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }


            if(!content.equals("")) {
                artBioText.setVisibility(View.VISIBLE);
                retryText.setVisibility(View.GONE);

                artBioText.setText(ss);
                artBioText.setMovementMethod(LinkMovementMethod.getInstance());

                artistEdit.setVisibility(View.GONE);
                updateTagsText.setVisibility(View.GONE);
                buttonUpdateMetadata.setVisibility(View.GONE);
                buttonUpdateMetadata.setClickable(false);
                artistEdit.setText("");

            }else {
                artBioText.setVisibility(View.GONE);
                retryText.setText(getString(R.string.artist_info_no_result));
                retryText.setVisibility(View.VISIBLE);
                TrackItem tempItem = MyApp.getService().getCurrentTrack();
                if(tempItem!=null) {
                    artistEdit.setVisibility(View.VISIBLE);
                    updateTagsText.setVisibility(View.VISIBLE);
                    buttonUpdateMetadata.setVisibility(View.VISIBLE);
                    buttonUpdateMetadata.setClickable(true);
                    artistEdit.setText(tempItem.getArtist());
                }
            }

            //check current now playing background setting
            ///get current setting
            // 0 - System default   1 - artist image  2 - custom
            int currentNowPlayingBackPref = MyApp.getPref().getInt(getString(R.string.pref_now_playing_back),1);

            if(currentNowPlayingBackPref==1) {
                if (!((ActivityNowPlaying) getActivity()).isArtistLoadedInBack()) {
                    new SetBlurryImagetask().execute(artistInfo);
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SetBlurryImagetask extends AsyncTask<ArtistInfo, String, Bitmap>{

        Bitmap b ;

        @Override
        protected Bitmap doInBackground(ArtistInfo... params) {

            //store file in cache with artist id as name
            //create folder in cache for artist images
            String CACHE_ART_THUMBS = MyApp.getContext().getCacheDir()+"/art_thumbs/";
            String actual_file_path = CACHE_ART_THUMBS+params[0].getOriginalArtist();
            File f = new File(CACHE_ART_THUMBS);
            if(!f.exists()){
                f.mkdir();
            }
            if(!new File(actual_file_path).exists()){
                //create file
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(new File(actual_file_path));
                    URL url = new URL(params[0].getImageUrl());
                    InputStream inputStream = url.openConnection().getInputStream();
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0;
                    while ( (bufferLength = inputStream.read(buffer)) > 0 )
                    {
                        fos.write(buffer, 0, bufferLength);
                    }
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            b= BitmapFactory.decodeFile(actual_file_path);
            return b;
        }

        protected void onPostExecute(Bitmap b) {

            //set background image

            if(b!=null && getActivity()!=null) {
                ((ActivityNowPlaying) getActivity()).setBlurryBackground(b);
            }
        }
    }
}
