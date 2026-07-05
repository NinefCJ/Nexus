# Nexus - Minecraft 命令助手

> 一款专为 Minecraft 基岩版设计的命令辅助工具，提供智能语法提示、命令补全、ID翻译库等强大功能。

[![GitHub Pages](https://img.shields.io/badge/Pages-online-success?logo=github&logoColor=white)](https://ninefcj.github.io/Nexus/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://developer.android.com)
[![Wiki](https://img.shields.io/badge/Wiki-docs-informational.svg)](https://github.com/NinefCJ/Nexus/wiki)

[English](README-en.md) | 简体中文

🌐 **在线预览**：[https://ninefcj.github.io/Nexus/](https://ninefcj.github.io/Nexus/)

📚 **Wiki 文档**：[https://github.com/NinefCJ/Nexus/wiki](https://github.com/NinefCJ/Nexus/wiki)

---

## 目录

- [项目概述](#项目概述)
- [项目结构](#项目结构)
- [技术架构](#技术架构)
- [快速开始](#快速开始)
- [模块详解](#模块详解)
- [数据模型](#数据模型)
- [性能优化](#性能优化)
- [主题系统](#主题系统)
- [贡献指南](#贡献指南)
- [相关项目](#相关项目)

---

## 项目概述

Nexus 是一款跨平台的 Minecraft 基岩版命令辅助工具，主要功能包括：

### 核心功能

| 功能 | 描述 |
|------|------|
| **命令补全** | 实时语法提示和自动补全，支持智能上下文ID补全 |
| **语法高亮** | 命令结构用不同颜色区分，可读性更强 |
| **错误检测** | 实时检测命令语法错误，即时反馈 |
| **ID翻译库** | 方块/物品/音效/粒子/动画完整中文翻译 |
| **命令模板** | 55+ 常用命令模板快速生成 |
| **悬浮窗** | 游戏中也能实时使用命令助手 |
| **主题系统** | 8种预设主题 + 自定义背景图片 |
| **毛玻璃UI** | 现代磨砂玻璃视觉效果 |
| **拓展包** | 支持JSON格式自定义拓展包 |
| **游戏桥接** | Messenger 跨进程通信，与游戏客户端双向交互 |
| **无障碍粘贴** | 通过 AccessibilityService 实现一键粘贴到游戏 |
| **快捷设置** | 系统快速设置面板一键启动悬浮窗 |
| **开机自启** | 设备开机后自动启动服务，随时待命 |
| **命令库导入** | 支持从文件管理器加载自定义命令库 |
| **Intent 调用** | 支持外部应用通过 Intent 调用命令助手功能 |

---

## 项目结构

```
Nexus/
├── Nexus-Core/                    # C++ 原生核心库 (跨平台基础)
│   ├── include/                   # 头文件目录
│   │   ├── command_helper_jni.hpp   # JNI 接口声明
│   │   ├── command_registry.hpp     # 命令注册表
│   │   ├── completion.hpp           # 补全引擎
│   │   ├── highlighter.hpp          # 语法高亮
│   │   ├── parser.hpp               # 命令解析器
│   │   ├── tokenizer.hpp            # 词法分析器
│   │   └── types.hpp                # 类型定义
│   ├── src/                        # 源代码目录
│   │   ├── command_helper_jni.cpp   # JNI 实现层
│   │   ├── command_registry.cpp     # 命令注册表实现
│   │   ├── completion.cpp           # 补全逻辑实现
│   │   ├── highlighter.cpp          # 高亮逻辑实现
│   │   ├── parser.cpp               # 解析器实现
│   │   └── tokenizer.cpp            # 词法分析器实现
│   ├── tests/                      # 单元测试
│   │   ├── parser_test.cpp
│   │   └── tokenizer_test.cpp
│   ├── third_party/                 # 第三方依赖
│   │   └── rapidjson/               # JSON 解析库
│   └── CMakeLists.txt               # CMake 构建配置
│
├── Nexus-Android/                  # Android 应用
│   └── app/
│       └── src/main/
│           ├── cpp/                 # Native C++ 代码
│           │   ├── CMakeLists.txt    # Native 构建配置
│           │   └── native.cpp       # JNI Native 方法
│           ├── java/com/nexuscmd/   # Kotlin 源代码
│           │   ├── MainActivity.kt       # 主界面
│           │   ├── MainViewModel.kt      # 视图模型
│           │   ├── CommandHelper.kt      # Native 调用封装
│           │   ├── FloatingWindowService.kt # 悬浮窗服务
│           │   │
│           │   ├── data/             # 数据层
│           │   │   ├── CommandRepository.kt       # 命令仓库
│           │   │   ├── CommandChainRepository.kt  # 命令链仓库
│           │   │   ├── HistoryManager.kt           # 历史记录管理
│           │   │   ├── SettingsManager.kt          # 设置管理
│           │   │   ├── AddonManager.kt             # 拓展包管理
│           │   │   ├── TemplateGenerator.kt         # 模板生成器
│           │   │   │
│           │   │   └── libraries/      # ID 翻译库
│           │   │       ├── BlockLibrary.kt          # 方块库 (456个)
│           │   │       ├── ItemLibrary.kt           # 物品库 (336个)
│           │   │       ├── SoundEffectLibrary.kt    # 音效库 (918个)
│           │   │       ├── ParticleLibrary.kt       # 粒子库 (400+)
│           │   │       └── AnimationLibrary.kt      # 动画库 (200+)
│           │   │
│           │   └── ui/               # UI 层
│           │       ├── components/    # 可复用组件
│           │       │   ├── MCSyntaxHighlighter.kt   # 命令语法高亮器
│           │       │   ├── SyntaxHighlightEditor.kt # 高亮编辑器
│           │       │   ├── SyntaxHintOverlay.kt     # 语法提示浮层
│           │       │   └── GlassmorphicCard.kt      # 毛玻璃卡片
│           │       │
│           │       └── theme/         # 主题系统
│           │           ├── Color.kt    # 颜色定义
│           │           ├── Theme.kt   # 主题配置
│           │           └── Type.kt    # 字体配置
│           │
│           └── res/                   # 资源文件
│               ├── drawable/          # 可绘制资源
│               │   ├── completion_chip_bg.xml
│               │   ├── floating_collapsed_bg.xml
│               │   ├── floating_expanded_bg.xml
│               │   └── floating_input_bg.xml
│               │
│               ├── layout/             # XML 布局 (悬浮窗)
│               │   ├── completion_chip.xml
│               │   └── floating_window.xml
│               │
│               ├── mipmap-*/          # 应用图标
│               │   ├── ic_launcher.png
│               │   └── ic_launcher_round.png
│               │
│               └── values/            # 值资源
│                   └── themes.xml    # 主题配置
│
├── Nexus-Addon-Spec/              # 拓展包规范 (Git 子模块)
│   └── → https://github.com/NinefCJ/Nexus-Addon-Spec
│
├── Command/                        # 命令定义数据
│   ├── commands_list.txt          # 命令列表
│   ├── 默认命令库.json            # 默认命令库
│   ├── 方块状态包.json            # 方块状态定义
│   └── JSON Schema包.json         # JSON Schema
│
├── web/                           # GitHub Pages 网站
│   └── index.html                 # 项目展示页面
│
├── wiki/                          # Wiki 文档源文件
│   ├── Home.md                    # 首页
│   ├── Getting-Started.md         # 快速上手
│   ├── Architecture.md            # 架构说明
│   ├── Core-Modules.md            # 核心模块
│   ├── Android-App.md             # Android 应用
│   ├── Addon-System.md            # 拓展包系统
│   ├── Command-Data-Format.md     # 命令数据格式
│   ├── Theme-System.md            # 主题系统
│   ├── Performance.md             # 性能优化
│   ├── FAQ.md                     # 常见问题
│   └── Contributing.md            # 贡献指南
│
├── .github/                       # GitHub 配置
│   └── workflows/
│       └── deploy-pages.yml       # Pages 自动部署工作流
│
└── README.md                      # 项目说明文档

```

---

## 技术架构

### 架构分层

```
┌─────────────────────────────────────────────┐
│              Android UI Layer                │
│         (Jetpack Compose + Material3)        │
├─────────────────────────────────────────────┤
│           ViewModel Layer                   │
│        (StateFlow + Coroutines)             │
├─────────────────────────────────────────────┤
│           Repository Layer                  │
│   (CommandRepo, Settings, Addon Manager)    │
├─────────────────────────────────────────────┤
│           JNI Bridge Layer                 │
│      (Kotlin ←→ C++ Interop)               │
├─────────────────────────────────────────────┤
│         C++ Core Library                    │
│  (Tokenizer → Parser → Completion Engine)   │
├─────────────────────────────────────────────┤
│          Native Platform                    │
│       (Android NDK + CMake)                 │
└─────────────────────────────────────────────┘
```

### 技术栈

| 层级 | 技术 | 用途 |
|------|------|------|
| **UI** | Jetpack Compose | 现代声明式 UI |
| **主题** | Material3 | 设计系统 |
| **状态** | ViewModel + StateFlow | 响应式状态管理 |
| **并发** | Kotlin Coroutines | 异步操作 |
| **Native** | C++17 + JNI | 高性能命令处理 |
| **JSON** | RapidJSON | 高效 JSON 解析 |
| **构建** | CMake + Gradle | 跨平台构建 |

---

## 快速开始

### 环境要求

- **Android Studio** Hedgehog (2023.1.1) 或更高版本
- **Android SDK** API Level 33 (Android 13)
- **NDK** r25 或更高版本
- **CMake** 3.22 或更高版本
- **JDK** 17

### 构建步骤

#### 1. 克隆项目

```bash
git clone https://github.com/NinefCJ/Nexus.git
cd Nexus

# 初始化拓展包规范子模块（可选，用于拓展包开发）
git submodule update --init --recursive
```

#### 2. 配置 Android SDK

在 `Nexus-Android/local.properties` 中设置 SDK 路径：

```properties
sdk.dir=/path/to/android-sdk
```

#### 3. 构建 Debug 版本

```bash
cd Nexus-Android
./gradlew assembleDebug
```

构建产物位于：`app/build/outputs/apk/debug/app-debug.apk`

#### 4. 构建 Release 版本

```bash
./gradlew assembleRelease
```

需要配置签名密钥：

```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            keyAlias = "your-key-alias"
            keyPassword = "your-key-password"
            storeFile = file("your-keystore.jks")
            storePassword = "your-store-password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

#### 5. 构建 C++ 核心 (可选)

```bash
cd Nexus-Core
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Release
make -j$(nproc)
```

---

## 模块详解

### Android 模块

#### 1. MainActivity.kt (主界面)

**职责**：应用入口，管理主界面状态和导航

**核心组件**：
- 开屏动画 (NexusSplashScreen)
- 主界面 (MainScreen)
- 底部导航栏 (ModernBottomNavigation)
- 顶部应用栏 (ModernTopAppBar)

**页面结构**：
```
MainScreen
├── EditorTab          # 命令编辑器
├── CommandLibraryTab  # 命令库
├── QuickCommandsTab   # 快速命令
├── ResourceLibraryTab # 资源库
│   ├── BlockLibraryTab      # 方块库
│   ├── ItemLibraryTab       # 物品库
│   ├── SoundEffectLibraryTab # 音效库
│   ├── ParticleLibraryTab   # 粒子库
│   └── AnimationLibraryTab   # 动画库
├── HistoryTab         # 历史记录
└── SettingsTab       # 设置
```

#### 2. MainViewModel.kt (视图模型)

**职责**：状态管理、业务逻辑、Native 调用协调

**核心状态**：
```kotlin
data class MainUiState(
    val commandText: String = "",
    val cursorPosition: Int = 0,
    val validation: ValidationResult? = null,
    val completions: List<CompletionItem> = emptyList(),
    val syntaxHint: SyntaxHint? = null,
    val quickCommands: List<Triple<String, String, ImageVector>> = emptyList(),
    // ... 更多状态
)
```

**性能优化**：
- `@Stable` 注解标记状态类
- 补全结果缓存 (`completionCache`)
- 防抖延迟处理 (`COMPLETION_DEBOUNCE_MS = 150`)
- 并行 Native 调用 (`viewModelScope.async`)

#### 3. FloatingWindowService.kt (悬浮窗服务)

**职责**：在游戏中提供实时命令辅助

**功能**：
- 悬浮窗口显示/隐藏
- 命令输入和补全
- 快速复制到剪贴板
- 最小化/展开模式

#### 4. CommandHelper.kt (Native 封装)

**职责**：封装 JNI 调用，提供 Kotlin 友好的接口

**主要方法**：
```kotlin
class CommandHelper {
    fun initialize(packPath: String): Boolean
    fun getCompletions(command: String, cursor: Int): List<CompletionItem>
    fun validateCommand(command: String): ValidationResult
    fun getSyntaxHint(command: String, cursor: Int): SyntaxHint?
    fun dispose()
}
```

### 数据层模块

#### ID 翻译库 (Libraries)

所有 ID 库遵循统一的设计模式：

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
│  - filter(): List<T>                │
│  - buildCommand(): String            │
└─────────────────────────────────────┘
```

##### BlockLibrary.kt (方块库)

- **数据量**：456 个基岩版方块
- **分类**：16 个分类（自然、装饰、功能等）
- **命令生成**：`/setblock <x y z> <block> [blockstates] [options]`

##### ItemLibrary.kt (物品库)

- **数据量**：336 个基岩版物品
- **分类**：10 个分类（工具、武器、食物等）
- **命令生成**：`/give <player> <item> [amount] [data] [components]`

##### SoundEffectLibrary.kt (音效库)

- **数据量**：918 个基岩版音效
- **分类**：37 个分类（环境、生物、方块等）
- **命令生成**：`/playsound <sound> <target> [x y z] [volume] [pitch] [minimumVolume]`

##### ParticleLibrary.kt (粒子库)

- **数据量**：400+ 个基岩版粒子
- **分类**：9 个分类
- **命令生成**：`/particle <name> [pos] [delta] [speed] [count] [mode] [player] [params]`

##### AnimationLibrary.kt (动画库)

- **数据量**：200+ 个基岩版动画
- **分类**：9 个分类（玩家、生物、表情等）
- **命令生成**：`/playanimation <target> <animation> [nextState] [stopExpression] [controller] [blendOutTime]`

### C++ 核心模块

#### 1. Tokenizer (词法分析器)

**职责**：将命令字符串分解为 Token 序列

**Token 类型**：
- `COMMAND` - 命令 (以 `/` 开头)
- `SELECTOR` - 选择器 (`@s`, `@p`, `@a`, `@e`, `@r`)
- `COORDINATE` - 坐标 (`~`, `^`, 数字)
- `STRING` - 字符串 (引号包裹)
- `NUMBER` - 数字
- `IDENTIFIER` - 标识符
- `BRACKET` - 括号 (`[]`, `{}`)
- `OPERATOR` - 操作符

#### 2. Parser (解析器)

**职责**：分析 Token 序列，构建语法树

**支持格式**：
- 普通命令：`/give @p diamond_sword`
- 目标选择器：`/tp @e[type=creeper] ~ ~ ~`
- NBT 数据：`/summon zombie ~ ~ ~ {Health:20f}`
- 分数：`/scoreboard players operation @p temp * @p score`

#### 3. Completion Engine (补全引擎)

**职责**：根据上下文生成智能补全建议

**补全类型**：
- **命令补全** - 补全命令名称
- **参数补全** - 补全命令参数
- **ID 补全** - 补全方块/物品/粒子 ID
- **选择器补全** - 补全选择器和参数
- **语法模板** - 整句语法模板

**缓存优化**：
- `SyntaxTemplateCache` - 语法模板缓存
- LRU 策略，限制最大 50 条

#### 4. Command Registry (命令注册表)

**职责**：管理和查询所有可用命令

**数据结构**：
```cpp
struct CommandInfo {
    std::string name;
    std::string description;
    std::vector<Parameter> parameters;
    std::string syntax_template;
};
```

---

## 数据模型

### Kotlin 数据类

```kotlin
// 命令补全项
data class CompletionItem(
    val label: String,        // 显示标签
    val detail: String,        // 详情描述
    val insertText: String,    // 插入文本
    val kind: CompletionKind   // 补全类型
)

// 语法提示
data class SyntaxHint(
    val template: String,      // 语法模板
    val description: String,   // 描述
    val parameters: List<SyntaxParameter> // 参数列表
)

// 命令信息
data class CommandInfo(
    val name: String,
    val syntax: String,
    val description: String,
    val category: String,
    val icon: ImageVector
)

// 历史记录项
data class HistoryItem(
    val command: String,
    val timestamp: Long,
    val isSuccess: Boolean
)

// 收藏命令
data class SavedCommand(
    val id: String,
    val name: String,
    val command: String,
    val createdAt: Long
)
```

### C++ 结构体

```cpp
// 补全结果
struct CompletionResult {
    std::string label;
    std::string detail;
    std::string insert_text;
    CompletionKind kind;
};

// 语法模板
struct SyntaxTemplate {
    std::string name;
    std::string template_str;
    std::vector<ParameterInfo> params;
};

// Token 定义
struct Token {
    TokenType type;
    std::string value;
    int position;
};
```

---

## 性能优化

### Android 端优化

#### 1. Compose 优化

| 优化项 | 实现方式 | 效果 |
|--------|---------|------|
| `@Stable` 注解 | 标记稳定类型 | 减少不必要的重组 |
| `remember` 缓存 | 缓存计算结果 | 避免重复计算 |
| `key` 参数 | LazyList items key | 精准更新列表项 |
| 独立 Composable | 提取独立组件 | 缩小重组范围 |

#### 2. 状态管理优化

```kotlin
// 防抖处理
LaunchedEffect(searchQuery) {
    delay(150)  // 150ms 防抖
    performSearch(searchQuery)
}

// 缓存机制
private val completionCache = mutableMapOf<String, List<CompletionItem>>()

// 并行执行
val completionsDeferred = viewModelScope.async { helper.getCompletions(...) }
val validationDeferred = viewModelScope.async { helper.validateCommand(...) }
```

#### 3. 列表优化

```kotlin
// 使用 key 提升性能
LazyColumn {
    items(commands, key = { it.name }) { command ->
        CommandItem(command)
    }
}

// 缓存静态列表
val categories = remember { listOf("全部", "物品", "实体", ...) }
```

### C++ 端优化

#### 1. 缓存策略

```cpp
class SyntaxTemplateCache {
private:
    std::unordered_map<std::string, std::string> cache_;
    std::mutex mutex_;
    static constexpr size_t MAX_CACHE_SIZE = 50;
    
public:
    std::string getOrCompute(const std::string& key, 
                             std::function<std::string()> compute);
};
```

#### 2. 字符串处理优化

```cpp
// 使用 inline 优化字符串前缀检查
inline bool starts_with_fast(const std::string& str, 
                             const std::string& prefix) {
    if (prefix.size() > str.size()) return false;
    return str.compare(0, prefix.size(), prefix) == 0;
}
```

#### 3. 内存管理

```cpp
// 使用 unique_ptr 管理全局对象
std::unique_ptr<CommandRegistry> g_registry;

// ResultCache 减少重复计算
class ResultCache {
    std::unordered_map<std::string, std::string> cache_;
    static constexpr size_t MAX_CACHE_SIZE = 100;
};
```

---

## 主题系统

### 预设主题

| 主题 | 描述 | 适合场景 |
|------|------|---------|
| 跟随系统 | 自动适配系统深/浅色模式 | 日常使用 |
| 浅色模式 | 明亮清爽的白色主题 | 白天户外 |
| 深色模式 | 护眼的深色主题 | 夜间使用 |
| 午夜蓝 | 深蓝色调 | 游戏环境 |
| AMOLED 黑 | 纯黑色省电主题 | OLED 屏幕 |
| 绿色护眼 | 温和的绿色调 | 长时间阅读 |
| 海洋蓝 | 清新的蓝色调 | 休闲使用 |
| 暖橙色调 | 温暖的橙色调 | 温暖氛围 |

### 自定义主题

```kotlin
// 设置管理器
class SettingsManager {
    var currentTheme: AppTheme
    var cardOpacity: Float        // 卡片透明度 (0.0-1.0)
    var glassIntensity: Float     // 毛玻璃强度 (0.0-1.0)
    var cardCornerRadius: Float   // 圆角大小 (dp)
    var useGlassmorphism: Boolean // 是否启用毛玻璃
    var useCustomBackground: Boolean
    var customBackgroundUri: String?
    var backgroundOpacity: Float
}
```

### 毛玻璃效果

```kotlin
// 启用毛玻璃
val useGlassmorphism by remember { mutableStateOf(true) }
val glassIntensity by remember { mutableFloatStateOf(0.85f) }

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
)
```

---

## 贡献指南

### 开发规范

#### 1. 代码风格

- Kotlin：遵循 [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- C++：遵循 [Google C++ Style Guide](https://google.github.io/styleguide/cppguide.html)
- 缩进：4 空格
- 行长度：最多 120 字符

#### 2. 提交规范

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

**Type 类型**：
- `feat`: 新功能
- `fix`: 错误修复
- `docs`: 文档更新
- `style`: 代码格式
- `refactor`: 重构
- `perf`: 性能优化
- `test`: 测试
- `chore`: 构建/工具

**示例**：
```
feat(library): 添加动画库模块

- 添加 MCAnimation 数据类
- 实现 AnimationLibrary 过滤和搜索
- 添加 UI 组件和命令生成
- 性能优化：LRU 缓存和索引预计算

Closes #123
```

#### 3. 分支管理

```
main          # 主分支，稳定版本
├── develop   # 开发分支
│   ├── feature/animation-library  # 功能分支
│   ├── fix/theme-switch-bug       # 修复分支
│   └── refactor/completion-engine # 重构分支
```

### 添加新的 ID 库

1. 创建数据类：
```kotlin
data class NewItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val extra: String = ""
)
```

2. 创建库对象：
```kotlin
object NewItemLibrary {
    val categories = listOf("全部", "分类1", "分类2")
    
    private val filterCache = LinkedHashMap<String, List<NewItem>>()
    
    val items = listOf(
        NewItem("example:item", "示例物品", "分类1", "这是一个示例")
    )
    
    fun filter(query: String, category: String?): List<NewItem> {
        // 实现过滤逻辑
    }
    
    fun buildCommand(item: NewItem, vararg params: String): String {
        // 实现命令生成
    }
}
```

3. 添加 UI 组件：
```kotlin
@Composable
fun NewItemLibraryTab(viewModel: MainViewModel) {
    // 实现 UI
}
```

4. 集成到主界面：
```kotlin
// MainActivity.kt
val subTabs = listOf("方块", "物品", "音效", "粒子", "动画", "新资源")
// 添加对应的 when 分支
```

### 运行测试

```bash
# C++ 单元测试
cd Nexus-Core/build
ctest --output-on-failure

# Android 单元测试
cd Nexus-Android
./gradlew testDebugUnitTest
```

### 问题反馈

请通过 [GitHub Issues](https://github.com/NinefCJ/Nexus/issues) 反馈问题，包含：

1. 问题描述
2. 复现步骤
3. 预期行为
4. 截图/日志
5. 环境信息 (Android 版本, 应用版本)

---

## 版本历史

See [CHANGELOG.md](CHANGELOG.md) for detailed version history.

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 联系方式

- **作者**: NexusTeam(氿九Ninef)
- **邮箱**: 2395953343@qq.com
- **网站**: [https://ninefcj.github.io/Nexus/](https://ninefcj.github.io/Nexus/)
- **Wiki**: [https://github.com/NinefCJ/Nexus/wiki](https://github.com/NinefCJ/Nexus/wiki)

---

## 相关项目

- [Nexus-Addon-Spec](https://github.com/NinefCJ/Nexus-Addon-Spec) - 拓展包代码规范与指南（子模块）
  - 在线预览：[https://ninefcj.github.io/Nexus-Addon-Spec/](https://ninefcj.github.io/Nexus-Addon-Spec/)
- [CA_reforged](https://github.com/huangyxHUTAO/CA_reforged) - 命令助手社区版
- [CHelper](https://github.com/Yancey2023/CHelper-Core) - C++ 内核架构参考
- [Minecraft Wiki](https://zh.minecraft.wiki) - 命令语法与 ID 数据

---

<div align="center">

Made with ❤️ for Minecraft Community

</div>
