// ignore_for_file: public_member_api_docs, sort_constructors_first
import 'package:fh_stepcounter/src/messages.g.dart';
import 'package:flutter/foundation.dart';

@immutable
class FHStepValue {
  final bool isRecording;
  final StepRecords? stepRecords;
  final StepToday? stepToday;
  final int currentPauseStep;
  FHStepValue(
      {this.stepToday,
      this.stepRecords,
      this.currentPauseStep = 0,
      this.isRecording = false});

  FHStepValue copyWith({
    bool? isSubscribed,
    bool? isClear,
    bool? isRecording,
    StepRecords? stepRecords,
    StepToday? stepToday,
    int? currentPauseStep = 0,
  }) {
    return FHStepValue(
      isRecording: isRecording ?? this.isRecording,
      stepToday: stepToday ?? this.stepToday,
      stepRecords: stepRecords ?? this.stepRecords,
      currentPauseStep: currentPauseStep ?? this.currentPauseStep,
    );
  }

  @override
  String toString() {
    return 'PawdayValue(isRecording: $isRecording, stepRecords: $stepRecords, stepToday: $stepToday, currentPauseStep: $currentPauseStep)';
  }
}

class StepRecords {
  final int? lastUpdated;
  final List<RecordItem> recorded;

  StepRecords({this.lastUpdated, this.recorded = const []});
}

class RecordItem {
  final double? time;
  final double? value;

  RecordItem({this.time, this.value});
}

class ServiceResponse {
  final String? type;
  final bool? isRunning;

  ServiceResponse({this.isRunning, this.type});
}
