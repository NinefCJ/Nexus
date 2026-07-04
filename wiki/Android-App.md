# Android 应用

Nexus Android 应用使用 Jetpack Compose + Material 3 构建，通过 JNI 调用 C++ 核心库提供命令辅助功能。

---

## 项目结构

```
Nexus-Android/
├── app/src/main/
│   ├── cpp/                              # JNI 桥接层
│   │   ├── CMakeLists.txt
│   │   └── native.cpp
│   │
│   ├── java/com/nexuscmd/
│   │   ├── MainActivity.kt               # 主界面
│   │   ├── MainViewModel.kt              # 视图模型
│   │   ├── CommandHelper.kt              # Native 封装
│   │   ├── FloatingWindowService.kt      # 悬浮窗服务
│   │   │
│   │   ├── data/                         # 数据层
│   │   │   ├── CommandRepository.kt
│   │   │   ├── CommandChainRepository.kt
│   │   │   ├── HistoryManager.kt
│   │   │   ├── SettingsManager.kt
│   │   │   ├── AddonManager.kt
│   │   │   ├── TemplateGenerator.kt
│   │   │   │
│   │   │   ├── BlockLibrary.kt
│   │   │   ├── ItemLibrary.kt
│   │   │   ├── SoundEffectLibrary.kt
│   │   │   ├── ParticleLibrary.kt
│   │   │   └── AnimationLibrary.kt
│   │   │
│   │   └── ui/                           # UI 层
│   │       ├── components/
│   │       │   ├── MCSyntaxHighlighter.kt
│   │       │   └── SyntaxHighlightEditor.kt
│   │       │
│   │       └── theme/
│   │           ├── Color.kt
│   │           ├── Theme.kt
│   │           └── Type.kt
│   │
│   └── res/                              # 资源文件
│       ├── drawable/
│       ├── layout/
│       ├── mipmap-*/
│       └── values/
│
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 包信息

| 属性 | 值 |
|------|-----|
| 包名 | `com.nexuscmd` |
| 最低 SDK | 24 (Android 7.0) |
| 目标 SDK | 34 (Android 14) |
| 当前版本 | 1.3.0 (versionCode: 4) |
| ABI 支持 | armeabi-v7a, arm64-v8a, x86, x86_64 |

---

## 核心组件

### 1. MainActivity.kt

**职责**：应用入口，管理主界面状态和导航。

**主要功能**：
- 开屏动画
- 底部导航切换
- 各 Tab 页面展示
- 权限申请处理

**页面结构**：

```
MainScreen
├── EditorTab           # 命令编辑器
├── CommandLibraryTab   # 命令库
├── QuickCommandsTab    # 快速命令
├── ResourceLibraryTab  # 资源库
│   ├── BlockLibraryTab       # 方块库
│   ├── ItemLibraryTab        # 物品库
│   ├── SoundEffectLibraryTab # 音效库
│   ├── ParticleLibraryTab    # 粒子库
│   └── AnimationLibraryTab   # 动画库
├── HistoryTab          # 历史记录
└── SettingsTab         # 设置
```

### 2. MainViewModel.kt

**职责**：全局状态管理、业务逻辑编排、Native 调用协调。

**核心状态 (MainUiState)**：

| 状态字段 | 类型 | 说明 |
|---------|------|------|
| `commandText` | `String` | 命令输入文本 |
| `cursorPosition` | `Int` | 光标位置 |
| `validation` | `ValidationResult?` | 验证结果 |
| `completions` | `List<CompletionItem>` | 补全建议列表 |
| `syntaxHint` | `SyntaxHint?` | 语法提示 |
| `quickCommands` | `List<Triple<String, String, ImageVector>>` | 快速命令 |

**性能优化策略**：
- 防抖延迟 (150ms) — 减少频繁计算
- 补全结果缓存 — 避免重复 Native 调用
- 并行 Native 调用 — `viewModelScope.async`
- `@Stable` 注解 — 减少不必要重组

### 3. CommandHelper.kt

**职责**：封装 JNI 调用，提供 Kotlin 友好的接口。

**主要方法**：

```kotlin
class CommandHelper {
    // 初始化核心库，加载命令数据包
    fun initialize(packPath: String): Boolean
    
    // 获取补全建议
    fun getCompletions(command: String, cursor: Int): List<CompletionItem>
    
    // 验证命令语法
    fun validateCommand(command: String): ValidationResult
    
    // 获取语法提示模板
    fun getSyntaxHint(command: String, cursor: Int): SyntaxHint?
    
    // 获取语法高亮信息
    fun getHighlightTokens(command: String): List<HighlightToken>
    
    // 释放资源
    fun dispose()
}
```

**使用模式**：

```kotlin
// 初始化
val helper = CommandHelper()
helper.initialize(packPath)

// 获取补全
val completions = helper.getCompletions("/give @p diamo", 14)

