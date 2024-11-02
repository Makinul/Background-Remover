package com.makinul.background.remover.ui.main

import ai.painlog.mmhi.ui.zoomable.MainViewModel
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import com.makinul.background.remover.base.BaseActivity
import com.makinul.background.remover.databinding.ActivityMainBinding
import com.makinul.background.remover.utils.AppConstants
import com.makinul.background.remover.utils.AppConstants.getAssetBitmap
import com.makinul.background.remover.utils.AppConstants.getUriBitmap
import com.makinul.background.remover.utils.AppConstants.listOfDemoImagesPath
import dagger.hilt.android.AndroidEntryPoint

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

        prepareImageData()
        setImage()
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
        binding.imageResult.setImageBitmap(bitmap)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        if (item.itemId == android.R.id.home) {
            finish() // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item)
    }
}