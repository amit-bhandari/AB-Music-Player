package com.music.player.bhandari.m.utils

import android.os.AsyncTask
import android.util.Log
import kotlin.Throws
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

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
class SignUp : AsyncTask<String?, String?, String?>() {
    private var name = ""
    private var response = "unexpected-error"

     override fun doInBackground(vararg params: String?): String? {
        val email = params[0]
        if (params[1] != null) {
            name = params[1]!!
        }
        val urlString =
            "https://thetechguru.in/wp-admin/admin-ajax.php?action=es_add_subscriber&es=subscribe"
        val queryPart1 = "esfpx_es_txt_email=$email"
        val queryPart2 = "&esfpx_es_txt_name=$name"
        val query =
            "$queryPart1$queryPart2&esfpx_es_txt_group=abmusic&esfpx_es-subscribe=ce72472f59"
        val resultToDisplay = ""
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            //Set to POST
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.doInput = true
            connection.addRequestProperty("REFERER", "https://thetechguru.in")
            connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded")
            connection.readTimeout = 10000
            val writer: Writer = OutputStreamWriter(connection.outputStream)
            writer.write(query)
            writer.flush()
            writer.close()
            response = readResponseFullyAsString(connection.inputStream, "UTF-8")
            //processResponse(response);
            Log.v("Response", response)
        } catch (e: Exception) {
            println(e.message)
            return e.message
        }
        return resultToDisplay
    }

    @Throws(IOException::class)
    private fun readResponseFullyAsString(inputStream: InputStream, encoding: String): String {
        return readFully(inputStream).toString(encoding)
    }

    @Throws(IOException::class)
    private fun readFully(inputStream: InputStream): ByteArrayOutputStream {
        val baos = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length = 0
        while (inputStream.read(buffer).also { length = it } != -1) {
            baos.write(buffer, 0, length)
        }
        return baos
    }

    override fun onPostExecute(result: String?) {
        when (response) {
            "subscribed-successfully" -> {}
            "already-exist", "unexpected-error", "subscribed-pending-doubleoptin" -> {}
        }
    }
}