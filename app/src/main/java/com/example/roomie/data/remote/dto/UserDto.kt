package com.example.roomie.data.remote.dto

import com.example.roomie.domain.model.User

data class UserDto (
    var id: Int?,
    var username: String?,
    var email: String?,
    var password: String?
) {
    fun mapToDomainModel(): User {
        return User(
            id = this.id,
            username = this.username,
            email = this.email
        )
    }
}