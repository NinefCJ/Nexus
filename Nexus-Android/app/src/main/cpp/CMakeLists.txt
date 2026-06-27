cmake_minimum_required(VERSION 3.22.1)
project("nexuscmd")

set(CORE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../Nexus-Core)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include(FetchContent)
FetchContent_Declare(
    rapidjson
    GIT_REPOSITORY https://github.com/Tencent/rapidjson.git
    GIT_TAG v1.1.0
)
FetchContent_GetProperties(rapidjson)
if(NOT rapidjson_POPULATED)
    FetchContent_Populate(rapidjson)
endif()

include_directories(${CORE_DIR}/include)
include_directories(${rapidjson_SOURCE_DIR}/include)

set(CORE_SOURCES
    ${CORE_DIR}/src/tokenizer.cpp
    ${CORE_DIR}/src/parser.cpp
    ${CORE_DIR}/src/completion.cpp
    ${CORE_DIR}/src/highlighter.cpp
    ${CORE_DIR}/src/command_registry.cpp
    ${CORE_DIR}/src/command_helper_jni.cpp
)

set(JNI_SOURCES
    native.cpp
)

add_library(${CMAKE_PROJECT_NAME} SHARED
    ${JNI_SOURCES}
    ${CORE_SOURCES}
)

target_include_directories(${CMAKE_PROJECT_NAME} PUBLIC ${CORE_DIR}/include)

find_library(log-lib log)

target_link_libraries(${CMAKE_PROJECT_NAME}
    ${log-lib}
)
