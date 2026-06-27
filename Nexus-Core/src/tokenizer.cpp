#include "tokenizer.hpp"
#include <cctype>

namespace mcmd {

Tokenizer::Tokenizer(std::string_view input) : input_(input) {}

std::vector<Token> Tokenizer::tokenize() {
    std::vector<Token> tokens;

    while (pos_ < input_.size()) {
        skipWhitespace();
        if (pos_ >= input_.size()) break;

        start_ = pos_;
        char c = peek();

        if (c == '/') {
            tokens.push_back(readCommand());
        } else if (c == '@') {
            tokens.push_back(readSelector());
        } else if (c == '"') {
            tokens.push_back(readString());
        } else if (std::isdigit(c) || c == '-' || c == '~') {
            tokens.push_back(readCoordinates());
        } else if (c == '[' || c == ']') {
            tokens.push_back(readBracket());
        } else if (std::isalnum(c) || c == ':' || c == '_') {
            tokens.push_back(readItemId());
        } else {
            // Skip unknown characters
            nextChar();
        }

        if (error_) break;
    }

    return tokens;
}

void Tokenizer::nextChar() {
    if (pos_ < input_.size()) {
        pos_++;
    }
}

void Tokenizer::skipWhitespace() {
    while (pos_ < input_.size() && std::isspace(input_[pos_])) {
        pos_++;
    }
}

Token Tokenizer::readCommand() {
    // Skip the leading '/'
    nextChar();

    while (pos_ < input_.size() && (std::isalnum(input_[pos_]) || input_[pos_] == '_')) {
        nextChar();
    }

    return Token{
        TokenType::Command,
        std::string(input_.substr(start_, pos_ - start_)),
        start_,
        pos_ - start_
    };
}

Token Tokenizer::readSelector() {
    // Read @ prefix
    nextChar();  // skip '@'

    // Read selector type: p, a, e, s, r, or full selector with [...]
    if (pos_ < input_.size() && std::isalpha(input_[pos_])) {
        nextChar();  // single letter selector
    } else if (peek() == '[') {
        // Full selector like @e[type=zombie]
        size_t bracket_start = pos_;
        nextChar();
        while (pos_ < input_.size() && peek() != ']') {
            if (peek() == '[') {
                // Nested bracket - skip for now
            }
            nextChar();
        }
        if (peek() == ']') nextChar();
    }

    return Token{
        TokenType::Selector,
        std::string(input_.substr(start_, pos_ - start_)),
        start_,
        pos_ - start_
    };
}

Token Tokenizer::readNumber() {
    // Handle negative
    if (peek() == '-') nextChar();

    while (pos_ < input_.size() && (std::isdigit(input_[pos_]) || input_[pos_] == '.')) {
        nextChar();
    }

    return Token{
        TokenType::Number,
        std::string(input_.substr(start_, pos_ - start_)),
        start_,
        pos_ - start_
    };
}

Token Tokenizer::readCoordinates() {
    bool has_tilde = false;
    bool has_caret = false;

    if (peek() == '~' || peek() == '^') {
        has_tilde = peek() == '~';
        has_caret = peek() == '^';
        nextChar();
    }

    // Read number part
    while (pos_ < input_.size() &&
           (std::isdigit(input_[pos_]) || input_[pos_] == '.' || input_[pos_] == '-')) {
        nextChar();
    }

    return Token{
        TokenType::Coordinates,
        std::string(input_.substr(start_, pos_ - start_)),
        start_,
        pos_ - start_
    };
}

Token Tokenizer::readItemId() {
    while (pos_ < input_.size() &&
           (std::isalnum(input_[pos_]) || input_[pos_] == ':' ||
            input_[pos_] == '_' || input_[pos_] == '-')) {
        nextChar();
    }

    return Token{
        TokenType::ItemId,
        std::string(input_.substr(start_, pos_ - start_)),
        start_,
        pos_ - start_
    };
}

Token Tokenizer::readString() {
    nextChar();  // skip opening quote

    while (pos_ < input_.size() && peek() != '"') {
        if (peek() == '\\' && peekNext() == '"') {
            nextChar();  // skip escape
        }
        nextChar();
    }

    if (peek() == '"') nextChar();  // skip closing quote

    return Token{
        TokenType::String,
        std::string(input_.substr(start_, pos_ - start_)),
        start_,
        pos_ - start_
    };
}

Token Tokenizer::readBracket() {
    char bracket = peek();
    nextChar();

    return Token{
        TokenType::Bracket,
        std::string(1, bracket),
        start_,
        1
    };
}

} // namespace mcmd
