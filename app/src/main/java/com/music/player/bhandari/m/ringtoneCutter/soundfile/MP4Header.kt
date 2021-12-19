/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.music.player.bhandari.m.ringtoneCutter.soundfile

import com.music.player.bhandari.m.utils.UtilityFun.isAdsRemoved
import com.music.player.bhandari.m.utils.AppLaunchCountManager.isEligibleForRatingAsk
import com.music.player.bhandari.m.utils.UtilityFun.logEvent
import com.music.player.bhandari.m.utils.UtilityFun.LaunchYoutube
import com.music.player.bhandari.m.utils.UtilityFun.defaultAlbumArtDrawable
import com.music.player.bhandari.m.utils.UtilityFun.msToString
import com.music.player.bhandari.m.utils.UtilityFun.SetRingtone
import com.music.player.bhandari.m.utils.UtilityFun.trackInfoBuild
import com.music.player.bhandari.m.utils.UtilityFun.AddToPlaylist
import com.music.player.bhandari.m.utils.UtilityFun.Share
import com.music.player.bhandari.m.utils.UtilityFun.ShareFromPath
import com.music.player.bhandari.m.utils.UtilityFun.Delete
import com.music.player.bhandari.m.model.TrackItem.getFilePath
import com.music.player.bhandari.m.model.TrackItem.id
import com.music.player.bhandari.m.utils.UtilityFun.screenWidth
import com.music.player.bhandari.m.utils.UtilityFun.restartApp
import com.music.player.bhandari.m.model.TrackItem.title
import com.music.player.bhandari.m.utils.UtilityFun.isConnectedToInternet
import com.music.player.bhandari.m.utils.UtilityFun.filterArtistString
import com.music.player.bhandari.m.model.TrackItem.setArtist
import com.music.player.bhandari.m.model.TrackItem.artist_id
import com.music.player.bhandari.m.model.TrackItem.getArtist
import com.music.player.bhandari.m.utils.UtilityFun.isBluetoothHeadsetConnected
import com.music.player.bhandari.m.utils.UtilityFun.drawableToBitmap
import com.music.player.bhandari.m.model.TrackItem.album
import com.music.player.bhandari.m.model.TrackItem.durInt
import com.music.player.bhandari.m.model.TrackItem.genre
import com.music.player.bhandari.m.utils.AppLaunchCountManager.app_launched
import com.music.player.bhandari.m.utils.UtilityFun.progressToTimer
import io.github.inflationx.viewpump.ViewPump.Builder.addInterceptor
import io.github.inflationx.viewpump.ViewPump.Builder.build
import com.music.player.bhandari.m.model.TrackItem.albumId
import com.music.player.bhandari.m.utils.UtilityFun.decodeUri
import com.music.player.bhandari.m.utils.AppLaunchCountManager.nowPlayingLaunched
import com.music.player.bhandari.m.utils.UtilityFun.getProgressPercentage
import com.music.player.bhandari.m.utils.AppLaunchCountManager.instantLyricsLaunched
import com.music.player.bhandari.m.lyricsExplore.PopularTrackRepo.fetchPopularTracks
import com.music.player.bhandari.m.utils.UtilityFun.getRandom
import java.lang.Thread
import java.lang.Runnable
import com.music.player.bhandari.m.MyApp
import com.music.player.bhandari.m.R
import android.util.Log
import org.json.JSONObject
import java.lang.Exception
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.music.player.bhandari.m.utils.UtilityFun
import com.music.player.bhandari.m.utils.AppLaunchCountManager
import android.os.Looper
import android.os.AsyncTask
import java.lang.Void
import android.graphics.Bitmap
import java.io.InputStream
import java.net.URL
import java.net.HttpURLConnection
import android.graphics.BitmapFactory
import java.net.MalformedURLException
import java.io.IOException
import android.annotation.TargetApi
import android.os.Build
import android.app.NotificationManager
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import android.content.Intent
import android.app.PendingIntent
import android.net.Uri
import android.app.IntentService
import android.content.ActivityNotFoundException
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.MenuInflater
import android.app.Activity
import android.view.MenuItem
import butterknife.BindView
import android.widget.TextView
import android.widget.ImageView
import butterknife.ButterKnife
import java.util.TreeMap
import java.util.ArrayList
import android.util.SparseBooleanArray
import android.view.InflateException
import android.widget.LinearLayout
import android.view.Gravity
import java.io.BufferedReader
import java.io.StringReader
import android.text.Html
import java.util.Collections
import java.lang.NumberFormatException
import java.lang.ArrayIndexOutOfBoundsException
import kotlin.jvm.Synchronized
import java.lang.StringBuilder
import java.lang.NullPointerException
import com.music.player.bhandari.m.model.dataItem
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView.SectionedAdapter
import android.graphics.drawable.Drawable
import com.music.player.bhandari.m.model.MusicLibrary
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.snackbar.Snackbar
import java.util.Comparator
import java.io.File
import androidx.core.content.FileProvider
import java.lang.IllegalArgumentException
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import java.util.LinkedHashMap
import android.os.Parcelable
import android.os.Environment
import android.widget.Toast
import android.annotation.SuppressLint
import java.io.FilenameFilter
import java.util.Arrays
import com.music.player.bhandari.m.model.PlaylistManager
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.View.OnTouchListener
import android.view.MotionEvent
import androidx.core.view.MotionEventCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.wang.avi.AVLoadingIndicatorView
import android.widget.ImageButton
import com.google.gson.JsonObject
import org.jsoup.Jsoup
import com.google.gson.JsonParser
import com.google.gson.JsonArray
import java.net.URLEncoder
import com.google.gson.JsonSyntaxException
import org.jsoup.select.Elements
import java.lang.StringIndexOutOfBoundsException
import org.jsoup.safety.Whitelist
import org.jsoup.HttpStatusException
import android.os.Parcel
import kotlin.Throws
import java.io.ByteArrayOutputStream
import java.io.ObjectOutput
import java.io.ObjectOutputStream
import android.os.Parcelable.Creator
import java.lang.ClassNotFoundException
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.util.Locale
import com.google.gson.JsonParseException
import java.net.URLDecoder
import java.lang.IllegalStateException
import org.jsoup.nodes.Document.OutputSettings
import java.lang.IndexOutOfBoundsException
import java.io.UnsupportedEncodingException
import java.security.NoSuchAlgorithmException
import org.xml.sax.SAXException
import javax.xml.parsers.ParserConfigurationException
import java.io.InputStreamReader
import java.security.MessageDigest
import javax.xml.parsers.DocumentBuilderFactory
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import android.database.Cursor
import com.google.gson.Gson
import android.content.ContentValues
import java.io.FileOutputStream
import java.io.FileInputStream
import java.util.HashMap
import java.util.ConcurrentModificationException
import android.media.AudioManager.OnAudioFocusChangeListener
import com.squareup.seismic.ShakeDetector
import android.media.AudioManager
import android.media.MediaPlayer
import android.content.IntentFilter
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.telephony.PhoneStateListener
import android.content.BroadcastReceiver
import android.hardware.SensorManager
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.telephony.TelephonyManager
import android.bluetooth.BluetoothAdapter
import java.lang.UnsupportedOperationException
import android.media.audiofx.PresetReverb
import android.appwidget.AppWidgetManager
import android.widget.RemoteViews
import android.content.ComponentName
import com.music.player.bhandari.m.widget.WidgetReceiver
import android.support.v4.media.MediaMetadataCompat
import android.media.MediaMetadata
import java.io.FileNotFoundException
import java.lang.SecurityException
import android.os.Binder
import java.lang.InterruptedException
import android.service.notification.NotificationListenerService
import android.media.RemoteController.OnClientUpdateListener
import android.media.RemoteController
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import java.lang.RuntimeException
import android.media.session.PlaybackState
import android.media.RemoteControlClient
import android.media.MediaMetadataRetriever
import android.service.notification.StatusBarNotification
import android.content.ContentResolver
import android.text.TextUtils
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.SeekBar
import android.widget.ProgressBar
import android.content.AsyncQueryHandler
import android.provider.MediaStore
import android.view.Window
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.view.KeyEvent
import java.lang.ref.WeakReference
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.gms.common.api.GoogleApiClient
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.EditText
import android.view.inputmethod.InputMethodManager
import androidx.drawerlayout.widget.DrawerLayout
import java.util.StringTokenizer
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.tabs.TabLayout
import android.content.res.ColorStateList
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.Auth
import com.google.android.material.internal.NavigationMenuView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.content.DialogInterface
import com.getkeepsafe.taptargetview.TapTargetView
import com.getkeepsafe.taptargetview.TapTarget
import com.google.android.material.appbar.AppBarLayout
import androidx.core.view.GravityCompat
import android.view.Menu
import android.media.audiofx.AudioEffect
import android.text.TextWatcher
import android.text.Editable
import com.music.player.bhandari.m.activity.ActivitySavedLyrics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.RatingBar
import com.google.android.gms.common.api.OptionalPendingResult
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import android.app.ActivityOptions
import androidx.core.app.ActivityCompat
import android.view.animation.Animation
import android.text.InputType
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.music.player.bhandari.m.utils.SignUp
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.ConnectionResult
import androidx.fragment.app.FragmentPagerAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import androidx.appcompat.widget.AppCompatButton
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.music.player.bhandari.m.model.InvitationItem
import com.google.gson.reflect.TypeToken
import butterknife.OnClick
import com.google.android.gms.appinvite.AppInviteInvitation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import android.widget.Button
import androidx.core.view.GestureDetectorCompat
import com.nshmura.snappysmoothscroller.SnappyLayoutManager
import com.nshmura.snappysmoothscroller.SnappyLinearLayoutManager
import com.nshmura.snappysmoothscroller.SnapType
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import android.view.GestureDetector.SimpleOnGestureListener
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.TextPaint
import android.graphics.Typeface
import android.text.Spanned
import android.text.method.LinkMovementMethod
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.theartofdev.edmodo.cropper.CropImage
import android.preference.PreferenceFragment
import android.preference.CheckBoxPreference
import androidx.recyclerview.widget.ItemTouchHelper
import android.preference.Preference
import android.preference.Preference.OnPreferenceClickListener
import android.preference.Preference.OnPreferenceChangeListener
import com.theartofdev.edmodo.cropper.CropImageView
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.JustifyContent
import android.app.ProgressDialog
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.calligraphy3.CalligraphyConfig
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.engine.GlideException
import android.widget.ScrollView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import android.view.WindowManager
import android.view.animation.ScaleAnimation
import jp.wasabeef.blurry.Blurry
import android.content.ContentUris
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sackcentury.shinebuttonlib.ShineButton
import android.util.DisplayMetrics
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import com.music.player.bhandari.m.transition.MorphMiniToNowPlaying
import com.music.player.bhandari.m.transition.MorphNowPlayingToMini
import com.music.player.bhandari.m.activity.FragmentAlbumArt
import androidx.recyclerview.widget.DividerItemDecoration
import java.lang.OutOfMemoryError
import com.music.player.bhandari.m.trackInfo.TrackInfoActivity
import android.view.inputmethod.EditorInfo
import android.text.method.ScrollingMovementMethod
import com.android.vending.billing.IInAppBillingService
import android.content.ServiceConnection
import android.os.RemoteException
import android.content.IntentSender.SendIntentException
import org.json.JSONException
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.DefaultItemAnimator
import com.music.player.bhandari.m.lyricsExplore.OnPopularTracksReady
import com.music.player.bhandari.m.lyricsExplore.PopularTrackRepo
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.android.gms.tasks.OnCompleteListener
import android.app.NotificationChannel
import android.content.pm.PackageManager
import com.google.android.material.appbar.CollapsingToolbarLayout
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.media.audiofx.BassBoost
import android.media.audiofx.LoudnessEnhancer
import com.getkeepsafe.taptargetview.TapTargetSequence
import java.util.UUID
import androidx.core.provider.FontsContractCompat
import android.os.HandlerThread
import androidx.appcompat.widget.AppCompatEditText
import android.app.Application
import kotlin.jvm.JvmOverloads
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.animation.AnimatorSet
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.annotation.ColorInt
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.ColorDrawable
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import android.view.ViewGroup.MarginLayoutParams
import android.animation.ObjectAnimator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import android.animation.AnimatorListenerAdapter
import android.content.res.TypedArray
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.core.view.ViewCompat
import androidx.annotation.AttrRes
import androidx.appcompat.widget.AppCompatImageView
import android.view.ScaleGestureDetector
import com.music.player.bhandari.m.customViews.ZoomTextView.ScaleListener
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import androidx.appcompat.widget.AppCompatSeekBar
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.Paint
import android.graphics.PorterDuffXfermode
import android.graphics.PorterDuff
import android.widget.TextView.BufferType
import android.text.SpannableStringBuilder
import androidx.annotation.Keep
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import android.view.View.MeasureSpec
import java.nio.ByteBuffer
import java.nio.ShortBuffer
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import java.nio.ByteOrder
import android.media.AudioRecord
import android.media.AudioFormat
import android.media.MediaRecorder
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.StringWriter
import java.io.PrintWriter
import android.media.AudioTrack
import com.music.player.bhandari.m.ringtoneCutter.SamplePlayer.OnCompletionListener
import android.view.GestureDetector
import android.content.res.Resources
import android.graphics.DashPathEffect
import android.app.Dialog
import java.io.RandomAccessFile
import android.media.RingtoneManager
import android.graphics.drawable.GradientDrawable
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

