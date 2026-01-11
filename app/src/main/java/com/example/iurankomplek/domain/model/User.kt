package com.example.iurankomplek.domain.model

/**
 * Domain model representing a user in the business domain.
 * 
 * This is a pure domain model representing the User entity in the business layer.
 * It contains business logic and validation rules independent of any framework.
 * 
 * @property id Unique identifier for the user
 * @property email User's email address (must contain @ symbol)
 * @property firstName User's first name
 * @property lastName User's last name
 * @property alamat User's address
 * @property avatar URL of user's avatar image
 * @property fullName Computed full name (first name + last name)
 */
data class User(
    val id: Long = 0,
    val email: String,
    val firstName: String,
    val lastName: String,
    val alamat: String,
    val avatar: String
) {
    val fullName: String
        get() = "$firstName $lastName"

    init {
        validate()
    }

    private fun validate() {
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(firstName.isNotBlank()) { "First name cannot be blank" }
        require(lastName.isNotBlank()) { "Last name cannot be blank" }
        require(alamat.isNotBlank()) { "Alamat cannot be blank" }
        require(email.contains("@")) { "Email must contain @ symbol" }
        require(email.length <= MAX_EMAIL_LENGTH) { "Email too long" }
        require(firstName.length <= MAX_NAME_LENGTH) { "First name too long" }
        require(lastName.length <= MAX_NAME_LENGTH) { "Last name too long" }
        require(alamat.length <= MAX_ALAMAT_LENGTH) { "Alamat too long" }
        require(avatar.length <= MAX_AVATAR_LENGTH) { "Avatar URL too long" }
    }

    companion object {
        const val MAX_EMAIL_LENGTH = 255
        const val MAX_NAME_LENGTH = 100
        const val MAX_ALAMAT_LENGTH = 500
        const val MAX_AVATAR_LENGTH = 500

        fun fromEntity(
            id: Long,
            email: String,
            firstName: String,
            lastName: String,
            alamat: String,
            avatar: String
        ): User {
            return User(
                id = id,
                email = email,
                firstName = firstName,
                lastName = lastName,
                alamat = alamat,
                avatar = avatar
            )
        }
    }
}
