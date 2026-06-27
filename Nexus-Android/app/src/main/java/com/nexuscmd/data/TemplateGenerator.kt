package com.nexuscmd.data

// Command templates for quick generation
data class CommandTemplate(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val template: String,
    val parameters: List<TemplateParameter>,
    val iconType: String = "default"
)

data class TemplateParameter(
    val name: String,
    val type: ParamType,
    val defaultValue: String,
    val options: List<String> = emptyList(),
    val description: String = ""
)

enum class ParamType {
    SELECTOR,      // @p, @a, @e, @s, @r
    PLAYER_NAME,   // 玩家名称
    ITEM_ID,       // 物品ID
    BLOCK_ID,      // 方块ID
    ENTITY_ID,     // 实体ID
    EFFECT_ID,     // 效果ID
    COORDINATE,    // 坐标 ~ ~ ~
    NUMBER,        // 数字
    STRING,        // 字符串
    BOOLEAN,       // true/false
    NBT,           // NBT数据
    SELECT         // 下拉选择
}

class TemplateGenerator {

    fun getTemplates(): List<CommandTemplate> = listOf(
        // 物品模板
        CommandTemplate(
            id = "give_basic",
            name = "给予物品",
            category = "物品",
            description = "给予玩家指定数量的物品",
            template = "/give <selector> <item> <amount>",
            parameters = listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("item", ParamType.ITEM_ID, "minecraft:diamond", emptyList(), "物品ID"),
                TemplateParameter("amount", ParamType.NUMBER, "1", emptyList(), "数量")
            )
        ),
        CommandTemplate(
            id = "give_enchanted",
            name = "给予附魔物品",
            category = "物品",
            description = "给予玩家带附魔的物品",
            template = "/give <selector> <item>{Enchantments:[{id:\"<enchant>\",lvl:<level>}]}",
            parameters = listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("item", ParamType.ITEM_ID, "minecraft:diamond_sword", emptyList(), "物品ID"),
                TemplateParameter("enchant", ParamType.SELECT, "minecraft:sharpness",
                    listOf("minecraft:sharpness", "minecraft:protection", "minecraft:efficiency", "minecraft:fortune", "minecraft:power"), "附魔类型"),
                TemplateParameter("level", ParamType.NUMBER, "5", emptyList(), "附魔等级")
            )
        ),
        CommandTemplate(
            id = "clear_items",
            name = "清除物品",
            category = "物品",
            description = "清除玩家指定物品",
            template = "/clear <selector> <item> <amount>",
            parameters = listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("item", ParamType.ITEM_ID, "minecraft:diamond", emptyList(), "物品ID"),
                TemplateParameter("amount", ParamType.NUMBER, "1", emptyList(), "数量(可选)")
            )
        ),

        // 实体模板
        CommandTemplate(
            id = "summon_basic",
            name = "生成实体",
            category = "实体",
            description = "在指定位置生成实体",
            template = "/summon <entity> <coords>",
            parameters = listOf(
                TemplateParameter("entity", ParamType.ENTITY_ID, "minecraft:zombie", emptyList(), "实体类型"),
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "生成位置")
            )
        ),
        CommandTemplate(
            id = "summon_named",
            name = "生成命名实体",
            category = "实体",
            description = "生成带自定义名称的实体",
            template = "/summon <entity> <coords> {CustomName:\"\\\"<name>\\\"\"}",
            parameters = listOf(
                TemplateParameter("entity", ParamType.ENTITY_ID, "minecraft:zombie", emptyList(), "实体类型"),
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "生成位置"),
                TemplateParameter("name", ParamType.STRING, "Boss", emptyList(), "实体名称")
            )
        ),
        CommandTemplate(
            id = "kill_entity",
            name = "删除实体",
            category = "实体",
            description = "删除指定实体",
            template = "/kill <selector>",
            parameters = listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@e", emptyList(), "目标实体")
            )
        ),

        // 效果模板
        CommandTemplate(
            id = "effect_give",
            name = "给予效果",
            category = "效果",
            description = "给予玩家药水效果",
            template = "/effect give <selector> <effect> <duration> <amplifier>",
            parameters = listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("effect", ParamType.EFFECT_ID, "minecraft:speed",
                    listOf("minecraft:speed", "minecraft:strength", "minecraft:regeneration", "minecraft:invisibility", "minecraft:night_vision"), "效果类型"),
                TemplateParameter("duration", ParamType.NUMBER, "30", emptyList(), "持续时间(秒)"),
                TemplateParameter("amplifier", ParamType.NUMBER, "1", emptyList(), "效果等级")
            )
        ),
        CommandTemplate(
            id = "effect_clear",
            name = "清除效果",
            category = "效果",
            description = "清除玩家的药水效果",
            template = "/effect clear <selector> <effect>",
            parameters = listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("effect", ParamType.EFFECT_ID, "minecraft:speed", emptyList(), "效果类型(可选)")
            )
        ),

        // 传送模板
        CommandTemplate(
            id = "tp_to_player",
            name = "传送到玩家",
            category = "传送",
            description = "传送玩家到另一个玩家位置",
            template = "/tp <selector> <target>",
            parameters = listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@s", emptyList(), "要传送的玩家"),
                TemplateParameter("target", ParamType.SELECTOR, "@p", emptyList(), "目标位置玩家")
            )
        ),
        CommandTemplate(
            id = "tp_to_coords",
            name = "传送到坐标",
            category = "传送",
            description = "传送玩家到指定坐标",
            template = "/tp <selector> <x> <y> <z>",
            parameters = listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@s", emptyList(), "要传送的玩家"),
                TemplateParameter("x", ParamType.NUMBER, "0", emptyList(), "X坐标"),
                TemplateParameter("y", ParamType.NUMBER, "64", emptyList(), "Y坐标"),
                TemplateParameter("z", ParamType.NUMBER, "0", emptyList(), "Z坐标")
            )
        ),
        CommandTemplate(
            id = "tp_relative",
            name = "相对传送",
            category = "传送",
            description = "相对传送玩家",
            template = "/tp <selector> ~<dx> ~<dy> ~<dz>",
            parameters = listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@s", emptyList(), "要传送的玩家"),
                TemplateParameter("dx", ParamType.NUMBER, "10", emptyList(), "X偏移"),
                TemplateParameter("dy", ParamType.NUMBER, "0", emptyList(), "Y偏移"),
                TemplateParameter("dz", ParamType.NUMBER, "0", emptyList(), "Z偏移")
            )
        ),

        // 方块模板
        CommandTemplate(
            id = "setblock",
            name = "放置方块",
            category = "方块",
            description = "在指定位置放置方块",
            template = "/setblock <coords> <block>",
            parameters = listOf(
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "放置位置"),
                TemplateParameter("block", ParamType.BLOCK_ID, "minecraft:stone", emptyList(), "方块类型")
            )
        ),
        CommandTemplate(
            id = "fill_basic",
            name = "填充区域",
            category = "方块",
            description = "填充一个区域",
            template = "/fill <from> <to> <block>",
            parameters = listOf(
                TemplateParameter("from", ParamType.COORDINATE, "~ ~ ~", emptyList(), "起点"),
                TemplateParameter("to", ParamType.COORDINATE, "~5 ~5 ~5", emptyList(), "终点"),
                TemplateParameter("block", ParamType.BLOCK_ID, "minecraft:stone", emptyList(), "方块类型")
            )
        ),
        CommandTemplate(
            id = "fill_replace",
            name = "替换方块",
            category = "方块",
            description = "替换区域内的特定方块",
            template = "/fill <from> <to> <block> replace <target>",
            parameters = listOf(
                TemplateParameter("from", ParamType.COORDINATE, "~ ~ ~", emptyList(), "起点"),
                TemplateParameter("to", ParamType.COORDINATE, "~5 ~5 ~5", emptyList(), "终点"),
                TemplateParameter("block", ParamType.BLOCK_ID, "minecraft:stone", emptyList(), "新方块"),
                TemplateParameter("target", ParamType.BLOCK_ID, "minecraft:dirt", emptyList(), "要替换的方块")
            )
        ),

        // 世界模板
        CommandTemplate(
            id = "time_set",
            name = "设置时间",
            category = "世界",
            description = "设置游戏时间",
            template = "/time set <time>",
            parameters = listOf(
                TemplateParameter("time", ParamType.SELECT, "day",
                    listOf("day", "noon", "night", "midnight", "sunrise", "sunset", "0", "6000", "12000", "18000"), "时间")
            )
        ),
        CommandTemplate(
            id = "weather",
            name = "设置天气",
            category = "世界",
            description = "设置游戏天气",
            template = "/weather <weather> <duration>",
            parameters = listOf(
                TemplateParameter("weather", ParamType.SELECT, "clear",
                    listOf("clear", "rain", "thunder"), "天气类型"),
                TemplateParameter("duration", ParamType.NUMBER, "6000", emptyList(), "持续时间")
            )
        ),
        CommandTemplate(
            id = "gamerule",
            name = "设置游戏规则",
            category = "世界",
            description = "修改游戏规则",
            template = "/gamerule <rule> <value>",
            parameters = listOf(
                TemplateParameter("rule", ParamType.SELECT, "keepInventory",
                    listOf("keepInventory", "mobGriefing", "doDaylightCycle", "doWeatherCycle", "doMobSpawning", "doTileDrops", "commandBlockOutput", "sendCommandFeedback"), "规则名称"),
                TemplateParameter("value", ParamType.BOOLEAN, "true", listOf("true", "false"), "规则值")
            )
        ),

        // 玩家模板
        CommandTemplate(
            id = "gamemode",
            name = "更改游戏模式",
            category = "玩家",
            description = "更改玩家的游戏模式",
            template = "/gamemode <mode> <selector>",
            parameters = listOf(
                TemplateParameter("mode", ParamType.SELECT, "survival",
                    listOf("survival", "creative", "adventure", "spectator"), "游戏模式"),
                TemplateParameter("selector", ParamType.SELECTOR, "@s", emptyList(), "目标玩家")
            )
        ),
        CommandTemplate(
            id = "spawnpoint",
            name = "设置出生点",
            category = "玩家",
            description = "设置玩家的出生点",
            template = "/spawnpoint <selector> <coords>",
            parameters = listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@s", emptyList(), "目标玩家"),
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "出生点位置")
            )
        ),

        // 执行模板
        CommandTemplate(
            id = "execute_at",
            name = "在位置执行",
            category = "执行",
            description = "在指定位置执行命令",
            template = "/execute at <selector> run <command>",
            parameters = listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "执行位置"),
                TemplateParameter("command", ParamType.STRING, "/say Hello", emptyList(), "要执行的命令")
            )
        ),
        CommandTemplate(
            id = "execute_if_block",
            name = "条件执行(方块)",
            category = "执行",
            description = "如果方块存在则执行命令",
            template = "/execute at <selector> if block <coords> <block> run <command>",
            parameters = listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@s", emptyList(), "执行位置"),
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "检测位置"),
                TemplateParameter("block", ParamType.BLOCK_ID, "minecraft:stone", emptyList(), "检测方块"),
                TemplateParameter("command", ParamType.STRING, "/say Block found", emptyList(), "要执行的命令")
            )
        ),

        // 记分板模板
        CommandTemplate(
            id = "scoreboard_create",
            name = "创建记分板",
            category = "记分板",
            description = "创建一个新的记分板",
            template = "/scoreboard objectives add <name> <criteria> <displayName>",
            parameters = listOf(
                TemplateParameter("name", ParamType.STRING, "points", emptyList(), "记分板名称"),
                TemplateParameter("criteria", ParamType.SELECT, "dummy",
                    listOf("dummy", "deathCount", "playerKillCount", "totalKillCount", "health", "xp", "level", "food"), "判断标准"),
                TemplateParameter("displayName", ParamType.STRING, "Points", emptyList(), "显示名称")
            )
        ),
        CommandTemplate(
            id = "scoreboard_set",
            name = "设置分数",
            category = "记分板",
            description = "设置玩家分数",
            template = "/scoreboard players set <selector> <objective> <score>",
            parameters = listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("objective", ParamType.STRING, "points", emptyList(), "记分板名称"),
                TemplateParameter("score", ParamType.NUMBER, "10", emptyList(), "分数值")
            )
        )
    )

    fun generateCommand(template: CommandTemplate, values: Map<String, String>): String {
        var command = template.template

        values.forEach { (key, value) ->
            command = command.replace("<$key>", value)
        }

        return command
    }

    fun getTemplatesByCategory(category: String): List<CommandTemplate> {
        return getTemplates().filter { it.category == category }
    }

    fun getCategories(): List<String> {
        return getTemplates().map { it.category }.distinct()
    }
}