# Fire TV app (Fire OS 7+)

This APK bundles the game's HTML and runs it fullscreen with a launcher icon. It reads Android joystick MotionEvents and gamepad KeyEvents directly, then passes a standard controller state into the game. Silk is not involved. Internet is still needed for Three.js, Tailwind, and fonts.

Download the APK from https://github.com/InVisionx/vader_zim_shooter/releases/tag/fire-tv-latest . This is a sideloadable test build; it is not an Amazon Appstore release. Controller input and performance must be checked on the television.

## Install from a computer

1. On Fire TV, open Settings > My Fire TV (or Device & Software) > About. If Developer Options is hidden, select the device name seven times, then return to the previous menu.
2. Open Developer Options and enable ADB Debugging.
3. Find the TV's IP address under About > Network. Connect the computer and TV to the same local network.
4. Install Android SDK Platform Tools on the computer, then run:

   adb connect YOUR_TV_IP:5555

5. Accept the connection prompt on the TV, then run:

   adb install -r call-of-doom-fire-tv.apk

6. Open Your Apps & Channels and launch Call of Doom. Use the app's menu to move its icon near the front/home favorites. You can disable ADB debugging after installing.

Amazon installation documentation: https://developer.amazon.com/docs/fire-tv/installing-and-running-your-app.html
Amazon connection instructions: https://developer.amazon.com/docs/fire-tv/connecting-adb-to-device.html

## Controls

A deploy/jump, B slide, X reload, Y or RB switch weapon, LB dash, left stick move, right stick aim, LT aim down sights, RT fire, left stick click sprint, right stick click slash, D-pad up orbital strike, Start pause/resume. Back opens the app exit menu.

The game menu's controller setup and fire remapping remain available. Version 0.2.1 uses a permanent signing key stored in encrypted GitHub Actions secrets, and the build verifies the public certificate fingerprint before publishing. Uninstall the original test build once to migrate to this key. Future builds retain this signing identity.

## Build

GitHub Actions builds and publishes the APK automatically when the app or index.html changes. For a local build, use Java 17, Android SDK 35, and Gradle 8.9, then run `gradle :app:assembleDebug` inside fire-tv.

## Version 0.2: performance and automatic game updates

Install this APK once over the previous version. It removes the normal input debug banner, caps TV rendering at 1280×720, disables expensive TV shadow/outline passes, reduces particles and radar refresh work, and frees expired projectile/particle GPU resources. Desktop graphics remain unchanged.

On each launch, the app checks the GitHub release for game content updates in the background. A newer compatible HTML update is downloaded into private app storage and verified against its SHA-256 digest before it is used. It applies on the next launch, without interrupting a match. Offline startup uses the last validated download or the bundled game. Native Android changes still require an APK install; this updater does not silently install APKs. No extra installation permission is needed for game content updates.

## Signing migration in 0.2.1

The original test APK and the 0.2.0 APK used different automatically generated debug signing certificates. Fire OS rejects that as an in-place update. Uninstall the original app once, then install call-of-doom-fire-tv-v0.2.1.apk. The permanent signing key is held in GitHub Actions repository secrets, and its public fingerprint is checked during every build. For local builds, provide FIRE_TV_KEYSTORE_PATH and FIRE_TV_SIGNING_PASSWORD. Never commit the private keystore or password.
