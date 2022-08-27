package com.home.automation.utils

import android.app.Activity
import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.home.automation.R


fun Context.longShowToast(message: String): Toast = Toast
    .makeText(this, message, Toast.LENGTH_LONG)
    .apply {
        show()
    }

fun Context.shortShowToast(message: String): Toast = Toast
    .makeText(this, message, Toast.LENGTH_SHORT)
    .apply {
        show()
    }


fun visible(view: View) {
    view.visibility = View.VISIBLE
}

fun gone(view: View) {
    view.visibility = View.GONE
}

fun invisible(view: View) {
    view.visibility = View.INVISIBLE
}

fun hideSoftKeyboard(view: View) {
    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun showSnackBarWithActionINDEFINITE(
    constraintLayout: Any,
    permission_error: String,
    okListener: View.OnClickListener?,
) {
    Snackbar.make(constraintLayout as View, permission_error, Snackbar.LENGTH_INDEFINITE)
        .setAction("Ok", okListener).show()
}

fun checkSinglePermission(
    activity: Activity,
    permission: String,
    permissionCode: Int,
): Boolean {
    if (ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_DENIED
    ) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),
            permissionCode
        )
    } else {
        return true
    }
    return false
}

fun settingActivityOpen(activity: Activity) {
    Toast.makeText(
        activity,
        "Go to settings and enable permissions",
        Toast.LENGTH_LONG
    )
        .show()
    val i = Intent()
    i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    i.addCategory(Intent.CATEGORY_DEFAULT)
    val packageName = activity.packageName
    i.data = Uri.parse("package:$packageName")
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    activity.startActivity(i)
}

fun showPermissionFaiDialog(activity: Activity, okListener: DialogInterface.OnClickListener) {
    MaterialAlertDialogBuilder(activity)
        .setMessage("Permission is required for this app")
        .setPositiveButton("OK", okListener)
        .setNegativeButton("Cancel", okListener)
        .create()
        .show()
}

fun hideKeyBoard(activity: Activity) {
    val view: View = activity.findViewById(android.R.id.content)
    val inputMethodManager: InputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.startActivityAnimation() {
    hideKeyBoard(this)
    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
}


fun Activity.finishActivityAnimation() {
    hideKeyBoard(this)
    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
}

fun Activity.copyToClipBoard(text: String) {
    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
}

fun Context.isOnline(): Boolean {
    var result = false  // https://stackoverflow.com/a/60827950/13082664
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                result = true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                result = true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                result = true
            }
        }
    } else {
        val activeNetwork = cm.activeNetworkInfo
        if (activeNetwork != null) {
            when (activeNetwork.type) {
                ConnectivityManager.TYPE_WIFI -> {
                    result = true
                }
                ConnectivityManager.TYPE_MOBILE -> {
                    result = true
                }
                ConnectivityManager.TYPE_VPN -> {
                    result = true
                }
            }
        }
    }
    return result
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

fun validEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

enum class VisibleStatus {
    YesInternet,
    Success,
    Error,
    NoInternet,
}

fun visibleStatus(
    status: VisibleStatus,
    noInternet: LinearLayout,
    noData: LinearLayout,
    recyclerView: RecyclerView,
    loading: LinearLayout,
) {
    when (status) {
        VisibleStatus.YesInternet -> {
            gone(noInternet)
            gone(noData)
            gone(recyclerView)
            visible(loading)
        }
        VisibleStatus.Success -> {
            gone(loading)
            gone(noInternet)
            gone(noData)
            visible(recyclerView)
        }
        VisibleStatus.Error -> {
            gone(loading)
            gone(noInternet)
            gone(recyclerView)
            visible(noData)
        }
        VisibleStatus.NoInternet -> {
            gone(loading)
            gone(noData)
            gone(recyclerView)
            visible(noInternet)
        }
    }
}