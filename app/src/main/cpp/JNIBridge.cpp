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

// - Init function
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

// - Key generation function
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_generateKeys(JNIEnv *env, jobject) {
    if (!g_context) {
        return nullptr;
    }

    try {
        // Create KeyGenerator
        g_keygen = make_unique<KeyGenerator>(*g_context);

        // Create SK, PK
        g_secret_key = make_unique<SecretKey>(g_keygen->secret_key());
        g_public_key = make_unique<PublicKey>();
        g_keygen->create_public_key(*g_public_key);

        // Create encryptor and decryptor
        g_encryptor = make_unique<Encryptor>(*g_context, *g_public_key);
        g_decryptor = make_unique<Decryptor>(*g_context, *g_secret_key);

        // Serialize keys
        stringstream data_stream;
        g_public_key->save(data_stream);
        string serialized_key = data_stream.str();

        // Convert to jbyteArray
        jbyteArray result = env->NewByteArray(static_cast<jsize>(serialized_key.size()));
        env->SetByteArrayRegion(result, 0, serialized_key.size(), reinterpret_cast<const jbyte*>(serialized_key.c_str()));

        return result;
    } catch (const exception &e) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Error in generateKeys: %s", e.what());
        return nullptr;
    } catch (...) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Unknown error in generateKeys");
        return nullptr;
    }
}