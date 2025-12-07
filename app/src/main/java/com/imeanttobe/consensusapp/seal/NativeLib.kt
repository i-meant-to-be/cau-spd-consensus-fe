package com.imeanttobe.consensusapp.seal

object NativeLib {
    init {
        System.loadLibrary("consensus_native")
    }

    /**
     * 연결 테스트용 함수.
     * @return 연결 테스트를 위한 문자열
     */
    external fun stringFromJNI(): String

    /**
     * SEAL 컨텍스트를 초기화합니다.
     * @return 초기화 성공 여부 (Boolean)
     */
    external fun initContext(): Boolean

    /**
     * 키를 생성합니다.
     * @return SealKeys 객체 (PK, SK)
     */
    external fun generateKeys(): SealKeys?

    /**
     * 평문을 암호문으로 암호화합니다.
     * @param inputVector 평문 (LongArray)
     * @return 암호문 (ByteArray?)
     */
    external fun encrypt(inputVector: LongArray, publicKeyBytes: ByteArray): ByteArray?

    /**
     * 암호문을 평문으로 복호화합니다.
     * @param cipherBytes 암호문
     * @return 복호화된 평문 (LongArray?)
     */
    external fun decrypt(cipherBytes: ByteArray): LongArray?

    /**
     * 두 개의 암호문을 동형암호 덧셈합니다.
     * @param cipherBytes1 첫 번째 암호문
     * @param cipherBytes2 두 번째 암호문
     * @return 합산된 암호문 (ByteArray?)
     */
    external fun addCiphertexts(cipherBytes1: ByteArray, cipherBytes2: ByteArray): ByteArray?
}
