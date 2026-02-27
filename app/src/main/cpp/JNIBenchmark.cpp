#include <jni.h>
#include <chrono>
#include <android/log.h>
#include "seal/seal.h"
#include "JNIBridge.h" // 공유할 g_context, g_evaluator 가져오기

using namespace std;
using namespace seal;

// 벤치마크 전용 전역 데이터 (이 파일 내에서만 유지)
static Ciphertext g_bench_ct1;
static Ciphertext g_bench_ct2;
static RelinKeys  g_bench_relin_keys;
static bool       g_bench_ready = false;

// 1. 사전 데이터 로드 (실험 시작 전 1회만 호출)
extern "C" JNIEXPORT jboolean JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_loadBenchmarkData(
        JNIEnv *env,
        jobject,
        jbyteArray ct1Bytes,
        jbyteArray ct2Bytes,
        jbyteArray rkBytes) {
    if (!g_context || !g_evaluator) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL_BENCH", "Context not initialized.");
        return JNI_FALSE;
    }

    if (!ct1Bytes || !ct2Bytes || !rkBytes) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL_BENCH", "Invalid input byte array.");
        return JNI_FALSE;
    }

    auto deserialize = [&](jbyteArray bytes, auto& obj) {
        jsize len = env->GetArrayLength(bytes);
        jbyte* ptr = env->GetByteArrayElements(bytes, nullptr);
        if (!ptr) {
            __android_log_print(ANDROID_LOG_ERROR, "SEAL_BENCH", "Failed to get Java byte array elements.");
            return;
        }

        string data(reinterpret_cast<char*>(ptr), len);
        env->ReleaseByteArrayElements(bytes, ptr, JNI_ABORT); // 메모리 해제
        stringstream stream(data);
        obj.load(*g_context, stream);
    };

    try {
        deserialize(ct1Bytes, g_bench_ct1);
        deserialize(ct2Bytes, g_bench_ct2);
        deserialize(rkBytes, g_bench_relin_keys);
        g_bench_ready = true;
        __android_log_print(ANDROID_LOG_INFO, "SEAL_BENCH", "Benchmark data loaded successfully.");
        return JNI_TRUE;
    } catch (const exception& e) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL_BENCH", "Error in loadBenchmarkData: %s", e.what());
        return JNI_FALSE;
    } catch (...) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL_BENCH", "Failed to load benchmark data.");
        return JNI_FALSE;
    }
}

// 2. 순수 연산 벤치마크 실행 (반복 횟수를 받아 총 소요 시간을 ms 단위로 반환)
extern "C" JNIEXPORT jlong JNICALL
Java_com_imeanttobe_consensusapp_seal_NativeLib_runBenchmarkMultiply(
        JNIEnv *env,
        jobject,
        jint iterations) {
    // Null check for params
    if (!g_bench_ready || !g_context || !g_evaluator) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL_BENCH", "Benchmark data not ready.");
        return -1;
    }

    // Set upper bound and lower bound for iterations
    if (iterations <= 0 || iterations > 1000000) {
        __android_log_print(ANDROID_LOG_ERROR, "SEAL_BENCH", "Invalid iteration count.");
        return -1;
    }

    Ciphertext result_ct; // 결과를 덮어쓸 임시 객체

    // --- 시간 측정 시작 ---
    auto start_time = chrono::high_resolution_clock::now();

    for (int i = 0; i < iterations; i++) {
        // 곱셈 수행 (크기가 3으로 늘어남)
        g_evaluator->multiply(g_bench_ct1, g_bench_ct2, result_ct);
        // 재선형화 수행 (In-place 연산으로 불필요한 메모리 복사 최소화, 크기 2로 복구)
        g_evaluator->relinearize_inplace(result_ct, g_bench_relin_keys);
    }

    // --- 시간 측정 종료 ---
    auto end_time = chrono::high_resolution_clock::now();

    // 밀리초(ms) 단위로 변환
    auto duration_ms = chrono::duration_cast<chrono::milliseconds>(end_time - start_time).count();

    return static_cast<jlong>(duration_ms);
}