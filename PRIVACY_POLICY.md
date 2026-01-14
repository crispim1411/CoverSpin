### CoverSpin: Privacy Policy

Welcome to the CoverSpin app for Android!

This is a utility app designed to provide screen rotation control on the cover screens of foldable devices. As the developer, I take your privacy very seriously. I have not programmed this app to collect any personally identifiable information.

---

### 🚨 Prominent Disclosure Regarding Accessibility Services

To provide its core functionality (reliable screen rotation on the cover screen), CoverSpin uses the **Android Accessibility API**.

**What this means for you:**
*   **Purpose:** CoverSpin uses the Accessibility Service to ensure the main rotation engine remains active and stable in the background. Modern Android versions can be aggressive in closing background services to save battery; using this API provides a robust way for the app to function even when not in the foreground.
*   **Action:** The service does not perform any gestures on your behalf, does not interact with other applications, and does not read or monitor any content on your screen. It is used strictly as a stability mechanism for the app's background process.
*   **Data Collection & Sharing:** CoverSpin **does not collect, store, or share** any personal or sensitive data through the Accessibility API. We do not monitor your screen content, log your keystrokes, or track your notifications.
*   **User Consent:** This service is only activated if you explicitly grant permission in your device's Accessibility settings. You can revoke this permission at any time.

---

### Data collected by the app

I hereby state that CoverSpin **does not collect, store, or transmit any personally identifiable information**. All data created by you (the user), such as your app preferences, is stored locally in your device's private storage. This data can be erased at any time by clearing the app's data or uninstalling it. No analytics or tracking software is present in the app.

### Explanation of permissions requested in the app

The list of permissions required by the app can be found in the `AndroidManifest.xml` file. Below is an explanation for why each permission is necessary for the app to function.

| Permission | Why it is required |
| :---: | --- |
| `android.permission.SYSTEM_ALERT_WINDOW` | This is a core permission for the app's main feature. It allows CoverSpin to create an invisible overlay on the screen. This overlay is what forces the screen to rotate based on the device's sensors, even on the cover screen where rotation is normally locked by the system. |
| `android.permission.BIND_ACCESSIBILITY_SERVICE` | Required to maintain the lifecycle and stability of the rotation engine in the background, preventing the system from closing the service unexpectedly. |
| `android.permission.FOREGROUND_SERVICE` & `android.permission.FOREGROUND_SERVICE_SPECIAL_USE` | These permissions allow CoverSpin's rotation service to run reliably in the background. This is necessary for the app to function when the cover screen is active and the main configuration app is not visible. |
| `android.permission.RECEIVE_BOOT_COMPLETED` | This allows CoverSpin to automatically restart its service when you reboot your device. This ensures you don't have to manually open the app and start the service every time your phone turns on. |

<hr style="border:1px solid gray">

If you have any questions or concerns regarding how the app protects your privacy, please feel free to send me an email.

Yours sincerely,  
Crispim.
