package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo

import android.util.Log
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * This file is part of QuickLyric
 * Created by geecko
 *
 *
 * QuickLyric is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * QuickLyric is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with QuickLyric.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
object Net {
    var USER_AGENT: String =
        "Mozilla/5.0 (Linux; U; Android 6.0.1; ko-kr; Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"

    @Throws(IOException::class)
    fun getUrlAsString(paramURL: String?): String {
        return getUrlAsString(URL(paramURL))
    }

    @Throws(IOException::class)
    fun getUrlAsString(paramURL: URL?): String {
        val request: Request =
            Request.Builder().header("User-Agent", USER_AGENT).url(paramURL).build()
        val client: OkHttpClient = OkHttpClient()
        client.setConnectTimeout(10, TimeUnit.SECONDS)
        val response: Response = client.newCall(request).execute()
        val string: String = response.body().toString()
        Log.d("Lyrics", "getUrlAsString: $string")
        return response.body().string()
    }
}