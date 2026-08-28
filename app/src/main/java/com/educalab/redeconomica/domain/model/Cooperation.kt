package com.educalab.redeconomica.domain.model

/**
 * Una etapa de un trabajo en equipo (por ejemplo: cultivar → moler → repartir).
 *
 * [rendimientoPorPersonaje] dice cuánto aporta cada habitante SI se le asigna
 * esta etapa. Un cero significa que esa persona no sirve para esa tarea, y el
 * niño lo descubre probando.
 */
data class CooperationStage(
    val id: String,
    val nombre: String,
    val orden: Int,
    val descripcion: String,
    val rendimientoPorPersonaje: Map<String, Int>
) {
    fun rendimientoDe(personajeId: String): Int = rendimientoPorPersonaje[personajeId] ?: 0
}

/** Reparto de tareas: personaje → etapa. Un personaje sin entrada no participa. */
data class CooperationPlan(val asignaciones: Map<String, String>) {

    fun con(personajeId: String, etapaId: String): CooperationPlan =
        CooperationPlan(asignaciones + (personajeId to etapaId))

    fun sin(personajeId: String): CooperationPlan =
        CooperationPlan(asignaciones - personajeId)

    fun etapaDe(personajeId: String): String? = asignaciones[personajeId]

    fun personajesEn(etapaId: String): List<String> =
        asignaciones.filterValues { it == etapaId }.keys.sorted()

    companion object {
        val VACIO = CooperationPlan(emptyMap())
    }
}

/** Resultado de un trabajo en equipo. */
data class CooperationOutcome(
    val capacidadPorEtapa: Map<String, Int>,
    val resultado: Int,
    val objetivo: Int,
    val completado: Boolean,
    val cuellosDeBotella: List<String>,
    val resultadoSinCooperar: Int,
    val mensaje: String,
    val explicacion: String
) {
    val mejoraPorCooperar: Int get() = resultado - resultadoSinCooperar
}
