package com.nexuscmd

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexuscmd.data.CommandRepository
import com.nexuscmd.data.HistoryItem
import com.nexuscmd.data.HistoryManager
import com.nexuscmd.data.SavedCommand
import com.nexuscmd.data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class MainUiState(
    val commandText: String = "",
    val cursorPosition: Int = 0,
    val completions: List<CompletionItem> = emptyList(),
    val validation: ValidationResult? = null,
    val currentCommandInfo: CommandInfo? = null,
    val quickCommands: List<Triple<String, String, ImageVector>> = emptyList(),

    // Favorites
    val favoriteCommands: List<SavedCommand> = emptyList(),
    val isCurrentCommandFavorite: Boolean = false,

    // History
    val historyItems: List<HistoryItem> = emptyList(),

    // All commands for library
    val allCommands: List<CommandLibraryItem> = emptyList(),
    val searchQuery: String = "",

    // Settings
    val isDarkTheme: Boolean = false,

    // Snackbar
    val snackbarMessage: String? = null
)

data class CommandLibraryItem(
    val name: String,
    val description: String,
    val syntax: String,
    val category: String,
    val icon: ImageVector
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val helper = CommandHelper.Registry.getInstance()
    private val commandRepository = CommandRepository(application)
    private val historyManager = HistoryManager(application)
    private val settingsManager = SettingsManager(application)

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val favorites = commandRepository.getFavoriteCommands()
            val history = historyManager.getHistory()
            val allCommands = getBuiltInCommands()
            val isDark = settingsManager.isDarkTheme

            _uiState.value = _uiState.value.copy(
                quickCommands = listOf(
                    Triple("/give @p diamond 1", "给予钻石", Icons.Default.CardGiftcard),
                    Triple("/summon minecraft:zombie", "生成僵尸", Icons.Default.Widgets),
                    Triple("/tp @s ~ ~ ~", "传送原地", Icons.Default.SwapHoriz),
                    Triple("/setblock ~ ~ ~ stone", "放置石头", Icons.Default.Create),
                    Triple("/fill ~ ~ ~ ~10 ~10 stone", "填充石头", Icons.Default.Map),
                    Triple("/effect @s speed 30 1", "加速效果", Icons.Default.Speed),
                    Triple("/scoreboard objectives add", "记分板", Icons.Default.Leaderboard),
                    Triple("/give @p written_book", "给予书本", Icons.Default.MenuBook)
                ),
                favoriteCommands = favorites,
                historyItems = history,
                allCommands = allCommands,
                isDarkTheme = isDark
            )
        }
    }

    fun onCommandTextChanged(newText: String) {
        viewModelScope.launch {
            val cursorPos = newText.length
            val isFavorite = if (newText.isNotBlank()) {
                commandRepository.isFavorite(newText)
            } else false

            _uiState.value = _uiState.value.copy(
                commandText = newText,
                cursorPosition = cursorPos,
                isCurrentCommandFavorite = isFavorite
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
            // Add to history
            historyManager.addToHistory(newText)
            refreshHistory()
        }
    }

    fun saveToFavorites(command: String, name: String = "", description: String = "") {
        viewModelScope.launch {
            val category = extractCommandCategory(command)
            val savedCommand = SavedCommand(
                id = "",
                command = command,
                name = name.ifEmpty { command.take(20) },
                description = description,
                category = category
            )
            commandRepository.addFavorite(savedCommand)
            refreshFavorites()
            _uiState.value = _uiState.value.copy(
                isCurrentCommandFavorite = true,
                snackbarMessage = "已添加到收藏"
            )
        }
    }

    fun removeFromFavorites(commandId: String) {
        viewModelScope.launch {
            commandRepository.removeFavorite(commandId)
            refreshFavorites()
            _uiState.value = _uiState.value.copy(snackbarMessage = "已取消收藏")
        }
    }

    fun toggleCurrentFavorite() {
        val command = _uiState.value.commandText
        if (command.isNotBlank()) {
            if (_uiState.value.isCurrentCommandFavorite) {
                val fav = _uiState.value.favoriteCommands.find { it.command == command }
                fav?.let { removeFromFavorites(it.id) }
            } else {
                saveToFavorites(command)
            }
        }
    }

    fun addToHistory(command: String) {
        viewModelScope.launch {
            historyManager.addToHistory(command)
            refreshHistory()
        }
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            historyManager.removeFromHistory(id)
            refreshHistory()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyManager.clearHistory()
            refreshHistory()
            _uiState.value = _uiState.value.copy(snackbarMessage = "历史已清空")
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun getFilteredCommands(): List<CommandLibraryItem> {
        val query = _uiState.value.searchQuery.lowercase()
        val commands = _uiState.value.allCommands
        return if (query.isEmpty()) {
            commands
        } else {
            commands.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.syntax.contains(query, ignoreCase = true)
            }
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        settingsManager.isDarkTheme = isDark
        _uiState.value = _uiState.value.copy(isDarkTheme = isDark)
    }

    fun clearAllData() {
        viewModelScope.launch {
            settingsManager.clearAllData()
            commandRepository.getFavoriteCommands().forEach {
                commandRepository.removeFavorite(it.id)
            }
            historyManager.clearHistory()
            loadInitialData()
            _uiState.value = _uiState.value.copy(snackbarMessage = "所有数据已清空")
        }
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    private fun refreshFavorites() {
        _uiState.value = _uiState.value.copy(
            favoriteCommands = commandRepository.getFavoriteCommands()
        )
    }

    private fun refreshHistory() {
        _uiState.value = _uiState.value.copy(
            historyItems = historyManager.getHistory()
        )
    }

    private fun extractCommandCategory(command: String): String {
        return when {
            command.startsWith("/give") -> "物品"
            command.startsWith("/summon") -> "实体"
            command.startsWith("/tp") || command.startsWith("/teleport") -> "传送"
            command.startsWith("/setblock") -> "方块"
            command.startsWith("/fill") -> "填充"
            command.startsWith("/effect") -> "效果"
            command.startsWith("/scoreboard") -> "记分板"
            command.startsWith("/execute") -> "执行"
            command.startsWith("/spawnpoint") -> "出生点"
            command.startsWith("/gamerule") -> "游戏规则"
            else -> "其他"
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

    private fun getBuiltInCommands(): List<CommandLibraryItem> = listOf(
        // 物品类
        CommandLibraryItem("give", "给予玩家物品", "/give <player> <item> [amount]", "物品", Icons.Default.CardGiftcard),
        CommandLibraryItem("clear", "清除玩家物品", "/clear <player> [item] [maxCount]", "物品", Icons.Default.Delete),
        CommandLibraryItem("replaceitem", "替换物品栏物品", "/replaceitem <slot> <item>", "物品", Icons.Default.SwapHoriz),

        // 实体类
        CommandLibraryItem("summon", "生成实体", "/summon <entity> [pos] [nbt]", "实体", Icons.Default.Widgets),
        CommandLibraryItem("kill", "删除实体", "/kill [target]", "实体", Icons.Default.Gradient),
        CommandLibraryItem("effect", "给予药水效果", "/effect <target> <effect> [seconds] [amplifier]", "实体", Icons.Default.Speed),

        // 传送类
        CommandLibraryItem("tp", "传送玩家", "/tp [target] <destination>", "传送", Icons.Default.SwapHoriz),
        CommandLibraryItem("teleport", "传送(高级)", "/teleport <targets> <location>", "传送", Icons.Default.NearMe),
        CommandLibraryItem("spawnpoint", "设置出生点", "/spawnpoint [player] [pos]", "传送", Icons.Default.Home),

        // 方块类
        CommandLibraryItem("setblock", "放置方块", "/setblock <pos> <block> [blockstate]", "方块", Icons.Default.Create),
        CommandLibraryItem("getblock", "获取方块信息", "/getblock <pos>", "方块", Icons.Default.Search),
        CommandLibraryItem("fill", "填充区域", "/fill <from> <to> <block> [options]", "方块", Icons.Default.Map),

        // 世界类
        CommandLibraryItem("time", "设置时间", "/time set <value>", "世界", Icons.Default.Schedule),
        CommandLibraryItem("weather", "设置天气", "/weather <clear|rain|thunder> [duration]", "世界", Icons.Default.WbSunny),
        CommandLibraryItem("worldborder", "世界边界", "/worldborder <set|center|damage|warning>", "世界", Icons.Default.Public),
        CommandLibraryItem("difficulty", "设置难度", "/difficulty <peaceful|easy|normal|hard>", "世界", Icons.Default.Shield),

        // 游戏类
        CommandLibraryItem("gamerule", "设置游戏规则", "/gamerule <rule> [value]", "游戏规则", Icons.Default.Settings),
        CommandLibraryItem("defaultgamemode", "设置默认游戏模式", "/defaultgamemode <mode>", "游戏规则", Icons.Default.SportsEsports),
        CommandLibraryItem("difficulty", "设置难度", "/difficulty <peaceful|easy|normal|hard>", "游戏规则", Icons.Default.Lock),

        // 记分板类
        CommandLibraryItem("scoreboard", "记分板主命令", "/scoreboard <objectives|players|teams>", "记分板", Icons.Default.Leaderboard),
        CommandLibraryItem("execute", "执行命令", "/execute <command>", "执行", Icons.Default.Terminal),

        // 玩家类
        CommandLibraryItem("msg", "发送私信", "/msg <player> <message>", "玩家", Icons.Default.Message),
        CommandLibraryItem("tell", "发送私信(同msg)", "/tell <player> <message>", "玩家", Icons.Default.Chat),
        CommandLibraryItem("w", "发送私信(同msg)", "/w <player> <message>", "玩家", Icons.Default.Forum),
        CommandLibraryItem("me", "动作命令", "/me <action>", "玩家", Icons.Default.Face),
        CommandLibraryItem("say", "广播消息", "/say <message>", "玩家", Icons.Default.Campaign),

        // 管理员类
        CommandLibraryItem("ban", "封禁玩家", "/ban <player> [reason]", "管理员", Icons.Default.Block),
        CommandLibraryItem("banip", "封禁IP", "/banip <address|name>", "管理员", Icons.Default.Gavel),
        CommandLibraryItem("kick", "踢出玩家", "/kick <player> [reason]", "管理员", Icons.Default.ExitToApp),
        CommandLibraryItem("op", "给予管理员权限", "/op <player>", "管理员", Icons.Default.AdminPanelSettings),
        CommandLibraryItem("deop", "移除管理员权限", "/deop <player>", "管理员", Icons.Default.PersonRemove),

        // 服务器类
        CommandLibraryItem("list", "列出玩家", "/list", "服务器", Icons.Default.People),
        CommandLibraryItem("stop", "停止服务器", "/stop", "服务器", Icons.Default.Power),
        CommandLibraryItem("reload", "重载配置", "/reload", "服务器", Icons.Default.Refresh)
    )
}
