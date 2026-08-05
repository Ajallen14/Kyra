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
            val service = AutomationAccessibilityService.instance

            if (service == null) {
                result.error("UNAVAILABLE", "Accessibility Service is not running.", null)
                return@setMethodCallHandler
            }

            // Existing read function
            if (call.method == "getScreenTree") {
                val tree = service.getScreenTree()
                result.success(tree)
            } 
            // NEW: Click function
            else if (call.method == "clickNode") {
                val targetText = call.argument<String>("text")
                if (targetText != null) {
                    val success = service.clickNodeByText(targetText)
                    result.success(success)
                } else {
                    result.error("INVALID_ARGUMENT", "Target text is required", null)
                }
            } else {
                result.notImplemented()
            }
        }
    }
}