package com.taxiflash.di

import com.taxiflash.data.repositories.TurnoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTurnoRepository(): TurnoRepository {
        return TurnoRepository()
    }
} 