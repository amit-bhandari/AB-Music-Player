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

class SoundFile  // A SoundFile object should only be created using the static methods create() and record().
private constructor() {
    private var mProgressListener: ProgressListener? = null
    private var mInputFile: File? = null

    // Member variables representing frame data
    private var mFileType: String? = null
    private var mFileSize: Int = 0
    private var mAvgBitRate // Average bit rate in kbps.
            : Int = 0
    private var mSampleRate: Int = 0
    private var mChannels: Int = 0
    private var mNumSamples // total number of samples per channel in audio file
            : Int = 0
    private var mDecodedBytes // Raw audio data
            : ByteBuffer? = null
    private var mDecodedSamples // shared buffer with mDecodedBytes.
            : ShortBuffer? = null

    // mDecodedSamples has the following format:
    // {s1c1, s1c2, ..., s1cM, s2c1, ..., s2cM, ..., sNc1, ..., sNcM}
    // where sicj is the ith sample of the jth channel (a sample is a signed short)
    // M is the number of channels (e.g. 2 for stereo) and N is the number of samples per channel.
    // Member variables for hack (making it work with old version, until app just uses the samples).
    private var mNumFrames: Int = 0
    private var mFrameGains: IntArray
    private var mFrameLens: IntArray?
    private var mFrameOffsets: IntArray?

    // Progress listener interface.
    open interface ProgressListener {
        /**
         * Will be called by the SoundFile class periodically
         * with values between 0.0 and 1.0.  Return true to continue
         * loading the file or recording the audio, and false to cancel or stop recording.
         */
        fun reportProgress(fractionComplete: Double): Boolean
    }

    // Custom exception for invalid inputs.
    inner class InvalidInputException constructor(message: String?) : Exception(message) {
        companion object {
            // Serial version ID generated by Eclipse.
            private val serialVersionUID: Long = -2505698991597837165L
        }
    }

    fun getFiletype(): String? {
        return mFileType
    }

    fun getFileSizeBytes(): Int {
        return mFileSize
    }

    fun getAvgBitrateKbps(): Int {
        return mAvgBitRate
    }

    fun getSampleRate(): Int {
        return mSampleRate
    }

    fun getChannels(): Int {
        return mChannels
    }

    fun getNumSamples(): Int {
        return mNumSamples // Number of samples per channel.
    }

    // Should be removed when the app will use directly the samples instead of the frames.
    fun getNumFrames(): Int {
        return mNumFrames
    }

    // Should be removed when the app will use directly the samples instead of the frames.
    fun getSamplesPerFrame(): Int {
        return 1024 // just a fixed value here...
    }

    // Should be removed when the app will use directly the samples instead of the frames.
    fun getFrameGains(): IntArray {
        return mFrameGains
    }

    fun getSamples(): ShortBuffer? {
        if (mDecodedSamples != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1
            ) {
                // Hack for Nougat where asReadOnlyBuffer fails to respect byte ordering.
                // See https://code.google.com/p/android/issues/detail?id=223824
                return mDecodedSamples
            } else {
                return mDecodedSamples!!.asReadOnlyBuffer()
            }
        } else {
            return null
        }
    }

    private fun setProgressListener(progressListener: ProgressListener) {
        mProgressListener = progressListener
    }

    @Throws(FileNotFoundException::class, IOException::class, InvalidInputException::class)
    private fun ReadFile(inputFile: File) {
        var extractor: MediaExtractor? = MediaExtractor()
        var format: MediaFormat? = null
        var i: Int
        mInputFile = inputFile
        val components: Array<String> = mInputFile!!.getPath().split("\\.".toRegex()).toTypedArray()
        mFileType = components.get(components.size - 1)
        mFileSize = mInputFile!!.length().toInt()
        extractor!!.setDataSource(mInputFile!!.getPath())
        val numTracks: Int = extractor.getTrackCount()
        // find and select the first audio track present in the file.
        i = 0
        while (i < numTracks) {
            format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)!!.startsWith("audio/")) {
                extractor.selectTrack(i)
                break
            }
            i++
        }
        if (i == numTracks) {
            throw InvalidInputException("No audio track found in " + mInputFile)
        }
        mChannels = format!!.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        // Expected total number of samples per channel.
        val expectedNumSamples: Int =
            ((format.getLong(MediaFormat.KEY_DURATION) / 1000000f) * mSampleRate + 0.5f).toInt()
        var codec: MediaCodec? =
            MediaCodec.createDecoderByType((format.getString(MediaFormat.KEY_MIME))!!)
        codec!!.configure(format, null, null, 0)
        codec.start()
        var decodedSamplesSize: Int = 0 // size of the output buffer containing decoded samples.
        var decodedSamples: ByteArray? = null
        val inputBuffers: Array<ByteBuffer> = codec.getInputBuffers()
        var outputBuffers: Array<ByteBuffer> = codec.getOutputBuffers()
        var sample_size: Int
        val info: BufferInfo = BufferInfo()
        var presentation_time: Long
        var tot_size_read: Int = 0
        var done_reading: Boolean = false

        // Set the size of the decoded samples buffer to 1MB (~6sec of a stereo stream at 44.1kHz).
        // For longer streams, the buffer size will be increased later on, calculating a rough
        // estimate of the total size needed to store all the samples in order to resize the buffer
        // only once.
        mDecodedBytes = ByteBuffer.allocate(1 shl 20)
        var firstSampleData: Boolean = true
        while (true) {
            // read data from file and feed it to the decoder input buffers.
            val inputBufferIndex: Int = codec!!.dequeueInputBuffer(100)
            if (!done_reading && inputBufferIndex >= 0) {
                sample_size = extractor!!.readSampleData(inputBuffers.get(inputBufferIndex), 0)
                if ((firstSampleData
                            && (format.getString(MediaFormat.KEY_MIME) == "audio/mp4a-latm") && (sample_size == 2))
                ) {
                    // For some reasons on some devices (e.g. the Samsung S3) you should not
                    // provide the first two bytes of an AAC stream, otherwise the MediaCodec will
                    // crash. These two bytes do not contain music data but basic info on the
                    // stream (e.g. channel configuration and sampling frequency), and skipping them
                    // seems OK with other devices (MediaCodec has already been configured and
                    // already knows these parameters).
                    extractor.advance()
                    tot_size_read += sample_size
                } else if (sample_size < 0) {
                    // All samples have been read.
                    codec.queueInputBuffer(
                        inputBufferIndex, 0, 0, -1, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    done_reading = true
                } else {
                    presentation_time = extractor.getSampleTime()
                    codec.queueInputBuffer(inputBufferIndex, 0, sample_size, presentation_time, 0)
                    extractor.advance()
                    tot_size_read += sample_size
                    if (mProgressListener != null) {
                        if (!mProgressListener!!.reportProgress(((tot_size_read).toFloat() / mFileSize).toDouble())) {
                            // We are asked to stop reading the file. Returning immediately. The
                            // SoundFile object is invalid and should NOT be used afterward!
                            extractor.release()
                            extractor = null
                            codec.stop()
                            codec.release()
                            codec = null
                            return
                        }
                    }
                }
                firstSampleData = false
            }

            // Get decoded stream from the decoder output buffers.
            val outputBufferIndex: Int = codec.dequeueOutputBuffer(info, 100)
            if (outputBufferIndex >= 0 && info.size > 0) {
                if (decodedSamplesSize < info.size) {
                    decodedSamplesSize = info.size
                    decodedSamples = ByteArray(decodedSamplesSize)
                }
                outputBuffers.get(outputBufferIndex).get(decodedSamples, 0, info.size)
                outputBuffers.get(outputBufferIndex).clear()
                // Check if buffer is big enough. Resize it if it's too small.
                if (mDecodedBytes.remaining() < info.size) {
                    // Getting a rough estimate of the total size, allocate 20% more, and
                    // make sure to allocate at least 5MB more than the initial size.
                    val position: Int = mDecodedBytes.position()
                    var newSize: Int =
                        ((position * (1.0 * mFileSize / tot_size_read)) * 1.2).toInt()
                    if (newSize - position < info.size + 5 * (1 shl 20)) {
                        newSize = position + info.size + (5 * (1 shl 20))
                    }
                    var newDecodedBytes: ByteBuffer? = null
                    // Try to allocate memory. If we are OOM, try to run the garbage collector.
                    var retry: Int = 10
                    while (retry > 0) {
                        try {
                            newDecodedBytes = ByteBuffer.allocate(newSize)
                            break
                        } catch (oome: OutOfMemoryError) {
                            // setting android:largeHeap="true" in <application> seem to help not
                            // reaching this section.
                            retry--
                        }
                    }
                    if (retry == 0) {
                        // Failed to allocate memory... Stop reading more data and finalize the
                        // instance with the data decoded so far.
                        break
                    }
                    //ByteBuffer newDecodedBytes = ByteBuffer.allocate(newSize);
                    mDecodedBytes.rewind()
                    newDecodedBytes!!.put(mDecodedBytes)
                    mDecodedBytes = newDecodedBytes
                    mDecodedBytes!!.position(position)
                }
                mDecodedBytes!!.put(decodedSamples, 0, info.size)
                codec.releaseOutputBuffer(outputBufferIndex, false)
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = codec.getOutputBuffers()
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Subsequent data will conform to new format.
                // We could check that codec.getOutputFormat(), which is the new output format,
                // is what we expect.
            }
            if (((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
                        || (mDecodedBytes!!.position() / (2 * mChannels)) >= expectedNumSamples)
            ) {
                // We got all the decoded data from the decoder. Stop here.
                // Theoretically dequeueOutputBuffer(info, ...) should have set info.flags to
                // MediaCodec.BUFFER_FLAG_END_OF_STREAM. However some phones (e.g. Samsung S3)
                // won't do that for some files (e.g. with mono AAC files), in which case subsequent
                // calls to dequeueOutputBuffer may result in the application crashing, without
                // even an exception being thrown... Hence the second check.
                // (for mono AAC files, the S3 will actually double each sample, as if the stream
                // was stereo. The resulting stream is half what it's supposed to be and with a much
                // lower pitch.)
                break
            }
        }
        mNumSamples = mDecodedBytes.position() / (mChannels * 2) // One sample = 2 bytes.
        mDecodedBytes.rewind()
        mDecodedBytes.order(ByteOrder.LITTLE_ENDIAN)
        mDecodedSamples = mDecodedBytes.asShortBuffer()
        mAvgBitRate = ((mFileSize * 8) * (mSampleRate.toFloat() / mNumSamples) / 1000).toInt()
        extractor!!.release()
        extractor = null
        codec!!.stop()
        codec.release()
        codec = null

        // Temporary hack to make it work with the old version.
        mNumFrames = mNumSamples / getSamplesPerFrame()
        if (mNumSamples % getSamplesPerFrame() != 0) {
            mNumFrames++
        }
        mFrameGains = IntArray(mNumFrames)
        mFrameLens = IntArray(mNumFrames)
        mFrameOffsets = IntArray(mNumFrames)
        var j: Int
        var gain: Int
        var value: Int
        val frameLens: Int = ((1000 * mAvgBitRate / 8) *
                (getSamplesPerFrame().toFloat() / mSampleRate)).toInt()
        i = 0
        while (i < mNumFrames) {
            gain = -1
            j = 0
            while (j < getSamplesPerFrame()) {
                value = 0
                for (k in 0 until mChannels) {
                    if (mDecodedSamples.remaining() > 0) {
                        value += Math.abs(mDecodedSamples.get().toInt())
                    }
                }
                value /= mChannels
                if (gain < value) {
                    gain = value
                }
                j++
            }
            mFrameGains.get(i) =
                Math.sqrt(gain.toDouble()).toInt() // here gain = sqrt(max value of 1st channel)...
            mFrameLens!!.get(i) = frameLens // totally not accurate...
            mFrameOffsets!!.get(i) = ((i * (1000 * mAvgBitRate / 8) *  //  = i * frameLens
                    (getSamplesPerFrame().toFloat() / mSampleRate))).toInt()
            i++
        }
        mDecodedSamples.rewind()
        // DumpSamples();  // Uncomment this line to dump the samples in a TSV file.
    }

    private fun RecordAudio() {
        if (mProgressListener == null) {
            // A progress listener is mandatory here, as it will let us know when to stop recording.
            return
        }
        mInputFile = null
        mFileType = "raw"
        mFileSize = 0
        mSampleRate = 44100
        mChannels = 1 // record mono audio.
        val buffer: ShortArray =
            ShortArray(1024) // buffer contains 1 mono frame of 1024 16 bits samples
        var minBufferSize: Int = AudioRecord.getMinBufferSize(
            mSampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        // make sure minBufferSize can contain at least 1 second of audio (16 bits sample).
        if (minBufferSize < mSampleRate * 2) {
            minBufferSize = mSampleRate * 2
        }
        val audioRecord: AudioRecord = AudioRecord(
            MediaRecorder.AudioSource.DEFAULT,
            mSampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize
        )

        // Allocate memory for 20 seconds first. Reallocate later if more is needed.
        mDecodedBytes = ByteBuffer.allocate(20 * mSampleRate * 2)
        mDecodedBytes.order(ByteOrder.LITTLE_ENDIAN)
        mDecodedSamples = mDecodedBytes.asShortBuffer()
        audioRecord.startRecording()
        while (true) {
            // check if mDecodedSamples can contain 1024 additional samples.
            if (mDecodedSamples.remaining() < 1024) {
                // Try to allocate memory for 10 additional seconds.
                val newCapacity: Int = mDecodedBytes.capacity() + 10 * mSampleRate * 2
                var newDecodedBytes: ByteBuffer? = null
                try {
                    newDecodedBytes = ByteBuffer.allocate(newCapacity)
                } catch (oome: OutOfMemoryError) {
                    break
                }
                val position: Int = mDecodedSamples.position()
                mDecodedBytes.rewind()
                newDecodedBytes.put(mDecodedBytes)
                mDecodedBytes = newDecodedBytes
                mDecodedBytes.order(ByteOrder.LITTLE_ENDIAN)
                mDecodedBytes.rewind()
                mDecodedSamples = mDecodedBytes.asShortBuffer()
                mDecodedSamples.position(position)
            }
            // TODO(nfaralli): maybe use the read method that takes a direct ByteBuffer argument.
            audioRecord.read(buffer, 0, buffer.size)
            mDecodedSamples.put(buffer)
            // Let the progress listener know how many seconds have been recorded.
            // The returned value tells us if we should keep recording or stop.
            if (!mProgressListener!!.reportProgress((
                        (mDecodedSamples.position()).toFloat() / mSampleRate).toDouble())
            ) {
                break
            }
        }
        audioRecord.stop()
        audioRecord.release()
        mNumSamples = mDecodedSamples.position()
        mDecodedSamples.rewind()
        mDecodedBytes.rewind()
        mAvgBitRate = mSampleRate * 16 / 1000

        // Temporary hack to make it work with the old version.
        mNumFrames = mNumSamples / getSamplesPerFrame()
        if (mNumSamples % getSamplesPerFrame() != 0) {
            mNumFrames++
        }
        mFrameGains = IntArray(mNumFrames)
        mFrameLens = null // not needed for recorded audio
        mFrameOffsets = null // not needed for recorded audio
        var i: Int
        var j: Int
        var gain: Int
        var value: Int
        i = 0
        while (i < mNumFrames) {
            gain = -1
            j = 0
            while (j < getSamplesPerFrame()) {
                if (mDecodedSamples.remaining() > 0) {
                    value = Math.abs(mDecodedSamples.get().toInt())
                } else {
                    value = 0
                }
                if (gain < value) {
                    gain = value
                }
                j++
            }
            mFrameGains.get(i) =
                Math.sqrt(gain.toDouble()).toInt() // here gain = sqrt(max value of 1st channel)...
            i++
        }
        mDecodedSamples.rewind()
        // DumpSamples();  // Uncomment this line to dump the samples in a TSV file.
    }

    // should be removed in the near future...
    @Throws(IOException::class)
    fun WriteFile(outputFile: File?, startFrame: Int, numFrames: Int) {
        val startTime: Float = startFrame.toFloat() * getSamplesPerFrame() / mSampleRate
        val endTime: Float = (startFrame + numFrames) as Float * getSamplesPerFrame() / mSampleRate
        WriteFile(outputFile, startTime, endTime)
    }

    @Throws(IOException::class)
    fun WriteFile(outputFile: File?, startTime: Float, endTime: Float) {
        val startOffset: Int = (startTime * mSampleRate).toInt() * 2 * mChannels
        var numSamples: Int = ((endTime - startTime) * mSampleRate).toInt()
        // Some devices have problems reading mono AAC files (e.g. Samsung S3). Making it stereo.
        val numChannels: Int = if ((mChannels == 1)) 2 else mChannels
        val mimeType: String = "audio/mp4a-latm"
        val bitrate: Int =
            64000 * numChannels // rule of thumb for a good quality: 64kbps per channel.
        var codec: MediaCodec? = MediaCodec.createEncoderByType(mimeType)
        val format: MediaFormat = MediaFormat.createAudioFormat(mimeType, mSampleRate, numChannels)
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        codec!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()

        // Get an estimation of the encoded data based on the bitrate. Add 10% to it.
        var estimatedEncodedSize: Int = ((endTime - startTime) * (bitrate / 8) * 1.1).toInt()
        var encodedBytes: ByteBuffer = ByteBuffer.allocate(estimatedEncodedSize)
        val inputBuffers: Array<ByteBuffer> = codec.getInputBuffers()
        var outputBuffers: Array<ByteBuffer> = codec.getOutputBuffers()
        val info: BufferInfo = BufferInfo()
        var done_reading: Boolean = false
        var presentation_time: Long = 0
        val frame_size: Int =
            1024 // number of samples per frame per channel for an mp4 (AAC) stream.
        var buffer: ByteArray =
            ByteArray(frame_size * numChannels * 2) // a sample is coded with a short.
        mDecodedBytes!!.position(startOffset)
        numSamples += (2 * frame_size) // Adding 2 frames, Cf. priming frames for AAC.
        var tot_num_frames: Int = 1 + (numSamples / frame_size) // first AAC frame = 2 bytes
        if (numSamples % frame_size != 0) {
            tot_num_frames++
        }
        val frame_sizes: IntArray = IntArray(tot_num_frames)
        var num_out_frames: Int = 0
        var num_frames: Int = 0
        var num_samples_left: Int = numSamples
        var encodedSamplesSize: Int = 0 // size of the output buffer containing the encoded samples.
        var encodedSamples: ByteArray? = null
        while (true) {
            // Feed the samples to the encoder.
            val inputBufferIndex: Int = codec.dequeueInputBuffer(100)
            if (!done_reading && inputBufferIndex >= 0) {
                if (num_samples_left <= 0) {
                    // All samples have been read.
                    codec.queueInputBuffer(
                        inputBufferIndex, 0, 0, -1, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    done_reading = true
                } else {
                    inputBuffers.get(inputBufferIndex).clear()
                    if (buffer.size > inputBuffers.get(inputBufferIndex).remaining()) {
                        // Input buffer is smaller than one frame. This should never happen.
                        continue
                    }
                    // bufferSize is a hack to create a stereo file from a mono stream.
                    val bufferSize: Int = if ((mChannels == 1)) (buffer.size / 2) else buffer.size
                    if (mDecodedBytes!!.remaining() < bufferSize) {
                        for (i in mDecodedBytes!!.remaining() until bufferSize) {
                            buffer.get(i) = 0 // pad with extra 0s to make a full frame.
                        }
                        mDecodedBytes!!.get(buffer, 0, mDecodedBytes!!.remaining())
                    } else {
                        mDecodedBytes!!.get(buffer, 0, bufferSize)
                    }
                    if (mChannels == 1) {
                        var i: Int = bufferSize - 1
                        while (i >= 1) {
                            buffer.get(2 * i + 1) = buffer.get(i)
                            buffer.get(2 * i) = buffer.get(i - 1)
                            buffer.get(2 * i - 1) = buffer.get(2 * i + 1)
                            buffer.get(2 * i - 2) = buffer.get(2 * i)
                            i -= 2
                        }
                    }
                    num_samples_left -= frame_size
                    inputBuffers.get(inputBufferIndex).put(buffer)
                    presentation_time = (((num_frames++) * frame_size * 1e6) / mSampleRate).toLong()
                    codec.queueInputBuffer(
                        inputBufferIndex, 0, buffer.size, presentation_time, 0)
                }
            }

            // Get the encoded samples from the encoder.
            val outputBufferIndex: Int = codec.dequeueOutputBuffer(info, 100)
            if ((outputBufferIndex >= 0) && (info.size > 0) && (info.presentationTimeUs >= 0)) {
                if (num_out_frames < frame_sizes.size) {
                    frame_sizes.get(num_out_frames++) = info.size
                }
                if (encodedSamplesSize < info.size) {
                    encodedSamplesSize = info.size
                    encodedSamples = ByteArray(encodedSamplesSize)
                }
                outputBuffers.get(outputBufferIndex).get(encodedSamples, 0, info.size)
                outputBuffers.get(outputBufferIndex).clear()
                codec.releaseOutputBuffer(outputBufferIndex, false)
                if (encodedBytes.remaining() < info.size) {  // Hopefully this should not happen.
                    estimatedEncodedSize = (estimatedEncodedSize * 1.2).toInt() // Add 20%.
                    val newEncodedBytes: ByteBuffer = ByteBuffer.allocate(estimatedEncodedSize)
                    val position: Int = encodedBytes.position()
                    encodedBytes.rewind()
                    newEncodedBytes.put(encodedBytes)
                    encodedBytes = newEncodedBytes
                    encodedBytes.position(position)
                }
                encodedBytes.put(encodedSamples, 0, info.size)
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = codec.getOutputBuffers()
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Subsequent data will conform to new format.
                // We could check that codec.getOutputFormat(), which is the new output format,
                // is what we expect.
            }
            if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                // We got all the encoded data from the encoder.
                break
            }
        }
        val encoded_size: Int = encodedBytes.position()
        encodedBytes.rewind()
        codec.stop()
        codec.release()
        codec = null

        // Write the encoded stream to the file, 4kB at a time.
        buffer = ByteArray(4096)
        try {
            val outputStream: FileOutputStream = FileOutputStream(outputFile)
            outputStream.write(
                MP4Header.getMP4Header(mSampleRate, numChannels, frame_sizes, bitrate))
            while (encoded_size - encodedBytes.position() > buffer.size) {
                encodedBytes.get(buffer)
                outputStream.write(buffer)
            }
            val remaining: Int = encoded_size - encodedBytes.position()
            if (remaining > 0) {
                encodedBytes.get(buffer, 0, remaining)
                outputStream.write(buffer, 0, remaining)
            }
            outputStream.close()
        } catch (e: IOException) {
            Log.e("Ringdroid", "Failed to create the .m4a file.")
            Log.e("Ringdroid", getStackTrace(e))
        }
    }

    // Method used to swap the left and right channels (needed for stereo WAV files).
    // buffer contains the PCM data: {sample 1 right, sample 1 left, sample 2 right, etc.}
    // The size of a sample is assumed to be 16 bits (for a single channel).
    // When done, buffer will contain {sample 1 left, sample 1 right, sample 2 left, etc.}
    private fun swapLeftRightChannels(buffer: ByteArray) {
        val left: ByteArray = ByteArray(2)
        val right: ByteArray = ByteArray(2)
        if (buffer.size % 4 != 0) {  // 2 channels, 2 bytes per sample (for one channel).
            // Invalid buffer size.
            return
        }
        var offset: Int = 0
        while (offset < buffer.size) {
            left.get(0) = buffer.get(offset)
            left.get(1) = buffer.get(offset + 1)
            right.get(0) = buffer.get(offset + 2)
            right.get(1) = buffer.get(offset + 3)
            buffer.get(offset) = right.get(0)
            buffer.get(offset + 1) = right.get(1)
            buffer.get(offset + 2) = left.get(0)
            buffer.get(offset + 3) = left.get(1)
            offset += 4
        }
    }

    // should be removed in the near future...
    @Throws(IOException::class)
    fun WriteWAVFile(outputFile: File?, startFrame: Int, numFrames: Int) {
        val startTime: Float = startFrame.toFloat() * getSamplesPerFrame() / mSampleRate
        val endTime: Float = (startFrame + numFrames) as Float * getSamplesPerFrame() / mSampleRate
        WriteWAVFile(outputFile, startTime, endTime)
    }

    @Throws(IOException::class)
    fun WriteWAVFile(outputFile: File?, startTime: Float, endTime: Float) {
        val startOffset: Int = (startTime * mSampleRate).toInt() * 2 * mChannels
        val numSamples: Int = ((endTime - startTime) * mSampleRate).toInt()

        // Start by writing the RIFF header.
        val outputStream: FileOutputStream = FileOutputStream(outputFile)
        outputStream.write(WAVHeader.getWAVHeader(mSampleRate, mChannels, numSamples))

        // Write the samples to the file, 1024 at a time.
        val buffer: ByteArray =
            ByteArray(1024 * mChannels * 2) // Each sample is coded with a short.
        mDecodedBytes!!.position(startOffset)
        var numBytesLeft: Int = numSamples * mChannels * 2
        while (numBytesLeft >= buffer.size) {
            if (mDecodedBytes!!.remaining() < buffer.size) {
                // This should not happen.
                for (i in mDecodedBytes!!.remaining() until buffer.size) {
                    buffer.get(i) = 0 // pad with extra 0s to make a full frame.
                }
                mDecodedBytes!!.get(buffer, 0, mDecodedBytes!!.remaining())
            } else {
                mDecodedBytes!!.get(buffer)
            }
            if (mChannels == 2) {
                swapLeftRightChannels(buffer)
            }
            outputStream.write(buffer)
            numBytesLeft -= buffer.size
        }
        if (numBytesLeft > 0) {
            if (mDecodedBytes!!.remaining() < numBytesLeft) {
                // This should not happen.
                for (i in mDecodedBytes!!.remaining() until numBytesLeft) {
                    buffer.get(i) = 0 // pad with extra 0s to make a full frame.
                }
                mDecodedBytes!!.get(buffer, 0, mDecodedBytes!!.remaining())
            } else {
                mDecodedBytes!!.get(buffer, 0, numBytesLeft)
            }
            if (mChannels == 2) {
                swapLeftRightChannels(buffer)
            }
            outputStream.write(buffer, 0, numBytesLeft)
        }
        outputStream.close()
    }

    // Debugging method dumping all the samples in mDecodedSamples in a TSV file.
    // Each row describes one sample and has the following format:
    // "<presentation time in seconds>\t<channel 1>\t...\t<channel N>\n"
    // File will be written on the SDCard under media/audio/debug/
    // If fileName is null or empty, then the default file name (samples.tsv) is used.
    // Helper method (samples will be dumped in media/audio/debug/samples.tsv).
    private fun DumpSamples(fileName: String? = null) {
        var fileName: String? = fileName
        var externalRootDir: String = Environment.getExternalStorageDirectory().getPath()
        if (!externalRootDir.endsWith("/")) {
            externalRootDir += "/"
        }
        var parentDir: String = externalRootDir + "media/audio/debug/"
        // Create the parent directory
        val parentDirFile: File = File(parentDir)
        parentDirFile.mkdirs()
        // If we can't write to that special path, try just writing directly to the SDCard.
        if (!parentDirFile.isDirectory()) {
            parentDir = externalRootDir
        }
        if (fileName == null || fileName.isEmpty()) {
            fileName = "samples.tsv"
        }
        val outFile: File = File(parentDir + fileName)

        // Start dumping the samples.
        var writer: BufferedWriter? = null
        var presentationTime: Float = 0f
        mDecodedSamples!!.rewind()
        var row: String?
        try {
            writer = BufferedWriter(FileWriter(outFile))
            for (sampleIndex in 0 until mNumSamples) {
                presentationTime = (sampleIndex).toFloat() / mSampleRate
                row = java.lang.Float.toString(presentationTime)
                for (channelIndex in 0 until mChannels) {
                    row += "\t" + mDecodedSamples!!.get()
                }
                row += "\n"
                writer.write(row)
            }
        } catch (e: IOException) {
            Log.w("Ringdroid", "Failed to create the sample TSV file.")
            Log.w("Ringdroid", getStackTrace(e))
        }
        // We are done here. Close the file and rewind the buffer.
        try {
            writer!!.close()
        } catch (e: Exception) {
            Log.w("Ringdroid", "Failed to close sample TSV file.")
            Log.w("Ringdroid", getStackTrace(e))
        }
        mDecodedSamples!!.rewind()
    }

    // Return the stack trace of a given exception.
    private fun getStackTrace(e: Exception): String {
        val writer: StringWriter = StringWriter()
        e.printStackTrace(PrintWriter(writer))
        return writer.toString()
    }

    companion object {
        // TODO(nfaralli): what is the real list of supported extensions? Is it device dependent?
        fun getSupportedExtensions(): Array<String> {
            return arrayOf("mp3", "wav", "3gpp", "3gp", "amr", "aac", "m4a", "ogg")
        }

        fun isFilenameSupported(filename: String): Boolean {
            val extensions: Array<String> = getSupportedExtensions()
            for (i in extensions.indices) {
                if (filename.endsWith("." + extensions.get(i))) {
                    return true
                }
            }
            return false
        }

        // Create and return a SoundFile object using the file fileName.
        @Throws(FileNotFoundException::class, IOException::class, InvalidInputException::class)
        fun create(
            fileName: String?,
            progressListener: ProgressListener
        ): SoundFile? {
            // First check that the file exists and that its extension is supported.
            val f: File = File(fileName)
            if (!f.exists()) {
                throw FileNotFoundException(fileName)
            }
            val name: String = f.getName().toLowerCase()
            val components: Array<String> = name.split("\\.".toRegex()).toTypedArray()
            if (components.size < 2) {
                return null
            }
            if (!Arrays.asList(*getSupportedExtensions())
                    .contains(components.get(components.size - 1))
            ) {
                return null
            }
            val soundFile: SoundFile = SoundFile()
            soundFile.setProgressListener(progressListener)
            soundFile.ReadFile(f)
            return soundFile
        }

        // Create and return a SoundFile object by recording a mono audio stream.
        fun record(progressListener: ProgressListener?): SoundFile? {
            if (progressListener == null) {
                // must have a progessListener to stop the recording.
                return null
            }
            val soundFile: SoundFile = SoundFile()
            soundFile.setProgressListener(progressListener)
            soundFile.RecordAudio()
            return soundFile
        }
    }
}