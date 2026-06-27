package com.nexuscmd.data

import com.nexuscmd.Block
import com.nexuscmd.Item

class CommandCompletionEnhancer(
    private val blockLibrary: BlockLibrary = BlockLibrary,
    private val itemLibrary: ItemLibrary = ItemLibrary,
    private val soundEffectLibrary: SoundEffectLibrary = SoundEffectLibrary,
    private val particleLibrary: ParticleLibrary = ParticleLibrary
) {

    data class EnhancedSuggestion(
        val text: String,
        val displayText: String,
        val type: SuggestionType,
        val description: String = ""
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
        val normalized = query.trim().lowercase().removePrefix("minecraft:")
        val blocks = blockLibrary.filter(normalized, null).take(15)
        return blocks.map { block ->
            EnhancedSuggestion(
                text = block.id,
                displayText = block.name,
                type = SuggestionType.BLOCK,
                description = block.id
            )
        }
    }

    private fun getItemSuggestions(query: String): List<EnhancedSuggestion> {
        val normalized = query.trim().lowercase().removePrefix("minecraft:")
        val items = itemLibrary.filter(normalized, null).take(15)
        return items.map { item ->
            EnhancedSuggestion(
                text = item.id,
                displayText = item.name,
                type = SuggestionType.ITEM,
                description = item.id
            )
        }
    }

    private fun getSoundSuggestions(query: String): List<EnhancedSuggestion> {
        val normalized = query.trim().lowercase().removePrefix("minecraft:")
        val sounds = soundEffectLibrary.filter(normalized, null).take(15)
        return sounds.map { sound ->
            EnhancedSuggestion(
                text = sound.id,
                displayText = sound.name,
                type = SuggestionType.SOUND,
                description = sound.id
            )
        }
    }

    private fun getParticleSuggestions(query: String): List<EnhancedSuggestion> {
        val normalized = query.trim().lowercase().removePrefix("minecraft:")
        val particles = particleLibrary.filter(normalized, null).take(15)
        return particles.map { particle ->
            EnhancedSuggestion(
                text = "minecraft:${particle.id}",
                displayText = particle.name,
                type = SuggestionType.PARTICLE,
                description = particle.id
            )
        }
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
