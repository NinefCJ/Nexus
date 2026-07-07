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

    // 性能优化：添加内存缓存
    private var themeCache: AppTheme? = null
    private var darkThemeCache: Boolean? = null
    private var customBackgroundCache: String? = null
    private var backgroundOpacityCache: Float? = null
    private var cardOpacityCache: Float? = null
    private var useCustomBackgroundCache: Boolean? = null
    private var useGlassmorphismCache: Boolean? = null
    private var glassmorphismIntensityCache: Float? = null
    private var cardCornerRadiusCache: Float? = null
    private var useDynamicColorCache: Boolean? = null
    private var useGradientAccentsCache: Boolean? = null

    var currentTheme: AppTheme
        get() {
            // 性能优化：使用缓存
            if (themeCache != null) return themeCache!!
            val id = prefs.getString(KEY_THEME, AppTheme.FOLLOW_SYSTEM.id) ?: AppTheme.FOLLOW_SYSTEM.id
            themeCache = AppTheme.values().find { it.id == id } ?: AppTheme.FOLLOW_SYSTEM
            return themeCache!!
        }
        set(value) {
            prefs.edit().putString(KEY_THEME, value.id).apply()
            themeCache = value
        }

    var isDarkTheme: Boolean
        get() {
            // 性能优化：使用缓存
            if (darkThemeCache != null) return darkThemeCache!!
            darkThemeCache = prefs.getBoolean(KEY_DARK_THEME, false)
            return darkThemeCache!!
        }
        set(value) {
            prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()
            darkThemeCache = value
        }

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
        get() {
            // 性能优化：使用缓存
            if (customBackgroundCache != null) return customBackgroundCache
            customBackgroundCache = prefs.getString(KEY_CUSTOM_BG, null)
            return customBackgroundCache
        }
        set(value) {
            prefs.edit().putString(KEY_CUSTOM_BG, value).apply()
            customBackgroundCache = value
        }

    var backgroundOpacity: Float
        get() {
            // 性能优化：使用缓存
            if (backgroundOpacityCache != null) return backgroundOpacityCache!!
            backgroundOpacityCache = prefs.getFloat(KEY_BG_OPACITY, 0.85f)
            return backgroundOpacityCache!!
        }
        set(value) {
            prefs.edit().putFloat(KEY_BG_OPACITY, value).apply()
            backgroundOpacityCache = value
        }

    var cardOpacity: Float
        get() {
            // 性能优化：使用缓存
            if (cardOpacityCache != null) return cardOpacityCache!!
            cardOpacityCache = prefs.getFloat(KEY_CARD_OPACITY, 0.9f)
            return cardOpacityCache!!
        }
        set(value) {
            prefs.edit().putFloat(KEY_CARD_OPACITY, value).apply()
            cardOpacityCache = value
        }

    var useCustomBackground: Boolean
        get() {
            // 性能优化：使用缓存
            if (useCustomBackgroundCache != null) return useCustomBackgroundCache!!
            useCustomBackgroundCache = prefs.getBoolean(KEY_USE_CUSTOM_BG, false)
            return useCustomBackgroundCache!!
        }
        set(value) {
            prefs.edit().putBoolean(KEY_USE_CUSTOM_BG, value).apply()
            useCustomBackgroundCache = value
        }

    // UI Customization
    var useGlassmorphism: Boolean
        get() {
            // 性能优化：使用缓存
            if (useGlassmorphismCache != null) return useGlassmorphismCache!!
            useGlassmorphismCache = prefs.getBoolean(KEY_USE_GLASS, true)
            return useGlassmorphismCache!!
        }
        set(value) {
            prefs.edit().putBoolean(KEY_USE_GLASS, value).apply()
            useGlassmorphismCache = value
        }

    var glassmorphismIntensity: Float
        get() {
            // 性能优化：使用缓存
            if (glassmorphismIntensityCache != null) return glassmorphismIntensityCache!!
            glassmorphismIntensityCache = prefs.getFloat(KEY_GLASS_INTENSITY, 0.7f)
            return glassmorphismIntensityCache!!
        }
        set(value) {
            prefs.edit().putFloat(KEY_GLASS_INTENSITY, value).apply()
            glassmorphismIntensityCache = value
        }

    var cardCornerRadius: Float
        get() {
            // 性能优化：使用缓存
            if (cardCornerRadiusCache != null) return cardCornerRadiusCache!!
            cardCornerRadiusCache = prefs.getFloat(KEY_CARD_CORNER_RADIUS, 16f)
            return cardCornerRadiusCache!!
        }
        set(value) {
            prefs.edit().putFloat(KEY_CARD_CORNER_RADIUS, value).apply()
            cardCornerRadiusCache = value
        }

    var useDynamicColor: Boolean
        get() {
            // 性能优化：使用缓存
            if (useDynamicColorCache != null) return useDynamicColorCache!!
            useDynamicColorCache = prefs.getBoolean(KEY_DYNAMIC_COLOR, false)
            return useDynamicColorCache!!
        }
        set(value) {
            prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, value).apply()
            useDynamicColorCache = value
        }

    var useGradientAccents: Boolean
        get() {
            // 性能优化：使用缓存
            if (useGradientAccentsCache != null) return useGradientAccentsCache!!
            useGradientAccentsCache = prefs.getBoolean(KEY_USE_GRADIENT, false)
            return useGradientAccentsCache!!
        }
        set(value) {
            prefs.edit().putBoolean(KEY_USE_GRADIENT, value).apply()
            useGradientAccentsCache = value
        }

    fun clearAllData() {
        prefs.edit().clear().apply()
        // 性能优化：清空缓存
        clearCache()
    }

    // 性能优化：清空缓存方法
    private fun clearCache() {
        themeCache = null
        darkThemeCache = null
        customBackgroundCache = null
        backgroundOpacityCache = null
        cardOpacityCache = null
        useCustomBackgroundCache = null
        useGlassmorphismCache = null
        glassmorphismIntensityCache = null
        cardCornerRadiusCache = null
        useDynamicColorCache = null
        useGradientAccentsCache = null
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
        private const val KEY_USE_GLASS = "use_glassmorphism"
        private const val KEY_GLASS_INTENSITY = "glassmorphism_intensity"
        private const val KEY_CARD_CORNER_RADIUS = "card_corner_radius"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_USE_GRADIENT = "use_gradient_accents"
    }
}
