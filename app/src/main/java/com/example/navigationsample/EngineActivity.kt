package com.example.navigationsample

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class EngineActivity : Activity() {

    private var overlayView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Configurações de Desbloqueio (Activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        // Tenta desbloquear imediatamente
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }

        // 2. Criar e Adicionar o Overlay Flutuante
        addRotationOverlay()

        // 3. Sair da frente visualmente
        // Isso devolve o foco para o YouTube/Launcher, reativando animações e toques,
        // mas nosso Overlay continua lá forçando a rotação.
        moveTaskToBack(true)
    }

    private fun addRotationOverlay() {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Criação da View Invisível
        overlayView = View(this)

        // Configuração dos Parâmetros da Janela Flutuante
        val params = WindowManager.LayoutParams(
            // Tamanho 0 ou 1 pixel é suficiente para overlays de sistema ditar rotação
            // se tiverem a flag certa. Teste com MATCH_PARENT se 0 falhar.
            0, 0,

            // Tipo de Janela: TYPE_APPLICATION_OVERLAY (Android O+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,

            // FLAGS:
            // NOT_FOCUSABLE: O toque passa para o app de baixo.
            // NOT_TOUCH_MODAL: Garante que cliques fora não sejam bloqueados.
            // WATCH_OUTSIDE_TOUCH: Monitora eventos.
            // SHOW_WHEN_LOCKED: Aparece na capa.
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,

            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START

        // O SEGREDO: Aplicar a orientação NESTA janela flutuante
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpeza ao fechar o app
        if (overlayView != null) {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
