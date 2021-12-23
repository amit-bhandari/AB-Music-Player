package com.music.player.bhandari.m.utils

import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.content.*
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.activity.ActivityPermissionSeek
import com.music.player.bhandari.m.model.MusicLibrary
import java.io.*
import java.util.*

/**
 * Copyright 2017 Amit Bhandari AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
object UtilityFun {
    val screenWidth: Int
        get() = Resources.getSystem().displayMetrics.widthPixels

    fun getProgressPercentage(currentDuration: Int, totalDuration: Int): Int {
        val percentage: Double
        val currentSeconds: Long = currentDuration.toLong()
        val totalSeconds: Long = totalDuration.toLong()

        // calculating percentage
        percentage = currentSeconds.toDouble() / totalSeconds * 100

        // return percentage
        return percentage.toInt()
    }

    fun progressToTimer(progress: Int, totalDuration: Int): Int {
        var totalDuration = totalDuration
        totalDuration /= 1000
        val currentDuration = (progress.toDouble() / 100 * totalDuration).toInt()

        // return current duration in milliseconds
        return currentDuration * 1000
    }

    fun msToString(pTime: Long): String {
        return String.format("%02d:%02d", pTime / 1000 / 60, pTime / 1000 % 60)
    }

    fun escapeDoubleQuotes(title: String?): String {
        //escape all the quotes
        val indexList = ArrayList<Int>()
        val stringBuffer = StringBuffer(title)
        var index = stringBuffer.indexOf("\"")
        while (index >= 0) {
            indexList.add(index)
            index = stringBuffer.indexOf("\"", index + 1)
        }
        var i = 0
        for (tempIndex in indexList) {
            stringBuffer.insert(tempIndex + i, "\\")
            i++
        }
        return stringBuffer.toString()
    }

    fun AddToPlaylist(context: Context, song_titles: IntArray?) {
//        val dialog: MaterialDialog = MyDialogBuilder(context)
//            .title(context.getString(R.string.select_playlist_title))
//            .items(PlaylistManager.getInstance(MyApp.getContext()).GetPlaylistList(true))
//            .itemsCallback(object : ListCallback() {
//                fun onSelection(
//                    dialog: MaterialDialog?,
//                    view: View?,
//                    which: Int,
//                    text: CharSequence
//                ) {
//                    PlaylistManager.getInstance(MyApp.getContext())
//                        .AddSongToPlaylist(text.toString(), song_titles)
//                }
//            })
//            .build()
//
//        //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//        dialog.show()
    }

    fun Share(context: Context, uris: ArrayList<Uri>, title: String?) {
        if (uris.size == 1) {
            val share = Intent(Intent.ACTION_SEND)
            share.type = "audio/*"
            share.putExtra(Intent.EXTRA_STREAM, uris[0])
            context.startActivity(Intent.createChooser(share, title))
        } else {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND_MULTIPLE
            intent.type = "*/*"
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            context.startActivity(Intent.createChooser(intent, title))
        }
    }

    fun ShareFromPath(context: Context, filePath: String) {
        val intentShareFile = Intent()
        intentShareFile.type = "audio/*"
        intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$filePath"))
        intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
            context.getString(R.string.share_file_extra_subject))
        intentShareFile.putExtra(Intent.EXTRA_TEXT,
            context.getString(R.string.share_file_extra_text))
        context.startActivity(Intent.createChooser(intentShareFile, "Share track via"))
    }

    fun LaunchYoutube(context: Context, query: String) {
        val intent = Intent(Intent.ACTION_SEARCH)
        intent.setPackage("com.google.android.youtube")
        intent.putExtra("query", query)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(intent)
            Toast.makeText(context, "Launching youtube in a moment...", Toast.LENGTH_SHORT).show()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Error launching Youtube", Toast.LENGTH_SHORT).show()
        }
    }

    fun Delete(context: Context, files: ArrayList<File>, ids: ArrayList<Int>?): Boolean {
        return if (DeleteFiles(files)) {
            if (ids != null) {
                DeleteFromContentProvider(ids, context)
            }
            true
        } else {
            false
        }
    }

    private fun DeleteFiles(files: ArrayList<File>): Boolean {
        var result = false
        for (f in files) {
            if (f.delete()) {
                result = true
            } else {
                result = false
                break
            }
        }
        return result
    }

    private fun DeleteFromContentProvider(ids: ArrayList<Int>, context: Context) {
        // boolean result =false;
        try {
            for (id in ids) { // NOTE: You would normally obtain this from the content provider!
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val itemUri = ContentUris.withAppendedId(contentUri, id.toLong())
                val rows = context.contentResolver.delete(itemUri, null, null)
                val path = itemUri.encodedPath
                if (rows == 0) {
                    Log.e("Example Code:", "Could not delete $path :(")
                    //result = false;
                    break
                } else {
                    Log.d("Example Code:", "Deleted $path ^_^")
                    //result = true;
                }
            }
        } catch (ignored: Exception) {
        }
        //return  result;
    }

    fun SetRingtone(context: Context, filePath: String?, id: Int) {
        if (!checkSystemWritePermission(context)) {
//            val dialog: MaterialDialog = MyDialogBuilder(context)
//                .title(context.getString(R.string.write_setting_perm_title))
//                .content(context.getString(R.string.write_setting_perm_content))
//                .positiveText(context.getString(R.string.okay))
//                .negativeText(context.getString(R.string.cancel))
//                .onPositive(object : SingleButtonCallback() {
//                    fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                        openAndroidPermissionsMenu(context)
//                    }
//                })
//                .build()
//
//            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//            dialog.show()
        } else {
//            val dialog: MaterialDialog = MyDialogBuilder(context)
//                .title(context.getString(R.string.action_set_as_ringtone))
//                .content("Would you like to use Ringtone Cutter first?")
//                .positiveText("Ringtone Cutter")
//                .negativeText("Set Directly")
//                .onPositive(object : SingleButtonCallback() {
//                    fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                        //launch ringtone cutter
//                        val intent =
//                            Intent(context.applicationContext, RingdroidEditActivity::class.java)
//                        intent.putExtra("file_path", filePath)
//                        intent.putExtra("was_get_content_intent", false)
//                        context.startActivity(intent)
//                    }
//                })
//                .onNegative(object : SingleButtonCallback() {
//                    fun onClick(dialog: MaterialDialog, which: DialogAction) {
//                        val item: TrackItem =
//                            MusicLibrary.getInstance().getTrackItemFromId(id) ?: return
//                        Executors.newSingleThreadExecutor().execute(Runnable {
//                            val k = File(filePath)
//                            val newFile = File(Environment.getExternalStoragePublicDirectory(
//                                Environment.DIRECTORY_RINGTONES)
//                                .absolutePath
//                                    + "/" + item.getTitle() + "_tone")
//                            try {
//                                newFile.createNewFile()
//                                copy(k, newFile)
//                            } catch (e: IOException) {
//                                e.printStackTrace()
//                            }
//                            if (!k.canRead()) {
//                                Handler(Looper.getMainLooper()).post {
//                                    Toast.makeText(context,
//                                        "Unable to set ringtone: " + item.getTitle(),
//                                        Toast.LENGTH_SHORT).show()
//                                }
//                                return@Runnable
//                            }
//                            val values = ContentValues()
//                            values.put(MediaStore.MediaColumns.DATA, newFile.absolutePath)
//                            values.put(MediaStore.MediaColumns.TITLE,
//                                item.getTitle().toString() + " Tone")
//                            values.put(MediaStore.MediaColumns.SIZE, k.length())
//                            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
//                            values.put(MediaStore.Audio.Media.DURATION, 230)
//                            values.put(MediaStore.Audio.Media.IS_RINGTONE, true)
//                            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true)
//                            values.put(MediaStore.Audio.Media.IS_ALARM, true)
//                            values.put(MediaStore.Audio.Media.IS_MUSIC, false)
//
//                            //Insert it into the database
//                            val uri1 =
//                                MediaStore.Audio.Media.getContentUriForPath(newFile.absolutePath)
//                            context.contentResolver.delete(uri1!!,
//                                MediaStore.MediaColumns.DATA + "=\"" + newFile.absolutePath + "\"",
//                                null)
//                            val newUri = context.contentResolver.insert(uri1, values)
//                            try {
//                                RingtoneManager.setActualDefaultRingtoneUri(
//                                    context,
//                                    RingtoneManager.TYPE_RINGTONE,
//                                    newUri
//                                )
//                            } catch (e: SecurityException) {
//                                Handler(Looper.getMainLooper()).post {
//                                    Toast.makeText(context,
//                                        "Error setting ringtone.",
//                                        Toast.LENGTH_SHORT).show()
//                                }
//                                return@Runnable
//                            }
//                            Handler(Looper.getMainLooper()).post {
//                                Toast.makeText(context,
//                                    "Ringtone set: " + item.getTitle(),
//                                    Toast.LENGTH_SHORT).show()
//                            }
//                        })
//                    }
//                })
//                .build()
//
//            //dialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//            dialog.show()
        }
    }

    @Throws(IOException::class)
    private fun copy(src: File, dst: File) {
        val `in`: InputStream = FileInputStream(src)
        val out: OutputStream = FileOutputStream(dst)

        // Transfer bytes from in to out
        val buf = ByteArray(1024)
        var len: Int
        while (`in`.read(buf).also { len = it } > 0) {
            out.write(buf, 0, len)
        }
        `in`.close()
        out.close()
    }

    private fun checkSystemWritePermission(context: Context): Boolean {
        var retVal = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(context)
            Log.d("Constants.TAG", "Can Write Settings: $retVal")
        }
        return retVal
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun openAndroidPermissionsMenu(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
            Uri.parse("package:" + context.packageName))
        //intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    val isConnectedToInternet: Boolean
        get() {
            val cm = MyApp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            return (activeNetwork != null
                    && activeNetwork.isConnectedOrConnecting)
        }

    fun trackInfoBuild(id: Int): StringBuilder {
        val trackInfo = StringBuilder()
        val item = MusicLibrary.instance!!.getTrackItemFromId(id) ?: return trackInfo
        trackInfo.append("Title : ")
            .append(item.title)
            .append("\n\n")
            .append("Artist : ")
            .append(item.getArtist())
            .append("\n\n").append("Album : ")
            .append(item.album).append("\n\n")
            .append("Duration : ")
            .append(item.durStr).append("\n\n")
            .append("File Path : ")
            .append(item.getFilePath()).append("\n\n")
            .append("File Size : ")
            .append(Formatter.formatFileSize(MyApp.getContext(), File(item.getFilePath()).length()))
        //.append(new File(item.getFilePath()).length()/(1024*1024)).append(" MB");
        return trackInfo
    }

    @Throws(FileNotFoundException::class, SecurityException::class)
    fun decodeUri(c: Context, uri: Uri?, requiredSize: Int): Bitmap? {
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(c.contentResolver.openInputStream(uri!!), null, o)
        var width_tmp = o.outWidth
        var height_tmp = o.outHeight
        var scale = 1
        while (true) {
            if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize) break
            width_tmp /= 2
            height_tmp /= 2
            scale *= 2
        }
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        return BitmapFactory.decodeStream(c.contentResolver.openInputStream(uri), null, o2)
    }

    fun filterArtistString(artist: String): String {
        var artist = artist
        artist = artist.lowercase(Locale.getDefault())
        if (artist.contains("&")) {
            val parts = artist.split("&".toRegex()).toTypedArray()
            artist = parts[0]
            return artist
        }
        if (artist.contains(",")) {
            val parts = artist.split(",".toRegex()).toTypedArray()
            artist = parts[0]
            return artist
        }
        if (artist.contains("feat")) {
            val parts = artist.split("feat".toRegex()).toTypedArray()
            artist = parts[0]
            return artist
        }
        if (artist.contains("ft")) {
            val parts = artist.split("ft".toRegex()).toTypedArray()
            artist = parts[0]
            return artist
        }
        return artist
    }

    //boolean hideAdsTemp = MyApp.getPref().getBoolean(MyApp.getContext().getString(R.string.pref_remove_ads_temp),false);
    //return MyApp.getPref().getBoolean(MyApp.getContext().getString(R.string.pref_remove_ads_after_payment),false);
    val isAdsRemoved: Boolean
        get() = true

    //boolean hideAdsTemp = MyApp.getPref().getBoolean(MyApp.getContext().getString(R.string.pref_remove_ads_temp),false);
    //return MyApp.getPref().getBoolean(MyApp.getContext().getString(R.string.pref_remove_ads_after_payment),false);
    fun logEvent(bundle: Bundle) {
        FirebaseAnalytics.getInstance(MyApp.getContext())
            .logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    /**
     * This gives you a random number in between from (inclusive) and to (exclusive)
     * @param from
     * @param to
     * @return
     */
    fun getRandom(from: Int, to: Int): Int {
        val r = Random()
        return r.nextInt(to - from) + from
    }

    fun restartApp() {
        val intent = Intent(MyApp.getContext(), ActivityPermissionSeek::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        MyApp.getContext().startActivity(intent)
    }

    //return batman in case user selected image causes out of memory
    val defaultAlbumArtDrawable: Drawable?
        get() = try {
            Drawable.createFromPath(MyApp.getContext().filesDir.toString() + MyApp.getContext()
                .getString(R.string.def_album_art_custom_image))
        } catch (e: OutOfMemoryError) {
            //return batman in case user selected image causes out of memory
            MyApp.getContext().getDrawable(R.drawable.ic_batman_1)
        }

    fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        /*if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }*/
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    val isBluetoothHeadsetConnected: Boolean
        get() {
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            return (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled
                    && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED)
        }
    /*public static int getCurrentThemeId(){
        //@todo do this in my app maybe, avoid lookup of mypref every time
        return MyApp.getPref().getInt(MyApp.getContext().getString(R.string.pref_theme_id), 0);
    }*/
    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    fun convertDpToPixel(dp: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    /**
     * https://stackoverflow.com/questions/4753251/how-to-go-about-formatting-1200-to-1-2k-in-java
     */
    private val c = charArrayOf('k', 'm', 'b', 't')

    /**
     * Recursive implementation, invokes itself for each factor of a thousand, increasing the class on each invokation.
     * @param n the number to format
     * @param iteration in fact this is the class from the array c
     * @return a String representing the number n formatted in a cool looking way.
     */
    fun coolFormat(n: Double, iteration: Int): String {
        val d = n.toLong() / 100 / 10.0
        val isRound =
            d * 10 % 10 == 0.0 //true if the decimal part is equal to 0 (then it's trimmed anyway)
        return when {
            d < 1000 //this determines the class, i.e. 'k', 'm' etc
            -> (when {
                d > 99.9 || isRound || !isRound && d > 9.99 //this decides whether to trim the decimals
                -> d.toInt() * 10 / 10
                else -> d.toString() + ""
            } // (int) d * 10 / 10 drops the decimal
                    ).toString() + "" + c[iteration]
            else -> coolFormat(d, iteration + 1)
        }
    }
}