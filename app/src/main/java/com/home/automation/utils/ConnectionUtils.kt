package com.home.automation.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Build
import androidx.lifecycle.LiveData


class ConnectionUtils(
    val context: Context,
) : LiveData<Boolean>() {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()


    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnectedOrConnecting) {
                postValue(true)
            } else {
                postValue(false)
            }
        }

    }

    private var networkCallback: ConnectivityManager.NetworkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                postValue(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                postValue(false)
            }
        }


    fun start() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        } else {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        }
    }

    fun stop() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            context.unregisterReceiver(broadCastReceiver)
        } else {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

}