package com.nexuscmd.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexuscmd.CommandInfo
import com.nexuscmd.SyntaxHint

@Composable
fun SyntaxHighlightEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = false,
    maxLines: Int = 4,
    enabled: Boolean = true
) {
    val highlighter = remember { MCSyntaxHighlighter() }
    val highlightedText = remember(value) { highlighter.highlight(value) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Placeholder
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            }

            // Syntax highlighted text (behind the actual input)
            if (value.isNotEmpty()) {
                Text(
                    text = highlightedText,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.matchParentSize()
                )
            }

            // Actual input field (transparent text)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = Color.Transparent
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = singleLine,
                maxLines = maxLines,
                enabled = enabled,
                decorationBox = { innerTextField ->
                    Box {
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
fun SyntaxHighlightedText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp
    )
) {
    val highlighter = remember { MCSyntaxHighlighter() }
    val highlightedText = remember(text) { highlighter.highlight(text) }

    Text(
        text = highlightedText,
        style = style,
        modifier = modifier
    )
}

@Composable
fun CommandPreviewCard(
    command: String,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (showLabel) {
                Text(
                    text = "命令预览",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            SyntaxHighlightedText(
                text = command,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
fun CommandInfoPanel(
    commandName: String,
    syntax: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row {
                Text(
                    text = "/$commandName",
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace,
                    color = SyntaxColors.Command
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = syntax,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SyntaxColorLegend(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "语法颜色",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            val items = listOf(
                "命令" to SyntaxColors.Command,
                "选择器" to SyntaxColors.Selector,
                "坐标" to SyntaxColors.Coordinate,
                "数字" to SyntaxColors.Number,
                "物品ID" to SyntaxColors.ItemId,
                "字符串" to SyntaxColors.String,
                "关键词" to SyntaxColors.Keyword,
                "NBT键" to SyntaxColors.NBTKey
            )

            items.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { (name, color) ->
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun SyntaxHintOverlay(
    syntaxHint: SyntaxHint,
    modifier: Modifier = Modifier
) {
    if (syntaxHint.template.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Syntax template display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                syntaxHint.template.split(" ").forEachIndexed { index, part ->
                    val isActive = index == syntaxHint.activeParamIndex
                    SyntaxTemplatePart(
                        text = part,
                        isActive = isActive,
                        isOptional = syntaxHint.isOptional && isActive
                    )
                    if (index < syntaxHint.template.split(" ").size - 1) {
                        Text(
                            text = " ",
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Parameter hint display
            if (syntaxHint.activeParamHint.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    // Parameter label chip
                    SuggestionChip(
                        onClick = { },
                        label = {
                            Text(
                                text = if (syntaxHint.isOptional) "[${syntaxHint.activeParamName}]" else "<${syntaxHint.activeParamName}>",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (syntaxHint.isOptional)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    // Hint text
                    Text(
                        text = syntaxHint.activeParamHint,
                        style = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SyntaxTemplatePart(
    text: String,
    isActive: Boolean,
    isOptional: Boolean
) {
    val backgroundColor = when {
        isActive && isOptional -> MaterialTheme.colorScheme.secondaryContainer
        isActive -> MaterialTheme.colorScheme.primaryContainer
        text.startsWith("/") -> MaterialTheme.colorScheme.tertiaryContainer
        text.startsWith("[") || text.endsWith("]") -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }

    val textColor = when {
        isActive && isOptional -> MaterialTheme.colorScheme.onSecondaryContainer
        isActive -> MaterialTheme.colorScheme.onPrimaryContainer
        text.startsWith("/") -> SyntaxColors.Command
        text.startsWith("[") || text.endsWith("]") -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    // Remove brackets for display if needed
    val displayText = text.removePrefix("[").removeSuffix("]")

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = displayText,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = textColor
            )
        )
    }
}

@Composable
fun CommandSyntaxCard(
    commandInfo: CommandInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "/${commandInfo.name}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = SyntaxColors.Command
                    )
                )
                if (commandInfo.syntax.isNotEmpty()) {
                    Text(
                        text = commandInfo.syntax,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            if (commandInfo.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = commandInfo.description,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}
