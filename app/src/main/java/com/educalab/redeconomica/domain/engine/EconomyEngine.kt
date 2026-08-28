package com.educalab.redeconomica.domain.engine

import com.educalab.redeconomica.domain.model.Allocation
import com.educalab.redeconomica.domain.model.AttemptResult
import com.educalab.redeconomica.domain.model.CooperationPlan
import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.ResourceDef
import com.educalab.redeconomica.domain.model.Scenario
import com.educalab.redeconomica.domain.model.ScenarioPayload
import com.educalab.redeconomica.domain.model.SpecializationPlan
import com.educalab.redeconomica.domain.model.TradeEvaluation
import com.educalab.redeconomica.domain.model.TradeOffer
import kotlin.math.abs

/** Lo que el niño responde en un escenario, según la mecánica de cada uno. */
sealed interface ScenarioAnswer {
    data class Intercambio(val oferta: TradeOffer) : ScenarioAnswer
    data class Evaluacion(val acepta: Boolean) : ScenarioAnswer
    data class Especializar(val plan: SpecializationPlan) : ScenarioAnswer
    data class Renuncia(val cantidadRenunciada: Int) : ScenarioAnswer
    data class Cooperar(val plan: CooperationPlan) : ScenarioAnswer
    data class Repartir(val reparto: Allocation) : ScenarioAnswer
    data class Decidir(val seleccion: List<String>) : ScenarioAnswer
    data class Ordenar(val orden: List<String>) : ScenarioAnswer
}

/** Estado vivo de una mesa de intercambios dentro de un escenario. */
data class TradeSession(
    val escenarioId: String,
    val jugador: EconomicCharacter,
    val otros: List<EconomicCharacter>,
    val objetivo: Inventory,
    val historial: List<TradeOffer> = emptyList(),
    val exigirBeneficioMutuo: Boolean = true
) {
    val objetivoCumplido: Boolean get() = jugador.inventario.contiene(objetivo)

    fun faltaParaObjetivo(): Inventory = Inventory.of(
        objetivo.contenido.mapValues { (id, n) ->
            (n - jugador.inventario.cantidad(id)).coerceAtLeast(0)
        }
    )

    fun con(id: String): EconomicCharacter? =
        if (id == jugador.id) jugador else otros.firstOrNull { it.id == id }
}

/** Un paso de la mesa de intercambios: nueva sesión + explicación de lo ocurrido. */
data class TradeStep(
    val sesion: TradeSession,
    val evaluacion: TradeEvaluation,
    val objetivoCumplido: Boolean
)

/**
 * Fachada del dominio económico.
 *
 * Reúne los motores especializados y evalúa cualquier escenario del Valle.
 * Ni un solo Composable calcula producción, inventarios ni aceptación de
 * intercambios: todo pasa por aquí.
 */
