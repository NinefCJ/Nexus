package com.nexuscmd.data

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
    SELECTOR,
    PLAYER_NAME,
    ITEM_ID,
    BLOCK_ID,
    ENTITY_ID,
    EFFECT_ID,
    COORDINATE,
    NUMBER,
    STRING,
    BOOLEAN,
    NBT,
    SELECT
}

class TemplateGenerator {

    fun getTemplates(): List<CommandTemplate> = listOf(
        // 物品
        CommandTemplate("give_basic", "给予物品", "物品",
            "给予玩家指定数量的物品",
            "/give <selector> <item> <amount>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("item", ParamType.ITEM_ID, "minecraft:diamond", emptyList(), "物品ID"),
                TemplateParameter("amount", ParamType.NUMBER, "1", emptyList(), "数量")
            )
        ),
        CommandTemplate("give_enchanted", "给予附魔物品", "物品",
            "给予玩家带附魔的物品",
            "/give <selector> <item>{Enchantments:[{id:\"<enchant>\",lvl:<level>}]}",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("item", ParamType.ITEM_ID, "minecraft:diamond_sword", emptyList(), "物品ID"),
                TemplateParameter("enchant", ParamType.SELECT, "minecraft:sharpness",
                    listOf("minecraft:sharpness", "minecraft:protection", "minecraft:efficiency", "minecraft:fortune", "minecraft:power", "minecraft:unbreaking", "minecraft:mending"), "附魔类型"),
                TemplateParameter("level", ParamType.NUMBER, "5", emptyList(), "附魔等级")
            )
        ),
        CommandTemplate("enchant_item", "附魔手持物品", "物品",
            "给玩家手持物品附魔",
            "/enchant <selector> <enchant> <level>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("enchant", ParamType.SELECT, "minecraft:sharpness",
                    listOf("minecraft:sharpness", "minecraft:protection", "minecraft:efficiency", "minecraft:fortune", "minecraft:power", "minecraft:unbreaking"), "附魔类型"),
                TemplateParameter("level", ParamType.NUMBER, "5", emptyList(), "附魔等级")
            )
        ),
        CommandTemplate("clear_items", "清除物品", "物品",
            "清除玩家指定物品",
            "/clear <selector> <item> <amount>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("item", ParamType.ITEM_ID, "minecraft:diamond", emptyList(), "物品ID"),
                TemplateParameter("amount", ParamType.NUMBER, "1", emptyList(), "数量(可选)")
            )
        ),

        // 实体
        CommandTemplate("summon_basic", "生成实体", "实体",
            "在指定位置生成实体",
            "/summon <entity> <coords>",
            listOf(
                TemplateParameter("entity", ParamType.ENTITY_ID, "minecraft:zombie", emptyList(), "实体类型"),
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "生成位置")
            )
        ),
        CommandTemplate("kill_entity", "删除实体", "实体",
            "删除指定实体",
            "/kill <selector>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@e", emptyList(), "目标实体")
            )
        ),
        CommandTemplate("damage_entity", "造成伤害", "实体",
            "对实体造成指定伤害",
            "/damage <selector> <amount>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标实体"),
                TemplateParameter("amount", ParamType.NUMBER, "10", emptyList(), "伤害值")
            )
        ),

        // 效果
        CommandTemplate("effect_give", "给予效果", "效果",
            "给予玩家药水效果",
            "/effect give <selector> <effect> <duration> <amplifier>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("effect", ParamType.SELECT, "minecraft:speed",
                    listOf("minecraft:speed", "minecraft:strength", "minecraft:regeneration", "minecraft:invisibility", "minecraft:night_vision", "minecraft:jump_boost", "minecraft:fire_resistance", "minecraft:haste"), "效果类型"),
                TemplateParameter("duration", ParamType.NUMBER, "30", emptyList(), "持续时间(秒)"),
                TemplateParameter("amplifier", ParamType.NUMBER, "1", emptyList(), "效果等级")
            )
        ),
        CommandTemplate("effect_clear", "清除效果", "效果",
            "清除玩家的药水效果",
            "/effect clear <selector>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家")
            )
        ),

        // 传送
        CommandTemplate("tp_to_player", "传送到玩家", "传送",
            "传送玩家到另一个玩家位置",
            "/tp <selector> <target>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@s", emptyList(), "要传送的玩家"),
                TemplateParameter("target", ParamType.SELECTOR, "@p", emptyList(), "目标玩家")
            )
        ),
        CommandTemplate("tp_to_coords", "传送到坐标", "传送",
            "传送玩家到指定坐标",
            "/tp <selector> <x> <y> <z>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@s", emptyList(), "要传送的玩家"),
                TemplateParameter("x", ParamType.NUMBER, "0", emptyList(), "X坐标"),
                TemplateParameter("y", ParamType.NUMBER, "64", emptyList(), "Y坐标"),
                TemplateParameter("z", ParamType.NUMBER, "0", emptyList(), "Z坐标")
            )
        ),
        CommandTemplate("spawnpoint_set", "设置出生点", "传送",
            "设置玩家的出生点",
            "/spawnpoint <selector> <coords>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@s", emptyList(), "目标玩家"),
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "出生点位置")
            )
        ),

        // 方块
        CommandTemplate("setblock", "放置方块", "方块",
            "在指定位置放置方块",
            "/setblock <coords> <block> replace",
            listOf(
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "放置位置"),
                TemplateParameter("block", ParamType.BLOCK_ID, "minecraft:stone", emptyList(), "方块类型")
            )
        ),
        CommandTemplate("fill_basic", "填充区域", "方块",
            "填充一个区域",
            "/fill <from> <to> <block>",
            listOf(
                TemplateParameter("from", ParamType.COORDINATE, "~ ~ ~", emptyList(), "起点"),
                TemplateParameter("to", ParamType.COORDINATE, "~5 ~5 ~5", emptyList(), "终点"),
                TemplateParameter("block", ParamType.BLOCK_ID, "minecraft:stone", emptyList(), "方块类型")
            )
        ),
        CommandTemplate("fill_replace", "替换方块", "方块",
            "替换区域内的特定方块",
            "/fill <from> <to> <block> replace <target>",
            listOf(
                TemplateParameter("from", ParamType.COORDINATE, "~ ~ ~", emptyList(), "起点"),
                TemplateParameter("to", ParamType.COORDINATE, "~5 ~5 ~5", emptyList(), "终点"),
                TemplateParameter("block", ParamType.BLOCK_ID, "minecraft:stone", emptyList(), "新方块"),
                TemplateParameter("target", ParamType.BLOCK_ID, "minecraft:dirt", emptyList(), "要替换的方块")
            )
        ),
        CommandTemplate("clone", "复制区域", "方块",
            "复制一个区域到另一个位置",
            "/clone <from1> <from2> <to>",
            listOf(
                TemplateParameter("from1", ParamType.COORDINATE, "~ ~ ~", emptyList(), "起点"),
                TemplateParameter("from2", ParamType.COORDINATE, "~5 ~5 ~5", emptyList(), "终点"),
                TemplateParameter("to", ParamType.COORDINATE, "~10 ~ ~", emptyList(), "目标位置")
            )
        ),

        // 世界
        CommandTemplate("time_set", "设置时间", "世界",
            "设置游戏时间",
            "/time set <time>",
            listOf(
                TemplateParameter("time", ParamType.SELECT, "day",
                    listOf("day", "noon", "night", "midnight", "sunrise", "sunset", "0", "1000", "6000", "12000", "13000", "18000"), "时间")
            )
        ),
        CommandTemplate("weather", "设置天气", "世界",
            "设置游戏天气",
            "/weather <weather> <duration>",
            listOf(
                TemplateParameter("weather", ParamType.SELECT, "clear",
                    listOf("clear", "rain", "thunder"), "天气类型"),
                TemplateParameter("duration", ParamType.NUMBER, "6000", emptyList(), "持续时间")
            )
        ),
        CommandTemplate("gamerule_set", "设置游戏规则", "世界",
            "修改游戏规则",
            "/gamerule <rule> <value>",
            listOf(
                TemplateParameter("rule", ParamType.SELECT, "keepInventory",
                    listOf("keepInventory", "mobGriefing", "doDaylightCycle", "doWeatherCycle", "doMobSpawning", "doTileDrops", "commandBlockOutput", "sendCommandFeedback", "doFireTick", "drowningDamage", "fallDamage", "fireDamage"), "规则名称"),
                TemplateParameter("value", ParamType.BOOLEAN, "true", listOf("true", "false"), "规则值")
            )
        ),
        CommandTemplate("difficulty", "设置难度", "世界",
            "设置游戏难度",
            "/difficulty <diff>",
            listOf(
                TemplateParameter("diff", ParamType.SELECT, "normal",
                    listOf("peaceful", "easy", "normal", "hard"), "难度等级")
            )
        ),
        CommandTemplate("tick_rate", "设置游戏速度", "世界",
            "修改tick速率(游戏速度)",
            "/tick rate <rate>",
            listOf(
                TemplateParameter("rate", ParamType.SELECT, "20",
                    listOf("1", "5", "10", "20", "40", "80", "100"), "tick/秒(默认20)")
            )
        ),
        CommandTemplate("locate_structure", "查找结构", "世界",
            "查找最近的结构",
            "/locate structure <structure>",
            listOf(
                TemplateParameter("structure", ParamType.SELECT, "minecraft:village",
                    listOf("minecraft:village", "minecraft:desert_pyramid", "minecraft:jungle_pyramid", "minecraft:stronghold", "minecraft:endcity", "minecraft:fortress", "minecraft:mansion", "minecraft:monument", "minecraft:ancient_city"), "结构类型")
            )
        ),

        // 游戏模式
        CommandTemplate("gamemode_set", "更改游戏模式", "游戏模式",
            "更改玩家的游戏模式",
            "/gamemode <mode> <selector>",
            listOf(
                TemplateParameter("mode", ParamType.SELECT, "creative",
                    listOf("survival", "creative", "adventure", "spectator"), "游戏模式"),
                TemplateParameter("selector", ParamType.SELECTOR, "@s", emptyList(), "目标玩家")
            )
        ),

        // 执行
        CommandTemplate("execute_at", "在位置执行", "执行",
            "在指定位置执行命令",
            "/execute at <selector> run <command>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "执行位置"),
                TemplateParameter("command", ParamType.STRING, "/say Hello", emptyList(), "要执行的命令")
            )
        ),
        CommandTemplate("execute_as", "以身份执行", "执行",
            "以实体身份执行命令",
            "/execute as <selector> run <command>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@a", emptyList(), "执行身份"),
                TemplateParameter("command", ParamType.STRING, "/say Hello", emptyList(), "要执行的命令")
            )
        ),
        CommandTemplate("execute_if_entity", "条件执行(实体)", "执行",
            "如果实体存在则执行",
            "/execute if entity <selector> run <command>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "检测实体"),
                TemplateParameter("command", ParamType.STRING, "/say Player found", emptyList(), "要执行的命令")
            )
        ),

        // 记分板
        CommandTemplate("scoreboard_create", "创建记分项", "记分板",
            "创建一个新的记分板",
            "/scoreboard objectives add <name> <criteria> <displayName>",
            listOf(
                TemplateParameter("name", ParamType.STRING, "points", emptyList(), "记分板名称"),
                TemplateParameter("criteria", ParamType.SELECT, "dummy",
                    listOf("dummy", "deathCount", "playerKillCount", "totalKillCount", "health", "xp", "level", "food", "armor"), "判断标准"),
                TemplateParameter("displayName", ParamType.STRING, "Points", emptyList(), "显示名称")
            )
        ),
        CommandTemplate("scoreboard_set", "设置分数", "记分板",
            "设置玩家分数",
            "/scoreboard players set <selector> <objective> <score>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("objective", ParamType.STRING, "points", emptyList(), "记分板名称"),
                TemplateParameter("score", ParamType.NUMBER, "10", emptyList(), "分数值")
            )
        ),
        CommandTemplate("scoreboard_add", "增加分数", "记分板",
            "给玩家加分",
            "/scoreboard players add <selector> <objective> <score>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("objective", ParamType.STRING, "points", emptyList(), "记分板名称"),
                TemplateParameter("score", ParamType.NUMBER, "1", emptyList(), "增加的分数")
            )
        ),
        CommandTemplate("scoreboard_display", "设置显示位置", "记分板",
            "设置记分板显示位置",
            "/scoreboard objectives setdisplay <slot> <objective>",
            listOf(
                TemplateParameter("slot", ParamType.SELECT, "sidebar",
                    listOf("list", "sidebar", "belowName"), "显示位置"),
                TemplateParameter("objective", ParamType.STRING, "points", emptyList(), "记分板名称")
            )
        ),

        // 玩家互动
        CommandTemplate("say_message", "广播消息", "玩家互动",
            "向所有玩家发送消息",
            "/say <message>",
            listOf(
                TemplateParameter("message", ParamType.STRING, "Hello World!", emptyList(), "消息内容")
            )
        ),
        CommandTemplate("title_show", "显示标题", "玩家互动",
            "向玩家显示标题",
            "/title <selector> title {\"text\":\"<title>\",\"color\":\"<color>\"}",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@a", emptyList(), "目标玩家"),
                TemplateParameter("title", ParamType.STRING, "Hello", emptyList(), "标题文本"),
                TemplateParameter("color", ParamType.SELECT, "white",
                    listOf("white", "red", "green", "blue", "yellow", "gold", "dark_purple", "aqua"), "标题颜色")
            )
        ),
        CommandTemplate("xp_add", "给予经验", "玩家互动",
            "给予玩家经验值/等级",
            "/xp add <selector> <amount> <type>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("amount", ParamType.NUMBER, "100", emptyList(), "数量"),
                TemplateParameter("type", ParamType.SELECT, "points",
                    listOf("points", "levels"), "类型")
            )
        ),

        // 声音粒子
        CommandTemplate("playsound_basic", "播放声音", "声音粒子",
            "向玩家播放声音",
            "/playsound <sound> <source> <selector>",
            listOf(
                TemplateParameter("sound", ParamType.SELECT, "minecraft:entity.experience_orb.pickup",
                    listOf("minecraft:entity.experience_orb.pickup", "minecraft:entity.player.levelup", "minecraft:block.note_block.chime", "minecraft:entity.elder_guardian.curse", "minecraft:block.anvil.use", "minecraft:entity.ender_dragon.growl"), "声音ID"),
                TemplateParameter("source", ParamType.SELECT, "master",
                    listOf("master", "music", "record", "weather", "block", "hostile", "neutral", "player", "ambient", "voice"), "声音来源"),
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家")
            )
        ),
        CommandTemplate("particle_basic", "生成粒子", "声音粒子",
            "在位置生成粒子效果",
            "/particle <particle> <coords> <delta> <speed> <count>",
            listOf(
                TemplateParameter("particle", ParamType.SELECT, "minecraft:heart",
                    listOf("minecraft:heart", "minecraft:flame", "minecraft:smoke", "minecraft:explosion", "minecraft:spark", "minecraft:end_rod", "minecraft:note", "minecraft:critical_hit"), "粒子类型"),
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "生成位置"),
                TemplateParameter("delta", ParamType.COORDINATE, "1 1 1", emptyList(), "扩散范围"),
                TemplateParameter("speed", ParamType.NUMBER, "1", emptyList(), "速度"),
                TemplateParameter("count", ParamType.NUMBER, "10", emptyList(), "数量")
            )
        ),

        // 管理员
        CommandTemplate("kick_player", "踢出玩家", "管理员",
            "将玩家踢出服务器",
            "/kick <selector> <reason>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("reason", ParamType.STRING, "Kicked by admin", emptyList(), "踢出原因")
            )
        ),

        // 速查
        CommandTemplate("quick_keepinv", "死亡不掉落", "速查",
            "开启死亡保留物品",
            "/gamerule keepInventory true",
            emptyList()
        ),
        CommandTemplate("quick_nogrief", "禁用生物破坏", "速查",
            "禁用爬行者等破坏方块",
            "/gamerule mobGriefing false",
            emptyList()
        ),
        CommandTemplate("quick_day", "设置白天", "速查",
            "设置时间为白天",
            "/time set day",
            emptyList()
        ),
        CommandTemplate("quick_clear", "设置晴天", "速查",
            "设置天气为晴天",
            "/weather clear",
            emptyList()
        ),
        CommandTemplate("quick_creative", "创造模式", "速查",
            "切换到创造模式",
            "/gamemode creative",
            emptyList()
        ),
        CommandTemplate("quick_survival", "生存模式", "速查",
            "切换到生存模式",
            "/gamemode survival",
            emptyList()
        ),
        CommandTemplate("quick_diamond", "给予钻石", "速查",
            "给自己64个钻石",
            "/give @s diamond 64",
            emptyList()
        ),
        CommandTemplate("quick_invisible", "隐身效果", "速查",
            "获得10分钟隐身效果",
            "/effect give @s minecraft:invisibility 600 1 true",
            emptyList()
        ),
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
