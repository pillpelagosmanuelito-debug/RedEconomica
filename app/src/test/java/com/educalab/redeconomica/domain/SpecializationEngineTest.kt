package com.educalab.redeconomica.domain

import com.educalab.redeconomica.data.seed.SeedResources
import com.educalab.redeconomica.domain.engine.SpecializationEngine
import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.Inventory
import com.educalab.redeconomica.domain.model.SpecializationPlan
import com.educalab.redeconomica.domain.model.ValleyPlace
import com.educalab.redeconomica.domain.model.WorkMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La lección de la especialización es un número, no un eslogan: repartir el
 * turno entre varias tareas produce menos. Aquí se comprueba ese número.
 */
class SpecializationEngineTest {

    private val motor = SpecializationEngine(SeedResources.PORID)

    private fun quien(id: String, produce: Map<String, Int>) = EconomicCharacter(
        id = id, nombre = id, oficio = "vecino", lugar = ValleyPlace.GRANJA,
        presentacion = "-", productividad = produce
    )

    private val lia = quien("lia", mapOf("manzana" to 6, "verdura" to 3))
    private val tomas = quien("tomas", mapOf("pan" to 5, "harina" to 4))

    @Test
    fun `especializarse produce toda la capacidad`() {
        val plan = SpecializationPlan(mapOf("lia" to "manzana"))
        val salida = motor.producir(listOf(lia), plan)
        assertEquals(6, salida.total.cantidad("manzana"))
    }

    @Test
    fun `hacer de todo reparte el turno y produce menos`() {
        val plan = SpecializationPlan(mapOf("lia" to null))
        val salida = motor.producir(listOf(lia), plan)
        assertEquals(3, salida.total.cantidad("manzana"))
        assertEquals(1, salida.total.cantidad("verdura"))
        assertEquals(WorkMode.DE_TODO, plan.modoDe("lia"))
    }

    @Test
    fun `quien no tiene tarea no produce nada`() {
        val salida = motor.producir(listOf(lia), SpecializationPlan.VACIO)
        assertTrue(salida.total.esVacio)
        assertEquals(WorkMode.DESCANSA, SpecializationPlan.VACIO.modoDe("lia"))
    }

    @Test
    fun `asignar una tarea que no sabe hacer no produce nada`() {
        val plan = SpecializationPlan(mapOf("lia" to "pan"))
        val salida = motor.producir(listOf(lia), plan)
        assertTrue(salida.total.esVacio)
        assertTrue(salida.lineas.first().comentario.contains("no sabe producir"))
    }

    @Test
    fun `dos especializados cubren un objetivo que de otro modo no sale`() {
        val objetivo = Inventory.of("manzana" to 6, "pan" to 5)
        val especializado = SpecializationPlan(mapOf("lia" to "manzana", "tomas" to "pan"))
        val deTodo = motor.planDeTodo(listOf(lia, tomas))
        assertTrue(motor.producir(listOf(lia, tomas), especializado).cumple(objetivo))
        assertFalse(motor.producir(listOf(lia, tomas), deTodo).cumple(objetivo))
    }

    @Test
    fun `la comparacion dice cual organiza mejor el trabajo`() {
        val comparacion = motor.comparar(
            listOf(lia, tomas),
            motor.planDeTodo(listOf(lia, tomas)),
            SpecializationPlan(mapOf("lia" to "manzana", "tomas" to "pan"))
        )
        assertTrue(comparacion.ganaB)
        assertFalse(comparacion.hayEmpate)
    }

    @Test
    fun `el mejor plan encontrado cumple el objetivo cuando existe`() {
        val objetivo = Inventory.of("manzana" to 6, "pan" to 5)
        val plan = motor.mejorPlan(listOf(lia, tomas), objetivo)
        assertTrue(motor.producir(listOf(lia, tomas), plan).cumple(objetivo))
    }

    @Test
    fun `planes que cumplen enumera todas las soluciones validas`() {
        val objetivo = Inventory.of("manzana" to 6)
        val planes = motor.planesQueCumplen(listOf(lia, tomas), objetivo)
        assertTrue(planes.isNotEmpty())
        assertTrue(planes.all { motor.producir(listOf(lia, tomas), it).cumple(objetivo) })
    }

    @Test
    fun `un objetivo imposible no devuelve ningun plan`() {
        val imposible = Inventory.of("manzana" to 99)
        assertTrue(motor.planesQueCumplen(listOf(lia, tomas), imposible).isEmpty())
    }

    @Test
    fun `lo que falta para el objetivo se calcula bien`() {
        val objetivo = Inventory.of("manzana" to 10)
        val salida = motor.producir(listOf(lia), SpecializationPlan(mapOf("lia" to "manzana")))
        assertEquals(4, salida.faltaPara(objetivo).cantidad("manzana"))
    }

    @Test
    fun `un objetivo ya cubierto no deja nada pendiente`() {
        val objetivo = Inventory.of("manzana" to 4)
        val salida = motor.producir(listOf(lia), SpecializationPlan(mapOf("lia" to "manzana")))
        assertTrue(salida.faltaPara(objetivo).esVacio)
    }

    @Test
    fun `una lista vacia de habitantes no produce nada y no revienta`() {
        val salida = motor.producir(emptyList(), SpecializationPlan.VACIO)
        assertTrue(salida.total.esVacio)
        assertTrue(salida.lineas.isEmpty())
    }
}
