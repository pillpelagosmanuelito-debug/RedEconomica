package com.educalab.redeconomica.domain.engine

import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.ResourceDef
import com.educalab.redeconomica.domain.model.TradeEvaluation

/** Cómo organiza el trabajo el Valle en un experimento. */
enum class LabMode(val etiqueta: String, val explicacion: String) {
    TODOS_DE_TODO("Todos hacen de todo", "Cada habitante reparte su turno entre todas sus tareas"),
    CADA_UNO_LO_SUYO("Cada uno se especializa", "Cada habitante dedica el turno a un solo producto")
}

/** Ajustes del Laboratorio del Valle. Todos los cambia el niño con sliders. */
data class LabConfig(
    val habitantes: Int = 3,
    val turnos: Int = 2,
    val modo: LabMode = LabMode.TODOS_DE_TODO,
    val permiteIntercambio: Boolean = false,
    val ponenEnComun: Boolean = false
) {
    init {
        require(habitantes in 2..6) { "El laboratorio admite entre 2 y 6 habitantes" }
        require(turnos in 1..6) { "El laboratorio admite entre 1 y 6 turnos" }
    }
}

/** Resultado medido de un experimento. */
data class LabRun(
    val config: LabConfig,
    val produccionTotal: Inventory,
    val valorTotal: Int,
    val intercambiosRealizados: Int,
    val necesidadesCubiertas: Int,
    val necesidadesTotales: Int,
    val detalle: List<String>,
    val resumen: String
) {
    val porcentajeNecesidades: Int
        get() = if (necesidadesTotales == 0) 0 else necesidadesCubiertas * 100 / necesidadesTotales
}

/** Dos experimentos, uno al lado del otro. */
data class LabComparison(
    val a: LabRun,
    val b: LabRun,
    val diferenciaValor: Int,
    val diferenciaNecesidades: Int,
    val conclusion: String
)

/**
 * Laboratorio del Valle.
 *
 * Aquí no se gana ni se pierde: se prueba. El niño cambia cuántos habitantes
 * hay, cuántos turnos trabajan, si se especializan, si pueden intercambiar y
 * si ponen los recursos en común, y ve el resultado medido de verdad — no un
 * texto inventado.
 */
class LabEngine(
    private val catalogo: Map<String, ResourceDef>,
    private val especializacion: SpecializationEngine = SpecializationEngine(catalogo),
    private val intercambio: TradeEngine = TradeEngine(catalogo)
) {

    fun ejecutar(config: LabConfig, base: List<EconomicCharacter>): LabRun {
        require(base.size >= config.habitantes) { "No hay tantos habitantes disponibles" }
        var equipo = base.take(config.habitantes).map { it.conInventario(Inventory.VACIO) }
        val detalle = mutableListOf<String>()

        val plan = when (config.modo) {
            LabMode.TODOS_DE_TODO -> especializacion.planDeTodo(equipo)
            LabMode.CADA_UNO_LO_SUYO -> especializacion.mejorPlan(equipo)
        }

        var produccion = Inventory.VACIO
        repeat(config.turnos) { turno ->
            val salida = especializacion.producir(equipo, plan)
            produccion = produccion.mas(salida.total)
            equipo = equipo.map { p ->
                p.conInventario(p.inventario.mas(salida.deQuien(p.id)))
            }
            detalle += "Turno ${turno + 1}: el Valle produce ${salida.total.descripcion(catalogo)}."
        }

        var intercambios = 0
        if (config.permiteIntercambio) {
            for (ronda in 1..RONDAS_DE_TRUEQUE) {
                var huboAlguno = false
                for (p in equipo.map { it.id }) {
                    val quien = equipo.first { it.id == p }
                    val otros = equipo.filter { it.id != p }
                    val oferta = intercambio.buscarIntercambios(quien, otros)
                        .minByOrNull { it.entrega.total } ?: continue
                    val receptor = equipo.first { it.id == oferta.receptorId }
                    val res = intercambio.evaluar(oferta, quien, receptor)
                    if (res is TradeEvaluation.Aceptado) {
                        equipo = equipo.map {
                            when (it.id) {
                                res.proponenteActualizado.id -> res.proponenteActualizado
                                res.receptorActualizado.id -> res.receptorActualizado
                                else -> it
                            }
                        }
                        intercambios++
                        huboAlguno = true
                    }
                }
                if (!huboAlguno) break
            }
            detalle += "Se hicieron $intercambios intercambios entre vecinos."
        }

        val necesidadesTotales = equipo.sumOf { it.necesidades.size }
        val cubiertas = if (config.ponenEnComun) {
            cubiertasEnComun(equipo)
        } else {
            equipo.sumOf { p ->
                p.necesidades.count { p.inventario.cantidad(it.recursoId) >= it.cantidad }
            }
        }
        if (config.ponenEnComun) {
            detalle += "Al poner los recursos en común, lo que le sobra a uno le sirve a otro."
        }

        val valor = produccion.valor(catalogo)
        val resumen = "Con ${config.habitantes} habitantes y ${config.turnos} turnos, " +
            "${config.modo.etiqueta.lowercase()}: el Valle produjo ${produccion.total} cosas " +
            "y cubrió $cubiertas de $necesidadesTotales necesidades."

        return LabRun(
            config = config,
            produccionTotal = produccion,
            valorTotal = valor,
            intercambiosRealizados = intercambios,
            necesidadesCubiertas = cubiertas,
            necesidadesTotales = necesidadesTotales,
            detalle = detalle,
            resumen = resumen
        )
    }

    fun comparar(a: LabRun, b: LabRun): LabComparison {
        val dv = b.valorTotal - a.valorTotal
        val dn = b.necesidadesCubiertas - a.necesidadesCubiertas
        val conclusion = when {
            dv > 0 && dn >= 0 -> "El experimento B consigue más cosas útiles y cubre igual o más necesidades."
            dv < 0 && dn <= 0 -> "El experimento A funcionó mejor esta vez."
            dn > 0 -> "B produce parecido, pero llega a más vecinos."
            dn < 0 -> "B produce parecido, pero deja a más gente sin lo que necesitaba."
            else -> "Los dos experimentos dan un resultado muy parecido."
        }
        return LabComparison(a, b, dv, dn, conclusion)
    }

    /** Reparto en común: se atiende primero a quien menos necesita, para llegar a más gente. */
    private fun cubiertasEnComun(equipo: List<EconomicCharacter>): Int {
        var almacen = equipo.fold(Inventory.VACIO) { acc, p -> acc.mas(p.inventario) }
        val pendientes = equipo.flatMap { p -> p.necesidades.map { p.id to it } }
            .sortedBy { it.second.cantidad }
        var cubiertas = 0
        for ((_, need) in pendientes) {
            val pedido = Inventory.of(need.recursoId to need.cantidad)
            val resto = almacen.menos(pedido)
            if (resto != null) {
                almacen = resto
                cubiertas++
            }
        }
        return cubiertas
    }

    private companion object {
        const val RONDAS_DE_TRUEQUE = 4
    }
}
