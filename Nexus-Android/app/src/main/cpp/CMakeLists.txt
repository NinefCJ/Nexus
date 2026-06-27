cmake_minimum_required(VERSION 3.22.1)

# Path to Nexus-Core
set(CORE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../Nexus-Core)

# Include C++ headers
include_directories(${CORE_DIR}/include)

# Add static library from core
add_library(core STATIC IMPORTED)
set_target_properties(core PROPERTIES IMPORTED_LOCATION
    ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/libcore.a)

# 设置 C++ 标准为 C++14（最稳定兼容的选择）
set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED True)

# Source files for JNI
set(JNI_SOURCES
    native.cpp
)

add_library(${CMAKE_PROJECT_NAME} SHARED ${JNI_SOURCES})

target_link_libraries(${CMAKE_PROJECT_NAME} core)

# Compile core library
add_library(core_lib STATIC
    ${CORE_DIR}/src/tokenizer.cpp
    ${CORE_DIR}/src/parser.cpp
    ${CORE_DIR}/src/completion.cpp
    ${CORE_DIR}/src/highlighter.cpp
    ${CORE_DIR}/src/command_registry.cpp
    ${CORE_DIR}/src/command_helper_jni.cpp
)

target_include_directories(core_lib PUBLIC ${CORE_DIR}/include)

# Link rapidjson and fmt (fetched by CMake)
# For Android, we use prebuilt or header-only versions
target_link_libraries(${CMAKE_PROJECT_NAME} core_lib)
