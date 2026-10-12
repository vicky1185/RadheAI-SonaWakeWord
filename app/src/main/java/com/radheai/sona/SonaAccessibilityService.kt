package com.radheai.sona

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class SonaAccessibilityService : AccessibilityService() {

    companion object {
        var instance: SonaAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    private fun openSystemScreen(action: String): Boolean {
        val intentAction = when (action) {
            "OPEN_SETTINGS" -> Settings.ACTION_SETTINGS
            "OPEN_WIFI_SETTINGS" -> Settings.ACTION_WIFI_SETTINGS
            "OPEN_BLUETOOTH_SETTINGS" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "OPEN_SOUND_SETTINGS" -> Settings.ACTION_SOUND_SETTINGS
            "OPEN_DISPLAY_SETTINGS" -> Settings.ACTION_DISPLAY_SETTINGS
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
        if (name.isBlank()) return false

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

        val knownPackages = mapOf(
            "youtube" to "com.google.android.youtube",
            "whatsapp" to "com.whatsapp",
            "instagram" to "com.instagram.android",
            "facebook" to "com.facebook.katana",
            "chrome" to "com.android.chrome",
            "gmail" to "com.google.android.gm",
            "maps" to "com.google.android.apps.maps",
            "google maps" to "com.google.android.apps.maps",
            "settings" to "com.android.settings"
        )

        return try {
            val packageName = knownPackages[name]

            val launchIntent = if (packageName != null) {
                packageManager.getLaunchIntentForPackage(packageName)
            } else {
                val installed = packageManager.getInstalledApplications(0)
                val normalizedName = name.filter { it.isLetterOrDigit() }

                val exact = installed.firstOrNull {
                    it.loadLabel(packageManager).toString()
                        .lowercase()
                        .filter { ch -> ch.isLetterOrDigit() } == normalizedName
                }

                val matches = if (exact != null) {
                    exact
                } else {
                    val prefixMatches = installed.filter {
                        val label = it.loadLabel(packageManager).toString()
                            .lowercase()
                            .filter { ch -> ch.isLetterOrDigit() }
                        label.startsWith(normalizedName)
                    }
                    prefixMatches.singleOrNull()
                }

                matches?.let {
                    packageManager.getLaunchIntentForPackage(it.packageName)
                }
            } ?: return false

            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun executeVoiceAction(intentType: String, target: String): Boolean {
        return when (intentType.trim().uppercase()) {
            "NAVIGATION_HOME" -> performGlobalAction(GLOBAL_ACTION_HOME)
            "NAVIGATION_BACK" -> performGlobalAction(GLOBAL_ACTION_BACK)
            "NAVIGATION_RECENTS" -> performGlobalAction(GLOBAL_ACTION_RECENTS)
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

            "OPEN_APP" -> openApp(target)

            "SCROLL_SCREEN" -> {
                val root = rootInActiveWindow ?: return false
                val action = if (target.trim().equals("UP", ignoreCase = true)) {
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
