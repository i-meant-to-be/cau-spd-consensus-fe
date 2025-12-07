package com.imeanttobe.consensusapp.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor() {
    companion object {
        // Define the constants for the encryption algorithm and key alias
        private const val KEY_ALIAS = "CONSENSUS_MASTER_KEY"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val KEY_SIZE = 256
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SIZE = 12

    }

    // Keystore instance
    private val keyStore by lazy {
        try {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load Android KeyStore", e)
        }
    }

    /**
     * Android Keystore에 저장된 키를 가져오거나, 없다면 새로 생성합니다.
     */
    private fun getOrCreateKey(): SecretKey {
        // 1. Try to get the key
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry

        // 2. If key exists, return it
        if (existingKey != null) {
            return existingKey.secretKey
        }

        // 3. Else, prepare key generation spec
        val paramsBuilder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        paramsBuilder.apply {
            setBlockModes(BLOCK_MODE)
            setEncryptionPaddings(PADDING)
            setKeySize(KEY_SIZE)
            setRandomizedEncryptionRequired(true)
        }

        // 4. Generate key with the spec
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM, ANDROID_KEYSTORE)
        keyGenerator.init(paramsBuilder.build())
        return keyGenerator.generateKey()
    }

    fun encrypt(plainText: ByteArray): EncryptionResult {
        // 0. Check whether plainText is not empty
        require(plainText.isNotEmpty()) { "Plain text cannot be empty" }

        try {
            // 1. Load cipher instance
            val cipher = Cipher.getInstance(TRANSFORMATION)

            // 2. Init cipher with encryption mode
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

            // 3. Return encrypted text and IV
            return EncryptionResult(cipher.doFinal(plainText), cipher.iv)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to encrypt data", e)
        }
    }

    fun decrypt(ciphertext: ByteArray, iv: ByteArray): ByteArray {
        require(ciphertext.isNotEmpty()) { "Encrypted text cannot be empty" }
        require(iv.isNotEmpty()) { "IV cannot be empty" }
        require(iv.size == IV_SIZE) { "IV must be $IV_SIZE bytes" }

        try {
            // 1. Load cipher instance and prepare variables
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)

            // 2. Init cipher with decryption mode
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

            // 3. Return decrypted text
            return cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to decrypt data", e)
        }
    }

    fun decrypt(encryptionResult: EncryptionResult): ByteArray {
        return decrypt(encryptionResult.ciphertext, encryptionResult.iv)
    }
}