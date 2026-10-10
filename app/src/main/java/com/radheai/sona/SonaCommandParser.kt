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
            text.contains("wifi") || text.contains("वाईफाई") ->
                cmd("OPEN_WIFI_SETTINGS")
            text.contains("bluetooth") || text.contains("ब्लूटूथ") ->
                cmd("OPEN_BLUETOOTH_SETTINGS")
            text.contains("sound settings") ->
                cmd("OPEN_SOUND_SETTINGS")
            text.contains("display settings") ->
                cmd("OPEN_DISPLAY_SETTINGS")
            text.contains("settings") || text.contains("सेटिंग") ->
                cmd("OPEN_SETTINGS")
            text.contains("notification") || text.contains("नोटिफिकेशन") ->
                cmd("OPEN_NOTIFICATIONS")
            text.contains("quick settings") ->
                cmd("OPEN_QUICK_SETTINGS")
            text.contains("go home") || text == "home" || text.contains("होम") ->
                cmd("NAVIGATION_HOME")
            text.contains("go back") || text == "back" || text.contains("वापस") ->
                cmd("NAVIGATION_BACK")
            text.contains("recent apps") || text.contains("recents") ->
                cmd("NAVIGATION_RECENTS")
            text.contains("scroll") || text.contains("स्क्रॉल") ||
                text.contains("नीचे") || text.contains("ऊपर") ->
                cmd("SCROLL_SCREEN",
                    if (text.contains("up") || text.contains("ऊपर")) "UP" else "DOWN")
            text.contains("open") || text.contains("खोल") ->
                cmd("OPEN_APP", when {
                    text.contains("whatsapp") || text.contains("व्हाट्सएप") -> "whatsapp"
                    text.contains("youtube") || text.contains("यूट्यूब") -> "youtube"
                    text.contains("camera") || text.contains("कैमरा") -> "camera"
                    text.contains("calculator") || text.contains("कैलकुलेटर") -> "calculator"
                    text.contains("chrome") -> "chrome"
                    text.contains("gmail") -> "gmail"
                    text.contains("maps") || text.contains("मैप") -> "maps"
                    else -> ""
                })
            else -> cmd("UNKNOWN")
        }
    }
}
