package com.educalab.redeconomica.domain.engine

import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.PlanComparison
import com.educalab.redeconomica.domain.model.ProductionLine
import com.educalab.redeconomica.domain.model.ProductionOutcome
import com.educalab.redeconomica.domain.model.ResourceDef
import com.educalab.redeconomica.domain.model.SpecializationPlan
import com.educalab.redeconomica.domain.model.WorkMode

/**
 * Motor de producción y especialización.
 *
 * Idea pedagógica: repartir el turno entre muchas tareas produce MENOS que
 * concentrarlo en una sola. Eso no se cuenta con un texto: se calcula y se
 * enseña con los dos resultados uno al lado del otro.
 *
 * Reglas de producción en un turno:
 *  - especializado en R  → produce `productividad[R]` unidades;
 *  - "hace de todo"      → reparte el turno entre los k productos que sabe
 *                          hacer, produciendo `productividad[i] / k` de cada
 *                          uno (división entera: el tiempo partido se pierde);
 *  - sin tarea           → no produce nada.
 */
class SpecializationEngine(private val catalogo: Map<String, ResourceDef>) {

    fun producir(
        personajes: List<EconomicCharacter>,
        plan: SpecializationPlan
    ): ProductionOutcome {
        val lineas = personajes.map { p -> lineaDe(p, plan) }
        val total = lineas.fold(Inventory.VACIO) { acc, l -> acc.mas(l.producido) }
        return ProductionOutcome(lineas, total)
    }

    private fun lineaDe(p: EconomicCharacter, plan: SpecializationPlan): ProductionLine =
        when (plan.modoDe(p.id)) {
            WorkMode.DESCANSA -> ProductionLine(
                p.id, p.nombre, WorkMode.DESCANSA, null, Inventory.VACIO,
                "${p.nombre} no tiene tarea asignada este turno."
            )

            WorkMode.DE_TODO -> {
                val posibles = p.oficiosPosibles()
                if (posibles.isEmpty()) {
                    ProductionLine(
                        p.id, p.nombre, WorkMode.DE_TODO, null, Inventory.VACIO,
                        "${p.nombre} todavía no sabe producir nada."
                    )
                } else {
                    val k = posibles.size
                    val mapa = posibles.associateWith { r -> p.produccionPorTurno(r) / k }
                    ProductionLine(
                        p.id, p.nombre, WorkMode.DE_TODO, null, Inventory.of(mapa),
                        "${p.nombre} reparte el turno entre $k tareas, así que de cada " +
                            "una saca menos."
                    )
                }
            }

            WorkMode.ESPECIALIZADO -> {
                val r = plan.asignaciones[p.id]!!
                val n = p.produccionPorTurno(r)
                val nombreR = nombre(r)
                if (n <= 0) {
                    ProductionLine(
                        p.id, p.nombre, WorkMode.ESPECIALIZADO, r, Inventory.VACIO,
                        "${p.nombre} no sabe producir $nombreR: este turno no consigue nada."
                    )
                } else {
                    ProductionLine(
                        p.id, p.nombre, WorkMode.ESPECIALIZADO, r, Inventory.of(r to n),
                        "${p.nombre} dedica todo el turno a $nombreR y consigue $n."
                    )
                }
            }
        }

    /** Plan en el que todo el mundo intenta hacer de todo. */
    fun planDeTodo(personajes: List<EconomicCharacter>): SpecializationPlan =
        SpecializationPlan(personajes.associate { it.id to null })

    /**
     * Mejor reparto encontrado.
     *
     * Se exploran todas las combinaciones cuando el escenario es pequeño
     * (lo habitual: 2-4 habitantes). Si el espacio de búsqueda creciera
     * demasiado se usa una heurística voraz por ventaja relativa, y se avisa
     * en el propio nombre del método que el resultado es "un buen plan", no
     * necesariamente único.
     */
    fun mejorPlan(
        personajes: List<EconomicCharacter>,
        objetivo: Inventory = Inventory.VACIO
    ): SpecializationPlan {
        val combos = combinaciones(personajes)
        if (combos == null) return planVorazPorVentaja(personajes)
        var mejor: SpecializationPlan = planDeTodo(personajes)
        var mejorPuntos = Int.MIN_VALUE
        for (plan in combos) {
            val out = producir(personajes, plan)
            val puntos = puntuar(out, objetivo)
            if (puntos > mejorPuntos) {
                mejorPuntos = puntos
                mejor = plan
            }
        }
        return mejor
    }

