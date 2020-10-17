package com.music.player.bhandari.m.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.music.player.bhandari.m.R;
import com.music.player.bhandari.m.UIElementHelper.TypeFaceHelper;
import com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo.lyrics.Lyrics;
import com.music.player.bhandari.m.MyApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class LyricsViewAdapter extends RecyclerView.Adapter<LyricsViewAdapter.MyViewHolder> {

    private String copyRightText;
    private Context context;

    private LayoutInflater inflater;
    private Lyrics mLyrics;

    private TreeMap<Long, String> dictionary = new TreeMap<>();

    private long mCurrentTime = 0L;
    private long mNextTime = 0L;
    private long mPrevTime = 0L;
    private List<Long> mTimes = new ArrayList<>();

    private SparseBooleanArray selectedItems;

    //flag is used when lyric are searched through explre feature
    private Boolean noDynamicLyrics = false;

    public LyricsViewAdapter(Context context, Lyrics lyrics){
        this.context = context;
        copyRightText = context.getString(R.string.lyric_copy_right_msg);
        inflater= LayoutInflater.from(context);
        mLyrics = lyrics;
        dictionary.clear();


        //ignore null pointer expcetion
        //happens when service is not started and instant lyric started
        try {
            mCurrentTime = MyApp.getService().getCurrentTrackProgress();
        }catch (NullPointerException ignored){}

        mNextTime = 0L;
        mPrevTime = 0L;
        mTimes.clear();
        selectedItems = new SparseBooleanArray();

        if(mLyrics.isLRC()) {
            setDynamicDictionary();
        }else {
            setStaticDictionary();
        }

        setHasStableIds(true);

    }

    public void setNoDynamicLyrics(Boolean noDynamicLyrics){
        this.noDynamicLyrics = noDynamicLyrics;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public LyricsViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        //calligraphy problem in kitkat
        //with text view having background
        try {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                view = inflater.inflate(R.layout.lyrics_line_text_view_kitkat, parent, false);
            } else {
                view = inflater.inflate(R.layout.lyrics_line_text_view, parent, false);
            }
        }catch (InflateException e){
            TextView textView = new TextView(context);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(param);
            textView.setTextSize(20);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTypeface(TypeFaceHelper.getTypeFace(context));
            textView.setClickable(true);
            textView.setPadding(10,10,10,10);
            textView.setId(R.id.lyrics_line);
            return new LyricsViewAdapter.MyViewHolder(textView);
        }
        return new LyricsViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LyricsViewAdapter.MyViewHolder holder, int position) {
        String line = "";

        line = dictionary.get(mTimes.get(position));
        if(line!=null) {
            holder.line.setText(line);
            Log.d("LyricsViewAdapter", "onBindViewHolder: current time " + mCurrentTime);
            //when lyrics are searched through explore, no need of running lyrics
            if(mLyrics.isLRC() && !noDynamicLyrics) {
                int color = mTimes.get(position) <= mCurrentTime ? Color.YELLOW : Color.WHITE;
                holder.line.setTextColor(color);
                Log.d("LyricsViewAdapter", "onBindViewHolder: setting color " + color);
            }

        }else {
            holder.line.setText("");
        }

        //last item is copyright text
        if(position==mTimes.size()-1){
            holder.line.setTextSize(10);
        }else {
            holder.line.setTextSize(22);
        }

        //holder.line.setTypeface(TypeFaceHelper.getTypeFace(context));
        holder.itemView.setActivated(selectedItems.get(position, false));
    }

    @Override
    public int getItemCount() {
            return mTimes.size();
    }

    public String getLineAtPosition(int position){
        return dictionary.get(mTimes.get(position));
    }

    private void setStaticDictionary(){
        long i = 0;
        try {
            BufferedReader br = new BufferedReader (new StringReader(Html.fromHtml(mLyrics.getText()).toString()));
            String str;
            while ((str = br.readLine()) != null) {
                //lyricLines.add(str);
                dictionary.put(i, str);
                mTimes.add(i);
                i++;
            }

            //put last element as lyrics copyright text
            if(mTimes.size()!=0) {
                dictionary.put(i,copyRightText);
                mTimes.add(i);
            }
            br.close();
        } catch (Exception ignored) {
        }
    }

    private void setDynamicDictionary() {
        if(mLyrics==null){
            return;
        }

        mNextTime = 0;

        List<String> texts = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new StringReader(mLyrics.getText()));

        String line;
        String[] arr;
        try {
            while (null != (line = reader.readLine())) {

                arr = parseLine(line);
                if (null == arr) {
                    continue;
                }


                if ((1 == arr.length) && texts.size()!=0) {
                    String last = texts.remove(texts.size() - 1);
                    texts.add(last + arr[0]);
                    continue;
                }

                for (int i = 0; i < arr.length - 1; i++) {
                    mTimes.add(Long.parseLong(arr[i]));
                    texts.add(arr[arr.length - 1]);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Collections.sort(mTimes);
        for (int i = 0; i < mTimes.size(); i++) {

                if (!(dictionary.isEmpty() && texts.get(i).replaceAll("\\s", "").isEmpty())) {
                    // Log.v(Constants.L_TAG+" chavan",texts.get(i));
                    dictionary.put(mTimes.get(i), texts.get(i));
                }
        }

        Collections.sort(mTimes);

        //put last element as lyrics copyright text
        if(mTimes.size()!=0) {
            dictionary.put(mTimes.get(mTimes.size() - 1) + 500, copyRightText);
            mTimes.add(mTimes.get(mTimes.size() - 1) + 500);
        }
    }

    private String[] parseLine(String line) {
        Matcher matcher = Pattern.compile("\\[.+\\].+").matcher(line);

        /*if (!matcher.matches() || line.contains("By:")) {
            if (line.contains("[by:") && line.length() > 6)
                this.uploader = line.substring(5, line.length() - 1);
            return null;
        }*/

        if (line.endsWith("]"))
            line += " ";
        line = line.replaceAll("\\[", "");
        String[] result = line.split("\\]");
        try {
            for (int i = 0; i < result.length - 1; ++i)
                result[i] = String.valueOf(parseTime(result[i]));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            return null;
        }

        return result;
    }

    private Long parseTime(String time) {
        String[] min = time.split(":");
        String[] sec;
        if (!min[1].contains("."))
            min[1] += ".00";
        sec = min[1].split("\\.");

        long minInt = Long.parseLong(min[0].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());
        long secInt = Long.parseLong(sec[0].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());
        long milInt = Long.parseLong(sec[1].replaceAll("\\D+", "")
                .replaceAll("\r", "").replaceAll("\n", "").trim());

        return minInt * 60 * 1000 + secInt * 1000 + milInt * 10;
    }

    public synchronized int changeCurrent(long time) {
        if (dictionary == null || dictionary.isEmpty()) {
            return -1;
        }

        mPrevTime = mCurrentTime;
        mNextTime = dictionary.lastKey();
        if (time < mNextTime)
            mNextTime = dictionary.higherKey(time);
        mCurrentTime = dictionary.firstKey();
        if (time > mCurrentTime)
            mCurrentTime = dictionary.floorKey(time);

        if (mCurrentTime != mPrevTime && mPrevTime != 0) {
            int index = mTimes.indexOf(mCurrentTime);
            notifyItemChanged(index);
            return index;
        }

        return -1;

    }

    public int getCurrentTimeIndex(){
        if(mTimes==null){
            return -1;
        }
        return mTimes.indexOf(dictionary.floorKey(mCurrentTime));
    }

    public String getStaticLyrics() {
        StringBuilder text = new StringBuilder();
        Iterator<String> iterator = dictionary.values().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (text.length() == 0 && next.replaceAll("\\s", "").isEmpty())
                continue;
            text.append(next);
            if (iterator.hasNext())
                text.append("<br/>\n");
        }
        return text.toString();
    }

    //methods for item selection
    public void toggleSelection(int pos) {

        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        }
        else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView line;

        MyViewHolder(View itemView) {
            super(itemView);
            line = itemView.findViewById(R.id.lyrics_line);
        }
    }
}
