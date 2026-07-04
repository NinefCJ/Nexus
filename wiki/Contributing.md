# 贡献指南

感谢你考虑为 Nexus 贡献代码！本文档将帮助你了解项目的开发规范和贡献流程。

---

## 如何贡献

你可以通过以下方式参与项目：

| 贡献类型 | 说明 |
|---------|------|
| 🐛 报告 Bug | 提交 Issue 描述问题 |
| 💡 功能建议 | 提交 Issue 提出想法 |
| 📖 文档改进 | 修正 Wiki 或 README |
| 🔧 代码贡献 | 提交 Pull Request |
| 🎨 主题设计 | 设计新主题 |
| 📦 拓展包 | 创建并分享拓展包 |

---

## 代码风格

### Kotlin 规范

- 遵循 [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- 缩进：4 空格
- 行长度：最多 120 字符
- 使用有意义的命名，避免缩写（除了常见的如 `cmd`, `ctx`）

**命名约定**：
- 类名：`PascalCase` (如 `CommandHelper`, `MainViewModel`)
- 函数/变量：`camelCase` (如 `getCompletions`, `commandText`)
- 常量：`UPPER_SNAKE_CASE` (如 `MAX_CACHE_SIZE`)
- 伴生对象常量：大写

**示例**：

```kotlin
class CommandRepository(
    private val context: Context
) {
    private val commandCache = mutableMapOf<String, CommandDef>()

    fun findCommand(name: String): CommandDef? {
        return commandCache[name]
    }

    companion object {
        private const val TAG = "CommandRepository"
    }
}
```

### C++ 规范

- 遵循 [Google C++ Style Guide](https://google.github.io/styleguide/cppguide.html)
- 缩进：4 空格
- 行长度：最多 120 字符
- 所有代码位于 `mcmd` 命名空间内

**命名约定**：
- 类名：`PascalCase` (如 `Tokenizer`, `Completion`)
- 函数/方法：`camelCase` (如 `getCompletions`, `parseCommand`)
- 成员变量：`snake_case_` 带尾下划线 (如 `cache_`, `tokens_`)
- 常量：`k` + PascalCase (如 `kMaxCacheSize`)
- 枚举类：`PascalCase`

**示例**：

```cpp
namespace mcmd {

class Tokenizer {
public:
    explicit Tokenizer(std::string_view input);
    std::vector<Token> tokenize();

private:
    Token readCommand();
    Token readSelector();

    std::string_view input_;
    size_t pos_ = 0;
    static constexpr size_t kMaxTokens = 64;
};

} // namespace mcmd
```

### Git 提交规范

采用 Conventional Commits 规范：

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

**Type 类型**：

| 类型 | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `docs` | 文档更新 |
| `style` | 代码格式（不影响功能） |
| `refactor` | 重构（既不新增功能也不修复 bug） |
| `perf` | 性能优化 |
| `test` | 测试相关 |
| `chore` | 构建/工具/依赖更新 |

**Scope 范围**（可选）：
- `core` - C++ 核心库
- `android` - Android 应用
- `ui` - UI 相关
- `docs` - 文档
- `ci` - 持续集成

**示例**：

```
feat(core): add animation library support

- Add MCAnimation data class
- Implement animation ID completion
- Add unit tests for animation parsing

Closes #123
```

---

## 分支管理

```
main                # 主分支，稳定版本
└── develop         # 开发分支，集成最新功能
    ├── feature/xxx   # 功能分支
    ├── fix/xxx       # Bug 修复分支
    └── refactor/xxx  # 重构分支
```

### 工作流程

1. 从 `develop` 分支创建新分支
2. 在新分支上开发
3. 提交 PR 到 `develop` 分支
4. Code Review 通过后合并
5. Release 时从 `develop` 合并到 `main`

### 分支命名

```
feature/short-description   # 功能分支
fix/short-description       # 修复分支
refactor/short-description  # 重构分支
docs/short-description     # 文档分支
```

---

## Pull Request 流程

### 提交 PR 前检查清单

- [ ] 代码通过编译，无错误
- [ ] 所有现有测试通过
- [ ] 新功能添加了相应的测试
- [ ] 代码符合项目风格规范
- [ ] 更新了相关文档（README / Wiki）
- [ ] 提交信息符合规范

### PR 模板

```markdown
## 描述

简要描述这个 PR 做了什么。

## 类型

- [ ] Bug 修复
- [ ] 新功能
- [ ] 性能优化
- [ ] 代码重构
- [ ] 文档更新
- [ ] 其他

## 关联 Issue

Closes #123

## 测试

- [ ] 单元测试通过
- [ ] 手动测试验证
- [ ] 无需测试（说明原因）

## 截图（如适用）

```

---

## 添加新的 ID 库

如需新增一个资源库（如附魔、生物群系等），请遵循以下步骤：

### 步骤 1：创建数据类

```kotlin
data class BiomeItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String
)
```

### 步骤 2：创建库对象

```kotlin
object BiomeLibrary {
    val categories = listOf("全部", "森林", "沙漠", "海洋", ...)

    private val filterCache = LinkedHashMap<String, List<BiomeItem>>(
        50, 0.75f, true
    )

    val items = listOf(
        BiomeItem("minecraft:plains", "平原", "草原", "平坦的草原生物群系"),
        // ... 更多条目
    )

    fun filter(query: String, category: String?): List<BiomeItem> {
        // 实现过滤逻辑
    }

    fun buildCommand(item: BiomeItem): String {
        // 实现命令生成
    }
}
```

### 步骤 3：添加 UI 组件

```kotlin
@Composable
fun BiomeLibraryTab(viewModel: MainViewModel) {
    // 实现 UI
}
```

### 步骤 4：集成到主界面

在 `MainActivity.kt` 中添加对应的 Tab。

---

## 运行测试

### C++ 单元测试

```bash
cd Nexus-Core
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Debug
make -j$(nproc)
ctest --output-on-failure
```

### Android 单元测试

```bash
cd Nexus-Android
./gradlew testDebugUnitTest
```

### Android Instrumented 测试

```bash
./gradlew connectedDebugAndroidTest
```

---

## 报告问题

请通过 [GitHub Issues](https://github.com/NexusTeam/Nexus/issues) 提交 Bug 或功能建议。

### Bug 报告模板

```markdown
## 问题描述

清晰描述遇到的问题。

## 复现步骤

1. 打开应用
2. 点击...
3. 出现错误

## 预期行为

描述你期望发生什么。

## 实际行为

描述实际发生了什么。

## 截图/日志

如有，请附上截图或日志。

## 环境信息

- 应用版本：v1.3.0
- Android 版本：Android 13
- 设备型号：Pixel 6
```

### 功能建议模板

```markdown
## 功能描述

清晰描述你想要的功能。

## 使用场景

描述这个功能的使用场景。

## 建议方案

如果你有实现想法，请描述。

## 参考（可选）

类似功能的截图或链接。
```

---

## 开发环境设置

参考 [快速开始](Getting-Started) 文档配置开发环境。

### 推荐 IDE

| 平台 | IDE |
|------|-----|
| Android | Android Studio Hedgehog+ |
| C++ 核心 | CLion / VS Code |

### 推荐插件

- Kotlin 插件（Android Studio 自带）
- C/C++ 插件
- Markdown 插件
- .ignore 插件

---

## 社区行为准则

我们致力于打造一个开放、包容、友好的社区。参与项目时请遵守以下准则：

- 尊重他人，友善沟通
- 接受不同的观点和经验
- 对建设性的批评保持开放
- 以社区整体利益为重

---

## 联系方式

- **作者**：NexusTeam (氿九Ninef)
- **邮箱**：2395953343@qq.com
- **GitHub Issues**：[提交问题](https://github.com/NexusTeam/Nexus/issues)

---

## 致谢

感谢所有为 Nexus 做出贡献的开发者和用户！

Made with ❤️ for Minecraft Community
