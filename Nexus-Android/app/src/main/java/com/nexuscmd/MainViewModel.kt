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
        // ============ 物品类 ============
        CommandLibraryItem("give", "给予玩家物品", "/give <targets> <item> [count]", "物品", Icons.Default.CardGiftcard),
        CommandLibraryItem("clear", "清除物品", "/clear [targets] [item] [maxCount]", "物品", Icons.Default.Delete),
        CommandLibraryItem("item", "物品栏操作", "/item replace <target> <slot> with <item>", "物品", Icons.Default.Inventory2),
        CommandLibraryItem("enchant", "附魔物品", "/enchant <targets> <enchantment> [level]", "物品", Icons.Default.AutoAwesome),
        CommandLibraryItem("loot", "战利品表", "/loot spawn <targetPos> loot <lootTable>", "物品", Icons.Default.Inventory),

        // ============ 实体类 ============
        CommandLibraryItem("summon", "生成实体", "/summon <entity> [pos] [nbt]", "实体", Icons.Default.Widgets),
        CommandLibraryItem("kill", "删除实体", "/kill [targets]", "实体", Icons.Default.Gradient),
        CommandLibraryItem("effect", "药水效果", "/effect <give|clear> <targets> [effect] [seconds] [amplifier]", "实体", Icons.Default.Speed),
        CommandLibraryItem("damage", "造成伤害", "/damage <target> <amount> [cause]", "实体", Icons.Default.HeartBroken),
        CommandLibraryItem("ride", "骑乘实体", "/ride <target> mount <vehicle>", "实体", Icons.Default.Directions),
        CommandLibraryItem("data", "实体数据", "/data get entity <target>", "实体", Icons.Default.Dataset),

        // ============ 传送类 ============
        CommandLibraryItem("tp", "传送玩家", "/tp [targets] <destination>", "传送", Icons.Default.SwapHoriz),
        CommandLibraryItem("teleport", "传送(完整)", "/teleport <targets> <location> [yRot] [xRot]", "传送", Icons.Default.NearMe),
        CommandLibraryItem("spawnpoint", "设置出生点", "/spawnpoint [targets] [pos]", "传送", Icons.Default.Home),
        CommandLibraryItem("setworldspawn", "设置世界出生点", "/setworldspawn [pos] [angle]", "传送", Icons.Default.Public),

        // ============ 方块类 ============
        CommandLibraryItem("setblock", "放置方块", "/setblock <pos> <block> [destroy|keep|replace]", "方块", Icons.Default.Create),
        CommandLibraryItem("fill", "填充区域", "/fill <from> <to> <block> [replace|destroy|hollow|outline|keep]", "方块", Icons.Default.Map),
        CommandLibraryItem("clone", "复制区域", "/clone <begin> <end> <destination>", "方块", Icons.Default.ContentCopy),
        CommandLibraryItem("fillbiome", "改变生物群系", "/fillbiome <from> <to> <biome>", "方块", Icons.Default.Nature),

        // ============ 世界类 ============
        CommandLibraryItem("time", "时间设置", "/time <set|add|query> <value>", "世界", Icons.Default.Schedule),
        CommandLibraryItem("weather", "天气设置", "/weather <clear|rain|thunder> [duration]", "世界", Icons.Default.WbSunny),
        CommandLibraryItem("worldborder", "世界边界", "/worldborder <set|add|center|damage|warning|get>", "世界", Icons.Default.BorderAll),
        CommandLibraryItem("difficulty", "难度设置", "/difficulty <peaceful|easy|normal|hard>", "世界", Icons.Default.Shield),
        CommandLibraryItem("gamerule", "游戏规则", "/gamerule <rule> [value]", "世界", Icons.Default.Tune),
        CommandLibraryItem("gamerule-keepInventory", "死亡不掉落", "/gamerule keepInventory true", "世界", Icons.Default.Lock),
        CommandLibraryItem("gamerule-mobGriefing", "禁用生物破坏", "/gamerule mobGriefing false", "世界", Icons.Default.Block),
        CommandLibraryItem("defaultgamemode", "默认游戏模式", "/defaultgamemode <survival|creative|adventure|spectator>", "世界", Icons.Default.SportsEsports),
        CommandLibraryItem("tick", "游戏速度", "/tick <rate|freeze|step|sprint|query>", "世界", Icons.Default.Speed),
        CommandLibraryItem("locate", "查找结构", "/locate structure <structure>", "世界", Icons.Default.Search),

        // ============ 游戏模式 ============
        CommandLibraryItem("gamemode", "游戏模式", "/gamemode <survival|creative|adventure|spectator> [targets]", "游戏模式", Icons.Default.VideogameAsset),
        CommandLibraryItem("gamemode-creative", "切换创造", "/gamemode creative", "游戏模式", Icons.Default.Palette),
        CommandLibraryItem("gamemode-survival", "切换生存", "/gamemode survival", "游戏模式", Icons.Default.SelfImprovement),

        // ============ 记分板 ============
        CommandLibraryItem("scoreboard", "记分板", "/scoreboard <objectives|players|teams|displays>", "记分板", Icons.Default.Leaderboard),
        CommandLibraryItem("scoreboard-objectives", "创建记分项", "/scoreboard objectives add <name> <criteria> [displayName]", "记分板", Icons.Default.AddTask),
        CommandLibraryItem("scoreboard-players", "设置分数", "/scoreboard players set <targets> <objective> <score>", "记分板", Icons.Default.Edit),
        CommandLibraryItem("scoreboard-setdisplay", "设置显示位置", "/scoreboard objectives setdisplay <list|sidebar|belowName> [objective]", "记分板", Icons.Default.Visibility),
        CommandLibraryItem("team", "队伍管理", "/team add <team>", "记分板", Icons.Default.Groups),
        CommandLibraryItem("tag", "标签管理", "/tag <targets> add <name>", "记分板", Icons.Default.Label),

        // ============ 执行命令 ============
        CommandLibraryItem("execute", "执行命令", "/execute <subcommand> run <command>", "执行", Icons.Default.Terminal),
        CommandLibraryItem("execute-as", "以实体身份执行", "/execute as <targets> run <command>", "执行", Icons.Default.Person),
        CommandLibraryItem("execute-at", "在位置执行", "/execute at <targets> run <command>", "执行", Icons.Default.LocationOn),
        CommandLibraryItem("execute-if-entity", "检测实体条件", "/execute if entity <targets> run <command>", "执行", Icons.Default.Check),
        CommandLibraryItem("execute-if-block", "检测方块条件", "/execute if block <pos> <block> run <command>", "执行", Icons.Default.GridOn),
        CommandLibraryItem("execute-positioned", "移动位置执行", "/execute positioned <pos> run <command>", "执行", Icons.Default.SwipeRight),
        CommandLibraryItem("execute-in", "切换维度执行", "/execute in <dimension> run <command>", "执行", Icons.Default.Layers),
        CommandLibraryItem("function", "执行函数", "/function <functionName>", "执行", Icons.Default.Functions),

        // ============ 玩家互动 ============
        CommandLibraryItem("msg", "发送私信", "/msg <target> <message>", "玩家互动", Icons.Default.Message),
        CommandLibraryItem("tell", "发送私信", "/tell <target> <message>", "玩家互动", Icons.Default.Chat),
        CommandLibraryItem("w", "发送私信(短)", "/w <target> <message>", "玩家互动", Icons.Default.Forum),
        CommandLibraryItem("me", "动作消息", "/me <action>", "玩家互动", Icons.Default.Face),
        CommandLibraryItem("say", "广播消息", "/say <message>", "玩家互动", Icons.Default.Campaign),
        CommandLibraryItem("tellraw", "原始JSON消息", "/tellraw <targets> <rawjson>", "玩家互动", Icons.Default.RssFeed),
        CommandLibraryItem("title", "标题显示", "/title <targets> title <title>", "玩家互动", Icons.Default.FormatSize),
        CommandLibraryItem("xp", "经验值", "/xp <add|set|query> <targets> <amount>", "玩家互动", Icons.Default.AutoGraph),
        CommandLibraryItem("seed", "世界种子", "/seed", "玩家互动", Icons.Default.Eco),

        // ============ 声音粒子 ============
        CommandLibraryItem("playsound", "播放声音", "/playsound <sound> <source> <targets> [pos] [volume] [pitch]", "声音粒子", Icons.Default.VolumeUp),
        CommandLibraryItem("stopsound", "停止声音", "/stopsound <targets> [source] [sound]", "声音粒子", Icons.Default.VolumeOff),
        CommandLibraryItem("particle", "生成粒子", "/particle <name> <pos> [delta] [speed] [count]", "声音粒子", Icons.Default.AutoFixHigh),

        // ============ 管理员 ============
        CommandLibraryItem("op", "给予管理权限", "/op <targets>", "管理员", Icons.Default.AdminPanelSettings),
        CommandLibraryItem("deop", "移除管理权限", "/deop <targets>", "管理员", Icons.Default.PersonRemove),
        CommandLibraryItem("ban", "封禁玩家", "/ban <targets> [reason]", "管理员", Icons.Default.Block),
        CommandLibraryItem("pardon", "解封玩家", "/pardon <targets>", "管理员", Icons.Default.PersonAdd),
        CommandLibraryItem("ban-ip", "封禁IP", "/ban-ip <target> [reason]", "管理员", Icons.Default.Gavel),
        CommandLibraryItem("pardon-ip", "解封IP", "/pardon-ip <target>", "管理员", Icons.Default.Refresh),
        CommandLibraryItem("kick", "踢出玩家", "/kick <targets> [reason]", "管理员", Icons.Default.ExitToApp),
        CommandLibraryItem("whitelist", "白名单", "/whitelist <add|remove|list|on|off|reload> [player]", "管理员", Icons.Default.VerifiedUser),
        CommandLibraryItem("list", "列出玩家", "/list", "管理员", Icons.Default.People),

        // ============ 服务器 ============
        CommandLibraryItem("stop", "停止服务器", "/stop", "服务器", Icons.Default.Power),
        CommandLibraryItem("reload", "重载数据包", "/reload", "服务器", Icons.Default.Refresh),
        CommandLibraryItem("save-all", "保存世界", "/save-all", "服务器", Icons.Default.Save),
        CommandLibraryItem("save-off", "关闭自动保存", "/save-off", "服务器", Icons.Default.Pause),
        CommandLibraryItem("save-on", "开启自动保存", "/save-on", "服务器", Icons.Default.PlayArrow),

        // ============ 进阶命令 ============
        CommandLibraryItem("advancement", "进度管理", "/advancement grant <targets> everything", "进阶", Icons.Default.WorkspacePremium),
        CommandLibraryItem("recipe", "配方管理", "/recipe give <targets> <recipe>", "进阶", Icons.Default.MenuBook),
        CommandLibraryItem("spectate", "旁观实体", "/spectate [target] [player]", "进阶", Icons.Default.Visibility),
        CommandLibraryItem("bossbar", "Boss条管理", "/bossbar add <id> <name>", "进阶", Icons.Default.SignalCellularAlt),
        CommandLibraryItem("attribute", "属性修改", "/attribute <target> <attribute> base set <value>", "进阶", Icons.Default.TrendingUp),
        CommandLibraryItem("schedule", "调度函数", "/schedule function <function> <time>", "进阶", Icons.Default.Schedule),
        CommandLibraryItem("structure", "结构生成", "/place template <template> <pos>", "进阶", Icons.Default.Apartment),

        // ============ 调试工具 ============
        CommandLibraryItem("help", "命令帮助", "/help [command]", "帮助", Icons.Default.Help),
        CommandLibraryItem("debug", "调试模式", "/debug <start|stop|report>", "帮助", Icons.Default.BugReport),
        CommandLibraryItem("datapack", "数据包管理", "/datapack list", "帮助", Icons.Default.Folder),

        // ============ 常用速查 ============
        CommandLibraryItem("速查-死亡不掉落", "开启死亡不掉落", "/gamerule keepInventory true", "速查", Icons.Default.Lock),
        CommandLibraryItem("速查-禁用生物破坏", "禁用爬行者破坏", "/gamerule mobGriefing false", "速查", Icons.Default.Block),
        CommandLibraryItem("速查-白天", "设置为白天", "/time set day", "速查", Icons.Default.LightMode),
        CommandLibraryItem("速查-晴天", "设置为晴天", "/weather clear", "速查", Icons.Default.WbSunny),
        CommandLibraryItem("速查-创造模式", "切换创造模式", "/gamemode creative", "速查", Icons.Default.Palette),
        CommandLibraryItem("速查-生存模式", "切换生存模式", "/gamemode survival", "速查", Icons.Default.SelfImprovement),
        CommandLibraryItem("速查-传送到出生地", "传送到出生点", "/spawnpoint @s", "速查", Icons.Default.Home),
        CommandLibraryItem("速查-给予一组钻石", "给自己64个钻石", "/give @s diamond 64", "速查", Icons.Default.Diamond),
    )
}
