package com.example.iurankomplek.session

import android.content.Context
import com.example.iurankomplek.model.User
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.runner.RunWith

/**
 * Unit tests for UserSessionManager class.
 * Tests cover: setCurrentUser, clearSession, restoreSession, currentUserId, isLoggedIn state flow.
 *
 * Requires a real AndroidKeyStore, so it runs as an instrumented test on device.
 */
@RunWith(AndroidJUnit4::class)
class UserSessionManagerTest {

    private lateinit var context: Context
    private lateinit var userSessionManager: UserSessionManager

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        // Restores persisted state on construction; the AndroidX crypto keyset files
        // must go too, otherwise the session leaks between test methods.
        listOf(
            "user_session_prefs",
            "__androidx_security_crypto_encrypted_prefs_key_keyset__",
            "__androidx_security_crypto_encrypted_prefs_value_keyset__"
        ).forEach { context.deleteSharedPreferences(it) }

        userSessionManager = UserSessionManager(context)
        userSessionManager.clearSession()
    }

    @Test
    fun verifyUserSessionManagerInitialState() {
        // Verify initial state - should be logged out
        assertFalse(userSessionManager.isLoggedIn.value)
        assertNull(userSessionManager.currentUser.value)
        assertNull(userSessionManager.currentUserId)
    }

    @Test
    fun setCurrentUserShouldUpdateCurrentUserStateFlow() {
        val testUser = User(
            id = "user123",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            avatar = "https://example.com/avatar.jpg"
        )
        
        userSessionManager.setCurrentUser(testUser)
        
        // Verify state flow is updated
        assertEquals(testUser, userSessionManager.currentUser.value)
        assertTrue(userSessionManager.isLoggedIn.value)
        assertEquals("user123", userSessionManager.currentUserId)
    }

    @Test
    fun clearSessionShouldResetCurrentUserStateFlow() {
        // First set a user
        val testUser = User(
            id = "user123",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            avatar = "https://example.com/avatar.jpg"
        )
        userSessionManager.setCurrentUser(testUser)
        
        // Verify user is set
        assertNotNull(userSessionManager.currentUser.value)
        assertTrue(userSessionManager.isLoggedIn.value)
        
        // Clear session
        userSessionManager.clearSession()
        
        // Verify state is reset
        assertNull(userSessionManager.currentUser.value)
        assertFalse(userSessionManager.isLoggedIn.value)
        assertNull(userSessionManager.currentUserId)
    }

    @Test
    fun setCurrentUserWithNullAvatarShouldWorkCorrectly() {
        val testUser = User(
            id = "user456",
            email = "noavatar@example.com",
            firstName = "Jane",
            lastName = "Smith",
            avatar = null
        )
        
        userSessionManager.setCurrentUser(testUser)
        
        // Verify user is set with null avatar
        assertEquals(testUser, userSessionManager.currentUser.value)
        assertTrue(userSessionManager.isLoggedIn.value)
        assertEquals("user456", userSessionManager.currentUserId)
    }

    @Test
    fun currentUserIdReturnsNullWhenNoUserIsLoggedIn() {
        assertNull(userSessionManager.currentUserId)
    }

    @Test
    fun currentUserIdReturnsCorrectIDAfterSettingUser() {
        val testUser = User(
            id = "specificUserId",
            email = "test@example.com",
            firstName = "Test",
            lastName = "User"
        )
        
        userSessionManager.setCurrentUser(testUser)
        
        assertEquals("specificUserId", userSessionManager.currentUserId)
    }

    @Test
    fun isLoggedInInitialStateIsFalse() {
        assertFalse(userSessionManager.isLoggedIn.value)
    }

    @Test
    fun isLoggedInBecomesTrueAfterSettingUser() {
        assertFalse(userSessionManager.isLoggedIn.value)
        
        val testUser = User(
            id = "user123",
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe"
        )
        userSessionManager.setCurrentUser(testUser)
        
        assertTrue(userSessionManager.isLoggedIn.value)
    }

    @Test
    fun clearSessionAfterSettingMultipleUsersShouldWorkCorrectly() {
        // Set first user
        val user1 = User(
            id = "user1",
            email = "user1@example.com",
            firstName = "User",
            lastName = "One"
        )
        userSessionManager.setCurrentUser(user1)
        
        // Replace with second user
        val user2 = User(
            id = "user2",
            email = "user2@example.com",
            firstName = "User",
            lastName = "Two"
        )
        userSessionManager.setCurrentUser(user2)
        
        // Verify second user is current
        assertEquals("user2", userSessionManager.currentUserId)
        assertTrue(userSessionManager.isLoggedIn.value)
        
        // Clear
        userSessionManager.clearSession()
        
        // Verify cleared
        assertNull(userSessionManager.currentUser.value)
        assertFalse(userSessionManager.isLoggedIn.value)
    }
}
