package com.nexuscmd.data

import android.content.Context
import android.content.SharedPreferences

enum class AppTheme(val id: String, val displayName: String) {
    FOLLOW_SYSTEM("follow_system", "跟随系统"),
    LIGHT("light", "浅色"),
    DARK("dark", "深色"),
    MIDNIGHT("midnight", "午夜"),
    AMOLED("amoled", "AMOLED黑"),
    GREEN("green", "绿色护眼"),
    OCEAN("ocean", "海洋蓝"),
    WARM("warm", "暖橙")
}

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var currentTheme: AppTheme
        get() {
            val id = prefs.getString(KEY_THEME, AppTheme.FOLLOW_SYSTEM.id) ?: AppTheme.FOLLOW_SYSTEM.id
            return AppTheme.values().find { it.id == id } ?: AppTheme.FOLLOW_SYSTEM
        }
        set(value) = prefs.edit().putString(KEY_THEME, value.id).apply()

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    var showFloatingWindowTip: Boolean
        get() = prefs.getBoolean(KEY_SHOW_FW_TIP, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_FW_TIP, value).apply()

    var lastUsedVersion: Int
        get() = prefs.getInt(KEY_LAST_VERSION, 0)
        set(value) = prefs.edit().putInt(KEY_LAST_VERSION, value).apply()

    fun clearAllData() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "nexus_settings"
        private const val KEY_THEME = "app_theme"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_SHOW_FW_TIP = "show_fw_tip"
        private const val KEY_LAST_VERSION = "last_version"
    }
}
