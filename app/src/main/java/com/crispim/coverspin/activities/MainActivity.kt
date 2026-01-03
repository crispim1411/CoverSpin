package com.crispim.coverspin.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.crispim.coverspin.SettingsViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

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
                    SettingsScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    DisposableEffect(LocalLifecycleOwner.current) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        (context as LifecycleOwner).lifecycle.addObserver(observer)
        onDispose {
            (context as LifecycleOwner).lifecycle.removeObserver(observer)
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

        val allPermissionsGranted = uiState.hasOverlayPermission && uiState.hasAccessibilityPermission
        if (!allPermissionsGranted) {
            WarningCard(
                title = "Warning",
                message = "For the app to function correctly, please grant the required permissions."
            )
        }

        WarningCard(
            title = "Warning",
            message = "If the screen does not rotate, add the app to GoodLock and try again."
        )

        if (!uiState.hasOverlayPermission || !uiState.hasAccessibilityPermission) {
            InfoCard(title = "Required Permissions") {
                if (!uiState.hasOverlayPermission) {
                    ConfigButton(
                        text = "Grant Overlay Permission",
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                "package:${context.packageName}".toUri()
                            )
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (!uiState.hasAccessibilityPermission) {
                    ConfigButton(
                        text = "Enable Accessibility Service",
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        InfoCard(title = "Settings") {
            Column {
                SettingRowSwitch(
                    title = "Volume Key Shortcut",
                    subtitle = "Double press volume down to toggle rotation",
                    checked = uiState.volumeShortcutsEnabled,
                    onCheckedChange = { viewModel.onVolumeShortcutsEnabledChange(it) },
                    enabled = allPermissionsGranted
                )

                SettingDivider()

                SettingRowSwitch(
                    title = "Gesture Button",
                    subtitle = "Show a floating button to toggle rotation",
                    checked = uiState.isGestureButtonEnabled,
                    onCheckedChange = { viewModel.onGestureButtonEnabledChange(context, it) },
                    enabled = allPermissionsGranted
                )

                SettingDivider()

                SettingRowSwitch(
                    title = "Show debug messages",
                    subtitle = "Display on-screen action logs (applies on device restart)",
                    checked = uiState.debugMessagesEnabled,
                    onCheckedChange = { viewModel.onDebugMessagesEnabledChange(it) },
                    enabled = allPermissionsGranted
                )

                SettingDivider()

                SettingRowSwitch(
                    title = "Keep screen on",
                    subtitle = "Keeps the screen on indefinitely (applies on device restart)",
                    checked = uiState.keepScreenOn,
                    onCheckedChange = { viewModel.onKeepScreenOnChange(it) },
                    enabled = allPermissionsGranted
                )

                SettingDivider()

                Button(
                    onClick = { viewModel.onStartEngine() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = allPermissionsGranted && !uiState.isInnerScreen,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "Start")
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
