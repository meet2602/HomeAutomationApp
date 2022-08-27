package com.home.automation

import android.annotation.SuppressLint
import android.app.Dialog
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.home.automation.adapters.DeviceControlAdapter
import com.home.automation.databinding.ActivityGlobalWifiBinding
import com.home.automation.databinding.AddDeviceDialogBinding
import com.home.automation.models.DevicesModel
import com.home.automation.utils.*
import com.home.automation.viewmodel.DeviceViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*


class GlobalWifiActivity : AppCompatActivity(), View.OnClickListener {

    private val binding: ActivityGlobalWifiBinding by lazy {
        DataBindingUtil.setContentView(
            this,
            R.layout.activity_global_wifi
        )
    }
    private val sessionManager: SessionManager by lazy { SessionManager(this) }

    //    private val connectionUtils: ConnectionUtils by lazy { ConnectionUtils(this) }
    private val connectivityObserver: NetworkConnectivityObserver by lazy {
        NetworkConnectivityObserver(this)
    }
    private val deviceDialog: Dialog by lazy {
        Dialog(this)
    }
    private val mp: MediaPlayer by lazy {
        MediaPlayer.create(this, R.raw.button_29)
    }
    private val deviceBinding: AddDeviceDialogBinding by lazy {
        DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.add_device_dialog,
            null,
            false
        )
    }

    private val deviceViewModel: DeviceViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
            .create(DeviceViewModel()::class.java)
    }

    private var isUpdate = false
    private var updateDeviceId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.noInternet.noInternetClicks = this
        initAddDialog()


        lifecycleScope.launch {
            connectivityObserver.observe().collect() {
                when (it) {
                    ConnectivityObserver.Status.Available -> {
                        callDeviceList()
                    }
                    ConnectivityObserver.Status.Unavailable -> {
                        visibleStatus(
                            VisibleStatus.NoInternet,
                            binding.noInternet.llNoInternet,
                            binding.llNoData.llNoData,
                            binding.recyclerView,
                            binding.loading.llLoading
                        )
                    }
                    ConnectivityObserver.Status.Losing -> {
                        visibleStatus(
                            VisibleStatus.NoInternet,
                            binding.noInternet.llNoInternet,
                            binding.llNoData.llNoData,
                            binding.recyclerView,
                            binding.loading.llLoading
                        )
                    }
                    ConnectivityObserver.Status.Lost -> {
                        visibleStatus(
                            VisibleStatus.NoInternet,
                            binding.noInternet.llNoInternet,
                            binding.llNoData.llNoData,
                            binding.recyclerView,
                            binding.loading.llLoading
                        )
                    }
                }
            }
        }
//        val connectionUtils = ConnectionUtils(this)
//        connectionUtils.observe(this) { isConnected ->
//            if (isConnected) {
//                callDeviceList()
//            } else {
//                visibleStatus(
//                    VisibleStatus.NoInternet,
//                    binding.noInternet.llNoInternet,
//                    binding.llNoData.llNoData,
//                    binding.recyclerView,
//                    binding.loading.llLoading
//                )
//            }
//        }
//        connectionUtils.start()

