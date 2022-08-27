package com.home.automation

import android.annotation.SuppressLint
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.Slider
import com.home.automation.databinding.ActivityDashBoardBinding
import com.home.automation.utils.*
import com.home.automation.viewmodel.DeviceViewModel
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.flag.FlagMode
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*


class DashBoardActivity : AppCompatActivity() {
    private val dashBoardBinding: ActivityDashBoardBinding by lazy {
        DataBindingUtil.setContentView(
            this,
            R.layout.activity_dash_board
        )
    }
    private val connectivityObserver: NetworkConnectivityObserver by lazy {
        NetworkConnectivityObserver(this)
    }
    private val deviceViewModel: DeviceViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
            .create(DeviceViewModel()::class.java)
    }
    private val sessionManager: SessionManager by lazy { SessionManager(this) }
    private val mp: MediaPlayer by lazy {
        MediaPlayer.create(this, R.raw.button_29)
    }
    private var isUpdate = false
    private val fanSpeed = arrayOf(0, 500, 400, 300, 200, 100)
    private val rotate = RotateAnimation(
        0F, 360F,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    )

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //callAllDevice()

        lifecycleScope.launch {
            connectivityObserver.observe().collect {
                when (it) {
                    ConnectivityObserver.Status.Available -> {
                        gone(dashBoardBinding.noInternet.llNoInternet)
//                        visible(dashBoardBinding..loading.llLoading)

                        visible(dashBoardBinding.rootLayout)
                        callDeviceList()
                    }
                    ConnectivityObserver.Status.Unavailable -> {
                        visible(dashBoardBinding.noInternet.llNoInternet)
                        gone(dashBoardBinding.rootLayout)
                    }
                    ConnectivityObserver.Status.Losing -> {
                        visible(dashBoardBinding.noInternet.llNoInternet)
                        gone(dashBoardBinding.rootLayout)
                    }
                    ConnectivityObserver.Status.Lost -> {
                        visible(dashBoardBinding.noInternet.llNoInternet)
                        gone(dashBoardBinding.rootLayout)
                    }
                }
            }
        }




        with(dashBoardBinding) {
            txtUsername.text = "Hi ${sessionManager.userName}"
            bulbLayout1.bulbSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isUpdate) {
                    val lightingBody = hashMapOf<String, Any>(
                        "deviceStatus" to isChecked
                    )
                    bulbStatusUpdate("lights", true, "0", lightingBody, true)
                    mp.start()
                }
            }
            bulbLayout2.bulbSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isUpdate) {
                    val lightingBody = hashMapOf<String, Any>(
                        "deviceStatus" to isChecked
                    )
                    bulbStatusUpdate("lights", true, "1", lightingBody, true)
                    mp.start()
                }
            }

            fanLayout.fanSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isUpdate) {
                    val fanBody = hashMapOf<String, Any>(
                        "deviceStatus" to isChecked
                    )
                    bulbStatusUpdate("fan", false, "", fanBody, true)
                    mp.start()
                }
            }
            fanLayout.fanSlider.addOnSliderTouchListener(
                object : Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {

                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        val fanBody = hashMapOf<String, Any>(
                            "deviceValue" to slider.value
                        )
                        bulbStatusUpdate("fan", false, "", fanBody, false)
                        mp.start()
                    }
                }
            )
            rgbLayout.root.setOnClickListener {
                val color = ColorPickerDialog.Builder(this@DashBoardActivity)
                    .setTitle("RGB")
                    .setPreferenceName("MyColorPicker")
                    .setCancelable(false)
                    .setPositiveButton("Ok",
                        ColorEnvelopeListener { envelope, _ ->
                            Log.d("color",
                                envelope.hexCode.toString())
                        })
                    .setNegativeButton(
                        getString(R.string.cancel)
                    ) { dialogInterface, _ -> dialogInterface.dismiss() }
                    .attachAlphaSlideBar(true) // the default value is true.
                    .attachBrightnessSlideBar(true) // the default value is true.
                    .setBottomSpace(12) // set a bottom space between the last sidebar and buttons.
                val colorPickerView: ColorPickerView = color.colorPickerView
//        colorPickerView.flagView = CustomFlag(this, R.layout.layout_flag)
                val bubbleFlag = BubbleFlag(this@DashBoardActivity)
                bubbleFlag.flagMode = FlagMode.ALWAYS
                colorPickerView.flagView = bubbleFlag
                colorPickerView.setColorListener(ColorEnvelopeListener { envelope, fromUser ->

                    Log.d("color = ", envelope.hexCode)
                    Log.d("a = ", envelope.argb[0].toString())
                    Log.d("r = ", envelope.argb[1].toString())
                    Log.d("g = ", envelope.argb[2].toString())
                    Log.d("b = ", envelope.argb[3].toString())
                    val rgbBody = hashMapOf<String, Any>(
                        "deviceHexCode" to "#${envelope.hexCode}",
                        "deviceValue1" to envelope.argb[1],
                        "deviceValue2" to envelope.argb[2],
                        "deviceValue3" to envelope.argb[3],
                    )
                    bulbStatusUpdate("rgb", false, "", rgbBody, false)

                })
                color.show()
            }

            rgbLayout.rgbSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isUpdate) {
                    val rgbBody = hashMapOf<String, Any>(
                        "deviceStatus" to isChecked
                    )
                    bulbStatusUpdate("rgb", false, "", rgbBody, true)
                    mp.start()
                }
            }

        }
    }

    private fun callAllDevice() {
        val lighting1Id = UUID.randomUUID().toString()
        val lighting1Body = hashMapOf<String, Any>(
            "devicePin" to 42,
            "deviceStatus" to false,
            "deviceType" to "light",
            "deviceId" to lighting1Id,
            "deviceName" to "light 1",
        )


        val lighting2Id = UUID.randomUUID().toString()
        val lighting2Body = hashMapOf<String, Any>(
            "devicePin" to 43,
            "deviceStatus" to false,
            "deviceType" to "light",
            "deviceId" to lighting2Id,
            "deviceName" to "light 2",
        )
        val lights = hashMapOf<String, Any>(
            "lights" to listOf(lighting1Body, lighting2Body)
        )
        callAddAllDevice(lights, "")
        val tempAnyHumidityId = UUID.randomUUID().toString()
        val tempAnyHumidityBody = hashMapOf<String, Any>(
            "tempValue" to 0.0F,
            "humidityValue" to 0.0F,
            "deviceType" to "roomValue",
            "deviceId" to tempAnyHumidityId,
        )
        callAddAllDevice(tempAnyHumidityBody, "roomValue")
        val fanId = UUID.randomUUID().toString()
        val fanBody = hashMapOf<String, Any>(
            "devicePin" to 45,
            "deviceValue" to 0,
            "deviceStatus" to false,
            "deviceType" to "fan",
            "deviceId" to fanId,
            "deviceName" to "Fan",
        )
        callAddAllDevice(fanBody, "fan")

        val rgbId = UUID.randomUUID().toString()
        val rgbBody = hashMapOf<String, Any>(
            "devicePin1" to 46,
            "devicePin2" to 47,
            "devicePin3" to 48,
            "deviceHexCode" to "",
            "deviceValue1" to 0,
            "deviceValue2" to 0,
            "deviceValue3" to 0,
            "deviceStatus" to false,
            "deviceType" to "rgb",
            "deviceId" to rgbId,
            "deviceName" to "RGB",
        )
        callAddAllDevice(rgbBody, "rgb")
    }

    private fun callAddAllDevice(body: HashMap<String, Any>, deviceId: String) {
        deviceViewModel.addAllDevice(sessionManager.userId, body, deviceId).observe(this) {
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

    @SuppressLint("SetTextI18n")
    private fun callDeviceList() {
        deviceViewModel.getAllDeviceList(sessionManager.userId).observe(this) {
            when (it.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    if (it.data != null) {
                        gone(dashBoardBinding.loading.llLoading)
                        val deviceModel = it.data
                        val statusModel = deviceModel.roomValue
                        val fanModel = deviceModel.fan
                        val bulbModel = deviceModel.lights
                        val rgbModel = deviceModel.rgb

                        with(
                            dashBoardBinding
                        ) {
                            tempLayout.tempValue.text =
                                statusModel.tempValue.toString() + " \u2103"
                            humidityLayout.humidityValue.text =
                                statusModel.humidityValue.toString() + " %"
                            bulbLayout1.blubTitle.text = bulbModel[0].deviceName
                            bulbLayout2.blubTitle.text = bulbModel[1].deviceName
                            if (bulbModel[0].deviceStatus) {
                                bulbLayout1.blubState.text = "ON"
                                bulbLayout1.bulbSwitch.isChecked = true
                            } else {
                                bulbLayout1.blubState.text = "OFF"
                                bulbLayout1.bulbSwitch.isChecked = false
                            }
                            if (bulbModel[1].deviceStatus) {
                                bulbLayout2.blubState.text = "ON"
                                bulbLayout2.bulbSwitch.isChecked = true
                            } else {
                                bulbLayout2.blubState.text = "OFF"
                                bulbLayout2.bulbSwitch.isChecked = false
                            }
                            fanLayout.fanTitle.text = fanModel.deviceName
                            fanLayout.fanSlider.value = fanModel.deviceValue.toFloat()
                            if (fanModel.deviceStatus) {
                                fanLayout.fanState.text = "ON"
                                fanLayout.fanSwitch.isChecked = true

                                if (fanModel.deviceValue > 0) {
                                    rotate.duration = fanSpeed[fanModel.deviceValue].toLong()
                                    rotate.repeatCount = Animation.INFINITE
                                    fanLayout.fanImg.startAnimation(rotate)
                                } else {
                                    fanLayout.fanImg.clearAnimation()
                                }
                            } else {
                                fanLayout.fanState.text = "OFF"
                                fanLayout.fanSwitch.isChecked = false
                                fanLayout.fanImg.clearAnimation()
                            }
                            if (rgbModel.deviceHexCode.isNotEmpty()) {
                                rgbLayout.rgbImg.setBackgroundColor(Color.parseColor(rgbModel.deviceHexCode))
                            }
                            if (rgbModel.deviceStatus) {
                                rgbLayout.rgbState.text = "ON"
                                rgbLayout.rgbSwitch.isChecked = true
                            } else {
                                rgbLayout.rgbState.text = "OFF"
                                rgbLayout.rgbSwitch.isChecked = false
                            }
                            isUpdate = true
                        }

                    }
                }
                Status.ERROR -> {

                }
            }

        }
    }

    private fun bulbStatusUpdate(
        deviceType: String,
        state: Boolean,
        devicePos: String,
        body: HashMap<String, Any>,
        toastState: Boolean,
    ) {
        deviceViewModel.bulbStatusUpdate(sessionManager.userId, state, deviceType, devicePos, body)
            .observe(this) {

                when (it.status) {

                    Status.LOADING -> {

                    }
                    Status.SUCCESS -> {
                        Log.d("success", it.message.toString())
                        if (toastState) {
                            longShowToast("Updated Successfully!")
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