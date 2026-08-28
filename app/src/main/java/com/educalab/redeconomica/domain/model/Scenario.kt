package com.educalab.redeconomica.domain.model

/** Tipo de actividad. Cada uno usa una mecánica distinta, no un cuestionario. */
enum class ActivityKind(val etiqueta: String, val comoSeJuega: String) {
    INTERCAMBIO("Intercambio", "Arrastra lo que ofreces y lo que pides"),
    EVALUAR_OFERTA("¿Aceptarías?", "Mira la propuesta y decide"),
    ESPECIALIZACION("Elige tu oficio", "Asigna a cada habitante una tarea"),
    COSTO_OPORTUNIDAD("Lo que dejas de hacer", "Mueve el reparto del turno y observa"),
    COOPERACION("Trabajo en equipo", "Coloca a cada uno en una etapa"),
    ESCASEZ("No alcanza para todos", "Reparte lo poco que hay"),
    DECISION("La gran decisión", "Gasta unos recursos limitados"),
    CADENA("Cadena de producción", "Ordena los pasos")
}

/** Datos propios de cada tipo de actividad. */
sealed interface ScenarioPayload {

    data class Intercambio(
        val objetivo: Inventory,
        val exigirBeneficioMutuo: Boolean = true,
        val pistaCorta: String = ""
    ) : ScenarioPayload

    data class EvaluarOferta(
        val oferta: TradeOffer,
        val aceptarEsLoCorrecto: Boolean,
        val motivoEsperado: TradeRejectReason?
    ) : ScenarioPayload

    data class Especializacion(
        val objetivo: Inventory,
        val comparaConDeTodo: Boolean = true
    ) : ScenarioPayload

    data class CostoOportunidad(
        val personajeId: String,
        val recursoElegido: String,
        val recursoRenunciado: String,
        val cantidadElegida: Int
    ) : ScenarioPayload

    data class Cooperacion(
        val etapas: List<CooperationStage>,
        val objetivo: Int
    ) : ScenarioPayload

    data class Escasez(val caso: ScarcityCase) : ScenarioPayload

    data class Decision(val caso: BudgetCase) : ScenarioPayload

    data class Cadena(val cadena: ChainDef) : ScenarioPayload
}

/**
 * Un desafío completo y autocontenido del Valle.
 *
 * Los escenarios son DATOS, no código de interfaz: se definen en el contenido
 * semilla, se validan con [com.educalab.redeconomica.domain.engine.ScenarioValidator]
 * y la pantalla solo los representa.
 */
data class Scenario(
    val id: String,
    val tipo: ActivityKind,
    val titulo: String,
    val situacion: String,
    val instruccion: String,
    val jugador: EconomicCharacter,
    val participantes: List<EconomicCharacter>,
    val payload: ScenarioPayload,
    val explicacionFinal: String,
    val conceptoId: String,
    val dificultad: Int
) {
    init {
        require(dificultad in 1..5) { "Dificultad de $id fuera de rango" }
    }

    fun participante(id: String): EconomicCharacter? =
        participantes.firstOrNull { it.id == id } ?: jugador.takeIf { it.id == id }

    val todos: List<EconomicCharacter> get() = listOf(jugador) + participantes
}

/** Cómo terminó un intento del niño en un escenario. */
data class AttemptResult(
    val escenarioId: String,
    val logrado: Boolean,
    val mensaje: String,
    val explicacion: String,
    val intentos: Int,
    val conceptoId: String
)
