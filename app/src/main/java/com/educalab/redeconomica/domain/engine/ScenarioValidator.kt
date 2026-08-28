package com.educalab.redeconomica.domain.engine

import com.educalab.redeconomica.domain.model.Scenario
import com.educalab.redeconomica.domain.model.ScenarioPayload
import com.educalab.redeconomica.domain.model.TradeEvaluation

/** Un problema detectado en un escenario antes de mostrárselo a nadie. */
data class ValidationIssue(val escenarioId: String, val problema: String)

/**
 * Validador de escenarios.
 *
 * Regla del proyecto: nunca se le presenta a un niño una situación imposible.
 * Antes de que un escenario llegue a la pantalla se comprueba que los
 * recursos existen, que las cantidades son coherentes y —lo más importante—
 * que TIENE al menos una solución. Las pruebas ejecutan este validador sobre
 * todo el contenido semilla.
 */
class ScenarioValidator(private val motor: EconomyEngine) {

    fun validarTodos(escenarios: List<Scenario>): List<ValidationIssue> =
        escenarios.flatMap { validar(it) }

    fun validar(e: Scenario): List<ValidationIssue> {
        val fallos = mutableListOf<ValidationIssue>()
        fun mal(texto: String) = fallos.add(ValidationIssue(e.id, texto))

        val ids = e.todos.map { it.id }
        if (ids.size != ids.toSet().size) mal("Hay habitantes repetidos")
        if (e.participantes.isEmpty()) mal("No hay ningún habitante en el escenario")
        if (e.titulo.isBlank()) mal("Falta el título")
        if (e.situacion.isBlank()) mal("Falta la situación narrada")
        if (e.instruccion.isBlank()) mal("Falta la instrucción")
        if (e.explicacionFinal.isBlank()) mal("Falta la explicación educativa")

        e.todos.forEach { p ->
            p.inventario.recursos.forEach {
                if (it !in motor.catalogo) mal("Recurso desconocido en inventario: $it")
            }
            p.necesidades.forEach {
                if (it.recursoId !in motor.catalogo) mal("Necesidad de recurso desconocido: ${it.recursoId}")
            }
            p.productividad.keys.forEach {
                if (it !in motor.catalogo) mal("Productividad de recurso desconocido: $it")
            }
        }

        when (val p = e.payload) {
            is ScenarioPayload.Intercambio -> {
                if (p.objetivo.esVacio) mal("El objetivo del intercambio está vacío")
                p.objetivo.recursos.forEach {
                    if (it !in motor.catalogo) mal("Objetivo con recurso desconocido: $it")
                }
                val soluciones = motor.intercambio.buscarIntercambios(
                    e.jugador, e.participantes, exigirBeneficioMutuo = p.exigirBeneficioMutuo
                )
                if (soluciones.isEmpty()) mal("Ningún intercambio es posible: el escenario no tiene salida")
                val alcanzable = p.objetivo.contenido.all { (r, n) ->
                    e.jugador.inventario.cantidad(r) +
                        e.participantes.sumOf { it.inventario.cantidad(r) } >= n
                }
                if (!alcanzable) mal("El objetivo pide más de lo que existe en el escenario")
            }

            is ScenarioPayload.EvaluarOferta -> {
                val prop = e.participante(p.oferta.proponenteId)
                val rec = e.participante(p.oferta.receptorId)
                if (prop == null || rec == null) mal("La oferta menciona a alguien que no está")
                else {
                    val res = motor.intercambio.evaluar(p.oferta, prop, rec, exigirBeneficioMutuo = false)
                    val aceptado = res is TradeEvaluation.Aceptado
                    if (aceptado != p.aceptarEsLoCorrecto) {
                        mal("La respuesta esperada no coincide con lo que decide el motor")
                    }
                }
            }

            is ScenarioPayload.Especializacion -> {
                if (p.objetivo.esVacio) mal("El objetivo de producción está vacío")
                val planes = motor.especializacion.planesQueCumplen(e.participantes, p.objetivo)
                if (planes.isEmpty()) mal("Ningún reparto de trabajo alcanza el objetivo")
            }

            is ScenarioPayload.CostoOportunidad -> {
                val per = e.participante(p.personajeId)
                if (per == null) mal("El personaje del costo de oportunidad no está en el escenario")
                else {
                    val pe = per.produccionPorTurno(p.recursoElegido)
                    val pr = per.produccionPorTurno(p.recursoRenunciado)
                    if (pe <= 0) mal("${per.nombre} no puede producir ${p.recursoElegido}")
                    if (pr <= 0) mal("${per.nombre} no puede producir ${p.recursoRenunciado}: no renuncia a nada")
                    if (p.cantidadElegida !in 1..pe) mal("La cantidad elegida no cabe en un turno")
                }
            }

            is ScenarioPayload.Cooperacion -> {
                if (p.etapas.size < 2) mal("Un trabajo en equipo necesita al menos dos etapas")
                if (p.objetivo <= 0) mal("El objetivo de cooperación debe ser positivo")
                val planes = motor.cooperacion.planesQueCumplen(e.participantes, p.etapas, p.objetivo)
                if (planes.isEmpty()) mal("Ningún reparto de tareas alcanza el objetivo")
                val sinNadie = p.etapas.filter { etapa ->
                    e.participantes.none { etapa.rendimientoDe(it.id) > 0 }
                }
                if (sinNadie.isNotEmpty()) {
                    mal("Hay etapas que nadie puede hacer: ${sinNadie.joinToString { it.nombre }}")
                }
            }

            is ScenarioPayload.Escasez -> {
                if (p.caso.recursoId !in motor.catalogo) mal("Recurso escaso desconocido")
                val validos = motor.escasez.repartosValidos(p.caso)
                if (validos.isEmpty()) mal("No existe ningún reparto válido")
                p.caso.demandas.forEach { d ->
                    if (e.participante(d.personajeId) == null && d.personajeId != e.jugador.id) {
                        mal("Demanda de alguien que no está en el escenario: ${d.personajeId}")
                    }
                }
            }

            is ScenarioPayload.Decision -> {
                val posibles = motor.escasez.decisionesPosibles(p.caso)
                if (posibles.isEmpty()) mal("Con ese presupuesto no se puede elegir nada")
                if (posibles.size == p.caso.opciones.size &&
                    p.caso.opciones.all { p.caso.presupuesto.contiene(it.costo) } &&
                    posibles.any { it.size == p.caso.opciones.size }
                ) {
                    mal("Alcanza para todo: no hay ninguna renuncia que aprender")
                }
            }

            is ScenarioPayload.Cadena -> {
                val idsPasos = p.cadena.pasos.map { it.id }
                if (idsPasos.size != idsPasos.toSet().size) mal("Pasos repetidos en la cadena")
                val ordenes = p.cadena.pasos.map { it.etapa.orden }
                if (ordenes != ordenes.sorted()) mal("La cadena semilla no está en orden correcto")
            }
        }
        return fallos
    }

    /** Cuántas soluciones distintas admite el escenario (0 si no aplica). */
    fun numeroDeSoluciones(e: Scenario): Int = when (val p = e.payload) {
        is ScenarioPayload.Especializacion ->
            motor.especializacion.planesQueCumplen(e.participantes, p.objetivo).size
        is ScenarioPayload.Cooperacion ->
            motor.cooperacion.planesQueCumplen(e.participantes, p.etapas, p.objetivo).size
        is ScenarioPayload.Escasez -> motor.escasez.repartosValidos(p.caso).size
        is ScenarioPayload.Decision -> motor.escasez.decisionesPosibles(p.caso).size
        is ScenarioPayload.Intercambio -> motor.intercambio.buscarIntercambios(
            e.jugador, e.participantes, exigirBeneficioMutuo = p.exigirBeneficioMutuo
        ).size
        else -> 0
    }
}
