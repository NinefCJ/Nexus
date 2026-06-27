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
        // ============ 物品模板 (基岩版) ============
        CommandTemplate("give_basic", "给予物品", "物品",
            "给予玩家指定数量的物品",
            "/give <selector> <item> <amount>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("item", ParamType.ITEM_ID, "diamond", emptyList(), "物品ID"),
                TemplateParameter("amount", ParamType.NUMBER, "1", emptyList(), "数量")
            )
        ),
        CommandTemplate("give_data", "给予物品(带数据值)", "物品",
            "给予带数据值的物品(基岩版)",
            "/give <selector> <item> <amount> <data>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("item", ParamType.ITEM_ID, "wool", emptyList(), "物品ID"),
                TemplateParameter("amount", ParamType.NUMBER, "1", emptyList(), "数量"),
                TemplateParameter("data", ParamType.NUMBER, "14", emptyList(), "数据值(如羊毛颜色)")
            )
        ),
        CommandTemplate("give_components", "给予物品(带组件)", "物品",
            "给予带特殊组件的物品",
            "/give <selector> <item> <amount> 0 <components>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("item", ParamType.ITEM_ID, "diamond_sword", emptyList(), "物品ID"),
                TemplateParameter("amount", ParamType.NUMBER, "1", emptyList(), "数量"),
                TemplateParameter("components", ParamType.SELECT, "{\"minecraft:can_destroy\":{\"blocks\":[\"stone\"]}}",
                    listOf("{\"minecraft:can_destroy\":{\"blocks\":[\"stone\"]}}", "{\"minecraft:keep_on_death\":{}}", "{\"minecraft:item_lock\":{\"mode\":\"lock_in_slot\"}}"), "组件JSON")
            )
        ),
        CommandTemplate("enchant_item", "附魔物品", "物品",
            "给玩家手持物品附魔",
            "/enchant <selector> <enchant> <level>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("enchant", ParamType.SELECT, "sharpness",
                    listOf("sharpness", "protection", "efficiency", "fortune", "power", "unbreaking", "fire_aspect", "knockback"), "附魔ID"),
                TemplateParameter("level", ParamType.NUMBER, "5", emptyList(), "附魔等级")
            )
        ),
        CommandTemplate("clear_items", "清除物品", "物品",
            "清除玩家指定物品",
            "/clear <selector> <item> <amount>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("item", ParamType.ITEM_ID, "diamond", emptyList(), "物品ID"),
                TemplateParameter("amount", ParamType.NUMBER, "1", emptyList(), "数量(可选)")
            )
        ),
        CommandTemplate("replaceitem", "替换物品栏物品", "物品",
            "替换指定物品栏位置物品",
            "/replaceitem entity <selector> slot.inventory <slot> <item>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("slot", ParamType.NUMBER, "0", emptyList(), "物品栏位置(0-35)"),
                TemplateParameter("item", ParamType.ITEM_ID, "diamond", emptyList(), "物品ID")
            )
        ),

        // ============ 实体模板 (基岩版) ============
        CommandTemplate("summon_basic", "生成实体", "实体",
            "在指定位置生成实体",
            "/summon <entity> <coords>",
            listOf(
                TemplateParameter("entity", ParamType.ENTITY_ID, "zombie", emptyList(), "实体类型"),
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "生成位置")
            )
        ),
        CommandTemplate("summon_event", "生成实体(带事件)", "实体",
            "生成带生成事件的实体",
            "/summon <entity> <coords> <event>",
            listOf(
                TemplateParameter("entity", ParamType.ENTITY_ID, "zombie", emptyList(), "实体类型"),
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "生成位置"),
                TemplateParameter("event", ParamType.SELECT, "minecraft:entity_spawned",
                    listOf("minecraft:entity_spawned", "minecraft:on_spawn", "minecraft:on_prime"), "生成事件")
            )
        ),
        CommandTemplate("summon_named", "生成命名实体", "实体",
            "生成带自定义名称的实体(基岩版)",
            "/summon <entity> <coords> <event> <name>",
            listOf(
                TemplateParameter("entity", ParamType.ENTITY_ID, "zombie", emptyList(), "实体类型"),
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "生成位置"),
                TemplateParameter("event", ParamType.STRING, "", emptyList(), "生成事件(可选)"),
                TemplateParameter("name", ParamType.STRING, "Boss", emptyList(), "实体名称")
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
        CommandTemplate("ride", "骑乘实体", "实体",
            "让实体骑乘另一个实体",
            "/ride <riders> ride <vehicle>",
            listOf(
                TemplateParameter("riders", ParamType.SELECTOR, "@s", emptyList(), "骑乘者"),
                TemplateParameter("vehicle", ParamType.SELECTOR, "@e[type=horse,r=5]", emptyList(), "被骑乘实体")
            )
        ),

        // ============ 效果模板 (基岩版) ============
        CommandTemplate("effect_give", "给予效果", "效果",
            "给予玩家药水效果(基岩版语法)",
            "/effect <selector> <effect> <duration> <amplifier> <particles>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("effect", ParamType.SELECT, "speed",
                    listOf("speed", "strength", "regeneration", "invisibility", "night_vision", "jump_boost", "fire_resistance", "haste", "slow_falling", "water_breathing", "health_boost"), "效果类型"),
                TemplateParameter("duration", ParamType.NUMBER, "30", emptyList(), "持续时间(秒)"),
                TemplateParameter("amplifier", ParamType.NUMBER, "1", emptyList(), "效果等级"),
                TemplateParameter("particles", ParamType.BOOLEAN, "true", listOf("true", "false"), "显示粒子")
            )
        ),
        CommandTemplate("effect_clear", "清除效果", "效果",
            "清除玩家的药水效果(基岩版)",
            "/effect <selector> clear",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家")
            )
        ),

        // ============ 传送模板 (基岩版) ============
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
        CommandTemplate("tp_relative", "相对传送", "传送",
            "相对传送玩家",
            "/tp <selector> ~<dx> ~<dy> ~<dz>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@s", emptyList(), "要传送的玩家"),
                TemplateParameter("dx", ParamType.NUMBER, "10", emptyList(), "X偏移"),
                TemplateParameter("dy", ParamType.NUMBER, "0", emptyList(), "Y偏移"),
                TemplateParameter("dz", ParamType.NUMBER, "0", emptyList(), "Z偏移")
            )
        ),
        CommandTemplate("tp_facing", "传送朝向", "传送",
            "传送并设置朝向",
            "/tp <selector> <x> <y> <z> facing <lookAt>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@s", emptyList(), "要传送的玩家"),
                TemplateParameter("x", ParamType.NUMBER, "0", emptyList(), "X坐标"),
                TemplateParameter("y", ParamType.NUMBER, "64", emptyList(), "Y坐标"),
                TemplateParameter("z", ParamType.NUMBER, "0", emptyList(), "Z坐标"),
                TemplateParameter("lookAt", ParamType.SELECTOR, "@p", emptyList(), "朝向目标")
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

        // ============ 方块模板 (基岩版) ============
        CommandTemplate("setblock", "放置方块", "方块",
            "在指定位置放置方块(基岩版)",
            "/setblock <coords> <block> <data> replace",
            listOf(
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "放置位置"),
                TemplateParameter("block", ParamType.BLOCK_ID, "stone", emptyList(), "方块类型"),
                TemplateParameter("data", ParamType.NUMBER, "0", emptyList(), "方块数据值")
            )
        ),
        CommandTemplate("fill_basic", "填充区域", "方块",
            "填充一个区域(基岩版)",
            "/fill <from> <to> <block>",
            listOf(
                TemplateParameter("from", ParamType.COORDINATE, "~ ~ ~", emptyList(), "起点"),
                TemplateParameter("to", ParamType.COORDINATE, "~5 ~5 ~5", emptyList(), "终点"),
                TemplateParameter("block", ParamType.BLOCK_ID, "stone", emptyList(), "方块类型")
            )
        ),
        CommandTemplate("fill_replace", "替换方块", "方块",
            "替换区域内的特定方块(基岩版)",
            "/fill <from> <to> <block> <data> replace <target> <targetData>",
            listOf(
                TemplateParameter("from", ParamType.COORDINATE, "~ ~ ~", emptyList(), "起点"),
                TemplateParameter("to", ParamType.COORDINATE, "~5 ~5 ~5", emptyList(), "终点"),
                TemplateParameter("block", ParamType.BLOCK_ID, "stone", emptyList(), "新方块"),
                TemplateParameter("data", ParamType.NUMBER, "0", emptyList(), "新方块数据"),
                TemplateParameter("target", ParamType.BLOCK_ID, "dirt", emptyList(), "要替换的方块"),
                TemplateParameter("targetData", ParamType.NUMBER, "0", emptyList(), "目标方块数据")
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

        // ============ 世界模板 (基岩版) ============
        CommandTemplate("time_set", "设置时间", "世界",
            "设置游戏时间",
            "/time set <time>",
            listOf(
                TemplateParameter("time", ParamType.SELECT, "day",
                    listOf("day", "night", "noon", "midnight", "sunrise", "sunset", "0", "1000", "6000", "12000", "13000", "18000"), "时间")
            )
        ),
        CommandTemplate("time_add", "增加时间", "世界",
            "增加游戏时间",
            "/time add <amount>",
            listOf(
                TemplateParameter("amount", ParamType.NUMBER, "1000", emptyList(), "增加的tick数")
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
                    listOf("keepInventory", "mobGriefing", "doDaylightCycle", "doWeatherCycle", "doMobSpawning", "doTileDrops", "commandBlockOutput", "sendCommandFeedback", "doFireTick", "drowningDamage", "fallDamage", "fireDamage", "showCoordinates", "showDaysPassed", "naturalRegeneration"), "规则名称"),
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
        CommandTemplate("tickingarea_add", "添加常加载区域", "世界",
            "添加一个常加载区域(基岩版独有)",
            "/tickingarea add <from> <to> <name>",
            listOf(
                TemplateParameter("from", ParamType.COORDINATE, "~ ~ ~", emptyList(), "起点"),
                TemplateParameter("to", ParamType.COORDINATE, "~10 ~10 ~10", emptyList(), "终点"),
                TemplateParameter("name", ParamType.STRING, "myArea", emptyList(), "区域名称")
            )
        ),
        CommandTemplate("tickingarea_circle", "添加圆形常加载", "世界",
            "添加圆形常加载区域",
            "/tickingarea add circle <center> <radius> <name>",
            listOf(
                TemplateParameter("center", ParamType.COORDINATE, "~ ~ ~", emptyList(), "中心点"),
                TemplateParameter("radius", ParamType.NUMBER, "4", emptyList(), "半径(区块数)"),
                TemplateParameter("name", ParamType.STRING, "myCircle", emptyList(), "区域名称")
            )
        ),
        CommandTemplate("locate", "查找结构", "世界",
            "查找最近的结构(基岩版)",
            "/locate <structure>",
            listOf(
                TemplateParameter("structure", ParamType.SELECT, "village",
                    listOf("village", "stronghold", "monument", "mansion", "fortress", "endcity", "ruins", "shipwreck", "buried_treasure", "pillageroutpost", "ancient_city"), "结构类型")
            )
        ),

        // ============ 游戏模式 (基岩版) ============
        CommandTemplate("gamemode_set", "更改游戏模式", "游戏模式",
            "更改玩家的游戏模式",
            "/gamemode <mode> <selector>",
            listOf(
                TemplateParameter("mode", ParamType.SELECT, "creative",
                    listOf("survival", "creative", "adventure", "spectator"), "游戏模式"),
                TemplateParameter("selector", ParamType.SELECTOR, "@s", emptyList(), "目标玩家")
            )
        ),

        // ============ 执行模板 (基岩版) ============
        CommandTemplate("execute_run", "执行命令", "执行",
            "在指定位置执行命令(基岩版语法)",
            "/execute <selector> <coords> run <command>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "执行者"),
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "执行位置"),
                TemplateParameter("command", ParamType.STRING, "/say Hello", emptyList(), "要执行的命令")
            )
        ),
        CommandTemplate("execute_detect", "检测执行", "执行",
            "检测方块后执行命令(基岩版语法)",
            "/execute <selector> <coords> detect <detectPos> <block> <data> <command>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@s", emptyList(), "执行者"),
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "执行位置"),
                TemplateParameter("detectPos", ParamType.COORDINATE, "~ ~-1 ~", emptyList(), "检测位置"),
                TemplateParameter("block", ParamType.BLOCK_ID, "grass", emptyList(), "检测方块"),
                TemplateParameter("data", ParamType.NUMBER, "0", emptyList(), "方块数据"),
                TemplateParameter("command", ParamType.STRING, "/say Standing on grass", emptyList(), "要执行的命令")
            )
        ),
        CommandTemplate("function", "执行函数", "执行",
            "执行函数文件",
            "/function <name>",
            listOf(
                TemplateParameter("name", ParamType.STRING, "myfunction", emptyList(), "函数名称")
            )
        ),

        // ============ 记分板模板 (基岩版) ============
        CommandTemplate("scoreboard_create", "创建记分项", "记分板",
            "创建一个新的记分板",
            "/scoreboard objectives add <name> <criteria> <displayName>",
            listOf(
                TemplateParameter("name", ParamType.STRING, "points", emptyList(), "记分板名称"),
                TemplateParameter("criteria", ParamType.SELECT, "dummy",
                    listOf("dummy", "deathCount", "playerKillCount", "totalKillCount"), "判断标准"),
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
                    listOf("list", "sidebar", "belowname"), "显示位置"),
                TemplateParameter("objective", ParamType.STRING, "points", emptyList(), "记分板名称")
            )
        ),
        CommandTemplate("tag_add", "添加标签", "记分板",
            "给实体添加标签",
            "/tag <selector> add <name>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@e", emptyList(), "目标实体"),
                TemplateParameter("name", ParamType.STRING, "myTag", emptyList(), "标签名称")
            )
        ),

        // ============ 玩家互动 (基岩版) ============
        CommandTemplate("say_message", "广播消息", "玩家互动",
            "向所有玩家发送消息",
            "/say <message>",
            listOf(
                TemplateParameter("message", ParamType.STRING, "Hello World!", emptyList(), "消息内容")
            )
        ),
        CommandTemplate("tellraw", "发送JSON消息", "玩家互动",
            "发送原始JSON消息(基岩版)",
            "/tellraw <selector> <rawtext>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@a", emptyList(), "目标玩家"),
                TemplateParameter("rawtext", ParamType.STRING, "{\"rawtext\":[{\"text\":\"Hello\"}]}", emptyList(), "JSON消息")
            )
        ),
        CommandTemplate("titleraw", "显示标题", "玩家互动",
            "向玩家显示标题(基岩版)",
            "/titleraw <selector> title <rawtext>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@a", emptyList(), "目标玩家"),
                TemplateParameter("rawtext", ParamType.STRING, "{\"rawtext\":[{\"text\":\"Welcome!\"}]}", emptyList(), "标题JSON")
            )
        ),
        CommandTemplate("xp_add", "给予经验", "玩家互动",
            "给予玩家经验值",
            "/xp <amount> <selector>",
            listOf(
                TemplateParameter("amount", ParamType.NUMBER, "100", emptyList(), "数量"),
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家")
            )
        ),
        CommandTemplate("xp_levels", "给予经验等级", "玩家互动",
            "给予玩家经验等级",
            "/xp <amount>L <selector>",
            listOf(
                TemplateParameter("amount", ParamType.NUMBER, "10", emptyList(), "等级数"),
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家")
            )
        ),

        // ============ 声音粒子 (基岩版) ============
        CommandTemplate("playsound_basic", "播放声音", "声音粒子",
            "向玩家播放声音",
            "/playsound <sound> <source> <selector>",
            listOf(
                TemplateParameter("sound", ParamType.SELECT, "random.orb",
                    listOf("random.orb", "random.levelup", "block.note.chime", "ambient.weather.thunder", "block.anvil.use", "mob.enderdragon.growl"), "声音ID"),
                TemplateParameter("source", ParamType.SELECT, "master",
                    listOf("master", "music", "record", "weather", "block", "hostile", "neutral", "player", "ambient", "voice"), "声音来源"),
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家")
            )
        ),
        CommandTemplate("particle_basic", "生成粒子", "声音粒子",
            "在位置生成粒子效果(基岩版)",
            "/particle <effect> <coords>",
            listOf(
                TemplateParameter("effect", ParamType.SELECT, "minecraft:heart_particle",
                    listOf("minecraft:heart_particle", "minecraft:basic_flame_particle", "minecraft:smoke_particle", "minecraft:explosion_particle", "minecraft:spark_particle"), "粒子类型"),
                TemplateParameter("coords", ParamType.COORDINATE, "~ ~ ~", emptyList(), "生成位置")
            )
        ),

        // ============ 基岩版独有命令 ============
        CommandTemplate("camerashake_add", "添加摄像机震动", "基岩版独有",
            "给玩家添加摄像机震动效果",
            "/camerashake <selector> add <intensity> <duration> <type>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@a", emptyList(), "目标玩家"),
                TemplateParameter("intensity", ParamType.NUMBER, "0.5", emptyList(), "震动强度"),
                TemplateParameter("duration", ParamType.NUMBER, "10", emptyList(), "持续时间(秒)"),
                TemplateParameter("type", ParamType.SELECT, "positional",
                    listOf("positional", "rotational"), "震动类型")
            )
        ),
        CommandTemplate("structure_save", "保存结构", "基岩版独有",
            "保存区域为结构文件",
            "/structure save <name> <from> <to> <includeEntities>",
            listOf(
                TemplateParameter("name", ParamType.STRING, "myStructure", emptyList(), "结构名称"),
                TemplateParameter("from", ParamType.COORDINATE, "~ ~ ~", emptyList(), "起点"),
                TemplateParameter("to", ParamType.COORDINATE, "~5 ~5 ~5", emptyList(), "终点"),
                TemplateParameter("includeEntities", ParamType.BOOLEAN, "true", listOf("true", "false"), "包含实体")
            )
        ),
        CommandTemplate("structure_load", "加载结构", "基岩版独有",
            "加载结构到指定位置",
            "/structure load <name> <to> <rotation> <mirror>",
            listOf(
                TemplateParameter("name", ParamType.STRING, "myStructure", emptyList(), "结构名称"),
                TemplateParameter("to", ParamType.COORDINATE, "~ ~ ~", emptyList(), "目标位置"),
                TemplateParameter("rotation", ParamType.SELECT, "0_degrees",
                    listOf("0_degrees", "90_degrees", "180_degrees", "270_degrees"), "旋转角度"),
                TemplateParameter("mirror", ParamType.SELECT, "none",
                    listOf("none", "x", "z", "xz"), "镜像模式")
            )
        ),
        CommandTemplate("playanimation", "播放动画", "基岩版独有",
            "让实体播放动画",
            "/playanimation <selector> <animation> <state>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@e[type=zombie,r=10]", emptyList(), "目标实体"),
                TemplateParameter("animation", ParamType.STRING, "animation.zombie.attack", emptyList(), "动画名称"),
                TemplateParameter("state", ParamType.SELECT, "playing",
                    listOf("playing", "stopped", "paused"), "动画状态")
            )
        ),
        CommandTemplate("scriptevent", "触发脚本事件", "基岩版独有",
            "向脚本系统发送事件",
            "/scriptevent <id> <message>",
            listOf(
                TemplateParameter("id", ParamType.STRING, "my:event", emptyList(), "事件ID"),
                TemplateParameter("message", ParamType.STRING, "Hello", emptyList(), "消息内容")
            )
        ),

        // ============ 管理员 (基岩版) ============
        CommandTemplate("kick_player", "踢出玩家", "管理员",
            "将玩家踢出服务器",
            "/kick <selector> <reason>",
            listOf(
                TemplateParameter("selector", ParamType.SELECTOR, "@p", emptyList(), "目标玩家"),
                TemplateParameter("reason", ParamType.STRING, "Kicked by admin", emptyList(), "踢出原因")
            )
        ),
        CommandTemplate("op_player", "给予管理权限", "管理员",
            "给予玩家管理权限",
            "/op <player>",
            listOf(
                TemplateParameter("player", ParamType.STRING, "PlayerName", emptyList(), "玩家名称")
            )
        ),
        CommandTemplate("deop_player", "移除管理权限", "管理员",
            "移除玩家管理权限",
            "/deop <player>",
            listOf(
                TemplateParameter("player", ParamType.STRING, "PlayerName", emptyList(), "玩家名称")
            )
        ),
        CommandTemplate("allowlist_add", "添加白名单", "管理员",
            "添加玩家到白名单(基岩版)",
            "/allowlist add <player>",
            listOf(
                TemplateParameter("player", ParamType.STRING, "PlayerName", emptyList(), "玩家名称")
            )
        ),

        // ============ 速查模板 (基岩版) ============
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
        CommandTemplate("quick_clear_weather", "设置晴天", "速查",
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
            "获得10分钟隐身效果(基岩版)",
            "/effect @s invisibility 600 1 true",
            emptyList()
        ),
        CommandTemplate("quick_tickingarea", "常加载区域", "速查",
            "添加常加载区域",
            "/tickingarea add ~ ~ ~ ~10 ~10 ~10",
            emptyList()
        ),
        CommandTemplate("quick_showcoords", "显示坐标", "速查",
            "显示玩家坐标",
            "/gamerule showCoordinates true",
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