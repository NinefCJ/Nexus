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
    external fun getSyntaxHint(input: String, cursor: Int): String
    external fun getParameterHint(commandName: String, paramIndex: Int): String

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

data class CommandInfo(
    val name: String,
    val syntax: String = "",
    val description: String = ""
)

data class SyntaxHint(
    val template: String = "",
    val activeParamStart: Int = 0,
    val activeParamEnd: Int = 0,
    val activeParamIndex: Int = 0,
    val activeParamName: String = "",
    val activeParamHint: String = "",
    val isOptional: Boolean = false
)

data class ParameterHint(
    val hint: String = ""
)
