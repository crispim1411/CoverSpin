package com.example.navigationsample

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 123)
        } else {
            iniciarEngine()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Settings.canDrawOverlays(this)) {
            iniciarEngine()
        } else {
            finish()
        }
    }

    private fun iniciarEngine() {
        val intent = Intent(this, EngineActivity::class.java)
        startActivity(intent)
        finish()
    }
}
