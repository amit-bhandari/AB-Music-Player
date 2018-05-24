package com.music.player.bhandari.m.utils;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.music.player.bhandari.m.MyApp;
import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.activity.ActivityPermissionSeek;
import com.music.player.bhandari.m.model.Constants;
import com.music.player.bhandari.m.model.MusicLibrary;
import com.music.player.bhandari.m.model.PlaylistManager;
import com.music.player.bhandari.m.model.TrackItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
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

public class UtilityFun {

    public static String secondsToString(int pTime) {
        return String.format("%02d:%02d", pTime / 60, pTime % 60);
    }

    public static int getProgressPercentage(int currentDuration, int totalDuration){
        Double percentage;

        long currentSeconds = (int) (currentDuration);
        long totalSeconds = (int) (totalDuration);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    public static int progressToTimer(int progress, int totalDuration) {
        int currentDuration;
        totalDuration = totalDuration / 1000;
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    public static String msToString(long pTime) {
        return String.format("%02d:%02d", (pTime/1000) / 60, (pTime/1000) % 60);
    }

    public static String escapeDoubleQuotes(String title){
        //escape all the quotes
        ArrayList<Integer> indexList = new ArrayList<>();
        StringBuffer stringBuffer = new StringBuffer(title);
        int index = stringBuffer.indexOf("\"");
        while(index >= 0) {
            indexList.add(index);
            index = stringBuffer.indexOf("\"", index+1);
        }
        int i=0;
        for(int tempIndex:indexList){
            stringBuffer.insert(tempIndex+i,"\\");
            i++;
        }
        return stringBuffer.toString();
    }

    public static void AddToPlaylist(final Context context, final int[] song_titles){
        new MaterialDialog.Builder(context)
                .typeface(TypeFaceHelper.getTypeFace(context),TypeFaceHelper.getTypeFace(context))
                .title(context.getString(R.string.select_playlist_title))
                .items(PlaylistManager.getInstance(context).GetPlaylistList(true))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        PlaylistManager.getInstance(context).AddSongToPlaylistNew(text.toString(),song_titles);
                    }
                })
                .show();

    }

    public static void Share(Context context, ArrayList<Uri> uris, String title){
        if(uris.size()==1) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/*");
            share.putExtra(Intent.EXTRA_STREAM, uris.get(0));
            context.startActivity(Intent.createChooser(share, title));
        }else {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("*/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            context.startActivity(Intent.createChooser(intent, title));
        }
    }

    public static void ShareFromPath(Context context, String filePath){
        Intent intentShareFile = new Intent();
        intentShareFile.setType("audio/*");
        intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+filePath));

        intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                context.getString(R.string.share_file_extra_subject));
        intentShareFile.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_file_extra_text));

        context.startActivity(Intent.createChooser(intentShareFile, "Share track via"));
    }

    public static void LaunchYoutube(@NonNull Context context, @NonNull String query){
        Intent intent = new Intent(Intent.ACTION_SEARCH);
        intent.setPackage("com.google.android.youtube");
        intent.putExtra("query",query );
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            Toast.makeText(context, "Launching youtube in a moment...", Toast.LENGTH_SHORT).show();
        }catch (ActivityNotFoundException e){
            Toast.makeText(context, "Error launching Youtube", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean Delete(Context context, ArrayList<File> files, ArrayList<Integer> ids){
        if(DeleteFiles(files)){
            if(ids!=null) {
                DeleteFromContentProvider(ids, context);
            }
            return true;
        }else {
            return false;
        }
    }

    private static boolean DeleteFiles(ArrayList<File> files){
        boolean result=false;
        for(File f:files){
            if(f.delete()){
                result = true;
            }else {
                result = false;
                break;
            }
        }
        return  result;
    }

    private static void DeleteFromContentProvider(ArrayList<Integer> ids, Context context){
       // boolean result =false;
        try {
            for (int id : ids) { // NOTE: You would normally obtain this from the content provider!
                Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Uri itemUri = ContentUris.withAppendedId(contentUri, id);
                int rows = context.getContentResolver().delete(itemUri, null, null);
                String path = itemUri.getEncodedPath();
                if (rows == 0) {
                    Log.e("Example Code:", "Could not delete " + path + " :(");
                    //result = false;
                    break;
                } else {
                    Log.d("Example Code:", "Deleted " + path + " ^_^");
                    //result = true;
                }
            }
        }catch (Exception ignored){}
        //return  result;
    }

    public static void SetRingtone(final Context context, final String filePath , final int id){
        if(!checkSystemWritePermission(context)){
            new MaterialDialog.Builder(context)
                    .typeface(TypeFaceHelper.getTypeFace(context),TypeFaceHelper.getTypeFace(context))
                    .title(context.getString(R.string.write_setting_perm_title))
                    .content(context.getString(R.string.write_setting_perm_content))
                    .positiveText(context.getString(R.string.okay))
                    .negativeText(context.getString(R.string.cancel))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            openAndroidPermissionsMenu(context);
                        }
                    })
                    .show();

        }else {

            final TrackItem item = MusicLibrary.getInstance().getTrackItemFromId(id);
            if(item==null) return;

            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    File k = new File(filePath);

                    File newFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES)
                            .getAbsolutePath()
                            + "/AB_Music_tone.mp3");
                    try
                    {
                        newFile.createNewFile();
                        copy(k,newFile);
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                    if(!k.canRead()){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Unable to set ringtone: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        return;
                    }
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DATA, newFile.getAbsolutePath());
                    values.put(MediaStore.MediaColumns.TITLE, item.getTitle()+" Tone");
                    values.put(MediaStore.MediaColumns.SIZE, k.length());
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
                    values.put(MediaStore.Audio.Media.DURATION, 230);
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                    values.put(MediaStore.Audio.Media.IS_ALARM, true);
                    values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                    //Insert it into the database
                    Uri uri1 = MediaStore.Audio.Media.getContentUriForPath(newFile.getAbsolutePath());
                    context.getContentResolver().delete(uri1, MediaStore.MediaColumns.DATA + "=\"" + newFile.getAbsolutePath() + "\"",
                            null);
                    Uri newUri = context.getContentResolver().insert(uri1, values);

                    try {
                        RingtoneManager.setActualDefaultRingtoneUri(
                                context,
                                RingtoneManager.TYPE_RINGTONE,
                                newUri
                        );
                    }catch (SecurityException e){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Error setting ringtone.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Ringtone set: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });



        }
    }

    private static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private static boolean checkSystemWritePermission(Context context) {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(context);
            Log.d(Constants.TAG, "Can Write Settings: " + retVal);
        }
        return retVal;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static void openAndroidPermissionsMenu(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName()));
        //intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isConnectedToInternet(){
        ConnectivityManager
                cm = (ConnectivityManager) MyApp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    public static StringBuilder trackInfoBuild(int id){
        StringBuilder trackInfo = new StringBuilder();
        TrackItem item = MusicLibrary.getInstance().getTrackItemFromId(id);
        if(item==null){
            return trackInfo;
        }

        trackInfo.append("Title : ")
                .append(item.getTitle())
                .append("\n\n")
                .append("Artist : ")
                .append(item.getArtist())
                .append("\n\n").append("Album : ")
                .append(item.getAlbum()).append("\n\n")
                .append("Duration : ")
                .append(item.getDurStr()).append("\n\n")
                .append("File Path : ")
                .append(item.getFilePath()).append("\n\n")
                .append("File Size : ")
                .append(android.text.format.Formatter.formatFileSize(MyApp.getContext(), new File(item.getFilePath()).length()));
                //.append(new File(item.getFilePath()).length()/(1024*1024)).append(" MB");

        return trackInfo;
    }

    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize)
            throws FileNotFoundException, SecurityException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;


        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth
                , height_tmp = o.outHeight;
        int scale = 1;

        while(true) {
            if(width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }


    public static String filterArtistString(@NonNull String artist) {

        artist = artist.toLowerCase();

        if(artist.contains("&")) {
            String[] parts = artist.split("&");
            artist = parts[0];
            return artist;
        }

        if(artist.contains(",")) {
            String[] parts = artist.split(",");
            artist = parts[0];
            return artist;
        }

        if(artist.contains("feat")) {
            String[] parts = artist.split("feat");
            artist = parts[0];
            return artist;
        }

        if(artist.contains("ft")) {
            String[] parts = artist.split("ft");
            artist = parts[0];
            return artist;
        }

        return artist;
    }


    //delete cache if more than 50mb
    //delete only if file is more than 2 day old
    public static void deleteCache(){
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.v("Amit AB","deleting cache");
                    int daysBack = 2;

                    File CACHE = MyApp.getContext().getCacheDir();
                    // Get length of file in bytes
                    long fileSizeInBytes = CACHE.length();
                    long fileSizeInKB = fileSizeInBytes / 1024;
                    long fileSizeInMB = fileSizeInKB / 1024;

                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_MONTH, daysBack * -1);
                    long purgeTime = cal.getTimeInMillis();

                    Log.v("Amit AB","cache size " + fileSizeInBytes);
                    Log.v("Amit AB","cache directory " + CACHE.getAbsolutePath());
                    //if (fileSizeInMB > 1) {
                        for (File listFile : CACHE.listFiles()) {

                            //if (listFile.lastModified() < purgeTime) {
                                listFile.delete();
                                Log.v("Amit AB","deleting "+listFile.getName() );
                            //}
                        }
                    //}
                }catch (Exception ignored){
                    Log.v("Amit AB","Error deleting cache");
                }
            }
        });
    }

    public static boolean isAdsRemoved(){
        //boolean hideAdsTemp = MyApp.getPref().getBoolean(MyApp.getContext().getString(R.string.pref_remove_ads_temp),false);
        return MyApp.getPref().getBoolean(MyApp.getContext().getString(R.string.pref_remove_ads_after_payment),false);
    }

    public static void logEvent(@NonNull Bundle bundle){
        FirebaseAnalytics.getInstance(MyApp.getContext()).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    /**
     * This gives you a random number in between from (inclusive) and to (exclusive)
     * @param from
     * @param to
     * @return
     */
    public static int getRandom(int from, int to){
        Random r = new Random();
        return r.nextInt(to-from) + from;
    }

    public static void restartApp() {
        Intent intent = new Intent(MyApp.getContext(), ActivityPermissionSeek.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        MyApp.getContext().startActivity(intent);
    }

}
