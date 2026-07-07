cmake_minimum_required(VERSION 3.16)
project(MCCommandHelper-Core VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Export compile commands for IDE support
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

# Source files
set(SOURCES
    src/tokenizer.cpp
    src/parser.cpp
    src/completion.cpp
    src/highlighter.cpp
    src/command_registry.cpp
    src/command_helper_jni.cpp
)

# Headers
set(HEADERS
    include/tokenizer.hpp
    include/parser.hpp
    include/completion.hpp
    include/highlighter.hpp
    include/command_registry.hpp
    include/command_helper_jni.hpp
    include/types.hpp
)

# Create library
add_library(${PROJECT_NAME} STATIC ${SOURCES} ${HEADERS})

# Include directories
target_include_directories(${PROJECT_NAME} PUBLIC
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
    $<INSTALL_INTERFACE:include>
)

# Public dependencies (using FetchContent for vendoring)
include(FetchContent)

FetchContent_Declare(
    rapidjson
    GIT_REPOSITORY https://github.com/Tencent/rapidjson.git
    GIT_TAG v1.1.0
)
FetchContent_MakeAvailable(rapidjson)

FetchContent_Declare(
    fmt
    GIT_REPOSITORY https://github.com/fmtlib/fmt.git
    GIT_TAG 10.2.1
)
FetchContent_MakeAvailable(fmt)

target_link_libraries(${PROJECT_NAME} PUBLIC rapidjson fmt)

# Testing
enable_testing()
FetchContent_Declare(
    googletest
    GIT_REPOSITORY https://github.com/google/googletest.git
    GIT_TAG v1.14.0
)
FetchContent_MakeAvailable(googletest)

add_executable(core_tests
    tests/tokenizer_test.cpp
    tests/parser_test.cpp
)
target_link_libraries(core_tests PRIVATE ${PROJECT_NAME} gtest gtest_main)
include(GoogleTest)
gtest_discover_tests(core_tests)
