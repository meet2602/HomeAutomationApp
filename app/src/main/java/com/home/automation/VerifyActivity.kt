package com.home.automation

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.home.automation.databinding.ActivityVerifyBinding
import com.home.automation.utils.*
import com.home.automation.viewmodel.UserViewModel

class VerifyActivity : AppCompatActivity() {
    private val verifyBinding: ActivityVerifyBinding by lazy {
        DataBindingUtil.setContentView(
            this,
            R.layout.activity_verify
        )
    }
    private val wifiManager: WifiManager by lazy {
        applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    }
    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
            .create(UserViewModel()::class.java)
    }
    private val sessionManager: SessionManager by lazy { SessionManager(this) }
    private val permissionRequestId = 1
    private val permissionRequestN = Manifest.permission.ACCESS_FINE_LOCATION
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scanWifiDevice()
            } else {
                showSnackBarWithActionINDEFINITE(
                    verifyBinding.root,
                    resources.getString(R.string.permission_error)
                ) { permission() }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verifyBinding.edToken.setText(sessionManager.userId)
        verifyBinding.copyBtn.setOnClickListener {
            init()
        }

    }

    private fun init() {
        if (checkSinglePermission(this, permissionRequestN, permissionRequestId)) {
            isWifi()
        }
    }

    private fun isWifi() {
        if (!wifiManager.isWifiEnabled) {
            permission()
        } else {
            scanWifiDevice()
        }
    }

    private fun permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val enableBT = Intent(Settings.Panel.ACTION_WIFI)
            resultLauncher.launch(enableBT)
        } else {
            if (isHotspot(wifiManager)) {
                try {
                    /// https://stackoverflow.com/questions/7048922/android-2-3-wifi-hotspot-api/7049074#7049074
                    val method = wifiManager.javaClass.getMethod(
                        "setWifiApEnabled",
                        android.net.wifi.WifiConfiguration::class.java,
                        Boolean::class.javaPrimitiveType
                    )
                    method.invoke(wifiManager, null, false)
                } catch (e: Exception) {
                    longShowToast(
                        "Please Turn Off Hotspot"
                    )
                }
            } else {
                wifiManager.isWifiEnabled = true
                scanWifiDevice()
            }
        }
    }

    private fun scanWifiDevice() {
        connectToWifi("NodeMCU", "123456789")
        val checkIpAddress = checkIpAddress(wifiManager, sessionManager)
        Log.d("checkIpAddress", checkIpAddress.toString())
        if (checkIpAddress) {
//            if (sessionManager.ipAddress.isNotEmpty()) {
            requestData("http://192.168.4.1/user/${FirebaseAuth.getInstance().uid!!}/dad/0123456789/")
            callGetDeviceStatus()
//            }else{
//                longShowToast("Device Not Connected")
//            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestId) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isWifi()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        permissionRequestN
                    )
                ) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            permissionRequestN
                        ) == PackageManager.PERMISSION_DENIED
                    ) {
                        settingActivityOpen(this)
                    }
                } else {
                    showPermissionFaiDialog(this) { _: DialogInterface?, which: Int ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                if (checkSinglePermission(
                                        this,
                                        permissionRequestN,
                                        permissionRequestId
                                    )
                                ) {
                                    isWifi()
                                }
                            }
                            DialogInterface.BUTTON_NEGATIVE -> {
                            }
                        }
                    }

                }
            }
        }
    }

    private fun callGetDeviceStatus() {
        userViewModel.getUserDetail(FirebaseAuth.getInstance().uid!!).observe(this) {
            when (it.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    Log.d("success", it.message.toString())
                    if (it.data != null) {
                        sessionManager.isDeviceConnected = it.data.deviceConnect
                        if (sessionManager.isDeviceConnected) {
                            longShowToast("Device is connected!")
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            longShowToast("Device is not connected!")
                        }
                    }
                }
                Status.ERROR -> {
                    Log.d("error", it.message.toString())
                    longShowToast(it.message.toString())
                }
            }

        }

    }

}
//http://192.168.4.1/user/YJU2xSe59mhDhqWgl69cDSzUCWv2/dad/0123456789/