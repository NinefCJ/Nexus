# 快速开始

本指南将帮助你从零开始搭建 Nexus 开发环境，并成功构建项目。

---

## 环境要求

| 工具 | 最低版本 | 推荐版本 |
|------|----------|----------|
| Android Studio | Hedgehog (2023.1.1) | Iguana (2023.2.1) 或更高 |
| Android SDK | API Level 33 | API Level 34 |
| NDK | r25 | r26b 或更高 |
| CMake | 3.22 | 3.27 |
| JDK | 17 | 17 |
| Git | 2.25 | 最新版 |

### 检查环境

```bash
# 检查 Java 版本
java -version

# 检查 CMake
cmake --version

# 检查 Gradle (项目自带 wrapper，无需全局安装)
./gradlew --version
```

---

## 获取源码

```bash
# 克隆仓库
git clone https://github.com/NexusTeam/Nexus.git
cd Nexus

# 查看版本
git tag --list
```

---

## Android 应用构建

### 1. 配置 SDK 路径

在 `Nexus-Android/local.properties` 中设置：

```properties
sdk.dir=/path/to/your/android-sdk
```

> 如果你使用 Android Studio 打开项目，它会自动配置此文件。

### 2. 使用 Android Studio 构建（推荐）

1. 打开 Android Studio
2. 选择 **Open an Existing Project**
3. 选择 `Nexus-Android` 目录
4. 等待 Gradle 同步完成
5. 点击 **Run** 按钮或使用 `Shift + F10`

### 3. 命令行构建

```bash
cd Nexus-Android

# 构建 Debug APK
./gradlew assembleDebug
# 输出位置: app/build/outputs/apk/debug/app-debug.apk

# 构建 Release APK
./gradlew assembleRelease

# 安装到设备
./gradlew installDebug

# 运行单元测试
./gradlew testDebugUnitTest
```

### 4. 构建 Release 版本

Release 构建需要配置签名密钥。在 `app/build.gradle.kts` 中添加：

```kotlin
android {
    signingConfigs {
        create("release") {
            keyAlias = "your-key-alias"
            keyPassword = "your-key-password"
            storeFile = file("your-keystore.jks")
            storePassword = "your-store-password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

> **安全提示**：不要将包含密钥的 `build.gradle.kts` 提交到公开仓库。

### 5. ABI 架构

项目默认构建以下架构：

- `armeabi-v7a` — 32 位 ARM (大多数旧设备)
- `arm64-v8a` — 64 位 ARM (现代设备)
- `x86` — 32 位 x86 (模拟器)
- `x86_64` — 64 位 x86 (模拟器/ChromeOS)

如需减少 APK 体积，可在 `build.gradle.kts` 中调整：

```kotlin
ndk {
    abiFilters += listOf("armeabi-v7a", "arm64-v8a")
}
```

---

## C++ 核心库独立构建

如果你只想构建或测试 C++ 核心库：

```bash
cd Nexus-Core

# 创建构建目录
mkdir build && cd build

# 配置 (Release 模式)
cmake .. -DCMAKE_BUILD_TYPE=Release

# 编译
make -j$(nproc)

# 运行单元测试
ctest --output-on-failure
```

### 可选依赖

C++ 核心库会通过 `FetchContent` 自动下载以下依赖：

| 库 | 版本 | 用途 |
|----|------|------|
| RapidJSON | 1.1.0 | JSON 解析 |
| fmt | 10.2.1 | 字符串格式化 |
| GoogleTest | 1.14.0 | 单元测试 |

无需手动安装，CMake 会自动处理。

---

## 项目目录概览

```
Nexus/
├── Nexus-Core/                    # C++ 核心库
│   ├── include/                   # 头文件
│   │   ├── types.hpp              # 类型定义
│   │   ├── tokenizer.hpp          # 词法分析器
│   │   ├── parser.hpp             # 语法解析器
│   │   ├── completion.hpp         # 补全引擎
│   │   ├── highlighter.hpp        # 语法高亮
│   │   ├── command_registry.hpp   # 命令注册表
│   │   └── command_helper_jni.hpp # JNI 接口
│   ├── src/                       # 实现文件
│   ├── tests/                     # 单元测试
│   └── CMakeLists.txt
│
├── Nexus-Android/                 # Android 应用
│   ├── app/src/main/
│   │   ├── cpp/                   # JNI 桥接
│   │   ├── java/com/nexuscmd/     # Kotlin 源码
│   │   └── res/                   # 资源
│   └── build.gradle.kts
│
└── Command/                       # 命令数据包
    ├── 默认命令库.json
    ├── 方块状态包.json
    └── JSON Schema包.json
```

---

## 常见构建问题

### 1. NDK 未找到

**错误**：`No version of NDK matched the requested version`

**解决**：
```bash
# 在 local.properties 中指定 NDK 路径
ndk.dir=/path/to/ndk

# 或通过 SDK Manager 安装推荐版本
```

### 2. CMake 版本不兼容

**错误**：`CMake '3.22.1' was not found`

**解决**：通过 SDK Manager 安装 CMake 3.22.1，或修改 `app/build.gradle.kts` 中的版本号。

### 3. Gradle 同步失败

尝试清理缓存：

```bash
cd Nexus-Android
./gradlew clean
./gradlew --refresh-dependencies
```

更多问题请参考 [FAQ](FAQ)。

---

## 下一步

- 了解 [技术架构](Architecture)
- 探索 [核心模块](Core-Modules)
- 学习 [拓展包开发](Addon-System)
- 阅读 [贡献指南](Contributing)
