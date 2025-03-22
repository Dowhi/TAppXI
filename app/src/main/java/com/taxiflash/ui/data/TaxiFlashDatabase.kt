package com.taxiflash.ui.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Carrera::class, Turno::class, Gasto::class, Usuario::class, OtrosIngresos::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TaxiFlashDatabase : RoomDatabase() {
    abstract fun carreraDao(): CarreraDao
    abstract fun turnoDao(): TurnoDao
    abstract fun gastoDao(): GastoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun otrosIngresosDao(): OtrosIngresosDao

    companion object {
        @Volatile
        private var Instance: TaxiFlashDatabase? = null

        fun getDatabase(context: Context): TaxiFlashDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    TaxiFlashDatabase::class.java,
                    "taxiflash_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { Instance = it }
            }
        }
    }
} 