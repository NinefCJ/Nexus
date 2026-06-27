#pragma once

#include "types.hpp"
#include <string_view>

namespace mcmd {

class Tokenizer {
public:
    explicit Tokenizer(std::string_view input);

    std::vector<Token> tokenize();
    const std::optional<ParseError>& error() const { return error_; }

private:
    void nextChar();
    void skipWhitespace();
    Token readCommand();
    Token readSelector();
    Token readNumber();
    Token readCoordinates();
    Token readItemId();
    Token readString();
    Token readBracket();

    char peek() const { return pos_ < input_.size() ? input_[pos_] : '\0'; }
    char peekNext() const { return pos_ + 1 < input_.size() ? input_[pos_ + 1] : '\0'; }

    std::string_view input_;
    size_t pos_ = 0;
    size_t start_ = 0;
    std::optional<ParseError> error_;
};

} // namespace mcmd
