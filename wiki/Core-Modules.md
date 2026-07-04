# 核心模块详解

Nexus-Core 是项目的 C++ 核心库，提供命令词法分析、语法解析、智能补全和语法高亮等核心功能。

---

## 模块总览

```
Nexus-Core/
├── include/
│   ├── types.hpp              # 类型定义
│   ├── tokenizer.hpp          # 词法分析器
│   ├── parser.hpp             # 语法解析器
│   ├── completion.hpp         # 补全引擎
│   ├── highlighter.hpp        # 语法高亮
│   ├── command_registry.hpp   # 命令注册表
│   └── command_helper_jni.hpp # JNI 接口
└── src/
    ├── tokenizer.cpp
    ├── parser.cpp
    ├── completion.cpp
    ├── highlighter.cpp
    ├── command_registry.cpp
    └── command_helper_jni.cpp
```

所有核心类都位于 `mcmd` 命名空间中。

---

## 1. 类型定义 (types.hpp)

定义了核心库使用的所有公共类型。

### TokenType 枚举

| 类型 | 说明 | 示例 |
|------|------|------|
| `Command` | 命令 (以 / 开头) | `/give`, `/tp` |
| `Selector` | 目标选择器 | `@p`, `@a`, `@e` |
| `String` | 引号字符串 | `"hello world"` |
| `Number` | 整数或小数 | `64`, `1.5` |
| `Coordinates` | 坐标 | `~`, `^`, `100` |
| `ItemId` | 物品/方块 ID | `minecraft:stone` |
| `Option` | 选项参数 | `-1` |
| `Bracket` | 括号 | `[`, `]`, `{`, `}` |
| `Error` | 错误 Token | - |

### Token 结构

```cpp
struct Token {
    TokenType type;       // Token 类型
    std::string value;    // Token 文本值
    size_t position;      // 起始位置 (偏移量)
    size_t length;        // 长度
};
```

### ParamType 枚举

参数类型，用于命令定义：

| 类型 | 说明 |
|------|------|
| `Selector` | 目标选择器 |
| `ItemId` | 物品/方块 ID |
| `Integer` | 整数 |
| `Float` | 浮点数 |
| `String` | 字符串 |
| `Coordinates` | 坐标 |
| `Boolean` | 布尔值 |
| `BlockState` | 方块状态 |
| `Component` | 组件 (NBT) |
| `Custom` | 自定义类型 |

### ParamDef 结构

```cpp
struct ParamDef {
    std::string name;                    // 参数名
    ParamType type;                      // 参数类型
    std::string description;             // 描述
    bool required;                       // 是否必填
    std::optional<std::string> default_value;  // 默认值
    std::vector<std::string> suggestions;      // 建议值列表
};
```

### CommandDef 结构

```cpp
struct CommandDef {
    std::string name;                  // 命令名 (不含 /)
    std::string syntax;                // 语法模板字符串
    std::string description;           // 命令描述
    std::vector<ParamDef> params;      // 参数列表
};
```

### CompletionItem 结构

```cpp
struct CompletionItem {
    std::string label;            // 显示标签
    std::string detail;           // 详情描述
    std::string insert_text;      // 插入文本
    enum class Kind { 
        Command, Selector, Parameter, Value 
    } kind;
};
```

### ParseError 结构

```cpp
struct ParseError {
    std::string message;     // 错误信息
    size_t position;         // 错误位置
    size_t length;           // 错误长度
};
```

---

## 2. 词法分析器 (Tokenizer)

**头文件**：`include/tokenizer.hpp`
**实现**：`src/tokenizer.cpp`

### 功能
将输入的命令字符串分解为 Token 序列，为后续语法分析做准备。

### 类定义

```cpp
class Tokenizer {
public:
    explicit Tokenizer(std::string_view input);
    std::vector<Token> tokenize();
    const std::optional<ParseError>& error() const;
};
```

### 使用示例

```cpp
mcmd::Tokenizer tokenizer("/give @p diamond_sword 64");
auto tokens = tokenizer.tokenize();

if (tokenizer.error()) {
    // 处理词法错误
    std::cerr << tokenizer.error()->message << std::endl;
}

for (const auto& token : tokens) {
    std::cout << "[" << static_cast<int>(token.type) << "] " 
              << token.value << " @ " << token.position << std::endl;
}
```

### 词法规则

**命令识别**：
- 以 `/` 开头的单词识别为 Command
- 命令名只包含小写字母和下划线

**选择器识别**：
- `@` + 单个字母 (`p`, `a`, `e`, `s`, `r`)
- 后面可跟方括号参数，如 `@e[type=creeper]`

**坐标识别**：
- `~` 相对坐标
- `^` 局部坐标
- 纯数字也可能是坐标（由上下文决定）

