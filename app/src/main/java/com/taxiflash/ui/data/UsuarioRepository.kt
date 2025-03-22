package com.taxiflash.ui.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repositorio para manejar operaciones relacionadas con los usuarios.
 */
class UsuarioRepository(private val usuarioDao: UsuarioDao) {
    
    // Registro de usuario
    suspend fun registrarUsuario(nombre: String, pin: String, dni: String, fechaNacimiento: LocalDate): Long {
        val usuario = Usuario(
            nombre = nombre,
            pin = pin,
            dni = dni,
            fechaNacimiento = fechaNacimiento,
            isActivo = false
        )
        return usuarioDao.insertarUsuario(usuario)
    }
    
    // Validación de PIN
    suspend fun validarPin(pin: String): Usuario? {
        return usuarioDao.validarUsuarioPorPin(pin)
    }
    
    // Recuperación de PIN por DNI y fecha de nacimiento
    suspend fun recuperarUsuario(dni: String, fechaNacimiento: LocalDate): Usuario? {
        return usuarioDao.validarUsuarioPorDniYFecha(dni, fechaNacimiento)
    }
    
    // Establecer usuario activo
    suspend fun establecerUsuarioActivo(userId: Long) {
        usuarioDao.desactivarTodosUsuarios()
        usuarioDao.actualizarEstadoActivo(userId, true)
    }
    
    // Obtener usuario activo
    suspend fun getUsuarioActivo(): Usuario? {
        return usuarioDao.getUsuarioActivo()
    }
    
    // Obtener todos los usuarios
    fun getTodosUsuarios(): Flow<List<Usuario>> {
        return usuarioDao.getTodosUsuarios()
    }
    
    // Verificar si un DNI ya existe
    suspend fun verificarDniExistente(dni: String): Boolean {
        return usuarioDao.getUsuarioPorDni(dni) != null
    }
    
    // Desactivar todos los usuarios
    suspend fun desactivarTodosUsuarios() {
        usuarioDao.desactivarTodosUsuarios()
    }
} 