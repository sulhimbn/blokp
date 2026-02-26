package com.example.iurankomplek.session

import android.content.Context
import com.example.iurankomplek.model.User
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for UserSessionManager class.
 * Tests cover: setCurrentUser, clearSession, restoreSession, currentUserId, isLoggedIn state flow.
 *
 * Uses Robolectric for Android Context.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class UserSessionManagerTest {

    private lateinit var context: Context
    private lateinit var userSessionManager: UserSessionManager

    @Before
    fun setup() {
        // Create Robolectric context
        context = Robolectric.buildActivity(android.app.Activity::class.java).get()
        
        // Create fresh instance for each test
        userSessionManager = UserSessionManager(context)
    }

    @Test
    fun `verify UserSessionManager initial state`() {
        // Verify initial state - should be logged out
        assertFalse(userSessionManager.isLoggedIn.value)
        assertNull(userSessionManager.currentUser.value)
        assertNull(userSessionManager.currentUserId)
    }

    @Test
    fun `setCurrentUser should update currentUser StateFlow`() {
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
    fun `clearSession should reset currentUser StateFlow`() {
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
    fun `setCurrentUser with null avatar should work correctly`() {
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
    fun `currentUserId returns null when no user is logged in`() {
        assertNull(userSessionManager.currentUserId)
    }

    @Test
    fun `currentUserId returns correct ID after setting user`() {
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
    fun `isLoggedIn initial state is false`() {
        assertFalse(userSessionManager.isLoggedIn.value)
    }

    @Test
    fun `isLoggedIn becomes true after setting user`() {
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
    fun `clearSession after setting multiple users should work correctly`() {
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
