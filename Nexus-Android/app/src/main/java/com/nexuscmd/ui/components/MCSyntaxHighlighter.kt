package com.nexuscmd.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

// Minecraft command syntax colors
object SyntaxColors {
    // Command parts
    val Command = Color(0xFF56B6C2)           // 青色 - /give, /tp
    val CommandPrefix = Color(0xFF888888)     // 灰色 - 斜杠 /

    // Selectors
    val Selector = Color(0xFF61AFEF)           // 蓝色 - @p, @a, @e
    val SelectorBracket = Color(0xFF5C6370)   // 暗灰 - [ ]

    // Coordinates
    val Coordinate = Color(0xFFD19A66)        // 橙色 - ~10, ^5
    val Number = Color(0xFFB5CEA8)            // 浅绿 - 数字

    // Strings and text
    val String = Color(0xFF98C379)            // 绿色 - "text"
    val NBTKey = Color(0xFFE06C75)            // 红色 - {CustomName:
    val NBTValue = Color(0xFF98C379)         // 绿色 - "value"

    // Item and block IDs
    val ItemId = Color(0xFFE5C07B)            // 黄色 - minecraft:diamond
    val BlockId = Color(0xFFE5C07B)           // 黄色 - minecraft:stone

    // Effects and enchantments
    val EffectId = Color(0xFFC678DD)         // 紫色 - minecraft:speed
    val EnchantId = Color(0xFFC678DD)         // 紫色 - minecraft:sharpness

    // Keywords
    val Keyword = Color(0xFFC678DD)           // 紫色 - if, run, set
    val Boolean = Color(0xFF56B6C2)           // 青色 - true, false

    // Operators
    val Operator = Color(0xFFABB2BF)         // 白色 - :, =

    // Comments
    val Comment = Color(0xFF5C6370)           // 暗灰 - 注释

    // Error
    val Error = Color(0xFFE06C75)             // 红色 - 错误
    val ErrorUnderline = Color(0xFFBE5046)    // 深红 - 下划线
}

data class SyntaxToken(
    val type: TokenType,
    val text: String,
    val start: Int,
    val end: Int
)

enum class TokenType {
    COMMAND,
    SELECTOR,
    SELECTOR_BRACKET,
    COORDINATE,
    NUMBER,
    STRING,
    NBT_KEY,
    NBT_VALUE,
    ITEM_ID,
    BLOCK_ID,
    EFFECT_ID,
    ENCHANT_ID,
    KEYWORD,
    BOOLEAN,
    OPERATOR,
    ERROR,
    NORMAL
}

class MCSyntaxHighlighter {

    // 基岩版完整命令列表
    private val commandNames = setOf(
        // 基础命令
        "give", "summon", "tp", "teleport", "setblock", "fill", "clear",
        "effect", "enchant", "kill", "replaceitem", "spawnpoint",
        "time", "weather", "gamerule", "difficulty", "gamemode", "defaultgamemode",
        "scoreboard", "execute", "tell", "msg", "w", "me", "say", "team",
        "title", "playsound", "stopsound", "particle", "function", "schedule",
        "advancement", "recipe", "loot", "bossbar", "tag", "trigger",
        "worldborder", "kick", "ban", "banip", "pardon", "pardon-ip",
        "op", "deop", "list", "whitelist", "publish", "save", "stop", "reload",
        
        // 基岩版独有命令
        "ability", "alwaysday", "camerashake", "clearspawnpoint", "clone",
        "connect", "damage", "deop", "difficulty", "effect", "enchant",
        "event", "execute", "fill", "forceload", "function", "gamemode",
        "gamerule", "give", "globalpause", "help", "immutableworld", "item",
        "jfr", "kick", "kill", "lesson", "list", "locate", "locatebiome",
        "loot", "marshal", "mask", "massawake", "mobs", "music", "network",
        "op", "ops", "pardon", "pardonip", "particle", "perf", "playanimation",
        "playsound", "publish", "ride", "save", "save query", "save resume",
        "save hold", "save", "schedule", "scoreboard", "setblock", "setidletimeout",
        "setspawnpoint", "setworldspawn", "spawnpoint", "spreadplayers", "stop",
        "stopsound", "structure", "summon", "tag", "teleport", "tell", "tellraw",
        "test", "testfor", "testforblock", "testforblocks", "tickingarea",
        "time", "title", "titleraw", "toggledownfall", "tp", "transfer",
        "volumearea", "wb", "worldbuilder", "wsserver",
        
        // 教育版命令
        "allowlist", "askraw", "blockplace", "blockpush", "broadcast",
        "bubblecabinet", "c", "casueffect", "classroom", "clearid",
        "closechat", "code", "connectwire", "count", "createagent",
        "damage", "debug", "detect", "detectredstone", "disconnectwire",
        "drop", "dropitem", "equip", "execute", "exit", "exp", "export",
        "fill", "fillbiome", "fog", "getchunkdata", "getclientvar",
        "getlocalplayername", "getplayercount", "getspawnpoint", "getwd",
        "give", "globalsound", "goto", "haschat", "hascontaineropen",
        "health", "hit", "import", "index", "inspect", "join", "kick",
        "kill", "list", "localevent", "log", "move", "msg", "music",
        "mute", "noclip", "notecraft", "particle", "play", "playanimation",
        "playerfalling", "players", "pos", "random", "release",
        "render", "ride", "rotate", "run", "scan", "score", "script",
        "select", "set", "setblock", "setchatqueue", "setmaxplayers",
        "setmotor", "setnosprint", "setplayername", "setpos", "setrole",
        "setspawn", "settimer", "settp", "setunlock", "shake", "show",
        "shutdownevents", "sig", "sound", "spawn", "spawndata", "spawnexplosions",
        "spawnitem", "spawnpoint", "spawnpxz", "spawnrelative", "stopsound",
        "structure", "summon", "tag", "talking", "teleport", "tell",
        "test", "testfor", "testforblock", "testforblocks", "tick",
        "title", "tickingarea", "time", "titleraw", "tm", "tp", "trace",
        "transfer", "unlockachievements", "volumearea", "walk", "weather",
        "worldbuilder", "wsserver", "xp"
    )

