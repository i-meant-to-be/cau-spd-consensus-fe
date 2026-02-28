package com.imeanttobe.consensusapp.domain.benchmark

import com.imeanttobe.consensusapp.seal.NativeLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class RunBenchmarkUseCase @Inject constructor() {
    companion object {
        private const val MAX_ITERATIONS = 1000000
        private const val MAX_THREADS = 32
    }

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
        // Validate parameters
        if (totalIterations <= 0 || threadCount <= 0) {
            throw IllegalArgumentException("Invalid iteration count")
        }
        val validTotalIterations = totalIterations.coerceAtMost(MAX_ITERATIONS)
        val validThreadCount = threadCount.coerceAtMost(MAX_THREADS)

        // Prepare parallel processing environments
        val dispatcher = when (mode) {
            BenchmarkMode.KOTLIN_DEFAULT -> Dispatchers.Default
            BenchmarkMode.KOTLIN_IO -> Dispatchers.IO
            BenchmarkMode.KOTLIN_CUSTOM_POOL -> Executors.newFixedThreadPool(validThreadCount).asCoroutineDispatcher()
            BenchmarkMode.CPP_NATIVE_THREAD -> {
                return withContext(Dispatchers.Default) {
                    NativeLib.runNativeBenchmarkMultiply(
                        totalIterations = validTotalIterations,
                        threadCount = validThreadCount
                    )
                }
            }
        }

        // Calculate iteration count for each thread
        val chunk = validTotalIterations / validThreadCount
        val remainder = validTotalIterations % validThreadCount

        // Run benchmark
        return try {
            withContext(dispatcher) {
                val timeTaken = measureTimeMillis {
                    val deferreds = (0 until validThreadCount).map { i ->
                        async {
                            val iterations = if (i == validThreadCount - 1) chunk + remainder else chunk

                            if (iterations > 0) {
                                NativeLib.runBenchmarkMultiply(iterations)
                            }
                        }
                    }

                    deferreds.awaitAll()
                }

                timeTaken
            }
        } finally {
            if (mode == BenchmarkMode.KOTLIN_CUSTOM_POOL) {
                (dispatcher as? ExecutorCoroutineDispatcher)?.close()
            }
        }
    }
}