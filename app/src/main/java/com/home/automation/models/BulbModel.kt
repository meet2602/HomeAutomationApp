package com.home.automation.models

data class BulbModel(
    var deviceId: String = "",
    var deviceName: String = "",
    var deviceDetail: String = "",
    var devicePin: Int = 0,
    var deviceStatus: Boolean = false,
    var deviceType: String = "",
)