
cat > app/src/main/java/com/radheai/sona/SonaAccessibilityService.kt <<'EOF'
package com.radheai.sona

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityService.GLOBAL_ACTION_HOME
import android.view.accessibility.AccessibilityService.GLOBAL_ACTION_BACK
import android.view.accessibility.AccessibilityService.GLOBAL_ACTION_RECENTS
import android.view.accessibility.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
import android.view.accessibility.AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS

class SonaAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Accessibility events are received here.
    }

    override fun onInterrupt() {}

    private fun openSystemScreen(action: String): Boolean {
        val intentAction = when (action) {
            "OPEN_SETTINGS" -> Settings.ACTION_SETTINGS
            "OPEN_WIFI_SETTINGS" -> Settings.ACTION_WIFI_SETTINGS
            "OPEN_BLUETOOTH_SETTINGS" ->
                Settings.ACTION_BLUETOOTH_SETTINGS
            "OPEN_SOUND_SETTINGS" -> Settings.ACTION_SOUND_SETTINGS
            "OPEN_DISPLAY_SETTINGS" ->
                Settings.ACTION_DISPLAY_SETTINGS
            else -> return false
        }

        return try {
            val intent = Intent(intentAction).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun openApp(target: String): Boolean {
        val name = target.trim().lowercase()

        if (name == "camera") {
            return try {
                val intent = Intent(
                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                true
            } catch (_: Exception) {
                false
            }
        }

        if (name == "calculator") {
            return try {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_CALCULATOR)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                true
            } catch (_: Exception) {
                false
            }
        }

        val packageName = when (name) {
            "youtube" -> "com.google.android.youtube"
            "whatsapp" -> "com.whatsapp"
            "chrome" -> "com.android.chrome"
            "gmail" -> "com.google.android.gm"
            "maps", "google maps" -> "com.google.android.apps.maps"
            "settings" -> "com.android.settings"
            else -> return false
        }

        return try {
            val intent = packageManager
                .getLaunchIntentForPackage(packageName)
                ?: return false

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun executeVoiceAction(
        intentType: String,
        target: String
    ): Boolean {
        return when (intentType.trim().uppercase()) {
            "NAVIGATION_HOME" ->
                performGlobalAction(GLOBAL_ACTION_HOME)

            "NAVIGATION_BACK" ->
                performGlobalAction(GLOBAL_ACTION_BACK)

            "NAVIGATION_RECENTS" ->
                performGlobalAction(GLOBAL_ACTION_RECENTS)

            "OPEN_NOTIFICATIONS" ->
                performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)

            "OPEN_QUICK_SETTINGS" ->
                performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)

            "OPEN_SETTINGS",
            "OPEN_WIFI_SETTINGS",
            "OPEN_BLUETOOTH_SETTINGS",
            "OPEN_SOUND_SETTINGS",
            "OPEN_DISPLAY_SETTINGS" ->
                openSystemScreen(intentType.trim().uppercase())

            "OPEN_APP" ->
                openApp(target)

            "SCROLL_SCREEN" -> {
                val root = rootInActiveWindow ?: return false
                val action = if (target.trim().equals(
                        "UP", ignoreCase = true
                    )
                ) {
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
