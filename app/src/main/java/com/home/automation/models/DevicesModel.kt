package com.home.automation.models

data class DevicesModel(
    var deviceId: String = "",
    var deviceName: String = "",
    var devicePin: String = "",
    var deviceStatus: Boolean = false,
)