**字符串识别**：
- 双引号 `"..."` 包裹
- 支持转义字符 `\"`

**数字识别**：
- 整数：`123`, `-45`
- 浮点数：`1.5`, `-.5`, `3.`

---

## 3. 语法解析器 (Parser)

**头文件**：`include/parser.hpp`
**实现**：`src/parser.cpp`

### 功能
对 Token 序列进行语法分析，构建抽象语法树 (AST)，并检测语法错误。

### AST 节点类型

```cpp
enum class NodeType {
    Root,         // 根节点
    Command,      // 命令节点
    Parameter,    // 参数节点
    Selector,     // 选择器节点
    Coordinates   // 坐标节点
};

struct AstNode {
    NodeType type;
    std::string value;
    size_t token_index;
    std::vector<std::shared_ptr<AstNode>> children;
};
```

### 类定义

```cpp
class Parser {
public:
    explicit Parser(const std::vector<Token>& tokens);
    std::shared_ptr<AstNode> parse();
    const std::optional<ParseError>& error() const;
};
```

### 使用示例

```cpp
mcmd::Tokenizer tokenizer(input);
auto tokens = tokenizer.tokenize();

mcmd::Parser parser(tokens);
auto ast = parser.parse();

if (parser.error()) {
    // 语法错误
    auto& err = *parser.error();
    highlight_error(err.position, err.length);
}
```

### 支持的语法格式

| 格式 | 示例 |
|------|------|
| 简单命令 | `/give @p diamond_sword` |
| 目标选择器 | `/tp @e[type=creeper] ~ ~1 ~` |
| NBT 数据 | `/summon zombie ~ ~ ~ {Health:20f}` |
| 分数操作 | `/scoreboard players operation @p temp *= @p score` |
| 方块状态 | `/setblock ~ ~ ~ stone[variant=granite]` |

---

## 4. 补全引擎 (Completion)

**头文件**：`include/completion.hpp`
**实现**：`src/completion.cpp`

### 功能
根据当前输入和光标位置，智能生成补全建议列表。

### 类定义

```cpp
class Completion {
public:
    Completion(const CommandRegistry& registry);

    // 获取光标位置的补全建议
    std::vector<CompletionItem> getCompletions(
        const std::vector<Token>& tokens,
        size_t cursor_position,
        const std::string& partial_input
    );

    // 获取语法模板提示
    SyntaxTemplate getSyntaxTemplate(
        const std::string& input, 
        size_t cursor_pos
    );

    // ... 更多方法
};
```

### SyntaxTemplate 结构

```cpp
struct SyntaxTemplate {
    std::string template_str;       // 完整模板，如 "/give <targets> <item> [amount]"
    size_t active_param_start;      // 当前参数起始位置
    size_t active_param_end;        // 当前参数结束位置
    size_t active_param_index;      // 当前参数索引 (0-based)
    std::string active_param_name;  // 当前参数名
    std::string active_param_hint;  // 当前参数提示
    bool is_optional;               // 是否为可选参数
};
```

### 补全类型

| 类型 | 触发场景 | 示例 |
|------|----------|------|
| 命令补全 | 输入 `/` 后 | 输入 `/gi` → 建议 `give` |
| 参数补全 | 输入命令后 | 输入 `/give ` → 建议 `@p`, `@a`, ... |
| ID 补全 | 物品/方块参数 | 输入 `diamo` → 建议 `diamond`, `diamond_sword` |
| 选择器补全 | 选择器参数 | 输入 `@e[t` → 建议 `type=...` |
| 坐标补全 | 坐标位置 | 输入 `~` → 建议 `~ ~ ~` |

### 补全流程

```
输入 + 光标位置
     ↓
确定当前参数位置
     ↓
查询命令定义
     ↓
确定参数类型
     ↓
  ┌─────────┴─────────┐
  ▼                   ▼
枚举型参数        自由型参数
(从建议列表过滤)   (从 ID 库匹配)
  └─────────┬─────────┘
            ↓
     排序 & 截取 Top N
            ↓
     返回补全列表
```

---

## 5. 语法高亮 (Highlighter)

**头文件**：`include/highlighter.hpp`
**实现**：`src/highlighter.cpp`

### 功能
根据 Token 类型生成高亮信息，用于 UI 层渲染彩色命令文本。

### 类定义

```cpp
class Highlighter {
public:
    std::vector<HighlightToken> highlight(const std::vector<Token>& tokens);
};
```

### HighlightToken 结构

```cpp
struct HighlightToken {
    TokenType type;    // 用于确定颜色
    size_t start;      // 起始偏移
    size_t end;        // 结束偏移
};
```

### 颜色映射 (UI 层)

