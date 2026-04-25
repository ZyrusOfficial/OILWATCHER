# Oil Watcher — Community Gas Price Monitoring App

## Architecture: Hybrid Native + WebView

This Android app uses a **hybrid architecture** where performance-critical screens
are built natively in Jetpack Compose, and content-heavy screens reuse polished
HTML/CSS via WebView.

```
    ★ NATIVE (Compose)              ▣ WEBVIEW (HTML)
    ┌──────────────────┐            ┌───────────────────┐
    │ Map Screen       │            │ Station Details    │
    │ Camera Capture   │            │ Community History  │
    │ Analyze & Preview│            │ Leaderboard        │
    └──────────────────┘            │ Profile            │
                                    │ Settings           │
                                    └───────────────────┘
```

## Project Structure

```
app/src/main/
├── java/com/oilwatcher/monitor/
│   ├── OilWatcherApp.kt              Hilt Application
│   ├── di/
│   │   └── AppModule.kt              DI: ML Kit, Context
│   ├── domain/model/
│   │   └── Models.kt                 Station, Prices, User, etc.
│   ├── data/ml/
│   │   └── PriceExtractor.kt         ML Kit OCR price extraction
│   ├── presentation/
│   │   ├── MainActivity.kt           Single-activity entry point
│   │   ├── theme/
│   │   │   ├── Color.kt              M3 color tokens (50+ colors)
│   │   │   ├── Type.kt               Typography (Clash/Epilogue/Manrope)
│   │   │   └── Theme.kt              M3 theme composable
│   │   ├── navigation/
│   │   │   ├── Routes.kt             Screen routes + bottom nav items
│   │   │   ├── BottomNavBar.kt        Glassmorphism bottom nav
│   │   │   └── AppNavHost.kt         NavHost: 3 native + 5 webview
│   │   ├── native_screens/
│   │   │   ├── map/MapScreen.kt       Google Maps + markers
│   │   │   ├── camera/CameraScreen.kt CameraX + viewfinder
│   │   │   └── analyze/AnalyzeScreen.kt Price verification form
│   │   └── webview/
│   │       ├── AndroidBridge.kt       JS ↔ Native bridge
│   │       └── WebViewScreen.kt       Compose WebView wrapper
│   └── assets/webview/
│       ├── js/bridge.js               Shared JS bridge
│       ├── station_details.html
│       ├── community_history.html
│       ├── community_leaderboard.html
│       ├── profile.html
│       └── settings.html
├── res/values/
│   ├── strings.xml
│   ├── colors.xml
│   └── themes.xml
├── AndroidManifest.xml
└── proguard-rules.pro
```

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI (Native) | Jetpack Compose + Material 3 |
| UI (WebView) | HTML/CSS + JS Bridge |
| OCR | Google ML Kit Text Recognition v2 (on-device) |
| Camera | CameraX |
| Maps | Google Maps SDK (maps-compose) |
| Backend | Firebase (Auth, Firestore, Storage) |
| Local DB | Room |
| DI | Hilt |
| Networking | Retrofit + Gson |
| Images | Coil |

## Setup Instructions

1. Open in **Android Studio Ladybug** or later
2. Add `google-services.json` to `app/` (from Firebase console)
3. Set your Google Maps API key in `gradle.properties`:
   ```
   MAPS_API_KEY=your_actual_key_here
   ```
4. Sync Gradle and run

## Design System

The app follows the **"Tactile Architect"** design language:
- **Colors**: Warm paper surfaces + Vivid Kinetic Orange (#FF5E00)
- **Typography**: Clash Display / Epilogue headlines, Manrope body
- **Corners**: 20dp universal radius
- **Borders**: None — tonal layering only
- **Glass**: Semi-transparent backgrounds with backdrop-blur
- **Feedback**: Universal active:scale press response