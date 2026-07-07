# 快速开始

## 环境要求

- **Android Studio** Hedgehog (2023.1.1) 或更高版本
- **Android SDK** API Level 24+
- **NDK** r25 或更高版本
- **CMake** 3.22 或更高版本
- **JDK** 17

## 构建步骤

### 1. 克隆项目

```bash
git clone https://github.com/NinefCJ/Nexus.git
cd Nexus
```

### 2. 配置 Android SDK

在 `Nexus-Android/local.properties` 中设置 SDK 路径：

```properties
sdk.dir=/path/to/android-sdk
```

### 3. 构建 Debug 版本

```bash
cd Nexus-Android
./gradlew assembleDebug
```

构建产物位于：`app/build/outputs/apk/debug/app-debug.apk`

### 4. 构建 Release 版本

```bash
./gradlew assembleRelease
```

需要配置签名密钥（创建 `keystore.properties`）：

```properties
keyAlias=your-key-alias
keyPassword=your-key-password
storeFile=your-keystore.jks
storePassword=your-store-password
```

## 运行应用

将 APK 安装到 Android 设备或模拟器上，启动应用即可使用。

## 使用说明

### 命令编辑器

在主界面的命令编辑器中输入命令，系统会实时提供补全建议和语法提示。

### 资源库

点击底部导航栏的"资源库"标签，可以浏览方块、物品、音效、粒子和动画库。

### 悬浮窗

在设置中开启悬浮窗权限后，可以在游戏中使用命令助手。
