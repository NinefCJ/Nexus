# 拓展包系统

Nexus 支持 JSON 格式的自定义拓展包，用户可以添加自定义命令、ID 库、方块状态等内容，扩展工具的功能。

---

## 什么是拓展包

拓展包是一个或多个 JSON 文件的集合，包含：
- 自定义命令定义
- 自定义 ID 翻译库
- 方块状态扩展
- JSON Schema 定义
- 命令模板

拓展包以 `.json` 文件形式存在，可以通过应用内的拓展包管理器安装和管理。

---

## 拓展包类型

| 类型 | 说明 | 典型用途 |
|------|------|----------|
| 命令包 | 新增或覆盖命令定义 | 添加模组命令、插件命令 |
| ID 库包 | 新增 ID 翻译数据 | 模组方块/物品翻译 |
| 方块状态包 | 新增方块状态定义 | 扩展方块状态补全 |
| Schema 包 | 新增 JSON Schema | 自定义 RawText 验证 |
| 模板包 | 新增命令模板 | 常用命令模板集合 |

一个拓展包可以同时包含多种类型的内容。

---

## 包结构规范

### 单文件包

最简单的拓展包就是一个 JSON 文件：

```
my-addon.json
```

### 多文件包

复杂的拓展包可以包含多个文件，推荐使用以下结构：

```
my-addon/
├── pack.json              # 包元数据 (可选)
├── commands/
│   ├── custom-cmds.json   # 自定义命令
│   └── mod-cmds.json      # 模组命令
├── libraries/
│   ├── mod-blocks.json    # 模组方块库
│   └── mod-items.json     # 模组物品库
├── blockstates/
│   └── mod-states.json    # 方块状态
└── schemas/
    └── rawtext.json       # JSON Schema
```

> **注意**：当前版本主要支持单文件 JSON 包，多文件包通过 ZIP 压缩后安装。

---

## 包元数据

所有拓展包都需要包含以下基本字段：

```json
{
  "name": "我的自定义拓展包",
  "author": "你的名字",
  "description": "拓展包的简要描述",
  "uuid": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "version": [1, 0, 0],
  "require": []
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `name` | `string` | 包名称，显示在拓展包列表中 |
| `author` | `string` | 作者名称 |
| `description` | `string` | 包描述，说明包含哪些内容 |
| `uuid` | `string` | 唯一标识符，用于依赖和更新 |
| `version` | `number[]` | 版本号 [major, minor, patch] |
| `require` | `string[]` | 依赖的其他包 UUID 列表 |

### 生成 UUID

可以使用以下方式生成 UUID：

```bash
# Linux / macOS
uuidgen

# Python
python3 -c "import uuid; print(uuid.uuid4())"

