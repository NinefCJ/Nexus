#pragma once

#include "types.hpp"
#include <memory>
#include <vector>

namespace mcmd {

// AST node types
enum class NodeType {
    Root,
    Command,
    Parameter,
    Selector,
    Coordinates
};

struct AstNode {
    NodeType type;
    std::string value;
    size_t token_index;
    std::vector<std::shared_ptr<AstNode>> children;
};

class Parser {
public:
    explicit Parser(const std::vector<Token>& tokens);

    std::shared_ptr<AstNode> parse();
    const std::optional<ParseError>& error() const { return error_; }

private:
    std::shared_ptr<AstNode> parseCommand();
    std::shared_ptr<AstNode> parseParameter();
    std::shared_ptr<AstNode> parseSelector();
    std::shared_ptr<AstNode> parseCoordinates();

    bool check(TokenType type) const;
    bool match(TokenType type);
    void advance();

    const std::vector<Token>& tokens_;
    size_t pos_ = 0;
    std::optional<ParseError> error_;
};

} // namespace mcmd
