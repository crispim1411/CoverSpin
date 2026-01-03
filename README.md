# CoverSpin 🔄

**CoverSpin** is a lightweight Android utility designed to manage and enable screen rotation on foldable devices (specifically tested on the Galaxy Z Flip 7)

The main goal is to allow screen orientation (Landscape/Portrait/Sensor) to work correctly on external screens (cover screens) where the native OS might impose restrictions.

## Setup and Installation
1. Download the latest [release](https://github.com/crispim1411/CoverSpin/releases) and extract it.
2. Install the apk file.
3. Accept the permissions.
4. **Important:** Add the app to **Good Lock (MultiStar)** on the Cover Screen launcher.
5. Open the app on the Cover Screen to initialize it.
6. Done.

*OBS: If the rotation stops working, restart CoverSpin on the cover screen again via Good Lock.*

## Features

The app has an option to intercept **Double Clicks** on the physical volume buttons to control rotation without needing to open the app again.

> *Note: Single clicks still adjust the volume normally. But this disables camera shutter by volume key, the torch functionality on the cover screen and increasing/decreasing the volume by holding the volume keys.*

- **Shortcuts:** Toggle rotation on/off using physical volume buttons.
- **Intelligent State:** Automatically pauses rotation when the screen is locked (to prevent Lockscreen bugs) and restores your preference upon unlocking.
- **Visual Feedback:** Displays custom overlay toasts on the cover screen to confirm when rotation is enabled/disabled.
- **Background Execution:** Runs as a lightweight Accessibility Service (`EventsService`) to monitor keys and screen state.

### Requirements

- **Min SDK:** Android 8.0+ (Oreo).
- **Permissions:** Overlay Permission & Accessibility Service.

## Contribution
Contributions are welcome! If you have a foldable device (Galaxy Z Flip, Moto Razr, etc.) and want to test or improve sensor behavior, feel free to open an Issue or Pull Request.

If you want to support the development:
[Help me buy ice cream 🍦](https://www.paypal.com/paypalme/crispim1411)