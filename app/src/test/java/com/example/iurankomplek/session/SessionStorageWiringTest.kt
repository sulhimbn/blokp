package com.example.iurankomplek.session

import androidx.security.crypto.MasterKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric has no AndroidKeyStore, so the encrypted preference store cannot be opened in a
 * JVM test. This pins the configuration facts that keep the real session store safe: the
 * production path resolves keystore-backed types, and the plain-preferences seam exists only
 * alongside them.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SessionStorageWiringTest {

    @Test
    fun `production path resolves a keystore-backed master key`() {
        val returnTypes = UserSessionManager::class.java.declaredMethods.map { it.returnType }

        assertTrue(
            "expected a MasterKey accessor among $returnTypes",
            returnTypes.contains(MasterKey::class.java)
        )
    }

    @Test
    fun `plain preferences remain an internal test seam`() {
        val twoArg = UserSessionManager::class.java.constructors.filter { it.parameterCount == 2 }

        assertEquals(
            "exactly one secondary constructor may accept a preferences override",
            1,
            twoArg.size
        )
        assertEquals(
            android.content.SharedPreferences::class.java,
            twoArg.single().parameterTypes[1]
        )
    }

    @Test
    fun `the encrypted store is reachable through the single-argument constructor`() {
        val singleArg = UserSessionManager::class.java.constructors.filter { it.parameterCount == 1 }

        assertEquals(1, singleArg.size)
        assertEquals(
            android.content.Context::class.java,
            singleArg.single().parameterTypes[0]
        )
    }
}
