#include "JNIBridge.h"
#include <jni.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_stringFromJNI(
        JNIEnv *env,
        jobject) {
    std::string hello = "Hello from C++ (SEAL Ready!)";
    return env->NewStringUTF(hello.c_str());
}