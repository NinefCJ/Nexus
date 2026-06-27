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
        // ============ 物品类 (基岩版) ============
        CommandLibraryItem("give", "给予玩家物品", "/give <player> <item> [amount] [data] [components]", "物品", Icons.Default.CardGiftcard),
        CommandLibraryItem("clear", "清除物品", "/clear [player] [item] [maxCount]", "物品", Icons.Default.Delete),
        CommandLibraryItem("replaceitem", "替换物品栏物品", "/replaceitem block <pos> slot.container <slot> <item> [amount] [data] [components]", "物品", Icons.Default.Inventory2),
        CommandLibraryItem("enchant", "附魔物品", "/enchant <player> <enchantmentId> [level]", "物品", Icons.Default.AutoAwesome),

        // ============ 实体类 (基岩版) ============
        CommandLibraryItem("summon", "生成实体", "/summon <entityType> [spawnPos] [spawnEvent] [nameTag]", "实体", Icons.Default.Widgets),
        CommandLibraryItem("kill", "删除实体", "/kill [target]", "实体", Icons.Default.Gradient),
        CommandLibraryItem("effect", "药水效果(基岩版)", "/effect <player> <effect> [seconds] [amplifier] [true]", "实体", Icons.Default.Speed),
        CommandLibraryItem("effect-clear", "清除效果", "/effect <player> clear", "实体", Icons.Default.CleaningServices),
        CommandLibraryItem("damage", "造成伤害", "/damage <target> <amount> [cause] [damager]", "实体", Icons.Default.HeartBroken),
        CommandLibraryItem("ride", "骑乘实体", "/ride <riders> <ride|evict>", "实体", Icons.Default.Directions),

        // ============ 传送类 (基岩版) ============
        CommandLibraryItem("tp", "传送玩家", "/tp <victim> <destination>", "传送", Icons.Default.SwapHoriz),
        CommandLibraryItem("tp-coords", "传送到坐标", "/tp <player> <x> <y> <z> [yRot] [xRot]", "传送", Icons.Default.LocationOn),
        CommandLibraryItem("tp-facing", "传送朝向", "/tp <player> <x> <y> <z> facing <lookAt>", "传送", Icons.Default.NearMe),
        CommandLibraryItem("teleport", "传送(完整)", "/teleport <target> <destination>", "传送", Icons.Default.NearMe),
        CommandLibraryItem("spawnpoint", "设置出生点", "/spawnpoint [player] [x] [y] [z]", "传送", Icons.Default.Home),
        CommandLibraryItem("setworldspawn", "设置世界出生点", "/setworldspawn [x] [y] [z]", "传送", Icons.Default.Public),

        // ============ 方块类 (基岩版) ============
        CommandLibraryItem("setblock", "放置方块", "/setblock <pos> <block> [tileData] [destroy|keep|replace]", "方块", Icons.Default.Create),
        CommandLibraryItem("fill", "填充区域", "/fill <from> <to> <block> [tileData] [destroy|hollow|keep|outline|replace]", "方块", Icons.Default.Map),
        CommandLibraryItem("fill-replace", "填充替换", "/fill <from> <to> <block> <tileData> replace <replaceBlock> <replaceData>", "方块", Icons.Default.Map),
        CommandLibraryItem("clone", "复制区域", "/clone <begin> <end> <destination> [maskMode] [cloneMode]", "方块", Icons.Default.ContentCopy),

        // ============ 世界类 (基岩版) ============
        CommandLibraryItem("time", "时间设置", "/time <set|add|query> <value>", "世界", Icons.Default.Schedule),
        CommandLibraryItem("time-set", "设置时间", "/time set <day|night|noon|midnight|sunrise|sunset|value>", "世界", Icons.Default.Schedule),
        CommandLibraryItem("weather", "天气设置", "/weather <clear|rain|thunder> [duration]", "世界", Icons.Default.WbSunny),
        CommandLibraryItem("difficulty", "难度设置", "/difficulty <peaceful|easy|normal|hard>", "世界", Icons.Default.Shield),
        CommandLibraryItem("gamerule", "游戏规则", "/gamerule <rule> [value]", "世界", Icons.Default.Tune),
        CommandLibraryItem("gamerule-keepInventory", "死亡不掉落", "/gamerule keepInventory true", "世界", Icons.Default.Lock),
        CommandLibraryItem("gamerule-mobGriefing", "禁用生物破坏", "/gamerule mobGriefing false", "世界", Icons.Default.Block),
        CommandLibraryItem("defaultgamemode", "默认游戏模式", "/defaultgamemode <survival|creative|adventure>", "世界", Icons.Default.SportsEsports),
        CommandLibraryItem("locate", "查找结构(基岩版)", "/locate <structure>", "世界", Icons.Default.Search),
        CommandLibraryItem("tickingarea", "常加载区域", "/tickingarea add <from> <to> [name]", "世界", Icons.Default.AreaChart),

        // ============ 游戏模式 (基岩版) ============
        CommandLibraryItem("gamemode", "游戏模式", "/gamemode <survival|creative|adventure|spectator> [player]", "游戏模式", Icons.Default.VideogameAsset),
        CommandLibraryItem("gamemode-creative", "切换创造", "/gamemode creative", "游戏模式", Icons.Default.Palette),
        CommandLibraryItem("gamemode-survival", "切换生存", "/gamemode survival", "游戏模式", Icons.Default.SelfImprovement),

        // ============ 记分板 (基岩版) ============
        CommandLibraryItem("scoreboard", "记分板", "/scoreboard <objectives|players>", "记分板", Icons.Default.Leaderboard),
        CommandLibraryItem("scoreboard-objectives", "创建记分项", "/scoreboard objectives add <name> <criteria> [displayName]", "记分板", Icons.Default.AddTask),
        CommandLibraryItem("scoreboard-players", "设置分数", "/scoreboard players <add|remove|set|reset> <player> <objective> <score>", "记分板", Icons.Default.Edit),
        CommandLibraryItem("scoreboard-setdisplay", "设置显示", "/scoreboard objectives setdisplay <list|sidebar|belowname> [objective]", "记分板", Icons.Default.Visibility),
        CommandLibraryItem("tag", "标签管理(基岩版)", "/tag <target> <add|remove|list> <name>", "记分板", Icons.Default.Label),

        // ============ 执行命令 (基岩版) ============
        CommandLibraryItem("execute", "执行命令(基岩版)", "/execute <target> <pos> <detect|run> <command>", "执行", Icons.Default.Terminal),
        CommandLibraryItem("execute-run", "执行命令", "/execute <target> <pos> run <command>", "执行", Icons.Default.PlayArrow),
        CommandLibraryItem("execute-detect", "检测执行", "/execute <target> <pos> detect <pos> <block> <data> <command>", "执行", Icons.Default.Check),
        CommandLibraryItem("function", "执行函数", "/function <name>", "执行", Icons.Default.Functions),

        // ============ 玩家互动 (基岩版) ============
        CommandLibraryItem("msg", "发送私信", "/msg <target> <message>", "玩家互动", Icons.Default.Message),
        CommandLibraryItem("tell", "发送私信", "/tell <target> <message>", "玩家互动", Icons.Default.Chat),
        CommandLibraryItem("w", "发送私信(短)", "/w <target> <message>", "玩家互动", Icons.Default.Forum),
        CommandLibraryItem("me", "动作消息", "/me <action>", "玩家互动", Icons.Default.Face),
        CommandLibraryItem("say", "广播消息", "/say <message>", "玩家互动", Icons.Default.Campaign),
        CommandLibraryItem("titleraw", "标题显示(JSON)", "/titleraw <player> <clear|reset|title|subtitle|actionbar> <rawtext>", "玩家互动", Icons.Default.FormatSize),
        CommandLibraryItem("tellraw", "原始JSON消息", "/tellraw <player> <rawtext>", "玩家互动", Icons.Default.RssFeed),
        CommandLibraryItem("xp", "经验值(基岩版)", "/xp <amount> [player]", "玩家互动", Icons.Default.AutoGraph),
        CommandLibraryItem("xp-level", "经验等级", "/xp <amount>L [player]", "玩家互动", Icons.Default.Graph),
        CommandLibraryItem("seed", "世界种子", "/seed", "玩家互动", Icons.Default.Eco),

        // ============ 声音粒子 (基岩版) ============
        CommandLibraryItem("playsound", "播放声音", "/playsound <sound> <source> <player> [pos] [volume] [pitch] [minVolume]", "声音粒子", Icons.Default.VolumeUp),
        CommandLibraryItem("stopsound", "停止声音", "/stopsound <player> [source] [sound]", "声音粒子", Icons.Default.VolumeOff),
        CommandLibraryItem("particle", "生成粒子", "/particle <effect> <pos>", "声音粒子", Icons.Default.AutoFixHigh),

        // ============ 管理员 (基岩版) ============
        CommandLibraryItem("op", "给予管理权限", "/op <player>", "管理员", Icons.Default.AdminPanelSettings),
        CommandLibraryItem("deop", "移除管理权限", "/deop <player>", "管理员", Icons.Default.PersonRemove),
        CommandLibraryItem("kick", "踢出玩家", "/kick <player> [reason]", "管理员", Icons.Default.ExitToApp),
        CommandLibraryItem("list", "列出玩家", "/list", "管理员", Icons.Default.People),
        CommandLibraryItem("allowlist", "白名单(基岩版)", "/allowlist <add|remove|list|on|off> [player]", "管理员", Icons.Default.VerifiedUser),
        CommandLibraryItem("setmaxplayers", "设置最大玩家数", "/setmaxplayers <max>", "管理员", Icons.Default.GroupAdd),

        // ============ 服务器 (基岩版) ============
        CommandLibraryItem("stop", "停止服务器", "/stop", "服务器", Icons.Default.Power),
        CommandLibraryItem("save", "保存世界", "/save <on|off|query|all|flush>", "服务器", Icons.Default.Save),

        // ============ 基岩版独特命令 ============
        CommandLibraryItem("camerashake", "摄像机震动", "/camerashake <players> <add|stop> [intensity] [duration] [shakeType]", "基岩版独有", Icons.Default.CameraAlt),
        CommandLibraryItem("dialogue", "NPC对话", "/dialogue <targets> change <npc>", "基岩版独有", Icons.Default.RecordVoiceOver),
        CommandLibraryItem("playanimation", "播放动画", "/playanimation <entity> <animation> [state]", "基岩版独有", Icons.Default.Movie),
        CommandLibraryItem("structure", "结构保存加载", "/structure <save|load|delete> <name> [pos]", "基岩版独有", Icons.Default.Apartment),
        CommandLibraryItem("scriptevent", "脚本事件", "/scriptevent <messageId> <message>", "基岩版独有", Icons.Default.Code),

        // ============ 调试工具 ============
        CommandLibraryItem("help", "命令帮助", "/help [page|command]", "帮助", Icons.Default.Help),
        CommandLibraryItem("testfor", "检测实体", "/testfor <target>", "帮助", Icons.Default.Check),
        CommandLibraryItem("testforblock", "检测方块", "/testforblock <pos> <block> [data]", "帮助", Icons.Default.GridOn),

        // ============ 常用速查 (基岩版) ============
        CommandLibraryItem("速查-死亡不掉落", "开启死亡不掉落", "/gamerule keepInventory true", "速查", Icons.Default.Lock),
        CommandLibraryItem("速查-禁用生物破坏", "禁用爬行者破坏", "/gamerule mobGriefing false", "速查", Icons.Default.Block),
        CommandLibraryItem("速查-白天", "设置为白天", "/time set day", "速查", Icons.Default.LightMode),
        CommandLibraryItem("速查-晴天", "设置为晴天", "/weather clear", "速查", Icons.Default.WbSunny),
        CommandLibraryItem("速查-创造模式", "切换创造模式", "/gamemode creative", "速查", Icons.Default.Palette),
        CommandLibraryItem("速查-生存模式", "切换生存模式", "/gamemode survival", "速查", Icons.Default.SelfImprovement),
        CommandLibraryItem("速查-给予钻石", "给自己64个钻石", "/give @s diamond 64", "速查", Icons.Default.Diamond),
        CommandLibraryItem("速查-隐身效果", "获得隐身效果", "/effect @s invisibility 600 1 true", "速查", Icons.Default.VisibilityOff),
        CommandLibraryItem("速查-常加载区域", "添加常加载", "/tickingarea add ~ ~ ~ ~10 ~10 ~10", "速查", Icons.Default.AreaChart),
    )
}
