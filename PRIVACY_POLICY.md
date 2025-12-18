### CoverSpin: Privacy Policy

Welcome to the CoverSpin app for Android!

This is a utility app designed to provide screen rotation control on the cover screens of foldable devices. As the developer, I take your privacy very seriously. I have not programmed this app to collect any personally identifiable information.

### Data collected by the app

I hereby state that CoverSpin **does not collect, store, or transmit any personally identifiable information**. All data created by you (the user), such as app preferences (like enabling or disabling shortcuts), is stored locally in your device's private storage. This data can be erased at any time by clearing the app's data or uninstalling it. No analytics or tracking software is present in the app.

### Explanation of permissions requested in the app

The list of permissions required by the app can be found in the `AndroidManifest.xml` file. Below is an explanation for why each permission is necessary for the app to function.

| Permission | Why it is required |
| :---: | --- |
| `android.permission.SYSTEM_ALERT_WINDOW` | This is a core permission for the app's main feature. It allows CoverSpin to create an invisible overlay on the screen. This overlay is what forces the screen to rotate based on the device's sensors, even on the cover screen where rotation is normally locked by the system. |
| `android.permission.BIND_ACCESSIBILITY_SERVICE` | This permission is required **only** for the volume button shortcuts feature. The Accessibility Service listens for `KeyEvent`s from the physical volume buttons to detect single and double presses. **The service does not monitor, log, or store any text you type or any other content on your screen.** Its sole purpose is to provide a shortcut to enable/disable screen rotation. The app declares itself to the Android system as not being a primary accessibility tool (`isAccessibilityTool=\"false\"`). |
| `android.permission.FOREGROUND_SERVICE` & `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` | These permissions allow CoverSpin's rotation and key-listening services to run reliably in the background. This is necessary for the app to function when the cover screen is active and the main configuration app is not visible. |
| `android.permission.RECEIVE_BOOT_COMPLETED` | This allows CoverSpin to automatically restart its service when you reboot your device. This ensures you don't have to manually open the app and start the service every time your phone turns on. |

<hr style="border:1px solid gray">

If you have any questions or concerns regarding how the app protects your privacy, please feel free to send me an email.

Yours sincerely,  
Crispim.