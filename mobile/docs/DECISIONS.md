# Mobile decisions

- **Android only** for now (no Mac available); the iOS folder is not generated.
- **Map:** `flutter_map` with Mapbox raster tiles (no native Mapbox setup). Incidents are drawn as red dots with a white border, like the web map.
- **Models:** plain hand-written Dart classes instead of freezed/json_serializable, to avoid `build_runner` time and memory on a laptop.
- **Config:** `--dart-define-from-file=env/dev.json` instead of native flavors.
- **Local backend:** `adb reverse tcp:8080 tcp:8080`, so the app uses `http://localhost:8080` and never needs the laptop's IP. Cleartext is enabled in the debug manifest only.
- **CI** is not set up yet.
