package com.makinul.background.remover.data

import androidx.annotation.StringRes

sealed class Data<out T> {
    data class Success<out T>(val data: T, val extraValue: Int = -1) : Data<T>()
    data class Error(
        val message: String = "",
        val extraValue: Int = -1,
        @StringRes val messageResId: Int = -1
    ) : Data<Nothing>()

    object Loading : Data<Nothing>()
}