//        val color = ColorPickerDialog.Builder(this)
//            .setTitle("RGB")
//            .setCancelable(false)
//            .setPositiveButton("Ok",
//                ColorEnvelopeListener { envelope, _ -> Log.d("color", envelope.hexCode.toString()) })
//            .setNegativeButton(
//                getString(R.string.cancel)
//            ) { dialogInterface, _ -> dialogInterface.dismiss() }
//            .attachAlphaSlideBar(true) // the default value is true.
//            .attachBrightnessSlideBar(true) // the default value is true.
//            .setBottomSpace(12) // set a bottom space between the last sidebar and buttons.
//        val colorPickerView: ColorPickerView = color.colorPickerView
////        colorPickerView.flagView = CustomFlag(this, R.layout.layout_flag)
//        val bubbleFlag = BubbleFlag(this)
//        bubbleFlag.flagMode = FlagMode.FADE
//        colorPickerView.flagView = bubbleFlag
//        color.show() // shows the dialog

    }


    override fun onDestroy() {
//        connectionUtils.stop()
        super.onDestroy()
    }

    private fun callDeviceList() {
        deviceViewModel.getDeviceList(sessionManager.userId).observe(this) {
            when (it.status) {
                Status.LOADING -> {
                    visibleStatus(
                        VisibleStatus.YesInternet,
                        binding.noInternet.llNoInternet,
                        binding.llNoData.llNoData,
                        binding.recyclerView,
                        binding.loading.llLoading
                    )
                }
                Status.SUCCESS -> {
                    if (it.data != null) {
                        gone(binding.loading.llLoading)

                        if (it.data.isNotEmpty()) {
                            visibleStatus(
                                VisibleStatus.Success,
                                binding.noInternet.llNoInternet,
                                binding.llNoData.llNoData,
                                binding.recyclerView,
                                binding.loading.llLoading
                            )
                            val deviceControlAdapter =
                                DeviceControlAdapter(it.data.sortedBy { it1 -> !it1.deviceStatus },
                                    object : DeviceControlAdapter.OnClickListener {
                                        override fun onClick(device: DevicesModel, position: Int) {
                                            val body = hashMapOf<String, Any>(
                                                "deviceStatus" to !device.deviceStatus
                                            )
                                            mp.start()
                                            callUpdateDevice(device.deviceId, body)
                                        }

                                        @SuppressLint("SetTextI18n")
                                        override fun onLongClick(device: DevicesModel) {
                                            isUpdate = true
                                            updateDeviceId = device.deviceId
                                            deviceBinding.saveBtn.text = "Update"
                                            deviceBinding.edDeviceName.setText(device.deviceName)
                                            deviceBinding.edDevicePin.setText(device.devicePin)
                                            deviceDialog.show()
                                            deviceBinding.edDeviceName.requestFocus()
                                        }

                                    })
                            binding.recyclerView.adapter = deviceControlAdapter

                        } else {
                            visibleStatus(
                                VisibleStatus.Error,
                                binding.noInternet.llNoInternet,
                                binding.llNoData.llNoData,
                                binding.recyclerView,
                                binding.loading.llLoading
                            )
                        }
                    }
                }
                Status.ERROR -> {
                    Log.d("error", it.message.toString())
                    longShowToast(it.message.toString())
                    visibleStatus(
                        VisibleStatus.Error,
                        binding.noInternet.llNoInternet,
                        binding.llNoData.llNoData,
                        binding.recyclerView,
                        binding.loading.llLoading
                    )
                }
            }

        }
    }


    private fun initAddDialog() {
        deviceDialog.setContentView(deviceBinding.root)
        deviceDialog.window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        deviceDialog.setCancelable(false)
        deviceBinding.saveBtn.setOnClickListener {
            if (deviceBinding.edDeviceName.text.toString().trim().isEmpty()) {
                deviceBinding.edDeviceName.error = "Required!"
            } else if (deviceBinding.edDevicePin.text.toString().trim().isEmpty()) {
                deviceBinding.edDevicePin.error = "Required!"
            } else {
                val deviceId = if (isUpdate) {
                    if (updateDeviceId != null) {
                        updateDeviceId
                    } else {
                        UUID.randomUUID().toString()
                    }
                } else {
                    UUID.randomUUID().toString()
                }!!
                val devicesModel = DevicesModel(
                    deviceId, deviceBinding.edDeviceName.text.toString().trim(),
                    deviceBinding.edDevicePin.text.toString().trim(), false
                )
                hideSoftKeyboard(it)
                deviceDialog.dismiss()
                if (isUpdate) {
                    val body = hashMapOf<String, Any>(
                        "deviceName" to deviceBinding.edDeviceName.text.toString().trim(),
                        "devicePin" to deviceBinding.edDevicePin.text.toString().trim(),
                    )
                    if (deviceId == updateDeviceId) {
                        callUpdateDevice(deviceId, body)
                    } else {
                        callAddDevice(devicesModel, deviceId)
                    }
                } else {
                    callAddDevice(devicesModel, deviceId)
                }
            }
        }
        deviceBinding.cancelBtn.setOnClickListener {
            hideSoftKeyboard(it)
            deviceDialog.dismiss()
        }

        binding.addDeviceBtn.setOnClickListener {
            isUpdate = false
            deviceBinding.edDeviceName.setText("")
            deviceBinding.edDevicePin.setText("")
            deviceBinding.saveBtn.text = this.resources.getText(R.string.save)
            deviceDialog.show()
            deviceBinding.edDeviceName.requestFocus()
        }
    }

    private fun callAddDevice(devicesModel: DevicesModel, deviceId: String) {
        deviceViewModel.addDevice(sessionManager.userId, devicesModel, deviceId).observe(this) {
            when (it.status) {

                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    Log.d("success", it.message.toString())
                    longShowToast("Device Added Successfully!")

                }
                Status.ERROR -> {
                    Log.d("error", it.message.toString())
                    longShowToast(it.message.toString())
                }
            }

        }
    }

    private fun callUpdateDevice(deviceId: String, body: HashMap<String, Any>) {
        deviceViewModel.updateDevice(sessionManager.userId, deviceId, body).observe(this) {

            when (it.status) {

                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    Log.d("success", it.message.toString())
                    longShowToast("Updated Successfully!")

                }
                Status.ERROR -> {
                    Log.d("error", it.message.toString())
                    longShowToast(it.message.toString())
                }
            }

        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            binding.noInternet.tvRetry.id -> {
                if (isOnline()) {
                    callDeviceList()
                } else {
                    visibleStatus(
                        VisibleStatus.NoInternet,
                        binding.noInternet.llNoInternet,
                        binding.llNoData.llNoData,
                        binding.recyclerView,
                        binding.loading.llLoading
                    )
                }
            }
        }
    }
}