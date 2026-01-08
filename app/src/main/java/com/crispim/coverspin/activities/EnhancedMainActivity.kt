package com.crispim.coverspin.activities

import android.content.Intent
import android.os.Build
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.crispim.coverspin.Constants
import com.crispim.coverspin.SettingsViewModel
import com.crispim.coverspin.models.*
import com.crispim.coverspin.services.BatteryTracker
import com.crispim.coverspin.services.ShortcutHelper

class EnhancedMainActivity : ComponentActivity() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle shortcuts
        handleShortcutIntent(intent)

        // Create shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutHelper(this).createShortcuts()
        }

        setContent {
            MaterialTheme(
                colorScheme = dynamicColorScheme(),
                typography = Typography()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EnhancedSettingsScreen(viewModel)
                }
            }
        }
    }

    private fun handleShortcutIntent(intent: Intent?) {
        when (intent?.getStringExtra("action")) {
            "toggle" -> viewModel.onStartEngine()
            "portrait" -> viewModel.setRotationMode(RotationMode.PORTRAIT)
            "landscape" -> viewModel.setRotationMode(RotationMode.LANDSCAPE)
            "settings" -> { /* Already in settings */ }
        }
    }

    @Composable
    private fun dynamicColorScheme(): ColorScheme {
        // Use system default or light theme for now
        // Can be enhanced later with proper dark mode detection
        return lightColorScheme(
            primary = Color(0xFF0D47A1),
            secondary = Color(0xFF90CAF9),
            background = Color(0xFFE3F2FD),
            surface = Color(0xFFFFFFFF),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFF000000),
            onBackground = Color(0xFF000000),
            onSurface = Color(0xFF000000)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showStopDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }

    DisposableEffect(context as LifecycleOwner) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Constants.APP_NAME) },
                actions = {
                    IconButton(onClick = { 
                        val intent = Intent(context, DebugActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.BugReport, "Debug")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Service Status Card
            ServiceStatusCard(uiState)

            // Quick Actions
            QuickActionsCard(uiState, viewModel, context, showStopDialog) { 
                showStopDialog = it 
            }

            // Rotation Settings
            RotationSettingsCard(uiState, viewModel, context)

            // Animation Settings
            AnimationSettingsCard(uiState, viewModel)

            // Gesture Settings
            GestureSettingsCard(uiState, viewModel)

            // Battery Optimization
            BatteryOptimizationCard(uiState, viewModel, context, showBatteryDialog) {
                showBatteryDialog = it
            }

            // Advanced Settings
            AdvancedSettingsCard(uiState, viewModel, context)

            // Permissions Warning
            if (!uiState.hasOverlayPermission || !uiState.hasAccessibilityPermission) {
                PermissionsCard(uiState, context)
            }
        }
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("Stop Service?") },
            text = { Text("Do you want to stop the service?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onStopEngine()
                    showStopDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun ServiceStatusCard(uiState: SettingsState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (uiState.isEngineRunning) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Service Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (uiState.isEngineRunning) "● Active" else "○ Inactive",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (uiState.isEngineRunning) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                if (uiState.isEngineRunning) Icons.Default.PlayArrow else Icons.Default.Stop,
                contentDescription = "Status",
                tint = if (uiState.isEngineRunning) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuickActionsCard(
    uiState: SettingsState,
    viewModel: SettingsViewModel,
    context: android.content.Context,
    showStopDialog: Boolean,
    onShowStopDialog: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Button(
                onClick = {
                    if (uiState.isEngineRunning) {
                        onShowStopDialog(true)
                    } else {
                        viewModel.onStartEngine()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.hasOverlayPermission && uiState.hasAccessibilityPermission && !uiState.isInnerScreen
            ) {
                Text(if (uiState.isEngineRunning) "Stop Service" else "Start Service")
            }
        }
    }
}

@Composable
fun RotationSettingsCard(uiState: SettingsState, viewModel: SettingsViewModel, context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Rotation Settings",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            SettingRowDropdown<RotationMode>(
                title = "Rotation Mode",
                subtitle = "Current: ${uiState.rotationMode.displayName}",
                options = RotationMode.entries.toList(),
                selectedOption = uiState.rotationMode,
                onOptionSelected = { viewModel.setRotationMode(it) },
                enabled = true
            )
            SettingDivider()
            SettingRowSwitch(
                title = "Gesture Button",
                subtitle = "Show floating button to toggle rotation",
                checked = uiState.isGestureButtonEnabled,
                onCheckedChange = { viewModel.onGestureButtonEnabledChange(context, it) },
                enabled = true
            )
        }
    }
}

@Composable
fun AnimationSettingsCard(uiState: SettingsState, viewModel: SettingsViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Rotation Animations",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            SettingRowDropdown<AnimationType>(
                title = "Animation Type",
                subtitle = "Current: ${uiState.animationType.displayName}",
                options = AnimationType.entries.toList(),
                selectedOption = uiState.animationType,
                onOptionSelected = { viewModel.setAnimationType(it) },
                enabled = true
            )
            SettingDivider()
            SettingRowSwitch(
                title = "Haptic Feedback",
                subtitle = "Vibrate on rotation",
                checked = uiState.hapticFeedbackEnabled,
                onCheckedChange = { viewModel.setHapticFeedbackEnabled(it) },
                enabled = true
            )
        }
    }
}

