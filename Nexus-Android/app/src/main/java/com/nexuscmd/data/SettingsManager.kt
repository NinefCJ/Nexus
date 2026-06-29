package com.nexuscmd.data

import android.content.Context
import android.content.SharedPreferences

enum class AppTheme(val id: String, val displayName: String, val isDark: Boolean = false) {
    FOLLOW_SYSTEM("follow_system", "跟随系统"),
    LIGHT("light", "浅色"),
    DARK("dark", "深色"),
    MIDNIGHT("midnight", "午夜蓝", true),
    AMOLED("amoled", "AMOLED黑", true),
    GREEN("green", "绿色护眼"),
    OCEAN("ocean", "海洋蓝"),
    WARM("warm", "暖橙"),
    MATCHA("matcha", "抹茶绿"),
    DREAMY_PURPLE("dreamy_purple", "梦幻紫"),
    SAKURA("sakura", "樱花粉"),
    ARCTIC("arctic", "北极蓝")
}

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val appContext = context.applicationContext

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

    var addonCompletionsFirst: Boolean
        get() = prefs.getBoolean(KEY_ADDON_FIRST, false)
        set(value) = prefs.edit().putBoolean(KEY_ADDON_FIRST, value).apply()

    var customBackgroundUri: String?
        get() = prefs.getString(KEY_CUSTOM_BG, null)
        set(value) = prefs.edit().putString(KEY_CUSTOM_BG, value).apply()

    var backgroundOpacity: Float
        get() = prefs.getFloat(KEY_BG_OPACITY, 0.85f)
        set(value) = prefs.edit().putFloat(KEY_BG_OPACITY, value).apply()

    var cardOpacity: Float
        get() = prefs.getFloat(KEY_CARD_OPACITY, 0.9f)
        set(value) = prefs.edit().putFloat(KEY_CARD_OPACITY, value).apply()

    var useCustomBackground: Boolean
        get() = prefs.getBoolean(KEY_USE_CUSTOM_BG, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_CUSTOM_BG, value).apply()

    fun clearAllData() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "nexus_settings"
        private const val KEY_THEME = "app_theme"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_SHOW_FW_TIP = "show_fw_tip"
        private const val KEY_LAST_VERSION = "last_version"
        private const val KEY_ADDON_FIRST = "addon_completions_first"
        private const val KEY_CUSTOM_BG = "custom_background_uri"
        private const val KEY_BG_OPACITY = "background_opacity"
        private const val KEY_CARD_OPACITY = "card_opacity"
        private const val KEY_USE_CUSTOM_BG = "use_custom_background"
    }
}
