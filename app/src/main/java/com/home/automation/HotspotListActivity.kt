package com.home.automation

import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.home.automation.adapters.HotspotDeviceListAdapter
import com.home.automation.databinding.ActivityHotspotListBinding
import com.home.automation.utils.*

class HotspotListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHotspotListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_hotspot_list)

        init()
    }

    private fun init() {
        visible(binding.loading.llLoading)
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        if (isHotspot(wifiManager)) {
            val deviceList = getClientList()
            if (deviceList.size > 0) {
                gone(binding.loading.llLoading)
                binding.recyclerView.adapter = HotspotDeviceListAdapter(deviceList)

            } else {
                gone(binding.loading.llLoading)
                visible(binding.llNoData.llNoData)
            }
        } else {
            longShowToast("Please, Turn On Hotspot")
        }
    }
}