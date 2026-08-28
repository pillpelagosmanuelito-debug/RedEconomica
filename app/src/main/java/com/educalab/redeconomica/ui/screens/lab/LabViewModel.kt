package com.educalab.redeconomica.ui.screens.lab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.redeconomica.AppContainer
import com.educalab.redeconomica.data.seed.SeedCharacters
import com.educalab.redeconomica.domain.engine.LabComparison
import com.educalab.redeconomica.domain.engine.LabConfig
import com.educalab.redeconomica.domain.engine.LabMode
import com.educalab.redeconomica.domain.engine.LabRun
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado del Laboratorio del Valle. */
data class LabUiState(
    val config: LabConfig = LabConfig(),
    val ultimo: LabRun? = null,
    val guardadoA: LabRun? = null,
    val guardadoB: LabRun? = null,
    val comparacion: LabComparison? = null,
    val experimentosGuardados: Int = 0
)

/**
 * Laboratorio del Valle.
 *
 * Aquí no hay puntuación ni vidas: el niño cambia las variables, ejecuta y
 * mira lo que pasa. Puede guardar dos experimentos y compararlos con números
 * calculados de verdad por `LabEngine`.
 */
class LabViewModel(private val contenedor: AppContainer) : ViewModel() {

    private val _estado = MutableStateFlow(LabUiState())
    val estado: StateFlow<LabUiState> = _estado

    private val base = SeedCharacters.HABITANTES

    init {
        viewModelScope.launch {
            contenedor.laboratorio.numeroExperimentosFlow().collect { n ->
                _estado.update { it.copy(experimentosGuardados = n) }
            }
        }
    }

    fun cambiarHabitantes(n: Int) = actualizarConfig { it.copy(habitantes = n.coerceIn(2, 6)) }
    fun cambiarTurnos(n: Int) = actualizarConfig { it.copy(turnos = n.coerceIn(1, 6)) }
    fun cambiarModo(modo: LabMode) = actualizarConfig { it.copy(modo = modo) }
    fun alternarIntercambio() = actualizarConfig { it.copy(permiteIntercambio = !it.permiteIntercambio) }
    fun alternarPonerEnComun() = actualizarConfig { it.copy(ponenEnComun = !it.ponenEnComun) }

    private fun actualizarConfig(bloque: (LabConfig) -> LabConfig) =
        _estado.update { it.copy(config = bloque(it.config), ultimo = null) }

    fun ejecutar() {
        val config = _estado.value.config
        val resultado = contenedor.motorLaboratorio.ejecutar(config, base)
        _estado.update { it.copy(ultimo = resultado) }
        viewModelScope.launch {
            contenedor.laboratorio.guardarExperimento("experimento", resultado)
            contenedor.progreso.revisarInsignias()
        }
    }

    fun guardarComoA() = _estado.update { it.copy(guardadoA = it.ultimo, comparacion = null) }

    fun guardarComoB() {
        _estado.update { estado ->
            val b = estado.ultimo
            val a = estado.guardadoA
            val comparacion = if (a != null && b != null) {
                contenedor.motorLaboratorio.comparar(a, b)
            } else null
            estado.copy(guardadoB = b, comparacion = comparacion)
        }
    }

    fun limpiarComparacion() = _estado.update {
        it.copy(guardadoA = null, guardadoB = null, comparacion = null)
    }
}
