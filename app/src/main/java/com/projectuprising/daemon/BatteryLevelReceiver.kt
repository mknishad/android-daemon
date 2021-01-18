package com.projectuprising.daemon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BatteryLevelReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BATTERY_LOW) {
      Log.d(TAG, "onReceive: Battery Low")
    } else if (intent.action == Intent.ACTION_BATTERY_OKAY) {
      Log.d(TAG, "onReceive: Battery Okay")
    }
  }

  companion object {
    private const val TAG = "BatteryLevelReceiver"
  }
}
