package com.makinul.background.remover.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import javax.inject.Inject

class DeviceUtils @Inject constructor(
    context: Context
) {
    // like as T7 Pro
    private val deviceModel: String = Build.MODEL

    val deviceName: String
        get() {
            val manufacturer = Build.MANUFACTURER
            val model = deviceModel
            return if (model.startsWith(manufacturer)) {
                capitalize(model)
            } else {
                capitalize(manufacturer) + " " + model
            }
        }

    private fun capitalize(s: String?): String {
        if (s.isNullOrEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            first.uppercaseChar().toString() + s.substring(1)
        }
    }

//    /**
//     * More specifically, Settings.Secure.ANDROID_ID. This is a 64-bit quantity
//     * that is generated and stored when the device first boots. It is reset
//     * when the device is wiped.
//     *
//     *
//     * ANDROID_ID seems a good choice for a unique device identifier. There are
//     * downsides: First, it is not 100% reliable on releases of Android prior to
//     * 2.2 . Also, there has been at least one widely-observed bug in a popular
//     * handset from a major manufacturer, where every instance has the same
//     * ANDROID_ID.
//     */

    val deviceId: String = Settings.Secure.getString(context?.contentResolver, Settings.Secure.ANDROID_ID)
//    val deviceId: String = InstanceID.getInstance(context).getID()
//    val deviceId: String? = AdvertisingIdClient.getAdvertisingIdInfo(context).id

    companion object {
        const val TAG = "DeviceUtils"
    }
}