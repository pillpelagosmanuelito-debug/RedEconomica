package com.educalab.redeconomica.domain.engine

import com.educalab.redeconomica.domain.model.Allocation
import com.educalab.redeconomica.domain.model.BudgetCase
import com.educalab.redeconomica.domain.model.BudgetResult
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.ResourceDef
import com.educalab.redeconomica.domain.model.ScarcityCase
import com.educalab.redeconomica.domain.model.ScarcityResult
import com.educalab.redeconomica.domain.model.Urgency

/**
 * Motor de escasez y de decisiones con recursos limitados.
 *
 * Escasez no se define: se reparte. El niño ve que 3 panes no llegan para 5
 * vecinos y tiene que decidir. Un reparto es VÁLIDO si no inventa recursos que
 * no existen y si atiende a quien lo necesita con urgencia alta; casi siempre
 * hay más de un reparto válido, y el motor los reconoce todos.
 */
class ScarcityEngine(private val catalogo: Map<String, ResourceDef> = emptyMap()) {

    fun evaluar(caso: ScarcityCase, reparto: Allocation): ScarcityResult {
        val conocidos = caso.demandas.map { it.personajeId }.toSet()
        val intrusos = reparto.porPersonaje.keys - conocidos
        if (intrusos.isNotEmpty()) {
            return ScarcityResult(
                valido = false,
                entregado = reparto.entregado,
                sobrante = caso.disponible - reparto.entregado,
                cubiertos = emptyList(), parciales = emptyList(), sinNada = emptyList(),
                urgentesCubiertos = false,
                mensaje = "Ese reparto incluye a alguien que no está pidiendo nada.",
                explicacion = "Reparte solo entre quienes han pedido ${nombre(caso.recursoId)}."
            )
        }

        val excesos = caso.demandas.filter { reparto.para(it.personajeId) > it.cantidad }
        if (reparto.entregado > caso.disponible) {
            return ScarcityResult(
                valido = false,
                entregado = reparto.entregado,
                sobrante = 0,
                cubiertos = emptyList(), parciales = emptyList(), sinNada = emptyList(),
                urgentesCubiertos = false,
                mensaje = "Estás repartiendo ${reparto.entregado} y solo hay ${caso.disponible}.",
                explicacion = "No se puede repartir lo que no existe. Eso es la escasez: " +
                    "hay menos de lo que hace falta."
            )
        }

        val cubiertos = caso.demandas.filter { reparto.para(it.personajeId) >= it.cantidad }
        val parciales = caso.demandas.filter {
            val n = reparto.para(it.personajeId); n in 1 until it.cantidad
        }
        val sinNada = caso.demandas.filter { reparto.para(it.personajeId) == 0 }
        val urgentes = caso.demandas.filter { it.urgencia == Urgency.ALTA }
        val urgentesOk = urgentes.all { reparto.para(it.personajeId) >= it.cantidad }
        val sobrante = caso.disponible - reparto.entregado

        val valido = urgentesOk && reparto.entregado <= caso.disponible
        val mensaje = when {
            !urgentesOk -> "Alguien que lo necesitaba mucho se ha quedado sin nada."
            excesos.isNotEmpty() -> "Reparto aceptado, aunque a alguien le has dado de más."
            sobrante > 0 -> "Reparto aceptado. Todavía sobran $sobrante."
            else -> "Buen reparto: has usado justo lo que había."
        }
        val explicacion = buildString {
            append("Había ${caso.disponible} y se pedían ${caso.demandaTotal}. ")
            if (caso.hayEscasez) {
                append("Faltaban ${caso.faltante}: por eso alguien tenía que quedarse con menos. ")
            }
            if (cubiertos.isNotEmpty()) {
                append("Quedan cubiertos: ${cubiertos.joinToString(", ") { it.nombre }}. ")
            }
            if (parciales.isNotEmpty()) {
                append("A medias: ${parciales.joinToString(", ") { it.nombre }}. ")
            }
            if (sinNada.isNotEmpty()) {
                append("Sin nada: ${sinNada.joinToString(", ") { it.nombre }}.")
            }
        }.trim()

        return ScarcityResult(
            valido = valido,
            entregado = reparto.entregado,
            sobrante = sobrante,
            cubiertos = cubiertos.map { it.personajeId },
            parciales = parciales.map { it.personajeId },
            sinNada = sinNada.map { it.personajeId },
            urgentesCubiertos = urgentesOk,
            mensaje = mensaje,
            explicacion = explicacion
        )
    }

