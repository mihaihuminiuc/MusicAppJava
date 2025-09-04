# MusicAppJava

Simple streaming music sample using a foreground service with Media3 ExoPlayer so playback continues when the UI is backgrounded.

## Features
- Foreground `Service` with `mediaPlayback` type
- ExoPlayer streaming an internet radio URL
- MediaSession for system integration
- Ongoing notification with Play/Pause action
- Compose UI (Kotlin) with simple button

## How to Run
1. Open in Android Studio (Hedgehog+ recommended).
2. Let Gradle sync (AGP 8.x, Kotlin 2.0, compileSdk 36).
3. Run on a device / emulator with internet access (API 31+ due to minSdk).
4. Tap Play to start stream. Press Home: audio continues, notification shows controls.

## Notes / Next Steps
- Replace test stream URL with your own stream (HLS / MP3) in `MusicForegroundService.ensurePlayer()`.
- Add audio focus handling and noisy (becoming noisy) broadcast for headphones removal.
- Add proper state flow from Service to UI instead of polling (e.g., `MutableStateFlow`).
- Add MediaStyle large icon and metadata updates.
- Support playback controls via Bluetooth / car by expanding MediaSession callbacks.

## License
This sample contains only original code you may adapt freely.
