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
        // Check whether key generator is exist
        if (!g_keygen) {
            g_keygen = make_unique<KeyGenerator>(*g_context);
        }

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
        if (seal_keys_class == nullptr) {
            __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Failed to find SealKeys class.");
            return nullptr;
        }

        // Find constructor
        jmethodID constructor = env->GetMethodID(seal_keys_class, "<init>", "([B[B)V");
        if (constructor == nullptr) {
            __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Failed to find constructor.");
            return nullptr;
        }

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

// - Encryption function
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_encrypt(
        JNIEnv *env,
        jobject,
        jlongArray input_vector) {
    // Check whether context is initialized
    if (!g_context || !g_encoder || !g_encryptor) {
        return nullptr;
    }

    try {
        // --- 1. JNI jlongArray -> C++ vector<int64_t> 변환 ---
        jsize len = env->GetArrayLength(input_vector);
        jlong* ptr = env->GetLongArrayElements(input_vector, 0);

        // 입력 데이터를 SEAL이 처리할 수 있는 벡터로 복사
        vector<int64_t> pod_matrix(len);
        for (int i = 0; i < len; i++) {
            pod_matrix[i] = ptr[i];
        }

        // JNI 메모리 해제 (데이터 복사했으므로 즉시 해제)
        env->ReleaseLongArrayElements(input_vector, ptr, 0);

        // --- 2. 인코딩 (Vector -> Plaintext) ---
        Plaintext plain;
        // BatchEncoder가 벡터를 다항식 슬롯에 배치합니다.
        // 나머지 슬롯은 자동으로 0으로 채워집니다.
        g_encoder->encode(pod_matrix, plain);

        // --- 3. 암호화 (Plaintext -> Ciphertext) ---
        Ciphertext encrypted;
        g_encryptor->encrypt(plain, encrypted);

        // --- 4. 직렬화 (Ciphertext -> Byte Array) ---
        stringstream stream;
        encrypted.save(stream);
        string serialized_data = stream.str();

        jsize output_len = static_cast<jsize>(serialized_data.size());
        jbyteArray result = env->NewByteArray(output_len);
        env->SetByteArrayRegion(result, 0, output_len, reinterpret_cast<const jbyte*>(serialized_data.c_str()));

        return result;
    } catch (const exception &e) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Error in encrypt: %s", e.what());
        return nullptr;
    } catch (...) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Unknown error in encrypt");
        return nullptr;
    }
}

// - Decryption function
extern "C" JNIEXPORT jlongArray JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_decrypt(
        JNIEnv *env,
        jobject,
        jbyteArray cipher_bytes) {
    // Check whether context is initialized
    if (!g_context || !g_encoder || !g_decryptor) {
        return nullptr;
    }

    try {
        // --- 1. JNI ByteArray -> Ciphertext 역직렬화 ---
        jsize len = env->GetArrayLength(cipher_bytes);
        jbyte *ptr = env->GetByteArrayElements(cipher_bytes, nullptr);

        string serialized_data(reinterpret_cast<char *>(ptr), len);
        env->ReleaseByteArrayElements(cipher_bytes, ptr, JNI_ABORT); // JNI_ABORT: 수정 안 했으므로 복사본 버림

        stringstream stream(serialized_data);
        Ciphertext encrypted;
        encrypted.load(*g_context, stream);

        // --- 2. 복호화 (Ciphertext -> Plaintext) ---
        Plaintext plain;
        g_decryptor->decrypt(encrypted, plain);

        // --- 3. 디코딩 (Plaintext -> Vector<int64_t>) ---
        vector<int64_t> pod_result;
        g_encoder->decode(plain, pod_result);

        // --- 4. C++ Vector -> JNI jlongArray 변환 ---
        // SEAL은 항상 슬롯 개수만큼(예: 4096개) 전체 벡터를 반환합니다.
        // 우리는 앞부분의 유효한 데이터만 필요하겠지만,
        // C++ 단에서는 길이를 모르므로 전체를 반환하고 Kotlin에서 자르는 게 안전합니다.

        // (최적화를 위해 최대 반환 크기를 제한하고 싶다면 여기서 조정 가능)
        jsize result_len = static_cast<jsize>(pod_result.size());
        jlongArray result = env->NewLongArray(result_len);

        // vector 데이터는 메모리에 연속적으로 있으므로 바로 복사 가능 (int64_t == jlong)
        env->SetLongArrayRegion(result, 0, result_len, reinterpret_cast<const jlong *>(pod_result.data()));

        return result;
    } catch (const exception &e) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Error in decrypt: %s", e.what());
        return nullptr;
    } catch (...) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Unknown error in decrypt");
        return nullptr;
    }
}