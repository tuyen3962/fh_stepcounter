//
//  FHStepStepCounterUtil.swift
//  FHStepStepCounterUtil
//
//  Created by Long on 28/02/2024.
//

import Foundation

struct FHStepStepCounterUtil{
    static let TOTAL_STEP_TODAY = "total_step_today"
    static let INITIAL_TIME = "initial_time"
    static let KEY_IS_RECORDING = "key_is_recording"
    static let KEY_HK_PAUSE_TIME = "hk_pause_time"

    
    static func setTotalStepToday(step: Int){
        DispatchQueue.main.async {
            UserDefaults.standard.set(step, forKey: TOTAL_STEP_TODAY)
            var event = Dictionary<String,Any>()
            event["onSensorChanged"] = step
            FhStepcounterPlugin.qe?.emit(event: event)
        }
    }
    
    static func getTotalStepToday() -> Int{
       return UserDefaults.standard.integer(forKey: TOTAL_STEP_TODAY)
    }
    
    static func setInitialDate(date: Double?){
        DispatchQueue.main.async {
            UserDefaults.standard.set(date, forKey: INITIAL_TIME)
        }
        
    }

    static func getInitialDate() -> Double?{
        return UserDefaults.standard.object(forKey: INITIAL_TIME) as? Double
    }
    
    static func clearData(){
        DispatchQueue.main.async {
            UserDefaults.standard.set(0, forKey: INITIAL_TIME)
            UserDefaults.standard.set(0, forKey: TOTAL_STEP_TODAY)
            UserDefaults.standard.set(0, forKey: KEY_HK_PAUSE_TIME)
    }
    }
    
    static func setIsRecording(value: Bool){
        var event = Dictionary<String,Any>()
        event["isRecording"] = value
        FhStepcounterPlugin.qe?.emit(event: event)
        NSLog("isRecording --> \(event.description)")
        UserDefaults.standard.set(value, forKey: KEY_IS_RECORDING)
    }
    
    static func getIsRecording() -> Bool{
       return UserDefaults.standard.bool(forKey: KEY_IS_RECORDING)
    }
    
    static func getPauseTime() -> Double{
        return UserDefaults.standard.double(forKey: KEY_HK_PAUSE_TIME)
    }

    static func setPauseTime(){
        DispatchQueue.main.async {
            let time = Date().timeIntervalSince1970
            UserDefaults.standard.set(time, forKey: KEY_HK_PAUSE_TIME)
        }
    }
}
