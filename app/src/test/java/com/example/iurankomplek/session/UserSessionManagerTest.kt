package com.example.iurankomplek.session

import com.example.iurankomplek.model.User
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for UserSessionManager state logic.
 * Persistence is supplied through [SessionStore] so the tests run on the JVM
 * without AndroidKeyStore; the encrypted implementation is covered by
 * EncryptedPrefsSessionStore's own instrumentation expectations.
 */
class UserSessionManagerTest {

    private class InMemorySessionStore : SessionStore {
        var stored: User? = null
        var saveCount = 0
        var clearCount = 0

        override fun loadUser(): User? = stored
        override fun saveUser(user: User) {
            stored = user
            saveCount++
        }

        override fun clear() {
            stored = null
            clearCount++
        }
    }

    private lateinit var store: InMemorySessionStore
    private lateinit var userSessionManager: UserSessionManager

    @Before
    fun setup() {
        store = InMemorySessionStore()
        userSessionManager = UserSessionManager(store)
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
    fun `setCurrentUser should persist the user through the session store`() {
        val testUser = User("user123", "test@example.com", "John", "Doe")

        userSessionManager.setCurrentUser(testUser)

        assertEquals(testUser, store.stored)
        assertEquals(1, store.saveCount)
        assertEquals(0, store.clearCount)
    }

    @Test
    fun `clearSession should wipe the persisted user`() {
        userSessionManager.setCurrentUser(User("user123", "test@example.com", "John", "Doe"))

        userSessionManager.clearSession()

        assertNull(store.stored)
        assertEquals(1, store.clearCount)
    }

    @Test
    fun `an existing persisted session should be restored on construction`() {
        val persisted = User("restored", "restored@example.com", "Grace", "Hopper")
        store.stored = persisted

        val restoredManager = UserSessionManager(store)

        assertEquals(persisted, restoredManager.currentUser.value)
        assertTrue(restoredManager.isLoggedIn.value)
        assertEquals("restored", restoredManager.currentUserId)
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
