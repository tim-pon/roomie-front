package com.example.roomie.data.local

import androidx.room.*
import com.example.roomie.domain.model.*
import com.example.roomie.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TransactionDao : BaseDao<Transaction> {

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    abstract fun getTransactionWithUsersById(id: Int): Flow<TransactionWithCreatorAndUsers>

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE flatId = :flatId")
    abstract fun getTransactionsWithUsersByFlatId(flatId: Int): Flow<List<TransactionWithCreatorAndUsers>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertTransactionUsersCrossRefs(transactionUserCrossRefs: List<TransactionUserCrossRef>)

    // Delete CrossRefs for Users not in the userIds List
    @Query("DELETE FROM transactionUserCrossRef WHERE transactionId = :transactionId AND userId NOT IN (:userIds)")
    abstract suspend fun deleteCrossRefs(transactionId: Int, userIds: List<Int>)

    @Query("DELETE FROM transactions WHERE flatId = :flatId and id NOT IN (:transactionIds)")
    abstract suspend fun clearInvalidCache(flatId: Int, transactionIds: List<Int>)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    abstract suspend fun deleteTransactionById(transactionId: Int)

}