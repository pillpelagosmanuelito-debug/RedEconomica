package com.educalab.redeconomica.domain.engine

import com.educalab.redeconomica.domain.model.ActionCounters
import com.educalab.redeconomica.domain.model.Badge
import com.educalab.redeconomica.domain.model.BadgeRule
import com.educalab.redeconomica.domain.model.MissionDef
import com.educalab.redeconomica.domain.model.MissionProgress
import com.educalab.redeconomica.domain.model.ModuleState
import com.educalab.redeconomica.domain.model.ProgressSummary

/**
 * Motor de progreso e insignias.
 *
 * Todo lo que se muestra en el perfil sale de contadores de acciones REALES
 * guardadas en la base de datos. No existe ningún "porcentaje de inteligencia
 * económica": el progreso cuenta lo que el niño hizo, no lo que vale.
 */
class ProgressEngine {

    /** Sellos del Valle: la moneda de progreso, ganada por hacer cosas. */
    fun sellos(c: ActionCounters): Int =
        c.intercambiosAceptados * 2 +
            c.especializacionesProbadas * 2 +
            c.cooperacionesCompletadas * 3 +
            c.repartosResueltos * 2 +
            c.decisionesTomadas * 2 +
            c.cadenasOrdenadas * 2 +
            c.experimentosRealizados +
            c.misionesCompletadas * 5 +
            c.conceptosDescubiertos * 3

    fun nivel(sellos: Int): Int = when {
        sellos < 10 -> 1
        sellos < 25 -> 2
        sellos < 45 -> 3
        sellos < 70 -> 4
        sellos < 100 -> 5
        sellos < 140 -> 6
        else -> 7
    }

    fun tituloNivel(nivel: Int): String = when (nivel) {
        1 -> "Vecino nuevo"
        2 -> "Aprendiz del mercado"
        3 -> "Buen intercambiador"
        4 -> "Especialista del Valle"
        5 -> "Organizador de equipos"
        6 -> "Cabeza de la cooperativa"
        else -> "Guía del Valle"
    }

    fun sellosParaSiguienteNivel(sellos: Int): Int {
        val topes = listOf(10, 25, 45, 70, 100, 140)
        val siguiente = topes.firstOrNull { it > sellos } ?: return 0
        return siguiente - sellos
    }

    fun resumen(
        contadores: ActionCounters,
        misionesTotales: Int,
        insigniasConseguidas: Int,
        insigniasTotales: Int
    ): ProgressSummary {
        val s = sellos(contadores)
        val n = nivel(s)
        return ProgressSummary(
            sellos = s,
            nivel = n,
            nivelTitulo = tituloNivel(n),
            sellosParaSiguienteNivel = sellosParaSiguienteNivel(s),
            misionesCompletadas = contadores.misionesCompletadas,
            misionesTotales = misionesTotales,
            intercambiosAceptados = contadores.intercambiosAceptados,
            especializacionesProbadas = contadores.especializacionesProbadas,
            cooperacionesCompletadas = contadores.cooperacionesCompletadas,
            repartosResueltos = contadores.repartosResueltos,
            decisionesTomadas = contadores.decisionesTomadas,
            experimentosRealizados = contadores.experimentosRealizados,
            conceptosDescubiertos = contadores.conceptosDescubiertos,
            objetosDesbloqueados = contadores.objetosDesbloqueados,
            insigniasConseguidas = insigniasConseguidas,
            insigniasTotales = insigniasTotales
        )
    }

    /**
     * Estado de una misión.
     *
     * Una misión queda DOMINADA cuando se completó sin ningún intento fallido:
     * es un reconocimiento a haberlo pensado, no una nota.
     */
    fun estadoDeMision(
        mision: MissionDef,
        progreso: MissionProgress?,
        completadas: Set<String>
    ): ModuleState {
        val requisito = mision.requiereMision
        if (requisito != null && requisito !in completadas) return ModuleState.BLOQUEADO
        if (progreso == null || progreso.escenariosCompletados.isEmpty()) return ModuleState.DISPONIBLE
        val todos = progreso.escenariosCompletados.containsAll(mision.escenarios)
        return when {
            todos && progreso.sinFallos -> ModuleState.DOMINADO
            todos -> ModuleState.COMPLETADO
            else -> ModuleState.INICIADO
        }
    }

    /** Insignias que ya se han ganado con los contadores actuales. */
    fun insigniasGanadas(insignias: List<Badge>, c: ActionCounters): List<Badge> =
        insignias.filter { valorDe(it.regla, c) >= it.meta }

    /** Cuánto llevas de una insignia concreta: se muestra como "3 de 5". */
    fun avanceDe(insignia: Badge, c: ActionCounters): Int =
        valorDe(insignia.regla, c).coerceAtMost(insignia.meta)

    private fun valorDe(regla: BadgeRule, c: ActionCounters): Int = when (regla) {
        BadgeRule.PRIMER_INTERCAMBIO -> c.intercambiosAceptados
        BadgeRule.INTERCAMBIOS_ACEPTADOS -> c.intercambiosAceptados
        BadgeRule.ESPECIALIZACIONES_PROBADAS -> c.especializacionesProbadas
        BadgeRule.COOPERACIONES_COMPLETADAS -> c.cooperacionesCompletadas
        BadgeRule.REPARTOS_RESUELTOS -> c.repartosResueltos
        BadgeRule.DECISIONES_TOMADAS -> c.decisionesTomadas
        BadgeRule.MISIONES_COMPLETADAS -> c.misionesCompletadas
        BadgeRule.CONCEPTOS_DESCUBIERTOS -> c.conceptosDescubiertos
        BadgeRule.OBJETOS_COLECCIONADOS -> c.objetosDesbloqueados
        BadgeRule.EXPERIMENTOS_REALIZADOS -> c.experimentosRealizados
    }
}
