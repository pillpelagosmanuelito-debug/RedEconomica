package com.educalab.redeconomica.domain

import com.educalab.redeconomica.data.seed.SeedResources
import com.educalab.redeconomica.domain.engine.ScarcityEngine
import com.educalab.redeconomica.domain.model.Allocation
import com.educalab.redeconomica.domain.model.BudgetCase
import com.educalab.redeconomica.domain.model.BuildOption
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.ScarcityCase
import com.educalab.redeconomica.domain.model.ScarcityDemand
import com.educalab.redeconomica.domain.model.Urgency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Escasez y decisiones con recursos limitados. */
class ScarcityEngineTest {

    private val motor = ScarcityEngine(SeedResources.PORID)

    private val casoPan = ScarcityCase(
        recursoId = "pan",
        disponible = 3,
        demandas = listOf(
            ScarcityDemand("lia", "Lía", 2, Urgency.ALTA, "-"),
            ScarcityDemand("bruno", "Bruno", 1, Urgency.ALTA, "-"),
            ScarcityDemand("nina", "Nina", 2, Urgency.MEDIA, "-")
        )
    )

    @Test
    fun `hay escasez cuando se pide mas de lo que hay`() {
        assertTrue(casoPan.hayEscasez)
        assertEquals(5, casoPan.demandaTotal)
        assertEquals(2, casoPan.faltante)
        assertFalse(motor.sePuedeContentarATodos(casoPan))
    }

    @Test
    fun `atender las urgencias altas es un reparto valido`() {
        val reparto = Allocation(mapOf("lia" to 2, "bruno" to 1, "nina" to 0))
        val res = motor.evaluar(casoPan, reparto)
        assertTrue(res.valido)
        assertTrue(res.urgentesCubiertos)
        assertEquals(0, res.sobrante)
    }

    @Test
    fun `dejar sin nada a quien lo necesita ya no vale`() {
        val reparto = Allocation(mapOf("nina" to 2, "bruno" to 1))
        val res = motor.evaluar(casoPan, reparto)
        assertFalse(res.valido)
        assertFalse(res.urgentesCubiertos)
    }

    @Test
    fun `no se puede repartir mas de lo que existe`() {
        val reparto = Allocation(mapOf("lia" to 2, "bruno" to 1, "nina" to 2))
        val res = motor.evaluar(casoPan, reparto)
        assertFalse(res.valido)
        assertTrue(res.explicacion.contains("escasez"))
    }

    @Test
    fun `repartir a alguien que no ha pedido nada no vale`() {
        val res = motor.evaluar(casoPan, Allocation(mapOf("emi" to 1)))
        assertFalse(res.valido)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un reparto negativo no se puede ni construir`() {
        Allocation(mapOf("lia" to -2))
    }

    @Test
    fun `cuando sobra margen hay varios repartos validos`() {
        val holgado = ScarcityCase(
            recursoId = "herramienta",
            disponible = 4,
            demandas = listOf(
                ScarcityDemand("bruno", "Bruno", 2, Urgency.ALTA, "-"),
                ScarcityDemand("nina", "Nina", 1, Urgency.MEDIA, "-"),
                ScarcityDemand("emi", "Emi", 1, Urgency.MEDIA, "-"),
                ScarcityDemand("rita", "Rita", 2, Urgency.MEDIA, "-")
            )
        )
        val validos = motor.repartosValidos(holgado)
        assertTrue("Se esperaban varias soluciones", validos.size > 1)
        assertTrue(validos.all { motor.evaluar(holgado, it).valido })
    }

    @Test
    fun `el reparto se clasifica en cubiertos, parciales y sin nada`() {
        val res = motor.evaluar(casoPan, Allocation(mapOf("lia" to 2, "bruno" to 1, "nina" to 0)))
        assertTrue(res.cubiertos.containsAll(listOf("lia", "bruno")))
        assertTrue(res.sinNada.contains("nina"))
        assertTrue(res.parciales.isEmpty())
    }

    // ------------------------------------------------------------ decisiones

    private val casoMadera = BudgetCase(
        titulo = "Diez troncos",
        presupuesto = Inventory.of("madera" to 10),
        opciones = listOf(
            BuildOption("mesa", "Mesa", "-", Inventory.of("madera" to 8), Inventory.of("mesa" to 1), 5),
            BuildOption("silla", "Silla", "-", Inventory.of("madera" to 5), Inventory.of("silla" to 1), 3),
            BuildOption("cesta", "Cestas", "-", Inventory.of("madera" to 3), Inventory.of("cesta" to 3), 3)
        ),
        maxSelecciones = 2
    )

    @Test
    fun `una eleccion que cabe en el presupuesto se acepta`() {
        val res = motor.evaluarDecision(casoMadera, listOf("mesa"))
        assertTrue(res.alcanza)
        assertNotNull(res.restante)
        assertEquals(2, res.restante!!.cantidad("madera"))
        assertTrue(res.renuncias.containsAll(listOf("Silla", "Cestas")))
    }

    @Test
    fun `una eleccion que no cabe se rechaza y se explica`() {
        val res = motor.evaluarDecision(casoMadera, listOf("mesa", "silla"))
        assertFalse(res.alcanza)
        assertTrue(res.explicacion.contains("cuesta"))
    }

    @Test
    fun `no elegir nada tambien se explica`() {
        val res = motor.evaluarDecision(casoMadera, emptyList())
        assertFalse(res.alcanza)
        assertEquals(3, res.renuncias.size)
    }

    @Test
    fun `elegir mas cosas de las permitidas no vale`() {
        val res = motor.evaluarDecision(casoMadera, listOf("mesa", "silla", "cesta"))
        assertFalse(res.alcanza)
        assertTrue(res.mensaje.contains("demasiadas"))
    }

    @Test
    fun `hay varias decisiones posibles y ninguna incluye todo`() {
        val posibles = motor.decisionesPosibles(casoMadera)
        assertTrue(posibles.size > 1)
        assertTrue(posibles.none { it.size == casoMadera.opciones.size })
    }

    @Test
    fun `lo que se obtiene se acumula al elegir dos opciones`() {
        val res = motor.evaluarDecision(casoMadera, listOf("silla", "cesta"))
        assertTrue(res.alcanza)
        assertEquals(1, res.obtenido.cantidad("silla"))
        assertEquals(3, res.obtenido.cantidad("cesta"))
        assertEquals(2, res.restante!!.cantidad("madera"))
    }
}
