package com.nexuscmd.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_SHOW_FW_TIP = "show_fw_tip"
        private const val KEY_LAST_VERSION = "last_version"
    }
}
