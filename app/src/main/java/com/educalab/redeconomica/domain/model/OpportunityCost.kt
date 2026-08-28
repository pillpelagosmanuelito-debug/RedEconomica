package com.educalab.redeconomica.domain.model

/** Un punto de "lo que puedo producir en un turno" si reparto así mi tiempo. */
data class ProductionPoint(val cantidadA: Int, val cantidadB: Int)

/** Cuánto dejas de producir al elegir una cosa en vez de otra. */
data class OpportunityCostResult(
    val recursoElegido: String,
    val cantidadElegida: Int,
    val recursoRenunciado: String,
    val cantidadRenunciadaExacta: Double,
    val cantidadRenunciadaRedondeada: Int,
    val texto: String,
    val explicacion: String
)

/** Quién debería dedicarse a qué, comparando lo que cada uno deja de hacer. */
data class ComparativeAdvantageResult(
    val personajeAId: String,
    val personajeBId: String,
    val recursoUno: String,
    val recursoDos: String,
    val costoAEnUno: Double,
    val costoBEnUno: Double,
    val recomendadoParaA: String,
    val recomendadoParaB: String,
    val empate: Boolean,
    val explicacion: String
)
