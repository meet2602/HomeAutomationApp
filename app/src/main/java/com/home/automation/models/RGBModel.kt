package com.home.automation.models

data class RGBModel(
    var deviceId: String = "",
    var deviceName: String = "",
    var devicePin1: Int = 0,
    var devicePin2: Int = 0,
    var devicePin3: Int = 0,
    var deviceStatus: Boolean = false,
    var deviceHexCode: String = "",
    var deviceType: String = "",
    var deviceValue1: Int = 0,
    var deviceValue2: Int = 0,
    var deviceValue3: Int = 0,
)
