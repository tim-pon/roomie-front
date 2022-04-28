package com.example.roomie.data.local

import androidx.room.*
import com.example.roomie.domain.model.Flat
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FlatDao : BaseDao<Flat> {

    @Query("SELECT * FROM flats WHERE id = :flatId")
    abstract fun getFlatById(flatId: Int): Flow<Flat>

}