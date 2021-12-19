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

class WAVHeader constructor(
// sampling frequency in Hz (e.g. 44100).
    private val mSampleRate: Int, // number of channels.
    private val mChannels: Int, // total number of samples per channel.
    private val mNumSamples: Int
) {
    private var mHeader // the complete header.
            : ByteArray?
    private val mNumBytesPerSample // number of bytes per sample, all channels included.
            : Int

    fun getWAVHeader(): ByteArray? {
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
        val header: ByteArray = ByteArray(46)
        var offset: Int = 0
        var size: Int

        // set the RIFF chunk
        System.arraycopy(byteArrayOf('R'.toByte(), 'I'.toByte(), 'F'.toByte(), 'F'.toByte()),
            0,
            header,
            offset,
            4)
        offset += 4
        size = 36 + mNumSamples * mNumBytesPerSample
        header.get(offset++) = (size and 0xFF).toByte()
        header.get(offset++) = ((size shr 8) and 0xFF).toByte()
        header.get(offset++) = ((size shr 16) and 0xFF).toByte()
        header.get(offset++) = ((size shr 24) and 0xFF).toByte()
        System.arraycopy(byteArrayOf('W'.toByte(), 'A'.toByte(), 'V'.toByte(), 'E'.toByte()),
            0,
            header,
            offset,
            4)
        offset += 4

        // set the fmt chunk
        System.arraycopy(byteArrayOf('f'.toByte(), 'm'.toByte(), 't'.toByte(), ' '.toByte()),
            0,
            header,
            offset,
            4)
        offset += 4
        System.arraycopy(byteArrayOf(0x10, 0, 0, 0), 0, header, offset, 4) // chunk size = 16
        offset += 4
        System.arraycopy(byteArrayOf(1, 0), 0, header, offset, 2) // format = 1 for PCM
        offset += 2
        header.get(offset++) = (mChannels and 0xFF).toByte()
        header.get(offset++) = ((mChannels shr 8) and 0xFF).toByte()
        header.get(offset++) = (mSampleRate and 0xFF).toByte()
        header.get(offset++) = ((mSampleRate shr 8) and 0xFF).toByte()
        header.get(offset++) = ((mSampleRate shr 16) and 0xFF).toByte()
        header.get(offset++) = ((mSampleRate shr 24) and 0xFF).toByte()
        val byteRate: Int = mSampleRate * mNumBytesPerSample
        header.get(offset++) = (byteRate and 0xFF).toByte()
        header.get(offset++) = ((byteRate shr 8) and 0xFF).toByte()
        header.get(offset++) = ((byteRate shr 16) and 0xFF).toByte()
        header.get(offset++) = ((byteRate shr 24) and 0xFF).toByte()
        header.get(offset++) = (mNumBytesPerSample and 0xFF).toByte()
        header.get(offset++) = ((mNumBytesPerSample shr 8) and 0xFF).toByte()
        System.arraycopy(byteArrayOf(0x10, 0), 0, header, offset, 2)
        offset += 2

        // set the beginning of the data chunk
        System.arraycopy(byteArrayOf('d'.toByte(), 'a'.toByte(), 't'.toByte(), 'a'.toByte()),
            0,
            header,
            offset,
            4)
        offset += 4
        size = mNumSamples * mNumBytesPerSample
        header.get(offset++) = (size and 0xFF).toByte()
        header.get(offset++) = ((size shr 8) and 0xFF).toByte()
        header.get(offset++) = ((size shr 16) and 0xFF).toByte()
        header.get(offset++) = ((size shr 24) and 0xFF).toByte()
        mHeader = header
    }

    companion object {
        fun getWAVHeader(sampleRate: Int, numChannels: Int, numSamples: Int): ByteArray? {
            return WAVHeader(sampleRate, numChannels, numSamples).mHeader
        }
    }

    init {
        mNumBytesPerSample = 2 * mChannels // assuming 2 bytes per sample (for 1 channel)
        mHeader = null
        setHeader()
    }
}