package com.example.daemon.endless

import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.example.daemon.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class EndlessService : Service() {

  private var wakeLock: PowerManager.WakeLock? = null
  private var isServiceStarted = false
  private var screenStateReceiver: ScreenStateReceiver? = null
  private var batteryLevelReceiver: BatteryLevelReceiver? = null

  override fun onBind(intent: Intent): IBinder? {
    log("Some component want to bind with the service")
    // We don't provide binding, so return null
    return null
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    log("onStartCommand executed with startId: $startId")
    if (intent != null) {
      val action = intent.action
      log("using an intent with action $action")
      when (action) {
        Actions.START.name -> startService()
        Actions.STOP.name -> stopService()
        else -> log("This should never happen. No action in the received intent")
      }
    } else {
      log(
        "with a null intent. It has been probably restarted by the system."
      )
    }
    // by returning this we make sure the service is restarted if the system kills the service
    return START_STICKY
  }

  override fun onCreate() {
    super.onCreate()
    log("The service has been created".toUpperCase())
    val notification = createNotification()
    startForeground(1, notification)
    registerScreenStateReceiver()
    registerBatteryLevelReceiver()
  }

  override fun onDestroy() {
    super.onDestroy()
    log("The service has been destroyed".toUpperCase())
    Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show()
    unregisterScreenStateReceiver()
    unregisterBatteryLevelReceiver()
  }

  override fun onTaskRemoved(rootIntent: Intent) {
    val restartServiceIntent = Intent(applicationContext, EndlessService::class.java).also {
      it.setPackage(packageName)
    };
    val restartServicePendingIntent: PendingIntent =
      PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
    applicationContext.getSystemService(Context.ALARM_SERVICE);
    val alarmService: AlarmManager =
      applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager;
    alarmService.set(
      AlarmManager.ELAPSED_REALTIME,
      SystemClock.elapsedRealtime() + 1000,
      restartServicePendingIntent
    );
  }

  private fun startService() {
    if (isServiceStarted) return
    log("Starting the foreground service task")
    Toast.makeText(this, "Service starting its task", Toast.LENGTH_SHORT).show()
    isServiceStarted = true
    setServiceState(this, ServiceState.STARTED)

    // we need this lock so our service gets not affected by Doze Mode
    wakeLock =
      (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
        newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
          acquire()
        }
      }

    // we're starting a loop in a coroutine
    GlobalScope.launch(Dispatchers.IO) {
      while (isServiceStarted) {
        launch(Dispatchers.IO) {
          /*val mActivityManager =
            this@EndlessService.getSystemService(ACTIVITY_SERVICE) as ActivityManager

          var currentPackageName = ""
          if (Build.VERSION.SDK_INT > 20) {
            currentPackageName = mActivityManager.runningAppProcesses[0].processName
          } else {
            currentPackageName = mActivityManager.getRunningTasks(1)[0].topActivity!!.packageName
          }

          Log.d(TAG, "startService: currentPackageName = $currentPackageName")*/

          var topPackageName: String? = null
          try {
            val am = this@EndlessService.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val appProcesses = am.runningAppProcesses
            for (appProcess in appProcesses) {
              Log.d(TAG, "startService: appProcess = ${appProcess.processName}")
              if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                topPackageName = appProcess.processName
              }
            }
          } catch (e: Exception) {
            Log.e(TAG, "startService: ", e)
          }
          Log.d(TAG, "Current App in foreground is: $topPackageName")
        }
        delay(5 * 1000)
      }
      log("End of the loop for the service")
    }
  }

  private fun stopService() {
    log("Stopping the foreground service")
    Toast.makeText(this, "Service stopping", Toast.LENGTH_SHORT).show()
    try {
      wakeLock?.let {
        if (it.isHeld) {
          it.release()
        }
      }
      stopForeground(true)
      stopSelf()
    } catch (e: Exception) {
      log("Service stopped without being started: ${e.message}")
    }
    isServiceStarted = false
    setServiceState(this, ServiceState.STOPPED)
  }

  private fun createNotification(): Notification {
    val notificationChannelId = "ENDLESS SERVICE CHANNEL"

    // depending on the Android API that we're dealing with we will have
    // to use a specific method to create the notification
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val channel = NotificationChannel(
        notificationChannelId,
        "Endless Service notifications channel",
        NotificationManager.IMPORTANCE_HIGH
      ).let {
        it.description = "Endless Service channel"
        it.enableLights(true)
        it.lightColor = Color.RED
        it.enableVibration(true)
        it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        it
      }
      notificationManager.createNotificationChannel(channel)
    }

    val pendingIntent: PendingIntent =
      Intent(this, MainActivity::class.java).let { notificationIntent ->
        PendingIntent.getActivity(this, 0, notificationIntent, 0)
      }

    val builder: Notification.Builder =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
        this,
        notificationChannelId
      ) else Notification.Builder(this)

    return builder
      .setContentTitle("Endless Service")
      .setContentText("This is your favorite endless service working")
      .setContentIntent(pendingIntent)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setTicker("Ticker text")
      .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
      .build()
  }

  private fun registerScreenStateReceiver() {
    screenStateReceiver = ScreenStateReceiver()
    val filter = IntentFilter()
    filter.addAction(Intent.ACTION_SCREEN_ON)
    filter.addAction(Intent.ACTION_SCREEN_OFF)
    registerReceiver(screenStateReceiver, filter)
  }

  private fun unregisterScreenStateReceiver() {
    try {
      if (screenStateReceiver != null) {
        unregisterReceiver(screenStateReceiver)
      }
    } catch (e: Exception) {
      Log.e(TAG, "unregisterScreenStateReceiver: ", e)
    }
  }

  private fun registerBatteryLevelReceiver() {
    batteryLevelReceiver = BatteryLevelReceiver()
    val filter = IntentFilter()
    filter.addAction(Intent.ACTION_BATTERY_LOW)
    filter.addAction(Intent.ACTION_BATTERY_OKAY)
    registerReceiver(batteryLevelReceiver, filter)
  }

  private fun unregisterBatteryLevelReceiver() {
    try {
      if (batteryLevelReceiver != null) {
        unregisterReceiver(batteryLevelReceiver)
      }
    } catch (e: Exception) {
      Log.e(TAG, "unregisterBatteryLevelReceiver: ", e)
    }
  }

  companion object {
    private const val TAG = "EndlessService"
  }
}
