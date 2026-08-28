package com.educalab.redeconomica.ui.screens.valley

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.redeconomica.AppContainer
import com.educalab.redeconomica.data.repository.MissionStatus
import com.educalab.redeconomica.data.repository.ProfileRepository
import com.educalab.redeconomica.domain.engine.DailyChallenge
import com.educalab.redeconomica.domain.model.ModuleState
import com.educalab.redeconomica.domain.model.ProgressSummary
import com.educalab.redeconomica.domain.model.UserProfile
import com.educalab.redeconomica.domain.model.ValleyPlace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lo que ve el niño al entrar en el Valle. */
data class ValleUiState(
    val cargando: Boolean = true,
    val perfil: UserProfile = ProfileRepository.PERFIL_POR_DEFECTO,
    val misiones: List<MissionStatus> = emptyList(),
    val resumen: ProgressSummary? = null,
    val zonasAbiertas: Set<ValleyPlace> = setOf(ValleyPlace.GRANJA, ValleyPlace.PLAZA),
    val reto: DailyChallenge? = null,
    val retoCompletado: Boolean = false,
    val escenariosParaRepasar: List<String> = emptyList()
) {
    val siguienteMision: MissionStatus?
        get() = misiones.firstOrNull {
            it.estado == ModuleState.DISPONIBLE || it.estado == ModuleState.INICIADO
        }
}

/**
 * Estado del mapa del Valle.
 *
 * Combina progreso, perfil y reto del día. Todo viene de Room; esta clase no
 * calcula reglas económicas, solo las presenta.
 */
class ValleyViewModel(private val contenedor: AppContainer) : ViewModel() {

    private val _estado = MutableStateFlow(ValleUiState())
    val estado: StateFlow<ValleUiState> = _estado

    init {
        viewModelScope.launch {
            combine(
                contenedor.progreso.estadosMisionesFlow(),
                contenedor.progreso.resumenFlow,
                contenedor.perfil.perfilFlow,
                contenedor.progreso.escenariosParaRepasarFlow()
            ) { misiones, resumen, perfil, repasar ->
                ValleUiState(
                    cargando = false,
                    perfil = perfil,
                    misiones = misiones,
                    resumen = resumen,
                    zonasAbiertas = zonasAbiertas(misiones),
                    escenariosParaRepasar = repasar
                )
            }.collect { nuevo ->
                _estado.update { anterior ->
                    nuevo.copy(reto = anterior.reto, retoCompletado = anterior.retoCompletado)
                }
            }
        }
        cargarRetoDelDia()
    }

    fun cargarRetoDelDia() {
        viewModelScope.launch {
            val reto = contenedor.laboratorio.retoDeHoy()
            val hecho = contenedor.laboratorio.estaCompletadoElRetoDeHoy()
            _estado.update { it.copy(reto = reto, retoCompletado = hecho) }
        }
    }

    private fun zonasAbiertas(misiones: List<MissionStatus>): Set<ValleyPlace> {
        val abiertas = mutableSetOf(ValleyPlace.GRANJA, ValleyPlace.PLAZA)
        misiones.forEach { estado ->
            if (estado.estado != ModuleState.BLOQUEADO) abiertas += estado.mision.lugar
            if (estado.estado == ModuleState.COMPLETADO || estado.estado == ModuleState.DOMINADO) {
                estado.mision.recompensa.zonaDesbloqueada?.let { abiertas += it }
            }
        }
        abiertas += ValleyPlace.ALMACEN
        return abiertas
    }
}
