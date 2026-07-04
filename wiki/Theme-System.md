# 主题系统

Nexus 拥有完善的主题系统，支持 12 种预设主题和高度自定义的视觉效果。

---

## 预设主题

### 主题列表

| 主题 | 标识 | 模式 | 特点 |
|------|------|------|------|
| 跟随系统 | `FOLLOW_SYSTEM` | 自动 | 根据系统设置自动切换深浅色 |
| 浅色模式 | `LIGHT` | 浅色 | 明亮清爽的白色主题 |
| 深色模式 | `DARK` | 深色 | 经典深灰色护眼主题 |
| 午夜蓝 | `MIDNIGHT` | 深色 | 深蓝色调，适合游戏环境 |
| AMOLED 黑 | `AMOLED` | 深色 | 纯黑色，OLED 屏省电 |
| 绿色护眼 | `GREEN` | 双模式 | 温和绿色调，长时间阅读 |
| 海洋蓝 | `OCEAN` | 双模式 | 清新蓝色调 |
| 暖橙色调 | `WARM` | 双模式 | 温暖橙色调 |
| 抹茶绿 | `MATCHA` | 双模式 | 柔和抹茶绿色 |
| 梦幻紫 | `DREAMY_PURPLE` | 双模式 | 梦幻紫粉色调 |
| 樱花粉 | `SAKURA` | 双模式 | 粉嫩樱花色系 |
| 北极蓝 | `ARCTIC` | 双模式 | 清爽冰蓝色调 |

### 主题色值参考

每个主题都包含以下核心色板：

| 色板角色 | 说明 |
|---------|------|
| `primary` | 主色调，用于按钮、选中状态 |
| `secondary` | 辅助色，用于强调元素 |
| `tertiary` | 第三色，用于装饰和图标 |
| `background` | 背景色 |
| `surface` | 表面色，卡片、对话框 |
| `onPrimary` | 主色上的文字颜色 |
| `onSecondary` | 辅助色上的文字颜色 |
| `onBackground` | 背景上的文字颜色 |
| `onSurface` | 表面上的文字颜色 |
| `surfaceVariant` | 变体表面色 |
| `onSurfaceVariant` | 变体表面上的文字颜色 |

---

## 语法高亮颜色

命令编辑器使用独立的语法高亮配色：

| 语法元素 | 颜色 | 十六进制 |
|----------|------|----------|
| 命令 (Command) | 青色 | `#56B6C2` |
| 关键词 (Keyword) | 紫色 | `#C678DD` |
| 字符串 (String) | 绿色 | `#98C379` |
| 数字 (Number) | 橙色 | `#D19A66` |
| 选择器 (Selector) | 蓝色 | `#61AFEF` |
| 错误 (Error) | 红色 | `#E06C75` |

这些颜色在所有主题中保持一致，确保命令可读性。

---

## 毛玻璃效果

Nexus 实现了美观的毛玻璃 (Glassmorphism) 视觉效果。

### 效果原理

毛玻璃效果通过以下方式实现：

```
背景图层
    ↓
模糊处理 (Blur)
    ↓
半透明渐变叠加
    ↓
边框 + 圆角
    ↓
内容图层
```

### 可调节参数

| 参数 | 范围 | 默认值 | 说明 |
|------|------|--------|------|
| 毛玻璃开关 | on/off | on | 是否启用毛玻璃效果 |
| 玻璃强度 | 0.0 - 1.0 | 0.85 | 模糊和透明度综合强度 |
| 卡片透明度 | 0.0 - 1.0 | 0.85 | 卡片背景透明度 |
| 圆角大小 | 8dp - 32dp | 16dp | 卡片圆角半径 |

### Compose 实现

```kotlin
Surface(
    modifier = Modifier
        .blur(radius = (20 * glassIntensity).dp)
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    surfaceColor.copy(alpha = 0.7f),
                    surfaceColor.copy(alpha = 0.9f)
                )
            )
        ),
    shape = RoundedCornerShape(cardCornerRadius.dp)
) {
    // 内容
}
```

---

## 自定义背景

### 功能说明

用户可以选择自定义图片作为应用背景，支持：
- 从相册选择图片
- 调整背景透明度
- 适配各种屏幕尺寸

### 设置项

| 设置 | 范围 | 默认值 | 说明 |
|------|------|--------|------|
| 自定义背景开关 | on/off | off | 是否启用自定义背景 |
| 背景图片 URI | - | null | 背景图片路径 |
| 背景透明度 | 0.0 - 1.0 | 0.3 | 背景图片透明度 |

---

## 主题系统架构

### 主题枚举

```kotlin
enum class AppTheme {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK,
    MIDNIGHT,
    AMOLED,
    GREEN,
    OCEAN,
    WARM,
    MATCHA,
    DREAMY_PURPLE,
    SAKURA,
    ARCTIC
}
```

### 主题 Composable

```kotlin
@Composable
fun MCCommandHelperTheme(
    theme: AppTheme = AppTheme.FOLLOW_SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
)
```

- 根据 `theme` 参数选择对应的 ColorScheme
- `FOLLOW_SYSTEM` 模式下自动适配系统深/浅色
- 支持 Android 12+ 的 Dynamic Color（动态取色）

---

## 添加新主题

### 步骤 1：定义颜色

在 `Color.kt` 中添加新主题的颜色：

```kotlin
// 新主题 - 浅色
val MyThemeLightPrimary = Color(0xFF...)
val MyThemeLightAccent = Color(0xFF...)
val MyThemeLightBg = Color(0xFF...)
val MyThemeLightSurface = Color(0xFF...)
val MyThemeLightText = Color(0xFF...)
val MyThemeLightTextSecondary = Color(0xFF...)

// 新主题 - 深色
val MyThemeDarkPrimary = Color(0xFF...)
val MyThemeDarkAccent = Color(0xFF...)
// ...
```

### 步骤 2：创建 ColorScheme

在 `Theme.kt` 中添加 ColorScheme：

```kotlin
private val MyThemeLightColorScheme = lightColorScheme(
    primary = MyThemeLightPrimary,
    secondary = MyThemeLightAccent,
    // ... 其他颜色
)

private val MyThemeDarkColorScheme = darkColorScheme(
    primary = MyThemeDarkPrimary,
    secondary = MyThemeDarkAccent,
    // ... 其他颜色
)
```

### 步骤 3：添加到枚举和选择逻辑

在 `AppTheme` 枚举中添加新项，并在 `MCCommandHelperTheme` 的 when 分支中添加对应逻辑。

---

## 更多阅读

- [Android 应用](Android-App) — UI 层架构
- [贡献指南](Contributing) — 参与主题开发
