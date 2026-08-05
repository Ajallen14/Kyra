package com.example.kyra

import android.content.Intent
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.util.Log

// To create an accessibility service, you must extend the AccessibilityService class
class AutomationAccessibilityService : AccessibilityService() {

    // This companion object allows MainActivity to find the running service
    companion object {
        var instance: AutomationAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this // Save the instance when the OS connects the service
        Log.d("AutoService", "Automation Accessibility Service Connected")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null // Clear it if the user turns off the service
        return super.onUnbind(intent)
    }

    // Called when the system detects an event that matches your service's configuration
    // This event acts as the entry point to the accessibility tree
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // TO DO: To trigger the read manually via Flutter later.
    }

    // Called when the system interrupts your service's feedback
    override fun onInterrupt() {
        Log.d("AutoService", "Service Interrupted")
    }

    // This is the custom function we will call from Flutter to get the screen text
    fun getScreenTree(): String {
        // Using the rootInActiveWindow property of your service to get the root AccessibilityNodeInfo of the view hierarchy
        val rootNode: AccessibilityNodeInfo? = rootInActiveWindow
        
        if (rootNode == null) {
            return "{\"error\": \"Root node is null. Ensure an app is open.\"}"
        }

        val treeBuilder = StringBuilder()
        traverseNode(rootNode, treeBuilder, 0)
        return treeBuilder.toString()
    }

    // To find a node by its text and click it
    fun clickNodeByText(targetText: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        // Find all nodes that contain the target text
        val nodes = rootNode.findAccessibilityNodeInfosByText(targetText)
        
        for (node in nodes) {
            // If the node itself is clickable, click it
            if (node.isClickable) {
                Log.d("AutoService", "Clicking exact node: $targetText")
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            
            // In many apps, the text isn't clickable, but its parent container is.
            // We traverse up the tree to find the nearest clickable parent.
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) {
                    Log.d("AutoService", "Clicking parent of: $targetText")
                    return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                parent = parent.parent
            }
        }
        return false
    }

    // Recursive function to read every UI element on the screen
    private fun traverseNode(node: AccessibilityNodeInfo, builder: StringBuilder, depth: Int) {
        val indent = " ".repeat(depth * 2)
        
        // Extracting standard properties from the AccessibilityNodeInfo 
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