package com.drag.ss.fh_stepcounter

import android.app.NotificationManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.drag.ss.fh_stepcounter.interfaces.SensorEventInterface
import com.drag.ss.fh_stepcounter.models.SensorResponse


class FHStepCounterSensorListener(private val context: Context, val sensorEventInterface: SensorEventInterface) : SensorEventListener {
    companion object{
        val TAG = "SensorListener"
        val SENSOR_STEP_BROADCAST = "SENSOR_STEP_BROADCAST"
        val SENSOR_STEP_BROADCAST_STOP = "SENSOR_STEP_BROADCAST_STOP"
        val ALARM_DELAY_IN_SECOND = 30
    }
    private var mSensorManager: SensorManager? = null
    private var mStepCounter: Sensor? = null

    var mSensor = Sensor.TYPE_STEP_COUNTER

    var enabled = false


    enum class SensorType(private val type: String) {
        COUNTER("COUNTER"),
        DETECTOR("DETECTOR");

        fun equalsName(otherType: String): Boolean {
            return type == otherType
        }

        override fun toString(): String {
            return type
        }
    }

    fun startSensor() {
        mSensorManager = (context.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
        mStepCounter = mSensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        mSensorManager?.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_FASTEST)
    }

    fun stopSensor() {
        mSensorManager?.unregisterListener(this, mStepCounter)
        mSensorManager = null
    }


    override fun onSensorChanged(sensorEvent: SensorEvent) {
        val mySensor: Sensor = sensorEvent.sensor

        if (mySensor.type == Sensor.TYPE_STEP_COUNTER) {
            val curTime = System.currentTimeMillis()
            var totalStep: Long = FHStepCounterUtil.getTotalStep(context)
            var lastStep: Long = FHStepCounterUtil.getLastUpdatedStep(context)
            var lastUpdate: Long = FHStepCounterUtil.getLastUpdatedTime(context)
            val recordedStep: ArrayList<HashMap<String, Double>>? =
                if (FHStepCounterUtil.getRecordedSteps(context) != null) FHStepCounterUtil.getRecordedSteps(
                    context
                ) else ArrayList<HashMap<String, Double>>()
            val sensorResponse = SensorResponse()

            // first initialize case => reset all data to default value:
            var isFineToUpdate = false
            if (lastUpdate == 0L) {
                // reset value
//                todayStep = 0;
                totalStep = 0
                lastStep = sensorEvent.values.get(0).toLong()
                lastUpdate = curTime
                //                recordedStep = Arguments.createArray();
                isFineToUpdate = true
            } else if (curTime - lastUpdate > 1000) {
                // calculate foot step counter:
                val deltaCount: Double
                if (sensorEvent.values.get(0) < lastStep) { // sensor might have been reset
                    deltaCount = sensorEvent.values[0].toDouble()
                } else {
                    deltaCount = ((sensorEvent.values[0] - lastStep).toDouble())
                }
                lastStep = sensorEvent.values[0].toLong()
                totalStep =
                    (totalStep + deltaCount).toLong() //update total steps (counted from the last opened app time
                lastUpdate = curTime
                val stepModel: HashMap<String, Double> = HashMap()
                stepModel["time"] = lastUpdate.toDouble()
                stepModel["value"] = deltaCount
                recordedStep?.add(stepModel)
                isFineToUpdate = true
            }
            if (isFineToUpdate) {
                sensorResponse.lastUpdated = lastUpdate
                sensorResponse.lastUpdatedStep = lastStep
                sensorResponse.totalStep = totalStep
                val todayStep = sensorResponse.getTodayStep()
                if (recordedStep != null) {
                    sensorResponse.recordedSteps = recordedStep

                }

                val event: HashMap<String, Any> = HashMap()
                event["onSensorChanged"] = sensorResponse.getTodayStep()
                FhStepcounterPlugin.queueEvent.emit(event)
                sensorEventInterface.onEvent(sensorResponse)

                /// TODO: do not know what did below doing
//             // context - is the context you get from broadcastreceivers onReceive
//                val rnApp: ReactApplication = mContext.getApplicationContext() as ReactApplication
//                val outputData: WritableMap = Arguments.createMap()
//                outputData.putDouble("todayStep", todayStep)
//                outputData.putDouble("lastUpdated", lastUpdate)
//                //              Toast.makeText(mContext, "today step:" + todayStep, Toast.LENGTH_LONG).show();
//          outputData.putArray("records", recordedStep);
//                if (rnApp.getReactNativeHost() != null && rnApp.getReactNativeHost()
//                        .getReactInstanceManager() != null && rnApp.getReactNativeHost()
//                        .getReactInstanceManager()
//                        .getCurrentReactContext() != null && (rnApp.getReactNativeHost()
//                        .getReactInstanceManager()
//                        .getCurrentReactContext()
//                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java) != null)
//                ) {
//                    rnApp.getReactNativeHost().getReactInstanceManager()
//                        .getCurrentReactContext()
//                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
//                        .emit("StepCounterEvent", outputData)
//                }
                // if (sensorEventInterface != null) {
                //   sensorEventInterface.onEvent(sensorResponse)
                //}
                Log.d(TAG, "Today step:$todayStep - lastUpdated:$lastUpdate")
            }
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}