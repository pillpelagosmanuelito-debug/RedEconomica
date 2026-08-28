package com.educalab.redeconomica.domain.model

/** Alguien que necesita parte de un recurso escaso. */
data class ScarcityDemand(
    val personajeId: String,
    val nombre: String,
    val cantidad: Int,
    val urgencia: Urgency,
    val motivo: String
) {
    init { require(cantidad > 0) { "Una demanda pide al menos 1 unidad" } }
}

/** Situación de escasez: hay menos de lo que hace falta. */
data class ScarcityCase(
    val recursoId: String,
    val disponible: Int,
    val demandas: List<ScarcityDemand>
) {
    init {
        require(disponible >= 0) { "No puede haber una cantidad disponible negativa" }
        require(demandas.isNotEmpty()) { "Hace falta al menos una demanda" }
    }

    val demandaTotal: Int get() = demandas.sumOf { it.cantidad }
    val hayEscasez: Boolean get() = demandaTotal > disponible
    val faltante: Int get() = (demandaTotal - disponible).coerceAtLeast(0)
}

/** Un reparto concreto propuesto por el niño. */
data class Allocation(val porPersonaje: Map<String, Int>) {

    init {
        porPersonaje.forEach { (id, n) ->
            require(n >= 0) { "Reparto negativo para '$id'" }
        }
    }

    val entregado: Int get() = porPersonaje.values.sum()

    fun para(personajeId: String): Int = porPersonaje[personajeId] ?: 0

    fun con(personajeId: String, cantidad: Int): Allocation =
        Allocation(porPersonaje + (personajeId to cantidad.coerceAtLeast(0)))

    companion object {
        val VACIO = Allocation(emptyMap())
    }
}

/** Valoración de un reparto. */
data class ScarcityResult(
    val valido: Boolean,
    val entregado: Int,
    val sobrante: Int,
    val cubiertos: List<String>,
    val parciales: List<String>,
    val sinNada: List<String>,
    val urgentesCubiertos: Boolean,
    val mensaje: String,
    val explicacion: String
)

// ---------------------------------------------------------------------------
// Decisiones con recursos limitados
// ---------------------------------------------------------------------------

/** Una cosa que se puede construir o conseguir gastando recursos. */
data class BuildOption(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val costo: Inventory,
    val obtiene: Inventory,
    val utilidad: Int
) {
    init { require(!costo.esVacio) { "Una opción debe costar algo" } }
}

/** "Tienes 10 de madera. ¿Una mesa o dos sillas?" */
data class BudgetCase(
    val titulo: String,
    val presupuesto: Inventory,
    val opciones: List<BuildOption>,
    val maxSelecciones: Int = 2
) {
    init {
        require(opciones.size >= 2) { "Hacen falta al menos dos alternativas" }
        require(maxSelecciones >= 1)
    }
}

/** Resultado de elegir un conjunto de opciones. */
data class BudgetResult(
    val alcanza: Boolean,
    val costoTotal: Inventory,
    val restante: Inventory?,
    val obtenido: Inventory,
    val renuncias: List<String>,
    val mensaje: String,
    val explicacion: String
)