    // 基岩版execute子命令
    private val keywords = setOf(
        "if", "unless", "at", "as", "in", "on", "positioned", "rotated",
        "facing", "anchored", "entity", "blocks", "store", "run", "set",
        "add", "remove", "list", "get", "reset", "operation",
        // 基岩版特有关键词
        "covalence", "detect", "forward", "horse", "owner", "radius",
        "rx", "rxm", "ry", "rym", "score_", "tag_", "type_", "x_", "xm", "y", "ym", "z", "zm",
        // 选择器参数
        "name", "type", "level", "game_mode", "x", "y", "z", "dx", "dy", "dz",
        "distance", "scores", "tag", "team", "limit", "sort", "sort_nearest",
        "sort_random", "sort_distance", "sort_arbitrary"
    )

    private val effects = setOf(
        "speed", "slowness", "haste", "mining_fatigue", "strength", "instant_health",
        "instant_damage", "jump_boost", "regeneration", "resistance", "fire_resistance",
        "water_breathing", "invisibility", "blindness", "night_vision", "hunger",
        "weakness", "poison", "wither", "health_boost", "absorption", "saturation",
        "glowing", "levitation", "luck", "luck_of_the_sea", "lure", "slow_falling",
        "conduit_power", "dolphins_grace", "bad_omen", "hero_of_the_village",
        "darkness", "wind_charged", "weaving", "spore_flower", "sniffer_egg"
    )

    private val enchantments = setOf(
        "protection", "fire_protection", "feather_falling", "blast_protection",
        "projectile_protection", "thorns", "respiration", "depth_strider",
        "aqua_affinity", "sharpness", "smite", "bane_of_arthropods", "knockback",
        "fire_aspect", "looting", "sweeping_edge", "efficiency", "silk_touch",
        "unbreaking", "fortune", "power", "punch", "flame", "infinity",
        "luck_of_the_sea", "lure", "loyalty", "impaling", "riptide", "channeling",
        "multishot", "quick_charge", "piercing", "mending", "vanishing_curse",
        "binding_curse", "soul_speed", "swift_sneak"
    )

