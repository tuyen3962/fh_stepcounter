




package com.drag.ss.fh_stepcounter

import FHStepCounterApi
import StepToday
import android.Manifest
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import androidx.annotation.RequiresApi
import com.drag.ss.fh_stepcounter.interfaces.SensorEventInterface
import com.drag.ss.fh_stepcounter.models.SensorResponse
import com.drag.ss.fh_stepcounter.receivers.AlarmReceiver
import com.drag.ss.fh_stepcounter.receivers.SensorAlarmBootReceiver
import com.drag.ss.fh_stepcounter.services.AlarmService
import com.dragn.pawday_step.QueueEvent

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import java.util.Date
import kotlin.math.roundToInt

/** FhStepcounterPlugin */
class FhStepcounterPlugin: FlutterPlugin, FHStepCounterApi, ActivityAware {
  private var activity: ActivityPluginBinding? = null
  private var binding: FlutterPlugin.FlutterPluginBinding? = null
  companion object{
    lateinit var queueEvent: QueueEvent
  }

  lateinit var  fhStepSensorListener: FHStepCounterSensorListener

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding
    FHStepCounterApi.setUp(this.binding!!.binaryMessenger, this)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity = null
    FHStepCounterApi.setUp(this.binding!!.binaryMessenger, null)
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activity = binding
    FHStepCounterApi.setUp(this.binding!!.binaryMessenger, this)
  }

  override fun onDetachedFromActivity() {
    activity = null
    binding = null
    FHStepCounterApi.setUp(this.binding!!.binaryMessenger, null)
  }

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    binding = flutterPluginBinding
    val eventChannel = EventChannel(this.binding?.binaryMessenger, "io.dragn/pawday/event_channel")
    FHStepCounterApi.setUp(this.binding!!.binaryMessenger, this)
    queueEvent = QueueEvent(eventChannel)
  }


  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    this.binding = null
    FHStepCounterApi.setUp(this.binding!!.binaryMessenger, null)
  }

  fun checkServiceRunning(serviceName: String): HashMap<String, Boolean> {
    val myMap: HashMap<String, Boolean> = HashMap()
    var serviceRunning = false
    val am = activity?.activity?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
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

  override fun requestPermission() {
    val arrayString = arrayOf<String>(Manifest.permission.ACTIVITY_RECOGNITION)
    activity?.activity?.requestPermissions(arrayString, 0)
    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU){
      this.activity?.activity?.startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
    }
  }

  override fun checkPermission(): Boolean {
      activity?.activity?.let {
          context ->
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          when {
            alarmManager.canScheduleExactAlarms() -> {
              return activity?.activity?.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
            }
            else -> {
              return false
            }
          }
        } else {
          return activity?.activity?.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        }
      }
    return false
  }



  override fun onStart(initialTodayStep: Double){
    activity?.activity?.let {
        context ->
      try {
        // check if already run this service:
        val event = HashMap<String, Any>()
        val isRecording: Boolean = FHStepCounterUtil.getIsRecording(context)
        if (isRecording) {
          val data: HashMap<*, *> = checkServiceRunning(AlarmService::class.java.name)
          if (data.containsKey("isRunning") && data.containsKey("isForeground")) {
            return
          } else {
            FHStepCounterUtil.setIsRecording(context, false)
            onStart(initialTodayStep)
            return
          }
        }

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.setAction(FHStepCounterSensorListener.SENSOR_STEP_BROADCAST)
        intent.putExtra("enabled", true)
        intent.putExtra("delay", FHStepCounterSensorListener.ALARM_DELAY_IN_SECOND)
        intent.putExtra("repeat", true)
        val pendingIntent = PendingIntent.getBroadcast(
          context,
          0,
          intent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager =  context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          System.currentTimeMillis() + FHStepCounterSensorListener.ALARM_DELAY_IN_SECOND,
          pendingIntent
        )
        FHStepCounterUtil.setIsRecording(context, true)

        /// for reboot
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_BOOT_COMPLETED)
        context.registerReceiver(mBroadcastReceiver, filter)

        val stepModel: HashMap<String, Double> = HashMap()
        stepModel["time"] = Date().time.toDouble()
        stepModel["value"] = initialTodayStep
        val recordedStep = ArrayList<HashMap<String, Double>>()
        recordedStep.add(stepModel)
        FHStepCounterUtil.setRecordedSteps(context, recordedStep)
        fhStepSensorListener = FHStepCounterSensorListener(context, object : SensorEventInterface {
          override fun onEvent(sensorResponse: SensorResponse?) {
            if (sensorResponse != null) {
              val iStepModel = HashMap<String, Double>()

              iStepModel["time"] = sensorResponse.lastUpdated.toDouble()
              iStepModel["value"] =  initialTodayStep.roundToInt().toDouble()
              val newRecordedSteps = ArrayList<HashMap<String, Double>>()
              newRecordedSteps.add(iStepModel)
              sensorResponse.recordedSteps = newRecordedSteps

              // save to storage
              val oldTotalStep: Long = FHStepCounterUtil.getTotalStep(context)
              FHStepCounterUtil.setLastUpdatedStep(context, sensorResponse.lastUpdatedStep)
              FHStepCounterUtil.setLastUpdatedTime(context, sensorResponse.lastUpdated)
              Log.i("lastUpdated", sensorResponse.lastUpdated.toString())
              FHStepCounterUtil.setTotalStep(context, sensorResponse.totalStep)
              if (oldTotalStep != sensorResponse.totalStep) { // only add new record if we have new changes
                FHStepCounterUtil.setRecordedSteps(context, sensorResponse.recordedSteps)
              }
            }
            fhStepSensorListener.stopSensor()
            // setup alarm for foreground/background/app killed work:
            //
            // setup broadcast receiver for step record:

            //
            event["start"] = true
            queueEvent.emit(event)
          }

          override fun onFailed() {
            event["start"] = false
            queueEvent.emit(event)
          }
        })
        fhStepSensorListener.startSensor()
      } catch (e: Exception) {
        queueEvent.emitError(e)
      }
    }
  }

  override fun getTodayStep() : StepToday {
    activity?.activity?.let {
      try {
        val lastUpdated = FHStepCounterUtil.getLastUpdatedTime(it).toDouble()
        val sensorResponse = SensorResponse()
        sensorResponse.recordedSteps = FHStepCounterUtil.getRecordedSteps(it) ?: ArrayList()
        val step = sensorResponse.getTodayStep()
        return StepToday(
          lastUpdated,
          step
        )
      } catch (e: java.lang.Exception) {
        return StepToday(null, 0)
      }
    }
    return StepToday(null, 0)
  }

  override fun getRecords() {
    activity?.activity?.let {
        context ->
      try {
        val recordedStep: ArrayList<HashMap<String, Double>> =
          FHStepCounterUtil.getRecordedSteps(
            context
          ) ?: ArrayList<HashMap<String, Double>>()
        val map: HashMap<String, Any> = HashMap()
        val event: HashMap<String, Any> = HashMap()
        map.put("lastUpdated", FHStepCounterUtil.getLastUpdatedTime(context))
        map.put("recorded", recordedStep)
        event["getRecords"] = map
        queueEvent.emit(event)
      } catch (e: java.lang.Exception) {
        queueEvent.emitError(e)
      }
    }
  }

  fun isServiceRunning(serviceName: String): Boolean {
    activity?.activity?.let {
        context -> var serviceRunning = false
      val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
      val l = activityManager.getRunningServices(50)
      val i: Iterator<ActivityManager.RunningServiceInfo> = l.iterator()
      while (i.hasNext()) {
        val runningServiceInfo = i
          .next()
        if (runningServiceInfo.service.className == serviceName) {
          serviceRunning = true
          if (runningServiceInfo.foreground) {
            //service run in foreground
          }
        }
      }
      return serviceRunning
    }
    return false
  }

  override fun stop() {
    activity?.activity?.let {
        context ->
      val isRunning: Boolean = FHStepCounterUtil.getIsRecording(context)
      val event: HashMap<String, Any> = HashMap()
      if (!isRunning) {
        return
      }
      val intent = Intent(context, AlarmReceiver::class.java)
      intent.putExtra("enabled", false)
      intent.putExtra("repeat", false)
      intent.setAction(FHStepCounterSensorListener.SENSOR_STEP_BROADCAST_STOP)
      // setup broadcast receiver for step record:
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        System.currentTimeMillis(),
        pendingIntent
      )
      logout()
      event["stop"] = true
      fhStepSensorListener.stopSensor()
      queueEvent.emit(event)
    }
  }

  override fun pause() {
    activity?.activity?.let {
        context ->
      val isRunning: Boolean = FHStepCounterUtil.getIsRecording(context)
      val event: HashMap<String, Any> = HashMap()
      if (!isRunning) {
        return
      }
      val intent = Intent(context, AlarmReceiver::class.java)
      intent.putExtra("enabled", false)
      intent.putExtra("repeat", false)
      intent.setAction(FHStepCounterSensorListener.SENSOR_STEP_BROADCAST_STOP)
      // setup broadcast receiver for step record:
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        System.currentTimeMillis(),
        pendingIntent
      )

      val sensorResponse = SensorResponse()
      sensorResponse.recordedSteps = FHStepCounterUtil.getRecordedSteps(context) ?: ArrayList()
      val todayStep = sensorResponse.getTodayStep()
      fhStepSensorListener.stopSensor()
      FHStepCounterUtil.setStepOnPause(context, todayStep)
      event["onPauseStep"] = todayStep
      queueEvent.emit(event)
    }
  }

  override fun clearData() {
    activity?.activity?.let {
        context ->
      FHStepCounterUtil.setLastUpdatedStep(context, 0L)
      FHStepCounterUtil.setLastUpdatedTime(context, 0L)
      FHStepCounterUtil.setTotalStep(context, 0L)
      FHStepCounterUtil.setRecordedSteps(context, ArrayList())
      FHStepCounterUtil.setIsRecording(context, false)
      FHStepCounterUtil.setStepOnPause(context, 0L)

      val event = HashMap<String, Any>()
      try {
        event["clearData"] = true
        queueEvent.emit(event)
      } catch (e: java.lang.Exception) {
        event["clearData"] = false
        queueEvent.emit(event)
      }
    }
  }

  override fun logout() {
    activity?.activity?.let {context ->
      FHStepCounterUtil.setLastUpdatedStep(context, 0L);
      FHStepCounterUtil.setLastUpdatedTime(context, 0L);
      FHStepCounterUtil.setTotalStep(context, 0L);
      FHStepCounterUtil.setRecordedSteps(context, ArrayList());
      FHStepCounterUtil.setIsRecording(context, false);
    }
  }

  override fun isRecording() : Boolean {
    activity?.activity?.let { context ->
      val output: Boolean = FHStepCounterUtil.getIsRecording(context)
      // val event = HashMap<String, Any>()
      return if (output) {
        val data: HashMap<*, *> = checkServiceRunning(AlarmService::class.java.name)
        if (data.containsKey("isRunning") && data.containsKey("isForeground")) {
          true
        } else {
          FHStepCounterUtil.setIsRecording(context, false)
          false
        }
      } else {
        false
      }
    }
    return  false
  }

  override fun getPauseSteps(): Long {
    activity?.activity?.let {
        context ->
      val a = FHStepCounterUtil.getStepOnPause(context)
      Log.i("getStepOnPause", a.toString())
      return FHStepCounterUtil.getStepOnPause(context)
    }
    return 0L
  }


  private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
        val serviceIntent = Intent(context, SensorAlarmBootReceiver::class.java)
        context.startService(serviceIntent)
      }
    }
  }
}

