import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(PigeonOptions(
    dartOut: "lib/src/messages.g.dart",
    kotlinOut:
        "android/src/main/kotlin/com/drag/ss/fh_stepcounter/FlutterMessage.kt",
    swiftOut: "ios/Classes/FlutterMessage.swift"))
@HostApi()
abstract class FHStepCounterApi {
  void requestPermission();
  bool checkPermission();
  void onStart(double initialTodayStep);
  StepToday getTodayStep();
  void getRecords();
  void stop();
  void pause();
  void clearData();
  void logout();
  bool isRecording();
  int getPauseSteps();
}

class StepToday {
  final double? lastUpdated;
  final int? step;

  StepToday({this.lastUpdated, this.step});
}
