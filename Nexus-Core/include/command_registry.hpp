#pragma once

#include "types.hpp"
#include <string>
#include <unordered_map>
#include <vector>

namespace mcmd {

class CommandRegistry {
public:
    CommandRegistry();

    // Load commands from JSON
    bool loadFromJson(const std::string& json_content);
    bool loadFromFile(const std::string& file_path);

    // Find command definition
    const CommandDef* findCommand(const std::string& name) const;

    // Get all command names
    std::vector<std::string> getCommandNames() const;

    // Get commands starting with prefix (for completion)
    std::vector<const CommandDef*> getCommandsStartingWith(const std::string& prefix) const;

private:
    std::unordered_map<std::string, CommandDef> commands_;
};

} // namespace mcmd
