package com.taxiflash.ui.di

import android.content.Context
import com.taxiflash.ui.data.TaxiFlashDatabase
import com.taxiflash.ui.data.CarreraDao
import com.taxiflash.ui.data.TurnoDao
import com.taxiflash.ui.data.GastoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TaxiFlashDatabase {
        return TaxiFlashDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideCarreraDao(database: TaxiFlashDatabase): CarreraDao {
        return database.carreraDao()
    }

    @Provides
    @Singleton
    fun provideTurnoDao(database: TaxiFlashDatabase): TurnoDao {
        return database.turnoDao()
    }

    @Provides
    @Singleton
    fun provideGastoDao(database: TaxiFlashDatabase): GastoDao {
        return database.gastoDao()
    }
} 