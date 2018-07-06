package com.music.player.bhandari.m.model;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.music.player.bhandari.m.DBHelper.DbHelperEqualizer;
import com.music.player.bhandari.m.DBHelper.DbHelperListOfPlaylist;
import com.music.player.bhandari.m.DBHelper.DbHelperUserMusicData;
import com.music.player.bhandari.m.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
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


public class PlaylistManager {

    /**
     * Attention
     * One of the ugliest piece of code I have ever written in my life resides in this file.
     * Please forgive me for this shit I have given to this world.
     *
     * Only way to make this code good is to rewrite it.
     *
     * In my defence, this is one of the very first coding project I have done in my life!
     */

    private Context context;
    private static PlaylistManager playlistManager;
    private static DbHelperUserMusicData dbHelperUserMusicData;
    private static DbHelperListOfPlaylist dbHelperListOfPlaylist;

    private static HashMap<String, Integer> trackCount;
    private static HashMap<String,Long> listOfPlaylists = new HashMap<>();   //cache playlist list to avoid unnecessary db calls

    private PlaylistManager(Context context){
        this.context=context;
        dbHelperUserMusicData = new DbHelperUserMusicData(context);
        dbHelperListOfPlaylist  = new DbHelperListOfPlaylist(context);
        GetPlaylistList(false);
    }

    public static PlaylistManager getInstance(Context context){
        if (playlistManager==null){
            playlistManager = new PlaylistManager(context);
        }
        return playlistManager;
    }

