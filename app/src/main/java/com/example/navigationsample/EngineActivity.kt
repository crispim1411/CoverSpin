package com.example.navigationsample

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class EngineActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Limpeza visual
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setDimAmount(0f)        // 2. Configurações de Desbloqueio
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        val params = window.attributes

        // TRUQUE DE MESTRE: Foco + Tamanho Zero
        // Mantemos a janela ativa para o sistema (rotação funciona),
        // mas com tamanho 0, todo toque na tela é tecnicamente "fora" dela.
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0
        params.width = 0 // Tamanho zero
        params.height = 0 // Tamanho zero

        // FLAGS:
        // - Removemos NOT_FOCUSABLE: A janela TEM foco, então a rotação é respeitada.
        // - NOT_TOUCH_MODAL: Permite que toques fora da janela (ou seja, na tela toda) passem para trás.
        // - WATCH_OUTSIDE_TOUCH: Monitora os toques.
        params.flags = (
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                )

        // Alpha precisa ser > 0 para o WindowManager não descartar a janela como "morta"
        params.alpha = 0.01f

        window.attributes = params

        // View vazia
        val view = View(this)
        view.setBackgroundColor(Color.TRANSPARENT)
        setContentView(view)

        // Rotação
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // Desbloqueio
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }
    }

    // Adicione este método para garantir que, se algum toque milagrosamente cair na activity,
    // ele seja ignorado.
    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        // Retornar false permite que o sistema continue processando (ou feche a activity se configurado)
        // Mas aqui, queremos apenas que ele não consuma.
        return false
    }

    override fun onResume() {
        super.onResume()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }
}
