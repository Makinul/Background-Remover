package com.makinul.background.remover.ui.splash.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.makinul.background.remover.base.BaseFragment
import com.makinul.background.remover.databinding.FragmentSplashBinding
import com.makinul.background.remover.ui.splash.SplashActivity
import com.makinul.background.remover.ui.splash.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : BaseFragment() {

    private val viewModel: SplashViewModel by activityViewModels()
    private var _binding: FragmentSplashBinding? = null

    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)

        viewModel.anonymousAuth.observe(viewLifecycleOwner) { event ->

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch(Dispatchers.Main) {
            delay(2000)
            (activity as SplashActivity?)?.gotoHomeActivity()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    companion object {
        private const val TAG = "SplashFragment"
    }
}