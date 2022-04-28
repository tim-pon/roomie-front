package com.example.roomie.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


/**
 * This object is the main entry point for dagger hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {}