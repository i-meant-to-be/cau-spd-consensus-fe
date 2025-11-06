#pragma once
#include <string>
#include <jni.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_stringFromJNI(
        JNIEnv *env,
        jobject);