internal class Atom {
    // note: latest versions of spec simply call it 'box' instead of 'atom'.
    private var mSize // includes atom header (8 bytes)
            : Int
    private var mType: Int
    private var mData // an atom can either contain data or children, but not both.
            : ByteArray?
    private var mChildren: Array<Atom?>?
    private var mVersion // if negative, then the atom does not contain version and flags data.
            : Byte
    private var mFlags: Int

    // create an empty atom of the given type.
    constructor(type: String) {
        mSize = 8
        mType = getTypeInt(type)
        mData = null
        mChildren = null
        mVersion = -1
        mFlags = 0
    }

    // create an empty atom of type type, with a given version and flags.
    constructor(type: String, version: Byte, flags: Int) {
        mSize = 12
        mType = getTypeInt(type)
        mData = null
        mChildren = null
        mVersion = version
        mFlags = flags
    }

    // set the size field of the atom based on its content.
    private fun setSize() {
        var size: Int = 8 // type + size
        if (mVersion >= 0) {
            size += 4 // version + flags
        }
        if (mData != null) {
            size += mData!!.size
        } else if (mChildren != null) {
            for (child: Atom? in mChildren!!) {
                size += child!!.getSize()
            }
        }
        mSize = size
    }

    // get the size of the this atom.
    fun getSize(): Int {
        return mSize
    }

