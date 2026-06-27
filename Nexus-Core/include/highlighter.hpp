#pragma once

#include "types.hpp"
#include <vector>

namespace mcmd {

class Highlighter {
public:
    // Generate highlight tokens for syntax highlighting
    std::vector<HighlightToken> highlight(const std::vector<Token>& tokens);

private:
    static TokenType mapTokenToHighlightType(TokenType token_type);
};

} // namespace mcmd
