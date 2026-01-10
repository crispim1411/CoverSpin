package com.crispim.coverspin

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.IBinder
import com.crispim.coverspin.services.ToastHelper

class RotationService : Service() {

    private var unlockReceiver: BroadcastReceiver? = null
    private lateinit var toastHelper: ToastHelper

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        toastHelper = ToastHelper(application)
        registerUnlockReceiver()
    }

    // Adicionado: Monitora mudanças de configuração (Abrir/Fechar aparelho)
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        launchEngine(this)
    }

    private fun registerUnlockReceiver() {
        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    if (intent.action == Intent.ACTION_USER_PRESENT || intent.action == Intent.ACTION_SCREEN_ON) {
                        launchEngine(context)
                        toastHelper.show("screen on!")
                    }
                } catch (e: Exception) {
                    toastHelper.show("unlock error: ")
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(unlockReceiver, filter)
        toastHelper.show("events service started!")
    }

    private fun launchEngine(context: Context) {
        val engineIntent = Intent(context, EngineActivity::class.java)
        engineIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(engineIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (unlockReceiver != null) unregisterReceiver(unlockReceiver)
    }
}
