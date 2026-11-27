# ElderAssist (VoiceNav)

An Android accessibility assistant built with **Kotlin** and **Jetpack Compose** to help older adults navigate smartphone settings, system actions, and contacts using plain-language voice or text commands.

---

## Core Architecture

1. **Input & Conversation (`feature/voice/`, `feature/home/`)**
   - `SpeechInputProvider` interface with `AndroidSpeechRecognizerProvider` as the default implementation (swappable for future dictation models / APIs).
   - `TextToSpeechSpeaker` for optional spoken feedback.
   - Elder-friendly Jetpack Compose UI with large touch targets (`148dp` Speak button), high-contrast feedback cards, manual text input fallback, and an always-visible Stop/Cancel control.
2. **Intelligence & Planning (`intelligence/`)**
   - Deterministic-first `CommandParser` + `DefaultIntentRouter` handles cancellation, 7 Settings destinations, Home/Back/Recents, Contacts workflows, ambiguity clarification, and safety blocks locally without network dependency.
   - `GeminiIntentInterpreter` (via `HttpGeminiClient` + official `generateContent` structured JSON schema) handles unmatched paraphrases with strict DTO validation.
   - `GeneralNavigationAgent` provides an opt-in, experimental bounded loop (max 5 steps, 1 action/cycle, confidence `>= 0.85`).
3. **Validation, Execution & Accessibility (`execution/`, `accessibility/`, `feature/contacts/`)**
   - Central `ActionValidator` enforces allowlists, permission/service state, fresh `snapshotId` + `nodeId`, excluded sensitive packages (banking/auth/UPI/password screens), and mandatory confirmation before contact writes.
   - `IntentExecutor` launches documented public Android Settings and Contacts intents with device-aware fallbacks.
   - `ElderAssistAccessibilityService` reads bounded screen trees (`maxDepth = 12`, `maxNodes = 60`), excludes password/OTP fields, and executes semantic node actions.

---

## Supported Commands (MVP)

- **Settings:** Wi-Fi, Bluetooth, Hotspot/Tethering, Display, Sound, Accessibility, Battery, General Settings.
- **System Navigation (requires Accessibility Helper enabled):** Go home, Go back, Show recent apps, Stop/Cancel.
- **Contacts:** Open Contacts, Search contact (with multi-match disambiguation), Create contact (with confirmation preview), Rename contact (with confirmation preview).
- **Blocked for Safety:** Deleting contacts/data, banking/UPI/payments, automated calls/messages.

---

## Setup & Build

### Prerequisites
- JDK 17+ (built & verified on OpenJDK 21)
- Android SDK Platform 36.1 (`compileSdk = 36.1`, `minSdk = 29`, `targetSdk = 36`)

### Local Configuration (`local.properties`, git-ignored)
Add your Android SDK path and optional Gemini API key in `local.properties` at the project root (never commit secrets):

```properties
sdk.dir=/path/to/Android/sdk
GEMINI_API_KEY=your_gemini_api_key_here
```

### Build & Test Commands
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Run Android lint
./gradlew lintDebug
```

Output APK path: `app/build/outputs/apk/debug/app-debug.apk`

---

## Permissions & Privacy

- `RECORD_AUDIO`: Requested just-in-time when tapping **Speak** (app remains usable via typed commands if denied).
- `READ_CONTACTS`: Requested just-in-time for contact search/rename disambiguation; contact names and phone numbers are never logged or sent to Gemini.
- `INTERNET`: Used only for Gemini fallback when deterministic parsing does not match.
- `AccessibilityService`: Manually enabled by the user in Android Settings; inspects the screen only while an active navigation task is running and excludes password/payment screens.

---

## Known Limitations

- **Voice provider:** Uses device `Android SpeechRecognizer` by default; on emulators or devices without a Google/system speech service, use the on-screen **"Or type a command"** input box.
- **OEM Settings variations:** Hotspot/tethering and battery subpages vary across manufacturers; `IntentExecutor` falls back safely to parent Settings screens when a subpage intent is unavailable.
- **Experimental AI Navigation:** Disabled by default in **Setup & Help**; capped at 5 steps and stops whenever a screen changes unexpectedly or confidence is low.
