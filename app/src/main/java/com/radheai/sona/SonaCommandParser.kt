package com.radheai.sona

data class SonaParsedCommand(
    val intent: String,
    val target: String,
    val rawUtterance: String
)

object SonaCommandParser {

    fun parse(utterance: String): SonaParsedCommand {
        val text = utterance.lowercase().trim()

        return when {
            text.contains("torch") || text.contains("flashlight") || text.contains("टॉर्च") -> {
                val turnOff = text.contains("off") || text.contains("बंद")
                SonaParsedCommand(
                    intent = "TOGGLE_FLASHLIGHT",
                    target = if (turnOff) "OFF" else "ON",
                    rawUtterance = utterance
                )
            }
            text.contains("open") || text.contains("खोलो") -> {
                val targetApp = when {
                    text.contains("whatsapp") || text.contains("व्हाट्सएप") -> "com.whatsapp"
                    text.contains("youtube") || text.contains("यूट्यूब") -> "com.google.android.youtube"
                    text.contains("camera") || text.contains("कैमरा") -> "com.android.camera2"
                    else -> "com.android.settings"
                }
                SonaParsedCommand(
                    intent = "OPEN_APP",
                    target = targetApp,
                    rawUtterance = utterance
                )
            }
            text.contains("scroll") || text.contains("स्क्रॉल") || text.contains("नीचे") -> {
                val direction = if (text.contains("up") || text.contains("ऊपर")) "UP" else "DOWN"
                SonaParsedCommand(
                    intent = "SCROLL_SCREEN",
                    target = direction,
                    rawUtterance = utterance
                )
            }
            else -> SonaParsedCommand(
                intent = "WAKE_DETECTED",
                target = "AWAITING_FOLLOWUP",
                rawUtterance = utterance
            )
        }
    }
}
