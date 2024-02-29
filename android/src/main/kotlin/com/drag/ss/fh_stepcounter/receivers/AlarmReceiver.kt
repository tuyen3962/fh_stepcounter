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
import com.drag.ss.fh_stepcounter.FHStepCounterUtil
import com.drag.ss.fh_stepcounter.services.AlarmService


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // setup broadcast receiver for reboot:
        val service = Intent(context, AlarmService::class.java)
        val enabled = intent?.getBooleanExtra("enabled", false)
        val delay = intent?.getIntExtra("delay", FHStepCounterSensorListener.ALARM_DELAY_IN_SECOND)
        val repeat = intent?.getBooleanExtra("repeat", false)
        service.setAction(intent?.action)
        service.putExtra("enabled", enabled)
        // Get AlarmManager instance
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
        if (intent != null && intent.action != null) {
            if (intent.action == FHStepCounterSensorListener.SENSOR_STEP_BROADCAST || intent.action == "BOOT_COMPLETED" || intent.action == "REBOOT_ALARM_BROADCAST" || intent.action == Intent.ACTION_BOOT_COMPLETED) {
                if (enabled == true) {
                    //  RESET/RECALL ALARM:
                    if (repeat == true) {
                        // setup broadcast receiver for step record:
                        if (delay != null) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                System.currentTimeMillis() + delay * 1000L,
                                recreateAlarmPendingIntent
                            )
                        }
                    }

                    // CALLING SERVICE:
                    service.putExtra("enabled", enabled)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(service)
                    } else {
                        context.startService(service)
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(service)
                    } else {
                        context.startService(service)
                    }
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val notificationCompat = NotificationManagerCompat.from(context)
                    notificationManager.cancel(1)
                    notificationCompat.cancel(1)
                    //              context.stopService(service);
                }
            } else if (intent.action == FHStepCounterSensorListener.SENSOR_STEP_BROADCAST_STOP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(service)
                } else {
                    context.startService(service)
                }
                if (recreateAlarmPendingIntent != null) {
                    alarmManager.cancel(recreateAlarmPendingIntent)
                    recreateAlarmPendingIntent.cancel()
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notificationCompat = NotificationManagerCompat.from(context)
                notificationManager.cancel(1)
                notificationCompat.cancel(1)
                FHStepCounterUtil.setLastUpdatedStep(context, 0L)
                FHStepCounterUtil.setLastUpdatedTime(context, 0L)
                FHStepCounterUtil.setTotalStep(context, 0L)
                FHStepCounterUtil.setRecordedSteps(context, ArrayList<HashMap<String, Double>>())
                FHStepCounterUtil.setIsRecording(context, false)
            }
        }
    }
}
