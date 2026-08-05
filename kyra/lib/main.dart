import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Kyra Test',
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  /// MethodChannel used to call Android native methods.
  static const MethodChannel _channel = MethodChannel(
    'com.example.kyra/automation',
  );

  /// Stores the latest response received from Android.
  String _output = "Press the button to read the current UI tree.";

  /// Calls the Android Accessibility Service through the MethodChannel.
  /// Android returns the UI tree as a String.
  Future<void> _getScreenTree() async {
    try {
      // Call the native Android method named "getScreenTree".
      final tree = await _channel.invokeMethod<String>('getScreenTree');

      // Update the UI with the returned screen tree.
      setState(() {
        _output = tree ?? "No data received.";
      });
    } on PlatformException catch (e) {
      setState(() {
        _output = "Error:\n${e.message}";
      });
    }
  }

  Future<void> _testAutoClick() async {
    try {
      // We are telling the native Android code to find and click the "Get Screen Tree" button
      final bool? success = await _channel.invokeMethod<bool>(
        'clickNode',
        {
          "text": "Get Screen Tree",
        }, // The exact text on the button we want to click
      );

      if (success == true) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text("Successfully clicked via native code!"),
          ),
        );
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text("Failed to find or click the node.")),
        );
      }
    } on PlatformException catch (e) {
      print("Error: ${e.message}");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Kyra UI Tree Test")),

      body: Padding(
        padding: const EdgeInsets.all(16),

        child: Column(
          children: [
            /// When pressed:
            /// Flutter → MethodChannel → Android →
            /// AccessibilityService → UI Tree → Flutter
            ElevatedButton(
              onPressed: _getScreenTree,
              child: const Text("Get Screen Tree"),
            ),

            const SizedBox(height: 20),

            ElevatedButton(
              onPressed: _testAutoClick,
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.purple[100],
              ),
              child: const Text("Test Auto-Click"),
            ),

            const SizedBox(height: 20),

            /// Displays the UI tree returned from Android.
            Expanded(
              child: SingleChildScrollView(child: SelectableText(_output)),
            ),
          ],
        ),
      ),
    );
  }
}
