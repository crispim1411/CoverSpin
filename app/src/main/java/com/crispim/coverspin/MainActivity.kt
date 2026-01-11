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
        } else {
            toastHelper.show("Missing permissions")
        }
    }

    override fun onResume() {
        super.onResume()
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
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
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
}
