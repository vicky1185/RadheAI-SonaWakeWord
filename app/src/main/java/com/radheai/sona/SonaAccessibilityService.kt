package com.radheai.sona

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class SonaAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Observe active window state when Sona dispatches UI commands
    }

    override fun onInterrupt() {}

    fun executeVoiceAction(intentType: String, target: String): Boolean {
        return when (intentType) {
            "NAVIGATION_HOME" -> performGlobalAction(GLOBAL_ACTION_HOME)
            "NAVIGATION_BACK" -> performGlobalAction(GLOBAL_ACTION_BACK)
            "SCROLL_SCREEN" -> {
                val root = rootInActiveWindow ?: return false
                val action = if (target == "UP") {
                    AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                } else {
                    AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                }
                root.performAction(action)
            }
            else -> false
        }
    }
}
