package com.imeanttobe.consensusapp.data.local.serializer

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.imeanttobe.consensusapp.PollInfoItems
import java.io.InputStream
import java.io.OutputStream

const val FILE_NAME_POLL_INFO_ITEMS = "poll_info_items.pb"

object PollInfoItemsSerializer : Serializer<PollInfoItems> {
    override val defaultValue: PollInfoItems
        get() = PollInfoItems.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): PollInfoItems {
        try {
            return PollInfoItems.parseFrom(input)
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: PollInfoItems, output: OutputStream) {
        t.writeTo(output)
    }
}

val Context.pollInfoItemsDataStore: DataStore<PollInfoItems> by dataStore(
    fileName = FILE_NAME_POLL_INFO_ITEMS,
    serializer = PollInfoItemsSerializer
)