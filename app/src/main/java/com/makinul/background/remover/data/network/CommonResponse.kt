package com.makinul.background.remover.data.network

import com.google.gson.annotations.SerializedName

class CommonResponse {
    @SerializedName("StatusCode")
    var statusCode = 0

    @SerializedName("StatusMessage")
    var statusMessage: String = "Unexpected error, please try again"
}