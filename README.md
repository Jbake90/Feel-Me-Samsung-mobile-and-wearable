# Feel Me – Samsung Phone + Galaxy Watch Companion

Concept for a lightweight connected experience where paired friends tap a button on their Galaxy Watch to send a gentle "thinking of you" notification to the other person’s watch and phone.

## Goals
- Make a single-tap interaction from the watch that delivers a short, meaningful acknowledgement.
- Keep setup simple: invite a partner/friend, accept, and start sending pings.
- Ensure reliable delivery to both watch and phone, even when the watch is temporarily offline.

## High-level architecture
- **Android phone app (Kotlin/Jetpack Compose)**
  - Auth (email/OTP or Sign in with Google) and pairing UI.
  - Stores the friend list and consent state.
  - Uses **Firebase Cloud Messaging (FCM)** for server-to-device notifications.
  - Exposes a **foreground service** that relays inbound pings to the paired watch via the Wearable Data Layer.
- **Galaxy Watch companion (Wear OS 3/4)**
  - Simple UI with a large tap target for sending a ping.
  - Receives pings through the Data Layer or FCM (when LTE/Wi‑Fi is available).
  - Haptics + ambient-friendly notification for inbound pings.
- **Backend (Firebase Functions or lightweight Node/Go service)**
  - Manages user accounts, device tokens, pairing requests, and FCM fan-out to all registered endpoints for a user.
  - Enforces rate limiting and abuse protections.

## Data flow
1. User A taps the watch button.
2. Watch sends an **action packet** to the paired phone via the Wearable Data Layer; the phone forwards it to the backend.
3. Backend validates pairing and pushes an FCM message to **User B’s phone and watch**.
4. User B’s phone relays the event to their watch; both surfaces show a notification with haptic feedback.

If the watch has direct network access, it can send/receive via FCM without the phone. Otherwise the phone acts as the bridge.

## Key platform pieces
- **Wearable Data Layer API** for phone↔watch messaging when co-located.
- **FCM** for wide-area delivery and device fan-out.
- **WorkManager** to queue outbound pings when the device is offline.
- **Room** or **DataStore** for lightweight local persistence of pairings and acknowledgements.

## Pairing/permissions
- Mutual consent is required: invitations expire and require explicit acceptance.
- Phone app asks for **Notification**, **Foreground service**, and **Body Sensors** (for haptics) where needed.
- On the watch, request **Body Sensors** (for haptics) and **Network** permissions when using LTE/Wi‑Fi.

## Offline and reliability
- Pings queued with WorkManager until connectivity returns.
- Data Layer guarantees delivery with acknowledgements; retries on failures.
- Backend enforces per-user rate limits and max queue depth to prevent spam.

## Privacy and safety
- Content is minimal (just a timestamp and sender ID); no location or message body is sent.
- All traffic uses TLS; tokens and IDs are rotated periodically.
- Clear opt-out and delete-account paths erase tokens and local caches.

## Future extensions
- Shared status ("Available", "Do Not Disturb").
- Optional mood colors/emojis on pings.
- Mini complications or tiles for faster access on Wear OS.

## How to turn this repo into a working build
Follow this checklist end-to-end; you do not need extra CLI tools beyond Android Studio.

1. **Install tools.** Install Android Studio Hedgehog (or newer) with **Android SDK 34**, **Google Play services**, and the **Wear OS** emulator image. In Device Manager, create a phone emulator and a Wear OS emulator, then **pair them**.
2. **Create a Firebase project.** In Firebase Console, create one project and enable **Cloud Messaging**. Add two Android apps with package IDs `com.feelme.mobile` and `com.feelme.wear`, then download each app’s `google-services.json`.
3. **Place Firebase configs.** Drop the downloaded files into `android/app-mobile/google-services.json` and `android/app-wear/google-services.json`.
4. **Open in Android Studio.** Open the `android` folder. Let Gradle sync and install dependencies.
5. **Run the phone app.** Select the phone device and run the **app-mobile** configuration. It shows the pairing text field, “Send ping” button, and keeps a foreground service alive for watch relays.
6. **Run the watch app.** Select the paired Wear emulator/device and run **app-wear**. You will see a single large button to send a ping.
7. **Wire the backend stubs.** The code already sends Data Layer pings locally, but internet delivery depends on three TODOs:
   - In `android/app-mobile/src/main/java/com/feelme/mobile/FirebasePingService.kt`, send the FCM token to your backend so it knows where to deliver pings.
   - In `android/app-mobile/src/main/java/com/feelme/mobile/PingWorker.kt` and `android/app-mobile/src/main/java/com/feelme/mobile/PingRepository.kt`, replace the placeholder network calls with a real HTTPS POST to your backend (or Firebase Function) that fan-outs to your partner.
   - In `android/app-wear/src/main/java/com/feelme/wear/WearFirebaseService.kt`, forward the watch FCM token to the backend as well.
8. **Test a round-trip.** With both apps running, tap **Send ping** on one side. You should see a haptic + notification on the other device (via Data Layer when co-located, or FCM when apart).
9. **Prepare for production.** Add sign-in (Firebase Auth is easiest), server-side rate limiting, and an HTTPS endpoint that accepts `{ fromUserId, toUserId }` and uses the saved tokens to send an FCM data message of type `ping` to every device for `toUserId`.

If you prefer a step-by-step walkthrough of the Android Studio steps, see `android/README.md`.

### Troubleshooting build errors
- **"Android SDK location not found"** — This means Gradle cannot see the SDK. Open the project in **Android Studio** and install the Android SDK (API 34+) when prompted. Studio will auto-create `android/local.properties` with a `sdk.dir=/path/to/Android/sdk` entry. If you are building from the terminal, create that file yourself or set `ANDROID_HOME`/`ANDROID_SDK_ROOT` to point at your SDK install.
