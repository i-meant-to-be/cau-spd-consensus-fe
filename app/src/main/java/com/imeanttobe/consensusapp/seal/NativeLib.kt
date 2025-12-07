package com.imeanttobe.consensusapp.seal

object NativeLib {
    init {
        System.loadLibrary("consensus_native")
    }

    external fun stringFromJNI(): String
    external fun initContext(): Boolean
    external fun generateKeys(): SealKeys?
    external fun encrypt(inputVector: LongArray): ByteArray?
    external fun decrypt(cipherBytes: ByteArray): LongArray?
    external fun addCiphertexts(cipherBytes1: ByteArray, cipherBytes2: ByteArray): ByteArray?
}
