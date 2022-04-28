package com.example.roomie.data.local

import androidx.room.*
import com.example.roomie.domain.model.Activity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Query("SELECT * FROM activities WHERE id = :activityId")
    fun getActivity(activityId: Int): Flow<Activity>

    @Query("SELECT * FROM activities WHERE flatId = :flatId")
    fun getActivities(flatId: Int): Flow<List<Activity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<Activity>)

    @Delete
    suspend fun deleteActivity(activity: Activity)

    @Query("DELETE FROM activities WHERE flatId = :flatId")
    suspend fun deleteActivities(flatId: Int)

}