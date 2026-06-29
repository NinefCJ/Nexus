package com.nexuscmd.data

import androidx.compose.runtime.Stable

@Stable
data class MCAnimation(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val targetType: String = "entity",
    val defaultController: String = ""
)

object AnimationLibrary {
    val categories = listOf(
        "全部", "玩家", "生物通用", "攻击动作", "日常动作", "表情动作", "载具", "方块实体", "特殊效果"
    )

    private const val MAX_CACHE_SIZE = 20
    private val filterCache = LinkedHashMap<String, List<MCAnimation>>(MAX_CACHE_SIZE, 0.75f, true)

    private val categoryIndex: Map<String, List<MCAnimation>> by lazy {
        animations.groupBy { it.category }
    }

    private val searchKeywords: List<Triple<String, String, MCAnimation>> by lazy {
        animations.flatMap { anim ->
            listOf(
                Triple(anim.id.lowercase(), "id", anim),
                Triple(anim.name.lowercase(), "name", anim),
                Triple(anim.description.lowercase(), "desc", anim),
                Triple(anim.targetType.lowercase(), "target", anim)
            )
        }
    }

    val animations = listOf(
        // 玩家动画
        MCAnimation("animation.player.attack.one_handed", "单手攻击", "玩家", "玩家单手攻击动画", "player", "player.attack.controller"),
        MCAnimation("animation.player.attack.two_handed", "双手攻击", "玩家", "玩家双手攻击动画", "player", "player.attack.controller"),
        MCAnimation("animation.player.attack.throw", "投掷攻击", "玩家", "玩家投掷物品动画", "player", "player.attack.controller"),
        MCAnimation("animation.player.attack.punch", "拳击", "玩家", "玩家赤手空拳攻击", "player", "player.attack.controller"),
        MCAnimation("animation.player.use_item", "使用物品", "玩家", "玩家使用物品的通用动画", "player"),
        MCAnimation("animation.player.eat", "吃东西", "玩家", "玩家进食动画", "player"),
        MCAnimation("animation.player.drink", "喝东西", "玩家", "玩家饮用动画", "player"),
        MCAnimation("animation.player.bow_and_arrow", "拉弓", "玩家", "玩家拉弓射箭动画", "player"),
        MCAnimation("animation.player.crossbow.hold", "持弩", "玩家", "玩家持有弩的姿势", "player"),
        MCAnimation("animation.player.crossbow.charge", "装填弩", "玩家", "玩家装填弩箭动画", "player"),
        MCAnimation("animation.player.shield_block", "盾牌格挡", "玩家", "玩家用盾牌格挡", "player"),
        MCAnimation("animation.player.sleep", "睡觉", "玩家", "玩家睡觉动画", "player", "player.sleep.controller"),
        MCAnimation("animation.player.sneak", "潜行", "玩家", "玩家潜行姿势", "player"),
        MCAnimation("animation.player.sprint", "疾跑", "玩家", "玩家疾跑姿势", "player"),
        MCAnimation("animation.player.swim", "游泳", "玩家", "玩家游泳动画", "player", "player.swim.controller"),
        MCAnimation("animation.player.crawl", "爬行", "玩家", "玩家爬行动画", "player"),
        MCAnimation("animation.player.climb", "攀爬", "玩家", "玩家攀爬梯子/藤蔓", "player"),
        MCAnimation("animation.player.ride.bob", "骑乘颠簸", "玩家", "玩家骑乘时的颠簸动画", "player"),
        MCAnimation("animation.player.dance", "跳舞", "玩家", "玩家跳舞动画（基岩版）", "player"),
        MCAnimation("animation.player.celebrate", "庆祝", "玩家", "玩家庆祝动作", "player"),
        MCAnimation("animation.player.wave", "挥手", "玩家", "玩家挥手打招呼", "player"),
        MCAnimation("animation.player.point", "指向", "玩家", "玩家用手指向目标", "player"),
        MCAnimation("animation.player.cry", "哭泣", "玩家", "玩家哭泣表情", "player"),
        MCAnimation("animation.player.yawn", "打哈欠", "玩家", "玩家打哈欠动画", "player"),
        MCAnimation("animation.player.think", "思考", "玩家", "玩家思考姿势", "player"),
        MCAnimation("animation.player.bow", "鞠躬", "玩家", "玩家鞠躬行礼", "player"),
        MCAnimation("animation.player.curtsey", "行屈膝礼", "玩家", "玩家行屈膝礼", "player"),
        MCAnimation("animation.player.nod", "点头", "玩家", "玩家点头同意", "player"),
        MCAnimation("animation.player.shake_head", "摇头", "玩家", "玩家摇头拒绝", "player"),
        MCAnimation("animation.player.wink", "眨眼", "玩家", "玩家眨眼示意", "player"),
        MCAnimation("animation.player.salute", "敬礼", "玩家", "玩家敬礼动作", "player"),
        MCAnimation("animation.player.choke", "窒息", "玩家", "玩家窒息挣扎", "player"),
        MCAnimation("animation.player.fishing", "钓鱼", "玩家", "玩家钓鱼姿势", "player"),
        MCAnimation("animation.player.elytra_fly", "鞘翅飞行", "玩家", "玩家使用鞘翅飞行", "player"),
        MCAnimation("animation.player.elytra_glide", "鞘翅滑翔", "玩家", "玩家滑翔姿态", "player"),
        MCAnimation("animation.player.elytra_land", "鞘翅着陆", "玩家", "玩家鞘翅着陆", "player"),

        // 生物通用动画
        MCAnimation("animation.hurt", "受伤", "生物通用", "实体受伤时的动画", "entity"),
        MCAnimation("animation.death", "死亡", "生物通用", "实体死亡动画", "entity"),
        MCAnimation("animation.attack", "攻击", "攻击动作", "实体攻击动画", "entity"),
        MCAnimation("animation.attack.melee", "近战攻击", "攻击动作", "近战攻击动画", "entity"),
        MCAnimation("animation.attack.ranged", "远程攻击", "攻击动作", "远程攻击动画", "entity"),
        MCAnimation("animation.walk", "行走", "日常动作", "实体行走动画", "entity"),
        MCAnimation("animation.idle", "空闲", "日常动作", "实体空闲待机动画", "entity"),
        MCAnimation("animation.look_at_target", "看向目标", "日常动作", "实体看向目标", "entity"),
        MCAnimation("animation.jump", "跳跃", "日常动作", "实体跳跃动画", "entity"),
        MCAnimation("animation.fall", "坠落", "日常动作", "实体坠落动画", "entity"),
        MCAnimation("animation.swim", "游泳", "日常动作", "实体游泳动画", "entity"),
        MCAnimation("animation.fly", "飞行", "日常动作", "实体飞行动画", "entity"),
        MCAnimation("animation.sit", "坐下", "日常动作", "实体坐下动画", "entity"),
        MCAnimation("animation.sleep", "睡觉", "日常动作", "实体睡觉动画", "entity"),
        MCAnimation("animation.eat", "进食", "日常动作", "实体进食动画", "entity"),
        MCAnimation("animation.drink", "饮水", "日常动作", "实体饮水动画", "entity"),
        MCAnimation("animation.breeding", "繁殖", "表情动作", "实体繁殖爱心动画", "entity"),
        MCAnimation("animation.baby_transform", "幼年成长", "表情动作", "幼年实体成长为成年", "entity"),
        MCAnimation("animation.tame", "驯服", "表情动作", "实体被驯服动画", "entity"),
        MCAnimation("animation.shake", "抖身", "表情动作", "实体抖动身体", "entity"),
        MCAnimation("animation.stretch", "伸展", "表情动作", "实体伸展身体", "entity"),
        MCAnimation("animation.yawn", "打哈欠", "表情动作", "实体打哈欠", "entity"),

        // 村民动画
        MCAnimation("animation.villager.work", "村民工作", "生物通用", "村民工作动画", "villager"),
        MCAnimation("animation.villager.harvest", "村民收获", "生物通用", "村民收获作物", "villager"),
        MCAnimation("animation.villager.farm", "村民耕作", "生物通用", "村民耕作动画", "villager"),
        MCAnimation("animation.villager.idle", "村民空闲", "生物通用", "村民空闲动画", "villager"),
        MCAnimation("animation.villager.nod", "村民点头", "表情动作", "村民点头表示同意", "villager"),
        MCAnimation("animation.villager.shake_head", "村民摇头", "表情动作", "村民摇头表示拒绝", "villager"),

        // 僵尸动画
        MCAnimation("animation.zombie.attack", "僵尸攻击", "攻击动作", "僵尸攻击动画", "zombie"),
        MCAnimation("animation.zombie.converting_to_drowned", "僵尸转溺尸", "特殊效果", "僵尸转化为溺尸", "zombie"),
        MCAnimation("animation.zombie_villager.converting", "僵尸村民转化", "特殊效果", "僵尸村民转化为村民", "zombie_villager"),

        // 骷髅动画
        MCAnimation("animation.skeleton.attack", "骷髅攻击", "攻击动作", "骷髅射箭攻击", "skeleton"),
        MCAnimation("animation.stray.attack", "流浪者攻击", "攻击动作", "流浪者射箭攻击", "stray"),

        // 爬行者动画
        MCAnimation("animation.creeper.hurt", "苦力怕受伤", "生物通用", "苦力怕受伤动画", "creeper"),
        MCAnimation("animation.creeper.death", "苦力怕死亡", "生物通用", "苦力怕死亡动画", "creeper"),
        MCAnimation("animation.creeper.swelling", "苦力怕膨胀", "特殊效果", "苦力怕即将爆炸时膨胀", "creeper"),
        MCAnimation("animation.creeper.explode", "苦力怕爆炸", "特殊效果", "苦力怕爆炸动画", "creeper"),

        // 末影人动画
        MCAnimation("animation.enderman.idle", "末影人空闲", "生物通用", "末影人空闲动画", "enderman"),
        MCAnimation("animation.enderman.scream", "末影人尖叫", "表情动作", "末影人尖叫进入敌对", "enderman"),
        MCAnimation("animation.enderman.stare", "末影人凝视", "表情动作", "末影人凝视玩家", "enderman"),
        MCAnimation("animation.enderman.teleport", "末影人传送", "特殊效果", "末影人瞬移动画", "enderman"),

        // 蜘蛛动画
        MCAnimation("animation.spider.climb", "蜘蛛攀爬", "日常动作", "蜘蛛爬墙动画", "spider"),
        MCAnimation("animation.spider.pounce", "蜘蛛扑击", "攻击动作", "蜘蛛扑击攻击", "spider"),

        // 岩浆怪/史莱姆动画
        MCAnimation("animation.slime.jump", "史莱姆跳跃", "日常动作", "史莱姆跳跃移动", "slime"),
        MCAnimation("animation.slime.squish", "史莱姆压扁", "表情动作", "史莱姆被压扁", "slime"),
        MCAnimation("animation.magma_cube.jump", "岩浆怪跳跃", "日常动作", "岩浆怪跳跃移动", "magma_cube"),

        // 守卫者动画
        MCAnimation("animation.guardian.idle", "守卫者空闲", "生物通用", "守卫者空闲动画", "guardian"),
        MCAnimation("animation.guardian.attack_loop", "守卫者激光", "攻击动作", "守卫者发射激光", "guardian"),
        MCAnimation("animation.elder_guardian.curse", "远古守卫者诅咒", "特殊效果", "远古守卫者给予挖掘疲劳", "elder_guardian"),

        // 烈焰人动画
        MCAnimation("animation.blaze.breathe", "烈焰人空闲", "生物通用", "烈焰人空闲动画", "blaze"),
        MCAnimation("animation.blaze.shoot", "烈焰人射击", "攻击动作", "烈焰人发射火球", "blaze"),

        // 恶魂动画
        MCAnimation("animation.ghast.moan", "恶魂呻吟", "表情动作", "恶魂空闲呻吟", "ghast"),
        MCAnimation("animation.ghast.scream", "恶魂尖叫", "表情动作", "恶魂受伤尖叫", "ghast"),
        MCAnimation("animation.ghast.charge", "恶魂准备", "攻击动作", "恶魂准备发射火球", "ghast"),
        MCAnimation("animation.ghast.fireball", "恶魂火球", "攻击动作", "恶魂发射火球", "ghast"),

        // 凋灵动画
        MCAnimation("animation.wither.idle", "凋灵空闲", "生物通用", "凋灵空闲动画", "wither"),
        MCAnimation("animation.wither.attack", "凋灵攻击", "攻击动作", "凋灵发射骷髅", "wither"),
        MCAnimation("animation.wither.spawn", "凋灵生成", "特殊效果", "凋灵生成动画", "wither"),
        MCAnimation("animation.wither.death", "凋灵死亡", "特殊效果", "凋灵死亡爆炸", "wither"),

        // 末影龙动画
        MCAnimation("animation.enderdragon.idle", "末影龙空闲", "生物通用", "末影龙空闲盘旋", "ender_dragon"),
        MCAnimation("animation.enderdragon.flap", "末影龙拍翅", "日常动作", "末影龙拍打翅膀", "ender_dragon"),
        MCAnimation("animation.enderdragon.growl", "末影龙咆哮", "表情动作", "末影龙咆哮", "ender_dragon"),
        MCAnimation("animation.enderdragon.hit", "末影龙被击", "生物通用", "末影龙被击中", "ender_dragon"),
        MCAnimation("animation.enderdragon.death", "末影龙死亡", "特殊效果", "末影龙死亡动画", "ender_dragon"),
        MCAnimation("animation.enderdragon.growth", "末影龙成长", "特殊效果", "末影龙成长动画", "ender_dragon"),
        MCAnimation("animation.enderdragon.heartbeat", "末影龙心跳", "特殊效果", "末影龙心跳效果", "ender_dragon"),

        // 羊驼/马动画
        MCAnimation("animation.llama.spit", "羊驼吐痰", "攻击动作", "羊驼吐痰攻击", "llama"),
        MCAnimation("animation.llama.walk", "羊驼行走", "日常动作", "羊驼行走动画", "llama"),
        MCAnimation("animation.horse.gallop", "马疾驰", "日常动作", "马匹疾驰", "horse"),
        MCAnimation("animation.horse.buck", "马弓背", "表情动作", "马弓背想甩下骑手", "horse"),
        MCAnimation("animation.horse.rear", "马直立", "表情动作", "马后腿直立", "horse"),
        MCAnimation("animation.horse.eat", "马进食", "日常动作", "马进食动画", "horse"),
        MCAnimation("animation.donkey.idle", "驴空闲", "生物通用", "驴空闲动画", "donkey"),
        MCAnimation("animation.mule.idle", "骡空闲", "生物通用", "骡空闲动画", "mule"),

        // 狼/猫动画
        MCAnimation("animation.wolf.shake", "狼抖身", "表情动作", "狼抖动身体甩水", "wolf"),
        MCAnimation("animation.wolf.tail", "狼摇尾", "表情动作", "狼摇尾巴", "wolf"),
        MCAnimation("animation.wolf.howl", "狼嚎叫", "表情动作", "狼嚎叫", "wolf"),
        MCAnimation("animation.cat.purr", "猫呼噜", "表情动作", "猫发出呼噜声", "cat"),
        MCAnimation("animation.cat.sit", "猫坐下", "日常动作", "猫坐下动画", "cat"),
        MCAnimation("animation.cat.lick", "猫舔毛", "表情动作", "猫舔舐毛发", "cat"),

        // 鹦鹉动画
        MCAnimation("animation.parrot.dance", "鹦鹉跳舞", "表情动作", "鹦鹉随音乐跳舞", "parrot"),
        MCAnimation("animation.parrot.sit", "鹦鹉站肩", "日常动作", "鹦鹉站在玩家肩上", "parrot"),
        MCAnimation("animation.parrot.fly", "鹦鹉飞行", "日常动作", "鹦鹉飞行动画", "parrot"),

        // 狐狸动画
        MCAnimation("animation.fox.sleep", "狐狸睡觉", "日常动作", "狐狸睡觉动画", "fox"),
        MCAnimation("animation.fox.sniff", "狐狸嗅探", "表情动作", "狐狸嗅探气味", "fox"),
        MCAnimation("animation.fox.spit", "狐狸吐物", "表情动作", "狐狸吐出物品", "fox"),
        MCAnimation("animation.fox.attack", "狐狸攻击", "攻击动作", "狐狸攻击动画", "fox"),

        MCAnimation("animation.panda.sit", "熊猫坐", "日常动作", "熊猫坐下吃竹子", "panda"),
        MCAnimation("animation.panda.roll", "熊猫打滚", "表情动作", "熊猫打滚玩耍", "panda"),
        MCAnimation("animation.panda.sneeze", "熊猫打喷嚏", "表情动作", "熊猫打喷嚏", "panda"),

        MCAnimation("animation.bear.stand", "熊站立", "日常动作", "熊后腿站立", "polar_bear"),
        MCAnimation("animation.bear.attack", "熊攻击", "攻击动作", "熊攻击动画", "polar_bear"),

        // 蜜蜂动画
        MCAnimation("animation.bee.loop", "蜜蜂循环", "生物通用", "蜜蜂循环飞行动画", "bee"),
        MCAnimation("animation.bee.aggressive", "蜜蜂敌对", "表情动作", "蜜蜂进入敌对状态", "bee"),
        MCAnimation("animation.bee.pollinate", "蜜蜂授粉", "日常动作", "蜜蜂为作物授粉", "bee"),
        MCAnimation("animation.bee.enter_hive", "蜜蜂入巢", "日常动作", "蜜蜂进入蜂巢", "bee"),
        MCAnimation("animation.bee.exit_hive", "蜜蜂出巢", "日常动作", "蜜蜂离开蜂巢", "bee"),

        // 山羊动画
        MCAnimation("animation.goat.prepare_ram", "山羊准备冲撞", "攻击动作", "山羊准备冲撞", "goat"),
        MCAnimation("animation.goat.ram_impact", "山羊冲撞", "攻击动作", "山羊冲撞命中", "goat"),
        MCAnimation("animation.goat.jump_to_block", "山羊跳跃", "日常动作", "山羊跳到方块上", "goat"),

        // 嗅探兽动画
        MCAnimation("animation.sniffer.idle", "嗅探兽空闲", "生物通用", "嗅探兽空闲动画", "sniffer"),
        MCAnimation("animation.sniffer.dig", "嗅探兽挖掘", "日常动作", "嗅探兽挖掘种子", "sniffer"),
        MCAnimation("animation.sniffer.sniff", "嗅探兽嗅探", "表情动作", "嗅探兽嗅探空气", "sniffer"),
        MCAnimation("animation.sniffer.explorer", "嗅探兽探测", "日常动作", "嗅探兽探测植物", "sniffer"),

        // 监守者动画
        MCAnimation("animation.warden.idle", "监守者空闲", "生物通用", "监守者空闲动画", "warden"),
        MCAnimation("animation.warden.attack", "监守者攻击", "攻击动作", "监守者近战攻击", "warden"),
        MCAnimation("animation.warden.sonic_boom", "监守者声波冲击", "攻击动作", "监守者释放声波冲击", "warden"),
        MCAnimation("animation.warden.dig", "监守者挖掘", "日常动作", "监守者从地下钻出", "warden"),
        MCAnimation("animation.warden.sniff", "监守者嗅探", "表情动作", "监守者嗅探目标", "warden"),
        MCAnimation("animation.warden.roar", "监守者咆哮", "表情动作", "监守者咆哮示威", "warden"),

        // 骆驼动画
        MCAnimation("animation.camel.walk", "骆驼行走", "日常动作", "骆驼行走动画", "camel"),
        MCAnimation("animation.camel.sit", "骆驼坐下", "日常动作", "骆驼坐下", "camel"),
        MCAnimation("animation.camel.dash", "骆驼冲刺", "日常动作", "骆驼冲刺", "camel"),

        // 载具动画
        MCAnimation("animation.boat.row", "船划行", "载具", "船划行前进动画", "boat"),
        MCAnimation("animation.minecart.riding", "矿车行进", "载具", "矿车行驶动画", "minecart"),
        MCAnimation("animation.horse.ride", "骑马", "载具", "骑马骑乘动画", "horse"),
        MCAnimation("animation.strider.walk", "炽足兽行走", "载具", "炽足兽在岩浆上行走", "strider"),
        MCAnimation("animation.strider.ride", "骑炽足兽", "载具", "骑乘炽足兽动画", "strider"),
        MCAnimation("animation.piglin.ride_strider", "猪灵骑炽足兽", "载具", "猪灵骑乘炽足兽", "piglin"),

        // 方块实体动画
        MCAnimation("animation.chest.open", "箱子打开", "方块实体", "箱子盖子打开动画", "chest"),
        MCAnimation("animation.chest.close", "箱子关闭", "方块实体", "箱子盖子关闭动画", "chest"),
        MCAnimation("animation.ender_chest.open", "末影箱打开", "方块实体", "末影箱打开动画", "ender_chest"),
        MCAnimation("animation.shulker_box.open", "潜影盒打开", "方块实体", "潜影盒打开动画", "shulker_box"),
        MCAnimation("animation.shulker_box.close", "潜影盒关闭", "方块实体", "潜影盒关闭动画", "shulker_box"),
        MCAnimation("animation.piston.extend", "活塞伸出", "方块实体", "活塞伸出动画", "piston"),
        MCAnimation("animation.piston.retract", "活塞缩回", "方块实体", "活塞缩回动画", "piston"),
        MCAnimation("animation.hopper.pull", "漏斗吸取", "方块实体", "漏斗吸取物品动画", "hopper"),
        MCAnimation("animation.lectern.book_turn", "讲台翻书", "方块实体", "讲台书本翻页", "lectern"),
        MCAnimation("animation.loom.pattern", "织布机图案", "方块实体", "织布机图案变化", "loom"),

        // 物品动画
        MCAnimation("animation.item.bob", "物品漂浮", "特殊效果", "掉落物漂浮动画", "item"),
        MCAnimation("animation.item.spin", "物品旋转", "特殊效果", "物品展示框旋转", "item_frame"),
        MCAnimation("animation.armor_stand.break", "盔甲架破坏", "方块实体", "盔甲架被破坏", "armor_stand"),
        MCAnimation("animation.armor_stand.hit", "盔甲架击打", "方块实体", "盔甲架被击打", "armor_stand"),
        MCAnimation("animation.armor_stand.place", "盔甲架放置", "方块实体", "盔甲架被放置", "armor_stand"),

        // 烟花/粒子动画
        MCAnimation("animation.firework.launch", "烟花发射", "特殊效果", "烟花火箭发射升空", "firework_rocket"),
        MCAnimation("animation.firework.blast", "烟花爆炸", "特殊效果", "烟花火箭爆炸", "firework_rocket"),
        MCAnimation("animation.firework.twinkle", "烟花闪烁", "特殊效果", "烟花闪烁效果", "firework_rocket"),
        MCAnimation("animation.tnt.primed", "TNT点燃", "特殊效果", "TNT被点燃闪烁", "tnt"),
        MCAnimation("animation.tnt.explode", "TNT爆炸", "特殊效果", "TNT爆炸动画", "tnt"),

        // 特殊动画效果
        MCAnimation("animation.totem.use", "不死图腾激活", "特殊效果", "不死图腾激活动画", "totem"),
        MCAnimation("animation.totem_of_undying", "不死图腾", "特殊效果", "不死图腾保你一命", "player"),
        MCAnimation("animation.trident.throw", "三叉戟投掷", "攻击动作", "三叉戟被投掷出去", "trident"),
        MCAnimation("animation.trident.riptide", "激流三叉戟", "特殊效果", "三叉戟激流推进", "trident"),
        MCAnimation("animation.elytra.equip", "装备鞘翅", "特殊效果", "装备鞘翅动画", "elytra"),
        MCAnimation("animation.elytra.fly", "鞘翅飞行", "特殊效果", "鞘翅滑翔动画", "elytra"),

        // 传送/魔法动画
        MCAnimation("animation.portal.travel", "传送门传送", "特殊效果", "通过传送门传送", "portal"),
        MCAnimation("animation.enderman.teleport", "末影人瞬移", "特殊效果", "末影人瞬移动画", "enderman"),
        MCAnimation("animation.ender_eye.launch", "末影之眼发射", "特殊效果", "末影之眼飞出", "ender_eye"),
        MCAnimation("animation.conduit.activate", "潮涌核心激活", "特殊效果", "潮涌核心被激活", "conduit"),
        MCAnimation("animation.conduit.ambient", "潮涌核心循环", "特殊效果", "潮涌核心循环动画", "conduit"),
        MCAnimation("animation.beacon.activate", "信标激活", "特殊效果", "信标被激活", "beacon"),
        MCAnimation("animation.beacon.power_select", "信标选效", "特殊效果", "选择信标效果", "beacon"),

        // 成就/仪式动画
        MCAnimation("animation.advancement.toast", "成就提示", "特殊效果", "获得成就提示动画", "player"),
        MCAnimation("animation.raid.omen", "不祥之兆", "特殊效果", "不祥之兆效果动画", "player"),
        MCAnimation("animation.bad_omen", "不祥之兆效果", "特殊效果", "玩家带有不祥之兆", "player"),
        MCAnimation("animation.village_hero", "村庄英雄", "特殊效果", "村庄英雄效果动画", "player"),

        // 表情动作（基岩版表情系统）
        MCAnimation("animation.emote.cheer", "欢呼", "表情动作", "玩家欢呼庆祝", "player"),
        MCAnimation("animation.emote.clap", "鼓掌", "表情动作", "玩家鼓掌", "player"),
        MCAnimation("animation.emote.dance", "跳舞", "表情动作", "玩家跳舞", "player"),
        MCAnimation("animation.emote.facepalm", "捂脸", "表情动作", "玩家捂脸表示无语", "player"),
        MCAnimation("animation.emote.headbang", "甩头", "表情动作", "玩家甩头", "player"),
        MCAnimation("animation.emote.hammer", "锤击", "表情动作", "玩家做锤击动作", "player"),
        MCAnimation("animation.emote.thumb_up", "点赞", "表情动作", "玩家竖大拇指", "player"),
        MCAnimation("animation.emote.think", "思考", "表情动作", "玩家手托下巴思考", "player"),
        MCAnimation("animation.emote.wave", "挥手", "表情动作", "玩家挥手打招呼", "player"),
        MCAnimation("animation.emote.bow", "鞠躬", "表情动作", "玩家鞠躬", "player"),
        MCAnimation("animation.emote.curtsey", "行屈膝礼", "表情动作", "玩家行屈膝礼", "player"),
        MCAnimation("animation.emote.salute", "敬礼", "表情动作", "玩家敬礼", "player"),
        MCAnimation("animation.emote.underwhelm", "无趣", "表情动作", "玩家表示无趣", "player"),
        MCAnimation("animation.emote.breakdance", "霹雳舞", "表情动作", "玩家跳霹雳舞", "player"),
        MCAnimation("animation.emote.air_guitar", "空气吉他", "表情动作", "玩家弹空气吉他", "player"),
        MCAnimation("animation.emote.diamonds_to_you", "钻石给你", "表情动作", "玩家递出钻石", "player"),
        MCAnimation("animation.emote.the_honk", "按喇叭", "表情动作", "玩家按喇叭动作", "player"),
        MCAnimation("animation.emote.woohoo", "哇哦", "表情动作", "玩家兴奋欢呼", "player"),

        // 机器人/机械动画
        MCAnimation("animation.iron_golem.walk", "铁傀儡行走", "日常动作", "铁傀儡行走动画", "iron_golem"),
        MCAnimation("animation.iron_golem.attack", "铁傀儡攻击", "攻击动作", "铁傀儡挥拳攻击", "iron_golem"),
        MCAnimation("animation.iron_golem.offer_flower", "铁傀儡献花", "表情动作", "铁傀儡向村民献花", "iron_golem"),
        MCAnimation("animation.snow_golem.walk", "雪傀儡行走", "日常动作", "雪傀儡行走动画", "snow_golem"),

        // 掠夺者/卫道士动画
        MCAnimation("animation.pillager.attack", "掠夺者攻击", "攻击动作", "掠夺者射箭攻击", "pillager"),
        MCAnimation("animation.evoker.cast_spell", "唤魔者施法", "攻击动作", "唤魔者施法动画", "evoker"),
        MCAnimation("animation.evoker.prepare_summon", "唤魔者召唤", "攻击动作", "唤魔者准备召唤恼鬼", "evoker"),
        MCAnimation("animation.evoker.prepare_attack", "唤魔者尖牙", "攻击动作", "唤魔者召唤尖牙攻击", "evoker"),
        MCAnimation("animation.vindicator.attack", "卫道士攻击", "攻击动作", "卫道士斧击攻击", "vindicator"),
        MCAnimation("animation.ravager.attack", "劫掠兽攻击", "攻击动作", "劫掠兽攻击动画", "ravager"),
        MCAnimation("animation.ravager.roar", "劫掠兽咆哮", "表情动作", "劫掠兽咆哮", "ravager"),

        // 猪灵/疣猪兽动画
        MCAnimation("animation.piglin.attack", "猪灵攻击", "攻击动作", "猪灵攻击动画", "piglin"),
        MCAnimation("animation.piglin.admire", "猪灵欣赏", "表情动作", "猪灵欣赏金锭", "piglin"),
        MCAnimation("animation.piglin.dance", "猪灵跳舞", "表情动作", "猪灵在猪灵堡垒跳舞", "piglin"),
        MCAnimation("animation.hoglin.attack", "疣猪兽攻击", "攻击动作", "疣猪兽冲撞攻击", "hoglin"),
        MCAnimation("animation.hoglin.retreat", "疣猪兽逃离", "日常动作", "疣猪兽逃离诡异菌", "hoglin"),
        MCAnimation("animation.zombified_piglin.attack", "僵尸猪灵攻击", "攻击动作", "僵尸猪灵攻击", "zombified_piglin"),

        // 远古守卫者/海底神殿
        MCAnimation("animation.elder_guardian.death", "远古守卫者死亡", "特殊效果", "远古守卫者死亡", "elder_guardian"),
        MCAnimation("animation.elder_guardian.hit", "远古守卫者被击", "生物通用", "远古守卫者被击中", "elder_guardian"),
        MCAnimation("animation.elder_guardian.idle", "远古守卫者空闲", "生物通用", "远古守卫者空闲", "elder_guardian"),

        // 潮涌核心
        MCAnimation("animation.conduit.deactivate", "潮涌核心失效", "特殊效果", "潮涌核心失效", "conduit"),
        MCAnimation("animation.conduit.attack", "潮涌核心攻击", "攻击动作", "潮涌核心攻击生物", "conduit"),
        MCAnimation("animation.conduit.short", "潮涌核心环境", "特殊效果", "潮涌核心环境效果", "conduit"),

        // 更多玩家动画
        MCAnimation("animation.player.elytra_idle", "鞘翅空闲", "玩家", "鞘翅装备时的空闲姿势", "player"),
        MCAnimation("animation.player.elytra_boost", "鞘翅加速", "玩家", "使用烟花火箭加速鞘翅", "player"),
        MCAnimation("animation.player.trident_idle", "三叉戟持握", "玩家", "持握三叉戟的姿势", "player"),
        MCAnimation("animation.player.trident_charge", "三叉戟蓄力", "玩家", "三叉戟蓄力投掷", "player"),
        MCAnimation("animation.player.crossbow_firework", "弩射烟花", "玩家", "用弩发射烟花火箭", "player"),

        // 额外表情
        MCAnimation("animation.emote.shrug", "耸肩", "表情动作", "玩家耸肩表示不知道", "player"),
        MCAnimation("animation.emote.sigh", "叹气", "表情动作", "玩家叹气", "player"),
        MCAnimation("animation.emote.surprised", "惊讶", "表情动作", "玩家惊讶表情", "player"),
        MCAnimation("animation.emote.laugh", "大笑", "表情动作", "玩家捧腹大笑", "player"),
        MCAnimation("animation.emote.angry", "愤怒", "表情动作", "玩家愤怒表情", "player"),
        MCAnimation("animation.emote.love", "爱心", "表情动作", "玩家比心", "player"),
        MCAnimation("animation.emote.eyebrow_raise", "挑眉", "表情动作", "玩家挑眉质疑", "player"),
        MCAnimation("animation.emote.sick", "生病", "表情动作", "玩家生病难受", "player"),
        MCAnimation("animation.emote.yawn", "打哈欠", "表情动作", "玩家打哈欠", "player"),
        MCAnimation("animation.emote.sleepy", "困倦", "表情动作", "玩家昏昏欲睡", "player")
    )

