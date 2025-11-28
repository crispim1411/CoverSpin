package com.crispim.coverspin

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.core.net.toUri

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Verifica Permissão de Sobreposição (Overlay)
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Por favor, permita a sobreposição.", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            )
            startActivityForResult(intent, 101)
            return
        }

        // 2. Verifica se o Serviço de Acessibilidade está ativo
        if (!isAccessibilityServiceEnabled(this, KeepAliveService::class.java)) {
            Toast.makeText(this, "Por favor, ative o serviço de Acessibilidade para o CoverSpin.", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityForResult(intent, 102)
            return
        }

        // 3. Se tudo estiver ok, inicia o motor e fecha a tela de config
        startEngine()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Ao voltar das configurações, verificamos tudo de novo chamando onCreate ou a lógica direta
        if (Settings.canDrawOverlays(this) && isAccessibilityServiceEnabled(this, KeepAliveService::class.java)) {
            startEngine()
        } else {
            // Se o usuário voltou sem dar permissão, mantemos a tela aberta ou fechamos (opcional)
            // Aqui optamos por reiniciar a verificação para insistir
            recreate()
        }
    }

    private fun startEngine() {
        // Inicia a Activity transparente do motor
        val intent = Intent(this, EngineActivity::class.java)
        startActivity(intent)

        // Remove animação e fecha
        finish()
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    // Função auxiliar para verificar se a Acessibilidade está ativa
    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        val componentName = ComponentName(context, service)
        val flatName = componentName.flattenToString()

        while (colonSplitter.hasNext()) {
            val component = colonSplitter.next()
            if (component.equals(flatName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
