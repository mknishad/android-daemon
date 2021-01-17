package com.example.daemon

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent


class WindowChangeDetectingService : AccessibilityService() {

  override fun onServiceConnected() {
    super.onServiceConnected()
    Log.d(TAG, "onServiceConnected: ")

    //Configure these here for compatibility with API 13 and below.
    val config = AccessibilityServiceInfo()
    config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
    config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
    if (Build.VERSION.SDK_INT >= 16) //Just in case this helps
      config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
    serviceInfo = config
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent) {
    if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
      if (event.packageName != null && event.className != null) {
        /*val componentName = ComponentName(
          event.packageName.toString(),
          event.className.toString()
        )*/
        //val activityInfo = tryGetActivity(componentName)
        Log.d(TAG, "onAccessibilityEvent: package = ${event.packageName}")
        //Toast.makeText(this, event.packageName, Toast.LENGTH_SHORT).show()
        //val isActivity = activityInfo != null
        //if (isActivity) Log.i(TAG, componentName.flattenToShortString())
      }
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    return START_STICKY
  }



  private fun tryGetActivity(componentName: ComponentName): ActivityInfo? {
    return try {
      packageManager.getActivityInfo(componentName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
      null
    }
  }

  override fun onInterrupt() {}

  companion object {
    private const val TAG = "WindowChangeDetectingSe"
  }
}
