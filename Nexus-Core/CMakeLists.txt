cmake_minimum_required(VERSION 3.21)
project(CHelper LANGUAGES CXX)

# using c++ 20
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# export compile commands
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

# Enable Compile warning
if (CMAKE_CXX_COMPILER_ID STREQUAL "GNU" OR CMAKE_CXX_COMPILER_ID STREQUAL "Clang")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -Wno-unused-parameter")
elseif (MSVC)
    add_compile_options("/W4")
    add_compile_options("/wd4100")
endif ()

# Fix issues in MSVC
if (MSVC)
    add_compile_options("/utf-8")
    add_compile_options("/Zc:__cplusplus")
endif ()

# ThirdParty: fmt
add_subdirectory(3rdparty/fmt)
if (MSVC)
    set_property(TARGET fmt PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
endif ()

# ThirdParty: utf8cpp
add_subdirectory(3rdparty/utfcpp)

# ThirdParty: xxHash
set(XXHASH_BUILD_XXHSUM OFF)
option(BUILD_SHARED_LIBS OFF)
add_subdirectory(3rdparty/xxHash/cmake_unofficial EXCLUDE_FROM_ALL)

# ThirdParty: serialization
add_subdirectory(3rdparty/serialization)

# ThirdParty: spdlog
set(SPDLOG_FMT_EXTERNAL ON)
add_subdirectory(3rdparty/spdlog)
if (MSVC)
    set_property(TARGET spdlog PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
endif ()

# ThirdParty: GTest
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
add_subdirectory(3rdparty/googletest)
if (MSVC)
    set_property(TARGET gtest PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
    set_property(TARGET gtest_main PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
endif ()

# Param Deliver
if (CMAKE_BUILD_TYPE AND (CMAKE_BUILD_TYPE STREQUAL "Debug"))
    set(CHelperDebug true)
else ()
    set(CHelperDebug false)
endif ()
set(RESOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../CHelper-Resource)
configure_file(ParamDeliver.h.in ${CMAKE_BINARY_DIR}/include/ParamDeliver.h @ONLY)

# Sources
file(GLOB_RECURSE NEXUS_CORE_SOURCE_FILE src/chelper/*.cpp)

# CHelper Core
add_library(NexusCore STATIC ${NEXUS_CORE_SOURCE_FILE})
target_precompile_headers(NexusCore PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/include/pch.h)
target_include_directories(NexusCore PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include ${CMAKE_BINARY_DIR}/include ${CMAKE_CURRENT_SOURCE_DIR}/src)
target_link_libraries(NexusCore PUBLIC fmt::fmt spdlog::spdlog utf8cpp xxHash::xxhash serialization::serialization)
if (MSVC)
    set_property(TARGET NexusCore PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
endif ()
add_library(Nexus::Core ALIAS NexusCore)

# CHelper Core (no filesystem)
add_library(NexusNoFilesystemCore STATIC ${NEXUS_CORE_SOURCE_FILE})
target_precompile_headers(NexusNoFilesystemCore PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/include/pch.h)
target_include_directories(NexusNoFilesystemCore PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include ${CMAKE_BINARY_DIR}/include ${CMAKE_CURRENT_SOURCE_DIR}/src)
target_link_libraries(NexusNoFilesystemCore PUBLIC fmt::fmt spdlog::spdlog utf8cpp xxHash::xxhash serialization::serialization)
target_compile_definitions(NexusNoFilesystemCore PUBLIC NEXUS_NO_FILESYSTEM)
if (MSVC)
    set_property(TARGET NexusNoFilesystemCore PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
endif ()
add_library(Nexus::NoFilesystemCore ALIAS NexusNoFilesystemCore)

# CHelper Android
if (NOT MSVC AND NOT EMSCRIPTEN)
    add_library(NexusAndroid SHARED src/apps/NexusAndroid.cpp)
    target_compile_definitions(NexusAndroid PUBLIC NexusAndroid)
    if (ANDROID)
        target_link_libraries(NexusAndroid PUBLIC android log)
    else ()
        target_include_directories(NexusAndroid PUBLIC include_android)
    endif ()
    target_link_libraries(NexusAndroid PRIVATE Nexus::Core)
endif ()

# CHelper Resource Generator
if (NOT ANDROID AND NOT EMSCRIPTEN)
    add_executable(NexusResourceGenerator src/apps/NexusResourceGenerator.cpp)
    target_link_libraries(NexusResourceGenerator PRIVATE Nexus::Core)
    if (MSVC)
        set_property(TARGET NexusResourceGenerator PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
    endif ()
endif ()

# CHelper Test
if (NOT ANDROID AND NOT EMSCRIPTEN)
    file(GLOB_RECURSE TEST_FILE tests/*.cpp)
    add_executable(NexusTest ${TEST_FILE})
    target_link_libraries(NexusTest PRIVATE Nexus::Core GTest::gtest_main)
    if (MSVC)
        set_property(TARGET NexusTest PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
    endif ()
endif ()

# Web Application
if (NOT ANDROID)
    add_library(NexusWeb src/apps/NexusWeb.cpp)
    if (NOT EMSCRIPTEN)
        target_include_directories(NexusWeb PRIVATE 3rdparty/emscripten/system/include)
    endif ()
    target_link_libraries(NexusWeb PRIVATE Nexus::NoFilesystemCore)
endif ()

# ThirdParty: Qt6
if (NOT ANDROID AND NOT EMSCRIPTEN)
    find_package(Qt6 QUIET COMPONENTS Core Gui Widgets)
    if (Qt6_FOUND)
        set(CMAKE_AUTOMOC ON)
        set(CMAKE_AUTORCC ON)
        set(CMAKE_AUTOUIC ON)

        # Qt Application
        add_executable(NexusQt WIN32
                src/apps/qt/NexusQt.h
                src/apps/qt/NexusQt.cpp
                src/apps/qt/chelper.qrc
                src/apps/qt/chelper.ui
        )
        target_link_libraries(NexusQt PRIVATE Nexus::Core Qt6::Core Qt6::Gui Qt6::Widgets)
        if (MSVC)
            set_property(TARGET NexusQt PROPERTY MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")
        endif ()
    endif ()
endif ()
