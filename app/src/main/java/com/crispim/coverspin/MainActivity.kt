package com.crispim.coverspin

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.core.net.toUri
import com.crispim.coverspin.services.ToastHelper

class MainActivity : Activity() {

    private lateinit var toastHelper: ToastHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toastHelper = ToastHelper(application)

        if (checkPermissions()) {
            toastHelper.show("Initializing")
            startEventsService()
            startEngine()
        } else {
            toastHelper.show("Waiting permissions")
        }
    }

    override fun onResume() {
        super.onResume()
        // Sempre verificamos ao voltar para o app
        if (checkPermissions()) {
            toastHelper.show("Initializing")
            startEventsService()
            startEngine()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (checkPermissions()) {
            toastHelper.show("Starting")
            startEngine()
            startEventsService()
        } else {
            finish()
        }
    }

    private fun checkPermissions() : Boolean {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            )
            startActivity(intent)
            return false
        }
        if (!hasAccessibilityPermission(application)) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            return false
        }
        return true
    }

    private fun startEngine() {
        val intent = Intent(this, EngineActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startEventsService() {
        val serviceIntent = Intent(this, RotationService::class.java)
        startForegroundService(serviceIntent)
    }

    private fun hasAccessibilityPermission(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        val componentName = ComponentName(context, RotationService::class.java)
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
