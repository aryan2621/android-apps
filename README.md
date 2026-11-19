# Android Apps

A collection of small Android apps. Each folder is a standalone Gradle project (Kotlin).

| App | What it does | Stack | Min SDK |
|---|---|---|---|
| [Cool Launcher](Cool%20Launcher) | Custom home-screen launcher with app icons and settings | Kotlin, Compose | 21 |
| [Instashow](Instashow) | Turns today's Health Connect stats into Instagram stories | Kotlin, Compose | 26 |
| [Pause](Pause) | A breathing screen before you open Instagram, YouTube and other feeds | Kotlin, Compose | 26 |
| [Tasker](Tasker) | Task management with reminders, notifications and Google sign-in | Kotlin, Compose | 24 |

## Build

```sh
cd <app folder>
./gradlew assembleDebug      # APK in app/build/outputs/apk/debug/
./gradlew installDebug       # install on a connected device or emulator
```

Requires JDK 17+ and the Android SDK. Open a folder in Android Studio to run it from the IDE.

## License

MIT, see [LICENSE](LICENSE).
