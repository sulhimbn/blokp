package com.example.iurankomplek.session

import com.example.iurankomplek.model.User

/**
 * Persistence boundary for the logged-in user. Implemented by
 * [EncryptedPrefsSessionStore] in production and by in-memory fakes in tests,
 * so session state logic does not depend on AndroidKeyStore availability.
 */
interface SessionStore {
    fun loadUser(): User?
    fun saveUser(user: User)
    fun clear()
}