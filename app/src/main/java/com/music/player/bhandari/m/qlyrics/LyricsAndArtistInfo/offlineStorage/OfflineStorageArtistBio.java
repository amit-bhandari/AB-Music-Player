package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.model.dataItem;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.ArtistInfo.ArtistInfo;
import com.music.player.bhandari.m.MyApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.concurrent.Executors;

/**
 * Created by Amit Bhandari on 3/29/2017.
 */

public class OfflineStorageArtistBio {
    public static ArtistInfo getArtistBioFromDB(TrackItem item){
        if(item==null){
            return null;
        }
        ArtistInfo artistInfo = null;

        try {
            DbHelperArtistBio dbHelperArtistBio = new DbHelperArtistBio(MyApp.getContext());
            SQLiteDatabase db = dbHelperArtistBio.getReadableDatabase();
            dbHelperArtistBio.onCreate(db);

            String where = DbHelperArtistBio.ARTIST_ID + " = " + item.getArtist_id()
                    + " OR " + DbHelperArtistBio.KEY_ARTIST + "= '" + item.getArtist().replace("'","''") +"'" ;;

            Cursor cursor = db.query(DbHelperArtistBio.TABLE_NAME,new String[]{DbHelperArtistBio.ARTIST_BIO}
                    ,where,null,null,null,null,"1");

            if(cursor!=null && cursor.getCount()!=0){
                cursor.moveToFirst();
                //retrieve and fill lyrics object
                Gson gson = new Gson();
                artistInfo = gson.fromJson(cursor.getString
                        (cursor.getColumnIndex(DbHelperArtistBio.ARTIST_BIO)),ArtistInfo.class);
                cursor.close();
            }

        }catch (Exception e){
            return null;
        }

        return artistInfo;
    }

    public static void putArtistBioFromDB(ArtistInfo artistInfo, TrackItem item){
        if(item==null || artistInfo==null){
            return;
        }

        try {
            DbHelperArtistBio dbHelperArtistBio = new DbHelperArtistBio(MyApp.getContext());
            SQLiteDatabase db = dbHelperArtistBio.getWritableDatabase();
            dbHelperArtistBio.onCreate(db);

            //check if already exists, if yes, return
            String where = DbHelperArtistBio.ARTIST_ID + " = " + item.getArtist_id()
                    + " OR " + DbHelperArtistBio.KEY_ARTIST + "= '" + item.getArtist().replace("'","''") +"'" ;;

            Cursor cursor = db.query(DbHelperArtistBio.TABLE_NAME,new String[]{DbHelperArtistBio.KEY_ARTIST}
                    ,where,null,null,null,null,"1");
            if(cursor!=null && cursor.getCount()>0){
                cursor.close();
                return;
            }

            //convert lyrics to json
            Gson gson = new Gson();
            String jsonInString = gson.toJson(artistInfo);

            ContentValues c = new ContentValues();
            c.put(DbHelperArtistBio.ARTIST_BIO, jsonInString);
            c.put(DbHelperArtistBio.KEY_ARTIST, item.getArtist());
            c.put(DbHelperArtistBio.ARTIST_ID, item.getArtist_id());
            db.insert(DbHelperArtistBio.TABLE_NAME, null, c);
        }catch (Exception ignored){

        }
    }

    public static void putArtistInfoToCache(final ArtistInfo artistInfo){
        //don't care about exception.
        //
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String CACHE_ART_INFO = MyApp.getContext().getCacheDir()+"/artistInfo/";
                    String actual_file_path = CACHE_ART_INFO+artistInfo.getCorrectedArtist();

                    if(new File(actual_file_path).exists()){
                        return;
                    }

                    File f = new File(CACHE_ART_INFO);
                    if(!f.exists()){
                        f.mkdir();
                    }
                    ObjectOutput out;
                    out = new ObjectOutputStream(new FileOutputStream(actual_file_path));
                    out.writeObject(artistInfo);
                    out.close();
                    Log.v("amit", "saved artist to cache");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static ArtistInfo getArtistInfoFromCache(String artist){
        String CACHE_ART_INFO = MyApp.getContext().getCacheDir()+"/artistInfo/";
        String actual_file_path = CACHE_ART_INFO+artist;
        ObjectInputStream in;
        ArtistInfo artistInfo = null;
        try {
            FileInputStream fileIn = new FileInputStream(actual_file_path);
            in = new ObjectInputStream(fileIn);
            artistInfo = (ArtistInfo) in.readObject();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(artistInfo!=null){
            Log.v("amit", "got from cache"+artistInfo.getOriginalArtist());
        }
        return artistInfo;
    }

    public static HashMap<String, String> getArtistImageUrls(){
        HashMap<String, String> map = new HashMap<>();
        try{
            ArrayList<dataItem> artistItems = new ArrayList<>(MusicLibrary.getInstance().getDataItemsArtist());
            for (dataItem item: artistItems){
                TrackItem trackItem = new TrackItem();
                trackItem.setArtist(item.artist_name);
                trackItem.setArtist_id(item.artist_id);

                ArtistInfo artistInfo = OfflineStorageArtistBio.getArtistBioFromDB(trackItem);
                if(artistInfo!=null){
                    map.put(artistInfo.getOriginalArtist(), artistInfo.getImageUrl());
                }
            }
        }catch (ConcurrentModificationException e){
            return map;
        }
        return map;
    }
}
