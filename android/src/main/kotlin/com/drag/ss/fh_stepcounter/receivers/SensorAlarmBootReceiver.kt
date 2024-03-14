package com.drag.ss.fh_stepcounter.receivers

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.drag.ss.fh_stepcounter.FHStepCounterSensorListener
import com.drag.ss.fh_stepcounter.services.AlarmService


class SensorAlarmBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.action != null) {
            if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                // setup broadcast receiver for reboot:
                val service = Intent(context, AlarmService::class.java)
                val enabled = true
                val delay: Int = FHStepCounterSensorListener.ALARM_DELAY_IN_SECOND
                val repeat = true
                service.setAction(intent.action)
                service.putExtra("enabled", enabled)
                // Get AlarmManager instance
                //val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val recreateAlarmIntent = Intent(context, AlarmReceiver::class.java)
                recreateAlarmIntent.setAction(FHStepCounterSensorListener.SENSOR_STEP_BROADCAST)
                recreateAlarmIntent.putExtra("enabled", true)
                recreateAlarmIntent.putExtra("delay", delay)
                recreateAlarmIntent.putExtra("repeat", true)
                val recreateAlarmPendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    recreateAlarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                if (enabled) {
                    //  RESET/RECALL ALARM:
                    if (repeat) {
                        // setup broadcast receiver for step record:
//                        alarmManager.setExactAndAllowWhileIdle(
//                            AlarmManager.RTC_WAKEUP,
//                            System.currentTimeMillis() + delay * 1000L,
//                            recreateAlarmPendingIntent
//                        )
                        context?.sendBroadcast(recreateAlarmIntent)
                    }
                    // CALLING SERVICE:
                    service.putExtra("enabled", enabled)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context?.startForegroundService(service)
                    } else {
                        context?.startService(service)
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context?.startForegroundService(service)
                    } else {
                        context?.startService(service)
                    }
                    val notificationManager =
                        context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val notificationCompat = NotificationManagerCompat.from(context)
                    notificationManager.cancel(1)
                    notificationCompat.cancel(1)
                }
            }
        }
    }
}