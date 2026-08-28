package com.educalab.redeconomica.domain.model

/**
 * Una propuesta de intercambio: quien propone ENTREGA algo y PIDE algo.
 *
 * En RedEconómica el jugador es un habitante más (id [ID_JUGADOR]), así que
 * el mismo motor evalúa los intercambios que propone el niño y los que le
 * proponen a él.
 */
data class TradeOffer(
    val proponenteId: String,
    val receptorId: String,
    val entrega: Inventory,
    val pide: Inventory
) {
    val esVacia: Boolean get() = entrega.esVacio || pide.esVacio

    fun invertida(): TradeOffer =
        TradeOffer(receptorId, proponenteId, pide, entrega)

    companion object {
        const val ID_JUGADOR = "jugador"
    }
}

/** Motivo por el que un intercambio no sale adelante. Siempre se explica al niño. */
enum class TradeRejectReason(val etiqueta: String) {
    OFERTA_VACIA("Falta completar la propuesta"),
    SIN_RECURSOS_PROPONENTE("No tienes eso para entregar"),
    SIN_RECURSOS_RECEPTOR("El habitante no tiene lo que pides"),
    NO_NECESITA_LO_OFRECIDO("No le hace falta lo que le ofreces"),
    PERDERIA_LO_QUE_NECESITA("Se quedaría sin algo que necesita mucho"),
    DESEQUILIBRIO("Le parece que entrega demasiado"),
    SIN_BENEFICIO_MUTUO("A ti no te resuelve ninguna necesidad")
}

/** Resultado de evaluar una propuesta. */
sealed interface TradeEvaluation {

    val oferta: TradeOffer

    data class Aceptado(
        override val oferta: TradeOffer,
        val proponenteActualizado: EconomicCharacter,
        val receptorActualizado: EconomicCharacter,
        val mensaje: String,
        val loQueGanaCadaUno: String
    ) : TradeEvaluation

    data class Rechazado(
        override val oferta: TradeOffer,
        val motivo: TradeRejectReason,
        val mensaje: String,
        val pista: String
    ) : TradeEvaluation
}
