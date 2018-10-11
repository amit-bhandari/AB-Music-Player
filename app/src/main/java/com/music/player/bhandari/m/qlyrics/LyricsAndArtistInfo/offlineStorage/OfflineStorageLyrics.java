package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics;
import com.music.player.bhandari.m.MyApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

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

/**
 * Some things in this class are messed up because of instant lyrics and explore lyrics feature
 * Those were unplanned changes
 * But code works good without any issue
 * If it looks stupid but it works, it ain't stupid
 */
public class OfflineStorageLyrics {

    //look into db for lyrics, if not found, return null
    public static Lyrics getLyricsFromDB(TrackItem item){
        if(item==null){
            return null;
        }
        Lyrics lyrics = null;

        try {
            DbHelperLyrics dbHelperLyrics = new DbHelperLyrics(MyApp.getContext());
            SQLiteDatabase db = dbHelperLyrics.getReadableDatabase();
            dbHelperLyrics.onCreate(db);

            String where = DbHelperLyrics._ID + " = " + item.getId();


            Cursor cursor = db.query(DbHelperLyrics.TABLE_NAME, new String[]{DbHelperLyrics.LYRICS}
                    , where, null, null, null, null, "1");

            if (cursor != null && cursor.getCount() != 0) {
                cursor.moveToFirst();
                //retrieve and fill lyrics object
                Gson gson = new Gson();
                lyrics = gson.fromJson(cursor.getString(cursor.getColumnIndex(DbHelperLyrics.LYRICS)), Lyrics.class);
                lyrics.setTrackId(item.getId());
                cursor.close();
            }
        }catch (Exception e){
            return null;
        }

        return lyrics;
    }

    public static void putLyricsInDB(Lyrics lyrics, TrackItem item){
        if(item==null || lyrics==null){
            return;
        }

        try {
            DbHelperLyrics dbHelperLyrics = new DbHelperLyrics(MyApp.getContext());
            SQLiteDatabase db = dbHelperLyrics.getWritableDatabase();
            dbHelperLyrics.onCreate(db);

            //check if data already exists, if it does, return
            String where = DbHelperLyrics._ID + " = " + item.getId()
                    + " OR " + DbHelperLyrics.KEY_TITLE + "= '" + item.getTitle().replace("'", "''") + "'";


            Cursor cursor = db.query(DbHelperLyrics.TABLE_NAME, new String[]{DbHelperLyrics.KEY_TITLE}
                    , where, null, null, null, null, "1");
            if(cursor!=null && cursor.getCount()>0){
                cursor.close();
                return;
            }

            //convert lyrics to json
            Gson gson = new Gson();
            String jsonInString = gson.toJson(lyrics);

            ContentValues c = new ContentValues();
            c.put(DbHelperLyrics.LYRICS, jsonInString);
            c.put(DbHelperLyrics.KEY_TITLE, item.getTitle());
            c.put(DbHelperLyrics._ID, item.getId());
            db.insert(DbHelperLyrics.TABLE_NAME, null, c);
        }catch (Exception ignored){}

    }

    //clear lyrics based on id (used for internal lyrics of AB Music offline tracks)
    public static boolean clearLyricsFromDB(TrackItem item){
        if(item==null){
            return false;
        }

        try {
            DbHelperLyrics dbHelperLyrics = new DbHelperLyrics(MyApp.getContext());
            SQLiteDatabase db = dbHelperLyrics.getReadableDatabase();
            dbHelperLyrics.onCreate(db);

            String where = DbHelperLyrics._ID + " = " + item.getId();

            int i = db.delete(DbHelperLyrics.TABLE_NAME,where,null);

            return i >= 1;
        }catch (Exception e){
            return false;
        }

    }

    public static boolean isLyricsPresentInDB(int id){
        try {
            DbHelperLyrics dbHelperLyrics = new DbHelperLyrics(MyApp.getContext());
            SQLiteDatabase db = dbHelperLyrics.getReadableDatabase();
            dbHelperLyrics.onCreate(db);

            String where = DbHelperLyrics._ID + " = " + id;

            Cursor cursor = db.query(DbHelperLyrics.TABLE_NAME, new String[]{DbHelperLyrics.LYRICS}
                    , where, null, null, null, null, "1");

            if (cursor != null && cursor.getCount() != 0) {
                cursor.close();
                return true;
            }else {
                return false;
            }

        }catch (Exception e){

            return false;
        }
    }

