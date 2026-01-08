package com.crispim.coverspin.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.crispim.coverspin.models.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

data class LogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val message: String
)

class DebugLogger private constructor() {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val maxLogs = 1000

    companion object {
        @Volatile
        private var INSTANCE: DebugLogger? = null

        fun getInstance(): DebugLogger {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DebugLogger().also { INSTANCE = it }
            }
        }
    }

    fun log(level: LogLevel, message: String) {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            message = message
        )
        _logs.value = (_logs.value + entry).takeLast(maxLogs)
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun exportLogs(context: Context) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val fileName = "coverspin_logs_${dateFormat.format(Date())}.txt"
            val file = File(context.getExternalFilesDir(null), fileName)

            FileWriter(file).use { writer ->
                _logs.value.forEach { log ->
                    val timeString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                        .format(Date(log.timestamp))
                    writer.write("[$timeString] [${log.level.displayName}] ${log.message}\n")
                }
            }

            // Share the file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export Logs"))
        } catch (e: Exception) {
            log(LogLevel.Error, "Failed to export logs: ${e.message}")
        }
    }
}

