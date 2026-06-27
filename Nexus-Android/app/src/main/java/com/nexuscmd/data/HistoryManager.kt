package com.nexuscmd.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class HistoryItem(
    val id: String,
    val command: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getHistory(): List<HistoryItem> {
        val json = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        return parseHistory(json)
    }

    fun addToHistory(command: String) {
        if (command.isBlank()) return
        val history = getHistory().toMutableList()

        // Remove duplicate if exists
        history.removeAll { it.command == command }

        // Add to beginning
        history.add(0, HistoryItem(
            id = generateId(),
            command = command,
            timestamp = System.currentTimeMillis()
        ))

        // Keep only last MAX_HISTORY items
        val trimmed = history.take(MAX_HISTORY)
        saveHistory(trimmed)
    }

    fun removeFromHistory(id: String) {
        val history = getHistory().toMutableList()
        history.removeAll { it.id == id }
        saveHistory(history)
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveHistory(history: List<HistoryItem>) {
        val jsonArray = JSONArray()
        history.forEach { item ->
            jsonArray.put(JSONObject().apply {
                put("id", item.id)
                put("command", item.command)
                put("timestamp", item.timestamp)
            })
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }

    private fun parseHistory(json: String): List<HistoryItem> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                HistoryItem(
                    id = obj.getString("id"),
                    command = obj.getString("command"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun generateId(): String = "hist_${System.currentTimeMillis()}_${(0..9999).random()}"

    companion object {
        private const val PREFS_NAME = "nexus_history"
        private const val KEY_HISTORY = "command_history"
        private const val MAX_HISTORY = 100
    }
}
