package com.taxiflash.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.Carrera
import com.taxiflash.ui.data.TaxiFlashDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditarDiaViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TaxiFlashDatabase.getDatabase(application)
    private val carreraDao = database.carreraDao()

    private val _carreras = MutableStateFlow<List<Carrera>>(emptyList())
    val carreras: StateFlow<List<Carrera>> = _carreras

    fun cargarCarrerasDia(fecha: String) {
        viewModelScope.launch {
            try {
                // Convertir la fecha dd/MM/yyyy a yyyyMMdd
                val fechaDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fecha)
                fechaDate?.let {
                    val fechaStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(it)
                    val carrerasDia = carreraDao.getCarrerasByFechaExacta(fechaStr)
                    _carreras.value = carrerasDia
                }
            } catch (e: Exception) {
                // Manejar el error
                e.printStackTrace()
            }
        }
    }
} 