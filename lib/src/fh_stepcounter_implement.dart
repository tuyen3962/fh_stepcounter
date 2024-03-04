import 'dart:developer';

import 'package:fh_stepcounter/fh_stepcounter.dart';
import 'package:fh_stepcounter/src/messages.g.dart';
import 'package:flutter/services.dart';

import 'fh_stepcounter_platform_interface.dart';

const EVENT_CHANNEL = "io.dragn/pawday/event_channel";

class FHStepCounterImplement implements FHStepCounterPlatform {
  final FHStepCounterApi _api = FHStepCounterApi();

  final FHStepValue _event = FHStepValue();
  EventChannel _eventChannel() {
    return const EventChannel(EVENT_CHANNEL);
  }

  @override
  Future<bool> checkPermission() async {
    return await _api.checkPermission();
  }

  @override
  Future<void> onStart({double initialToday = 0}) async {
    await _api.onStart(initialToday);
  }

  @override
  Future<void> clearData() async {
    await _api.clearData();
  }

  @override
  Future<void> getRecords() async {
    await _api.getRecords();
  }

  @override
  Future<StepToday?> getTodayStep() async {
    final res = await _api.getTodayStep() as StepToday?;
    return res;
  }

  @override
  Future<bool> isRecording() async {
    final result = await _api.isRecording();
    return result;
  }

  @override
  Future<void> logout() async {
    await _api.logout();
  }

  @override
  Future<void> requestPermission() async {
    await _api.requestPermission();
  }

  @override
  Future<void> onStop() async {
    await _api.stop();
  }

  @override
  Future<void> onPause() async {
    await _api.pause();
  }

  @override
  Stream<FHStepValue> stream() {
    return _eventChannel().receiveBroadcastStream().map((event) {
      final Map<dynamic, dynamic> map = event as Map<dynamic, dynamic>;
      log("event channel => $map");
      if (map.containsKey("isRecording") && map["isRecording"] == true) {
        return _event.copyWith(isRecording: true);
      }
      if (map.containsKey("onSensorChanged") &&
          (map["onSensorChanged"] != null || map["onSensorChanged"] != false)) {
        return _event.copyWith(
            isRecording: true,
            stepToday: StepToday(
                step: int.tryParse(map["onSensorChanged"].toString()) ?? 0));
      }
      if (map.containsKey("start") && map["start"] == true) {
        return _event.copyWith(isRecording: map["start"]);
      }
      if (map.containsKey("stop") && map["stop"] == true) {
        return FHStepValue();
      }
      if (map.containsKey("clearData") && map["clearData"] == true) {
        return _event.copyWith(isClear: true);
      }
      if (map.containsKey("getRecords") && map["getRecords"] != null) {
        final data = map["getRecords"];

        final List<RecordItem> recordList =
            data["recorded"].map<RecordItem>((e) {
          return RecordItem(time: e["time"], value: e["value"]);
        }).toList();

        final step =
            StepRecords(lastUpdated: data["lastUpdated"], recorded: recordList);
        return _event.copyWith(stepRecords: step);
      }

      if (map.containsKey("onPauseStep") && map["onPauseStep"] != null) {
        int pauseStep = map["onPauseStep"] as int;
        return _event.copyWith(
            stepToday: StepToday(
                lastUpdated: DateTime.now().millisecondsSinceEpoch.toDouble(),
                step: pauseStep),
            currentPauseStep: pauseStep);
      }
      return _event;
    });
  }

  @override
  Future<int> getPauseSteps() async {
    return _api.getPauseSteps();
  }
}
