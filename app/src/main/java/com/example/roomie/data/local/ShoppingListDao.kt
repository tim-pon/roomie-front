package com.example.roomie.data.local

import androidx.room.*
import com.example.roomie.domain.model.ShoppingList
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ShoppingListDao : BaseDao<ShoppingList> {

    @Query("SELECT * FROM shoppingLists WHERE flatId = :flatId")
    abstract fun getShoppingLists(flatId: Int): Flow<List<ShoppingList>>

    @Query("SELECT * FROM shoppingLists WHERE id = :shoppingListId")
    abstract fun getShoppingList(shoppingListId: Int): Flow<ShoppingList>

    @Query("DELETE FROM shoppingLists WHERE id = :shoppingListId")
    abstract suspend fun deleteShoppingListById(shoppingListId: Int)

    @Query("DELETE FROM shoppingLists WHERE flatId = :flatId AND id NOT IN (:shoppingListIds)")
    abstract suspend fun clearInvalidCache(flatId: Int, shoppingListIds: List<Int>)

}