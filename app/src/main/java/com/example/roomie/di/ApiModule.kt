package com.example.roomie.di

import com.example.roomie.data.remote.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * This object contains methods to inject api interfaces for REST calls.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {


    /**
     * Provides a singelton of ShoppingListApi interface.
     * @param retrofit turns HTTP API into a Java interface.
     */
    @Provides
    @Singleton
    fun provideShoppingListApi(retrofit: Retrofit): ShoppingListApi =
        retrofit.create(ShoppingListApi::class.java)


    /**
     * Provides a singelton of UserApi interface.
     * @param retrofit turns HTTP API into a Java interface.
     */
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)


    /**
     * Provides a singelton of FlatApi interface.
     * @param retrofit turns HTTP API into a Java interface.
     */
    @Provides
    @Singleton
    fun provideFlatApi(retrofit: Retrofit): FlatApi =
        retrofit.create(FlatApi::class.java)


    /**
     * Provides a singelton of ArticleApi interface.
     * @param retrofit turns HTTP API into a Java interface.
     */
    @Provides
    @Singleton
    fun provideArticleApi(retrofit: Retrofit): ArticleApi =
        retrofit.create(ArticleApi::class.java)


    /**
     * Provides a singelton of UserAuthenticationApi interface.
     * @param retrofit turns HTTP API into a Java interface.
     */
    @Provides
    @Singleton
    fun provideUserAuthenticationApi(retrofit: Retrofit): UserAuthenticationApi =
        retrofit.create(UserAuthenticationApi::class.java)


    /**
     * Provides a singelton of TransactionApi interface.
     * @param retrofit turns HTTP API into a Java interface.
     */
    @Provides
    @Singleton
    fun provideTransactionApi(retrofit: Retrofit): TransactionApi =
        retrofit.create(TransactionApi::class.java)

    /**
     * Provides a singelton of TransactionApi interface.
     * @param retrofit turns HTTP API into a Java interface.
     */
    @Provides
    @Singleton
    fun provideActivityApi(retrofit: Retrofit): ActivityApi =
        retrofit.create(ActivityApi::class.java)

    @Provides
    @Singleton
    fun provideGraphApi(retrofit: Retrofit): GraphApi =
        retrofit.create(GraphApi::class.java)

}