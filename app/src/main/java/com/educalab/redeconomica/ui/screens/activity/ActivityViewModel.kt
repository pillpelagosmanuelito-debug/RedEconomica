package com.educalab.redeconomica.ui.screens.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.educalab.redeconomica.AppContainer
import com.educalab.redeconomica.data.repository.ProgressUpdate
import com.educalab.redeconomica.domain.engine.ScenarioAnswer
import com.educalab.redeconomica.domain.engine.TradeSession
import com.educalab.redeconomica.domain.model.Allocation
import com.educalab.redeconomica.domain.model.AttemptResult
import com.educalab.redeconomica.domain.model.BudgetResult
import com.educalab.redeconomica.domain.model.ChainStep
import com.educalab.redeconomica.domain.model.CooperationOutcome
import com.educalab.redeconomica.domain.model.CooperationPlan
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.PlanComparison
import com.educalab.redeconomica.domain.model.ProductionOutcome
import com.educalab.redeconomica.domain.model.Scenario
import com.educalab.redeconomica.domain.model.ScenarioPayload
import com.educalab.redeconomica.domain.model.ScarcityResult
import com.educalab.redeconomica.domain.model.SpecializationPlan
import com.educalab.redeconomica.domain.model.TradeEvaluation
import com.educalab.redeconomica.domain.model.TradeOffer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la actividad que el niño tiene delante. */
data class ActividadUiState(
    val escenario: Scenario? = null,
    val intentos: Int = 0,
    val resultado: AttemptResult? = null,
    val mostrarResultado: Boolean = false,
    val actualizacion: ProgressUpdate? = null,
    val pista: String? = null,

    // Mesa de intercambios
    val sesion: TradeSession? = null,
    val socioSeleccionado: String? = null,
    val ofrezco: Map<String, Int> = emptyMap(),
    val pido: Map<String, Int> = emptyMap(),
    val ultimaEvaluacion: TradeEvaluation? = null,

    // Producción y especialización
    val plan: SpecializationPlan = SpecializationPlan.VACIO,
    val produccion: ProductionOutcome? = null,
    val comparacion: PlanComparison? = null,

    // Trabajo en equipo
    val planCooperacion: CooperationPlan = CooperationPlan.VACIO,
    val resultadoCooperacion: CooperationOutcome? = null,

    // Reparto de lo escaso
    val reparto: Allocation = Allocation.VACIO,
    val resultadoReparto: ScarcityResult? = null,

    // Decisión con recursos limitados
    val seleccion: List<String> = emptyList(),
    val resultadoDecision: BudgetResult? = null,

    // Lo que dejas de hacer
    val respuestaRenuncia: Int = 0,

    // Cadena de producción
    val ordenActual: List<ChainStep> = emptyList(),
    val pasoLevantado: String? = null
) {
    val inventarioOfrecido: Inventory get() = Inventory.of(ofrezco)
    val inventarioPedido: Inventory get() = Inventory.of(pido)
}

/**
 * Cerebro de todas las actividades del Valle.
 *
 * No implementa NINGUNA regla económica: pregunta a `EconomyEngine` y guarda
 * el resultado con `ProgressRepository`. Así las reglas se pueden probar sin
 * abrir la app.
 */
class ActivityViewModel(
    private val contenedor: AppContainer,
    private val escenarioId: String
) : ViewModel() {

    private val _estado = MutableStateFlow(ActividadUiState())
    val estado: StateFlow<ActividadUiState> = _estado

    private val motor get() = contenedor.motor

    init {
        cargar()
    }

    private fun cargar() {
        val escenario = contenedor.catalogo.escenario(escenarioId)
        val inicial = ActividadUiState(escenario = escenario)
        _estado.value = when (val p = escenario.payload) {
            is ScenarioPayload.Intercambio -> inicial.copy(
                sesion = motor.nuevaSesion(escenario),
                socioSeleccionado = escenario.participantes.firstOrNull()?.id
            )
            is ScenarioPayload.Cadena -> inicial.copy(
                ordenActual = motor.cadena.barajar(p.cadena, escenario.id.hashCode().toLong())
            )
            is ScenarioPayload.Escasez -> inicial.copy(
                reparto = Allocation(p.caso.demandas.associate { it.personajeId to 0 })
            )
            else -> inicial
        }
    }

    // ------------------------------------------------------------ intercambio

    fun elegirSocio(id: String) = _estado.update {
        it.copy(socioSeleccionado = id, pido = emptyMap(), ultimaEvaluacion = null)
    }

    fun cambiarOferta(recursoId: String, cantidad: Int) = _estado.update {
        val nuevo = it.ofrezco.toMutableMap()
        if (cantidad <= 0) nuevo.remove(recursoId) else nuevo[recursoId] = cantidad
        it.copy(ofrezco = nuevo, ultimaEvaluacion = null)
    }

    fun cambiarPeticion(recursoId: String, cantidad: Int) = _estado.update {
        val nuevo = it.pido.toMutableMap()
        if (cantidad <= 0) nuevo.remove(recursoId) else nuevo[recursoId] = cantidad
        it.copy(pido = nuevo, ultimaEvaluacion = null)
    }

    fun limpiarMesa() = _estado.update {
        it.copy(ofrezco = emptyMap(), pido = emptyMap(), ultimaEvaluacion = null)
    }

    fun proponerIntercambio() {
        val s = _estado.value
        val sesion = s.sesion ?: return
        val socio = s.socioSeleccionado ?: return
        val oferta = TradeOffer(
            proponenteId = sesion.jugador.id,
            receptorId = socio,
            entrega = s.inventarioOfrecido,
            pide = s.inventarioPedido
        )
        val paso = motor.proponer(sesion, oferta)
        val aceptado = paso.evaluacion is TradeEvaluation.Aceptado
        val motivo = (paso.evaluacion as? TradeEvaluation.Rechazado)?.motivo?.name

        viewModelScope.launch {
            contenedor.progreso.registrarIntercambio(escenarioId, oferta, aceptado, motivo)
        }

        _estado.update {
            it.copy(
                sesion = paso.sesion,
                ultimaEvaluacion = paso.evaluacion,
                ofrezco = if (aceptado) emptyMap() else it.ofrezco,
                pido = if (aceptado) emptyMap() else it.pido,
                pista = null
            )
        }

        if (paso.objetivoCumplido) {
            registrar(
                AttemptResult(
                    escenarioId = escenarioId,
                    logrado = true,
                    mensaje = "¡Conseguido! Ya tienes lo que hacía falta.",
                    explicacion = _estado.value.escenario?.explicacionFinal.orEmpty(),
                    intentos = _estado.value.intentos + 1,
                    conceptoId = _estado.value.escenario?.conceptoId.orEmpty()
                )
            )
        }
    }

    fun pedirPista() {
        val sesion = _estado.value.sesion
        val escenario = _estado.value.escenario ?: return
        val corta = (escenario.payload as? ScenarioPayload.Intercambio)?.pistaCorta
        if (sesion == null) {
            _estado.update { it.copy(pista = corta) }
            return
        }
        val sugerida = motor.pista(sesion)
        val texto = if (sugerida != null) {
            val quien = sesion.otros.firstOrNull { it.id == sugerida.receptorId }?.nombre ?: "alguien"
            "Prueba con $quien: ofrécele ${sugerida.entrega.descripcion(motor.catalogo)} " +
                "y pídele ${sugerida.pide.descripcion(motor.catalogo)}."
        } else {
            corta ?: "Mira otra vez qué necesita cada vecino."
        }
        _estado.update { it.copy(pista = texto) }
    }

    // -------------------------------------------------------- especialización

    fun asignarOficio(personajeId: String, recursoId: String?) = _estado.update {
        it.copy(plan = it.plan.con(personajeId, recursoId), produccion = null, comparacion = null)
    }

    fun quitarOficio(personajeId: String) = _estado.update {
        it.copy(plan = it.plan.sin(personajeId), produccion = null, comparacion = null)
    }

    fun ejecutarProduccion() {
        val s = _estado.value
        val escenario = s.escenario ?: return
        val payload = escenario.payload as? ScenarioPayload.Especializacion ?: return
        val salida = motor.especializacion.producir(escenario.participantes, s.plan)
        val comparacion = if (payload.comparaConDeTodo) {
            motor.especializacion.comparar(
                escenario.participantes,
                motor.especializacion.planDeTodo(escenario.participantes),
                s.plan
            )
        } else null

        _estado.update { it.copy(produccion = salida, comparacion = comparacion) }

        viewModelScope.launch {
            contenedor.progreso.registrarEspecializacion(
                escenarioId = escenarioId,
                plan = s.plan,
                resultado = salida,
                valor = salida.total.valor(motor.catalogo),
                cumplio = salida.cumple(payload.objetivo)
            )
            responder(ScenarioAnswer.Especializar(s.plan))
        }
    }

    // ------------------------------------------------------------ cooperación

    fun asignarEtapa(personajeId: String, etapaId: String) = _estado.update {
        it.copy(planCooperacion = it.planCooperacion.con(personajeId, etapaId), resultadoCooperacion = null)
    }

    fun ejecutarCooperacion() {
        val s = _estado.value
        val escenario = s.escenario ?: return
        val payload = escenario.payload as? ScenarioPayload.Cooperacion ?: return
        val salida = motor.cooperacion.ejecutar(
            escenario.participantes, payload.etapas, s.planCooperacion, payload.objetivo
        )
        _estado.update { it.copy(resultadoCooperacion = salida) }
        viewModelScope.launch {
            contenedor.progreso.registrarCooperacion(escenarioId, s.planCooperacion, salida)
            responder(ScenarioAnswer.Cooperar(s.planCooperacion))
        }
    }

    // --------------------------------------------------------------- escasez

    fun cambiarReparto(personajeId: String, cantidad: Int) = _estado.update {
        it.copy(reparto = it.reparto.con(personajeId, cantidad), resultadoReparto = null)
    }

    fun confirmarReparto() {
        val s = _estado.value
        val escenario = s.escenario ?: return
        val payload = escenario.payload as? ScenarioPayload.Escasez ?: return
        val res = motor.escasez.evaluar(payload.caso, s.reparto)
        _estado.update { it.copy(resultadoReparto = res) }
        viewModelScope.launch {
            contenedor.progreso.registrarReparto(
                escenarioId, payload.caso.recursoId, payload.caso.disponible, s.reparto, res.valido
            )
            responder(ScenarioAnswer.Repartir(s.reparto))
        }
    }

    // -------------------------------------------------------------- decisión

    fun alternarOpcion(opcionId: String) = _estado.update {
        val nueva = if (opcionId in it.seleccion) it.seleccion - opcionId else it.seleccion + opcionId
        it.copy(seleccion = nueva, resultadoDecision = null)
    }

    fun confirmarDecision() {
        val s = _estado.value
        val escenario = s.escenario ?: return
        val payload = escenario.payload as? ScenarioPayload.Decision ?: return
        val res = motor.escasez.evaluarDecision(payload.caso, s.seleccion)
        _estado.update { it.copy(resultadoDecision = res) }
        viewModelScope.launch {
            contenedor.progreso.registrarDecision(escenarioId, s.seleccion, res.renuncias, res.alcanza)
            responder(ScenarioAnswer.Decidir(s.seleccion))
        }
    }

    // ------------------------------------------------- costo de oportunidad

    fun cambiarRenuncia(valor: Int) = _estado.update { it.copy(respuestaRenuncia = valor) }

    fun confirmarRenuncia() {
        viewModelScope.launch { responder(ScenarioAnswer.Renuncia(_estado.value.respuestaRenuncia)) }
    }

    // ------------------------------------------------------ evaluar una oferta

    fun responderOferta(acepta: Boolean) {
        viewModelScope.launch { responder(ScenarioAnswer.Evaluacion(acepta)) }
    }

    // ---------------------------------------------------------------- cadena

    fun tocarPaso(pasoId: String) = _estado.update { s ->
        val levantado = s.pasoLevantado
        if (levantado == null) {
            s.copy(pasoLevantado = pasoId)
        } else if (levantado == pasoId) {
            s.copy(pasoLevantado = null)
        } else {
            val lista = s.ordenActual.toMutableList()
            val i = lista.indexOfFirst { it.id == levantado }
            val j = lista.indexOfFirst { it.id == pasoId }
            if (i >= 0 && j >= 0) {
                val tmp = lista[i]; lista[i] = lista[j]; lista[j] = tmp
            }
            s.copy(ordenActual = lista, pasoLevantado = null)
        }
    }

    fun moverPaso(pasoId: String, haciaArriba: Boolean) = _estado.update { s ->
        val lista = s.ordenActual.toMutableList()
        val i = lista.indexOfFirst { it.id == pasoId }
        val j = if (haciaArriba) i - 1 else i + 1
        if (i < 0 || j !in lista.indices) return@update s
        val tmp = lista[i]; lista[i] = lista[j]; lista[j] = tmp
        s.copy(ordenActual = lista)
    }

    fun confirmarCadena() {
        val s = _estado.value
        val escenario = s.escenario ?: return
        val payload = escenario.payload as? ScenarioPayload.Cadena ?: return
        val orden = s.ordenActual.map { it.id }
        val res = motor.cadena.evaluar(payload.cadena, orden)
        viewModelScope.launch {
            contenedor.progreso.registrarCadena(escenarioId, orden, res.correcto, res.aciertosSeguidos)
            responder(ScenarioAnswer.Ordenar(orden))
        }
    }

    // ----------------------------------------------------------------- común

    private suspend fun responder(respuesta: ScenarioAnswer) {
        val escenario = _estado.value.escenario ?: return
        val intentos = _estado.value.intentos + 1
        val resultado = motor.evaluar(escenario, respuesta, intentos)
        _estado.update { it.copy(intentos = intentos) }
        registrar(resultado)
    }

    private fun registrar(resultado: AttemptResult) {
        val escenario = _estado.value.escenario ?: return
        viewModelScope.launch {
            val actualizacion = contenedor.progreso.registrarIntento(escenario, resultado)
            if (resultado.logrado &&
                contenedor.laboratorio.retoDeHoy()?.escenarioId == escenario.id
            ) {
                contenedor.laboratorio.marcarRetoCompletado()
            }
            _estado.update {
                it.copy(
                    resultado = resultado,
                    mostrarResultado = true,
                    actualizacion = actualizacion
                )
            }
        }
    }

    fun reintentar() = _estado.update {
        it.copy(mostrarResultado = false, resultado = null, actualizacion = null, pista = null)
    }

    fun cerrarCelebracion() = _estado.update { it.copy(actualizacion = null) }
}
