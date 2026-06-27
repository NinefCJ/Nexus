#pragma once

#include <string>
#include <vector>
#include <variant>
#include <optional>

namespace mcmd {

// Token types for Minecraft commands
enum class TokenType {
    Command,       // /give, /tp, etc.
    Selector,      // @p, @a, @e, @s
    String,        // quoted strings
    Number,        // integers and decimals
    Coordinates,   // ~, ^, absolute coords
    ItemId,        // minecraft:stone
    Option,        // -1, optional params
    Bracket,       // [], ()
    Error
};

struct Token {
    TokenType type;
    std::string value;
    size_t position;
    size_t length;
};

struct Position {
    size_t line;
    size_t column;
    size_t offset;
};

// Command parameter types
enum class ParamType {
    Selector,
    ItemId,
    Integer,
    Float,
    String,
    Coordinates,
    Boolean,
    BlockState,
    Component,
    Custom
};

struct ParamDef {
    std::string name;
    ParamType type;
    std::string description;
    bool required;
    std::optional<std::string> default_value;
    std::vector<std::string> suggestions;  // For enum-like params
};

// Command definition
struct CommandDef {
    std::string name;
    std::string syntax;
    std::string description;
    std::vector<ParamDef> params;
};

// Completion suggestion
struct CompletionItem {
    std::string label;
    std::string detail;
    std::string insert_text;
    enum class Kind { Command, Selector, Parameter, Value };
    Kind kind;
};

// Highlight token for syntax highlighting
struct HighlightToken {
    TokenType type;
    size_t start;
    size_t end;
};

// Parse error
struct ParseError {
    std::string message;
    size_t position;
    size_t length;
};

inline bool starts_with(const std::string& str, const std::string& prefix) {
    return str.size() >= prefix.size() && str.compare(0, prefix.size(), prefix) == 0;
}

} // namespace mcmd
