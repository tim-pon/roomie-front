package com.example.roomie.domain.model

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity(
    tableName = "flats",
    primaryKeys = ["id"],
)
data class Flat (
    val id: Int?,
    val name: String?,
    val creatorId: Int?,
    val entryCode: String?,
    val fetchedAt: Long = System.currentTimeMillis()
) {

    @Ignore
    var users: MutableList<User> = mutableListOf()
}


@Entity(
    tableName = "flatUserCrossRef",
    primaryKeys = ["flatId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = Flat::class,
            parentColumns = ["id"],
            childColumns = ["flatId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class FlatUserCrossRef(
    val flatId: Int,
    val userId: Int
)


data class FlatWithUsers(
    @Embedded val flat: Flat,
    @Relation(
        parentColumn = "id",
        entity = User::class,
        entityColumn = "id",
        associateBy = Junction(
            value = FlatUserCrossRef::class,
            parentColumn = "flatId",
            entityColumn = "userId"
        )
    ) val users: List<User>
)