    fun highlight(command: String): AnnotatedString {
        if (command.isEmpty()) return AnnotatedString("")

        return buildAnnotatedString {
            var pos = 0
            val len = command.length

            while (pos < len) {
                val remaining = command.substring(pos)

                // Check for command prefix /
                if (command[pos] == '/' && pos == 0) {
                    appendStyled("/", TokenType.NORMAL)
                    pos++

                    // Read command name
                    val cmdEnd = command.indexOf(' ', pos).let { if (it == -1) len else it }
                    val cmdName = command.substring(pos, cmdEnd).lowercase()

                    if (cmdName in commandNames) {
                        appendStyled(cmdName, TokenType.COMMAND)
                    } else {
                        appendStyled(cmdName, TokenType.NORMAL)
                    }
                    pos = cmdEnd
                    continue
                }

                // Skip whitespace
                if (command[pos] == ' ' || command[pos] == '\t') {
                    append(command[pos])
                    pos++
                    continue
                }

                // Check for selector @p, @a, @e, @s, @r
                if (command[pos] == '@') {
                    val selectorEnd = findSelectorEnd(command, pos)
                    val selector = command.substring(pos, selectorEnd)
                    appendStyled(selector, TokenType.SELECTOR)
                    pos = selectorEnd
                    continue
                }

                // Check for quoted string
                if (command[pos] == '"') {
                    val stringEnd = findStringEnd(command, pos)
                    val str = command.substring(pos, stringEnd)
                    appendStyled(str, TokenType.STRING)
                    pos = stringEnd
                    continue
                }

                // Check for NBT brace {
                if (command[pos] == '{') {
                    val braceEnd = findNBTEnd(command, pos)
                    val nbt = command.substring(pos, braceEnd)
                    highlightNBT(nbt, this)
                    pos = braceEnd
                    continue
                }

                // Check for coordinates ~ ^ -
                if (command[pos] == '~' || command[pos] == '^' ||
                    (command[pos] == '-' && (pos + 1 >= len || command[pos + 1].isDigit()))) {
                    val coordEnd = findCoordinateEnd(command, pos)
                    val coord = command.substring(pos, coordEnd)
                    appendStyled(coord, TokenType.COORDINATE)
                    pos = coordEnd
                    continue
                }

                // Check for number
                if (command[pos].isDigit()) {
                    val numEnd = findNumberEnd(command, pos)
                    val num = command.substring(pos, numEnd)
                    appendStyled(num, TokenType.NUMBER)
                    pos = numEnd
                    continue
                }

                // Check for item/block ID (minecraft:xxx)
                if (remaining.startsWith("minecraft:") || remaining.contains("minecraft:")) {
                    val idEnd = findIdEnd(command, pos)
                    val id = command.substring(pos, idEnd)
                    val type = when {
                        id.contains("enchantment") || enchantments.any { id.endsWith(it) } -> TokenType.ENCHANT_ID
                        effects.any { id.endsWith(it) } -> TokenType.EFFECT_ID
                        id.contains(":") -> TokenType.ITEM_ID
                        else -> TokenType.NORMAL
                    }
                    appendStyled(id, type)
                    pos = idEnd
                    continue
                }

                // Check for keyword
                val wordEnd = findWordEnd(command, pos)
                val word = command.substring(pos, wordEnd)
                val wordLower = word.lowercase()

                when {
                    wordLower in keywords -> appendStyled(word, TokenType.KEYWORD)
                    wordLower == "true" || wordLower == "false" -> appendStyled(word, TokenType.BOOLEAN)
                    else -> append(word)
                }
                pos = wordEnd
            }
        }
    }

    private fun AnnotatedString.Builder.appendStyled(text: String, type: TokenType) {
        val style = when (type) {
            TokenType.COMMAND -> SpanStyle(color = SyntaxColors.Command, fontWeight = FontWeight.Bold)
            TokenType.SELECTOR -> SpanStyle(color = SyntaxColors.Selector, fontWeight = FontWeight.Bold)
            TokenType.SELECTOR_BRACKET -> SpanStyle(color = SyntaxColors.SelectorBracket)
            TokenType.COORDINATE -> SpanStyle(color = SyntaxColors.Coordinate)
            TokenType.NUMBER -> SpanStyle(color = SyntaxColors.Number)
            TokenType.STRING -> SpanStyle(color = SyntaxColors.String, fontStyle = FontStyle.Italic)
            TokenType.NBT_KEY -> SpanStyle(color = SyntaxColors.NBTKey)
            TokenType.NBT_VALUE -> SpanStyle(color = SyntaxColors.NBTValue)
            TokenType.ITEM_ID -> SpanStyle(color = SyntaxColors.ItemId)
            TokenType.BLOCK_ID -> SpanStyle(color = SyntaxColors.BlockId)
            TokenType.EFFECT_ID -> SpanStyle(color = SyntaxColors.EffectId)
            TokenType.ENCHANT_ID -> SpanStyle(color = SyntaxColors.EnchantId)
            TokenType.KEYWORD -> SpanStyle(color = SyntaxColors.Keyword, fontWeight = FontWeight.Bold)
            TokenType.BOOLEAN -> SpanStyle(color = SyntaxColors.Boolean)
            TokenType.OPERATOR -> SpanStyle(color = SyntaxColors.Operator)
            TokenType.ERROR -> SpanStyle(color = SyntaxColors.Error)
            TokenType.NORMAL -> SpanStyle(color = SyntaxColors.Operator)
        }
        pushStyle(style)
        append(text)
        pop()
    }

