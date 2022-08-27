package com.home.automation.models

data class StatusModel(
    var deviceId: String = "",
    var deviceType: String = "",
    var humidityValue: Int = 0,
    var tempValue: Int = 0,
)