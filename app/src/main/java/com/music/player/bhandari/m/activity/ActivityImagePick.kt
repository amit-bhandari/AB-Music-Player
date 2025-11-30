package com.music.player.bhandari.m.activity


import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.music.player.bhandari.m.R
import com.music.player.bhandari.m.MyApp

class ActivityImagePick : AppCompatActivity() {

    private lateinit var cropImage: ActivityResultLauncher<CropImageContractOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_pick)

        val backgroundSelectionStatus = intent.getIntExtra("status", -1)
        val MAIN_LIB = 0
        val NOW_PLAYING = 1
        val NAVIGATION_DRAWER = 2
        val DEFAULT_ALBUM_ART = 3

        cropImage = registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                val croppedImageUri = result.uriContent
                val croppedImageFilePath = result.getUriFilePath(this, false)
                
                var savePath = ""
                
                when (backgroundSelectionStatus) {
                    MAIN_LIB -> savePath = filesDir.toString() + getString(R.string.main_lib_back_custom_image)
                    NOW_PLAYING -> savePath = filesDir.toString() + getString(R.string.now_playing_back_custom_image)
                    DEFAULT_ALBUM_ART -> savePath = filesDir.toString() + getString(R.string.def_album_art_custom_image)
                    NAVIGATION_DRAWER -> savePath = filesDir.toString() + getString(R.string.nav_back_custom_image)
                    else -> savePath = filesDir.toString() + getString(R.string.nav_back_custom_image)
                }

                try {
                    val inputStream = contentResolver.openInputStream(croppedImageUri!!)
                    val outputStream = java.io.FileOutputStream(savePath)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    
                    when (backgroundSelectionStatus) {
                        NOW_PLAYING -> MyApp.getPref().edit().putInt(getString(R.string.pref_now_playing_back), 3).apply()
                        DEFAULT_ALBUM_ART -> MyApp.getPref().edit().putInt(getString(R.string.pref_default_album_art), 1).apply()
                        NAVIGATION_DRAWER -> MyApp.getPref().edit().putInt(getString(R.string.pref_nav_library_back), 1).apply()
                        else -> MyApp.getPref().edit().putInt(getString(R.string.pref_nav_library_back), 1).apply()
                    }

                    Toast.makeText(this, "Background successfully updated!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to save file: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            } else {
                val exception = result.error
                Toast.makeText(this, "Error: ${exception?.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val cropOptions = CropImageOptions()
        cropOptions.guidelines = CropImageView.Guidelines.ON
        cropOptions.fixAspectRatio = true
        cropOptions.activityBackgroundColor = Color.BLACK;
        
        when (backgroundSelectionStatus) {
            MAIN_LIB -> {
                cropOptions.aspectRatioX = 11
                cropOptions.aspectRatioY = 16
            }
            NOW_PLAYING -> {
                cropOptions.aspectRatioX = 9
                cropOptions.aspectRatioY = 16
            }
            NAVIGATION_DRAWER -> {
                cropOptions.aspectRatioX = 11
                cropOptions.aspectRatioY = 16
            }
            DEFAULT_ALBUM_ART -> {
                cropOptions.aspectRatioX = 1
                cropOptions.aspectRatioY = 1
            }
            else -> {
                cropOptions.aspectRatioX = 1
                cropOptions.aspectRatioY = 1
            }
        }

        val contractOptions = CropImageContractOptions(null, cropOptions)
        cropImage.launch(contractOptions)
    }
}