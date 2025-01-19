package com.makinul.background.remover.data.model

data class Device(
    val deviceId: String = "",
    val deviceName: String = "",
    val time: Long = System.currentTimeMillis()
)
