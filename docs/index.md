# Nexus - Minecraft 命令助手

> 一款专为 Minecraft 基岩版设计的命令辅助工具，提供智能语法提示、命令补全、ID翻译库等强大功能。

[Nexus](https://github.com/NinefCJ/Nexus) 是一款跨平台的 Minecraft 基岩版命令辅助工具，基于 [CHelper](https://github.com/Yancey2023/CHelper) 项目重构。

## 核心功能

- **命令补全**：实时语法提示和自动补全
- **语法高亮**：命令结构用不同颜色区分
- **错误检测**：实时检测命令语法错误
- **旧命令转换**：1.18 旧命令自动转换为 1.20+ 新命令
- **命令穷举**：批量生成参数组合命令
- **命令库**：本地/云端命令库管理
- **悬浮窗**：游戏中实时使用命令助手
- **主题系统**：多种预设主题 + 自定义背景

## 技术架构

```
┌─────────────────────────────────────────────┐
│              Android UI Layer                │
│         (Jetpack Compose + Material3)        │
├─────────────────────────────────────────────┤
│           ViewModel Layer                   │
│        (StateFlow + Coroutines)             │
├─────────────────────────────────────────────┤
│           JNI Bridge Layer                  │
│      (Kotlin ←→ C++ Interop)               │
├─────────────────────────────────────────────┤
│         C++ Core Library                    │
│  (Lexer → Parser → Completion Engine)       │
└─────────────────────────────────────────────┘
```

## 下载安装

Android 用户可以从 [GitHub Releases](https://github.com/NinefCJ/Nexus/releases) 下载最新版本的 APK 文件。

## 许可证

本项目采用 GPL-3.0 许可证。
