package com.nexuscmd.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class SavedCommand(
    val id: String,
    val command: String,
    val name: String,
    val description: String,
    val category: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

class CommandRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFavoriteCommands(): List<SavedCommand> {
        val json = prefs.getString(KEY_FAVORITES, "[]") ?: "[]"
        return parseCommands(json)
    }

    fun addFavorite(command: SavedCommand) {
        val favorites = getFavoriteCommands().toMutableList()
        if (favorites.none { it.command == command.command }) {
            favorites.add(0, command.copy(id = generateId(), isFavorite = true))
            saveFavorites(favorites)
        }
    }

    fun removeFavorite(commandId: String) {
        val favorites = getFavoriteCommands().toMutableList()
        favorites.removeAll { it.id == commandId }
        saveFavorites(favorites)
    }

    fun isFavorite(command: String): Boolean {
        return getFavoriteCommands().any { it.command == command }
    }

    fun toggleFavorite(command: SavedCommand) {
        if (command.isFavorite) {
            removeFavorite(command.id)
        } else {
            addFavorite(command)
        }
    }

    private fun saveFavorites(commands: List<SavedCommand>) {
        val jsonArray = JSONArray()
        commands.forEach { cmd ->
            jsonArray.put(JSONObject().apply {
                put("id", cmd.id)
                put("command", cmd.command)
                put("name", cmd.name)
                put("description", cmd.description)
                put("category", cmd.category)
                put("isFavorite", true)
                put("createdAt", cmd.createdAt)
            })
        }
        prefs.edit().putString(KEY_FAVORITES, jsonArray.toString()).apply()
    }

    private fun parseCommands(json: String): List<SavedCommand> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                SavedCommand(
                    id = obj.getString("id"),
                    command = obj.getString("command"),
                    name = obj.optString("name", ""),
                    description = obj.optString("description", ""),
                    category = obj.optString("category", "未分类"),
                    isFavorite = obj.optBoolean("isFavorite", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun generateId(): String = "cmd_${System.currentTimeMillis()}_${(0..9999).random()}"

    companion object {
        private const val PREFS_NAME = "nexus_commands"
        private const val KEY_FAVORITES = "favorite_commands"
    }
}
