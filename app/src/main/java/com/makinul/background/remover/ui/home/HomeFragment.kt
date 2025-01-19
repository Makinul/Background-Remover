package com.makinul.background.remover.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.makinul.background.remover.base.BaseFragment
import com.makinul.background.remover.databinding.FragmentHomeBinding
import com.makinul.background.remover.ui.main.MainActivity
import com.makinul.background.remover.utils.AppConstants
import com.makinul.background.remover.utils.AppConstants.getAssetBitmap
import com.makinul.background.remover.utils.AppConstants.listOfDemoImagesPath
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)


        binding.uploadImage.setOnClickListener {
            getImageFromGallery.launch(arrayOf("image/*"))
        }
        binding.demoImage0.setOnClickListener {
            gotoHomeActivity(AppConstants.KEY_IMAGE_TYPE_ASSET, listOfDemoImagesPath[0])
        }
        binding.demoImage1.setOnClickListener {
            gotoHomeActivity(AppConstants.KEY_IMAGE_TYPE_ASSET, listOfDemoImagesPath[1])
        }
        binding.demoImage2.setOnClickListener {
            gotoHomeActivity(AppConstants.KEY_IMAGE_TYPE_ASSET, listOfDemoImagesPath[2])
        }
        binding.demoImage3.setOnClickListener {
            gotoHomeActivity(AppConstants.KEY_IMAGE_TYPE_ASSET, listOfDemoImagesPath[3])
        }
        binding.privacyPolicy.setOnClickListener {
            (activity as HomeActivity?)?.goForPrivacyPolicy()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.demoImage0.setImageBitmap(getAssetBitmap(context, listOfDemoImagesPath[0]))
        binding.demoImage1.setImageBitmap(getAssetBitmap(context, listOfDemoImagesPath[1]))
        binding.demoImage2.setImageBitmap(getAssetBitmap(context, listOfDemoImagesPath[2]))
        binding.demoImage3.setImageBitmap(getAssetBitmap(context, listOfDemoImagesPath[3]))
    }

    private val getImageFromGallery =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri == null)
                return@registerForActivityResult

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                val source = ImageDecoder.createSource(
//                    requireContext().contentResolver, uri
//                )
//                ImageDecoder.decodeBitmap(source)
//            } else {
//                MediaStore.Images.Media.getBitmap(
//                    requireContext().contentResolver, uri
//                )
//            }.copy(Bitmap.Config.ARGB_8888, true)?.let { bitmap ->
//                gotoHomeActivity(bitmap)
//            }
            gotoHomeActivity(AppConstants.KEY_IMAGE_TYPE_URI, uri.toString())
        }

    private fun gotoHomeActivity(imageType: String, imagePath: String) {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(AppConstants.KEY_IMAGE_TYPE, imageType)
        intent.putExtra(AppConstants.KEY_IMAGE_PATH, imagePath)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        (activity as? HomeActivity)?.updateHomeIcon()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}