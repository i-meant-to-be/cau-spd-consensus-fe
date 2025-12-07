#pragma once
#include <jni.h>

// Prefix: Java_com_imeanttobe_consensusapp_seal_NativeLib_

// Test function
extern "C" JNIEXPORT jstring JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_stringFromJNI(JNIEnv *env, jobject);

// Init contexts and create instances
extern "C" JNIEXPORT jboolean JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_initContext(JNIEnv *env, jobject);

// Create PK and SK
extern "C" JNIEXPORT jobject JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_generateKeys(JNIEnv *env, jobject);

// Encrypts a vector of integers.
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_encrypt(JNIEnv *env, jobject, jlongArray inputVector);

// Decrypts a vector of integers.
extern "C" JNIEXPORT jlongArray JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_decrypt(JNIEnv *env, jobject, jbyteArray cipherBytes);

/*
// Load given PK on native side
extern "C" JNIEXPORT void JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_loadPublicKey(JNIEnv *env, jobject, jbyteArray pkBytes);
*/