#include "command_helper_jni.hpp"
#include "tokenizer.hpp"
#include "parser.hpp"
#include "completion.hpp"
#include "highlighter.hpp"
#include "command_registry.hpp"
#include <rapidjson/stringbuffer.h>
#include <rapidjson/writer.h>

namespace mcmd {

static CommandRegistry* g_registry = nullptr;
static Completion* g_completion = nullptr;

bool CommandHelperJni::initialize(const std::string& json_data) {
    if (!g_registry) {
        g_registry = new CommandRegistry();
    }
    if (!g_completion) {
        g_completion = new Completion(*g_registry);
    }

    if (!json_data.empty()) {
        return g_registry->loadFromJson(json_data);
    }
    return true;
}

std::string CommandHelperJni::getCompletions(const std::string& input, int cursor_position) {
    if (!g_completion) return "[]";

    Tokenizer tokenizer(input);
    auto tokens = tokenizer.tokenize();

    auto completions = g_completion->getCompletions(tokens, cursor_position, input);

    rapidjson::StringBuffer sb;
    rapidjson::Writer<rapidjson::StringBuffer> writer(sb);

    writer.StartArray();
    for (const auto& item : completions) {
        writer.StartObject();
        writer.Key("label"); writer.String(item.label.c_str());
        writer.Key("detail"); writer.String(item.detail.c_str());
        writer.Key("insertText"); writer.String(item.insert_text.c_str());
        writer.EndObject();
    }
    writer.EndArray();

    return sb.GetString();
}

std::string CommandHelperJni::getHighlights(const std::string& input) {
    Tokenizer tokenizer(input);
    auto tokens = tokenizer.tokenize();

    Highlighter highlighter;
    auto highlights = highlighter.highlight(tokens);

    rapidjson::StringBuffer sb;
    rapidjson::Writer<rapidjson::StringBuffer> writer(sb);

    writer.StartArray();
    for (const auto& h : highlights) {
        writer.StartObject();
        writer.Key("type"); writer.Int(static_cast<int>(h.type));
        writer.Key("start"); writer.Int(h.start);
        writer.Key("end"); writer.Int(h.end);
        writer.EndObject();
    }
    writer.EndArray();

    return sb.GetString();
}

std::string CommandHelperJni::validateCommand(const std::string& input) {
    Tokenizer tokenizer(input);
    auto tokens = tokenizer.tokenize();

    if (tokenizer.error()) {
        rapidjson::StringBuffer sb;
        rapidjson::Writer<rapidjson::StringBuffer> writer(sb);
        writer.StartObject();
        writer.Key("hasError"); writer.Bool(true);
        writer.Key("message"); writer.String(tokenizer.error()->message.c_str());
        writer.Key("position"); writer.Int(tokenizer.error()->position);
        writer.EndObject();
        return sb.GetString();
    }

    Parser parser(tokens);
    auto ast = parser.parse();

    if (parser.error()) {
        rapidjson::StringBuffer sb;
        rapidjson::Writer<rapidjson::StringBuffer> writer(sb);
        writer.StartObject();
        writer.Key("hasError"); writer.Bool(true);
        writer.Key("message"); writer.String(parser.error()->message.c_str());
        writer.Key("position"); writer.Int(parser.error()->position);
        writer.EndObject();
        return sb.GetString();
    }

    return "{\"hasError\":false}";
}

std::string CommandHelperJni::getCommandInfo(const std::string& command_name) {
    if (!g_registry) return "{}";

    const auto* cmd = g_registry->findCommand(command_name);
    if (!cmd) return "{}";

    rapidjson::StringBuffer sb;
    rapidjson::Writer<rapidjson::StringBuffer> writer(sb);

    writer.StartObject();
    writer.Key("name"); writer.String(cmd->name.c_str());
    writer.Key("syntax"); writer.String(cmd->syntax.c_str());
    writer.Key("description"); writer.String(cmd->description.c_str());
    writer.EndObject();

    return sb.GetString();
}

std::string CommandHelperJni::getSyntaxHint(const std::string& input, int cursor_position) {
    if (!g_completion) return "{}";

    auto template_result = g_completion->getSyntaxTemplate(input, static_cast<size_t>(cursor_position));

    rapidjson::StringBuffer sb;
    rapidjson::Writer<rapidjson::StringBuffer> writer(sb);

    writer.StartObject();
    writer.Key("template"); writer.String(template_result.template_str.c_str());
    writer.Key("activeParamStart"); writer.Int(template_result.active_param_start);
    writer.Key("activeParamEnd"); writer.Int(template_result.active_param_end);
    writer.Key("activeParamIndex"); writer.Int(template_result.active_param_index);
    writer.Key("activeParamName"); writer.String(template_result.active_param_name.c_str());
    writer.Key("activeParamHint"); writer.String(template_result.active_param_hint.c_str());
    writer.Key("isOptional"); writer.Bool(template_result.is_optional);
    writer.EndObject();

    return sb.GetString();
}

std::string CommandHelperJni::getParameterHint(const std::string& command_name, int param_index) {
    if (!g_completion) return "{}";

    auto hint = g_completion->getParameterHint(command_name, static_cast<size_t>(param_index));

    rapidjson::StringBuffer sb;
    rapidjson::Writer<rapidjson::StringBuffer> writer(sb);

    writer.StartObject();
    writer.Key("hint"); writer.String(hint.c_str());
    writer.EndObject();

    return sb.GetString();
}

} // namespace mcmd
