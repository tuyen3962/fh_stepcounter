package com.drag.ss.fh_stepcounter.models

import android.util.Log
import java.util.ArrayList
import java.util.Date

data class SensorResponse(
    var lastUpdated: Long = 0,
    var lastUpdatedStep: Long = 0,
    var totalStep: Long = 0,
    var recordedSteps: ArrayList<HashMap<String, Double>> = ArrayList<HashMap<String, Double>>()

) {
    fun getTodayStep(): Long {
        var count: Long = 0
        val today = Date()
        val todayStartOfDay = Date()
        todayStartOfDay.hours = 0
        todayStartOfDay.minutes = 0
        todayStartOfDay.seconds = 0
        val todayEndOfDay = Date()
        todayEndOfDay.date = today.date + 1
        todayEndOfDay.hours = 0
        todayEndOfDay.minutes = 0
        todayEndOfDay.seconds = 0

            for (i in 0 until recordedSteps.size) {
                if (this.recordedSteps.isNotEmpty() && todayStartOfDay.time <= this.recordedSteps[i].getValue("time") && this.recordedSteps[i].getValue("time") <= todayEndOfDay.time
                ) {
                    count += this.recordedSteps[i].getValue("value").toLong()
                }
            }
        return count
    }
}