    /**
     * Todos los repartos válidos del caso. Puede haber muchos: se limita el
     * espacio de búsqueda para no bloquear la interfaz.
     */
    fun repartosValidos(caso: ScarcityCase, maximo: Int = 200): List<Allocation> {
        var tamano = 1L
        caso.demandas.forEach {
            tamano *= (it.cantidad + 1)
            if (tamano > MAX_COMBINACIONES) return emptyList()
        }
        var acumulado = listOf<Map<String, Int>>(emptyMap())
        caso.demandas.forEach { d ->
            val nuevas = ArrayList<Map<String, Int>>()
            for (parcial in acumulado) {
                for (n in 0..d.cantidad) {
                    if (parcial.values.sum() + n <= caso.disponible) {
                        nuevas += parcial + (d.personajeId to n)
                    }
                }
            }
            acumulado = nuevas
        }
        return acumulado
            .map { Allocation(it) }
            .filter { evaluar(caso, it).valido }
            .take(maximo)
    }

    /** ¿Se puede contentar a todo el mundo? Si no, hay escasez de verdad. */
    fun sePuedeContentarATodos(caso: ScarcityCase): Boolean = !caso.hayEscasez

    // ------------------------------------------------------------------------
    // Decisiones con recursos limitados
    // ------------------------------------------------------------------------

    fun evaluarDecision(caso: BudgetCase, seleccion: List<String>): BudgetResult {
        val elegidas = seleccion.mapNotNull { id -> caso.opciones.firstOrNull { it.id == id } }
        if (elegidas.isEmpty()) {
            return BudgetResult(
                alcanza = false,
                costoTotal = Inventory.VACIO,
                restante = caso.presupuesto,
                obtenido = Inventory.VACIO,
                renuncias = caso.opciones.map { it.nombre },
                mensaje = "Todavía no has elegido nada.",
                explicacion = "Con ${caso.presupuesto.descripcion(catalogo)} no se puede " +
                    "hacer todo: hay que escoger."
            )
        }
        if (elegidas.size > caso.maxSelecciones) {
            return BudgetResult(
                alcanza = false,
                costoTotal = elegidas.fold(Inventory.VACIO) { a, o -> a.mas(o.costo) },
                restante = null,
                obtenido = Inventory.VACIO,
                renuncias = emptyList(),
                mensaje = "Has elegido demasiadas cosas.",
                explicacion = "Como mucho puedes elegir ${caso.maxSelecciones}."
            )
        }

        val costo = elegidas.fold(Inventory.VACIO) { a, o -> a.mas(o.costo) }
        val restante = caso.presupuesto.menos(costo)
        val renuncias = caso.opciones.filter { it.id !in seleccion }.map { it.nombre }

        if (restante == null) {
            return BudgetResult(
                alcanza = false,
                costoTotal = costo,
                restante = null,
                obtenido = Inventory.VACIO,
                renuncias = renuncias,
                mensaje = "No te alcanza para eso.",
                explicacion = "Tienes ${caso.presupuesto.descripcion(catalogo)} y eso cuesta " +
                    "${costo.descripcion(catalogo)}."
            )
        }

        val obtenido = elegidas.fold(Inventory.VACIO) { a, o -> a.mas(o.obtiene) }
        return BudgetResult(
            alcanza = true,
            costoTotal = costo,
            restante = restante,
            obtenido = obtenido,
            renuncias = renuncias,
            mensaje = "Decisión tomada: consigues ${obtenido.descripcion(catalogo)}.",
            explicacion = if (renuncias.isEmpty())
                "Te ha alcanzado para todo. Te quedan ${restante.descripcion(catalogo)}."
            else
                "Al elegir esto has renunciado a: ${renuncias.joinToString(", ")}. " +
                    "Te quedan ${restante.descripcion(catalogo)}."
        )
    }

    /** Combinaciones de opciones que caben en el presupuesto. */
    fun decisionesPosibles(caso: BudgetCase): List<List<String>> {
        val ids = caso.opciones.map { it.id }
        val resultado = mutableListOf<List<String>>()
        val n = ids.size
        val limite = 1 shl n
        for (mascara in 1 until limite) {
            val sel = ids.filterIndexed { i, _ -> (mascara shr i) and 1 == 1 }
            if (sel.size > caso.maxSelecciones) continue
            if (evaluarDecision(caso, sel).alcanza) resultado += sel
        }
        return resultado.sortedBy { it.size }
    }

    private fun nombre(recursoId: String) = catalogo[recursoId]?.plural ?: recursoId

    private companion object {
        const val MAX_COMBINACIONES = 60_000L
    }
}