    private fun findSelectorEnd(command: String, start: Int): Int {
        var pos = start + 1
        while (pos < command.length && (command[pos].isLetter() || command[pos] == '_' || command[pos] == '[' || command[pos] == ']')) {
            if (command[pos] == '[') {
                // Find matching ]
                var depth = 1
                pos++
                while (pos < command.length && depth > 0) {
                    if (command[pos] == '[') depth++
                    if (command[pos] == ']') depth--
                    pos++
                }
                break
            }
            pos++
        }
        return pos
    }

    private fun findStringEnd(command: String, start: Int): Int {
        var pos = start + 1
        while (pos < command.length) {
            if (command[pos] == '"' && command[pos - 1] != '\\') {
                pos++
                break
            }
            pos++
        }
        return pos
    }

    private fun findNBTEnd(command: String, start: Int): Int {
        var pos = start + 1
        var depth = 1
        var inString = false
        while (pos < command.length && depth > 0) {
            if (command[pos] == '"' && (pos == 0 || command[pos - 1] != '\\')) {
                inString = !inString
            }
            if (!inString) {
                if (command[pos] == '{') depth++
                if (command[pos] == '}') depth--
            }
            pos++
        }
        return pos
    }

    private fun findCoordinateEnd(command: String, start: Int): Int {
        var pos = start
        if (command[pos] == '~' || command[pos] == '^') pos++
        while (pos < command.length && (command[pos].isDigit() || command[pos] == '.' || command[pos] == '-')) {
            pos++
        }
        return pos
    }

    private fun findNumberEnd(command: String, start: Int): Int {
        var pos = start
        while (pos < command.length && (command[pos].isDigit() || command[pos] == '.')) {
            pos++
        }
        return pos
    }

    private fun findIdEnd(command: String, start: Int): Int {
        var pos = start
        while (pos < command.length && (command[pos].isLetter() || command[pos] == '_' ||
                   command[pos] == ':' || command[pos] == '-' || command[pos].isDigit())) {
            pos++
        }
        return pos
    }

    private fun findWordEnd(command: String, start: Int): Int {
        var pos = start
        while (pos < command.length && (command[pos].isLetter() || command[pos] == '_' || command[pos] == '-')) {
            pos++
        }
        return pos
    }

    private fun highlightNBT(nbt: String, builder: AnnotatedString.Builder) {
        var pos = 0
        val len = nbt.length
        var inKey = true
        var braceDepth = 0

        while (pos < len) {
            when {
                nbt[pos] == '{' -> {
                    builder.appendStyled("{", TokenType.NORMAL)
                    braceDepth++
                    inKey = true
                    pos++
                }
                nbt[pos] == '}' -> {
                    builder.appendStyled("}", TokenType.NORMAL)
                    braceDepth--
                    inKey = false
                    pos++
                }
                nbt[pos] == ':' -> {
                    builder.appendStyled(":", TokenType.OPERATOR)
                    inKey = false
                    pos++
                }
                nbt[pos] == ',' -> {
                    builder.appendStyled(",", TokenType.NORMAL)
                    inKey = true
                    pos++
                }
                nbt[pos] == '"' -> {
                    val end = findStringEnd(nbt, pos)
                    val str = nbt.substring(pos, end)
                    if (inKey) {
                        builder.appendStyled(str, TokenType.NBT_KEY)
                    } else {
                        builder.appendStyled(str, TokenType.NBT_VALUE)
                    }
                    pos = end
                }
                nbt[pos] == ' ' || nbt[pos] == '\t' -> {
                    builder.append(nbt[pos])
                    pos++
                }
                nbt[pos].isDigit() || nbt[pos] == '-' -> {
                    val end = findNumberEnd(nbt, pos)
                    builder.appendStyled(nbt.substring(pos, end), TokenType.NUMBER)
                    pos = end
                }
                else -> {
                    val end = findWordEnd(nbt, pos)
                    val word = nbt.substring(pos, end)
                    when {
                        word == "true" || word == "false" -> builder.appendStyled(word, TokenType.BOOLEAN)
                        word == "b" || word == "s" || word == "i" || word == "l" -> {
                            builder.appendStyled(word, TokenType.NUMBER)
                        }
                        else -> builder.appendStyled(word, TokenType.NBT_VALUE)
                    }
                    pos = end
                }
            }
        }
    }
}
