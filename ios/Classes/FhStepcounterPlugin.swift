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
        registrar.addApplicationDelegate(plugin)
      }
    
    
    func requestPermission() throws {
        hks.requestPermission()
    }
    
    func checkPermission() throws -> Bool {
        return hks.checkPermission()
    }
    
    func onStart(initialTodayStep: Double) throws {
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
            let startWith = Date(timeIntervalSince1970: Double(startTime))
            hks.excute(withStart: startWith, end: now.endOfDate()!, completion: {
                result, error in if result != nil {
                    FHStepStepCounterUtil.setTotalStepToday(step: Int( initialTodayStep + (result ?? 0)))
                }
            })
            return
        }
    }
    
    func getTodayStep() throws -> StepToday {
        return StepToday(lastUpdated: nil , step:  Int64(FHStepStepCounterUtil.getTotalStepToday()))
    }
    
    func onResume(initialTodayStep: Double) -> Bool{
        let isPause = FHStepStepCounterUtil.getPauseTime()
        FHStepStepCounterUtil.setTotalStepToday(step: Int(initialTodayStep))
        let todayStep = initialTodayStep
        let now = Date()
        if(isPause != 0){
            let date = Date(timeIntervalSince1970: isPause)
            hks.excute(withStart: date, end: now.endOfDate()!, completion: {
                result, error in if result != nil{
                    FHStepStepCounterUtil.setTotalStepToday(step: Int(todayStep + (result ?? 0)))
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
        event["isRecording"] = false
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
        fatalError("Oops")
    }
    
    func isRecording() throws -> Bool {
        return FHStepStepCounterUtil.getIsRecording()
    }
    
    func getPauseSteps() throws -> Int64 {
        let stepDictionary = FHStepStepCounterUtil.getPauseTime()
        return Int64(stepDictionary)
        
    }
    
    public func applicationWillTerminate(_ application: UIApplication) {
        do {
            try stop()
            debugPrint("applicationWillTerminate")
        }catch{
            NSLog("applicationWillTerminate: \(error)")
        }
    }
    
    public func applicationDidBecomeActive(_ application: UIApplication) {
        NSLog("applicationDidBecomeActive")
    }

    public func applicationWillResignActive(_ application: UIApplication) {
        NSLog("applicationWillResignActive")
    }

    public func applicationDidEnterBackground(_ application: UIApplication) {
        NSLog("applicationDidEnterBackground")
    }

    public func applicationWillEnterForeground(_ application: UIApplication) {
        NSLog("applicationWillEnterForeground")
    }
}
