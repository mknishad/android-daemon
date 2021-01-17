package com.example.daemon.endless

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log

class NetworkStateReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
      val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
      val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
      val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

      when (activeNetwork?.type) {
        ConnectivityManager.TYPE_WIFI -> {
          Log.d(TAG, "onReceive: Connected to WIFI!")
        }
        ConnectivityManager.TYPE_MOBILE -> {
          Log.d(TAG, "onReceive: Connected to Mobile data")
        }
        else -> {
          Log.d(TAG, "onReceive: Network Disconnected!")
        }
      }
    }
  }

  companion object {
    private const val TAG = "NetworkStateReceiver"
  }
}
