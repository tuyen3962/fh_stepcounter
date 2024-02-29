package com.drag.ss.fh_stepcounter.models

import com.google.gson.annotations.SerializedName

data class StepModel(
    @SerializedName("time")
    var time: String? = null,
    @SerializedName("value")
    var value: String? = null)
