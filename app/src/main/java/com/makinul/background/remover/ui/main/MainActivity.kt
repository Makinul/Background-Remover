package com.makinul.background.remover.ui.main

import ai.painlog.mmhi.ui.zoomable.MainViewModel
import android.content.ContentValues
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import com.makinul.background.remover.R
import com.makinul.background.remover.base.BaseActivity
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
import kotlin.math.abs
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

        binding.seeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.overlay.clearPoints()
                binding.overlay.clearLines()
                if (fromUser) {
                    seekBarProgress = progress
                    binding.overlay.setSeekBarProgress(progress)
                    binding.overlay.setEraseBarSize(progress)
                }
                binding.overlay.invalidate()
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
        binding.overlay.setEraseBarSize(seekBarProgress)

        binding.imageResult.setActionListener(object : ActionListener {

            private val pointArray: ArrayList<Point> = ArrayList()

            override fun onEdit(point: Point) {
                if (pointArray.isNotEmpty()) {
                    val pointA = pointArray[pointArray.size - 1]
                    val distance = AppConstants.getDistance(pointA, point)
                    if (distance > minDistance) {
                        val points = ddaLine(pointArray[pointArray.size - 1], point)
                        pointArray.addAll(points)
                    }
                    pointArray.add(point)
                } else {
                    pointArray.add(point)
                }

                binding.overlay.setPoints(pointArray)
                binding.overlay.invalidate()
            }

            override fun onDragStarted() {
                pointArray.clear()
//                binding.overlay.setPoints(pointArray)
//                binding.overlay.invalidate()
            }

            override fun onComplete(imageState: ImageState, points: List<Point>) {
                val tmpArray: ArrayList<Point> = ArrayList()
                tmpArray.addAll(pointArray)
                if (imageState == ImageState.EDIT) {
                    editBitmap(tmpArray)
                } else if (imageState == ImageState.ZOOM) {
                    pointArray.clear()

                    val currentState = binding.imageResult.getCurrentScale()
                    showLog("currentState $currentState")

                    binding.overlay.setPoints(pointArray)
                    binding.overlay.invalidate()

                    return
                }
                pointArray.clear()
//                showLog("pointArray $pointArray")
            }
        })

        binding.erase.setOnClickListener {
//            val pointArray = ArrayList<Point>()
//            pointArray.add(Point(x = 200f, y = 200f)) // image view x && y position
//            editBitmap(pointArray)

            binding.erase.isSelected = true
            binding.restore.isSelected = false
        }

        binding.restore.setOnClickListener {
            binding.erase.isSelected = false
            binding.restore.isSelected = true
        }
        binding.erase.isSelected = true

        prepareImageData()
        setImage()
