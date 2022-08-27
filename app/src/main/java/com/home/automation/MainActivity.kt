package com.home.automation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.home.automation.databinding.ActivityMainBinding
import com.home.automation.utils.startActivityAnimation


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.bluetoothBtn.setOnClickListener {
            startActivity(Intent(this, BluetoothListActivity::class.java))
            startActivityAnimation()
        }

        binding.localWifiBtn.setOnClickListener {
            startActivity(Intent(this, WifiControlActivity::class.java))
            startActivityAnimation()
        }

        binding.globalWifiBtn.setOnClickListener {
            startActivity(Intent(this, GlobalWifiActivity::class.java))
            startActivityAnimation()
        }

    }

}

