package com.example.roomie.domain.model

import androidx.room.*
import com.example.roomie.core.Constants.TMP_ID
import com.squareup.moshi.Json

@Entity(
    tableName = "articles",
    primaryKeys = ["id"],
    foreignKeys = [ForeignKey(
        entity = ShoppingList::class,
        parentColumns = ["id"],
        childColumns = ["shoppingListId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("shoppingListId")]
)
data class Article(
    var id: Int,
    @Json(name = "listId")
    val shoppingListId: Int,
    var name: String,
    var category: String?,
    var amount: Double?,
    var unit: String?,
    var bought: Boolean,
    val fetchedAt: Long = System.currentTimeMillis()
) {

//    // On insert Sqlite sets current timestamp by default
//    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
//    // Tells moshi to ignore this field
//    @Transient
//    lateinit var fetchedAt: String



    companion object {

        /**
         * Creates an empty default article with an temporary id of -1.
         * Called by ArticleViewModel if no article id got passed
         *
         * @param shoppingListId sets the shoppinglist id
         */
        fun init(shoppingListId: Int): Article =
            Article(TMP_ID, shoppingListId, "", "", 0.0, "",false)
    }
}