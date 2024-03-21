package com.drag.ss.fh_stepcounter
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.drag.ss.fh_stepcounter.models.SensorResponse

class NotificationHandler(private val mContext: Context) {
    private val notificationManager: NotificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(mContext.applicationContext)
    private var channelId = ""

    init {
        this.channelId = createNotificationChannel("step_counter", "Background Step Counter Service");
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val channel = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_LOW
        )
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager.createNotificationChannel(channel)
        return channelId
    }

    fun cancelSensorNotification() {
        notificationManager.cancel(1)
        notificationManagerCompat.cancel(1)
    }

    fun createSensorNotification(): Notification {
        val sensorResponse = SensorResponse()
        sensorResponse.recordedSteps = FHStepCounterUtil.getRecordedSteps(mContext) ?: ArrayList()
        val footStepToday =
            String.format("%,d", sensorResponse.getTodayStep())
        val uri = Uri.parse("teamscare://app/open/check_point")
        val buttonIntent = Intent(Intent.ACTION_VIEW, uri)
        val pendingIntent = PendingIntent.getActivity(
            mContext,
            0,
            buttonIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val remoteViews =
                RemoteViews(mContext.packageName, R.layout.custom_notification_a12)
            remoteViews.setTextViewText(R.id.tv_content, footStepToday)
            val largeIcon =
                BitmapFactory.decodeResource(mContext.resources, R.mipmap.ic_launcher_round)
            // setup broadcast receiver for step record:
           // remoteViews.setOnClickPendingIntent(R.id.tv_turn_off, pendingIntent)
            return NotificationCompat.Builder(mContext, channelId)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setCustomContentView(remoteViews)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setPriority(notificationManager.importance) //        .setColor(ContextCompat.getColor(mContext, R.color.red))
                .setSmallIcon(R.mipmap.ic_launcher_round) // we must add this to use custom notification
                .setLargeIcon(largeIcon)
                .build()
        }
        val remoteViews =
            RemoteViews(mContext.packageName, R.layout.custom_notification)
        remoteViews.setTextViewText(R.id.tv_content, footStepToday)
        val largeIcon =
            BitmapFactory.decodeResource(mContext.resources, R.mipmap.ic_launcher_round)
        // setup broadcast receiver for step record:
       // remoteViews.setOnClickPendingIntent(R.id.tv_turn_off, pendingIntent)
        return NotificationCompat.Builder(mContext, channelId)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setCustomContentView(remoteViews)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(notificationManager.importance)
            .setSmallIcon(R.mipmap.ic_launcher_round) // we must add this to use custom notification
            .setLargeIcon(largeIcon) //      .setLargeIcon(largeIcon) // we must add this to use custom notification
            .build()
    }
}
