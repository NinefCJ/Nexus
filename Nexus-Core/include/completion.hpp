#pragma once

#include "types.hpp"
#include "command_registry.hpp"
#include <vector>

namespace mcmd {

class Completion {
public:
    Completion(const CommandRegistry& registry);

    // Get completions at cursor position
    std::vector<CompletionItem> getCompletions(
        const std::vector<Token>& tokens,
        size_t cursor_position,
        const std::string& partial_input
    );

    // Get completions based on current command context
    std::vector<CompletionItem> getCommandCompletions(
        const std::string& command_name,
        const std::string& partial_param
    );

    // Get selector completions (@p, @a, @e, etc.)
    std::vector<CompletionItem> getSelectorCompletions(const std::string& partial);

    // Get coordinate completions
    std::vector<CompletionItem> getCoordinateCompletions(const std::string& partial);

private:
    const CommandRegistry& registry_;
};

} // namespace mcmd
