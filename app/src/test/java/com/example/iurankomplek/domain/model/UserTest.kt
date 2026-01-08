package com.example.iurankomplek.domain.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class UserTest {

    @Test
    fun user_withValidData_isCreatedSuccessfully() {
        val user = User(
            id = 1L,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "http://example.com/avatar.jpg"
        )

        assertEquals(1L, user.id)
        assertEquals("test@example.com", user.email)
        assertEquals("John", user.firstName)
        assertEquals("Doe", user.lastName)
        assertEquals("123 Main St", user.alamat)
        assertEquals("http://example.com/avatar.jpg", user.avatar)
    }

    @Test
    fun fullName_returnsFirstNameAndLastNameCombined() {
        val user = User(
            id = 1L,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "http://example.com/avatar.jpg"
        )

        assertEquals("John Doe", user.fullName)
    }

    @Test
    fun user_withBlankEmail_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            User(
                id = 1L,
                email = "   ",
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "http://example.com/avatar.jpg"
            )
        }

        assertTrue(exception.message!!.contains("Email cannot be blank"))
    }

    @Test
    fun user_withEmailWithoutAtSymbol_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            User(
                id = 1L,
                email = "invalidemail.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "http://example.com/avatar.jpg"
            )
        }

        assertTrue(exception.message!!.contains("Email must contain @"))
    }

    @Test
    fun user_withEmailExceedingMaxLength_throwsIllegalArgumentException() {
        val longEmail = "a".repeat(256) + "@example.com"

        val exception = assertFailsWith<IllegalArgumentException> {
            User(
                id = 1L,
                email = longEmail,
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "http://example.com/avatar.jpg"
            )
        }

        assertTrue(exception.message!!.contains("Email too long"))
    }

    @Test
    fun user_withEmailAtMaxLength_isCreatedSuccessfully() {
        val email = "a".repeat(255)

        val user = User(
            id = 1L,
            email = email,
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "http://example.com/avatar.jpg"
        )

        assertEquals(email, user.email)
    }

    @Test
    fun user_withBlankFirstName_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            User(
                id = 1L,
                email = "test@example.com",
                firstName = "   ",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "http://example.com/avatar.jpg"
            )
        }

        assertTrue(exception.message!!.contains("First name cannot be blank"))
    }

    @Test
    fun user_withFirstNameExceedingMaxLength_throwsIllegalArgumentException() {
        val longName = "a".repeat(101)

        val exception = assertFailsWith<IllegalArgumentException> {
            User(
                id = 1L,
                email = "test@example.com",
                firstName = longName,
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "http://example.com/avatar.jpg"
            )
        }

        assertTrue(exception.message!!.contains("First name too long"))
    }

    @Test
    fun user_withFirstNameAtMaxLength_isCreatedSuccessfully() {
        val firstName = "a".repeat(100)

        val user = User(
            id = 1L,
            email = "test@example.com",
            firstName = firstName,
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "http://example.com/avatar.jpg"
        )

        assertEquals(firstName, user.firstName)
    }

    @Test
    fun user_withBlankLastName_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            User(
                id = 1L,
                email = "test@example.com",
                firstName = "John",
                lastName = "   ",
                alamat = "123 Main St",
                avatar = "http://example.com/avatar.jpg"
            )
        }

        assertTrue(exception.message!!.contains("Last name cannot be blank"))
    }

    @Test
    fun user_withLastNameExceedingMaxLength_throwsIllegalArgumentException() {
        val longName = "a".repeat(101)

        val exception = assertFailsWith<IllegalArgumentException> {
            User(
                id = 1L,
                email = "test@example.com",
                firstName = "John",
                lastName = longName,
                alamat = "123 Main St",
                avatar = "http://example.com/avatar.jpg"
            )
        }

        assertTrue(exception.message!!.contains("Last name too long"))
    }

    @Test
    fun user_withLastNameAtMaxLength_isCreatedSuccessfully() {
        val lastName = "a".repeat(100)

        val user = User(
            id = 1L,
            email = "test@example.com",
            firstName = "John",
            lastName = lastName,
            alamat = "123 Main St",
            avatar = "http://example.com/avatar.jpg"
        )

        assertEquals(lastName, user.lastName)
    }

    @Test
    fun user_withBlankAlamat_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            User(
                id = 1L,
                email = "test@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "   ",
                avatar = "http://example.com/avatar.jpg"
            )
        }

        assertTrue(exception.message!!.contains("Alamat cannot be blank"))
    }

    @Test
    fun user_withAlamatExceedingMaxLength_throwsIllegalArgumentException() {
        val longAlamat = "a".repeat(501)

        val exception = assertFailsWith<IllegalArgumentException> {
            User(
                id = 1L,
                email = "test@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = longAlamat,
                avatar = "http://example.com/avatar.jpg"
            )
        }

        assertTrue(exception.message!!.contains("Alamat too long"))
    }

    @Test
    fun user_withAlamatAtMaxLength_isCreatedSuccessfully() {
        val alamat = "a".repeat(500)

        val user = User(
            id = 1L,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = alamat,
            avatar = "http://example.com/avatar.jpg"
        )

        assertEquals(alamat, user.alamat)
    }

    @Test
    fun user_withAvatarExceedingMaxLength_throwsIllegalArgumentException() {
        val longAvatar = "a".repeat(501)

        val exception = assertFailsWith<IllegalArgumentException> {
            User(
                id = 1L,
                email = "test@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = longAvatar
            )
        }

        assertTrue(exception.message!!.contains("Avatar URL too long"))
    }

    @Test
    fun user_withAvatarAtMaxLength_isCreatedSuccessfully() {
        val avatar = "a".repeat(500)

        val user = User(
            id = 1L,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = avatar
        )

        assertEquals(avatar, user.avatar)
    }

    @Test
    fun user_withEmptyEmail_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            User(
                id = 1L,
                email = "",
                firstName = "John",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "http://example.com/avatar.jpg"
            )
        }

        assertTrue(exception.message!!.contains("Email cannot be blank"))
    }

    @Test
    fun user_withEmptyFirstName_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            User(
                id = 1L,
                email = "test@example.com",
                firstName = "",
                lastName = "Doe",
                alamat = "123 Main St",
                avatar = "http://example.com/avatar.jpg"
            )
        }

        assertTrue(exception.message!!.contains("First name cannot be blank"))
    }

    @Test
    fun user_withEmptyLastName_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            User(
                id = 1L,
                email = "test@example.com",
                firstName = "John",
                lastName = "",
                alamat = "123 Main St",
                avatar = "http://example.com/avatar.jpg"
            )
        }

        assertTrue(exception.message!!.contains("Last name cannot be blank"))
    }

    @Test
    fun user_withEmptyAlamat_throwsIllegalArgumentException() {
        val exception = assertFailsWith<IllegalArgumentException> {
            User(
                id = 1L,
                email = "test@example.com",
                firstName = "John",
                lastName = "Doe",
                alamat = "",
                avatar = "http://example.com/avatar.jpg"
            )
        }

        assertTrue(exception.message!!.contains("Alamat cannot be blank"))
    }

    @Test
    fun fromEntity_createsUserFromParameters() {
        val user = User.fromEntity(
            id = 1L,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "http://example.com/avatar.jpg"
        )

        assertEquals(1L, user.id)
        assertEquals("test@example.com", user.email)
        assertEquals("John", user.firstName)
        assertEquals("Doe", user.lastName)
        assertEquals("123 Main St", user.alamat)
        assertEquals("http://example.com/avatar.jpg", user.avatar)
    }

    @Test
    fun user_withMultipleAtSymbolsInEmail_isCreatedSuccessfully() {
        val user = User(
            id = 1L,
            email = "john@doe@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "http://example.com/avatar.jpg"
        )

        assertEquals("john@doe@example.com", user.email)
    }

    @Test
    fun user_withZeroId_isCreatedSuccessfully() {
        val user = User(
            id = 0L,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "http://example.com/avatar.jpg"
        )

        assertEquals(0L, user.id)
    }

    @Test
    fun user_withNumericValuesInFirstName_isCreatedSuccessfully() {
        val user = User(
            id = 1L,
            email = "test@example.com",
            firstName = "John123",
            lastName = "Doe",
            alamat = "123 Main St",
            avatar = "http://example.com/avatar.jpg"
        )

        assertEquals("John123", user.firstName)
    }

    @Test
    fun user_withSpecialCharactersInAlamat_isCreatedSuccessfully() {
        val user = User(
            id = 1L,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            alamat = "123 Main St, Apt 4B",
            avatar = "http://example.com/avatar.jpg"
        )

        assertEquals("123 Main St, Apt 4B", user.alamat)
    }
}
