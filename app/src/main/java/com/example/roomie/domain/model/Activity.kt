package com.example.roomie.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "activities",
    primaryKeys = ["id"]
)
data class Activity(
    val id: Int,
    val flatId: Int,
    val userId: Int,
    val activity: String,
    val username: String,
    val referenceName: String?,
    val createdOn: String?
) {

    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    lateinit var fetchedAt: String

}