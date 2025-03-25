package com.taxiflash.ui.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Base de datos principal de la aplicación
 */
@Database(
    entities = [Carrera::class, Turno::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun carreraDao(): CarreraDao
    abstract fun turnoDao(): TurnoDao
} 