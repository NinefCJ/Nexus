#include "highlighter.hpp"

namespace mcmd {

std::vector<HighlightToken> Highlighter::highlight(const std::vector<Token>& tokens) {
    std::vector<HighlightToken> highlights;

    for (const auto& token : tokens) {
        highlights.push_back({
            mapTokenToHighlightType(token.type),
            token.position,
            token.position + token.length
        });
    }

    return highlights;
}

TokenType Highlighter::mapTokenToHighlightType(TokenType token_type) {
    // For now, return the same type
    // In a full implementation, this would map to editor-specific categories
    return token_type;
}

} // namespace mcmd
