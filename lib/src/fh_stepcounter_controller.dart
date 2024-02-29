import 'package:fh_stepcounter/fh_stepcounter.dart';
import 'package:fh_stepcounter/src/fh_stepcounter_implement.dart';
import 'package:flutter/foundation.dart';

class FHStepCounterController extends ValueNotifier<FHStepEvent> {
  final FHStepCounterImplement _api = FHStepCounterImplement();

  FHStepCounterController() : super(FHStepEvent());

  Future<bool> checkPermission() async {
    return await _api.checkPermission();
  }

  Future<void> requestPermission() async {
    return await _api.requestPermission();
  }

  Future<bool> isRecording() async {
    return await _api.isRecording();
  }

  Future<StepToday?> getTodayStep() async {
    return await _api.getTodayStep();
  }

  Stream<FHStepEvent> get stream => _api.stream();

  Future<void> onStart({required double initialTodayStep}) async {
    return await _api.onStart(initialToday: initialTodayStep);
  }

  Future<void> onStop() async {
    return await _api.onStop();
  }

  Future<void> onPause() async {
    return await _api.onPause();
  }

  Future<int> getPauseStep() async {
    return await _api.getPauseSteps();
  }

  Future<void> cleanRecords() async {
    return await _api.clearData();
  }
}
