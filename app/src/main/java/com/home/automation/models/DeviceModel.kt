package com.home.automation.models

import com.google.firebase.database.PropertyName

data class DeviceModel(

    @PropertyName("Fan")
    var fan: FanModel = FanModel(),
    @PropertyName("Lights")
    var lights: MutableList<BulbModel> = mutableListOf(),
    @PropertyName("RGB")
    var rgb: RGBModel = RGBModel(),
    @PropertyName("RoomValue")
    var roomValue: StatusModel = StatusModel(),
)