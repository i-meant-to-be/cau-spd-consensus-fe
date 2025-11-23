package com.imeanttobe.consensusapp.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.imeanttobe.consensusapp.data.serializer.idDataStore
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class IdRepoTest {
    private lateinit var repo: IdRepoImpl
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repo = IdRepoImpl(context.idDataStore)
    }

    @After
    fun tearDown() = runTest {
        repo.setId("")
    }

    @Test
    fun saveAndGetId_VerifyValue() = runTest {
        val expectedId = UUID.randomUUID().toString()
        repo.setId(expectedId)

        val actualId = repo.getId()
        assert(actualId == expectedId)
    }

    @Test
    fun checkIdExistence_ReturnsTrue_WhenIdExists() = runTest {
        val expectedId = UUID.randomUUID().toString()
        repo.setId(expectedId)

        val exists = repo.isExist()
        assert(exists)
    }

    @Test
    fun checkIdExistence_ReturnsFalse_WhenIdDoesNotExist() = runTest {
        val exists = repo.isExist()
        assert(!exists)
    }

    @Test
    fun checkIdExistence_ReturnsFalse_WhenIdIsEmpty() = runTest {
        repo.setId("")

        val exists = repo.isExist()
        assert(!exists)
    }

    @Test
    fun checkIdExistence_ReturnsFalse_WhenIdIsBlank() = runTest {
        repo.setId("   ")

        val exists = repo.isExist()
        assert(!exists)
    }
}