package com.nexuscmd.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.nexuscmd.data.AppTheme

// 浅色主题
private val LightColorScheme = lightColorScheme(
    primary = MCLightPrimary,
    secondary = MCLightAccent,
    tertiary = MCGreen,
    background = MCLightBg,
    surface = MCLightSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = MCLightText,
    onSurface = MCLightText,
    surfaceVariant = MCLightPrimary.copy(alpha = 0.1f),
    onSurfaceVariant = MCLightTextSecondary
)

// 深色主题
private val DarkColorScheme = darkColorScheme(
    primary = MCDarkPrimary,
    secondary = MCDarkAccent,
    tertiary = MCLightGreen,
    background = MCDarkBg,
    surface = MCDarkSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = MCDarkText,
    onSurface = MCDarkText,
    surfaceVariant = MCDarkPrimary.copy(alpha = 0.3f),
    onSurfaceVariant = MCDarkTextSecondary
)

// 午夜蓝主题
private val MidnightColorScheme = darkColorScheme(
    primary = MidnightPrimary,
    secondary = MidnightAccent,
    tertiary = MidnightTertiary,
    background = MidnightBg,
    surface = MidnightSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = MidnightText,
    onSurface = MidnightText,
    surfaceVariant = MidnightPrimary.copy(alpha = 0.25f),
    onSurfaceVariant = MidnightTextSecondary
)

// AMOLED纯黑主题
private val AmoledColorScheme = darkColorScheme(
    primary = AmoledPrimary,
    secondary = AmoledAccent,
    tertiary = AmoledTertiary,
    background = AmoledBg,
    surface = AmoledSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = AmoledText,
    onSurface = AmoledText,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = AmoledTextSecondary
)

// 绿色护眼主题
private val GreenLightColorScheme = lightColorScheme(
    primary = GreenLightPrimary,
    secondary = GreenLightAccent,
    tertiary = GreenLightTertiary,
    background = GreenLightBg,
    surface = GreenLightSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = GreenLightText,
    onSurface = GreenLightText,
    surfaceVariant = GreenLightPrimary.copy(alpha = 0.15f),
    onSurfaceVariant = GreenLightTextSecondary
)

private val GreenDarkColorScheme = darkColorScheme(
    primary = GreenDarkPrimary,
    secondary = GreenDarkAccent,
    tertiary = GreenDarkTertiary,
    background = GreenDarkBg,
    surface = GreenDarkSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = GreenDarkText,
    onSurface = GreenDarkText,
    surfaceVariant = GreenDarkPrimary.copy(alpha = 0.25f),
    onSurfaceVariant = GreenDarkTextSecondary
)

// 海洋蓝主题
private val OceanLightColorScheme = lightColorScheme(
    primary = OceanLightPrimary,
    secondary = OceanLightAccent,
    tertiary = OceanLightTertiary,
    background = OceanLightBg,
    surface = OceanLightSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = OceanLightText,
    onSurface = OceanLightText,
    surfaceVariant = OceanLightPrimary.copy(alpha = 0.12f),
    onSurfaceVariant = OceanLightTextSecondary
)

private val OceanDarkColorScheme = darkColorScheme(
    primary = OceanDarkPrimary,
    secondary = OceanDarkAccent,
    tertiary = OceanDarkTertiary,
    background = OceanDarkBg,
    surface = OceanDarkSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = OceanDarkText,
    onSurface = OceanDarkText,
    surfaceVariant = OceanDarkPrimary.copy(alpha = 0.25f),
    onSurfaceVariant = OceanDarkTextSecondary
)

// 暖橙主题
private val WarmLightColorScheme = lightColorScheme(
    primary = WarmLightPrimary,
    secondary = WarmLightAccent,
    tertiary = WarmLightTertiary,
    background = WarmLightBg,
    surface = WarmLightSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = WarmLightText,
    onSurface = WarmLightText,
    surfaceVariant = WarmLightPrimary.copy(alpha = 0.12f),
    onSurfaceVariant = WarmLightTextSecondary
)

private val WarmDarkColorScheme = darkColorScheme(
    primary = WarmDarkPrimary,
    secondary = WarmDarkAccent,
    tertiary = WarmDarkTertiary,
    background = WarmDarkBg,
    surface = WarmDarkSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = WarmDarkText,
    onSurface = WarmDarkText,
    surfaceVariant = WarmDarkPrimary.copy(alpha = 0.25f),
    onSurfaceVariant = WarmDarkTextSecondary
)

// 抹茶绿主题
private val MatchaLightColorScheme = lightColorScheme(
    primary = MatchaLightPrimary,
    secondary = MatchaLightAccent,
    tertiary = MatchaLightTertiary,
    background = MatchaLightBg,
    surface = MatchaLightSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = MatchaLightText,
    onSurface = MatchaLightText,
    surfaceVariant = MatchaLightPrimary.copy(alpha = 0.12f),
    onSurfaceVariant = MatchaLightTextSecondary
)

private val MatchaDarkColorScheme = darkColorScheme(
    primary = MatchaDarkPrimary,
    secondary = MatchaDarkAccent,
    tertiary = MatchaDarkTertiary,
    background = MatchaDarkBg,
    surface = MatchaDarkSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = MatchaDarkText,
    onSurface = MatchaDarkText,
    surfaceVariant = MatchaDarkPrimary.copy(alpha = 0.25f),
    onSurfaceVariant = MatchaDarkTextSecondary
)

