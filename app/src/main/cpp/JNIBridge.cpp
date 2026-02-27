#ifndef JNIBRIDGE_H
#define JNIBRIDGE_H

// Headers
#include <jni.h>
#include <string>
#include <vector>
#include <sstream>
#include <android/log.h>

#include "seal/seal.h"
#include "JNIBridge.h"

// Namespaces
using namespace std;
using namespace seal;

// Unique pointers
unique_ptr<SEALContext>  g_context;
unique_ptr<Evaluator>    g_evaluator;
unique_ptr<BatchEncoder> g_encoder;

// Constants
static const string PATH_SEAL_KEYS_CLASS = "com/imeanttobe/consensusapp/seal/SealKeys";
static const int POLY_MODULUS_DEGREE = 8096;

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
        size_t poly_modulus_degree = POLY_MODULUS_DEGREE;
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
        g_encoder = make_unique<BatchEncoder>(*g_context);

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
        // Generate key generator
        KeyGenerator keygen(*g_context);

        // Create SK, PK, RK
        SecretKey secret_key = keygen.secret_key();
        PublicKey public_key;
        RelinKeys relin_key;
        keygen.create_public_key(public_key);
        keygen.create_relin_keys(relin_key);

        // Serialize pk
        stringstream pk_stream;
        public_key.save(pk_stream);
        string serialized_pk = pk_stream.str();

        // Convert pk to jbyteArray
        jbyteArray pk_bytes = env->NewByteArray(static_cast<jsize>(serialized_pk.size()));
        env->SetByteArrayRegion(pk_bytes, 0, static_cast<jsize>(serialized_pk.size()), reinterpret_cast<const jbyte*>(serialized_pk.c_str()));

        // Serialize sk
        stringstream sk_stream;
        secret_key.save(sk_stream);
        string serialized_sk = sk_stream.str();

        // Convert sk to jbyteArray
        jbyteArray sk_bytes = env->NewByteArray(static_cast<jsize>(serialized_sk.size()));
        env->SetByteArrayRegion(sk_bytes, 0, static_cast<jsize>(serialized_sk.size()), reinterpret_cast<const jbyte*>(serialized_sk.c_str()));

        // Serialize rk
        stringstream rk_stream;
        relin_key.save(rk_stream);
        string serialized_rk = rk_stream.str();

        // Convert rk to jbyteArray
        jbyteArray rk_bytes = env->NewByteArray(static_cast<jsize>(serialized_rk.size()));
        env->SetByteArrayRegion(rk_bytes, 0, static_cast<jsize>(serialized_rk.size()), reinterpret_cast<const jbyte*>(serialized_rk.c_str()));

        // Find SealKeys class
        jclass seal_keys_class = env->FindClass(PATH_SEAL_KEYS_CLASS.c_str());
        if (seal_keys_class == nullptr) {
            __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Failed to find SealKeys class.");
            return nullptr;
        }

        // Find constructor
        jmethodID constructor = env->GetMethodID(seal_keys_class, "<init>", "([B[B[B)V");
        if (constructor == nullptr) {
            __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Failed to find constructor.");
            return nullptr;
        }

        // Create Java object
        jobject seal_keys = env->NewObject(seal_keys_class, constructor, pk_bytes, sk_bytes, rk_bytes);

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
        jlongArray inputVector,
	    jbyteArray publicKeyBytes
) {
    // Check whether context is initialized
    if (!g_context || !g_encoder) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Context is not ready.");
        return nullptr;
    }

	// Check whether input array is valid
	if (inputVector == nullptr || publicKeyBytes == nullptr) {
		__android_log_print(ANDROID_LOG_ERROR, "SEAL", "Input vector or public key is null.");
		return nullptr;
	}

    try {
        // --- 2. 공개 키 로드 (ByteArray -> PublicKey) ---
        // 전달받은 바이트 배열을 C++ 스트림으로 변환
        jsize pk_len = env->GetArrayLength(publicKeyBytes);
        jbyte* pk_ptr = env->GetByteArrayElements(publicKeyBytes, nullptr);
        string pk_str(reinterpret_cast<char*>(pk_ptr), pk_len);
        env->ReleaseByteArrayElements(publicKeyBytes, pk_ptr, JNI_ABORT);

        stringstream pk_stream(pk_str);
        PublicKey public_key;
        public_key.load(*g_context, pk_stream); // 공개 키 복원

        // --- 3. 일회용 Encryptor 생성 ---
        // 전역 변수(g_encryptor)를 쓰지 않고, 이 투표만을 위한 암호화기를 만듭니다.
        Encryptor local_encryptor(*g_context, public_key);

        // --- 1. JNI jlongArray -> C++ vector<int64_t> 변환 ---
        jsize len = env->GetArrayLength(inputVector);
        jlong* ptr = env->GetLongArrayElements(inputVector, 0);

        // 입력 데이터를 SEAL이 처리할 수 있는 벡터로 복사
        vector<int64_t> pod_matrix(len);
        for (int i = 0; i < len; i++) {
            pod_matrix[i] = ptr[i];
        }

        // JNI 메모리 해제 (데이터 복사했으므로 즉시 해제)
        env->ReleaseLongArrayElements(inputVector, ptr, JNI_ABORT);

        // --- 2. 인코딩 (Vector -> Plaintext) ---
        Plaintext plain;
        // BatchEncoder가 벡터를 다항식 슬롯에 배치합니다.
        // 나머지 슬롯은 자동으로 0으로 채워집니다.
        g_encoder->encode(pod_matrix, plain);

        // --- 3. 암호화 (Plaintext -> Ciphertext) ---
        Ciphertext encrypted;
        local_encryptor.encrypt(plain, encrypted);

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
        jbyteArray cipherBytes,
        jbyteArray secretKeyBytes) {
    // Check whether context is initialized
    if (!g_context || !g_encoder) {
        return nullptr;
    }

	// Check whether input byte array is valid
	if (cipherBytes == nullptr || secretKeyBytes == nullptr) {
		__android_log_print(ANDROID_LOG_ERROR, "SEAL", "Input ciphertext byte array or secret key is null.");
		return nullptr;
	}

    try {
        // --- 0. 비밀 키 로드 (ByteArray -> SecretKey) ---
        jsize sk_len = env->GetArrayLength(secretKeyBytes);
        jbyte* sk_ptr = env->GetByteArrayElements(secretKeyBytes, nullptr);
        string sk_str(reinterpret_cast<char*>(sk_ptr), sk_len);
        env->ReleaseByteArrayElements(secretKeyBytes, sk_ptr, JNI_ABORT);

        stringstream sk_stream(sk_str);
        SecretKey secret_key;
        secret_key.load(*g_context, sk_stream);

        Decryptor local_decryptor(*g_context, secret_key);

        // --- 1. JNI ByteArray -> Ciphertext 역직렬화 ---
        jsize len = env->GetArrayLength(cipherBytes);
        jbyte *ptr = env->GetByteArrayElements(cipherBytes, nullptr);

        string serialized_data(reinterpret_cast<char *>(ptr), len);
        env->ReleaseByteArrayElements(cipherBytes, ptr, JNI_ABORT); // JNI_ABORT: 수정 안 했으므로 복사본 버림

        stringstream stream(serialized_data);
        Ciphertext encrypted;
        encrypted.load(*g_context, stream);

        // --- 2. 복호화 (Ciphertext -> Plaintext) ---
        Plaintext plain;
        local_decryptor.decrypt(encrypted, plain);

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

// - Ciphertext addition function
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_addCiphertexts(
    JNIEnv* env,
    jobject,
    jbyteArray cipherBytes1,
    jbyteArray cipherBytes2) {
    // Check whether context is initialized
    if (!g_context || !g_evaluator) {
        return nullptr;
    }

	// Check whether input byte arrays are valid
	if (cipherBytes1 == nullptr || cipherBytes2 == nullptr) {
		__android_log_print(ANDROID_LOG_ERROR, "SEAL", "Input ciphertext byte arrays are null.");
		return nullptr;
	}

    auto deserialize_ciphertext = [&](jbyteArray cipherBytes, Ciphertext& ct) {
		// JNI ByteArray -> Ciphertext 역직렬화
		jsize len = env->GetArrayLength(cipherBytes);
		jbyte* ptr = env->GetByteArrayElements(cipherBytes, nullptr);
		string serialized_data(reinterpret_cast<char*>(ptr), len);
		env->ReleaseByteArrayElements(cipherBytes, ptr, JNI_ABORT);

		// 결과 Ciphertext에 로드
		stringstream stream(serialized_data);
		ct.load(*g_context, stream);
        };

    try {
        // --- 1. JNI ByteArray -> Ciphertext 역직렬화 
        Ciphertext encrypted1;
		deserialize_ciphertext(cipherBytes1, encrypted1);

        Ciphertext encrypted2;
        deserialize_ciphertext(cipherBytes2, encrypted2);

        // --- 2. 덧셈 연산 (Ciphertext + Ciphertext) ---
        Ciphertext encrypted_result;
        g_evaluator->add(encrypted1, encrypted2, encrypted_result);

        // --- 3. 직렬화 (Ciphertext -> Byte Array) ---
        stringstream result_stream;
        encrypted_result.save(result_stream);
        string serialized_result = result_stream.str();

        jsize output_len = static_cast<jsize>(serialized_result.size());
        jbyteArray result = env->NewByteArray(output_len);
        env->SetByteArrayRegion(result, 0, output_len, reinterpret_cast<const jbyte*>(serialized_result.c_str()));

        return result;
    } catch (const exception& e) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Error in addCiphertexts: %s", e.what());
        return nullptr;
    } catch (...) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Unknown error in addCiphertexts");
        return nullptr;
    }
}


extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_multiplyCiphertexts(
        JNIEnv* env,
        jobject,
        jbyteArray cipherBytes1,
        jbyteArray cipherBytes2,
        jbyteArray relinKeyBytes) {
    // Check whether context is initialized
    if (!g_context || !g_evaluator) {
        return nullptr;
    }

    // Check whether input byte arrays are valid
    if (cipherBytes1 == nullptr || cipherBytes2 == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Input ciphertext byte arrays are null.");
        return nullptr;
    }
    if (relinKeyBytes == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Input relin key byte array is null.");
        return nullptr;
    }

    auto deserialize_ciphertext = [&](jbyteArray cipherBytes, Ciphertext& ct) {
        // JNI ByteArray -> Ciphertext 역직렬화
        jsize len = env->GetArrayLength(cipherBytes);
        jbyte* ptr = env->GetByteArrayElements(cipherBytes, nullptr);
        string serialized_data(reinterpret_cast<char*>(ptr), len);
        env->ReleaseByteArrayElements(cipherBytes, ptr, JNI_ABORT);

        // 결과 Ciphertext에 로드
        stringstream stream(serialized_data);
        ct.load(*g_context, stream);
    };

    auto deserialize_relin_key = [&](jbyteArray relinKeyBytes, RelinKeys& rk) {
        // JNI ByteArray -> Ciphertext 역직렬화
        jsize len = env->GetArrayLength(relinKeyBytes);
        jbyte* ptr = env->GetByteArrayElements(relinKeyBytes, nullptr);
        string serialized_data(reinterpret_cast<char*>(ptr), len);
        env->ReleaseByteArrayElements(relinKeyBytes, ptr, JNI_ABORT);

        // 결과 Ciphertext에 로드
        stringstream stream(serialized_data);
        rk.load(*g_context, stream);
    };

    try {
        // --- 1. JNI ByteArray -> Ciphertext 역직렬화
        Ciphertext encrypted1;
        deserialize_ciphertext(cipherBytes1, encrypted1);

        Ciphertext encrypted2;
        deserialize_ciphertext(cipherBytes2, encrypted2);

        RelinKeys relin_key;
        deserialize_relin_key(relinKeyBytes, relin_key);

        // --- 2. 곱셈 연산 (Ciphertext * Ciphertext) ---
        Ciphertext encrypted_result;
        g_evaluator->multiply(encrypted1, encrypted2, encrypted_result);
        g_evaluator->relinearize_inplace(encrypted_result, relin_key);

        // --- 3. 직렬화 (Ciphertext -> Byte Array) ---
        stringstream result_stream;
        encrypted_result.save(result_stream);
        string serialized_result = result_stream.str();

        jsize output_len = static_cast<jsize>(serialized_result.size());
        jbyteArray result = env->NewByteArray(output_len);
        env->SetByteArrayRegion(result, 0, output_len, reinterpret_cast<const jbyte*>(serialized_result.c_str()));

        return result;
    } catch (const exception& e) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Error in multiplyCiphertexts: %s", e.what());
        return nullptr;
    } catch (...) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL", "Unknown error in multiplyCiphertexts");
        return nullptr;
    }
}

#endif