package com.cookbook.app.utils

import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.cookbook.app.R
import com.cookbook.app.model.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.concurrent.TimeUnit

class Constants {

    companion object{
        var loggedUserId:String = ""
        var loggedUser: User? = null
        var alert: AlertDialog? = null

        fun isOnline(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        }

        fun getTimeAgo(timestamp: Long): String {
            val currentTime = System.currentTimeMillis()
            val diff = currentTime - timestamp

            val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            return when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "$minutes minutes ago"
                hours < 24 -> "$hours hours ago"
                days < 7 -> "$days days ago"
                days < 30 -> "${days / 7} weeks ago"
                else -> "${days / 30} months ago"
            }
        }

        fun showAlert(context: Context, message: String) {
            MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                .create().show()
        }

        fun showAlert(context: Context, message: String,listener: DialogInterface.OnClickListener) {
            MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog, which -> listener.onClick(dialog,which) }
                .create().show()
        }

        fun startLoading(context: Context) {
            if(alert == null){
                val builder = MaterialAlertDialogBuilder(context)
                val layout = LayoutInflater.from(context).inflate(R.layout.custom_loading, null)
                builder.setView(layout)
                builder.setCancelable(false)
                alert = builder.create()
                alert?.show()
            }
        }

        fun dismiss() {
            alert?.dismiss()
        }
    }

}