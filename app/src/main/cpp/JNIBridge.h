#pragma once
#include <jni.h>
#include <memory>
#include "seal/seal.h"

// Prefix: Java_com_imeanttobe_consensusapp_seal_NativeLib_

extern std::unique_ptr<seal::SEALContext> g_context;
extern std::unique_ptr<seal::Evaluator> g_evaluator;
extern std::unique_ptr<seal::BatchEncoder> g_encoder;

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
Java_com_imeanttobe_consensusapp_seal_NativeLib_encrypt(JNIEnv *env, jobject, jlongArray inputVector, jbyteArray publicKeyBytes);

// Decrypts a vector of integers.
extern "C" JNIEXPORT jlongArray JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_decrypt(JNIEnv *env, jobject, jbyteArray cipherBytes, jbyteArray secretKeyBytes);

// Adds two ciphertexts.
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_addCiphertexts(JNIEnv* env, jobject, jbyteArray cipherBytes1, jbyteArray cipherBytes2);

// Multiply two ciphertexts.
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_multiplyCiphertexts(JNIEnv* env, jobject, jbyteArray cipherBytes1, jbyteArray cipherBytes2, jbyteArray relinKeyBytes);