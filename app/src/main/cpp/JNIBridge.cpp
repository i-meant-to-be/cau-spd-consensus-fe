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

// Constants
static const string PATH_SEAL_KEYS_CLASS = "com/imeanttobe/consensusapp/seal/SealKeys";

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
extern "C" JNIEXPORT jobject JNICALL
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

        // Serialize pk
        stringstream pk_stream;
        g_public_key->save(pk_stream);
        string serialized_pk = pk_stream.str();

        // Convert pk to jbyteArray
        jbyteArray pk_bytes = env->NewByteArray(static_cast<jsize>(serialized_pk.size()));
        env->SetByteArrayRegion(pk_bytes, 0, static_cast<jsize>(serialized_pk.size()), reinterpret_cast<const jbyte*>(serialized_pk.c_str()));

        // Serialize sk
        stringstream sk_stream;
        g_secret_key->save(sk_stream);
        string serialized_sk = sk_stream.str();

        // Convert sk to jbyteArray
        jbyteArray sk_bytes = env->NewByteArray(static_cast<jsize>(serialized_sk.size()));
        env->SetByteArrayRegion(sk_bytes, 0, static_cast<jsize>(serialized_sk.size()), reinterpret_cast<const jbyte*>(serialized_sk.c_str()));

        // Find SealKeys class
        jclass seal_keys_class = env->FindClass(PATH_SEAL_KEYS_CLASS.c_str());

        // Find constructor
        jmethodID constructor = env->GetMethodID(seal_keys_class, "<init>", "([B[B)V");

        // Create Java object
        jobject seal_keys = env->NewObject(seal_keys_class, constructor, pk_bytes, sk_bytes);

        return seal_keys;
    } catch (const exception &e) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Error in generateKeys: %s", e.what());
        return nullptr;
    } catch (...) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Unknown error in generateKeys");
        return nullptr;
    }
}