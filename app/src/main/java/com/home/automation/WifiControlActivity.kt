package com.home.automation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.home.automation.databinding.ActivityWifiControlBinding
import com.home.automation.databinding.IpAddressDialogBinding
import com.home.automation.utils.*
import java.util.*


class WifiControlActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWifiControlBinding
    private lateinit var wifiManager: WifiManager
    private val permissionRequestId = 1
    private val permissionRequestN = Manifest.permission.RECORD_AUDIO

    private lateinit var deviceDialog: Dialog
    private lateinit var sessionManager: SessionManager

    private lateinit var mp: MediaPlayer
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val results: ArrayList<String> = result.data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                ) as ArrayList<String>
                binding.tvSpeechToText.text = Objects.requireNonNull(results)[0]
//                requestData("http://${sessionManager.ipAddress}/${binding.tvSpeechToText.text}")
                if (binding.tvSpeechToText.text.toString() == "all is on") {
                    requestData("http://${sessionManager.ipAddress}/led1on")
                    requestData("http://${sessionManager.ipAddress}/led2on")
                    requestData("http://${sessionManager.ipAddress}/led3on")
                    requestData("http://${sessionManager.ipAddress}/led4on")
                    requestData("http://${sessionManager.ipAddress}/led5on")
                } else if (binding.tvSpeechToText.text.toString() == "all is off") {
                    requestData("http://${sessionManager.ipAddress}/led1off")
                    requestData("http://${sessionManager.ipAddress}/led2off")
                    requestData("http://${sessionManager.ipAddress}/led3off")
                    requestData("http://${sessionManager.ipAddress}/led4off")
                    requestData("http://${sessionManager.ipAddress}/led5off")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wifi_control)
        sessionManager = SessionManager(this)
        mp = MediaPlayer.create(this, R.raw.button_29)
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        deviceDialog = Dialog(this)

        initDialog()
        if (!isHotspot(wifiManager)) {
            longShowToast(
                "Sorry, Please Turn On Hotspot."
            )
        }
//        else if (!wifiManager.isWifiEnabled) {
//            longShowToast(
//                "Sorry, Please Turn On Wifi"
//            )
//        }

//        else if (!checkIpAddress()) {
//            longShowToast("Sorry, Device is not connected!")
//        }

        binding.ivMic.setOnClickListener {
            if (checkSinglePermission(this, permissionRequestN, permissionRequestId)) {
                voiceRecognition()
            }
        }
        binding.led1ToggleBtn.setOnCheckedChangeListener { _, isChecked ->
            onOffToggle(binding.led1ToggleBtn, isChecked, "led1", binding.status1Txt, "Led 1:")
        }
        binding.led2ToggleBtn.setOnCheckedChangeListener { _, isChecked ->
            onOffToggle(binding.led2ToggleBtn, isChecked, "led2", binding.status2Txt, "Led 2:")
        }
        binding.led3ToggleBtn.setOnCheckedChangeListener { _, isChecked ->
            onOffToggle(binding.led3ToggleBtn, isChecked, "led3", binding.status3Txt, "Led 3:")

        }
        binding.led4ToggleBtn.setOnCheckedChangeListener { _, isChecked ->
            onOffToggle(binding.led4ToggleBtn, isChecked, "led4", binding.status4Txt, "Led 4:")
        }
        binding.led5ToggleBtn.setOnCheckedChangeListener { _, isChecked ->
            onOffToggle(binding.led5ToggleBtn, isChecked, "led5", binding.status5Txt, "Led 5:")

        }

    }

    private fun voiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something")
        try {
            resultLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onOffToggle(
        ledToggleBtn: ToggleButton,
        isChecked: Boolean,
        name: String,
        statusText: TextView,
        status: String,
    ) {
        if (isHotspot(wifiManager)) {
            mp.start()
            if (isChecked) {
                sendSignal(name + "on") // on led
            } else {
                sendSignal(name + "off") // off led
            }
            statusText.text = status + ledToggleBtn.text.toString()
        } else {
            longShowToast(
                "Sorry, Please Turn On Hotspot."
            )
        }
    }

    private fun sendSignal(number: String) {
        requestData("http://${sessionManager.ipAddress}/$number")
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestId) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                voiceRecognition()
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
                                    voiceRecognition()
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

    private fun initDialog() {

        val deviceBinding: IpAddressDialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.ip_address_dialog,
            null,
            false
        )
        deviceDialog.setContentView(deviceBinding.root)
        deviceDialog.window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        deviceDialog.setCancelable(false)
        deviceBinding.edIpAddress.setText(sessionManager.ipAddress)

        deviceBinding.saveBtn.setOnClickListener {
            if (deviceBinding.edIpAddress.text.toString().trim().isNotEmpty()) {
                sessionManager.ipAddress = deviceBinding.edIpAddress.text.toString().trim()
                hideSoftKeyboard(it)
                longShowToast("Ip Address Updated!")
                deviceDialog.dismiss()
            } else {
                deviceBinding.edIpAddress.error = "Required!"
            }
        }
        deviceBinding.cancelBtn.setOnClickListener {
            hideSoftKeyboard(it)
            deviceDialog.dismiss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.wifi_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.wifiListId -> {
                startActivity(Intent(this, HotspotListActivity::class.java))
                true
            }
            R.id.ipAddressId -> {
                deviceDialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}