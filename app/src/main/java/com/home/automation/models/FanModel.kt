package com.home.automation.models

data class FanModel(
    var deviceId: String = "",
    var deviceName: String = "",
    var devicePin: Int = 0,
    var deviceStatus: Boolean = false,
    var deviceType: String = "",
    var deviceValue: Int = 0,
)