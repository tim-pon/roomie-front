package com.example.roomie.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction
import com.example.roomie.domain.model.*

@Dao
abstract class UserDao : BaseDao<User> {

    @Query("SELECT * FROM users")
    abstract fun getUserInfos(): Flow<List<User>>

    @Transaction
    @Query("SELECT * FROM flats WHERE id = :flatId")
    abstract fun getFlatWithUsers(flatId: Int): Flow<FlatWithUsers>

    @Query("DELETE FROM users WHERE id NOT IN (:userIds)")
    abstract suspend fun deleteUsersNotInFlat(userIds: List<Int>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertFlatUserCrossRefs(flatUserCrossRefs: List<FlatUserCrossRef>)

    @Insert
    abstract suspend fun addUsersToFlat(userCrossRefs: List<FlatUserCrossRef>)

}