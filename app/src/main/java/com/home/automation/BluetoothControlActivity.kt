package com.home.automation

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.home.automation.databinding.ActivityBluetoothControlBinding
import com.home.automation.utils.*
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors


class BluetoothControlActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothControlBinding
    private val myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    companion object {
        var bluetoothSocket: BluetoothSocket? = null
        var mIsBluetoothConnected = false
        var isConnectDevice = true
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_DISCONNECTED -> {
                        finish()
                    }
                }
            }
        }
    }

    private lateinit var bluetoothDevice: BluetoothDevice
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth_control)
        bluetoothDevice = intent.extras?.getParcelable("device")!!
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(broadcastReceiver, filter)
        visible(binding.loading.llLoading)
        binding.led1ToggleBtn.setOnClickListener {
            onOffToggle(binding.led1ToggleBtn, "led1", binding.status1Txt, "Led 1:")
        }
        binding.led2ToggleBtn.setOnClickListener {
            onOffToggle(binding.led2ToggleBtn, "led2", binding.status2Txt, "Led 2:")
        }
        binding.led3ToggleBtn.setOnClickListener {
            onOffToggle(binding.led3ToggleBtn, "led3", binding.status3Txt, "Led 3:")
        }
        binding.led4ToggleBtn.setOnClickListener {
            onOffToggle(binding.led4ToggleBtn, "led4", binding.status4Txt, "Led 4:")
        }
        binding.led5ToggleBtn.setOnClickListener {
            onOffToggle(binding.led5ToggleBtn, "led5", binding.status5Txt, "Led 5:")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onOffToggle(
        ledToggleBtn: ToggleButton,
        name: String,
        statusText: TextView,
        status: String,
    ) {
        if (bluetoothSocket != null) {
            if (ledToggleBtn.isChecked) {
                sendSignal(name + "on*") // on led
            } else {
                sendSignal(name + "off*") // off led
            }
            statusText.text = status + ledToggleBtn.text.toString()
        } else {
            longShowToast("Sorry, Device is not connected!")
            finish()
        }
    }

    private fun sendSignal(number: String) {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket!!.outputStream.write(number.toByteArray())
            } catch (e: IOException) {
                longShowToast("Something Went Wrong!")
            }
        } else {
            longShowToast("Sorry, Device is not connected!")
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        menu.findItem(R.id.refreshId).title = "Serial Monitor"
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refreshId -> {
                startActivity(Intent(this, SerialMonitorActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        if (bluetoothSocket == null || !mIsBluetoothConnected) {
            connectDevice()
        }
        super.onResume()
    }

    override fun onDestroy() {
        if (bluetoothSocket != null && mIsBluetoothConnected) {
            disconnect()
        }
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    @SuppressLint("MissingPermission")
    private fun connectDevice() {
        Executors.newSingleThreadExecutor().execute {
            try {
                if (bluetoothSocket == null || !mIsBluetoothConnected) {

                    bluetoothSocket =
                        bluetoothDevice.createInsecureRfcommSocketToServiceRecord(myUUID)
                    val bluetoothManager =
                        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                    bluetoothManager.adapter.cancelDiscovery()
                    bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                isConnectDevice = false
            }
            Handler(Looper.getMainLooper()).post {
                if (isConnectDevice) {
                    mIsBluetoothConnected = true
                    longShowToast("Connected to device")
                    gone(binding.loading.llLoading)
                } else {
                    Snackbar.make(binding.root, R.string.hc_error, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok) { finish() }.show()
                }

            }
        }
    }

    private fun disconnect() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket!!.close()
            } catch (e: IOException) {
                longShowToast("Something Went Wrong!")
            }
        }
        finish()
    }
}