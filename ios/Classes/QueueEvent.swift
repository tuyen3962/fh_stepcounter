//
//  QueueEvent.swift
//  FHStepStepCounterUtil
//
//  Created by Long on 27/02/2024.
//

import Foundation
import Flutter

class QueueEvent : NSObject, FlutterStreamHandler{
    func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        self.eventSink = events
        return nil
    }
    
    func onCancel(withArguments arguments: Any?) -> FlutterError? {
        self.eventSink = nil
        return nil
    }
    
    private var eventSink: FlutterEventSink? = nil
    private let eventChannel: FlutterEventChannel
    
    init(eventChannel: FlutterEventChannel) {
        self.eventChannel = eventChannel
    }
    
    func emit(event: Dictionary<String, Any>){
        self.eventSink?(event)
    }
    
    func emitError(exception: Error){
        self.eventSink?(FlutterError(code: "500", message: "error", details: exception))
    }
}


