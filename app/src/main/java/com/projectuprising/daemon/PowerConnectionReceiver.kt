package com.projectuprising.daemon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PowerConnectionReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_POWER_CONNECTED) {
      Log.d(TAG, "onReceive: Power Connected")
    } else if (intent.action == Intent.ACTION_POWER_DISCONNECTED) {
      Log.d(TAG, "onReceive: Power Disconnected")
    }
  }

  companion object {
    private const val TAG = "PowerConnectionReceiver"
  }
}