    /**
     * Todos los repartos que cumplen [objetivo]. Puede haber varios: un mismo
     * objetivo suele admitir más de una organización razonable del trabajo.
     */
    fun planesQueCumplen(
        personajes: List<EconomicCharacter>,
        objetivo: Inventory
    ): List<SpecializationPlan> {
        val combos = combinaciones(personajes) ?: return emptyList()
        return combos.filter { producir(personajes, it).cumple(objetivo) }
    }

    fun comparar(
        personajes: List<EconomicCharacter>,
        planA: SpecializationPlan,
        planB: SpecializationPlan
    ): PlanComparison {
        val a = producir(personajes, planA)
        val b = producir(personajes, planB)
        val valorA = a.total.valor(catalogo)
        val valorB = b.total.valor(catalogo)
        val recursos = (a.total.recursos + b.total.recursos).sorted()
        val dif = recursos.associateWith { b.total.cantidad(it) - a.total.cantidad(it) }
        val texto = when {
            valorB > valorA -> "Organizados así el Valle consigue más cosas útiles."
            valorB < valorA -> "Con este reparto el Valle consigue menos que antes."
            else -> "Las dos formas de trabajar dan un resultado parecido."
        }
        return PlanComparison(a, b, valorA, valorB, dif, texto)
    }

    // ---------------------------------------------------------------- interno

    /**
     * Enumera planes: cada habitante o se especializa en uno de sus productos,
     * o hace de todo. Devuelve null si hay demasiadas combinaciones.
     */
    private fun combinaciones(personajes: List<EconomicCharacter>): List<SpecializationPlan>? {
        val opcionesPorPersonaje = personajes.map { p ->
            val ops: List<String?> = p.oficiosPosibles() + listOf<String?>(null)
            ops
        }
        var tamano = 1L
        opcionesPorPersonaje.forEach {
            tamano *= it.size
            if (tamano > MAX_COMBINACIONES) return null
        }
        var acumulado = listOf<Map<String, String?>>(emptyMap())
        personajes.forEachIndexed { idx, p ->
            val nuevas = ArrayList<Map<String, String?>>(acumulado.size * opcionesPorPersonaje[idx].size)
            for (parcial in acumulado) {
                for (op in opcionesPorPersonaje[idx]) {
                    nuevas += parcial + (p.id to op)
                }
            }
            acumulado = nuevas
        }
        return acumulado.map { SpecializationPlan(it) }
    }

    private fun planVorazPorVentaja(personajes: List<EconomicCharacter>): SpecializationPlan {
        val maximos = mutableMapOf<String, Int>()
        personajes.forEach { p ->
            p.productividad.forEach { (r, n) ->
                maximos[r] = maxOf(maximos[r] ?: 0, n)
            }
        }
        val asign = personajes.associate { p ->
            val mejor = p.oficiosPosibles().maxByOrNull { r ->
                val techo = maximos[r] ?: 1
                if (techo == 0) 0.0 else p.produccionPorTurno(r).toDouble() / techo
            }
            p.id to mejor
        }
        return SpecializationPlan(asign)
    }

    private fun puntuar(out: ProductionOutcome, objetivo: Inventory): Int {
        val base = out.total.valor(catalogo)
        return if (objetivo.esVacio || out.cumple(objetivo)) base + BONUS_OBJETIVO else base
    }

    private fun nombre(recursoId: String) = catalogo[recursoId]?.plural ?: recursoId

    private companion object {
        const val MAX_COMBINACIONES = 4096L
        const val BONUS_OBJETIVO = 1000
    }
}
