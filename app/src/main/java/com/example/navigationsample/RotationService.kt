package com.example.navigationsample

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.IBinder

class RotationService : Service() {

    private var unlockReceiver: BroadcastReceiver? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        launchEngine(this)
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
                if (intent.action == Intent.ACTION_USER_PRESENT || intent.action == Intent.ACTION_SCREEN_ON) {
                    launchEngine(context)
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(unlockReceiver, filter)
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
