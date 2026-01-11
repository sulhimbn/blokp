package com.example.iurankomplek.data.entity

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class UserEntityTest {

    @Test
    fun `create user with valid data should succeed`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        assertEquals(1L, user.id)
        assertEquals("test@example.com", user.email)
        assertEquals("John", user.firstName)
        assertEquals("Doe", user.lastName)
        assertEquals("123 Main St", user.alamat)
        assertEquals("https://example.com/avatar.jpg", user.avatar)
    }

    @Test
    fun `create user with default dates should use current time`() {
        val beforeCreate = Date()
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
        val afterCreate = Date()

        assertNotNull(user.createdAt)
        assertNotNull(user.updatedAt)
        assertTrue(user.createdAt.time >= beforeCreate.time)
        assertTrue(user.createdAt.time <= afterCreate.time)
        assertTrue(user.updatedAt.time >= beforeCreate.time)
        assertTrue(user.updatedAt.time <= afterCreate.time)
    }

    @Test
    fun `create user with custom dates should preserve them`() {
        val customDate = Date(1000000)
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg",
            createdAt = customDate,
            updatedAt = customDate
        )

        assertEquals(customDate, user.createdAt)
        assertEquals(customDate, user.updatedAt)
    }

    @Test
    fun `fullName should combine first and last name`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        assertEquals("John Doe", user.fullName)
    }

    @Test
    fun `fullName should handle middle names`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John William",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        assertEquals("John William Doe", user.fullName)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create user with blank email should throw exception`() {
        UserEntity(
            id = 1,
            email = "",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create user with blank firstName should throw exception`() {
        UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create user with blank lastName should throw exception`() {
        UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create user with blank alamat should throw exception`() {
        UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "",
            avatar = "https://example.com/avatar.jpg"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create user with whitespace-only email should throw exception`() {
        UserEntity(
            id = 1,
            email = "   ",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create user with email without @ symbol should throw exception`() {
        UserEntity(
            id = 1,
            email = "invalidemail.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create user with email exceeding max length should throw exception`() {
        val longEmail = "a".repeat(256) + "@example.com"
        UserEntity(
            id = 1,
            email = longEmail,
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create user with firstName exceeding max length should throw exception`() {
        val longName = "a".repeat(101)
        UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = longName,
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create user with lastName exceeding max length should throw exception`() {
        val longName = "a".repeat(101)
        UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = longName,
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create user with alamat exceeding max length should throw exception`() {
        val longAlamat = "a".repeat(501)
        UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = longAlamat,
            avatar = "https://example.com/avatar.jpg"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `create user with avatar URL exceeding max length should throw exception`() {
        val longAvatar = "https://example.com/" + "a".repeat(2039)
        UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = longAvatar
        )
    }

    @Test
    fun `create user with email at max length should succeed`() {
        val maxEmail = "a".repeat(251) + "@example.com"
        val user = UserEntity(
            id = 1,
            email = maxEmail,
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        assertEquals(maxEmail, user.email)
    }

    @Test
    fun `create user with firstName at max length should succeed`() {
        val maxName = "a".repeat(100)
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = maxName,
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        assertEquals(maxName, user.firstName)
    }

    @Test
    fun `create user with lastName at max length should succeed`() {
        val maxName = "a".repeat(100)
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = maxName,
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        assertEquals(maxName, user.lastName)
    }

    @Test
    fun `create user with alamat at max length should succeed`() {
        val maxAlamat = "a".repeat(500)
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = maxAlamat,
            avatar = "https://example.com/avatar.jpg"
        )

        assertEquals(maxAlamat, user.alamat)
    }

    @Test
    fun `create user with avatar URL at max length should succeed`() {
        val maxAvatar = "https://example.com/" + "a".repeat(2023)
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = maxAvatar
        )

        assertEquals(maxAvatar, user.avatar)
    }

    @Test
    fun `create user with valid email format should succeed`() {
        val validEmails = listOf(
            "simple@example.com",
            "very.common@example.com",
            "disposable.style.email.with+symbol@example.com",
            "other.email-with-hyphen@example.com",
            "fully-qualified-domain@example.com",
            "user.name+tag+sorting@example.com",
            "x@example.com",
            "example-indeed@strange-example.com",
            "admin@mailserver1"
        )

        validEmails.forEach { email ->
            val user = UserEntity(
                id = 1,
                email = email,
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "https://example.com/avatar.jpg"
            )
            assertEquals(email, user.email)
        }
    }

    @Test
    fun `create user with special characters in name should succeed`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "José",
            lastName = "García",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        assertEquals("José", user.firstName)
        assertEquals("García", user.lastName)
        assertEquals("José García", user.fullName)
    }

    @Test
    fun `create user with special characters in alamat should succeed`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St, Apt 4B - Floor 2",
            avatar = "https://example.com/avatar.jpg"
        )

        assertEquals("123 Main St, Apt 4B - Floor 2", user.alamat)
    }

    @Test
    fun `create user with default id should use 0`() {
        val user = UserEntity(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        assertEquals(0L, user.id)
    }

    @Test
    fun `create user with long avatar URL should succeed`() {
        val longAvatar = "https://example.com/images/avatars/" + "a".repeat(1900) + ".jpg"
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = longAvatar
        )

        assertEquals(longAvatar, user.avatar)
    }

    @Test
    fun `create user with empty avatar URL should throw exception`() {
        try {
            UserEntity(
                id = 1,
                email = "test@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = ""
            )
            fail("Should have thrown IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Avatar URL"))
        }
    }

    @Test
    fun `data class equality should work correctly`() {
        val user1 = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val user2 = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        assertEquals(user1, user2)
        assertEquals(user1.hashCode(), user2.hashCode())
    }

    @Test
    fun `data class copy should work correctly`() {
        val user1 = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "https://example.com/avatar.jpg"
        )

        val user2 = user1.copy(firstName = "Jane")

        assertEquals("John", user1.firstName)
        assertEquals("Jane", user2.firstName)
        assertEquals(user1.lastName, user2.lastName)
        assertEquals(user1.email, user2.email)
    }

    @Test
    fun `create user with numeric characters in alamat should succeed`() {
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "Jl. Raya No. 123, RT 05/RW 02, Kecamatan X",
            avatar = "https://example.com/avatar.jpg"
        )

        assertEquals("Jl. Raya No. 123, RT 05/RW 02, Kecamatan X", user.alamat)
    }

    @Test
    fun `create user with URL containing query parameters should succeed`() {
        val avatarWithQuery = "https://example.com/avatar.jpg?width=200&height=200&quality=90"
        val user = UserEntity(
            id = 1,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = avatarWithQuery
        )

        assertEquals(avatarWithQuery, user.avatar)
    }
}
