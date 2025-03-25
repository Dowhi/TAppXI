package com.taxiflash.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taxiflash.data.repositories.TurnoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val turnoRepository: TurnoRepository
) : ViewModel() {
    
    private val _mensajeError = MutableLiveData<String>("")
    val mensajeError: LiveData<String> = _mensajeError
    
    fun limpiarError() {
        _mensajeError.value = ""
    }
    
    // Aquí irían otros métodos del ViewModel
} 