package com.example.navigationsample

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class RotationService : Service() {

    private var windowManager: WindowManager? = null
    private var orientationView: View? = null
    private var unlockReceiver: BroadcastReceiver? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotif()

        // 1. Registra o Receiver para abrir o app ao desbloquear
        registerUnlockReceiver()

        // 2. Inicializa o WindowManager e força a rotação imediatamente
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        //addOrientationOverlay()
    }


    private fun registerUnlockReceiver() {
        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (Intent.ACTION_USER_PRESENT == intent.action) {

                    // 1. Abre sua App Principal (Conteúdo)
                    val mainIntent = Intent(context, MainActivity::class.java)
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    context.startActivity(mainIntent)

                    // 2. Abre a Engine Transparente (Para destravar a rotação)
                    val engineIntent = Intent(context, EngineActivity::class.java)
                    engineIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    // MULTIPLE_TASK garante que ela rode independente da main
                    engineIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    context.startActivity(engineIntent)
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(unlockReceiver, filter)
    }

    private fun startForegroundServiceNotif() {
        val channelId = "rotation_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Cover Screen Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Modo Cover Screen Ativo")
            .setContentText("Forçando Landscape e aguardando desbloqueio")
            .setSmallIcon(android.R.drawable.ic_menu_rotate)
            .build()

        startForeground(1, notification)
    }

//    private fun addOrientationOverlay() {
//        if (orientationView != null) return
//
//        orientationView = View(this)
//
//        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
//        } else {
//            WindowManager.LayoutParams.TYPE_PHONE
//        }
//
//        val params = WindowManager.LayoutParams(
//            // ALTERAÇÃO 1: Usar 1px em vez de 0. O sistema pode ignorar views de tamanho 0 para rotação.
//            1, 1,
//            layoutType,
//            // Flags de permissão e comportamento
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
//                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
//                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
//                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or // Permite desenhar fora dos limites padrão
//                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
//                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
//                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, // Mantém a janela "acordada" para eventos
//            PixelFormat.TRANSLUCENT
//        )
//
//        // Move a view para um canto onde não atrapalhe visualmente (embora seja transparente)
//        params.gravity = android.view.Gravity.TOP or android.view.Gravity.START
//
//        // Força Landscape
//        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//
//        try {
//            windowManager?.addView(orientationView, params)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpeza: Remove o receiver e a rotação forçada ao parar o serviço
        if (unlockReceiver != null) {
            unregisterReceiver(unlockReceiver)
        }
        if (orientationView != null) {
            windowManager?.removeView(orientationView)
            orientationView = null
        }
    }
}
