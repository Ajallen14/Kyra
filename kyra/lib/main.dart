import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// Entry point of the Flutter application.
void main() {
  runApp(const MyApp());
}

/// Root widget of the application.
class MyApp extends StatelessWidget {
  const MyApp({super.key});

  /// MethodChannel used to communicate with Android native code.
  ///
  /// The channel name MUST match the channel name defined in
  /// MainActivity.kt.
  static const MethodChannel _channel =
      MethodChannel('com.example.kyra/automation');

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Kyra Test',
      home: const HomePage(),
    );
  }
}

/// Home screen used for testing the Accessibility Service.
class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {

  /// MethodChannel used to call Android native methods.
  static const MethodChannel _channel =
      MethodChannel('com.example.kyra/automation');

  /// Stores the latest response received from Android.
  String _output = "Press the button to read the current UI tree.";

  /// Calls the Android Accessibility Service through the MethodChannel.
  ///
  /// Android returns the UI tree as a String.
  Future<void> _getScreenTree() async {
    try {

      // Call the native Android method named "getScreenTree".
      final tree =
          await _channel.invokeMethod<String>('getScreenTree');

      // Update the UI with the returned screen tree.
      setState(() {
        _output = tree ?? "No data received.";
      });

    } on PlatformException catch (e) {

      // Display any native Android error.
      setState(() {
        _output = "Error:\n${e.message}";
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(

      // App title shown at the top.
      appBar: AppBar(
        title: const Text("Kyra UI Tree Test"),
      ),

      body: Padding(
        padding: const EdgeInsets.all(16),

        child: Column(
          children: [

            /// Temporary button used only for testing.
            ///
            /// When pressed:
            /// Flutter → MethodChannel → Android →
            /// AccessibilityService → UI Tree → Flutter
            ElevatedButton(
              onPressed: _getScreenTree,
              child: const Text("Get Screen Tree"),
            ),

            const SizedBox(height: 20),

            /// Displays the UI tree returned from Android.
            Expanded(
              child: SingleChildScrollView(
                child: SelectableText(_output),
              ),
            ),
          ],
        ),
      ),
    );
  }
}