| Token 类型 | 颜色 | 说明 |
|-----------|------|------|
| Command | 紫色 / #BB86FC | 命令名称 |
| Selector | 青色 / #03DAC6 | 目标选择器 |
| String | 绿色 / #4CAF50 | 字符串 |
| Number | 橙色 / #FF9800 | 数字 |
| Coordinates | 蓝色 / #2196F3 | 坐标 |
| ItemId | 粉色 / #E91E63 | 物品/方块 ID |
| Error | 红色 / #F44336 红色波浪线 | 错误 |

---

## 6. 命令注册表 (CommandRegistry)

**头文件**：`include/command_registry.hpp`
**实现**：`src/command_registry.cpp`

### 功能
管理所有可用命令的定义，提供查询接口。

### 类定义

```cpp
class CommandRegistry {
public:
    CommandRegistry();

    // 从 JSON 加载命令定义
    bool loadFromJson(const std::string& json_content);
    bool loadFromFile(const std::string& file_path);

    // 查找命令定义
    const CommandDef* findCommand(const std::string& name) const;

    // 获取所有命令名
    std::vector<std::string> getCommandNames() const;

    // 前缀匹配 (用于补全)
    std::vector<const CommandDef*> getCommandsStartingWith(
        const std::string& prefix
    ) const;
};
```

### JSON 命令定义格式

```json
{
  "commands": [
    {
      "name": "give",
      "description": "给予玩家物品",
      "syntax": "/give <targets> <item> [amount] [data]",
      "params": [
        {
          "name": "targets",
          "type": "Selector",
          "description": "目标玩家",
          "required": true
        },
        {
          "name": "item",
          "type": "ItemId",
          "description": "物品 ID",
          "required": true
        },
        {
          "name": "amount",
          "type": "Integer",
          "description": "数量",
          "required": false,
          "default": "1"
        }
      ]
    }
  ]
}
```

---

## 7. JNI 接口 (command_helper_jni)

**头文件**：`include/command_helper_jni.hpp`
**实现**：`src/command_helper_jni.cpp`

### 功能
提供 Java/Kotlin 调用 C++ 核心的 JNI 桥接接口。

### 主要 JNI 方法

| Java 方法 | C++ 实现 | 功能 |
|-----------|----------|------|
| `nativeInit` | `Java_com_nexuscmd_CommandHelper_nativeInit` | 初始化核心库 |
| `nativeGetCompletions` | `Java_com_nexuscmd_CommandHelper_nativeGetCompletions` | 获取补全建议 |
| `nativeValidate` | `Java_com_nexuscmd_CommandHelper_nativeValidate` | 验证命令 |
| `nativeGetSyntaxHint` | `Java_com_nexuscmd_CommandHelper_nativeGetSyntaxHint` | 获取语法提示 |
| `nativeGetHighlights` | `Java_com_nexuscmd_CommandHelper_nativeGetHighlights` | 获取高亮信息 |
| `nativeDispose` | `Java_com_nexuscmd_CommandHelper_nativeDispose` | 释放资源 |

### 使用模式

```kotlin
// Kotlin 端
class CommandHelper {
    private external fun nativeInit(packPath: String): Boolean
    private external fun nativeGetCompletions(cmd: String, cursor: Int): Array<CompletionItem>
    // ...
    
    fun initialize(packPath: String): Boolean {
        return nativeInit(packPath)
    }
    
    fun getCompletions(command: String, cursor: Int): List<CompletionItem> {
        return nativeGetCompletions(command, cursor).toList()
    }
}
```

---

## 单元测试

核心库使用 GoogleTest 框架进行单元测试。

### 测试文件

| 文件 | 测试内容 |
|------|----------|
| `tests/tokenizer_test.cpp` | 词法分析器测试 |
| `tests/parser_test.cpp` | 语法解析器测试 |

### 运行测试

```bash
cd Nexus-Core/build
cmake .. -DCMAKE_BUILD_TYPE=Debug
make -j$(nproc)
ctest --output-on-failure
```

### 添加新测试

```cpp
#include <gtest/gtest.h>
#include "tokenizer.hpp"

TEST(TokenizerTest, SimpleCommand) {
    mcmd::Tokenizer tokenizer("/give @p stone 64");
    auto tokens = tokenizer.tokenize();
    
    ASSERT_FALSE(tokenizer.error().has_value());
    ASSERT_EQ(tokens.size(), 4);
    EXPECT_EQ(tokens[0].type, mcmd::TokenType::Command);
    EXPECT_EQ(tokens[0].value, "give");
}
```

---

## 更多阅读

- [技术架构](Architecture) — 整体架构设计
- [命令数据格式](Command-Data-Format) — JSON 命令库详细规范
- [性能优化](Performance) — 核心库性能调优
