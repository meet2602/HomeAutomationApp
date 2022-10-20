package com.home.automation.models

import com.google.firebase.database.PropertyName

data class ComponentModel(
    @PropertyName("rgbStateMsg") var RGBStateMsg: String = "",
    @PropertyName("fanStateMsg") var fanStateMsg: String = "",
    @PropertyName("led1StateMsg") var led1StateMsg: String = "",
    @PropertyName("led2StateMsg") var led2StateMsg: String = "",
    @PropertyName("led3StateMsg") var led3StateMsg: String = "",
    @PropertyName("led4StateMsg") var led4StateMsg: String = "",
    var tempValue: Int = 0,
)