    private fun getTypeInt(type_str: String): Int {
        var type: Int = 0
        type = type or ((type_str.get(0)).toByte() shl 24)
        type = type or ((type_str.get(1)).toByte() shl 16)
        type = type or ((type_str.get(2)).toByte() shl 8)
        type = type or (type_str.get(3)).toByte()
        return type
    }

    fun getTypeInt(): Int {
        return mType
    }

    fun getTypeStr(): String {
        var type: String = ""
        type += (((mType shr 24) and 0xFF).toByte()).toChar()
        type += (((mType shr 16) and 0xFF).toByte()).toChar()
        type += (((mType shr 8) and 0xFF).toByte()).toChar()
        type += ((mType and 0xFF).toByte()).toChar()
        return type
    }

    fun setData(data: ByteArray?): Boolean {
        if (mChildren != null || data == null) {
            // TODO(nfaralli): log something here
            return false
        }
        mData = data
        setSize()
        return true
    }

    fun getData(): ByteArray? {
        return mData
    }

    fun addChild(child: Atom?): Boolean {
        if (mData != null || child == null) {
            // TODO(nfaralli): log something here
            return false
        }
        var numChildren: Int = 1
        if (mChildren != null) {
            numChildren += mChildren!!.size
        }
        val children: Array<Atom?> = arrayOfNulls(numChildren)
        if (mChildren != null) {
            System.arraycopy(mChildren, 0, children, 0, mChildren!!.size)
        }
        children.get(numChildren - 1) = child
        mChildren = children
        setSize()
        return true
    }

