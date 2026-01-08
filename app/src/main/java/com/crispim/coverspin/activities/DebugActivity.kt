package com.crispim.coverspin.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crispim.coverspin.services.DebugLogger
import com.crispim.coverspin.services.LogEntry
import java.text.SimpleDateFormat
import java.util.*

class DebugActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DebugScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen() {
    val debugLogger = remember { DebugLogger.getInstance() }
    val context = LocalContext.current
    var logLevelFilter by remember { mutableStateOf(com.crispim.coverspin.models.LogLevel.Debug) }
    val logs by debugLogger.logs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug & Logs") },
                navigationIcon = {
                    IconButton(onClick = { /* Navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { debugLogger.clearLogs() }) {
                        Icon(Icons.Default.Delete, "Clear Logs")
                    }
                    IconButton(onClick = { debugLogger.exportLogs(context) }) {
                        Icon(Icons.Default.Download, "Export Logs")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Log Level Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.crispim.coverspin.models.LogLevel.entries.forEach { level ->
                    FilterChip(
                        selected = logLevelFilter == level,
                        onClick = { logLevelFilter = level },
                        label = { Text(level.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Service Status
            ServiceStatusCard()

            Spacer(modifier = Modifier.height(16.dp))

            // Logs
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val filteredLogs = logs.filter { it.level == logLevelFilter || logLevelFilter == com.crispim.coverspin.models.LogLevel.Debug }
                    items(filteredLogs.reversed()) { log ->
                        LogEntryItem(log)
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceStatusCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Service Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            StatusRow("Overlay", EngineActivity.isOverlayActive)
            StatusRow("EventsService", true) // Would need to check actual status
            StatusRow("EngineActivity", EngineActivity.isOverlayActive)
        }
    }
}

@Composable
fun StatusRow(label: String, isActive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(
            if (isActive) "Active" else "Inactive",
            color = if (isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

@Composable
fun LogEntryItem(log: LogEntry) {
    val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    val timeString = dateFormat.format(Date(log.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (log.level) {
                com.crispim.coverspin.models.LogLevel.Error -> Color(0xFFFFEBEE)
                com.crispim.coverspin.models.LogLevel.Info -> Color(0xFFE3F2FD)
                com.crispim.coverspin.models.LogLevel.Debug -> Color(0xFFF5F5F5)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "[$timeString]",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray
                )
                Text(
                    log.level.displayName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (log.level) {
                        com.crispim.coverspin.models.LogLevel.Error -> Color(0xFFD32F2F)
                        com.crispim.coverspin.models.LogLevel.Info -> Color(0xFF1976D2)
                        com.crispim.coverspin.models.LogLevel.Debug -> Color(0xFF616161)
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                log.message,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

