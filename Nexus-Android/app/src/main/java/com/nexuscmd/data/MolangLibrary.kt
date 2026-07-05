package com.nexuscmd.data

object MolangLibrary {
    val properties = listOf(
        // 实体属性
        MolangItem("query.health", "生命值", "获取实体当前生命值"),
        MolangItem("query.max_health", "最大生命值", "获取实体最大生命值"),
        MolangItem("query.is_dead", "是否死亡", "实体是否已死亡"),
        MolangItem("query.is_sneaking", "是否潜行", "实体是否正在潜行"),
        MolangItem("query.is_sprinting", "是否疾跑", "实体是否正在疾跑"),
        MolangItem("query.is_grounded", "是否着地", "实体是否接触地面"),
        MolangItem("query.is_in_water", "是否在水中", "实体是否在水中"),
        MolangItem("query.is_in_lava", "是否在岩浆中", "实体是否在岩浆中"),
        MolangItem("query.is_on_fire", "是否着火", "实体是否正在燃烧"),
        MolangItem("query.is_riding", "是否骑乘", "实体是否正在骑乘"),
        MolangItem("query.is_invulnerable", "是否无敌", "实体是否处于无敌状态"),
        MolangItem("query.air_supply", "氧气值", "实体剩余氧气值"),
        MolangItem("query.food_level", "饥饿值", "玩家饥饿等级"),
        MolangItem("query.experience_level", "经验等级", "玩家经验等级"),
        MolangItem("query.attack_time", "攻击时间", "攻击动画进行时间"),
        MolangItem("query.hurt_time", "受伤时间", "受伤动画进行时间"),
        MolangItem("query.age", "年龄", "实体年龄（0为幼年）"),
        MolangItem("query.scale", "缩放比例", "实体缩放比例"),
        MolangItem("query.yaw", "偏航角", "实体水平朝向"),
        MolangItem("query.pitch", "俯仰角", "实体垂直朝向"),
        MolangItem("query.rotation", "旋转", "实体当前旋转"),
        MolangItem("query.position", "位置", "实体当前位置"),
        MolangItem("query.velocity", "速度", "实体当前速度"),
        MolangItem("query.movement_speed", "移动速度", "实体移动速度"),
        MolangItem("query.anim_time", "动画时间", "当前动画播放时间"),
        MolangItem("query.anim_progress", "动画进度", "当前动画播放进度"),
        MolangItem("query.target_distance", "目标距离", "到目标实体的距离"),
        MolangItem("query.target_x", "目标X", "目标实体X坐标"),
        MolangItem("query.target_y", "目标Y", "目标实体Y坐标"),
        MolangItem("query.target_z", "目标Z", "目标实体Z坐标"),
        MolangItem("query.damage_taken", "受到伤害", "实体受到的伤害"),
        MolangItem("query.attack_damage", "攻击伤害", "实体攻击伤害"),
        MolangItem("query.is_angry", "是否愤怒", "生物是否处于愤怒状态"),
        MolangItem("query.is_tamed", "是否被驯服", "生物是否被驯服"),
        MolangItem("query.is_sitting", "是否坐下", "生物是否坐下"),
        MolangItem("query.is_wet", "是否湿润", "实体是否湿润"),
        MolangItem("query.is_visible", "是否可见", "实体是否可见"),
        MolangItem("query.is_baby", "是否幼年", "实体是否为幼年形态"),
        MolangItem("query.is_breathing", "是否呼吸", "实体是否在呼吸"),
        MolangItem("query.is_eating", "是否进食", "实体是否在进食"),
        MolangItem("query.is_sleeping", "是否睡觉", "实体是否在睡觉"),
        MolangItem("query.is_swimming", "是否游泳", "实体是否在游泳"),
        MolangItem("query.is_flying", "是否飞行", "实体是否在飞行"),
        MolangItem("query.is_falling", "是否坠落", "实体是否正在坠落"),
        MolangItem("query.is_climbing", "是否攀爬", "实体是否在攀爬"),
        MolangItem("query.is_burning", "是否燃烧", "实体是否正在燃烧"),
        MolangItem("query.is_glowing", "是否发光", "实体是否发光"),
        MolangItem("query.is_invisible", "是否隐身", "实体是否隐身"),
        MolangItem("query.is_silent", "是否静音", "实体是否静音"),
        MolangItem("query.is_powered", "是否通电", "实体是否通电"),
        MolangItem("query.is_stunned", "是否眩晕", "实体是否眩晕"),
        MolangItem("query.is_shaking", "是否抖动", "实体是否在抖动"),
        MolangItem("query.is_charging", "是否蓄力", "实体是否在蓄力攻击"),
        
        // 世界属性
        MolangItem("query.time_of_day", "时间", "当前游戏时间"),
        MolangItem("query.day_of_year", "日期", "当前游戏天数"),
        MolangItem("query.season", "季节", "当前季节"),
        MolangItem("query.moon_phase", "月相", "当前月相"),
        MolangItem("query.weather", "天气", "当前天气"),
        MolangItem("query.temperature", "温度", "当前温度"),
        MolangItem("query.humidity", "湿度", "当前湿度"),
        
        // 数学常量
        MolangItem("math.pi", "π", "圆周率"),
        MolangItem("math.e", "e", "自然对数底数"),
        MolangItem("math.phi", "φ", "黄金比例"),
        MolangItem("math.infinity", "∞", "无穷大"),
        
        // 随机值
        MolangItem("query.random", "随机", "0-1随机值"),
        MolangItem("query.random_integer", "随机整数", "随机整数值"),
        
        // 时间相关
        MolangItem("query.life_time", "生命周期", "实体存活时间"),
        MolangItem("query.tick_count", "tick数", "游戏tick计数"),
        MolangItem("query.real_time", "真实时间", "真实世界时间"),
    )

