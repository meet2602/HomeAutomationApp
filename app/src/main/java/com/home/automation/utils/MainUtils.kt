package com.home.automation.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.*
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.StrictMode
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.home.automation.models.HotspotModel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.Socket
import java.net.SocketException


fun requestData(url: String) {
    try {
        Log.d("url", url)
        val client = OkHttpClient()
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val request = Request.Builder()
            .url(url)
            .build()

        val response: Response = client.newCall(request).execute()
        Log.d("body", response.body.toString())
        response.body?.string()
    } catch (error: Exception) {
        Log.d("error", error.toString())
        error.toString()
    }


//    try {
//        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)
//        val clientSocket = Socket(url, 80)
//        clientSocket.close()
//    } catch (e: Exception) {
//        e.printStackTrace()
//    }
}

fun isHotspot(manager: WifiManager): Boolean {
    try {
        val method = manager.javaClass.getDeclaredMethod("isWifiApEnabled")
        return (method.invoke(manager) as Boolean)
    } catch (ignored: Throwable) {
    }
    return false
}

fun Activity.connectToWifi(ssid: String, password: String) {
    val wifiManager =
        applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val wifiNetworkSuggestion =
            WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .setIsHiddenSsid(true) //specify if the network does not broadcast itself and OS must perform a forced scan in order to connect
                .build()


        val suggestionsList = listOf(wifiNetworkSuggestion)
        val status = wifiManager.addNetworkSuggestions(suggestionsList)
        if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
        }
    } else {
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = "\"" + ssid + "\""
        wifiConfig.preSharedKey = "\"" + password + "\""
        var netId: Int = wifiManager.addNetwork(wifiConfig)
        if (netId == -1) netId = getExistingNetworkId(wifiConfig.SSID, wifiManager)

        wifiManager.enableNetwork(netId, true)
        wifiManager.reconnect()
    }

}

@SuppressLint("MissingPermission")
private fun getExistingNetworkId(SSID: String, wifiManager: WifiManager): Int {
    val configuredNetworks: List<WifiConfiguration> = wifiManager.configuredNetworks
    for (existingConfig in configuredNetworks) {
        if (SSID.equals(existingConfig.SSID, ignoreCase = true)) {
            return existingConfig.networkId
        }
    }
    return -1
}

fun getLocalIpAddress(): String? {
    try {
        val en = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val enumIpAddress = en.nextElement().inetAddresses
            while (enumIpAddress.hasMoreElements()) {
                val inetAddress = enumIpAddress.nextElement()
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                    return inetAddress.hostAddress
                }
            }
        }
    } catch (ex: SocketException) {
        ex.printStackTrace()
    }
    return null
}

fun Activity.checkIpAddress(wifiManager: WifiManager, sessionManager: SessionManager): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        if (capabilities != null) {
            if (capabilities) {
                Log.d("IpAddress =", getLocalIpAddress().toString())
                val info = wifiManager.connectionInfo
                val connectedName = info.ssid
                val connectedMacAddress = info.bssid
                val signStrength = getStatus(info.rssi)
                sessionManager.ipAddress = getLocalIpAddress().toString()
                Log.d(
                    "connect_User", connectedName +
                            "\n" +
                            connectedMacAddress + "\n" + signStrength
                )
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }
    return false
}

fun getStatus(DBm: Int): String {
    return if (DBm >= -50) "Excellent" else if (DBm < -50 && DBm >= -60) "Good" else if (DBm < -60 && DBm >= -70) "Fair" else "Poor"
}

val Context.isConnected: Boolean
    get() {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                val nw = connectivityManager.activeNetwork ?: return false
                val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
                when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            }
            else -> {
                // Use depreciated methods only on older devices
                val nwInfo = connectivityManager.activeNetworkInfo ?: return false
                nwInfo.isConnected
            }
        }
    }

fun getClientList(): ArrayList<HotspotModel> {
    val result = ArrayList<HotspotModel>()
    var bufferedReader: BufferedReader? = null
    try {
        bufferedReader = BufferedReader(FileReader("/proc/net/arp"))
        bufferedReader.forEachLine {
            val splitter =
                it.split(" +".toRegex()).dropLastWhile { it1 -> it1.isEmpty() }.toTypedArray()
            if (splitter.size >= 4) {
                val ip = splitter[0]
                val mac = splitter[3]
                val device = splitter[5]
                if (mac.matches("..:..:..:..:..:..".toRegex())) {
                    result.add(
                        HotspotModel(
                            ip,
                            mac, device
                        )
                    )
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            bufferedReader?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return result
}
