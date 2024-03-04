import 'package:fh_stepcounter/fh_stepcounter.dart';
import 'package:fh_stepcounter/src/fh_stepcounter_implement.dart';
import 'package:fh_stepcounter/src/fh_stepcounter_platform_interface.dart';
import 'package:flutter/foundation.dart';

class FHStepCounterController extends ValueNotifier<FHStepValue> {
  final FHStepCounterPlatform _platform = FHStepCounterImplement();

  FHStepCounterController() : super(FHStepValue());

  Future<bool> checkPermission() async {
    return await _platform.checkPermission();
  }

  Future<void> requestPermission() async {
    return await _platform.requestPermission();
  }

  Future<bool> isRecording() async {
    return await _platform.isRecording();
  }

  Future<StepToday?> getTodayStep() async {
    return await _platform.getTodayStep();
  }

  Stream<FHStepValue> get stream => _platform.stream();

  Future<void> onStart({required double initialTodayStep}) async {
    return await _platform.onStart(initialToday: initialTodayStep);
  }

  Future<void> onStop() async {
    return await _platform.onStop();
  }

  Future<void> onPause() async {
    return await _platform.onPause();
  }

  Future<int> getPauseStep() async {
    return await _platform.getPauseSteps();
  }

  Future<void> cleanRecords() async {
    return await _platform.clearData();
  }
}
