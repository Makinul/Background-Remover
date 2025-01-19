package com.makinul.background.remover.ui.settings.support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.makinul.background.remover.base.BaseFragment
import com.makinul.background.remover.data.Status
import com.makinul.background.remover.data.model.firebase.Support
import com.makinul.background.remover.databinding.FragmentSupportBinding
import com.makinul.background.remover.ui.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupportFragment : BaseFragment() {

    private val viewmodel: SettingsViewModel by activityViewModels()
    private var _binding: FragmentSupportBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupportBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewmodel.saveSupport.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.SUCCESS -> {

                    }

                    Status.ERROR -> {

                    }

                    Status.LOADING -> {

                    }
                }
            }
        }

        binding.submitButton.setOnClickListener {
            var name = binding.yourName.text.toString()
            var email = binding.yourEmail.text.toString()
            var message = binding.yourMessage.text.toString()

            if (name.isEmpty()) {
                name = "Nasim"
            }
            if (email.isEmpty()) {
                email = "mcnasim@gmail.com"
            }
            if (message.isEmpty()) {
                message = "Test support message"
            }

            val support = Support(name = name, email = email, message = message)
            viewmodel.saveSupport(support)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}