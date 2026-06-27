# Nexus

Minecraft命令助手 - 仿照CHelper开发的跨平台命令辅助工具

## 项目结构

```
Nexus/
├── Nexus-Core/      # C++ 内核 (独立子模块)
├── Nexus-Android/  # Android 应用
└── resourcepack/   # JSON命令定义
```

## 核心功能

- 命令补全：实时语法提示和自动补全
- 语法高亮：命令结构用不同颜色区分
- 错误检测：实时检测命令语法错误
- 命令库：收藏常用命令
- 悬浮窗：游戏中也能实时使用命令助手

## 技术栈

| 层级 | 技术 |
|------|------|
| 内核 | C++17, rapidjson, fmt |
| 移动端 | Kotlin, Jetpack Compose, JNI |
| 构建 | CMake, Gradle |

## 构建

### C++ 内核
```bash
cd Nexus-Core
mkdir build && cd build
cmake ..
make
```

### Android 应用
```bash
cd Nexus-Android
./gradlew assembleDebug
```

## 已完成功能

- [x] C++ 内核: Tokenizer, Parser, Completion引擎
- [x] Android 应用: Jetpack Compose 现代化UI
- [x] 悬浮窗服务: 支持游戏中实时命令补全
- [x] Material3 主题系统 (深色/浅色)
- [x] 底部导航栏 (编辑器/命令库/历史/设置)
- [x] 快速命令面板
- [x] 命令语法验证

## 开发计划

- [ ] 命令库收藏功能
- [ ] 历史记录保存
- [ ] 语法高亮增强
- [ ] 更多MC命令支持
- [ ] 语法转换功能 (旧→新)
