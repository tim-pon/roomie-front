package com.example.roomie.di

import android.app.Application
import androidx.room.Room
import com.example.roomie.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/**
 * This object contains methods to inject DAOSs,
 * which handles local caching/storage of data.
 */
@Module
@InstallIn(SingletonComponent::class)
object DaoModule {

    /**
     * Provides singleton of AppDatabase,
     * which is the base class for all Room databases.
     *
     * @param app Base class for maintaining global application state
     */
    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase =
        Room.databaseBuilder(app, AppDatabase::class.java, "app_database")
            .build()


    /**
     * Provides singleton of the ArticleDao,
     * which handles articles caching.
     *
     * @param db Base class for all Room databases
     */
    @Provides
    @Singleton
    fun provideArticleDao(db: AppDatabase): ArticleDao =
        db.articleDao()


    /**
     * Provides singleton of the ShoppingListDao,
     * which handles shopping list caching.
     *
     * @param db Base class for all Room databases
     */
    @Provides
    @Singleton
    fun provideShoppingListDao(db: AppDatabase): ShoppingListDao =
        db.shoppingListDao()


    /**
     * Provides singleton of the TransactionDao,
     * which handles transformation caching.
     *
     * @param db Base class for all Room databases
     */
    @Provides
    @Singleton
    fun provideTransactionDao(db: AppDatabase): TransactionDao =
        db.transactionDao()


    /**
     * Provides singleton of the FlatDao,
     * which handles flat caching.
     *
     * @param db Base class for all Room databases
     */
    @Provides
    @Singleton
    fun provideFlatDao(db: AppDatabase): FlatDao =
        db.flatDao()


    /**
     * Provides singleton of the UserDao,
     * which handles user caching.
     *
     * @param db Base class for all Room databases
     */
    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao =
        db.userDao()


    /**
     * Provides singleton of the ActivityDao,
     * which handles activity caching.
     *
     * @param db Base class for all Room databases
     */
    @Provides
    @Singleton
    fun provideActivityDao(db: AppDatabase): ActivityDao =
        db.activityDao()
}