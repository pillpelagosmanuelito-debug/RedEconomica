package com.educalab.redeconomica.domain

import com.educalab.redeconomica.data.seed.SeedResources
import com.educalab.redeconomica.domain.engine.OpportunityCostEngine
import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.ValleyPlace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * "Lo que dejas de hacer". La ventaja comparativa es la parte más delicada de
 * la app: aquí se fija con números pequeños y comprobables.
 */
class OpportunityCostEngineTest {

    private val motor = OpportunityCostEngine(SeedResources.PORID)

    private fun quien(id: String, produce: Map<String, Int>) = EconomicCharacter(
        id = id, nombre = id, oficio = "vecino", lugar = ValleyPlace.GRANJA,
        presentacion = "-", productividad = produce
    )

    private val lia = quien("lia", mapOf("manzana" to 6, "verdura" to 3))
    private val bruno = quien("bruno", mapOf("manzana" to 3, "verdura" to 3))

    @Test
    fun `dedicar todo el turno a una cosa cuesta toda la otra`() {
        val res = motor.costoDeOportunidad(lia, "manzana", "verdura", 6)
        assertEquals(3.0, res.cantidadRenunciadaExacta, 1e-9)
        assertEquals(3, res.cantidadRenunciadaRedondeada)
    }

    @Test
    fun `medio turno cuesta la mitad`() {
        val res = motor.costoDeOportunidad(lia, "manzana", "verdura", 3)
        assertEquals(1.5, res.cantidadRenunciadaExacta, 1e-9)
    }

    @Test
    fun `no producir nada no cuesta nada`() {
        val res = motor.costoDeOportunidad(lia, "manzana", "verdura", 0)
        assertEquals(0.0, res.cantidadRenunciadaExacta, 1e-9)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `no se puede elegir mas de lo que cabe en un turno`() {
        motor.costoDeOportunidad(lia, "manzana", "verdura", 99)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `no se puede elegir una cantidad negativa`() {
        motor.costoDeOportunidad(lia, "manzana", "verdura", -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `no se puede calcular el costo de algo que no sabe hacer`() {
        motor.costoDeOportunidad(lia, "pan", "verdura", 1)
    }

    @Test
    fun `si no sabe hacer lo otro no renuncia a nada`() {
        val soloFruta = quien("solo", mapOf("manzana" to 5))
        val res = motor.costoDeOportunidad(soloFruta, "manzana", "verdura", 5)
        assertEquals(0.0, res.cantidadRenunciadaExacta, 1e-9)
        assertTrue(res.explicacion.contains("no renuncia"))
    }

    @Test
    fun `las combinaciones posibles van de todo a nada`() {
        val puntos = motor.combinacionesPosibles(lia, "manzana", "verdura")
        assertEquals(7, puntos.size)
        assertEquals(0, puntos.first().cantidadA)
        assertEquals(3, puntos.first().cantidadB)
        assertEquals(6, puntos.last().cantidadA)
        assertEquals(0, puntos.last().cantidadB)
    }

    @Test
    fun `la ventaja absoluta la tiene quien produce mas`() {
        assertEquals("lia", motor.ventajaAbsoluta(lia, bruno, "manzana"))
        assertNull(motor.ventajaAbsoluta(lia, bruno, "verdura"))
    }

    @Test
    fun `la ventaja comparativa no la decide quien produce mas`() {
        // Lía es mejor o igual en las dos, pero pierde menos si va a la fruta.
        val res = motor.ventajaComparativa(lia, bruno, "manzana", "verdura")
        assertEquals("manzana", res.recomendadoParaA)
        assertEquals("verdura", res.recomendadoParaB)
        assertEquals(0.5, res.costoAEnUno, 1e-9)
        assertEquals(1.0, res.costoBEnUno, 1e-9)
    }

    @Test
    fun `cuando los costos son iguales el motor reconoce el empate`() {
        val otro = quien("otro", mapOf("manzana" to 4, "verdura" to 2))
        val res = motor.ventajaComparativa(lia, otro, "manzana", "verdura")
        assertTrue(res.empate)
        assertTrue(res.explicacion.contains("igual"))
    }

    @Test
    fun `si el segundo pierde menos, es el segundo quien debe producirlo`() {
        val rapido = quien("rapido", mapOf("manzana" to 8, "verdura" to 2))
        val res = motor.ventajaComparativa(lia, rapido, "manzana", "verdura")
        assertEquals("verdura", res.recomendadoParaA)
        assertEquals("manzana", res.recomendadoParaB)
    }
}
