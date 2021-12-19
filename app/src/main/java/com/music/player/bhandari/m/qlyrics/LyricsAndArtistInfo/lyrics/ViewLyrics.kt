/*
 * *
 *  * This file is part of QuickLyric
 *  * Created by PedroHLC, modified by geecko
 *  *
 *  * QuickLyric is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * QuickLyric is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  * You should have received a copy of the GNU General Public License
 *  * along with QuickLyric.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.music.player.bhandari.m.model.TrackItem
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Levenshtein
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Net
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.annotations.Reflection
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.Companion.NEGATIVE_RESULT
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.Companion.NO_RESULT
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.Companion.POSITIVE_RESULT
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.Companion.SEARCH_ITEM
import com.squareup.okhttp.*
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

object ViewLyrics {
    /*
     * Needed data
     */
    private val url: String = "http://search.crintsoft.com/searchlyrics.htm"

    //ACTUAL: http://search.crintsoft.com/searchlyrics.htm
    //CLASSIC: http://www.viewlyrics.com:1212/searchlyrics.htm
    val clientUserAgent: String = "MiniLyrics4Android"

    //NORMAL: MiniLyrics <version> for <player>
    //EXAMPLE: MiniLyrics 7.6.44 for Windows Media Player
    //MOBILE: MiniLyrics4Android
    private val clientTag: String = "client=\"ViewLyricsOpenSearcher\""

    //NORMAL: MiniLyrics
    //MOBILE: MiniLyricsForAndroid
    private val searchQueryBase: String =
        "<?xml version='1.0' encoding='utf-8' ?><searchV1 artist=\"%s\" title=\"%s\" OnlyMatched=\"1\" %s/>"
    private val searchQueryPage: String = " RequestPage='%d'"
    private val magickey: ByteArray = "Mlv1clt4.0".toByteArray()

    /*
     * Search function
    	 */
    @Reflection
    fun fromURL(url: String?, artist: String?, title: String?): Lyrics {
        // TODO: support ViewLyrics URL
        return Lyrics(NO_RESULT)
    }

    @Throws(IOException::class,
        NoSuchAlgorithmException::class,
        SAXException::class,
        ParserConfigurationException::class)
    fun fromMetaData(artist: String?, title: String?): Lyrics {
        val results: ArrayList<Lyrics> =
            search(String.format(searchQueryBase, artist, title, clientTag + String.format(
                searchQueryPage, 0)))
        if (results.size == 0) return Lyrics(NEGATIVE_RESULT)
        var lrcIndex: Int = 0
        var lrcLyricFound: Boolean = false
        for (i in results.indices) {
            if (results.get(i).getURL()!!.endsWith("lrc")) {
                lrcLyricFound = true
                lrcIndex = i
                break
            }
        }

        //lrcIndex = 0;

        /*for(int i=0;i<results.size();i++) {
            Log.v("ViewLyrics", "Lyrics " + i + " : " + results.get(i).getOriginalTrack() + " : " + results.get(i).getOriginalTrack());
            Log.v("ViewLyrics", "Lyrics " + i + " : " + results.get(i).getURL());
        }

        Handler handler = new Handler(Looper.getMainLooper());

        / *
        handler.post(new Runnable() {
            @Override
            public void run() {

                new MyDialogBuilder(MyApp.getContext())
                        .title("title").show();
            }
        });*/
        var url: String?
        val foundTitle: String?
        val foundArtist: String?
        if (lrcLyricFound) {
            url = results.get(lrcIndex).getURL()
            foundTitle = results.get(lrcIndex).getTrack()
            foundArtist = results.get(lrcIndex).getArtist()
        } else {
            url = results[0].getURL()
            foundTitle = results[0].getTrack()
            foundArtist = results[0].getArtist()
        }
        url = url!!.replace("minilyrics", "viewlyrics")
        val artistDistance: Int = Levenshtein.distance(results[0].getOriginalArtist()!!, artist!!)
        val titleDistance: Int = Levenshtein.distance(results[0].getTrack()!!, title!!)
        val result: Lyrics = Lyrics(POSITIVE_RESULT)
        result.setTitle(foundTitle)
        result.setArtist(foundArtist)
        result.setOriginalArtist(artist)
        result.setOriginalTitle(title)
        result.setSource(clientUserAgent)
        val arr =
            Net.getUrlAsString(url).replace("(\\[(?=.[a-z]).+\\]|<.+?>|www.*[\\s])", "")
                .replace("[\n]\\[(.*?)\\]+[\\s]", "").split("\n")
        var output: String = ""
        if (url.endsWith("txt") /*|| artistDistance > 6 || titleDistance > 6*/) {
            for (line: String in arr) {
                output = output + line + "\n"
            }
            result.setText(output.replace("\n", "<br />"))
            return result
        }
        result.setLRC(url.endsWith("lrc"))
        for (line: String in arr) {
            val word: String = line.replace("[^A-Za-z\\s]".toRegex(), "")
            val newLine: String = line.replace("\\]\\[".toRegex(), "\\]$word\n\\[")
            output += newLine
        }
        result.setText(output.replace("\\[".toRegex(), "\n\\[ "))
        return result
    }

    //for giving choice to select lyrics from available ones
    @Throws(IOException::class,
        NoSuchAlgorithmException::class,
        SAXException::class,
        ParserConfigurationException::class)
    fun fromMetaData(
        context: Context?,
        artist: String?,
        title: String?,
        item: TrackItem?,
        callback: Lyrics.Callback
    ) {
        Log.d("ActivityInstantLyric", "onLyricsDownloaded: in view lyric thread now")
        val handler: Handler = Handler(Looper.getMainLooper())
       // val builder: MaterialDialog.Builder = MyDialogBuilder(context)
//        builder.title("Searching for other lyrics...")
//        builder.progress(true, 0)

        //all this bullshit because of inner class shit
        //and the fact that android do not allow changing UI from non UI thread, duh
       // val dialog: Array<MaterialDialog?> = arrayOfNulls<MaterialDialog>(1)
        handler.post {
//            dialog[0] = builder.build()
//            dialog[0].show()
        }
        val results: ArrayList<Lyrics> =
            search(String.format(searchQueryBase, artist, title, clientTag + String.format(
                searchQueryPage, 0)))
        if (results.size == 0) {
            handler.post {
//                dialog[0].dismiss()
                Toast.makeText(context, "No lyrics found for track!", Toast.LENGTH_SHORT).show()
            }
            return
        }
        for (i in results.indices) {
            //Log.v("ViewLyrics", "Lyrics " + i + " : " + results.get(i).getOriginalTrack() + " : " + results.get(i).getOriginalTrack());
            Log.v("ViewLyrics", "Lyrics " + i + " : " + results.get(i).getURL())
        }
        Log.d("ActivityInstantLyric", "onLyricsDownloaded: found lyrics count " + results.size)

        //show dialog with available options
        val resultTrackTitles: ArrayList<String> = ArrayList()
        for (lyric: Lyrics in results) {
            if (lyric.getURL()!!.endsWith("lrc")) {
                resultTrackTitles.add(lyric.getTrack()
                    .toString() + " : " + lyric.getArtist() + " (Running lyrics) ")
            } else {
                resultTrackTitles.add(lyric.getTrack().toString() + " : " + lyric.getArtist())
            }
        }
        handler.post {
          //  dialog[0].dismiss()
//            val mDialog: MaterialDialog = builder
//                .title("Choose any one")
//                .items(resultTrackTitles)
//                .itemsCallback(object : ListCallback() {
//                    fun onSelection(
//                        dialog: MaterialDialog?,
//                        view: View?,
//                        which: Int,
//                        text: CharSequence
//                    ) {
//                        Log.d("ActivityInstantLyric", "onLyricsDownloaded: clicked on $text")
//                        Executors.newSingleThreadExecutor().execute {
//                            var url: String? = results[which].getURL()
//                            val foundTitle: String? = results[which].getTrack()
//                            val foundArtist: String? = results[which].getArtist()
//                            url = url!!.replace("minilyrics", "viewlyrics")
//                            val result: Lyrics = Lyrics(POSITIVE_RESULT)
//                            result.setTitle(foundTitle)
//                            result.setArtist(foundArtist)
//                            result.setOriginalArtist(artist)
//                            result.setOriginalTitle(title)
//                            result.setSource(clientUserAgent)
//                            var arr: Array<String?> = arrayOfNulls(0)
//                            try {
//                                arr = Net.getUrlAsString(url)
//                                    .replace("(\\[(?=.[a-z]).+\\]|<.+?>|www.*[\\s])", "")
//                                    .replace("[\n]\\[(.*?)\\]+[\\s]", "").split("\n")
//                            } catch (e: IOException) {
//                                e.printStackTrace()
//                            }
//                            var output: String = ""
//                            Log.d("ActivityInstantLyric",
//                                "onLyricsDownloaded: lyrics type " + (if (url.endsWith("lrc")) "lrc" else "txt"))
//                            if (url.endsWith("txt") /*|| artistDistance > 6 || titleDistance > 6*/) {
//                                for (line: String? in arr) {
//                                    output = output + line + "\n"
//                                }
//                                result.setText(output.replace("\n", "<br />"))
//                                //return result;
//                            } else {
//                                result.setLRC(url.endsWith("lrc"))
//                                for (line: String? in arr) {
//                                    val word: String =
//                                        line!!.replace("[^A-Za-z\\s]".toRegex(), "")
//                                    val newLine: String = line.replace("\\]\\[".toRegex(),
//                                        "\\]$word\n\\[")
//                                    output += newLine
//                                }
//
//                                // Log.v(Constants.L_TAG+"chavan 0.5",output.replaceAll("\\[", "\n\\[ "));
//                                result.setText(output.replace("\\[".toRegex(), "\n\\[ "))
//                            }
//                            if (item != null && result.getFlag() === Lyrics.POSITIVE_RESULT) {
//                                // Log.v(Constants.L_TAG,lyrics.getText() );
//                                OfflineStorageLyrics.clearLyricsFromDB(item)
//                                OfflineStorageLyrics.putLyricsInDB(result, item)
//                            }
//
//                            //if from instant lyric activity, delete current lyrics from cache
//                            if (result.getFlag() === POSITIVE_RESULT) {
//                                OfflineStorageLyrics.clearLyricsFromCache(result)
//                            }
//                            handler.post { callback.onLyricsDownloaded(result) }
//                        }
//                    }
//                })
//                .build()
//
//            //mDialog.getWindow().getAttributes().windowAnimations = R.style.MyAnimation_Window;
//            mDialog.show()
        }
    }

    @Throws(IOException::class,
        ParserConfigurationException::class,
        SAXException::class,
        NoSuchAlgorithmException::class)
    private fun search(searchQuery: String): ArrayList<Lyrics> {
        val client: OkHttpClient = OkHttpClient()
        client.setConnectTimeout(10, TimeUnit.SECONDS)
        client.setReadTimeout(30, TimeUnit.SECONDS)
        Log.d("ViewLyrics", "search: $searchQuery")
        val body: RequestBody = RequestBody.create(MediaType.parse("application/text"),
            assembleQuery(searchQuery.toByteArray(charset("UTF-8"))))
        val request: Request = Request.Builder()
            .header("User-Agent", clientUserAgent)
            .post(body)
            .url(url)
            .build()
        val d = Log.d("ViewLyrics", "search: body $body")
        val response: Response = client.newCall(request).execute()
        val rd =
            BufferedReader(InputStreamReader(response.body().byteStream(), "ISO_8859_1"))

        // Get full result
        val builder: StringBuilder = StringBuilder()
        val buffer: CharArray = CharArray(8192)
        var read: Int
        while ((rd.read(buffer, 0, buffer.size).also { read = it }) > 0) {
            builder.append(buffer, 0, read)
        }
        val full: String = builder.toString()
        Log.d("ViewLyrics", "search: $full")

        // Decrypt, parse, store, and return the result list
        return parseResultXML(decryptResultXML(full))
    }

    /*
     * Add MD5 and Encrypts Search Query
	 */
    @Throws(NoSuchAlgorithmException::class, IOException::class)
    fun assembleQuery(valueBytes: ByteArray): ByteArray {
        // Create the variable POG to be used in a dirt code
        val pog: ByteArray =
            ByteArray(valueBytes.size + magickey.size) //TODO Give a better name then POG

        // POG = XMLQuery + Magic Key
        System.arraycopy(valueBytes, 0, pog, 0, valueBytes.size)
        System.arraycopy(magickey, 0, pog, valueBytes.size, magickey.size)

        // POG is hashed using MD5
        val pog_md5: ByteArray = MessageDigest.getInstance("MD5").digest(pog)

        //TODO Thing about using encryption or k as 0...
        // Prepare encryption key
        var j: Int = 0
        for (octet: Byte in valueBytes) {
            j += octet.toInt()
        }
        val k = (j / valueBytes.size)

        // Value is encrypted
        for (m in valueBytes.indices) valueBytes[m] = (k xor valueBytes[m].toInt()).toByte()

        // Prepare result code
        val result = ByteArrayOutputStream()

        // Write Header
        result.write(0x02)
        result.write(k)
        result.write(0x04)
        result.write(0x00)
        result.write(0x00)
        result.write(0x00)

        // Write Generated MD5 of POG problaby to be used in a search cache
        result.write(pog_md5)

        // Write encrypted value
        result.write(valueBytes)
        Log.d("ViewLyrics", "assembleQuery: $result")

        // Return magic encoded query
        return result.toByteArray()
    }

    /*
     * Decrypts only the XML from the entire result
	 */
    fun decryptResultXML(value: String): String {
        // Get Magic key value
        val magickey: Char = value.get(1)

        // Prepare output
        val neomagic: ByteArrayOutputStream = ByteArrayOutputStream()

        // Decrypts only the XML
        for (i in 22 until value.length) neomagic.write((value[i].xor(magickey)))
        Log.d("ViewLyrics", "decryptResultXML: $neomagic")
        // Return value
        return neomagic.toString()
    }

    /*
	 * Create the ArrayList<LyricInfo>
	 */
    private fun readStrFromAttr(elem: Element, attr: String, def: String): String {
        val data: String? = elem.getAttribute(attr)
        try {
            if (data != null && data.isNotEmpty()) return data
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return def
    }

    @Throws(SAXException::class, IOException::class, ParserConfigurationException::class)
    fun parseResultXML(resultXML: String): ArrayList<Lyrics> {
        // Create array for storing the results
        val availableLyrics: ArrayList<Lyrics> = ArrayList<Lyrics>()

        // Parse XML
        val resultBA =
            ByteArrayInputStream(resultXML.toByteArray(charset("UTF-8")))
        val resultRootElem: Element =
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resultBA)
                .documentElement
        val server_url: String =
            readStrFromAttr(resultRootElem, "server_url", "http://www.viewlyrics.com/")
        val resultItemList: NodeList = resultRootElem.getElementsByTagName("fileinfo")
        for (i in 0 until resultItemList.length) {
            val itemElem: Element = resultItemList.item(i) as Element
            val item: Lyrics = Lyrics(SEARCH_ITEM)
            item.setURL(server_url + readStrFromAttr(itemElem, "link", ""))
            item.setArtist(readStrFromAttr(itemElem, "artist", ""))
            item.setTitle(readStrFromAttr(itemElem, "title", ""))
            //item.setLyricsFileName(readStrFromAttr(itemElem, "filename", ""));
            //itemInfo.setFType(readIntFromAttr(itemElem, "file_type", 0));
            //itemInfo.setMatchVal(readFloatFromAttr(itemElem, "match_value", 0.0F));
            //itemInfo.setTimeLenght(readIntFromAttr(itemElem, "timelength", 0));
            availableLyrics.add(item)
        }
        return availableLyrics
    }
}