// 释放
helper.dispose()
```

### 4. FloatingWindowService.kt

**职责**：悬浮窗服务，在游戏中提供实时命令辅助。

**功能**：
- 悬浮窗口显示/隐藏
- 最小化/展开模式切换
- 命令输入与补全
- 一键复制到剪贴板
- 拖拽移动位置

**生命周期**：
- `onCreate()` — 创建悬浮窗 View
- `onStartCommand()` — 处理显示/隐藏指令
- `onDestroy()` — 移除悬浮窗，释放资源

---

## 数据层

### ID 翻译库设计模式

所有 ID 库遵循统一的设计模式，便于扩展和维护：

```
┌─────────────────────────────────────┐
│         Data Class                  │
│  - id: String (Minecraft ID)       │
│  - name: String (中文名)            │
│  - category: String (分类)          │
│  - description: String (描述)       │
│  - extra: ... (额外参数)             │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│        Library Object               │
│  - categories: List<String>         │
│  - items: List<DataClass>           │
│  - filter(query, category): List<T> │
│  - buildCommand(item, params): String│
└─────────────────────────────────────┘
```

### 各库详情

| 库 | 数据量 | 分类数 | 生成命令 |
|----|--------|--------|----------|
| BlockLibrary | 456 | 16 | `/setblock` |
| ItemLibrary | 336 | 10 | `/give` |
| SoundEffectLibrary | 918 | 37 | `/playsound` |
| ParticleLibrary | 400+ | 9 | `/particle` |
| AnimationLibrary | 200+ | 9 | `/playanimation` |

### SettingsManager

**职责**：管理应用设置项的持久化。

**设置项**：

| 设置 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `currentTheme` | `AppTheme` | `FOLLOW_SYSTEM` | 当前主题 |
| `cardOpacity` | `Float` | `0.85f` | 卡片透明度 |
| `glassIntensity` | `Float` | `0.85f` | 毛玻璃强度 |
| `cardCornerRadius` | `Float` | `16f` | 圆角大小 (dp) |
| `useGlassmorphism` | `Boolean` | `true` | 启用毛玻璃 |
| `useCustomBackground` | `Boolean` | `false` | 自定义背景 |
| `customBackgroundUri` | `String?` | `null` | 背景图片 URI |
| `backgroundOpacity` | `Float` | `0.3f` | 背景透明度 |

### HistoryManager

**职责**：管理命令历史记录。

**功能**：
- 添加历史记录
- 按时间倒序查询
- 清除历史
- 收藏常用命令

### AddonManager

**职责**：拓展包加载与管理。

**功能**：
- 扫描可用拓展包
- 加载/卸载拓展包
- 拓展包依赖检查
- JSON Schema 验证

---

## UI 层

### 语法高亮编辑器

`SyntaxHighlightEditor.kt` + `MCSyntaxHighlighter.kt`

**功能**：
- 实时语法高亮渲染
- 光标位置跟踪
- 补全提示浮层
- 语法模板提示

**实现原理**：
1. 文本变化时调用 Native 获取高亮 Token
2. 使用 `AnnotatedString` 渲染带样式文本
3. 光标位置变化时更新语法提示

### 主题系统

详见 [主题系统](Theme-System) 文档。

### 毛玻璃效果

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
        )
) {
    // 内容
}
```

---

## JNI 桥接层

### native.cpp

Android 端的 JNI 入口文件，实现了所有 `native` 方法。

### 数据传递优化

| 优化项 | 实现方式 |
|--------|----------|
| 字符串传递 | 使用 `GetStringUTFChars` / `ReleaseStringUTFChars` |
| 数组返回 | 构建 `jobjectArray`，批量返回 |
| 类引用缓存 | 全局缓存 `jclass` 和 `methodID` |
| 异常检查 | 每次 JNI 调用后检查 `ExceptionCheck` |

### CMake 配置

`app/src/main/cpp/CMakeLists.txt` 配置 Native 库构建，将 Nexus-Core 链接为静态库。

---

## 构建配置

### build.gradle.kts 关键配置

```kotlin
android {
    namespace = "com.nexuscmd"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nexuscmd"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "1.3.0"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }
    
    buildFeatures {
        compose = true
    }
}
```

### 主要依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| `androidx.core:core-ktx` | 1.12.0 | Kotlin 扩展 |
| `androidx.lifecycle:*` | 2.6.2 | ViewModel & Lifecycle |
| `androidx.activity:activity-compose` | 1.8.1 | Compose Activity |
| `compose-bom` | 2023.10.01 | Compose BOM |
| `material3` | - | Material 3 组件 |
| `material-icons-extended` | 1.5.4 | 扩展图标 |

---

## 更多阅读

- [技术架构](Architecture) — 整体架构设计
- [主题系统](Theme-System) — 主题与毛玻璃效果
- [性能优化](Performance) — Android 端性能调优
- [贡献指南](Contributing) — 参与 Android 端开发
