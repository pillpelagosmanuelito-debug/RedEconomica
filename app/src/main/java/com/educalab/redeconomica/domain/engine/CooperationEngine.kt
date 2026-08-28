package com.educalab.redeconomica.domain.engine

import com.educalab.redeconomica.domain.model.CooperationOutcome
import com.educalab.redeconomica.domain.model.CooperationPlan
import com.educalab.redeconomica.domain.model.CooperationStage
import com.educalab.redeconomica.domain.model.EconomicCharacter

/**
 * Motor de cooperación.
 *
 * Un trabajo del Valle es una CADENA de etapas. Lo que sale al final no es la
 * suma de lo que hace cada uno: es lo que permite la etapa más floja. Si nadie
 * transporta, da igual cuánto se cultive.
 *
 *   resultado = min( capacidad de cada etapa )
 *   capacidad(etapa) = suma del rendimiento de quienes trabajan en ella
 *
 * Trabajando por separado, cada habitante tiene que hacer TODAS las etapas él
 * solo, así que su producción individual es el mínimo de sus rendimientos.
 * Sumando esos mínimos casi siempre sale menos que coordinándose: ahí está la
 * lección, y es un número, no un eslogan.
 */
class CooperationEngine {

    fun ejecutar(
        personajes: List<EconomicCharacter>,
        etapas: List<CooperationStage>,
        plan: CooperationPlan,
        objetivo: Int
    ): CooperationOutcome {
        require(etapas.isNotEmpty()) { "Un trabajo en equipo necesita etapas" }
        require(objetivo > 0) { "El objetivo debe ser positivo" }

        val ordenadas = etapas.sortedBy { it.orden }
        val capacidades = ordenadas.associate { etapa ->
            etapa.id to plan.personajesEn(etapa.id).sumOf { etapa.rendimientoDe(it) }
        }
        val resultado = capacidades.values.minOrNull() ?: 0
        val cuellos = capacidades.filterValues { it == resultado }.keys.sorted()
        val solos = resultadoSinCooperar(personajes, ordenadas)
        val completado = resultado >= objetivo

        val nombresCuello = cuellos.mapNotNull { id -> ordenadas.firstOrNull { it.id == id }?.nombre }
        val mensaje = when {
            resultado == 0 -> "El trabajo se queda parado: hay una etapa sin nadie."
            completado -> "¡Pedido completado! Cada uno hizo una parte y entre todos salió."
            else -> "Falta poco: se han conseguido $resultado de $objetivo."
        }
        val explicacion = when {
            resultado == 0 ->
                "La cadena se rompe en: ${nombresCuello.joinToString(", ")}. " +
                    "Si una etapa no tiene a nadie, lo que hacen las demás no llega al final."
            completado ->
                "La etapa más justa fue ${nombresCuello.joinToString(", ")}. " +
                    "Por separado el equipo solo habría conseguido $solos; " +
                    "repartiendo el trabajo consiguió $resultado."
            else ->
                "La etapa que frena todo es ${nombresCuello.joinToString(", ")}. " +
                    "Prueba a mover a alguien allí y mira qué pasa."
        }

        return CooperationOutcome(
            capacidadPorEtapa = capacidades,
            resultado = resultado,
            objetivo = objetivo,
            completado = completado,
            cuellosDeBotella = cuellos,
            resultadoSinCooperar = solos,
            mensaje = mensaje,
            explicacion = explicacion
        )
    }

    /** Lo que conseguirían si cada uno hiciera el trabajo entero por su cuenta. */
    fun resultadoSinCooperar(
        personajes: List<EconomicCharacter>,
        etapas: List<CooperationStage>
    ): Int = personajes.sumOf { p ->
        etapas.minOfOrNull { it.rendimientoDe(p.id) } ?: 0
    }

    /**
     * Todos los repartos que alcanzan el objetivo.
     *
     * Sirve para reconocer que un desafío puede tener VARIAS soluciones
     * correctas, y para que las pruebas lo comprueben de verdad.
     */
    fun planesQueCumplen(
        personajes: List<EconomicCharacter>,
        etapas: List<CooperationStage>,
        objetivo: Int
    ): List<CooperationPlan> {
        val ids = etapas.map { it.id }
        var tamano = 1L
        repeat(personajes.size) {
            tamano *= ids.size
            if (tamano > MAX_COMBINACIONES) return emptyList()
        }
        var acumulado = listOf<Map<String, String>>(emptyMap())
        personajes.forEach { p ->
            val nuevas = ArrayList<Map<String, String>>(acumulado.size * ids.size)
            for (parcial in acumulado) for (e in ids) nuevas += parcial + (p.id to e)
            acumulado = nuevas
        }
        return acumulado
            .map { CooperationPlan(it) }
            .filter { ejecutar(personajes, etapas, it, objetivo).completado }
    }

    private companion object {
        const val MAX_COMBINACIONES = 20_000L
    }
}
