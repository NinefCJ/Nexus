# Nexus - Minecraft 基岩版命令助手

> 一款专为 Minecraft 基岩版设计的命令辅助工具，提供智能语法提示、命令补全、ID翻译库等强大功能。

[English](README-en.md) | 简体中文

---

## 目录

- [项目概述](#项目概述)
- [项目结构](#项目结构)
- [技术架构](#技术架构)
- [快速开始](#快速开始)
- [功能特性](#功能特性)
- [贡献指南](#贡献指南)

---

## 项目概述

Nexus 是一款跨平台的 Minecraft 基岩版命令辅助工具，基于 [CHelper](https://github.com/Yancey2023/CHelper) 项目重构，主要功能包括：

### 核心功能

| 功能 | 描述 |
|------|------|
| **命令补全** | 实时语法提示和自动补全，支持智能上下文ID补全 |
| **语法高亮** | 命令结构用不同颜色区分，可读性更强 |
| **错误检测** | 实时检测命令语法错误，即时反馈 |
| **参数提示** | 显示命令参数说明和语法模板 |
| **旧命令转换** | 1.18 旧命令自动转换为 1.20+ 新命令 |
| **命令穷举** | 批量生成参数组合命令 |
| **命令库** | 本地/云端命令库管理 |
| **悬浮窗** | 游戏中也能实时使用命令助手 |
| **输入法模式** | 命令转换输入法，输入时自动转换 |
| **主题系统** | 多种预设主题 + 自定义背景图片 |
| **Raw JSON** | JSON 文本生成器，支持颜色和格式 |

---

## 项目结构

```
Nexus/
├── Nexus-Core/                    # C++ 原生核心库 (跨平台基础)
│   ├── include/                   # 头文件目录
│   ├── src/                       # 源代码目录
│   │   ├── chelper/               # 核心模块
│   │   │   ├── auto_suggestion/   # 自动补全
│   │   │   ├── command_structure/ # 命令结构
│   │   │   ├── lexer/             # 词法分析器
│   │   │   ├── linter/            # 语法检查
│   │   │   ├── node/              # AST 节点
│   │   │   ├── old2new/           # 旧命令转新命令
│   │   │   ├── parameter_hint/    # 参数提示
│   │   │   ├── parser/            # 语法解析器
│   │   │   ├── resources/         # 资源管理
│   │   │   ├── serialization/     # 序列化
│   │   │   ├── syntax_highlight/  # 语法高亮
│   │   │   └── util/              # 工具函数
│   │   └── apps/                  # 应用入口
│   │       └── NexusAndroid.cpp   # Android JNI 入口
│   ├── tests/                     # 单元测试
│   ├── 3rdparty/                  # 第三方依赖
│   └── CMakeLists.txt             # CMake 构建配置
│
├── Nexus-Android/                  # Android 应用
│   ├── app/
│   │   ├── libs/                  # 预编译原生库
│   │   │   └── arm64-v8a/
│   │   │       └── libNexusAndroid.so
│   │   └── src/main/
│   │       ├── assets/            # 资源包
│   │       │   ├── cpack/         # 命令资源包
│   │       │   ├── old2new/       # 旧命令转换数据
│   │       │   └── about/         # 关于页面文本
│   │       ├── java/com/nexuscmd/ # Kotlin 源代码
│   │       │   ├── android/       # Android 相关
│   │       │   │   ├── activity/  # 活动
│   │       │   │   ├── service/   # 服务
│   │       │   │   ├── util/      # 工具
│   │       │   │   └── window/    # 悬浮窗管理
│   │       │   ├── core/          # 核心模块
│   │       │   ├── data/          # 数据层
│   │       │   ├── network/       # 网络模块
│   │       │   └── ui/            # UI 层
│   │       │       ├── about/     # 关于页面
│   │       │       ├── completion/# 命令补全
│   │       │       ├── enumeration/# 穷举功能
│   │       │       ├── home/      # 首页
│   │       │       ├── library/   # 命令库
│   │       │       ├── loongflow/ # 游龙流编辑器
│   │       │       ├── old2new/   # 旧命令转换
│   │       │       ├── rawtext/   # Raw JSON
│   │       │       ├── settings/  # 设置
│   │       │       └── common/    # 公共组件
│   │       └── res/               # 资源文件
│   ├── gradle/                    # Gradle 配置
│   ├── build.gradle.kts           # 构建配置
│   └── settings.gradle.kts        # 项目配置
│
├── Command/                        # 命令定义数据
│   ├── commands_list.txt          # 命令列表
│   ├── 默认命令库.json            # 默认命令库
│   ├── 方块状态包.json            # 方块状态定义
│   └── JSON Schema包.json         # JSON Schema
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
│   (LocalCommandLab, Settings, Background)   │
├─────────────────────────────────────────────┤
│           JNI Bridge Layer                 │
│      (Kotlin ←→ C++ Interop)               │
├─────────────────────────────────────────────┤
│         C++ Core Library                    │
│  (Lexer → Parser → Completion Engine)       │
├─────────────────────────────────────────────┤
│          Native Platform                    │
│       (Android NDK + CMake)                 │
└─────────────────────────────────────────────┘
```

### 技术栈

| 层级 | 技术 | 用途 |
|------|------|------|
| **UI** | Jetpack Compose | 现代声明式 UI |
| **导航** | Navigation Compose | 导航管理 |
| **状态** | ViewModel + StateFlow | 响应式状态管理 |
| **数据存储** | DataStore | 键值对存储 |
| **图片加载** | Coil | 图片加载 |
| **网络** | OkHttp + Retrofit | HTTP 请求 |
| **序列化** | Kotlinx Serialization | JSON 序列化 |
| **Native** | C++17 + JNI | 高性能命令处理 |
| **构建** | CMake + Gradle | 跨平台构建 |

---

## 快速开始

### 环境要求

- **Android Studio** Hedgehog (2023.1.1) 或更高版本
- **Android SDK** API Level 24+
- **NDK** r25 或更高版本
- **CMake** 3.22 或更高版本
- **JDK** 17

### 构建步骤

#### 1. 克隆项目

```bash
git clone https://github.com/NinefCJ/Nexus.git
cd Nexus
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

需要配置签名密钥（创建 `keystore.properties`）：

```properties
keyAlias=your-key-alias
keyPassword=your-key-password
storeFile=your-keystore.jks
storePassword=your-store-password
```

---

## 功能特性

### 命令补全

- **实时补全**：输入时即时提供补全建议
- **智能上下文**：根据光标位置提供精准补全
- **语法高亮**：不同类型的 token 使用不同颜色
- **错误检测**：实时检测语法错误并提示原因

### 旧命令转换

- **1.18 → 1.20+**：自动转换旧命令为新语法
- **方块数据值转状态**：自动映射方块数据值
- **execute 命令转换**：转换 execute 旧语法
- **输入法模式**：输入时自动转换，无需手动操作

### 悬浮窗模式

- **游戏中使用**：无需退出游戏即可使用
- **快速复制**：一键复制命令到剪贴板
- **最小化**：不使用时缩小为悬浮图标
- **透明度调节**：可调节图标和界面透明度

### 命令库

- **本地命令库**：创建和管理本地命令集合
- **云端命令库**：浏览和下载社区分享的命令
- **上传分享**：分享自己的命令给其他用户
- **搜索功能**：快速搜索命令库

### 游龙流编辑器

- **可视化编程**：用流程图方式创建命令链
- **条件分支**：支持 if-else 条件判断
- **循环结构**：支持循环执行
- **导出命令**：一键导出为命令字符串

### 主题系统

- **预设主题**：多种内置主题可选
- **自定义背景**：从相册导入背景图片
- **毛玻璃效果**：现代磨砂玻璃视觉效果

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
feat(completion): 添加 /hud 指令补全

- 添加 HUD 元素列表
- 支持 hide/reset 操作
- 更新快捷命令库
```

#### 3. 分支管理

```
main          # 主分支，稳定版本
├── develop   # 开发分支
│   ├── feature/new-command  # 功能分支
│   ├── fix/bug-fix          # 修复分支
│   └── refactor/code-cleanup # 重构分支
```

### 运行测试

```bash
# C++ 单元测试
cd Nexus-Core
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Release
make -j$(nproc)
ctest --output-on-failure

# Android 单元测试
cd Nexus-Android
./gradlew testDebugUnitTest

# Android 构建
./gradlew assembleDebug
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

### v1.4.0 (2026-07-07)

- 基于 CHelper 重构，核心功能全面升级
- 全新命令补全引擎，性能大幅提升
- 添加旧命令转新命令功能（含输入法模式）
- 添加命令穷举功能
- 添加命令库（本地+云端）
- 添加游龙流可视化编辑器
- 添加 Raw JSON 文本生成器
- 优化悬浮窗体验

### v1.3.0

- 完善音效库和动画库
- 添加音效试听功能
- 添加 /playanimation 和 /hud 指令支持
- 优化 UI 交互

---

## 许可证

本项目采用 GPL-3.0 许可证 - 详见 [LICENSE](LICENSE) 文件

## 参考项目

- [CHelper](https://github.com/Yancey2023/CHelper) - C++ 内核架构参考
- [Minecraft Wiki](https://zh.minecraft.wiki) - 命令语法与 ID 数据

## 联系方式

- **作者**: NexusTeam (氿九Ninef)
- **邮箱**: 2395953343@qq.com

---

<div align="center">

Made with ❤️ for Minecraft Community

</div>
