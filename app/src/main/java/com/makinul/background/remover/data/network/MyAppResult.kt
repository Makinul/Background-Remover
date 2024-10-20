package com.makinul.background.remover.data.network

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class MyAppResult<out T : Any> {

    data class Success<out T : Any>(val data: T) : MyAppResult<T>()
    data class Error(val error: MyAppError) : MyAppResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=${error.errorCode}]"
        }
    }
}