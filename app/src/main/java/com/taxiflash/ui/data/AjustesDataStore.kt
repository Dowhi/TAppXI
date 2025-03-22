package com.taxiflash.ui.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * Clase de utilidad para manejar las preferencias de la aplicación con DataStore
 */
object AjustesDataStore {
    // DataStore para ajustes
    val Context.dataStore by preferencesDataStore(name = "ajustes")
    
    // Claves para las preferencias
    val THEME_KEY = booleanPreferencesKey("tema_oscuro")
    val NOTIFICATIONS_KEY = booleanPreferencesKey("notificaciones")
    val CURRENCY_FORMAT_KEY = stringPreferencesKey("moneda")
    val DATE_FORMAT_KEY = stringPreferencesKey("formato_fecha")
    val OBJETIVO_IMPORTE_KEY = doublePreferencesKey("objetivo_importe")
} 