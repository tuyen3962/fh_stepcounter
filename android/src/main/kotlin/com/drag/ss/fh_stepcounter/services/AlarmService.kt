package com.drag.ss.fh_stepcounter.services

import android.app.ActivityManager
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.drag.ss.fh_stepcounter.FHStepCounterSensorListener
import com.drag.ss.fh_stepcounter.FHStepCounterSensorListener.Companion.TAG
import com.drag.ss.fh_stepcounter.FHStepCounterUtil
import com.drag.ss.fh_stepcounter.NotificationHandler
import com.drag.ss.fh_stepcounter.interfaces.SensorEventInterface
import com.drag.ss.fh_stepcounter.models.SensorResponse


class AlarmService : Service() {

    var notificationHandler: NotificationHandler? = null
    var FHStepSensorListener: FHStepCounterSensorListener? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        if (notificationHandler == null) {
            notificationHandler = NotificationHandler(this)
        }
        startForeground(1, notificationHandler?.createSensorNotification())
    }

    private fun checkServiceRunning(serviceName: String): HashMap<*, *> {
        val myMap: HashMap<String?, Boolean?> = HashMap()
        var serviceRunning = false
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val l = am.getRunningServices(50)
        val i: Iterator<ActivityManager.RunningServiceInfo> = l.iterator()
        while (i.hasNext()) {
            val runningServiceInfo = i
                .next()
            if (runningServiceInfo.service.className == serviceName) {
                serviceRunning = true
                if (serviceRunning) {
                    myMap["isRunning"] = serviceRunning
                }
                if (runningServiceInfo.foreground) {
                    myMap["isForeground"] = runningServiceInfo.foreground
                }
            }
        }
        return myMap
    }

    private fun turnOffService(startId: Int) {
        FHStepSensorListener?.stopSensor()
        FHStepSensorListener = null
        if (notificationHandler != null) {
            notificationHandler?.cancelSensorNotification()
        }
        val data = checkServiceRunning(AlarmService::class.java.name)
        if (data.containsKey("isRunning")) {
            stopSelfResult(startId)
            stopSelf(startId)
        }
        if (data.containsKey("isForeground")) {
            stopForeground(true)
        }
        FHStepCounterUtil.setIsRecording(this, false)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val myAction: String?
        val myEnabled: Boolean
        if (intent != null) {
            myAction = intent.action
            myEnabled = intent.getBooleanExtra("enabled", false)
            FHStepCounterUtil.setBgAction(this, myAction)
            FHStepCounterUtil.setIsEnabled(this, myEnabled)
        } else {
            myAction = FHStepCounterUtil.getBgAction(this)
            myEnabled = FHStepCounterUtil.getIsEnabled(this)
        }
        if (myAction != null && myAction == FHStepCounterSensorListener.SENSOR_STEP_BROADCAST || myAction == "BOOT_COMPLETED" || myAction == "REBOOT_ALARM_BROADCAST") {
            if (myEnabled) {
                val temp = ""
                if (FHStepSensorListener == null) {
                    FHStepSensorListener =
                        FHStepCounterSensorListener(this, object : SensorEventInterface {
                            override fun onEvent(sensorResponse: SensorResponse?) {
                                if (sensorResponse != null) {
                                    // save to storage
                                    FHStepCounterUtil.setLastUpdatedStep(
                                        this@AlarmService,
                                        sensorResponse.lastUpdatedStep
                                    )
                                    FHStepCounterUtil.setLastUpdatedTime(
                                        this@AlarmService,
                                        sensorResponse.lastUpdated
                                    )
                                    FHStepCounterUtil.setTotalStep(
                                        this@AlarmService,
                                        sensorResponse.totalStep
                                    )
                                    FHStepCounterUtil.setRecordedSteps(
                                        this@AlarmService,
                                        sensorResponse.recordedSteps
                                    )
                                }
                                if (FHStepSensorListener != null) {
                                    val notification: Notification? =
                                        notificationHandler?.createSensorNotification()
                                    startForeground(1, notification)
                                }
                                // setup alarm for foreground/background/app killed work:
                                FHStepCounterUtil.setIsRecording(applicationContext, true)
                            }

                            override fun onFailed() {
                                val notification: Notification? =
                                    notificationHandler?.createSensorNotification()
                                startForeground(1, notification)
                                FHStepCounterUtil.setIsRecording(applicationContext, true)
                            }
                        })
                    FHStepSensorListener?.startSensor()
                    Log.d(TAG, "SensorListener NULL")
                } else {
                    Log.d(TAG, "rnSensorListener co ne")
                    val notification: Notification? =
                        notificationHandler?.createSensorNotification()
                    startForeground(1, notification)
                    FHStepCounterUtil.setIsRecording(applicationContext, true)
                }
            } else {
                turnOffService(startId)
                FHStepCounterUtil.setIsRecording(applicationContext, false)
            }
        } else if (myAction == FHStepCounterSensorListener.SENSOR_STEP_BROADCAST_STOP) {
            turnOffService(startId)
//            val outputData: WritableMap = Arguments.createMap()
//            outputData.putBoolean("isRecording", RnSensorStepUtil.getIsRecording(this))
//            // context - is the context you get from broadcastreceivers onReceive
//            val rnApp: ReactApplication = this.applicationContext as ReactApplication
//            if (rnApp.getReactNativeHost() != null && rnApp.getReactNativeHost()
//                    .getReactInstanceManager() != null && rnApp.getReactNativeHost()
//                    .getReactInstanceManager()
//                    .getCurrentReactContext() != null && (rnApp.getReactNativeHost()
//                    .getReactInstanceManager()
//                    .getCurrentReactContext()
//                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java) != null)
//            ) {
//                rnApp.getReactNativeHost().getReactInstanceManager()
//                    .getCurrentReactContext()
//                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
//                    .emit("StepCounterStatus", outputData)
//            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        if (FHStepSensorListener != null) {
            FHStepSensorListener?.stopSensor()
            FHStepSensorListener = null
        }
        super.onDestroy()
    }
}