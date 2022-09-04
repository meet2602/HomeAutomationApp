package com.home.automation.models

data class RGBModel(
    var devicePin: Int = 0,
    var deviceId: String = "",
    var deviceName: String = "",
    var redLightPin: Int = 0,
    var greenLightPin: Int = 0,
    var blueLightPin: Int = 0,
    var deviceStatus: Boolean = false,
    var deviceHexCode: String = "",
    var deviceDetail: String = "",
    var deviceType: String = "",
    var redLightValue: Int = 0,
    var greenLightValue: Int = 0,
    var blueLightValue: Int = 0,
)
