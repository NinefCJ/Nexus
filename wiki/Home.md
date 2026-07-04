# Nexus - Minecraft 命令助手

> 一款专为 Minecraft 基岩版设计的跨平台命令辅助工具，提供智能语法提示、命令补全、ID 翻译库等强大功能。

---

## 📑 快速导航

| 分类 | 文档 | 说明 |
|------|------|------|
| 🚀 **入门** | [快速开始](Getting-Started) | 环境配置与构建指南 |
| 🏗️ **架构** | [技术架构](Architecture) | 整体架构与技术栈 |
| 📦 **核心模块** | [核心模块](Core-Modules) | C++ 核心库详解 |
| 📱 **Android** | [Android 应用](Android-App) | Android 端模块说明 |
| 📋 **数据格式** | [命令数据格式](Command-Data-Format) | JSON 命令库规范 |
| 🔌 **拓展** | [拓展包系统](Addon-System) | 自定义拓展包开发 |
| 🎨 **主题** | [主题系统](Theme-System) | 主题与毛玻璃效果 |
| ⚡ **性能** | [性能优化](Performance) | 性能调优指南 |
| 🤝 **贡献** | [贡献指南](Contributing) | 参与项目开发 |
| ❓ **FAQ** | [常见问题](FAQ) | 常见问题解答 |

---

## ✨ 核心特性

### 命令辅助
- **智能补全** — 实时语法提示，上下文感知的 ID 补全
- **语法高亮** — 命令结构彩色区分，一目了然
- **错误检测** — 实时语法校验，即时反馈问题位置
- **语法模板** — 55+ 命令模板，快速生成复杂命令

### 资源库
- **方块库** — 456 个基岩版方块完整中文翻译
- **物品库** — 336 个物品详细分类
- **音效库** — 918 个音效一键生成播放命令
- **粒子库** — 400+ 粒子效果参数说明
- **动画库** — 200+ 动画完整列表

### 界面体验
- **悬浮窗** — 游戏内实时调用，无需切换
- **8 种主题** — 跟随系统 / 浅色 / 深色 / 午夜蓝 等
- **毛玻璃 UI** — 现代磨砂玻璃视觉效果
- **自定义背景** — 支持自定义背景图片

---

## 📁 项目结构

```
Nexus/
├── Nexus-Core/          # C++ 原生核心库
│   ├── include/         # 头文件
│   ├── src/             # 源代码
│   ├── tests/           # 单元测试
│   └── third_party/     # 第三方依赖
│
├── Nexus-Android/       # Android 应用
│   └── app/src/main/
│       ├── cpp/         # JNI 桥接层
│       ├── java/        # Kotlin 源码
│       └── res/         # 资源文件
│
└── Command/             # 命令定义数据
    ├── 默认命令库.json
    ├── 方块状态包.json
    └── JSON Schema包.json
```

---

## 🛠️ 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| UI 框架 | Jetpack Compose | 2023.10.01 |
| 设计系统 | Material 3 | - |
| 状态管理 | ViewModel + StateFlow | 2.6.2 |
| 异步 | Kotlin Coroutines | - |
| Native | C++17 + JNI | - |
| JSON | RapidJSON | 1.1.0 |
| 构建 | CMake + Gradle | 3.22 / 8.2 |
| 测试 | GoogleTest + JUnit | 1.14.0 |

---

## 📥 快速开始

```bash
# 克隆项目
git clone https://github.com/NexusTeam/Nexus.git
cd Nexus

# 构建 Android 应用
cd Nexus-Android
./gradlew assembleDebug

# 构建 C++ 核心 (可选)
cd ../Nexus-Core
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Release
make -j$(nproc)
```

完整构建指南请参考 [快速开始](Getting-Started)。

---

## 🤝 社区与支持

- **GitHub Issues** — [提交 Bug 或功能建议](https://github.com/NexusTeam/Nexus/issues)
- **邮箱** — 2395953343@qq.com
- **作者** — NexusTeam (氿九Ninef)

---

<div align="center">

Made with ❤️ for Minecraft Community

</div>
