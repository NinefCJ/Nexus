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

package com.nexuscmd.ui.settings

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.nexuscmd.R
import com.nexuscmd.data.SettingsDataStore
import com.nexuscmd.ui.common.AccentColor
import com.nexuscmd.ui.common.NexusTheme
import com.nexuscmd.ui.common.dialog.ChoosingDialog
import com.nexuscmd.ui.common.dialog.InputStringDialog
import com.nexuscmd.ui.common.dialog.IsConfirmDialog
import com.nexuscmd.ui.common.layout.Collection
import com.nexuscmd.ui.common.layout.CollectionName
import com.nexuscmd.ui.common.layout.NameAndAction
import com.nexuscmd.ui.common.layout.RootViewWithHeaderAndCopyright
import com.nexuscmd.ui.common.layout.SettingsItem
import com.nexuscmd.ui.common.widget.Divider

@Composable
fun SettingsScreen(
    chooseBackground: () -> Unit,
    restoreBackground: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingsDataStore = remember(context) { SettingsDataStore(context) }
    var isShowResumeBackgroundDialog by remember { mutableStateOf(false) }
    var isShowChooseThemeDialog by remember { mutableStateOf(false) }
    var isShowChooseAccentColorDialog by remember { mutableStateOf(false) }
    var isShowInputFontSizeScaleDialog by remember { mutableStateOf(false) }
    var isShowInputFloatingWindowIconAlphaDialog by remember { mutableStateOf(false) }
    var isShowInputFloatingWindowScreenAlphaDialog by remember { mutableStateOf(false) }
    var isShowInputFloatingWindowIconSizeDialog by remember { mutableStateOf(false) }
    var isShowChooseCpackBranchDialog by remember { mutableStateOf(false) }
    var isShowChooseTagClickDialog by remember { mutableStateOf(false) }
    var isShowChooseAmbiguousLineDialog by remember { mutableStateOf(false) }
    var isShowChooseLibraryHomeRecommendDialog by remember { mutableStateOf(false) }
    var isShowInputSyntaxHighlightMaxLengthDialog by remember { mutableStateOf(false) }
    val isEnableUpdateNotifications by settingsDataStore.isEnableUpdateNotifications()
        .collectAsState(initial = null)
    val accentColor by settingsDataStore.accentColor()
        .collectAsState(initial = "indigo")
    val fontSizeScale by settingsDataStore.fontSizeScale()
        .collectAsState(initial = 1.0f)
    val isEnableAnimation by settingsDataStore.isEnableAnimation()
        .collectAsState(initial = true)
    val isEnableBlurBackground by settingsDataStore.isEnableBlurBackground()
        .collectAsState(initial = false)
    val isEnableRoundedCorners by settingsDataStore.isEnableRoundedCorners()
        .collectAsState(initial = true)
    val isEnableSoundEffects by settingsDataStore.isEnableSoundEffects()
        .collectAsState(initial = false)
    val cpackBranch by settingsDataStore.cpackBranch()
        .collectAsState(initial = null)
    val isCheckingBySelection by settingsDataStore.isCheckingBySelection()
        .collectAsState(initial = null)
    val isHideWindowWhenCopying by settingsDataStore.isHideWindowWhenCopying()
        .collectAsState(initial = null)
    val isSavingWhenPausing by settingsDataStore.isSavingWhenPausing()
        .collectAsState(initial = null)
    val isCrowded by settingsDataStore.isCrowded()
        .collectAsState(initial = null)
    val isShowErrorReason by settingsDataStore.isShowErrorReason()
        .collectAsState(initial = null)
    val isSyntaxHighlight by settingsDataStore.isSyntaxHighlight()
        .collectAsState(initial = null)
    val floatingWindowIconAlpha by settingsDataStore.floatingWindowIconAlpha()
        .collectAsState(initial = null)
    val floatingWindowScreenAlpha by settingsDataStore.floatingWindowScreenAlpha()
        .collectAsState(initial = null)
    val floatingWindowIconSize by settingsDataStore.floatingWindowIconSize()
        .collectAsState(initial = null)
    val isFloatingWindowFontAlphaSync by settingsDataStore.isFloatingWindowFontAlphaSync()
        .collectAsState(initial = null)
    var tagClickBehavior by remember { mutableStateOf("search") }
    var ambiguousLineDefault by remember { mutableStateOf("comment") }
    val isHideMetadataPreview by settingsDataStore.isHideMetadataPreview()
        .collectAsState(initial = false)
    val isEnableMcdHighlight by settingsDataStore.isEnableMcdHighlight()
        .collectAsState(initial = true)
    val isPublicLibraryHomeRecommend by settingsDataStore.isPublicLibraryHomeRecommend()
        .collectAsState(initial = true)
    val syntaxHighlightMaxLength by settingsDataStore.syntaxHighlightMaxLength()
        .collectAsState(initial = null)
    val tagClickBehaviorFlow by settingsDataStore.tagClickBehavior()
        .collectAsState(initial = "search")
    val ambiguousLineDefaultFlow by settingsDataStore.ambiguousLineDefault()
        .collectAsState(initial = "comment")
    SideEffect {
        if (tagClickBehavior != tagClickBehaviorFlow) {
            tagClickBehavior = tagClickBehaviorFlow
        }
        if (ambiguousLineDefault != ambiguousLineDefaultFlow) {
            ambiguousLineDefault = ambiguousLineDefaultFlow
        }
    }
    var cpackBranchesWithTranslate by remember {
        mutableStateOf(
            arrayOf(
                "release-vanilla" to "正式版-原版",
                "release-experiment" to "正式版-实验性玩法",
                "beta-vanilla" to "测试版-原版",
                "beta-experiment" to "测试版-实验性玩法",
                "netease-vanilla" to "中国版-原版",
                "netease-experiment" to "中国版-实验性玩法",
            )
        )
    }
    LaunchedEffect(context) {
        val filenames = withContext(Dispatchers.IO) { context.assets.list("cpack")!! }
        val cpackBranches =
            arrayOf(
                "release-vanilla",
                "release-experiment",
                "beta-vanilla",
                "beta-experiment",
                "netease-vanilla",
                "netease-experiment"
            )
        val cpackBranchTranslations =
            arrayOf(
                "正式版-原版-",
                "正式版-实验性玩法-",
                "测试版-原版-",
                "测试版-实验性玩法-",
                "中国版-原版-",
                "中国版-实验性玩法-"
            )
        val newCPackBranchesWithTranslate = mutableListOf<Pair<String, String>>()
        for (filename in filenames) {
            for (i in 0..<cpackBranches.size) {
                if (filename!!.startsWith(cpackBranches[i])) {
                    val version = filename.substring(
                        cpackBranches[i].length,
                        filename.length - ".cpack".length
                    )
                    newCPackBranchesWithTranslate.add("${cpackBranchTranslations[i]}${version}" to cpackBranches[i])
                }
            }
        }
        cpackBranchesWithTranslate = newCPackBranchesWithTranslate.toTypedArray()
    }
    val accentColorName = remember(accentColor) {
        when (accentColor) {
            "indigo" -> "靛蓝色"
            "blue" -> "蓝色"
            "purple" -> "紫色"
            "green" -> "绿色"
            "orange" -> "橙色"
            "pink" -> "粉色"
            "teal" -> "青色"
            "red" -> "红色"
            else -> "靛蓝色"
        }
    }
    RootViewWithHeaderAndCopyright(stringResource(R.string.layout_settings_title)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            CollectionName(stringResource(R.string.layout_settings_application_update))
            Collection {
                SettingsItem(
                    name = stringResource(R.string.layout_settings_is_enable_update_notification),
                    description = stringResource(R.string.layout_settings_is_enable_update_notification_description),
                    checked = isEnableUpdateNotifications,
                    leadingIcon = R.drawable.check_circle,
                    onCheckedChange = {
                        coroutineScope.launch {
                            settingsDataStore.setIsEnableUpdateNotifications(it)
                        }
                    },
                )
            }
            CollectionName("外观设置")
            Collection {
                NameAndAction(
                    name = "主题配色",
                    description = "当前: $accentColorName",
                    leadingIcon = R.drawable.pencil,
                ) {
                    isShowChooseAccentColorDialog = true
                }
                Divider()
                NameAndAction(
                    name = stringResource(R.string.layout_settings_choose_theme),
                    description = stringResource(R.string.layout_settings_choose_theme_description),
                    leadingIcon = R.drawable.eye,
                ) {
                    isShowChooseThemeDialog = true
                }
                Divider()
                NameAndAction(
                    name = "字体大小",
                    description = "当前: ${(fontSizeScale * 100).toInt()}%",
                    leadingIcon = R.drawable.plus,
                ) {
                    isShowInputFontSizeScaleDialog = true
                }
                Divider()
                SettingsItem(
                    name = "启用动画效果",
                    description = "开启后切换主题、页面时有平滑过渡动画",
                    checked = isEnableAnimation,
                    leadingIcon = R.drawable.pencil,
                    onCheckedChange = {
                        coroutineScope.launch {
                            settingsDataStore.setIsEnableAnimation(it)
                        }
                    },
                )
                Divider()
                SettingsItem(
                    name = "启用毛玻璃背景",
                    description = "为组件背景添加模糊效果（可能影响性能）",
                    checked = isEnableBlurBackground,
                    leadingIcon = R.drawable.eye,
                    onCheckedChange = {
                        coroutineScope.launch {
                            settingsDataStore.setIsEnableBlurBackground(it)
                        }
                    },
                )
                Divider()
                SettingsItem(
                    name = "启用圆角设计",
                    description = "为卡片、按钮等组件添加圆角效果",
                    checked = isEnableRoundedCorners,
                    leadingIcon = R.drawable.pencil,
                    onCheckedChange = {
                        coroutineScope.launch {
                            settingsDataStore.setIsEnableRoundedCorners(it)
                        }
                    },
                )
                Divider()
                SettingsItem(
                    name = "启用音效",
                    description = "操作时播放提示音效",
                    checked = isEnableSoundEffects,
                    leadingIcon = R.drawable.pencil,
                    onCheckedChange = {
                        coroutineScope.launch {
                            settingsDataStore.setIsEnableSoundEffects(it)
                        }
                    },
                )
            }
            CollectionName("自定义背景")
            Collection {
                NameAndAction(
                    name = stringResource(R.string.layout_settings_choose_background),
                    description = stringResource(R.string.layout_settings_choose_background_description),
                    leadingIcon = R.drawable.eye,
                ) {
                    chooseBackground()
                }
                Divider()
                NameAndAction(
                    name = stringResource(R.string.layout_settings_restore_background),
                    description = stringResource(R.string.layout_settings_restore_background_description),
                    leadingIcon = R.drawable.refresh,
                ) {
                    isShowResumeBackgroundDialog = true
                }
            }
            CollectionName("悬浮窗设置")
            Collection {
                NameAndAction(
                    name = stringResource(R.string.layout_settings_floating_window_icon_alpha),
                    description = stringResource(R.string.layout_settings_floating_window_icon_alpha_description),
                    leadingIcon = R.drawable.eye_off,
                ) {
                    isShowInputFloatingWindowIconAlphaDialog = true
                }
                Divider()
                NameAndAction(
                    name = stringResource(R.string.layout_settings_floating_window_screen_alpha),
                    description = stringResource(R.string.layout_settings_floating_window_screen_alpha_description),
                    leadingIcon = R.drawable.eye_off,
                ) {
                    isShowInputFloatingWindowScreenAlphaDialog = true
                }
                Divider()
                SettingsItem(
                    name = "悬浮窗字体是否跟随透明",
                    description = "开启后字体随窗口一同透明；关闭后仅背景透明，字体保持清晰",
                    checked = isFloatingWindowFontAlphaSync,
                    leadingIcon = R.drawable.eye,
                    onCheckedChange = {
                        coroutineScope.launch {
                            settingsDataStore.setIsFloatingWindowFontAlphaSync(it)
                        }
                    },
                )
                Divider()
                NameAndAction(
                    name = stringResource(R.string.layout_settings_floating_window_icon_size),
                    description = stringResource(R.string.layout_settings_floating_window_icon_size_description),
                    leadingIcon = R.drawable.plus,
                ) {
                    isShowInputFloatingWindowIconSizeDialog = true
                }
            }
            CollectionName(stringResource(R.string.layout_settings_completion_settings))
            Collection {
                val currentCpackBranchTranslation =
                    remember(cpackBranch, cpackBranchesWithTranslate) {
                        for (pair in cpackBranchesWithTranslate) {
                            if (cpackBranch == pair.second) {
                                return@remember pair.first
                            }
                        }
                        return@remember cpackBranch
                    }
                NameAndAction(
                    name = stringResource(R.string.layout_settings_choose_cpack),
                    description = stringResource(
                        R.string.layout_settings_current_cpack,
                        currentCpackBranchTranslation
                            ?: stringResource(R.string.layout_settings_unknown_branch)
                    ),
                    leadingIcon = R.drawable.box,
                ) {
                    isShowChooseCpackBranchDialog = true
                }
                Divider()
                SettingsItem(
                    name = stringResource(R.string.layout_setting_checking_by_selection),
                    description = stringResource(R.string.layout_setting_checking_by_selection_description),
                    checked = isCheckingBySelection,
                    leadingIcon = R.drawable.check_circle,
                    onCheckedChange = {
                        coroutineScope.launch {
                            settingsDataStore.setIsCheckingBySelection(it)
                        }
                    },
                )
                Divider()
                SettingsItem(
                    name = stringResource(R.string.layout_setting_is_hide_window_when_copying),
                    description = stringResource(R.string.layout_setting_is_hide_window_when_copying_description),
                    checked = isHideWindowWhenCopying,
                    leadingIcon = R.drawable.copy,
                    onCheckedChange = {
                        coroutineScope.launch {
                            settingsDataStore.setIsHideWindowWhenCopying(it)
                        }
                    },
                )
                Divider()
                SettingsItem(
                    name = stringResource(R.string.layout_setting_is_saving_when_pausing),
                    description = stringResource(R.string.layout_setting_is_saving_when_pausing_description),
                    checked = isSavingWhenPausing,
                    leadingIcon = R.drawable.folder,
                    onCheckedChange = {
                        coroutineScope.launch {
                            settingsDataStore.setIsSavingWhenPausing(it)
                        }
                    },
                )
                Divider()
                SettingsItem(
                    name = stringResource(R.string.layout_setting_is_crowed),
                    description = stringResource(R.string.layout_setting_is_crowed_description),
                    checked = isCrowded,
                    leadingIcon = R.drawable.more,
                    onCheckedChange = {
                        coroutineScope.launch {
                            settingsDataStore.setIsCrowded(it)
                        }
                    },
                )
                Divider()
                SettingsItem(
                    name = stringResource(R.string.layout_setting_is_show_error_reason),
                    description = stringResource(R.string.layout_setting_is_show_error_reason_description),
                    checked = isShowErrorReason,
                    leadingIcon = R.drawable.alert_triangle,
                    onCheckedChange = {
                        coroutineScope.launch {
                            settingsDataStore.setIsShowErrorReason(it)
                        }
                    },
                )
                Divider()
                SettingsItem(
                    name = stringResource(R.string.layout_setting_is_syntax_highlight),
                    description = stringResource(R.string.layout_setting_is_syntax_highlight_description),
                    checked = isSyntaxHighlight,
                    leadingIcon = R.drawable.pencil,
                    onCheckedChange = {
                        coroutineScope.launch {
                            settingsDataStore.setIsSyntaxHighlight(it)
                        }
                    },
                )
                Divider()
                NameAndAction(
                    name = "高亮自动关闭阈值",
                    description = "当前限制: ${syntaxHighlightMaxLength ?: 4000} 字符 (为防卡死)",
                    leadingIcon = R.drawable.alert_triangle,
                ) {
                    isShowInputSyntaxHighlightMaxLengthDialog = true
                }
            }
            CollectionName("命令库设置")
            Collection {
                NameAndAction(
                    name = "命令库默认主页流",
                    description = "当前: ${if (isPublicLibraryHomeRecommend) "猜你喜欢" else "按时间最新发布"}",
                    leadingIcon = R.drawable.book,
                ) {
                    isShowChooseLibraryHomeRecommendDialog = true
                }
                Divider()
                NameAndAction(
                    name = "Tag 点击行为",
                    description = "当前: ${if (tagClickBehavior == "search") "搜索该 Tag" else "进入详情页"}",
                    leadingIcon = R.drawable.book,
                ) {
                    isShowChooseTagClickDialog = true
                }
                Divider()
                NameAndAction(
                    name = "无法推断行的默认处理",
                    description = "当前: ${if (ambiguousLineDefault == "comment") "当作注释" else "当作指令"}",
                    leadingIcon = R.drawable.book,
                ) {
                    isShowChooseAmbiguousLineDialog = true
                }
                Divider()
                SettingsItem(
                    name = "隐藏正文元数据预览",
                    description = "隐藏 MCD 可视化中 @name、@version 等元信息区",
                    checked = isHideMetadataPreview,
                    leadingIcon = R.drawable.eye_off,
                    onCheckedChange = {
                        coroutineScope.launch {
                            settingsDataStore.setIsHideMetadataPreview(it)
                        }
                    },
                )
                Divider()
                SettingsItem(
                    name = "[实验性] 命令块语法高亮",
                    description = "支持命令库在 v2 渲染时为命令块附加语法高亮",
                    checked = isEnableMcdHighlight,
                    leadingIcon = R.drawable.pencil,
                    onCheckedChange = {
                        coroutineScope.launch {
                            settingsDataStore.setIsEnableMcdHighlight(it)
                        }
                    },
                )
            }
        }
    }
    if (isShowChooseThemeDialog) {
        val data = remember {
            arrayOf(
                "浅色模式" to "MODE_NIGHT_NO",
                "深色模式" to "MODE_NIGHT_YES",
                "跟随系统" to "MODE_NIGHT_FOLLOW_SYSTEM",
            )
        }
        ChoosingDialog(
            onDismissRequest = { isShowChooseThemeDialog = false },
            data = data,
            onChoose = {
                coroutineScope.launch {
                    settingsDataStore.setThemeId(it)
                }
            })
    }
    if (isShowChooseAccentColorDialog) {
        val data = remember {
            arrayOf(
                "靛蓝色" to "indigo",
                "蓝色" to "blue",
                "紫色" to "purple",
                "绿色" to "green",
                "橙色" to "orange",
                "粉色" to "pink",
                "青色" to "teal",
                "红色" to "red",
            )
        }
        ChoosingDialog(
            onDismissRequest = { isShowChooseAccentColorDialog = false },
            data = data,
            onChoose = {
                coroutineScope.launch {
                    settingsDataStore.setAccentColor(it)
                }
            })
    }
    if (isShowResumeBackgroundDialog) {
        IsConfirmDialog(
            onDismissRequest = { isShowResumeBackgroundDialog = false },
            content = "是否恢复背景？",
            onConfirm = {
                restoreBackground()
            }
        )
    }
    if (isShowInputFontSizeScaleDialog) {
        val textFieldState = rememberTextFieldState(
            initialText = (fontSizeScale * 100).toInt().toString()
        )
        InputStringDialog(
            onDismissRequest = { isShowInputFontSizeScaleDialog = false },
            title = "请输入字体大小百分比 (50-200)",
            textFieldState = textFieldState,
            onConfirm = {
                try {
                    var integer = textFieldState.text.toString().toInt()
                    if (integer < 50) {
                        integer = 50
                    } else if (integer > 200) {
                        integer = 200
                    }
                    coroutineScope.launch {
                        settingsDataStore.setFontSizeScale(integer / 100f)
                    }
                } catch (_: NumberFormatException) {
                }
            }
        )
    }
    if (isShowInputFloatingWindowIconSizeDialog && floatingWindowIconSize != null) {
        val textFieldState = rememberTextFieldState(
            initialText = floatingWindowIconSize!!.toString()
        )
        InputStringDialog(
            onDismissRequest = { isShowInputFloatingWindowIconSizeDialog = false },
            title = "请输入悬浮窗图标大小",
            textFieldState = textFieldState,
            onConfirm = {
                try {
                    var integer = textFieldState.text.toString().toInt()
                    if (integer < 10) {
                        integer = 10
                    } else if (integer > 100) {
                        integer = 100
                    }
                    coroutineScope.launch {
                        settingsDataStore.setFloatingWindowIconSize(integer)
                    }
                } catch (_: NumberFormatException) {
                }
            }
        )
    }
    if (isShowInputFloatingWindowIconAlphaDialog && floatingWindowIconAlpha != null) {
        val textFieldState = rememberTextFieldState(
            initialText = (floatingWindowIconAlpha!! * 100).toInt().toString()
        )
        InputStringDialog(
            onDismissRequest = { isShowInputFloatingWindowIconAlphaDialog = false },
            title = "请输入图标透明度",
            textFieldState = textFieldState,
            onConfirm = {
                try {
                    var integer = textFieldState.text.toString().toInt()
                    if (integer < 10) {
                        integer = 10
                    } else if (integer > 100) {
                        integer = 100
                    }
                    coroutineScope.launch {
                        settingsDataStore.setFloatingWindowIconAlpha(integer / 100f)
                    }
                } catch (_: NumberFormatException) {
                }
            }
        )
    }
    if (isShowInputFloatingWindowScreenAlphaDialog && floatingWindowScreenAlpha != null) {
        val textFieldState = rememberTextFieldState(
            initialText = (floatingWindowScreenAlpha!! * 100).toInt().toString()
        )
        InputStringDialog(
            onDismissRequest = { isShowInputFloatingWindowScreenAlphaDialog = false },
            title = "请输入透明度",
            textFieldState = textFieldState,
            onConfirm = {
                try {
                    var integer = textFieldState.text.toString().toInt()
                    if (integer < 10) {
                        integer = 10
                    } else if (integer > 100) {
                        integer = 100
                    }
                    coroutineScope.launch {
                        settingsDataStore.setFloatingWindowScreenAlpha(integer / 100f)
                    }
                } catch (_: NumberFormatException) {
                }
            }
        )
    }
    if (isShowChooseCpackBranchDialog) {
        ChoosingDialog(
            onDismissRequest = { isShowChooseCpackBranchDialog = false },
            data = cpackBranchesWithTranslate,
            onChoose = {
                coroutineScope.launch {
                    settingsDataStore.setCpackBranch(it)
                }
            })
    }
    if (isShowChooseTagClickDialog) {
        ChoosingDialog(
            onDismissRequest = { isShowChooseTagClickDialog = false },
            data = arrayOf(
                "搜索该 Tag" to "search",
                "进入详情页" to "detail"
            ),
            onChoose = {
                tagClickBehavior = it
                coroutineScope.launch {
                    settingsDataStore.setTagClickBehavior(it)
                }
            })
    }
    if (isShowChooseAmbiguousLineDialog) {
        ChoosingDialog(
            onDismissRequest = { isShowChooseAmbiguousLineDialog = false },
            data = arrayOf(
                "当作注释" to "comment",
                "当作指令" to "command"
            ),
            onChoose = {
                ambiguousLineDefault = it
                coroutineScope.launch {
                    settingsDataStore.setAmbiguousLineDefault(it)
                }
            })
    }
    if (isShowChooseLibraryHomeRecommendDialog) {
        ChoosingDialog(
            onDismissRequest = { isShowChooseLibraryHomeRecommendDialog = false },
            data = arrayOf(
                "猜你喜欢" to "true",
                "按时间最新发布" to "false"
            ),
            onChoose = {
                Log.d("CPL_Tab", "Settings: user chose isRecommend=$it")
                coroutineScope.launch {
                    settingsDataStore.setPublicLibraryHomeRecommend(it == "true")
                    Log.d(
                        "CPL_Tab",
                        "Settings: saved to DataStore isRecommend=${it == "true"}"
                    )
                }
            })
    }
    if (isShowInputSyntaxHighlightMaxLengthDialog && syntaxHighlightMaxLength != null) {
        val textFieldState = rememberTextFieldState(
            initialText = syntaxHighlightMaxLength!!.toString()
        )
        InputStringDialog(
            onDismissRequest = { isShowInputSyntaxHighlightMaxLengthDialog = false },
            title = "请输入高亮自动关闭阈值 (0-100000)",
            textFieldState = textFieldState,
            onConfirm = {
                try {
                    var integer = textFieldState.text.toString().toInt()
                    if (integer < 0) {
                        integer = 0
                    } else if (integer > 100000) {
                        integer = 100000
                    }
                    coroutineScope.launch {
                        settingsDataStore.setSyntaxHighlightMaxLength(integer)
                    }
                } catch (_: NumberFormatException) {
                }
            }
        )
    }
}

@Preview
@Composable
fun SettingsScreenLightThemePreview() {
    NexusTheme(
        theme = NexusTheme.Theme.Light,
        accentColor = AccentColor.INDIGO,
        backgroundBitmap = null
    ) {
        SettingsScreen(
            chooseBackground = {},
            restoreBackground = {},
        )
    }
}

@Preview
@Composable
fun SettingsScreenDarkThemePreview() {
    NexusTheme(
        theme = NexusTheme.Theme.Dark,
        accentColor = AccentColor.INDIGO,
        backgroundBitmap = null
    ) {
        SettingsScreen(
            chooseBackground = {},
            restoreBackground = {},
        )
    }
}