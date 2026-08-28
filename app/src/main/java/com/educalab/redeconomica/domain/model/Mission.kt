package com.educalab.redeconomica.domain.model

/** Los siete conceptos que recorre RedEconómica, en el orden en que se viven. */
enum class ConceptId(val titulo: String, val nivel: Int, val ideaCorta: String) {
    NECESIDADES("Necesidades", 1, "Cada persona necesita cosas distintas"),
    RECURSOS("Recursos", 2, "No siempre tenemos todo lo que necesitamos"),
    INTERCAMBIO("Intercambio", 3, "Puedo cambiar lo que me sobra por lo que me falta"),
    ESPECIALIZACION("Especialización", 4, "Concentrarse en una tarea produce más"),
    COOPERACION("Cooperación", 5, "Juntos llegamos donde solos no llegamos"),
    ELECCION("Elección y costo de oportunidad", 6, "Elegir algo es renunciar a otra cosa"),
    INTEGRACION("Todo junto", 7, "Una pequeña economía funcionando")
}

/** Estado visual de un módulo o misión. Nunca se expresa solo con color. */
enum class ModuleState(val etiqueta: String, val icono: String) {
    BLOQUEADO("Bloqueado", "candado"),
    DISPONIBLE("Disponible", "estrella_vacia"),
    INICIADO("Empezado", "reloj"),
    COMPLETADO("Completado", "check"),
    DOMINADO("Dominado", "corona")
}

/** Lo que se gana al terminar una misión. Siempre por acciones reales. */
data class MissionReward(
    val objetos: List<String> = emptyList(),
    val insigniaId: String? = null,
    val zonaDesbloqueada: ValleyPlace? = null,
    val sellos: Int = 1
)

/** Una misión del Valle: narrativa + escenarios + recompensa. */
data class MissionDef(
    val id: String,
    val numero: Int,
    val titulo: String,
    val lugar: ValleyPlace,
    val concepto: ConceptId,
    val narrativaInicio: String,
    val narrativaFinal: String,
    val objetivoVisible: String,
    val escenarios: List<String>,
    val recompensa: MissionReward,
    val requiereMision: String? = null
) {
    init {
        require(escenarios.isNotEmpty()) { "La misión $id no tiene escenarios" }
    }
}

/** Progreso guardado de una misión concreta. */
data class MissionProgress(
    val misionId: String,
    val estado: ModuleState,
    val escenariosCompletados: Set<String>,
    val intentosTotales: Int,
    val sinFallos: Boolean
) {
    fun porcentaje(total: Int): Int =
        if (total == 0) 0 else (escenariosCompletados.size * 100) / total
}
