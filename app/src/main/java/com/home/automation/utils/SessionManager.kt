package com.home.automation.utils

import android.content.Context


class SessionManager(context: Context) {
    private var preferences =
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    private var editor = preferences.edit()

//    private val preferences: SharedPreferences = EncryptedSharedPreferences.create(
//        "home_automation_file",
//        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
//        context,
//        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//    )
//    private var editor = preferences.edit()

    private val keyIpAddress = "ipAddress"

    private val keyUserId = "userId"
    private val keyUserName = "userName"
    private val keyUserEmail = "userEmail"
    private val keyIsDeviceConnect = "isDeviceConnect"


    fun login(
        userId: String,
        name: String,
        email: String,
        deviceIpAddress: String,
        isDeviceConnect: Boolean,
    ) {
        editor = preferences?.edit()
        editor.putString(keyUserId, userId)
        editor.putString(keyUserName, name)
        editor.putString(keyUserEmail, email)
        editor.putString(keyIpAddress, deviceIpAddress)
        editor.putBoolean(keyIsDeviceConnect, isDeviceConnect)
        editor.apply()
    }

    fun createAccount(userId: String, name: String, email: String) {
        editor = preferences?.edit()
        editor.putString(keyUserId, userId)
        editor.putString(keyUserName, name)
        editor.putString(keyUserEmail, email)
        editor.putString(keyIpAddress, "")
        editor.putBoolean(keyIsDeviceConnect, false)
        editor.apply()
    }


    var ipAddress
        //        get() = preferences.getString(keyIpAddress, context.getString(R.string.default_ip))
        get() = preferences.getString(keyIpAddress, "")
            .toString()
        set(value) {
            editor.putString(keyIpAddress, value)
            editor.apply()
        }
    var userId
        get() = preferences.getString(keyUserId, "")
            .toString()
        set(value) {
            editor.putString(keyUserId, value)
            editor.apply()
        }
    var userName
        get() = preferences.getString(keyUserName, "")
            .toString()
        set(value) {
            editor.putString(keyUserName, value)
            editor.apply()
        }
    var userEmail
        get() = preferences.getString(keyUserEmail, "")
            .toString()
        set(value) {
            editor.putString(keyUserEmail, value)
            editor.apply()
        }
    var isDeviceConnected
        get() = preferences.getBoolean(keyIsDeviceConnect, false)
        set(value) {
            editor.putBoolean(keyIsDeviceConnect, value)
            editor.apply()
        }

    fun clearData() {
        editor.clear()
        editor.apply()
    }
}