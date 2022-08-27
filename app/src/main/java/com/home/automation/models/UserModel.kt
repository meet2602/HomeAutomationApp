package com.home.automation.models


data class UserModel(
    var userId: String = "",
    var userName: String = "",
    var userEmail: String = "",
    var deviceIpAddress: String = "",
    var deviceConnect: Boolean = false,
)