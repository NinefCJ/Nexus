package com.nexuscmd

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexuscmd.data.HistoryItem
import com.nexuscmd.data.SavedCommand
import com.nexuscmd.data.SoundEffect
import com.nexuscmd.data.SoundEffectLibrary
import com.nexuscmd.data.ParticleLibrary
import com.nexuscmd.data.Particle
import com.nexuscmd.data.BlockLibrary
import com.nexuscmd.data.Block
import com.nexuscmd.data.ItemLibrary
import com.nexuscmd.data.Item
import com.nexuscmd.data.AddonPack
import com.nexuscmd.data.AddonManager
import com.nexuscmd.ui.components.SyntaxHighlightEditor
import com.nexuscmd.ui.theme.MCCommandHelperTheme
import com.nexuscmd.ui.theme.SyntaxCommand
import com.nexuscmd.ui.components.SyntaxColorLegend
import com.nexuscmd.ui.components.SyntaxHighlightedText
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CommandHelper.Registry.getInstance().initialize("")

        setContent {
            val currentTheme by viewModel.uiState.collectAsState()
            MCCommandHelperTheme(theme = currentTheme.currentTheme) {
                MainScreen(
                    viewModel = viewModel,
                    onRequestFloatingPermission = { requestFloatingWindowPermission() },
                    onStartFloating = { startFloatingWindow() }
                )
            }
        }
    }

    private fun requestFloatingWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_FLOATING_CODE)
        }
    }

    private fun startFloatingWindow() {
        val intent = Intent(this, FloatingWindowService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    companion object {
        private const val REQUEST_FLOATING_CODE = 1001
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onRequestFloatingPermission: () -> Unit,
    onStartFloating: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddonPage by remember { mutableStateOf(false) }
    var showCommandChainPage by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Modern background with custom image support
    Box(modifier = Modifier.fillMaxSize()) {
        // Background layer
        BackgroundLayer(
            useCustomBackground = uiState.useCustomBackground,
            customBackgroundUri = uiState.customBackgroundUri,
            backgroundOpacity = uiState.backgroundOpacity
        )

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent,
            topBar = {
                ModernTopAppBar(
                    onToggleTheme = { viewModel.setDarkTheme(!uiState.isDarkTheme) },
                    isDark = uiState.isDarkTheme,
                    onRequestFloatingPermission = onRequestFloatingPermission,
                    onStartFloating = onStartFloating,
                    cardOpacity = uiState.cardOpacity,
                    useGlassmorphism = uiState.useGlassmorphism,
                    glassIntensity = uiState.glassmorphismIntensity,
                    cardCornerRadius = uiState.cardCornerRadius
                )
            },
            bottomBar = {
                ModernBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    cardOpacity = uiState.cardOpacity,
                    useGlassmorphism = uiState.useGlassmorphism,
                    glassIntensity = uiState.glassmorphismIntensity,
                    cardCornerRadius = uiState.cardCornerRadius
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (selectedTab) {
                    0 -> EditorTab(viewModel, uiState)
                    1 -> CommandLibraryTab(viewModel, uiState)
                    2 -> QuickCommandsTab(viewModel)
                    3 -> ResourceLibraryTab(viewModel)
                    4 -> HistoryTab(viewModel, uiState)
                    5 -> SettingsTab(
                        viewModel = viewModel,
                        onRequestPermission = onRequestFloatingPermission,
                        onStartFloating = onStartFloating,
                        onOpenAddons = { showAddonPage = true },
                        onOpenCommandChains = { showCommandChainPage = true }
                    )
                }
            }
        }
    }

    if (showAddonPage) {
        AddonManagerPage(
            onClose = { showAddonPage = false },
            viewModel = viewModel
        )
    }

    if (showCommandChainPage) {
        CommandChainManagerPage(
            onClose = { showCommandChainPage = false },
            viewModel = viewModel
        )
    }
}

@Composable
fun EditorTab(viewModel: MainViewModel, uiState: MainUiState) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var expandedSections by remember { mutableStateOf(setOf("quickInsert", "quickCommands", "favorites")) }

    fun toggleSection(section: String) {
        expandedSections = if (expandedSections.contains(section)) {
            expandedSections - section
        } else {
            expandedSections + section
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CommandInputCard(
            commandText = uiState.commandText,
            onCommandChange = { viewModel.onCommandTextChanged(it) },
            validation = uiState.validation,
            completions = uiState.completions,
            onCompletionClick = { viewModel.applyCompletion(it) },
            isFavorite = uiState.isCurrentCommandFavorite,
            onFavoriteClick = { viewModel.toggleCurrentFavorite() },
            onCopyClick = {
                if (uiState.commandText.isNotEmpty()) {
                    clipboardManager.setText(AnnotatedString(uiState.commandText))
                    viewModel.addToHistory(uiState.commandText)
                }
            },
            onShareClick = {
                if (uiState.commandText.isNotEmpty()) {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, uiState.commandText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "分享命令"))
                }
            },
            syntaxHint = uiState.syntaxHint
        )

        if (uiState.currentCommandInfo != null) {
            CommandInfoCard(info = uiState.currentCommandInfo)
        }

        ExpandableSection(
            title = "快速插入",
            icon = Icons.Default.AutoFixHigh,
            isExpanded = expandedSections.contains("quickInsert"),
            onToggle = { toggleSection("quickInsert") }
        ) {
            QuickInsertToolbar(
                onInsert = { text ->
                    val current = viewModel.uiState.value.commandText
                    viewModel.onCommandTextChanged(current + text)
                }
            )
        }

        ExpandableSection(
            title = "快速命令",
            icon = Icons.Default.Lightbulb,
            isExpanded = expandedSections.contains("quickCommands"),
            onToggle = { toggleSection("quickCommands") }
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(uiState.quickCommands) { (cmd, desc, icon) ->
                    QuickCommandItem(
                        command = cmd,
                        description = desc,
                        icon = icon,
                        onClick = { viewModel.onCommandTextChanged(cmd) },
                        onLongClick = {
                            clipboardManager.setText(AnnotatedString(cmd))
                        }
                    )
                }
            }
        }

        if (uiState.favoriteCommands.isNotEmpty()) {
            ExpandableSection(
                title = "我的收藏",
                icon = Icons.Default.Favorite,
                isExpanded = expandedSections.contains("favorites"),
                onToggle = { toggleSection("favorites") }
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(uiState.favoriteCommands.take(5)) { fav ->
                        FavoriteCommandItem(
                            command = fav,
                            onClick = { viewModel.onCommandTextChanged(fav.command) },
                            onDelete = { viewModel.removeFromFavorites(fav.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun QuickCommandsTab(viewModel: MainViewModel) {
    val quickCommands = viewModel.getQuickCommands()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    
    val categories = listOf("全部", "游戏规则", "效果", "传送", "模式", "物品", "其他")
    
    val filteredCommands = remember(quickCommands, searchQuery, selectedCategory) {
        val filtered = if (searchQuery.isEmpty()) {
            quickCommands
        } else {
            quickCommands.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.syntax.contains(searchQuery, ignoreCase = true)
            }
        }
        if (selectedCategory == null || selectedCategory == "全部") {
            filtered
        } else {
            filtered.filter { it.category == selectedCategory }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("搜索速查命令...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
            singleLine = true
        )
        
        // Category chips
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories.size) { index ->
                FilterChip(
                    selected = selectedCategory == categories[index] || (index == 0 && selectedCategory == null),
                    onClick = { 
                        selectedCategory = if (index == 0) null else categories[index]
                    },
                    label = { Text(categories[index]) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Commands list
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredCommands.size) { index ->
                val cmd = filteredCommands[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.onCommandTextChanged(cmd.syntax)
                    }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = cmd.icon,
                                contentDescription = cmd.name,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cmd.name.removePrefix("速查-"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = cmd.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cmd.syntax,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceLibraryTab(viewModel: MainViewModel) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("方块", "物品", "音效", "粒子")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedSubTab) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = { Text(title) }
                )
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            when (selectedSubTab) {
                0 -> BlockLibraryTab(viewModel)
                1 -> ItemLibraryTab(viewModel)
                2 -> SoundEffectLibraryTab(viewModel)
                3 -> ParticleLibraryTab(viewModel)
            }
        }
    }
}

@Composable
fun QuickInsertToolbar(onInsert: (String) -> Unit) {
    val items = listOf(
        "@s" to "自己",
        "@p" to "最近",
        "@a" to "全部",
        "@e" to "实体",
        "@r" to "随机",
        "~ ~ ~" to "位置",
        " " to "空格"
    )
    
    val secondaryItems = listOf(
        "[" to "[",
        "]" to "]",
        "{" to "{",
        "}" to "}",
        "=" to "=",
        "," to ",",
        "\"" to "\""
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(items) { (text, label) ->
                AssistChip(
                    onClick = { onInsert(text) },
                    label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = {
                        Text(
                            text = text.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(secondaryItems) { (text, label) ->
                AssistChip(
                    onClick = { onInsert(text) },
                    label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.height(32.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandInputCard(
    commandText: String,
    onCommandChange: (String) -> Unit,
    validation: ValidationResult?,
    completions: List<CompletionItem>,
    onCompletionClick: (CompletionItem) -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    syntaxHint: SyntaxHint? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "命令编辑器",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))

                // Favorite button
                IconButton(
                    onClick = onFavoriteClick,
                    enabled = commandText.isNotEmpty()
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Copy button
                IconButton(
                    onClick = onCopyClick,
                    enabled = commandText.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "复制",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Share button
                IconButton(
                    onClick = onShareClick,
                    enabled = commandText.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "分享",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Input area with syntax highlighting
            SyntaxHighlightEditor(
                value = commandText,
                onValueChange = onCommandChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = "/give @p diamond_sword 1",
                singleLine = false,
                maxLines = 4
            )

            // Syntax hint overlay
            syntaxHint?.let { hint ->
                if (hint.template.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    com.nexuscmd.ui.components.SyntaxHintOverlay(
                        syntaxHint = hint,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Validation status
            validation?.let { result ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (result.hasError) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = result.message ?: "语法错误",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (commandText.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "命令语法正确",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Completions
            AnimatedVisibility(visible = completions.isNotEmpty()) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "补全建议",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    completions.take(5).forEach { item ->
                        CompletionChip(
                            item = item,
                            onClick = { onCompletionClick(item) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CompletionChip(item: CompletionItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AutoFixHigh,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
        )
        if (item.detail.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CommandInfoCard(info: CommandInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "/${info.name}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            SyntaxHighlightedText(
                text = info.syntax,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = info.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCommandItem(
    command: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                SyntaxHighlightedText(
                    text = command,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FavoriteCommandItem(
    command: SavedCommand,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = command.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                SyntaxHighlightedText(
                    text = command.command,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CommandLibraryTab(viewModel: MainViewModel, uiState: MainUiState) {
    val filteredCommands = viewModel.getFilteredCommands()
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var displayCount by remember { mutableStateOf(30) }
    val categories = listOf("全部", "物品", "实体", "传送", "方块", "世界", "游戏模式", "记分板", "执行", "玩家互动", "声音粒子", "管理员", "服务器", "基岩版独有", "帮助")

    val displayedCommands = remember(filteredCommands, selectedCategory) {
        val all = if (selectedCategory == null || selectedCategory == "全部") {
            filteredCommands
        } else {
            filteredCommands.filter { it.category == selectedCategory }
        }
        all
    }

    val showCommands = displayedCommands.take(displayCount)
    val hasMore = displayCount < displayedCommands.size

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(selectedCategory, uiState.searchQuery) {
        displayCount = 30
        listState.scrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索命令...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category || (category == "全部" && selectedCategory == null),
                        onClick = {
                            selectedCategory = if (category == "全部") null else category
                        },
                        label = { Text(category, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        item {
            Text(
                text = "共 ${displayedCommands.size} 条命令",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(showCommands, key = { it.name }) { cmd ->
            SimpleCommandItem(
                command = cmd,
                onClick = { viewModel.onCommandTextChanged(cmd.syntax) }
            )
        }

        if (hasMore) {
            item {
                TextButton(
                    onClick = { displayCount += 30 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("加载更多 (${displayedCommands.size - displayCount} 条剩余)")
                }
            }
        }
    }
}

@Composable
fun SimpleCommandItem(
    command: CommandLibraryItem,
    onClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = command.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "/${command.name}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = command.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = { clipboardManager.setText(AnnotatedString(command.syntax)) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "复制",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SoundEffectLibraryTab(viewModel: MainViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var displayCount by remember { mutableStateOf(30) }
    var target by remember { mutableStateOf("@s") }
    var position by remember { mutableStateOf("~ ~ ~") }
    var volume by remember { mutableStateOf("1") }
    var pitch by remember { mutableStateOf("1") }
    var minimumVolume by remember { mutableStateOf("0") }
    var showParams by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }
    val displayedEffects = remember(searchQuery, selectedCategory) {
        SoundEffectLibrary.filter(searchQuery, selectedCategory)
    }

    LaunchedEffect(searchQuery, selectedCategory) {
        displayCount = 30
        listState.scrollToItem(0)
    }

    DisposableEffect(Unit) {
        onDispose { toneGenerator.release() }
    }

    val showEffects = displayedEffects.take(displayCount)
    val hasMore = displayCount < displayedEffects.size

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索音效ID、中文名或说明...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(SoundEffectLibrary.categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category || (category == "全部" && selectedCategory == null),
                        onClick = {
                            selectedCategory = if (category == "全部") null else category
                        },
                        label = { Text(category, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "共 ${displayedEffects.size} 个音效",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { showParams = !showParams }) {
                    Text(
                        if (showParams) "隐藏参数" else "参数设置",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        if (showParams) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("@s", "@p", "@a", "@r").forEach { selector ->
                                FilterChip(
                                    selected = target == selector,
                                    onClick = { target = selector },
                                    label = { Text(selector, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = position,
                                onValueChange = { position = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("坐标", style = MaterialTheme.typography.bodySmall) },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = volume,
                                onValueChange = { volume = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("音量", style = MaterialTheme.typography.bodySmall) },
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = pitch,
                                onValueChange = { pitch = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("音高", style = MaterialTheme.typography.bodySmall) },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = minimumVolume,
                                onValueChange = { minimumVolume = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("最小音量", style = MaterialTheme.typography.bodySmall) },
                                singleLine = true
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        items(showEffects, key = { it.id }) { effect ->
            val command = SoundEffectLibrary.buildPlaySoundCommand(
                effect = effect,
                target = target,
                position = position,
                volume = volume.ifBlank { effect.volume },
                pitch = pitch.ifBlank { effect.pitch },
                minimumVolume = minimumVolume.ifBlank { "0" }
            )
            SimpleSoundItem(
                effect = effect,
                onPreview = {
                    toneGenerator.startTone(toneForSoundEffect(effect), 350)
                },
                onUse = {
                    viewModel.onCommandTextChanged(command)
                    viewModel.addToHistory(command)
                },
                onCopy = {
                    clipboardManager.setText(AnnotatedString(command))
                    viewModel.addToHistory(command)
                }
            )
        }

        if (hasMore) {
            item {
                TextButton(
                    onClick = { displayCount += 40 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("加载更多 (${displayedEffects.size - displayCount} 个剩余)")
                }
            }
        }
    }
}

@Composable
fun SimpleSoundItem(
    effect: SoundEffect,
    onPreview: () -> Unit,
    onUse: () -> Unit,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = effect.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = effect.id,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )
        }
        IconButton(onClick = onPreview, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "试听",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "复制",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        IconButton(onClick = onUse, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "使用",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundEffectItem(
    effect: SoundEffect,
    command: String,
    onPreview: () -> Unit,
    onUse: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = effect.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = effect.id,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                AssistChip(
                    onClick = {},
                    label = { Text(effect.category) },
                    enabled = false
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = effect.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            SyntaxHighlightedText(
                text = command,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onPreview,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("试听")
                }
                OutlinedButton(
                    onClick = { onCopy(command) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("复制")
                }
                Button(
                    onClick = { onUse(command) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("使用")
                }
            }
        }
    }
}

fun toneForSoundEffect(effect: SoundEffect): Int {
    return when (effect.category) {
        "实体" -> ToneGenerator.TONE_DTMF_2
        "玩家" -> ToneGenerator.TONE_PROP_ACK
        "方块" -> ToneGenerator.TONE_PROP_BEEP
        "红石" -> ToneGenerator.TONE_DTMF_5
        "天气" -> ToneGenerator.TONE_DTMF_0
        "音乐" -> ToneGenerator.TONE_DTMF_8
        "UI" -> ToneGenerator.TONE_PROP_PROMPT
        else -> ToneGenerator.TONE_PROP_NACK
    }
}

@Composable
fun BlockLibraryTab(viewModel: MainViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var displayCount by remember { mutableStateOf(30) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val displayedBlocks = remember(searchQuery, selectedCategory) {
        BlockLibrary.filter(searchQuery, selectedCategory)
    }

    LaunchedEffect(searchQuery, selectedCategory) {
        displayCount = 30
        listState.scrollToItem(0)
    }

    val showBlocks = displayedBlocks.take(displayCount)
    val hasMore = displayCount < displayedBlocks.size

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索方块ID、中文名...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(BlockLibrary.categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category || (category == "全部" && selectedCategory == null),
                        onClick = {
                            selectedCategory = if (category == "全部") null else category
                        },
                        label = { Text(category, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Text(
                text = "共 ${displayedBlocks.size} 个方块",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        items(showBlocks, key = { it.id }) { block ->
            SimpleLibraryItem(
                title = block.name,
                subtitle = block.id,
                description = block.description,
                icon = Icons.Default.ViewModule,
                iconTint = MaterialTheme.colorScheme.primary,
                copyText = block.id,
                onClick = { viewModel.onCommandTextChanged("/setblock ~ ~ ~ ${block.id}") }
            )
        }

        if (hasMore) {
            item {
                TextButton(
                    onClick = { displayCount += 40 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("加载更多 (${displayedBlocks.size - displayCount} 个剩余)")
                }
            }
        }
    }
}

@Composable
fun ItemLibraryTab(viewModel: MainViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var displayCount by remember { mutableStateOf(30) }
    val clipboardManager = LocalClipboardManager.current
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val displayedItems = remember(searchQuery, selectedCategory) {
        ItemLibrary.filter(searchQuery, selectedCategory)
    }

    LaunchedEffect(searchQuery, selectedCategory) {
        displayCount = 30
        listState.scrollToItem(0)
    }

    val showItems = displayedItems.take(displayCount)
    val hasMore = displayCount < displayedItems.size

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索物品ID、中文名...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ItemLibrary.categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category || (category == "全部" && selectedCategory == null),
                        onClick = {
                            selectedCategory = if (category == "全部") null else category
                        },
                        label = { Text(category, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Text(
                text = "共 ${displayedItems.size} 个物品",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        items(showItems, key = { it.id }) { item ->
            SimpleLibraryItem(
                title = item.name,
                subtitle = item.id,
                description = item.description,
                icon = Icons.Default.Category,
                iconTint = MaterialTheme.colorScheme.secondary,
                copyText = item.id,
                onClick = { viewModel.onCommandTextChanged("/give @s ${item.id} 1") }
            )
        }

        if (hasMore) {
            item {
                TextButton(
                    onClick = { displayCount += 40 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("加载更多 (${displayedItems.size - displayCount} 个剩余)")
                }
            }
        }
    }
}

@Composable
fun SimpleLibraryItem(
    title: String,
    subtitle: String,
    description: String = "",
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    copyText: String,
    onClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(
            onClick = { clipboardManager.setText(AnnotatedString(copyText)) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "复制",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ParticleLibraryTab(viewModel: MainViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var displayCount by remember { mutableStateOf(30) }
    var position by remember { mutableStateOf("~ ~ ~") }
    var delta by remember { mutableStateOf("0 0 0") }
    var speed by remember { mutableStateOf("0") }
    var count by remember { mutableStateOf("1") }
    var mode by remember { mutableStateOf("default") }
    var showParams by remember { mutableStateOf(false) }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val displayedParticles = remember(searchQuery, selectedCategory) {
        ParticleLibrary.filter(searchQuery, selectedCategory)
    }

    LaunchedEffect(searchQuery, selectedCategory) {
        displayCount = 30
        listState.scrollToItem(0)
    }

    val showParticles = displayedParticles.take(displayCount)
    val hasMore = displayCount < displayedParticles.size

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索粒子ID、中文名或说明...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ParticleLibrary.categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category || (category == "全部" && selectedCategory == null),
                        onClick = {
                            selectedCategory = if (category == "全部") null else category
                        },
                        label = { Text(category, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "共 ${displayedParticles.size} 个粒子",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { showParams = !showParams }) {
                    Text(
                        if (showParams) "隐藏参数" else "参数设置",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        if (showParams) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = position,
                                onValueChange = { position = it },
                                label = { Text("位置", style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = count,
                                onValueChange = { count = it },
                                label = { Text("数量", style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.weight(0.5f),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = delta,
                                onValueChange = { delta = it },
                                label = { Text("偏移", style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = speed,
                                onValueChange = { speed = it },
                                label = { Text("速度", style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.weight(0.5f),
                                singleLine = true
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        items(showParticles, key = { it.id }) { particle ->
            val command = ParticleLibrary.buildParticleCommand(particle, position, delta, speed, count, mode)
            SimpleParticleItem(
                particle = particle,
                onCopy = { clipboardManager.setText(AnnotatedString(command)) },
                onInsert = { viewModel.onCommandTextChanged(command) }
            )
        }

        if (hasMore) {
            item {
                TextButton(
                    onClick = { displayCount += 40 },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("加载更多 (${displayedParticles.size - displayCount} 个剩余)")
                }
            }
        }
    }
}

@Composable
fun SimpleParticleItem(
    particle: Particle,
    onCopy: () -> Unit,
    onInsert: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onInsert)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.BubbleChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = particle.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = particle.id,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )
        }
        IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "复制",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        IconButton(onClick = onInsert, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "插入",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ParticleLibraryItem(
    particle: Particle,
    position: String,
    delta: String,
    speed: String,
    count: String,
    mode: String,
    onCopy: () -> Unit,
    onInsert: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val command = ParticleLibrary.buildParticleCommand(particle, position, delta, speed, count, mode)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BubbleChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = particle.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = particle.id,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = particle.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = command,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(command))
                    },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onInsert,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("插入", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun HistoryTab(viewModel: MainViewModel, uiState: MainUiState) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    if (uiState.historyItems.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无历史记录",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "你输入的命令将显示在这里",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with clear button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "历史记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.clearHistory() }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清空")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.historyItems) { item ->
                    HistoryItemCard(
                        item = item,
                        onClick = { viewModel.onCommandTextChanged(item.command) },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(item.command))
                        },
                        onDelete = { viewModel.deleteHistoryItem(item.id) },
                        dateFormat = dateFormat
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    item: HistoryItem,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                SyntaxHighlightedText(
                    text = item.command,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = dateFormat.format(Date(item.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "复制",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsTab(
    viewModel: MainViewModel,
    onRequestPermission: () -> Unit,
    onStartFloating: () -> Unit,
    onOpenAddons: () -> Unit,
    onOpenCommandChains: () -> Unit
) {
    var showClearDataDialog by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableIntStateOf(0) }

    val sections = listOf(
        Pair("主题", Icons.Default.Palette),
        Pair("背景", Icons.Default.Image),
        Pair("效果", Icons.Default.AutoAwesome),
        Pair("悬浮窗", Icons.Default.PictureInPicture),
        Pair("拓展", Icons.Default.Extension),
        Pair("数据", Icons.Default.Storage),
        Pair("关于", Icons.Default.Info)
    )

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("清空所有数据") },
            text = { Text("确定要清空所有收藏、历史记录和设置吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    }
                ) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            },
            divider = { Divider(color = Color.Transparent) }
        ) {
            sections.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedSection == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = title
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section content
        when (selectedSection) {
            0 -> ThemeSection(viewModel)  // 主题选择
            1 -> BackgroundSection(viewModel)  // 背景图片
            2 -> EffectSection(viewModel)  // 毛玻璃等效果
            3 -> FloatingWindowSection(onRequestPermission, onStartFloating)
            4 -> ExtensionSection(viewModel, onOpenAddons, onOpenCommandChains)
            5 -> DataSection(viewModel) { showClearDataDialog = true }
            6 -> AboutSection()
        }
    }
}

/**
 * 主题选择页面
 */
@Composable
fun ThemeSection(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Theme selection
        item {
            SectionTitle(title = "选择主题", icon = Icons.Default.Palette)
        }

        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = uiState.cardCornerRadius,
                glassIntensity = if (uiState.useGlassmorphism) uiState.glassmorphismIntensity else 0f
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "当前主题：${uiState.currentTheme.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "选择你喜欢的主题风格",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 暗色/浅色主题分类
                    Text(
                        text = "🎨 浅色主题",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(com.nexuscmd.data.AppTheme.values().filter { !it.isDark && it != com.nexuscmd.data.AppTheme.FOLLOW_SYSTEM }) { theme ->
                            EnhancedThemePreviewCard(
                                theme = theme,
                                isSelected = uiState.currentTheme == theme,
                                onClick = { viewModel.setTheme(theme) },
                                cornerRadius = uiState.cardCornerRadius
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "🌙 深色主题",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(com.nexuscmd.data.AppTheme.values().filter { it.isDark }) { theme ->
                            EnhancedThemePreviewCard(
                                theme = theme,
                                isSelected = uiState.currentTheme == theme,
                                onClick = { viewModel.setTheme(theme) },
                                cornerRadius = uiState.cardCornerRadius
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "⚙️ 跟随系统",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        EnhancedThemePreviewCard(
                            theme = com.nexuscmd.data.AppTheme.FOLLOW_SYSTEM,
                            isSelected = uiState.currentTheme == com.nexuscmd.data.AppTheme.FOLLOW_SYSTEM,
                            onClick = { viewModel.setTheme(com.nexuscmd.data.AppTheme.FOLLOW_SYSTEM) },
                            cornerRadius = uiState.cardCornerRadius
                        )
                    }
                }
            }
        }
    }
}

/**
 * 背景设置页面
 */
@Composable
fun BackgroundSection(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForImagePicker { uri ->
        uri?.let {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)
            viewModel.setCustomBackground(it.toString())
            viewModel.setUseCustomBackground(true)
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Custom background
        item {
            SectionTitle(title = "自定义背景", icon = Icons.Default.Image)
        }

        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = uiState.cardCornerRadius,
                glassIntensity = if (uiState.useGlassmorphism) uiState.glassmorphismIntensity else 0f
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "使用自定义背景",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "上传图片作为应用背景",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.useCustomBackground && uiState.customBackgroundUri != null,
                            onCheckedChange = { enabled ->
                                if (enabled && uiState.customBackgroundUri == null) {
                                    pickImageLauncher()
                                } else {
                                    viewModel.setUseCustomBackground(enabled)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.useCustomBackground && uiState.customBackgroundUri != null) {
                        Surface(
                            shape = RoundedCornerShape(uiState.cardCornerRadius.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                            LaunchedEffect(uiState.customBackgroundUri) {
                                try {
                                    val uri = android.net.Uri.parse(uiState.customBackgroundUri)
                                    val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                                    pfd?.use {
                                        bitmap = android.graphics.BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
                                    }
                                } catch (e: Exception) {
                                    bitmap = null
                                }
                            }
                            bitmap?.let { bmp ->
                                androidx.compose.foundation.Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { pickImageLauncher() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("更换")
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.setUseCustomBackground(false)
                                    viewModel.setCustomBackground(null)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("移除")
                            }
                        }
                    } else {
                        Button(
                            onClick = { pickImageLauncher() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("选择背景图片")
                        }
                    }
                }
            }
        }

        // Background opacity
        item {
            SectionTitle(title = "背景透明度", icon = Icons.Default.Opacity)
        }

        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = uiState.cardCornerRadius,
                glassIntensity = if (uiState.useGlassmorphism) uiState.glassmorphismIntensity else 0f
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "背景透明度",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(uiState.backgroundOpacity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = uiState.backgroundOpacity,
                        onValueChange = { viewModel.setBackgroundOpacity(it) },
                        valueRange = 0.3f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

/**
 * 效果设置页面 - 毛玻璃、圆角、渐变等
 */
@Composable
fun EffectSection(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Glassmorphism
        item {
            SectionTitle(title = "毛玻璃效果", icon = Icons.Default.BlurCircular)
        }

        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = uiState.cardCornerRadius,
                glassIntensity = if (uiState.useGlassmorphism) uiState.glassmorphismIntensity else 0f
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "启用毛玻璃效果",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "为卡片和导航栏添加毛玻璃效果",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.useGlassmorphism,
                            onCheckedChange = { viewModel.setUseGlassmorphism(it) }
                        )
                    }

                    if (uiState.useGlassmorphism) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "毛玻璃强度",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(uiState.glassmorphismIntensity * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = uiState.glassmorphismIntensity,
                            onValueChange = { viewModel.setGlassmorphismIntensity(it) },
                            valueRange = 0.3f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // Card styling
        item {
            SectionTitle(title = "卡片样式", icon = Icons.Default.Dashboard)
        }

        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = uiState.cardCornerRadius,
                glassIntensity = if (uiState.useGlassmorphism) uiState.glassmorphismIntensity else 0f
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "卡片圆角",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.cardCornerRadius.toInt()} dp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = uiState.cardCornerRadius,
                        onValueChange = { viewModel.setCardCornerRadius(it) },
                        valueRange = 0f..32f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "卡片透明度",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(uiState.cardOpacity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = uiState.cardOpacity,
                        onValueChange = { viewModel.setCardOpacity(it) },
                        valueRange = 0.5f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Gradient accents
        item {
            SectionTitle(title = "装饰效果", icon = Icons.Default.AutoAwesome)
        }

        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = uiState.cardCornerRadius,
                glassIntensity = if (uiState.useGlassmorphism) uiState.glassmorphismIntensity else 0f
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "渐变强调色",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "为按钮和卡片添加渐变色",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.useGradientAccents,
                            onCheckedChange = { viewModel.setUseGradientAccents(it) }
                        )
                    }

                    if (uiState.useGradientAccents) {
                        Spacer(modifier = Modifier.height(16.dp))
                        // 渐变预览
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(uiState.cardCornerRadius.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            com.nexuscmd.ui.theme.GradientStart,
                                            com.nexuscmd.ui.theme.GradientEnd
                                        )
                                    )
                                )
                        ) {
                            Text(
                                text = "渐变预览",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }

        // Dynamic color (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            item {
                SectionTitle(title = "动态颜色", icon = Icons.Default.Palette)
            }

            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = uiState.cardCornerRadius,
                    glassIntensity = if (uiState.useGlassmorphism) uiState.glassmorphismIntensity else 0f
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "使用动态颜色",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "根据壁纸自动调整主题颜色（仅 Android 12+）",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.useDynamicColor,
                                onCheckedChange = { viewModel.setUseDynamicColor(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingWindowSection(
    onRequestPermission: () -> Unit,
    onStartFloating: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(title = "悬浮窗设置", icon = Icons.Default.PictureInPicture)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingRow(
                        icon = Icons.Default.PictureInPicture,
                        title = "悬浮窗权限",
                        description = "开启悬浮窗需要系统权限",
                        onClick = onRequestPermission
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingRow(
                        icon = Icons.Default.OpenInNew,
                        title = "开启悬浮窗",
                        description = "在游戏中也能使用命令助手",
                        onClick = onStartFloating
                    )
                }
            }
        }
    }
}

@Composable
fun ExtensionSection(
    viewModel: MainViewModel,
    onOpenAddons: () -> Unit,
    onOpenCommandChains: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(title = "拓展功能", icon = Icons.Default.Extension)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingRow(
                        icon = Icons.Default.Extension,
                        title = "拓展包管理",
                        description = "安装、管理自定义拓展包",
                        onClick = onOpenAddons
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingToggleRow(
                        icon = Icons.Default.SwapVert,
                        title = "拓展包补全优先",
                        description = "开启后拓展包内容显示在原版之前",
                        checked = uiState.addonCompletionsFirst,
                        onCheckedChange = { viewModel.setAddonCompletionsFirst(it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingRow(
                        icon = Icons.Default.Link,
                        title = "命令方块链",
                        description = "管理和编辑命令方块链",
                        onClick = onOpenCommandChains
                    )
                }
            }
        }
    }
}

@Composable
fun DataSection(
    viewModel: MainViewModel,
    onClearAllData: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(title = "数据管理", icon = Icons.Default.Storage)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingRow(
                        icon = Icons.Default.DeleteSweep,
                        title = "清空所有数据",
                        description = "清除收藏、历史记录和设置",
                        onClick = onClearAllData,
                        isDestructive = true
                    )
                }
            }
        }
    }
}

@Composable
fun AboutSection() {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: "1.0.0"

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(title = "关于应用", icon = Icons.Default.Info)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nexus",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "MC 命令助手 v$versionName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "强大的 Minecraft 命令编辑工具",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingRow(
                        icon = Icons.Default.Code,
                        title = "源代码",
                        description = "GitHub: NinefCJ/Nexus",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/NinefCJ/Nexus"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isDestructive)
                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun rememberLauncherForImagePicker(
    onResult: (android.net.Uri?) -> Unit
): () -> Unit {
    val context = LocalContext.current
    var currentOnResult by remember { mutableStateOf(onResult) }
    currentOnResult = onResult

    val launcher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        currentOnResult(uri)
    }

    return {
        launcher.launch(arrayOf("image/*"))
    }
}

@Composable
fun ThemePreviewCard(
    theme: com.nexuscmd.data.AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = when (theme) {
        com.nexuscmd.data.AppTheme.FOLLOW_SYSTEM ->
            listOf(
                androidx.compose.ui.graphics.Color(0xFF4A90D9),
                androidx.compose.ui.graphics.Color(0xFF1A1A2E),
                androidx.compose.ui.graphics.Color(0xFFF5F7FA)
            )
        com.nexuscmd.data.AppTheme.LIGHT ->
            listOf(
                androidx.compose.ui.graphics.Color(0xFF4A90D9),
                androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                androidx.compose.ui.graphics.Color(0xFFF5F7FA)
            )
        com.nexuscmd.data.AppTheme.DARK ->
            listOf(
                androidx.compose.ui.graphics.Color(0xFF5C9CE6),
                androidx.compose.ui.graphics.Color(0xFF1E1E1E),
                androidx.compose.ui.graphics.Color(0xFF121212)
            )
        com.nexuscmd.data.AppTheme.MIDNIGHT ->
            listOf(
                androidx.compose.ui.graphics.Color(0xFF7C8CF3),
                androidx.compose.ui.graphics.Color(0xFF1E293B),
                androidx.compose.ui.graphics.Color(0xFF0F172A)
            )
        com.nexuscmd.data.AppTheme.AMOLED ->
            listOf(
                androidx.compose.ui.graphics.Color(0xFF818CF8),
                androidx.compose.ui.graphics.Color(0xFF0A0A0A),
                androidx.compose.ui.graphics.Color(0xFF000000)
            )
        com.nexuscmd.data.AppTheme.GREEN ->
            listOf(
                androidx.compose.ui.graphics.Color(0xFF4CAF50),
                androidx.compose.ui.graphics.Color(0xFFE8F5E9),
                androidx.compose.ui.graphics.Color(0xFFF8FAF5)
            )
        com.nexuscmd.data.AppTheme.OCEAN ->
            listOf(
                androidx.compose.ui.graphics.Color(0xFF0288D1),
                androidx.compose.ui.graphics.Color(0xFFE1F5FE),
                androidx.compose.ui.graphics.Color(0xFFF1F8FC)
            )
        com.nexuscmd.data.AppTheme.WARM ->
            listOf(
                androidx.compose.ui.graphics.Color(0xFFE65100),
                androidx.compose.ui.graphics.Color(0xFFFFE0B2),
                androidx.compose.ui.graphics.Color(0xFFFBF5F0)
            )
        com.nexuscmd.data.AppTheme.MATCHA ->
            listOf(
                androidx.compose.ui.graphics.Color(0xFF7CB342),
                androidx.compose.ui.graphics.Color(0xFFDCEDC8),
                androidx.compose.ui.graphics.Color(0xFFF7F9F0)
            )
        com.nexuscmd.data.AppTheme.DREAMY_PURPLE ->
            listOf(
                androidx.compose.ui.graphics.Color(0xFF7C4DFF),
                androidx.compose.ui.graphics.Color(0xFFE1BEE7),
                androidx.compose.ui.graphics.Color(0xFFF8F5FF)
            )
        com.nexuscmd.data.AppTheme.SAKURA ->
            listOf(
                androidx.compose.ui.graphics.Color(0xFFEC407A),
                androidx.compose.ui.graphics.Color(0xFFF8BBD0),
                androidx.compose.ui.graphics.Color(0xFFFFF5F8)
            )
        com.nexuscmd.data.AppTheme.ARCTIC ->
            listOf(
                androidx.compose.ui.graphics.Color(0xFF1976D2),
                androidx.compose.ui.graphics.Color(0xFFBBDEFB),
                androidx.compose.ui.graphics.Color(0xFFF0F8FF)
            )
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null,
        modifier = androidx.compose.ui.Modifier.width(72.dp)
    ) {
        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors[2])
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(colors[0])
                )
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .align(androidx.compose.ui.Alignment.BottomStart)
                        .padding(start = 6.dp, bottom = 6.dp)
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(colors[1])
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

// ============ Template Generator Tab ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateGeneratorTab(viewModel: MainViewModel) {
    val templateGenerator = remember { com.nexuscmd.data.TemplateGenerator() }
    val templates = remember { templateGenerator.getTemplates() }
    val categories = remember { templateGenerator.getCategories() }

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedTemplate by remember { mutableStateOf<com.nexuscmd.data.CommandTemplate?>(null) }
    var parameterValues by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var generatedCommand by remember { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        if (selectedTemplate == null) {
            // Template list view
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "模板生成器",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "选择模板快速生成命令",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Category chips
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("全部") }
                        )
                        categories.forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val displayedTemplates = if (selectedCategory != null) {
                    templates.filter { it.category == selectedCategory }
                } else {
                    templates
                }

                items(displayedTemplates) { template ->
                    TemplateCard(
                        template = template,
                        onClick = {
                            selectedTemplate = template
                            parameterValues = template.parameters.associate { it.name to it.defaultValue }
                            generatedCommand = templateGenerator.generateCommand(template, parameterValues)
                        }
                    )
                }
            }
        } else {
            // Template editor view
            TemplateEditor(
                template = selectedTemplate!!,
                parameterValues = parameterValues,
                generatedCommand = generatedCommand,
                onParameterChange = { name, value ->
                    parameterValues = parameterValues + (name to value)
                    generatedCommand = templateGenerator.generateCommand(selectedTemplate!!, parameterValues)
                },
                onCopy = {
                    clipboardManager.setText(AnnotatedString(generatedCommand))
                    viewModel.addToHistory(generatedCommand)
                },
                onUseInEditor = {
                    viewModel.onCommandTextChanged(generatedCommand)
                },
                onBack = { selectedTemplate = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateCard(
    template: com.nexuscmd.data.CommandTemplate,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = template.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = template.template,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditor(
    template: com.nexuscmd.data.CommandTemplate,
    parameterValues: Map<String, String>,
    generatedCommand: String,
    onParameterChange: (String, String) -> Unit,
    onCopy: () -> Unit,
    onUseInEditor: () -> Unit,
    onBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = template.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Parameters
        Text(
            text = "参数设置",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(template.parameters.size) { index ->
                val param = template.parameters[index]
                val currentValue = parameterValues[param.name] ?: param.defaultValue

                ParameterInputField(
                    parameter = param,
                    value = currentValue,
                    onValueChange = { onParameterChange(param.name, it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generated command preview
        Text(
            text = "生成的命令",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SyntaxHighlightedText(
                    text = generatedCommand,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    clipboardManager.setText(AnnotatedString(generatedCommand))
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "复制")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCopy,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("复制")
            }
            Button(
                onClick = onUseInEditor,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("使用")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterInputField(
    parameter: com.nexuscmd.data.TemplateParameter,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = parameter.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = parameter.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        when (parameter.type) {
            com.nexuscmd.data.ParamType.SELECT -> {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        parameter.options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onValueChange(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            com.nexuscmd.data.ParamType.BOOLEAN -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = value == "true",
                        onClick = { onValueChange("true") },
                        label = { Text("true") }
                    )
                    FilterChip(
                        selected = value == "false",
                        onClick = { onValueChange("false") },
                        label = { Text("false") }
                    )
                }
            }
            com.nexuscmd.data.ParamType.SELECTOR -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("@p", "@a", "@e", "@s", "@r").forEach { selector ->
                        FilterChip(
                            selected = value == selector,
                            onClick = { onValueChange(selector) },
                            label = { Text(selector) }
                        )
                    }
                }
            }
            else -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(parameter.defaultValue) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddonManagerPage(
    onClose: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedAddon by remember { mutableStateOf<AddonPack?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    val addonManager = remember { AddonManager.getInstance(context) }
    var addons by remember { mutableStateOf(addonManager.loadAddons()) }

    fun refreshAddons() {
        addons = addonManager.loadAddons()
    }

    val filteredAddons = remember(addons, searchQuery) {
        if (searchQuery.isBlank()) addons
        else addons.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.author.contains(searchQuery, ignoreCase = true)
        }
    }

    if (selectedAddon != null) {
        AddonDetailPage(
            addon = selectedAddon!!,
            onBack = { selectedAddon = null },
            onToggleEnabled = { addonId, enabled ->
                if (enabled) addonManager.enableAddon(addonId)
                else addonManager.disableAddon(addonId)
                refreshAddons()
                selectedAddon = addonManager.loadAddons().find { it.id == addonId }
            },
            onUninstall = { addonId ->
                addonManager.uninstallAddon(addonId)
                refreshAddons()
                selectedAddon = null
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("拓展包管理", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "导入拓展包")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            Surface(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("搜索拓展包...", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清除",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "已安装",
                    value = "${addons.size}",
                    icon = Icons.Default.Extension,
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "已启用",
                    value = "${addons.count { it.enabled }}",
                    icon = Icons.Default.CheckCircle,
                    gradientColors = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF2E7D32)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Addon list
            if (filteredAddons.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.ExtensionOff,
                    title = "暂无拓展包",
                    description = "点击右上角按钮导入拓展包",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAddons) { addon ->
                        AddonCard(
                            addon = addon,
                            onClick = { selectedAddon = addon },
                            onToggleEnabled = { enabled ->
                                if (enabled) addonManager.enableAddon(addon.id)
                                else addonManager.disableAddon(addon.id)
                                refreshAddons()
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    if (showImportDialog) {
        ImportAddonDialog(
            onDismiss = { showImportDialog = false },
            onImport = { json ->
                val success = addonManager.installAddon(json)
                if (success) {
                    refreshAddons()
                }
                success
            }
        )
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(gradientColors)
                )
                .padding(16.dp)
        ) {
            Column {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun AddonCard(
    addon: AddonPack,
    onClick: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = addon.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "v${addon.version}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = addon.description.ifEmpty { "暂无描述" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AddonBadge(text = "${addon.customBlocks.size} 方块", tint = Color(0xFFE65100))
                    AddonBadge(text = "${addon.customItems.size} 物品", tint = Color(0xFF1565C0))
                    AddonBadge(text = "${addon.customCommands.size} 命令", tint = Color(0xFF2E7D32))
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Toggle
            Switch(
                checked = addon.enabled,
                onCheckedChange = onToggleEnabled
            )
        }
    }
}

@Composable
fun AddonBadge(text: String, tint: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = tint.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = tint,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddonDetailPage(
    addon: AddonPack,
    onBack: () -> Unit,
    onToggleEnabled: (String, Boolean) -> Unit,
    onUninstall: (String) -> Unit
) {
    var showUninstallDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("拓展包详情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Extension,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = addon.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "v${addon.version} · ${addon.author}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = addon.description.ifEmpty { "暂无描述" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "启用拓展包",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = addon.enabled,
                                onCheckedChange = { onToggleEnabled(addon.id, it) }
                            )
                        }
                    }
                }
            }

            // Content statistics
            item {
                Text(
                    text = "内容统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatItem(
                        label = "方块",
                        value = addon.customBlocks.size.toString(),
                        icon = Icons.Default.ViewModule,
                        color = Color(0xFFE65100),
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatItem(
                        label = "物品",
                        value = addon.customItems.size.toString(),
                        icon = Icons.Default.Category,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatItem(
                        label = "音效",
                        value = addon.customSounds.size.toString(),
                        icon = Icons.Default.VolumeUp,
                        color = Color(0xFF6A1B9A),
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatItem(
                        label = "粒子",
                        value = addon.customParticles.size.toString(),
                        icon = Icons.Default.BubbleChart,
                        color = Color(0xFF00838F),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatItem(
                        label = "命令",
                        value = addon.customCommands.size.toString(),
                        icon = Icons.Default.LibraryBooks,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatItem(
                        label = "模板",
                        value = addon.customTemplates.size.toString(),
                        icon = Icons.Default.AutoFixHigh,
                        color = Color(0xFFEF6C00),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Uninstall button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showUninstallDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("卸载拓展包", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (showUninstallDialog) {
        AlertDialog(
            onDismissRequest = { showUninstallDialog = false },
            title = { Text("卸载拓展包") },
            text = { Text("确定要卸载「${addon.name}」吗？所有自定义内容将被移除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUninstall(addon.id)
                        showUninstallDialog = false
                    }
                ) {
                    Text("卸载", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUninstallDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun DetailStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ImportAddonDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Boolean
) {
    var inputText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入拓展包") },
        text = {
            Column {
                Text(
                    text = "请输入拓展包JSON内容：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    placeholder = { Text("{\n\"id\": ...\n}") },
                    shape = RoundedCornerShape(12.dp),
                    isError = errorMessage != null
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (inputText.isBlank()) {
                        errorMessage = "请输入JSON内容"
                    } else {
                        val success = onImport(inputText)
                        if (success) {
                            onDismiss()
                        } else {
                            errorMessage = "JSON格式无效，请检查内容"
                        }
                    }
                }
            ) {
                Text("导入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandChainManagerPage(
    onClose: () -> Unit,
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showEditor by remember { mutableStateOf(false) }
    var editingChain by remember { mutableStateOf<com.nexuscmd.data.CommandChain?>(null) }
    var selectedChain by remember { mutableStateOf<com.nexuscmd.data.CommandChain?>(null) }

    val filteredChains = remember(uiState.commandChains, searchQuery) {
        if (searchQuery.isBlank()) uiState.commandChains
        else viewModel.searchCommandChains(searchQuery)
    }

    if (showEditor) {
        CommandChainEditorPage(
            chain = editingChain,
            onBack = {
                showEditor = false
                editingChain = null
            },
            onSave = { chain ->
                if (editingChain == null) {
                    viewModel.addCommandChain(chain)
                } else {
                    viewModel.updateCommandChain(chain)
                }
                showEditor = false
                editingChain = null
            }
        )
        return
    }

    if (selectedChain != null) {
        CommandChainDetailPage(
            chain = selectedChain!!,
            onBack = { selectedChain = null },
            onEdit = {
                editingChain = it
                showEditor = true
                selectedChain = null
            },
            onDelete = { chainId ->
                viewModel.deleteCommandChain(chainId)
                selectedChain = null
            },
            onUseCommand = { command ->
                viewModel.onCommandTextChanged(command)
                viewModel.addToHistory(command)
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("命令方块链", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditor = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "新建命令链")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Surface(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("搜索命令链...", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清除",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "命令链",
                    value = "${uiState.commandChains.size}",
                    icon = Icons.Default.Link,
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "总步骤",
                    value = "${uiState.commandChains.sumOf { it.steps.size }}",
                    icon = Icons.Default.FormatListNumbered,
                    gradientColors = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF2E7D32)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredChains.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.LinkOff,
                    title = "暂无命令链",
                    description = "点击右上角按钮创建命令链",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredChains) { chain ->
                        CommandChainCard(
                            chain = chain,
                            onClick = { selectedChain = chain }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun CommandChainCard(
    chain: com.nexuscmd.data.CommandChain,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chain.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = chain.description.ifEmpty { "暂无描述" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AddonBadge(
                        text = "${chain.steps.size} 步骤",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    AddonBadge(
                        text = chain.category,
                        tint = Color(0xFFEF6C00)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandChainDetailPage(
    chain: com.nexuscmd.data.CommandChain,
    onBack: () -> Unit,
    onEdit: (com.nexuscmd.data.CommandChain) -> Unit,
    onDelete: (String) -> Unit,
    onUseCommand: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("命令链详情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(chain) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chain.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = chain.category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = chain.description.ifEmpty { "暂无描述" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "命令步骤",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(chain.steps) { step ->
                CommandChainStepItem(
                    step = step,
                    index = chain.steps.indexOf(step),
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(step.command))
                    },
                    onUse = {
                        onUseCommand(step.command)
                    }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除命令链") },
            text = { Text("确定要删除「${chain.name}」吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(chain.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun CommandChainStepItem(
    step: com.nexuscmd.data.CommandChainStep,
    index: Int,
    onCopy: () -> Unit,
    onUse: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.name.ifEmpty { "步骤 ${index + 1}" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (step.delay > 0) {
                        Text(
                            text = "延迟 ${step.delay} tick",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                SyntaxHighlightedText(
                    text = step.command,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(10.dp)
                )
            }
            if (step.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "备注: ${step.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制", style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = onUse,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("使用", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandChainEditorPage(
    chain: com.nexuscmd.data.CommandChain?,
    onBack: () -> Unit,
    onSave: (com.nexuscmd.data.CommandChain) -> Unit
) {
    var name by remember { mutableStateOf(chain?.name ?: "") }
    var description by remember { mutableStateOf(chain?.description ?: "") }
    var category by remember { mutableStateOf(chain?.category ?: "未分类") }
    var steps by remember { mutableStateOf(chain?.steps?.toMutableList() ?: mutableListOf()) }
    var showStepEditor by remember { mutableStateOf(false) }
    var editingStep by remember { mutableStateOf<Pair<Int, com.nexuscmd.data.CommandChainStep>?>(null) }
    var showSaveError by remember { mutableStateOf(false) }

    val categories = listOf("未分类", "红石", "传送", "效果", "建筑", "小游戏", "其他")

    if (showSaveError) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000)
            showSaveError = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (chain == null) "新建命令链" else "编辑命令链",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (name.isBlank() || steps.isEmpty()) {
                            showSaveError = true
                        } else {
                            val newChain = com.nexuscmd.data.CommandChain(
                                id = chain?.id ?: "",
                                name = name,
                                description = description,
                                steps = steps,
                                category = category
                            )
                            onSave(newChain)
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "保存")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "基本信息",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("名称") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                isError = showSaveError && name.isBlank()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("描述") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "分类",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                categories.take(4).forEach { cat ->
                                    FilterChip(
                                        selected = category == cat,
                                        onClick = { category = cat },
                                        label = { Text(cat, style = MaterialTheme.typography.bodySmall) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                categories.drop(4).forEach { cat ->
                                    FilterChip(
                                        selected = category == cat,
                                        onClick = { category = cat },
                                        label = { Text(cat, style = MaterialTheme.typography.bodySmall) }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "命令步骤",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            editingStep = null
                            showStepEditor = true
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("添加步骤")
                        }
                    }
                }

                if (steps.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.FormatListNumbered,
                            title = "暂无步骤",
                            description = "点击上方按钮添加命令步骤"
                        )
                    }
                } else {
                    items(steps.size) { index ->
                        val step = steps[index]
                        EditableStepItem(
                            step = step,
                            index = index,
                            canMoveUp = index > 0,
                            canMoveDown = index < steps.size - 1,
                            onEdit = {
                                editingStep = index to step
                                showStepEditor = true
                            },
                            onDelete = {
                                steps = steps.toMutableList().also { it.removeAt(index) }
                            },
                            onMoveUp = {
                                if (index > 0) {
                                    steps = steps.toMutableList().also {
                                        val temp = it[index]
                                        it[index] = it[index - 1]
                                        it[index - 1] = temp
                                    }
                                }
                            },
                            onMoveDown = {
                                if (index < steps.size - 1) {
                                    steps = steps.toMutableList().also {
                                        val temp = it[index]
                                        it[index] = it[index + 1]
                                        it[index + 1] = temp
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showStepEditor) {
        StepEditorDialog(
            step = editingStep?.second,
            index = editingStep?.first,
            onDismiss = {
                showStepEditor = false
                editingStep = null
            },
            onSave = { newStep ->
                steps = steps.toMutableList().also { list ->
                    if (editingStep != null) {
                        list[editingStep!!.first] = newStep
                    } else {
                        list.add(newStep)
                    }
                }
                showStepEditor = false
                editingStep = null
            }
        )
    }
}

@Composable
fun EditableStepItem(
    step: com.nexuscmd.data.CommandChainStep,
    index: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.name.ifEmpty { "步骤 ${index + 1}" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (step.delay > 0) {
                        Text(
                            text = "延迟 ${step.delay} tick",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "上移",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "下移",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = step.command,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(10.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StepEditorDialog(
    step: com.nexuscmd.data.CommandChainStep?,
    index: Int?,
    onDismiss: () -> Unit,
    onSave: (com.nexuscmd.data.CommandChainStep) -> Unit
) {
    var stepName by remember { mutableStateOf(step?.name ?: "") }
    var command by remember { mutableStateOf(step?.command ?: "") }
    var delay by remember { mutableStateOf(step?.delay?.toString() ?: "0") }
    var note by remember { mutableStateOf(step?.note ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (step == null) "添加步骤" else "编辑步骤") },
        text = {
            Column {
                OutlinedTextField(
                    value = stepName,
                    onValueChange = { stepName = it },
                    label = { Text("步骤名称（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = command,
                    onValueChange = {
                        command = it
                        showError = false
                    },
                    label = { Text("命令") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    isError = showError
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = delay,
                    onValueChange = { delay = it.filter { c -> c.isDigit() } },
                    label = { Text("延迟（tick）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                if (showError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "请输入命令内容",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (command.isBlank()) {
                        showError = true
                    } else {
                        val newStep = com.nexuscmd.data.CommandChainStep(
                            id = step?.id ?: "step_${System.currentTimeMillis()}_${(0..9999).random()}",
                            command = command,
                            name = stepName,
                            delay = delay.toIntOrNull() ?: 0,
                            note = note
                        )
                        onSave(newStep)
                    }
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// ============ Modern UI Components ============

@Composable
fun BackgroundLayer(
    useCustomBackground: Boolean,
    customBackgroundUri: String?,
    backgroundOpacity: Float
) {
    val context = LocalContext.current

    if (useCustomBackground && customBackgroundUri != null) {
        var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

        LaunchedEffect(customBackgroundUri) {
            try {
                val uri = android.net.Uri.parse(customBackgroundUri)
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                parcelFileDescriptor?.use { pfd ->
                    bitmap = android.graphics.BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
                }
            } catch (e: Exception) {
                bitmap = null
            }
        }

        bitmap?.let { bmp ->
            androidx.compose.foundation.Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(backgroundOpacity)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                        )
                    )
                )
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopAppBar(
    onToggleTheme: () -> Unit,
    isDark: Boolean,
    onRequestFloatingPermission: () -> Unit,
    onStartFloating: () -> Unit,
    cardOpacity: Float = 0.9f,
    useGlassmorphism: Boolean = true,
    glassIntensity: Float = 0.7f,
    cardCornerRadius: Float = 16f
) {
    val surfaceColor = if (useGlassmorphism) {
        MaterialTheme.colorScheme.surface.copy(alpha = glassIntensity * 0.9f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = cardOpacity)
    }
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (useGlassmorphism) 0.2f else 0.1f)

    Surface(
        color = surfaceColor,
        tonalElevation = 0.dp,
        shadowElevation = if (useGlassmorphism) 0.dp else 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(cardCornerRadius.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Nexus",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MC 命令助手",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "切换主题",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onRequestFloatingPermission) {
                        Icon(
                            imageVector = Icons.Default.PictureInPicture,
                            contentDescription = "悬浮窗权限",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onStartFloating) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "开启悬浮窗",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            if (useGlassmorphism) {
                Divider(
                    color = borderColor,
                    thickness = 0.5.dp
                )
            }
        }
    }
}

@Composable
fun ModernBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    cardOpacity: Float = 0.9f,
    useGlassmorphism: Boolean = true,
    glassIntensity: Float = 0.7f,
    cardCornerRadius: Float = 16f
) {
    val tabs = listOf(
        Triple("编辑器", Icons.Default.Edit, 0),
        Triple("命令库", Icons.Default.LibraryBooks, 1),
        Triple("速查", Icons.Default.Bolt, 2),
        Triple("资源库", Icons.Default.GridView, 3),
        Triple("历史", Icons.Default.History, 4),
        Triple("设置", Icons.Default.Settings, 5)
    )

    val surfaceColor = if (useGlassmorphism) {
        MaterialTheme.colorScheme.surface.copy(alpha = glassIntensity * 0.95f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = cardOpacity)
    }
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (useGlassmorphism) 0.2f else 0.1f)

    Surface(
        color = surfaceColor,
        tonalElevation = 0.dp,
        shadowElevation = if (useGlassmorphism) 0.dp else 2.dp
    ) {
        Column {
            if (useGlassmorphism) {
                Divider(
                    color = borderColor,
                    thickness = 0.5.dp
                )
            }
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                tabs.forEach { (title, icon, index) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = title
                            )
                        },
                        label = {
                            Text(
                                title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

// ============ Glassmorphism / Mica UI Components ============

/**
 * 毛玻璃卡片组件 - 提供现代化的毛玻璃效果
 * 使用半透明背景、边框高光和可选的模糊效果模拟毛玻璃质感
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Float = 16f,
    glassIntensity: Float = 0.7f,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    val surfaceColor = backgroundColor.copy(alpha = glassIntensity * 0.85f)
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius.dp),
        color = surfaceColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.drawBehind {
                drawRect(
                    color = borderColor,
                    size = size,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        ) {
            content()
        }
    }
}

/**
 * 毛玻璃容器 - 用于包裹整个页面内容
 */
@Composable
fun GlassmorphicContainer(
    modifier: Modifier = Modifier,
    cornerRadius: Float = 24f,
    glassIntensity: Float = 0.7f,
    showBorder: Boolean = true,
    content: @Composable () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface.copy(alpha = glassIntensity * 0.75f)
    val borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius.dp),
        color = surfaceColor,
        tonalElevation = 0.dp,
        shadowElevation = if (glassIntensity > 0.5f) 2.dp else 0.dp
    ) {
        if (showBorder) {
            Box(
                modifier = Modifier.drawBehind {
                    drawRect(
                        color = borderColor,
                        size = size,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            ) {
                content()
            }
        } else {
            content()
        }
    }
}

/**
 * 顶部导航毛玻璃栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassmorphicTopBar(
    modifier: Modifier = Modifier,
    cornerRadius: Float = 0f,
    glassIntensity: Float = 0.7f,
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val surfaceColor = MaterialTheme.colorScheme.surface.copy(alpha = glassIntensity * 0.9f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius.dp),
        color = surfaceColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column {
            TopAppBar(
                title = title,
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
            if (cornerRadius == 0f) {
                Divider(
                    color = borderColor,
                    thickness = 0.5.dp
                )
            }
        }
    }
}

/**
 * 底部导航毛玻璃栏
 */
@Composable
fun GlassmorphicBottomBar(
    modifier: Modifier = Modifier,
    cornerRadius: Float = 0f,
    glassIntensity: Float = 0.7f,
    content: @Composable () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface.copy(alpha = glassIntensity * 0.95f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius.dp),
        color = surfaceColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column {
            Divider(
                color = borderColor,
                thickness = 0.5.dp
            )
            content()
        }
    }
}

/**
 * 渐变色按钮
 */
@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            com.nexuscmd.ui.theme.GradientStart,
            com.nexuscmd.ui.theme.GradientEnd
        )
    ),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradient, RoundedCornerShape(12.dp))
                .then(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

/**
 * 渐变卡片
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            com.nexuscmd.ui.theme.GradientStart.copy(alpha = 0.8f),
            com.nexuscmd.ui.theme.GradientEnd.copy(alpha = 0.8f)
        )
    ),
    cornerRadius: Float = 16f,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius.dp),
        color = Color.Transparent,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.background(gradient, RoundedCornerShape(cornerRadius.dp))
        ) {
            content()
        }
    }
}

/**
 * 主题预览卡片 - 增强版，显示更多信息
 */
@Composable
fun EnhancedThemePreviewCard(
    theme: com.nexuscmd.data.AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    cornerRadius: Float = 12f
) {
    val colors = getThemeColors(theme)
    val primaryColor = colors[0]
    val surfaceColor = colors[2]

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(cornerRadius.dp),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.width(80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 主题预览
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(surfaceColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(primaryColor)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 6.dp, bottom = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors[1])
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 主题名称
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            // 暗色模式标识
            if (theme.isDark) {
                Text(
                    text = "🌙",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

private fun getThemeColors(theme: com.nexuscmd.data.AppTheme): List<Color> {
    return when (theme) {
        com.nexuscmd.data.AppTheme.FOLLOW_SYSTEM ->
            listOf(Color(0xFF4A90D9), Color(0xFF1A1A2E), Color(0xFFF5F7FA))
        com.nexuscmd.data.AppTheme.LIGHT ->
            listOf(Color(0xFF4A90D9), Color(0xFFFFFFFF), Color(0xFFF5F7FA))
        com.nexuscmd.data.AppTheme.DARK ->
            listOf(Color(0xFF5C9CE6), Color(0xFF1E1E1E), Color(0xFF121212))
        com.nexuscmd.data.AppTheme.MIDNIGHT ->
            listOf(Color(0xFF7C8CF3), Color(0xFF1E293B), Color(0xFF0F172A))
        com.nexuscmd.data.AppTheme.AMOLED ->
            listOf(Color(0xFF818CF8), Color(0xFF0A0A0A), Color(0xFF000000))
        com.nexuscmd.data.AppTheme.GREEN ->
            listOf(Color(0xFF4CAF50), Color(0xFFE8F5E9), Color(0xFFF8FAF5))
        com.nexuscmd.data.AppTheme.OCEAN ->
            listOf(Color(0xFF0288D1), Color(0xFFE1F5FE), Color(0xFFF1F8FC))
        com.nexuscmd.data.AppTheme.WARM ->
            listOf(Color(0xFFE65100), Color(0xFFFFE0B2), Color(0xFFFBF5F0))
        com.nexuscmd.data.AppTheme.MATCHA ->
            listOf(Color(0xFF7CB342), Color(0xFFDCEDC8), Color(0xFFF7F9F0))
        com.nexuscmd.data.AppTheme.DREAMY_PURPLE ->
            listOf(Color(0xFF7C4DFF), Color(0xFFE1BEE7), Color(0xFFF8F5FF))
        com.nexuscmd.data.AppTheme.SAKURA ->
            listOf(Color(0xFFEC407A), Color(0xFFF8BBD0), Color(0xFFFFF5F8))
        com.nexuscmd.data.AppTheme.ARCTIC ->
            listOf(Color(0xFF1976D2), Color(0xFFBBDEFB), Color(0xFFF0F8FF))
    }
}
