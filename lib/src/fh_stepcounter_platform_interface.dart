// ignore_for_file: public_member_api_docs, sort_constructors_first
import 'package:fh_stepcounter/fh_stepcounter.dart';
import 'package:fh_stepcounter/src/messages.g.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

abstract class FHStepCounterPlatform extends PlatformInterface {
  /// Constructs a PawdayStepPlatform.
  FHStepCounterPlatform() : super(token: _token);

  static final Object _token = Object();

  static FHStepCounterPlatform _instance = _PlaceholderImplementation();

  /// The default instance of [FHStepCounterPlatform] to use.
  ///
  /// Defaults to [MethodChannelPawdayStep].
  static FHStepCounterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FHStepCounterPlatform] when
  /// they register themselves.
  static set instance(FHStepCounterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> requestPermission() {
    throw UnimplementedError('requestPermission() has not been implemented.');
  }

  Future<bool> checkPermission() {
    throw UnimplementedError('checkPermission() has not been implemented.');
  }

  Future<StepToday?> getTodayStep() {
    throw UnimplementedError('getTodayStep() has not been implemented.');
  }

  Future<void> getRecords() {
    throw UnimplementedError('getRecords() has not been implemented.');
  }

  Future<void> onStop() {
    throw UnimplementedError('onStop() has not been implemented.');
  }

  Future<void> clearData() {
    throw UnimplementedError('clearData() has not been implemented.');
  }

  Future<void> logout() {
    throw UnimplementedError('logout() has not been implemented.');
  }

  Future<bool> isRecording() {
    throw UnimplementedError('isRecording() has not been implemented.');
  }

  Future<void> onStart({double initialToday = 0}) {
    throw UnimplementedError('onStart() has not been implemented.');
  }

  Future<void> onPause() {
    throw UnimplementedError('onPause() has not been implemented.');
  }

  Future<int> getPauseSteps() {
    throw UnimplementedError('getPauseSteps() has not been implemented.');
  }

  Stream<FHStepValue> stream() {
    throw UnimplementedError('onListen() has not been implemented.');
  }
}

class _PlaceholderImplementation extends FHStepCounterPlatform {}
