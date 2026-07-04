# 技术架构

Nexus 采用分层架构设计，以 C++ 核心库为基础，通过 JNI 桥接层为 Android 应用提供高性能的命令处理能力。

---

## 总体架构

```
┌─────────────────────────────────────────────────────┐
│                  Android UI Layer                    │
│            (Jetpack Compose + Material3)             │
├─────────────────────────────────────────────────────┤
│                 ViewModel Layer                      │
│              (StateFlow + Coroutines)                │
├─────────────────────────────────────────────────────┤
│                Repository Layer                      │
│      (CommandRepo / Settings / Addon Manager)        │
├─────────────────────────────────────────────────────┤
│                 JNI Bridge Layer                     │
│             (Kotlin ←→ C++ Interop)                  │
├─────────────────────────────────────────────────────┤
│               C++ Core Library                       │
│    (Tokenizer → Parser → Completion Engine)          │
├─────────────────────────────────────────────────────┤
│                Native Platform                       │
│            (Android NDK + CMake)                     │
└─────────────────────────────────────────────────────┘
```

---

## 各层职责

### 1. UI 层 (Jetpack Compose)

**职责**：呈现用户界面，处理用户交互

| 组件 | 作用 |
|------|------|
| `MainActivity.kt` | 应用入口，界面导航管理 |
| `SyntaxHighlightEditor.kt` | 语法高亮编辑器组件 |
| `MCSyntaxHighlighter.kt` | 命令语法高亮渲染 |
| 主题系统 (`Color.kt`/`Theme.kt`/`Type.kt`) | 视觉风格统一管理 |

**技术要点**：
- 声明式 UI，减少状态同步错误
- Material 3 设计系统
- 组合式布局，组件复用性高

### 2. ViewModel 层

**职责**：状态管理、业务逻辑协调、异步处理

| 组件 | 作用 |
|------|------|
| `MainViewModel.kt` | 全局状态容器，业务逻辑编排 |
| StateFlow | 响应式状态流，驱动 UI 更新 |
| Coroutines | 异步任务调度，避免阻塞主线程 |

**关键特性**：
- 防抖处理 (150ms) — 优化输入体验
- 结果缓存 — 减少重复计算
- 并行 Native 调用 — 提升响应速度

### 3. Repository 层

**职责**：数据访问抽象，统一数据来源

| 模块 | 功能 |
|------|------|
| `CommandRepository.kt` | 命令数据加载与查询 |
| `CommandChainRepository.kt` | 命令链数据管理 |
| `HistoryManager.kt` | 历史记录持久化 |
| `SettingsManager.kt` | 设置项存取 |
| `AddonManager.kt` | 拓展包加载与管理 |
| `TemplateGenerator.kt` | 命令模板生成 |
| 五大 ID 库 | 方块/物品/音效/粒子/动画数据 |

### 4. JNI 桥接层

**职责**：Kotlin 与 C++ 之间的类型转换和方法映射

| 文件 | 作用 |
|------|------|
| `CommandHelper.kt` | Kotlin 端封装，提供友好接口 |
| `native.cpp` | C++ 端 JNI 实现 |
| `command_helper_jni.hpp/cpp` | JNI 接口声明与实现 |

**性能考量**：
- 批量数据传递，减少 JNI 调用次数
- 字符串预分配，避免频繁拷贝
- 缓存 JNI 类引用和方法 ID

### 5. C++ 核心层

**职责**：高性能命令解析、补全、高亮计算

```
输入字符串
    ↓
┌───────────┐
│ Tokenizer │  →  Token 序列
└───────────┘
    ↓
┌───────────┐
│  Parser   │  →  语法树 (AST)
└───────────┘
    ↓
┌─────────────────┐
│  Completion     │  →  补全建议列表
│  Engine         │  →  语法模板提示
└─────────────────┘
    ↓
┌───────────┐
│ Highlighter│ →  高亮 Token 序列
└───────────┘
```

| 模块 | 作用 |
|------|------|
| `Tokenizer` | 词法分析，将字符串分解为 Token |
| `Parser` | 语法分析，构建抽象语法树 |
| `Completion` | 智能补全，根据上下文生成建议 |
| `Highlighter` | 语法高亮计算 |
| `CommandRegistry` | 命令定义注册与查询 |

### 6. 原生平台层

**职责**：操作系统交互，编译与构建

- Android NDK — C/C++ 编译工具链
- CMake — 跨平台构建系统
- 标准 C++17 库

---

## 数据流

### 命令补全流程

```
用户输入
   ↓
EditText  onChange
   ↓
ViewModel.updateCommandText()
   ↓
防抖 150ms
   ↓
  ┌──────────────────────────────────┐
  │ 并行调用 Native                   │
  │  ├─ getCompletions()             │
  │  ├─ validateCommand()            │
  │  └─ getSyntaxHint()              │
  └──────────────────────────────────┘
   ↓
结果合并到 MainUiState
   ↓
StateFlow 发出新状态
   ↓
Compose 重组，更新 UI
```

### 语法高亮流程

```
命令文本变化
   ↓
MCSyntaxHighlighter.highlight()
   ↓
调用 Native getHighlightTokens()
   ↓
生成 AnnotatedString
   ↓
TextField 渲染带样式文本
```

---

## 技术选型理由

| 技术 | 选型理由 |
|------|----------|
| **Jetpack Compose** | 现代声明式 UI，开发效率高，状态管理清晰 |
| **C++ 核心库** | 高性能词法/语法分析，比纯 Kotlin 快 5-10 倍 |
| **JNI 桥接** | 兼顾性能与开发效率，核心计算用 C++，UI 用 Kotlin |
| **RapidJSON** | 高性能 JSON 解析，适合移动端资源受限环境 |
| **ViewModel + StateFlow** | 官方推荐架构，生命周期安全，响应式编程 |
| **Material 3** | Google 最新设计系统，主题化能力强 |
| **CMake** | 跨平台构建，便于未来扩展到 iOS/桌面端 |

---

## 设计原则

### 1. 单一职责
每个模块只做一件事，如 Tokenizer 只负责词法分析，不涉及补全逻辑。

### 2. 分层解耦
UI 层不直接操作 C++ 核心，通过 ViewModel + Repository + JNI 逐层调用。

### 3. 性能优先
核心计算路径使用 C++ 实现，关键路径使用缓存和防抖优化。

### 4. 可扩展性
- 命令数据通过 JSON 配置，无需改代码即可新增命令
- 拓展包系统支持第三方扩展
- ID 库遵循统一模式，新增资源库只需遵循约定

### 5. 可测试性
- C++ 核心有独立单元测试
- 业务逻辑集中在 ViewModel，便于单元测试
- Repository 层抽象数据来源，可 Mock

---

## 跨平台规划

当前只支持 Android，但架构设计预留了跨平台能力：

```
        ┌───────────────┐
        │   Nexus-Core  │       ← 可复用 C++ 核心
        └───────┬───────┘
                │
    ┌───────────┼───────────┐
    ▼           ▼           ▼
 Android       iOS       Desktop
  (JNI)     (Objective-C++) (Qt/...)
```

- C++ 核心库完全独立于平台
- JNI 层是 Android 特有，iOS 可替换为 Objective-C++ 桥接
- 数据格式统一，各平台共享命令数据包

---

## 更多阅读

- [核心模块详解](Core-Modules) — C++ 各模块深入解析
- [Android 应用](Android-App) — Android 端架构与组件
- [性能优化](Performance) — 性能调优细节
