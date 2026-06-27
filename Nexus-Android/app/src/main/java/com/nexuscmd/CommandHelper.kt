package com.nexuscmd

import android.content.Context

class CommandHelper private constructor() {
    companion object {
        init {
            System.loadLibrary("nexus")
        }
    }

    external fun initialize(jsonData: String): Boolean
    external fun getCompletions(input: String, cursor: Int): String
    external fun getHighlights(input: String): String
    external fun validateCommand(input: String): String
    external fun getCommandInfo(commandName: String): String

    object Registry {
        private var instance: CommandHelper? = null

        fun getInstance(): CommandHelper {
            if (instance == null) {
                instance = CommandHelper()
            }
            return instance!!
        }
    }
}

data class CompletionItem(
    val label: String,
    val detail: String,
    val insertText: String
)

data class HighlightToken(
    val type: Int,
    val start: Int,
    val end: Int
)

data class ValidationResult(
    val hasError: Boolean,
    val message: String? = null,
    val position: Int? = null
)
