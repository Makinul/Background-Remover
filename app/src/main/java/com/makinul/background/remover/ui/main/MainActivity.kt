package com.makinul.background.remover.ui.main

import ai.painlog.mmhi.ui.zoomable.MainViewModel
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import androidx.activity.viewModels
import com.makinul.background.remover.R
import com.makinul.background.remover.base.BaseActivity
import com.makinul.background.remover.databinding.ActivityMainBinding
import com.makinul.background.remover.utils.AppConstants
import com.makinul.background.remover.utils.AppConstants.getAssetBitmap
import com.makinul.background.remover.utils.AppConstants.getUriBitmap
import com.makinul.background.remover.utils.Extensions.visible
import com.makinul.background.remover.utils.ImageSegmentHelper
import com.makinul.background.remover.utils.PoseLandmarkHelper
import com.mmh.emmahealth.data.Status
import dagger.hilt.android.AndroidEntryPoint
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

        prepareHelper(bitmap)
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
                saveImageToUserMemory()
                finish()
            }

            else -> {
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun saveImageToUserMemory() {

    }
}