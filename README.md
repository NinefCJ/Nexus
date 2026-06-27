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

- 命令补全：实时语法提示和自动补全，支持智能上下文ID补全
- 语法高亮：命令结构用不同颜色区分
- 错误检测：实时检测命令语法错误
- ID翻译库：方块/物品/音效/粒子完整中文翻译
- 8种主题：跟随系统/浅色/深色/午夜/AMOLED黑/绿色护眼/海洋蓝/暖橙
- 悬浮窗：游戏中也能实时使用命令助手
- 拓展包系统：支持JSON格式自定义拓展包

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
- [x] 底部导航栏 (编辑器/模板/命令库/方块/物品/音效/粒子/历史/设置)
- [x] 快速命令面板
- [x] 命令语法验证
- [x] **方块库**: 456个基岩版方块ID与中文翻译，16个分类，支持搜索筛选
- [x] **物品库**: 336个基岩版物品ID与中文翻译，10个分类，支持搜索筛选
- [x] **音效库**: 918个基岩版音效ID，37个分类，支持/playsound命令生成
- [x] **粒子库**: 400+基岩版粒子ID，9个分类，支持/particle命令生成
- [x] **模板生成器**: 55+基岩版命令模板
- [x] **语法高亮**: 支持基岩版命令、教育版命令关键词着色
- [x] **命令库**: 80+基岩版命令，支持分类浏览和搜索
- [x] **历史记录**: 自动保存使用过的命令
- [x] **收藏功能**: 支持收藏常用命令
- [x] **8种主题**: 跟随系统/浅色/深色/午夜/AMOLED黑/绿色护眼/海洋蓝/暖橙
- [x] **快捷工具栏**: 选择器/坐标/符号快捷插入
- [x] **命令补全增强**: 智能上下文ID补全（方块/物品/音效/粒子）
- [x] **拓展包系统框架**: 支持JSON格式自定义拓展包

## 参考项目

本项目参考以下项目进行完善：
- [CA_reforged](https://github.com/huangyxHUTAO/CA_reforged) - 命令助手社区版，提供命令补全与ID翻译数据参考
- [CHelper](https://github.com/Yancey2023/CHelper-Core) - C++内核架构参考
- [中文 Minecraft Wiki](https://zh.minecraft.wiki) - 命令语法与ID数据参考

## 开发计划

- [ ] 无障碍服务集成（监听MC聊天自动粘贴）
- [ ] 方块状态参数支持
- [ ] 命令在线更新
- [ ] 拓展包UI管理页面
- [ ] 拓展包社区分享
- [ ] 多语言支持（英文）
