# Goal Challenge — Native Android

A native Android rewrite of the supplied `index.html` + `game.html` browser game.

## What is included
- Native Kotlin Android app — no WebView, no JavaScript runtime.
- Separate pure-Kotlin `game-core` module for physics/rules.
- Fixed 60 Hz simulation with display-synced rendering.
- Ported gravity, bounce, ball-to-ball collision, black-hole rules, scoring and automatic next rounds.
- Native Canvas stadium/field/goal/hole/ball rendering.
- Original supplied player artwork extracted from the HTML and stored as normal Android resources.
- Native sound effects, winner TextToSpeech and optional haptic feedback.
- Persistent score/settings using SharedPreferences.
- Responsive virtual 430×764 game world that aspect-fits phones, tablets, foldables and landscape.
- No login, ads, analytics, network or INTERNET permission.
- Unit-testable deterministic engine with seeded RNG support.

## Build requirements
- Android Studio with Android SDK Platform 36 installed.
- JDK 17 or newer (AGP 9.4 supports JDK 17; Android Studio bundled JDK is recommended).
- Internet on the first Gradle sync to download Gradle/plugins.

## Open in Android Studio
1. Extract the ZIP.
2. Open the `goalchallenge` folder.
3. Let Android Studio install/sync Android SDK 36 if requested.
4. Select the `app` run configuration and run on a phone/emulator.

## Command line (Windows)
```bat
gradlew.bat :game-core:test
gradlew.bat :app:assembleDebug
gradlew.bat :app:bundleRelease
```

## Build and install on a connected Android phone (Linux)
Connect and unlock the phone, enable USB debugging, and accept the USB debugging prompt. Double-click the **Goal Challenge - Build and Install** desktop shortcut. It builds and tests the game, installs the debug app on the single connected device, and opens it.

You can also run it from a terminal:
```bash
./build-and-install.sh
```

To create the desktop shortcut for the current checkout, run `./install-desktop-shortcut.sh` once. Then double-click **Goal Challenge - Build and Install** from the Desktop.

The included `gradlew.bat` is a portable bootstrap that downloads Gradle 9.6.0 on first use. This avoids shipping an opaque wrapper JAR in this generated archive.

For a conventional repository setup, run `SETUP_STANDARD_GRADLE_WRAPPER.bat` once on your Windows machine with internet access. It generates the official Gradle Wrapper JAR/scripts for Gradle 9.6.0.

## Package ID
Current application ID: `com.goalchallenge.game`

**Important:** a Google Play application ID cannot be changed after publishing. Change it before the first Play release if you want another ID.

## Release output
After signing is configured, the Play upload artifact is:
`app/build/outputs/bundle/release/app-release.aab`

## Source mapping
The original web sources are retained under `original-web/` for reference only. They are not packaged into the Android app.

## Policy / IP note
The supplied build uses the provided Messi/Ronaldo names and images. Google Play technical compatibility does not grant rights to celebrity names, photos, trademarks, or likenesses. Before production publication, confirm you have the necessary rights or replace them using `Branding.kt` and the player drawable files. See `docs/PLAY_RELEASE_CHECKLIST.md`.