// 梦幻紫主题
private val DreamyPurpleLightColorScheme = lightColorScheme(
    primary = DreamyPurpleLightPrimary,
    secondary = DreamyPurpleLightAccent,
    tertiary = DreamyPurpleLightTertiary,
    background = DreamyPurpleLightBg,
    surface = DreamyPurpleLightSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = DreamyPurpleLightText,
    onSurface = DreamyPurpleLightText,
    surfaceVariant = DreamyPurpleLightPrimary.copy(alpha = 0.12f),
    onSurfaceVariant = DreamyPurpleLightTextSecondary
)

private val DreamyPurpleDarkColorScheme = darkColorScheme(
    primary = DreamyPurpleDarkPrimary,
    secondary = DreamyPurpleDarkAccent,
    tertiary = DreamyPurpleDarkTertiary,
    background = DreamyPurpleDarkBg,
    surface = DreamyPurpleDarkSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = DreamyPurpleDarkText,
    onSurface = DreamyPurpleDarkText,
    surfaceVariant = DreamyPurpleDarkPrimary.copy(alpha = 0.25f),
    onSurfaceVariant = DreamyPurpleDarkTextSecondary
)

// 樱花粉主题
private val SakuraLightColorScheme = lightColorScheme(
    primary = SakuraLightPrimary,
    secondary = SakuraLightAccent,
    tertiary = SakuraLightTertiary,
    background = SakuraLightBg,
    surface = SakuraLightSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = SakuraLightText,
    onSurface = SakuraLightText,
    surfaceVariant = SakuraLightPrimary.copy(alpha = 0.12f),
    onSurfaceVariant = SakuraLightTextSecondary
)

private val SakuraDarkColorScheme = darkColorScheme(
    primary = SakuraDarkPrimary,
    secondary = SakuraDarkAccent,
    tertiary = SakuraDarkTertiary,
    background = SakuraDarkBg,
    surface = SakuraDarkSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = SakuraDarkText,
    onSurface = SakuraDarkText,
    surfaceVariant = SakuraDarkPrimary.copy(alpha = 0.25f),
    onSurfaceVariant = SakuraDarkTextSecondary
)

// 北极蓝主题
private val ArcticLightColorScheme = lightColorScheme(
    primary = ArcticLightPrimary,
    secondary = ArcticLightAccent,
    tertiary = ArcticLightTertiary,
    background = ArcticLightBg,
    surface = ArcticLightSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = ArcticLightText,
    onSurface = ArcticLightText,
    surfaceVariant = ArcticLightPrimary.copy(alpha = 0.12f),
    onSurfaceVariant = ArcticLightTextSecondary
)

private val ArcticDarkColorScheme = darkColorScheme(
    primary = ArcticDarkPrimary,
    secondary = ArcticDarkAccent,
    tertiary = ArcticDarkTertiary,
    background = ArcticDarkBg,
    surface = ArcticDarkSurface,
    onPrimary = ColorWhite,
    onSecondary = ColorWhite,
    onTertiary = ColorWhite,
    onBackground = ArcticDarkText,
    onSurface = ArcticDarkText,
    surfaceVariant = ArcticDarkPrimary.copy(alpha = 0.25f),
    onSurfaceVariant = ArcticDarkTextSecondary
)

@Composable
fun MCCommandHelperTheme(
    theme: AppTheme = AppTheme.FOLLOW_SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    
    val colorScheme = when (theme) {
        AppTheme.FOLLOW_SYSTEM -> {
            when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (systemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }
                systemDark -> DarkColorScheme
                else -> LightColorScheme
            }
        }
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.MIDNIGHT -> MidnightColorScheme
        AppTheme.AMOLED -> AmoledColorScheme
        AppTheme.GREEN -> if (systemDark) GreenDarkColorScheme else GreenLightColorScheme
        AppTheme.OCEAN -> if (systemDark) OceanDarkColorScheme else OceanLightColorScheme
        AppTheme.WARM -> if (systemDark) WarmDarkColorScheme else WarmLightColorScheme
        AppTheme.MATCHA -> if (systemDark) MatchaDarkColorScheme else MatchaLightColorScheme
        AppTheme.DREAMY_PURPLE -> if (systemDark) DreamyPurpleDarkColorScheme else DreamyPurpleLightColorScheme
        AppTheme.SAKURA -> if (systemDark) SakuraDarkColorScheme else SakuraLightColorScheme
        AppTheme.ARCTIC -> if (systemDark) ArcticDarkColorScheme else ArcticLightColorScheme
    }

    val isDark = when (theme) {
        AppTheme.FOLLOW_SYSTEM -> systemDark
        AppTheme.LIGHT -> false
        AppTheme.GREEN -> systemDark
        AppTheme.OCEAN -> systemDark
        AppTheme.WARM -> systemDark
        AppTheme.MATCHA -> systemDark
        AppTheme.DREAMY_PURPLE -> systemDark
        AppTheme.SAKURA -> systemDark
        AppTheme.ARCTIC -> systemDark
        else -> true
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            window.navigationBarColor = colorScheme.surface.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
