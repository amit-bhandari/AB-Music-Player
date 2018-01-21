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

package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.model.TrackItem;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Levenshtein;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.annotations.Reflection;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.Net;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.offlineStorage.OfflineStorageLyrics;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.NEGATIVE_RESULT;
import static com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.NO_RESULT;
import static com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.POSITIVE_RESULT;
import static com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics.SEARCH_ITEM;

public class ViewLyrics {

    /*
     * Needed data
     */
    private static final String url = "http://search.crintsoft.com/searchlyrics.htm";
    //ACTUAL: http://search.crintsoft.com/searchlyrics.htm
    //CLASSIC: http://www.viewlyrics.com:1212/searchlyrics.htm

    public static final String clientUserAgent = "MiniLyrics4Android";
    //NORMAL: MiniLyrics <version> for <player>
    //EXAMPLE: MiniLyrics 7.6.44 for Windows Media Player
    //MOBILE: MiniLyrics4Android

    private static final String clientTag = "client=\"ViewLyricsOpenSearcher\"";
    //NORMAL: MiniLyrics
    //MOBILE: MiniLyricsForAndroid

    private static final String searchQueryBase = "<?xml version='1.0' encoding='utf-8' ?><searchV1 artist=\"%s\" title=\"%s\" OnlyMatched=\"1\" %s/>";

    private static final String searchQueryPage = " RequestPage='%d'";

    private static final byte[] magickey = "Mlv1clt4.0".getBytes();

	/*
     * Search function
    	 */

    @Reflection
    public static Lyrics fromURL(String url, String artist, String title){
        // TODO: support ViewLyrics URL
        return new Lyrics(NO_RESULT);
    }

    public static Lyrics fromMetaData(String artist, String title) throws IOException, NoSuchAlgorithmException, SAXException, ParserConfigurationException {
        ArrayList<Lyrics> results =
                search(
                        String.format(searchQueryBase, artist, title, clientTag +
                                String.format(searchQueryPage, 0)) // Create XMLQuery String
                );
        if (results.size() == 0)
            return new Lyrics(NEGATIVE_RESULT);

        int lrcIndex = 0;
        boolean lrcLyricFound = false;

        for(int i=0;i<results.size();i++){
            if(results.get(i).getURL().endsWith("lrc")){
                lrcLyricFound=true;
                lrcIndex=i;
                break;
            }
        }


        lrcIndex = 0;

        for(int i=0;i<results.size();i++) {
            Log.v("ViewLyrics", "Lyrics " + i + " : " + results.get(i).getOriginalTrack() + " : " + results.get(i).getOriginalTrack());
            Log.v("ViewLyrics", "Lyrics " + i + " : " + results.get(i).getURL());
        }

        Handler handler = new Handler(Looper.getMainLooper());

        /*
        handler.post(new Runnable() {
            @Override
            public void run() {

                new MaterialDialog.Builder(MyApp.getContext())
                        .title("title").show();
            }
        });*/

        String url;
        String foundTitle;
        String foundArtist;
        if(lrcLyricFound) {
            url = results.get(lrcIndex).getURL();
            foundTitle = results.get(lrcIndex).getTrack();
            foundArtist = results.get(lrcIndex).getArtist();
        }else {
            url = results.get(0).getURL();
            foundTitle = results.get(0).getTrack();
            foundArtist = results.get(0).getArtist();
        }
        url = url.replace("minilyrics", "viewlyrics");

        int artistDistance = Levenshtein.distance(results.get(0).getOriginalArtist(), artist);
        int titleDistance = Levenshtein.distance(results.get(0).getTrack(), title);


        Lyrics result = new Lyrics(POSITIVE_RESULT);
        result.setTitle(foundTitle);
        result.setArtist(foundArtist);
        result.setOriginalArtist(artist);
        result.setOriginalTitle(title);
        result.setSource(clientUserAgent);

        String[] arr = Net.getUrlAsString(url).replaceAll("(\\[(?=.[a-z]).+\\]|<.+?>|www.*[\\s])", "").replaceAll("[\n]\\[(.*?)\\]+[\\s]","").split("\n");
        String output = "";

        if (url.endsWith("txt") /*|| artistDistance > 6 || titleDistance > 6*/) {

            for(String line : arr) {
                output = output.concat(line+"\n");
            }
            result.setText(output.replace("\n","<br />"));
            return result;
        }

        result.setLRC(url.endsWith("lrc"));
        for(String line : arr){
            String word = line.replaceAll("[^A-Za-z\\s]","");
            String newLine = line.replaceAll("\\]\\[","\\]"+word+"\n\\[");
            output = output.concat(newLine);
        }
        result.setText(output.replaceAll("\\[", "\n\\[ "));
        return result;
    }


