package com.music.player.bhandari.m.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

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

public class SignUp extends AsyncTask<String, String, String> {

    private String name="";
    private String response = "unexpected-error";

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        String email = params[0];
        if(params[1]!=null) {
            name = params[1];
        }

        String urlString = "https://www.thetechguru.in/?es=subscribe";

        String queryPart1 = "es_email=" + email;

        String queryPart2 = "&es_name=" + name;

        String query = queryPart1 + queryPart2 + "&es_group=abmusic&timestamp=&action=0.597592245452881&es_from=abmusic";

        String resultToDisplay = "";

        try {

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            //Set to POST
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.addRequestProperty("REFERER", "https://thetechguru.in");
            connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            connection.setReadTimeout(10000);
            Writer writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(query);
            writer.flush();
            writer.close();

            response = readResponseFullyAsString(connection.getInputStream(),"UTF-8");
            //processResponse(response);

            Log.v("Response",response);

        } catch (Exception e) {

            System.out.println(e.getMessage());

            return e.getMessage();

        }
        return resultToDisplay;

    }

    private String readResponseFullyAsString(InputStream inputStream, String encoding) throws IOException {
        return readFully(inputStream).toString(encoding);
    }

    private ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos;
    }

    @Override
    protected void onPostExecute(String result) {
        switch (response){
            case "subscribed-successfully":
                break;

            case "already-exist":
            case "unexpected-error":
            case "subscribed-pending-doubleoptin":
                break;
        }

    }
}