@Composable
fun GestureSettingsCard(uiState: SettingsState, viewModel: SettingsViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Gesture Controls",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            SettingRowSwitch(
                title = "Enable Gestures",
                subtitle = "Use swipe gestures to control rotation",
                checked = uiState.gesturesEnabled,
                onCheckedChange = { viewModel.setGesturesEnabled(it) },
                enabled = true
            )
        }
    }
}

@Composable
fun BatteryOptimizationCard(
    uiState: SettingsState,
    viewModel: SettingsViewModel,
    context: android.content.Context,
    showBatteryDialog: Boolean,
    onShowBatteryDialog: (Boolean) -> Unit
) {
    val batteryTracker = remember { BatteryTracker(context) }
    val batteryStats = remember { batteryTracker.getEstimatedBatteryUsage() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Battery Optimization",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                "Last 24h: ${batteryStats.last24Hours}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Average per day: ${batteryStats.averagePerDay}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { onShowBatteryDialog(true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Battery Settings")
            }
        }
    }
}

@Composable
fun AdvancedSettingsCard(uiState: SettingsState, viewModel: SettingsViewModel, context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Advanced Settings",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            SettingRowSwitch(
                title = "Auto-start on Boot",
                subtitle = "Start service automatically after reboot",
                checked = uiState.autoStartOnBoot,
                onCheckedChange = { viewModel.setAutoStartOnBoot(it) },
                enabled = true
            )
            SettingDivider()
            SettingRowSwitch(
                title = "Keep Screen On",
                subtitle = "Prevent screen timeout",
                checked = uiState.keepScreenOn,
                onCheckedChange = { viewModel.onKeepScreenOnChange(it) },
                enabled = true
            )
            SettingDivider()
            SettingRowDropdown<LogLevel>(
                title = "Log Level",
                subtitle = "Controls on-screen messages",
                options = LogLevel.entries.toList(),
                selectedOption = uiState.logLevel,
                onOptionSelected = { viewModel.onLogLevelChange(it) },
                enabled = true
            )
        }
    }
}

@Composable
fun PermissionsCard(uiState: SettingsState, context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Required Permissions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            if (!uiState.hasOverlayPermission) {
                Button(
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            "package:${context.packageName}".toUri()
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Overlay Permission")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (!uiState.hasAccessibilityPermission) {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enable Accessibility Service")
                }
            }
        }
    }
}

// Reuse existing composables from MainActivity - they're already defined there


