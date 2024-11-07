package com.makinul.background.remover.ui.main

import ai.painlog.mmhi.ui.zoomable.MainViewModel
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import com.makinul.background.remover.R
import com.makinul.background.remover.base.BaseActivity
import com.makinul.background.remover.data.model.Line
import com.makinul.background.remover.data.model.Point
import com.makinul.background.remover.databinding.ActivityMainBinding
import com.makinul.background.remover.utils.AppConstants
import com.makinul.background.remover.utils.AppConstants.KEY_IMAGE_TYPE_ASSET
import com.makinul.background.remover.utils.AppConstants.KEY_IMAGE_TYPE_URI
import com.makinul.background.remover.utils.AppConstants.getAssetBitmap
import com.makinul.background.remover.utils.AppConstants.getUriBitmap
import com.makinul.background.remover.utils.Extensions.visible
import com.makinul.background.remover.utils.ImageSegmentHelper
import com.makinul.background.remover.utils.InteractiveSegmentationHelper
import com.mmh.emmahealth.data.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
//                showLog("onProgressChanged fromUser $fromUser, progress $progress")
                if (fromUser) {
                    seekBarProgress = progress
                    binding.overlay.setSeekBarProgress(progress)
                    binding.overlay.setEraseBarSize(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                showLog("onStartTrackingTouch")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                showLog("onStopTrackingTouch")
                hideSeekBarProgress()
            }
        })
        seekBarProgress = binding.seeBar.progress
