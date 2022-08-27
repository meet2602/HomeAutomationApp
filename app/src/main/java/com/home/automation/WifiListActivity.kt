package com.home.automation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.home.automation.adapters.WifiDeviceListAdapter
import com.home.automation.databinding.ActivityWifiListBinding
import com.home.automation.utils.*
import java.util.*


class WifiListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWifiListBinding
    private var wifiManager: WifiManager? = null
    private val permissionRequestId = 1
    private val permissionRequestN = Manifest.permission.ACCESS_FINE_LOCATION
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scanWifiDevice()
            } else {
                showSnackBarWithActionINDEFINITE(
                    binding.root,
                    resources.getString(R.string.permission_error)
                ) { permission() }
            }
        }

//    private val receiverWifi = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            val action = intent.action
//
//            if (network.getType() == ConnectivityManager.TYPE_WIFI) {
//            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION == action) {
//                val sb = StringBuilder()
//                val wifiList: List<ScanResult> = wifiManager!!.scanResults
//                val deviceList: ArrayList<String> = ArrayList()
//                for (scanResult in wifiList) {
//                    sb.append("\n").append(scanResult.SSID).append(" - ")
//                        .append(scanResult.capabilities)
//                    deviceList.add(scanResult.SSID.toString() + " - " + scanResult.capabilities)
//                }
//                Toast.makeText(context, sb, Toast.LENGTH_SHORT).show()
//                val arrayAdapter: ArrayAdapter<*> =
//                    ArrayAdapter(context, android.R.layout.simple_list_item_1, deviceList.toArray())
//                binding.recyclerView.adapter = arrayAdapter
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wifi_list)
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            if (capabilities != null) {
                if (capabilities) {
                    Log.d("IpAddress =", getLocalIpAddress().toString())
                    val info = wifiManager!!.connectionInfo
                    val connectedName = info.ssid
                    val connectedMacAddress = info.bssid
                    val signStrength = getStatus(info.rssi)

                    Log.d(
                        "connect_User", connectedName +
                                "\n" +
                                connectedMacAddress + "\n" + signStrength
                    )
                }
            }
        }
        init()


    }

    private fun init() {
        visible(binding.loading.llLoading)
        if (checkSinglePermission(this, permissionRequestN, permissionRequestId)) {
            isWifi()
        }
    }

    private fun isWifi() {
        if (wifiManager == null) {
            longShowToast("Wifi not found")
            gone(binding.loading.llLoading)
            visible(binding.llNoData.llNoData)
        } else if (!wifiManager!!.isWifiEnabled) {
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
            if (isHotspot(wifiManager!!)) {
                try {
                    /// https://stackoverflow.com/questions/7048922/android-2-3-wifi-hotspot-api/7049074#7049074
                    val method = wifiManager!!.javaClass.getMethod(
                        "setWifiApEnabled",
                        android.net.wifi.WifiConfiguration::class.java,
                        Boolean::class.javaPrimitiveType
                    )
                    method.invoke(wifiManager!!, null, false)
                } catch (e: Exception) {
                    longShowToast(
                        "Please Turn Off Hotspot"
                    )
                }
            } else {
                wifiManager!!.isWifiEnabled = true
                scanWifiDevice()
            }
        }
    }

    private fun scanWifiDevice() {
        longShowToast("Wifi Enabled successfully")
        val scanResults: List<ScanResult> = wifiManager!!.scanResults
        if (scanResults.isNotEmpty()) {
            gone(binding.loading.llLoading)
            val scanDeviceList = ArrayList(scanResults)
            val wifiDeviceListAdapter = WifiDeviceListAdapter(scanDeviceList,
                object : WifiDeviceListAdapter.OnClickListener {
                    override fun onConnectClick(position: Int, device: ScanResult) {
                        if (wifiManager!!.isWifiEnabled) {
                        } else {
                            permission()
                        }
                    }
                })
            binding.recyclerView.adapter = wifiDeviceListAdapter
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


//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun turnOnHotspot() {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//        wifiManager!!.startLocalOnlyHotspot(
//            object : LocalOnlyHotspotCallback() {
//                override fun onStarted(reservation: LocalOnlyHotspotReservation) {
//                    super.onStarted(reservation)
//                    hotspotReservation = reservation
//                    val key: String = hotspotReservation!!.wifiConfiguration!!.preSharedKey
//                    val ussid: String = hotspotReservation!!.wifiConfiguration!!.SSID
//                    println("KEY: $key")
//                    println("USSID: $ussid")
//                }
//
//                override fun onStopped() {
//                    super.onStopped()
//                    println("STOPPED THE HOTSPOT")
//                }
//
//                override fun onFailed(reason: Int) {
//                    super.onFailed(reason)
//                    println("FAILED THE HOTSPOT")
//                }
//            }, Handler()
//        )
//    }

    //        private fun setWifiEnabled(enabled: Boolean) {
//        try {
//            Runtime.getRuntime()
//                .exec(arrayOf("su", "-c", "svc wifi", if (enabled) "enable" else "disable"))
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refreshId -> {
                init()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}