package com.example.iurankomplek.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.iurankomplek.data.constraints.DatabaseConstraints
import java.util.Date

@Entity(
    tableName = DatabaseConstraints.Users.TABLE_NAME,
    indices = [
        Index(value = [DatabaseConstraints.Users.Columns.EMAIL], unique = true),
        Index(value = [DatabaseConstraints.Users.Columns.LAST_NAME, DatabaseConstraints.Users.Columns.FIRST_NAME])
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DatabaseConstraints.Users.Columns.ID)
    val id: Long = 0,

    @ColumnInfo(name = DatabaseConstraints.Users.Columns.EMAIL)
    val email: String,

    @ColumnInfo(name = DatabaseConstraints.Users.Columns.FIRST_NAME)
    val firstName: String,

    @ColumnInfo(name = DatabaseConstraints.Users.Columns.LAST_NAME)
    val lastName: String,

    @ColumnInfo(name = DatabaseConstraints.Users.Columns.ALAMAT)
    val alamat: String,

    @ColumnInfo(name = DatabaseConstraints.Users.Columns.AVATAR)
    val avatar: String,

    @ColumnInfo(name = DatabaseConstraints.Users.Columns.CREATED_AT)
    val createdAt: Date = Date(),

    @ColumnInfo(name = DatabaseConstraints.Users.Columns.UPDATED_AT)
    val updatedAt: Date = Date()
) {
    init {
        validate()
    }

    private fun validate() {
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(firstName.isNotBlank()) { "First name cannot be blank" }
        require(lastName.isNotBlank()) { "Last name cannot be blank" }
        require(alamat.isNotBlank()) { "Alamat cannot be blank" }
        require(email.contains("@")) { "Email must contain @ symbol" }
        require(email.length <= DatabaseConstraints.Users.Constraints.MAX_EMAIL_LENGTH) { "Email too long" }
        require(firstName.length <= DatabaseConstraints.Users.Constraints.MAX_NAME_LENGTH) { "First name too long" }
        require(lastName.length <= DatabaseConstraints.Users.Constraints.MAX_NAME_LENGTH) { "Last name too long" }
        require(alamat.length <= DatabaseConstraints.Users.Constraints.MAX_ALAMAT_LENGTH) { "Alamat too long" }
        require(avatar.length <= DatabaseConstraints.Users.Constraints.MAX_AVATAR_LENGTH) { "Avatar URL too long" }
    }

    val fullName: String
        get() = "$firstName $lastName"
}
