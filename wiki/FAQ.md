# 常见问题

本文档汇总了 Nexus 使用和开发中常见的问题及解答。

---

## 使用相关

### Q1: Nexus 支持哪些 Minecraft 版本？

Nexus 主要针对 **Minecraft 基岩版**（Bedrock Edition）设计，支持 1.19 及以上版本。Java 版的命令语法略有不同，部分命令可能不兼容。

### Q2: 悬浮窗如何开启？

1. 打开 Nexus 应用
2. 进入「设置」
3. 找到「悬浮窗」选项并开启
4. 在系统设置中授予悬浮窗权限
5. 开启后会显示一个悬浮按钮，点击即可展开

> **注意**：不同品牌手机的悬浮窗权限位置可能不同，请根据系统提示操作。

### Q3: 如何添加自定义命令？

有两种方式：

**方式一：拓展包（推荐）**
1. 创建 JSON 格式的拓展包
2. 在「设置」→「拓展包管理」中安装
3. 启用后即可使用自定义命令

**方式二：快速命令**
1. 在「快速命令」页面添加常用命令
2. 设置名称和命令内容
3. 一键插入到编辑器

### Q4: 命令补全不显示怎么办？

请检查以下几点：

1. **确认输入格式**：命令必须以 `/` 开头
2. **检查数据包**：确保默认命令库已加载
3. **重启应用**：有时重启可以解决临时问题
4. **清除缓存**：设置中清除应用数据后重试

如果以上方法都无效，请提交 Issue 反馈。

### Q5: 支持 iOS 吗？

目前仅支持 Android 平台。iOS 版本正在规划中，尚未有明确的发布时间。

### Q6: 如何导出/导入命令历史？

目前历史记录存储在应用本地，暂不支持直接导出导入。你可以通过复制单个命令来手动保存常用命令。

---

## 构建相关

### Q7: Android Studio 构建失败怎么办？

常见原因和解决方法：

**NDK 未安装**
```
错误：No version of NDK matched the requested version
```
解决：打开 SDK Manager → SDK Tools → 安装对应版本的 NDK

**Gradle 同步失败**
```bash
cd Nexus-Android
./gradlew clean
./gradlew --refresh-dependencies
```

**内存不足**
在 `gradle.properties` 中增加内存：
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
```

### Q8: 如何只构建特定 ABI？

修改 `app/build.gradle.kts`：

```kotlin
ndk {
    abiFilters += listOf("arm64-v8a")  // 只构建 64 位 ARM
}
```

这可以显著减少构建时间和 APK 体积。

### Q9: C++ 核心库如何单独测试？

```bash
cd Nexus-Core
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Debug
make -j$(nproc)
ctest --output-on-failure
```

---

## 开发相关

### Q10: 如何添加新命令到默认命令库？

1. 打开 `Command/默认命令库.json`
2. 在 `commands` 数组中添加命令定义
3. 遵循 [命令数据格式](Command-Data-Format) 规范
4. 提交 PR 到官方仓库

### Q11: 如何调试 JNI 代码？

在 Android Studio 中：

1. 选择 `Run → Edit Configurations`
2. 找到你的 app 配置
3. Debugger 选项卡选择 `Dual (Java + Native)`
4. 下断点后点击 Debug 按钮

### Q12: 性能优化从哪里入手？

建议的优先级：

1. **UI 卡顿** → 检查 Compose 重组次数
2. **补全慢** → 检查缓存命中率
3. **启动慢** → 延迟加载非核心数据
4. **内存高** → 检查图片和大对象

更多优化技巧请参考 [性能优化](Performance) 文档。

### Q13: 可以商用吗？

Nexus 采用 MIT 许可证，你可以：
- ✅ 商业使用
- ✅ 修改源码
- ✅ 分发
- ✅ 专利使用
- ✅ 私人使用

只需在你的项目中包含原作者的版权声明和许可证文本。

---

## 拓展包相关

### Q14: 拓展包支持哪些内容？

目前支持：
- 自定义命令定义
- 方块/物品/音效/粒子/动画 ID 库扩展
- 方块状态扩展
- JSON Schema 定义

### Q15: 拓展包会影响性能吗？

少量拓展包（<10 个）对性能影响很小。如果安装了大量拓展包（>50 个），可能会：
- 略微增加启动时间
- 增加内存占用
- 补全计算时间略有增加

建议只保留需要的拓展包。

### Q16: 拓展包冲突怎么办？

如果两个包定义了相同的命令：
- 优先级高的包会覆盖低的
- 可以在拓展包管理中调整优先级

---

## 主题相关

### Q17: 如何提交新主题？

欢迎提交新主题！步骤：
1. 在 `Color.kt` 中定义主题颜色
2. 在 `Theme.kt` 中添加 ColorScheme
3. 在 `AppTheme` 枚举中添加新项
4. 提交 PR 并附主题预览图

详见 [主题系统](Theme-System) 文档。

### Q18: 毛玻璃效果不显示？

毛玻璃效果在以下情况可能不生效：
- Android 版本过低（需要 Android 8+）
- GPU 不支持模糊
- 关闭了硬件加速

可以在设置中调整毛玻璃强度或关闭该效果。

---

## 其他

### Q19: 项目的开发计划是什么？

目前规划中的功能：
- [ ] iOS 版本
- [ ] 命令链可视化编辑
- [ ] 更多预设主题
- [ ] 云同步历史记录
- [ ] 插件系统

可以关注 GitHub Projects 页面了解最新进度。

### Q20: 如何支持项目？

你可以通过以下方式支持项目：

- **⭐ Star**：在 GitHub 上点 Star
- **🐛 反馈 Bug**：提交 Issue 帮助改进
- **💡 提建议**：分享你的想法
- **🔧 贡献代码**：提交 Pull Request
- **📦 制作拓展包**：分享给社区
- **📢 宣传**：推荐给朋友

---

## 没有找到答案？

如果以上 FAQ 没有解决你的问题：

1. 搜索 [GitHub Issues](https://github.com/NexusTeam/Nexus/issues) 看看是否有人问过
2. 查看项目 [Wiki](Home) 中的其他文档
3. 提交新的 Issue，详细描述你的问题

---

## 更多阅读

- [快速开始](Getting-Started) — 环境搭建指南
- [使用教程](Home) — 功能使用说明
- [贡献指南](Contributing) — 参与项目开发
