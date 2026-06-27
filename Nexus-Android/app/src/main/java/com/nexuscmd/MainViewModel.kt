package com.nexuscmd

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class MainUiState(
    val commandText: String = "",
    val cursorPosition: Int = 0,
    val completions: List<CompletionItem> = emptyList(),
    val validation: ValidationResult? = null,
    val currentCommandInfo: CommandInfo? = null,
    val quickCommands: List<Triple<String, String, ImageVector>> = emptyList()
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val helper = CommandHelper.Registry.getInstance()

    init {
        _uiState.value = _uiState.value.copy(
            quickCommands = listOf(
                Triple("/give @p", "给予玩家物品", Icons.Default.CardGiftcard),
                Triple("/summon", "生成实体", Icons.Default.Widgets),
                Triple("/tp @s", "传送自己", Icons.Default.SwapHoriz),
                Triple("/setblock", "放置方块", Icons.Default.Create),
                Triple("/fill", "填充区域", Icons.Default.Map)
            )
        )
    }

    fun onCommandTextChanged(newText: String) {
        viewModelScope.launch {
            val cursorPos = newText.length
            _uiState.value = _uiState.value.copy(
                commandText = newText,
                cursorPosition = cursorPos
            )

            val completionsJson = helper.getCompletions(newText, cursorPos)
            val completions = parseCompletions(completionsJson)

            val validationJson = helper.validateCommand(newText)
            val validation = parseValidation(validationJson)

            val commandInfo = extractCommandInfo(newText)

            _uiState.value = _uiState.value.copy(
                completions = completions,
                validation = validation,
                currentCommandInfo = commandInfo
            )
        }
    }

    fun applyCompletion(item: CompletionItem) {
        viewModelScope.launch {
            val newText = item.insertText
            _uiState.value = _uiState.value.copy(
                commandText = newText,
                cursorPosition = newText.length,
                completions = emptyList()
            )
        }
    }

    private fun extractCommandInfo(text: String): CommandInfo? {
        if (text.isEmpty() || !text.startsWith("/")) return null

        val parts = text.trimStart('/').split(" ", limit = 2)
        if (parts.isEmpty()) return null

        val cmdName = parts[0]
        if (cmdName.isEmpty()) return null

        val infoJson = helper.getCommandInfo(cmdName)
        return try {
            val obj = JSONObject(infoJson)
            if (obj.has("name")) {
                CommandInfo(
                    name = obj.getString("name"),
                    syntax = obj.optString("syntax", ""),
                    description = obj.optString("description", "")
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun parseCompletions(json: String): List<CompletionItem> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                CompletionItem(
                    label = obj.getString("label"),
                    detail = obj.optString("detail", ""),
                    insertText = obj.getString("insertText")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseValidation(json: String): ValidationResult {
        return try {
            val obj = JSONObject(json)
            ValidationResult(
                hasError = obj.getBoolean("hasError"),
                message = obj.optString("message", null),
                position = if (obj.has("position")) obj.getInt("position") else null
            )
        } catch (e: Exception) {
            ValidationResult(hasError = true, message = e.message)
        }
    }
}
