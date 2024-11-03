package com.makinul.background.remover.ui.main

import ai.painlog.mmhi.ui.zoomable.MainViewModel
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.makinul.background.remover.R
import com.makinul.background.remover.base.BaseActivity
import com.makinul.background.remover.data.model.Point
import com.makinul.background.remover.databinding.ActivityMainBinding
import com.makinul.background.remover.utils.AppConstants
import com.makinul.background.remover.utils.AppConstants.getAssetBitmap
import com.makinul.background.remover.utils.AppConstants.getUriBitmap
import com.makinul.background.remover.utils.Extensions.visible
import com.makinul.background.remover.utils.ImageSegmentHelper
import com.makinul.background.remover.utils.InteractiveSegmentationHelper
import com.makinul.background.remover.utils.PoseLandmarkHelper
import com.mmh.emmahealth.data.Status
import dagger.hilt.android.AndroidEntryPoint
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.min

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewModel.maskedBitmapArray.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.SUCCESS -> {
                        binding.progressBar.hide()
                        it.data?.let { bitmapArray ->
                            prepareMaskedBitmap(bitmapArray)
                        }
                    }

                    Status.ERROR -> {
                        binding.progressBar.hide()
                    }

                    Status.LOADING -> {
                        binding.progressBar.visible()
                    }
                }
            }
        }

        prepareImageData()
        setImage()

        binding.seeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                showLog("onProgressChanged fromUser $fromUser, progress $progress")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                showLog("onStartTrackingTouch")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                showLog("onStopTrackingTouch")
            }
        })
    }

    private var imageType: String? = null
    private var imagePath: String? = null

    private fun prepareImageData() {
        intent.extras?.let {
            imageType = it.getString(AppConstants.KEY_IMAGE_TYPE)
            imagePath = it.getString(AppConstants.KEY_IMAGE_PATH)
        }
    }

    private fun setImage() {
        val bitmap = when (imageType) {
            AppConstants.KEY_IMAGE_TYPE_ASSET -> {
                getAssetBitmap(this, imagePath)
            }

            AppConstants.KEY_IMAGE_TYPE_URI -> {
                getUriBitmap(this, imagePath)
            }

            else -> {
                return
            }
        }

        if (bitmap == null)
            return

        binding.imageResult.setImageBitmap(bitmap)

        imageWidth = bitmap.width
        imageHeight = bitmap.height

        scaleFactor = min(
            (binding.overlay.width.toFloat() / imageWidth.toFloat()),
            (binding.overlay.height.toFloat() / imageHeight.toFloat())
        )

//        prepareHelper(bitmap)
//        prepareImageSegmentation(bitmap)

        saveBitmapToLocalStorage(bitmap, "New again")
    }

    private var imageWidth: Int = -1
    private var imageHeight: Int = -1
    private var scaleFactor: Float = 1f

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    private fun prepareHelper(rawBitmap: Bitmap) {
        backgroundExecutor = Executors.newSingleThreadExecutor()
        // Create the PoseLandmarkHelper that will handle the inference
        backgroundExecutor.execute {
            binding.progressBar.visible()
            val poseLandmarkHelper = PoseLandmarkHelper(
                context = this@MainActivity
            )
            val selfieSegmentHelper = ImageSegmentHelper(
                context = this@MainActivity,
                currentModel = ImageSegmentHelper.MODEL_SELFIE_SEGMENTER
            )
            val selfieMulticlassSegmentHelper = ImageSegmentHelper(
                context = this@MainActivity,
                currentModel = ImageSegmentHelper.MODEL_SELFIE_MULTICLASS
            )

            binding.progressBar.hide()
            viewModel.processBitmapToRemoveBackground(
                rawBitmap = rawBitmap,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                scaleFactor = scaleFactor,
                poseLandmarkHelper,
                selfieSegmentHelper,
                selfieMulticlassSegmentHelper
            )
        }
    }

    private fun prepareMaskedBitmap(bitmapArray: IntArray) {
        val processedBitmap = Bitmap.createBitmap(
            bitmapArray,
            imageWidth,
            imageHeight,
            Bitmap.Config.ARGB_8888
        )

//        val scaleWidth = (processedBitmap.width * scaleFactor).toInt()
//        val scaleHeight = (processedBitmap.height * scaleFactor).toInt()
//        val maskBitmap =
//            Bitmap.createScaledBitmap(processedBitmap, scaleWidth, scaleHeight, false)

        showLog("processedBitmap $processedBitmap")
        binding.imageResult.setImageBitmap(processedBitmap)

        saveBitmapToLocalStorage(bitmap = processedBitmap, "New Image")
    }

    private fun prepareImageSegmentation(rawBitmap: Bitmap) {
        val interactiveSegmentationHelper = InteractiveSegmentationHelper(
            context = this@MainActivity,
            interactiveSegmentationListener
        )
        interactiveSegmentationHelper.setInputImage(
            rawBitmap
        )

        binding.imageResult.setListener(object : ZoomableImageListener {
            override fun onDrag() {
                showLog("onDrag")
            }

            override fun onEdit(points: List<Point>) {
                if (points.isNotEmpty()) {
                    val point = points[0]
                    interactiveSegmentationHelper.segment(point.x, point.y)
                }
            }
        })
    }

    private val interactiveSegmentationListener: InteractiveSegmentationHelper.InteractiveSegmentationListener =
        object : InteractiveSegmentationHelper.InteractiveSegmentationListener {
            override fun onError(error: String) {
                showLog("error $error")
            }

            override fun onResults(result: InteractiveSegmentationHelper.ResultBundle?) {
                showLog("result $result")

                result?.let {
                    setMaskResult(
                        it.byteBuffer,
                        it.maskWidth,
                        it.maskHeight
                    )
                }
            }
        }

    /**
     * Converts byteBuffer to mask bitmap
     * Scales the bitmap to match the view
     */
    fun setMaskResult(byteBuffer: ByteBuffer, maskWidth: Int, maskHeight: Int) {
        val pixels = IntArray(byteBuffer.capacity())
        for (i in pixels.indices) {
            val index = byteBuffer.get(i).toInt()
            val color = if (index == 0) Color.TRANSPARENT else Color.RED
            pixels[i] = color
        }

        val bitmap = Bitmap.createBitmap(
            pixels,
            maskWidth,
            maskHeight,
            Bitmap.Config.ARGB_8888
        )

//        // Assumes portrait for this sample, but scaling can be adjusted for landscape.
//        // Code for selecting orientation excluded for sample simplicity
//        val scaleFactor =
//            min(width * 1f / bitmap.width, height * 1f / bitmap.height)
//        val scaleWidth = (bitmap.width * scaleFactor).toInt()
//        val scaleHeight = (bitmap.height * scaleFactor).toInt()
//        maskBitmap =
//            Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, false)
//        invalidate()

        binding.imageResult.setImageBitmap(bitmap)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_top_item_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        when (item.itemId) {
            android.R.id.home -> {
                finish() // close this activity and return to preview activity (if there is any)
            }

            R.id.action_save -> {
                requestStoragePermission()
            }

            else -> {
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // Register ActivityResult handler
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            // Handle permission requests results
            // See the permission example in the Android platform samples: https://github.com/android/platform-samples

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (results[READ_MEDIA_IMAGES] == true) {
                    showLog("READ_MEDIA_IMAGES")
                }
            } else {
                if (results[WRITE_EXTERNAL_STORAGE] == true) {
                    showLog("WRITE_EXTERNAL_STORAGE")
                }
            }
        }

    // Check permission
    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        // Permission request logic
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES))
        } else {
            requestPermissions.launch(arrayOf(WRITE_EXTERNAL_STORAGE))
        }
    }

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_AGAINST_NOT_NOTHING_EXPECTED_TYPE")
    private fun saveBitmapToLocalStorage(
        bitmap: Bitmap,
        displayName: String
    ): Boolean {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/YourAppName"
                )
            }

            val imageUri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let { uri ->
                contentResolver.openOutputStream(uri).use { outputStream: OutputStream? ->
                    outputStream?.let {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                        it.flush()
                        return@saveBitmapToLocalStorage true
                    }
                }
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}