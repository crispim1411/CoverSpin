package com.example.navigationsample

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class RotationService : Service() {

    private var unlockReceiver: BroadcastReceiver? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotif()

        // Lança a Engine imediatamente
        launchEngine(this)

        registerUnlockReceiver()
    }

    private fun registerUnlockReceiver() {
        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_USER_PRESENT || intent.action == Intent.ACTION_SCREEN_ON) {
                    // Reaplica a Engine para garantir rotação e desbloqueio
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
        // REORDER_TO_FRONT traz a activity existente para o topo sem recriar
        engineIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        // Remove animação para ser imperceptível
        engineIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

        context.startActivity(engineIntent)
    }

    private fun startForegroundServiceNotif() {
        val channelId = "rotation_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rotation Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Rotação Ativa")
            .setContentText("Controlando orientação da tela")
            .setSmallIcon(android.R.drawable.ic_menu_rotate)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (unlockReceiver != null) unregisterReceiver(unlockReceiver)
    }
}
