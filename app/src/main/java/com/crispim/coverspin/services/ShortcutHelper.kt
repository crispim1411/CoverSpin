package com.crispim.coverspin.services

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import com.crispim.coverspin.Constants
import com.crispim.coverspin.activities.MainActivity
import com.crispim.coverspin.models.RotationMode

@RequiresApi(Build.VERSION_CODES.N_MR1)
class ShortcutHelper(private val context: Context) {

    fun createShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            val shortcuts = listOf(
                createToggleShortcut(),
                createPortraitShortcut(),
                createLandscapeShortcut(),
                createSettingsShortcut()
            )
            shortcutManager.dynamicShortcuts = shortcuts
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createToggleShortcut(): ShortcutInfo {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("action", "toggle")
        }
        return ShortcutInfo.Builder(context, "toggle_rotation")
            .setShortLabel("Toggle Rotation")
            .setLongLabel("Toggle Auto Rotation")
            .setIcon(Icon.createWithResource(context, android.R.drawable.ic_menu_rotate))
            .setIntent(intent)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createPortraitShortcut(): ShortcutInfo {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("action", "portrait")
        }
        return ShortcutInfo.Builder(context, "portrait_lock")
            .setShortLabel("Portrait")
            .setLongLabel("Lock to Portrait")
            .setIcon(Icon.createWithResource(context, android.R.drawable.ic_menu_revert))
            .setIntent(intent)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createLandscapeShortcut(): ShortcutInfo {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("action", "landscape")
        }
        return ShortcutInfo.Builder(context, "landscape_lock")
            .setShortLabel("Landscape")
            .setLongLabel("Lock to Landscape")
            .setIcon(Icon.createWithResource(context, android.R.drawable.ic_menu_rotate))
            .setIntent(intent)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createSettingsShortcut(): ShortcutInfo {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("action", "settings")
        }
        return ShortcutInfo.Builder(context, "settings")
            .setShortLabel("Settings")
            .setLongLabel("CoverSpin Settings")
            .setIcon(Icon.createWithResource(context, android.R.drawable.ic_menu_preferences))
            .setIntent(intent)
            .build()
    }
}

