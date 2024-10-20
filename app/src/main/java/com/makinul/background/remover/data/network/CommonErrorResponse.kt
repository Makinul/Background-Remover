package com.makinul.background.remover.data.network

import com.google.gson.annotations.SerializedName

class CommonErrorResponse {
    @SerializedName("Successful")
    var isSuccessful: Boolean? = false

    @SerializedName("Message")
    var message: String? = "Unexpected error, please try again"
}