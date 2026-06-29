#include "completion.hpp"
#include "types.hpp"
#include <algorithm>
#include <sstream>
#include <unordered_map>

namespace mcmd {

static const std::vector<std::string> SELECTORS = {
    "@p", "@a", "@e", "@s", "@r",
    "@p[tag=]", "@a[tag=]", "@e[tag=]", "@s[tag=]", "@r[tag=]"
};

static const std::vector<std::string> COORDINATE_PREFIXES = {
    "~", "^", "~0", "^0"
};

static const std::unordered_map<std::string, std::string> PARAM_HINTS = {
    {"targets", "目标玩家或实体 (如 @p, @a, @s, @e[type=zombie])"},
    {"item", "物品ID (如 minecraft:diamond, stone)"},
    {"amount", "数量 (默认: 1)"},
    {"pos", "坐标位置 (如 ~ ~ ~, 0 64 0)"},
    {"block", "方块ID (如 stone, minecraft:diamond_block)"},
    {"entity", "实体类型 (如 minecraft:zombie, minecraft:skeleton)"},
    {"destination", "目标位置或玩家"},
    {"from", "起点坐标"},
    {"to", "终点坐标"},
    {"effect", "效果ID (如 speed, jump_boost, invisibility)"},
    {"duration", "持续时间 (秒)"},
    {"amplifier", "效果等级 (0-255)"},
    {"player", "玩家名称"},
    {"message", "消息内容"},
    {"gamemode", "游戏模式 (creative, survival, adventure, spectator)"},
    {"rule", "规则名称"},
    {"value", "规则值"},
    {"score", "计分项名称"},
    {"objective", "记分板对象名称"},
    {"sound", "声音ID (如 minecraft:entity.player.levelup)"},
    {"particle", "粒子ID (如 minecraft:heart, minecraft:explode)"}
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
        if (starts_with(cmd_name, "/")) {
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
    if (starts_with(partial_param, "@")) {
        auto selector_completions = getSelectorCompletions(partial_param);
        results.insert(results.end(), selector_completions.begin(), selector_completions.end());
    }

    return results;
}

std::vector<CompletionItem> Completion::getSelectorCompletions(const std::string& partial) {
    std::vector<CompletionItem> results;

    for (const auto& selector : SELECTORS) {
        if (starts_with(selector, partial)) {
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
        if (starts_with(prefix, partial)) {
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

std::string Completion::getSyntaxHint(const std::string& input, size_t cursor_pos) {
    auto template_result = getSyntaxTemplate(input, cursor_pos);
    return template_result.template_str;
}

std::string Completion::getParameterHint(const std::string& command_name, size_t param_index) {
    const auto* cmd = registry_.findCommand(command_name);
    if (!cmd) return "";

    if (param_index < cmd->params.size()) {
        const auto& param = cmd->params[param_index];
        auto it = PARAM_HINTS.find(param.name);
        if (it != PARAM_HINTS.end()) {
            return it->second;
        }
        return param.description;
    }
    return "";
}

Completion::SyntaxTemplate Completion::getSyntaxTemplate(const std::string& input, size_t cursor_pos) {
    SyntaxTemplate result;
    result.active_param_start = 0;
    result.active_param_end = 0;
    result.active_param_index = 0;
    result.is_optional = false;

    if (input.empty() || !starts_with(input, "/")) {
        result.template_str = "";
        return result;
    }

    // Parse command name
    std::string remaining = input.substr(1);  // Remove leading /
    size_t space_pos = remaining.find(' ');

    std::string cmd_name;
    std::string args_str;

    if (space_pos == std::string::npos) {
        cmd_name = remaining;
        args_str = "";
    } else {
        cmd_name = remaining.substr(0, space_pos);
        args_str = remaining.substr(space_pos + 1);
    }

    const auto* cmd = registry_.findCommand(cmd_name);
    if (!cmd) {
        result.template_str = "/" + cmd_name;
        return result;
    }

    // Build template from command definition
    result.template_str = buildSyntaxTemplate(cmd, 0);
    result.active_param_hint = getParameterHint(cmd_name, 0);

    // Count how many args have been provided
    size_t args_provided = 0;
    if (!args_str.empty()) {
        // Count spaces to determine arg count
        size_t count = 1;
        for (char c : args_str) {
            if (c == ' ') count++;
        }
        args_provided = count;
    }

    result.active_param_index = args_provided;

    if (args_provided < cmd->params.size()) {
        result.is_optional = cmd->params[args_provided].required == false;
    } else {
        result.is_optional = true;
    }

    // Calculate active param position in the template
    size_t current_arg_idx = 0;
    size_t template_pos = 1;  // Start after '/'

    // Skip command name in template
    template_pos += cmd_name.length();

    for (size_t i = 0; i < cmd->params.size(); i++) {
        const auto& param = cmd->params[i];

        // Add space before param
        if (template_pos > 1) {  // Not right after '/'
            template_pos++;  // space
        }

        size_t param_name_start = template_pos;

        // Check if this is the active parameter
        if (i == args_provided) {
            result.active_param_start = param_name_start;
            result.active_param_end = param_name_start + param.name.length();
            result.active_param_name = param.name;
            result.is_optional = !param.required;
            auto hint_it = PARAM_HINTS.find(param.name);
            if (hint_it != PARAM_HINTS.end()) {
                result.active_param_hint = hint_it->second;
            } else {
                result.active_param_hint = param.description;
            }
        }

        template_pos += param.name.length();

        // Add brackets for optional params
        if (!param.required) {
            // Brackets are added by buildSyntaxTemplate, so we need to account for them
        }
    }

    return result;
}

std::string Completion::buildSyntaxTemplate(const CommandDef* cmd, size_t active_param_index) {
    std::ostringstream oss;
    oss << "/" << cmd->name;

    for (size_t i = 0; i < cmd->params.size(); i++) {
        const auto& param = cmd->params[i];
        oss << " ";

        if (!param.required) {
            oss << "[";
        }

        // Highlight the active parameter with special markers
        if (i == active_param_index) {
            oss << "<" << param.name << ">";
        } else {
            oss << param.name;
        }

        if (!param.required) {
            oss << "]";
        }
    }

    return oss.str();
}

} // namespace mcmd
