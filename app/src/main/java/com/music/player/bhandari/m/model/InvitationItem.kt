package com.music.player.bhandari.m.model

import com.music.player.bhandari.m.utils.UtilityFun.escapeDoubleQuotes
import java.lang.NumberFormatException
import android.media.MediaMetadataRetriever
import android.util.Log
import android.content.ContentResolver
import java.util.concurrent.atomic.AtomicInteger
import java.util.ArrayList
import java.util.Collections
import java.util.LinkedHashMap
import android.util.SparseArray
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import java.lang.Runnable
import java.lang.InterruptedException
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.Intent
import java.lang.Exception
import android.net.Uri
import android.provider.MediaStore
import android.database.Cursor
import com.music.player.bhandari.m.utils.UtilityFun
import android.graphics.Bitmap
import android.content.ContentUris
import android.os.ParcelFileDescriptor
import java.io.FileDescriptor
import android.graphics.BitmapFactory
import java.io.File
import androidx.annotation.RequiresApi
import android.os.Build
import java.io.IOException
import kotlin.jvm.Synchronized
import java.util.HashMap
import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues
import android.os.Looper
import android.widget.Toast
import android.annotation.SuppressLint
import android.view.Gravity
import java.util.ConcurrentModificationException
import java.util.Comparator
import android.database.DatabaseUtils
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet

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
class InvitationItem {
    var invitationId = ""
    var invitationAccepted = false

    constructor()
    constructor(invitationId: String, invitationAccepted: Boolean) {
        this.invitationId = invitationId
        this.invitationAccepted = invitationAccepted
    }
}