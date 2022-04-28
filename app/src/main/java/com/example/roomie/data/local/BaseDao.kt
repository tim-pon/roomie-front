package com.example.roomie.data.local

import androidx.room.*


@Dao
interface BaseDao<T> {
    /**
     * Insert an object in the database.
     *
     * @param obj the object to be inserted.
     * @return The SQLite row id
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(obj: T): Long

    /**
     * Insert an array of objects in the database.
     *
     * @param obj the objects to be inserted.
     * @return The SQLite row ids
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(obj: List<T>): List<Long>

    /**
     * Update an object from the database.
     *
     * @param obj the object to be updated
     */
    @Update
    suspend fun update(obj: T)

    /**
     * Update an array of objects from the database.
     *
     * @param obj the object to be updated
     */
    @Update
    suspend fun update(obj: List<T>)

    /**
     * Delete an object from the database
     *
     * @param obj the object to be deleted
     */
    @Delete
    suspend fun delete(obj: T)

    /**
     * Insert or update an object to the database.
     * Implementation of the SQL UPSERT logic.
     *
     * @param obj the object to be inserted or updated
     */
    @Transaction
    suspend fun upsert(obj: @JvmSuppressWildcards T) {
        if (insert(obj) == -1L) update(obj)
    }

    /**
     * Insert or update an array of objects to the database.
     * Implementation of the SQL UPSERT logic.
     *
     * @param objList the array of objects to be inserted or updated
     */
    @Transaction
    suspend fun upsert(objList: List<@JvmSuppressWildcards T>) {
        val insertResult = insert(objList)
        val toUpdate = mutableListOf<T>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L)
                toUpdate.add((objList[i]))
        }

        if (toUpdate.isNotEmpty())
            update(toUpdate)
    }
}