package com.example.kyra

import android.content.Intent
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

class AutomationAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AutomationAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d("AutoService", "Automation Accessibility Service Connected")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun getScreenTree(): String {
        val rootNode: AccessibilityNodeInfo? = rootInActiveWindow
        if (rootNode == null) return "{\"error\": \"Root node is null. Ensure an app is open.\"}"

        val treeBuilder = StringBuilder()
        traverseNode(rootNode, treeBuilder, 0)
        return treeBuilder.toString()
    }

    fun clickNodeByText(targetText: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByText(targetText)
        
        for (node in nodes) {
            var target: AccessibilityNodeInfo? = node
            
            // Traverse up to find a clickable node if the text node itself isn't clickable
            while (target != null && !target.isClickable) {
                target = target.parent
            }

            if (target != null) {
                // 1. Try standard Android semantic click
                val standardClick = target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (standardClick) return true
                
                // 2. Fallback for Flutter/Custom views: Dispatch physical tap
                return dispatchPhysicalClick(target)
            }
        }
        return false
    }

    private fun dispatchPhysicalClick(node: AccessibilityNodeInfo): Boolean {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        
        val x = rect.centerX().toFloat()
        val y = rect.centerY().toFloat()
        
        if (x <= 0f || y <= 0f) return false

        val path = Path()
        path.moveTo(x, y)

        val gestureBuilder = GestureDescription.Builder()
        // Tap starting at 10ms lasting for 50ms
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 10L, 50L)) 
        
        return dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun traverseNode(node: AccessibilityNodeInfo, builder: StringBuilder, depth: Int) {
        val indent = " ".repeat(depth * 2)
        val viewId = node.viewIdResourceName ?: ""
        val text = node.text ?: node.contentDescription ?: ""
        val isClickable = node.isClickable

        if (viewId.isNotEmpty() || text.isNotEmpty()) {
            builder.append("$indent<node id=\"$viewId\" text=\"$text\" clickable=\"$isClickable\" />\n")
        }

        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            if (childNode != null) {
                traverseNode(childNode, builder, depth + 1)
                childNode.recycle()
            }
        }
    }
}