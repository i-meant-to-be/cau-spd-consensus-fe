# CMake 최소 버전을 NDK 권장 사항으로 설정
cmake_minimum_required(VERSION 3.18.1)
project(consensus_native)

# --- 1. Microsoft SEAL 빌드 설정 ---
# add_subdirectory로 SEAL을 빌드하기 *전에* 옵션을 미리 설정합니다.

# 우리는 SEAL 라이브러리만 필요하므로, 테스트와 예제는 빌드하지 않습니다.
set(SEAL_BUILD_TESTS OFF CACHE BOOL "Build SEAL tests")
set(SEAL_BUILD_EXAMPLES OFF CACHE BOOL "Build SEAL examples")
set(SEAL_USE_ZLIB ON CACHE BOOL "Use ZLIB for compression")

# NDK가 제공하는 Zlib 라이브러리를 찾습니다.
# find_package(ZLIB REQUIRED)
find_library(z-lib z)

# 크로스플랫폼 빌드 오류 해결
set(SEAL___BUILTIN_CLZLL_FOUND_EXITCODE 0 CACHE STRING "Manually set exit code for cross-compiling")
set(SEAL__ADDCARRY_U64_FOUND_EXITCODE 0 CACHE STRING "Manually set exit code for cross-compiling")
set(SEAL__SUBBORROW_U64_FOUND_EXITCODE 0 CACHE STRING "Manually set exit code for cross-compiling")

# C# (.NET) 빌드에 필요한 C 라이브러리 빌드 비활성화 (가장 중요)
set(SEAL_BUILD_SEAL_C OFF CACHE BOOL "Build C export library")

# 벤치마크 빌드 비활성화
set(SEAL_BUILD_BENCH OFF CACHE BOOL "Build C++ benchmarks")

# 불필요한 의존 라이브러리 비활성화
set(SEAL_USE_MSGSL OFF CACHE BOOL "Use Microsoft GSL")
set(SEAL_USE_ZSTD OFF CACHE BOOL "Use Zstandard compression")
set(SEAL_USE_INTEL_HEXL OFF CACHE BOOL "Use Intel HEXL library")

# --- 2. Microsoft SEAL 빌드 실행 ---
# `app/src/main/cpp/SEAL` 폴더에 있는 SEAL의 CMakeLists.txt를 실행시킵니다.
# 이 명령 하나로 SEAL 라이브러리(libseal.a)가 빌드됩니다.
add_subdirectory(SEAL)

# --- 3. JNI 브릿지 라이브러리 생성 (Task 2 준비) ---

# NDK의 로깅 라이브러리를 찾습니다. (C++에서 Log.d(...) 사용 시 필요)
find_library(log-lib log)

# "jni_bridge.cpp" 파일을 컴파일하여 "consensus_native"라는 이름의
# .so (공유 라이브러리) 파일을 생성합니다.
# (아직 jni_bridge.cpp 파일이 없으므로, 지금 바로 다음 단계에서 생성해야 합니다.)
add_library(consensus_native SHARED
        JNIBridge.cpp)

# "consensus_native" 라이브러리가 "SEAL" 라이브러리에 의존함을 명시합니다.
# 또한 NDK의 Zlib과 log 라이브러리에도 연결(link)합니다.
target_link_libraries(consensus_native
        PUBLIC
        seal          # SEAL 라이브러리
        ${z-lib} # NDK의 Zlib
        ${log-lib}    # NDK의 Log
)

# 16 KB 호환성 강제 설정
if(ANDROID_ABI STREQUAL "arm64-v8a" OR ANDROID_ABI STREQUAL "x86_64")
    target_link_options(consensus_native PRIVATE "-Wl,-z,max-page-size=16384")
endif()