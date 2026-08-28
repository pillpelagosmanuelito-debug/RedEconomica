package com.educalab.redeconomica.domain.engine

import com.educalab.redeconomica.domain.model.ComparativeAdvantageResult
import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.OpportunityCostResult
import com.educalab.redeconomica.domain.model.ProductionPoint
import com.educalab.redeconomica.domain.model.ResourceDef
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Motor de "lo que dejas de hacer".
 *
 * Antes de nombrar el costo de oportunidad, la app lo hace visible: si Ana
 * puede hacer 8 frutas O 4 panes, dedicar el turno a las frutas significa
 * quedarse sin 4 panes. El motor calcula esa renuncia y también quién debería
 * dedicarse a qué comparando cuánto pierde cada uno.
 */
class OpportunityCostEngine(private val catalogo: Map<String, ResourceDef> = emptyMap()) {

    /**
     * Cuánto de [recursoRenunciado] se pierde al producir [cantidadElegida] de
     * [recursoElegido].
     */
    fun costoDeOportunidad(
        personaje: EconomicCharacter,
        recursoElegido: String,
        recursoRenunciado: String,
        cantidadElegida: Int
    ): OpportunityCostResult {
        require(cantidadElegida >= 0) { "La cantidad elegida no puede ser negativa" }
        val pElegido = personaje.produccionPorTurno(recursoElegido)
        val pRenunciado = personaje.produccionPorTurno(recursoRenunciado)
        require(pElegido > 0) {
            "${personaje.nombre} no puede producir ${nombre(recursoElegido)}"
        }
        require(cantidadElegida <= pElegido) {
            "${personaje.nombre} no llega a $cantidadElegida ${nombre(recursoElegido)} en un turno"
        }

        val exacto = cantidadElegida.toDouble() * pRenunciado / pElegido
        val redondeado = exacto.roundToInt()
        val nombreE = nombre(recursoElegido)
        val nombreR = nombre(recursoRenunciado)

        val texto = "Producir $cantidadElegida $nombreE cuesta ${formatea(exacto)} $nombreR."
        val explicacion = if (pRenunciado == 0) {
            "${personaje.nombre} no sabe hacer $nombreR, así que no renuncia a nada."
        } else {
            "En un turno ${personaje.nombre} puede hacer $pElegido $nombreE o $pRenunciado $nombreR. " +
                "El tiempo es el mismo, así que cada $nombreE que hace le quita tiempo para $nombreR."
        }

        return OpportunityCostResult(
            recursoElegido = recursoElegido,
            cantidadElegida = cantidadElegida,
            recursoRenunciado = recursoRenunciado,
            cantidadRenunciadaExacta = exacto,
            cantidadRenunciadaRedondeada = redondeado,
            texto = texto,
            explicacion = explicacion
        )
    }

    /**
     * Todas las combinaciones que caben en un turno: la base del gráfico
     * "puedo hacer esto O aquello".
     */
    fun combinacionesPosibles(
        personaje: EconomicCharacter,
        recursoA: String,
        recursoB: String
    ): List<ProductionPoint> {
        val pa = personaje.produccionPorTurno(recursoA)
        val pb = personaje.produccionPorTurno(recursoB)
        if (pa <= 0) return listOf(ProductionPoint(0, pb))
        return (0..pa).map { x ->
            ProductionPoint(x, (pb * (pa - x)) / pa)
        }
    }

    /** Quién produce más de un recurso en el mismo tiempo. */
    fun ventajaAbsoluta(
        a: EconomicCharacter,
        b: EconomicCharacter,
        recursoId: String
    ): String? {
        val pa = a.produccionPorTurno(recursoId)
        val pb = b.produccionPorTurno(recursoId)
        return when {
            pa > pb -> a.id
            pb > pa -> b.id
            else -> null
        }
    }

    /**
     * Ventaja comparativa: no gana quien produce más, sino quien pierde menos
     * al dedicarse a ello.
     *
     * costo de [recursoUno] para X = (lo que X hace de recursoDos) / (lo que X
     * hace de recursoUno). Quien tenga el costo más bajo debería producir
     * [recursoUno].
     */
    fun ventajaComparativa(
        a: EconomicCharacter,
        b: EconomicCharacter,
        recursoUno: String,
        recursoDos: String
    ): ComparativeAdvantageResult {
        val a1 = a.produccionPorTurno(recursoUno)
        val a2 = a.produccionPorTurno(recursoDos)
        val b1 = b.produccionPorTurno(recursoUno)
        val b2 = b.produccionPorTurno(recursoDos)
        require(a1 > 0 && b1 > 0) { "Ambos deben poder producir ${nombre(recursoUno)}" }

        val costoA = a2.toDouble() / a1
        val costoB = b2.toDouble() / b1
        val empate = abs(costoA - costoB) < 1e-9

        val recA: String
        val recB: String
        when {
            empate -> { recA = recursoUno; recB = recursoDos }
            costoA < costoB -> { recA = recursoUno; recB = recursoDos }
            else -> { recA = recursoDos; recB = recursoUno }
        }

        val n1 = nombre(recursoUno)
        val n2 = nombre(recursoDos)
        val explicacion = if (empate) {
            "Los dos pierden lo mismo al cambiar de tarea, así que cualquiera de los " +
                "dos repartos funciona igual de bien."
        } else {
            "Cuando ${a.nombre} hace $n1, deja de hacer ${formatea(costoA)} $n2 por cada uno. " +
                "A ${b.nombre} le cuesta ${formatea(costoB)}. " +
                "Conviene que ${if (costoA < costoB) a.nombre else b.nombre} se dedique a $n1: " +
                "es quien menos deja de hacer."
        }

        return ComparativeAdvantageResult(
            personajeAId = a.id,
            personajeBId = b.id,
            recursoUno = recursoUno,
            recursoDos = recursoDos,
            costoAEnUno = costoA,
            costoBEnUno = costoB,
            recomendadoParaA = recA,
            recomendadoParaB = recB,
            empate = empate,
            explicacion = explicacion
        )
    }

    private fun nombre(recursoId: String) = catalogo[recursoId]?.plural ?: recursoId

    /** Números amables para 8-12 años: sin decimales cuando no hacen falta. */
    private fun formatea(valor: Double): String {
        val redondeado = (valor * 100).roundToInt() / 100.0
        return if (abs(redondeado - redondeado.roundToInt()) < 1e-9) {
            redondeado.roundToInt().toString()
        } else {
            redondeado.toString().replace('.', ',')
        }
    }
}
