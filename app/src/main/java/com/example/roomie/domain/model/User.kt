package com.example.roomie.domain.model

import androidx.room.Entity
import androidx.room.Ignore
import com.example.roomie.core.Constants
import com.example.roomie.data.remote.dto.UserDto
import java.time.Instant

@Entity(
    tableName = "users",
    primaryKeys = ["id"]
)

data class User(
    val id: Int?,
    val username: String?,
    val email: String?,
    val fetchedAt: Long = System.currentTimeMillis()
){
    @Ignore
    private var password: String? = null

    @Ignore
    var flats: MutableList<Flat> = mutableListOf()

//    // On insert Sqlite sets current timestamp by default
//    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
//    @Transient
//    lateinit var fetchedAt: String

    // Only required for transaction dialog
    @Ignore     // Don't save in cache
    @Transient  // Don't parse to json
    @JvmField   // Required for databinding
    var isInTransaction: Boolean = false


    constructor(
        id: Int?,
        username: String?,
        email: String?,
        password: String?,
        fetchedAt: Long = System.currentTimeMillis()
    ): this(id, username, email, fetchedAt) {
        this.password = password
    }

    fun getImageUrl() = Constants.BASE_URL + "user/image/$id"


    fun mapToDto(): UserDto {
        return UserDto(
            id = this.id,
            username = this.username,
            email = this.email,
            password = this.password
        )
    }
}

data class ChangePassword(
    val oldpassword: String?,
    val newpassword: String?,
)
