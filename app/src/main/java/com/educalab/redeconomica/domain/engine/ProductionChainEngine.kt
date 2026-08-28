package com.educalab.redeconomica.domain.engine

import com.educalab.redeconomica.domain.model.ChainDef
import com.educalab.redeconomica.domain.model.ChainResult
import com.educalab.redeconomica.domain.model.ChainStep

/**
 * Motor de cadenas de producción.
 *
 * El niño arrastra los pasos hasta ponerlos en orden. El motor no dice
 * "incorrecto": dice en qué punto se rompe la cadena y por qué ese paso no
 * puede ir antes.
 */
class ProductionChainEngine {

    fun evaluar(cadena: ChainDef, ordenPropuesto: List<String>): ChainResult {
        val correcto = cadena.ordenCorrecto
        if (ordenPropuesto.size != correcto.size ||
            ordenPropuesto.toSet() != correcto.toSet()
        ) {
            return ChainResult(
                correcto = false,
                primerErrorEn = -1,
                aciertosSeguidos = 0,
                mensaje = "Falta colocar algún paso.",
                explicacion = "La cadena tiene ${correcto.size} pasos y todos tienen su sitio."
            )
        }

        var aciertos = 0
        while (aciertos < correcto.size && ordenPropuesto[aciertos] == correcto[aciertos]) {
            aciertos++
        }

        if (aciertos == correcto.size) {
            return ChainResult(
                correcto = true,
                primerErrorEn = -1,
                aciertosSeguidos = aciertos,
                mensaje = "¡Cadena completa! El producto llega hasta quien lo necesitaba.",
                explicacion = cadena.moraleja
            )
        }

        val pasoMal = cadena.pasos.first { it.id == ordenPropuesto[aciertos] }
        val pasoQueTocaba = cadena.pasos.first { it.id == correcto[aciertos] }
        return ChainResult(
            correcto = false,
            primerErrorEn = aciertos,
            aciertosSeguidos = aciertos,
            mensaje = "La cadena se corta en el paso ${aciertos + 1}.",
            explicacion = "\"${pasoMal.nombre}\" todavía no puede pasar: " +
                "antes hace falta \"${pasoQueTocaba.nombre}\" (${pasoQueTocaba.etapa.etiqueta})."
        )
    }

    /** Baraja los pasos de forma reproducible para un escenario dado. */
    fun barajar(cadena: ChainDef, semilla: Long): List<ChainStep> {
        val lista = cadena.pasos.toMutableList()
        val rnd = java.util.Random(semilla)
        for (i in lista.indices.reversed()) {
            val j = rnd.nextInt(i + 1)
            val tmp = lista[i]; lista[i] = lista[j]; lista[j] = tmp
        }
        // Evita entregar la cadena ya resuelta.
        if (lista.map { it.id } == cadena.ordenCorrecto && lista.size > 1) {
            val tmp = lista[0]; lista[0] = lista[1]; lista[1] = tmp
        }
        return lista
    }
}
