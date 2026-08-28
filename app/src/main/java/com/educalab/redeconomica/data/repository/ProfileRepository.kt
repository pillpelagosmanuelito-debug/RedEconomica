package com.educalab.redeconomica.data.repository

import com.educalab.redeconomica.data.local.Mappers
import com.educalab.redeconomica.data.local.dao.ProfileDao
import com.educalab.redeconomica.data.local.entity.ProfileEntity
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Perfil del niño y ajustes.
 *
 * Solo se guarda un alias que él elige y un avatar dibujado en la propia app.
 * Ni nombre real, ni edad, ni correo, ni nada que identifique a nadie.
 */
class ProfileRepository(private val dao: ProfileDao) {

    val perfilFlow: Flow<UserProfile> = dao.perfilFlow().map { fila ->
        fila?.let { Mappers.aDominio(it) } ?: PERFIL_POR_DEFECTO
    }

    val almacenFlow: Flow<Inventory> = dao.almacenFlow().map { filas ->
        Inventory.of(filas.associate { it.recursoId to it.cantidad })
    }

    suspend fun perfil(): UserProfile =
        dao.perfil()?.let { Mappers.aDominio(it) } ?: PERFIL_POR_DEFECTO

    suspend fun asegurarPerfil() {
        if (dao.perfil() == null) {
            dao.guardar(
                ProfileEntity(
                    id = 1,
                    alias = PERFIL_POR_DEFECTO.alias,
                    avatarId = PERFIL_POR_DEFECTO.avatarId,
                    onboardingHecho = false,
                    sonidoActivo = true,
                    vibracionActiva = true,
                    textoGrande = false,
                    creadoMillis = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun guardarIdentidad(alias: String, avatarId: String) {
        asegurarPerfil()
        val limpio = alias.trim().take(MAX_ALIAS).ifBlank { PERFIL_POR_DEFECTO.alias }
        dao.actualizarIdentidad(limpio, avatarId)
    }

    suspend fun terminarOnboarding() {
        asegurarPerfil()
        dao.marcarOnboarding(true)
    }

    suspend fun repetirOnboarding() {
        asegurarPerfil()
        dao.marcarOnboarding(false)
    }

    suspend fun guardarAjustes(sonido: Boolean, vibracion: Boolean, textoGrande: Boolean) {
        asegurarPerfil()
        dao.actualizarAjustes(sonido, vibracion, textoGrande)
    }

    companion object {
        const val MAX_ALIAS = 16
        val PERFIL_POR_DEFECTO = UserProfile(
            alias = "Vecino",
            avatarId = "avatar_1",
            onboardingHecho = false,
            sonidoActivo = true,
            vibracionActiva = true,
            textoGrande = false
        )
    }
}
