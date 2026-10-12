package com.radheai.sona

data class SonaParsedCommand(
    val intent: String,
    val target: String,
    val rawUtterance: String
)

object SonaCommandParser {

    fun parse(utterance: String): SonaParsedCommand {
        val text = utterance.lowercase().trim()

        fun cmd(type: String, target: String = "") =
            SonaParsedCommand(type, target, utterance)

        return when {
            text.contains("wifi") || text.contains("wi-fi") ||
                text.contains("वाईफाई") ->
                cmd("OPEN_WIFI_SETTINGS")

            text.contains("bluetooth") || text.contains("ब्लूटूथ") ->
                cmd("OPEN_BLUETOOTH_SETTINGS")

            text.contains("sound settings") ||
                text.contains("आवाज की सेटिंग") ->
                cmd("OPEN_SOUND_SETTINGS")

            text.contains("display settings") ||
                text.contains("डिस्प्ले सेटिंग") ->
                cmd("OPEN_DISPLAY_SETTINGS")

            text.contains("quick settings") ||
                text.contains("क्विक सेटिंग") ->
                cmd("OPEN_QUICK_SETTINGS")

            text.contains("notification") ||
                text.contains("नोटिफिकेशन") ->
                cmd("OPEN_NOTIFICATIONS")

            text.contains("settings") || text.contains("सेटिंग") ->
                cmd("OPEN_SETTINGS")

            text == "home" || text.contains("go home") ||
                text == "मैक्स होम" ->
                cmd("NAVIGATION_HOME")

            text == "back" || text.contains("go back") ||
                text == "वापस" ->
                cmd("NAVIGATION_BACK")

            text.contains("recent apps") ||
                text.contains("recents") ||
                text.contains("हाल के ऐप") ->
                cmd("NAVIGATION_RECENTS")

            text.contains("scroll") || text.contains("स्क्रॉल") ||
                text == "नीचे" || text == "ऊपर" ->
                cmd(
                    "SCROLL_SCREEN",
                    if (text.contains("up") || text.contains("ऊपर")) "UP"
                    else "DOWN"
                )

            text.contains("flashlight") || text.contains("torch") ||
                text.contains("टॉर्च") ->
                cmd("TOGGLE_FLASHLIGHT")

            else -> {
                val launchVerbs = listOf(
                    "open", "launch", "start", " kholo", "kholo",
                    "khol", "chalao", "chalu karo", "please",
                    "खोलो", "खोलना", "खोल", "खोल दो", "खोलिए",
                    "चलाओ", "चालू करो", "शुरू करो", "चालू", "खोलकर"
                )

                val asksToLaunch = launchVerbs.any { text.contains(it.trim()) }

                val cleaned = text
                    .replace("please", "")
                    .replace("open", "")
                    .replace("launch", "")
                    .replace("start", "")
                    .replace("chalu karo", "")
                    .replace("chalao", "")
                    .replace("kholo", "")
                    .replace("khol", "")
                    .replace("खोलकर", "")
                    .replace("खोलिए", "")
                    .replace("खोलना", "")
                    .replace("खोल दो", "")
                    .replace("खोलो", "")
                    .replace("खोल", "")
                    .replace("चालू करो", "")
                    .replace("शुरू करो", "")
                    .replace("चलाओ", "")
                    .replace("चालू", "")
                    .replace("मैक्स", "")
                    .replace("max", "")
                    .trim(' ', ',', '.', '!', '?')

                val target = when {
                    cleaned.contains("व्हाट्सएप") ||
                        cleaned.contains("व्हाट्स ऐप") -> "whatsapp"
                    cleaned.contains("यूट्यूब") -> "youtube"
                    cleaned.contains("इंस्टाग्राम") -> "instagram"
                    cleaned.contains("फेसबुक") -> "facebook"
                    cleaned.contains("कैमरा") -> "camera"
                    cleaned.contains("कैलकुलेटर") -> "calculator"
                    cleaned.contains("मैप") -> "maps"
                    else -> cleaned
                }

                if (target.isNotBlank() && (asksToLaunch || target != text)) {
                    cmd("OPEN_APP", target)
                } else {
                    cmd("UNKNOWN")
                }
            }
        }
    }
}