    // return the child atom of the corresponding type.
    // type can contain grand children: e.g. type = "trak.mdia.minf"
    // return null if the atom does not contain such a child.
    fun getChild(type: String): Atom? {
        if (mChildren == null) {
            return null
        }
        val types: Array<String> = type.split("\\.".toRegex(), 2).toTypedArray()
        for (child: Atom? in mChildren!!) {
            if ((child!!.getTypeStr() == types.get(0))) {
                if (types.size == 1) {
                    return child
                } else {
                    return child.getChild(types.get(1))
                }
            }
        }
        return null
    }

    // return a byte array containing the full content of the atom (including header)
    fun getBytes(): ByteArray {
        val atom_bytes: ByteArray = ByteArray(mSize)
        var offset: Int = 0
        atom_bytes.get(offset++) = ((mSize shr 24) and 0xFF).toByte()
        atom_bytes.get(offset++) = ((mSize shr 16) and 0xFF).toByte()
        atom_bytes.get(offset++) = ((mSize shr 8) and 0xFF).toByte()
        atom_bytes.get(offset++) = (mSize and 0xFF).toByte()
        atom_bytes.get(offset++) = ((mType shr 24) and 0xFF).toByte()
        atom_bytes.get(offset++) = ((mType shr 16) and 0xFF).toByte()
        atom_bytes.get(offset++) = ((mType shr 8) and 0xFF).toByte()
        atom_bytes.get(offset++) = (mType and 0xFF).toByte()
        if (mVersion >= 0) {
            atom_bytes.get(offset++) = mVersion
            atom_bytes.get(offset++) = ((mFlags shr 16) and 0xFF).toByte()
            atom_bytes.get(offset++) = ((mFlags shr 8) and 0xFF).toByte()
            atom_bytes.get(offset++) = (mFlags and 0xFF).toByte()
        }
        if (mData != null) {
            System.arraycopy(mData, 0, atom_bytes, offset, mData!!.size)
        } else if (mChildren != null) {
            var child_bytes: ByteArray
            for (child: Atom? in mChildren!!) {
                child_bytes = child!!.getBytes()
                System.arraycopy(child_bytes, 0, atom_bytes, offset, child_bytes.size)
                offset += child_bytes.size
            }
        }
        return atom_bytes
    }

    // Used for debugging purpose only.
    public override fun toString(): String {
        var str: String = ""
        val atom_bytes: ByteArray = getBytes()
        for (i in atom_bytes.indices) {
            if (i % 8 == 0 && i > 0) {
                str += '\n'
            }
            str += String.format("0x%02X", atom_bytes.get(i))
            if (i < atom_bytes.size - 1) {
                str += ','
                if (i % 8 < 7) {
                    str += ' '
                }
            }
        }
        str += '\n'
        return str
    }
}

