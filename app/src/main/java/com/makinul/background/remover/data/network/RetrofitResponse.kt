package com.makinul.background.remover.data.network

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RetrofitResponse<T> {
    @SerializedName("StatusCode")
    @Expose
    var statusCode: Int? = 0

    @SerializedName("StatusMessage")
    @Expose
    var statusMessage: String? = "Unexpected error, please try again"

    @SerializedName("Successful")
    @Expose
    var isSuccessful: Boolean? = false

    @SerializedName("Message")
    @Expose
    var message: String? = "Unexpected error, please try again"

    @SerializedName("Data")
    @Expose
    var data: T? = null
}