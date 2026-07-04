# 命令数据格式

Nexus 使用 JSON 格式定义命令库，支持自定义拓展包扩展。本文档详细说明命令数据的 JSON 格式规范。

---

## 概述

所有命令数据以 JSON 格式存储，核心包含：
- 命令定义 (commands)
- 参数定义 (params)
- 建议值列表 (suggestions)

---

## 顶层结构

```json
{
  "name": "默认命令库",
  "author": "NexusTeam",
  "description": "Minecraft 基岩版默认命令库",
  "uuid": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "version": [1, 0, 0],
  "require": [],
  "commands": [ ... ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | `string` | 是 | 包名称 |
| `author` | `string` | 否 | 作者名 |
| `description` | `string` | 否 | 描述 |
| `uuid` | `string` | 是 | 唯一标识符 (UUID v4) |
| `version` | `number[]` | 是 | 版本号 [major, minor, patch] |
| `require` | `string[]` | 否 | 依赖的其他包 UUID 列表 |
| `commands` | `array` | 是 | 命令定义数组 |

---

## 命令定义 (CommandDef)

```json
{
  "name": "give",
  "description": "给予玩家物品",
  "syntax": "/give <targets> <item> [amount] [data]",
  "category": "物品",
  "permissionLevel": 1,
  "params": [ ... ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | `string` | 是 | 命令名 (不含 /) |
| `description` | `string` | 是 | 命令描述 |
| `syntax` | `string` | 是 | 语法模板字符串 |
| `category` | `string` | 否 | 分类 |
| `permissionLevel` | `number` | 否 | 权限等级 (0-4) |
| `params` | `array` | 是 | 参数定义数组 |

### 语法模板约定

- `<param>` — 必填参数，使用尖括号
- `[param]` — 可选参数，使用方括号
- 参数顺序与 `params` 数组顺序一致

**示例**：`/give <targets> <item> [amount] [data]`

---

## 参数定义 (ParamDef)

```json
{
  "name": "targets",
  "type": "Selector",
  "description": "目标玩家或实体",
  "required": true,
  "default": null,
  "suggestions": ["@p", "@a", "@e", "@s", "@r"]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | `string` | 是 | 参数名 |
| `type` | `string` | 是 | 参数类型，见下表 |
| `description` | `string` | 否 | 参数描述 |
| `required` | `boolean` | 否 | 是否必填，默认 `true` |
| `default` | `string/null` | 否 | 默认值 |
| `suggestions` | `string[]` | 否 | 建议值列表 |

### 参数类型 (ParamType)

| 类型值 | 说明 | 示例 |
|--------|------|------|
| `Selector` | 目标选择器 | `@p`, `@a`, `@e[type=creeper]` |
| `ItemId` | 物品/方块 ID | `minecraft:stone`, `diamond_sword` |
| `BlockId` | 方块 ID | `minecraft:stone` |
| `Integer` | 整数 | `64`, `-1` |
| `Float` | 浮点数 | `1.5`, `0.0` |
| `String` | 字符串 | `"hello"` |
| `Coordinates` | 坐标 | `~ ~1 ~`, `^ ^ ^5` |
| `Boolean` | 布尔值 | `true`, `false` |
| `BlockState` | 方块状态 | `variant=granite` |
| `Component` | 组件/NBT | `{Health:20f}` |
| `JsonRawText` | JSON 文本组件 | `{"rawtext":[{"text":"hi"}]}` |
| `Custom` | 自定义类型 | - |

### 特殊建议值

`suggestions` 字段支持特殊值，用于动态生成建议：

| 特殊值 | 说明 |
|--------|------|
| `$blocks` | 所有方块 ID |
| `$items` | 所有物品 ID |
| `$entities` | 所有实体 ID |
| `$particles` | 所有粒子 ID |
| `$sounds` | 所有音效 ID |
| `$animations` | 所有动画 ID |
| `$effects` | 所有药水效果 ID |
| `$enchantments` | 所有附魔 ID |
| `$biomes` | 所有生物群系 ID |

**示例**：

```json
{
  "name": "block",
  "type": "BlockId",
  "description": "方块类型",
  "required": true,
  "suggestions": ["$blocks"]
}
```

---

## 完整示例

### 简单命令：`/give`

```json
{
  "name": "give",
  "description": "给予玩家指定数量的物品",
  "syntax": "/give <targets> <item> [amount] [data]",
  "category": "物品",
  "permissionLevel": 1,
  "params": [
    {
      "name": "targets",
      "type": "Selector",
      "description": "目标玩家",
      "required": true,
      "suggestions": ["@p", "@a", "@e", "@s", "@r"]
    },
    {
      "name": "item",
      "type": "ItemId",
      "description": "物品 ID",
      "required": true,
      "suggestions": ["$items"]
    },
    {
      "name": "amount",
      "type": "Integer",
      "description": "数量 (1-64)",
      "required": false,
      "default": "1"
    },
    {
      "name": "data",
      "type": "Integer",
      "description": "数据值",
      "required": false,
      "default": "0"
    }
  ]
}
```

### 复杂命令：`/execute`

```json
{
  "name": "execute",
  "description": "以指定实体身份执行命令",
  "syntax": "/execute <origin> <position> <command>",
  "category": "管理",
  "permissionLevel": 2,
  "params": [
    {
      "name": "origin",
      "type": "Selector",
      "description": "执行源实体",
      "required": true,
      "suggestions": ["@p", "@a", "@e", "@s", "@r"]
    },
    {
      "name": "position",
      "type": "Coordinates",
      "description": "执行位置",
      "required": true
    },
    {
      "name": "command",
      "type": "String",
      "description": "要执行的命令",
      "required": true
    }
  ]
}
```

---

## 方块状态包格式

方块状态包定义了所有方块的可能状态值。

```json
{
  "name": "方块状态包",
  "author": "NexusTeam",
  "uuid": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "version": [1, 0, 0],
  "blockStates": {
    "stone": {
      "variant": {
        "type": "enum",
        "values": ["stone", "granite", "diorite", "andesite"],
        "default": "stone"
      }
    },
    "log": {
      "pillar_axis": {
        "type": "enum",
        "values": ["x", "y", "z"],
        "default": "y"
      }
    }
  }
}
```

### 状态类型

| 类型 | 说明 | 额外字段 |
|------|------|----------|
| `enum` | 枚举值 | `values`, `default` |
| `int` | 整数范围 | `min`, `max`, `default` |
| `bool` | 布尔值 | `default` |

---

## JSON Schema 包格式

JSON Schema 包用于验证复杂的 JSON 数据（如 RawText 格式）。

```json
{
  "name": "JSON Schema包",
  "author": "南鸢晨星",
  "uuid": "a1471826-7086-022e-cd02-8a5a317e825a",
  "version": [1, 0, 0],
  "require": [],
  "jsonSchema": {
    "rawtext": {
      "title": "Minecraft RawText Schema",
      "type": "object",
      "properties": {
        "rawtext": {
          "type": "array",
          "items": {
            "oneOf": [
              {
                "type": "object",
                "properties": {
                  "text": { "type": "string" }
                },
                "required": ["text"]
              },
              {
                "type": "object",
                "properties": {
                  "translate": { "type": "string" }
                },
                "required": ["translate"]
              }
            ]
          }
        }
      },
      "required": ["rawtext"]
    }
  }
}
```

---

## 加载顺序与优先级

当多个包定义了相同命令时，后加载的包会覆盖先加载的。

加载顺序：
1. 内置默认命令库
2. 用户安装的拓展包 (按安装顺序)
3. 用户自定义包

可在设置中调整包的优先级。

---

## 验证工具

使用以下方式验证你的 JSON 文件：

```bash
# 检查 JSON 语法是否正确
python3 -m json.tool your_pack.json > /dev/null

# 或使用 jq
jq . your_pack.json
```

---

## 更多阅读

- [拓展包系统](Addon-System) — 如何创建和安装拓展包
- [核心模块](Core-Modules) — CommandRegistry 加载逻辑
