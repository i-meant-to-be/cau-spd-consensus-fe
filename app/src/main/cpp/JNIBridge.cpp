// Headers
#include <jni.h>
#include <string>
#include <vector>
#include <sstream>
#include <android/log.h>

#include "seal/seal.h"
#include "JNIBridge.h"

// Prefix: Java_com_imeanttobe_consensusapp_seal_NativeLib_

// Namespaces
using namespace std;
using namespace seal;

// Unique pointers
static unique_ptr<SEALContext>  g_context;
static unique_ptr<KeyGenerator> g_keygen;
static unique_ptr<Encryptor>    g_encryptor;
static unique_ptr<Decryptor>    g_decryptor;
static unique_ptr<Evaluator>    g_evaluator;
static unique_ptr<BatchEncoder> g_encoder;
static unique_ptr<PublicKey>    g_public_key;
static unique_ptr<SecretKey>    g_secret_key;

// JNI Functions
// - Sample function
extern "C" JNIEXPORT jstring JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_stringFromJNI(JNIEnv *env,
        jobject) {
    string hello = "Hello from C++ (SEAL Ready!)";
    return env->NewStringUTF(hello.c_str());
}

// - Init and key generation function
extern "C" JNIEXPORT jboolean JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_initContext(JNIEnv *env, jobject) {
    try {
        // If already initialized, return
        if (g_context) {
            return JNI_TRUE;
        }

        // Set params for BFV scheme
        EncryptionParameters params(scheme_type::bfv);
        size_t poly_modulus_degree = 4096;
        params.set_poly_modulus_degree(poly_modulus_degree);
        params.set_coeff_modulus(CoeffModulus::BFVDefault(poly_modulus_degree));
        params.set_plain_modulus(PlainModulus::Batching(poly_modulus_degree, 20));

        auto context = make_unique<SEALContext>(params);
        if (!context->parameters_set()) {
            return JNI_FALSE;
        }

        // Create global instances
        g_context = std::move(context);
        g_evaluator = make_unique<Evaluator>(*g_context);
        g_keygen = make_unique<KeyGenerator>(*g_context);

        return JNI_TRUE;
    } catch (const exception &e) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Error in init: %s", e.what());
        return JNI_FALSE;
    } catch (...) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Unknown error in init");
        return JNI_FALSE;
    }
}