class MP4Header constructor(
    sampleRate: Int,
    numChannels: Int,
    frame_size: IntArray?,
    bitrate: Int
) {
    private val mFrameSize // size of each AAC frames, in bytes. First one should be 2.
            : IntArray?
    private var mMaxFrameSize // size of the biggest frame.
            : Int
    private var mTotSize // size of the AAC stream.
            : Int
    private val mBitrate // bitrate used to encode the AAC stream.
            : Int
    private val mTime // time used for 'creation time' and 'modification time' fields.
            : ByteArray
    private val mDurationMS // duration of stream in milliseconds.
            : ByteArray
    private val mNumSamples // number of samples in the stream.
            : ByteArray
    private var mHeader // the complete header.
            : ByteArray?
    private val mSampleRate // sampling frequency in Hz (e.g. 44100).
            : Int
    private val mChannels // number of channels.
            : Int

    fun getMP4Header(): ByteArray? {
        return mHeader
    }

    public override fun toString(): String {
        var str: String = ""
        if (mHeader == null) {
            return str
        }
        val num_32bits_per_lines: Int = 8
        var count: Int = 0
        for (b: Byte in mHeader!!) {
            val break_line: Boolean = count > 0 && count % (num_32bits_per_lines * 4) == 0
            val insert_space: Boolean = (count > 0) && (count % 4 == 0) && !break_line
            if (break_line) {
                str += '\n'
            }
            if (insert_space) {
                str += ' '
            }
            str += String.format("%02X", b)
            count++
        }
        return str
    }

    private fun setHeader() {
        // create the atoms needed to build the header.
        val a_ftyp: Atom = getFTYPAtom()
        val a_moov: Atom = getMOOVAtom()
        val a_mdat: Atom = Atom("mdat") // create an empty atom. The AAC stream data should follow
        // immediately after. The correct size will be set later.

        // set the correct chunk offset in the stco atom.
        val a_stco: Atom? = a_moov.getChild("trak.mdia.minf.stbl.stco")
        if (a_stco == null) {
            mHeader = null
            return
        }
        val data: ByteArray? = a_stco.getData()
        val chunk_offset: Int = a_ftyp.getSize() + a_moov.getSize() + a_mdat.getSize()
        var offset: Int = data!!.size - 4 // here stco should contain only one chunk offset.
        data.get(offset++) = ((chunk_offset shr 24) and 0xFF).toByte()
        data.get(offset++) = ((chunk_offset shr 16) and 0xFF).toByte()
        data.get(offset++) = ((chunk_offset shr 8) and 0xFF).toByte()
        data.get(offset++) = (chunk_offset and 0xFF).toByte()

        // create the header byte array based on the previous atoms.
        val header: ByteArray =
            ByteArray(chunk_offset) // here chunk_offset is also the size of the header
        offset = 0
        for (atom: Atom in arrayOf(a_ftyp, a_moov, a_mdat)) {
            val atom_bytes: ByteArray = atom.getBytes()
            System.arraycopy(atom_bytes, 0, header, offset, atom_bytes.size)
            offset += atom_bytes.size
        }

        //set the correct size of the mdat atom
        val size: Int = 8 + mTotSize
        offset -= 8
        header.get(offset++) = ((size shr 24) and 0xFF).toByte()
        header.get(offset++) = ((size shr 16) and 0xFF).toByte()
        header.get(offset++) = ((size shr 8) and 0xFF).toByte()
        header.get(offset++) = (size and 0xFF).toByte()
        mHeader = header
    }

    private fun getFTYPAtom(): Atom {
        val atom: Atom = Atom("ftyp")
        atom.setData(byteArrayOf(
            'M'.toByte(), '4'.toByte(), 'A'.toByte(), ' '.toByte(),  // Major brand
            0, 0, 0, 0,  // Minor version
            'M'.toByte(), '4'.toByte(), 'A'.toByte(), ' '.toByte(),  // compatible brands
            'm'.toByte(), 'p'.toByte(), '4'.toByte(), '2'.toByte(),
            'i'.toByte(), 's'.toByte(), 'o'.toByte(), 'm'
                .toByte()))
        return atom
    }

    private fun getMOOVAtom(): Atom {
        val atom: Atom = Atom("moov")
        atom.addChild(getMVHDAtom())
        atom.addChild(getTRAKAtom())
        return atom
    }

    private fun getMVHDAtom(): Atom {
        val atom: Atom = Atom("mvhd", 0.toByte(), 0)
        atom.setData(byteArrayOf(
            mTime.get(0),
            mTime.get(1),
            mTime.get(2),
            mTime.get(3),  // creation time.
            mTime.get(0),
            mTime.get(1),
            mTime.get(2),
            mTime.get(3),  // modification time.
            0,
            0,
            0x03,
            0xE8.toByte(),  // timescale = 1000 => duration expressed in ms.
            mDurationMS.get(0),
            mDurationMS.get(1),
            mDurationMS.get(2),
            mDurationMS.get(3),  // duration in ms.
            0,
            1,
            0,
            0,  // rate = 1.0
            1,
            0,  // volume = 1.0
            0,
            0,  // reserved
            0,
            0,
            0,
            0,  // reserved
            0,
            0,
            0,
            0,  // reserved
            0,
            1,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,  // unity matrix
            0,
            0,
            0,
            0,
            0,
            1,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0x40,
            0,
            0,
            0,
            0,
            0,
            0,
            0,  // pre-defined
            0,
            0,
            0,
            0,  // pre-defined
            0,
            0,
            0,
            0,  // pre-defined
            0,
            0,
            0,
            0,  // pre-defined
            0,
            0,
            0,
            0,  // pre-defined
            0,
            0,
            0,
            0,  // pre-defined
            0,
            0,
            0,
            2 // next track ID
        ))
        return atom
    }

    private fun getTRAKAtom(): Atom {
        val atom: Atom = Atom("trak")
        atom.addChild(getTKHDAtom())
        atom.addChild(getMDIAAtom())
        return atom
    }

    private fun getTKHDAtom(): Atom {
        val atom: Atom = Atom("tkhd", 0.toByte(), 0x07) // track enabled, in movie, and in preview.
        atom.setData(byteArrayOf(
            mTime.get(0),
            mTime.get(1),
            mTime.get(2),
            mTime.get(3),  // creation time.
            mTime.get(0),
            mTime.get(1),
            mTime.get(2),
            mTime.get(3),  // modification time.
            0,
            0,
            0,
            1,  // track ID
            0,
            0,
            0,
            0,  // reserved
            mDurationMS.get(0),
            mDurationMS.get(1),
            mDurationMS.get(2),
            mDurationMS.get(3),  // duration in ms.
            0,
            0,
            0,
            0,  // reserved
            0,
            0,
            0,
            0,  // reserved
            0,
            0,  // layer
            0,
            0,  // alternate group
            1,
            0,  // volume = 1.0
            0,
            0,  // reserved
            0,
            1,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,  // unity matrix
            0,
            0,
            0,
            0,
            0,
            1,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0x40,
            0,
            0,
            0,
            0,
            0,
            0,
            0,  // width
            0,
            0,
            0,
            0 // height
        ))
        return atom
    }

    private fun getMDIAAtom(): Atom {
        val atom: Atom = Atom("mdia")
        atom.addChild(getMDHDAtom())
        atom.addChild(getHDLRAtom())
        atom.addChild(getMINFAtom())
        return atom
    }

    private fun getMDHDAtom(): Atom {
        val atom: Atom = Atom("mdhd", 0.toByte(), 0)
        atom.setData(byteArrayOf(
            mTime.get(0),
            mTime.get(1),
            mTime.get(2),
            mTime.get(3),  // creation time.
            mTime.get(0),
            mTime.get(1),
            mTime.get(2),
            mTime.get(3),  // modification time.
            (mSampleRate shr 24).toByte(),
            (mSampleRate shr 16).toByte(),  // timescale = Fs =>
            (mSampleRate shr 8).toByte(),
            (mSampleRate).toByte(),  // duration expressed in samples.
            mNumSamples.get(0),
            mNumSamples.get(1),
            mNumSamples.get(2),
            mNumSamples.get(3),  // duration
            0,
            0,  // languages
            0,
            0 // pre-defined
        ))
        return atom
    }

    private fun getHDLRAtom(): Atom {
        val atom: Atom = Atom("hdlr", 0.toByte(), 0)
        atom.setData(byteArrayOf(
            0,
            0,
            0,
            0,  // pre-defined
            's'.toByte(),
            'o'.toByte(),
            'u'.toByte(),
            'n'.toByte(),  // handler type
            0,
            0,
            0,
            0,  // reserved
            0,
            0,
            0,
            0,  // reserved
            0,
            0,
            0,
            0,  // reserved
            'S'.toByte(),
            'o'.toByte(),
            'u'.toByte(),
            'n'.toByte(),  // name (used only for debugging and inspection purposes).
            'd'.toByte(),
            'H'.toByte(),
            'a'.toByte(),
            'n'.toByte(),
            'd'.toByte(),
            'l'.toByte(),
            'e'.toByte(),
            '\u0000'
                .toByte()))
        return atom
    }

    private fun getMINFAtom(): Atom {
        val atom: Atom = Atom("minf")
        atom.addChild(getSMHDAtom())
        atom.addChild(getDINFAtom())
        atom.addChild(getSTBLAtom())
        return atom
    }

    private fun getSMHDAtom(): Atom {
        val atom: Atom = Atom("smhd", 0.toByte(), 0)
        atom.setData(byteArrayOf(
            0, 0,  // balance (center)
            0, 0 // reserved
        ))
        return atom
    }

    private fun getDINFAtom(): Atom {
        val atom: Atom = Atom("dinf")
        atom.addChild(getDREFAtom())
        return atom
    }

    private fun getDREFAtom(): Atom {
        val atom: Atom = Atom("dref", 0.toByte(), 0)
        val url: ByteArray = getURLAtom().getBytes()
        val data: ByteArray = ByteArray(4 + url.size)
        data.get(3) = 0x01 // entry count = 1
        System.arraycopy(url, 0, data, 4, url.size)
        atom.setData(data)
        return atom
    }

    private fun getURLAtom(): Atom {
        val atom: Atom = Atom("url ", 0.toByte(), 0x01) // flags = 0x01: data is self contained.
        return atom
    }

    private fun getSTBLAtom(): Atom {
        val atom: Atom = Atom("stbl")
        atom.addChild(getSTSDAtom())
        atom.addChild(getSTTSAtom())
        atom.addChild(getSTSCAtom())
        atom.addChild(getSTSZAtom())
        atom.addChild(getSTCOAtom())
        return atom
    }

    private fun getSTSDAtom(): Atom {
        val atom: Atom = Atom("stsd", 0.toByte(), 0)
        val mp4a: ByteArray = getMP4AAtom().getBytes()
        val data: ByteArray = ByteArray(4 + mp4a.size)
        data.get(3) = 0x01 // entry count = 1
        System.arraycopy(mp4a, 0, data, 4, mp4a.size)
        atom.setData(data)
        return atom
    }

    // See also Part 14 section 5.6.1 of ISO/IEC 14496 for this atom.
    private fun getMP4AAtom(): Atom {
        val atom: Atom = Atom("mp4a")
        val ase: ByteArray = byteArrayOf( // Audio Sample Entry data
            0, 0, 0, 0, 0, 0,  // reserved
            0, 1,  // data reference index
            0, 0, 0, 0,  // reserved
            0, 0, 0, 0,  // reserved
            (mChannels shr 8).toByte(), mChannels.toByte(),  // channel count
            0, 0x10,  // sample size
            0, 0,  // pre-defined
            0, 0,  // reserved
            (mSampleRate shr 8).toByte(), (mSampleRate).toByte(), 0, 0)
        val esds: ByteArray = getESDSAtom().getBytes()
        val data: ByteArray = ByteArray(ase.size + esds.size)
        System.arraycopy(ase, 0, data, 0, ase.size)
        System.arraycopy(esds, 0, data, ase.size, esds.size)
        atom.setData(data)
        return atom
    }

    private fun getESDSAtom(): Atom {
        val atom: Atom = Atom("esds", 0.toByte(), 0)
        atom.setData(getESDescriptor())
        return atom
    }

    // Returns an ES Descriptor for an ISO/IEC 14496-3 audio stream, AAC LC, 44100Hz, 2 channels,
    // 1024 samples per frame per channel. The decoder buffer size is set so that it can contain at
    // least 2 frames. (See section 7.2.6.5 of ISO/IEC 14496-1 for more details).
    private fun getESDescriptor(): ByteArray {
        val samplingFrequencies: IntArray =
            intArrayOf(96000, 88200, 64000, 48000, 44100, 32000, 24000,
                22050, 16000, 12000, 11025, 8000, 7350)
        // First 5 bytes of the ES Descriptor.
        val ESDescriptor_top: ByteArray = byteArrayOf(0x03, 0x19, 0x00, 0x00, 0x00)
        // First 4 bytes of Decoder Configuration Descriptor. Audio ISO/IEC 14496-3, AudioStream.
        val decConfigDescr_top: ByteArray = byteArrayOf(0x04, 0x11, 0x40, 0x15)
        // Audio Specific Configuration: AAC LC, 1024 samples/frame/channel.
        // Sampling frequency and channels configuration are not set yet.
        val audioSpecificConfig: ByteArray = byteArrayOf(0x05, 0x02, 0x10, 0x00)
        val slConfigDescr: ByteArray = byteArrayOf(0x06, 0x01, 0x02) // specific for MP4 file.
        var offset: Int
        var bufferSize: Int = 0x300
        while (bufferSize < 2 * mMaxFrameSize) {
            // TODO(nfaralli): what should be the minimum size of the decoder buffer?
            // Should it be a multiple of 256?
            bufferSize += 0x100
        }

        // create the Decoder Configuration Descriptor
        val decConfigDescr: ByteArray = ByteArray(2 + decConfigDescr_top.get(1))
        System.arraycopy(decConfigDescr_top, 0, decConfigDescr, 0, decConfigDescr_top.size)
        offset = decConfigDescr_top.size
        decConfigDescr.get(offset++) = ((bufferSize shr 16) and 0xFF).toByte()
        decConfigDescr.get(offset++) = ((bufferSize shr 8) and 0xFF).toByte()
        decConfigDescr.get(offset++) = (bufferSize and 0xFF).toByte()
        decConfigDescr.get(offset++) = ((mBitrate shr 24) and 0xFF).toByte()
        decConfigDescr.get(offset++) = ((mBitrate shr 16) and 0xFF).toByte()
        decConfigDescr.get(offset++) = ((mBitrate shr 8) and 0xFF).toByte()
        decConfigDescr.get(offset++) = (mBitrate and 0xFF).toByte()
        decConfigDescr.get(offset++) = ((mBitrate shr 24) and 0xFF).toByte()
        decConfigDescr.get(offset++) = ((mBitrate shr 16) and 0xFF).toByte()
        decConfigDescr.get(offset++) = ((mBitrate shr 8) and 0xFF).toByte()
        decConfigDescr.get(offset++) = (mBitrate and 0xFF).toByte()
        var index: Int
        index = 0
        while (index < samplingFrequencies.size) {
            if (samplingFrequencies.get(index) == mSampleRate) {
                break
            }
            index++
        }
        if (index == samplingFrequencies.size) {
            // TODO(nfaralli): log something here.
            // Invalid sampling frequency. Default to 44100Hz...
            index = 4
        }
        audioSpecificConfig.get(2) = audioSpecificConfig.get(2) or ((index shr 1) and 0x07).toByte()
        audioSpecificConfig.get(3) =
            audioSpecificConfig.get(3) or (((index and 1) shl 7) or ((mChannels and 0x0F) shl 3)).toByte()
        System.arraycopy(
            audioSpecificConfig, 0, decConfigDescr, offset, audioSpecificConfig.size)

        // create the ES Descriptor
        val ESDescriptor: ByteArray = ByteArray(2 + ESDescriptor_top.get(1))
        System.arraycopy(ESDescriptor_top, 0, ESDescriptor, 0, ESDescriptor_top.size)
        offset = ESDescriptor_top.size
        System.arraycopy(decConfigDescr, 0, ESDescriptor, offset, decConfigDescr.size)
        offset += decConfigDescr.size
        System.arraycopy(slConfigDescr, 0, ESDescriptor, offset, slConfigDescr.size)
        return ESDescriptor
    }

    private fun getSTTSAtom(): Atom {
        val atom: Atom = Atom("stts", 0.toByte(), 0)
        val numAudioFrames: Int = mFrameSize!!.size - 1
        atom.setData(byteArrayOf(
            0,
            0,
            0,
            0x02,  // entry count
            0,
            0,
            0,
            0x01,  // first frame contains no audio
            0,
            0,
            0,
            0,
            ((numAudioFrames shr 24) and 0xFF).toByte(),
            ((numAudioFrames shr 16) and 0xFF).toByte(),
            ((numAudioFrames shr 8) and 0xFF).toByte(),
            (numAudioFrames and 0xFF).toByte(),
            0,
            0,
            0x04,
            0))
        return atom
    }

    private fun getSTSCAtom(): Atom {
        val atom: Atom = Atom("stsc", 0.toByte(), 0)
        val numFrames: Int = mFrameSize!!.size
        atom.setData(byteArrayOf(
            0,
            0,
            0,
            0x01,  // entry count
            0,
            0,
            0,
            0x01,  // first chunk
            ((numFrames shr 24) and 0xFF).toByte(),
            ((numFrames shr 16) and 0xFF).toByte(),  // samples per
            ((numFrames shr 8) and 0xFF).toByte(),
            (numFrames and 0xFF).toByte(),  // chunk
            0,
            0,
            0,
            0x01))
        return atom
    }

    private fun getSTSZAtom(): Atom {
        val atom: Atom = Atom("stsz", 0.toByte(), 0)
        val numFrames: Int = mFrameSize!!.size
        val data: ByteArray = ByteArray(8 + 4 * numFrames)
        var offset: Int = 0
        data.get(offset++) = 0 // sample size (=0 => each frame can have a different size)
        data.get(offset++) = 0
        data.get(offset++) = 0
        data.get(offset++) = 0
        data.get(offset++) = ((numFrames shr 24) and 0xFF).toByte() // sample count
        data.get(offset++) = ((numFrames shr 16) and 0xFF).toByte()
        data.get(offset++) = ((numFrames shr 8) and 0xFF).toByte()
        data.get(offset++) = (numFrames and 0xFF).toByte()
        for (size: Int in mFrameSize) {
            data.get(offset++) = ((size shr 24) and 0xFF).toByte()
            data.get(offset++) = ((size shr 16) and 0xFF).toByte()
            data.get(offset++) = ((size shr 8) and 0xFF).toByte()
            data.get(offset++) = (size and 0xFF).toByte()
        }
        atom.setData(data)
        return atom
    }

    private fun getSTCOAtom(): Atom {
        val atom: Atom = Atom("stco", 0.toByte(), 0)
        atom.setData(byteArrayOf(
            0, 0, 0, 0x01,  // entry count
            0, 0, 0, 0 // chunk offset. Set to 0 here. Must be set later. Here it should be
            // the size of the complete header, as the AAC stream will follow
            // immediately.
        ))
        return atom
    }

    companion object {
        fun getMP4Header(
            sampleRate: Int, numChannels: Int, frame_size: IntArray?, bitrate: Int
        ): ByteArray? {
            return MP4Header(sampleRate, numChannels, frame_size, bitrate).mHeader
        }
    }

    // Creates a new MP4Header object that should be used to generate an .m4a file header.
    init {
        if ((frame_size == null) || (frame_size.size < 2) || (frame_size.get(0) != 2)) {
            //TODO(nfaralli): log something here
            return
        }
        mSampleRate = sampleRate
        mChannels = numChannels
        mFrameSize = frame_size
        mBitrate = bitrate
        mMaxFrameSize = mFrameSize!!.get(0)
        mTotSize = mFrameSize.get(0)
        for (i in 1 until mFrameSize.size) {
            if (mMaxFrameSize < mFrameSize.get(i)) {
                mMaxFrameSize = mFrameSize.get(i)
            }
            mTotSize += mFrameSize.get(i)
        }
        var time: Long = System.currentTimeMillis() / 1000
        time += ((66 * 365 + 16) * 24 * 60 * 60).toLong() // number of seconds between 1904 and 1970
        mTime = ByteArray(4)
        mTime.get(0) = ((time shr 24) and 0xFF).toByte()
        mTime.get(1) = ((time shr 16) and 0xFF).toByte()
        mTime.get(2) = ((time shr 8) and 0xFF).toByte()
        mTime.get(3) = (time and 0xFF).toByte()
        val numSamples: Int = 1024 * (frame_size!!.size - 1) // 1st frame does not contain samples.
        var durationMS: Int = (numSamples * 1000) / mSampleRate
        if ((numSamples * 1000) % mSampleRate > 0) {  // round the duration up.
            durationMS++
        }
        mNumSamples = byteArrayOf(
            ((numSamples shr 26) and 0XFF).toByte(),
            ((numSamples shr 16) and 0XFF).toByte(),
            ((numSamples shr 8) and 0XFF).toByte(),
            (numSamples and 0XFF).toByte()
        )
        mDurationMS = byteArrayOf(
            ((durationMS shr 26) and 0XFF).toByte(),
            ((durationMS shr 16) and 0XFF).toByte(),
            ((durationMS shr 8) and 0XFF).toByte(),
            (durationMS and 0XFF).toByte()
        )
        setHeader()
    }
}