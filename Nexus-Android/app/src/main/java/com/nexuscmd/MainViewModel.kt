package com.nexuscmd

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexuscmd.data.AddonManager
import com.nexuscmd.data.AddonPack
import com.nexuscmd.data.CommandChainRepository
import com.nexuscmd.data.CommandChain
import com.nexuscmd.data.CommandChainStep
import com.nexuscmd.data.CommandRepository
import com.nexuscmd.data.HistoryItem
import com.nexuscmd.data.HistoryManager
import com.nexuscmd.data.SavedCommand
import com.nexuscmd.data.SettingsManager
import com.nexuscmd.data.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
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

    // Syntax hint for inline display
    val syntaxHint: SyntaxHint? = null,

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
    val currentTheme: AppTheme = AppTheme.FOLLOW_SYSTEM,

    // Custom background
    val customBackgroundUri: String? = null,
    val useCustomBackground: Boolean = false,
    val backgroundOpacity: Float = 0.85f,
    val cardOpacity: Float = 0.9f,

    // UI Customization
    val useGlassmorphism: Boolean = true,
    val glassmorphismIntensity: Float = 0.7f,
    val cardCornerRadius: Float = 16f,
    val useDynamicColor: Boolean = false,
    val useGradientAccents: Boolean = false,

    // Snackbar
    val snackbarMessage: String? = null,

    // Addons
    val installedAddons: List<AddonPack> = emptyList(),
    val addonCommands: List<SavedCommand> = emptyList(),
    val addonTemplates: List<SavedCommand> = emptyList(),
    val addonCompletionsFirst: Boolean = false,

    // Command Chains
    val commandChains: List<CommandChain> = emptyList()
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
    private val addonManager = AddonManager.getInstance(application)
    private val commandChainRepository = CommandChainRepository(application)

    private var completionJob: Job? = null

    companion object {
        private const val COMPLETION_DEBOUNCE_MS = 150L
    }

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val favorites = commandRepository.getFavoriteCommands()
            val history = historyManager.getHistory()
            val allCommands = getBuiltInCommands()
            val theme = settingsManager.currentTheme
            val isDark = settingsManager.isDarkTheme

            // Load addon data
            val addons = addonManager.loadAddons()
            val enabledAddons = addons.filter { it.enabled }
            val addonCommands = enabledAddons.flatMap { it.customCommands }
            val addonTemplates = enabledAddons.flatMap { it.customTemplates }
            val addonFirst = settingsManager.addonCompletionsFirst

            // Load command chains
            val chains = commandChainRepository.getAllChains()

            // Build quick commands from addons
            val addonQuickCommands = addonCommands.map { cmd ->
                Triple(cmd.command, cmd.name, Icons.Default.Extension)
            }

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
                ) + addonQuickCommands.take(4),  // Add up to 4 addon commands
                favoriteCommands = favorites,
                historyItems = history,
                allCommands = allCommands,
                currentTheme = theme,
                isDarkTheme = isDark,
                installedAddons = addons,
                addonCommands = addonCommands,
                addonTemplates = addonTemplates,
                addonCompletionsFirst = addonFirst,
                commandChains = chains,
                customBackgroundUri = settingsManager.customBackgroundUri,
                useCustomBackground = settingsManager.useCustomBackground,
                backgroundOpacity = settingsManager.backgroundOpacity,
                cardOpacity = settingsManager.cardOpacity,
                useGlassmorphism = settingsManager.useGlassmorphism,
                glassmorphismIntensity = settingsManager.glassmorphismIntensity,
                cardCornerRadius = settingsManager.cardCornerRadius,
                useDynamicColor = settingsManager.useDynamicColor,
                useGradientAccents = settingsManager.useGradientAccents
            )
        }
    }

    fun onCommandTextChanged(newText: String) {
        _uiState.value = _uiState.value.copy(
            commandText = newText,
            cursorPosition = newText.length
        )

        completionJob?.cancel()
        completionJob = viewModelScope.launch {
            kotlinx.coroutines.delay(COMPLETION_DEBOUNCE_MS)

            val isFavorite = if (newText.isNotBlank()) {
                commandRepository.isFavorite(newText)
            } else false

            _uiState.value = _uiState.value.copy(
                isCurrentCommandFavorite = isFavorite
            )

            if (newText.isBlank() || !newText.startsWith("/")) {
                _uiState.value = _uiState.value.copy(
                    completions = emptyList(),
                    validation = null,
                    currentCommandInfo = null,
                    syntaxHint = null
                )
                return@launch
            }

            val cursorPos = newText.length
            val completionsJson = helper.getCompletions(newText, cursorPos)
            val completions = parseCompletions(completionsJson)

            val validationJson = helper.validateCommand(newText)
            val validation = parseValidation(validationJson)

            val commandInfo = extractCommandInfo(newText)
            val syntaxHintJson = helper.getSyntaxHint(newText, cursorPos)
            val syntaxHint = parseSyntaxHint(syntaxHintJson)

            _uiState.value = _uiState.value.copy(
                completions = completions,
                validation = validation,
                currentCommandInfo = commandInfo,
                syntaxHint = syntaxHint
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

    fun setTheme(theme: AppTheme) {
        settingsManager.currentTheme = theme
        val isDark = when (theme) {
            AppTheme.FOLLOW_SYSTEM -> settingsManager.isDarkTheme
            AppTheme.LIGHT -> false
            AppTheme.GREEN, AppTheme.OCEAN, AppTheme.WARM,
            AppTheme.MATCHA, AppTheme.DREAMY_PURPLE, AppTheme.SAKURA, AppTheme.ARCTIC -> settingsManager.isDarkTheme
            else -> true
        }
        _uiState.value = _uiState.value.copy(
            currentTheme = theme,
            isDarkTheme = isDark
        )
    }

    fun setCustomBackground(uri: String?) {
        settingsManager.customBackgroundUri = uri
        _uiState.value = _uiState.value.copy(customBackgroundUri = uri)
    }

    fun setUseCustomBackground(enabled: Boolean) {
        settingsManager.useCustomBackground = enabled
        _uiState.value = _uiState.value.copy(useCustomBackground = enabled)
    }

    fun setBackgroundOpacity(opacity: Float) {
        settingsManager.backgroundOpacity = opacity
        _uiState.value = _uiState.value.copy(backgroundOpacity = opacity)
    }

    fun setCardOpacity(opacity: Float) {
        settingsManager.cardOpacity = opacity
        _uiState.value = _uiState.value.copy(cardOpacity = opacity)
    }

    fun setUseGlassmorphism(enabled: Boolean) {
        settingsManager.useGlassmorphism = enabled
        _uiState.value = _uiState.value.copy(useGlassmorphism = enabled)
    }

    fun setGlassmorphismIntensity(intensity: Float) {
        settingsManager.glassmorphismIntensity = intensity
        _uiState.value = _uiState.value.copy(glassmorphismIntensity = intensity)
    }

    fun setCardCornerRadius(radius: Float) {
        settingsManager.cardCornerRadius = radius
        _uiState.value = _uiState.value.copy(cardCornerRadius = radius)
    }

    fun setUseDynamicColor(enabled: Boolean) {
        settingsManager.useDynamicColor = enabled
        _uiState.value = _uiState.value.copy(useDynamicColor = enabled)
    }

    fun setUseGradientAccents(enabled: Boolean) {
        settingsManager.useGradientAccents = enabled
        _uiState.value = _uiState.value.copy(useGradientAccents = enabled)
    }

    fun setDarkTheme(isDark: Boolean) {
        settingsManager.isDarkTheme = isDark
        val currentTheme = settingsManager.currentTheme
        val newTheme = when {
            currentTheme == AppTheme.FOLLOW_SYSTEM -> {
                if (isDark) AppTheme.DARK else AppTheme.LIGHT
            }
            currentTheme == AppTheme.LIGHT && isDark -> AppTheme.DARK
            currentTheme == AppTheme.DARK && !isDark -> AppTheme.LIGHT
            else -> currentTheme
        }
        if (newTheme != currentTheme) {
            settingsManager.currentTheme = newTheme
        }
        _uiState.value = _uiState.value.copy(
            isDarkTheme = isDark,
            currentTheme = newTheme
        )
    }

    fun setAddonCompletionsFirst(enabled: Boolean) {
        settingsManager.addonCompletionsFirst = enabled
        _uiState.value = _uiState.value.copy(addonCompletionsFirst = enabled)
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

    fun refreshAddonData() {
        viewModelScope.launch {
            val addons = addonManager.loadAddons()
            val enabledAddons = addons.filter { it.enabled }
            val addonCommands = enabledAddons.flatMap { it.customCommands }
            val addonTemplates = enabledAddons.flatMap { it.customTemplates }

            val addonQuickCommands = addonCommands.map { cmd ->
                Triple(cmd.command, cmd.name, Icons.Default.Extension)
            }

            _uiState.value = _uiState.value.copy(
                installedAddons = addons,
                addonCommands = addonCommands,
                addonTemplates = addonTemplates,
                quickCommands = listOf(
                    Triple("/give @p diamond 1", "给予钻石", Icons.Default.CardGiftcard),
                    Triple("/summon minecraft:zombie", "生成僵尸", Icons.Default.Widgets),
                    Triple("/tp @s ~ ~ ~", "传送原地", Icons.Default.SwapHoriz),
                    Triple("/setblock ~ ~ ~ stone", "放置石头", Icons.Default.Create),
                    Triple("/fill ~ ~ ~ ~10 ~10 stone", "填充石头", Icons.Default.Map),
                    Triple("/effect @s speed 30 1", "加速效果", Icons.Default.Speed),
                    Triple("/scoreboard objectives add", "记分板", Icons.Default.Leaderboard),
                    Triple("/give @p written_book", "给予书本", Icons.Default.MenuBook)
                ) + addonQuickCommands.take(4)
            )
        }
    }

    fun getAddonFilteredCommands(): List<CommandLibraryItem> {
        val query = _uiState.value.searchQuery.lowercase()
        return _uiState.value.addonCommands.map { cmd ->
            CommandLibraryItem(
                name = cmd.name,
                description = cmd.description,
                syntax = cmd.command,
                category = "拓展包",
                icon = Icons.Default.Extension
            )
        }.filter {
            query.isEmpty() ||
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.syntax.contains(query, ignoreCase = true)
        }
    }

    fun getAddonTemplates(): List<SavedCommand> {
        return _uiState.value.addonTemplates
    }

    fun addCommandChain(chain: CommandChain) {
        viewModelScope.launch {
            commandChainRepository.addChain(chain)
            refreshCommandChains()
        }
    }

    fun updateCommandChain(chain: CommandChain) {
        viewModelScope.launch {
            commandChainRepository.updateChain(chain)
            refreshCommandChains()
        }
    }

    fun deleteCommandChain(chainId: String) {
        viewModelScope.launch {
            commandChainRepository.deleteChain(chainId)
            refreshCommandChains()
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "已删除命令链"
            )
        }
    }

    fun searchCommandChains(query: String): List<CommandChain> {
        return commandChainRepository.searchChains(query)
    }

    private fun refreshCommandChains() {
        val chains = commandChainRepository.getAllChains()
        _uiState.value = _uiState.value.copy(commandChains = chains)
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
                message = obj.optString("message", "").takeIf { it.isNotEmpty() },
                position = if (obj.has("position")) obj.getInt("position") else null
            )
        } catch (e: Exception) {
            ValidationResult(hasError = true, message = e.message)
        }
    }

    private fun parseSyntaxHint(json: String): SyntaxHint? {
        return try {
            val obj = JSONObject(json)
            if (obj.has("template") && obj.getString("template").isNotEmpty()) {
                SyntaxHint(
                    template = obj.getString("template"),
                    activeParamStart = obj.optInt("activeParamStart", 0),
                    activeParamEnd = obj.optInt("activeParamEnd", 0),
                    activeParamIndex = obj.optInt("activeParamIndex", 0),
                    activeParamName = obj.optString("activeParamName", ""),
                    activeParamHint = obj.optString("activeParamHint", ""),
                    isOptional = obj.optBoolean("isOptional", false)
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun getBuiltInCommands(): List<CommandLibraryItem> = listOf(
        // ============ 基础命令 (官方JSON库) ============
        CommandLibraryItem("?", "显示命令帮助", "/? <页数>", "帮助", Icons.Default.Help),
        CommandLibraryItem("aimassist", "开启或关闭玩家的辅助瞄准功能", "/aimassist <玩家> <清除|设置>", "玩家", Icons.Default.MyLocation),
        CommandLibraryItem("alwaysday", "切换昼夜更替锁定", "/alwaysday [lock]", "世界", Icons.Default.WbSunny),
        CommandLibraryItem("camera", "修改玩家的相机视角", "/camera <玩家> <重置|设置|跟踪>", "基岩版独有", Icons.Default.CameraAlt),
        CommandLibraryItem("camerashake", "对玩家视野施以一定强度和时间的摇晃效果", "/camerashake <添加|停止> <玩家>", "基岩版独有", Icons.Default.CameraAlt),
        CommandLibraryItem("clear", "清除玩家物品栏中的物品", "/clear <玩家> [物品ID] [数据值] [最大数量]", "物品", Icons.Default.Delete),
        CommandLibraryItem("clearspawnpoint", "重置玩家重生点", "/clearspawnpoint [玩家]", "传送", Icons.Default.Home),
        CommandLibraryItem("clone", "在区域间复制方块", "/clone <起点> <终点> <目标点> [遮罩模式]", "方块", Icons.Default.ContentCopy),
        CommandLibraryItem("connect", "连接WebSocket服务器", "/connect <服务器地址>", "服务器", Icons.Default.Public),
        CommandLibraryItem("controlscheme", "修改相机预设的控制方案", "/controlscheme <玩家> <清除|设置>", "基岩版独有", Icons.Default.Gamepad),
        CommandLibraryItem("damage", "对实体造成来源于特定实体的伤害", "/damage <目标> <伤害> [伤害类型]", "实体", Icons.Default.HeartBroken),
        CommandLibraryItem("daylock", "锁定或解锁终为白日", "/daylock [lock]", "世界", Icons.Default.WbSunny),
        CommandLibraryItem("deop", "撤销管理员身份", "/deop <玩家>", "管理员", Icons.Default.PersonRemove),
        CommandLibraryItem("dialogue", "为玩家打开或改变NPC的对话框", "/dialogue <NPC> <玩家> [场景名称]", "基岩版独有", Icons.Default.RecordVoiceOver),
        CommandLibraryItem("difficulty", "设定难度等级", "/difficulty <难度>", "世界", Icons.Default.Shield),
        CommandLibraryItem("effect", "管理玩家及其他实体上的状态效果", "/effect <实体> <效果ID> [持续秒数] [等级]", "实体", Icons.Default.Speed),
        CommandLibraryItem("enchant", "为一个实体手持的物品添加附魔", "/enchant <目标> <魔咒ID> [等级]", "物品", Icons.Default.AutoAwesome),
        CommandLibraryItem("event", "触发实体事件", "/event <目标> <实体事件>", "基岩版独有", Icons.Default.Bolt),
        CommandLibraryItem("execute", "更改命令执行的主体、位置等上下文", "/execute <子命令> <run> <命令>", "执行", Icons.Default.Terminal),
        CommandLibraryItem("fill", "用指定方块填充区域", "/fill <起点> <终点> <方块ID> [旧方块处理方式]", "方块", Icons.Default.Map),
        CommandLibraryItem("fog", "更改玩家的迷雾效果", "/fog <玩家> <添加|移除> <迷雾ID>", "基岩版独有", Icons.Default.Cloud),
        CommandLibraryItem("function", "调用函数", "/function <函数名>", "执行", Icons.Default.Functions),
        CommandLibraryItem("gamemode", "更改游戏模式", "/gamemode <游戏模式> [玩家]", "游戏模式", Icons.Default.VideogameAsset),
        CommandLibraryItem("gamerule", "查看或修改游戏规则", "/gamerule <规则> [值]", "世界", Icons.Default.Tune),
        CommandLibraryItem("gametest", "GameTest 测试", "/gametest <运行|创建|清除>", "开发", Icons.Default.BugReport),
        CommandLibraryItem("give", "给予玩家特定物品", "/give <玩家> <物品名称> [数量] [数据值]", "物品", Icons.Default.CardGiftcard),
        CommandLibraryItem("help", "显示命令帮助", "/help [页数|命令]", "帮助", Icons.Default.Help),
        CommandLibraryItem("hud", "修改HUD的可见性", "/hud <玩家> <修改> [HUD元素]", "基岩版独有", Icons.Default.Visibility),
        CommandLibraryItem("inputpermission", "对玩家的权限状态进行指定操作", "/inputpermission <查询|设置> <玩家>", "基岩版独有", Icons.Default.TouchApp),
        CommandLibraryItem("kick", "踢出特定玩家", "/kick <玩家> <原因>", "管理员", Icons.Default.ExitToApp),
        CommandLibraryItem("kill", "击杀或移除实体", "/kill <目标>", "实体", Icons.Default.Gradient),
        CommandLibraryItem("list", "列出在线玩家", "/list", "管理员", Icons.Default.People),
        CommandLibraryItem("locate", "寻找最近的特定生物群系或结构的坐标", "/locate <群系|结构> <ID>", "世界", Icons.Default.Search),
        CommandLibraryItem("loot", "将指定的战利品放入物品栏或世界", "/loot <给予|生成|替换>", "物品", Icons.Default.Redeem),
        CommandLibraryItem("me", "发送一条关于自己的消息", "/me <消息>", "玩家互动", Icons.Default.Face),
        CommandLibraryItem("mobevent", "控制或查询允许运行的生物事件", "/mobevent <事件> [状态]", "世界", Icons.Default.Tune),
        CommandLibraryItem("msg", "将一条私聊消息发送给一个或多个玩家", "/msg <玩家> <消息>", "玩家互动", Icons.Default.Message),
        CommandLibraryItem("music", "播放指定的音乐", "/music <播放|停止|音量>", "声音粒子", Icons.Default.VolumeUp),
        CommandLibraryItem("op", "赋予特定玩家管理员身份", "/op <玩家>", "管理员", Icons.Default.AdminPanelSettings),
        CommandLibraryItem("particle", "在指定位置生成粒子发射器", "/particle <颗粒效果> [生成位置]", "声音粒子", Icons.Default.AutoFixHigh),
        CommandLibraryItem("place", "放置已配置的地物、结构模板", "/place <地物|结构> <ID> [位置]", "世界", Icons.Default.Apartment),
        CommandLibraryItem("playanimation", "在特定实体上播放实体动画", "/playanimation <实体> <动画ID>", "基岩版独有", Icons.Default.Movie),
        CommandLibraryItem("playsound", "在特定位置为指定玩家播放声音", "/playsound <声音ID> <目标> [位置]", "声音粒子", Icons.Default.VolumeUp),
        CommandLibraryItem("recipe", "对玩家赋予或收回指定的配方", "/recipe <赋予|收回> <玩家> <配方ID>", "基岩版独有", Icons.Default.Receipt),
        CommandLibraryItem("reload", "重新加载行为包中的函数与脚本", "/reload", "服务器", Icons.Default.Refresh),
        CommandLibraryItem("replaceitem", "替换方块或实体物品栏内的物品", "/replaceitem <方块|实体> <位置> <槽位>", "物品", Icons.Default.Inventory2),
        CommandLibraryItem("ride", "管理实体的骑乘关系", "/ride <乘客> <骑乘|取消>", "实体", Icons.Default.Directions),
        CommandLibraryItem("say", "向所有玩家广播消息", "/say <消息>", "玩家互动", Icons.Default.Campaign),
        CommandLibraryItem("schedule", "计划执行函数", "/schedule <添加|移除> <函数> <时间>", "执行", Icons.Default.Schedule),
        CommandLibraryItem("scoreboard", "管理记分板中的记分项和分数持有者", "/scoreboard <管理记分项|管理分数>", "记分板", Icons.Default.Leaderboard),
        CommandLibraryItem("script", "调试GameTest框架选项", "/script <调试器|性能分析器>", "开发", Icons.Default.Code),
        CommandLibraryItem("scriptevent", "通过ID和消息来触发Script API脚本事件", "/scriptevent <消息ID> <消息>", "基岩版独有", Icons.Default.Code),
        CommandLibraryItem("setblock", "更改指定坐标位置的方块", "/setblock <坐标> <方块ID> [替换方式]", "方块", Icons.Default.Create),
        CommandLibraryItem("setmaxplayers", "设置世界的最大可加入玩家数", "/setmaxplayers <最大玩家数>", "管理员", Icons.Default.GroupAdd),
        CommandLibraryItem("setworldspawn", "设置世界重生点", "/setworldspawn [位置]", "传送", Icons.Default.Public),
        CommandLibraryItem("spawnpoint", "设置玩家重生点", "/spawnpoint [玩家] [位置]", "传送", Icons.Default.Home),
        CommandLibraryItem("spreadplayers", "将指定实体传送到区域内的随机位置", "/spreadplayers <X> <Z> <间距> <范围> <实体>", "传送", Icons.Default.SwapHoriz),
        CommandLibraryItem("stopsound", "为指定玩家停止播放声音", "/stopsound <玩家> <声音ID>", "声音粒子", Icons.Default.VolumeOff),
        CommandLibraryItem("structure", "使用命令保存或加载结构", "/structure <保存|加载|删除> <结构ID>", "基岩版独有", Icons.Default.Apartment),
        CommandLibraryItem("summon", "召唤一个实体", "/summon <实体类型> [生成位置]", "实体", Icons.Default.Widgets),
        CommandLibraryItem("tag", "管理实体的标签", "/tag <目标> <添加|移除|列表>", "记分板", Icons.Default.Label),
        CommandLibraryItem("teleport", "传送实体到指定的地点", "/teleport <目标> <目的地>", "传送", Icons.Default.SwapHoriz),
        CommandLibraryItem("tell", "发送私聊消息", "/tell <玩家> <消息>", "玩家互动", Icons.Default.Chat),
        CommandLibraryItem("tellraw", "向指定玩家发送JSON文本消息", "/tellraw <玩家> <JSON消息>", "玩家互动", Icons.Default.RssFeed),
        CommandLibraryItem("testfor", "测试指定的实体是否存在", "/testfor <目标>", "帮助", Icons.Default.Check),
        CommandLibraryItem("testforblock", "测试指定坐标的方块是否满足条件", "/testforblock <位置> <方块ID>", "方块", Icons.Default.GridOn),
        CommandLibraryItem("testforblocks", "测试指定区域的方块是否满足条件", "/testforblocks <起点> <终点> <目标点>", "方块", Icons.Default.GridOn),
        CommandLibraryItem("tickingarea", "添加、删除或列出常加载区域", "/tickingarea <添加|删除|列表>", "世界", Icons.Default.AreaChart),
        CommandLibraryItem("time", "更改或查询世界的游戏时间", "/time <set|add|query> <值>", "世界", Icons.Default.Schedule),
        CommandLibraryItem("title", "控制屏幕标题", "/title <玩家> <clear|title|subtitle>", "玩家互动", Icons.Default.FormatSize),
        CommandLibraryItem("titleraw", "控制屏幕JSON文本标题", "/titleraw <玩家> <JSON标题>", "玩家互动", Icons.Default.FormatSize),
        CommandLibraryItem("toggledownfall", "切换当前天气是否降雨", "/toggledownfall", "世界", Icons.Default.WbCloudy),
        CommandLibraryItem("tp", "传送实体", "/tp <目标> <目的地>", "传送", Icons.Default.SwapHoriz),
        CommandLibraryItem("w", "发送私聊消息(短)", "/w <玩家> <消息>", "玩家互动", Icons.Default.Forum),
        CommandLibraryItem("weather", "设置或查询当前天气", "/weather <clear|rain|thunder> [持续时间]", "世界", Icons.Default.WbSunny),
        CommandLibraryItem("wsserver", "连接WebSocket服务器", "/wsserver <服务器地址>", "服务器", Icons.Default.Public),
        CommandLibraryItem("xp", "调整玩家的经验值", "/xp <数量> [玩家]", "玩家互动", Icons.Default.AutoGraph),
    )

    fun getQuickCommands(): List<CommandLibraryItem> = listOf(
        // ============ 游戏规则 ============
        CommandLibraryItem("死亡不掉落", "开启死亡不掉落", "/gamerule keepInventory true", "游戏规则", Icons.Default.Lock),
        CommandLibraryItem("禁用生物破坏", "禁用爬行者破坏", "/gamerule mobGriefing false", "游戏规则", Icons.Default.Block),
        CommandLibraryItem("显示坐标", "开启显示坐标", "/gamerule showCoordinates true", "游戏规则", Icons.Default.LocationOn),
        CommandLibraryItem("关闭火焰蔓延", "防止火灾蔓延", "/gamerule fireDamage false", "游戏规则", Icons.Default.LocalFireDepartment),
        CommandLibraryItem("关闭TNT爆炸", "禁用TNT伤害", "/gamerule tntExplodes false", "游戏规则", Icons.Default.Warning),
        
        // ============ 效果类 ============
        CommandLibraryItem("隐身效果", "获得隐身效果", "/effect @s invisibility 600 1 true", "效果", Icons.Default.VisibilityOff),
        CommandLibraryItem("速度效果", "永久加速", "/effect @s speed 999999 2", "效果", Icons.Default.Speed),
        CommandLibraryItem("夜视效果", "永久夜视", "/effect @s night_vision 999999 1 true", "效果", Icons.Default.Visibility),
        CommandLibraryItem("水下呼吸", "永久水下呼吸", "/effect @s water_breathing 999999 1 true", "效果", Icons.Default.Water),
        CommandLibraryItem("跳跃提升", "跳得更高", "/effect @s jump_boost 999999 5", "效果", Icons.Default.TrendingUp),
        CommandLibraryItem("生命提升", "生命提升255级", "/effect @s health_boost 999999 255", "效果", Icons.Default.Favorite),
        CommandLibraryItem("瞬间治疗", "瞬间恢复生命", "/effect @s instant_health 1 255", "效果", Icons.Default.Favorite),
        CommandLibraryItem("防火效果", "永久防火", "/effect @s fire_resistance 999999 1 true", "效果", Icons.Default.Shield),
        
        // ============ 传送类 ============
        CommandLibraryItem("设置家", "设置个人出生点", "/spawnpoint @s ~ ~ ~", "传送", Icons.Default.Home),
        CommandLibraryItem("重置出生点", "重置个人出生点", "/clearspawnpoint @s", "传送", Icons.Default.Refresh),
        
        // ============ 模式切换 ============
        CommandLibraryItem("创造模式", "切换创造模式", "/gamemode creative @s", "模式", Icons.Default.Palette),
        CommandLibraryItem("生存模式", "切换生存模式", "/gamemode survival @s", "模式", Icons.Default.SelfImprovement),
        CommandLibraryItem("冒险模式", "切换冒险模式", "/gamemode adventure @s", "模式", Icons.Default.Explore),
        CommandLibraryItem("旁观模式", "切换旁观模式", "/gamemode spectator @s", "模式", Icons.Default.Visibility),
        
        // ============ 物品类 ============
        CommandLibraryItem("给予钻石", "给自己64个钻石", "/give @s diamond 64", "物品", Icons.Default.Diamond),
        CommandLibraryItem("给予铁锭", "给自己64个铁锭", "/give @s iron_ingot 64", "物品", Icons.Default.Build),
        CommandLibraryItem("给予金锭", "给自己64个金锭", "/give @s gold_ingot 64", "物品", Icons.Default.Star),
        CommandLibraryItem("给予下界之星", "给自己下界之星", "/give @s nether_star 1", "物品", Icons.Default.Stars),
        CommandLibraryItem("给予命令方块", "获得命令方块", "/give @s command_block 1", "物品", Icons.Default.Terminal),
        CommandLibraryItem("给予结构方块", "获得结构方块", "/give @s structure_block 1", "物品", Icons.Default.Apartment),
        
        // ============ 其他常用 ============
        CommandLibraryItem("白天", "设置为白天", "/time set day", "其他", Icons.Default.LightMode),
        CommandLibraryItem("夜晚", "设置为夜晚", "/time set night", "其他", Icons.Default.DarkMode),
        CommandLibraryItem("晴天", "设置为晴天", "/weather clear", "其他", Icons.Default.WbSunny),
        CommandLibraryItem("雨天", "设置为雨天", "/weather rain", "其他", Icons.Default.WbCloudy),
        CommandLibraryItem("雷雨", "设置为雷雨", "/weather thunder", "其他", Icons.Default.Bolt),
        CommandLibraryItem("生成闪电", "召唤闪电", "/summon lightning_bolt", "其他", Icons.Default.Bolt),
        CommandLibraryItem("常加载区域", "添加常加载", "/tickingarea add ~ ~ ~ ~10 ~10 ~10", "其他", Icons.Default.AreaChart),
        CommandLibraryItem("清除掉落物", "清除所有掉落物", "/kill @e[type=item]", "其他", Icons.Default.DeleteSweep),
        CommandLibraryItem("清除生物", "清除所有生物", "/kill @e[type=!player]", "其他", Icons.Default.DeleteSweep),
        CommandLibraryItem("锁定白天", "锁定为白天", "/daylock true", "其他", Icons.Default.WbSunny),
    )
}
