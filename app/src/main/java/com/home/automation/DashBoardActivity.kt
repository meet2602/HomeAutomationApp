package com.home.automation

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.Slider
import com.home.automation.databinding.ActivityDashBoardBinding
import com.home.automation.databinding.PickColorLayoutBinding
import com.home.automation.utils.*
import com.home.automation.viewmodel.DeviceViewModel
import com.home.automation.viewmodel.UserViewModel
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
    private val rgbColorBinding: PickColorLayoutBinding by lazy {
        DataBindingUtil.inflate(
            LayoutInflater.from(this),
            R.layout.pick_color_layout,
            null,
            false
        )
    }

    private val connectivityObserver: NetworkConnectivityObserver by lazy {
        NetworkConnectivityObserver(this)
    }
    private val deviceViewModel: DeviceViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
            .create(DeviceViewModel()::class.java)
    }
    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
            .create(UserViewModel()::class.java)
    }
    private val sessionManager: SessionManager by lazy { SessionManager(this) }
    private val mp: MediaPlayer by lazy {
        MediaPlayer.create(this, R.raw.button_29)
    }
    private var isUpdate = false
    private val fanSpeed = arrayOf(0, 400, 300, 200, 100, 50)
    private val fanSpeedOnTxt = arrayOf("fan0On", "fan1On", "fan2On", "fan3On", "fan4On", "fan5On")
    private val fanSpeedOffTxt =
        arrayOf("fan0Off", "fan1Off", "fan2Off", "fan3Off", "fan4Off", "fan5Off")
    private val rotate = RotateAnimation(
        0F, 360F,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    )
    private val rgbDialog: Dialog by lazy {
        Dialog(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        callAddComponents()
        lifecycleScope.launch {
            connectivityObserver.observe().collect {
                when (it) {
                    ConnectivityObserver.Status.Available -> {
                        gone(dashBoardBinding.noInternet.llNoInternet)
//                        visible(dashBoardBinding.loading.llLoading)
                        visible(dashBoardBinding.rootLayout)
                        callComponent()
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



        rgbDialog.setContentView(rgbColorBinding.root)
        rgbDialog.window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        rgbDialog.setCancelable(false)
        rgbColorBinding.cancelBtn.setOnClickListener { rgbDialog.dismiss() }
        rgbColorBinding.redLayout.seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                rgbColorBinding.redLayout.txtColorValue.text = seekBar.progress.toString()
                setRGBColor()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        rgbColorBinding.greenLayout.seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                rgbColorBinding.greenLayout.txtColorValue.text = seekBar.progress.toString()
                setRGBColor()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        rgbColorBinding.blueLayout.seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                rgbColorBinding.blueLayout.txtColorValue.text = seekBar.progress.toString()
                setRGBColor()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        rgbColorBinding.saveBtn.setOnClickListener {
            val status = if (dashBoardBinding.rgbLayout.rgbSwitch.isChecked) "RGBOn" else "RGBOff"
            val value =
                "${status}/${rgbColorBinding.redLayout.seekBar.progress}/${rgbColorBinding.greenLayout.seekBar.progress}/${rgbColorBinding.blueLayout.seekBar.progress}"
            val body = hashMapOf<String, Any>(
                "rgbStateMsg" to value
            )
            callUpdateComponent(body, true)
            rgbDialog.dismiss()
        }
        with(dashBoardBinding) {
            txtUsername.text = "Hi ${sessionManager.userName}"
            logoutBtn.setOnClickListener {
                showConfirmDialog("Logout",
                    "Are you sure you wish to logout?") { _: DialogInterface?, which: Int ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            userViewModel.signOut(sessionManager).observe(this@DashBoardActivity) {
                                when (it.status) {

                                    Status.LOADING -> {

                                    }
                                    Status.SUCCESS -> {
                                        longShowToast("Logout Successfully!")
                                        startActivity(Intent(this@DashBoardActivity,
                                            LoginActivity::class.java))
                                        finish()
                                    }
                                    Status.ERROR -> {
                                        Log.d("error", it.message.toString())
                                    }
                                }
                            }
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }
            }
            bulbLayout1.bulbSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isUpdate) {
                    val status = if (isChecked) "led1On" else "led1Off"
                    val body = hashMapOf<String, Any>(
                        "led1StateMsg" to status
                    )
                    callUpdateComponent(body, true)
                    mp.start()
                }
            }
            bulbLayout2.bulbSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isUpdate) {
                    val status = if (isChecked) "led2On" else "led2Off"
                    val body = hashMapOf<String, Any>(
                        "led2StateMsg" to status
                    )
                    callUpdateComponent(body, true)
                    mp.start()
                }
            }
            bulbLayout3.bulbSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isUpdate) {
                    val status = if (isChecked) "led3On" else "led3Off"
                    val body = hashMapOf<String, Any>(
                        "led3StateMsg" to status
                    )
                    callUpdateComponent(body, true)
                    mp.start()
                }
            }
            bulbLayout4.bulbSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isUpdate) {
                    val status = if (isChecked) "led4On" else "led4Off"
                    val body = hashMapOf<String, Any>(
                        "led4StateMsg" to status
                    )
                    callUpdateComponent(body, true)
                    mp.start()
                }
            }
            fanLayout.fanSlider.addOnSliderTouchListener(
                object : Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {}
                    override fun onStopTrackingTouch(slider: Slider) {
                        val status =
                            if (fanLayout.fanSwitch.isChecked) "fan${slider.value.toInt()}On" else "fan${slider.value.toInt()}Off"
                        val body = hashMapOf<String, Any>(
                            "fanStateMsg" to status
                        )
                        callUpdateComponent(body, true)
                        mp.start()
                    }
                }
            )
            fanLayout.fanSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isUpdate) {
                    val status =
                        if (isChecked) "fan${fanLayout.fanSlider.value.toInt()}On" else "fan${fanLayout.fanSlider.value.toInt()}Off"
                    val body = hashMapOf<String, Any>(
                        "fanStateMsg" to status
                    )
                    callUpdateComponent(body, true)
                    mp.start()
                }
            }
            allApplienceLayout.bulbSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isUpdate) {
                    if (isChecked) {
                        isUpdate = false
                        val body = hashMapOf<String, Any>(
                            "led1StateMsg" to "led1On",
                            "led2StateMsg" to "led2On",
                            "led3StateMsg" to "led3On",
                            "led4StateMsg" to "led4On",
                            "fanStateMsg" to "fan4On",
                            "rgbStateMsg" to "RGBOn/255/255/255"
                        )
                        callUpdateComponent(body, false)
                        allApplienceLayout.blubState.text = "ON"
                        allApplienceLayout.bulbSwitch.isChecked = true
                        allApplienceLayout.bulbImg.setImageDrawable(resources.getDrawable(R.drawable.blub_image,
                            theme))
                    } else {
                        isUpdate = false
                        val body = hashMapOf<String, Any>(
                            "led1StateMsg" to "led1Off",
                            "led2StateMsg" to "led2Off",
                            "led3StateMsg" to "led3Off",
                            "led4StateMsg" to "led4Off",
                            "fanStateMsg" to "fan${fanLayout.fanSlider.value.toInt()}Off",
                            "rgbStateMsg" to "RGBOff/255/255/255"
                        )
                        callUpdateComponent(body, false)
                        allApplienceLayout.blubState.text = "OFF"
                        allApplienceLayout.bulbSwitch.isChecked = false
                        allApplienceLayout.bulbImg.setImageDrawable(resources.getDrawable(R.drawable.bulb_off_img,
                            theme))
                    }

                    mp.start()
                }
            }

            rgbLayout.root.setOnClickListener {
                rgbDialog.show()
//                val color = ColorPickerDialog.Builder(this@DashBoardActivity)
//                    .setTitle("RGB")
//                    .setPreferenceName("MyColorPicker")
//                    .setCancelable(false)
//                    .setPositiveButton("Ok",
//                        ColorEnvelopeListener { envelope, _ ->
//                            Log.d("color",
//                                envelope.hexCode.toString())
//                            val hex = java.lang.String.format("#%02x%02x%02x",
//                                envelope.argb[1],
//                                envelope.argb[2],
//                                envelope.argb[3])
//                            val value = "RGB/${envelope.argb[1]}/${envelope.argb[2]}/${envelope.argb[3]}"
//                            val body = hashMapOf<String, Any>(
//                                "rgbStateMsg" to value
//                            )
//                            callUpdateComponent(body, true)
//                            rgbLayout.rgbImg.setBackgroundColor(Color.parseColor(hex))
//
//                        })
//                    .setNegativeButton(
//                        getString(R.string.cancel)
//                    ) { dialogInterface, _ -> dialogInterface.dismiss() }
//                    .attachAlphaSlideBar(true) // the default value is true.
//                    .attachBrightnessSlideBar(true) // the default value is true.
//                    .setBottomSpace(12) // set a bottom space between the last sidebar and buttons.
//                val colorPickerView: ColorPickerView = color.colorPickerView
////        colorPickerView.flagView = CustomFlag(this, R.layout.layout_flag)
//                val bubbleFlag = BubbleFlag(this@DashBoardActivity)
//                bubbleFlag.flagMode = FlagMode.ALWAYS
//                colorPickerView.flagView = bubbleFlag
//                colorPickerView.setColorListener(ColorEnvelopeListener { envelope, fromUser ->
//
//                    Log.d("color = ", envelope.hexCode)
//                    Log.d("a = ", envelope.argb[0].toString())
//                    Log.d("r = ", envelope.argb[1].toString())
//                    Log.d("g = ", envelope.argb[2].toString())
//                    Log.d("b = ", envelope.argb[3].toString())
//
////                    val rgbBody = hashMapOf<String, Any>(
////                        "deviceHexCode" to "#${envelope.hexCode}",
////                        "redLightValue" to envelope.argb[1],
////                        "greenLightValue" to envelope.argb[2],
////                        "blueLightValue" to envelope.argb[3],
////                    )
////                    bulbStatusUpdate("RGB", false, "", rgbBody, false)
////                    val body = hashMapOf<String, Any>(
////                        "RGB1StateHexCode" to "#${envelope.hexCode}",
////                        "RGB1RValue" to envelope.argb[1],
////                        "RGB1GValue" to envelope.argb[2],
////                        "RGB1BValue" to envelope.argb[3],
////                    )
////                    callUpdateComponent(body, false)
//
//                })
//                color.show()
            }

            rgbLayout.rgbSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isUpdate) {
                    val status =
                        if (isChecked) "RGBOn" else "RGBOff"
                    val value =
                        "${status}/${rgbColorBinding.redLayout.seekBar.progress}/${rgbColorBinding.greenLayout.seekBar.progress}/${rgbColorBinding.blueLayout.seekBar.progress}"
                    val body = hashMapOf<String, Any>(
                        "rgbStateMsg" to value
                    )
                    callUpdateComponent(body, true)
                    mp.start()
                }
            }

        }
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    private fun callComponent() {
        deviceViewModel.getComponent(sessionManager.userId).observe(this) {
            when (it.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    if (it.data != null) {
                        Log.d("success", it.data.toString())
                        gone(dashBoardBinding.loading.llLoading)
                        val data = it.data

                        with(
                            dashBoardBinding
                        ) {
                            tempLayout.tempValue.text =
                                data.tempValue.toString() + " \u2103"
//                            humidityLayout.humidityValue.text =
//                                data.humidityValue.toString() + " %"
                            bulbLayout1.blubTitle.text = "Light 1"
                            bulbLayout2.blubTitle.text = "Light 2"
                            bulbLayout3.blubTitle.text = "Light 3"
                            bulbLayout4.blubTitle.text = "Light 4"
                            allApplienceLayout.blubTitle.text = "All Appliences"

                            if (data.led1StateMsg == "led1On") {
                                bulbLayout1.blubState.text = "ON"
                                bulbLayout1.bulbSwitch.isChecked = true
                                bulbLayout1.bulbImg.setImageDrawable(resources.getDrawable(R.drawable.blub_image,
                                    theme))
                            } else {
                                bulbLayout1.blubState.text = "OFF"
                                bulbLayout1.bulbSwitch.isChecked = false
                                bulbLayout1.bulbImg.setImageDrawable(resources.getDrawable(R.drawable.bulb_off_img,
                                    theme))
                            }
                            if (data.led2StateMsg == "led2On") {
                                bulbLayout2.blubState.text = "ON"
                                bulbLayout2.bulbSwitch.isChecked = true
                                bulbLayout2.bulbImg.setImageDrawable(resources.getDrawable(R.drawable.blub_image,
                                    theme))
                            } else {
                                bulbLayout2.blubState.text = "OFF"
                                bulbLayout2.bulbSwitch.isChecked = false
                                bulbLayout2.bulbImg.setImageDrawable(resources.getDrawable(R.drawable.bulb_off_img,
                                    theme))
                            }

                            if (data.led3StateMsg == "led3On") {
                                bulbLayout3.blubState.text = "ON"
                                bulbLayout3.bulbSwitch.isChecked = true
                                bulbLayout3.bulbImg.setImageDrawable(resources.getDrawable(R.drawable.blub_image,
                                    theme))
                            } else {
                                bulbLayout3.blubState.text = "OFF"
                                bulbLayout3.bulbSwitch.isChecked = false
                                bulbLayout3.bulbImg.setImageDrawable(resources.getDrawable(R.drawable.bulb_off_img,
                                    theme))
                            }
                            if (data.led4StateMsg == "led4On") {
                                bulbLayout4.blubState.text = "ON"
                                bulbLayout4.bulbSwitch.isChecked = true
                                bulbLayout4.bulbImg.setImageDrawable(resources.getDrawable(R.drawable.blub_image,
                                    theme))
                            } else {
                                bulbLayout4.blubState.text = "OFF"
                                bulbLayout4.bulbSwitch.isChecked = false
                                bulbLayout4.bulbImg.setImageDrawable(resources.getDrawable(R.drawable.bulb_off_img,
                                    theme))
                            }
                            if (fanSpeedOnTxt.indexOf(data.fanStateMsg) != -1) {
                                fanLayout.fanSlider.value =
                                    fanSpeedOnTxt.indexOf(data.fanStateMsg).toFloat()
                                fanLayout.fanState.text = "ON"
                                fanLayout.fanSwitch.isChecked = true
                                if (data.fanStateMsg != "fan0On") {
                                    rotate.duration =
                                        fanSpeed[fanSpeedOnTxt.indexOf(data.fanStateMsg)].toLong()
                                    rotate.repeatCount = Animation.INFINITE
                                    fanLayout.fanImg.startAnimation(rotate)
                                } else {
                                    fanLayout.fanImg.clearAnimation()
                                }
                            } else if (fanSpeedOffTxt.indexOf(data.fanStateMsg) != -1) {
                                fanLayout.fanSlider.value =
                                    fanSpeedOffTxt.indexOf(data.fanStateMsg).toFloat()
                                fanLayout.fanState.text = "OFF"
                                fanLayout.fanSwitch.isChecked = false
                                fanLayout.fanImg.clearAnimation()
                            }
                            val rgbStatue = data.RGBStateMsg.split("/")
                            if (data.RGBStateMsg.indexOf("RGBOn") != -1) {
                                rgbLayout.rgbState.text = "ON"
                                rgbLayout.rgbSwitch.isChecked = true
                            } else if (data.RGBStateMsg.indexOf("RGBOff") != -1) {
                                rgbLayout.rgbState.text = "OFF"
                                rgbLayout.rgbSwitch.isChecked = false
                            }
                            if (rgbStatue.size == 4) {
                                val hex = String.format("#%02x%02x%02x",
                                    rgbStatue[1].toInt(),
                                    rgbStatue[2].toInt(),
                                    rgbStatue[3].toInt())
                                rgbLayout.rgbImg.setBackgroundColor(Color.parseColor(hex))
                                rgbColorBinding.colorView.setBackgroundColor(Color.parseColor(hex))

                                rgbColorBinding.redLayout.seekBar.progress = rgbStatue[1].toInt()
                                rgbColorBinding.redLayout.textFieldValue.text = "R"
                                rgbColorBinding.redLayout.txtColorValue.text = rgbStatue[1]

                                rgbColorBinding.greenLayout.seekBar.progress = rgbStatue[2].toInt()
                                rgbColorBinding.greenLayout.textFieldValue.text = "G"
                                rgbColorBinding.greenLayout.txtColorValue.text = rgbStatue[2]

                                rgbColorBinding.blueLayout.seekBar.progress = rgbStatue[3].toInt()
                                rgbColorBinding.blueLayout.textFieldValue.text = "B"
                                rgbColorBinding.blueLayout.txtColorValue.text = rgbStatue[3]
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

    private fun setRGBColor() {
        val hex = String.format("#%02x%02x%02x",
            rgbColorBinding.redLayout.seekBar.progress,
            rgbColorBinding.greenLayout.seekBar.progress,
            rgbColorBinding.blueLayout.seekBar.progress)
        rgbColorBinding.colorView.setBackgroundColor(Color.parseColor(hex))
    }

    private fun callUpdateComponent(
        body: HashMap<String, Any>,
        toastState: Boolean,
    ) {
        deviceViewModel.updateComponents(sessionManager.userId, body)
            .observe(this) {

                when (it.status) {

                    Status.LOADING -> {

                    }
                    Status.SUCCESS -> {
                        Log.d("success", it.message.toString())
                        if (toastState && isUpdate) {
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

    private fun callAddComponents() {
        val body = hashMapOf<String, Any>(
            "led1StateMsg" to "led1Off",
            "led2StateMsg" to "led2Off",
            "led3StateMsg" to "led3Off",
            "led4StateMsg" to "led4Off",
            "fan1StateMsg" to "fan1Off/0",
            "fan2StateMsg" to "fan2Off/0",
            "RGB1StateMsg" to "rgb1Off/#FFFFFF/0/0/0",
            "RGB2StateMsg" to "rgb2Off/#FFFFFF/0/0/0",
            "tempValue" to 0.0F,
            "humidityValue" to 0.0F,
        )

        deviceViewModel.addComponents(sessionManager.userId, body).observe(this) {
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
}