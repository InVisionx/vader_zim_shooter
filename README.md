# Call of Doom — Living Room Edition

Upload index.html to the root of your GitHub repository. In the repository's Settings > Pages, choose deployment from your main branch and the root folder, then save. Open the published HTTPS address on the TV.

Pair your controller with the TV, open the game, and press a controller button to make it detectable. Press A / Cross again to deploy. Start pauses or resumes; D-pad up/down navigates the menu; left/right changes selected options; A confirms or toggles. The menu includes the full combat control map, look sensitivity, inverted Y, starting weapon, music, and optional fullscreen.

Controller support depends on the TV browser exposing the standard Gamepad API, plus WebGL support for the game itself. A paired controller may control the TV interface without being available to websites. If the menu never reports a controller connection, try a different browser or connect a computer to the TV over HDMI. Some browsers require an initial remote/mouse click to enable audio or fullscreen.

This is a single HTML file with internet-loaded Three.js, Tailwind CSS, and Google Fonts. Internet access is required. Gameplay uses local bots; it is not online multiplayer. No build step is needed.

Changes: simplified menu and HUD surfaces, larger typography, controller settings and connection status, analog sticks with radial deadzones, frame-rate-independent aiming, analog movement speed, standard Xbox/PlayStation button mapping, pause/reset on disconnect or lost focus, and reduced rendering resolution for high-resolution displays.

Validation: inline JavaScript syntax checks and simulated controller tests for deadzones, movement, aim, fire, deploy, pause/resume, and disconnect reset passed. Visual layout, physical controllers, audio/fullscreen behavior, and TV performance have not been verified on hardware.

Controller update (v2): Controller / TV mode blocks emulated mouse clicks from firing, provides live axis/button diagnostics, and includes alternate right-stick axis mappings. Amazon documents that Fire TV Silk does not fully support gamepad input: https://docs.aws.amazon.com/gameliftstreams/latest/developerguide/sdk-browsers-input.html . Hiding the page cursor cannot force a browser to expose controller sticks or hide its own browser pointer. Keyboard/mouse play is available through the Input setting.
