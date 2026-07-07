# Nexus - Minecraft Bedrock Edition Command Helper

> A command assistance tool designed specifically for Minecraft Bedrock Edition, providing intelligent syntax hints, command completion, ID translation library and more powerful features.

[English](README-en.md) | [简体中文](README.md)

---

## Table of Contents

- [Project Overview](#project-overview)
- [Project Structure](#project-structure)
- [Technical Architecture](#technical-architecture)
- [Getting Started](#getting-started)
- [Features](#features)
- [Contributing Guide](#contributing-guide)

---

## Project Overview

Nexus is a cross-platform Minecraft Bedrock Edition command assistance tool, refactored from [CHelper](https://github.com/Yancey2023/CHelper), with the following main features:

### Core Features

| Feature | Description |
|---------|-------------|
| **Command Completion** | Real-time syntax hints and auto-completion with smart context-aware ID completion |
| **Syntax Highlighting** | Different colors for command structure, better readability |
| **Error Detection** | Real-time command syntax validation with instant feedback |
| **Parameter Hints** | Display command parameter descriptions and syntax templates |
| **Old Command Conversion** | Convert 1.18 old commands to 1.20+ new commands |
| **Command Enumeration** | Batch generate parameter combination commands |
| **Command Library** | Local/cloud command library management |
| **Floating Window** | Real-time command assistance even while gaming |
| **Input Method Mode** | Auto-convert commands while typing |
| **Theme System** | Multiple preset themes + custom background images |
| **Raw JSON** | JSON text generator with color and formatting support |

---

## Project Structure

```
Nexus/
├── Nexus-Core/                    # C++ Native Core Library (Cross-platform Foundation)
│   ├── include/                   # Header Files Directory
│   ├── src/                       # Source Code Directory
│   │   ├── chelper/               # Core Modules
│   │   │   ├── auto_suggestion/   # Auto Completion
│   │   │   ├── command_structure/ # Command Structure
│   │   │   ├── lexer/             # Lexer
│   │   │   ├── linter/            # Linter
│   │   │   ├── node/              # AST Nodes
│   │   │   ├── old2new/           # Old to New Command Conversion
│   │   │   ├── parameter_hint/    # Parameter Hints
│   │   │   ├── parser/            # Parser
│   │   │   ├── resources/         # Resource Management
│   │   │   ├── serialization/     # Serialization
│   │   │   ├── syntax_highlight/  # Syntax Highlighting
│   │   │   └── util/              # Utility Functions
│   │   └── apps/                  # Application Entry
│   │       └── NexusAndroid.cpp   # Android JNI Entry
│   ├── tests/                     # Unit Tests
│   ├── 3rdparty/                  # Third-party Dependencies
│   └── CMakeLists.txt             # CMake Build Configuration
│
├── Nexus-Android/                  # Android Application
│   ├── app/
│   │   ├── libs/                  # Precompiled Native Libraries
│   │   │   └── arm64-v8a/
│   │   │       └── libNexusAndroid.so
│   │   └── src/main/
│   │       ├── assets/            # Resource Packages
│   │       │   ├── cpack/         # Command Resource Packages
│   │       │   ├── old2new/       # Old Command Conversion Data
│   │       │   └── about/         # About Page Text
│   │       ├── java/com/nexuscmd/ # Kotlin Source Code
│   │       │   ├── android/       # Android Related
│   │       │   │   ├── activity/  # Activities
│   │       │   │   ├── service/   # Services
│   │       │   │   ├── util/      # Utilities
│   │       │   │   └── window/    # Floating Window Management
│   │       │   ├── core/          # Core Modules
│   │       │   ├── data/          # Data Layer
│   │       │   ├── network/       # Network Module
│   │       │   └── ui/            # UI Layer
│   │       │       ├── about/     # About Page
│   │       │       ├── completion/# Command Completion
│   │       │       ├── enumeration/# Enumeration
│   │       │       ├── home/      # Home Page
│   │       │       ├── library/   # Command Library
│   │       │       ├── loongflow/ # LoongFlow Editor
│   │       │       ├── old2new/   # Old Command Conversion
│   │       │       ├── rawtext/   # Raw JSON
│   │       │       ├── settings/  # Settings
│   │       │       └── common/    # Common Components
│   │       └── res/               # Resource Files
│   ├── gradle/                    # Gradle Configuration
│   ├── build.gradle.kts           # Build Configuration
│   └── settings.gradle.kts        # Project Configuration
│
├── Command/                        # Command Definition Data
│   ├── commands_list.txt          # Command List
│   ├── 默认命令库.json            # Default Command Library
│   ├── 方块状态包.json            # Block State Definition
│   └── JSON Schema包.json         # JSON Schema
│
└── README.md                      # Project Documentation
```

---

## Technical Architecture

### Architecture Layers

```
┌─────────────────────────────────────────────┐
│              Android UI Layer                │
│         (Jetpack Compose + Material3)        │
├─────────────────────────────────────────────┤
│           ViewModel Layer                   │
│        (StateFlow + Coroutines)             │
├─────────────────────────────────────────────┤
│           Repository Layer                  │
│   (LocalCommandLab, Settings, Background)   │
├─────────────────────────────────────────────┤
│           JNI Bridge Layer                  │
│      (Kotlin ←→ C++ Interop)               │
├─────────────────────────────────────────────┤
│         C++ Core Library                    │
│  (Lexer → Parser → Completion Engine)       │
├─────────────────────────────────────────────┤
│          Native Platform                    │
│       (Android NDK + CMake)                 │
└─────────────────────────────────────────────┘
```

### Technology Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| **UI** | Jetpack Compose | Modern declarative UI |
| **Navigation** | Navigation Compose | Navigation management |
| **State** | ViewModel + StateFlow | Reactive state management |
| **Data Storage** | DataStore | Key-value storage |
| **Image Loading** | Coil | Image loading |
| **Network** | OkHttp + Retrofit | HTTP requests |
| **Serialization** | Kotlinx Serialization | JSON serialization |
| **Native** | C++17 + JNI | High-performance command processing |
| **Build** | CMake + Gradle | Cross-platform build |

---

## Getting Started

### Environment Requirements

- **Android Studio** Hedgehog (2023.1.1) or higher
- **Android SDK** API Level 24+
- **NDK** r25 or higher
- **CMake** 3.22 or higher
- **JDK** 17

### Build Steps

#### 1. Clone the Project

```bash
git clone https://github.com/NinefCJ/Nexus.git
cd Nexus
```

#### 2. Configure Android SDK

Set SDK path in `Nexus-Android/local.properties`:

```properties
sdk.dir=/path/to/android-sdk
```

#### 3. Build Debug Version

```bash
cd Nexus-Android
./gradlew assembleDebug
```

Build output: `app/build/outputs/apk/debug/app-debug.apk`

#### 4. Build Release Version

```bash
./gradlew assembleRelease
```

Requires signing key configuration (create `keystore.properties`):

```properties
keyAlias=your-key-alias
keyPassword=your-key-password
storeFile=your-keystore.jks
storePassword=your-store-password
```

---

## Features

### Command Completion

- **Real-time Completion**: Instant completion suggestions while typing
- **Smart Context**: Precise completion based on cursor position
- **Syntax Highlighting**: Different colors for different token types
- **Error Detection**: Real-time syntax error detection with hints

### Old Command Conversion

- **1.18 → 1.20+**: Auto-convert old commands to new syntax
- **Block Data Value Conversion**: Auto-map block data values to block states
- **Execute Command Conversion**: Convert execute old syntax
- **Input Method Mode**: Auto-convert while typing, no manual operation needed

### Floating Window Mode

- **In-game Usage**: Use without exiting the game
- **Quick Copy**: One-click copy to clipboard
- **Minimize**: Shrink to floating icon when not in use
- **Opacity Adjustment**: Adjust icon and interface opacity

### Command Library

- **Local Command Library**: Create and manage local command collections
- **Cloud Command Library**: Browse and download community-shared commands
- **Upload & Share**: Share your commands with other users
- **Search**: Quick search in command library

### LoongFlow Editor

- **Visual Programming**: Create command chains with flowcharts
- **Conditional Branches**: Support if-else conditions
- **Loop Structures**: Support loop execution
- **Export Commands**: One-click export to command strings

### Theme System

- **Preset Themes**: Multiple built-in themes to choose from
- **Custom Background**: Import background images from gallery
- **Glassmorphism**: Modern frosted glass visual effects

---

## Contributing Guide

### Development Standards

#### 1. Code Style

- Kotlin: Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- C++: Follow [Google C++ Style Guide](https://google.github.io/styleguide/cppguide.html)
- Indentation: 4 spaces
- Line length: Maximum 120 characters

#### 2. Commit Convention

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

**Type Categories**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation update
- `style`: Code formatting
- `refactor`: Refactoring
- `perf`: Performance optimization
- `test`: Testing
- `chore`: Build/tooling

**Example**:
```
feat(completion): Add /hud command completion

- Add HUD element list
- Support hide/reset operations
- Update quick command library
```

#### 3. Branch Management

```
main          # Main branch, stable version
├── develop   # Development branch
│   ├── feature/new-command  # Feature branch
│   ├── fix/bug-fix          # Fix branch
│   └── refactor/code-cleanup # Refactor branch
```

### Running Tests

```bash
# C++ unit tests
cd Nexus-Core
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Release
make -j$(nproc)
ctest --output-on-failure

# Android unit tests
cd Nexus-Android
./gradlew testDebugUnitTest

# Android build
./gradlew assembleDebug
```

### Reporting Issues

Please report issues via [GitHub Issues](https://github.com/NinefCJ/Nexus/issues) with:

1. Issue description
2. Reproduction steps
3. Expected behavior
4. Screenshots/logs
5. Environment info (Android version, app version)

---

## Changelog

### v1.4.0 (2026-07-07)

- Refactored based on CHelper, core features fully upgraded
- New command completion engine with significantly improved performance
- Added old-to-new command conversion (including input method mode)
- Added command enumeration feature
- Added command library (local + cloud)
- Added LoongFlow visual editor
- Added Raw JSON text generator
- Improved floating window experience

### v1.3.0

- Improved sound effect library and animation library
- Added sound effect preview feature
- Added /playanimation and /hud command support
- Optimized UI interaction

---

## License

This project is licensed under the GPL-3.0 License - see [LICENSE](LICENSE) file for details.

## Reference Projects

- [CHelper](https://github.com/Yancey2023/CHelper) - C++ Core Architecture Reference
- [Minecraft Wiki](https://minecraft.wiki) - Command Syntax and ID Data Reference

## Contact

- **Author**: Nexus Team (氿九Ninef)
- **Email**: 2395953343@qq.com

---

<div align="center">

Made with ❤️ for Minecraft Community

</div>
