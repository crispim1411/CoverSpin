package com.example.navigationsample

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager

class EngineActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Configurações de Janela (Replica o comportamento do app referência)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        // Flags essenciais para manter a tela ligada e permitir o desbloqueio
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        // 2. Solicitação agressiva de desbloqueio (Cópia exata da lógica decompilada)
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissError() {
                    super.onDismissError()
                    // Opcional: logar erro
                }

                override fun onDismissSucceeded() {
                    super.onDismissSucceeded()
                    // Sucesso no desbloqueio
                }

                override fun onDismissCancelled() {
                    super.onDismissCancelled()
                    // O usuário cancelou
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()

        // 3. Lógica inteligente de Display (Extraído do código decompilado)
        // Se o display ID for 0, significa que é a tela principal (interna).
        // Nesse caso, o app fecha essa activity transparente para não atrapalhar o uso normal.
        // A Cover Screen geralmente tem ID diferente de 0.
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display
        } else {
            windowManager.defaultDisplay
        }

        if (display != null && display.displayId == 0) {
            // Estamos na tela interna, não precisamos forçar rotação aqui.
            finishAndRemoveTask()
        }

        // Nota: A rotação "sensor" já está definida no AndroidManifest.xml
        // na tag <activity android:screenOrientation="sensor">, então não precisa repetir aqui.
    }
}
