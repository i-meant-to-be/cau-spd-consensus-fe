package com.imeanttobe.consensusapp.domain.benchmark

import com.imeanttobe.consensusapp.seal.NativeLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class RunBenchmarkUseCase @Inject constructor() {
    /**
     * 벤치마크 실행.
     * @param mode 벤치마크 모드 (Dispatchers.IO, Dispatchers.Default, Kotlin 스레드 풀, C++ 스레드)
     * @param totalIterations 전체 곱셈 연산 횟수
     * @param threadCount 사용할 Coroutines/스레드 개수
     * @return 소요 시간 (ms)
     */
    suspend operator fun invoke(
        mode: BenchmarkMode,
        totalIterations: Int,
        threadCount: Int
    ): Long {
        // Prepare parallel processing environments
        val dispatcher = when (mode) {
            BenchmarkMode.KOTLIN_DEFAULT -> Dispatchers.Default
            BenchmarkMode.KOTLIN_IO -> Dispatchers.IO
            BenchmarkMode.KOTLIN_CUSTOM_POOL -> Executors.newFixedThreadPool(threadCount).asCoroutineDispatcher()
            BenchmarkMode.CPP_NATIVE_THREAD -> {
                return NativeLib.runNativeBenchmarkMultiply(
                    totalIterations = totalIterations,
                    threadCount = threadCount
                )
            }
        }

        // Calculate iteration count for each thread
        val chunk = totalIterations / threadCount
        val remainder = totalIterations % threadCount

        // Run benchmark
        return withContext(dispatcher) {
            val timeTaken = measureTimeMillis {
                val deferreds = (0 until threadCount).map { i ->
                    async {
                        val iterations = if (i == threadCount - 1) chunk + remainder else chunk

                        if (iterations > 0) {
                            NativeLib.runBenchmarkMultiply(iterations)
                        }
                    }
                }

                deferreds.awaitAll()
            }

            if (mode == BenchmarkMode.KOTLIN_CUSTOM_POOL) {
                (dispatcher as? ExecutorCoroutineDispatcher)?.close()
            }

            timeTaken
        }
    }
}