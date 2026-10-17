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

The game menu's controller setup and fire remapping remain available. The build's signing key is retained in GitHub Actions cache where available; if a future debug build reports a signing conflict, uninstall the old app before installing the new one.

## Build

GitHub Actions builds and publishes the APK automatically when the app or index.html changes. For a local build, use Java 17, Android SDK 35, and Gradle 8.9, then run `gradle :app:assembleDebug` inside fire-tv.
