package com.makinul.background.remover.base

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.makinul.background.remover.R

open class BaseFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLog()
    }

    fun showLog(message: String = getString(R.string.test_message)) {
        Log.v(TAG, message)
    }

    fun showLog(@StringRes message: Int) {
        showLog(getString(message))
    }

    fun showToast(message: String = getString(R.string.test_message)) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showToast(@StringRes message: Int) {
        showToast(getString(message))
    }

    companion object {
        private const val TAG = "BaseFragment"
    }

}