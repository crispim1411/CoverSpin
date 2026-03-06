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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

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
        val lifecycleOwner = LocalLifecycleOwner.current
        
        var showAccessibilityDialog by remember { mutableStateOf(false) }
        var hasAccessibility by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
        var hasOverlay by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

        // Update state when app returns to foreground
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
            Spacer(modifier = Modifier.height(24.dp))

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
                        imageVector = Icons.Rounded.RotateRight,
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

            Spacer(modifier = Modifier.height(8.dp))

            // Status Cards
            if (!hasAccessibility || !hasOverlay) {
                StatusCard(
                    title = "Action Required",
                    message = "To display the menu on the cover screen, please grant the permissions below.",
                    isError = true
                )
            } else {
                StatusCard(
                    title = "Ready to Go!",
                    message = "Service is active. The menu button will appear on your cover screen.",
                    isError = false
                )
            }

            // Accessibility Permission
            PermissionItem(
                title = "Accessibility Service",
                description = "Required to display the floating menu button on the cover screen.",
                isEnabled = hasAccessibility,
                onClick = { showAccessibilityDialog = true }
            )

            // Overlay Permission
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

            Spacer(modifier = Modifier.height(16.dp))
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