    //methods for storing and retrieving instant lyrics
    //unlike lyrics from AB Music, track item will not have id
    public static Lyrics getInstantLyricsFromDB(TrackItem item){
        if(item==null){
            return null;
        }
        Lyrics lyrics = null;

        try {
            DbHelperLyrics dbHelperLyrics = new DbHelperLyrics(MyApp.getContext());
            SQLiteDatabase db = dbHelperLyrics.getReadableDatabase();
            dbHelperLyrics.onCreate(db);

            String where = DbHelperLyrics._ID + " = " + item.getId() + " AND " + DbHelperLyrics.KEY_TITLE
                    + " = '" + item.getTitle().replace("'", "''") +"'";


            Cursor cursor = db.query(DbHelperLyrics.TABLE_NAME, new String[]{DbHelperLyrics.LYRICS}
                    , where, null, null, null, null, "1");


            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                //retrieve and fill lyrics object
                Gson gson = new Gson();
                lyrics = gson.fromJson(cursor.getString(cursor.getColumnIndex(DbHelperLyrics.LYRICS)), Lyrics.class);
                lyrics.setTrackId(item.getId());
                cursor.close();
            }
        }catch (Exception e){
            return null;
        }

        return lyrics;
    }

    public static boolean putInstantLyricsInDB(Lyrics lyrics, TrackItem item){
        if(item==null || lyrics==null){
            return false;
        }

        try {
            DbHelperLyrics dbHelperLyrics = new DbHelperLyrics(MyApp.getContext());
            SQLiteDatabase db = dbHelperLyrics.getWritableDatabase();
            dbHelperLyrics.onCreate(db);

            //check if data already exists, if it does, return
            String where = DbHelperLyrics._ID + " = " + item.getId()
                    + " AND " + DbHelperLyrics.KEY_TITLE + "= '" + item.getTitle().replace("'", "''") + "'";


            Cursor cursor = db.query(DbHelperLyrics.TABLE_NAME, new String[]{DbHelperLyrics.KEY_TITLE}
                    , where, null, null, null, null, "1");
            if(cursor!=null && cursor.getCount()>0){
                cursor.close();
                return true;
            }

            //convert lyrics to json
            Gson gson = new Gson();
            String jsonInString = gson.toJson(lyrics);

            ContentValues c = new ContentValues();
            c.put(DbHelperLyrics.LYRICS, jsonInString);
            c.put(DbHelperLyrics.KEY_TITLE, item.getTitle());
            c.put(DbHelperLyrics._ID, item.getId());
            db.insert(DbHelperLyrics.TABLE_NAME, null, c);

            return true;

        }catch (Exception ignored){return false;}

    }

    /**
     * check if lyrics present in db for given track title and id
     * this method is used in lyric view and instant lyric screen to determine save or delete action for fab
     * @param track
     * @param id
     * @return
     */
    public static boolean isLyricsPresentInDB(String track, int id){
        Log.d("OfflineStorage", "isLyricsPresentInDB: " + track + " " + id );
        try {
            DbHelperLyrics dbHelperLyrics = new DbHelperLyrics(MyApp.getContext());
            SQLiteDatabase db = dbHelperLyrics.getReadableDatabase();
            dbHelperLyrics.onCreate(db);

            String where = DbHelperLyrics.KEY_TITLE + " = '" + track.replace("'", "''") +"'  AND "
                    + DbHelperLyrics._ID + " = " + id;

            Cursor cursor = db.query(DbHelperLyrics.TABLE_NAME, new String[]{DbHelperLyrics.LYRICS}
                    , where, null, null, null, null, "1");

            if (cursor != null && cursor.getCount() != 0) {
                cursor.close();
                return true;
            }else {
                return false;
            }

        }catch (Exception e){

            return false;
        }
    }

    //clear lyrics based on track title and id =- 1,used in instant lyrics screen
    public static boolean clearLyricsFromDB(String track){

        try {
            DbHelperLyrics dbHelperLyrics = new DbHelperLyrics(MyApp.getContext());
            SQLiteDatabase db = dbHelperLyrics.getReadableDatabase();
            dbHelperLyrics.onCreate(db);

            String where = DbHelperLyrics.KEY_TITLE + " = '" + track.replace("'", "''") +"'  AND "
                    + DbHelperLyrics._ID + " = -1";

            int i = db.delete(DbHelperLyrics.TABLE_NAME,where,null);

            return i >= 1;
        }catch (Exception e){
            return false;
        }

    }

    //clear lyrics given track title and track id (used from saved lyrics screen)
    //id == -1 in case lyrics is saved from tracks other than AB Music offline tracks
    public static boolean clearLyricsFromDB(String track, int id){

        try {
            DbHelperLyrics dbHelperLyrics = new DbHelperLyrics(MyApp.getContext());
            SQLiteDatabase db = dbHelperLyrics.getReadableDatabase();
            dbHelperLyrics.onCreate(db);

            String where = DbHelperLyrics.KEY_TITLE + " = '" + track.replace("'", "''") +"'  AND "
                    + DbHelperLyrics._ID + " = " + id;

            int i = db.delete(DbHelperLyrics.TABLE_NAME,where,null);

            return i >= 1;
        }catch (Exception e){
            return false;
        }

    }

    //temporary cache for instant lyrics and explore lyrics screens for avoiding repetitive lyric network calls
    //
    public static void putLyricsToCache(final Lyrics lyrics){

        //don't care about exception.
        //
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String CACHE_ART_LYRICS = MyApp.getContext().getCacheDir()+"/lyrics/";
                    String actual_file_path = CACHE_ART_LYRICS+lyrics.getOriginalTrack()+lyrics.getOriginalArtist();

                    if(new File(actual_file_path).exists()){
                        return;
                    }

                    File f = new File(CACHE_ART_LYRICS);
                    if(!f.exists()){
                        f.mkdir();
                    }
                    ObjectOutput out;
                    out = new ObjectOutputStream(new FileOutputStream(actual_file_path));
                    out.writeObject(lyrics);
                    out.close();
                    Log.v("Amit AB", "saved lyrics to cache");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public static void clearLyricsFromCache(Lyrics lyrics){
        try {
            String CACHE_ART_LYRICS = MyApp.getContext().getCacheDir()+"/lyrics/";
            String actual_file_path = CACHE_ART_LYRICS+lyrics.getOriginalTrack()+lyrics.getOriginalArtist();

            File lyricFile = new File(actual_file_path);
            if(lyricFile.exists()){
                lyricFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Lyrics getLyricsFromCache(TrackItem item){
        String CACHE_ART_LYRICS = MyApp.getContext().getCacheDir()+"/lyrics/";
        String actual_file_path = CACHE_ART_LYRICS+item.getTitle()+item.getArtist();
        ObjectInputStream in;
        Lyrics lyrics = null;
        try {
            FileInputStream fileIn = new FileInputStream(actual_file_path);
            in = new ObjectInputStream(fileIn);
            lyrics = (Lyrics) in.readObject();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(lyrics!=null){
            Log.v("Amit AB", "got from cache"+lyrics.getOriginalTrack());
        }
        return lyrics;
    }

    //get all saved lyrics from db
    public static List<Lyrics> getAllSavedLyrics(){
        List<Lyrics> lyrics = new  ArrayList<>();

        try {
            DbHelperLyrics dbHelperLyrics = new DbHelperLyrics(MyApp.getContext());
            SQLiteDatabase db = dbHelperLyrics.getReadableDatabase();
            dbHelperLyrics.onCreate(db);

            //String where = DbHelperLyrics._ID + " = " + item.getId();


            Cursor cursor = db.query(DbHelperLyrics.TABLE_NAME, null, null, null, null, null, null, null);

            if (cursor != null && cursor.getCount() != 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Gson gson = new Gson();
                    int id = cursor.getInt(cursor.getColumnIndex(DbHelperLyrics._ID));
                    Lyrics lyric = gson.fromJson(cursor.getString(cursor.getColumnIndex(DbHelperLyrics.LYRICS)), Lyrics.class);
                    if(lyric!=null) {
                        lyric.setTrackId(id);
                        lyrics.add(lyric);
                        Log.d("OfflineStorage", "getAllSavedLyrics: " + lyric.getTrack() + " : " + lyric.getTrackId());
                    }
                }
                cursor.close();
            }
        }catch (Exception e){
            return lyrics;
        }

        return lyrics;
    }
}
