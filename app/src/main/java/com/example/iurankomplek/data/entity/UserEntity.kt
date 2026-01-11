package com.example.iurankomplek.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.iurankomplek.data.constraints.UserConstraints
import java.util.Date

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["last_name", "first_name"])
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "first_name")
    val firstName: String,

    @ColumnInfo(name = "last_name")
    val lastName: String,

    @ColumnInfo(name = "alamat")
    val alamat: String,

    @ColumnInfo(name = "avatar")
    val avatar: String,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updated_at")
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
        require(email.length <= UserConstraints.Constraints.MAX_EMAIL_LENGTH) { "Email too long" }
        require(firstName.length <= UserConstraints.Constraints.MAX_NAME_LENGTH) { "First name too long" }
        require(lastName.length <= UserConstraints.Constraints.MAX_NAME_LENGTH) { "Last name too long" }
        require(alamat.length <= UserConstraints.Constraints.MAX_ALAMAT_LENGTH) { "Alamat too long" }
        require(avatar.length <= UserConstraints.Constraints.MAX_AVATAR_LENGTH) { "Avatar URL too long" }
    }

    val fullName: String
        get() = "$firstName $lastName"
}
