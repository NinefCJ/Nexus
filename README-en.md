# Nexus - Minecraft Command Helper

> A command assistance tool designed specifically for Minecraft Bedrock Edition, providing intelligent syntax hints, command completion, ID translation library and more powerful features.

[English](README-en.md) | [简体中文](README.md)

---

## Table of Contents

- [Project Overview](#project-overview)
- [Project Structure](#project-structure)
- [Technical Architecture](#technical-architecture)
- [Getting Started](#getting-started)
- [Module Details](#module-details)
- [Data Models](#data-models)
- [Performance Optimization](#performance-optimization)
- [Theme System](#theme-system)
- [Contributing Guide](#contributing-guide)

---

## Project Overview

Nexus is a cross-platform Minecraft Bedrock Edition command assistance tool with the following main features:

### Core Features

| Feature | Description |
|---------|-------------|
| **Command Completion** | Real-time syntax hints and auto-completion with smart context-aware ID completion |
| **Syntax Highlighting** | Different colors for command structure, better readability |
| **Error Detection** | Real-time command syntax validation with instant feedback |
| **ID Translation Library** | Complete Chinese translations for blocks, items, sounds, particles, and animations |
| **Command Templates** | 55+ Bedrock Edition command templates for quick generation |
| **Floating Window** | Real-time command assistance even while gaming |
| **Theme System** | 8 preset themes + custom background images |
| **Glassmorphism UI** | Modern frosted glass visual effects |
| **Addon System** | Support for JSON format custom addon packages |

---

## Project Structure

```
Nexus/
├── Nexus-Core/                    # C++ Native Core Library (Cross-platform Foundation)
│   ├── include/                   # Header Files Directory
│   │   ├── command_helper_jni.hpp   # JNI Interface Declaration
│   │   ├── command_registry.hpp     # Command Registry
│   │   ├── completion.hpp           # Completion Engine
│   │   ├── highlighter.hpp          # Syntax Highlighter
│   │   ├── parser.hpp               # Command Parser
│   │   ├── tokenizer.hpp            # Lexical Analyzer
│   │   └── types.hpp                # Type Definitions
│   ├── src/                        # Source Code Directory
│   │   ├── command_helper_jni.cpp   # JNI Implementation
│   │   ├── command_registry.cpp     # Command Registry Implementation
│   │   ├── completion.cpp           # Completion Logic Implementation
│   │   ├── highlighter.cpp          # Highlighter Logic Implementation
│   │   ├── parser.cpp               # Parser Implementation
│   │   └── tokenizer.cpp            # Lexical Analyzer Implementation
│   ├── tests/                      # Unit Tests
│   │   ├── parser_test.cpp
│   │   └── tokenizer_test.cpp
│   ├── third_party/                 # Third-party Dependencies
│   │   └── rapidjson/               # JSON Parsing Library
│   └── CMakeLists.txt               # CMake Build Configuration
│
├── Nexus-Android/                  # Android Application
│   └── app/
│       └── src/main/
│           ├── cpp/                 # Native C++ Code
│           │   ├── CMakeLists.txt    # Native Build Configuration
│           │   └── native.cpp       # JNI Native Methods
│           ├── java/com/nexuscmd/   # Kotlin Source Code
│           │   ├── MainActivity.kt       # Main Activity
│           │   ├── MainViewModel.kt      # ViewModel
│           │   ├── CommandHelper.kt      # Native Call Wrapper
│           │   ├── FloatingWindowService.kt # Floating Window Service
│           │   │
│           │   ├── data/             # Data Layer
│           │   │   ├── CommandRepository.kt       # Command Repository
│           │   │   ├── CommandChainRepository.kt  # Command Chain Repository
│           │   │   ├── HistoryManager.kt           # History Manager
│           │   │   ├── SettingsManager.kt          # Settings Manager
│           │   │   ├── AddonManager.kt             # Addon Manager
│           │   │   ├── TemplateGenerator.kt         # Template Generator
│           │   │   │
│           │   │   └── libraries/      # ID Translation Libraries
│           │   │       ├── BlockLibrary.kt          # Block Library (456)
│           │   │       ├── ItemLibrary.kt           # Item Library (336)
│           │   │       ├── SoundEffectLibrary.kt   # Sound Effect Library (918)
│           │   │       ├── ParticleLibrary.kt      # Particle Library (400+)
│           │   │       └── AnimationLibrary.kt     # Animation Library (200+)
│           │   │
│           │   └── ui/               # UI Layer
│           │       ├── components/    # Reusable Components
│           │       │   ├── MCSyntaxHighlighter.kt   # Command Syntax Highlighter
│           │       │   ├── SyntaxHighlightEditor.kt # Highlighting Editor
│           │       │   ├── SyntaxHintOverlay.kt     # Syntax Hint Overlay
│           │       │   └── GlassmorphicCard.kt     # Glassmorphic Card
│           │       │
│           │       └── theme/         # Theme System
│           │           ├── Color.kt    # Color Definitions
│           │           ├── Theme.kt   # Theme Configuration
│           │           └── Type.kt    # Typography Configuration
│           │
│           └── res/                   # Resource Files
│               ├── drawable/          # Drawable Resources
│               │   ├── completion_chip_bg.xml
│               │   ├── floating_collapsed_bg.xml
│               │   ├── floating_expanded_bg.xml
│               │   └── floating_input_bg.xml
│               │
│               ├── layout/             # XML Layouts (Floating Window)
│               │   ├── completion_chip.xml
│               │   └── floating_window.xml
│               │
│               ├── mipmap-*/          # App Icons
│               │   ├── ic_launcher.png
│               │   └── ic_launcher_round.png
│               │
│               └── values/            # Value Resources
│                   └── themes.xml    # Theme Configuration
│
├── Command/                        # Command Definition Data
│   ├── commands_list.txt          # Command List
│   ├── 默认命令库.json            # Default Command Library
│   ├── 方块状态包.json            # Block State Definition
│   └── JSON Schema包.json         # JSON Schema
│
├── docs/                          # Documentation Directory (Optional)
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
│   (CommandRepo, Settings, Addon Manager)    │
├─────────────────────────────────────────────┤
│           JNI Bridge Layer                  │
│      (Kotlin ←→ C++ Interop)               │
├─────────────────────────────────────────────┤
│         C++ Core Library                    │
│  (Tokenizer → Parser → Completion Engine)   │
├─────────────────────────────────────────────┤
│          Native Platform                    │
│       (Android NDK + CMake)                 │
└─────────────────────────────────────────────┘
```

### Technology Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| **UI** | Jetpack Compose | Modern declarative UI |
| **Theme** | Material3 | Design system |
| **State** | ViewModel + StateFlow | Reactive state management |
| **Concurrency** | Kotlin Coroutines | Asynchronous operations |
| **Native** | C++17 + JNI | High-performance command processing |
| **JSON** | RapidJSON | Efficient JSON parsing |
| **Build** | CMake + Gradle | Cross-platform build |

---

## Getting Started

### Environment Requirements

- **Android Studio** Hedgehog (2023.1.1) or higher
- **Android SDK** API Level 33 (Android 13)
- **NDK** r25 or higher
- **CMake** 3.22 or higher
- **JDK** 17

### Build Steps

#### 1. Clone the Project

```bash
git clone https://github.com/your-repo/Nexus.git
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

Requires signing key configuration:

```kotlin
// app/build.gradle.kts
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
        }
    }
}
```

#### 5. Build C++ Core (Optional)

```bash
cd Nexus-Core
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Release
make -j$(nproc)
```

---

## Module Details

### Android Modules

#### 1. MainActivity.kt (Main Activity)

**Responsibility**: Application entry point, manages main UI state and navigation

**Core Components**:
- Splash Animation (NexusSplashScreen)
- Main Screen (MainScreen)
- Bottom Navigation Bar (ModernBottomNavigation)
- Top App Bar (ModernTopAppBar)

**Page Structure**:
```
MainScreen
├── EditorTab          # Command Editor
├── CommandLibraryTab  # Command Library
├── QuickCommandsTab   # Quick Commands
├── ResourceLibraryTab # Resource Library
│   ├── BlockLibraryTab       # Block Library
│   ├── ItemLibraryTab        # Item Library
│   ├── SoundEffectLibraryTab  # Sound Effect Library
│   ├── ParticleLibraryTab    # Particle Library
│   └── AnimationLibraryTab    # Animation Library
├── HistoryTab         # History
└── SettingsTab       # Settings
```

#### 2. MainViewModel.kt (ViewModel)

**Responsibility**: State management, business logic, Native call coordination

**Core State**:
```kotlin
data class MainUiState(
    val commandText: String = "",
    val cursorPosition: Int = 0,
    val validation: ValidationResult? = null,
    val completions: List<CompletionItem> = emptyList(),
    val syntaxHint: SyntaxHint? = null,
    val quickCommands: List<Triple<String, String, ImageVector>> = emptyList(),
    // ... more states
)
```

**Performance Optimizations**:
- `@Stable` annotation for state classes
- Completion result caching (`completionCache`)
- Debounce delay handling (`COMPLETION_DEBOUNCE_MS = 150`)
- Parallel Native calls (`viewModelScope.async`)

#### 3. FloatingWindowService.kt (Floating Window Service)

**Responsibility**: Provides real-time command assistance in-game

**Features**:
- Floating window show/hide
- Command input and completion
- Quick copy to clipboard
- Minimize/expand mode

#### 4. CommandHelper.kt (Native Wrapper)

**Responsibility**: Wraps JNI calls, provides Kotlin-friendly interface

**Main Methods**:
```kotlin
class CommandHelper {
    fun initialize(packPath: String): Boolean
    fun getCompletions(command: String, cursor: Int): List<CompletionItem>
    fun validateCommand(command: String): ValidationResult
    fun getSyntaxHint(command: String, cursor: Int): SyntaxHint?
    fun dispose()
}
```

### Data Layer Modules

#### ID Translation Libraries

All ID libraries follow a unified design pattern:

```
┌─────────────────────────────────────┐
│         Data Class                  │
│  - id: String (Minecraft ID)        │
│  - name: String (Display Name)      │
│  - category: String (Category)      │
│  - description: String (Description)│
│  - extra: ... (Extra Parameters)    │
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│        Library Object                │
│  - categories: List<String>          │
│  - items: List<DataClass>            │
│  - filter(): List<T>                │
│  - buildCommand(): String           │
└─────────────────────────────────────┘
```

##### BlockLibrary.kt (Block Library)

- **Data Size**: 456 Bedrock Edition blocks
- **Categories**: 16 categories (Nature, Decoration, Function, etc.)
- **Command Generation**: `/setblock <x y z> <block> [blockstates] [options]`

##### ItemLibrary.kt (Item Library)

- **Data Size**: 336 Bedrock Edition items
- **Categories**: 10 categories (Tools, Weapons, Food, etc.)
- **Command Generation**: `/give <player> <item> [amount] [data] [components]`

##### SoundEffectLibrary.kt (Sound Effect Library)

- **Data Size**: 918 Bedrock Edition sound effects
- **Categories**: 37 categories (Ambient, Creatures, Blocks, etc.)
- **Command Generation**: `/playsound <sound> <target> [x y z] [volume] [pitch] [minimumVolume]`

##### ParticleLibrary.kt (Particle Library)

- **Data Size**: 400+ Bedrock Edition particles
- **Categories**: 9 categories
- **Command Generation**: `/particle <name> [pos] [delta] [speed] [count] [mode] [player] [params]`

##### AnimationLibrary.kt (Animation Library)

- **Data Size**: 200+ Bedrock Edition animations
- **Categories**: 9 categories (Player, Creatures, Emotes, etc.)
- **Command Generation**: `/playanimation <target> <animation> [nextState] [stopExpression] [controller] [blendOutTime]`

### C++ Core Modules

#### 1. Tokenizer (Lexical Analyzer)

**Responsibility**: Breaks command strings into token sequences

**Token Types**:
- `COMMAND` - Command (starting with `/`)
- `SELECTOR` - Selector (`@s`, `@p`, `@a`, `@e`, `@r`)
- `COORDINATE` - Coordinate (`~`, `^`, numbers)
- `STRING` - String (quoted)
- `NUMBER` - Number
- `IDENTIFIER` - Identifier
- `BRACKET` - Bracket (`[]`, `{}`)
- `OPERATOR` - Operator

#### 2. Parser

**Responsibility**: Analyzes token sequences, builds syntax tree

**Supported Formats**:
- Regular commands: `/give @p diamond_sword`
- Target selectors: `/tp @e[type=creeper] ~ ~ ~`
- NBT data: `/summon zombie ~ ~ ~ {Health:20f}`
- Scores: `/scoreboard players operation @p temp * @p score`

#### 3. Completion Engine

**Responsibility**: Generates smart completion suggestions based on context

**Completion Types**:
- **Command Completion** - Complete command names
- **Parameter Completion** - Complete command parameters
- **ID Completion** - Complete block/item/particle IDs
- **Selector Completion** - Complete selectors and parameters
- **Syntax Template** - Complete syntax templates

**Cache Optimization**:
- `SyntaxTemplateCache` - Syntax template cache
- LRU strategy with maximum 50 entries

#### 4. Command Registry

**Responsibility**: Manages and queries all available commands

**Data Structure**:
```cpp
struct CommandInfo {
    std::string name;
    std::string description;
    std::vector<Parameter> parameters;
    std::string syntax_template;
};
```

---

## Data Models

### Kotlin Data Classes

```kotlin
// Command Completion Item
data class CompletionItem(
    val label: String,        // Display label
    val detail: String,      // Detail description
    val insertText: String,  // Insert text
    val kind: CompletionKind // Completion type
)

// Syntax Hint
data class SyntaxHint(
    val template: String,      // Syntax template
    val description: String,    // Description
    val parameters: List<SyntaxParameter> // Parameter list
)

// Command Info
data class CommandInfo(
    val name: String,
    val syntax: String,
    val description: String,
    val category: String,
    val icon: ImageVector
)

// History Item
data class HistoryItem(
    val command: String,
    val timestamp: Long,
    val isSuccess: Boolean
)

// Saved Command
data class SavedCommand(
    val id: String,
    val name: String,
    val command: String,
    val createdAt: Long
)
```

### C++ Structs

```cpp
// Completion Result
struct CompletionResult {
    std::string label;
    std::string detail;
    std::string insert_text;
    CompletionKind kind;
};

// Syntax Template
struct SyntaxTemplate {
    std::string name;
    std::string template_str;
    std::vector<ParameterInfo> params;
};

// Token Definition
struct Token {
    TokenType type;
    std::string value;
    int position;
};
```

---

## Performance Optimization

### Android Side Optimization

#### 1. Compose Optimization

| Optimization | Implementation | Effect |
|--------------|----------------|--------|
| `@Stable` Annotation | Mark stable types | Reduce unnecessary recomposition |
| `remember` Cache | Cache computed results | Avoid redundant calculations |
| `key` Parameter | LazyList items key | Precise list item updates |
| Independent Composable | Extract independent components | Narrow recomposition scope |

#### 2. State Management Optimization

```kotlin
// Debounce handling
LaunchedEffect(searchQuery) {
    delay(150)  // 150ms debounce
    performSearch(searchQuery)
}

// Cache mechanism
private val completionCache = mutableMapOf<String, List<CompletionItem>>()

// Parallel execution
val completionsDeferred = viewModelScope.async { helper.getCompletions(...) }
val validationDeferred = viewModelScope.async { helper.validateCommand(...) }
```

#### 3. List Optimization

```kotlin
// Use key for better performance
LazyColumn {
    items(commands, key = { it.name }) { command ->
        CommandItem(command)
    }
}

// Cache static lists
val categories = remember { listOf("All", "Items", "Entities", ...) }
```

### C++ Side Optimization

#### 1. Cache Strategy

```cpp
class SyntaxTemplateCache {
private:
    std::unordered_map<std::string, std::string> cache_;
    std::mutex mutex_;
    static constexpr size_t MAX_CACHE_SIZE = 50;
    
public:
    std::string getOrCompute(const std::string& key, 
                             std::function<std::string()> compute);
};
```

#### 2. String Processing Optimization

```cpp
// Use inline to optimize string prefix checking
inline bool starts_with_fast(const std::string& str, 
                             const std::string& prefix) {
    if (prefix.size() > str.size()) return false;
    return str.compare(0, prefix.size(), prefix) == 0;
}
```

#### 3. Memory Management

```cpp
// Use unique_ptr to manage global objects
std::unique_ptr<CommandRegistry> g_registry;

// ResultCache to reduce redundant calculations
class ResultCache {
    std::unordered_map<std::string, std::string> cache_;
    static constexpr size_t MAX_CACHE_SIZE = 100;
};
```

---

## Theme System

### Preset Themes

| Theme | Description | Best For |
|-------|-------------|----------|
| Follow System | Auto-adapts to system light/dark mode | Daily use |
| Light Mode | Bright and fresh white theme | Outdoor daytime |
| Dark Mode | Eye-protective dark theme | Night use |
| Midnight Blue | Deep blue tone | Gaming environment |
| AMOLED Black | Pure black for power saving | OLED screens |
| Green Eye Care | Gentle green tone | Long reading |
| Ocean Blue | Fresh blue tone | Casual use |
| Warm Orange | Warm orange tone | Cozy atmosphere |

### Custom Themes

```kotlin
// Settings Manager
class SettingsManager {
    var currentTheme: AppTheme
    var cardOpacity: Float        // Card opacity (0.0-1.0)
    var glassIntensity: Float     // Glass blur intensity (0.0-1.0)
    var cardCornerRadius: Float  // Corner radius (dp)
    var useGlassmorphism: Boolean // Enable glassmorphism
    var useCustomBackground: Boolean
    var customBackgroundUri: String?
    var backgroundOpacity: Float
}
```

### Glassmorphism Effect

```kotlin
// Enable glassmorphism
val useGlassmorphism by remember { mutableStateOf(true) }
val glassIntensity by remember { mutableFloatStateOf(0.85f) }

Surface(
    modifier = Modifier
        .blur(radius = (20 * glassIntensity).dp)
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    surfaceColor.copy(alpha = 0.7f),
                    surfaceColor.copy(alpha = 0.9f)
                )
            )
        )
)
```

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
feat(library): Add animation library module

- Add MCAnimation data class
- Implement AnimationLibrary filtering and search
- Add UI components and command generation
- Performance optimization: LRU cache and index pre-computation

Closes #123
```

#### 3. Branch Management

```
main          # Main branch, stable version
├── develop   # Development branch
│   ├── feature/animation-library  # Feature branch
│   ├── fix/theme-switch-bug      # Fix branch
│   └── refactor/completion-engine # Refactor branch
```

### Adding New ID Library

1. Create data class:
```kotlin
data class NewItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val extra: String = ""
)
```

2. Create library object:
```kotlin
object NewItemLibrary {
    val categories = listOf("All", "Category1", "Category2")
    
    private val filterCache = LinkedHashMap<String, List<NewItem>>()
    
    val items = listOf(
        NewItem("example:item", "Example Item", "Category1", "This is an example")
    )
    
    fun filter(query: String, category: String?): List<NewItem> {
        // Implement filter logic
    }
    
    fun buildCommand(item: NewItem, vararg params: String): String {
        // Implement command generation
    }
}
```

3. Add UI component:
```kotlin
@Composable
fun NewItemLibraryTab(viewModel: MainViewModel) {
    // Implement UI
}
```

4. Integrate into main screen:
```kotlin
// MainActivity.kt
val subTabs = listOf("Blocks", "Items", "Sounds", "Particles", "Animations", "NewResource")
// Add corresponding when branches
```

### Running Tests

```bash
# C++ unit tests
cd Nexus-Core/build
ctest --output-on-failure

# Android unit tests
cd Nexus-Android
./gradlew testDebugUnitTest
```

### Reporting Issues

Please report issues via [GitHub Issues](https://github.com/your-repo/Nexus/issues) with:

1. Issue description
2. Reproduction steps
3. Expected behavior
4. Screenshots/logs
5. Environment info (Android version, app version)

---

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for detailed version history.

## License

This project is licensed under the MIT License - see [LICENSE](LICENSE) file for details.

## Reference Projects

- [CA_reforged](https://github.com/huangyxHUTAO/CA_reforged) - Command Helper Community Edition
- [CHelper](https://github.com/Yancey2023/CHelper-Core) - C++ Core Architecture Reference
- [Minecraft Wiki](https://minecraft.wiki) - Command Syntax and ID Data Reference

## Contact

- **Author**: Nexus Team
- **Email**: contact@nexuscmd.com
- **Website**: https://nexuscmd.com

---

<div align="center">

Made with ❤️ for Minecraft Community

</div>
