package com.home.automation.models

data class DeviceModel(

    var fan: FanModel = FanModel(),
    var lights: MutableList<BulbModel> = mutableListOf(),
    var rgb: RGBModel = RGBModel(),
    var roomValue: StatusModel = StatusModel(),
)