package com.projectuprising.daemon

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    title = "Endless Service"

    findViewById<Button>(R.id.btnStartService).let {
      it.setOnClickListener {
        log("START THE FOREGROUND SERVICE ON DEMAND")
        actionOnService(Actions.START)
      }
    }

    findViewById<Button>(R.id.btnStopService).let {
      it.setOnClickListener {
        log("STOP THE FOREGROUND SERVICE ON DEMAND")
        actionOnService(Actions.STOP)
      }
    }

    openAccessibilitySettings()
  }

  /*private fun enableAccessibilitySettings() {
    //TODO: First add WRITE_SETTINGS and WRITE_SECURE_SETTINGS permissions in AndroidManifest.xml

    Settings.Secure.putString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    "com.projectuprising.daemon/.WindowChangeDetectingService")
    Settings.Secure.putString(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED,"1")
  }*/

  private fun openAccessibilitySettings() {
    val am = this.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
    val accessibilityServiceList = am.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)
    var found = false
    accessibilityServiceList.forEach {
      if (it.eventTypes == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
        found = true
      }
    }
    if (!found) {
      val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
      startActivity(intent)
    }
  }

  private fun actionOnService(action: Actions) {
    if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) return
    Intent(this, EndlessService::class.java).also {
      it.action = action.name
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        log("Starting the service in >=26 Mode")
        startForegroundService(it)
        return
      }
      log("Starting the service in < 26 Mode")
      startService(it)
    }
  }
}
