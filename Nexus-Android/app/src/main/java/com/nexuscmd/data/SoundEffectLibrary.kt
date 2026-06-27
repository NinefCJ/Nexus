package com.nexuscmd.data

data class SoundEffect(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val volume: String = "1",
    val pitch: String = "1"
)

object SoundEffectLibrary {
    val categories = listOf("全部", "方块", "实体", "玩家", "环境", "音乐", "UI", "天气", "红石", "其他")

    val effects = listOf(
        SoundEffect("random.levelup", "升级", "玩家", "玩家升级提示音"),
        SoundEffect("random.orb", "经验球", "玩家", "拾取经验球"),
        SoundEffect("random.pop", "拾取物品", "玩家", "拾取物品的短促音"),
        SoundEffect("random.eat", "进食", "玩家", "玩家吃食物"),
        SoundEffect("random.drink", "饮用", "玩家", "玩家喝药水或饮品"),
        SoundEffect("random.bow", "弓发射", "玩家", "弓箭射出"),
        SoundEffect("random.bowhit", "弓命中", "玩家", "箭矢命中目标"),
        SoundEffect("random.break", "工具损坏", "玩家", "装备或工具损坏"),
        SoundEffect("random.anvil_land", "铁砧落地", "方块", "铁砧坠落并落地"),
        SoundEffect("random.anvil_use", "铁砧使用", "方块", "使用铁砧"),
        SoundEffect("random.chestopen", "箱子打开", "方块", "打开箱子"),
        SoundEffect("random.chestclosed", "箱子关闭", "方块", "关闭箱子"),
        SoundEffect("random.door_open", "门打开", "方块", "打开木门"),
        SoundEffect("random.door_close", "门关闭", "方块", "关闭木门"),
        SoundEffect("dig.stone", "挖掘石头", "方块", "破坏石质方块"),
        SoundEffect("dig.grass", "挖掘草方块", "方块", "破坏草、泥土类方块"),
        SoundEffect("dig.wood", "挖掘木头", "方块", "破坏木质方块"),
        SoundEffect("dig.sand", "挖掘沙子", "方块", "破坏沙子类方块"),
        SoundEffect("dig.gravel", "挖掘沙砾", "方块", "破坏沙砾类方块"),
        SoundEffect("dig.glass", "玻璃破碎", "方块", "玻璃破碎"),
        SoundEffect("block.note.bass", "音符盒低音", "红石", "音符盒低音"),
        SoundEffect("block.note.pling", "音符盒叮声", "红石", "音符盒高亮音"),
        SoundEffect("block.note.chime", "音符盒风铃", "红石", "音符盒风铃音色"),
        SoundEffect("block.note.harp", "音符盒竖琴", "红石", "音符盒竖琴音色"),
        SoundEffect("block.note.snare", "音符盒小鼓", "红石", "音符盒小鼓音色"),
        SoundEffect("block.note.basedrum", "音符盒大鼓", "红石", "音符盒大鼓音色"),
        SoundEffect("mob.zombie.say", "僵尸叫声", "实体", "僵尸空闲叫声"),
        SoundEffect("mob.zombie.hurt", "僵尸受伤", "实体", "僵尸受伤"),
        SoundEffect("mob.zombie.death", "僵尸死亡", "实体", "僵尸死亡"),
        SoundEffect("mob.skeleton.say", "骷髅叫声", "实体", "骷髅空闲音"),
        SoundEffect("mob.skeleton.hurt", "骷髅受伤", "实体", "骷髅受伤"),
        SoundEffect("mob.creeper.say", "苦力怕", "实体", "苦力怕引爆前嘶声"),
        SoundEffect("mob.endermen.idle", "末影人空闲", "实体", "末影人空闲音"),
        SoundEffect("mob.endermen.portal", "末影人传送", "实体", "末影人传送音"),
        SoundEffect("mob.enderdragon.growl", "末影龙咆哮", "实体", "末影龙咆哮"),
        SoundEffect("mob.wither.spawn", "凋灵生成", "实体", "凋灵生成音效"),
        SoundEffect("mob.wither.death", "凋灵死亡", "实体", "凋灵死亡音效"),
        SoundEffect("mob.villager.yes", "村民同意", "实体", "村民肯定音"),
        SoundEffect("mob.villager.no", "村民拒绝", "实体", "村民否定音"),
        SoundEffect("mob.cat.meow", "猫叫", "实体", "猫喵叫"),
        SoundEffect("mob.wolf.bark", "狼叫", "实体", "狼吠叫"),
        SoundEffect("ambient.cave", "洞穴环境", "环境", "洞穴氛围音"),
        SoundEffect("ambient.weather.thunder", "雷声", "天气", "打雷音效", volume = "2"),
        SoundEffect("ambient.weather.rain", "雨声", "天气", "下雨环境音"),
        SoundEffect("fire.fire", "火焰燃烧", "环境", "火焰持续燃烧"),
        SoundEffect("fire.ignite", "点燃", "环境", "打火石点燃"),
        SoundEffect("liquid.water", "水流", "环境", "水流声音"),
        SoundEffect("liquid.lava", "岩浆", "环境", "岩浆流动"),
        SoundEffect("portal.portal", "传送门", "环境", "下界传送门环境音"),
        SoundEffect("portal.travel", "传送门穿越", "环境", "穿越传送门"),
        SoundEffect("random.explode", "爆炸", "环境", "爆炸音效", volume = "2"),
        SoundEffect("random.fizz", "熄灭", "环境", "火焰或液体熄灭"),
        SoundEffect("music.game", "游戏音乐", "音乐", "普通游戏背景音乐"),
        SoundEffect("music.menu", "菜单音乐", "音乐", "主菜单背景音乐"),
        SoundEffect("music.nether", "下界音乐", "音乐", "下界背景音乐"),
        SoundEffect("music.end", "末地音乐", "音乐", "末地背景音乐"),
        SoundEffect("music.game.creative", "创造音乐", "音乐", "创造模式背景音乐"),
        SoundEffect("note.bass", "旧版低音", "红石", "兼容旧版音符盒低音"),
        SoundEffect("note.pling", "旧版叮声", "红石", "兼容旧版音符盒叮声"),
        SoundEffect("ui.button.click", "按钮点击", "UI", "界面按钮点击"),
        SoundEffect("random.click", "点击", "UI", "普通点击音"),
        SoundEffect("random.toast", "提示弹出", "UI", "提示或通知音")
    )

    fun buildPlaySoundCommand(
        effect: SoundEffect,
        target: String = "@s",
        position: String = "~ ~ ~",
        volume: String = effect.volume,
        pitch: String = effect.pitch,
        minimumVolume: String = "0"
    ): String {
        return "/playsound ${effect.id} $target $position $volume $pitch $minimumVolume"
    }

    fun filter(query: String, category: String?): List<SoundEffect> {
        val normalizedQuery = query.trim().lowercase()
        return effects.filter { effect ->
            val matchesCategory = category == null || category == "全部" || effect.category == category
            val matchesQuery = normalizedQuery.isEmpty() ||
                effect.id.contains(normalizedQuery, ignoreCase = true) ||
                effect.name.contains(normalizedQuery, ignoreCase = true) ||
                effect.description.contains(normalizedQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }
}
