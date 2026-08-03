package com.example.kyra

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    
    //It must match exactly on Flutter and Kotlin.
    private val CHANNEL = "com.example.kyra/automation"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            
            // Flutter is asking for the screen tree
            if (call.method == "getScreenTree") {
                val service = AutomationAccessibilityService.instance
                
                if (service != null) {
                    // If the service is running, get the tree and send it back to Flutter
                    val tree = service.getScreenTree()
                    result.success(tree)
                } else {
                    // If the service is null, the user hasn't enabled it in Android Settings
                    result.error("UNAVAILABLE", "Accessibility Service is not running. Please enable it in Settings.", null)
                }
            } else {
                result.notImplemented()
            }
        }
    }
}