package com.example.roomie.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.roomie.domain.model.*

@Database(
    entities = [
        Article::class,
        ShoppingList::class,
        Transaction::class,
        TransactionUserCrossRef::class,
        User::class,
        FlatUserCrossRef::class,
        Flat::class,
        Activity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun articleDao(): ArticleDao

    abstract fun shoppingListDao(): ShoppingListDao

    abstract fun transactionDao(): TransactionDao

    abstract fun flatDao(): FlatDao

    abstract fun userDao(): UserDao

    abstract fun activityDao(): ActivityDao
}