    val variables = listOf(
        MolangItem("variable.is_attacking", "is_attacking", "攻击状态变量"),
        MolangItem("variable.is_moving", "is_moving", "移动状态变量"),
        MolangItem("variable.move_speed", "move_speed", "移动速度变量"),
        MolangItem("variable.turn_speed", "turn_speed", "转向速度变量"),
        MolangItem("variable.jump_power", "jump_power", "跳跃力度变量"),
        MolangItem("variable.attack_damage", "attack_damage", "攻击伤害变量"),
        MolangItem("variable.health", "health", "生命值变量"),
        MolangItem("variable.max_health", "max_health", "最大生命值变量"),
        MolangItem("variable.stamina", "stamina", "耐力值变量"),
        MolangItem("variable.anim_blend", "anim_blend", "动画混合变量"),
        MolangItem("variable.walk_speed", "walk_speed", "行走速度变量"),
        MolangItem("variable.run_speed", "run_speed", "跑步速度变量"),
        MolangItem("variable.swim_speed", "swim_speed", "游泳速度变量"),
        MolangItem("variable.fly_speed", "fly_speed", "飞行速度变量"),
        MolangItem("variable.custom", "custom", "自定义变量"),
    )

    val functions = listOf(
        // 数学函数
        MolangItem("math.sin(x)", "sin", "正弦函数"),
        MolangItem("math.cos(x)", "cos", "余弦函数"),
        MolangItem("math.tan(x)", "tan", "正切函数"),
        MolangItem("math.asin(x)", "asin", "反正弦函数"),
        MolangItem("math.acos(x)", "acos", "反余弦函数"),
        MolangItem("math.atan(x)", "atan", "反正切函数"),
        MolangItem("math.atan2(y, x)", "atan2", "双参数反正切"),
        MolangItem("math.sqrt(x)", "sqrt", "平方根"),
        MolangItem("math.abs(x)", "abs", "绝对值"),
        MolangItem("math.floor(x)", "floor", "向下取整"),
        MolangItem("math.ceil(x)", "ceil", "向上取整"),
        MolangItem("math.round(x)", "round", "四舍五入"),
        MolangItem("math.min(a, b)", "min", "取最小值"),
        MolangItem("math.max(a, b)", "max", "取最大值"),
        MolangItem("math.clamp(x, min, max)", "clamp", "限制范围"),
        MolangItem("math.lerp(a, b, t)", "lerp", "线性插值"),
        MolangItem("math.pow(x, y)", "pow", "幂运算"),
        MolangItem("math.exp(x)", "exp", "指数函数"),
        MolangItem("math.log(x)", "log", "自然对数"),
        MolangItem("math.log10(x)", "log10", "常用对数"),
        MolangItem("math.mod(x, y)", "mod", "取模运算"),
        MolangItem("math.sign(x)", "sign", "符号函数"),
        MolangItem("math.lerp_angle(a, b, t)", "lerp_angle", "角度插值"),
        MolangItem("math.distance(x1, y1, z1, x2, y2, z2)", "distance", "计算距离"),
        
        // 动画函数
        MolangItem("animation_progress(anim)", "animation_progress", "获取动画进度"),
        MolangItem("animation_length(anim)", "animation_length", "获取动画长度"),
        MolangItem("animation_speed(anim)", "animation_speed", "获取动画速度"),
        MolangItem("blend_weight(anim)", "blend_weight", "获取混合权重"),
        
        // 时间函数
        MolangItem("time.day", "time.day", "当前游戏天数"),
        MolangItem("time.hour", "time.hour", "当前游戏小时"),
        MolangItem("time.minute", "time.minute", "当前游戏分钟"),
        MolangItem("time.second", "time.second", "当前游戏秒"),
        MolangItem("time.tick", "time.tick", "当前游戏tick"),
        
        // 随机函数
        MolangItem("random()", "random", "0-1随机值"),
        MolangItem("random_range(min, max)", "random_range", "范围随机"),
        MolangItem("random_integer(min, max)", "random_integer", "范围随机整数"),
        
        // 逻辑函数
        MolangItem("conditional(a, b, c)", "conditional", "条件判断"),
        
        // 颜色函数
        MolangItem("rgb(r, g, b)", "rgb", "RGB颜色"),
        MolangItem("rgba(r, g, b, a)", "rgba", "RGBA颜色"),
        MolangItem("hsv(h, s, v)", "hsv", "HSV颜色"),
        
        // 向量函数
        MolangItem("vector.dot(a, b)", "vector.dot", "向量点积"),
        MolangItem("vector.cross(a, b)", "vector.cross", "向量叉积"),
        MolangItem("vector.length(v)", "vector.length", "向量长度"),
        MolangItem("vector.normalize(v)", "vector.normalize", "向量归一化"),
        MolangItem("vector.distance(a, b)", "vector.distance", "向量距离"),
    )

    val operators = listOf(
        MolangItem("+", "+", "加法"),
        MolangItem("-", "-", "减法"),
        MolangItem("*", "*", "乘法"),
        MolangItem("/", "/", "除法"),
        MolangItem("%", "%", "取模"),
        MolangItem("^", "^", "幂运算"),
        MolangItem("==", "==", "等于"),
        MolangItem("!=", "!=", "不等于"),
        MolangItem("<", "<", "小于"),
        MolangItem(">", ">", "大于"),
        MolangItem("<=", "<=", "小于等于"),
        MolangItem(">=", ">=", "大于等于"),
        MolangItem("&&", "&&", "逻辑与"),
        MolangItem("||", "||", "逻辑或"),
        MolangItem("!", "!", "逻辑非"),
    )

    fun filter(query: String): List<MolangItem> {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) {
            return properties + variables + functions
        }
        
        return (properties + variables + functions)
            .filter { item ->
                item.name.lowercase().contains(normalized) ||
                item.display.lowercase().contains(normalized) ||
                item.description.lowercase().contains(normalized)
            }
            .take(20)
    }
}

data class MolangItem(
    val name: String,
    val display: String,
    val description: String
)
