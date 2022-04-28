package com.example.roomie.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "shoppingLists",
    primaryKeys = ["id"]
)
data class ShoppingList(
    var id: Int,
    val flatId: Int,
    var name: String,
//    val description: String,
    val totalItems: Int,
    val items: Int,
    val fetchedAt: Long = System.currentTimeMillis()
) {

//    // On insert Sqlite sets current timestamp by default
//    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
//    lateinit var fetchedAt: String

    fun openArticles() = totalItems - items


    companion object {

        /**
         * Creates an empty default shoppinglist with an temporary id of -1.
         * Called by ShoppingListViewModel if no shoppinglist id got passed
         *
         * @param flatId sets the flatId of the shoppinglist
         * @param name sets the name of the shoppinglist
         */
        fun init(flatId: Int, name: String): ShoppingList =
            ShoppingList(-1,flatId,name,0,0)
    }
}
