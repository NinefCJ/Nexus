package com.nexuscmd.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class AddonPack(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val author: String,
    val icon: String = "",
    val enabled: Boolean = true,
    val customBlocks: List<Block> = emptyList(),
    val customItems: List<Item> = emptyList(),
    val customSounds: List<SoundEffect> = emptyList(),
    val customParticles: List<Particle> = emptyList(),
    val customCommands: List<SavedCommand> = emptyList(),
    val customTemplates: List<SavedCommand> = emptyList()
)

class AddonManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("nexus_addons", Context.MODE_PRIVATE)

    private val installedAddons = mutableListOf<AddonPack>()

    init {
        loadDefaultAddons()
    }

    private fun loadDefaultAddons() {
        val exampleAddon = AddonPack(
            id = "example_custom_items",
            name = "示例拓展包",
            description = "展示拓展包功能的示例，包含一些自定义内容",
            version = "1.0.0",
            author = "Nexus Team",
            enabled = true,
            customBlocks = listOf(
                Block("custom:ruby_block", "红宝石块", "建筑", "由红宝石制成的装饰方块"),
                Block("custom:crystal_ore", "水晶矿石", "自然", "深山中的神秘水晶矿")
            ),
            customItems = listOf(
                Item("custom:ruby_sword", "红宝石剑", "武器", "锋利的红宝石制成的剑"),
                Item("custom:magic_wand", "魔法魔杖", "工具", "可以释放魔法的魔杖")
            ),
            customSounds = listOf(
                SoundEffect("custom:magic_chime", "魔法铃声", "魔法", "神奇的魔法音效", "1", "1")
            ),
            customParticles = listOf(
                Particle("custom:star_sparkle", "星星闪光", "魔法", "闪亮的星星粒子效果", true)
            ),
            customCommands = listOf(
                SavedCommand(
                    id = "custom:fly_cmd",
                    command = "/effect @s levitation 10 1 true",
                    name = "飞行效果",
                    description = "给玩家10秒的漂浮效果",
                    category = "实用"
                )
            ),
            customTemplates = listOf(
                SavedCommand(
                    id = "custom:house_tpl",
                    command = "/fill ~-5 ~ ~-5 ~5 ~5 ~5 stone hollow",
                    name = "快速造房",
                    description = "快速生成一个石屋框架",
                    category = "建筑"
                )
            )
        )
        installedAddons.add(exampleAddon)
    }

    fun loadAddons(): List<AddonPack> {
        return installedAddons.toList()
    }

    fun enableAddon(addonId: String) {
        val addon = installedAddons.find { it.id == addonId } ?: return
        val index = installedAddons.indexOf(addon)
        installedAddons[index] = addon.copy(enabled = true)
        prefs.edit().putBoolean("${addonId}_enabled", true).apply()
    }

    fun disableAddon(addonId: String) {
        val addon = installedAddons.find { it.id == addonId } ?: return
        val index = installedAddons.indexOf(addon)
        installedAddons[index] = addon.copy(enabled = false)
        prefs.edit().putBoolean("${addonId}_enabled", false).apply()
    }

    fun installAddon(json: String): Boolean {
        return try {
            val obj = JSONObject(json)
            val addon = parseAddon(obj)
            installedAddons.removeAll { it.id == addon.id }
            installedAddons.add(addon)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun uninstallAddon(addonId: String) {
        installedAddons.removeAll { it.id == addonId }
        prefs.edit().remove("${addonId}_enabled").apply()
    }

    fun getEnabledBlocks(): List<Block> {
        return installedAddons.filter { it.enabled }.flatMap { it.customBlocks }
    }

    fun getEnabledItems(): List<Item> {
        return installedAddons.filter { it.enabled }.flatMap { it.customItems }
    }

    fun getEnabledSounds(): List<SoundEffect> {
        return installedAddons.filter { it.enabled }.flatMap { it.customSounds }
    }

    fun getEnabledParticles(): List<Particle> {
        return installedAddons.filter { it.enabled }.flatMap { it.customParticles }
    }

    fun getEnabledCommands(): List<SavedCommand> {
        return installedAddons.filter { it.enabled }.flatMap { it.customCommands }
    }

    fun getEnabledTemplates(): List<SavedCommand> {
        return installedAddons.filter { it.enabled }.flatMap { it.customTemplates }
    }

    private fun parseAddon(obj: JSONObject): AddonPack {
        val id = obj.optString("id", "")
        val name = obj.optString("name", id)
        val description = obj.optString("description", "")
        val version = obj.optString("version", "1.0.0")
        val author = obj.optString("author", "未知作者")
        val icon = obj.optString("icon", "")
        val enabled = prefs.getBoolean("${id}_enabled", true)

        val blocks = parseBlocks(obj.optJSONArray("blocks"))
        val items = parseItems(obj.optJSONArray("items"))
        val sounds = parseSounds(obj.optJSONArray("sounds"))
        val particles = parseParticles(obj.optJSONArray("particles"))
        val commands = parseSavedCommands(obj.optJSONArray("commands"))
        val templates = parseSavedCommands(obj.optJSONArray("templates"))

        return AddonPack(
            id = id,
            name = name,
            description = description,
            version = version,
            author = author,
            icon = icon,
            enabled = enabled,
            customBlocks = blocks,
            customItems = items,
            customSounds = sounds,
            customParticles = particles,
            customCommands = commands,
            customTemplates = templates
        )
    }

    private fun parseBlocks(arr: JSONArray?): List<Block> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val obj = arr.optJSONObject(i) ?: return@mapNotNull null
            Block(
                id = obj.optString("id", ""),
                name = obj.optString("name", ""),
                category = obj.optString("category", "其他"),
                description = obj.optString("description", "")
            )
        }
    }

    private fun parseItems(arr: JSONArray?): List<Item> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val obj = arr.optJSONObject(i) ?: return@mapNotNull null
            Item(
                id = obj.optString("id", ""),
                name = obj.optString("name", ""),
                category = obj.optString("category", "其他"),
                description = obj.optString("description", "")
            )
        }
    }

    private fun parseSounds(arr: JSONArray?): List<SoundEffect> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val obj = arr.optJSONObject(i) ?: return@mapNotNull null
            SoundEffect(
                id = obj.optString("id", ""),
                name = obj.optString("name", ""),
                category = obj.optString("category", "其他"),
                description = obj.optString("description", ""),
                volume = obj.optString("volume", "1"),
                pitch = obj.optString("pitch", "1")
            )
        }
    }

    private fun parseParticles(arr: JSONArray?): List<Particle> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val obj = arr.optJSONObject(i) ?: return@mapNotNull null
            Particle(
                id = obj.optString("id", ""),
                name = obj.optString("name", ""),
                category = obj.optString("category", "其他"),
                description = obj.optString("description", ""),
                hasColor = obj.optBoolean("hasColor", true)
            )
        }
    }

    private fun parseSavedCommands(arr: JSONArray?): List<SavedCommand> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val obj = arr.optJSONObject(i) ?: return@mapNotNull null
            SavedCommand(
                id = obj.optString("id", ""),
                command = obj.optString("command", ""),
                name = obj.optString("name", ""),
                description = obj.optString("description", ""),
                category = obj.optString("category", "其他")
            )
        }
    }

    fun exportAddon(addon: AddonPack): String {
        val obj = JSONObject().apply {
            put("id", addon.id)
            put("name", addon.name)
            put("description", addon.description)
            put("version", addon.version)
            put("author", addon.author)
            put("icon", addon.icon)
            put("blocks", JSONArray(addon.customBlocks.map { b ->
                JSONObject().apply {
                    put("id", b.id)
                    put("name", b.name)
                    put("category", b.category)
                    put("description", b.description)
                }
            }))
            put("items", JSONArray(addon.customItems.map { item ->
                JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("category", item.category)
                    put("description", item.description)
                }
            }))
            put("commands", JSONArray(addon.customCommands.map { c ->
                JSONObject().apply {
                    put("id", c.id)
                    put("command", c.command)
                    put("name", c.name)
                    put("description", c.description)
                    put("category", c.category)
                }
            }))
        }
        return obj.toString(2)
    }

    companion object {
        private var instance: AddonManager? = null

        fun getInstance(context: Context): AddonManager {
            if (instance == null) {
                instance = AddonManager(context.applicationContext)
            }
            return instance!!
        }
    }
}
