# Auto Shorts Android App

A fully functional Android application for **Auto Shorts** - an AI-powered platform that converts long videos into viral short-form videos (Instagram Reels, YouTube Shorts, TikTok).

## Features

- 📱 **Upload Videos** - Local file picker or YouTube URL
- ⚙️ **Customize Settings** - Duration (30s-120s), quantity (1-10), language
- 🔄 **Real-time Processing** - Live progress updates with step-by-step status
- 🎬 **Video Preview** - ExoPlayer with 9:16 aspect ratio
- ✏️ **Caption Customization** - Style, color, and font size options
- 🚀 **Rocket Share** - AI-generated titles, descriptions, and hashtags
- 📤 **One-Click Sharing** - Instagram, YouTube, TikTok integration

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM with StateFlow |
| Navigation | Navigation Compose |
| Networking | Retrofit 2.9 + OkHttp 4.12 |
| Video Player | ExoPlayer (Media3) 1.2 |
| Background Tasks | WorkManager 2.9 |
| Animations | Lottie Compose 6.3 |

## Screens

1. **Splash Screen** - Animated intro with app branding
2. **Upload Screen** - Video selection and settings configuration
3. **Processing Screen** - Real-time progress with step indicators
4. **Result Screen** - Video preview with caption customization
5. **Rocket Share Screen** - AI metadata generation and sharing

## Project Structure

```
app/src/main/java/com/autoshorts/app/
├── AutoShortsApp.kt          # Application class
├── MainActivity.kt           # Single activity
├── navigation/               # Navigation routes
├── data/
│   ├── api/                  # Retrofit API service
│   ├── model/                # Data models
│   └── repository/           # Repository layer
├── ui/
│   ├── theme/                # Colors, Typography, Theme
│   ├── components/           # Reusable UI components
│   └── screens/              # All app screens
├── util/                     # Constants, Extensions
└── worker/                   # WorkManager workers
```

## Setup

1. Clone this repository
2. Open in Android Studio
3. Update `BASE_URL` in `app/src/main/java/com/autoshorts/app/util/Constants.kt`
4. Sync Gradle and run on emulator or device

## Backend API

Configure your backend URL in `Constants.kt`:
- Emulator: `http://10.0.2.2:8000/`
- Physical device: `http://YOUR_PC_IP:8000/`
- Production: `https://your-api.com/`

## Requirements

- Android Studio Hedgehog or newer
- Android SDK 26+ (Android 8.0)
- Kotlin 1.9.20
- JDK 17

## License

MIT License