class EconomyEngine(
    val catalogo: Map<String, ResourceDef>,
    val intercambio: TradeEngine = TradeEngine(catalogo),
    val especializacion: SpecializationEngine = SpecializationEngine(catalogo),
    val cooperacion: CooperationEngine = CooperationEngine(),
    val escasez: ScarcityEngine = ScarcityEngine(catalogo),
    val costoOportunidad: OpportunityCostEngine = OpportunityCostEngine(catalogo),
    val cadena: ProductionChainEngine = ProductionChainEngine()
) {

    // ------------------------------------------------------- mesa de trueques

    fun nuevaSesion(escenario: Scenario): TradeSession {
        val payload = escenario.payload as? ScenarioPayload.Intercambio
            ?: error("El escenario ${escenario.id} no es de intercambio")
        return TradeSession(
            escenarioId = escenario.id,
            jugador = escenario.jugador,
            otros = escenario.participantes,
            objetivo = payload.objetivo,
            exigirBeneficioMutuo = payload.exigirBeneficioMutuo
        )
    }

    fun proponer(sesion: TradeSession, oferta: TradeOffer): TradeStep {
        val receptor = sesion.otros.firstOrNull { it.id == oferta.receptorId }
            ?: return TradeStep(
                sesion,
                TradeEvaluation.Rechazado(
                    oferta,
                    com.educalab.redeconomica.domain.model.TradeRejectReason.SIN_RECURSOS_RECEPTOR,
                    "Ese habitante no está en el mercado ahora mismo.",
                    "Elige a alguien de los que ves en la plaza."
                ),
                sesion.objetivoCumplido
            )

        val evaluacion = intercambio.evaluar(
            oferta, sesion.jugador, receptor, sesion.exigirBeneficioMutuo
        )
        return when (evaluacion) {
            is TradeEvaluation.Rechazado -> TradeStep(sesion, evaluacion, sesion.objetivoCumplido)
            is TradeEvaluation.Aceptado -> {
                val nuevaSesion = sesion.copy(
                    jugador = evaluacion.proponenteActualizado,
                    otros = sesion.otros.map {
                        if (it.id == receptor.id) evaluacion.receptorActualizado else it
                    },
                    historial = sesion.historial + oferta
                )
                TradeStep(nuevaSesion, evaluacion, nuevaSesion.objetivoCumplido)
            }
        }
    }

    /** Una propuesta que sí funcionaría, para dar una pista sin resolverlo todo. */
    fun pista(sesion: TradeSession): TradeOffer? =
        intercambio.buscarIntercambios(
            sesion.jugador, sesion.otros, exigirBeneficioMutuo = sesion.exigirBeneficioMutuo
        ).minByOrNull { it.entrega.total }

    // ------------------------------------------------ evaluación de escenarios

    fun evaluar(escenario: Scenario, respuesta: ScenarioAnswer, intentos: Int): AttemptResult =
        when (val p = escenario.payload) {
            is ScenarioPayload.Intercambio -> evaluarIntercambio(escenario, p, respuesta, intentos)
            is ScenarioPayload.EvaluarOferta -> evaluarOferta(escenario, p, respuesta, intentos)
            is ScenarioPayload.Especializacion -> evaluarEspecializacion(escenario, p, respuesta, intentos)
            is ScenarioPayload.CostoOportunidad -> evaluarCosto(escenario, p, respuesta, intentos)
            is ScenarioPayload.Cooperacion -> evaluarCooperacion(escenario, p, respuesta, intentos)
            is ScenarioPayload.Escasez -> evaluarEscasez(escenario, p, respuesta, intentos)
            is ScenarioPayload.Decision -> evaluarDecision(escenario, p, respuesta, intentos)
            is ScenarioPayload.Cadena -> evaluarCadena(escenario, p, respuesta, intentos)
        }

    private fun evaluarIntercambio(
        e: Scenario, p: ScenarioPayload.Intercambio, r: ScenarioAnswer, intentos: Int
    ): AttemptResult {
        val a = r as? ScenarioAnswer.Intercambio ?: return desajuste(e, intentos)
        val receptor = e.participantes.firstOrNull { it.id == a.oferta.receptorId }
            ?: return AttemptResult(
                e.id, false, "Ese habitante no está aquí.",
                "Propón el intercambio a alguien de la plaza.", intentos, e.conceptoId
            )
        val res = intercambio.evaluar(a.oferta, e.jugador, receptor, p.exigirBeneficioMutuo)
        return when (res) {
            is TradeEvaluation.Aceptado -> {
                val nuevoInv = res.proponenteActualizado.inventario
                val logrado = nuevoInv.contiene(p.objetivo)
                AttemptResult(
                    e.id, logrado, res.mensaje,
                    if (logrado) e.explicacionFinal else res.loQueGanaCadaUno,
                    intentos, e.conceptoId
                )
            }
            is TradeEvaluation.Rechazado ->
                AttemptResult(e.id, false, res.mensaje, res.pista, intentos, e.conceptoId)
        }
    }

    private fun evaluarOferta(
        e: Scenario, p: ScenarioPayload.EvaluarOferta, r: ScenarioAnswer, intentos: Int
    ): AttemptResult {
        val a = r as? ScenarioAnswer.Evaluacion ?: return desajuste(e, intentos)
        val acierta = a.acepta == p.aceptarEsLoCorrecto
        val mensaje = when {
            acierta && p.aceptarEsLoCorrecto -> "Bien visto: a los dos les viene bien."
            acierta -> "Bien visto: ese trato no le convenía a alguien."
            p.aceptarEsLoCorrecto -> "Míralo otra vez: sí había un beneficio para ambos."
            else -> "Míralo otra vez: alguien salía perdiendo."
        }
        val motivo = p.motivoEsperado?.etiqueta
        return AttemptResult(
            e.id, acierta, mensaje,
            if (motivo != null && !p.aceptarEsLoCorrecto)
                "$motivo. ${e.explicacionFinal}" else e.explicacionFinal,
            intentos, e.conceptoId
        )
    }

    private fun evaluarEspecializacion(
        e: Scenario, p: ScenarioPayload.Especializacion, r: ScenarioAnswer, intentos: Int
    ): AttemptResult {
        val a = r as? ScenarioAnswer.Especializar ?: return desajuste(e, intentos)
        val out = especializacion.producir(e.participantes, a.plan)
        val logrado = out.cumple(p.objetivo)
        val falta = out.faltaPara(p.objetivo)
        return AttemptResult(
            escenarioId = e.id,
            logrado = logrado,
            mensaje = if (logrado)
                "¡Lo habéis conseguido! El Valle produce lo que hacía falta."
            else
                "Casi. Todavía falta ${falta.descripcion(catalogo)}.",
            explicacion = if (logrado) e.explicacionFinal
            else "Fíjate en quién produce más de cada cosa y prueba otro reparto.",
            intentos = intentos,
            conceptoId = e.conceptoId
        )
    }

    private fun evaluarCosto(
        e: Scenario, p: ScenarioPayload.CostoOportunidad, r: ScenarioAnswer, intentos: Int
    ): AttemptResult {
        val a = r as? ScenarioAnswer.Renuncia ?: return desajuste(e, intentos)
        val personaje = e.participante(p.personajeId)
            ?: error("Falta el personaje ${p.personajeId} en ${e.id}")
        val res = costoOportunidad.costoDeOportunidad(
            personaje, p.recursoElegido, p.recursoRenunciado, p.cantidadElegida
        )
        val acierta = abs(a.cantidadRenunciada - res.cantidadRenunciadaExacta) < 0.5
        return AttemptResult(
            e.id, acierta,
            if (acierta) "¡Exacto! ${res.texto}"
            else "Todavía no. Compara los dos totales del turno.",
            if (acierta) e.explicacionFinal else res.explicacion,
            intentos, e.conceptoId
        )
    }

    private fun evaluarCooperacion(
        e: Scenario, p: ScenarioPayload.Cooperacion, r: ScenarioAnswer, intentos: Int
    ): AttemptResult {
        val a = r as? ScenarioAnswer.Cooperar ?: return desajuste(e, intentos)
        val out = cooperacion.ejecutar(e.participantes, p.etapas, a.plan, p.objetivo)
        return AttemptResult(
            e.id, out.completado, out.mensaje,
            if (out.completado) "${out.explicacion} ${e.explicacionFinal}" else out.explicacion,
            intentos, e.conceptoId
        )
    }

    private fun evaluarEscasez(
        e: Scenario, p: ScenarioPayload.Escasez, r: ScenarioAnswer, intentos: Int
    ): AttemptResult {
        val a = r as? ScenarioAnswer.Repartir ?: return desajuste(e, intentos)
        val res = escasez.evaluar(p.caso, a.reparto)
        return AttemptResult(
            e.id, res.valido, res.mensaje,
            if (res.valido) "${res.explicacion} ${e.explicacionFinal}" else res.explicacion,
            intentos, e.conceptoId
        )
    }

    private fun evaluarDecision(
        e: Scenario, p: ScenarioPayload.Decision, r: ScenarioAnswer, intentos: Int
    ): AttemptResult {
        val a = r as? ScenarioAnswer.Decidir ?: return desajuste(e, intentos)
        val res = escasez.evaluarDecision(p.caso, a.seleccion)
        return AttemptResult(
            e.id, res.alcanza, res.mensaje,
            if (res.alcanza) "${res.explicacion} ${e.explicacionFinal}" else res.explicacion,
            intentos, e.conceptoId
        )
    }

    private fun evaluarCadena(
        e: Scenario, p: ScenarioPayload.Cadena, r: ScenarioAnswer, intentos: Int
    ): AttemptResult {
        val a = r as? ScenarioAnswer.Ordenar ?: return desajuste(e, intentos)
        val res = cadena.evaluar(p.cadena, a.orden)
        return AttemptResult(
            e.id, res.correcto, res.mensaje, res.explicacion, intentos, e.conceptoId
        )
    }

    private fun desajuste(e: Scenario, intentos: Int) = AttemptResult(
        e.id, false,
        "Esa respuesta no encaja con esta actividad.",
        "Vuelve a intentarlo usando los controles de la pantalla.",
        intentos, e.conceptoId
    )
}
