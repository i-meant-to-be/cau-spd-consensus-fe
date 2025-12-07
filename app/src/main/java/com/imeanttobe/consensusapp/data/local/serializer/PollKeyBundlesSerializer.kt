package com.imeanttobe.consensusapp.data.local.serializer

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.imeanttobe.consensusapp.PollKeyBundles
import java.io.InputStream
import java.io.OutputStream

const val FILE_NAME_KEY_BUNDLE = "poll_key_bundle.pb"

object PollKeyBundlesSerializer : Serializer<PollKeyBundles> {
    override val defaultValue: PollKeyBundles
        get() = PollKeyBundles.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): PollKeyBundles {
        try {
            return PollKeyBundles.parseFrom(input)
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: PollKeyBundles, output: OutputStream) {
        t.writeTo(output)
    }
}

val Context.pollKeyBundlesDataStore: DataStore<PollKeyBundles> by dataStore(
    fileName = FILE_NAME_KEY_BUNDLE,
    serializer = PollKeyBundlesSerializer
)