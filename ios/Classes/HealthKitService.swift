//
//  HealthCheckService.swift
//  FHStepStep
//
//  Created by Long on 27/02/2024.
//

import Foundation
import HealthKit

class HealthKitService{
    private let hk = HKHealthStore()
    let qe: QueueEvent
    private let typesToRead: Set<HKSampleType> = [
        HKSampleType.quantityType(forIdentifier: .stepCount)!,
    ]
    private var observer: HKObserverQuery?
    private var statistic: HKStatisticsQuery?
    
    init(qe: QueueEvent) {
        self.qe = qe
    }
    
    func checkPermission() -> Bool{
        let stepCountType = HKQuantityType.quantityType(forIdentifier: .stepCount)!
        NSLog(hk.authorizationStatus(for: stepCountType).rawValue.description)
            if HKHealthStore.isHealthDataAvailable(){
                if(hk.authorizationStatus(for: stepCountType) == .sharingAuthorized){
                    return true
                }
            }
            return false
    }
    
    
    func excute(withStart: Date, end: Date, completion: @escaping (Double?, Error?) -> Void){
        let stepsQuantityType = HKQuantityType.quantityType(forIdentifier: .stepCount)!
        let predicate = HKQuery.predicateForSamples(
               withStart: withStart,
               end: end,
               options: .strictStartDate
           )
        observer = HKObserverQuery(sampleType: stepsQuantityType, predicate: nil){
            query, completionHandler, error in
            if let error = error{
                FhStepcounterPlugin.qe?.emitError(exception: error)
                return
            }
            self.fetchStepCount(quantityType: stepsQuantityType, predicate: predicate, completion: completion)
        }
        hk.execute(observer!)
    }
    
    private func fetchStepCount(quantityType: HKQuantityType, predicate: NSPredicate, completion: @escaping (Double?, Error?) -> Void){
        statistic = HKStatisticsQuery(
               quantityType: quantityType,
               quantitySamplePredicate: predicate,
               options: .cumulativeSum
           ) { _, result, err in
               guard let result = result, let sum = result.sumQuantity() else {
                   completion(0.0, err)
                   return
               }
               NSLog("semme \(sum.description)")
               completion(sum.doubleValue(for: HKUnit.count()), err)
           }
           hk.execute(statistic!)
    }
    
    
    func requestPermission(){
            if HKHealthStore.isHealthDataAvailable() {
                if #available(iOS 15.0, *) {
                    let stepCountType = HKQuantityType.quantityType(forIdentifier: .stepCount)!
                    hk.requestAuthorization(toShare: [stepCountType], read: [stepCountType]){_,_ in
                    }
                }
            }
        
    }
    
    func stop(){
        if(observer != nil){
            hk.stop(observer!)
        }
        if(statistic != nil){
            hk.stop(statistic!)
        }
        FHStepStepCounterUtil.clearData()
    }
}


extension Date{
    func endOfDate() -> Date?{
        let calendar = Calendar.current
        
        var components = calendar.dateComponents([.year, .month, .day, .hour, .minute, .second], from: self)
                components.hour = 23
               components.minute = 59
               components.second = 59
                
               return calendar.date(from: components)
    }
    
    func startOfDate() -> Date?{
        let calendar = Calendar.current
        
        var components = calendar.dateComponents([.year, .month, .day, .hour, .minute, .second], from: self)
                components.hour = 0
               components.minute = 0
               components.second = 0
                
               return calendar.date(from: components)
    }
}
