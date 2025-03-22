package com.taxiflash.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.taxiflash.ui.data.Usuario
import com.taxiflash.ui.data.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * ViewModel para manejar la lógica de usuarios.
 */
class UsuarioViewModel(private val repository: UsuarioRepository) : ViewModel() {
    
    // Estado del PIN ingresado
    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin.asStateFlow()
    
    // Estado del nombre ingresado
    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()
    
    // Estado del DNI ingresado
    private val _dni = MutableStateFlow("")
    val dni: StateFlow<String> = _dni.asStateFlow()
    
    // Estado de la fecha de nacimiento ingresada
    private val _fechaNacimiento = MutableStateFlow<LocalDate?>(null)
    val fechaNacimiento: StateFlow<LocalDate?> = _fechaNacimiento.asStateFlow()
    
    // Estado para mostrar fecha de nacimiento en formato de texto
    private val _fechaNacimientoTexto = MutableStateFlow("")
    val fechaNacimientoTexto: StateFlow<String> = _fechaNacimientoTexto.asStateFlow()
    
    // Estado para mensaje de error
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()
    
    // Estado para usuario actual
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()
    
    // Estado para lista de usuarios
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.getTodosUsuarios().collect { listaUsuarios ->
                _usuarios.value = listaUsuarios
            }
            
            // Cargar usuario activo al iniciar
            _usuarioActual.value = repository.getUsuarioActivo()
        }
    }
    
    fun updatePin(nuevoPin: String) {
        if (nuevoPin.length <= 4) {
            _pin.value = nuevoPin
        }
    }
    
    fun updateNombre(nuevoNombre: String) {
        _nombre.value = nuevoNombre
    }
    
    fun updateDni(nuevoDni: String) {
        _dni.value = nuevoDni
    }
    
    fun updateFechaNacimiento(fecha: LocalDate) {
        _fechaNacimiento.value = fecha
        _fechaNacimientoTexto.value = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
    
    fun resetError() {
        _mensajeError.value = null
    }
    
    fun resetPIN() {
        _pin.value = ""
    }
    
    fun validarPin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val usuario = repository.validarPin(_pin.value)
            if (usuario != null) {
                repository.establecerUsuarioActivo(usuario.id)
                _usuarioActual.value = usuario
                onSuccess()
            } else {
                _mensajeError.value = "PIN incorrecto"
                resetPIN()
            }
        }
    }
    
    fun registrarUsuario(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Validaciones
            if (_nombre.value.isBlank()) {
                _mensajeError.value = "El nombre no puede estar vacío"
                return@launch
            }
            
            if (_pin.value.length != 4) {
                _mensajeError.value = "El PIN debe tener 4 dígitos"
                return@launch
            }
            
            if (_dni.value.isBlank()) {
                _mensajeError.value = "El DNI no puede estar vacío"
                return@launch
            }
            
            if (_fechaNacimiento.value == null) {
                _mensajeError.value = "Debe seleccionar una fecha de nacimiento"
                return@launch
            }
            
            // Verificar si el DNI ya existe
            if (repository.verificarDniExistente(_dni.value)) {
                _mensajeError.value = "Este DNI ya está registrado"
                return@launch
            }
            
            // Registrar usuario
            val userId = repository.registrarUsuario(
                _nombre.value,
                _pin.value,
                _dni.value,
                _fechaNacimiento.value!!
            )
            
            // Establecer como usuario activo
            repository.establecerUsuarioActivo(userId)
            _usuarioActual.value = repository.getUsuarioActivo()
            
            // Limpiar campos
            resetCampos()
            
            onSuccess()
        }
    }
    
    fun recuperarPin(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            if (_dni.value.isBlank()) {
                _mensajeError.value = "El DNI no puede estar vacío"
                return@launch
            }
            
            if (_fechaNacimiento.value == null) {
                _mensajeError.value = "Debe seleccionar una fecha de nacimiento"
                return@launch
            }
            
            val usuario = repository.recuperarUsuario(_dni.value, _fechaNacimiento.value!!)
            if (usuario != null) {
                onSuccess(usuario.pin)
            } else {
                _mensajeError.value = "No se encontró ningún usuario con esos datos"
            }
        }
    }
    
    private fun resetCampos() {
        _nombre.value = ""
        _pin.value = ""
        _dni.value = ""
        _fechaNacimiento.value = null
        _fechaNacimientoTexto.value = ""
    }
    
    fun cambiarUsuario() {
        viewModelScope.launch {
            repository.desactivarTodosUsuarios()
            _usuarioActual.value = null
            resetPIN()
        }
    }
    
    fun limpiarCamposRegistro() {
        resetCampos()
    }
    
    // Factory para crear el ViewModel con las dependencias necesarias
    class Factory(private val repository: UsuarioRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UsuarioViewModel::class.java)) {
                return UsuarioViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 