package com.example.roomie.domain.model

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.example.roomie.core.Constants.BACKEND_DATE_TIME_FORMAT
import com.example.roomie.core.Constants.FINANCE_TRANSACTION_DATE_FORMAT
import com.example.roomie.core.Constants.FINANCE_TRANSACTION_TIME_FORMAT
import com.example.roomie.data.remote.dto.TransactionDto
import com.squareup.moshi.Json
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Entity(
    tableName = "transactions",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["creatorId"]
        )
    ],
    indices = [Index("creatorId")]
)
data class Transaction(
    var id: Int?,
    var name: String,
    var flatId: Int?,
    var creatorId: Int?,
    var price: Double?,
    @Json(name = "createdOn")
    val createdAt: String?,
    var paid: Boolean?,
    val fetchedAt: Long = System.currentTimeMillis()
) {

    // Parses createdAt to Date type
    fun getCreatedAtDate(): Date = SimpleDateFormat(BACKEND_DATE_TIME_FORMAT, Locale.getDefault()).parse(createdAt!!)!!

    fun getDate() = getSystemTime()?.format(DateTimeFormatter.ofPattern(FINANCE_TRANSACTION_DATE_FORMAT))

    fun getTime() = getSystemTime()?.format(DateTimeFormatter.ofPattern(FINANCE_TRANSACTION_TIME_FORMAT))

    fun getSystemTime(): ZonedDateTime? {
        val time = Instant.parse(createdAt).atZone(ZoneId.systemDefault())
        return time
    }

}

data class TransactionWithCreator(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "creatorId",
        entityColumn = "id"
    ) val creator: User?
)


@Entity(
    tableName = "transactionUserCrossRef",
    primaryKeys = ["transactionId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("transactionId"),
    ]
)
data class TransactionUserCrossRef(
    val transactionId: Int,
    val userId: Int
)


data class TransactionWithCreatorAndUsers(
    @Embedded val transactionWithCreator: TransactionWithCreator,
    @Relation(
        parentColumn = "id",
        entity = User::class,
        entityColumn = "id",
        associateBy = Junction(
            value = TransactionUserCrossRef::class,
            parentColumn = "transactionId",
            entityColumn = "userId"
        )
    )
    @Json(name = "userId")
    var users: List<User>?
) {
    fun isUserInList(userId: Int) = users?.find { it.id == userId } == null

    fun mapToDto(): TransactionDto {
        return TransactionDto(
            id = this.transactionWithCreator.transaction.id,
            name = this.transactionWithCreator.transaction.name,
            price = this.transactionWithCreator.transaction.price,
            users = this.users?.map { it.mapToDto() },
            userIds = this.users?.map { it.id!! },
            flatId = this.transactionWithCreator.transaction.flatId,
            creatorId = this.transactionWithCreator.transaction.creatorId,
            createdAt = this.transactionWithCreator.transaction.createdAt,
            paid = this.transactionWithCreator.transaction.paid,
            liability = null
        )
    }

    companion object {
        fun init(flatId: Int): TransactionWithCreatorAndUsers =
            TransactionWithCreatorAndUsers(
                TransactionWithCreator(
                    transaction = Transaction(
                        id = -1,
                        name = "",
                        flatId = flatId,
                        createdAt = SimpleDateFormat(BACKEND_DATE_TIME_FORMAT).format(Date().time),
                        creatorId = null,
                        paid = null,
                        price = 0.0
                    ),
                    creator = null,
                ),
                users = emptyList()
            )
    }
}