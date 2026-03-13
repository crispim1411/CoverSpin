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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.RotateRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (handleIntent(intent)) {
            finish()
            return
        }

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (handleIntent(intent)) {
            finish()
        }
    }

    private fun handleIntent(intent: Intent?): Boolean {
        return when (intent?.action) {
            "com.crispim.coverspin.ACTION_ENABLE_ROTATION" -> {
                EngineActivity.routineSetRotation(this, true)
                true
            }
            "com.crispim.coverspin.ACTION_DISABLE_ROTATION" -> {
                EngineActivity.routineSetRotation(this, false)
                true
            }
            else -> false
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsScreen() {
        val context = LocalContext.current
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        
        var showAccessibilityDialog by remember { mutableStateOf(false) }
        var hasAccessibility by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
        var hasOverlay by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

        val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
        var rotationMode by remember { 
            mutableStateOf(prefs.getString("rotation_mode", "AUTO") ?: "AUTO") 
        }
        var trackLogsEnabled by remember {
            mutableStateOf(prefs.getBoolean("track_logs", false))
        }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    hasAccessibility = isAccessibilityServiceEnabled(context)
                    hasOverlay = Settings.canDrawOverlays(context)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        if (showAccessibilityDialog) {
            AccessibilityDisclosureDialog(
                onConfirm = {
                    showAccessibilityDialog = false
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                },
                onDismiss = { showAccessibilityDialog = false }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Logo and Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.RotateRight,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "CoverSpin",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (!hasAccessibility || !hasOverlay) {
                StatusCard(
                    title = "Action Required",
                    message = "To enable the cover screen menu and unlock the app settings, please grant the permissions below.",
                    isError = true
                )

                PermissionItem(
                    title = "Accessibility Service",
                    description = "Required to display the floating menu button on the cover screen.",
                    isEnabled = hasAccessibility,
                    onClick = { showAccessibilityDialog = true }
                )

                PermissionItem(
                    title = "Display Over Other Apps",
                    description = "Required to draw the menu interface on the cover screen.",
                    isEnabled = hasOverlay,
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            "package:${context.packageName}".toUri()
                        )
                        context.startActivity(intent)
                    }
                )
            } else {
                StatusCard(
                    title = "Ready to Go!",
                    message = "Service is active. The menu button will appear on your cover screen.",
                    isError = false
                )

                TutorialsSection()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Gesture Button Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val modes = listOf("AUTO", "MANUAL", "OFF")
                            modes.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                                    onClick = { 
                                        rotationMode = mode
                                        EngineActivity.updateMode(context.applicationContext, mode)
                                    },
                                    selected = rotationMode == mode
                                ) {
                                    Text(mode.lowercase().replaceFirstChar { it.uppercase() })
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when(rotationMode) {
                                "OFF" -> "The gesture button is disabled."
                                "AUTO" -> "The button toggles between auto-rotation and locked mode."
                                else -> "The button appears when you tilt the device to suggest a rotation, similar to Android's system button."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Track Logs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Enable debug toasts for error tracking.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = trackLogsEnabled,
                            onCheckedChange = { enabled ->
                                trackLogsEnabled = enabled
                                EngineActivity.updateTrackLogs(context, enabled)
                            }
                        )
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
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    @Composable
    fun TutorialsSection() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Guides & Tutorials",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TutorialItem(
                    title = "Initial Setup",
                    videoId = "9zIniCLuvDE",
                    modifier = Modifier.weight(1f)
                )
                TutorialItem(
                    title = "Routine Buttons",
                    videoId = "XjAt4IQP1Dc",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    @Composable
    fun TutorialItem(title: String, videoId: String, modifier: Modifier = Modifier) {
        val context = LocalContext.current
        Card(
            modifier = modifier
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, "https://www.youtube.com/watch?v=$videoId".toUri())
                    context.startActivity(intent)
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                ) {
                    AsyncImage(
                        model = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )
            }
        }
    }

    @Composable
    fun StatusCard(title: String, message: String, isError: Boolean) {
        val bgColor = if (isError) Color(0xFFFFF9C4) else Color(0xFFC8E6C9)
        val contentColor = if (isError) Color(0xFFFBC02D) else Color(0xFF388E3C)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = bgColor)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isError) Icons.Rounded.Warning else Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, color = contentColor)
                    Text(text = message, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                }
            }
        }
    }

    @Composable
    fun PermissionItem(title: String, description: String, isEnabled: Boolean, onClick: () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isEnabled,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnabled) Color.Gray.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isEnabled) "Enabled" else "Configure")
                }
            }
        }
    }

    @Composable
    fun AccessibilityDisclosureDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Accessibility API Usage", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "CoverSpin uses the Accessibility Service to:\n\n" +
                    "• Detect the current screen state on the cover display.\n" +
                    "• Display the floating menu button over the system UI.\n\n" +
                    "We do not collect, store, or share any personal data."
                )
            },
            confirmButton = {
                Button(onClick = onConfirm) { Text("Enable") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        val componentName = ComponentName(context, UnlockService::class.java)
        val flatName = componentName.flattenToString()

        while (colonSplitter.hasNext()) {
            val component = colonSplitter.next()
            if (component.equals(flatName, ignoreCase = true)) return true
        }
        return false
    }
}
