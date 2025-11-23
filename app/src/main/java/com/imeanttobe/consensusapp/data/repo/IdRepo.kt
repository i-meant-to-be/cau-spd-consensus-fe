package com.imeanttobe.consensusapp.data.repo

interface IdRepo {
    suspend fun getId(): String
    suspend fun setId(id: String)
    suspend fun isExist(): Boolean
}