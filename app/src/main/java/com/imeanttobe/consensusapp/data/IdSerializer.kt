package com.imeanttobe.consensusapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import com.imeanttobe.consensusapp.Id
import java.io.InputStream
import java.io.OutputStream

const val FILE_NAME = "id.pb"

object IdSerializer : Serializer<Id> {
    override val defaultValue: Id
        get() = Id.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Id {
        try {
            return Id.parseFrom(input)
        } catch (exception: Exception) {
            throw InvalidProtocolBufferException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Id, output: OutputStream) {
        t.writeTo(output)
    }
}

val Context.idDataStore: DataStore<Id> by dataStore(
    fileName = FILE_NAME,
    serializer = IdSerializer
)