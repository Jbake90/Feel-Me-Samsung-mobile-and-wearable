# Feel Me starter scripts (phone + Galaxy Watch)

This folder contains Android Studio-ready starter projects for the phone app (`app-mobile`) and Wear OS companion (`app-wear`). They are intentionally simple so you can paste in your Firebase keys and run end-to-end without wiring a custom backend yet.

## Quick start
1. Install **Android Studio Hedgehog** (or newer) with the **Android SDK 34** and **Wear OS** tools.
2. In Firebase Console create one project, enable **Cloud Messaging**, and download two `google-services.json` files: one for `com.feelme.mobile` and one for `com.feelme.wear` (add both app IDs in Firebase). Place them at:
   - `android/app-mobile/google-services.json`
   - `android/app-wear/google-services.json`
3. Open this `android` folder in Android Studio. It will sync the Gradle project and download dependencies.
4. Create a virtual phone and a Wear OS emulator (or use physical devices). Pair them through the **Device Manager**.
5. Run **app-mobile** on the phone and **app-wear** on the watch. Use the phone screen to enter a partner ID/email, hit **Pair**, then tap **Send ping** on either device.

### If the build says the Android SDK is missing
Gradle errors like `Android SDK location not found` or `sdk.dir missing` mean your SDK path is not configured. Fix it by:
- Opening the project in **Android Studio**, letting it install SDK **API 34+**, and saving `android/local.properties` automatically with `sdk.dir=/path/to/Android/sdk`.
- Or, when building from the command line, create `android/local.properties` yourself with that `sdk.dir` line **or** export `ANDROID_HOME`/`ANDROID_SDK_ROOT` to your SDK path before running Gradle.

## Detailed setup (for first-timers)
1. **Clone and open:** From Android Studio’s welcome screen choose **Open**, select this `android` directory, and wait for Gradle sync to finish.
2. **Install emulators:** In **Tools → Device Manager**, create a Pixel-class phone with API 34 and a **Wear OS 4 (Round)** device. Click the overflow menu to **Pair Wearable** to your phone emulator.
3. **Add Firebase configs:** Copy the two `google-services.json` files into the paths above. If you forget this, the build will fail with a missing `google-services` error.
4. **Pick build variants:** In the **Build Variants** panel, keep `debug` selected for both modules.
5. **Run the apps:**
   - Choose the phone emulator/device from the run target list and run **app-mobile**.
   - Choose the paired watch emulator/device and run **app-wear**.
6. **Verify Data Layer path:** With both apps running and connected, tap **Send ping** in either app; you should see a logcat entry noting a `/feelme/ping` message even before you wire the cloud path.

## Fill in the TODOs so pings work over the internet
The starter code already relays pings locally (phone ↔ watch) via the Wearable Data Layer. To reach your partner when devices are apart, complete these steps:

- **Register FCM tokens:**
  - `app-mobile`: In `src/main/java/com/feelme/mobile/FirebasePingService.kt`, send the refreshed token to your backend (e.g., HTTPS POST `/registerDevice`).
  - `app-wear`: In `src/main/java/com/feelme/wear/WearFirebaseService.kt`, do the same for the watch token.
- **Send pings through your backend:** Replace the placeholder network calls in `src/main/java/com/feelme/mobile/PingRepository.kt` and `src/main/java/com/feelme/mobile/PingWorker.kt` with a real HTTPS call. Example for a Firebase Function:

```kotlin
// Inside PingWorker.doWork()
val payload = mapOf("fromUserId" to fromId, "toUserId" to toId)
val requestBody = payload.entries.joinToString("&") { "${it.key}=${it.value}" }
val url = URL("https://<your-cloud-function-url>/sendPing")
with(url.openConnection() as HttpURLConnection) {
    requestMethod = "POST"
    doOutput = true
    outputStream.write(requestBody.toByteArray())
    if (responseCode != 200) return Result.retry()
}
```

- **Handle incoming pings:** The FirebaseMessagingService classes already surface a notification. Customize the notification text or deep links as you like.

After wiring these pieces, you can run on two separate phone/watch pairs (or one pair and one standalone watch with LTE/Wi‑Fi) and see pings arrive over the network.

## What’s included
- **Phone app (Jetpack Compose):** pairing text field, “Send ping” button, foreground relay service, WorkManager stub for cloud delivery, and FCM handler for incoming pings.
- **Wear app (Wear Compose):** single-button UI, Data Layer send/receive, FCM handler, and notification channel.
- **Data Layer wiring:** both apps share the `/feelme/ping` path to mirror pings when devices are co-located.
- **FCM stubs:** FirebaseMessagingService implementations on both apps log tokens and surface incoming pings as notifications. Replace the TODO comments with calls to your backend.

## Minimal backend
You can start with a Firebase Cloud Function that echoes pings back to the target user ID. Pseudocode:
```js
exports.sendPing = onRequest(async (req, res) => {
  const { fromUserId, toUserId } = req.body;
  const message = {
    token: await lookupToken(toUserId),
    data: { type: 'ping', fromUserId },
  };
  await getMessaging().send(message);
  res.json({ ok: true });
});
```

Call this endpoint from `PingWorker` after replacing the TODO with a real HTTPS POST.

## Safety + rollout notes
- Rate-limit sends server side and expire pairing requests.
- Add sign-in (Firebase Auth is the quickest) before shipping widely.
- Keep the foreground service running on the phone so Data Layer relays stay alive.
- Test with Bluetooth-only, Wi‑Fi, and LTE watch scenarios to confirm both Data Layer and FCM paths work.
