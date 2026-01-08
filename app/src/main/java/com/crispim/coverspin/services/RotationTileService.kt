package com.crispim.coverspin.services

import android.annotation.TargetApi
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.crispim.coverspin.Constants
import com.crispim.coverspin.activities.EngineActivity
import com.crispim.coverspin.models.RotationMode

@TargetApi(Build.VERSION_CODES.N)
class RotationTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        toggleRotation()
        updateTile()
    }

    private fun toggleRotation() {
        val cacheHelper = CacheHelper(getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE))
        val currentMode = RotationMode.fromInt(
            cacheHelper.getRotationMode()
        )
        
        val nextMode = when (currentMode) {
            RotationMode.AUTO -> RotationMode.PORTRAIT
            RotationMode.PORTRAIT -> RotationMode.LANDSCAPE
            RotationMode.LANDSCAPE -> RotationMode.AUTO
            RotationMode.SENSOR -> RotationMode.AUTO
            RotationMode.LOCKED -> RotationMode.AUTO
        }
        
        cacheHelper.setRotationMode(nextMode.value)
        
        // Apply rotation if engine is running
        if (EngineActivity.isOverlayActive) {
            applyRotationMode(nextMode)
        } else {
            // Start engine if not running
            EngineActivity.initialize(this)
        }
    }

    private fun applyRotationMode(mode: RotationMode) {
        when (mode) {
            RotationMode.AUTO, RotationMode.SENSOR -> {
                EngineActivity.setRotationEnabled(true)
            }
            RotationMode.PORTRAIT -> {
                // Lock to portrait - would need additional implementation
                EngineActivity.setRotationEnabled(false)
            }
            RotationMode.LANDSCAPE -> {
                // Lock to landscape - would need additional implementation
                EngineActivity.setRotationEnabled(false)
            }
            RotationMode.LOCKED -> {
                EngineActivity.setRotationEnabled(false)
            }
        }
    }

    private fun updateTile() {
        val cacheHelper = CacheHelper(getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE))
        val currentMode = RotationMode.fromInt(cacheHelper.getRotationMode())
        val isRunning = EngineActivity.isOverlayActive
        
        qsTile?.apply {
            state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = "CoverSpin: ${currentMode.displayName}"
            contentDescription = "CoverSpin rotation control"
            
            // Set icon based on mode
            icon = Icon.createWithResource(
                this@RotationTileService,
                when (currentMode) {
                    RotationMode.AUTO -> android.R.drawable.ic_menu_rotate
                    RotationMode.PORTRAIT -> android.R.drawable.ic_menu_revert
                    RotationMode.LANDSCAPE -> android.R.drawable.ic_menu_rotate
                    RotationMode.SENSOR -> android.R.drawable.ic_menu_rotate
                    RotationMode.LOCKED -> android.R.drawable.ic_lock_idle_lock
                }
            )
            
            updateTile()
        }
    }
}

