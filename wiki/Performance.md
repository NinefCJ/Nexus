# 性能优化

Nexus 在多个层面进行了性能优化，确保在移动端也能流畅运行。本文档介绍主要的优化策略和调优方法。

---

## 优化总览

```
┌─────────────────────────────────────────┐
│         UI 层 (Jetpack Compose)          │
│  • remember 缓存                         │
│  • @Stable 注解                          │
│  • LazyList key 优化                     │
│  • 防抖处理                              │
├─────────────────────────────────────────┤
│         业务层 (ViewModel)                │
│  • 补全结果缓存                          │
│  • 并行 Native 调用                      │
│  • 协程调度优化                          │
├─────────────────────────────────────────┤
│         JNI 桥接层                        │
│  • 批量数据传递                          │
│  • 类引用缓存                            │
│  • 字符串预分配                          │
├─────────────────────────────────────────┤
│         C++ 核心层                        │
│  • LRU 缓存策略                          │
│  • 字符串优化                            │
│  • 内存池                                │
│  • 快速路径优化                          │
└─────────────────────────────────────────┘
```

---

## UI 层优化

### 1. Compose 重组优化

| 优化技术 | 说明 | 效果 |
|---------|------|------|
| `@Stable` 注解 | 标记稳定类型，告知编译器类型不变 | 跳过不必要的重组比较 |
| `remember` | 缓存计算结果，重组时复用 | 避免重复计算 |
| `derivedStateOf` | 派生状态，仅在依赖变化时更新 | 减少无关状态触发的重组 |
| 独立 Composable | 提取小型独立组件 | 缩小重组范围 |

**示例：@Stable 状态类**

```kotlin
@Stable
data class MainUiState(
    val commandText: String = "",
    val completions: List<CompletionItem> = emptyList()
)
```

### 2. 列表性能优化

```kotlin
// 使用 key 参数确保精准更新
LazyColumn {
    items(
        items = completions,
        key = { it.label }  // 唯一稳定的 key
    ) { item ->
        CompletionItemView(item)
    }
}
```

**优化要点**：
- 为 `LazyColumn`/`LazyRow` 的 item 提供稳定的 key
- 避免在 item lambda 中创建新对象
- 使用 `remember` 缓存静态列表数据

### 3. 防抖处理

输入框文本变化时使用防抖，避免频繁触发补全计算：

```kotlin
private const val COMPLETION_DEBOUNCE_MS = 150L

LaunchedEffect(commandText) {
    delay(COMPLETION_DEBOUNCE_MS)
    updateCompletions(commandText, cursorPosition)
}
```

**防抖延迟选择**：

| 场景 | 推荐延迟 | 说明 |
|------|----------|------|
| 补全建议 | 150ms | 平衡响应速度和性能 |
| 搜索过滤 | 200-300ms | 减少搜索计算频率 |
| 语法校验 | 300ms | 不需要太频繁 |

---

## 业务层优化

### 1. 补全结果缓存

```kotlin
private val completionCache = LinkedHashMap<String, List<CompletionItem>>(
    50, 0.75f, true  // accessOrder = true → LRU
)

fun getCompletions(command: String, cursor: Int): List<CompletionItem> {
    val key = "$command:$cursor"
    completionCache[key]?.let { return it }
    
    val result = nativeGetCompletions(command, cursor)
    completionCache[key] = result
    return result
}
```

**缓存策略**：
- LRU 淘汰策略
- 最大缓存 50 条
- 缓存 key = 命令文本 + 光标位置

### 2. 并行 Native 调用

使用 `async` 并行执行多个 Native 调用，减少总等待时间：

```kotlin
suspend fun updateCommandState(text: String, cursor: Int) {
    val completionsDeferred = viewModelScope.async {
        helper.getCompletions(text, cursor)
    }
    val validationDeferred = viewModelScope.async {
        helper.validateCommand(text)
    }
    val syntaxHintDeferred = viewModelScope.async {
        helper.getSyntaxHint(text, cursor)
    }
    
    // 等待所有并行任务完成
    val completions = completionsDeferred.await()
    val validation = validationDeferred.await()
    val syntaxHint = syntaxHintDeferred.await()
    
    // 更新 UI 状态
    _uiState.update { it.copy(...) }
}
```

**性能提升**：串行需要 T1+T2+T3，并行只需要 max(T1, T2, T3)。

### 3. 协程调度

- CPU 密集型任务使用 `Dispatchers.Default`
- I/O 操作使用 `Dispatchers.IO`
- UI 更新始终在 `Dispatchers.Main`

```kotlin
viewModelScope.launch(Dispatchers.Default) {
    // 后台线程执行重计算
    val result = heavyComputation()
    
    withContext(Dispatchers.Main) {
        // 主线程更新 UI
        updateUi(result)
    }
}
```

---

## JNI 层优化

### 1. 减少 JNI 调用次数

