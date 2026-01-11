package com.crispim.coverspin

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.Display
import androidx.core.net.toUri

class MainActivity : Activity() {
    private lateinit var toastHelper: ToastHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toastHelper = ToastHelper(application)

        if (checkPermissions()) {
            startEngine()
        }
    }

    private fun checkPermissions() : Boolean {
        var hasOverlayPermission = true
        var hasAccessibilityPermission = true

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            )
            startActivity(intent)
            hasOverlayPermission = false
        }

        if (!hasAccessibilityPermission(application)) {
            showAccessibilityDisclosure()
            hasAccessibilityPermission = false
        }

        return hasOverlayPermission && hasAccessibilityPermission
    }

    private fun startEngine() {
        val displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager
        val isInnerScreen = displayManager.getDisplay(1)?.state == Display.STATE_ON
        if (!isInnerScreen) {
            toastHelper.show("Please open this app from the Cover Screen")
            finish()
            return
        }

        if (!EngineActivity.isOverlayActive) {
            EngineActivity.initialize(application)
        }
        finish()
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
    }

    private fun hasAccessibilityPermission(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        val componentName = ComponentName(context, UnlockService::class.java)
        val flatName = componentName.flattenToString()

        while (colonSplitter.hasNext()) {
            val component = colonSplitter.next()
            if (component.equals(flatName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun showAccessibilityDisclosure() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Accessibility API Usage")
        builder.setMessage(
            "This app requires the Accessibility Service API to provide its core functionality: " +
                    "Managing screen rotation and system events on the Cover Screen.\n\n" +
                    "• We DO NOT collect or share any personal data.\n" +
                    "• You can disable this at any time in settings."
        )

        builder.setPositiveButton("Accept and Enable") { _, _ ->
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        builder.setNegativeButton("Decline") { _, _ ->
            finish()
        }

        builder.setCancelable(false)
        builder.show()
    }
}
