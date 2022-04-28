package com.example.roomie.di

import com.example.roomie.data.local.*
import com.example.roomie.data.remote.*
import com.example.roomie.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/**
 * This object contains methods to inject repositories,
 * which represent the single source of truth.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {


    /**
     * Provides a singelton of the UserAuthenticationRepository,
     * which is the single source of truth for authentication data.
     *
     * @param userDao reads and writes user data to local cache
     * @param api performs api request for authentication
     */
    @Provides
    @Singleton
    fun provideUserAuthenticationRepository(
        api: UserAuthenticationApi,
        flatDao: FlatDao
    ): AuthenticationRepository =
        AuthenticationRepository(api, flatDao)


    /**
     * Provides a singelton of the FlatRepository,
     * which is the single source of truth for flat data.
     *
     * @param db Base class for all Room databases
     * @param flatDao reads and writes flat data to local cache
     * @param flatApi performs api request for flats
     * @param userDao reads and writes user data to local cache
     */
    @Provides
    @Singleton
    fun provideFlatRepository(
        db: AppDatabase,
        flatDao: FlatDao,
        flatApi: FlatApi,
        userDao: UserDao,
    ): FlatRepository =
        FlatRepository(db, flatDao, flatApi, userDao)


    /**
     * Provides a singelton of the ArticleRepository,
     * which is the single source of truth for article data.
     *
     * @param db Base class for all Room databases
     * @param dao reads and writes articles data to local cache
     * @param api performs api request for articles
     */
    @Provides
    @Singleton
    fun provideArticleRepository(
        db: AppDatabase,
        dao: ArticleDao,
        api: ArticleApi,
    ): ArticleRepository =
        ArticleRepository(db, dao, api)


    /**
     * Provides a singelton of the SettingsRepository,
     * which is the single source of truth for settings data.
     *
     * @param userApi performs api request for user data
     * @param flatApi performs api request for flats
     */
    @Provides
    @Singleton
    fun provideSettingsRepository(
        userApi: UserApi,
        flatApi: FlatApi,
    ): SettingsRepository =
        SettingsRepository(userApi, flatApi)


    /**
     * Provides a singelton of the ShoppingListRepository,
     * which is the single source of truth for shopping data.
     *
     * @param db Base class for all Room databases
     * @param dao reads and writes shoppinglist data to local cache
     * @param api performs api request for shoppinglists
     */
    @Provides
    @Singleton
    fun provideShoppingListRepository(
        db: AppDatabase,
        dao: ShoppingListDao,
        api: ShoppingListApi,
    ): ShoppingListRepository =
        ShoppingListRepository(db, dao, api)


    /**
     * Provides a singelton of the TransactionRepository,
     * which is the single source of truth for transaction data.
     *
     * @param db Base class for all Room databases
     * @param dao reads and writes transaction data to local cache
     * @param api performs api request for transactions
     */
    @Provides
    @Singleton
    fun provideTransactionRepository(
        db: AppDatabase,
        dao: TransactionDao,
        api: TransactionApi,
        userDao: UserDao,
    ): TransactionRepository =
        TransactionRepository(db, dao, api, userDao)


    /**
     * Provides a singelton of the ActivityRepository,
     * which is the single source of truth for activity data.
     *
     * @param db Base class for all Room databases
     * @param dao reads and writes transaction data to local cache
     * @param api performs api request for transactions
     */
    @Provides
    @Singleton
    fun provideActivityRepository(
        db: AppDatabase,
        dao: ActivityDao,
        api: ActivityApi,
    ): ActivityRepository =
        ActivityRepository(db, dao, api)

    @Provides
    @Singleton
    fun provideGraphRepository(
        api: GraphApi
    ): GraphRepository =
        GraphRepository(api)
}