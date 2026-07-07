package com.nexuscmd.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class CommandChainStep(
    val id: String,
    val command: String,
    val name: String = "",
    val delay: Int = 0,
    val note: String = ""
)

data class CommandChain(
    val id: String,
    val name: String,
    val description: String = "",
    val steps: List<CommandChainStep>,
    val category: String = "未分类",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

class CommandChainRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAllChains(): List<CommandChain> {
        val json = prefs.getString(KEY_CHAINS, "[]") ?: "[]"
        return parseChains(json)
    }

    fun getChainById(id: String): CommandChain? {
        return getAllChains().find { it.id == id }
    }

    fun addChain(chain: CommandChain) {
        val chains = getAllChains().toMutableList()
        val newChain = chain.copy(id = generateId())
        chains.add(0, newChain)
        saveChains(chains)
    }

    fun updateChain(chain: CommandChain) {
        val chains = getAllChains().toMutableList()
        val index = chains.indexOfFirst { it.id == chain.id }
        if (index >= 0) {
            chains[index] = chain.copy(updatedAt = System.currentTimeMillis())
            saveChains(chains)
        }
    }

    fun deleteChain(chainId: String) {
        val chains = getAllChains().toMutableList()
        chains.removeAll { it.id == chainId }
        saveChains(chains)
    }

    fun searchChains(query: String): List<CommandChain> {
        val lowerQuery = query.lowercase()
        return getAllChains().filter { chain ->
            chain.name.lowercase().contains(lowerQuery) ||
            chain.description.lowercase().contains(lowerQuery) ||
            chain.steps.any { it.command.lowercase().contains(lowerQuery) }
        }
    }

    private fun saveChains(chains: List<CommandChain>) {
        val jsonArray = JSONArray()
        chains.forEach { chain ->
            jsonArray.put(chainToJson(chain))
        }
        prefs.edit().putString(KEY_CHAINS, jsonArray.toString()).apply()
    }

    private fun chainToJson(chain: CommandChain): JSONObject {
        val stepsArray = JSONArray()
        chain.steps.forEach { step ->
            stepsArray.put(JSONObject().apply {
                put("id", step.id)
                put("command", step.command)
                put("name", step.name)
                put("delay", step.delay)
                put("note", step.note)
            })
        }
        return JSONObject().apply {
            put("id", chain.id)
            put("name", chain.name)
            put("description", chain.description)
            put("category", chain.category)
            put("steps", stepsArray)
            put("createdAt", chain.createdAt)
            put("updatedAt", chain.updatedAt)
        }
    }

    private fun parseChains(json: String): List<CommandChain> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                val stepsArray = obj.optJSONArray("steps") ?: JSONArray()
                val steps = (0 until stepsArray.length()).map { j ->
                    val stepObj = stepsArray.getJSONObject(j)
                    CommandChainStep(
                        id = stepObj.optString("id", generateStepId()),
                        command = stepObj.getString("command"),
                        name = stepObj.optString("name", ""),
                        delay = stepObj.optInt("delay", 0),
                        note = stepObj.optString("note", "")
                    )
                }
                CommandChain(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    description = obj.optString("description", ""),
                    steps = steps,
                    category = obj.optString("category", "未分类"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun generateId(): String =
        "chain_${System.currentTimeMillis()}_${(0..9999).random()}"

    private fun generateStepId(): String =
        "step_${System.currentTimeMillis()}_${(0..9999).random()}"

    companion object {
        private const val PREFS_NAME = "nexus_command_chains"
        private const val KEY_CHAINS = "command_chains"
    }
}
