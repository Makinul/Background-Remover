package com.makinul.background.remover.base

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.makinul.background.remover.R

open class BaseActivity : AppCompatActivity() {

    fun showToast(message: String = getString(R.string.test_message)) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showToast(@StringRes message: Int) {
        showToast(getString(message))
    }

    companion object {
        private const val TAG = "BaseActivity"
    }
}