    void PopulateUserMusicTable(){
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.v(Constants.TAG,"populating");
                    //DbHelperUserMusicData dbHelperUserMusicData = new DbHelperUserMusicData(context);
                    SQLiteDatabase db = dbHelperUserMusicData.getWritableDatabase();
                    Cursor cur;
                    ArrayList<dataItem> dataItems = MusicLibrary.getInstance().getDataItemsForTracks();
                    for (dataItem item : dataItems) {
                        //check if song already contains in db, if no, add it
                        String where = DbHelperUserMusicData.KEY_ID + " = "
                                + item.id;
                        cur = db.query(DbHelperUserMusicData.TABLE_NAME
                                , new String[]{DbHelperUserMusicData.KEY_ID}
                                , where, null, null, null, null, null);
                        if (cur.getCount() == 0) {
                            ContentValues c = new ContentValues();
                            c.put(DbHelperUserMusicData.KEY_TITLE, item.title);
                            c.put(DbHelperUserMusicData.KEY_ID, item.id);
                            c.put(DbHelperUserMusicData.KEY_COUNT, 0);
                            c.put(DbHelperUserMusicData.KEY_FAV, 0);
                            c.put(DbHelperUserMusicData.KEY_TIME_STAMP, 0);
                            c.put(DbHelperUserMusicData.KEY_LAST_PLAYING_QUEUE, 0);
                            db.insert(DbHelperUserMusicData.TABLE_NAME, null, c);
                        }
                        cur.close();
                    }
                }catch (Exception e){
                    //igore any exception
                    //concurrent modification exception
                }
            }
        });
    }

    public void addEntryToMusicTable(dataItem d){
        try {
            SQLiteDatabase db = dbHelperUserMusicData.getWritableDatabase();
            Cursor cur;
            String where = DbHelperUserMusicData.KEY_TITLE + "= '"
                    + d.title.replace("'", "''") + "'";
            cur = db.query(DbHelperUserMusicData.TABLE_NAME
                    , new String[]{DbHelperUserMusicData.KEY_TITLE}
                    , where, null, null, null, null, null);
            if (cur.getCount() == 0) {
                ContentValues c = new ContentValues();
                c.put(DbHelperUserMusicData.KEY_TITLE, d.title);
                c.put(DbHelperUserMusicData.KEY_ID, d.id);
                c.put(DbHelperUserMusicData.KEY_COUNT, 0);
                c.put(DbHelperUserMusicData.KEY_FAV, 0);
                c.put(DbHelperUserMusicData.KEY_TIME_STAMP, 0);
                c.put(DbHelperUserMusicData.KEY_LAST_PLAYING_QUEUE, 0);
                db.insert(DbHelperUserMusicData.TABLE_NAME, null, c);
            }
            cur.close();
        }catch (Exception e){
            //igore any exception
            //concurrent modification exception
        }
    }

    /*
    user_addable = only playlist in which user can add songs
     */

    public ArrayList<String> GetPlaylistList(boolean userAddable){
        if(listOfPlaylists.size()==0){
            SQLiteDatabase db = dbHelperListOfPlaylist.getReadableDatabase();
            dbHelperListOfPlaylist.onCreate(db);
            Cursor cursor = db.query(Constants.SYSTEM_PLAYLISTS.PLAYLIST_LIST, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                String s = cursor.getString(0).replace("_", " ");
                listOfPlaylists.put(s, getTrackCount(s));
            }
            cursor.close();
        }
        Log.d("PlaylistManager", "GetPlaylistList: " + listOfPlaylists.keySet());

        ArrayList<String> temp = new ArrayList<>();
        for (String name : listOfPlaylists.keySet()) {
            if (userAddable) {
                if(!name.replace(" ","_").equals(Constants.SYSTEM_PLAYLISTS.MOST_PLAYED)
                        && !name.replace(" ","_").equals(Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED)
                        && !name.replace(" ","_").equals(Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED))
                    temp.add(name);
            } else {
                temp.add(name);
            }
        }

        return temp;
    }

    public boolean CreatePlaylist(String playlist_name){

        playlist_name = playlist_name.replace(" ","_");
        //try adding column
        //DbHelperUserMusicData dbHelperUserMusicData = new DbHelperUserMusicData(context);
        SQLiteDatabase db = dbHelperUserMusicData.getWritableDatabase();
        dbHelperUserMusicData.onCreate(db);
        //create column for newly created playlist
        String insertQuery = "ALTER TABLE " + DbHelperUserMusicData.TABLE_NAME + " ADD COLUMN "
                + playlist_name + " INTEGER DEFAULT 0";
        try {
            db.execSQL(insertQuery);
        }catch (Exception ignored){

        }
        //try creating entry in playlist list
        //DbHelperListOfPlaylist dbHelperListOfPlaylist = new DbHelperListOfPlaylist(context);
        db = dbHelperListOfPlaylist.getWritableDatabase();
        dbHelperListOfPlaylist.onCreate(db);

        String where = DbHelperListOfPlaylist.KEY_TITLE + "= '" + playlist_name.replace("'", "''") + "'";
        if(db.query(Constants.SYSTEM_PLAYLISTS.PLAYLIST_LIST, new String[]{DbHelperListOfPlaylist.KEY_TITLE}
                ,where, null, null, null, null).getCount()==0){
            ContentValues c = new ContentValues();
            c.put(DbHelperListOfPlaylist.KEY_TITLE, playlist_name);
            db.insert(Constants.SYSTEM_PLAYLISTS.PLAYLIST_LIST, null, c);

            //invalidate playlist cache @todo can be better I guess
            listOfPlaylists.clear(); //it will be populated automatically on next db call
            return true;
        }
        return false;
    }

    public boolean DeletePlaylist(String playlist_name){
        playlist_name = playlist_name.replace(" ","_");
        //delete column is not possible in sqlite
        //just a remove playlist entry from playlist list
        //and clear the column
        if(playlist_name.equals(Constants.SYSTEM_PLAYLISTS.MOST_PLAYED)
                || playlist_name.equals(Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED)
                || playlist_name.equals(Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED)
                || playlist_name.equals(Constants.SYSTEM_PLAYLISTS.MY_FAV)){
            return false;
        }


        //DbHelperUserMusicData dbHelperUserMusicData = new DbHelperUserMusicData(context);
        SQLiteDatabase db = dbHelperUserMusicData.getWritableDatabase();
        dbHelperUserMusicData.onCreate(db);
        ContentValues c = new ContentValues();
        c.put(playlist_name, 0);
        db.update(DbHelperUserMusicData.TABLE_NAME, c, null, null);

        //DbHelperListOfPlaylist dbHelperListOfPlaylist
        //      = new DbHelperListOfPlaylist(context);
        db = dbHelperListOfPlaylist.getWritableDatabase();
        dbHelperListOfPlaylist.onCreate(db);

        //invalidate playlist cache
        listOfPlaylists.clear(); //it will be populated automatically on next db call


        return db.delete(Constants.SYSTEM_PLAYLISTS.PLAYLIST_LIST
                , DbHelperListOfPlaylist.KEY_TITLE + "= '" + playlist_name.replace("'", "''") + "'"
                , null) != 0;
    }

    public  void AddSongToPlaylist(final String playlist_name_arg, final int[] song_ids){


        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final String playlist_name =  playlist_name_arg.replace(" ","_");
                final Handler hand = new Handler(Looper.getMainLooper());
                SQLiteDatabase db = dbHelperUserMusicData.getWritableDatabase();
                dbHelperUserMusicData.onCreate(db);

                //check if song exists
                if(song_ids.length==1) {
                    String where = DbHelperUserMusicData.KEY_ID + "= '" +song_ids[0] + "'"
                            + " AND \"" + playlist_name + "\" != 0" ;
                    if(db.query(DbHelperUserMusicData.TABLE_NAME
                            , new String[]{"\"" + playlist_name + "\""}, where, null, null, null, null)
                            .getCount()>0){
                        hand.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, context.getString(R.string.song_already_exists_in) + playlist_name, Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                }

                String max = "MAX(" + playlist_name + ")";
                Cursor cursor = db.query(DbHelperUserMusicData.TABLE_NAME, new String [] {max}, null, null, null, null, null);
                cursor.moveToFirst();
                int maxValue = cursor.getInt(0);
                cursor.close();

                for (int id:song_ids) {
                    ContentValues c = new ContentValues();
                    c.put(playlist_name, ++maxValue);
                    db.update(DbHelperUserMusicData.TABLE_NAME,c,DbHelperUserMusicData.KEY_ID + "= ?", new String[] {id+""});
                }


                String favPlayListName = playlist_name.replace("_", " ");
                if(listOfPlaylists.containsKey(favPlayListName)){
                    listOfPlaylists.put(favPlayListName, listOfPlaylists.get(favPlayListName)+1);
                }

                hand.post(new Runnable() {
                    @SuppressLint("ShowToast")
                    @Override
                    public void run() {

                        if(playlist_name.equals(Constants.SYSTEM_PLAYLISTS.MY_FAV)){
                            return;
                        }

                        Toast toast;
                        toast = Toast.makeText(context, context.getString(R.string.songs_added_in) + playlist_name.replace("_", " "), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                });
            }
        });

    }

    public void addSongToFav(final int id){
        final String playlist_name =  DbHelperUserMusicData.KEY_FAV.replace(" ","_");
        SQLiteDatabase db = dbHelperUserMusicData.getWritableDatabase();
        dbHelperUserMusicData.onCreate(db);

        String max = "MAX(" + playlist_name + ")";
        Cursor cursor = db.query(DbHelperUserMusicData.TABLE_NAME, new String [] {max}, null, null, null, null, null);
        cursor.moveToFirst();
        int maxValue = cursor.getInt(0);
        cursor.close();

        ContentValues c = new ContentValues();
        c.put(playlist_name, ++maxValue);
        db.update(DbHelperUserMusicData.TABLE_NAME,c,DbHelperUserMusicData.KEY_ID + "= ?", new String[] {id+""});

        String favPlayListName = Constants.SYSTEM_PLAYLISTS.MY_FAV.replace("_", " ");
        if(listOfPlaylists.containsKey(favPlayListName)){
            listOfPlaylists.put(favPlayListName, listOfPlaylists.get(favPlayListName)+1);
        }
    }

    public void RemoveSongFromPlaylistNew(String playlist_name, int id){
        playlist_name = playlist_name.replace(" ","_");
        {
            //user playlist
            //DbHelperUserMusicData dbHelperMusicData
            //      = new DbHelperUserMusicData(context);
            SQLiteDatabase db = dbHelperUserMusicData.getReadableDatabase();
            dbHelperUserMusicData.onCreate(db);
            try {
                String where = DbHelperUserMusicData.KEY_ID + "='" + id + "'";
                ContentValues c = new ContentValues();
                c.put(playlist_name, 0);
                db.update(DbHelperUserMusicData.TABLE_NAME, c, where, null);

                if(listOfPlaylists.containsKey(playlist_name.replace("_", " "))){
                    listOfPlaylists.put(playlist_name.replace("_", " "),
                            listOfPlaylists.get(playlist_name.replace("_", " "))-1);
                }

                Toast.makeText(context,context.getString(R.string.removed_from_playlist),Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Toast.makeText(context,context.getString(R.string.error_removing),Toast.LENGTH_SHORT).show();
            }
        }
    }

    public ArrayList<dataItem> GetPlaylist(String playlist_name){
        Log.d("PlaylistManager", "GetPlaylist: " + playlist_name);
        playlist_name = playlist_name.replace(" ","_");
        ArrayList<dataItem> trackList;
        switch (playlist_name){
            case Constants.SYSTEM_PLAYLISTS.MOST_PLAYED:
                trackList = GetMostPlayed();
                break;

            case Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED:
                trackList=GetRecentlyPlayed();
                break;

            case Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED:
                trackList=GetRecentlyAdded();
                break;

            case Constants.SYSTEM_PLAYLISTS.MY_FAV:
                trackList = GetFav();
                break;

            default:
                trackList = GetUserPlaylist(playlist_name);
                break;
        }
        return trackList;
    }

    public void AddToRecentlyPlayedAndUpdateCount(final int _id){

        //thread for updating play numberOfTracks
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {


                //DbHelperUserMusicData dbHelperUserMusicData = new DbHelperUserMusicData(context);
                SQLiteDatabase db = dbHelperUserMusicData.getWritableDatabase();
                dbHelperUserMusicData.onCreate(db);
                try {
                    String getCurrentCountQuery = "SELECT " + DbHelperUserMusicData.KEY_COUNT
                            + " FROM " + DbHelperUserMusicData.TABLE_NAME + " WHERE "
                            + DbHelperUserMusicData.KEY_ID + " = '" + _id + "'";
                    Cursor getCurrentCountCursor = db.rawQuery(getCurrentCountQuery, null);
                    getCurrentCountCursor.moveToFirst();
                    int currentCount = getCurrentCountCursor.getInt
                            (getCurrentCountCursor.getColumnIndex(DbHelperUserMusicData.KEY_COUNT));
                    getCurrentCountCursor.close();

                    ContentValues c = new ContentValues();
                    c.put(DbHelperUserMusicData.KEY_COUNT, currentCount + 1);

                    db.update(DbHelperUserMusicData.TABLE_NAME, c, DbHelperUserMusicData.KEY_ID + "= ?", new String[]{_id+""});

                }
                catch (Exception e){
                    Log.v("Serious","Error" + e.getStackTrace().toString());
                }
            }
        });

        String recentPlayListName = Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED.replace("_", " ");
        if(listOfPlaylists.containsKey(recentPlayListName)){
            listOfPlaylists.put(recentPlayListName, listOfPlaylists.get(recentPlayListName)+1);
        }

        //thread for adding entry in recently played
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                //DbHelperUserMusicData dbHelperUserMusicData = new DbHelperUserMusicData(context);
                SQLiteDatabase db = dbHelperUserMusicData.getWritableDatabase();
                dbHelperUserMusicData.onCreate(db);

                ContentValues c = new ContentValues();
                c.put(DbHelperUserMusicData.KEY_TIME_STAMP, System.currentTimeMillis());
                db.update(DbHelperUserMusicData.TABLE_NAME,c,DbHelperUserMusicData.KEY_ID + "= ?", new String[] {_id+""});

            }
        });
    }

    public boolean isFavNew(int id){

        boolean returnValue=false;

        //DbHelperUserMusicData dbHelperUserMusicData = new DbHelperUserMusicData(context);
        SQLiteDatabase db = dbHelperUserMusicData.getReadableDatabase();
        dbHelperUserMusicData.onCreate(db);

        String where = DbHelperUserMusicData.KEY_ID + "= '" + id +"'" ;
        Cursor cursor = db.query(DbHelperUserMusicData.TABLE_NAME
                ,new String[]{DbHelperUserMusicData.KEY_FAV},where,null,null,null
                ,null,null);

        if(cursor.getCount()!=0){
            cursor.moveToFirst();
            if(cursor.getInt(cursor.getColumnIndex(DbHelperUserMusicData.KEY_FAV))>0){
                returnValue =true;
            }
        }
        cursor.close();
        return  returnValue;
    }

    public void RemoveFromFavNew(final int id){
        SQLiteDatabase db = dbHelperUserMusicData.getWritableDatabase();
        dbHelperUserMusicData.onCreate(db);

        ContentValues c = new ContentValues();
        c.put(DbHelperUserMusicData.KEY_FAV, 0);
        db.update(DbHelperUserMusicData.TABLE_NAME,c,DbHelperUserMusicData.KEY_ID + "= ?", new String[] {id+""});

        String favPlayListName = Constants.SYSTEM_PLAYLISTS.MY_FAV.replace("_", " ");
        if(listOfPlaylists.containsKey(favPlayListName)){
            listOfPlaylists.put(favPlayListName, listOfPlaylists.get(favPlayListName)-1);
        }
    }

    public void StoreLastPlayingQueueNew(final ArrayList<Integer> tracklist){

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbHelperUserMusicData.getWritableDatabase();
                dbHelperUserMusicData.onCreate(db);

                //clear column first
                ContentValues c = new ContentValues();
                c.put(DbHelperUserMusicData.KEY_LAST_PLAYING_QUEUE, 0);
                db.update(DbHelperUserMusicData.TABLE_NAME,c,null,null);

                int count = 0;

                try {
                    for (int id : tracklist) {
                        c = new ContentValues();
                        c.put(DbHelperUserMusicData.KEY_LAST_PLAYING_QUEUE, 1);
                        db.update(DbHelperUserMusicData.TABLE_NAME, c, DbHelperUserMusicData.KEY_ID + "= ?", new String[]{id+""});
                        count++;
                    }
                }catch (ConcurrentModificationException ignored){
                    //other instance of this thread started, ignore this exception
                    //let other thread save the queue, exit this thread
                    Log.d("PlaylistManager", "run: Error storing queue : Concurrrent modification");
                }catch (Exception ignored){

                }

                Log.d("PlaylistManager", "StorePlayingQue: restored list count : " + tracklist.size());
                Log.d("PlaylistManager", "StorePlayingQue: restored queue count : " + count);
            }
        });

    }

    public ArrayList<Integer> RestoreLastPlayingQueueNew(){
//        DbHelperUserMusicData dbHelperUserMusicData = new DbHelperUserMusicData(context);
        SQLiteDatabase db = dbHelperUserMusicData.getReadableDatabase();
        dbHelperUserMusicData.onCreate(db);

        String where = DbHelperUserMusicData.KEY_LAST_PLAYING_QUEUE + " = 1";
        Cursor c = db.query(DbHelperUserMusicData.TABLE_NAME, new String[]{DbHelperUserMusicData.KEY_ID}
                , where, null
                , null, null, null);
        ArrayList<Integer> tracklist=new ArrayList<>();
        while (c.moveToNext()) {
            int id;
            try {
                id = Integer.valueOf(c.getString(0));
            }catch (Exception e){continue;}

            tracklist.add(id);
        }
        c.close();
        Log.d("PlaylistManager", "RestoreLastPlayingQueue: restored queue count : " + tracklist.size());
        return tracklist;
    }

    public boolean ClearPlaylist( String playlist_name){

        if(listOfPlaylists.containsKey(playlist_name)){
            listOfPlaylists.put(playlist_name,0L);
        }

        playlist_name = playlist_name.replace(" ","_");

        switch (playlist_name){
            case Constants.SYSTEM_PLAYLISTS.MOST_PLAYED:
                playlist_name = DbHelperUserMusicData.KEY_COUNT;
                break;

            case Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED:
                playlist_name = DbHelperUserMusicData.KEY_TIME_STAMP;
                break;

            case Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED:
                return false;

            default:
                break;
        }

        SQLiteDatabase db = dbHelperUserMusicData.getWritableDatabase();
        dbHelperUserMusicData.onCreate(db);

        String query = "UPDATE `" + DbHelperUserMusicData.TABLE_NAME +"` SET `" + playlist_name + "` = '0'";
        try {
            db.execSQL(query);
        }catch (Exception e){
            Log.d("PlaylistManager", "ClearPlaylist: error");
            return false;
        }
        return true;
    }

    public long getTrackCountFromCache(String playlist_name){

        if (listOfPlaylists.containsKey(playlist_name)){
            return listOfPlaylists.get(playlist_name);
        }else {
            return 0;
        }
    }

    //private methods
    private ArrayList<dataItem> GetFav(){

        //DbHelperUserMusicData dbHelperUserMusicData = new DbHelperUserMusicData(context);
        SQLiteDatabase db = dbHelperUserMusicData.getReadableDatabase();
        dbHelperUserMusicData.onCreate(db);

        String where = DbHelperUserMusicData.KEY_FAV  + " != 0";

        Cursor c = db.query(DbHelperUserMusicData.TABLE_NAME, new String[]{DbHelperUserMusicData.KEY_ID}
                , where, null
                , null, null, DbHelperUserMusicData.KEY_FAV  );
        ArrayList<dataItem> tracklist=new ArrayList<>();
        while (c.moveToNext()) {
            for (dataItem d: MusicLibrary.getInstance().getDataItemsForTracks()){
                if(d.id == c.getInt(0)) {
                    tracklist.add(d);
                    break;
                }
            }
        }
        c.close();
        return tracklist;
    }

    private ArrayList<dataItem> GetRecentlyPlayed(){
        // DbHelperUserMusicData dbHelperUserMusicData = new DbHelperUserMusicData(context);
        SQLiteDatabase db = dbHelperUserMusicData.getReadableDatabase();
        dbHelperUserMusicData.onCreate(db);

        ArrayList<dataItem> tracklist= new ArrayList<>();
        String where = DbHelperUserMusicData.KEY_TIME_STAMP + " > 0 ";
        Cursor cursor = db.query(DbHelperUserMusicData.TABLE_NAME,new String[]{DbHelperUserMusicData.KEY_ID}
                ,where,null,null,null,DbHelperUserMusicData.KEY_TIME_STAMP+" DESC",""+Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED_MAX);

        while (cursor.moveToNext()) {
            for (dataItem d : MusicLibrary.getInstance().getDataItemsForTracks()) {
                if (d.id ==  cursor.getInt(0)) {
                    tracklist.add(d);
                    break;
                }
            }
        }
        cursor.close();

        return tracklist;
    }

    private ArrayList<dataItem> GetRecentlyAdded(){
        HashMap<String, Integer> pathToId=new HashMap<>();
        ArrayList<File> musicFiles=new ArrayList<>();
        for (dataItem item:MusicLibrary.getInstance().getDataItemsForTracks()){
            musicFiles.add(new File(item.file_path));
            pathToId.put(item.file_path
                    ,item.id);
        }

        Collections.sort( musicFiles, new Comparator()
        {
            public int compare(Object o1, Object o2) {

                if (((File)o1).lastModified() > ((File)o2).lastModified()) {
                    return -1;
                } else if (((File)o1).lastModified() < ((File)o2).lastModified()) {
                    return +1;
                } else {
                    return 0;
                }
            }
        });

        ArrayList<dataItem> tracklist = new ArrayList<>();

        int lessThanCount = musicFiles.size()>50?50:musicFiles.size();

        for(int i=0;i<lessThanCount;i++){
            for (dataItem d: MusicLibrary.getInstance().getDataItemsForTracks()){
                if(d.id == pathToId.get(musicFiles.get(i).getAbsolutePath())) {
                    tracklist.add(d);
                    break;
                }
            }
        }
        return tracklist;
    }

    private ArrayList<dataItem> GetUserPlaylist(String playlist_name){

        playlist_name = "\"" + playlist_name + "\"";

        //DbHelperUserMusicData dbHelperUserMusicData = new DbHelperUserMusicData(context);
        SQLiteDatabase db = dbHelperUserMusicData.getReadableDatabase();
        dbHelperUserMusicData.onCreate(db);

        String where = playlist_name + " != 0";

        Cursor c = db.query(DbHelperUserMusicData.TABLE_NAME, new String[]{DbHelperUserMusicData.KEY_ID}
                , where, null
                , null, null,  playlist_name );
        ArrayList<dataItem> tracklist=new ArrayList<>();

        while (c.moveToNext()) {
            for (dataItem d : MusicLibrary.getInstance().getDataItemsForTracks()) {
                if (d.id == c.getInt(0)) {
                    tracklist.add(d);
                    break;
                }
            }
        }
        c.close();
        return tracklist;
    }

    private ArrayList<dataItem> GetMostPlayed(){
        //   DbHelperUserMusicData dbHelperUserMusicData = new DbHelperUserMusicData(context);
        SQLiteDatabase db = dbHelperUserMusicData.getReadableDatabase();
        dbHelperUserMusicData.onCreate(db);

        ArrayList<dataItem> tracklist= new ArrayList<>();
        String where = DbHelperUserMusicData.KEY_COUNT + " > 0 ";

        Cursor cursor = db.query(DbHelperUserMusicData.TABLE_NAME,new String[]{DbHelperUserMusicData.KEY_ID}
                ,where,null,null,null,DbHelperUserMusicData.KEY_COUNT+" DESC",""+Constants.SYSTEM_PLAYLISTS.MOST_PLAYED_MAX);

        /*while (cursor.moveToNext()){
            tracklist.add(cursor.getString(0));
        }*/

        while (cursor.moveToNext()) {
            for (dataItem d : MusicLibrary.getInstance().getDataItemsForTracks()) {
                if (d.id == cursor.getInt(0)) {
                    tracklist.add(d);
                    break;
                }
            }
        }

        cursor.close();
        return tracklist;
    }

    //get track count for playlist from db
    private long getTrackCount(String playlist_name){
        playlist_name = playlist_name.replace(" ","_");

        //DbHelperUserMusicData dbHelperUserMusicData = new DbHelperUserMusicData(context);
        SQLiteDatabase db = dbHelperUserMusicData.getReadableDatabase();
        dbHelperUserMusicData.onCreate(db);

        if(playlist_name.equals(Constants.SYSTEM_PLAYLISTS.MOST_PLAYED)){
            String where = DbHelperUserMusicData.KEY_COUNT + " > 0 ";

            Cursor cursor = db.query(DbHelperUserMusicData.TABLE_NAME,new String[]{DbHelperUserMusicData.KEY_ID}
                    ,where,null,null,null,DbHelperUserMusicData.KEY_COUNT+" DESC",""+Constants.SYSTEM_PLAYLISTS.MOST_PLAYED_MAX);
            int count =  cursor.getCount();
            cursor.close();
            return count;
        }

        if(playlist_name.equals(Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED)){
            String where = DbHelperUserMusicData.KEY_TIME_STAMP + " > 0 ";
            Cursor cursor = db.query(DbHelperUserMusicData.TABLE_NAME,new String[]{DbHelperUserMusicData.KEY_ID}
                    ,where,null,null,null,DbHelperUserMusicData.KEY_TIME_STAMP+" DESC",""+Constants.SYSTEM_PLAYLISTS.RECENTLY_PLAYED_MAX);

            int count =  cursor.getCount();
            cursor.close();
            return count;
        }

        if(playlist_name.equals(Constants.SYSTEM_PLAYLISTS.RECENTLY_ADDED)){
            return 50;  //very ugly
        }

        playlist_name = "\"" + playlist_name + "\"";

        String where = playlist_name + " != 0";
        long count = DatabaseUtils.queryNumEntries(db, DbHelperUserMusicData.TABLE_NAME, where);

        Log.d("PlaylistManager", "getTrackCount: "+ playlist_name + " : " + count);
        return count;
    }
}



