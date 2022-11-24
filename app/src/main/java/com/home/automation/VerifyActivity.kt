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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
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
    private var isValidPassword = false
    private var isValidConPassword = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verifyBinding.edPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditTextPassword(
                    verifyBinding.edPassword,
                    verifyBinding.txtPasswordL,
                    "password"
                )
            }
        })
        verifyBinding.edConPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditTextPassword(
                    verifyBinding.edConPassword,
                    verifyBinding.txtConPasswordL,
                    "conPassword"
                )
            }
        })
        verifyBinding.copyBtn.setOnClickListener {
            if (verifyBinding.edWifiName.text.toString().trim().isEmpty()) {
                verifyBinding.txtWifiNameL.error = "Required"
            } else if (verifyBinding.edWifiName.text.toString().trim().length > 8) {
                verifyBinding.txtWifiNameL.error = "Wifi name must be 8 characters!"
            } else {
                verifyBinding.txtWifiNameL.error = null
                validateEditTextPassword(
                    verifyBinding.edPassword,
                    verifyBinding.txtPasswordL,
                    "password"
                )
                if (isValidPassword) {
                    validateEditTextPassword(
                        verifyBinding.edConPassword,
                        verifyBinding.txtConPasswordL,
                        "conPassword"
                    )
                    if (isValidConPassword) {
                        if (verifyBinding.edPassword.text.toString()
                                .trim() != verifyBinding.edConPassword.text.toString().trim()
                        ) {
                            verifyBinding.txtConPasswordL.error = "Password don't match!"
                        } else {
                            verifyBinding.txtConPasswordL.error = null
                            init()
                        }
                    }
                }
            }
        }

    }

    private fun validateEditTextPassword(
        editText: EditText,
        textInputLayout: TextInputLayout,
        state: String,
    ) {
        var truthState = false
        if (editText.text.toString().trim().isEmpty()) {
            textInputLayout.error = "Required"
        } else if (editText.text.toString().length != 8) {
            textInputLayout.error = "Password must be 8 characters!"
        } else {
            textInputLayout.error = null
            truthState = true
        }
        if (truthState) {
            if (state == "password") {
                isValidPassword = true
            } else if (state == "conPassword") {
                isValidConPassword = true
            }
        } else {
            if (state == "password") {
                isValidPassword = false
            } else if (state == "conPassword") {
                isValidConPassword = false
            }
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
            requestData("http://192.168.4.1/user/${FirebaseAuth.getInstance().uid!!}/${
                verifyBinding.edWifiName.text.toString().trim()
            }/${verifyBinding.edPassword.text.toString().trim()}/")
            callGetDeviceStatus()
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
                        sessionManager.ipAddress = it.data.deviceIpAddress
                        if (sessionManager.isDeviceConnected) {
                            longShowToast("Device is connected!")
                            startActivity(Intent(this, DashBoardActivity::class.java))
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