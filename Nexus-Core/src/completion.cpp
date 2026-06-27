#include "completion.hpp"
#include <algorithm>

namespace mcmd {

static const std::vector<std::string> SELECTORS = {
    "@p", "@a", "@e", "@s", "@r",
    "@p[tag=]", "@a[tag=]", "@e[tag=]", "@s[tag=]", "@r[tag=]"
};

static const std::vector<std::string> COORDINATE_PREFIXES = {
    "~", "^", "~0", "^0"
};

Completion::Completion(const CommandRegistry& registry) : registry_(registry) {}

std::vector<CompletionItem> Completion::getCompletions(
    const std::vector<Token>& tokens,
    size_t cursor_position,
    const std::string& partial_input
) {
    // If no input yet, return all commands
    if (partial_input.empty() || partial_input == "/") {
        return getCommandCompletions("", "");
    }

    // Check if we're completing a command name
    if (tokens.empty() || (tokens.size() == 1 && tokens[0].type == TokenType::Command)) {
        std::string cmd_name = partial_input;
        if (cmd_name.starts_with("/")) {
            cmd_name = cmd_name.substr(1);
        }
        return getCommandCompletions(cmd_name, "");
    }

    // We're completing parameters
    if (!tokens.empty() && tokens[0].type == TokenType::Command) {
        return getCommandCompletions(tokens[0].value, partial_input);
    }

    return {};
}

std::vector<CompletionItem> Completion::getCommandCompletions(
    const std::string& command_name,
    const std::string& partial_param
) {
    std::vector<CompletionItem> results;

    // Find commands matching the prefix
    auto commands = registry_.getCommandsStartingWith(command_name);
    for (const auto* cmd : commands) {
        results.push_back({
            "/" + cmd->name,
            cmd->description,
            "/" + cmd->name + " ",
            CompletionItem::Kind::Command
        });
    }

    // If partial_param starts with @, provide selector completions
    if (partial_param.starts_with("@")) {
        auto selector_completions = getSelectorCompletions(partial_param);
        results.insert(results.end(), selector_completions.begin(), selector_completions.end());
    }

    return results;
}

std::vector<CompletionItem> Completion::getSelectorCompletions(const std::string& partial) {
    std::vector<CompletionItem> results;

    for (const auto& selector : SELECTORS) {
        if (selector.starts_with(partial)) {
            results.push_back({
                selector,
                "Target selector",
                selector,
                CompletionItem::Kind::Selector
            });
        }
    }

    return results;
}

std::vector<CompletionItem> Completion::getCoordinateCompletions(const std::string& partial) {
    std::vector<CompletionItem> results;

    for (const auto& prefix : COORDINATE_PREFIXES) {
        if (prefix.starts_with(partial)) {
            results.push_back({
                prefix,
                "Coordinate",
                prefix,
                CompletionItem::Kind::Value
            });
        }
    }

    return results;
}

} // namespace mcmd
