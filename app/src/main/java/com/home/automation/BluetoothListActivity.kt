package com.home.automation

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.home.automation.adapters.BluetoothDeviceListAdapter
import com.home.automation.databinding.ActivityBluetoothListBinding
import com.home.automation.utils.*
import java.lang.reflect.Method

class BluetoothListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBluetoothListBinding
    private val permissionRequestN = Manifest.permission.BLUETOOTH
    private var bluetoothAdapter: BluetoothAdapter? = null

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                longShowToast("Bluetooth Enabled successfully")
                scanDevice()
            } else {
                showSnackBarWithActionINDEFINITE(
                    binding.root,
                    resources.getString(R.string.permission_error)
                ) { permission() }
            }
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
                    BluetoothAdapter.STATE_OFF -> {}
                    BluetoothAdapter.STATE_TURNING_OFF -> {
                        init()
                    }
                    BluetoothAdapter.STATE_ON -> {}
                    BluetoothAdapter.STATE_TURNING_ON -> {}
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth_list)
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(broadcastReceiver, filter)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter



        init()
    }

    private fun init() {
        visible(binding.loading.llLoading)
        if (bluetoothAdapter == null) {
            longShowToast("Bluetooth not found")
            gone(binding.loading.llLoading)
            visible(binding.llNoData.llNoData)
        } else if (!bluetoothAdapter!!.isEnabled) {
            permission()
        } else {
            scanDevice()
        }
    }

    private fun permission() {
        val enableBT = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        resultLauncher.launch(enableBT)
    }

    private fun scanDevice() {
        if (ContextCompat.checkSelfPermission(
                this,
                permissionRequestN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val deviceList = bluetoothAdapter!!.bondedDevices
            if (deviceList.size > 0) {
                gone(binding.loading.llLoading)
                val unPairDeviceList = ArrayList(deviceList)
                val bluetoothDeviceListAdapter = BluetoothDeviceListAdapter(unPairDeviceList,
                    object : BluetoothDeviceListAdapter.OnClickListener {
                        override fun onConnectClick(position: Int, device: BluetoothDevice) {
                            if (bluetoothAdapter!!.isEnabled) {
                                val intent =
                                    Intent(
                                        this@BluetoothListActivity,
                                        BluetoothControlActivity::class.java
                                    )
                                intent.putExtra("device", device)
                                startActivity(intent)
                            } else {
                                permission()
                            }
                        }

                        override fun onUnPairClick(
                            position: Int,
                            device: BluetoothDevice,
                            notifyItemRemoved: Unit,
                        ) {
                            if (bluetoothAdapter!!.isEnabled) {
                                try {
                                    val removeBondMethod: Method =
                                        device.javaClass.getMethod("removeBond")
                                    if (removeBondMethod.invoke(device) as Boolean) {
                                        longShowToast(
                                            "Device Unpaired Successfully!"
                                        )
                                        unPairDeviceList.removeAt(position)
                                        notifyItemRemoved
                                    } else {
                                        longShowToast("Something Went Wrong!")
                                        val intentOpenBluetoothSettings = Intent()
                                        intentOpenBluetoothSettings.action =
                                            Settings.ACTION_BLUETOOTH_SETTINGS
                                        startActivity(intentOpenBluetoothSettings)
                                    }
                                } catch (e: Exception) {
                                    longShowToast(e.message.toString())
                                    e.printStackTrace()
                                }
                            } else {
                                permission()
                            }
                        }
                    })
                binding.recyclerView.adapter = bluetoothDeviceListAdapter
            } else {

                gone(binding.loading.llLoading)
                visible(binding.llNoData.llNoData)
            }
        } else {
            gone(binding.loading.llLoading)
            visible(binding.llNoData.llNoData)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }
}