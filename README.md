# MCCommandHelper

Minecraft命令助手 - 仿照CHelper开发的跨平台命令辅助工具

## 项目结构

```
MCCommandHelper/
├── MCCommandHelper-Core/      # C++ 内核 (独立子模块)
├── MCCommandHelper-Android/  # Android 应用
└── resourcepack/             # JSON命令定义
```

## 核心功能

- 命令补全：实时语法提示和自动补全
- 语法高亮：命令结构用不同颜色区分
- 错误检测：实时检测命令语法错误
- 命令库：收藏常用命令

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

## 开发计划

- [x] 项目结构初始化
- [x] C++ 内核基础实现 (Tokenizer, Parser)
- [x] 命令补全引擎
- [x] Android 项目初始化
- [ ] Android JNI集成
- [ ] 完整UI开发
- [ ] 命令库功能
- [ ] 语法转换功能
