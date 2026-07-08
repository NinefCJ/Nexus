/**
 * It is part of Nexus. Nexus is a command helper for Minecraft Bedrock Edition.
 * Copyright (C) 2026  Yancey
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.nexuscmd.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.rememberPlatformOverscrollFactory
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

enum class AccentColor {
    INDIGO,
    BLUE,
    PURPLE,
    GREEN,
    ORANGE,
    PINK,
    TEAL,
    RED
}

private fun getAccentColor(accent: AccentColor, isDark: Boolean): Pair<Color, Color> {
    return when (accent) {
        AccentColor.INDIGO -> if (isDark) Color(0xFF818CF8) to Color(0x40818CF8) else Color(0xFF6366F1) to Color(0x406366F1)
        AccentColor.BLUE -> if (isDark) Color(0xFF60A5FA) to Color(0x4060A5FA) else Color(0xFF3B82F6) to Color(0x403B82F6)
        AccentColor.PURPLE -> if (isDark) Color(0xFFA78BFA) to Color(0x40A78BFA) else Color(0xFF8B5CF6) to Color(0x408B5CF6)
        AccentColor.GREEN -> if (isDark) Color(0xFF4ADE80) to Color(0x404ADE80) else Color(0xFF22C55E) to Color(0x4022C55E)
        AccentColor.ORANGE -> if (isDark) Color(0xFFFB923C) to Color(0x40FB923C) else Color(0xFFF97316) to Color(0x40F97316)
        AccentColor.PINK -> if (isDark) Color(0xFFF472B6) to Color(0x40F472B6) else Color(0xFFEC4899) to Color(0x40EC4899)
        AccentColor.TEAL -> if (isDark) Color(0xFF2DD4BF) to Color(0x402DD4BF) else Color(0xFF14B8A6) to Color(0x4014B8A6)
        AccentColor.RED -> if (isDark) Color(0xFFF87171) to Color(0x40F87171) else Color(0xFFEF4444) to Color(0x40EF4444)
    }
}

private fun getLightColorPalette(accent: AccentColor): NexusColors {
    val (mainColor, mainColorSecondary) = getAccentColor(accent, false)
    return NexusColors(
        mainColor = mainColor,
        mainColorSecondary = mainColorSecondary,
        background = Color(0xFFF8FAFC),
        backgroundComponent = Color(0xFFFFFFFF),
        backgroundComponentNoTranslate = Color(0xFFFFFFFF),
        textMain = Color(0xFF1E293B),
        textBond = Color(0xFF0F172A),
        textSecondary = Color(0xFF64748B),
        textHint = Color(0xFF94A3B8),
        textErrorReason = Color(0xFFEF4444),
        underlineErrorReason = Color(0xFFEF4444),
        line = Color(0xFFE2E8F0),
        iconMain = Color(0xFF64748B),
        scrollBar = Color(0x8094A3B8),
        overscrollGlowColor = Color(0xFFFFFFFF),
    )
}

private fun getDarkColorPalette(accent: AccentColor): NexusColors {
    val (mainColor, mainColorSecondary) = getAccentColor(accent, true)
    return NexusColors(
        mainColor = mainColor,
        mainColorSecondary = mainColorSecondary,
        background = Color(0xFF0B0D10),
        backgroundComponent = Color(0xFF16181D),
        backgroundComponentNoTranslate = Color(0xFF1A1D22),
        textMain = Color(0xFFE2E8F0),
        textBond = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        textHint = Color(0xFF64748B),
        textErrorReason = Color(0xFFF87171),
        underlineErrorReason = Color(0xFFF87171),
        line = Color(0xFF262A31),
        iconMain = Color(0xFF94A3B8),
        scrollBar = Color(0x8064748B),
        overscrollGlowColor = Color(0xFF000000),
    )
}

private val LocalTheme = compositionLocalOf {
    NexusTheme.Theme.Light
}

private val LocalNexusColors = compositionLocalOf {
    getLightColorPalette(AccentColor.INDIGO)
}

private val LocalBackground = compositionLocalOf<ImageBitmap?> {
    null
}

private val LocalFontSizeScale = compositionLocalOf {
    1.0f
}

private val LocalIsEnableAnimation = compositionLocalOf {
    true
}

private val LocalIsEnableBlurBackground = compositionLocalOf {
    false
}

private val LocalIsEnableRoundedCorners = compositionLocalOf {
    true
}

object NexusTheme {
    val theme: Theme
        @Composable
        get() = LocalTheme.current
    val colors: NexusColors
        @Composable
        get() = LocalNexusColors.current
    val backgroundBitmap: ImageBitmap?
        @Composable
        get() = LocalBackground.current
    val fontSizeScale: Float
        @Composable
        get() = LocalFontSizeScale.current
    val isEnableAnimation: Boolean
        @Composable
        get() = LocalIsEnableAnimation.current
    val isEnableBlurBackground: Boolean
        @Composable
        get() = LocalIsEnableBlurBackground.current
    val isEnableRoundedCorners: Boolean
        @Composable
        get() = LocalIsEnableRoundedCorners.current

    enum class Theme {
        Light, Dark
    }
}

class NexusColors(
    mainColor: Color,
    mainColorSecondary: Color,
    background: Color,
    backgroundComponent: Color,
    backgroundComponentNoTranslate: Color,
    textMain: Color,
    textBond: Color,
    textSecondary: Color,
    textHint: Color,
    textErrorReason: Color,
    underlineErrorReason: Color,
    line: Color,
    iconMain: Color,
    scrollBar: Color,
    overscrollGlowColor: Color,
) {
    var mainColor: Color by mutableStateOf(mainColor)
        private set
    var mainColorSecondary: Color by mutableStateOf(mainColorSecondary)
        private set
    var background: Color by mutableStateOf(background)
        private set
    var backgroundComponent: Color by mutableStateOf(backgroundComponent)
        private set
    var backgroundComponentNoTranslate: Color by mutableStateOf(backgroundComponentNoTranslate)
        private set
    var textMain: Color by mutableStateOf(textMain)
        private set
    var textBond: Color by mutableStateOf(textBond)
        private set
    var textSecondary: Color by mutableStateOf(textSecondary)
        private set
    var textHint: Color by mutableStateOf(textHint)
        private set
    var textErrorReason: Color by mutableStateOf(textErrorReason)
        private set
    var underlineErrorReason: Color by mutableStateOf(underlineErrorReason)
        private set
    var line: Color by mutableStateOf(line)
        private set
    var iconMain: Color by mutableStateOf(iconMain)
        private set
    var scrollBar: Color by mutableStateOf(scrollBar)
        private set
    var overscrollGlowColor: Color by mutableStateOf(overscrollGlowColor)
        private set
}

private object NoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource) =
        NoIndicationInstance()

    override fun equals(other: Any?) = other === this

    override fun hashCode() = -1

    class NoIndicationInstance : Modifier.Node()
}

@Composable
fun NexusTheme(
    theme: NexusTheme.Theme,
    accentColor: AccentColor = AccentColor.INDIGO,
    backgroundBitmap: ImageBitmap?,
    screenAlphaOverride: Float = 1.0f,
    fontSizeScale: Float = 1.0f,
    isEnableAnimation: Boolean = true,
    isEnableBlurBackground: Boolean = false,
    isEnableRoundedCorners: Boolean = true,
    content: @Composable () -> Unit
) {
    val targetColor = when (theme) {
        NexusTheme.Theme.Light -> getLightColorPalette(accentColor)
        NexusTheme.Theme.Dark -> getDarkColorPalette(accentColor)
    }

    val animationSpec = if (isEnableAnimation) TweenSpec<Color>(600) else TweenSpec<Color>(0)
    val mainColor = animateColorAsState(targetColor.mainColor, animationSpec)
    val mainColorSecondary =
        animateColorAsState(targetColor.mainColorSecondary, animationSpec)
    val background = animateColorAsState(
        targetColor.background.copy(alpha = targetColor.background.alpha * screenAlphaOverride),
        animationSpec
    )
    val backgroundComponent = animateColorAsState(
        targetColor.backgroundComponent.copy(alpha = targetColor.backgroundComponent.alpha * screenAlphaOverride),
        animationSpec
    )
    val backgroundComponentNoTranslate =
        animateColorAsState(
            targetColor.backgroundComponentNoTranslate.copy(alpha = targetColor.backgroundComponentNoTranslate.alpha * screenAlphaOverride),
            animationSpec
        )
    val textMain = animateColorAsState(targetColor.textMain, animationSpec)
    val textBond = animateColorAsState(targetColor.textBond, animationSpec)
    val textSecondary = animateColorAsState(targetColor.textSecondary, animationSpec)
    val textHint = animateColorAsState(targetColor.textHint, animationSpec)
    val textErrorReason = animateColorAsState(targetColor.textErrorReason, animationSpec)
    val underlineErrorReason = animateColorAsState(targetColor.underlineErrorReason, animationSpec)
    val line = animateColorAsState(targetColor.line, animationSpec)
    val iconMain = animateColorAsState(targetColor.iconMain, animationSpec)
    val scrollBar = animateColorAsState(targetColor.scrollBar, animationSpec)
    val overscrollGlowColor = animateColorAsState(targetColor.overscrollGlowColor, animationSpec)

    val colors = NexusColors(
        mainColor = mainColor.value,
        mainColorSecondary = mainColorSecondary.value,
        background = background.value,
        backgroundComponent = backgroundComponent.value,
        backgroundComponentNoTranslate = backgroundComponentNoTranslate.value,
        textMain = textMain.value,
        textBond = textBond.value,
        textSecondary = textSecondary.value,
        textHint = textHint.value,
        textErrorReason = textErrorReason.value,
        underlineErrorReason = underlineErrorReason.value,
        line = line.value,
        iconMain = iconMain.value,
        scrollBar = scrollBar.value,
        overscrollGlowColor = overscrollGlowColor.value,
    )
    val textSelectionColors = TextSelectionColors(
        handleColor = mainColor.value,
        backgroundColor = mainColorSecondary.value
    )
    val overscrollFactory = rememberPlatformOverscrollFactory(overscrollGlowColor.value)
    CompositionLocalProvider(
        LocalTheme provides theme,
        LocalNexusColors provides colors,
        LocalBackground provides backgroundBitmap,
        LocalFontSizeScale provides fontSizeScale,
        LocalIsEnableAnimation provides isEnableAnimation,
        LocalIsEnableBlurBackground provides isEnableBlurBackground,
        LocalIsEnableRoundedCorners provides isEnableRoundedCorners,
        LocalTextSelectionColors provides textSelectionColors,
        LocalIndication provides NoIndication,
        LocalOverscrollFactory provides overscrollFactory,
    ) {
        content()
    }
}