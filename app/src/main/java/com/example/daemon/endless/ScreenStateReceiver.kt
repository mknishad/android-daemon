package com.example.daemon.endless

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScreenStateReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_SCREEN_ON) {
      Log.d(TAG, "onReceive: Screen On")
    } else if (intent.action == Intent.ACTION_SCREEN_OFF){
      Log.d(TAG, "onReceive: Screen Off")
    }
  }

  companion object {
    private const val TAG = "ScreenStateReceiver"
  }
}
