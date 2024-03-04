import Flutter
import UIKit

public class FhStepcounterPlugin: NSObject, FlutterPlugin, FHStepCounterApi {
  static var qe: QueueEvent?
    private let hks: HealthKitService
    
    init(qe: QueueEvent) {
        FhStepcounterPlugin.qe = qe
        self.hks = HealthKitService(qe: qe)
    }
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterEventChannel(name: "io.dragn/pawday/event_channel", binaryMessenger: registrar.messenger())

        let qe = QueueEvent(eventChannel: channel)
        channel.setStreamHandler(qe)
        let plugin = FhStepcounterPlugin(qe: qe)
        FHStepCounterApiSetup.setUp(binaryMessenger: registrar.messenger(), api: plugin)
        registrar.publish(plugin)
      }
    
    
    func requestPermission() throws {
        hks.requestPermission()
    }
    
    func checkPermission() throws -> Bool {
        return hks.checkPermission()
    }
    
    func onStart(initialTodayStep: Double) throws {
        NSLog("onStart native plugin init step \(initialTodayStep)")
        var event = Dictionary<String, Any>()
        event["start"] = true
        event["onSensorChanged"] = Int(initialTodayStep)
        let now = Date()
        FHStepStepCounterUtil.setIsRecording(value: true)
        FHStepStepCounterUtil.setInitialDate(date: now.timeIntervalSince1970)
        FhStepcounterPlugin.qe?.emit(event: event)
        guard onResume(initialTodayStep: initialTodayStep) == true else{
            let rawData = FHStepStepCounterUtil.getPauseTime()
            let startTime = rawData == 0 ? Date().timeIntervalSince1970 : rawData
            NSLog("startTime \(startTime)")
            let startWith = Date(timeIntervalSince1970: Double(startTime))
            hks.excute(withStart: startWith, end: now.endOfDate()!, completion: {
                result, error in if result != nil {
                    FHStepStepCounterUtil.setTotalStepToday(step: Int(result ?? 0))
                }
            })
            return
        }
    }
    
    func getTodayStep() throws -> [String : Double]? {
        var total = Dictionary<String, Double>()
        total["step"] = Double(FHStepStepCounterUtil.getTotalStepToday())
        return total
    }
    
    func onResume(initialTodayStep: Double) -> Bool{
        let isPause = FHStepStepCounterUtil.getPauseTime()
        FHStepStepCounterUtil.setTotalStepToday(step: Int(initialTodayStep))
        NSLog("onResume \(initialTodayStep)")
        let todayStep = initialTodayStep
        let now = Date()
        if(isPause != 0){
            let date = Date(timeIntervalSince1970: isPause)
            hks.excute(withStart: date, end: now.endOfDate()!, completion: {
                result, error in if result != nil{
                    FHStepStepCounterUtil.setTotalStepToday(step:  Int(todayStep + (result ?? 0)))
                }
            })
            return true
        }
        return false
    }
    
    func getRecords() throws {
        throw NSException(
            name: NSExceptionName(rawValue: "Not implemented!"),
            reason: "A concrete subclass did not provide its own implementation of getRecords()",
            userInfo: nil
        ) as! Error
    }
    
    
    func stop() throws {
        var event = Dictionary<String, Any>()
        event["stop"] = true
        FHStepStepCounterUtil.clearData()
        FhStepcounterPlugin.qe?.emit(event: event)
        
        hks.stop()
    }
    
    func pause() throws {
        var event = Dictionary<String, Any>()
        let currentStep = FHStepStepCounterUtil.getTotalStepToday()
        FHStepStepCounterUtil.setPauseTime()
        event["onPauseStep"] = currentStep
        FhStepcounterPlugin.qe?.emit(event: event)
        hks.stop()
    }
    
    func clearData() throws {
        FHStepStepCounterUtil.clearData()
    }
    
    func logout() throws {
        NSLog("logout native plugin")
        print()
    }
    
    func isRecording() throws -> Bool {
        return FHStepStepCounterUtil.getIsRecording()
    }
    
    func getPauseSteps() throws -> Int64 {
        NSLog("getPauseSteps native plugin")
        let stepDictionary = FHStepStepCounterUtil.getPauseTime()
        return Int64(stepDictionary)
        
    }
}
