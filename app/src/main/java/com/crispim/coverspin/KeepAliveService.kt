package com.crispim.coverspin

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder

class KeepAliveService : Service() {

    companion object {
        private const val CHANNEL_ID = "CoverSpinKeepAlive"
        private const val NOTIFICATION_ID = 42
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Cria e exibe a notificação obrigatória para Foreground Service
        startForegroundProtection()

        // 2. Garante que o overlay esteja ativo
        // Se a EngineActivity ainda não ativou o overlay, iniciamos ela.
        if (!EngineActivity.isOverlayActive) {
            try {
                val engineIntent = Intent(this, EngineActivity::class.java)
                engineIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                engineIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(engineIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // START_STICKY: Diz ao Android para recriar o serviço se ele for morto por falta de memória
        return START_STICKY
    }

    private fun startForegroundProtection() {
        // Cria o canal de notificação (Obrigatório no Android 8+)
        createNotificationChannel()

        // Cria uma Intent para abrir o app se clicar na notificação
        val notificationIntent = Intent(this, EngineActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("CoverSpin Ativo")
            .setContentText("Mantendo rotação da capa ativa")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Impede que o usuário remova deslizando
            .build()

        // Inicia o serviço em primeiro plano assumindo Android 14+ (Upside Down Cake)
        try {
            // 'specialUse' é o tipo ideal para overlays e utilitários de sistema
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } catch (e: Exception) {
            // Fallback apenas se houver erro específico de permissão/tipo,
            // mas ainda tentando iniciar foreground
            e.printStackTrace()
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "CoverSpin Service",
            NotificationManager.IMPORTANCE_MIN // IMPORTANCE_MIN deixa a notificação "silenciosa" e recolhida
        )
        serviceChannel.description = "Mantém o serviço de rotação ativo"
        serviceChannel.setShowBadge(false)

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Lógica de "imortalidade": Se o usuário limpar os recentes,
        // agendamos um reinício imediato via AlarmManager.

        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)

        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext,
            1,
            restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmService = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

        // Reinicia em 1 segundo
        alarmService.set(
            android.app.AlarmManager.ELAPSED_REALTIME,
            android.os.SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )

        super.onTaskRemoved(rootIntent)
    }
}
