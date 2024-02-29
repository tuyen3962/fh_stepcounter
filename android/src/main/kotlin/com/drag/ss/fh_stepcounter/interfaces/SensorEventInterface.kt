package com.drag.ss.fh_stepcounter.interfaces

import com.drag.ss.fh_stepcounter.models.SensorResponse

interface SensorEventInterface {
    fun onEvent(sensorResponse: SensorResponse?)
    fun onFailed()
}