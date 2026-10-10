# Radhe AI — Sona Wake Word

Android Studio starter project (`com.radheai.sona`).

## Build
1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Run on an Android phone.
4. Grant microphone permission.
5. Tap "Start Sona".
6. Say "Sona" or "सोना".

## Important
This starter detects the wake word using Android SpeechRecognizer. It is not yet an offline wake-word engine or a full local LLM. Android may restrict microphone/background operation depending on device settings.

Next modules can add a true offline wake-word engine, local LLM, command parser, and Accessibility Service.
