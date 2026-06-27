#pragma once

#include <string>
#include <vector>
#include "types.hpp"

#ifdef __ANDROID__
#include <jni.h>
#define EXPORT extern "C" JNIEXPORT
#else
#define EXPORT
#endif

namespace mcmd {

class CommandHelperJni {
public:
    // Initialize the command registry with JSON data
    static bool initialize(const std::string& json_data);

    // Get completion suggestions
    static std::string getCompletions(const std::string& input, int cursor_position);

    // Get syntax highlights
    static std::string getHighlights(const std::string& input);

    // Validate command and get errors
    static std::string validateCommand(const std::string& input);

    // Get command info
    static std::string getCommandInfo(const std::string& command_name);
};

} // namespace mcmd
