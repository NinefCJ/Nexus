package com.nexuscmd.data

class CommandCompletionEnhancer(
    private val blockLibrary: BlockLibrary = BlockLibrary,
    private val itemLibrary: ItemLibrary = ItemLibrary,
    private val soundEffectLibrary: SoundEffectLibrary = SoundEffectLibrary,
    private val particleLibrary: ParticleLibrary = ParticleLibrary,
    private val addonBlocks: List<Block> = emptyList(),
    private val addonItems: List<Item> = emptyList(),
    private val addonSounds: List<SoundEffect> = emptyList(),
    private val addonParticles: List<Particle> = emptyList(),
    private val addonFirst: Boolean = false
) {

    data class EnhancedSuggestion(
        val text: String,
        val displayText: String,
        val type: SuggestionType,
        val description: String = "",
        val source: String = "minecraft"  // "minecraft" or addon id
    )

    enum class SuggestionType {
        COMMAND,
        BLOCK,
        ITEM,
        SOUND,
        PARTICLE,
        SELECTOR,
        COORDINATE,
        TAG
    }

    fun enhanceCompletions(
        currentInput: String,
        cursorPosition: Int,
        baseCompletions: List<String>
    ): List<EnhancedSuggestion> {
        val text = currentInput.take(cursorPosition)

        if (text.startsWith("/") && text.contains(" ")) {
            val cmdName = text.trimStart('/').substringBefore(" ")
            val args = text.substringAfter(" ").split(" ")
            val currentArgIndex = args.size - 1
            val currentArg = args.lastOrNull() ?: ""

            return when (cmdName.lowercase()) {
                "setblock", "fill", "clone", "testforblock", "testforblocks" -> {
                    if (currentArgIndex >= 2) getBlockSuggestions(currentArg)
                    else baseCompletions.map { EnhancedSuggestion(it, it, SuggestionType.COMMAND) }
                }
                "give", "replaceitem", "clear", "enchant" -> {
                    if (currentArgIndex >= 1) getItemSuggestions(currentArg)
                    else baseCompletions.map { EnhancedSuggestion(it, it, SuggestionType.COMMAND) }
                }
                "playsound", "stopsound" -> {
                    if (currentArgIndex >= 0) getSoundSuggestions(currentArg)
                    else baseCompletions.map { EnhancedSuggestion(it, it, SuggestionType.COMMAND) }
                }
                "particle" -> {
                    if (currentArgIndex >= 0) getParticleSuggestions(currentArg)
                    else baseCompletions.map { EnhancedSuggestion(it, it, SuggestionType.COMMAND) }
                }
                "hud" -> {
                    getHudSuggestions(currentArgIndex, currentArg, baseCompletions)
                }
                "summon", "effect" -> {
                    if (currentArgIndex >= 0) baseCompletions.map { EnhancedSuggestion(it, it, SuggestionType.COMMAND) }
                    else baseCompletions.map { EnhancedSuggestion(it, it, SuggestionType.COMMAND) }
                }
                else -> baseCompletions.map { EnhancedSuggestion(it, it, SuggestionType.COMMAND) }
            }
        }

        return baseCompletions.map { EnhancedSuggestion(it, it, SuggestionType.COMMAND) }
    }

    private fun getBlockSuggestions(query: String): List<EnhancedSuggestion> {
        val normalized = query.trim().lowercase()
        val vanillaBlocks = blockLibrary.filter(normalized.removePrefix("minecraft:"), null).take(15)
        val addonBlockSuggestions = addonBlocks
            .filter {
                it.id.contains(normalized, ignoreCase = true) ||
                it.name.contains(normalized, ignoreCase = true)
            }
            .take(15)
            .map { block ->
                EnhancedSuggestion(
                    text = block.id,
                    displayText = block.name,
                    type = SuggestionType.BLOCK,
                    description = block.id,
                    source = "addon"
                )
            }

        val vanillaSuggestions = vanillaBlocks.map { block ->
            EnhancedSuggestion(
                text = block.id,
                displayText = block.name,
                type = SuggestionType.BLOCK,
                description = block.id,
                source = "minecraft"
            )
        }

        return if (addonFirst) addonBlockSuggestions + vanillaSuggestions
               else vanillaSuggestions + addonBlockSuggestions
    }

    private fun getItemSuggestions(query: String): List<EnhancedSuggestion> {
        val normalized = query.trim().lowercase()
        val vanillaItems = itemLibrary.filter(normalized.removePrefix("minecraft:"), null).take(15)
        val addonItemSuggestions = addonItems
            .filter {
                it.id.contains(normalized, ignoreCase = true) ||
                it.name.contains(normalized, ignoreCase = true)
            }
            .take(15)
            .map { item ->
                EnhancedSuggestion(
                    text = item.id,
                    displayText = item.name,
                    type = SuggestionType.ITEM,
                    description = item.id,
                    source = "addon"
                )
            }

        val vanillaSuggestions = vanillaItems.map { item ->
            EnhancedSuggestion(
                text = item.id,
                displayText = item.name,
                type = SuggestionType.ITEM,
                description = item.id,
                source = "minecraft"
            )
        }

        return if (addonFirst) addonItemSuggestions + vanillaSuggestions
               else vanillaSuggestions + addonItemSuggestions
    }

    private fun getSoundSuggestions(query: String): List<EnhancedSuggestion> {
        val normalized = query.trim().lowercase()
        val vanillaSounds = soundEffectLibrary.filter(normalized.removePrefix("minecraft:"), null).take(15)
        val addonSoundSuggestions = addonSounds
            .filter {
                it.id.contains(normalized, ignoreCase = true) ||
                it.name.contains(normalized, ignoreCase = true)
            }
            .take(15)
            .map { sound ->
                EnhancedSuggestion(
                    text = sound.id,
                    displayText = sound.name,
                    type = SuggestionType.SOUND,
                    description = sound.id,
                    source = "addon"
                )
            }

        val vanillaSuggestions = vanillaSounds.map { sound ->
            EnhancedSuggestion(
                text = sound.id,
                displayText = sound.name,
                type = SuggestionType.SOUND,
                description = sound.id,
                source = "minecraft"
            )
        }

        return if (addonFirst) addonSoundSuggestions + vanillaSuggestions
               else vanillaSuggestions + addonSoundSuggestions
    }

    private fun getHudSuggestions(argIndex: Int, currentArg: String, baseCompletions: List<String>): List<EnhancedSuggestion> {
        val normalized = currentArg.trim().lowercase()
        return when (argIndex) {
            0 -> selectors.filter { it.text.lowercase().contains(normalized) }
            1 -> listOf(
                EnhancedSuggestion("hide", "hide (隐藏)", SuggestionType.COMMAND, "隐藏指定的HUD元素"),
                EnhancedSuggestion("show", "show (显示)", SuggestionType.COMMAND, "显示指定的HUD元素")
            ).filter { it.text.lowercase().contains(normalized) }
            2 -> hudElements.filter { it.text.lowercase().contains(normalized) || it.displayText.lowercase().contains(normalized) }
            else -> baseCompletions.map { EnhancedSuggestion(it, it, SuggestionType.COMMAND) }
        }
    }

    private val hudElements = listOf(
        EnhancedSuggestion("all", "all (全部)", SuggestionType.COMMAND, "所有HUD元素"),
        EnhancedSuggestion("paperdoll", "paperdoll (纸娃娃)", SuggestionType.COMMAND, "玩家纸娃娃显示"),
        EnhancedSuggestion("armor", "armor (盔甲值)", SuggestionType.COMMAND, "盔甲值显示"),
        EnhancedSuggestion("tooltips", "tooltips (物品提示)", SuggestionType.COMMAND, "物品提示信息"),
        EnhancedSuggestion("touch_controls", "touch_controls (触控按钮)", SuggestionType.COMMAND, "触控控制按钮"),
        EnhancedSuggestion("crosshair", "crosshair (十字准星)", SuggestionType.COMMAND, "十字准星显示"),
        EnhancedSuggestion("hotbar", "hotbar (快捷栏)", SuggestionType.COMMAND, "物品快捷栏"),
        EnhancedSuggestion("health", "health (生命值)", SuggestionType.COMMAND, "生命值显示"),
        EnhancedSuggestion("progress", "progress (进度条)", SuggestionType.COMMAND, "进度条（如跳跃蓄力）"),
        EnhancedSuggestion("food", "food (饥饿值)", SuggestionType.COMMAND, "饥饿值显示"),
        EnhancedSuggestion("air", "air (氧气值)", SuggestionType.COMMAND, "氧气值显示"),
        EnhancedSuggestion("horse_health", "horse_health (坐骑生命)", SuggestionType.COMMAND, "坐骑生命值"),
        EnhancedSuggestion("mount_hotbar", "mount_hotbar (坐骑快捷栏)", SuggestionType.COMMAND, "坐骑物品快捷栏")
    )

    private fun getParticleSuggestions(query: String): List<EnhancedSuggestion> {
        val normalized = query.trim().lowercase()
        val vanillaParticles = particleLibrary.filter(normalized.removePrefix("minecraft:"), null).take(15)
        val addonParticleSuggestions = addonParticles
            .filter {
                it.id.contains(normalized, ignoreCase = true) ||
                it.name.contains(normalized, ignoreCase = true)
            }
            .take(15)
            .map { particle ->
                EnhancedSuggestion(
                    text = "minecraft:${particle.id}",
                    displayText = particle.name,
                    type = SuggestionType.PARTICLE,
                    description = particle.id,
                    source = "addon"
                )
            }

        val vanillaSuggestions = vanillaParticles.map { particle ->
            EnhancedSuggestion(
                text = "minecraft:${particle.id}",
                displayText = particle.name,
                type = SuggestionType.PARTICLE,
                description = particle.id,
                source = "minecraft"
            )
        }

        return if (addonFirst) addonParticleSuggestions + vanillaSuggestions
               else vanillaSuggestions + addonParticleSuggestions
    }

    companion object {
        val selectors = listOf(
            EnhancedSuggestion("@s", "@s (自己)", SuggestionType.SELECTOR, "选择执行命令的实体"),
            EnhancedSuggestion("@p", "@p (最近玩家)", SuggestionType.SELECTOR, "选择最近的玩家"),
            EnhancedSuggestion("@a", "@a (所有玩家)", SuggestionType.SELECTOR, "选择所有玩家"),
            EnhancedSuggestion("@e", "@e (所有实体)", SuggestionType.SELECTOR, "选择所有实体"),
            EnhancedSuggestion("@r", "@r (随机)", SuggestionType.SELECTOR, "随机选择玩家")
        )

        val coordinates = listOf(
            EnhancedSuggestion("~ ~ ~", "~ ~ ~ (当前位置)", SuggestionType.COORDINATE, "相对坐标，当前位置"),
            EnhancedSuggestion("^ ^ ^", "^ ^ ^ (视线前方)", SuggestionType.COORDINATE, "局部坐标，视线方向"),
            EnhancedSuggestion("~1 ~1 ~1", "~1 ~1 ~1", SuggestionType.COORDINATE, "相对坐标偏移"),
            EnhancedSuggestion("~~1 ~", "~~1 ~ (上方1格)", SuggestionType.COORDINATE, "当前位置上方1格")
        )
    }
}
