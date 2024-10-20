package com.makinul.background.remover.data.network

/**
 * Home searched user error information is exposed to the UI
 */
data class MyAppError(
    val errorCode: Int,
    val errorMessageResource: Int,
    val errorMessage: String? = null
)