package com.makinul.background.remover.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.makinul.background.remover.R
import com.makinul.background.remover.base.BaseFragment
import com.makinul.background.remover.databinding.FragmentHomeBinding
import com.makinul.background.remover.ui.main.MainActivity
import com.makinul.background.remover.utils.AppConstants
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uploadImage.setOnClickListener {
            gotoHomeActivity(null)
        }
        binding.demoImage0.setOnClickListener {
            gotoHomeActivity(R.drawable.demo_image_0)
        }
        binding.demoImage1.setOnClickListener {
            gotoHomeActivity(R.drawable.demo_image_1)
        }
        binding.demoImage2.setOnClickListener {
            gotoHomeActivity(R.drawable.demo_image_2)
        }
        binding.demoImage3.setOnClickListener {
            gotoHomeActivity(R.drawable.demo_image_3)
        }
    }

    private fun gotoHomeActivity(@DrawableRes imageResource: Int?) {
        val intent = Intent(context, MainActivity::class.java)
        imageResource?.let { intent.putExtra(AppConstants.KEY_IMAGE_RESOURCE, imageResource) }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}