//        showLog("seekBarProgress $seekBarProgress")
        binding.overlay.setEraseBarSize(seekBarProgress)

        binding.imageResult.setListener(object : ZoomableImageListener {

            override fun onComplete(imageState: ImageState, points: List<Point>) {
                val getCurrentScale = binding.imageResult.getCurrentScale()
                showLog("onComplete: image scale $getCurrentScale, overlay scale $scaleFactor")

//                if (points.isNotEmpty()) {
//                    val point = points[0]
//                    interactiveSegmentationHelper.segment(point.x, point.y)
//                }
//                showLog("points $points")
//                pointArray
//                binding.overlay.setLines()
                val lineArray: ArrayList<Line> = ArrayList()
//                for (i in 1 until points.size) {
//                    lineArray.add(Line(pointA = points[i - 1], pointB = points[i]))
//                }
//                if (points.size > 1) {
//                    lineArray.add(Line(pointA = points[0], pointB = points[points.size - 1]))
//                }
                showLog("minDistance $minDistance, overlayDistancePercentage $overlayDistancePercentage, imageDistancePercentage $imageDistancePercentage")
                for (i in 1 until points.size) {
                    val pointA = points[i - 1]
                    val pointB = points[i]

                    val distance = AppConstants.getDistance(pointA, pointB)
                    showLog("distance $distance")

                    if (distance > minDistance) {

                    }
                }

                binding.overlay.setPoints(points)
                binding.overlay.setLines(lineArray)
                binding.overlay.invalidate()
            }
        })

        val x1 = 0f
        val y1 = 0f
        val x2 = 8f
        val y2 = 8f

        val linePoints = bresenhamLine(x1, y1, x2, y2)
        showLog("Points on the line: $linePoints")
    }

    private fun bresenhamLine(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float
    ): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()

        val dx = kotlin.math.abs(x2 - x1)
        val dy = kotlin.math.abs(y2 - y1)

        val sx = if (x1 < x2) 5f else -5f
        val sy = if (y1 < y2) 5f else -5f

        var err = dx - dy
        var x = x1
        var y = y1

        while (true) {
            points.add(Pair(x, y)) // Add current point to the list

            if (x >= x2 && y >= y2) break // Exit if we've reached the end point

            val e2 = 2 * err
            if (e2 > -dy) {
                err -= dy
                x += sx
            }
            if (e2 < dx) {
                err += dx
                y += sy
            }
        }

        return points
    }

    private var seekBarProgress = 0
    private var seekbarProgressJob: Job? = null

    private fun hideSeekBarProgress() {
//        backgroundScope?.launch(Dispatchers.Main) {
//            delay(1000)
//            binding.overlay.setSeekBarProgress(0)
////            showLog("hideSeekBarProgress")
//        }
        seekbarProgressJob?.cancel()
        seekbarProgressJob = null
        seekbarProgressJob = lifecycleScope.launch(Dispatchers.Main) {
            delay(1000)
            binding.overlay.setSeekBarProgress(0)
        }
    }

    private var imageType: String? = null
    private var imagePath: String? = null

    private fun prepareImageData() {
        intent.extras?.let {
            imageType = it.getString(AppConstants.KEY_IMAGE_TYPE)
            imagePath = it.getString(AppConstants.KEY_IMAGE_PATH)
        } ?: run {
            imageType = KEY_IMAGE_TYPE_ASSET
            imagePath = AppConstants.listOfDemoImagesPath[3]
        }
    }

    private fun setImage() {
        val bitmap = when (imageType) {
            KEY_IMAGE_TYPE_ASSET -> {
                getAssetBitmap(this, imagePath)
            }

            KEY_IMAGE_TYPE_URI -> {
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
        imageDistance = AppConstants.getDistance(
            Point(0f, 0f),
            Point(imageWidth.toFloat(), imageHeight.toFloat())
        )
        imageDistancePercentage = imageDistance / 100f

        binding.overlay.post {
            val overlayWidth = binding.overlay.width
            val overlayHeight = binding.overlay.height
            scaleFactor = min(
                (overlayWidth.toFloat() / imageWidth.toFloat()),
                (overlayHeight.toFloat() / imageHeight.toFloat())
            )
            overlayDistancePercentage = AppConstants.getDistance(
                Point(0f, 0f),
                Point(overlayWidth.toFloat(), overlayHeight.toFloat())
            ) / 100f

            minDistance = min(overlayDistancePercentage, imageDistancePercentage) / 2f
        }
        prepareHelper(bitmap)
//        prepareImageSegmentation(bitmap)
//        saveBitmapToLocalStorage(bitmap, "New again")
    }

    private var imageWidth: Int = -1
    private var imageHeight: Int = -1
    private var imageDistance: Float = -1f
    private var imageDistancePercentage: Float = -1f
    private var scaleFactor: Float = 1f
    private var overlayDistancePercentage: Float = -1f
    private var minDistance: Float = -1f

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    private fun prepareHelper(rawBitmap: Bitmap) {
        backgroundExecutor = Executors.newSingleThreadExecutor()
        // Create the PoseLandmarkHelper that will handle the inference
        backgroundExecutor.execute {
            binding.progressBar.visible()
//            val poseLandmarkHelper = PoseLandmarkHelper(
//                context = this@MainActivity
//            )
//            val selfieSegmentHelper = ImageSegmentHelper(
//                context = this@MainActivity,
//                currentModel = ImageSegmentHelper.MODEL_SELFIE_SEGMENTER
//            )
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
                null,
                null,
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

        val scaleWidth = (processedBitmap.width * scaleFactor).toInt()
        val scaleHeight = (processedBitmap.height * scaleFactor).toInt()
        val maskBitmap =
            Bitmap.createScaledBitmap(processedBitmap, scaleWidth, scaleHeight, false)
        showLog("processedBitmap $processedBitmap")

        binding.overlay.setMaskBitmap(maskBitmap)
        binding.overlay.invalidate()

//        binding.imageResult.setImageBitmap(processedBitmap)
//        saveBitmapToLocalStorage(bitmap = processedBitmap, "New Image")
    }

    private fun prepareImageSegmentation(rawBitmap: Bitmap) {
        val interactiveSegmentationHelper = InteractiveSegmentationHelper(
            context = this@MainActivity,
            interactiveSegmentationListener
        )
        interactiveSegmentationHelper.setInputImage(
            rawBitmap
        )
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

    private var optionMenu: Menu? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        optionMenu = menu
        menuInflater.inflate(R.menu.main_top_item_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        updateHistoryMenuButtons()
        return super.onPrepareOptionsMenu(menu)
    }

    private fun updateHistoryMenuButtons() {
        optionMenu?.let { menu ->
            val undoActionItem = menu.findItem(R.id.action_undo)
            val redoActionItem = menu.findItem(R.id.action_redo)

            var undoActionDrawable = undoActionItem.icon
            undoActionDrawable = DrawableCompat.wrap(undoActionDrawable!!)
            DrawableCompat.setTint(undoActionDrawable, ContextCompat.getColor(this, R.color.gray))
            undoActionItem.setIcon(undoActionDrawable)

            var redoActionDrawable = redoActionItem.icon
            redoActionDrawable = DrawableCompat.wrap(redoActionDrawable!!)
            DrawableCompat.setTint(redoActionDrawable, ContextCompat.getColor(this, R.color.gray))
            redoActionItem.setIcon(redoActionDrawable)

            undoActionItem.setEnabled(false)
            redoActionItem.setEnabled(false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        when (item.itemId) {
            android.R.id.home -> {
                finish() // close this activity and return to preview activity (if there is any)
            }

            R.id.action_save -> {
//                requestStoragePermission()
//                saveBitmapToLocalStorage()
            }

            else -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    // Register ActivityResult handler
//    private val requestPermissions =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
//            // Handle permission requests results
//            // See the permission example in the Android platform samples: https://github.com/android/platform-samples
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                if (results[READ_MEDIA_IMAGES] == true) {
//                    showLog("READ_MEDIA_IMAGES")
//                }
//            } else {
//                if (results[WRITE_EXTERNAL_STORAGE] == true) {
//                    showLog("WRITE_EXTERNAL_STORAGE")
//                }
//            }
//        }
//
//    // Check permission
//    private fun checkStoragePermission(): Boolean {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            ContextCompat.checkSelfPermission(
//                this,
//                READ_MEDIA_IMAGES
//            ) == PackageManager.PERMISSION_GRANTED
//        } else {
//            ContextCompat.checkSelfPermission(
//                this,
//                WRITE_EXTERNAL_STORAGE
//            ) == PackageManager.PERMISSION_GRANTED
//        }
//    }
//
//    private fun requestStoragePermission() {
//        // Permission request logic
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES))
//        } else {
//            requestPermissions.launch(arrayOf(WRITE_EXTERNAL_STORAGE))
//        }
//    }

    private fun saveBitmapToLocalStorage(
        bitmap: Bitmap,
        displayName: String
    ) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/" + getString(R.string.app_name)
                )
            }

            val imageUri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let { uri ->
                contentResolver.openOutputStream(uri).use { outputStream: OutputStream? ->
                    outputStream?.let {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                        it.flush()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        showLog("onDestroyView")
        seekbarProgressJob?.cancel()
        super.onDestroy()
    }
}