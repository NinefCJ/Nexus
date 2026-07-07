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

package com.nexuscmd.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.toast.Toaster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.nexuscmd.R
import com.nexuscmd.android.util.PolicyGrantManager
import com.nexuscmd.android.window.FloatingWindowManager
import com.nexuscmd.data.SettingsDataStore
import com.nexuscmd.ui.AboutScreenKey
import com.nexuscmd.ui.CompletionScreenKey
import com.nexuscmd.ui.EnumerationScreenKey
import com.nexuscmd.ui.LibraryMainScreenKey
import com.nexuscmd.ui.Old2NewIMEGuideScreenKey
import com.nexuscmd.ui.Old2NewScreenKey
import com.nexuscmd.ui.RawtextScreenKey
import com.nexuscmd.ui.SettingsScreenKey
import com.nexuscmd.ui.ShowTextScreenKey
import com.nexuscmd.ui.common.NexusTheme
import com.nexuscmd.ui.common.dialog.IsConfirmDialog
import com.nexuscmd.ui.common.dialog.PolicyGrantDialog
import com.nexuscmd.ui.common.layout.Copyright
import com.nexuscmd.ui.common.layout.RootView
import com.nexuscmd.ui.common.widget.Text

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    navController: NavHostController = rememberNavController(),
    floatingWindowManager: FloatingWindowManager? = null
) {
    val context = LocalContext.current
    val settingsDataStore = remember(context) { SettingsDataStore(context) }
    val isShowPublicLibrary by settingsDataStore.isShowPublicLibrary()
        .collectAsState(initial = false)
    val floatingWindowIconSize by settingsDataStore.floatingWindowIconSize()
        .collectAsState(initial = 40)
    val floatingWindowIconAlpha by settingsDataStore.floatingWindowIconAlpha()
        .collectAsState(initial = 1.0f)
    val floatingWindowScreenAlpha by settingsDataStore.floatingWindowScreenAlpha()
        .collectAsState(initial = 1.0f)
    val isFloatingWindowFontAlphaSync by settingsDataStore.isFloatingWindowFontAlphaSync()
        .collectAsState(initial = true)
    val publicLibraryMinVersion by settingsDataStore.publicLibraryMinVersion()
        .collectAsState(initial = 0)

    RootView {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NexusTheme.colors.mainColor)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.pack_icon),
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier.size(width = 48.dp, height = 48.dp)
                        )
                        Column(modifier = Modifier.padding(start = 14.dp)) {
                            Text(
                                text = stringResource(R.string.layout_home_app_name),
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                            )
                            Text(
                                text = stringResource(R.string.layout_home_app_description),
                                style = TextStyle(
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Feature Grid
                HomeFeatureGrid(
                    navController = navController,
                    viewModel = viewModel,
                    floatingWindowManager = floatingWindowManager,
                    floatingWindowIconSize = floatingWindowIconSize,
                    floatingWindowIconAlpha = floatingWindowIconAlpha,
                    floatingWindowScreenAlpha = floatingWindowScreenAlpha,
                    isFloatingWindowFontAlphaSync = isFloatingWindowFontAlphaSync,
                    isShowPublicLibrary = isShowPublicLibrary,
                    publicLibraryMinVersion = publicLibraryMinVersion
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
            Copyright(Modifier.align(Alignment.CenterHorizontally))
        }
    }

    // Dialogs remain unchanged
    if (viewModel.isShowPermissionRequestWindow) {
        IsConfirmDialog(
            onDismissRequest = {
                viewModel.isShowPermissionRequestWindow = false
            },
            content = "需要悬浮窗权限，请进入设置进行授权",
            confirmText = "打开设置",
            onConfirm = {
                XXPermissions.with(context)
                    .permission(PermissionLists.getSystemAlertWindowPermission())
                    .request { _, deniedList ->
                        if (deniedList.isEmpty()) {
                            Toaster.show("悬浮窗权限获取成功")
                        } else {
                            Toaster.show("悬浮窗权限获取失败")
                        }
                    }
            },
            onCancel = {
                viewModel.isShowPermissionRequestWindow = false
            }
        )
    }
    if (viewModel.isShowXiaomiClipboardPermissionTips) {
        IsConfirmDialog(
            onDismissRequest = {
                viewModel.isShowXiaomiClipboardPermissionTips = false
            },
            content = "对于小米手机和红米手机，需要将写入剪切板权限设置为始终允许才能在悬浮窗复制文本。具体设置方式如下：设置-应用设置-权限管理-应用权限管理-Nexus-写入剪切板-始终允许。",
            cancelText = "不再提示",
            onCancel = {
                viewModel.dismissShowXiaomiClipboardPermissionTipsForever()
                viewModel.startFloatingWindow(
                    context,
                    true,
                    floatingWindowIconSize,
                    floatingWindowIconAlpha,
                    floatingWindowScreenAlpha,
                    isFloatingWindowFontAlphaSync,
                    floatingWindowManager,
                )
            },
            onConfirm = {
                viewModel.startFloatingWindow(
                    context,
                    true,
                    floatingWindowIconSize,
                    floatingWindowIconAlpha,
                    floatingWindowScreenAlpha,
                    isFloatingWindowFontAlphaSync,
                    floatingWindowManager,
                )
            }
        )
    }
    if (viewModel.isShowPolicyGrantDialog) {
        val policyPageTitle = stringResource(R.string.layout_about_privacy_policy)
        PolicyGrantDialog(
            content = if (viewModel.policyGrantState == PolicyGrantManager.State.NOT_READ)
                stringResource(R.string.dialog_policy_grant_message_if_unread) else
                stringResource(R.string.dialog_policy_grant_message_if_updated),
            readPolicy = {
                viewModel.viewModelScope.launch {
                    val content = withContext(Dispatchers.IO) {
                        context.assets.open("about/privacy_policy.txt").bufferedReader()
                            .use { it.readText() }
                    }
                    navController.navigate(ShowTextScreenKey(policyPageTitle, content))
                }
            },
            onConfirm = {
                viewModel.agreePolicy()
            },
        )
    }
    if (viewModel.isShowAnnouncementDialog) {
        IsConfirmDialog(
            onDismissRequest = {
                viewModel.isShowAnnouncementDialog = false
                viewModel.checkUpdate()
            },
            title = viewModel.announcement!!.title!!,
            content = viewModel.announcement!!.message!!,
            cancelText = if (viewModel.announcement!!.isForce!!) "取消" else "不再提醒",
            onCancel = {
                if (!viewModel.announcement!!.isForce!!) {
                    viewModel.ignoreCurrentAnnouncement()
                }
            }
        )
    }
    if (viewModel.isShowCommandLabVersionDialog) {
        IsConfirmDialog(
            onDismissRequest = { viewModel.dismissCommandLabVersionDialog() },
            title = "版本提示",
            content = "公有命令库需要最新版软件才能使用，请下载最新版软件体验完整功能。"
        )
    }
    if (viewModel.isShowUpdateNotificationsDialog) {
        val content = remember(viewModel.latestVersionInfo) {
            viewModel.latestVersionInfo!!.versionName + "版本已发布，欢迎下载体验。本次更新内容如下：\n" + viewModel.latestVersionInfo!!.changelog
        }
        IsConfirmDialog(
            onDismissRequest = { viewModel.isShowUpdateNotificationsDialog = false },
            title = "更新提醒",
            content = content,
            cancelText = "忽略此版本",
            onCancel = { viewModel.ignoreLatestVersion() }
        )
    }
}

@Composable
private fun HomeFeatureGrid(
    navController: NavHostController,
    viewModel: HomeViewModel,
    floatingWindowManager: FloatingWindowManager?,
    floatingWindowIconSize: Int,
    floatingWindowIconAlpha: Float,
    floatingWindowScreenAlpha: Float,
    isFloatingWindowFontAlphaSync: Boolean,
    isShowPublicLibrary: Boolean,
    publicLibraryMinVersion: Int
) {
    val context = LocalContext.current

    // Section: Core
    HomeSectionTitle(title = stringResource(R.string.layout_home_command_completion))
    HomeTwoColumnGrid(
        items = listOf(
            HomeFeatureItem(
                icon = R.drawable.box,
                title = stringResource(R.string.layout_home_command_completion_app_mode),
                subtitle = "应用内编辑",
                onClick = {
                    if (viewModel.isUsingFloatingWindow(floatingWindowManager)) {
                        Toaster.show("你必须关闭悬浮窗模式才可以进入应用模式")
                    } else {
                        navController.navigate(CompletionScreenKey)
                    }
                }
            ),
            HomeFeatureItem(
                icon = R.drawable.pencil,
                title = stringResource(R.string.layout_home_command_completion_floating_window_mode),
                subtitle = "悬浮窗编辑",
                onClick = {
                    if (viewModel.isUsingFloatingWindow(floatingWindowManager)) {
                        viewModel.stopFloatingWindow(floatingWindowManager)
                    } else {
                        viewModel.startFloatingWindow(
                            context, false,
                            floatingWindowIconSize, floatingWindowIconAlpha,
                            floatingWindowScreenAlpha, isFloatingWindowFontAlphaSync,
                            floatingWindowManager,
                        )
                    }
                }
            ),
            HomeFeatureItem(
                icon = R.drawable.help_circle,
                title = stringResource(R.string.layout_home_command_completion_settings),
                subtitle = "偏好设置",
                onClick = { navController.navigate(SettingsScreenKey) }
            )
        )
    )

    // Section: Tools
    HomeSectionTitle(title = stringResource(R.string.layout_home_old2new))
    HomeTwoColumnGrid(
        items = listOf(
            HomeFeatureItem(
                icon = R.drawable.arrow_right,
                title = stringResource(R.string.layout_home_old2new_app_mode),
                subtitle = "命令转换",
                onClick = { navController.navigate(Old2NewScreenKey) }
            ),
            HomeFeatureItem(
                icon = R.drawable.arrow_forward_up,
                title = stringResource(R.string.layout_home_old2new_ime_mode),
                subtitle = "输入法转换",
                onClick = { navController.navigate(Old2NewIMEGuideScreenKey) }
            )
        )
    )

    HomeSectionTitle(title = stringResource(R.string.layout_home_enumeration))
    HomeTwoColumnGrid(
        items = listOf(
            HomeFeatureItem(
                icon = R.drawable.refresh,
                title = stringResource(R.string.layout_home_enumeration_app_mode),
                subtitle = "参数穷举",
                onClick = { navController.navigate(EnumerationScreenKey) }
            )
        )
    )

    // Section: Experimental
    HomeSectionTitle(title = stringResource(R.string.layout_home_experimental_feature))
    HomeTwoColumnGrid(
        items = listOf(
            HomeFeatureItem(
                icon = R.drawable.book,
                title = "命令库",
                subtitle = "浏览与收藏",
                onClick = {
                    if (isShowPublicLibrary) {
                        viewModel.checkCommandLabVersion(publicLibraryMinVersion) {
                            navController.navigate(LibraryMainScreenKey)
                        }
                    } else {
                        navController.navigate(LibraryMainScreenKey)
                    }
                }
            ),
            HomeFeatureItem(
                icon = R.drawable.ic_loong_flow_bubble,
                title = "游龙导出",
                subtitle = "可视化导出",
                onClick = {
                    if (!XXPermissions.isGrantedPermission(
                            context,
                            PermissionLists.getSystemAlertWindowPermission()
                        )
                    ) {
                        viewModel.isShowPermissionRequestWindow = true
                    } else {
                        floatingWindowManager?.loongFlowManager?.showExport(context)
                    }
                }
            ),
            HomeFeatureItem(
                icon = R.drawable.file_arrow_left,
                title = "Raw JSON",
                subtitle = "原始文本生成",
                onClick = { navController.navigate(RawtextScreenKey) }
            )
        )
    )

    // Section: About
    HomeSectionTitle(title = stringResource(R.string.layout_home_about))
    HomeTwoColumnGrid(
        items = listOf(
            HomeFeatureItem(
                icon = R.drawable.heart,
                title = stringResource(R.string.layout_home_about_app_mode),
                subtitle = "关于本应用",
                onClick = { navController.navigate(AboutScreenKey) }
            )
        )
    )
}

@Composable
private fun HomeSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        style = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = NexusTheme.colors.textSecondary
        )
    )
}

@Composable
private fun HomeTwoColumnGrid(items: List<HomeFeatureItem>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = NexusTheme.colors.backgroundComponent,
        border = BorderStroke(1.dp, NexusTheme.colors.line),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        HomeFeatureCard(
                            icon = item.icon,
                            title = item.title,
                            subtitle = item.subtitle,
                            onClick = item.onClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

data class HomeFeatureItem(
    @DrawableRes val icon: Int,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@Composable
private fun HomeFeatureCard(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NexusTheme.colors.backgroundComponentNoTranslate)
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NexusTheme.colors.mainColorSecondary),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(NexusTheme.colors.mainColor)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1
        )
        Text(
            text = subtitle,
            style = TextStyle(
                fontSize = 12.sp,
                color = NexusTheme.colors.textSecondary
            ),
            maxLines = 1
        )
    }
}

@Preview
@Composable
fun HomeScreenLightThemePreview() {
    NexusTheme(theme = NexusTheme.Theme.Light, backgroundBitmap = null) {
        HomeScreen()
    }
}

@Preview
@Composable
fun HomeScreenDarkThemePreview() {
    NexusTheme(theme = NexusTheme.Theme.Dark, backgroundBitmap = null) {
        HomeScreen()
    }
}
