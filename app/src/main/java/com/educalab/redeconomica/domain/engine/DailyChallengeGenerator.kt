package com.educalab.redeconomica.domain.engine

import com.educalab.redeconomica.domain.model.Scenario
import java.util.Random

/** El reto económico de hoy. Se calcula en el dispositivo, sin conexión. */
data class DailyChallenge(
    val diaIndice: Long,
    val escenarioId: String,
    val tipoEtiqueta: String,
    val titulo: String,
    val invitacion: String
)

/**
 * Generador del "Reto económico del día".
 *
 * Es determinista: el mismo día siempre propone el mismo reto, así que si el
 * niño cierra la app y vuelve, encuentra lo mismo. No usa Internet, no usa
 * relojes de servidor y no castiga por no jugar ayer.
 */
class DailyChallengeGenerator {

    fun indiceDeDia(millis: Long): Long = Math.floorDiv(millis, MILIS_POR_DIA)

    fun retoDe(diaIndice: Long, disponibles: List<Scenario>): DailyChallenge? {
        if (disponibles.isEmpty()) return null
        val ordenados = disponibles.sortedBy { it.id }
        val rnd = Random(diaIndice * 7919L + 13L)
        val elegido = ordenados[rnd.nextInt(ordenados.size)]
        val invitacion = INVITACIONES[(diaIndice.mod(INVITACIONES.size.toLong())).toInt()]
        return DailyChallenge(
            diaIndice = diaIndice,
            escenarioId = elegido.id,
            tipoEtiqueta = elegido.tipo.etiqueta,
            titulo = elegido.titulo,
            invitacion = invitacion
        )
    }

    private companion object {
        const val MILIS_POR_DIA = 86_400_000L
        val INVITACIONES = listOf(
            "Hoy el Valle tiene un problemilla. ¿Le echas una mano?",
            "Alguien en la plaza necesita algo. Pásate a ver.",
            "Un reto corto para empezar el día.",
            "En el mercado hay movimiento hoy.",
            "Hoy toca decidir. No hay prisa.",
            "El Valle se ha organizado mal. ¿Lo arreglas?",
            "Una idea nueva que probar."
        )
    }
}
