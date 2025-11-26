# Technical architecture

This document sketches how to ship the "Feel Me" phone + Galaxy Watch experience with today’s Samsung ecosystem (Galaxy Watch 4+ on Wear OS 3/4).

## Modules
- `app-mobile` (Android phone)
  - Kotlin + Jetpack Compose UI.
  - Uses Google Play Services Wearable (Data Layer) for local phone↔watch transport.
  - Foreground service to keep the bridge alive when relaying notifications.
- `app-wear` (Wear OS companion)
  - Kotlin + Wear Compose.
  - Tile + app screen for the single-tap "ping" action.
  - Uses haptics-friendly notification style for inbound pings.
- `backend`
  - Firebase Functions (TypeScript) or a small Node/Go service.
  - Persists users, pairings, and device tokens; pushes via FCM.

## Message contracts
- **Ping request**
  ```json
  {
    "type": "ping",
    "fromUserId": "u_123",
    "toUserId": "u_456",
    "sentAt": 1710000000,
    "deviceId": "watch-abc"
  }
  ```
- **Ping receipt/ack**: same payload with `type: "ack"` to allow retry/backoff.

## Flows
### Sending from watch
1. User taps the watch tile/button.
2. `app-wear` sends a `ping` packet over the Data Layer to the phone.
3. `app-mobile` enqueues a WorkManager job that calls the backend API.
4. Backend validates the pairing and fan-outs an FCM to target user devices (phone + watch).
5. `app-mobile` (for the recipient) raises a high-priority notification and mirrors to the watch via Data Layer if it’s nearby.

### Receiving directly on watch (no phone nearby)
1. `app-wear` registers an FCM token and maintains it with the backend.
2. Backend includes the watch token when fan-ing out.
3. Watch shows a notification and sends an `ack` to the backend when the user dismisses/views it.

## Reliability
- Data Layer already retries; include an `ack` payload so the sender can suppress duplicate UI.
- WorkManager with `NetworkType.CONNECTED` for phone-to-cloud delivery; exponential backoff on failures.
- FCM high-priority messages for timely delivery; throttle to reasonable limits.

## Security and abuse
- Pairing requires mutual acceptance; invites expire after 24h.
- Backend enforces per-user send rate limits and max active invites.
- Use Firebase Authentication or token-based auth; sign all requests from device with an auth token.

## Minimal screens
- **Phone**: onboarding, invite list, incoming invite approval, settings (mute, delete account).
- **Watch**: tile with a single ping action, history of last few pings, mute toggle.

## Build/test notes
- Develop with Android Studio Hedgehog or newer.
- Use the Wear OS emulator plus a paired virtual phone for Data Layer testing.
- Add unit tests for pairing logic and a small fake backend for integration tests.
