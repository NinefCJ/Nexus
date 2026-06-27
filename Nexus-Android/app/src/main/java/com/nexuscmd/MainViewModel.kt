package com.nexuscmd

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nexuscmd.data.AddonManager
import com.nexuscmd.data.AddonPack
import com.nexuscmd.data.CommandRepository
import com.nexuscmd.data.HistoryItem
import com.nexuscmd.data.HistoryManager
import com.nexuscmd.data.SavedCommand
import com.nexuscmd.data.SettingsManager
import com.nexuscmd.data.AppTheme
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
    val currentTheme: AppTheme = AppTheme.FOLLOW_SYSTEM,

    // Snackbar
    val snackbarMessage: String? = null,

    // Addons
    val installedAddons: List<AddonPack> = emptyList(),
    val addonCommands: List<SavedCommand> = emptyList(),
    val addonTemplates: List<SavedCommand> = emptyList(),
    val addonCompletionsFirst: Boolean = false
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

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val favorites = commandRepository.getFavoriteCommands()
            val history = historyManager.getHistory()
            val allCommands = getBuiltInCommands()
            val theme = settingsManager.currentTheme

            // Load addon data
            val addons = addonManager.loadAddons()
            val enabledAddons = addons.filter { it.enabled }
            val addonCommands = enabledAddons.flatMap { it.customCommands }
            val addonTemplates = enabledAddons.flatMap { it.customTemplates }
            val addonFirst = settingsManager.addonCompletionsFirst

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
                installedAddons = addons,
                addonCommands = addonCommands,
                addonTemplates = addonTemplates,
                addonCompletionsFirst = addonFirst
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

    fun setTheme(theme: AppTheme) {
        settingsManager.currentTheme = theme
        _uiState.value = _uiState.value.copy(currentTheme = theme)
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
        // ============ 物品类 (基岩版) ============
        CommandLibraryItem("give", "给予玩家物品", "/give <player: target> <itemName: Item> [amount: int] [data: int] [components: json]", "物品", Icons.Default.CardGiftcard),
        CommandLibraryItem("clear", "清除玩家物品", "/clear [player: target] [itemName: Item] [data: int] [maxCount: int]", "物品", Icons.Default.Delete),
        CommandLibraryItem("replaceitem", "替换物品栏物品", "/replaceitem entity <target: target> <slotType: EntityEquipmentSlot> <slotId: int> <itemName: Item> [amount: int] [data: int] [components: json]", "物品", Icons.Default.Inventory2),
        CommandLibraryItem("replaceitem-block", "替换容器物品", "/replaceitem block <position: x y z> slot.container <slotId: int> <itemName: Item> [amount: int] [data: int] [components: json]", "物品", Icons.Default.Inventory2),
        CommandLibraryItem("enchant", "附魔玩家手持物品", "/enchant <player: target> <enchantmentName: Enchant> [level: int]", "物品", Icons.Default.AutoAwesome),

        // ============ 实体类 (基岩版) ============
        CommandLibraryItem("summon", "生成实体", "/summon <entityType: EntityType> [spawnPos: x y z] [spawnEvent: string] [nameTag: string]", "实体", Icons.Default.Widgets),
        CommandLibraryItem("kill", "清除实体", "/kill [target: target]", "实体", Icons.Default.Gradient),
        CommandLibraryItem("effect", "添加状态效果", "/effect <player: target> <effect: Effect> [seconds: int] [amplifier: int] [hideParticles: Boolean]", "实体", Icons.Default.Speed),
        CommandLibraryItem("effect-clear", "清除状态效果", "/effect <player: target> clear", "实体", Icons.Default.CleaningServices),
        CommandLibraryItem("damage", "对实体造成伤害", "/damage <target: target> <amount: int> [cause: DamageCause] [entity: target]", "实体", Icons.Default.HeartBroken),
        CommandLibraryItem("ride", "控制骑乘状态", "/ride <riders: target> <start_riding|stop_riding|evict_riders|summon_rider|summon_ride> ...", "实体", Icons.Default.Directions),

        // ============ 传送类 (基岩版) ============
        CommandLibraryItem("tp", "传送到实体", "/tp <victim: target> <destination: target> [checkForBlocks: Boolean]", "传送", Icons.Default.SwapHoriz),
        CommandLibraryItem("tp-coords", "传送到坐标", "/tp <victim: target> <destination: x y z> [yRot: value] [xRot: value] [checkForBlocks: Boolean]", "传送", Icons.Default.LocationOn),
        CommandLibraryItem("tp-facing", "传送并面向目标", "/tp <victim: target> <destination: x y z> facing <lookAtEntity: target> [checkForBlocks: Boolean]", "传送", Icons.Default.NearMe),
        CommandLibraryItem("teleport", "传送(tp别名)", "/teleport <victim: target> <destination: target> [checkForBlocks: Boolean]", "传送", Icons.Default.NearMe),
        CommandLibraryItem("spawnpoint", "设置玩家出生点", "/spawnpoint [player: target] [spawnPos: x y z]", "传送", Icons.Default.Home),
        CommandLibraryItem("setworldspawn", "设置世界出生点", "/setworldspawn [spawnPoint: x y z]", "传送", Icons.Default.Public),
        CommandLibraryItem("spreadplayers", "随机散布实体", "/spreadplayers <x: value> <z: value> <spreadDistance: float> <maxRange: float> <victim: target>", "传送", Icons.Default.SwapHoriz),

        // ============ 方块类 (基岩版最新版) ============
        CommandLibraryItem("setblock", "放置方块", "/setblock <position: x y z> <tileName: Block> [blockStates: block states] [destroy|keep|replace]", "方块", Icons.Default.Create),
        CommandLibraryItem("fill", "填充区域", "/fill <from: x y z> <to: x y z> <tileName: Block> [blockStates: block states] [oldBlockHandling: FillMode]", "方块", Icons.Default.Map),
        CommandLibraryItem("fill-replace", "填充替换", "/fill <from: x y z> <to: x y z> <tileName: Block> replace [replaceTileName: Block] [replaceBlockStates: block states]", "方块", Icons.Default.Map),
        CommandLibraryItem("clone", "复制区域", "/clone <begin: x y z> <end: x y z> <destination: x y z> [maskMode: MaskMode] [cloneMode: CloneMode]", "方块", Icons.Default.ContentCopy),
        CommandLibraryItem("testforblock", "检测方块", "/testforblock <position: x y z> <tileName: Block> [blockStates: block states]", "方块", Icons.Default.GridOn),
        CommandLibraryItem("testforblocks", "检测区域方块", "/testforblocks <begin: x y z> <end: x y z> <destination: x y z> [mode: TestForBlocksMode]", "方块", Icons.Default.GridOn),

        // ============ 世界类 (基岩版) ============
        CommandLibraryItem("time", "时间设置", "/time <set|add|query> <amount: int>", "世界", Icons.Default.Schedule),
        CommandLibraryItem("time-set", "设置时间", "/time set <day|night|noon|midnight|sunrise|sunset|amount: int>", "世界", Icons.Default.Schedule),
        CommandLibraryItem("weather", "天气设置", "/weather <clear|rain|thunder> [duration: int]", "世界", Icons.Default.WbSunny),
        CommandLibraryItem("difficulty", "难度设置", "/difficulty <peaceful|easy|normal|hard>", "世界", Icons.Default.Shield),
        CommandLibraryItem("gamerule", "游戏规则", "/gamerule <rule: GameRule> [value: Boolean|int|float]", "世界", Icons.Default.Tune),
        CommandLibraryItem("gamerule-keepInventory", "死亡不掉落", "/gamerule keepInventory true", "世界", Icons.Default.Lock),
        CommandLibraryItem("gamerule-mobGriefing", "禁用生物破坏", "/gamerule mobGriefing false", "世界", Icons.Default.Block),
        CommandLibraryItem("locate", "查找结构或生物群系", "/locate <structure|biome> <feature: string>", "世界", Icons.Default.Search),
        CommandLibraryItem("tickingarea", "常加载区域", "/tickingarea add <from: x y z> <to: x y z> [name: string]", "世界", Icons.Default.AreaChart),
        CommandLibraryItem("tickingarea-circle", "圆形常加载区域", "/tickingarea add circle <center: x y z> <radius: int> [name: string]", "世界", Icons.Default.AreaChart),
        CommandLibraryItem("mobevent", "生物事件开关", "/mobevent <event: string> [value: Boolean]", "世界", Icons.Default.Tune),
        CommandLibraryItem("daylock", "锁定日夜循环", "/daylock [lock: Boolean]", "世界", Icons.Default.WbSunny),
        CommandLibraryItem("alwaysday", "daylock别名", "/alwaysday [lock: Boolean]", "世界", Icons.Default.WbSunny),

        // ============ 游戏模式 (基岩版) ============
        CommandLibraryItem("gamemode", "游戏模式", "/gamemode <survival|creative|adventure|spectator> [player: target]", "游戏模式", Icons.Default.VideogameAsset),
        CommandLibraryItem("gamemode-creative", "切换创造", "/gamemode creative [player: target]", "游戏模式", Icons.Default.Palette),
        CommandLibraryItem("gamemode-survival", "切换生存", "/gamemode survival [player: target]", "游戏模式", Icons.Default.SelfImprovement),

        // ============ 记分板 (基岩版) ============
        CommandLibraryItem("scoreboard", "记分板", "/scoreboard <objectives|players> ...", "记分板", Icons.Default.Leaderboard),
        CommandLibraryItem("scoreboard-objectives", "创建记分项", "/scoreboard objectives add <objective: string> dummy [displayName: string]", "记分板", Icons.Default.AddTask),
        CommandLibraryItem("scoreboard-players", "设置分数", "/scoreboard players <set|add|remove> <player: target> <objective: string> <count: int>", "记分板", Icons.Default.Edit),
        CommandLibraryItem("scoreboard-random", "随机分数", "/scoreboard players random <player: target> <objective: string> <min: int> <max: int>", "记分板", Icons.Default.Casino),
        CommandLibraryItem("scoreboard-test", "检测分数范围", "/scoreboard players test <player: target> <objective: string> <min: wildcard int> [max: wildcard int]", "记分板", Icons.Default.Check),
        CommandLibraryItem("scoreboard-operation", "分数运算", "/scoreboard players operation <player: target> <targetObjective: string> <operation: operator> <selector: target> <objective: string>", "记分板", Icons.Default.Calculate),
        CommandLibraryItem("scoreboard-setdisplay", "设置显示", "/scoreboard objectives setdisplay <list|sidebar|belowname> [objective: string] [ascending|descending]", "记分板", Icons.Default.Visibility),
        CommandLibraryItem("tag", "标签管理", "/tag <target: target> <add|remove|list> [name: string]", "记分板", Icons.Default.Label),

        // ============ 执行命令 (基岩版最新版) ============
        CommandLibraryItem("execute", "链式执行命令", "/execute <subcommand> ... <run|if|unless> ...", "执行", Icons.Default.Terminal),
        CommandLibraryItem("execute-run", "执行命令", "/execute as <origin: target> at <origin: target> run <command: command>", "执行", Icons.Default.PlayArrow),
        CommandLibraryItem("execute-if-block", "检测方块后执行", "/execute if block <position: x y z> <block: Block> [blockStates: block states] run <command: command>", "执行", Icons.Default.Check),
        CommandLibraryItem("execute-if-entity", "检测实体后执行", "/execute if entity <target: target> run <command: command>", "执行", Icons.Default.Check),
        CommandLibraryItem("function", "执行函数", "/function <name: filepath>", "执行", Icons.Default.Functions),

        // ============ 玩家互动 (基岩版) ============
        CommandLibraryItem("msg", "发送私信", "/msg <target: target> <message: message>", "玩家互动", Icons.Default.Message),
        CommandLibraryItem("tell", "发送私信", "/tell <target: target> <message: message>", "玩家互动", Icons.Default.Chat),
        CommandLibraryItem("w", "发送私信(短)", "/w <target: target> <message: message>", "玩家互动", Icons.Default.Forum),
        CommandLibraryItem("me", "动作消息", "/me <message: message>", "玩家互动", Icons.Default.Face),
        CommandLibraryItem("say", "广播消息", "/say <message: message>", "玩家互动", Icons.Default.Campaign),
        CommandLibraryItem("title", "标题显示", "/title <player: target> <clear|reset|title|subtitle|actionbar> [titleText: message]", "玩家互动", Icons.Default.FormatSize),
        CommandLibraryItem("titleraw", "标题显示(JSON)", "/titleraw <player: target> <clear|reset|title|subtitle|actionbar> [raw json message: json]", "玩家互动", Icons.Default.FormatSize),
        CommandLibraryItem("tellraw", "原始JSON消息", "/tellraw <player: target> <raw json message: json>", "玩家互动", Icons.Default.RssFeed),
        CommandLibraryItem("xp", "经验值", "/xp <amount: int> [player: target]", "玩家互动", Icons.Default.AutoGraph),
        CommandLibraryItem("xp-level", "经验等级", "/xp <amount: int>L [player: target]", "玩家互动", Icons.Default.Graph),

        // ============ 声音粒子 (基岩版) ============
        CommandLibraryItem("playsound", "播放声音", "/playsound <sound: string> [player: target] [position: x y z] [volume: float] [pitch: float] [minimumVolume: float]", "声音粒子", Icons.Default.VolumeUp),
        CommandLibraryItem("stopsound", "停止声音", "/stopsound <player: target> [sound: string]", "声音粒子", Icons.Default.VolumeOff),
        CommandLibraryItem("particle", "生成粒子", "/particle <effect: string> <position: x y z>", "声音粒子", Icons.Default.AutoFixHigh),
        CommandLibraryItem("music", "音乐控制", "/music <play|queue|stop|volume> ...", "声音粒子", Icons.Default.VolumeUp),

        // ============ 管理员 (基岩版) ============
        CommandLibraryItem("op", "给予管理权限", "/op <player: target>", "管理员", Icons.Default.AdminPanelSettings),
        CommandLibraryItem("deop", "移除管理权限", "/deop <player: target>", "管理员", Icons.Default.PersonRemove),
        CommandLibraryItem("kick", "踢出玩家", "/kick <name: target> [reason: message]", "管理员", Icons.Default.ExitToApp),
        CommandLibraryItem("list", "列出玩家", "/list", "管理员", Icons.Default.People),
        CommandLibraryItem("whitelist", "白名单", "/whitelist <add|remove|list|on|off|reload> [player: string]", "管理员", Icons.Default.VerifiedUser),
        CommandLibraryItem("allowlist", "白名单别名", "/allowlist <add|remove|list|on|off|reload> [player: string]", "管理员", Icons.Default.VerifiedUser),
        CommandLibraryItem("setmaxplayers", "设置最大玩家数", "/setmaxplayers <maxPlayers: int>", "管理员", Icons.Default.GroupAdd),
        CommandLibraryItem("permission", "权限管理", "/permission <list|reload>", "管理员", Icons.Default.AdminPanelSettings),
        CommandLibraryItem("ops", "权限管理别名", "/ops <list|reload>", "管理员", Icons.Default.AdminPanelSettings),

        // ============ 服务器 (基岩版) ============
        CommandLibraryItem("stop", "停止服务器", "/stop", "服务器", Icons.Default.Power),
        CommandLibraryItem("save", "保存世界", "/save <hold|query|resume>", "服务器", Icons.Default.Save),
        CommandLibraryItem("transfer", "转移服务器", "/transfer <player: target> <server: string> [port: int]", "服务器", Icons.Default.Public),
        CommandLibraryItem("wsserver", "连接WebSocket服务器", "/wsserver <serverUri: string>", "服务器", Icons.Default.Public),
        CommandLibraryItem("connect", "wsserver别名", "/connect <serverUri: string>", "服务器", Icons.Default.Public),

        // ============ 基岩版独有 ============
        CommandLibraryItem("ability", "玩家能力", "/ability <player: target> <ability: Ability> [value: Boolean]", "基岩版独有", Icons.Default.Accessibility),
        CommandLibraryItem("camera", "摄像机控制", "/camera <players: target> <clear|fade|set> ...", "基岩版独有", Icons.Default.CameraAlt),
        CommandLibraryItem("camerashake", "摄像机震动", "/camerashake <players: target> add [intensity: float] [seconds: float] [shakeType: CameraShakeType]", "基岩版独有", Icons.Default.CameraAlt),
        CommandLibraryItem("camerashake-stop", "停止摄像机震动", "/camerashake <players: target> stop", "基岩版独有", Icons.Default.CameraAlt),
        CommandLibraryItem("dialogue", "NPC对话", "/dialogue <open|change> ...", "基岩版独有", Icons.Default.RecordVoiceOver),
        CommandLibraryItem("event", "触发实体事件", "/event entity <target: target> <eventName: string>", "基岩版独有", Icons.Default.Bolt),
        CommandLibraryItem("fog", "迷雾设置", "/fog <victim: target> <push|pop|remove> ...", "基岩版独有", Icons.Default.Cloud),
        CommandLibraryItem("hud", "HUD显示控制", "/hud <target: target> <hide|reset> <hud_element: HudElement>", "基岩版独有", Icons.Default.Visibility),
        CommandLibraryItem("inputpermission", "输入权限", "/inputpermission <query|set> <player: target> ...", "基岩版独有", Icons.Default.TouchApp),
        CommandLibraryItem("playanimation", "播放动画", "/playanimation <entity: target> <animation: string> [next_state: string] [blend_out_time: float] [stop_expression: string] [controller: string]", "基岩版独有", Icons.Default.Movie),
        CommandLibraryItem("querytarget", "查询目标数据", "/querytarget <target: target>", "基岩版独有", Icons.Default.Search),
        CommandLibraryItem("structure", "结构保存加载", "/structure <save|load|delete> <name: string> ...", "基岩版独有", Icons.Default.Apartment),
        CommandLibraryItem("scriptevent", "脚本事件", "/scriptevent <messageId: string> [message: message]", "基岩版独有", Icons.Default.Code),
        CommandLibraryItem("worldbuilder", "世界建造者模式", "/worldbuilder", "基岩版独有", Icons.Default.Build),
        CommandLibraryItem("gametips", "游戏提示", "/gametips <player: target> <tip: string>", "基岩版独有", Icons.Default.Lightbulb),

        // ============ 帮助与检测 ============
        CommandLibraryItem("help", "命令帮助", "/help [page: int|command: CommandName]", "帮助", Icons.Default.Help),
        CommandLibraryItem("?", "命令帮助别名", "/? [page: int|command: CommandName]", "帮助", Icons.Default.Help),
        CommandLibraryItem("testfor", "检测实体", "/testfor <victim: target>", "帮助", Icons.Default.Check),

        // ============ 常用速查 (基岩版) ============
        CommandLibraryItem("速查-死亡不掉落", "开启死亡不掉落", "/gamerule keepInventory true", "速查", Icons.Default.Lock),
        CommandLibraryItem("速查-禁用生物破坏", "禁用爬行者破坏", "/gamerule mobGriefing false", "速查", Icons.Default.Block),
        CommandLibraryItem("速查-白天", "设置为白天", "/time set day", "速查", Icons.Default.LightMode),
        CommandLibraryItem("速查-晴天", "设置为晴天", "/weather clear", "速查", Icons.Default.WbSunny),
        CommandLibraryItem("速查-创造模式", "切换创造模式", "/gamemode creative @s", "速查", Icons.Default.Palette),
        CommandLibraryItem("速查-生存模式", "切换生存模式", "/gamemode survival @s", "速查", Icons.Default.SelfImprovement),
        CommandLibraryItem("速查-给予钻石", "给自己64个钻石", "/give @s diamond 64", "速查", Icons.Default.Diamond),
        CommandLibraryItem("速查-隐身效果", "获得隐身效果并隐藏粒子", "/effect @s invisibility 600 1 true", "速查", Icons.Default.VisibilityOff),
        CommandLibraryItem("速查-显示坐标", "开启显示坐标", "/gamerule showCoordinates true", "速查", Icons.Default.LocationOn),
        CommandLibraryItem("速查-常加载区域", "添加常加载", "/tickingarea add ~ ~ ~ ~10 ~10 ~10", "速查", Icons.Default.AreaChart),
    )
}
