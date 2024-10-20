package com.makinul.background.remover.base

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BgRemoverApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.v(TAG, "onCreate")
    }

    companion object {

        private const val TAG = "BgRemoverApp"
    }
}