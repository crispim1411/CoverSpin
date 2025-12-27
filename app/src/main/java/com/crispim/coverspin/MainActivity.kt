package com.crispim.coverspin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val primaryColor = Color(0xFF0D47A1)
        val secondaryColor = Color(0xFF90CAF9)
        val backgroundColor = Color(0xFFE3F2FD)
        val surfaceColor = Color.White

        val customColorScheme = lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            secondary = secondaryColor,
            onSecondary = Color.Black,
            background = backgroundColor,
            surface = surfaceColor,
            onSurface = Color.Black
        )

        setContent {
            MaterialTheme(colorScheme = customColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsScreen()
                }
            }
        }
    }

    @Composable
    fun SettingsScreen() {
        val context = LocalContext.current
        val sharedPrefs = context.getSharedPreferences("CoverSpin", Context.MODE_PRIVATE)

        var volumeShortcutsEnabled by remember {
            mutableStateOf(sharedPrefs.getBoolean(Constants.PREF_KEY_VOLUME_SHORTCUTS, true))
        }
        var clickDelay by remember {
            mutableStateOf(sharedPrefs.getInt(Constants.PREF_KEY_CLICK_DELAY, Constants.DEFAULT_CLICK_DELAY_MS).toFloat())
        }
        var isRotationEnabled by remember { mutableStateOf(EngineActivity.loadUserPrefRotation(context)) }
        var isEngineRunning by remember { mutableStateOf(EngineActivity.isOverlayActive) }
        var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
        var hasAccessibilityPermission by remember { mutableStateOf(isAccessibilityServiceEnabled(context, EventsService::class.java)) }

        DisposableEffect(context as LifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    isEngineRunning = EngineActivity.isOverlayActive
                    hasOverlayPermission = Settings.canDrawOverlays(context)
                    hasAccessibilityPermission = isAccessibilityServiceEnabled(context, EventsService::class.java)
                    isRotationEnabled = EngineActivity.loadUserPrefRotation(context)
                    volumeShortcutsEnabled = sharedPrefs.getBoolean(Constants.PREF_KEY_VOLUME_SHORTCUTS, true)
                    clickDelay = sharedPrefs.getInt(Constants.PREF_KEY_CLICK_DELAY, Constants.DEFAULT_CLICK_DELAY_MS).toFloat()
                }
            }
            context.lifecycle.addObserver(observer)
            onDispose {
                context.lifecycle.removeObserver(observer)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ScreenRotation,
                        contentDescription = "Logo",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "CoverSpin",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            val allPermissionsGranted = hasOverlayPermission && hasAccessibilityPermission
            if (!allPermissionsGranted) {
                WarningCard(
                    title = "Warning",
                    message = "For the app to function correctly, please grant the required permissions."
                )
            }

            if (!hasOverlayPermission || !hasAccessibilityPermission) {
                InfoCard(title = "Required Permissions") {
                    if (!hasOverlayPermission) {
                        ConfigButton(
                            text = "Grant Overlay Permission",
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    "package:$packageName".toUri()
                                )
                                startActivity(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (!hasAccessibilityPermission) {
                        ConfigButton(
                            text = "Enable Accessibility Service",
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                startActivity(intent)
                            }
                        )
                    }
                }
            }

            if (allPermissionsGranted) {
                InfoCard(title = "Settings") {
                    Column {
                        SettingRowSwitch(
                            title = "Auto-rotation Active",
                            subtitle = "Enables cover screen auto-rotation",
                            checked = isRotationEnabled,
                            onCheckedChange = {
                                isRotationEnabled = it
                                EngineActivity.setNewUserPrefRotation(context, it)
                                if (EngineActivity.isOverlayActive) {
                                    EngineActivity.setRotationEnabled(it)
                                }
                            },
                            enabled = isEngineRunning
                        )

                        SettingDivider()

                        SettingRowSwitch(
                            title = "Volume Key Shortcut",
                            subtitle = "Double press volume down to enable/disable auto-rotation",
                            checked = volumeShortcutsEnabled,
                            onCheckedChange = {
                                volumeShortcutsEnabled = it
                                sharedPrefs.edit { putBoolean(Constants.PREF_KEY_VOLUME_SHORTCUTS, it) }
                            }
                        )

                        SettingDivider()

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Double-Press Delay",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${clickDelay.toInt()} ms",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = clickDelay,
                                onValueChange = { clickDelay = it },
                                valueRange = 200f..400f,
                                steps = 3,
                                onValueChangeFinished = {
                                    sharedPrefs.edit { putInt(Constants.PREF_KEY_CLICK_DELAY, clickDelay.toInt()) }
                                }
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    val url = "https://www.paypal.com/paypalme/crispim1411"
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(text = "Buy me an Ice Cream ($1)")
            }

            OutlinedButton(
                onClick = {
                    val url = "https://github.com/crispim1411/CoverSpin/issues"
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(text = "A Bug? Report it!")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    
    @Composable
    fun SettingDivider(){
        Spacer (modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray.copy(alpha = 0.3f)))
        Spacer(modifier = Modifier.height(12.dp))
    }
    
    @Composable
    fun SettingRowSwitch(
        title: String,
        subtitle: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        enabled: Boolean = true
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.secondary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                    uncheckedTrackColor = Color.Gray
                )
            )
        }
    }

    @Composable
    fun InfoCard(title: String, content: @Composable () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                content()
            }
        }
    }

    @Composable
    fun ConfigButton(text: String, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(text)
        }
    }

    @Composable
    fun WarningCard(title: String, message: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Rounded.Info, contentDescription = "Warning", tint = Color(0xFFFBC02D))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFFBC02D),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )
            }
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
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
