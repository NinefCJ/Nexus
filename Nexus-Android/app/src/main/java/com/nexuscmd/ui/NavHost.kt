/**
 * It is part of Nexus. Nexus is a command helper for Minecraft Bedrock Edition.
 * Copyright (C) 2026  Yancey
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.nexuscmd.ui

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import com.nexuscmd.android.window.FloatingWindowManager
import com.nexuscmd.core.NexusCore
import com.nexuscmd.ui.about.AboutScreen
import com.nexuscmd.ui.common.dialog.IsConfirmDialog
import com.nexuscmd.ui.completion.CompletionScreen
import com.nexuscmd.ui.completion.HistoryScreen
import com.nexuscmd.ui.enumeration.EnumerationScreen
import com.nexuscmd.ui.home.HomeScreen
import com.nexuscmd.ui.library.CPLUploadScreen
import com.nexuscmd.ui.library.CPLUserScreen
import com.nexuscmd.ui.library.LibraryMainScreen
import com.nexuscmd.ui.library.LocalLibraryEditScreen
import com.nexuscmd.ui.library.LocalLibraryListScreen
import com.nexuscmd.ui.library.LocalLibraryShowScreen
import com.nexuscmd.ui.library.MessageScreen
import com.nexuscmd.ui.library.PublicLibraryListScreen
import com.nexuscmd.ui.library.PublicLibraryShowScreen
import com.nexuscmd.ui.library.profile.UserProfileScreen
import com.nexuscmd.ui.library.score.LeaderboardScreen
import com.nexuscmd.ui.library.search.LibrarySearchScreen
import com.nexuscmd.ui.old2new.Old2NewIMEGuideScreen
import com.nexuscmd.ui.old2new.Old2NewScreen
import com.nexuscmd.ui.rawtext.RawtextScreen
import com.nexuscmd.ui.settings.SettingsScreen
import com.nexuscmd.ui.showtext.ShowTextScreen

@Serializable
object HomeScreenKey

@Serializable
object CompletionScreenKey

@Serializable
object HistoryScreenKey

@Serializable
object SettingsScreenKey

@Serializable
object Old2NewScreenKey

@Serializable
object Old2NewIMEGuideScreenKey

@Serializable
object EnumerationScreenKey

@Serializable
object LocalLibraryListScreenKey

@Serializable
data class LocalLibraryShowScreenKey(
    val id: Int
)

@Serializable
data class LibraryEditScreenKey(
    val id: Int?
)

@Serializable
object RawtextScreenKey

@Serializable
object AboutScreenKey

@Serializable
object PublicLibraryListScreenKey

@Serializable
object LibraryMainScreenKey

@Serializable
data class PublicLibraryShowScreenKey(
    val id: Int,
    val isPrivate: Boolean = false
)

@Serializable
data class ShowTextScreenKey(
    val title: String,
    val content: String
)

@Serializable
data class LibrarySearchScreenKey(
    val initialKeyword: String? = null
)


@Serializable
object CPLUserScreenKey

@Serializable
data class CPLUploadScreenKey(
    val editLibraryId: Int = -1,
    val editLibraryJson: String? = null
)

@Serializable
object LeaderboardScreenKey

@Serializable
data class UserProfileScreenKey(
    val id: Int
)

@Serializable
object MessageScreenKey

@Composable
fun NavHost(
    navController: NavHostController,
    floatingWindowManager: FloatingWindowManager,
    chooseBackground: () -> Unit,
    restoreBackground: () -> Unit,
    isShowSavingBackgroundDialog: MutableState<Boolean> = mutableStateOf(false),
    shutdown: () -> Unit
) {
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    DisposableEffect(navController, focusManager, softwareKeyboardController) {
        val listener = NavController.OnDestinationChangedListener { _, _, _ ->
            focusManager.clearFocus()
            softwareKeyboardController?.hide()
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
    NavHost(
        navController = navController,
        startDestination = HomeScreenKey,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
    ) {
        composable<HomeScreenKey> {
            HomeScreen(navController = navController, floatingWindowManager = floatingWindowManager)
        }
        composable<CompletionScreenKey> {
            CompletionScreen(
                viewModel = viewModel(),
                navController = navController,
                shutdown = shutdown,
                hideView = {}
            )
        }
        composable<HistoryScreenKey> {
            HistoryScreen()
        }
        composable<SettingsScreenKey> {
            SettingsScreen(
                chooseBackground = chooseBackground,
                restoreBackground = restoreBackground,
            )
        }
        composable<Old2NewScreenKey> {
            val context = LocalContext.current
            Old2NewScreen(
                old2new = { old -> NexusCore.old2new(context, old) }
            )
        }
        composable<Old2NewIMEGuideScreenKey> {
            Old2NewIMEGuideScreen()
        }
        composable<EnumerationScreenKey> {
            EnumerationScreen()
        }
        composable<LocalLibraryListScreenKey> {
            LocalLibraryListScreen(navController = navController)
        }
        composable<LocalLibraryShowScreenKey> { navBackStackEntry ->
            val localLibraryShow: LocalLibraryShowScreenKey = navBackStackEntry.toRoute()
            LocalLibraryShowScreen(id = localLibraryShow.id)
        }
        composable<LibraryEditScreenKey> { navBackStackEntry ->
            val localLibraryEdit: LibraryEditScreenKey = navBackStackEntry.toRoute()
            LocalLibraryEditScreen(id = localLibraryEdit.id)
        }
        composable<RawtextScreenKey> {
            RawtextScreen()
        }
        composable<AboutScreenKey> {
            AboutScreen(navController)
        }
        composable<ShowTextScreenKey> { navBackStackEntry ->
            val showText: ShowTextScreenKey = navBackStackEntry.toRoute()
            ShowTextScreen(
                title = showText.title,
                content = showText.content
            )
        }
        composable<PublicLibraryListScreenKey> {
            PublicLibraryListScreen(navController = navController)
        }
        composable<LibraryMainScreenKey> {
            LibraryMainScreen(
                navController = navController,
                isFloatingWindow = false
            )
        }
        composable<PublicLibraryShowScreenKey> { navBackStackEntry ->
            val publicLibraryShow: PublicLibraryShowScreenKey = navBackStackEntry.toRoute()
            PublicLibraryShowScreen(
                id = publicLibraryShow.id,
                isPrivate = publicLibraryShow.isPrivate,
                navController = navController
            )
        }
        composable<LibrarySearchScreenKey> { navBackStackEntry ->
            val args: LibrarySearchScreenKey = navBackStackEntry.toRoute()
            LibrarySearchScreen(navController = navController, initialKeyword = args.initialKeyword)
        }
        composable<CPLUserScreenKey> {
            CPLUserScreen(navController = navController)
        }
        composable<CPLUploadScreenKey> { backStackEntry ->
            val customKey = backStackEntry.toRoute<CPLUploadScreenKey>()
            CPLUploadScreen(
                navController = navController,
                editLibraryId = customKey.editLibraryId,
                editLibraryJson = customKey.editLibraryJson
            )
        }
        composable<LeaderboardScreenKey> {
            LeaderboardScreen(navController)
        }
        composable<UserProfileScreenKey> { backStackEntry ->
            val customKey = backStackEntry.toRoute<UserProfileScreenKey>()
            UserProfileScreen(customKey.id, navController)
        }
        composable<MessageScreenKey> {
            MessageScreen()
        }
    }
    if (isShowSavingBackgroundDialog.value) {
        IsConfirmDialog(
            onDismissRequest = { isShowSavingBackgroundDialog.value = false },
            content = "背景图片正在保存中，请稍候",
        )
    }
}

@Composable
fun FloatingWindowNavHost(
    navController: NavHostController,
    shutdown: () -> Unit,
    hideView: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = CompletionScreenKey,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
    ) {
        composable<HomeScreenKey> {
            HomeScreen(navController = navController)
        }
        composable<CompletionScreenKey> {
            CompletionScreen(
                viewModel = viewModel(),
                navController = navController,
                shutdown = shutdown,
                hideView = hideView
            )
        }
        composable<HistoryScreenKey> {
            HistoryScreen()
        }
        composable<Old2NewScreenKey> {
            val context = LocalContext.current
            Old2NewScreen(
                old2new = { old -> NexusCore.old2new(context, old) }
            )
        }
        composable<Old2NewIMEGuideScreenKey> {
            Old2NewIMEGuideScreen()
        }
        composable<EnumerationScreenKey> {
            EnumerationScreen()
        }
        composable<LocalLibraryListScreenKey> {
            LocalLibraryListScreen(navController = navController)
        }
        composable<LocalLibraryShowScreenKey> { navBackStackEntry ->
            val localLibraryShow: LocalLibraryShowScreenKey = navBackStackEntry.toRoute()
            LocalLibraryShowScreen(id = localLibraryShow.id)
        }
        composable<LibraryEditScreenKey> { navBackStackEntry ->
            val localLibraryEdit: LibraryEditScreenKey = navBackStackEntry.toRoute()
            LocalLibraryEditScreen(id = localLibraryEdit.id)
        }
        composable<RawtextScreenKey> {
            RawtextScreen()
        }
        composable<AboutScreenKey> {
            AboutScreen(navController)
        }
        composable<ShowTextScreenKey> { navBackStackEntry ->
            val showText: ShowTextScreenKey = navBackStackEntry.toRoute()
            ShowTextScreen(
                title = showText.title,
                content = showText.content
            )
        }
        composable<PublicLibraryListScreenKey> {
            PublicLibraryListScreen(navController = navController, isFloatingWindow = true)
        }
        composable<LibraryMainScreenKey> {
            LibraryMainScreen(
                navController = navController,
                isFloatingWindow = true
            )
        }
        composable<PublicLibraryShowScreenKey> { navBackStackEntry ->
            val customKey = navBackStackEntry.toRoute<PublicLibraryShowScreenKey>()
            PublicLibraryShowScreen(customKey.id, customKey.isPrivate, navController)
        }
        composable<LibrarySearchScreenKey> { navBackStackEntry ->
            val args: LibrarySearchScreenKey = navBackStackEntry.toRoute()
            LibrarySearchScreen(navController = navController, initialKeyword = args.initialKeyword)
        }
        composable<CPLUserScreenKey> {
            CPLUserScreen(navController = navController)
        }
        composable<CPLUploadScreenKey> { backStackEntry ->
            val customKey = backStackEntry.toRoute<CPLUploadScreenKey>()
            CPLUploadScreen(
                navController = navController,
                editLibraryId = customKey.editLibraryId,
                editLibraryJson = customKey.editLibraryJson
            )
        }
        composable<LeaderboardScreenKey> {
            LeaderboardScreen(navController)
        }
        composable<UserProfileScreenKey> { backStackEntry ->
            val customKey = backStackEntry.toRoute<UserProfileScreenKey>()
            UserProfileScreen(customKey.id, navController)
        }
        composable<MessageScreenKey> {
            MessageScreen()
        }
    }
}
