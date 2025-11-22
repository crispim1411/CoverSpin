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

        // 1. Garante que não haja UI visual
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setDimAmount(0f)

        // 2. Configurações para aparecer sobre o bloqueio
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        // 3. FLAGS CRUCIAIS
        // NOT_TOUCHABLE + NOT_FOCUSABLE = O toque passa direto para o app de baixo.
        // SHOW_WHEN_LOCKED = Aparece na Cover Screen.
        window.addFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or // Permite toque fora da janela
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        // Ajusta o tamanho para 1px no canto para minimizar impacto
        val params = window.attributes
        params.gravity = Gravity.TOP or Gravity.START
        params.width = 1
        params.height = 1
        params.alpha = 0.0f // Invisível
        window.attributes = params

        // 4. View Fantasma (Necessária para a Activity existir e segurar a rotação)
        val view = View(this)
        view.setBackgroundColor(Color.TRANSPARENT)
        setContentView(view)

        // 5. FORÇAR A ROTAÇÃO
        // Como esta Activity está no topo (mesmo invisível/intocável), ela dita a regra.
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR

        // 6. Desbloqueio
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }
    }

    override fun onResume() {
        super.onResume()
        // Reforça a rotação sempre que o app tenta ganhar foco (mesmo sem foco real)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    // Importante: Não chame finish() aqui. A Activity precisa ficar viva para manter a rotação.
}
