//
//  FHStepStepCounterUtil.swift
//  FHStepStepCounterUtil
//
//  Created by Long on 28/02/2024.
//

import Foundation

struct FHStepStepCounterUtil{
    static let KEY_HK_PAUSE = "hk_pasuse"
    static let TOTAL_STEP_TODAY = "total_step_today"
    //static let INITIAL_DATE = "initial_date"
    static let KEY_IS_RECORDING = "key_is_recording"
   
    static func getOnPauseStep() -> Dictionary<String, Int>?{
        return UserDefaults.standard.object(forKey: KEY_HK_PAUSE) as? Dictionary<String, Int>
    }
    
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
    
//    static func setInitialDate(date: Int?){
//        UserDefaults.standard.set(date, forKey: INITIAL_DATE)
//    }
//
//    static func getInitialDate() -> Int?{
//        return UserDefaults.standard.object(forKey: INITIAL_DATE) as? Int
//    }
    
    static func clearData(){
       // UserDefaults.standard.set(nil, forKey: INITIAL_DATE)
        UserDefaults.standard.set(0, forKey: TOTAL_STEP_TODAY)
        UserDefaults.standard.set(0, forKey: KEY_HK_PAUSE)
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
    
    static func setOnPauseStep(step: Int){
        var curentStep = Dictionary<String, Int>()
        curentStep["time"] = Int(Date().timeIntervalSince1970)
        curentStep["step"] = step
        UserDefaults.standard.set(curentStep, forKey: KEY_HK_PAUSE)    }
}
