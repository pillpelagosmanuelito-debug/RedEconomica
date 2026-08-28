package com.educalab.redeconomica.domain

import com.educalab.redeconomica.domain.engine.CooperationEngine
import com.educalab.redeconomica.domain.model.CooperationPlan
import com.educalab.redeconomica.domain.model.CooperationStage
import com.educalab.redeconomica.domain.model.EconomicCharacter
import com.educalab.redeconomica.domain.model.ValleyPlace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * En una cadena, lo que sale al final es lo que permite el eslabón más flojo.
 * Estas pruebas fijan esa regla y comprueban que cooperar mejora de verdad.
 */
class CooperationEngineTest {

    private val motor = CooperationEngine()

    private fun quien(id: String) = EconomicCharacter(
        id = id, nombre = id, oficio = "vecino", lugar = ValleyPlace.TALLER,
        presentacion = "-", productividad = emptyMap()
    )

    private val equipo = listOf(quien("nina"), quien("sofia"), quien("rita"))

    private val etapas = listOf(
        CooperationStage("cortar", "Cortar", 1, "-", mapOf("nina" to 5, "sofia" to 3, "rita" to 0)),
        CooperationStage("clavar", "Clavar", 2, "-", mapOf("sofia" to 5, "nina" to 2, "rita" to 0)),
        CooperationStage("subir", "Subir", 3, "-", mapOf("rita" to 4, "nina" to 1, "sofia" to 0))
    )

    private val planBueno = CooperationPlan(
        mapOf("nina" to "cortar", "sofia" to "clavar", "rita" to "subir")
    )

    @Test
    fun `el resultado es el minimo de las etapas`() {
        val salida = motor.ejecutar(equipo, etapas, planBueno, objetivo = 4)
        assertEquals(4, salida.resultado)
        assertTrue(salida.completado)
    }

    @Test
    fun `una etapa sin nadie corta la cadena`() {
        val plan = CooperationPlan(mapOf("nina" to "cortar", "sofia" to "clavar"))
        val salida = motor.ejecutar(equipo, etapas, plan, objetivo = 4)
        assertEquals(0, salida.resultado)
        assertFalse(salida.completado)
        assertTrue(salida.cuellosDeBotella.contains("subir"))
    }

    @Test
    fun `el motor senala cual es el eslabon mas flojo`() {
        val plan = CooperationPlan(
            mapOf("nina" to "clavar", "sofia" to "cortar", "rita" to "subir")
        )
        val salida = motor.ejecutar(equipo, etapas, plan, objetivo = 4)
        assertEquals(2, salida.resultado)
        assertTrue(salida.cuellosDeBotella.contains("clavar"))
    }

    @Test
    fun `trabajar por separado produce menos que coordinarse`() {
        val solos = motor.resultadoSinCooperar(equipo, etapas)
        val juntos = motor.ejecutar(equipo, etapas, planBueno, 4).resultado
        assertTrue(juntos > solos)
    }

    @Test
    fun `sumar gente a una etapa aumenta su capacidad`() {
        val plan = CooperationPlan(
            mapOf("nina" to "cortar", "sofia" to "cortar", "rita" to "subir")
        )
        val salida = motor.ejecutar(equipo, etapas, plan, objetivo = 1)
        assertEquals(8, salida.capacidadPorEtapa["cortar"])
    }

    @Test
    fun `hay mas de un reparto valido cuando el objetivo es holgado`() {
        val planes = motor.planesQueCumplen(equipo, etapas, objetivo = 1)
        assertTrue("Se esperaba más de un reparto válido", planes.size > 1)
    }

    @Test
    fun `no hay repartos validos si el objetivo es inalcanzable`() {
        assertTrue(motor.planesQueCumplen(equipo, etapas, objetivo = 99).isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un objetivo cero no es un trabajo valido`() {
        motor.ejecutar(equipo, etapas, planBueno, objetivo = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un trabajo sin etapas no se puede ejecutar`() {
        motor.ejecutar(equipo, emptyList(), planBueno, objetivo = 3)
    }

    @Test
    fun `la explicacion menciona lo que se consigue por separado`() {
        val salida = motor.ejecutar(equipo, etapas, planBueno, objetivo = 4)
        assertTrue(salida.explicacion.contains("separado"))
        assertTrue(salida.mejoraPorCooperar > 0)
    }
}