- 批量获取数据，一次调用返回多个结果
- 避免在循环中频繁调用 JNI 方法

### 2. 缓存 JNI 引用

```cpp
// 全局缓存 jclass 和 jmethodID
static jclass completionItemClass = nullptr;
static jmethodID completionItemCtor = nullptr;

JNIEXPORT void JNICALL
Java_com_nexuscmd_CommandHelper_nativeInit(JNIEnv *env, jobject thiz) {
    // 初始化时查找并缓存
    jclass cls = env->FindClass("com/nexuscmd/CompletionItem");
    completionItemClass = (jclass)env->NewGlobalRef(cls);
    completionItemCtor = env->GetMethodID(completionItemClass, "<init>", "...");
}
```

### 3. 字符串传递优化

- 优先使用 `GetStringUTFChars` / `ReleaseStringUTFChars`
- 避免在 C++ 层持有 jstring 引用
- 大字符串考虑使用直接缓冲区

---

## C++ 核心层优化

### 1. LRU 缓存

```cpp
class SyntaxTemplateCache {
private:
    std::unordered_map<std::string, std::string> cache_;
    std::list<std::string> lru_list_;
    std::mutex mutex_;
    static constexpr size_t MAX_CACHE_SIZE = 50;

public:
    std::string getOrCompute(const std::string& key,
                             std::function<std::string()> compute) {
        std::lock_guard<std::mutex> lock(mutex_);
        
        auto it = cache_.find(key);
        if (it != cache_.end()) {
            // 移动到 LRU 列表头部
            lru_list_.remove(key);
            lru_list_.push_front(key);
            return it->second;
        }
        
        // 计算新值
        std::string value = compute();
        
        // 插入缓存
        if (cache_.size() >= MAX_CACHE_SIZE) {
            // 淘汰最久未使用的
            std::string oldest = lru_list_.back();
            lru_list_.pop_back();
            cache_.erase(oldest);
        }
        
        cache_[key] = value;
        lru_list_.push_front(key);
        return value;
    }
};
```

### 2. 字符串优化

```cpp
// 内联前缀检查，避免函数调用开销
inline bool starts_with_fast(const std::string& str,
                             const std::string& prefix) {
    if (prefix.size() > str.size()) return false;
    return str.compare(0, prefix.size(), prefix) == 0;
}

// 优先使用 string_view 避免拷贝
std::string_view trim(std::string_view s) {
    size_t start = s.find_first_not_of(" \t");
    size_t end = s.find_last_not_of(" \t");
    if (start == std::string_view::npos) return {};
    return s.substr(start, end - start + 1);
}
```

### 3. 内存管理

```cpp
// 使用智能指针自动管理生命周期
std::unique_ptr<CommandRegistry> g_registry;

// 预分配 vector 容量
std::vector<Token> tokens;
tokens.reserve(32);  // 预估最大 token 数

// Result 缓存减少重复计算
class ResultCache {
    std::unordered_map<std::string, std::vector<CompletionItem>> cache_;
    static constexpr size_t MAX_CACHE_SIZE = 100;
};
```

### 4. 快速路径优化

对常见情况进行快速路径处理，避免走完整流程：

```cpp
std::vector<CompletionItem> getCompletions(...) {
    // 快速路径：空输入
    if (input.empty()) {
        return getAllCommands();
    }
    
    // 快速路径：只输入了 /
    if (input == "/") {
        return getAllCommands();
    }
    
    // 正常路径...
}
```

---

## 性能指标

### 目标性能

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 补全响应时间 | < 50ms | 从输入变化到补全显示 |
| 高亮渲染 | < 10ms | 语法高亮计算 |
| 启动时间 | < 500ms | 冷启动到主界面 |
| 内存占用 | < 100MB | 运行时内存 |
| 帧率 | 稳定 60fps | 列表滚动等动画 |

### 性能测试方法

```bash
# Android 性能分析
# 使用 Android Studio Profiler

# C++ 基准测试
cd Nexus-Core/build
cmake .. -DCMAKE_BUILD_TYPE=Release
./core_tests --gtest_filter=*Perf*
```

---

## 常见性能问题排查

### 1. 补全卡顿

可能原因：
- 防抖时间太短 → 增加到 200ms
- 缓存命中率太低 → 检查缓存策略
- ID 库数据量过大 → 考虑分批加载

### 2. 列表滑动卡顿

可能原因：
- 缺少 key 参数 → 添加稳定的 key
- item 布局太复杂 → 简化布局
- 图片加载阻塞 → 使用异步加载

### 3. 启动慢

可能原因：
- 初始化加载数据太多 → 延迟加载非核心数据
- 主线程阻塞 → 将初始化移到后台线程
- 依赖库初始化慢 → 懒加载

---

## 更多阅读

- [技术架构](Architecture) — 整体架构设计
- [核心模块](Core-Modules) — C++ 核心模块详解
- [Android 应用](Android-App) — Android 端架构
