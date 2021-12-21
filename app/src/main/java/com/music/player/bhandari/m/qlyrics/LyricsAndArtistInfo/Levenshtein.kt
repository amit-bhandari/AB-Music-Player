/*
 * *
 *  * This file is part of QuickLyric
 *  * Created by geecko
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
package com.music.player.bhandari.m.qlyrics.LyricsAndArtistInfo

import java.util.*
import kotlin.math.min

object Levenshtein {
    fun distance(a: String, b: String): Int {
        var a: String = a
        var b: String = b
        a = a.toLowerCase(Locale.US)
        b = b.toLowerCase(Locale.US)
        // i == 0
        val costs: IntArray = IntArray(b.length + 1)
        for (j in costs.indices) costs[j] = j
        for (i in 1..a.length) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i
            var nw: Int = i - 1
            for (j in 1..b.length) {
                val cj: Int = min(1 + min(costs[j], costs[j - 1]),
                    if (a[i - 1] == b[j - 1]) nw else nw + 1)
                nw = costs[j]
                costs[j] = cj
            }
        }
        return costs.get(b.length)
    }
}