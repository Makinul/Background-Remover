package com.makinul.background.remover.data.model

import java.io.Serializable

data class User(
    var userId: String = "",
    var firstName: String? = null,
    var lastName: String? = null,
    var gender: String? = null,
    var dateOfBirth: String? = null,
    var email: String? = null,
    var contactNo: String? = null,
    var profileImage: String? = null,
    var countryCode: String? = null,
    var countryName: String? = null,
    var zipCode: String? = null,
    var fcmToken: List<FcmToken> = emptyList(),
    var devices: Map<String, Device> = emptyMap(),
) : Serializable {
    override fun toString(): String {
        return "$firstName $lastName"
    }
}
