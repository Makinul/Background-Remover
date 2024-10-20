package com.mmh.emmahealth.data

import androidx.annotation.StringRes

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

data class Resource<out T>(
    val status: Status,
    val data: T?,
    val message: String?,
    @StringRes val messageResId: Int = -1,
    val extraValue: Int = -1
) {

    companion object {
        fun <T> success(
            data: T,
            @StringRes messageResId: Int = -1,
            extraValue: Int = -1
        ): Resource<T> {
            return Resource(
                status = Status.SUCCESS,
                data = data,
                message = null,
                messageResId = messageResId,
                extraValue = extraValue
            )
        }

        fun <T> error(
            message: String? = null,
            @StringRes messageResId: Int = -1,
            extraValue: Int = -1
        ): Resource<T> {
            return Resource(
                status = Status.ERROR,
                data = null,
                message = message,
                messageResId = messageResId,
                extraValue = extraValue
            )
        }

        fun <T> loading(): Resource<T> {
            return Resource(
                Status.LOADING,
                data = null,
                message = null,
                messageResId = -1,
                extraValue = -1
            )
        }
    }
}
