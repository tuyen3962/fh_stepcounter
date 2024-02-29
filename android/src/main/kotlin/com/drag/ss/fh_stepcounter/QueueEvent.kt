package com.dragn.pawday_step

import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import java.lang.Exception

class QueueEvent(private val eventChannel: EventChannel) {

    private var eventSink: EventChannel.EventSink? = null
    companion object{
        private var instance: QueueEvent? = null
        fun getInstance(eventChannel: EventChannel): QueueEvent {
            return instance ?: synchronized(this) {
                instance ?: QueueEvent(eventChannel).also { instance = it }
            }
        }
    }



    fun emit(event: HashMap<String, Any>){
        eventSink?.success(event)
    }

    fun emitError(exception: Exception){
        eventSink?.error("500", "error", exception.toString())
    }
    init {
        eventChannel.setStreamHandler(
            object : EventChannel.StreamHandler{
                override fun onListen(arguments: Any?, events: EventSink?) {
                    eventSink = events
                }
                override fun onCancel(arguments: Any?) {
                    eventSink = null
                }

            }
        )
    }
}