#include "parser.hpp"

namespace mcmd {

Parser::Parser(const std::vector<Token>& tokens) : tokens_(tokens) {}

std::shared_ptr<AstNode> Parser::parse() {
    auto root = std::make_shared<AstNode>();
    root->type = NodeType::Root;

    while (pos_ < tokens_.size()) {
        if (error_) break;

        auto node = parseCommand();
        if (node) {
            root->children.push_back(node);
        } else {
            break;
        }
    }

    return root;
}

std::shared_ptr<AstNode> Parser::parseCommand() {
    if (!check(TokenType::Command)) {
        if (!tokens_.empty()) {
            error_ = ParseError{
                "Expected command starting with /",
                tokens_[pos_].position,
                tokens_[pos_].length
            };
        }
        return nullptr;
    }

    auto cmd = std::make_shared<AstNode>();
    cmd->type = NodeType::Command;
    cmd->token_index = pos_;
    cmd->value = tokens_[pos_].value;
    advance();

    // Parse parameters
    while (pos_ < tokens_.size() && !check(TokenType::Command)) {
        if (check(TokenType::Selector)) {
            cmd->children.push_back(parseSelector());
        } else if (check(TokenType::Coordinates)) {
            cmd->children.push_back(parseCoordinates());
        } else {
            // Generic parameter
            auto param = std::make_shared<AstNode>();
            param->type = NodeType::Parameter;
            param->token_index = pos_;
            param->value = tokens_[pos_].value;
            advance();
            cmd->children.push_back(param);
        }
    }

    return cmd;
}

std::shared_ptr<AstNode> Parser::parseSelector() {
    auto selector = std::make_shared<AstNode>();
    selector->type = NodeType::Selector;
    selector->token_index = pos_;
    selector->value = tokens_[pos_].value;
    advance();
    return selector;
}

std::shared_ptr<AstNode> Parser::parseCoordinates() {
    auto coords = std::make_shared<AstNode>();
    coords->type = NodeType::Coordinates;
    coords->token_index = pos_;
    coords->value = tokens_[pos_].value;
    advance();
    return coords;
}

bool Parser::check(TokenType type) const {
    return pos_ < tokens_.size() && tokens_[pos_].type == type;
}

bool Parser::match(TokenType type) {
    if (check(type)) {
        advance();
        return true;
    }
    return false;
}

void Parser::advance() {
    if (pos_ < tokens_.size()) {
        pos_++;
    }
}

} // namespace mcmd
