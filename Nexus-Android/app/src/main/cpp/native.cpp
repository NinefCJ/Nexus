#include <jni.h>
#include <string>
#include "command_helper_jni.hpp"

using namespace mcmd;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_nexuscmd_CommandHelper_initialize(JNIEnv *env, jobject, jstring json_data) {
    const char *json = env->GetStringUTFChars(json_data, nullptr);
    bool result = CommandHelperJni::initialize(std::string(json ? json : ""));
    env->ReleaseStringUTFChars(json_data, json);
    return result;
}

JNIEXPORT jstring JNICALL
Java_com_nexuscmd_CommandHelper_getCompletions(JNIEnv *env, jobject, jstring input, jint cursor) {
    const char *input_str = env->GetStringUTFChars(input, nullptr);
    auto result = CommandHelperJni::getCompletions(std::string(input_str), cursor);
    env->ReleaseStringUTFChars(input, input_str);
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_nexuscmd_CommandHelper_getHighlights(JNIEnv *env, jobject, jstring input) {
    const char *input_str = env->GetStringUTFChars(input, nullptr);
    auto result = CommandHelperJni::getHighlights(std::string(input_str));
    env->ReleaseStringUTFChars(input, input_str);
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_nexuscmd_CommandHelper_validateCommand(JNIEnv *env, jobject, jstring input) {
    const char *input_str = env->GetStringUTFChars(input, nullptr);
    auto result = CommandHelperJni::validateCommand(std::string(input_str));
    env->ReleaseStringUTFChars(input, input_str);
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_nexuscmd_CommandHelper_getCommandInfo(JNIEnv *env, jobject, jstring command_name) {
    const char *name = env->GetStringUTFChars(command_name, nullptr);
    auto result = CommandHelperJni::getCommandInfo(std::string(name));
    env->ReleaseStringUTFChars(command_name, name);
    return env->NewStringUTF(result.c_str());
}

}
