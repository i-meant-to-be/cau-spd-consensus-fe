package com.imeanttobe.consensusapp.domain.repo

interface IdRepo {
    suspend fun getId(): Result<String>
    suspend fun setId(id: String): Result<Boolean>
    suspend fun isExist(): Result<Boolean>

    fun getCachedIdOrEmpty(): String
}