/*
 * Copyright (C) 2009 Google Inc.
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
package com.music.player.bhandari.m.ringtoneCutter

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

class SongMetadataReader internal constructor(activity: Activity?, filename: String?) {
    var GENRES_URI: Uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
    var mActivity: Activity? = null
    var mFilename: String? = ""
    var mTitle: String? = ""
    var mArtist: String? = ""
    var mAlbum: String? = ""
    var mGenre: String = ""
    var mYear: Int = -1
    private fun ReadMetadata() {
        // Get a map from genre ids to names
        val genreIdMap: HashMap<String, String> = HashMap()
        var c: Cursor? = mActivity!!.getContentResolver().query(
            GENRES_URI, arrayOf(
                MediaStore.Audio.Genres._ID,
                MediaStore.Audio.Genres.NAME),
            null, null, null)
        c!!.moveToFirst()
        while (!c.isAfterLast()) {
            genreIdMap.put(c.getString(0), c.getString(1))
            c.moveToNext()
        }
        c.close()
        mGenre = ""
        for (genreId: String in genreIdMap.keys) {
            c = mActivity!!.getContentResolver().query(
                makeGenreUri(genreId), arrayOf(MediaStore.Audio.Media.DATA),
                MediaStore.Audio.Media.DATA + " LIKE \"" + mFilename + "\"",
                null, null)
            if (c!!.getCount() != 0) {
                mGenre = (genreIdMap.get(genreId))!!
                break
            }
            c.close()
            c = null
        }
        val uri: Uri? = MediaStore.Audio.Media.getContentUriForPath((mFilename)!!)
        c = mActivity!!.getContentResolver().query(
            (uri)!!, arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.DATA),
            MediaStore.Audio.Media.DATA + " LIKE \"" + mFilename + "\"",
            null, null)
        if (c!!.getCount() == 0) {
            mTitle = getBasename(mFilename)
            mArtist = ""
            mAlbum = ""
            mYear = -1
            return
        }
        c.moveToFirst()
        mTitle = getStringFromColumn(c, MediaStore.Audio.Media.TITLE)
        if (mTitle == null || mTitle!!.length == 0) {
            mTitle = getBasename(mFilename)
        }
        mArtist = getStringFromColumn(c, MediaStore.Audio.Media.ARTIST)
        mAlbum = getStringFromColumn(c, MediaStore.Audio.Media.ALBUM)
        mYear = getIntegerFromColumn(c, MediaStore.Audio.Media.YEAR)
        c.close()
    }

    private fun makeGenreUri(genreId: String): Uri {
        val CONTENTDIR: String = MediaStore.Audio.Genres.Members.CONTENT_DIRECTORY
        return Uri.parse(
            StringBuilder()
                .append(GENRES_URI.toString())
                .append("/")
                .append(genreId)
                .append("/")
                .append(CONTENTDIR)
                .toString())
    }

    private fun getStringFromColumn(c: Cursor?, columnName: String): String? {
        val index: Int = c!!.getColumnIndexOrThrow(columnName)
        val value: String? = c.getString(index)
        if (value != null && value.length > 0) {
            return value
        } else {
            return null
        }
    }

    private fun getIntegerFromColumn(c: Cursor?, columnName: String): Int {
        val index: Int = c!!.getColumnIndexOrThrow(columnName)
        val value: Int = c.getInt(index)
        if (value != null) {
            return value
        } else {
            return -1
        }
    }

    private fun getBasename(filename: String?): String {
        return filename!!.substring(filename.lastIndexOf('/') + 1,
            filename.lastIndexOf('.'))
    }

    init {
        mActivity = activity
        mFilename = filename
        mTitle = getBasename(filename)
        try {
            ReadMetadata()
        } catch (e: Exception) {
        }
    }
}