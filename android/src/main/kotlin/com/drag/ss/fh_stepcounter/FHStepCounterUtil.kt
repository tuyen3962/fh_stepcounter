package com.drag.ss.fh_stepcounter

import android.content.Context
import com.drag.ss.fh_stepcounter.models.StepModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class FHStepCounterUtil {
   companion object{
       private var gson: Gson = Gson()
       const val KEY_RN_SENSOR_STEP_FILE = "rn_sensor_step_file"
       const val KEY_LAST_SENSOR_UPDATED_TIME = "last_sensor_updated_time"
       const val KEY_LAST_SENSOR_UPDATED_STEP = "last_sensor_updated_step"
       const val KEY_TOTAL_SENSOR_STEP = "sensor_total_step"
       const val KEY_TODAY_SENSOR_STEP = "sensor_today_step"
       const val KEY_RECORDED_STEPS = "sensor_recorded_steps"
       const val KEY_DELAY_TIME_MILIS = "sensor_delay_time_milis"
       const val KEY_SENSOR_IS_RECORDING = "sensor_is_recording"
       const val KEY_SENSOR_IS_ENABLED = "sensor_is_enabled"
       const val KEY_SENSOR_BG_ACTION = "sensor_bg_action"
       const val KEY_SENSOR_PAUSE = "sensor_pause"

       // creating a new variable for gson.
       fun getLastUpdatedTime(context: Context): Long {
           val sp = context.getSharedPreferences(
               KEY_RN_SENSOR_STEP_FILE,
               Context.MODE_PRIVATE
           )
           return sp.getLong(KEY_LAST_SENSOR_UPDATED_TIME, 0L)
       }

       fun setLastUpdatedTime(context: Context, value: Long) {
           val sp = context.getSharedPreferences(KEY_RN_SENSOR_STEP_FILE, Context.MODE_PRIVATE)
           sp.edit().putLong(KEY_LAST_SENSOR_UPDATED_TIME, value).apply()
       }

       fun getLastUpdatedStep(context: Context): Long {
           val sp = context.getSharedPreferences(
               KEY_RN_SENSOR_STEP_FILE,
               Context.MODE_PRIVATE
           )
           return sp.getLong(KEY_LAST_SENSOR_UPDATED_STEP, 0L)
       }

       fun setLastUpdatedStep(context: Context, value: Long) {
           val sp = context.getSharedPreferences(KEY_RN_SENSOR_STEP_FILE, Context.MODE_PRIVATE)
           sp.edit().putLong(KEY_LAST_SENSOR_UPDATED_STEP, value).apply()
       }

       fun getTotalStep(context: Context): Long {
           val sp = context.getSharedPreferences(
               KEY_RN_SENSOR_STEP_FILE,
               Context.MODE_PRIVATE
           )
           return sp.getLong(KEY_TOTAL_SENSOR_STEP, 0L)
       }

       fun setTotalStep(context: Context, value: Long) {
           val sp = context.getSharedPreferences(KEY_RN_SENSOR_STEP_FILE, Context.MODE_PRIVATE)
           sp.edit().putLong(KEY_TOTAL_SENSOR_STEP, value).apply()
       }

       fun getRecordedSteps(context: Context): ArrayList<HashMap<String, Double>>? {
           val sp = context.getSharedPreferences(KEY_RN_SENSOR_STEP_FILE, Context.MODE_PRIVATE)
           // below line is to get the type of our array list.
           val storedData = sp.getString(KEY_RECORDED_STEPS, "")
           val sectionlist: List<StepModel> =
               gson.fromJson(storedData, object : TypeToken<List<StepModel>>() {}.type)
           if (sectionlist.isNotEmpty()) {
               val writableArray = ArrayList<HashMap<String, Double>>()
               for (item in sectionlist) {
                   val stepModel: HashMap<String, Double> = HashMap()
                   if (item.time != null) {
                       stepModel.put("time", item.time!!.toDouble())
                       stepModel.put("value", item.value!!.toDouble())
                       writableArray.add(stepModel)
                   }
               }
               return writableArray
           }
           return ArrayList<HashMap<String, Double>>()
       }

       fun setRecordedSteps(context: Context, value: ArrayList<HashMap<String, Double>>) {
           val sp = context.getSharedPreferences(KEY_RN_SENSOR_STEP_FILE, Context.MODE_PRIVATE)
           // getting data from gson and storing it in a string.
           val json = value.toString()
           sp.edit().putString(KEY_RECORDED_STEPS, json).apply()
       }

       fun getIsRecording(context: Context): Boolean {
           val sp = context.getSharedPreferences(
               KEY_RN_SENSOR_STEP_FILE,
               Context.MODE_PRIVATE
           )
           return sp.getBoolean(KEY_SENSOR_IS_RECORDING, false)
       }

       fun setIsRecording(context: Context, value: Boolean) {
           val sp = context.getSharedPreferences(KEY_RN_SENSOR_STEP_FILE, Context.MODE_PRIVATE)
           val isRecording = sp.getBoolean(KEY_SENSOR_IS_RECORDING, false)
           sp.edit().putBoolean(KEY_SENSOR_IS_RECORDING, value).apply()
           if(isRecording != value){
               val event = HashMap<String ,Any>()
               event["isRecording"] = value
               FhStepcounterPlugin.queueEvent.emit(event)
           }
       }

       fun getIsEnabled(context: Context): Boolean {
           val sp = context.getSharedPreferences(
               KEY_RN_SENSOR_STEP_FILE,
               Context.MODE_PRIVATE
           )
           return sp.getBoolean(KEY_SENSOR_IS_ENABLED, false)
       }

       fun setIsEnabled(context: Context, value: Boolean) {
           val sp = context.getSharedPreferences(KEY_RN_SENSOR_STEP_FILE, Context.MODE_PRIVATE)
           sp.edit().putBoolean(KEY_SENSOR_IS_ENABLED, value).apply()
       }

       fun getBgAction(context: Context): String? {
           val sp = context.getSharedPreferences(
               KEY_RN_SENSOR_STEP_FILE,
               Context.MODE_PRIVATE
           )
           return sp.getString(KEY_SENSOR_BG_ACTION, "")
       }

       fun setBgAction(context: Context, value: String?) {
           val sp = context.getSharedPreferences(KEY_RN_SENSOR_STEP_FILE, Context.MODE_PRIVATE)
           sp.edit().putString(KEY_SENSOR_BG_ACTION, value).apply()
       }

       fun setStepOnPause(context: Context, value: Long){
           val sp = context.getSharedPreferences(KEY_SENSOR_PAUSE, Context.MODE_PRIVATE)
           sp.edit().putLong(KEY_SENSOR_PAUSE, value)
       }

       fun getStepOnPause(context: Context) : Long{
           val sp = context.getSharedPreferences(
               KEY_RN_SENSOR_STEP_FILE,
               Context.MODE_PRIVATE
           )
           return  sp.getLong(KEY_SENSOR_PAUSE, 0L)
       }

       fun getInstance(): FHStepCounterUtil {
           return FHStepCounterSensorUtilInstance.mFHSensorStepUtil
       }

       object FHStepCounterSensorUtilInstance {
           val mFHSensorStepUtil: FHStepCounterUtil = FHStepCounterUtil()
       }
   }
}
