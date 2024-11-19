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
import androidx.core.graphics.drawable.toBitmap
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

//                binding.overlay.setPoints(pointArray)
//                binding.overlay.invalidate()
            }

            override fun onDragStarted() {
                pointArray.clear()
//                binding.overlay.setPoints(pointArray)
//                binding.overlay.invalidate()
            }

            override fun onComplete(imageState: ImageState, points: List<Point>) {
//                val getCurrentScale = binding.imageResult.getCurrentScale()
//                showLog("onComplete: image scale $getCurrentScale, overlay scale $scaleFactor")
////                if (points.isNotEmpty()) {
////                    val point = points[0]
////                    interactiveSegmentationHelper.segment(point.x, point.y)
////                }
////                showLog("points $points")
////                pointArray
////                binding.overlay.setLines()
//                val pointArray: ArrayList<Point> = ArrayList()
//                val lineArray: ArrayList<Line> = ArrayList()
//                showLog("minDistance $minDistance, overlayDistancePercentage $overlayDistancePercentage, imageDistancePercentage $imageDistancePercentage")
//                for (i in 1 until points.size) {
//                    val pointA = points[i - 1]
//                    pointArray.add(pointA)
//                    val pointB = points[i]
////                    lineArray.add(Line(pointA, pointB))
//                    showLog("pointA $pointA, pointB $pointB")
//                    val distance = AppConstants.getDistance(pointA, pointB)
//                    showLog("distance $distance")
//                    if (distance > minDistance) {
//                        val linePoints = ddaLine(pointA, pointB)
//                        pointArray.addAll(linePoints)
//                    }
//                    pointArray.add(pointB)
//                }
//
////                if ()
////                for (point in pointArray) {
////                    showLog("point $point")
////                }
//
////                for (i in 1 until points.size) {
////                    val pointA = points[i - 1]
////                    val pointB = points[i]
////
////                    val distance = AppConstants.getDistance(pointA, pointB)
////                    if (distance > minDistance) {
////                        showLog("distance $distance")
////                    }
////                }
//                binding.overlay.setPoints(pointArray)
////                binding.overlay.setLines(lineArray)
//                binding.overlay.invalidate()

                if (imageState == ImageState.EDIT) {
                    editBitmap(ArrayList(points))
                }
                pointArray.clear()
            }
        })

        binding.erase.setOnClickListener {
////            val points: ArrayList<Point> = ArrayList()
//            val pointA = Point(x = 480f, y = 600f)
//            val pointB = Point(x = 400f, y = 600f)
////            val pointA = Point(x = 400f, y = 640f)
////            val pointB = Point(x = 480f, y = 600f)
////            val distance = AppConstants.getDistance(pointA, pointB)
////            val minDistance = 18.926826f
////            val minDistance = 1f
////            showLog("distance $distance")
////            points.add(pointA)
////            val linePoints = findIntersectPoints(pointA, pointB, minDistance)
////            points.addAll(linePoints)
////            points.add(pointB)
////            showLog("Points on the line: $linePoints")
//            val linePoints = ddaLine(pointA, pointB)
////            for (point in linePoints) {
////                showLog("point $point")
////            }
//            binding.overlay.setPoints(linePoints)
//            binding.overlay.invalidate()

//            val x = 5f
//            val y = 5f
//            val seekBarProgress = 3
//            val circlePoints = circlePoints(x.toInt(), y.toInt(), seekBarProgress)
//            for (point in circlePoints) {
//                showLog("point $point")
//            }
//            binding.overlay.setPoints(circlePoints)
//            binding.overlay.invalidate()

//            val xc = 5  // Center x
//            val yc = 5  // Center y
//            val radius = 3
//            val areaPoints = circleAreaPoints(xc, yc, radius)
//            showLog("Points within the circle: ${areaPoints.size}")
//            for (point in areaPoints) {
//                showLog("point $point")
//            }
            val pointArray = ArrayList<Point>()
            pointArray.add(Point(x = 100f, y = 100f))
            editBitmap(pointArray)

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
    }

    private fun startEditSelectedPoints(pointArray: ArrayList<Point>) {
//        val x = pointArray[0].x / scaleFactor // image view position x
//        val y = pointArray[0].y / scaleFactor // image view position y
//
//        val imageScale = binding.imageResult.getCurrentScale()
//        val matrixValues = binding.imageResult.getMatrixValues()
//        showLog("startEditSelectedPoints x $x, y $y")
//        showLog("startEditSelectedPoints imageWidth $imageWidth, imageHeight $imageHeight")
//        showLog("startEditSelectedPoints imageResult width ${binding.imageResult.width}, imageResult height ${binding.imageResult.height}")
//
//        val pixel = rawBitmap!!.getPixel(x.toInt(), y.toInt())
//        val circleAreaPoints = circleAreaPoints(x.toInt(), y.toInt(), seekBarProgress)

//        val circlePoints = circlePoints(x.toInt(), y.toInt(), seekBarProgress / 2)
//        showLog("circlePoints $circlePoints")

        for (point in pointArray) {
            val x = point.x / scaleFactor // image view position x
            val y = point.y / scaleFactor // image view position y

//            val imageScale = binding.imageResult.getCurrentScale()
//            val matrixValues = binding.imageResult.getMatrixValues()
//            showLog("startEditSelectedPoints x $x, y $y")
//            showLog("startEditSelectedPoints imageWidth $imageWidth, imageHeight $imageHeight")
//            showLog("startEditSelectedPoints imageResult width ${binding.imageResult.width}, imageResult height ${binding.imageResult.height}")

//            val pixel = rawBitmap!!.getPixel(x.toInt(), y.toInt())
            val circleAreaPoints = circleAreaPoints(x.toInt(), y.toInt(), seekBarProgress)
            showLog()
            for (circlePoint in circleAreaPoints) {
                rawBitmap!!.setPixel(circlePoint.first, circlePoint.second, Color.TRANSPARENT)
            }
        }
        binding.imageResult.setImageBitmap(rawBitmap)
    }

    private fun editBitmap(pointArray: List<Point>) {
        lifecycleScope.launch(Dispatchers.Main) {
            val width: Int = rawBitmap!!.width
            val height: Int = rawBitmap!!.height

            val bitmapArray = IntArray(width * height)
            rawBitmap!!.getPixels(bitmapArray, 0, width, 0, 0, width, height)

            for (point in pointArray) {
                val selectedX = point.x // scaleFactor // image view position x
                val selectedY = point.y // scaleFactor // image view position y
                showLog("selectedX $selectedX, selectedY $selectedY")
                val circleAreaPoints =
                    circleAreaPoints(selectedX.toInt(), selectedY.toInt(), seekBarProgress)
//                showLog("circleAreaPoints $circleAreaPoints")
                for (circlePoint in circleAreaPoints) {
                    val x = circlePoint.first
                    val y = circlePoint.second
                    val i = (y * width) + x
                    showLog("i $i")
                    showLog("x $x, y $y")
                    bitmapArray[i] = Color.TRANSPARENT
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

            val processedBitmap = Bitmap.createBitmap(
                bitmapArray, width, height, Bitmap.Config.ARGB_8888
            )

            binding.imageResult.setImageBitmap(processedBitmap)
        }
    }

    private fun findIntersectPoints(pointA: Point, pointB: Point, minDistance: Float): List<Point> {
        val points = mutableListOf<Point>()

        val x1 = pointA.x
        val x2 = pointB.x
        val y1 = pointA.y
        val y2 = pointB.y

        val dx = abs(x2 - x1)
        val dy = abs(y2 - y1)

        var x: Float = x1
        var y: Float = y1

        val sx = if (x1 < x2) {
            1f * minDistance
        } else {
            -1f * minDistance
        }
        val sy = if (y1 < y2) {
            minDistance
        } else {
            -minDistance
        }
//        showLog("sx $sx, sy $sy")
        var err = dx - dy
        while (true) {
            val e2 = 2 * err
            if (e2 > -dy) {
                err -= dy
                x += sx
            }
            if (e2 < dx) {
                err += dx
                y += sy
            }
//            showLog("Point $x, $y")
            // Exit if we've reached the end point
            if (x1 == x2 && y1 == y2) {
                return points
            } else if (x1 == x2 || y1 == y2) {
                if (y1 == y2) {
                    if (x1 > x2) {
                        if (x <= x2) {
                            break
                        }
                    } else { // x2 > x1
                        if (x >= x2) {
                            break
                        }
                    }
                } else { // x1 == x2
                    if (y1 > y2) {
                        if (y <= y2) {
                            break
                        }
                    } else { // y2 > y1
                        if (y >= y2) {
                            break
                        }
                    }
                }
            } else { // none of them are equals
                if (x2 > x1 && y2 > y1) { // bottom right
                    if (x >= x2 && y >= y2) {
                        break
                    }
                    if (x >= x2 || y >= y2) {
                        continue
                    }
                } else if (x1 > x2 && y1 > y2) { // top left
                    if (x <= x2 && y <= y2) {
                        break
                    }
                    if (x <= x2 || y <= y2) {
                        continue
                    }
                } else if (x1 > x2 && y2 > y1) { // bottom left
                    if (x <= x2 && y >= y2) {
                        break
                    }
                    if (x <= x2 || y >= y2) {
                        continue
                    }
                } else if (x2 > x1 && y1 > y2) { // top right
                    if (x >= x2 && y <= y2) {
                        break
                    }
                    if (x >= x2 || y <= y2) {
                        continue
                    }
                }
//                showLog("Point x1 $x1, y1 $y1")
//                showLog("Point x2 $x2, y2 $y2")
                showLog("x $x, y $y")
                // Add current point to the list
                points.add(Point(x, y))
            }
        }
        return points
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

    private fun circlePoints(xc: Int, yc: Int, r: Int): List<Point> {
        val points = mutableListOf<Pair<Int, Int>>()

        var x = 0
        var y = r
        var d = 1 - r

        while (x <= y) {
            // Add points for all 8 octants
            points.add(Pair(xc + x, yc + y))
            points.add(Pair(xc - x, yc + y))
            points.add(Pair(xc + x, yc - y))
            points.add(Pair(xc - x, yc - y))
            points.add(Pair(xc + y, yc + x))
            points.add(Pair(xc - y, yc + x))
            points.add(Pair(xc + y, yc - x))
            points.add(Pair(xc - y, yc - x))

            // Update based on the midpoint
            if (d < 0) {
                d += 2 * x + 3
            } else {
                d += 2 * (x - y) + 5
                y -= 1
            }
            x += 1
        }

        // Fill the circle inside (optional)
        val filledPoints = mutableListOf<Point>()
        for (point in points) {
            val xCoord = point.first
            val yCoord = point.second
            // Draw horizontal line between the two edges in each row
            for (i in (xc - xCoord)..(xc + xCoord)) {
                filledPoints.add(Point(i.toFloat(), yCoord.toFloat()))
            }
        }

        return filledPoints
    }

    private fun circleAreaPoints(xc: Int, yc: Int, radius: Int): List<Pair<Int, Int>> {
        val points = mutableListOf<Pair<Int, Int>>()

        // Define the bounding box for the circle
        val xMin = xc - radius
        val xMax = xc + radius
        val yMin = yc - radius
        val yMax = yc + radius

        // Check each point within the bounding box
        for (x in xMin..xMax) {
            for (y in yMin..yMax) {
                // Calculate distance from the center to this point
                if ((x - xc) * (x - xc) + (y - yc) * (y - yc) <= radius * radius) {
                    points.add(Pair(x, y)) // Point is within the circle
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
            imagePath = AppConstants.listOfDemoImagesPath[0]
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
        binding.imageResult.setImageBitmap(rawBitmap)
        imageWidth = bitmap.width
        imageHeight = bitmap.height
        imageDistance = AppConstants.getDistance(
            Point(0f, 0f), Point(imageWidth.toFloat(), imageHeight.toFloat())
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
                Point(0f, 0f), Point(overlayWidth.toFloat(), overlayHeight.toFloat())
            ) / 100f

            minDistance = min(overlayDistancePercentage, imageDistancePercentage) / 2f
        }
//        prepareHelper(bitmap)
//        prepareImageSegmentation(bitmap)
//        saveBitmapToLocalStorage(bitmap, "New again")
        binding.erase.performClick()
    }

    private var rawBitmap: Bitmap? = null
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
            bitmapArray, imageWidth, imageHeight, Bitmap.Config.ARGB_8888
        )

        val scaleWidth = (processedBitmap.width * scaleFactor).toInt()
        val scaleHeight = (processedBitmap.height * scaleFactor).toInt()
        val maskBitmap = Bitmap.createScaledBitmap(processedBitmap, scaleWidth, scaleHeight, false)
        showLog("processedBitmap $processedBitmap")

        binding.overlay.setMaskBitmap(maskBitmap)
        binding.overlay.invalidate()

//        binding.imageResult.setImageBitmap(processedBitmap)
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

                result?.let {
                    setMaskResult(
                        it.byteBuffer, it.maskWidth, it.maskHeight
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
                val bitmap = binding.imageResult.drawable.toBitmap()
                saveBitmapToLocalStorage(bitmap, "new")
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
        bitmap: Bitmap, displayName: String
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