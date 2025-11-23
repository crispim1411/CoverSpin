# CoverSpin 🔄

**CoverSpin** is a lightweight Android utility designed to manage and force screen rotation on foldable devices (Flip 6 and 7)

The main goal is to allow screen orientation (Landscape/Portrait/Sensor) to work correctly on external screens (cover screens) where the native OS might impose restrictions.

## Setup and Installation
1. Go to this link. Download the relase-01.zip and extract It
2. Install de apk file
3. Accept the permissions
4. Open the app in the coverscreen to initialize It
5. Done

OBS: For work you need to add the app in GoodLock and initialize on CoverScreen

OBS2: If you close All Apps in Recent Apps you'll need to initialize CoverSpin on coverscreen again 

OBS3: It appears to work only for Flip 6 and 7 

## Features

- **Invisible Control Overlay:** Uses a transparent `View` of type `TYPE_APPLICATION_OVERLAY` to inject orientation parameters into the WindowManager.
- **Screen Detection:** Automatically closes if started on the main internal screen (`displayId == 0`) to save resources and focus only on the secondary screen.
- **Background Execution:** The app moves to the background (`moveTaskToBack`) immediately after initialization, keeping the overlay active without occupying the user interface.
- **Lock Screen Support:** Configured to work even when the device is locked (`FLAG_SHOW_WHEN_LOCKED`).

## The Rotation Secret
Android generally prioritizes the orientation of the top-most window. CoverSpin creates an invisible 0x0 pixel window with high priority and sets the orientation on it.
This forces the Android `WindowManager` to respect the orientation defined by the sensor (gyroscope/accelerometer), allowing rotation on screens that would normally be locked in Portrait mode.

### Requirements

- Min SDK: usually 26+ for foldables
- To work, the app requires the overlay permission (Display over other apps).

### Roadmap & Known Issues 🚧

- **Bug Fix:** The aspect ratio icon is misaligned when the cover screen is rotated to landscape mode.
- **Bug Fix:** It seems to ignoring the time to lock on coverscreen
- **Feature:** Plan to include the Recent Apps view

## Contribution
Contributions are welcome! If you have a foldable device (Galaxy Z Flip, Moto Razr, etc.) and want to test or improve sensor behavior, feel free to open an Issue or Pull Request.