    //for giving choice to select lyrics from available ones
    public static void fromMetaData(final Context context, final String artist, final String title, final TrackItem item, final Lyrics.Callback callback)
            throws IOException, NoSuchAlgorithmException, SAXException, ParserConfigurationException {


        Log.d("ActivityInstantLyric", "onLyricsDownloaded: in view lyric thread now");

        final Handler handler = new Handler(Looper.getMainLooper());

        final MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                .typeface(TypeFaceHelper.getTypeFace(context),TypeFaceHelper.getTypeFace(context));

        builder.title("Searching for other lyrics...");
        builder.progress(true, 0);

        //all this bullshit because of inner class shit
        //and the fact that android do not allow changing UI from non UI thread, duh
        final MaterialDialog[] dialog = new MaterialDialog[1];

        handler.post(new Runnable() {
            @Override
            public void run() {
                dialog[0] = builder.build();
                dialog[0].show();
            }
        });

        final ArrayList<Lyrics> results =
                search(
                        String.format(searchQueryBase, artist, title, clientTag +
                                String.format(searchQueryPage, 0)) // Create XMLQuery String
                );
        if (results.size() == 0) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    dialog[0].dismiss();
                    Toast.makeText(context, "No lyrics found for track!", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        for(int i=0;i<results.size();i++) {
            //Log.v("ViewLyrics", "Lyrics " + i + " : " + results.get(i).getOriginalTrack() + " : " + results.get(i).getOriginalTrack());
            Log.v("ViewLyrics", "Lyrics " + i + " : " + results.get(i).getURL());
        }

        Log.d("ActivityInstantLyric", "onLyricsDownloaded: found lyrics count "  + results.size());

        //show dialog with available options
        final ArrayList<String> resultTrackTitles = new ArrayList<>();
        for(Lyrics lyric:results){
            if(lyric.getURL().endsWith("lrc")) {
                resultTrackTitles.add(lyric.getTrack() + " : " + lyric.getArtist() + " (Running lyrics) ");
            }else {
                resultTrackTitles.add(lyric.getTrack() + " : " + lyric.getArtist());
            }
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                dialog[0].dismiss();
                builder
                        .title("Choose any one")
                        .items(resultTrackTitles)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {

                                Log.d("ActivityInstantLyric", "onLyricsDownloaded: clicked on " + text);

                                Executors.newSingleThreadExecutor().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        String url = results.get(which).getURL();
                                        String foundTitle = results.get(which).getTrack();
                                        String foundArtist = results.get(which).getArtist();


                                        url = url.replace("minilyrics", "viewlyrics");

                                        final Lyrics result = new Lyrics(POSITIVE_RESULT);
                                        result.setTitle(foundTitle);
                                        result.setArtist(foundArtist);
                                        result.setOriginalArtist(artist);
                                        result.setOriginalTitle(title);
                                        result.setSource(clientUserAgent);

                                        String[] arr = new String[0];
                                        try {
                                            arr = Net.getUrlAsString(url).replaceAll("(\\[(?=.[a-z]).+\\]|<.+?>|www.*[\\s])", "").replaceAll("[\n]\\[(.*?)\\]+[\\s]","").split("\n");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        String output = "";

                                        Log.d("ActivityInstantLyric", "onLyricsDownloaded: lyrics type " + (url.endsWith("lrc") ? "lrc" : "txt"));

                                        if (url.endsWith("txt") /*|| artistDistance > 6 || titleDistance > 6*/) {
                                            for(String line : arr) {
                                                output = output.concat(line+"\n");
                                            }
                                            result.setText(output.replace("\n","<br />"));
                                            //return result;
                                        }else {
                                            result.setLRC(url.endsWith("lrc"));
                                            for(String line : arr){
                                                String word = line.replaceAll("[^A-Za-z\\s]","");
                                                String newLine = line.replaceAll("\\]\\[","\\]"+word+"\n\\[");
                                                output = output.concat(newLine);
                                            }

                                            // Log.v(Constants.L_TAG+"chavan 0.5",output.replaceAll("\\[", "\n\\[ "));
                                            result.setText(output.replaceAll("\\[", "\n\\[ "));

                                        }

                                        if(item!=null && result.getFlag()==Lyrics.POSITIVE_RESULT) {
                                            // Log.v(Constants.L_TAG,lyrics.getText() );
                                            OfflineStorageLyrics.clearLyricsFromDB(item);
                                            OfflineStorageLyrics.putLyricsInDB(result, item);
                                        }

                                        //if from instant lyric activity, delete current lyrics from cache
                                        if(result.getFlag()==POSITIVE_RESULT){
                                            OfflineStorageLyrics.clearLyricsFromCache(result);
                                        }

                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                callback.onLyricsDownloaded(result);
                                            }
                                        });
                                    }
                                });
                            }
                        })
                        .show();
            }
        });
    }

    private static ArrayList<Lyrics> search(String searchQuery) throws IOException, ParserConfigurationException, SAXException, NoSuchAlgorithmException {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);

        RequestBody body = RequestBody.create(MediaType.parse("application/text"), assembleQuery(searchQuery.getBytes("UTF-8")));

        Request request = new Request.Builder()
                .header("User-Agent", clientUserAgent)
                .post(body)
                .url(url)
                .build();

        Response response = client.newCall(request).execute();

        BufferedReader rd = new BufferedReader
                (new InputStreamReader(response.body().byteStream(), "ISO_8859_1"));

        // Get full result
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[8192];
        int read;
        while ((read = rd.read(buffer, 0, buffer.length)) > 0) {
            builder.append(buffer, 0, read);
        }
        String full = builder.toString();

        // Decrypt, parse, store, and return the result list
        return parseResultXML(decryptResultXML(full));
    }

	/*
     * Add MD5 and Encrypts Search Query
	 */

    public static byte[] assembleQuery(byte[] valueBytes) throws NoSuchAlgorithmException, IOException {
        // Create the variable POG to be used in a dirt code
        byte[] pog = new byte[valueBytes.length + magickey.length]; //TODO Give a better name then POG

        // POG = XMLQuery + Magic Key
        System.arraycopy(valueBytes, 0, pog, 0, valueBytes.length);
        System.arraycopy(magickey, 0, pog, valueBytes.length, magickey.length);

        // POG is hashed using MD5
        byte[] pog_md5 = MessageDigest.getInstance("MD5").digest(pog);

        //TODO Thing about using encryption or k as 0...
        // Prepare encryption key
        int j = 0;
        for (byte octet : valueBytes) {
            j += octet;
        }
        int k = (byte) (j / valueBytes.length);

        // Value is encrypted
        for (int m = 0; m < valueBytes.length; m++)
            valueBytes[m] = (byte) (k ^ valueBytes[m]);

        // Prepare result code
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        // Write Header
        result.write(0x02);
        result.write(k);
        result.write(0x04);
        result.write(0x00);
        result.write(0x00);
        result.write(0x00);

        // Write Generated MD5 of POG problaby to be used in a search cache
        result.write(pog_md5);

        // Write encrypted value
        result.write(valueBytes);

        // Return magic encoded query
        return result.toByteArray();
    }

	/*
     * Decrypts only the XML from the entire result
	 */

    public static String decryptResultXML(String value) {
        // Get Magic key value
        char magickey = value.charAt(1);

        // Prepare output
        ByteArrayOutputStream neomagic = new ByteArrayOutputStream();

        // Decrypts only the XML
        for (int i = 22; i < value.length(); i++)
            neomagic.write((byte) (value.charAt(i) ^ magickey));

        // Return value
        return neomagic.toString();
    }

	/*
	 * Create the ArrayList<LyricInfo>
	 */

    private static String readStrFromAttr(Element elem, String attr, String def) {
        String data = elem.getAttribute(attr);
        try {
            if (data != null && data.length() > 0)
                return data;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return def;
    }

    public static ArrayList<Lyrics> parseResultXML(String resultXML) throws SAXException, IOException, ParserConfigurationException {
        // Create array for storing the results
        ArrayList<Lyrics> availableLyrics = new ArrayList<>();

        // Parse XML
        ByteArrayInputStream resultBA = new ByteArrayInputStream(resultXML.getBytes("UTF-8"));
        Element resultRootElem = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resultBA).getDocumentElement();

        String server_url = readStrFromAttr(resultRootElem, "server_url", "http://www.viewlyrics.com/");

        NodeList resultItemList = resultRootElem.getElementsByTagName("fileinfo");
        for (int i = 0; i < resultItemList.getLength(); i++) {
            Element itemElem = (Element) resultItemList.item(i);
            Lyrics item = new Lyrics(SEARCH_ITEM);

            item.setURL(server_url + readStrFromAttr(itemElem, "link", ""));
            item.setArtist(readStrFromAttr(itemElem, "artist", ""));
            item.setTitle(readStrFromAttr(itemElem, "title", ""));
            //item.setLyricsFileName(readStrFromAttr(itemElem, "filename", ""));
            //itemInfo.setFType(readIntFromAttr(itemElem, "file_type", 0));
            //itemInfo.setMatchVal(readFloatFromAttr(itemElem, "match_value", 0.0F));
            //itemInfo.setTimeLenght(readIntFromAttr(itemElem, "timelength", 0));


            availableLyrics.add(item);
        }

        return availableLyrics;
    }


}
