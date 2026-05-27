# 🐉 Block Puzzle Dragon

A dragon-themed Tetris-like block puzzle game for Android.

## Features

- 🎮 Classic 10×20 Tetris-style gameplay
- 🐉 Dragon colour theme (fire orange, mystic purple, dragon gold)
- 👻 Ghost piece preview (shows where piece will land)
- 🔮 7-bag random piece generation (fair randomizer)
- 📈 Progressive difficulty — speed increases every 10 lines
- 🏆 Local high score (SharedPreferences)
- ⏸ Pause / Resume with auto-pause on `onPause()`
- 👆 Swipe gestures + on-screen button panel
  - Swipe left/right → move
  - Swipe down → hard drop
  - Single tap on board → rotate
  - Buttons: ◀ ▶ Rotate ▼Soft ⬇Drop ⏸Pause ↺Restart

## Project structure

```
BlockPuzzleDragon/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/blockpuzzle/dragon/
│       │   ├── MainActivity.kt      ← Activity, input wiring
│       │   ├── GameView.kt          ← SurfaceView renderer + game loop
│       │   ├── GameEngine.kt        ← Pure game logic (no Android deps)
│       │   └── Tetromino.kt         ← Piece shapes & rotation
│       └── res/
│           ├── layout/activity_main.xml
│           ├── values/{colors, strings, themes}.xml
│           └── drawable/ + mipmap-hdpi/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/wrapper/gradle-wrapper.properties
```

## Requirements

- Android Studio Hedgehog (2023.1) or newer
- Android SDK 34
- Minimum Android 7.0 (API 24)
- Kotlin 1.9.x

## Build & Run

1. Open the `BlockPuzzleDragon/` folder in Android Studio
2. Let Gradle sync
3. Run on a device or emulator (portrait orientation)

## Scoring

| Lines cleared at once | Points × level |
|---|---|
| 1 (Single) | 100 |
| 2 (Double) | 300 |
| 3 (Triple) | 500 |
| 4 (Dragon!) | 800 |

Level increases every 10 lines. Drop speed starts at 800 ms/tick and decreases by 70 ms per level (minimum 80 ms).
