package com.makinul.background.remover.data.model.firebase

import java.io.Serializable

data class Support(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var message: String = "",
    var created: Long = -1,
    var timeZone: String = "",
    var status: Int = -1,
    var deviceId: String? = null,
    var deviceName: String? = null
) : Serializable {
    override fun toString(): String {
        return name
    }
}