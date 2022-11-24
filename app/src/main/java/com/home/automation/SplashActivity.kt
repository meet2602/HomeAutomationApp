package com.home.automation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.home.automation.databinding.ActivitySplashBinding
import com.home.automation.utils.SessionManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val binding: ActivitySplashBinding by lazy {
        DataBindingUtil.setContentView(
            this,
            R.layout.activity_splash
        )
    }
    private val sessionManager: SessionManager by lazy { SessionManager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.gifImageView.postDelayed({
            if (sessionManager.isDeviceConnected) {
                startActivity(Intent(this, DashBoardActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this@SplashActivity,
                    LoginActivity::class.java))
                finish()
            }
        }, 1500)
    }
}