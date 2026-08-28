package com.educalab.redeconomica.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.redeconomica.AppContainer
import com.educalab.redeconomica.data.repository.ProfileRepository
import com.educalab.redeconomica.domain.model.Badge
import com.educalab.redeconomica.domain.model.CollectionItem
import com.educalab.redeconomica.domain.model.GlossaryEntry
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.ProgressSummary
import com.educalab.redeconomica.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Perfil, colección, insignias y diccionario: todo lo "mío" del Valle. */
data class PerfilUiState(
    val perfil: UserProfile = ProfileRepository.PERFIL_POR_DEFECTO,
    val resumen: ProgressSummary? = null,
    val insignias: List<Badge> = emptyList(),
    val insigniasGanadas: Set<String> = emptySet(),
    val avanceInsignias: Map<String, Int> = emptyMap(),
    val objetos: List<CollectionItem> = emptyList(),
    val objetosDesbloqueados: Set<String> = emptySet(),
    val diccionario: List<GlossaryEntry> = emptyList(),
    val conceptosDescubiertos: Set<String> = emptySet(),
    val almacen: Inventory = Inventory.VACIO
)

class ProfileViewModel(private val contenedor: AppContainer) : ViewModel() {

    private val _estado = MutableStateFlow(PerfilUiState())
    val estado: StateFlow<PerfilUiState> = _estado

    init {
        viewModelScope.launch {
            combine(
                contenedor.perfil.perfilFlow,
                contenedor.progreso.resumenFlow,
                contenedor.catalogo.insigniasFlow(),
                contenedor.progreso.insigniasGanadasFlow(),
                contenedor.progreso.contadoresFlow
            ) { perfil, resumen, insignias, ganadas, contadores ->
                val motor = com.educalab.redeconomica.domain.engine.ProgressEngine()
                PerfilUiState(
                    perfil = perfil,
                    resumen = resumen,
                    insignias = insignias,
                    insigniasGanadas = ganadas,
                    avanceInsignias = insignias.associate { it.id to motor.avanceDe(it, contadores) }
                )
            }.collect { parcial ->
                _estado.update {
                    parcial.copy(
                        objetos = it.objetos,
                        objetosDesbloqueados = it.objetosDesbloqueados,
                        diccionario = it.diccionario,
                        conceptosDescubiertos = it.conceptosDescubiertos,
                        almacen = it.almacen
                    )
                }
            }
        }
        viewModelScope.launch {
            combine(
                contenedor.catalogo.objetosFlow(),
                contenedor.progreso.objetosDesbloqueadosFlow(),
                contenedor.catalogo.diccionarioFlow(),
                contenedor.progreso.conceptosDescubiertosFlow(),
                contenedor.perfil.almacenFlow
            ) { objetos, desbloqueados, diccionario, conceptos, almacen ->
                _estado.value.copy(
                    objetos = objetos,
                    objetosDesbloqueados = desbloqueados,
                    diccionario = diccionario,
                    conceptosDescubiertos = conceptos,
                    almacen = almacen
                )
            }.collect { nuevo -> _estado.value = nuevo }
        }
    }

    fun guardarIdentidad(alias: String, avatarId: String) {
        viewModelScope.launch { contenedor.perfil.guardarIdentidad(alias, avatarId) }
    }

    fun guardarAjustes(sonido: Boolean, vibracion: Boolean, textoGrande: Boolean) {
        viewModelScope.launch { contenedor.perfil.guardarAjustes(sonido, vibracion, textoGrande) }
    }

    fun terminarOnboarding() {
        viewModelScope.launch { contenedor.perfil.terminarOnboarding() }
    }

    fun repetirOnboarding() {
        viewModelScope.launch { contenedor.perfil.repetirOnboarding() }
    }

    fun reiniciarProgreso() {
        viewModelScope.launch { contenedor.sembrador.reiniciarProgreso() }
    }
}