//        showErrorAlert()
    }

    private fun editBitmap(editPointArray: List<Point>) {
        lifecycleScope.launch(Dispatchers.Main) {

            val viewWidth = binding.imageResult.width
            val viewHeight = binding.imageResult.height

            scaleFactor = min(
                (viewWidth.toFloat() / bitmapWidth.toFloat()),
                (viewHeight.toFloat() / bitmapHeight.toFloat())
            )

            val bitmapScaledWidth = (bitmapWidth * scaleFactor).toInt()
            val bitmapScaledHeight = (bitmapHeight * scaleFactor).toInt()

//            showLog("scaleFactor $scaleFactor")
//            val bitmapArray = IntArray(bitmapWidth * bitmapHeight)
//            rawBitmap!!.getPixels(bitmapArray, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight)
            val imageEditType = if (binding.restore.isSelected) {
                ImageEditType.RESTORE
            } else {
                ImageEditType.ERASE
            }
            val radius = (seekBarProgress / scaleFactor).toInt()
            val item = ImageEdit(
                type = imageEditType,
                pointArray = editPointArray,
                radius = radius
            )

            for (index in items.size - 1 downTo (currentItemPosition + 1)) {
                items.removeAt(index)
            }

            items.add(item)
            currentItemPosition += 1

            val leftPosition = abs(viewWidth - bitmapScaledWidth) / 2f
            val topPosition = abs(viewHeight - bitmapScaledHeight) / 2f
//            showLog("leftPosition $leftPosition, topPosition $topPosition")

            for (point in editPointArray) {
                val selectedX = point.x // * scaleFactor // image view position x
                val selectedY = point.y // * scaleFactor // image view position y

                val x = (selectedX - leftPosition) / scaleFactor
                val y = (selectedY - topPosition) / scaleFactor

//                showLog("x $x, y $y")
//                val i = ((y * bitmapWidth) + x).toInt()
//                bitmapArray[i] = Color.RED

                val circleAreaPoints = circleAreaPoints(x, y, radius)
                for (circlePoint in circleAreaPoints) {
                    val circleX = circlePoint.first
                    val circleY = circlePoint.second
                    if (circleX < 0 || circleX > bitmapWidth || circleY < 0 || circleY > bitmapHeight)
                        continue

                    val i = ((circleY * bitmapWidth) + circleX).toInt()
                    if (i + 1 >= (bitmapWidth * bitmapHeight))
                        continue

                    if (imageEditType == ImageEditType.RESTORE) {
                        val pixel = rawBitmap!!.getPixel(circleX.toInt(), circleY.toInt())
                        val previousColor = Color.rgb(
                            Color.red(pixel),
                            Color.green(pixel),
                            Color.blue(pixel)
                        )
                        processedBitmapArray[i] = previousColor
                    } else {
                        processedBitmapArray[i] = Color.TRANSPARENT
                    }
                }
//                val i = (height * y.toInt()) + x.toInt()
//                bitmapArray[i] = Color.TRANSPARENT
            }

//            for (y in 0 until height) {
//                for (x in 0 until width) {
//                    if (x % 10 == 0 && y % 10 == 0) {
//                        val i = (y * width) + x
//                        bitmapArray[i] = Color.TRANSPARENT
//                    }
//                }
//            }
//            val processedBitmap = Bitmap.createBitmap(
//                bitmapArray, imageWidth, imageHeight, Bitmap.Config.ARGB_8888
//            )

            prepareMaskedBitmap(processedBitmapArray)
            binding.overlay.clearPoints()
            binding.overlay.invalidate()

            updateHistoryMenuButtons()
//            binding.imageResult.setImageBitmap(processedBitmap)
        }
    }

    private fun ddaLine(pointA: Point, pointB: Point): List<Point> {
        val points = mutableListOf<Point>()

        val x1 = pointA.x
        val x2 = pointB.x
        val y1 = pointA.y
        val y2 = pointB.y

        val dx = x2 - x1
        val dy = y2 - y1

        // Determine the number of steps required
        val steps = maxOf(abs(dx), abs(dy)).toInt() / 2

        // Calculate the increments
        val xInc = dx / steps
        val yInc = dy / steps

        // Starting point
        var x = x1
        var y = y1

//        showLog("steps $steps")
//        showLog("xInc $xInc, yInc $yInc")

        for (i in 0..steps) {
            // Add the current point to the list
            points.add(Point(x, y))
            x += xInc
            y += yInc
        }

        return points
    }

    private fun circleAreaPoints(xc: Float, yc: Float, radius: Int): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()

        // Define the bounding box with floating-point bounds rounded to integers
        val xMin = kotlin.math.floor(xc - radius).toInt()
        val xMax = kotlin.math.ceil(xc + radius).toInt()
        val yMin = kotlin.math.floor(yc - radius).toInt()
        val yMax = kotlin.math.ceil(yc + radius).toInt()

        // Check each point within the bounding box
        for (x in xMin..xMax) {
            for (y in yMin..yMax) {
                // Calculate the squared distance from the center to this point
                if ((x - xc) * (x - xc) + (y - yc) * (y - yc) <= (radius * radius).toFloat()) {
                    points.add(Pair(x.toFloat(), y.toFloat())) // Add point as a floating-point pair
                }
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
            imagePath = AppConstants.listOfDemoImagesPath[4]
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

        if (bitmap == null) return

        rawBitmap = bitmap
        bitmapWidth = bitmap.width
        bitmapHeight = bitmap.height

        processedBitmapArray = IntArray(bitmapWidth * bitmapHeight)
        rawBitmap!!.getPixels(processedBitmapArray, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight)

        binding.imageResult.setImageBitmap(rawBitmap)

        imageDistance = AppConstants.getDistance(
            Point(0f, 0f), Point(bitmapWidth.toFloat(), bitmapHeight.toFloat())
        )
        imageDistancePercentage = imageDistance / 100f

        binding.overlay.post {
            val overlayWidth = binding.overlay.width
            val overlayHeight = binding.overlay.height
            scaleFactor = min(
                (overlayWidth.toFloat() / bitmapWidth.toFloat()),
                (overlayHeight.toFloat() / bitmapHeight.toFloat())
            )
            overlayDistancePercentage = AppConstants.getDistance(
                Point(0f, 0f), Point(overlayWidth.toFloat(), overlayHeight.toFloat())
            ) / 100f
//            showLog("scaleFactor $scaleFactor")
            minDistance = min(overlayDistancePercentage, imageDistancePercentage) / 2f
        }
//        prepareHelper(bitmap)
//        prepareImageSegmentation(bitmap)
//        saveBitmapToLocalStorage(bitmap, "New again")
//        binding.erase.performClick()
    }

    private var rawBitmap: Bitmap? = null
    private var bitmapWidth: Int = -1
    private var bitmapHeight: Int = -1
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
                imageWidth = bitmapWidth,
                imageHeight = bitmapHeight,
                scaleFactor = scaleFactor,
                null,
                null,
                selfieMulticlassSegmentHelper
            )
        }
    }

    private var processedBitmapArray: IntArray = IntArray(0)

    private fun prepareMaskedBitmap(bitmapArray: IntArray) {
        processedBitmapArray = bitmapArray
        val processedBitmap = Bitmap.createBitmap(
            bitmapArray, bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888
        )

        val scaleWidth = (processedBitmap.width * scaleFactor).toInt()
        val scaleHeight = (processedBitmap.height * scaleFactor).toInt()
        val maskBitmap = Bitmap.createScaledBitmap(processedBitmap, scaleWidth, scaleHeight, false)

        val overlayWidth = binding.overlay.width
        val overlayHeight = binding.overlay.height

        val leftPosition = abs(overlayWidth - scaleWidth) / 2f
        val topPosition = abs(overlayHeight - scaleHeight) / 2f

        binding.overlay.setMaskBitmap(maskBitmap, leftPosition, topPosition)
        binding.overlay.invalidate()

        binding.imageResult.setImageBitmap(null)
//        saveBitmapToLocalStorage(bitmap = processedBitmap, "New Image")
    }

    private fun prepareImageSegmentation(rawBitmap: Bitmap) {
        val interactiveSegmentationHelper = InteractiveSegmentationHelper(
            context = this@MainActivity, interactiveSegmentationListener
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
//
//                result?.let {
//                    setMaskResult(
//                        it.byteBuffer, it.maskWidth, it.maskHeight
//                    )
//                }
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
            pixels, maskWidth, maskHeight, Bitmap.Config.ARGB_8888
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

    private val items: ArrayList<ImageEdit> = ArrayList()
    private var currentItemPosition = -1

    private fun updateHistoryMenuButtons() {
        optionMenu?.let { menu ->
            val undoActionItem = menu.findItem(R.id.action_undo)
            val redoActionItem = menu.findItem(R.id.action_redo)

            if (items.isEmpty()) {
                changeEditActionButton(undoActionItem, false)
                changeEditActionButton(redoActionItem, false)
            } else {
                if (currentItemPosition >= items.size - 1) {
                    changeEditActionButton(undoActionItem, true)
                    changeEditActionButton(redoActionItem, false)
                } else if (currentItemPosition < 0) {
                    changeEditActionButton(undoActionItem, false)
                    changeEditActionButton(redoActionItem, true)
                } else {
                    changeEditActionButton(undoActionItem, true)
                    changeEditActionButton(redoActionItem, true)
                }
            }
        }
    }

    private fun changeEditActionButton(actionItem: MenuItem, enable: Boolean = false) {
        var undoActionDrawable = actionItem.icon
        undoActionDrawable = DrawableCompat.wrap(undoActionDrawable!!)
        if (enable) {
            DrawableCompat.setTint(
                undoActionDrawable,
                ContextCompat.getColor(this, R.color.white)
            )
        } else {
            DrawableCompat.setTint(
                undoActionDrawable,
                ContextCompat.getColor(this, R.color.gray)
            )
        }
        actionItem.setIcon(undoActionDrawable)
        actionItem.setEnabled(enable)
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
//                val bitmap = binding.imageResult.drawable.toBitmap()
                val processedBitmap = Bitmap.createBitmap(
                    processedBitmapArray, bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888
                )
                saveBitmapToLocalStorage(processedBitmap)
            }

            R.id.action_undo -> {
                currentItemPosition -= 1
                updateHistoryMenuButtons()
                updateEditedView()
            }

            R.id.action_redo -> {
                currentItemPosition += 1
                updateHistoryMenuButtons()
                updateEditedView()
            }

            else -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateEditedView() {
        lifecycleScope.launch(Dispatchers.Main) {
            val viewWidth = binding.imageResult.width
            val viewHeight = binding.imageResult.height

            scaleFactor = min(
                (viewWidth.toFloat() / bitmapWidth.toFloat()),
                (viewHeight.toFloat() / bitmapHeight.toFloat())
            )

            val bitmapScaledWidth = (bitmapWidth * scaleFactor).toInt()
            val bitmapScaledHeight = (bitmapHeight * scaleFactor).toInt()

            val bitmapArray = IntArray(bitmapWidth * bitmapHeight)
            rawBitmap!!.getPixels(bitmapArray, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight)

            val leftPosition = abs(viewWidth - bitmapScaledWidth) / 2f
            val topPosition = abs(viewHeight - bitmapScaledHeight) / 2f
//            showLog("leftPosition $leftPosition, topPosition $topPosition")

            for (index in 0..currentItemPosition) {
                val item = items[index]

                for (point in item.pointArray) {
                    val selectedX = point.x // * scaleFactor // image view position x
                    val selectedY = point.y // * scaleFactor // image view position y

                    val x = (selectedX - leftPosition) / scaleFactor
                    val y = (selectedY - topPosition) / scaleFactor

                    val circleAreaPoints = circleAreaPoints(x, y, item.radius)

                    for (circlePoint in circleAreaPoints) {
                        val circleX = circlePoint.first
                        val circleY = circlePoint.second
                        if (circleX < 0 || circleX > bitmapWidth || circleY < 0 || circleY > bitmapHeight)
                            continue

                        val i = ((circleY * bitmapWidth) + circleX).toInt()
                        if (i + 1 >= (bitmapWidth * bitmapHeight))
                            continue

                        if (item.type == ImageEditType.RESTORE) {
                            val pixel = rawBitmap!!.getPixel(circleX.toInt(), circleY.toInt())
                            val previousColor = Color.rgb(
                                Color.red(pixel),
                                Color.green(pixel),
                                Color.blue(pixel)
                            )
                            bitmapArray[i] = previousColor
                        } else {
                            bitmapArray[i] = Color.TRANSPARENT
                        }
                    }
                }
            }

            prepareMaskedBitmap(bitmapArray)
            binding.overlay.invalidate()
        }
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

    private fun saveBitmapToLocalStorage(bitmap: Bitmap) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "${System.currentTimeMillis()}.png")
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

    private fun showErrorAlert() {
        // on below line we are creating a variable for builder to build our alert dialog and passing a custom theme to it.
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        // on below line we are setting message for our alert dialog.
        builder.setMessage("Welcome to Tutorials Point")
        // on below line we are setting title for our alert dialog.
        builder.setTitle("Welcome")
        // on below line we are setting cancelable for our alert dialog.
        builder.setCancelable(false)
        // on below line we are setting positive button for our alert dialog and adding click listener to it.
        builder.setPositiveButton("Cancel", { dialog: DialogInterface, which: Int ->
            // below method is use to dismiss the dialog.
            dialog.cancel()
        })

        // on below line we are Creating the Alert dialog
        val alertDialog: AlertDialog = builder.create()
        // on below line we are displaying our alert dialog.
        alertDialog.show()
    }

    private fun showLog(message: String = getString(R.string.test_message)) {
        AppConstants.showLog(TAG, message)
    }

    private fun showLog(@StringRes message: Int) {
        showLog(getString(message))
    }

    override fun onDestroy() {
        seekbarProgressJob?.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    enum class ImageEditType {
        ERASE, RESTORE
    }

    data class ImageEdit(
        val type: ImageEditType,
        val pointArray: List<Point> = emptyList(),
        val radius: Int
    )
}