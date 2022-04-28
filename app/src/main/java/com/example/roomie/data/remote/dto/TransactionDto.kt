package com.example.roomie.data.remote.dto

import com.example.roomie.domain.model.*
import com.squareup.moshi.Json

data class TransactionDto (
    var id: Int?,
    var name: String,
    var flatId: Int?,
    var creatorId: Int?,
    var price: Double?,
    @Json(name = "createdOn")
    var createdAt: String?,
    var paid: Boolean?,
    @Json(name = "userInfos")
    var users: List<UserDto>?,
    var userIds: List<Int>?,
    var liability: Double?

) {
    fun mapToDomainModel(): TransactionWithCreatorAndUsers {
        return TransactionWithCreatorAndUsers(
            transactionWithCreator = TransactionWithCreator(
                Transaction(
                    id = this.id,
                    name = this.name,
                    flatId = this.flatId,
                    creatorId = this.creatorId,
                    price = this.price,
                    createdAt = this.createdAt,
                    paid = this.paid,
                ),
                creator = null
            ),
            users = this.users?.map { it.mapToDomainModel() }
        )
    }
}