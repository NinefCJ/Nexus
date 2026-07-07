# 开发指南

## 开发规范

### 代码风格

- **Kotlin**：遵循 [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **C++**：遵循 [Google C++ Style Guide](https://google.github.io/styleguide/cppguide.html)
- **缩进**：4 空格
- **行长度**：最多 120 字符

### 提交规范

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

**Type 类型**：

- `feat`：新功能
- `fix`：错误修复
- `docs`：文档更新
- `style`：代码格式
- `refactor`：重构
- `perf`：性能优化
- `test`：测试
- `chore`：构建/工具

### 分支管理

```
main          # 主分支，稳定版本
├── develop   # 开发分支
│   ├── feature/new-command  # 功能分支
│   ├── fix/bug-fix          # 修复分支
│   └── refactor/code-cleanup # 重构分支
```

## 项目结构

```
Nexus/
├── Nexus-Core/              # C++ 原生核心库
│   ├── include/             # 头文件
│   ├── src/                 # 源代码
│   └── tests/               # 单元测试
├── Nexus-Android/           # Android 应用
│   ├── app/
│   │   ├── libs/            # 预编译原生库
│   │   └── src/main/
│   │       ├── assets/      # 资源包
│   │       ├── java/        # Kotlin 源代码
│   │       └── res/         # 资源文件
├── Command/                 # 命令定义数据
└── docs/                    # 文档
```

## 运行测试

### C++ 单元测试

```bash
cd Nexus-Core
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Release
make -j$(nproc)
ctest --output-on-failure
```

### Android 单元测试

```bash
cd Nexus-Android
./gradlew testDebugUnitTest
```

## 添加新功能

### 添加新命令

1. 在 `Command/` 目录下添加命令定义
2. 更新 `Nexus-Core` 中的命令注册表
3. 更新 Android 端的命令补全逻辑

### 添加新资源库

1. 创建数据类
2. 创建库对象
3. 添加 UI 组件
4. 集成到主界面

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交代码
4. 发起 Pull Request
