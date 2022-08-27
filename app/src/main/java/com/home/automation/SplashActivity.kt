package com.home.automation

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.home.automation.databinding.ActivitySplashBinding
import com.home.automation.utils.*
import com.home.automation.viewmodel.UserViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity(), View.OnClickListener {
    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
            .create(UserViewModel()::class.java)
    }
    private val binding: ActivitySplashBinding by lazy {
        DataBindingUtil.setContentView(
            this,
            R.layout.activity_splash
        )
    }
    private val connectivityObserver: NetworkConnectivityObserver by lazy {
        NetworkConnectivityObserver(this)
    }
    private val sessionManager: SessionManager by lazy { SessionManager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.noInternet.noInternetClicks = this

    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            connectivityObserver.observe().collect {
                when (it) {
                    ConnectivityObserver.Status.Available -> {
                        gone(binding.noInternet.llNoInternet)
                        visible(binding.txtWelcome)
                        if (FirebaseAuth.getInstance().currentUser != null) {
                            callGetDeviceStatus()
                        } else {
                            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                            finish()
                        }
                    }
                    ConnectivityObserver.Status.Unavailable -> {
                        visible(binding.noInternet.llNoInternet)
                        gone(binding.txtWelcome)
                    }
                    ConnectivityObserver.Status.Losing -> {
                        visible(binding.noInternet.llNoInternet)
                        gone(binding.txtWelcome)
                    }
                    ConnectivityObserver.Status.Lost -> {
                        visible(binding.noInternet.llNoInternet)
                        gone(binding.txtWelcome)
                    }
                }
            }
        }

    }

    private fun callGetDeviceStatus() {
        userViewModel.splashGetUserDetail(FirebaseAuth.getInstance().uid!!).observe(this) {
            when (it.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    Log.d("success", it.data.toString())
                    if (it.data != null) {
                        sessionManager.isDeviceConnected = it.data.deviceConnect
                        sessionManager.ipAddress = it.data.deviceIpAddress
                        if (sessionManager.isDeviceConnected) {
                            startActivity(Intent(this, DashBoardActivity::class.java))
                            finish()
                        } else {
                            startActivity(Intent(this, VerifyActivity::class.java))
                            finish()
                        }
                    } else {
                        startActivity(Intent(this, VerifyActivity::class.java))
                        finish()
                    }
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
                    gone(binding.noInternet.llNoInternet)
                    visible(binding.txtWelcome)
                    if (FirebaseAuth.getInstance().currentUser != null) {
                        callGetDeviceStatus()
                    } else {
                        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                        finish()
                    }
                } else {
                    visible(binding.noInternet.llNoInternet)
                    gone(binding.txtWelcome)
                }
            }
        }
    }
}