    fun buildPlayAnimationCommand(
        animation: MCAnimation,
        target: String = "@s",
        nextState: String = "default",
        stopExpression: String = "",
        controller: String = "",
        blendOutTime: String = "0"
    ): String {
        val sb = StringBuilder()
        sb.append("/playanimation ")
        sb.append(target)
        sb.append(' ')
        sb.append(animation.id)
        if (nextState.isNotEmpty() && nextState != "default") {
            sb.append(' ')
            sb.append(nextState)
        }
        if (stopExpression.isNotEmpty()) {
            sb.append(' ')
            sb.append(stopExpression)
        }
        if (controller.isNotEmpty()) {
            sb.append(' ')
            sb.append(controller)
        }
        if (blendOutTime.isNotEmpty() && blendOutTime != "0") {
            sb.append(' ')
            sb.append(blendOutTime)
        }
        return sb.toString()
    }

    fun filter(query: String, category: String?): List<MCAnimation> {
        val normalizedQuery = query.trim().lowercase()
        val cacheKey = "$normalizedQuery|${category ?: "null"}"

        synchronized(filterCache) {
            filterCache[cacheKey]?.let { return it }
        }

        val categoryFiltered = if (category == null || category == "全部") {
            animations
        } else {
            categoryIndex[category] ?: emptyList()
        }

        val result = if (normalizedQuery.isEmpty()) {
            categoryFiltered
        } else {
            val matchedIds = mutableSetOf<MCAnimation>()
            for ((keyword, _, anim) in searchKeywords) {
                if (keyword.contains(normalizedQuery)) {
                    matchedIds.add(anim)
                }
            }
            categoryFiltered.filter { it in matchedIds }
        }

        synchronized(filterCache) {
            if (filterCache.size >= MAX_CACHE_SIZE) {
                val eldest = filterCache.entries.first().key
                filterCache.remove(eldest)
            }
            filterCache[cacheKey] = result
        }

        return result
    }
}
