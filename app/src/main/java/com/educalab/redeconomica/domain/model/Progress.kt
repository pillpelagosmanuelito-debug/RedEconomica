package com.educalab.redeconomica.domain.model

/** Qué acción real desbloquea una insignia. */
enum class BadgeRule {
    PRIMER_INTERCAMBIO,
    INTERCAMBIOS_ACEPTADOS,
    ESPECIALIZACIONES_PROBADAS,
    COOPERACIONES_COMPLETADAS,
    REPARTOS_RESUELTOS,
    DECISIONES_TOMADAS,
    MISIONES_COMPLETADAS,
    CONCEPTOS_DESCUBIERTOS,
    OBJETOS_COLECCIONADOS,
    EXPERIMENTOS_REALIZADOS
}

/**
 * Insignia del Valle.
 *
 * Ninguna insignia habla de ser listo: todas describen algo que el niño HIZO.
 */
data class Badge(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val regla: BadgeRule,
    val meta: Int,
    val arteId: String
) {
    init { require(meta >= 1) { "La insignia $id necesita una meta alcanzable" } }
}

/** Insignia ya conseguida. */
data class UserBadge(val insigniaId: String, val fechaMillis: Long)

/** Objeto del Almacén del Valle: se desbloquea usándolo, no comprándolo. */
data class CollectionItem(
    val id: String,
    val nombre: String,
    val familia: ResourceType,
    val descripcion: String,
    val comoSeConsigue: String,
    val recursoId: String?
)

/** Entrada del Diccionario del Valle. */
data class GlossaryEntry(
    val id: String,
    val termino: String,
    val definicionInfantil: String,
    val ejemplo: String,
    val conceptoId: ConceptId,
    val arteId: String,
    val miniActividad: String?
)

/** Perfil local del niño. Sin nombre real, sin correo, sin nada personal. */
data class UserProfile(
    val alias: String,
    val avatarId: String,
    val onboardingHecho: Boolean,
    val sonidoActivo: Boolean,
    val vibracionActiva: Boolean,
    val textoGrande: Boolean
)

/** Resumen de progreso que alimenta el mapa del Valle y el perfil. */
data class ProgressSummary(
    val sellos: Int,
    val nivel: Int,
    val nivelTitulo: String,
    val sellosParaSiguienteNivel: Int,
    val misionesCompletadas: Int,
    val misionesTotales: Int,
    val intercambiosAceptados: Int,
    val especializacionesProbadas: Int,
    val cooperacionesCompletadas: Int,
    val repartosResueltos: Int,
    val decisionesTomadas: Int,
    val experimentosRealizados: Int,
    val conceptosDescubiertos: Int,
    val objetosDesbloqueados: Int,
    val insigniasConseguidas: Int,
    val insigniasTotales: Int
)

/** Contadores crudos de acciones reales. De aquí sale todo lo demás. */
data class ActionCounters(
    val intercambiosAceptados: Int = 0,
    val intercambiosPropuestos: Int = 0,
    val especializacionesProbadas: Int = 0,
    val cooperacionesCompletadas: Int = 0,
    val repartosResueltos: Int = 0,
    val decisionesTomadas: Int = 0,
    val cadenasOrdenadas: Int = 0,
    val experimentosRealizados: Int = 0,
    val misionesCompletadas: Int = 0,
    val conceptosDescubiertos: Int = 0,
    val objetosDesbloqueados: Int = 0
)
