package com.educalab.redeconomica.domain.model

/** Cómo trabaja un habitante durante un turno. */
enum class WorkMode(val etiqueta: String, val explicacion: String) {
    ESPECIALIZADO("Se especializa", "Dedica todo el turno a un solo producto"),
    DE_TODO("Hace de todo", "Reparte el turno entre todo lo que sabe hacer"),
    DESCANSA("Sin tarea", "Nadie le ha asignado trabajo")
}

/**
 * Plan de trabajo del Valle.
 *
 * `asignaciones[idPersonaje] = idRecurso` → se especializa en ese recurso.
 * `asignaciones[idPersonaje] = null`      → hace un poco de todo.
 * Si un personaje no aparece en el mapa, ese turno no produce.
 */
data class SpecializationPlan(val asignaciones: Map<String, String?>) {

    fun modoDe(personajeId: String): WorkMode = when {
        !asignaciones.containsKey(personajeId) -> WorkMode.DESCANSA
        asignaciones[personajeId] == null -> WorkMode.DE_TODO
        else -> WorkMode.ESPECIALIZADO
    }

    fun con(personajeId: String, recursoId: String?): SpecializationPlan =
        SpecializationPlan(asignaciones + (personajeId to recursoId))

    fun sin(personajeId: String): SpecializationPlan =
        SpecializationPlan(asignaciones - personajeId)

    companion object {
        val VACIO = SpecializationPlan(emptyMap())
    }
}

/** Lo que produjo un habitante concreto en un turno. */
data class ProductionLine(
    val personajeId: String,
    val nombre: String,
    val modo: WorkMode,
    val recursoElegido: String?,
    val producido: Inventory,
    val comentario: String
)

/** Resultado completo de ejecutar un plan de producción. */
data class ProductionOutcome(
    val lineas: List<ProductionLine>,
    val total: Inventory
) {
    fun deQuien(personajeId: String): Inventory =
        lineas.firstOrNull { it.personajeId == personajeId }?.producido ?: Inventory.VACIO

    fun cumple(objetivo: Inventory): Boolean = total.contiene(objetivo)

    fun faltaPara(objetivo: Inventory): Inventory {
        val falta = objetivo.contenido.mapValues { (id, n) ->
            (n - total.cantidad(id)).coerceAtLeast(0)
        }
        return Inventory.of(falta)
    }
}

/** Comparación entre dos formas de organizar el trabajo. */
data class PlanComparison(
    val resultadoA: ProductionOutcome,
    val resultadoB: ProductionOutcome,
    val valorA: Int,
    val valorB: Int,
    val diferenciaPorRecurso: Map<String, Int>,
    val explicacion: String
) {
    val hayEmpate: Boolean get() = valorA == valorB
    val ganaB: Boolean get() = valorB > valorA
}
