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

    // Get syntax hint for the current input (for inline syntax display)
    std::string getSyntaxHint(const std::string& input, size_t cursor_pos);

    // Get parameter hint at cursor position
    std::string getParameterHint(const std::string& command_name, size_t param_index);

    // Get formatted syntax template with markers for current position
    struct SyntaxTemplate {
        std::string template_str;    // Full template like "/give <targets> <item> [amount]"
        size_t active_param_start;    // Start position of current parameter
        size_t active_param_end;      // End position of current parameter
        size_t active_param_index;    // Which parameter is active (0-based)
        std::string active_param_name; // Name of active parameter
        std::string active_param_hint; // Hint for current parameter
        bool is_optional;             // Is current parameter optional
    };
    SyntaxTemplate getSyntaxTemplate(const std::string& input, size_t cursor_pos);

private:
    const CommandRegistry& registry_;
    std::string buildSyntaxTemplate(const CommandDef* cmd, size_t param_index);
};

} // namespace mcmd
