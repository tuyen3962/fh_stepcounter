import 'dart:async';

import 'package:fh_stepcounter/fh_stepcounter.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool isGranted = false;
  int step = 0;
  bool isRecording = false;
  int pauseStep = 0;
  StepRecords records = StepRecords();
  final FHStepCounterController controller = FHStepCounterController();

  @override
  void initState() {
    controller.stream.asBroadcastStream().listen((event) {
      if (event.stepToday != null) {
        setState(() {
          step = event.stepToday?.step?.toInt() ?? 0;
        });
      }
      if (event.isRecording != isRecording) {
        setState(() {
          isRecording = !isRecording;
        });
      }
      if (event.stepRecords != null) {
        setState(() {
          records = event.stepRecords!;
        });
      }

      if (event.currentPauseStep != 0) {
        setState(() {
          pauseStep = event.currentPauseStep;
        });
      }
    });
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    final bool _isGranted = await controller.checkPermission();
    setState(() {
      isGranted = _isGranted;
    });
    if (!_isGranted) {
      await controller.requestPermission();
    }
    if (!mounted) return;
  }

  Future toggle() async {
    if (isRecording) {
      await controller.onStop();
    } else {
      final bool _isGranted = await controller.checkPermission();
      if (!_isGranted) return;
      await controller.onStart(initialTodayStep: 0);
    }

    setState(() {});
  }

  Future pauseToggle() async {
    if (isRecording) {
      await controller.onPause();
    } else {
      final bool _isGranted = await controller.checkPermission();
      if (!_isGranted) return;
      await controller.onStart(initialTodayStep: pauseStep.toDouble());
    }

    setState(() {});
  }

  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        floatingActionButton: Column(
          mainAxisAlignment: MainAxisAlignment.end,
          children: [
            FloatingActionButton(
                onPressed: pauseToggle,
                child: isRecording
                    ? const Icon(Icons.pause)
                    : const Icon(Icons.play_arrow)),
            const SizedBox(height: 16),
            FloatingActionButton(
                onPressed: toggle,
                child: isRecording
                    ? const Icon(Icons.stop)
                    : const Icon(Icons.play_arrow)),
          ],
        ),
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text('Granted permission: $isGranted'),
              Text("Step today: $step"),
              Text("Is recording: $isRecording"),
              Text("Pause steps: $pauseStep")
            ],
          ),
        ),
      ),
    );
  }
}
