# Mobile decisions

- **Android only** for now (no Mac available); the iOS folder is not generated.
- **Map:** `flutter_map` with Mapbox raster tiles (no native Mapbox setup). Incidents are drawn as red dots with a white border, like the web map.
- **Models:** plain hand-written Dart classes instead of freezed/json_serializable, to avoid `build_runner` time and memory on a laptop.
- **Config:** `--dart-define-from-file=env/dev.json` instead of native flavors.
- **Local backend:** `adb reverse tcp:8080 tcp:8080`, so the app uses `http://localhost:8080` and never needs the laptop's IP. Cleartext is enabled in the debug manifest only.
- **CI** is not set up yet.

## Walk sessions and safety (M5, M6)
- **Location reports use REST** (`PUT .../update/location`), not the socket: each report is confirmed or refused, and a failed one is simply replaced by the next fix. The socket carries alerts and one `/app/session.location` message per connection, which the server uses to start watching that connection (it is also a real location update now).
- **Reporting rate:** at most every 10 s, only after moving 3 m, and at least every 30 s. The server decides what counts as standing still (15 m, 180 s).
- **A walk can only start within 100 m of its route.** Starting elsewhere would look like an instant deviation, and 200 m off the route for 2 minutes raises a real emergency on the server.
- **The socket is signed in** with `Authorization: Bearer <jwt>` on STOMP CONNECT (the server refuses a connection without it). The token is read fresh on every (re)connect.
- **Reconnects** wait 1, 2, 4, 8, 16, then 30 s. The "Connection lost" banner shows only after 5 s down.
- **The "Are you safe?" prompt** counts down 45 s (the server's grace period). The poll of the walk (every 20 s) is a backup if a live message is missed.

## Backend notes from the mobile work
- STOMP authentication happens on CONNECT only: nothing checks that a user may SUBSCRIBE to `/topic/alert/{sessionId}` or `/topic/session/{sessionId}`, so any signed-in user could listen to another walk's alerts.
- A JWT that expires during a long walk makes REST calls return 401, which signs the app out and stops tracking. Token lifetime (`AUTH_TOKEN_JWT_EXPIRATION_MS`) should comfortably cover a walk.
- Once the app has registered its connection, a connection that stays down for more than 60 s raises a real emergency (emails the contacts). Closing the app mid-walk therefore ends in an emergency by design.
