package com.taxiflash.ui.di

import com.taxiflash.ui.data.CarreraDao
import com.taxiflash.ui.data.CarreraRepository
import com.taxiflash.ui.data.GastoDao
import com.taxiflash.ui.data.GastoRepository
import com.taxiflash.ui.data.TurnoDao
import com.taxiflash.ui.data.TurnoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de inyección de dependencias para los repositorios
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    /**
     * Proporciona el repositorio de carreras
     */
    @Provides
    @Singleton
    fun provideCarreraRepository(carreraDao: CarreraDao): CarreraRepository {
        return CarreraRepository(carreraDao)
    }
    
    /**
     * Proporciona el repositorio de gastos
     */
    @Provides
    @Singleton
    fun provideGastoRepository(gastoDao: GastoDao): GastoRepository {
        return GastoRepository(gastoDao)
    }
    
    /**
     * Proporciona el repositorio de turnos
     */
    @Provides
    @Singleton
    fun provideTurnoRepository(turnoDao: TurnoDao, carreraDao: CarreraDao): TurnoRepository {
        return TurnoRepository(turnoDao, carreraDao)
    }
} 