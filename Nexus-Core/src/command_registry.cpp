#include "command_registry.hpp"
#include <rapidjson/document.h>
#include <fstream>

namespace mcmd {

CommandRegistry::CommandRegistry() {
    // Register built-in commands
    commands_ = {
        {"give", CommandDef{
            "give",
            "/give <targets> <item> [amount] [components]",
            "向指定玩家给予物品",
            {
                {"targets", ParamType::Selector, "目标玩家或实体", true},
                {"item", ParamType::ItemId, "物品ID", true},
                {"amount", ParamType::Integer, "数量", false, "1"}
            }
        }},
        {"summon", CommandDef{
            "summon",
            "/summon <entity> [pos] [components]",
            "生成实体",
            {
                {"entity", ParamType::String, "实体类型", true},
                {"pos", ParamType::Coordinates, "生成位置", false}
            }
        }},
        {"tp", CommandDef{
            "tp",
            "/tp [targets] <destination>",
            "传送玩家",
            {
                {"targets", ParamType::Selector, "目标玩家", false, "@s"},
                {"destination", ParamType::Selector, "目标位置/玩家", true}
            }
        }},
        {"setblock", CommandDef{
            "setblock",
            "/setblock <pos> <block> [blockstate] [components]",
            "放置方块",
            {
                {"pos", ParamType::Coordinates, "位置", true},
                {"block", ParamType::ItemId, "方块ID", true}
            }
        }},
        {"fill", CommandDef{
            "fill",
            "/fill <from> <to> <block> [options]",
            "填充区域",
            {
                {"from", ParamType::Coordinates, "起点", true},
                {"to", ParamType::Coordinates, "终点", true},
                {"block", ParamType::ItemId, "方块ID", true}
            }
        }}
    };
}

bool CommandRegistry::loadFromJson(const std::string& json_content) {
    rapidjson::Document doc;
    if (doc.Parse(json_content.c_str()).HasParseError()) {
        return false;
    }

    if (!doc.HasMember("commands")) return false;

    const auto& commands = doc["commands"];
    for (auto it = commands.MemberBegin(); it != commands.MemberEnd(); ++it) {
        CommandDef def;
        def.name = it->name.GetString();

        if (it->value.HasMember("syntax")) {
            def.syntax = it->value["syntax"].GetString();
        }
        if (it->value.HasMember("description")) {
            def.description = it->value["description"].GetString();
        }

        commands_[def.name] = def;
    }

    return true;
}

bool CommandRegistry::loadFromFile(const std::string& file_path) {
    std::ifstream file(file_path);
    if (!file.is_open()) return false;

    std::string content((std::istreambuf_iterator<char>(file)),
                        std::istreambuf_iterator<char>());
    return loadFromJson(content);
}

const CommandDef* CommandRegistry::findCommand(const std::string& name) const {
    auto it = commands_.find(name);
    if (it != commands_.end()) {
        return &it->second;
    }
    return nullptr;
}

std::vector<std::string> CommandRegistry::getCommandNames() const {
    std::vector<std::string> names;
    for (const auto& [name, _] : commands_) {
        names.push_back(name);
    }
    return names;
}

std::vector<const CommandDef*> CommandRegistry::getCommandsStartingWith(const std::string& prefix) const {
    std::vector<const CommandDef*> results;
    for (const auto& [name, def] : commands_) {
        if (name.find(prefix) == 0) {
            results.push_back(&def);
        }
    }
    return results;
}

} // namespace mcmd
