package com.imeanttobe.consensusapp.seal

data class SealKeys(
    val pk: ByteArray,
    val sk: ByteArray,
    val rk: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SealKeys
        if (!pk.contentEquals(other.pk)) return false
        if (!sk.contentEquals(other.sk)) return false
        if (!rk.contentEquals(other.rk)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = pk.contentHashCode()
        result = 31 * result + sk.contentHashCode()
        result = 31 * result + rk.contentHashCode()
        return result
    }
}