# 在线工具
# https://www.uuidgenerator.net/
```

> **重要**：每个拓展包必须有唯一的 UUID，不要重复使用。

---

## 创建命令拓展包

### 步骤 1：准备 JSON 结构

创建一个 JSON 文件，按照 [命令数据格式](Command-Data-Format) 规范编写：

```json
{
  "name": "我的自定义命令包",
  "author": "YourName",
  "description": "添加一些自定义命令",
  "uuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "version": [1, 0, 0],
  "require": [],
  "commands": [
    {
      "name": "mycmd",
      "description": "我的自定义命令",
      "syntax": "/mycmd <target> [message]",
      "category": "自定义",
      "params": [
        {
          "name": "target",
          "type": "Selector",
          "description": "目标玩家",
          "required": true,
          "suggestions": ["@p", "@a", "@s"]
        },
        {
          "name": "message",
          "type": "String",
          "description": "消息内容",
          "required": false
        }
      ]
    }
  ]
}
```

### 步骤 2：验证 JSON

确保 JSON 语法正确：

```bash
python3 -m json.tool my-commands.json > /dev/null && echo "OK"
```

### 步骤 3：安装到应用

1. 将 JSON 文件复制到手机存储
2. 打开 Nexus 应用
3. 进入「设置」→「拓展包管理」
4. 点击「安装拓展包」
5. 选择你的 JSON 文件
6. 安装完成后启用该包

---

## 创建 ID 翻译库包

### 方块/物品库格式

```json
{
  "name": "自定义方块包",
  "author": "YourName",
  "uuid": "...",
  "version": [1, 0, 0],
  "libraries": {
    "blocks": [
      {
        "id": "mymod:ruby_block",
        "name": "红宝石方块",
        "category": "建筑",
        "description": "闪亮的红宝石方块"
      }
    ],
    "items": [
      {
        "id": "mymod:ruby",
        "name": "红宝石",
        "category": "材料",
        "description": "珍贵的红宝石"
      }
    ]
  }
}
```

### 音效/粒子/动画库

```json
{
  "libraries": {
    "sounds": [
      {
        "id": "mymod:magic_spell",
        "name": "魔法咒语",
        "category": "魔法",
        "description": "施放魔法的声音"
      }
    ],
    "particles": [
      {
        "id": "mymod:sparkle",
        "name": "闪光粒子",
        "category": "特效",
        "description": "闪闪发光的粒子效果"
      }
    ],
    "animations": [
      {
        "id": "mymod:dance",
        "name": "跳舞动作",
        "category": "表情",
        "description": "欢快的舞蹈"
      }
    ]
  }
}
```

---

## 依赖管理

### 声明依赖

如果你的拓展包依赖另一个包，在 `require` 字段中声明：

```json
{
  "name": "拓展包 B",
  "uuid": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
  "version": [1, 0, 0],
  "require": [
    "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
  ]
}
```

### 依赖解析规则

- 加载包时自动检查依赖是否存在
- 缺少依赖时给出提示，不加载该包
- 依赖按 UUID 匹配，不按名称
- 版本兼容：major 版本相同即兼容

---

## 覆盖与优先级

### 命令覆盖

如果多个包定义了相同名称的命令，优先级高的包会覆盖优先级低的。

默认优先级（从低到高）：
1. 内置默认命令库
2. 其他拓展包 (按安装顺序)
3. 用户自定义包

可以在拓展包管理中调整优先级。

### ID 库合并

ID 库不会覆盖，而是合并。如果两个包定义了相同 ID 的条目，优先级高的名称和描述会覆盖低的。

---

## 最佳实践

### 1. 包命名规范

- 使用有意义的包名，如「工业时代模组拓展」
- 作者名使用你的昵称或团队名
- 描述要清晰说明包的内容和用途

### 2. 版本号规范

遵循语义化版本 (SemVer)：

| 版本位 | 说明 | 示例 |
|--------|------|------|
| Major | 不兼容的大改动 | 1.x.x → 2.0.0 |
| Minor | 向后兼容的新功能 | 1.0.x → 1.1.0 |
| Patch | 向后兼容的修复 | 1.0.0 → 1.0.1 |

### 3. 性能建议

- 单个包建议不超过 5000 条命令/ID
- 避免不必要的大量重复数据
- 使用建议值特殊变量 (`$blocks` 等) 代替硬编码

### 4. 测试包

安装前务必在测试环境验证：

1. JSON 语法正确
2. 所有字段类型正确
3. UUID 唯一
4. 命令名无冲突
5. 依赖正确声明

---

## 常见问题

### Q: 拓展包安装后不生效？

检查以下几点：
1. 包是否已启用（拓展包管理中查看）
2. JSON 格式是否正确
3. 命令名是否拼写正确
4. 优先级是否足够高

### Q: 如何更新拓展包？

1. 修改 JSON 文件
2. 更新 version 字段
3. 重新安装即可覆盖旧版本

### Q: 可以删除内置命令吗？

不能直接删除，但可以通过创建同命令名的空定义来覆盖。更推荐在设置中调整命令显示。

---

## 分享你的拓展包

如果你创建了有用的拓展包，欢迎分享给社区：

1. 将包文件上传到 GitHub / 网盘
2. 在社区论坛发布介绍帖
3. 附上使用说明和截图

---

## 更多阅读

- [命令数据格式](Command-Data-Format) — 详细的 JSON 格式规范
- [贡献指南](Contributing) — 参与官方拓展包开发
